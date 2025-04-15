package ru.igels.camerastream02.domain

import android.graphics.*
import io.github.crow_misia.libyuv.*
import ru.igels.camerastream02.domain.logger.dLog
import ru.igels.camerastream02.domain.logger.eLog
import ru.igels.camerastreamer.shared.models.DeviceOrientation
import java.nio.ByteBuffer
import kotlin.math.abs

class FrameProcessor() {

    private val logTag: String = "FrameProcessor"

    private var text: String = ""
    private var rotation: DeviceOrientation = DeviceOrientation.TOP
    private var height: Int = 0
    private var width: Int = 0
    private var bmpout: Bitmap? = null
    private var nv21Buff: Nv21Buffer? = null
    private var nv21BuffR: Nv21Buffer? = null
    private var argbBuff: AbgrBuffer? = null
    private var argbBuffIN: AbgrBuffer? = null
    private var i420Buffer: I420Buffer? = null
    private var nv12Buffer: Nv12Buffer? = null
    private var testBuffer: ArgbBuffer? = null
    private var bufferRotation = RotateMode.ROTATE_0

    init {
    }

    private fun setParams(width: Int, height: Int, rotation: DeviceOrientation, text: String? = "") {
        this.text = text ?: ""

        if (this.width != width || this.height != height || this.rotation != rotation
        ) {
            this.width = width
            this.height = height
            this.rotation = rotation
            Thread.sleep(200) // !!! Wait for end of last operations

            nv21Buff = Nv21Buffer.allocate(width, height)

            var _width = width
            var _height = height

            if (rotation == DeviceOrientation.TOP || rotation == DeviceOrientation.BOTTOM) {
                 _width = height
                _height = width
            }
//                nv21BuffR = Nv21Buffer.allocate(height, width)
//                argbBuff = AbgrBuffer.allocate(height, width)
//                bmpout = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888)
//
//                argbBuffIN = AbgrBuffer.allocate(height, width)
//                i420Buffer = I420Buffer.allocate(height, width)
//                nv12Buffer = Nv12Buffer.allocate(height, width)
//            } else {
                nv21BuffR = Nv21Buffer.allocate(_width, _height)
                argbBuff = AbgrBuffer.allocate(_width, _height)
                bmpout = Bitmap.createBitmap(_width, _height, Bitmap.Config.ARGB_8888)

                argbBuffIN = AbgrBuffer.allocate(_width, _height)
                i420Buffer = I420Buffer.allocate(_width, _height)
                nv12Buffer = Nv12Buffer.allocate(width, _height)

                testBuffer= ArgbBuffer.allocate(_width, _height)
//            }

            when (rotation) {
                DeviceOrientation.TOP -> bufferRotation = RotateMode.ROTATE_90
                DeviceOrientation.LEFT -> bufferRotation = RotateMode.ROTATE_180
                DeviceOrientation.BOTTOM -> bufferRotation = RotateMode.ROTATE_270
                DeviceOrientation.RIGHT -> bufferRotation = RotateMode.ROTATE_0
                DeviceOrientation.UNKNOWN -> bufferRotation = RotateMode.ROTATE_0
            }
            dLog(
                logTag,
                "Setup frame processor width: $_width, height: $_height, rotation: $rotation"
            )
        }
    }

    @Synchronized
    fun processFrameNV21(
        data: ByteArray,
        width: Int,
        height: Int,
        rotation: DeviceOrientation,
        text: String? = ""
    ): Bitmap? {
        setParams(width, height, rotation, text)
        if (!checkParamsIN()) return null
        try {
            nv21Buff?.asBuffer()?.put(data)
            if(bufferRotation != RotateMode.ROTATE_0) { // Crash on some devices
                nv21Buff?.rotate(nv21BuffR!!, bufferRotation)
                nv21BuffR?.convertTo(argbBuff!!)
            }
            else {
                nv21Buff?.convertTo(argbBuff!!)
            }
            bmpout?.copyPixelsFromBuffer(argbBuff?.asBuffer())
            if (text != null && text.isNotEmpty()) {
                createText()
            }
        } catch (e: Exception) {
            eLog(logTag, "Error process NV21 ${e.message}")
        }
        return bmpout
    }

    private fun createText() {
//        val r = MainApp.getInstance().applicationContext.resources
//        val px = TypedValue.applyDimension(
//            TypedValue.COMPLEX_UNIT_DIP,
//            textSize,
//            r.displayMetrics
//        )
//        val textM = paint.measureText(text)

        var textSize = 20F
        if(width>=1280) textSize = 35F

        val textX = 0F
        val textY = 0F
        val canvas = Canvas(bmpout!!)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = textSize

        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        val textWith = abs(bounds.right + bounds.left)
        val textHeight = abs(bounds.top + bounds.bottom)

        paint.style = Paint.Style.FILL;
        paint.color = Color.BLACK
        canvas.drawRect(0F, 0F, textWith + 2F, textHeight + 2F, paint);

        paint.color = Color.WHITE
        canvas.drawText(text, textX + 1F, textY + textHeight + 1F, paint)
        // size 35F, for 1280x720
    }

    @Synchronized
    fun processFrameRGBA2I420Planar(bmp: Bitmap): ByteBuffer? {
        if (!checkParamsOUT()) return null
        try {
            bmp.copyPixelsToBuffer(argbBuffIN?.asBuffer())
            argbBuffIN?.convertTo(i420Buffer!!)
            val size = i420Buffer?.asBuffer()?.capacity() ?: 0
            if (size > 0) {
                return i420Buffer?.asBuffer()
            }
        } catch (e: Exception) {
            eLog(logTag, "Error processFrameRGBA2I420Planar ${e.message}")
        }
        return null
    }

    @Synchronized
    fun processFrameRGBA2I420SemiPlanar(bmp: Bitmap): ByteBuffer? {
        if (!checkParamsOUT()) return null
        try {
            bmp.copyPixelsToBuffer(testBuffer?.asBuffer())
            testBuffer?.convertTo(nv21BuffR!!)
            val size = nv21BuffR?.asBuffer()?.capacity() ?: 0
            if (size > 0) {
                return nv21BuffR?.asBuffer()
            }
        } catch (e: Exception) {
            eLog(logTag, "Error processFrameRGBA2I420SemiPlanar ${e.message}")
        }
        return null
    }

    fun processFrameRGBA2NV12(bmp: Bitmap): ByteBuffer? {
        if (!checkParamsOUT()) return null
        try {
            bmp.copyPixelsToBuffer(argbBuffIN?.asBuffer())
            argbBuffIN?.convertTo(nv12Buffer!!)
            val size = nv12Buffer?.asBuffer()?.capacity() ?: 0
            if (size > 0) {
                return nv12Buffer?.asBuffer()
            }
        } catch (e: Exception) {
            eLog(logTag, "Error processFrameRGBA2NV12 ${e.message}")
        }
        return null
    }

    private fun checkParamsIN(): Boolean {
        if (width == 0 || height == 0 || nv21Buff == null || nv21BuffR == null || argbBuff == null)
            return false
        return true
    }

    private fun checkParamsOUT(): Boolean {
        if (width == 0 || height == 0 || argbBuffIN == null || i420Buffer == null || nv12Buffer == null)
            return false
        return true
    }
}