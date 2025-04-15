package ru.igels.camerastream02.data

import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaFormat
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue

data class MediaCodecFrame(
    val dataIn: ByteBuffer?,
    val infoIn: MediaCodec.BufferInfo,
    val format: MediaFormat?,
    val SpsPpS: Pair<ByteArray, ByteArray>?
) {
    val data: ByteArray = ByteArray(infoIn.size)
    val info: MediaCodec.BufferInfo

    init {
        dataIn?.rewind()
        dataIn?.get(data)
//        data = ByteBuffer.wrap(bytes)
        info = MediaCodec.BufferInfo().also {
            it.offset = infoIn.offset
            it.size = infoIn.size
            it.presentationTimeUs = infoIn.presentationTimeUs
            it.flags = infoIn.flags
        }
    }

    fun asBuffer(): ByteBuffer {
        return ByteBuffer.wrap(data)
    }

    fun withoutStart(): ByteArray {
        val newArray = ByteArray(data.size - 4)
        System.arraycopy(data, 4, newArray, 0, newArray.size)
        return newArray
    }
}

data class MediaCodecFrameAudio(val dataIn: ByteBuffer?, val infoIn: MediaCodec.BufferInfo, val sampleRate: Int, val channels: Int) {
    val data: ByteArray = ByteArray(infoIn.size)
    val info: MediaCodec.BufferInfo

    init {
        dataIn?.rewind()
        dataIn?.get(data)
        info = MediaCodec.BufferInfo().also {
            it.offset = infoIn.offset
            it.size = infoIn.size
            it.presentationTimeUs = infoIn.presentationTimeUs
            it.flags = infoIn.flags
        }
    }
}

object FrameQueues {
    val mediaCodecFrames = ArrayBlockingQueue<MediaCodecFrame>(10)
    val mediaCodecFramesAudio = ArrayBlockingQueue<MediaCodecFrameAudio>(10)
    val rawFrameFlow =
        MutableSharedFlow<ByteArray>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val framesFlow =
        MutableSharedFlow<Bitmap>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
}