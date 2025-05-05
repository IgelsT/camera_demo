package services

import (
	"camera_api/internal/errors"
	QB "camera_api/internal/query_bulder"
	"camera_api/internal/requests"
	"camera_api/pkg/utils"
	"encoding/base64"
	"encoding/json"
	"strings"
)

type Devices struct {
	DeviceId          int     `db:"device_id"`
	DeviceUid         string  `db:"device_uid"`
	DeviceName        *string `db:"device_name"`
	DeviceDescription *string `db:"device_description"`
	DeviceInfo        *string `db:"device_info"`
	DeviceAccess      int     `db:"device_access"`
	UserId            int     `db:"user_id"`
	DeviceToken       string  `db:"device_token"`
	DeviceDeleted     int     `db:"device_deleted"`
}

type DeviceCamera struct {
	CameraId          int    `db:"camera_id" json:"camera_id"`
	CameraNum         int    `db:"camera_num" json:"camera_num"`
	CameraType        string `db:"camera_type" json:"camera_type"`
	CameraResolutions string `db:"camera_resolutions" json:"camera_resolutions"`
	CameraFocuses     string `db:"camera_focuses" json:"camera_focuses"`
	DeviceId          int    `db:"device_id" json:"device_id"`
}

type DeviceService struct{}

var deviceLimit = 3

// var publicDevices = 1

func NewDeviceService() *DeviceService {
	return &DeviceService{}
}

func (s *DeviceService) UpdateDevice(device *Devices) {
	sql := `UPDATE devices SET device_uid=:device_uid, device_name=:device_name,
        device_description=:device_description, device_info=:device_info, device_access=:device_access,
		user_id=:user_id, device_deleted=:device_deleted, device_token=:device_token 
		WHERE device_id=:device_id`
	QB.UPDATE(sql).Named(device)
}

func (s *DeviceService) CreateDevice(device *Devices) {
	sql := `INSERT INTO devices (device_uid, device_name, device_description, device_info, device_access, user_id, device_token, device_deleted)
	VALUES(:device_uid, :device_name, :device_description, :device_info, :device_access, :user_id, 
	:device_token, :device_deleted)`
	QB.INSERT(sql).Named(device)
}

// #region --------------------- mobile app ----------------------------
func (s *DeviceService) genToken(device_uid string, user_email string) string {
	json, err := json.Marshal(map[string]string{"user": user_email, "device": device_uid})
	if err != nil {
		panic(err)
	}
	firstPath := base64.StdEncoding.EncodeToString([]byte(json))
	secondPath := utils.RandomPassword(15)
	return firstPath + "." + secondPath
}

func (s *DeviceService) GetDeviceToken(device_uid string, user_id int, user_email string) string {
	token := s.genToken(device_uid, user_email)

	device := s.GetDeviceByUID(user_id, device_uid, true)
	if device == nil {
		device = &Devices{
			UserId:        user_id,
			DeviceUid:     device_uid,
			DeviceDeleted: 0,
			DeviceAccess:  1,
			DeviceToken:   token,
		}
		s.CreateDevice(device)
	} else {
		device.DeviceToken = token
		s.UpdateDevice(device)
	}

	return token
}

func (s *DeviceService) SetDeviceCamList(
	user_id int,
	device_uid string,
	camera_num int,
	camera_type string,
	resolutions []string,
	focuses []string,
) {
	s.CheckDeviceLimit(user_id, device_uid)
	res := strings.Join(resolutions, ",")
	foc := strings.Join(focuses, ",")
	QB.CallPRC("pr_update_camera", user_id, device_uid, camera_num, camera_type, res, foc)
}

func (s *DeviceService) SetDeviceState(user_id int, state requests.Camera_state) {
	s.CheckDeviceLimit(user_id, state.Device_uid)
	QB.CallPRC("pr_update_device_state",
		user_id,
		state.Device_uid,
		state.Device_name,
		state.Device_description,
		state.Device_camera_id,
		state.Device_focus,
		state.Device_resolution,
		state.Device_orientation,
		state.Device_fps,
		state.Device_quality,
		state.Device_power,
		state.Device_status,
		state.Device_location,
	)
}

