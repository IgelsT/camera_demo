package ru.igels.camerastream02.ui

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import ru.igels.camerastream02.data.PermissionData
import ru.igels.camerastream02.databinding.ActivityLogoBinding
import ru.igels.camerastream02.domain.logger.iLog

class LogoActivity : AppCompatActivity() {
    private val logTag: String = "LogoAct"
    private lateinit var binding: ActivityLogoBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permission ->
        iLog(logTag, "$permission")
        if(permission.isNotEmpty()) {
//            MainApp.getInstance().closeLogoActivity()
            this.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestPermissionLauncher.launch(PermissionData.appPermission)
//        val fragmentTransaction = supportFragmentManager.beginTransaction()
//        val fr = RequestPermissionFragment()
//        fr.callback = {
//            MainApp.getInstance().closeLogoActivity()
//            this.finish()
//        }
//        fragmentTransaction.replace(R.id.frame, fr)
//        fragmentTransaction.addToBackStack(null)
//        fragmentTransaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}