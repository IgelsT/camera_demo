package ru.igels.camerastream02.network.rtmpclient

import android.media.MediaCodec
import ru.igels.camerastream02.domain.logger.eLog
import ru.igels.camerastream02.domain.logger.iLog
import java.nio.ByteBuffer
import java.util.*
import kotlin.experimental.and

/**
 * E.4.1 FLV Tag, page 75
 */
private object SrsCodecFlvTag {
    const val Reserved = 0  // set to the zero to reserved, for array map.
    const val Audio = 8     // 8 = audio
    const val Video = 9     // 9 = video
    const val Script = 18   // 18 = script data
}

// E.4.3.1 VIDEODATA
// CodecID UB [4]
// Codec Identifier. The following values are defined:
private object SrsCodecVideo {
    const val Reserved = 0 // set to the zero to reserved, for array map.
    const val Reserved1 = 1
    const val Reserved2 = 9
    const val Disabled = 8 // for user to disable video, for example, use pure audio hls.
    const val SorensonH263 = 2
    const val ScreenVideo = 3
    const val On2VP6 = 4
    const val On2VP6WithAlphaChannel = 5
    const val ScreenVideoVersion2 = 6
    const val AVC = 7
}

private object SrsCodecVideoAVCFrame {
    const val Reserved = 0     // set to the zero to reserved, for array map.
    const val Reserved1 = 6
    const val KeyFrame = 1
    const val InterFrame = 2
    const val DisposableInterFrame = 3
    const val GeneratedKeyFrame = 4
    const val VideoInfoFrame = 5
}

private object SrsCodecVideoAVCType {
    const val Reserved = 3 // set to the max value to reserved, for array map.
    const val SequenceHeader = 0
    const val NALU = 1
    const val SequenceHeaderEOF = 2
}

/**
 * the muxed flv frame.
 */
class SrsFlvFrame {
    // the tag bytes.
    var flvTag: SrsAllocator.Allocation? = null

    // the codec type for audio/aac and video/avc for instance.
    var avc_aac_type = 0

    // the frame type, keyframe or not.
    var frame_type = 0

    // the tag type, audio, video or data.
    var type = 0

    // the dts in ms, tbn is 1000.
    var dts = 0
    val isKeyFrame: Boolean
        get() = isVideo && frame_type == SrsCodecVideoAVCFrame.KeyFrame
    val isSequenceHeader: Boolean
        get() = avc_aac_type == 0
    val isVideo: Boolean
        get() = type == SrsCodecFlvTag.Video
    val isAudio: Boolean
        get() = type == SrsCodecFlvTag.Audio
}

/**
 * Table 7-1 – NAL unit type codes, syntax element categories, and NAL unit type classes
 * H.264-AVC-ISO_IEC_14496-10-2012.pdf, page 83.
 */
private object SrsAvcNaluType {
    // Unspecified
    const val Reserved = 0

    // Coded slice of a non-IDR picture slice_layer_without_partitioning_rbsp( )
    const val NonIDR = 1

    // Coded slice data partition A slice_data_partition_a_layer_rbsp( )
    const val DataPartitionA = 2

    // Coded slice data partition B slice_data_partition_b_layer_rbsp( )
    const val DataPartitionB = 3

    // Coded slice data partition C slice_data_partition_c_layer_rbsp( )
    const val DataPartitionC = 4

    // Coded slice of an IDR picture slice_layer_without_partitioning_rbsp( )
    const val IDR = 5

    // Supplemental enhancement information (SEI) sei_rbsp( )
    const val SEI = 6

    // Sequence parameter set seq_parameter_set_rbsp( )
    const val SPS = 7

    // Picture parameter set pic_parameter_set_rbsp( )
    const val PPS = 8

    // Access unit delimiter access_unit_delimiter_rbsp( )
    const val AccessUnitDelimiter = 9

    // End of sequence end_of_seq_rbsp( )
    const val EOSequence = 10

    // End of stream end_of_stream_rbsp( )
    const val EOStream = 11

    // Filler data filler_data_rbsp( )
    const val FilterData = 12

    // Sequence parameter set extension seq_parameter_set_extension_rbsp( )
    const val SPSExt = 13

    // Prefix NAL unit prefix_nal_unit_rbsp( )
    const val PrefixNALU = 14

    // Subset sequence parameter set subset_seq_parameter_set_rbsp( )
    const val SubsetSPS = 15

    // Coded slice of an auxiliary coded picture without partitioning slice_layer_without_partitioning_rbsp( )
    const val LayerWithoutPartition = 19

