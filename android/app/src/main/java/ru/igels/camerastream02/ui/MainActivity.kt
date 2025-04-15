package ru.igels.camerastream02.ui

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import ru.igels.camerastream02.R
import ru.igels.camerastream02.databinding.ActivityMainBinding
import ru.igels.camerastream02.ui.viewmodels.MainModel
import ru.igels.camerastream02.domain.logger.iLog
import ru.igels.camerastream02.ui.viewmodels.ActiveFragment

class MainActivity : AppCompatActivity() {

    private val logTag: String = "MainAct"
    private lateinit var binding: ActivityMainBinding

    private val model: MainModel by lazy {
        ViewModelProvider(this)[MainModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        iLog(logTag, "MainActivity created")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.mainActivityVewState.collect {
                    when(it) {
                        ActiveFragment.PERMISSION -> showFragment(NoPermissionFragment())
                        ActiveFragment.LOGIN -> showFragment(LoginFragment())
                        ActiveFragment.MAIN -> showFragment(MainFragment())
                        else -> {}
                    }
                }
            }
        }
//        hideSystemBars()
    }

    private fun showFragment(fr: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mainLayout, fr)
//        fragmentTransaction.addToBackStack(null)
//        fragmentTransaction.commitAllowingStateLoss()
        fragmentTransaction.commit()
    }

//    private fun hideSystemBars() {
//        val windowInsetsController =
//            ViewCompat.getWindowInsetsController(window.decorView) ?: return
//        // Configure the behavior of the hidden system bars
//        windowInsetsController.systemBarsBehavior =
//            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//        // Hide both the status bar and the navigation bar
//        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
//    }

    //!!!Hack menu button event rise error
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_MENU) {
            true
        } else super.onKeyDown(keyCode, event)
    }
}