package ru.igels.camerastream02.encoders

import android.media.*
import android.media.AudioFormat.CHANNEL_IN_MONO
import android.media.AudioFormat.ENCODING_PCM_16BIT
import android.os.Build
import android.os.Handler
import ru.igels.camerastream02.data.MediaCodecFrameAudio
import ru.igels.camerastream02.domain.logger.dLog
import ru.igels.camerastream02.domain.logger.eLog
import ru.igels.camerastream02.domain.logger.iLog
import ru.igels.camerastream02.utilities.Utils
import ru.igels.camerastream02.utilities.toFile
import java.nio.ByteBuffer


class AudioEncoder {
    private val logTag = "MediaAudioCodec"

    private val AAC_MIME = "audio/mp4a-latm"
    private val VORBIS_MIME = "audio/ogg"
    private val OPUS_MIME = "audio/opus"
    private val SAMPLE_RATE = 44100
    private val CHANNEL_CONFIG = CHANNEL_IN_MONO
    private val AUDIO_FORMAT =  ENCODING_PCM_16BIT
    private var minBufferSize = 0
    private val BIT_RATE = 64000
    val SAMPLES_PER_FRAME = 1024 // AAC, bytes/frame/channel
    val FRAMES_PER_BUFFER = 25

    private val AUDIO_SOURCES = intArrayOf(
        MediaRecorder.AudioSource.MIC,  // 1
        MediaRecorder.AudioSource.DEFAULT,  // 0
        MediaRecorder.AudioSource.CAMCORDER,  // 5
        MediaRecorder.AudioSource.VOICE_COMMUNICATION,
        MediaRecorder.AudioSource.VOICE_RECOGNITION
    )

    private val prevOutputPTSUs = 0L
    private var mMediaFormat: MediaFormat? = null

//    private val codecType = MediaFormat.MIMETYPE_AUDIO_AAC
    private var codecsByFormat: MutableList<DeviceCodecInfo> = arrayListOf()

    private var mCodec: MediaCodec? = null
    private var audioRecord: AudioRecord? = null
    private val inputHandler: Handler = Utils.getThread("AudioCodecInThread")
    private val outputHandler: Handler = Utils.getThread("AudioCodecOutThread")
    private var outCallback: (MediaCodecFrameAudio) -> Unit = {}

    private var isCoding = false

    @Synchronized
    fun start(outCallback: (MediaCodecFrameAudio) -> Unit): Boolean {
        val params = checkParams(outCallback)
        if (mCodec != null && audioRecord != null && params) return true
        if (mCodec != null) stop()

        codecsByFormat = EncoderUtils.getCodecInfo(AAC_MIME)
        isCoding = false

        val resultCodec = setupCodec()
        val resultAudio = setupAudioInput()
        return if (resultCodec && resultAudio) {
            inputHandler.postDelayed({
                processInput()
            }, 1000)

            outputHandler.post {
                processOutput()
            }
            true
        } else {
            stop()
            false
        }
    }

    private fun checkParams(
        outCallback: (MediaCodecFrameAudio) -> Unit
    ): Boolean {
        var result = true
//        if (this.width != width) {
//            this.width = width
//            result = false
//        }
//        if (this.height != height) {
//            this.height = height
//            result = false
//        }
//        if (this.fps != fps) {
//            this.fps = fps
//            result = false
//        }
//        if (this.bitrate != bitrate) {
//            this.bitrate = bitrate
//            result = false
//        }
//        if (this.inCallback != inCallback) {
//            this.inCallback = inCallback
//            result = false
//        }
        if (this.outCallback != outCallback) {
            this.outCallback = outCallback
            result = false
        }
        return result
    }