    // Coded slice extension slice_layer_extension_rbsp( )
    const val CodedSliceExt = 20
}

/**
 * the search result for annexb.
 */
private class SrsAnnexbSearch {
    var nb_start_code = 0
    var match = false
}

class SrsFlvFrameBytes {
    var data: ByteBuffer? = null
    var size = 0

    internal constructor() {}
    internal constructor(buff: ByteBuffer) {
        val size = buff.capacity()
        val bytes = ByteArray(size)
        buff[bytes]
        data = ByteBuffer.wrap(bytes)
        this.size = size
    }
}

class SrsAllocator @JvmOverloads constructor(
    private val individualAllocationSize: Int,
    initialAllocationCount: Int = 0
) {
    inner class Allocation(size: Int) {
        private val data: ByteArray
        private var size: Int

        init {
            data = ByteArray(size)
            this.size = 0
        }

        fun array(): ByteArray {
            return data
        }

        fun size(): Int {
            return size
        }

        fun appendOffset(offset: Int) {
            size += offset
        }

        fun clear() {
            size = 0
        }

        fun put(b: Byte) {
            data[size++] = b
        }

        fun put(b: Byte, pos: Int) {
            var pos = pos
            data[pos++] = b
            size = if (pos > size) pos else size
        }

        fun put(s: Short) {
            put(s.toByte())
//            put((s ushr 8).toByte())
        }

        fun put(i: Int) {
            put(i.toByte())
            put((i ushr 8).toByte())
            put((i ushr 16).toByte())
            put((i ushr 24).toByte())
        }

        fun put(bs: ByteArray) {
            System.arraycopy(bs, 0, data, size, bs.size)
            size += bs.size
        }
    }

    @Volatile
    private var availableSentinel: Int
    private var availableAllocations: Array<Allocation?>
    /**
     * Constructs an instance with some [Allocation]s created up front.
     *
     *
     *
     * @param individualAllocationSize The length of each individual [Allocation].
     * @param initialAllocationCount The number of allocations to create up front.
     */
    /**
     * Constructs an instance without creating any [Allocation]s up front.
     *
     * @param individualAllocationSize The length of each individual [Allocation].
     */
    init {
        availableSentinel = initialAllocationCount + 10
        availableAllocations = arrayOfNulls(availableSentinel)
        for (i in 0 until availableSentinel) {
            availableAllocations[i] = Allocation(individualAllocationSize)
        }
    }

    @Synchronized
    fun allocate(size: Int): Allocation? {
        for (i in 0 until availableSentinel) {
            if (availableAllocations[i]!!.size() >= size) {
                val ret = availableAllocations[i]
                availableAllocations[i] = null
                return ret
            }
        }
        return Allocation(if (size > individualAllocationSize) size else individualAllocationSize)
    }

    @Synchronized
    fun release(allocation: Allocation) {
        allocation.clear()
        for (i in 0 until availableSentinel) {
            if (availableAllocations[i]!!.size() == 0) {
                availableAllocations[i] = allocation
                return
            }
        }
        if (availableSentinel + 1 > availableAllocations.size) {
            availableAllocations =
                Arrays.copyOf(availableAllocations, availableAllocations.size * 2)
        }
        availableAllocations[availableSentinel++] = allocation
    }
}

class Muxer {

    private val logTag = "Muxer"

    private var h264_sps: ByteBuffer? = null
    private var h264_pps: ByteBuffer? = null
    private val annexb = SrsAnnexbSearch()
    private var h264_sps_changed = false
    private var h264_pps_changed = false
    private var h264_sps_pps_changed = false
    private var h264_sps_pps_sent = false
    private var needToFindKeyFrame = true
    private val seq_hdr = SrsFlvFrameBytes()
    private val sps_hdr = SrsFlvFrameBytes()
    private val sps_bb = SrsFlvFrameBytes()
    private val pps_hdr = SrsFlvFrameBytes()
    private val pps_bb = SrsFlvFrameBytes()
    private var video_tag: SrsAllocator.Allocation? = null
    private val VIDEO_ALLOC_SIZE = 128 * 1024
    private val mVideoAllocator = SrsAllocator(VIDEO_ALLOC_SIZE)
    private val ipbs = ArrayList<SrsFlvFrameBytes>()

    init {
        iLog(logTag, "INIT")
    }

    private fun resetSpsPps() {
        if (null != h264_sps) {
            Arrays.fill(h264_sps!!.array(), 0x00.toByte())
            h264_sps!!.clear()
        }
        if (null != h264_pps) {
            Arrays.fill(h264_pps!!.array(), 0x00.toByte())
            h264_pps!!.clear()
        }
    }


