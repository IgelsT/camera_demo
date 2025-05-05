package requests

type ApiJsonRequest[T any] struct {
	Data T `form:"data" json:"data" xml:"data" binding:"required"`
}

type RegisterRequest struct {
	User_email    string `form:"user_email" json:"user_email" xml:"user_email" binding:"required"`
	User_password string `json:"user_password" binding:"required"`
}

type ConfirmEmailRequest struct {
	Hash string `form:"hash" json:"hash" xml:"hash" binding:"required"`
}

type RecoveryRequest struct {
	User_email string `form:"user_email" json:"user_email" xml:"user_email" binding:"required"`
}

type LoginRequest struct {
	User_email    string `form:"user_email" json:"user_email" xml:"user_email" binding:"required"`
	User_password string `json:"user_password" binding:"required"`
}

type LoginDeviceRequest struct {
	User_email    string `form:"user_email" json:"user_email" xml:"user_email" binding:"required"`
	User_password string `json:"user_password" binding:"required"`
	Device_uid    string `json:"device_uid" binding:"required"`
}

type SaveProfileRequest struct {
	User_password string `json:"user_password" binding:"required"`
}
