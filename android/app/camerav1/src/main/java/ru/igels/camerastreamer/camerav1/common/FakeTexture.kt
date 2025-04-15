package ru.igels.camerastreamer.camerav1.common

import android.graphics.SurfaceTexture
import android.opengl.*
import android.opengl.EGL14.*
import android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES
import android.opengl.GLES20.*
import ru.igels.camerastreamer.camerav1.common.Logger.eLog


internal class FakeTexture(var errorCallback: (error: String)-> Unit) : SurfaceTexture.OnFrameAvailableListener {

    private val logTag: String = "Camera1Core"
    private var mEGLDisplay = EGL_NO_DISPLAY
    private var mEGLContext = EGL_NO_CONTEXT
    private var mEGLConfig: EGLConfig? = null
    private var mTextureTarget = GL_TEXTURE_EXTERNAL_OES
    private var mTextureId = 0
    private var mEGLSurface: EGLSurface? = null

    private var surfaceTexture: SurfaceTexture? = null
//    val fpsMeasure = FPSMeasure(label = "fake texture")

    fun getTexture(): SurfaceTexture? {
        setupEGL()
        mEGLSurface = createPBOSurface(1, 1)
        makeCurrent(mEGLSurface!!)

        mTextureId = createTextureObject()
        surfaceTexture = SurfaceTexture(mTextureId)
        surfaceTexture?.setOnFrameAvailableListener(this)
        Logger.iLog(logTag, "Create fake texture")
        return surfaceTexture
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        try {
//            surfaceTexture?.updateTexImage()
//            surfaceTexture?.timestamp
//            val pixelBuf: ByteBuffer = ByteBuffer.allocateDirect(1280 * 720 * 4)
//            pixelBuf.order(ByteOrder.LITTLE_ENDIAN)
//            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//            GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
//            // Try to ensure that rendering has finished.
//            GLES20.glFinish();
//            GLES20.glReadPixels(0, 0, 1280, 720,
//                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuf);
//            val data = ByteArray(1280 * 720 * 4)
//            pixelBuf.get(data)
//            fpsMeasure.measureFPS()
        }
        catch (e: Exception) {
            eLog(logTag, "onFrameAvailable $e")
            errorCallback(e.toString())
        }
    }

    private fun setupEGL() {
        val EGL_RECORDABLE_ANDROID = 0x3142
        val confAttr = intArrayOf(
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,    // very important!
            EGL_SURFACE_TYPE, EGL_PBUFFER_BIT,          // we will create a pixelbuffer surface
            EGL_RED_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_BLUE_SIZE, 8,
            EGL_ALPHA_SIZE, 8,     // if you need the alpha channel
            EGL_DEPTH_SIZE, 16,    // if you need the depth buffer
            EGL_RECORDABLE_ANDROID, 1,
            EGL_NONE
        )
        // EGL context attributes
        val ctxAttr = intArrayOf(
            EGL_CONTEXT_CLIENT_VERSION, 2,              // very important!
            EGL_NONE
        )

        mEGLDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
        val version = IntArray(2)
        if (!eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            mEGLDisplay = null
            throw RuntimeException("unable to initialize EGL14")
        }

        // choose the first config, i.e. best config
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        if (!eglChooseConfig(
                mEGLDisplay, confAttr, 0, configs, 0, configs.size,
                numConfigs, 0
            )
        ) {
            eLog(logTag, "unable to find RGB8888 / $version EGLConfig")
            throw java.lang.RuntimeException("Unable to find a suitable EGLConfig")
        }
        mEGLConfig = configs[0]

        //Create context
        mEGLContext = eglCreateContext(mEGLDisplay, mEGLConfig, EGL_NO_CONTEXT, ctxAttr, 0)
        checkGlError("eglCreateContext")
        Logger.iLog(logTag, "EGL setup success!")
    }

    private fun createPBOSurface(w: Int, h: Int): EGLSurface? {
        // surface attributes
        // the surface size is set to the input frame size
        val surfaceAttr = intArrayOf(
            EGL_WIDTH, w,
            EGL_HEIGHT, h,
            EGL_NONE
        )
        mEGLSurface = eglCreatePbufferSurface(mEGLDisplay, mEGLConfig, surfaceAttr, 0)
        checkGlError("eglCreatePbufferSurface")
        if (mEGLSurface == null) {
            throw java.lang.RuntimeException("surface was null")
        }
        return mEGLSurface
    }

    private fun makeCurrent(surfase: EGLSurface) {
        if (!eglMakeCurrent(mEGLDisplay, surfase, surfase, mEGLContext)) {
            throw java.lang.RuntimeException("eglMakeCurrent failed")
        }

    }

    private fun checkGlError(op: String) {
        val error = glGetError()
        if (error != GL_NO_ERROR) {
            val msg = op + ": glError 0x" + Integer.toHexString(error)
            throw java.lang.RuntimeException(msg)
        }
    }

    private fun createTextureObject(): Int {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        checkGlError("glGenTextures")
        val texId = textures[0]
        GLES20.glBindTexture(mTextureTarget, texId)
        checkGlError("glBindTexture $texId")
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        checkGlError("glTexParameter")
        return texId
    }

    fun destroyTexture() {
        if (mEGLDisplay !== EGL_NO_DISPLAY) {
            eglMakeCurrent(
                mEGLDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE,
                EGL_NO_CONTEXT
            )
            eglDestroyContext(mEGLDisplay, mEGLContext)
            eglReleaseThread()
            eglTerminate(mEGLDisplay)
        }
        surfaceTexture = null
        mEGLDisplay = EGL_NO_DISPLAY
        mEGLContext = EGL_NO_CONTEXT
        mEGLConfig = null
        Logger.iLog(logTag, "Destroy fake CameraTexture")
    }

    protected fun finalize() {
        Logger.iLog(logTag, "Finalize CameraTexture class")
        destroyTexture()
    }
}