package ru.igels.camerastream02.domain.models

data class CredentialsModel(
    var baseUrl: String = "",
    var userName: String = "",
    var userPassword: String = "",
    var accountToken: String = "",
    var deviceToken: String = "",
    var rtmpAddress: String = ""
) {
//    fun deepCopy(): CredentialsModel {
//        return Gson().fromJson(Gson().toJson(this), this.javaClass)
//    }
}
