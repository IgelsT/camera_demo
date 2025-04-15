package ru.igels.camerastreamer.camerav1

import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Handler
import ru.igels.camerastreamer.camerav1.common.FakeTexture
import ru.igels.camerastreamer.camerav1.common.Logger
import ru.igels.camerastreamer.camerav1.common.Utils
import ru.igels.camerastreamer.camerav1.shared.CameraParamsModel
import ru.igels.camerastreamer.camerav1.shared.CameraState
import ru.igels.camerastreamer.camerav1.shared.ErrorCodes
import ru.igels.camerastreamer.camerav1.shared.ICameraCallBack
import ru.igels.camerastreamer.shared.logger.ILogger
import ru.igels.camerastreamer.shared.models.CameraInfoModel
import ru.igels.camerastreamer.shared.models.DeviceOrientation
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.abs

@Suppress("DEPRECATION")
class Camera1Core(private var callback: ICameraCallBack, logger: ILogger? = null) {

    private var camerasList: List<CameraInfoModel> = listOf()
    private val logTag = "Camera1Core"
    private var cameraId: Int = 0
    private var mCamera: Camera? = null
    private var mParams: Camera.Parameters? = null
    private val camHandler: Handler = Utils.getThread("CameraThread")
    private val imageFormat = ImageFormat.NV21
    private var width: Int = 0
    private var height: Int = 0
    private var rotation: Int = 0
    private var focus = ru.igels.camerastreamer.shared.models.CAMFOCUSES.AUTO
    private var fakeTexture: FakeTexture?

    //    private var fakeSurface = FakeSurface(MainApp.getInstance().applicationContext)
    private var camTexture: SurfaceTexture? = null
    private var range: IntArray = intArrayOf(0, 0)
    private var yuvBufferSize: Int = 0
    private val BUFFERS_COUNT = 5
    private val mLock = ReentrantLock(true)
    private var cameraState: CameraState = CameraState.Idle

    private val popularRes = arrayOf(
        "320x240", "640x480", "720x480", "800x480", "960x720", "1280x720", "1920x1080"
    )

    init {
        Logger.externalLogger = logger
        Logger.iLog(logTag, "Create camera class")
        fakeTexture = FakeTexture {
            errorHandler(it)
        }
        camTexture = fakeTexture?.getTexture()
        if (camTexture == null)
            throw Exception("Cannot create fake texture")
    }


    @Synchronized
    fun getCameraList(): List<ru.igels.camerastreamer.shared.models.CameraInfoModel> {
        camerasList = getCamerasOfDevice()
        return camerasList
    }

    private fun setState(state: CameraState) {
        if (cameraState != state) {
            cameraState = state
            callback.cameraState(cameraState)
        }
    }

    @Synchronized
    fun start(params: CameraParamsModel): CameraState {
        val paramsChecked =
            checkParams(params.cameraID, params.width, params.height, params.rotation, params.focus)
        if (paramsChecked && cameraState is CameraState.Open) {
            setState(cameraState)
            return cameraState
        }
        if (!paramsChecked && cameraState is CameraState.Open) stop()
        startAll()
        return cameraState
    }

    private fun checkParams(
        cameraID: Int,
        width: Int,
        height: Int,
        rotation: DeviceOrientation,
        focus: ru.igels.camerastreamer.shared.models.CAMFOCUSES,
    ): Boolean {
        var result = true
        if (this.cameraId != cameraID) {
            this.cameraId = cameraID
            result = false
        }
        if (this.width != width) {
            this.width = width
            result = false
        }
        if (this.height != height) {
            this.height = height
            result = false
        }
        if (this.focus != focus) {
            this.focus = focus
            result = false
        }
        return result
    }

    @Synchronized
    fun stop(): CameraState {
        if(mCamera == null) return cameraState
        setState(CameraState.Busy("Closing"))
        camProcess(closeCamera, ErrorCodes.ERROR_CLOSE_CAMERA)?.let {
            setState(it)
            return cameraState
        }
        setState(CameraState.Idle)
        return cameraState
    }

