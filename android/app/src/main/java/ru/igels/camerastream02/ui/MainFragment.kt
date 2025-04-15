package ru.igels.camerastream02.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.igels.camerastream02.R
import ru.igels.camerastream02.databinding.FragmentMainBinding
import ru.igels.camerastream02.domain.logger.dLog
import ru.igels.camerastream02.domain.logger.eLog
import ru.igels.camerastream02.domain.logger.iLog
import ru.igels.camerastream02.ui.viewmodels.MainModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MainFragment : Fragment(R.layout.fragment_main), View.OnClickListener,
    SurfaceHolder.Callback {

    private val logTag: String = "MainFragment"
    private lateinit var binding: FragmentMainBinding
    private val model: MainModel by activityViewModels()
    private var surfaceView: SurfaceView? = null
    private var updateSurfaceJob: Job? = null
    private var isActive = false

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = MainFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentMainBinding.inflate(layoutInflater)
        binding.btStartStop.setOnClickListener(this)
        binding.btSettings.setOnClickListener(this)
        binding.btLogout.setOnClickListener(this)

        surfaceView = binding.surfaceView
        surfaceView?.holder?.addCallback(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.mainActivityState.collect {
                    dLog(logTag, "fragment state")
                    process(it.isBusy)
                    binding.btStartStop.text = if(it.isStream) "STOP" else "START"
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

        private fun process(value: Boolean) {
        if (value) {
            binding.tvError.visibility = INVISIBLE
            binding.btStartStop.visibility = INVISIBLE
            binding.btSettings.visibility = INVISIBLE
            binding.pbStartStream.visibility = VISIBLE
        } else {
            binding.tvError.visibility = INVISIBLE
            binding.btStartStop.visibility = VISIBLE
            binding.btSettings.visibility = VISIBLE
            binding.pbStartStream.visibility = INVISIBLE
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btStartStop -> {
                model.startStopStream()
            }

            R.id.btSettings -> {
//                throw Exception("ALARMAAA!!!!!")
                startActivity(Intent(activity, SettingsActivity::class.java))
            }

            R.id.btLogout -> model.logout()

            else -> {}
        }
    }

    override fun onResume() {
        super.onResume()
        iLog(logTag, "resumed")
        isActive = true
        model.startPreview()
    }

    override fun onPause() {
        super.onPause()
        iLog(logTag, "paused")
        isActive = false
        model.stopPreview()
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        iLog(logTag, "surfaceCreated")
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        iLog(logTag, "surfaceChanged")
        updateSurfaceJob = lifecycleScope.launch(Dispatchers.Default) {
            model.getFrameQueue().collect {
                updateSurface(it)
            }
        }
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        surfaceView = null
        updateSurfaceJob?.cancel()
        iLog(logTag, "surfaceDestroyed")
    }

    private fun updateSurface(bmp: Bitmap) {
        if (!isActive) return
        try {
            val lc = surfaceView?.holder?.lockCanvas()
            if (lc != null) {
//            val width = it.width
//            val height = bmp.height * it.width / bmp.width
//            it.drawColor(0, PorterDuff.Mode.CLEAR)
                val rectDst = Rect(0, 0, lc.width, lc.height)
//                Rect(0, (it.height - height) / 2, width, (it.height - height) / 2 + height)
                //val rectSrc = Rect(0, 0, bmp.width, bmp.height)
                lc.drawBitmap(bmp, null, rectDst, null)
                surfaceView?.holder?.unlockCanvasAndPost(lc)
            }
        } catch (e: Exception) {
            eLog(logTag, e.message.toString())
        }
    }
}