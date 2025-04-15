package ru.igels.camerastream02.encoders

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.annotation.RequiresApi
import ru.igels.camerastream02.domain.logger.eLog
import ru.igels.camerastream02.domain.logger.iLog
import ru.igels.camerastream02.data.MediaCodecFrame
import ru.igels.camerastream02.utilities.Utils
import java.nio.ByteBuffer

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class VideoEncoderAsync {

    private val logTag = "MediaCodec"
    private var mCodec: MediaCodec? = null
    private val codecType = MediaFormat.MIMETYPE_VIDEO_AVC
    private val codecHandler: Handler = Utils.getThread("CodecThread")
//    private val inputHandler: Handler = Utils.getThread("CodecInThread")
    //    private val codecType = MediaFormat.MIMETYPE_VIDEO_H263
    //    private val codecType = MediaFormat.MIMETYPE_VIDEO_HEVC
    private val iframeInterval = 2
    private var mMediaFormat: MediaFormat? = null
    private var isCoding = false

    //Colors under api 23
    // int COLOR_FormatYUV420Planar = 19, COLOR_FormatYUV420SemiPlanar = 21;
    private var allBitrateModes: ArrayList<Int> = arrayListOf()

    // Hardware encoders
    // - OMX.TI.DUCATI1.VIDEO.H264E
    // - OMX.qcom.7x30.video.encoder.avc
    // - OMX.Nvidia.h264.encoder
    // - OMX.SEC.AVC.Encoder
    private val allhardwareEncoders =
        arrayListOf("omx.ti", "omx.qcom", "omx.nvidia", "omx.sec", "c2.mtk", "omx.mtk")
    private val allSoftEncoders = arrayListOf("omx.google")

    private var codecsByFormat: MutableList<DeviceCodecInfo> = arrayListOf()
    private var currentCodec = 0
    private var currentColor = 0

    private var width: Int = 0
    private var height: Int = 0
    private var fps: Int = 0
    private var bitrate: Int = 0
    private var inCallback: (format: Int) -> ByteBuffer? = { null }
    private var outCallback: (MediaCodecFrame) -> Unit = {}

    private var frameInterval: Int = 0
    private var requestFrameTime: Long = 0
    private var SpsPps: Pair<ByteArray, ByteArray>? = null
    private var presentTimeUs: Long = 0L
    private var frameIndex = 1
//    private val fpsMeasure = FPSMeasure()

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            allBitrateModes = arrayListOf(
                MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ, // 0 Constant quality mode
                MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR, // 1 Variable bitrate mode
                MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR, // 2 Constant bitrate mode
                //        MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR_FD // 3 Constant bitrate mode with frame drops
            )
        }
    }

    @Synchronized
    fun start(
        width: Int,
        height: Int,
        fps: Int,
        bitrate: Int,
        inCallback: (Int) -> ByteBuffer?,
        outCallback: (MediaCodecFrame) -> Unit
    ): Boolean {
        val params = checkParams(width, height, fps, bitrate, inCallback, outCallback)
        if (mCodec != null && params) return true
        if (mCodec != null) stop()

        frameInterval = 1000 / fps
        codecsByFormat = arrayListOf()
        getCodecInfo(codecType)
        currentCodec = codecsByFormat.indexOfFirst { el -> el.isHardware }
        if (currentCodec == -1) currentCodec = 0
        currentColor = codecsByFormat[currentCodec].supportedColors[0]
        SpsPps = null
        isCoding = false
        requestFrameTime = System.currentTimeMillis()
        codecHandler.post {
            setupCodec()
        }
        return true

    }

    private fun checkParams(
        width: Int,
        height: Int,
        fps: Int,
        bitrate: Int,
        inCallback: (Int) -> ByteBuffer?,
        outCallback: (MediaCodecFrame) -> Unit
    ): Boolean {
        var result = true
        if (this.width != width) {
            this.width = width
            result = false
        }
        if (this.height != height) {
            this.height = height
            result = false
        }
        if (this.fps != fps) {
            this.fps = fps
            result = false
        }
        if (this.bitrate != bitrate) {
            this.bitrate = bitrate
            result = false
        }
        if (this.inCallback != inCallback) {
            this.inCallback = inCallback
            result = false
        }
        if (this.outCallback != outCallback) {
            this.outCallback = outCallback
            result = false
        }
        return result
    }

    private fun setupCodec(): Boolean {
        presentTimeUs = System.nanoTime() / 1000
        try {
            mCodec = MediaCodec.createByCodecName(codecsByFormat[currentCodec].name)
            val format = MediaFormat.createVideoFormat(codecType, width, height)
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0)
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, currentColor)
            format.setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iframeInterval)
