package ru.igels.camerastream02.encoders

data class DeviceCodecInfo(var name: String, var isHardware: Boolean) {
    var supportedColors: MutableList<Int> = arrayListOf()
    var supportedBitrateModes: MutableList<Int> = arrayListOf()
    var supportedBitRateRange = IntArray(2)
}
