package requests

import "encoding/json"

type CameraInfoFrontRequest struct {
	Device_id int `json:"device_id" binding:"required"`
}

type CameraSaveParamsRequest struct {
	Device_id          int         `json:"device_id" binding:"required"`
	Device_name        string      `json:"device_name" binding:"required"`
	Device_description string      `json:"device_description" binding:"required"`
	Device_access      int         `json:"device_access" binding:"required"`
	Device_camera_id   *int        `json:"device_camera_id" binding:"required"`
	Device_focus       string      `json:"device_focus" binding:"required"`
	Device_resolution  string      `json:"device_resolution" binding:"required"`
	Device_orientation string      `json:"device_orientation" binding:"required"`
	Device_fps         int         `json:"device_fps" binding:"required"`
	Device_quality     int         `json:"device_quality" binding:"required"`
	Device_status      json.Number `json:"device_status,omitempty" binding:"required"`
	On_dash            *int        `json:"on_dash" binding:"required"`
}

type DeviceDeleteFrontRequest struct {
	Device_id int `json:"device_id" binding:"required"`
}

type MsgDeleteFrontRequest struct {
	Device_id int `json:"device_id" binding:"required"`
}

type LogListFrontRequest struct {
	Device_id int `json:"device_id" binding:"required"`
}

type LogListRequestFrontRequest struct {
	Device_id int `json:"device_id" binding:"required"`
}

type LogFileRequestFrontRequest struct {
	Device_id int    `json:"device_id" binding:"required"`
	File_name string `json:"file_name" binding:"required"`
}

type LogFileDownloadFrontRequest struct {
	Device_id int    `json:"device_id" binding:"required"`
	Filename  string `json:"filename" binding:"required"`
}
