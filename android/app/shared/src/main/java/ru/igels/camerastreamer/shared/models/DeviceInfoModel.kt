package ru.igels.camerastreamer.shared.models

data class DeviceInfoModel(
    val osversion: String? = "",
    val versionsdk: String? = "",
    val serial: String? = "",
    val android_id: String? = "",
    val id: String? = "",
    val model: String? = "",
    val manufacturer: String? = "",
    val brand: String? = "",
    val type: String? = "",
    val user: String? = "",
    val version_codes: Int = 0,
    val host: String? = "",
    val fingerprint: String? = "",
    val version_release: String? = "",
    val device: String? = "",
    val product: String? = "",
    val board: String? = "",
    val cpu_abi: String? = "",
    val display: String? = "",
    val hardware: String? = "",
)