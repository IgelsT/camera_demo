package ru.igels.camerastreamer.camerav1.common

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import ru.igels.camerastreamer.camerav1.common.Logger.eLog

internal class FakeSurface(var context: Context) {

    private val logTag: String = "FakeSurface"

    var windowManager: WindowManager? = null
    var mSurfaceView: SurfaceView? = null
    var mSurfaceHolder: SurfaceHolder? = null

    fun getSurface(): SurfaceView? {
        return mSurfaceView
    }

    private fun CreateSW() {
//        // Create new SurfaceView, set its size to 1x1, move it to the top left corner and set this service as a callback
//        if (windowManager == null || mSurfaceView == null || mSurfaceHolder == null) {
//            AppUtils.Log("CamUtils", "Create mSurfaceView");
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?;
        mSurfaceView = SurfaceView(context)
        val layoutParams = WindowManager.LayoutParams(
            1, 1,
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )
        try {
            layoutParams.gravity = Gravity.LEFT or Gravity.TOP
            windowManager?.addView(mSurfaceView, layoutParams);
//                mSurfaceHolder = mSurfaceView.getHolder();
//                mSurfaceHolder.addCallback(new SurfaceHolder . Callback () {
//                    @Override
//                    public void surfaceCreated(SurfaceHolder surfaceHolder) {
//                        StartCamAction(surfaceHolder);
//                    }
//                    @Override
//                    public void surfaceChanged(
//                        SurfaceHolder holder,
//                        int format,
//                        int width,
//                        int height
//                    ) {}
//                    @Override
//                    public void surfaceDestroyed(SurfaceHolder holder) {}
//                });
        } catch (e: Exception) {
            eLog(logTag, "Error create fake surface ${e.message}");
        }
//        } else {
//            AppUtils.Log("CamUtils", "mSurfaceView exist");
//            StartCamAction(mSurfaceHolder);
//        }
    }
}