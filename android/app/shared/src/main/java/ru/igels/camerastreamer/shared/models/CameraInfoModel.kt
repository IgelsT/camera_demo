package ru.igels.camerastreamer.shared.models

enum class CAMFACING {
    FRONT, BACK, EXTERNAL, UNKNOWN
}

enum class CAMFOCUSES {
    AUTO, INFINITE, FIXED, NORMAL
}

enum class DeviceOrientation { //What side of device is on top
    TOP, LEFT, RIGHT, BOTTOM, UNKNOWN
}

enum class QualityList {
    LOW, MEDIUM, NORMAL, HIGH, MAX
}

class CameraInfoModel(
    var cameraID: Int,
    var facing: CAMFACING,
    var res: List<Pair<Int, Int>>,
    var focuses: List<CAMFOCUSES>) {
}