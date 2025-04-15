package ru.igels.camerastream02.network.rtmpclient

import java.nio.ByteBuffer

object FlvMuxer {
    const val KeyFrame = 1
    const val InterFrame = 2

    const val logTag = "Flv muxer"

    fun muxSequenceHeader(sps: ByteArray, pps: ByteArray): ByteArray {
        // 5bytes sps/pps header:
        //      configurationVersion, AVCProfileIndication, profile_compatibility,
        //      AVCLevelIndication, lengthSizeMinusOne
        // 3bytes size of sps:
        //      numOfSequenceParameterSets, sequenceParameterSetLength(2B)
        // Nbytes of sps.
        //      sequenceParameterSetNALUnit
        // 3bytes size of pps:
        //      numOfPictureParameterSets, pictureParameterSetLength
        // Nbytes of pps:
        //      pictureParameterSetNALUnit
        // decode the SPS:
        // @see: 7.3.2.1.1, H.264-AVC-ISO_IEC_14496-10-2012.pdf, page 62
        val header = ByteArray(11 + sps.size + pps.size)
        // @see: Annex A Profiles and levels, H.264-AVC-ISO_IEC_14496-10.pdf, page 205
        //      Baseline profile profile_idc is 66(0x42).
        //      Main profile profile_idc is 77(0x4d).
        //      Extended profile profile_idc is 88(0x58).
        // generate the sps/pps header
        // 5.3.4.2.1 Syntax, H.264-AVC-ISO_IEC_14496-15.pdf, page 16
        // configurationVersion
        header[0] = 0x01.toByte()
        // AVCProfileIndication
        header[1] = sps[1]
        // profile_compatibility
        header[2] = 0x00.toByte()
        // AVCLevelIndication
        header[3] = sps[3]
        // lengthSizeMinusOne, or NAL_unit_length, always use 4bytes size,
        // so we always set it to 0x03.
        header[4] = 0x03.toByte()
        // sps
        // 5.3.4.2.1 Syntax, H.264-AVC-ISO_IEC_14496-15.pdf, page 16
        // numOfSequenceParameterSets, always 1
        header[5] = 0x01.toByte()
        // sequenceParameterSetLength
        header[7] = sps.size.toByte()
        // sequenceParameterSetNALUnit
        System.arraycopy(sps, 0, header, 8, sps.size)
        // pps
        // 5.3.4.2.1 Syntax, H.264-AVC-ISO_IEC_14496-15.pdf, page 16
        // numOfPictureParameterSets, always 1
        header[8 + sps.size] = 0x01.toByte()
        // pictureParameterSetLength
        header[10 + sps.size] = pps.size.toByte()
        // pictureParameterSetNALUnit
        System.arraycopy(pps, 0, header, 10 + sps.size + 1, pps.size)
        return header
    }

    fun muxFlvTag(
        frames: List<ByteArray>,
        frame_type: Int,
        isHeader: Boolean,
        pts: Int
    ): ByteArray {
        // for h264 in RTMP video payload, there is 5bytes header:
        //      1bytes, FrameType | CodecID
        //      1bytes, AVCPacketType
        //      3bytes, CompositionTime, the cts.
        // @see: E.4.3 Video Tags, video_file_format_spec_v10_1.pdf, page 78
        val size = if (isHeader) 5 + frames[0].size
        else 5 + frames.fold(0) { acc, el -> acc + el.size + 4 }
        val result = ByteArray(size)
        // @see: E.4.3 Video Tags, video_file_format_spec_v10_1.pdf, page 78
        // Frame Type, Type of video frame.
        // CodecID, Codec Identifier.
        // set the rtmp header

        //frame_type
        // const val KeyFrame = 1
        // const val InterFrame = 2
        result[0] = ((frame_type shl 4) or 7).toByte()

        // AVCPacketType
        // SequenceHeader = 0
        // NALU = 1
        if (!isHeader) result[1] = 1

        // CompositionTime
        // pts = dts + cts, or
        // cts = pts - dts.
        // where cts is the header in rtmp video packet payload header.
        val i_pts = pts.toInt()
//        val cts = 0
        result[2] = (i_pts shr 16).toByte()
        result[3] = (i_pts shr 8).toByte()
        result[4] = i_pts.toByte()
//        System.arraycopy(intToByteArray(dts), 0, result, 2, 3)

        if (isHeader) {
            System.arraycopy(frames[0], 0, result, 5, frames[0].size)
        } else {
            var pos = 5
            for (frame in frames) {
                System.arraycopy(intToByteArray(frame.size), 0, result, pos, 4)
                pos += 4
                System.arraycopy(frame, 0, result, pos, frame.size)
                pos += frame.size
            }
        }
        return result
    }

    private fun  intToByteArray(value: Int): ByteArray {
        val buffer = ByteBuffer.allocate(4)
        buffer.putInt(value)
        return buffer.array()
    }

    fun muxNaluHeader(frame: SrsFlvFrameBytes): SrsFlvFrameBytes? {
        val nalu_hdr = SrsFlvFrameBytes()
        nalu_hdr.data = ByteBuffer.allocate(4)
        nalu_hdr.size = 4
        // 5.3.4.2.1 Syntax, H.264-AVC-ISO_IEC_14496-15.pdf, page 16
        // lengthSizeMinusOne, or NAL_unit_length, always use 4bytes size
        val NAL_unit_length = frame.size

        // mux the avc NALU in "ISO Base Media File Format"
        // from H.264-AVC-ISO_IEC_14496-15.pdf, page 20
        // NALUnitLength
        nalu_hdr.data!!.putInt(NAL_unit_length)

        // reset the buffer.
        nalu_hdr.data!!.rewind()
        return nalu_hdr
    }
}