    fun writeVideoSample(bb: ByteBuffer, bi: MediaCodec.BufferInfo): SrsFlvFrame? {
        if (bi.size < 4) return null;
//        resetSpsPps()
        //            mVideoAllocator.release(frame.flvTag!!)
//
        val pts = (bi.presentationTimeUs / 1000);
        val dts = pts;
        var type = SrsCodecVideoAVCFrame.InterFrame
        val frame = demuxAnnexb(bb, bi, true);
        val nal_unit_type = (frame!!.data!!.get(0) and 0x1f).toInt()
        if (nal_unit_type == 5) {
            type = SrsCodecVideoAVCFrame.KeyFrame
        } else if (nal_unit_type == SrsAvcNaluType.SPS || nal_unit_type == SrsAvcNaluType.PPS) {
            val frame_pps = demuxAnnexb(bb, bi, false);
            frame.size = frame.size - frame_pps!!.size - 4;  // 4 ---> 00 00 00 01 pps
            if (!frame.data!!.equals(h264_sps)) {
                val sps = ByteArray(frame.size)
                frame.data!!.get(sps);
                h264_sps_changed = true;
                h264_sps = ByteBuffer.wrap(sps);
//                    writeH264SpsPps(dts, pts);
            }
//
            val frame_sei = demuxAnnexb(bb, bi, false);
            if (frame_sei!!.size > 0) {
                if (SrsAvcNaluType.SEI == (frame_sei.data!!.get(0) and 0x1f).toInt())
                    frame_pps.size = frame_pps.size - frame_sei.size - 3;// 3 ---> 00 00 01 SEI
            }

            if (!frame_pps.data!!.equals(h264_pps)) {
                val pps = ByteArray(frame_pps.size);
                frame_pps.data!!.get(pps);
                h264_pps_changed = true;
                h264_pps = ByteBuffer.wrap(pps);
                //writeH264SpsPps(dts, pts);
            }


            if (h264_sps_changed || h264_pps_changed) {
                h264_sps_pps_changed = true;
                return writeH264SpsPps(dts.toInt(), pts.toInt());
            }
            return null;
        } else if (nal_unit_type != SrsAvcNaluType.NonIDR) {
            return null;
        }

        ipbs.clear();
        if (type == SrsCodecVideoAVCFrame.KeyFrame && h264_sps_pps_changed) {
            //prepend SPS\PPS to IDR
            val sps_frame = SrsFlvFrameBytes(h264_sps!!);
            val pps_frame = SrsFlvFrameBytes(h264_pps!!);

            ipbs.add(muxNaluHeader(sps_frame)!!);
            ipbs.add(sps_frame);
            ipbs.add(muxNaluHeader(pps_frame)!!);
            ipbs.add(pps_frame);
            h264_sps_pps_changed = false;
            iLog(
                logTag,
                String.format(
                    "prepend key frame SPS/PPS. DTS: %d, SPS/PPS size: %d/%d, IDR size: %d",
                    dts,
                    sps_frame.size,
                    pps_frame.size,
                    frame.size
                )
            );
        }

        ipbs.add(muxNaluHeader(frame)!!);
        ipbs.add(frame);

        //writeH264SpsPps(dts, pts);
        return writeH264IpbFrame(ipbs, type, dts.toInt(), pts.toInt());
    }

    fun clearAllocation(flvTag: SrsAllocator.Allocation) {
        mVideoAllocator.release(flvTag)
    }

