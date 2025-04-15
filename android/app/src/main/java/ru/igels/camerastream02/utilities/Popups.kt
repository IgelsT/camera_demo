package ru.igels.camerastream02.utilities

import android.widget.Toast
import ru.igels.camerastream02.MainApp

object Popups {
    fun showToast(message: String) {
        Toast.makeText(
            MainApp.getInstance().applicationContext, message,
            Toast.LENGTH_LONG
        ).show()
    }
}