//            format.setInteger(MediaFormat.KEY_DURATION,1000000/fps)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                format.setInteger(MediaFormat.KEY_CAPTURE_RATE, fps)
            }
            var bmode = "default"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && codecsByFormat[0].supportedBitrateModes.size > 0) {
                format.setInteger(
                    MediaFormat.KEY_BITRATE_MODE,
                    codecsByFormat[0].supportedBitrateModes[0]
                )
                bmode = codecsByFormat[0].supportedBitrateModes[0].toString()
            }

//            format.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline);
//            format.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel22);
            // {frame-rate=30, max-input-size=0, height=640, color-format=19, width=480, bitrate=1228800, mime=video/avc, i-frame-interval=2}
            mCodec?.configure(
                format,
                null,
                null,
                MediaCodec.CONFIGURE_FLAG_ENCODE
            )
//            mMediaFormat = mCodec?.outputFormat
            mCodec?.setCallback(mediaCodecCallBack)
            mCodec?.start()
            iLog(
                logTag,
                "Media codec started on $width x $height. Bitrate mode $bmode, bitrate: $bitrate, fps $fps"
            )
            isCoding = true
            return true
        } catch (e: Exception) {
            mCodec = null
            eLog(logTag, "Error start encoder ${e.message}")
            return false
        }
    }

    private val mediaCodecCallBack = object : MediaCodec.Callback() {
        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            val nextTime = frameInterval - (System.currentTimeMillis() - requestFrameTime)
            if (nextTime > 0) Thread.sleep(nextTime)
            requestFrameTime = System.currentTimeMillis()
            try {
                val data = inCallback(currentColor) ?: ByteBuffer.allocate(1)
                val inputData = mCodec?.getInputBuffer(index)
                inputData?.clear()
                inputData?.limit(data.limit())
                inputData?.put(data)
                val pts = (System.nanoTime() / 1000) - presentTimeUs
                mCodec?.queueInputBuffer(
                    index,
                    0,
                    data.limit(),
                    computePresentationTime(frameIndex),
                    0
                )
                frameIndex++
            } catch (e: Exception) {
                eLog(logTag, "Error mediacodec input buffer! ${e.message}")
            }
        }

        override fun onOutputBufferAvailable(
            codec: MediaCodec,
            index: Int,
            info: MediaCodec.BufferInfo
        ) {
            try{
                val outputBuffer = mCodec?.getOutputBuffer(index)
                outputBuffer?.let {
                    if (SpsPps == null) SpsPps = decodeSpsPpsFromBuffer(it)
                    outCallback(MediaCodecFrame(it, info, mMediaFormat, SpsPps))
                }
                mCodec?.releaseOutputBuffer(index, false)
//                fpsMeasure.measureFPS()
            } catch (e: Exception) {
                eLog(logTag, "Error mediacodec output buffer!")
            }
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            eLog(logTag, "Error mediacodec ${e.message}")
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            mMediaFormat = format
//            setVideoBitrateOnFly(bitrate)
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun setVideoBitrateOnFly(bitrate: Int) {
        if (mCodec != null) {
            this.bitrate = bitrate
            val bundle = Bundle()
            bundle.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, bitrate)
            try {
                mCodec?.setParameters(bundle)
            } catch (e: IllegalStateException) {
                eLog(logTag, "encoder need be running ${e}")
            }
        }
    }

    private fun computePresentationTime(frameIndex: Int): Long {
        return 132 + frameIndex * 1000000 / this.fps*1L
    }

    private fun decodeSpsPpsFromBuffer(
        buff: ByteBuffer,
    ): Pair<ByteArray, ByteArray>? {

        val buff = buff.duplicate()
        val length = buff.limit()
        val csd = ByteArray(length)
        buff[csd, 0, length]
        buff.rewind()
        var i = 0
        val dividers = intArrayOf(-1,-1, length)
        var dividerIndex = 0
//        var spsIndex = -1
//        var ppsIndex = -1
        while (i < length - 4) {
            if (csd[i].toInt() == 0 && csd[i + 1].toInt() == 0 && csd[i + 2].toInt() == 0 && csd[i + 3].toInt() == 1) {
                dividers[dividerIndex] = i
                dividerIndex++
                i += 3
                if (dividerIndex == 3) break
//                if (spsIndex == -1) {
//                    spsIndex = i
//                } else {
//                    ppsIndex = i
//                    break
//                }
            }
            i++
        }
        if (dividers[0] != -1 && dividers[1] != -1) {
            val sps = ByteArray(dividers[1] - dividers[0] - 4)
            val pps = ByteArray(dividers[2] - dividers[1] - 4)
            System.arraycopy(csd, dividers[0] + 4, sps, 0, dividers[1] - dividers[0] - 4)
            System.arraycopy(csd, dividers[1] + 4, pps, 0, dividers[2] - dividers[1] - 4)
            iLog(logTag, String.format("Found SPS PPS in h264 frame, sps=%dB, pps=%dB", sps.size, pps.size))
            return Pair(sps, pps)
        }
        return null
    }

    private fun getCodecInfo(currentType: String) {
        val numCodecs = MediaCodecList.getCodecCount()
        for (i in 0 until numCodecs) {
            val codecInfo = MediaCodecList.getCodecInfoAt(i)
//            printAll(codecInfo)
            if (codecInfo.isEncoder && currentType in codecInfo.supportedTypes) {
                val typeName = codecInfo.supportedTypes.findLast { el -> el == codecType }
                if (typeName != null) {
                    val codec = DeviceCodecInfo(codecInfo.name, !isSoftEncoder(codecInfo.name))
                    val cap = codecInfo.getCapabilitiesForType(typeName)
                    for (color in cap.colorFormats) {
                        if (color == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar
                            || color == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
                        ) codec.supportedColors.add(0, color)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        for (mode in allBitrateModes) {
                            if (cap.encoderCapabilities.isBitrateModeSupported(mode))
                                codec.supportedBitrateModes.add(mode)
                        }
                        codec.supportedBitRateRange[0] = cap.videoCapabilities.bitrateRange.lower
                        codec.supportedBitRateRange[1] = cap.videoCapabilities.bitrateRange.upper
                    }
                    codecsByFormat.add(codec)
                }
            }
        }
    }

    private fun printAll(codecInfo: MediaCodecInfo) {
        if (codecInfo.isEncoder) {
            iLog(logTag, "-- Codec -- ${codecInfo.name}")
        }
    }

    private fun isSoftEncoder(name: String): Boolean {
        for (n in allSoftEncoders) {
            if (name.startsWith(n, true)) return true
        }
        return false
    }

    @Synchronized
    fun stop() {
        try {
            isCoding = false
            mCodec?.stop()
            iLog(logTag, "Media codec stopped.")
        } catch (e: Exception) {
            eLog(logTag, "Media codec stop error ${e.message}.")
        } finally {
            mCodec = null
        }

    }

    protected fun finalize() {
        iLog(logTag, "Finalize codec class")
        stop()
    }
}