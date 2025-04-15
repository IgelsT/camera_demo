//package ru.igels.camerastream02.network.notneed
//
//import android.media.MediaCodec
//import android.os.Handler
//import net.butterflytv.rtmp_client.RTMPMuxer
//import net.butterflytv.rtmp_client.RtmpClient
//import ru.igels.camerastream02.domain.eLog
//import ru.igels.camerastream02.domain.iLog
//import ru.igels.camerastream02.data.FrameQueues
//import ru.igels.camerastream02.network.rtmpclient.FlvMuxer
//import ru.igels.camerastream02.utilites.*
//
//class RtmpSender1 {
//
//    val logTag = "RtmpSender"
//    private val rtmpHandler: Handler = Utils.getThread("rtmpThread")
//    val mRTMPMuxer = RTMPMuxer()
//    val mRTMPClient = RtmpClient()
//    var mRtmpReady = false
//    var spsppsData = ByteArray(29)
//    private var width: Int = 0
//    private var height: Int = 0
//    private var fps: Int = 0
//    private var rtmpUrl = ""
//    var spsPpsIsSend = false
//
//    fun start(url: String, width: Int, height: Int, fps: Int) {
//        rtmpUrl = url
//        this.width = width
//        this.height = height
//
//        this.fps = fps
//
//        rtmpHandler.post {
//            startSender()
//        }
//    }
//
//    private fun startSender() {
//        rtmp1()
//    }
//
//    private fun rtmp1() {
//        try {
//            spsPpsIsSend = false
//            var url = "$rtmpUrl live=1"
//            url = "rtmp://camera.imile.ru/live/camera1 live=1"
//            iLog(logTag, "Connecting to $url")
////            mRTMPMuxer.open(url, width, height)
//            mRTMPClient.open(url, true)
//            mRtmpReady = true
//            sendRTMP1()
//        } catch (e: Exception) {
//            eLog(logTag, "Error rtmp connect")
//        }
//    }
//
//    private fun sendRTMP1() {
//        while (mRtmpReady) {
//            val frame = FrameQueues.mediaCodecFrames.take()
//            var result = -1
//            try {
//                if (frame.SpsPpS != null) {
//                    val pts = (frame.info.presentationTimeUs / 1000);
//                    if (!spsPpsIsSend) {
//                        val spsPpsHeader =
//                            FlvMuxer.muxSequenceHeader(frame.SpsPpS.first, frame.SpsPpS.second)
//                        val packet =
//                            FlvMuxer.muxFlvTag(
//                                listOf(spsPpsHeader),
//                                FlvMuxer.KeyFrame,
//                                true,
//                                pts,
//                            )
////                        result = mRTMPMuxer.writeVideo(packet,0,packet.size,pts)
//                        result = mRTMPClient.write(packet)
//                        spsPpsIsSend = true
//                    } else {
//                        if (frame.info.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) {
////                            mRTMPMuxer.writeVideo(spsppsData + frame.data, 0, frame.data.size+29, frame.timeStamp)
//                            val spsPpsHeader =
//                                FlvMuxer.muxSequenceHeader(frame.SpsPpS.first, frame.SpsPpS.second)
//                            val packet =
//                                FlvMuxer.muxFlvTag(
//                                    listOf(spsPpsHeader, frame.withoutStart()),
//                                    FlvMuxer.KeyFrame,
//                                    false,
//                                    pts,
//                                )
////                            result = mRTMPMuxer.writeVideo(packet,0,packet.size,pts)
//                            result = mRTMPClient.write(packet)
//                        } else {
//                            val packet =
//                                FlvMuxer.muxFlvTag(
//                                    listOf(frame.withoutStart()),
//                                    FlvMuxer.InterFrame,
//                                    false,
//                                    pts,
//                                )
////                            result = mRTMPMuxer.writeVideo(packet,0,packet.size,pts)
//                            result = mRTMPClient.write(packet)
////                            mRTMPMuxer.writeVideo(frame.data, 0, frame.data.size, frame.timeStamp)
//                        }
//                    }
//                    if (result == -1) {
//                    }
//                    //                    eLog(
////                        logTag,
////                        "Error write to stream " + Thread.currentThread().name
////                    )
//                }
//
//            } catch (e: Exception) {
//                eLog(logTag, e.message)
//            }
//        }
//    }
//
//    fun stop() {
//        mRtmpReady = false
//        mRTMPMuxer.close()
//    }
//
//    protected fun finalize() {
//        iLog(logTag, "Finalize codec class")
//        stop()
//        Utils.closeThread(rtmpHandler)
//    }
//}