    private fun setupCodec(): Boolean {
        try {
            mCodec = MediaCodec.createByCodecName(codecsByFormat[0].name)

            val audioFormat = MediaFormat.createAudioFormat(AAC_MIME, SAMPLE_RATE, if (CHANNEL_CONFIG == CHANNEL_IN_MONO) 1 else 2)
            audioFormat.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
            audioFormat.setInteger(
                MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC
            )
            audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, CHANNEL_CONFIG)
            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE)
            audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, if (CHANNEL_CONFIG == CHANNEL_IN_MONO) 1 else 2)
            audioFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, SAMPLE_RATE)

            mCodec?.configure(
                audioFormat,
                null,
                null,
                MediaCodec.CONFIGURE_FLAG_ENCODE
            )
            mCodec?.start()
            iLog(
                logTag,
                "codec started $audioFormat"
            )
            isCoding = true
            return true
        } catch (e: Exception) {
            mCodec = null
            eLog(logTag, "Error start encoder ${e.message}")
            return false
        }
    }

    private fun setupAudioInput(): Boolean {
        minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        try {
            audioRecord =
                AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    minBufferSize
                )
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord = null
                return false
            }

//            audioRecord!!.setRecordPositionUpdateListener(object: OnRecordPositionUpdateListener {
//                override fun onMarkerReached(recorder: AudioRecord?) {
//                    TODO("Not yet implemented")
//                }
//
//                override fun onPeriodicNotification(recorder: AudioRecord?) {
//                    TODO("Not yet implemented")
//                }
//
//            })
            audioRecord?.startRecording()
            iLog(logTag, "Start audio recording")
            return true
        } catch (e: SecurityException) {
            eLog(logTag, "No permission to record audio!")
        } catch (e: Exception) {
            audioRecord = null
        }
        return false
    }

    private fun processInput() {
        val data = ByteBuffer.allocateDirect(minBufferSize)
        var readBytes: Int
        while (isCoding) {
            try {
                data.clear()
                readBytes = audioRecord!!.read(data, minBufferSize)
                if (readBytes <= 0) continue

                val pts = getPTSUs()
                data.position(readBytes)
                data.flip()
                toFile(data.array())
                continue
                val index = mCodec!!.dequeueInputBuffer(1000)
                if (index < 0) continue

                var inputData: ByteBuffer? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    inputData = mCodec?.getInputBuffer(index)
                else {
                    val buffers = mCodec?.inputBuffers
                    buffers?.let { inputData = it[index] }
                }
                inputData?.clear()
                inputData?.limit(data.limit())
                inputData?.put(data)
                mCodec?.queueInputBuffer(index, 0, data.limit(), pts, 0)
            } catch (e: Exception) {
                eLog(logTag, "Error mediacodec input buffer! ${e.message}")
            }
        }
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
                        outCallback(
                            MediaCodecFrameAudio(
                                it,
                                mI,
                                SAMPLE_RATE,
                                if (CHANNEL_CONFIG == CHANNEL_IN_MONO) 1 else 2
                            )
                        ) // mMediaFormat, SpsPps))
                    }
                    mCodec?.releaseOutputBuffer(index, false)
                }
            } catch (e: Exception) {
                eLog(logTag, "Error mediacodec output buffer!")
            }
        }
    }

    private fun getPTSUs(): Long {
        var result = System.nanoTime() / 1000L
        // presentationTimeUs should be monotonic
        // otherwise muxer fail to write
        if (result < prevOutputPTSUs) result += prevOutputPTSUs - result
        return result
    }

    @Synchronized
    fun stop() {
        try {
            isCoding = false
            if (audioRecord != null) {
                iLog(logTag, "Audio recording stopped.")
                audioRecord?.stop()
                audioRecord?.release()
            }
            if (mCodec != null) {
                mCodec?.stop()
                iLog(logTag, "codec stopped.")
            }
        } catch (e: Exception) {
            eLog(logTag, "codec stop error ${e.message}.")
        } finally {
            mCodec = null
            audioRecord = null
        }

    }

    protected fun finalize() {
        iLog(logTag, "Finalize codec class")
        stop()
        Utils.closeThread(inputHandler)
        Utils.closeThread(outputHandler)
    }
}