package services

import (
	"camera_api/internal/errors"
	"encoding/base64"
	"encoding/json"
	"strings"

	"github.com/gin-gonic/gin"
)

const AUTH_SERVICE = "AUTH_SERVICE"

type AuthService struct {
	user   *Users
	device *Devices
}

func NewAuthService(auth string) *AuthService {
	user, device := checkAuthorization(auth)
	return &AuthService{user, device}
}

func GetAuthService(c *gin.Context) *AuthService {
	as, ok := c.Get(AUTH_SERVICE)
	if ok {
		if s, ok := as.(*AuthService); ok {
			return s
		}
	}
	panic(errors.ErrorFromCode(errors.INTERNAL_ERROR, "no auth service"))
}

func checkAuthorization(auth string) (*Users, *Devices) {
	var user *Users
	var device *Devices

	if auth == "" {
		errorAuth()
	}

	authParts := strings.Split(auth, `.`)
	if len(authParts) != 2 {
		errorAuth()
	}

	firstPath, err := base64.StdEncoding.DecodeString(authParts[0])
	if err != nil {
		errorAuth()
	}

	jsonMap := map[string]string{}
	err = json.Unmarshal(firstPath, &jsonMap)
	if err != nil {
		panic(err)
	}

	us := NewUserService()

	if _, ok := jsonMap["device"]; ok {
		device = NewDeviceService().GetDeviceByToken(auth)
		if device == nil {
			errorAuth()
		}
		user = us.UserById(device.UserId)

	} else if _, ok := jsonMap["user"]; ok {
		user = us.UserByToken(auth)
	}

	if user == nil {
		errorAuth()
	}

	if user.UserConfirm == 0 {
		panic(errors.ErrorFromCode(errors.USER_NOT_CONFIRM))
	}

	us.UpdateUserLastActivity(user.UserId)

	return user, device
}

func errorAuth() {
	panic(errors.ErrorFromCode(errors.BAD_TOKEN))
}

func (s *AuthService) GetUserId() int {
	if s.user == nil {
		panic(errors.ErrorFromCode(errors.INTERNAL_ERROR, "User not set"))
	}
	return s.user.UserId
}

func (s *AuthService) GetDeviceUid() string {
	s.CheckDevice()
	return s.device.DeviceUid
}

func (s *AuthService) CheckDevice() {
	if s.device == nil {
		panic(errors.ErrorFromCode(errors.INTERNAL_ERROR, "Device not set"))
	}
}

func (s *AuthService) GetDeviceId() int {
	s.CheckDevice()
	return s.device.DeviceId
}
