package errors

import (
	"fmt"
	"strconv"
)

// HTTP_ERROR_CODES
// [200, "OK"]
// [204, "No Content"]
// [400, "Bad Request"]
// [401, "Unauthorized"]
// [403, "Forbidden"]
// [404, "Not Found"]
// 405 Method Not Allowed
// 409 Conflict
// 422 Unprocessable Entity
// [500, "Internal Server Error"]
// 501 Not Implemented

var ENV_FILE_NOTFOUND = [...]string{"ENV_FILE_NOTFOUND", ".env file not found", "500"}
var JSON_DECODE_ERROR = [...]string{"JSON_DECODE_ERROR", "json decode error", "400"}
var WRONG_REQUEST = [...]string{"WRONG_REQUEST", "wrong request", "400"}
var PARAM_REQUIRED = [...]string{"PARAM_REQUIRED", "param required", "422"}
var INTERNAL_ERROR = [...]string{"INTERNAL_ERROR", "internal error", "500"}
var NO_RETURN_DATA = [...]string{"NO_RETURN_DATA", "no return data", "500"}
var WRONG_RETURN_DATA = [...]string{"WRONG_RETURN_DATA", "wrong return data, must be array", "500"}
var CREATE_MODEL_ERRROR = [...]string{"CREATE_MODEL_ERRROR", "empty required fields", "500"}
var DB_CONNECTION_ERROR = [...]string{"DB_CONNECTION_ERROR", "DB connection error", "500"}
var DB_REQUEST_ERROR = [...]string{"DB_REQUEST_ERROR", "DB request error", "500"}
var BAD_TOKEN = [...]string{"BAD_TOKEN", "bad token", "401"}
var EMAIL_EXIST = [...]string{"EMAIL_EXIST", "email exist", "409"}
var EMAIL_NOT_EXIST = [...]string{"EMAIL_NOT_EXIST", "email не зарегистрирован!", "404"}
var EMAIL_SEND_ERROR = [...]string{"EMAIL_SEND_ERROR", "email send error", "500"}
var WRONG_PASSWORD = [...]string{"WRONG_PASSWORD", "Не правильный email/пароль!", "403"}
var EMPTY_PASSWORD = [...]string{"EMPTY_PASSWORD", "empty password!", "400"}
var INVALID_HASH = [...]string{"WRONG_HASH", "wrong hash", "403"}
var USER_NOT_CONFIRM = [...]string{"NOT_CONFIRMED", "Аккаунт не подтвержден!", "401"}
var FILESYSTEM_ERROR = [...]string{"FILESYSTEM_ERROR", "error on filesistem operation", "500"}
var DEVICE_NOT_FOUND = [...]string{"DEVICE_NOT_FOUND", "Device not found!", "404"}
var ERROR_REQUEST_PARAMS = [...]string{"ERROR_REQUEST_PARAMS", "error in request params", "400"}
var ERROR_SEND_TO_DEVICE = [...]string{"ERROR_SEND_TO_DEVICE", "Error send to device!", "400"}
var FILE_NOT_FOUND = [...]string{"FILE_NOT_FOUND", "File not found!", "404"}
var FILE_UPLOAD_ERROR = [...]string{"FILE_UPLOAD_ERROR", "File upload error!", "500"}
var DEVICE_LIMIT = [...]string{"DEVICE_LIMIT", "device limit reached", "403"}

type ApiError struct {
	Code     string `json:"code" binding:"required"`
	HTTPCode int    `json:"-"`
	Message  string `json:"message" binding:"required"`
	Reason   string `json:"reason" binding:"required"`
	Debug    any    `json:"debug" binding:"required"`
}

func (e *ApiError) Error() string {
	return fmt.Sprintf("Api Errror: %s %s %s", e.Code, e.Message, e.Reason)
}

func ErrorFromCode(err [3]string, reason ...string) *ApiError {
	e := &ApiError{
		Code:    err[0],
		Message: err[1],
	}

	if code, err := strconv.Atoi(err[2]); err == nil {
		e.HTTPCode = code
	} else {
		e.HTTPCode = 200
	}

	if len(reason) > 0 {
		e.Reason = reason[0]
	}

	return e
}

func ErrorFromError(err error, reason ...string) *ApiError {
	e := &ApiError{
		Code:     "INTERNAL_ERROR",
		Message:  "internal error",
		HTTPCode: 500,
		Reason:   err.Error(),
	}

	if len(reason) > 0 {
		e.Reason = reason[0]
	}

	return e
}
