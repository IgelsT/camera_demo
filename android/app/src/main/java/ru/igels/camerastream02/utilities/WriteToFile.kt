package ru.igels.camerastream02.utilities

import android.graphics.Bitmap
import android.os.Environment
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


var outputStream: BufferedOutputStream? = null

fun bmpToFile(bmp: Bitmap) {
    val sdf = SimpleDateFormat("dd.M.yyy HH:mm:ss")
    val file = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
        "${sdf.toString()}.jpg"
    )
    val bos = ByteArrayOutputStream();
    bmp.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
    val bitmapdata = bos.toByteArray();
    val fos = FileOutputStream(file);
    fos.write(bitmapdata);
    fos.flush();
    fos.close();
}

fun openFile() {
    val mFile = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
        "newfile.h264"
    )

    try {
        outputStream = BufferedOutputStream(FileOutputStream(mFile))
//            Log.i("Encoder", "outputStream initialized")
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}

fun toFile(outData: ByteArray) {
    try {
        outputStream?.write(outData, 0, outData.size) // гоним байты в поток
    } catch (e: Exception) {
        // TODO Auto-generated catch block
        e.printStackTrace()
    }
}

private fun getMefdiaFile(): File? {
    // To be safe, you should check that the SDCard is mounted
    // using Environment.getExternalStorageState() before doing this.
    val mediaStorageDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        "Camera2Test"
    )

    // This location works best if you want the created images to be shared
    // between applications and persist after your app has been uninstalled.

    // Create the storage directory if it does not exist
    if (!mediaStorageDir.exists()) {
        if (!mediaStorageDir.mkdirs()) {
            return null
        }
    }

    // Create a media file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val mediaFile: File
    mediaFile = File(mediaStorageDir.path + File.separator + "IMG_" + timeStamp + ".jpg")
    return mediaFile
}