func (s *DeviceService) UpdateInfo(user_id int, device_id int, info requests.Device_info) {
	device := s.CheckDeviceByID(device_id, user_id)

	json, err := json.Marshal(info)
	if err != nil {
		panic(err)
	}

	sql := "UPDATE devices SET device_info = ? WHERE device_id = ?"
	QB.UPDATE(sql).Params(json, device.DeviceId)
}

func (s *DeviceService) UpdateDeviceTime(user_id int, device_id int) {
	sql := "UPDATE device_state SET device_lastactivity = NOW()"
	QB.UPDATE(sql).Params()
}

//#endregion --------------------- mobile app ----------------------------

//#region --------------------- Check device ----------------------------

func (s *DeviceService) GetDeviceByID(device_id int, all ...bool) *Devices {
	sql := "SELECT * FROM devices WHERE device_id=?"
	if len(all) == 0 || !all[0] {
		sql = sql + " AND device_deleted = 0"
	}
	return QB.GET[Devices](sql).Params(device_id).One()
}

func (s *DeviceService) GetDeviceByUID(user_id int, device_uid string, all ...bool) *Devices {
	sql := "SELECT * FROM devices WHERE device_uid=? AND user_id=?"
	if len(all) == 0 || !all[0] {
		sql = sql + " AND device_deleted = 0"
	}
	return QB.GET[Devices](sql).Params(device_uid, user_id).One()
}

func (s *DeviceService) GetDeviceByToken(device_token string, all ...bool) *Devices {
	sql := "SELECT * FROM devices WHERE device_token=?"
	if len(all) == 0 || !all[0] {
		sql = sql + " AND device_deleted = 0"
	}
	return QB.GET[Devices](sql).Params(device_token).One()
}

func (s *DeviceService) CheckDeviceLimit(user_id int, device_uid string) {
	sql := "SELECT * FROM devices WHERE user_id = ? AND device_uid != ? AND device_deleted = 0"
	result := QB.GET[Devices](sql).Params(user_id, device_uid).All()
	if len(result) >= deviceLimit {
		panic(errors.ErrorFromCode(errors.DEVICE_LIMIT))
	}
}

func (s *DeviceService) CheckDeviceByID(device_id int, user_id int) *Devices {
	device := s.GetDeviceByID(device_id)
	if device == nil {
		panic(errors.ErrorFromCode(errors.DEVICE_NOT_FOUND))
	}

	if device.UserId != user_id {
		panic(errors.ErrorFromCode(errors.DEVICE_NOT_FOUND))
	}
	return device
}

func (s *DeviceService) CheckDeviceByUID(user_id int, device_uid string) *Devices {
	device := s.GetDeviceByUID(user_id, device_uid)
	if device == nil {
		panic(errors.ErrorFromCode(errors.DEVICE_NOT_FOUND))
	}
	return device
}

//#endregion --------------------- Check device ----------------------------

//#region ------------------------ Logs ------------------------------------

type DeviceLogs struct {
	LogId    int    `db:"log_id"`
	LogName  string `db:"log_name"`
	DeviceId int    `db:"device_id"`
}

func (s *DeviceService) UpdateLogList(logList []string, device_id int) {
	sql := "DELETE FROM device_logs WHERE device_id = ?"
	QB.DELETE(sql).Params(device_id)

	for _, log := range logList {
		sql := "INSERT INTO device_logs(device_id, log_name) VALUES(?,?)"
		QB.INSERT(sql).Params(device_id, log)
	}
}

func (s *DeviceService) GetLogList(device_id int, device_uid string) []DeviceLogs {
	sql := "SELECT * from device_logs WHERE device_id = ?"
	logList := QB.GET[DeviceLogs](sql).Params(device_id).All()
	return logList
}

//#endregion ------------------------ Logs ------------------------------------

//#region --------------------- Front ----------------------------

type DeviceList struct {
	DeviceId     int     `db:"device_id" json:"device_id"`
	DeviceName   *string `db:"device_name" json:"device_name"`
	DevicePower  int     `db:"device_power" json:"device_power"`
	DeviceStatus int     `db:"device_status" json:"device_status"`
	DeviceOnline int     `db:"device_online" json:"device_online"`
}

