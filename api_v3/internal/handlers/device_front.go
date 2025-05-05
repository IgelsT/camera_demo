package handlers

import (
	"camera_api/internal/errors"
	. "camera_api/internal/requests"
	"camera_api/internal/sender"
	"camera_api/internal/services"
	"camera_api/pkg/appconfig"
	"encoding/base64"
	"os"

	"github.com/gin-gonic/gin"
)

type DeviceFrontHandler struct {
	DeviceService    *services.DeviceService
	MessagesService  *services.MessagesService
	DashboardService *services.DashboardService
}

func NewFrontDeviceHandler(ds *services.DeviceService, ms *services.MessagesService, dbs *services.DashboardService) *DeviceFrontHandler {
	return &DeviceFrontHandler{
		DeviceService:    ds,
		MessagesService:  ms,
		DashboardService: dbs,
	}

}

func (h *DeviceFrontHandler) Index(c *gin.Context) {
	as := services.GetAuthService(c)
	user_id := as.GetUserId()

	list := h.DeviceService.GetDeviceList(user_id)

	response := map[string]any{
		"devicelist": list,
	}

	sender.ApiSendResponse(c, response)
}

func (h *DeviceFrontHandler) Info(c *gin.Context) {
	json := ChechParams(c, &CameraInfoFrontRequest{})
	as := services.GetAuthService(c)
	user_id := as.GetUserId()

	info := h.DeviceService.GetDeviceInfo(user_id, json.Device_id)

	if info == nil {
		panic(errors.ErrorFromCode(errors.DEVICE_NOT_FOUND))
	}

	camlist := h.DeviceService.GetDeviceCams(json.Device_id)

	msglist := h.MessagesService.GetMessagesToSend(user_id, info.DeviceUid)

	response := map[string]any{
		"deviceinfo": info,
		"devicecams": camlist,
		"devicemsg":  msglist,
	}

	sender.ApiSendResponse(c, response)
}

func (h *DeviceFrontHandler) SaveParams(c *gin.Context) {
	json := ChechParams(c, &CameraSaveParamsRequest{})
	as := services.GetAuthService(c)
	user_id := as.GetUserId()

	device := h.DeviceService.CheckDeviceByID(json.Device_id, user_id)

	h.DeviceService.SetAccess(json.Device_access, device.DeviceId)
	h.DashboardService.SetToDash(user_id, device.DeviceId, *json.On_dash)

	deviceState := map[string]interface{}{
		"device_name":        json.Device_name,
		"device_description": json.Device_description,
		"device_camera_id":   json.Device_camera_id,
		"device_focus":       json.Device_focus,
		"device_resolution":  json.Device_resolution,
		"device_orientation": json.Device_orientation,
		"device_fps":         json.Device_fps,
		"device_quality":     json.Device_quality,
		"device_status":      json.Device_status,
		"rtmp_address":       appconfig.GetAppConfig().FrontRTMPAddress,
	}

	h.DeviceService.SendSettiongsToDevice(user_id, device.DeviceId, device.DeviceUid, deviceState)
	msglist := h.MessagesService.GetMessagesToSend(user_id, device.DeviceUid)
	response := map[string]any{
		"devicemsg": msglist,
	}

	sender.ApiSendResponse(c, response)
}

func (h *DeviceFrontHandler) Delete(c *gin.Context) {
	json := ChechParams(c, &DeviceDeleteFrontRequest{})
	as := services.GetAuthService(c)
	user_id := as.GetUserId()

	device := h.DeviceService.CheckDeviceByID(json.Device_id, user_id)

	h.DeviceService.DeleteDevice(device.DeviceId)

	response := map[string]any{
		"deviceDelete": "ok",
	}

	sender.ApiSendResponse(c, response)

}

func (h *DeviceFrontHandler) DelMsg(c *gin.Context) {
	json := ChechParams(c, &MsgDeleteFrontRequest{})
	as := services.GetAuthService(c)
	user_id := as.GetUserId()

	device := h.DeviceService.CheckDeviceByID(json.Device_id, user_id)

	h.MessagesService.DeleteMessages(user_id, device.DeviceUid)

	response := map[string]any{
		"DelMsg": "ok",
	}

	sender.ApiSendResponse(c, response)
}

func (h *DeviceFrontHandler) LogsList(c *gin.Context) {
	json := ChechParams(c, &LogListFrontRequest{})
	as := services.GetAuthService(c)
	user_id := as.GetUserId()

	device := h.DeviceService.CheckDeviceByID(json.Device_id, user_id)
	loglist := h.DeviceService.GetLogList(device.DeviceId, device.DeviceUid)
	result := []map[string]interface{}{}

	logPath := appconfig.GetAppConfig().FrontDeviceLogPath
	for _, log := range loglist {
		str := map[string]interface{}{
			"log_id":    log.LogId,
			"log_name":  log.LogName,
			"device_id": log.DeviceId,
		}

		if _, err := os.Stat(logPath + device.DeviceUid + "/" + log.LogName); err == nil {
			str["file"] = true
		} else {
			str["file"] = false
		}

		result = append(result, str)
	}

	msglist := h.MessagesService.GetMessagesToSend(user_id, device.DeviceUid)

	response := map[string]any{
		"loglist":   result,
		"devicemsg": msglist,
	}

	sender.ApiSendResponse(c, response)
}

func (h *DeviceFrontHandler) RequestLogs(c *gin.Context) {
	json := ChechParams(c, &LogListRequestFrontRequest{})
	as := services.GetAuthService(c)
	user_id := as.GetUserId()

	device := h.DeviceService.CheckDeviceByID(json.Device_id, user_id)
	h.DeviceService.SendRequestLogsToDevice(user_id, device.DeviceId, device.DeviceUid)

	msglist := h.MessagesService.GetMessagesToSend(user_id, device.DeviceUid)
	response := map[string]any{
		"devicemsg": msglist,
	}

	sender.ApiSendResponse(c, response)
}

func (h *DeviceFrontHandler) RequestLogFile(c *gin.Context) {
	json := ChechParams(c, &LogFileRequestFrontRequest{})
	as := services.GetAuthService(c)
	user_id := as.GetUserId()

	device := h.DeviceService.CheckDeviceByID(json.Device_id, user_id)
	h.DeviceService.SendRequestLogFileToDevice(user_id, device.UserId, device.DeviceUid, json.File_name)

	msglist := h.MessagesService.GetMessagesToSend(user_id, device.DeviceUid)
	response := map[string]any{
		"devicemsg": msglist,
	}

	sender.ApiSendResponse(c, response)
}

func (h *DeviceFrontHandler) LogFile(c *gin.Context) {
	json := ChechParams(c, &LogFileDownloadFrontRequest{})
	as := services.GetAuthService(c)
	user_id := as.GetUserId()

	device := h.DeviceService.CheckDeviceByID(json.Device_id, user_id)

	logPath := appconfig.GetAppConfig().FrontDeviceLogPath
	fullFilename := logPath + device.DeviceUid + "/" + json.Filename

	if _, err := os.Stat(logPath + device.DeviceUid + "/" + json.Filename); err != nil {
		panic(errors.ErrorFromCode(errors.FILE_NOT_FOUND))
	}

	fileStr, err := os.ReadFile(fullFilename)

	if err != nil {
		panic(errors.ErrorFromCode(errors.FILE_NOT_FOUND))
	}

	file64 := base64.StdEncoding.EncodeToString([]byte(fileStr))

	response := map[string]any{
		"file_name": json.Filename,
		"file64":    file64,
	}

	sender.ApiSendResponse(c, response)
}
