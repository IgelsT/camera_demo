package handlers

import (
	"camera_api/internal/errors"
	. "camera_api/internal/requests"
	"camera_api/internal/sender"
	"camera_api/internal/services"
	"camera_api/pkg/appconfig"
	"camera_api/pkg/utils"
	"strconv"

	"github.com/gin-gonic/gin"
)

type AuthHandler struct {
	UserService   *services.UserService
	NotifyService *services.NotifyService
	DeviceService *services.DeviceService
}

func NewAuthHandler(us *services.UserService, ns *services.NotifyService, ds *services.DeviceService) *AuthHandler {
	return &AuthHandler{
		UserService:   us,
		NotifyService: ns,
		DeviceService: ds,
	}
}

func (h *AuthHandler) Register(c *gin.Context) {
	json := ChechParams(c, &RegisterRequest{})

	// Check if user exist
	user := h.UserService.UserByEmail(json.User_email)
	if user != nil {
		panic(errors.ErrorFromCode(errors.EMAIL_EXIST))
	}

	// Create user
	user = h.UserService.CreateUser(json.User_email, json.User_password)

	// Send confirm email
	if err := h.NotifyService.SendConfirm(json.User_email, user.UserHash); err != nil {
		panic(errors.ErrorFromCode(errors.EMAIL_SEND_ERROR, err.Error()))
	}

	response := map[string]string{
		"register": "ok",
	}
	if appconfig.GetAppDevMode() {
		response["confirm"] = user.UserHash
	}

	sender.ApiSendResponse(c, response)
}

func (h *AuthHandler) ConfirmEmail(c *gin.Context) {
	json := ChechParams(c, &ConfirmEmailRequest{})

	user := h.UserService.UserByHash(json.Hash)
	if user == nil {
		panic(errors.ErrorFromCode(errors.INVALID_HASH))
	}

	user.UserConfirm = 1

	h.UserService.UpdateUser(user)

	sender.ApiSendResponse(c, map[string]string{"hash": "ok"})
}

func (h *AuthHandler) Recovery(c *gin.Context) {
	json := ChechParams(c, &RecoveryRequest{})

	user := h.UserService.UserByEmail(json.User_email)
	if user == nil {
		panic(errors.ErrorFromCode(errors.EMAIL_NOT_EXIST))
	}

	if user.UserConfirm == 1 {
		password := utils.RandomPassword(6)
		h.UserService.UpdatePassword(user.UserId, utils.HashString(password))

		if err := h.NotifyService.RecoveryPassword(user.UserEmail, password); err != nil {
			panic(errors.ErrorFromCode(errors.EMAIL_SEND_ERROR, err.Error()))
		}
	} else {
		if err := h.NotifyService.SendConfirm(json.User_email, user.UserHash); err != nil {
			panic(errors.ErrorFromCode(errors.EMAIL_SEND_ERROR, err.Error()))
		}
	}

	sender.ApiSendResponse(c, map[string]string{"recovery": "recovery"})
}

func (h *AuthHandler) Login(c *gin.Context) {
	json := ChechParams(c, &LoginRequest{})

	user := h.UserService.UserByEMailPasswd(json.User_email, json.User_password)
	if user == nil {
		panic(errors.ErrorFromCode(errors.WRONG_PASSWORD))
	}
	if user.UserConfirm == 0 {
		panic(errors.ErrorFromCode(errors.USER_NOT_CONFIRM))
	}

	response := map[string]string{
		"hash":       h.UserService.MakeToken(user.UserId, json.User_email),
		"user_id":    strconv.Itoa(user.UserId),
		"user_name":  user.UserName,
		"user_email": json.User_email,
	}
	sender.ApiSendResponse(c, response)
}

func (h *AuthHandler) LoginDevice(c *gin.Context) {
	json := ChechParams(c, &LoginDeviceRequest{})

	user := h.UserService.UserByEMailPasswd(json.User_email, json.User_password)
	if user == nil {
		panic(errors.ErrorFromCode(errors.WRONG_PASSWORD))
	}

	device_token := h.DeviceService.GetDeviceToken(
		json.Device_uid,
		user.UserId,
		user.UserEmail,
	)
	if device_token == "" {
		panic(errors.ErrorFromCode(errors.INTERNAL_ERROR, "error generate device token"))
	}

	response := map[string]string{
		"device_token": device_token,
		"device_uid":   json.Device_uid,
		"user_id":      strconv.Itoa(user.UserId),
		"user_name":    user.UserName,
		"user_email":   json.User_email,
		"rtmp_address": appconfig.GetAppConfig().FrontRTMPAddress,
	}
	sender.ApiSendResponse(c, response)
}

func (h *AuthHandler) SaveProfile(c *gin.Context) {
	json := ChechParams(c, &SaveProfileRequest{})
	as := services.GetAuthService(c)

	user_id := as.GetUserId()
	h.UserService.UpdatePassword(user_id, json.User_password)
	sender.ApiSendResponse(c, map[string]string{"password": "changed"})
}
