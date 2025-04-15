package ru.igels.camerastream02.encoders

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.os.Build

object EncoderUtils {

    // Hardware encoders
    // - OMX.TI.DUCATI1.VIDEO.H264E
    // - OMX.qcom.7x30.video.encoder.avc
    // - OMX.Nvidia.h264.encoder
    // - OMX.SEC.AVC.Encoder
    private val allhardwareEncoders =
        arrayListOf("omx.ti", "omx.qcom", "omx.nvidia", "omx.sec", "c2.mtk", "omx.mtk")
    private val allSoftEncoders = arrayListOf("omx.google")

    private var allBitrateModes: ArrayList<Int> = arrayListOf()

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

    fun getCodecInfo(currentType: String): MutableList<DeviceCodecInfo> {
        val codecsByFormat: MutableList<DeviceCodecInfo> = arrayListOf()
        val numCodecs = MediaCodecList.getCodecCount()
        for (i in 0 until numCodecs) {
            val codecInfo = MediaCodecList.getCodecInfoAt(i)
//            printAll(codecInfo)
            if (codecInfo.isEncoder && currentType in codecInfo.supportedTypes) {
                val typeName = codecInfo.supportedTypes.findLast { el -> el == currentType }
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
                        if(cap.videoCapabilities != null) {
                            codec.supportedBitRateRange[0] = cap.videoCapabilities.bitrateRange.lower
                            codec.supportedBitRateRange[1] = cap.videoCapabilities.bitrateRange.upper
                        }
                        if(cap.audioCapabilities != null) {
                            codec.supportedBitRateRange[0] = cap.audioCapabilities.bitrateRange.lower
                            codec.supportedBitRateRange[1] = cap.audioCapabilities.bitrateRange.upper
                        }
                    }
                    codecsByFormat.add(codec)
                }
            }
        }
        return codecsByFormat
    }

    private fun isSoftEncoder(name: String): Boolean {
        val name2l = name.lowercase()
        return (name2l.startsWith("omx.google.")
                || name2l.startsWith("omx.ffmpeg.")
                || name2l.startsWith("omx.sec.") && name2l.contains(".sw.")
                || name2l == "omx.qcom.video.decoder.hevcswvdec"
                || name2l.startsWith("c2.android.")
                || name2l.startsWith("c2.google.")
                || !name2l.startsWith("omx.") && !name2l.startsWith("c2."))
    }

    private fun isSoftEncoderOld(name: String): Boolean {
        for (n in allSoftEncoders) {
            if (name.startsWith(n, true)) return true
        }
        return false
    }
}