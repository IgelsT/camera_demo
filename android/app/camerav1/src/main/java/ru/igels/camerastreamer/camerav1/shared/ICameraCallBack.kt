package ru.igels.camerastreamer.camerav1.shared


interface ICameraCallBack {
    fun cameraState(state: CameraState)
    fun rawDataCallBack(data: ByteArray)
}