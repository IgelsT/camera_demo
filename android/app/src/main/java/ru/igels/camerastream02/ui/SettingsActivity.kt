package ru.igels.camerastream02.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import ru.igels.camerastream02.R
import ru.igels.camerastream02.databinding.ActivitySettingsBinding
import ru.igels.camerastream02.ui.viewmodels.SettingsViewModel
import ru.igels.camerastream02.utilities.Popups


class SettingsActivity : AppCompatActivity(), OnTouchListener {

    private val logTag: String = "SettingsAct"
    private lateinit var binding: ActivitySettingsBinding

    private val model: SettingsViewModel by lazy {
        ViewModelProvider(this)[SettingsViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cbFlipVertical.isChecked = model.getFlipVertical()
        binding.cbFlipVertical.setOnCheckedChangeListener { _, isChecked ->
            hideKeyboard()
            model.setFlipVertical(isChecked)
        }
        binding.cbAccSave.isChecked = model.getAccSave()
        binding.cbAccSave.setOnCheckedChangeListener { _, isChecked ->
            hideKeyboard()
            model.setAccSave(isChecked)
        }
        binding.cbAutoStart.isChecked = model.getAutoStart()
        binding.cbAutoStart.setOnCheckedChangeListener { _, isChecked ->
            hideKeyboard()
            model.setAutoStart(isChecked)
        }
//        binding.txtBaseUrl.setText(model.getServerAddress())
//        binding.txtUserName.setText(model.getUserName())
//        binding.txtPassword.setText(model.getUserPassword())
        binding.txtDeviceName.setText(model.getDeviceName())
        binding.txtDeviceDescr.setText(model.getDeviceDescr())

        binding.topAppBar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.topAppBar.setNavigationOnClickListener {
            this.finish()
        }
        binding.topAppBar.menu.findItem(R.id.saveSettings).setOnMenuItemClickListener {
            checkValues()
            return@setOnMenuItemClickListener true
        }
    }

    private fun checkValues() {
        if (binding.txtDeviceName.text?.isBlank() == true) {
            binding.inputDeviceName.boxStrokeColor = Color.RED
            Popups.showToast("Device name require!")
            binding.txtDeviceName.requestFocus()
            return
        }
        if (binding.txtDeviceDescr.text?.isBlank() == true) {
            binding.inputDeviceDescr.boxStrokeColor = Color.RED
            Popups.showToast("Device description require!")
            binding.txtDeviceDescr.requestFocus()
            return
        }
//        if (binding.txtBaseUrl.text?.isBlank() == true) {
//            binding.inputBaseUrl.boxStrokeColor = Color.RED
//            Popups.showToast("Server address require!")
//            binding.txtBaseUrl.requestFocus()
//            return
//        }
//        if (binding.txtUserName.text?.isBlank() == true) {
//            binding.inputUserName.boxStrokeColor = Color.RED
//            Popups.showToast("User name require!")
//            binding.txtUserName.requestFocus()
//            return
//        }
//        if (binding.txtPassword.text?.isBlank() == true) {
//            binding.inputPassword.boxStrokeColor = Color.RED
//            Popups.showToast("User name require!")
//            binding.txtPassword.requestFocus()
//            return
//        }
//        model.setServerAddress(binding.txtBaseUrl.text.toString())
//        model.setUserName(binding.txtUserName.text.toString())
//        model.setUserPassword(binding.txtPassword.text.toString())
        model.setDeviceName(binding.txtDeviceName.text.toString())
        model.setDeviceDescr(binding.txtDeviceDescr.text.toString())
        model.saveSettings()
        finish()
    }

    override fun onResume() {
        super.onResume()
        setDDL(binding.spCamera, model.getCameraList())
        setDDL(binding.spResolution, model.getResolutionList())
        setDDL(binding.spFPS, model.getFpsList())
        setDDL(binding.spQuality, model.getQualityList())
        setDDL(binding.spFocus, model.getFocusList())
        hideKeyboard()
    }

    private fun setDDL(ddl: android.widget.AutoCompleteTextView, values: Pair<List<String>, Int>) {
        if(values.first.isEmpty()) return
        ddl.setAdapter(ArrayAdapter(this, R.layout.drop_down_item, values.first))
        ddl.freezesText = true;
        if(values.second >= values.first.size) ddl.setText(values.first[0], false)
        else ddl.setText(values.first[values.second], false)
        ddl.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ -> ddlSelect(ddl.id, position) }
        ddl.setOnTouchListener(this)
    }

    private fun ddlSelect(id: Int, position: Int) {
        hideKeyboard()
        when (id) {
            R.id.spCamera -> {
                model.setCameraSelected(position)
                val newResolution = model.getResolutionList()
                setDDL(binding.spResolution, Pair(newResolution.first, 0))
            }
            R.id.spResolution -> model.setResolutionSelected(position)
            R.id.spFPS -> model.setFpsSelected(position)
            R.id.spQuality -> model.setQualitySelected(position)
            R.id.spFocus -> model.setFocusSelected(position)
        }
    }


    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            hideKeyboard()
        }
        return false
    }

    private fun hideKeyboard() {
        binding.txtDeviceDescr.clearFocus()
        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.txtDeviceDescr.windowToken, 0)
    }
}