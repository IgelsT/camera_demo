package ru.igels.camerastream02.network.notneed

import android.media.MediaCodec.BUFFER_FLAG_CODEC_CONFIG
import android.os.Handler
import ru.igels.camerastream02.data.AndroidInfoData
import ru.igels.camerastream02.data.appsettings.SettingsData
import ru.igels.camerastream02.domain.logger.eLog
import ru.igels.camerastream02.domain.logger.iLog
import ru.igels.camerastream02.data.FrameQueues
import ru.igels.camerastream02.network.notneed.rtmp.H264VideoFrame
import ru.igels.camerastream02.network.notneed.rtmp.RtmpConnectionListener
import ru.igels.camerastream02.network.notneed.rtmp.RtmpMuxer
import ru.igels.camerastream02.network.notneed.rtmp.Time
import ru.igels.camerastream02.utilities.*
import java.io.IOException
import java.util.concurrent.TimeUnit

class RtmpSender2 : RtmpConnectionListener {
    private var presentTimeUs: Long = 0
    var muxer: RtmpMuxer? = null
    private val rtmpHandler: Handler = Utils.getThread("rtmpThread")
    private val RECONNECT_INTERVAL = 10 // in sec


    val logTag = "RtmpSender"
    var mRtmpReady = false
    private var width: Int = 0
    private var height: Int = 0
    private var fps: Int = 0
    private var rtmpUrl = ""
           private var isStarted = false

    fun start(url: String, width: Int, height: Int, fps: Int) {
        rtmpUrl = url
        this.width = width
        this.height = height
        this.fps = fps
        if (presentTimeUs == 0L) {
            presentTimeUs = System.nanoTime() / 1000
        }
        isStarted = true
        rtmpHandler.post {
            connect()
        }
    }

    private fun connect() {
        if (!isStarted) return
        val settings = SettingsData.getInstance().settings
        try {
            iLog(logTag, "Connect to rtmp ${settings.baseUrl}")
            muxer = RtmpMuxer(
                settings.baseUrl,
                1935,
                Time { System.currentTimeMillis() })
            muxer?.setConnectTimeout(10000)
            muxer?.start(this, "live", settings.baseUrl, AndroidInfoData.getAndroidID())

        } catch (e: Exception) {
            eLog(logTag, "Error rtmp connect")
        }
    }

    fun rtmpSend() {
        while (mRtmpReady) {
//            fpsMeasure.measureFPS()
            val frame = FrameQueues.mediaCodecFrames.poll(1000, TimeUnit.MICROSECONDS)
            if(frame !=null && frame.SpsPpS != null) {
                val h264Frame = object :
                    H264VideoFrame {
                    override fun isHeader(): Boolean =
                        (frame.info.flags == BUFFER_FLAG_CODEC_CONFIG)
                    override fun getTimestamp(): Long = frame.info.presentationTimeUs
                    override fun getData(): ByteArray = frame.data
                    override fun isKeyframe(): Boolean = (frame.info.flags == 1)
                }
                try {
//                val time = measureTimeMillis {
                    muxer?.postVideo(h264Frame)
//                }
//                iLog(logTag, "Time to send $time ${frame.data.size}")
                } catch (e: Exception) {
                    eLog(logTag, e.message + " " + Thread.currentThread().name)
                    reconnectAfterTime()
                }
            }
        }
    }

    private fun reconnectAfterTime() {
        mRtmpReady = false
        muxer?.stop()
        muxer = null
        iLog(logTag, "Wait $RECONNECT_INTERVAL sec. to reconnect")
            rtmpHandler.postDelayed({
                connect()
            }, RECONNECT_INTERVAL * 1000L)

    }

    override fun onConnected() {
        iLog(logTag, "RTMP connected. " + Thread.currentThread().name)
        rtmpHandler.post {
            muxer!!.createStream(AndroidInfoData.getAndroidID())
        }
    }

    override fun onReadyToPublish() {
        iLog(logTag, "RTMP ready to publish " + Thread.currentThread().name)
        mRtmpReady = true
        rtmpHandler.post {
            rtmpSend()
        }
    }

    override fun onConnectionError(e: IOException) {
        eLog(logTag, "RTMP connection error " + e.message)
//        reconnectAfterTime()
    }

    fun stop() {
        isStarted = false
        mRtmpReady = false
        try {
            muxer?.stop()
            muxer = null
            iLog(logTag, "RTMP Stop")
        } catch (e: Exception) {
            eLog(logTag, "RTMP Stop error ${e.message}")
        }
    }

    protected fun finalize() {
        iLog(logTag, "Finalize codec class")
        stop()
        Utils.closeThread(rtmpHandler)
    }

}