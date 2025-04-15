package ru.igels.camerastream02.network
//
//import android.media.MediaCodec
//import android.os.Handler
//import com.github.faucamp.simplertmp.DefaultRtmpPublisher
//import com.github.faucamp.simplertmp.RtmpHandler
//import ru.igels.camerastream02.domain.eLog
//import ru.igels.camerastream02.domain.iLog
//import ru.igels.camerastream02.data.FrameQueues
//import ru.igels.camerastream02.network.rtmpclient.FlvMuxer
//import ru.igels.camerastream02.network.rtmpclient.Muxer
//import ru.igels.camerastream02.utilities.Utils
//import java.io.IOException
//import java.net.SocketException
//
//
//class RtmpSender : RtmpHandler.RtmpListener {
//
//    private var mRtmpReady: Boolean = false
//    private val logTag = "RtmpSender4"
//    private val rtmpHandler: Handler = Utils.getThread("rtmpThread")
//    private var isStarted: Boolean = false
//    private val RECONNECT_INTERVAL = 10 // in sec
//
//    var mRtmpHandler: RtmpHandler = RtmpHandler(this)
//
//    var publisher: DefaultRtmpPublisher? = null
//
//    private var width: Int = 0
//    private var height: Int = 0
//    private var fps: Int = 0
//    private var rtmpUrl = ""
//    private var muxer: Muxer? = Muxer()
//    var spsPpsIsSend = false
////    var pts = 0L
//
//    @Synchronized
//    fun start(url: String, width: Int, height: Int, fps: Int) {
//
//        val params = checkParams(url, width, height, fps)
//        if (publisher != null && params) return
//        if (publisher != null) stop()
//
//        isStarted = true
//        iLog(logTag, "RTMP Start")
//        connect()
//    }
//
//    private fun checkParams(url: String, width: Int, height: Int, fps: Int): Boolean {
//        var result = true
//        if (this.rtmpUrl != url) {
//            this.rtmpUrl = url
//            result = false
//        }
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
//        return result
//    }
//
//    private fun connect() {
//        spsPpsIsSend = false
//        publisher = DefaultRtmpPublisher(mRtmpHandler);
//        iLog(logTag, String.format("connecting to RTMP server by url=%s\n", rtmpUrl))
//        if (publisher?.connect(rtmpUrl) == true) {
//            publisher?.setVideoResolution(width, height)
//            publisher?.publish("live")
//        }
//    }
//
//    private fun sendRTMP() {
//        mRtmpReady = true
//        while (mRtmpReady) {
//            val frame = FrameQueues.mediaCodecFrames.take()
//            if (frame.SpsPpS != null) {
//                val pts = (frame.info.presentationTimeUs / 1000).toInt();
//                val cts = 10
//                var keyType = FlvMuxer.InterFrame
//                if (frame.info.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) {
////                    keyType = FlvMuxer.KeyFrame
//                    iLog(logTag, "KeyFrame")
//                    sendSPS(frame.SpsPpS, pts)
//                }
//                val packet = FlvMuxer.muxFlvTag(listOf(frame.withoutStart()), keyType, false, cts)
//                publisher?.publishVideoData(packet, packet.size, pts)
//
//                //                if (!spsPpsIsSend) {
////                    sendSPS(frame.SpsPpS, pts)
////                    spsPpsIsSend = true
////                } else {
////                    val cts = 10
////                    var keyType = FlvMuxer.InterFrame
////                    if (frame.info.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) {
////                        keyType = FlvMuxer.KeyFrame
////                        iLog(logTag, "KeyFrame")
////                    }
////                    val packet =
////                        FlvMuxer.muxFlvTag(
////                            listOf(frame.withoutStart()), keyType, false, cts,
////                        )
////                    publisher?.publishVideoData(packet, packet.size, pts)
////                }
//            }
//        }
//    }
//
//    private fun sendSPS(sps: Pair<ByteArray, ByteArray>, pts: Int) {
//        val cts = 10
//        val spsPpsHeader = FlvMuxer.muxSequenceHeader(sps.first, sps.second)
//        val packet =
//            FlvMuxer.muxFlvTag(
//                listOf(spsPpsHeader), FlvMuxer.KeyFrame, true, cts,
//            )
//        publisher?.publishVideoData(packet, packet.size, pts)
//    }
//
//    private fun reconnectAfterTime() {
//        mRtmpReady = false
//        publisher = null
//        if (isStarted) {
//            iLog(logTag, "Wait $RECONNECT_INTERVAL sec. to reconnect")
//            rtmpHandler.postDelayed({
//                connect()
//            }, RECONNECT_INTERVAL * 1000L)
//        }
//    }
//
//
//    @Synchronized
//    fun stop() {
//        isStarted = false
//        mRtmpReady = false
//        try {
//            publisher?.close()
//            publisher = null
//            iLog(logTag, "RTMP Stop")
//        } catch (e: Exception) {
//            eLog(logTag, "RTMP Stop error ${e.message}")
//        }
//    }
//
//    protected fun finalize() {
//        iLog(logTag, "Finalize RTMPSender class")
//        stop()
//        Utils.closeThread(rtmpHandler)
//    }
//
//    override fun onRtmpConnecting(msg: String?) {}
//
//    override fun onRtmpConnected(msg: String?) {
//        iLog(logTag, "onConnectionSuccessRtmp")
//        rtmpHandler.post {
//            sendRTMP()
//        }
//    }
//
//    override fun onRtmpVideoStreaming() {}
//
//    override fun onRtmpAudioStreaming() {
//        TODO("Not yet implemented")
//    }
//
//    override fun onRtmpStopped() {
//        iLog(logTag, "onRtmpStopped")
//    }
//
//    override fun onRtmpDisconnected() {
//        iLog(logTag, "onRtmpDisconnected")
//        reconnectAfterTime()
//    }
//
//    override fun onRtmpVideoFpsChanged(fps: Double) {
//        iLog(logTag, "RTMP fps changed: $fps")
//    }
//
//    override fun onRtmpVideoBitrateChanged(bitrate: Double) {
//        iLog(logTag, "RTMP bitrate changed: $bitrate")
//    }
//
//    override fun onRtmpAudioBitrateChanged(bitrate: Double) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onRtmpSocketException(e: SocketException?) {
//        eLog(logTag, "onRtmpSocketException ${e?.message} ${e?.cause}")
//        mRtmpReady = false
//        try {
//            publisher?.close()
//        } catch (e: Exception) {
//            eLog(logTag, "error close connection ${e.message} ${e.cause}")
//            reconnectAfterTime()
//        }
//    }
//
//    override fun onRtmpIOException(e: IOException?) {
//        eLog(logTag, "onRtmpIOException ${e?.message}")
//        mRtmpReady = false
//        try {
//            publisher?.close()
//        } catch (e: Exception) {
//            eLog(logTag, "error close connection ${e.message} ${e.cause}")
//            reconnectAfterTime()
//        }
//
//    }
//
//    override fun onRtmpIllegalArgumentException(e: IllegalArgumentException?) {
//        eLog(logTag, "onRtmpIllegalArgumentException ${e?.message}")
//    }
//
//    override fun onRtmpIllegalStateException(e: IllegalStateException?) {
//        eLog(logTag, "onRtmpIllegalStateException ${e?.message}")
//    }
//    // server gone
//    // onRtmpSocketException sendto failed: EPIPE (Broken pipe) android.system.ErrnoException: sendto failed: EPIPE (Broken pipe)
//    // onRtmpDisconnected
//
//    // server not response
//    // onRtmpIOException failed to connect to /62.141.119.28 (port 1935) after 3000ms: isConnected failed: ECONNREFUSED (Connection refused)
//    // onRtmpDisconnected
//
//    // Wifi gone
//    // onRtmpSocketException sendto failed: EPIPE (Broken pipe) android.system.ErrnoException: sendto failed: EPIPE (Broken pipe)
//    //onRtmpDisconnected
//
//    // Wifi off connect
//    // onRtmpIOException failed to connect to /62.141.119.28 (port 1935) after 3000ms: connect failed: ENETUNREACH (Network is unreachable)
//    // onRtmpDisconnected
//}