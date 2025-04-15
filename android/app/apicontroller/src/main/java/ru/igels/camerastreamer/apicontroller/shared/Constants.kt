package ru.igels.camerastreamer.apicontroller.shared

enum class API_ERROR_CODES(val code: String) {
    BAD_TOKEN("BAD_TOKEN"),
    DEVICE_LIMIT("DEVICE_LIMIT"),
    WRONG_PASSWORD("WRONG_PASSWORD"),
    USER_NOT_CONFIRM("USER_NOT_CONFIRM"),
    WRONG_REQUEST("WRONG_REQUEST"),
    INTERNAL_ERROR("INTERNAL_ERROR"),

    //LOCAL
    UNKNOWN_ERROR("UNKNOWN_ERROR"),
    RESPONSE_JSON_PARSE_ERROR("RESPONSE_JSON_PARSE_ERROR"),
    NETWORK_ERROR("NETWORK_ERROR"),
    LOGIN_ERROR("LOGIN_ERROR"),
}

enum class API_MESSAGES(val code: String) {
    UNKNOWN("UNKNOWN"),
    SETTINGS("settings"),
    LOGS("getLogs"),
    LOG_FILE("getLogFile"),
}

var apiSettings: ApiSettings = ApiSettings()