package ru.igels.camerastream02.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import ru.igels.camerastream02.R
import ru.igels.camerastream02.databinding.FragmentLoginBinding
import ru.igels.camerastream02.domain.logger.dLog
import ru.igels.camerastream02.ui.viewmodels.MainModel
import ru.igels.camerastream02.utilities.Popups

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment(R.layout.fragment_login) {
    //    // TODO: Rename and change types of parameters
//    private var param1: String? = null
//    private var param2: String? = null
    private val logTag = "LoginFragment"
    private lateinit var binding: FragmentLoginBinding
    var defStrokeColor = 0

//    private val model: MainModel by lazy {
//        ViewModelProvider(this)[MainModel::class.java]
//    }

    private val model: MainModel by activityViewModels()

    inner class TextFieldValidation(private val view: TextInputLayout) : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if ((s?.length ?: 0) == 0) view.boxStrokeColor = Color.RED
            else view.boxStrokeColor = defStrokeColor
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dLog(logTag, "onCreate")
        binding = FragmentLoginBinding.inflate(layoutInflater)

        defStrokeColor = binding.txtBaseUrl.boxStrokeColor

        binding.inpBaseUrl.addTextChangedListener(TextFieldValidation(binding.txtBaseUrl))
        binding.inpUserName.addTextChangedListener(TextFieldValidation(binding.txtUserName))
        binding.inpPassword.addTextChangedListener(TextFieldValidation(binding.txtPassword))
        binding.inpPassword.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus -> if (!hasFocus) hideKeyboard() }

        binding.swMain.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard()
            }
            true
        }
//        binding.txtRegister.setOnTouchListener(this);
//        binding.txtForgotPassword.setOnTouchListener(this);

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.mainActivityState.collect {
                    dLog(logTag, "fragment state")
                    binding.inpBaseUrl.setText(it.baseUrl)
                    binding.inpUserName.setText(it.userName)
                    binding.inpPassword.setText(it.userPassword)
                    if (it.isBusy) {
                        binding.pbLoading.visibility = VISIBLE
                        binding.btLogin.visibility = GONE
                    } else {
                        binding.pbLoading.visibility = GONE
                        binding.btLogin.visibility = VISIBLE
                        binding.btLogin.setOnClickListener {
                            hideKeyboard()
                            login()
                        }
                    }
                    binding.txtError.text = it.errorMessage
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dLog(logTag, "onCreateView")
        return binding.root
    }

    private fun login() {
        if (binding.inpBaseUrl.text?.isBlank() == true) {
            binding.txtBaseUrl.boxStrokeColor = Color.RED
            Popups.showToast("Server address require!")
            binding.inpBaseUrl.requestFocus()
            return
        }
        if (binding.inpUserName.text?.isBlank() == true) {
            binding.txtUserName.boxStrokeColor = Color.RED
            Popups.showToast("User name require!")
            binding.inpUserName.requestFocus()
            return
        }
        if (binding.inpPassword.text?.isBlank() == true) {
            binding.txtPassword.boxStrokeColor = Color.RED
            Popups.showToast("Password require!")
            binding.inpPassword.requestFocus()
            return
        }
        saveState()
        model.login()
    }

    private fun saveState() {
        model.setCredentials(
            binding.inpBaseUrl.text.toString(),
            binding.inpUserName.text.toString(),
            binding.inpPassword.text.toString()
        )
    }

    private fun hideKeyboard() {
        binding.txtUserName.clearFocus()
        val imm: InputMethodManager =
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.txtUserName.windowToken, 0)
    }

    override fun onPause() {
        super.onPause()
        dLog(logTag, "onPause")
        saveState()
    }

    override fun onDestroy() {
        super.onDestroy()
        dLog(logTag, "OnDestroy")
        saveState()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            LoginFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
            }
    }

//    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
//        iLog(logTag, "$view")
//        var url = "http://" + SettingsData.getSettings().baseUrl
//        val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//        startActivity(urlIntent)
//        return false
//    }
}