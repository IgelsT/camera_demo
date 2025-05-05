package handlers

import (
	. "camera_api/internal/requests"
	"camera_api/internal/sender"
	"camera_api/internal/services"

	"github.com/gin-gonic/gin"
)

type DeviceHandler struct {
	DeviceService   *services.DeviceService
	MessagesService *services.MessagesService
}

func NewDeviceHandler(ds *services.DeviceService, ms *services.MessagesService) *DeviceHandler {
	return &DeviceHandler{
		DeviceService:   ds,
		MessagesService: ms,
	}
}

func (h *DeviceHandler) SetCameraList(c *gin.Context) {
	json := ChechParams(c, &CameraListRequest{})

	as := services.GetAuthService(c)
	user_id := as.GetUserId()
	device_uid := as.GetDeviceUid()
	// 320x240,352x288,640x480,704x576,960x720,960x640,1184x666,1280x720
	for _, value := range json.Camera_list {
		h.DeviceService.SetDeviceCamList(
			user_id,
			device_uid,
			value.CameraID,
			value.Facing,
			value.GetResArray(),
			value.Focuses,
		)
	}

	response := map[string]string{
		"camera_list": "ok",
	}

	sender.ApiSendResponse(c, response)
}

func (h *DeviceHandler) SetDeviceState(c *gin.Context) {
	json := ChechParams(c, &CameraStateRequest{})
	as := services.GetAuthService(c)
	user_id := as.GetUserId()

	h.DeviceService.SetDeviceState(user_id, json.State)

	response := map[string]string{
		"setDeviceStateResponse": "ok",
	}

	sender.ApiSendResponse(c, response)
}

func (h *DeviceHandler) SetDeviceInfo(c *gin.Context) {
	json := ChechParams(c, &CameraInfoRequest{})
	as := services.GetAuthService(c)
	device_id := as.GetDeviceId()
	user_id := as.GetUserId()

	h.DeviceService.UpdateInfo(user_id, device_id, json.Info)

	response := map[string]string{
		"SetDeviceInfo": "ok",
	}

	sender.ApiSendResponse(c, response)
}

func (h *DeviceHandler) Ping(c *gin.Context) {
	as := services.GetAuthService(c)
	user_id := as.GetUserId()
	device_id := as.GetDeviceId()
	device_uid := as.GetDeviceUid()

	h.DeviceService.UpdateDeviceTime(user_id, device_id)

	response := map[string]any{
		"Ping": "ok",
	}

	messages := h.MessagesService.GetMessagesToSend(user_id, device_uid)
	if len(messages) != 0 {
		response["messages"] = messages
	}

	sender.ApiSendResponse(c, response)
}

func (h *DeviceHandler) AppliedMessages(c *gin.Context) {
	json := ChechParams(c, &AppliedMessagesRequest{})
	as := services.GetAuthService(c)
	user_id := as.GetUserId()
	device_uid := as.GetDeviceUid()

	h.MessagesService.ApplyMessages(user_id, device_uid, json.Messages)

	response := map[string]any{
		"AppliedMessages": "ok",
	}

	sender.ApiSendResponse(c, response)
}

func (h *DeviceHandler) ExecutedMessages(c *gin.Context) {
	json := ChechParams(c, &ExecuteMessageRequest{})
	as := services.GetAuthService(c)
	user_id := as.GetUserId()
	device_uid := as.GetDeviceUid()

	h.MessagesService.ExecutedMessage(user_id, device_uid, json.Message_id)

	response := map[string]any{
		"AppliedMessages": "ok",
	}

	sender.ApiSendResponse(c, response)
}

func (h *DeviceHandler) SetLogList(c *gin.Context) {
	json := ChechParams(c, &LogListRequest{})
	as := services.GetAuthService(c)
	device_id := as.GetDeviceId()

	h.DeviceService.UpdateLogList(json.Logs, device_id)

	response := map[string]any{
		"SetLogList": "ok",
	}

	sender.ApiSendResponse(c, response)
}

func (h *DeviceHandler) SendLog(c *gin.Context) {
	// $result = false;
	// // LogClass::LogV($_FILES);
	// foreach ($_FILES as $file) {
	// 	$path = config('app.app_settings.device_logpath') . $this->device_uid;
	// 	if (!is_dir($path)) {
	// 		mkdir($path);
	// 	}
	// 	$target = $path . "/" . $file['name'];
	// 	$result = move_uploaded_file($_FILES['file']['tmp_name'], $target);
	// }
	// return ['result' => $result];
}