    private fun writeH264IpbFrame(
        frames: ArrayList<SrsFlvFrameBytes>,
        type: Int,
        dts: Int,
        pts: Int
    ): SrsFlvFrame? {
        // when sps or pps not sent, ignore the packet.
        // @see https://github.com/simple-rtmp-server/srs/issues/203
        if (!h264_sps_pps_sent) {
            return null
        }
        video_tag = muxFlvTag(frames, type, SrsCodecVideoAVCType.NALU, dts, pts)

        val frame = SrsFlvFrame()
        frame.flvTag = video_tag!!
        frame.type = SrsCodecFlvTag.Video
        frame.dts = dts
        frame.frame_type = type
        frame.avc_aac_type = SrsCodecVideoAVCType.NALU
        return frame
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

    fun demuxAnnexb(
        bb: ByteBuffer,
        bi: MediaCodec.BufferInfo,
        isOnlyChkHeader: Boolean
    ): SrsFlvFrameBytes? {
        val tbb = SrsFlvFrameBytes()
        if (bb.position() < bi.size - 4) {
            // each frame must prefixed by annexb format.
            // about annexb, @see H.264-AVC-ISO_IEC_14496-10.pdf, page 211.
            val tbbsc = if (isOnlyChkHeader) searchStartcode(bb, bi) else searchAnnexb(bb, bi)
            // tbbsc.nb_start_code always 4 , after 00 00 00 01
            if (!tbbsc!!.match || tbbsc.nb_start_code < 3) {
                eLog(logTag, "annexb not match.")
            } else {
                // the start codes.
                for (i in 0 until tbbsc.nb_start_code) {
                    bb.get()
                }
                // find out the frame size.
                tbb.data = bb.slice()
                tbb.size = bi.size - bb.position()
            }
        }
        return tbb
    }

    private fun searchStartcode(bb: ByteBuffer, bi: MediaCodec.BufferInfo): SrsAnnexbSearch? {
        annexb.match = false
        annexb.nb_start_code = 0
        if (bi.size - 4 > 0) {
            if (bb[0].toInt() == 0x00 && bb[1].toInt() == 0x00 && bb[2].toInt() == 0x00 && bb[3].toInt() == 0x01) {
                // match N[00] 00 00 00 01, where N>=0
                annexb.match = true
                annexb.nb_start_code = 4
            } else if (bb[0].toInt() == 0x00 && bb[1].toInt() == 0x00 && bb[2].toInt() == 0x01) {
                // match N[00] 00 00 01, where N>=0
                annexb.match = true
                annexb.nb_start_code = 3
            }
        }
        return annexb
    }

    private fun searchAnnexb(bb: ByteBuffer, bi: MediaCodec.BufferInfo): SrsAnnexbSearch? {
        annexb.match = false
        annexb.nb_start_code = 0
        for (i in bb.position() until bi.size - 4) {
            // not match.
            if (bb[i].toInt() != 0x00 || bb[i + 1].toInt() != 0x00) {
                continue
            }
            // match N[00] 00 00 01, where N>=0
            if (bb[i + 2].toInt() == 0x01) {
                annexb.match = true
                annexb.nb_start_code = i + 3 - bb.position()
                break
            }
            // match N[00] 00 00 00 01, where N>=0
            if (bb[i + 2].toInt() == 0x00 && bb[i + 3].toInt() == 0x01) {
                annexb.match = true
                annexb.nb_start_code = i + 4 - bb.position()
                break
            }
        }
        return annexb
    }

    private fun writeH264SpsPps(dts: Int, pts: Int): SrsFlvFrame? {
        // when sps or pps changed, update the sequence header,
        // for the pps maybe not changed while sps changed.
        // so, we must check when each video ts message frame parsed.
        if (h264_sps_pps_sent && !h264_sps_changed && !h264_pps_changed) {
            return null
        }

        // when not got sps/pps, wait.
        if (h264_pps == null || h264_sps == null) {
            return null
        }

        // h264 raw to h264 packet.
        val frames = ArrayList<SrsFlvFrameBytes>()
        muxSequenceHeader(h264_sps!!, h264_pps!!, dts, pts, frames)

        // h264 packet to flv packet.
        val frame_type = SrsCodecVideoAVCFrame.KeyFrame
        val avc_packet_type = SrsCodecVideoAVCType.SequenceHeader
        video_tag = muxFlvTag(frames, frame_type, avc_packet_type, dts, pts)

        // the timestamp in rtmp message header is dts.
//        writeRtmpPacket(
//            SrsCodecFlvTag.Video,
//            dts,
//            frame_type,
//            avc_packet_type,
//            video_tag!!
//        )

        val frame = SrsFlvFrame()
        frame.flvTag = video_tag!!
        frame.type = SrsCodecFlvTag.Video
        frame.dts = dts
        frame.frame_type = frame_type
        frame.avc_aac_type = avc_packet_type

        // reset sps and pps.
        h264_sps_changed = false
        h264_pps_changed = false
        h264_sps_pps_sent = true
        iLog(
            logTag, String.format(
                "flv: h264 sps/pps sent, sps=%dB, pps=%dB",
                h264_sps!!.array().size, h264_pps!!.array().size
            )
        )
        return frame
    }

    fun muxSequenceHeader(
        sps: ByteBuffer, pps: ByteBuffer, dts: Int, pts: Int,
        frames: ArrayList<SrsFlvFrameBytes>
    ) {
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
        if (seq_hdr.data == null) {
            seq_hdr.data = ByteBuffer.allocate(5)
            seq_hdr.size = 5
        }
        seq_hdr.data!!.rewind()
        // @see: Annex A Profiles and levels, H.264-AVC-ISO_IEC_14496-10.pdf, page 205
        //      Baseline profile profile_idc is 66(0x42).
        //      Main profile profile_idc is 77(0x4d).
        //      Extended profile profile_idc is 88(0x58).
        val profile_idc = sps[1]
        //u_int8_t constraint_set = frame[2];
        val level_idc = sps[3]

        // generate the sps/pps header
        // 5.3.4.2.1 Syntax, H.264-AVC-ISO_IEC_14496-15.pdf, page 16
        // configurationVersion
        seq_hdr.data!!.put(0x01.toByte())
        // AVCProfileIndication
        seq_hdr.data!!.put(profile_idc)
        // profile_compatibility
        seq_hdr.data!!.put(0x00.toByte())
        // AVCLevelIndication
        seq_hdr.data!!.put(level_idc)
        // lengthSizeMinusOne, or NAL_unit_length, always use 4bytes size,
        // so we always set it to 0x03.
        seq_hdr.data!!.put(0x03.toByte())

        // reset the buffer.
        seq_hdr.data!!.rewind()
        frames.add(seq_hdr)

        // sps
        if (sps_hdr.data == null) {
            sps_hdr.data = ByteBuffer.allocate(3)
            sps_hdr.size = 3
        }
        sps_hdr.data!!.rewind()
        // 5.3.4.2.1 Syntax, H.264-AVC-ISO_IEC_14496-15.pdf, page 16
        // numOfSequenceParameterSets, always 1
        sps_hdr.data!!.put(0x01.toByte())
        // sequenceParameterSetLength
        sps_hdr.data!!.putShort(sps.array().size.toShort())
        sps_hdr.data!!.rewind()
        frames.add(sps_hdr)

        // sequenceParameterSetNALUnit
        sps_bb.size = sps.array().size
        sps_bb.data = sps.duplicate()
        frames.add(sps_bb)

        // pps
        if (pps_hdr.data == null) {
            pps_hdr.data = ByteBuffer.allocate(3)
            pps_hdr.size = 3
        }
        pps_hdr.data!!.rewind()
        // 5.3.4.2.1 Syntax, H.264-AVC-ISO_IEC_14496-15.pdf, page 16
        // numOfPictureParameterSets, always 1
        pps_hdr.data!!.put(0x01.toByte())
        // pictureParameterSetLength
        pps_hdr.data!!.putShort(pps.array().size.toShort())
        pps_hdr.data!!.rewind()
        frames.add(pps_hdr)

        // pictureParameterSetNALUnit
        pps_bb.size = pps.array().size
        pps_bb.data = pps.duplicate()
        frames.add(pps_bb)
    }


    fun muxFlvTag(
        frames: ArrayList<SrsFlvFrameBytes>, frame_type: Int,
        avc_packet_type: Int, dts: Int, pts: Int
    ): SrsAllocator.Allocation? {
        // for h264 in RTMP video payload, there is 5bytes header:
        //      1bytes, FrameType | CodecID
        //      1bytes, AVCPacketType
        //      3bytes, CompositionTime, the cts.
        // @see: E.4.3 Video Tags, video_file_format_spec_v10_1.pdf, page 78
        var size = 5
        for (i in frames.indices) {
            size += frames[i].size
        }
        val allocation = mVideoAllocator.allocate(size)

        // @see: E.4.3 Video Tags, video_file_format_spec_v10_1.pdf, page 78
        // Frame Type, Type of video frame.
        // CodecID, Codec Identifier.
        // set the rtmp header
        allocation!!.put((frame_type shl 4 or SrsCodecVideo.AVC).toByte())

        // AVCPacketType
        allocation!!.put(avc_packet_type.toByte())

        // CompositionTime
        // pts = dts + cts, or
        // cts = pts - dts.
        // where cts is the header in rtmp video packet payload header.
        val cts = pts - dts
        allocation.put((cts shr 16).toByte())
        allocation.put((cts shr 8).toByte())
        allocation.put(cts.toByte())

        // h.264 raw data.
        for (i in frames.indices) {
            val frame = frames[i]
            try {
                frame.data!![allocation.array(), allocation.size(), frame.size]
                allocation.appendOffset(frame.size)
            } catch (e: Exception) {
                eLog(logTag, "Error")
            }

        }
        return allocation
    }
}