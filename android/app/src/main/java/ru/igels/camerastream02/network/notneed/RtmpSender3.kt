package ru.igels.camerastream02.network.notneed

import android.media.MediaFormat
import android.os.Handler
import com.pedro.rtmp.rtmp.RtmpClient
import com.pedro.rtmp.utils.ConnectCheckerRtmp
import ru.igels.camerastream02.domain.logger.eLog
import ru.igels.camerastream02.domain.logger.iLog
import ru.igels.camerastream02.data.FrameQueues
import ru.igels.camerastream02.utilities.Utils
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

class RtmpSender3 : ConnectCheckerRtmp {

    private val logTag = "RtmpSender"
    private var reconnectInterval = 10 // in sec
    private val DEFAULT_RECONNECT_INTERVAL = 10 // in sec
    private val REJECTED_RECONNECT_INTERVAL = 30 // in sec

    private val setLog: Boolean = false
    private var presentTimeUs: Long = 0
    private val rtmpHandler: Handler = Utils.getThread("rtmpThread")
    private var mRtmpReady: Boolean = false
    private var isStarted: Boolean = false
    private var mRtmpClient = RtmpClient(this)
    private var mMediaFormat: MediaFormat? = null

    private var width: Int = 0
    private var height: Int = 0
    private var fps: Int = 0
    private var rtmpUrl = ""
    private var oldSps: ByteBuffer? = null
    private var oldPps: ByteBuffer? = null
    private var spsPpsIsSend = false
    private var audioInfoSend = false

    fun start(url: String, width: Int, height: Int, fps: Int) {
        if (url == rtmpUrl && width == this.width && height == this.height && fps == this.fps && isStarted) return
        stop()
        rtmpUrl = url
        this.width = width
        this.height = height
        this.fps = fps
        if (presentTimeUs == 0L) {
            presentTimeUs = System.nanoTime() / 1000
        }
        isStarted = true
        reconnectInterval = DEFAULT_RECONNECT_INTERVAL
        rtmpHandler.post {
            startSender()
        }
    }

    private fun startSender() {
        mRtmpClient = RtmpClient(this)
        mRtmpClient.setLogs(setLog);
//        mRtmpClient.setVideoResolution(width, height);
        mRtmpClient.setFps(fps);
        mRtmpClient.setOnlyVideo(true)
        mRtmpClient.connect(rtmpUrl)
//        mMediaFormat?.let { sendSPSandPPS(it) }
    }

    private fun sendRTMP() {
        var packets = 0
        var packetsSize = 0L
        spsPpsIsSend = false
        audioInfoSend = false
        while (mRtmpReady) {
            val frame = FrameQueues.mediaCodecFrames.poll(1000, TimeUnit.MICROSECONDS)
            try {
                if (frame?.SpsPpS == null) continue
                if (!spsPpsIsSend) {
                    spsPpsIsSend = true
                    sendSPSandPPS1(frame.SpsPpS)//                    sendSPSandPPS(frame.format!!)
                }
                mRtmpClient.sendVideo(ByteBuffer.wrap(frame.data), frame.info)
            } catch (e: Exception) {
                eLog(logTag, e.message ?: "")
            }
            if (FrameQueues.mediaCodecFramesAudio.isEmpty()) continue
            val audioFrame = FrameQueues.mediaCodecFramesAudio.poll(1000, TimeUnit.MICROSECONDS)
            try {
                if (frame?.SpsPpS == null) continue
                if (!audioInfoSend) {
                    audioInfoSend = true
                    mRtmpClient.setAudioInfo(audioFrame.sampleRate, audioFrame.channels == 2)
                }
                mRtmpClient.sendAudio(ByteBuffer.wrap(audioFrame.data), audioFrame.info)
            } catch (e: Exception) {
                eLog(logTag, e.message ?: "")
            }
        }
        mRtmpClient.disconnect()
    }

    private fun sendSPSandPPS1(ppsSps: kotlin.Pair<ByteArray, ByteArray>) {
        oldSps = ByteBuffer.wrap(ppsSps.first)
        oldPps = ByteBuffer.wrap(ppsSps.second)
        if (oldSps != null && oldPps != null)
            mRtmpClient.setVideoInfo(oldSps!!, oldPps!!, null)
    }

    private fun sendSPSandPPS(mediaFormat: MediaFormat) {
        oldSps = mediaFormat.getByteBuffer("csd-0")
        oldPps = mediaFormat.getByteBuffer("csd-1")
        if (oldSps != null && oldPps != null)
            mRtmpClient.setVideoInfo(oldSps!!, oldPps!!, null)
    }

    fun stop() {
        isStarted = false
        mRtmpReady = false
        mMediaFormat = null
        spsPpsIsSend = false
        try {
            mRtmpClient.disconnect()
        } catch (e: Exception) {
            eLog(logTag, "Error close rtmp ${e.message}")
        }
    }

    protected fun finalize() {
        iLog(logTag, "Finalize RTMPSender class")
        isStarted = false
        stop()
        Utils.closeThread(rtmpHandler)
    }

    override fun onAuthErrorRtmp() {
        TODO("Not yet implemented")
    }

    override fun onAuthSuccessRtmp() {
        TODO("Not yet implemented")
    }

    override fun onConnectionFailedRtmp(reason: String) {
        iLog(logTag, "onConnectionFailedRtmp on $reason")
        if (reason.contains("Rejected")) reconnectInterval = REJECTED_RECONNECT_INTERVAL
        mRtmpReady = false
        try {
            mRtmpClient.disconnect()
        } catch (e: Exception) {
            eLog(logTag, "Error close rtmp ${e.message}")
        }
        if (isStarted) {
            iLog(logTag, "Wait $reconnectInterval sec. to reconnect")
            rtmpHandler.postDelayed({
                startSender()
            }, reconnectInterval * 1000L)
        }
    }

    override fun onConnectionStartedRtmp(rtmpUrl: String) {
        iLog(logTag, "onConnectionStartedRtmp on $rtmpUrl")
    }

    override fun onConnectionSuccessRtmp() {
        iLog(logTag, "onConnectionSuccessRtmp")
        reconnectInterval = DEFAULT_RECONNECT_INTERVAL
        mRtmpReady = true
        rtmpHandler.post {
            sendRTMP()
        }
    }

    override fun onDisconnectRtmp() {
        iLog(logTag, "onDisconnectRtmp")
    }

    override fun onNewBitrateRtmp(bitrate: Long) {
//        iLog(logTag, "onNewBitrateRtmp $bitrate")
    }
}