type DeviceInfo struct {
	DeviceId           int     `db:"device_id" json:"device_id"`
	DeviceUid          string  `db:"device_uid" json:"device_uid"`
	DeviceName         *string `db:"device_name" json:"device_name"`
	DeviceDescription  *string `db:"device_description" json:"device_description"`
	DeviceInfo         *string `db:"device_info" json:"device_info"`
	DeviceAccess       int     `db:"device_access" json:"device_access"`
	DeviceCameraId     int     `db:"device_camera_id" json:"device_camera_id"`
	DeviceFocus        string  `db:"device_focus" json:"device_focus"`
	DeviceResolution   string  `db:"device_resolution" json:"device_resolution"`
	DeviceOrientation  string  `db:"device_orientation" json:"device_orientation"`
	DeviceFps          int     `db:"device_fps" json:"device_fps"`
	DeviceQuality      int     `db:"device_quality" json:"device_quality"`
	DevicePower        int     `db:"device_power" json:"device_power"`
	DeviceStatus       int     `db:"device_status" json:"device_status"`
	DeviceLastactivity string  `db:"device_lastactivity" json:"device_lastactivity"`
	DeviceOnline       int     `db:"device_online" json:"device_online"`
	OnDash             int     `db:"on_dash" json:"on_dash"`
}

func (s *DeviceService) GetDeviceList(user_id int) []DeviceList {
	sql := `SELECT devices.device_id, device_name, device_power, device_status, device_online
		  FROM devices
		  LEFT JOIN device_state ON devices.device_id = device_state.device_id
		  WHERE device_deleted = 0 AND user_id = ?`

	return QB.GET[DeviceList](sql).Params(user_id).All()
}

func (s *DeviceService) GetDeviceInfo(user_id int, device_id int) *DeviceInfo {
	sql := `SELECT devices.device_id, device_uid, device_name, device_description, device_info, device_access
		, device_camera_id, device_focus, device_resolution, device_orientation, device_fps, device_quality
		, device_power, device_status, device_lastactivity, device_online
		, if(ifnull(dashboard.dash_id, 0)=0,0,1) AS on_dash
		FROM devices
		LEFT JOIN device_state ON devices.device_id = device_state.device_id
		LEFT JOIN dashboard ON devices.device_id = dashboard.device_id AND dashboard.user_id = devices.user_id
		WHERE devices.user_id = ? AND devices.device_id = ?`
	return QB.GET[DeviceInfo](sql).Params(user_id, device_id).One()
}

func (s *DeviceService) GetDeviceCams(device_id int) []DeviceCamera {
	sql := "SELECT * FROM device_camera WHERE device_id = ?"

	return QB.GET[DeviceCamera](sql).Params(device_id).All()
}

func (s *DeviceService) SetAccess(access int, device_id int) {
	sql := "UPDATE devices SET device_access = ? WHERE device_id = ?"
	QB.UPDATE(sql).Params(access, device_id)
}

func (s *DeviceService) SendSettiongsToDevice(user_id int, device_id int, device_uid string, device_state interface{}) {
	message := map[string]interface{}{
		"action": "settings",
		"data":   device_state,
	}
	messageJson, err := json.Marshal(message)
	if err != nil {
		panic(err)
	}

	QB.CallPRC("pr_message_to_device", user_id, device_id, device_uid, messageJson)
}

func (s *DeviceService) DeleteDevice(device_id int) {
	sql := "UPDATE devices SET device_deleted = 1 WHERE device_id = ?"
	QB.UPDATE(sql).Params(device_id)
}

func (s *DeviceService) SendRequestLogsToDevice(user_id int, device_id int, device_uid string) {
	message := map[string]interface{}{
		"action": "getLogs",
		"data":   nil,
	}
	messageJson, err := json.Marshal(message)
	if err != nil {
		panic(err)
	}

	QB.CallPRC("pr_message_to_device", user_id, device_id, device_uid, messageJson)
}

func (s *DeviceService) SendRequestLogFileToDevice(user_id int, device_id int, device_uid string, filename string) {
	message := map[string]interface{}{
		"action": "getLogs",
		"data":   map[string]string{"file_name": filename},
	}
	messageJson, err := json.Marshal(message)
	if err != nil {
		panic(err)
	}

	QB.CallPRC("pr_message_to_device", user_id, device_id, device_uid, messageJson)
}

//#endregion --------------------- Front ----------------------------