    private fun startAll() {
        setState(CameraState.Busy("Try to open camera $cameraId, ${width}x${height}"))
        camProcess(openCamera, ErrorCodes.ERROR_OPEN_CAMERA)?.let {
            setState(it)
            closeCamera()
            return
        }
        setState(CameraState.Busy("start preview"))
        camProcess(startPreview, ErrorCodes.ERROR_START_PREVIEW)?.let {
            setState(it)
            closeCamera()
            return
        }
        setState(CameraState.Open("Camera $cameraId ready with, ${width}x${height}, fps ${range[0]}-${range[1]}"))
    }

    private fun camProcess(process: () -> Unit, error: ErrorCodes): CameraState? {
        var result: CameraState? = null
        val t1 = System.currentTimeMillis()
        var isProcess = true
        camHandler.post {
            isProcess = try {
                process()
                false
            } catch (e: Exception) {
                result = CameraState.Error(error, e.message)
                false
            }
        }
        while (isProcess && System.currentTimeMillis() - t1 <= 2000) {
            Thread.sleep(100)
        }
        if (isProcess) result = CameraState.Error(
            ErrorCodes.CAMERA_PROCESS_TIMEOUT,
            "Camera process timeout"
        )
        return result
    }

    private val openCamera = fun() {
        mCamera = Camera.open(cameraId)
        mCamera?.setErrorCallback(camErrorCalBack)
        mParams = mCamera?.parameters
        mParams?.setPreviewSize(width, height)
        mParams?.previewFormat = imageFormat
        //Need variable fps. On fixed some cameras are dark.
        range = adaptFpsRange(
            30, mParams?.supportedPreviewFpsRange as List<IntArray>
        )
        mParams?.setPreviewFpsRange(range[0], range[1])
//            mParams?.setRecordingHint(true)
//            mParams?.set("video-size", "${width}x${height}")
        setFocus(mParams, focus)
        mCamera?.parameters = mParams
    }

    private val startPreview = fun() {
        mCamera?.setPreviewTexture(camTexture)
//            mCamera?.setDisplayOrientation(rotation)
        yuvBufferSize = width * height * 3 / 2
        for (i in 1..BUFFERS_COUNT) {
            val cameraBuffer = ByteArray(yuvBufferSize)
            mCamera?.addCallbackBuffer(cameraBuffer)
        }
        mCamera?.setPreviewCallbackWithBuffer(camCallback)
        mCamera?.startPreview()
    }

