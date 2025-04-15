package ru.igels.camerastream02.encoders

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import ru.igels.camerastream02.data.MediaCodecFrame
import ru.igels.camerastream02.domain.logger.dLog
import ru.igels.camerastream02.domain.logger.eLog
import ru.igels.camerastream02.domain.logger.iLog
import ru.igels.camerastream02.utilities.Utils
import java.nio.ByteBuffer

class VideoEncoder {

    private val logTag = "MediaVideoCodec"
    private var mCodec: MediaCodec? = null
    private val inputHandler: Handler = Utils.getThread("VideoCodecInThread")
    private val outputHandler: Handler = Utils.getThread("VideoCodecOutThread")

    //    private val codecType = MediaFormat.MIMETYPE_VIDEO_H263
    //    private val codecType = MediaFormat.MIMETYPE_VIDEO_HEVC
    private val H264_MIME = "video/avc"
    private val H265_MIME = "video/hevc"
//    private val codecType = MediaFormat.MIMETYPE_VIDEO_AVC

    private val iframeInterval = 2
    private var mMediaFormat: MediaFormat? = null
    private var mMediaInfo = MediaCodec.BufferInfo()
    private var isCoding = false

    //Colors under api 23
    // int COLOR_FormatYUV420Planar = 19, COLOR_FormatYUV420SemiPlanar = 21;

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
        codecsByFormat = EncoderUtils.getCodecInfo(H264_MIME)
        currentCodec = codecsByFormat.indexOfFirst { el -> el.isHardware }
        if (currentCodec == -1) currentCodec = 0
        currentColor = selectColor(codecsByFormat[currentCodec].supportedColors)
        SpsPps = null
        isCoding = false

        val result = setupCodec()
        return if (isCoding) {
            inputHandler.postDelayed({
                requestFrame()
            }, 1000)

            outputHandler.post {
                processOutput()
            }
            true
        } else false
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

    private fun selectColor(supportedColors: MutableList<Int>): Int {
        var color = supportedColors.find { it == 19 }
        if(color != null) return color

        color = supportedColors.find { it == 21 }
        if(color != null) return color

        return supportedColors[0]
    }

    private fun setupCodec() {
        presentTimeUs = System.nanoTime() / 1000
        try {
            mCodec = MediaCodec.createByCodecName(codecsByFormat[currentCodec].name)
            val format = MediaFormat.createVideoFormat(H264_MIME, width, height)
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0)
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, currentColor)
            format.setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                format.setInteger(MediaFormat.KEY_CAPTURE_RATE, fps)
            }
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iframeInterval)
            var bmode = "default"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && codecsByFormat[0].supportedBitrateModes.size > 0) {
                format.setInteger(
                    MediaFormat.KEY_BITRATE_MODE,
                    codecsByFormat[0].supportedBitrateModes[0]
                )
                bmode = codecsByFormat[0].supportedBitrateModes[0].toString()
            }
            format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
//            format.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileExtended);
//            format.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel62);
            // {frame-rate=30, max-input-size=0, height=640, color-format=19, width=480, bitrate=1228800, mime=video/avc, i-frame-interval=2}
            mCodec?.configure(
                format,
                null,
                null,
                MediaCodec.CONFIGURE_FLAG_ENCODE
            )
//            mMediaFormat = mCodec?.outputFormat
//            mCodec?.setCallback(mediaCodecCallBack)
            mCodec?.start()
            iLog(
                logTag,
                "Media codec started on $width x $height. Bitrate mode $bmode, bitrate: $bitrate, fps $fps"
            )
            isCoding = true
        } catch (e: Exception) {
            mCodec = null
            eLog(logTag, "Error start encoder ${e.message}")
        }
    }

    private fun requestFrame() {
        requestFrameTime = System.currentTimeMillis()
        val frame = inCallback(currentColor)
        if (frame != null) processInput(frame)
        if (isCoding) {
            var nextTime = frameInterval - (System.currentTimeMillis() - requestFrameTime)
            nextTime = if (nextTime >= 0) nextTime else 0
            inputHandler.postDelayed({
                requestFrame()
            }, nextTime)
        }
    }

    private fun processInput(data: ByteBuffer) {
        try {
            val index = mCodec!!.dequeueInputBuffer(1000)
            if (index >= 0) {
                var inputData: ByteBuffer? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    inputData = mCodec?.getInputBuffer(index)
                else {
                    val buffers = mCodec?.inputBuffers
                    buffers?.let { inputData = it[index] }
                }
                if(inputData == null) return

//                if(inputData!!.capacity() > data.limit())
//                    inputData!!.limit(data.limit())
//                else data.limit(inputData!!.limit())
                inputData!!.limit(data.limit())
                inputData?.put(data)
                val pts = System.nanoTime() / 1000 - presentTimeUs
                mCodec?.queueInputBuffer(index, 0, data.limit(), pts,0)
                frameIndex++
            }
        } catch (e: Exception) {
            eLog(logTag, "Error mediacodec input buffer! ${e.message}")
        }
    }

    private fun computePresentationTime(frameIndex: Int): Long {
        return 132 + frameIndex * 1000000 / this.fps*1L
    }

    private fun processOutput() {
        while (isCoding) {
            try {
                val mI = MediaCodec.BufferInfo()
                val index = mCodec?.dequeueOutputBuffer(mI, 10000) ?: -1
                if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
//                    eLog(logTag, "no output buffer")
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    dLog(logTag, "output format changed ${mCodec?.outputFormat}")
                    mMediaFormat = mCodec?.outputFormat
                }
                if (index >= 0) {
                    var outputBuffer: ByteBuffer? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        outputBuffer = mCodec?.getOutputBuffer(index)
                    else {
                        val buffers = mCodec?.outputBuffers
                        buffers?.let { outputBuffer = it[index] }
                    }

                    outputBuffer?.let {
                        if (SpsPps == null) SpsPps = decodeSpsPpsFromBuffer(it)
                        outCallback(MediaCodecFrame(it, mI, mMediaFormat, SpsPps))
                    }
                    mCodec?.releaseOutputBuffer(index, false)
                }
            } catch (e: Exception) {
                eLog(logTag, "Error mediacodec output buffer!")
            }
        }
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
        while (i < length - 4) {
            if (csd[i].toInt() == 0 && csd[i + 1].toInt() == 0 && csd[i + 2].toInt() == 0 && csd[i + 3].toInt() == 1) {
                dividers[dividerIndex] = i
                dividerIndex++
                i += 3
                if (dividerIndex == 3) break
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

    private fun printAll(codecInfo: MediaCodecInfo) {
        if (codecInfo.isEncoder) {
            iLog(logTag, "-- Codec -- ${codecInfo.name}")
        }
    }

    @Synchronized
    fun stop() {
        try {
            isCoding = false
            if(mCodec != null) {
                mCodec?.stop()
                iLog(logTag, "Media codec stopped.")
            }
        } catch (e: Exception) {
            eLog(logTag, "Media codec stop error ${e.message}.")
        } finally {
            mCodec = null
        }

    }

    protected fun finalize() {
        iLog(logTag, "Finalize codec class")
        stop()
        Utils.closeThread(inputHandler)
        Utils.closeThread(outputHandler)
    }
}