    private fun setFocus(mParam: Camera.Parameters?, focus: ru.igels.camerastreamer.shared.models.CAMFOCUSES) {
        val supportedFocusModes: List<String> = mParam?.supportedFocusModes as List<String>
        if (supportedFocusModes.isNotEmpty()) {
            mParams?.focusMode = supportedFocusModes[0]
            when (focus) {
                ru.igels.camerastreamer.shared.models.CAMFOCUSES.AUTO -> {
                    if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
                        mParams?.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
                }
                ru.igels.camerastreamer.shared.models.CAMFOCUSES.INFINITE -> {
                    if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_INFINITY))
                        mParams?.focusMode = Camera.Parameters.FOCUS_MODE_INFINITY
                }
                ru.igels.camerastreamer.shared.models.CAMFOCUSES.FIXED -> {
                    if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED))
                        mParams?.focusMode = Camera.Parameters.FOCUS_MODE_FIXED
                }
                ru.igels.camerastreamer.shared.models.CAMFOCUSES.NORMAL -> {
                    if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
                        mParams?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
                }
            }
        }
    }

    private val camErrorCalBack = Camera.ErrorCallback { error, _ ->
        errorHandler(error.toString())
    }

    private fun errorHandler(error: String) {
        Logger.eLog(logTag, "camera error $error")
        callback.cameraState(CameraState.Error(ErrorCodes.ERROR_CAMERA, error))
    }


    private val camCallback = Camera.PreviewCallback { data, _ ->
        try {
            callback.rawDataCallBack(data)
            mCamera?.addCallbackBuffer(data)
        } catch (e: Exception) {
            errorHandler(e.toString())
        }
    }

    private fun adaptFpsRange(expectedFps: Int, fpsRanges: List<IntArray>): IntArray {
        var index = 0
        val fps = expectedFps * 1000
        var diffMin = 0

        for (i in fpsRanges.indices) {
            val dist = abs(fpsRanges[i][1] - fps)
            val diff = fpsRanges[i][1] - fpsRanges[i][0]
            if (dist < abs(fpsRanges[index][1] - fps) && diff >= diffMin) {
                diffMin = diff
                index = i
            } else if (dist == abs(fpsRanges[index][1] - fps) && diff >= diffMin && fpsRanges[i][1] > fpsRanges[index][1]) {
                diffMin = diff
                index = i
            }
        }
        return fpsRanges[index]
    }

    private fun getCamerasOfDevice(): List<ru.igels.camerastreamer.shared.models.CameraInfoModel> {
        if(cameraState != CameraState.Idle) {
            Logger.eLog(logTag, "Error get cameras list, camera busy.")
            return emptyList()
        }
        val camerasList: MutableList<ru.igels.camerastreamer.shared.models.CameraInfoModel> = mutableListOf()
        try {
            mLock.tryLock(1, TimeUnit.SECONDS)
            val camNums = Camera.getNumberOfCameras()
            for (cameraId in 0 until camNums) {
                setState(CameraState.Busy("Opening"))
                val caminfo = Camera.CameraInfo()
                Camera.getCameraInfo(cameraId, caminfo)
                mCamera = Camera.open(cameraId)
                mCamera?.setErrorCallback(camErrorCalBack)
                setState(CameraState.Open("Read camera $cameraId params"))
                mParams = mCamera?.parameters
                val facing =
                    if (caminfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
                        ru.igels.camerastreamer.shared.models.CAMFACING.BACK
                    else ru.igels.camerastreamer.shared.models.CAMFACING.FRONT

                val focuses = mParams?.supportedFocusModes as List<String>
                val resolutions =
                    (mParams?.supportedPreviewSizes as List<Camera.Size>).sortedBy { el -> el.width }
                        .filter { el -> popularRes.contains("${el.width}x${el.height}") }
                        .map { Pair(it.width, it.height) }
//                val res = mParams?.get("preview-size-values")?.split(",")
//                    ?.filter { el -> popularRes.contains(el) }?.map {
//                        Pair(it.split("x")[0].toInt(), it.split("x")[1].toInt())
//                    } ?: listOf()
                val focusesList: MutableList<ru.igels.camerastreamer.shared.models.CAMFOCUSES> = arrayListOf()
                if (focuses.contains(Camera.Parameters.FOCUS_MODE_AUTO)) focusesList.add(ru.igels.camerastreamer.shared.models.CAMFOCUSES.AUTO)
                if (focuses.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) focusesList.add(
                    ru.igels.camerastreamer.shared.models.CAMFOCUSES.INFINITE
                )
                if (focuses.contains(Camera.Parameters.FOCUS_MODE_FIXED)) focusesList.add(ru.igels.camerastreamer.shared.models.CAMFOCUSES.FIXED)
                if (focuses.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) focusesList.add(
                    ru.igels.camerastreamer.shared.models.CAMFOCUSES.NORMAL
                )
                camerasList.add(
                    ru.igels.camerastreamer.shared.models.CameraInfoModel(
                        cameraId,
                        facing,
                        resolutions,
                        focusesList
                    )
                )
                mCamera?.setErrorCallback(null)
                mCamera?.release()
                setState(CameraState.Idle)
            }
        } catch (e: Exception) {
            setState(CameraState.Error(ErrorCodes.ERROR_OPEN_CAMERA, e.message))
            throw e
        } finally {
            mCamera?.release()
            mCamera = null
            if (mLock.isLocked) mLock.unlock()
        }
        return camerasList.toList()
    }

    private val closeCamera = fun() {
        mCamera?.stopPreview()
        Thread.sleep(200)
        mCamera?.setPreviewCallback(null)
        mCamera?.setErrorCallback(null)
        mCamera?.release()
        mCamera = null
    }

    protected fun finalize() {
        Logger.iLog(logTag, "Finalize camera class")
        stop()
        fakeTexture = null
        Utils.closeThread(camHandler)
    }
}