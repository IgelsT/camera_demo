package requests

import (
	"encoding/json"
	"fmt"
)

type CameraListRequest struct {
	Camera_list []Camera_info `json:"cameralist" binding:"required"`
}

type Camera_info struct {
	CameraID int          `json:"cameraID" binding:"required"`
	Facing   string       `json:"facing" binding:"required"`
	Focuses  []string     `json:"focuses" binding:"required"`
	Res      []Camera_res `json:"res" binding:"required"`
}

type Camera_res struct {
	First  int `json:"first" binding:"required"`
	Second int `json:"second" binding:"required"`
}

func (s *Camera_res) ToString() string {
	return fmt.Sprintf("%dx%d", s.First, s.Second)
}

func (s *Camera_info) GetResArray() []string {
	var result []string
	for _, value := range s.Res {
		result = append(result, value.ToString())
	}
	return result
}

type CameraStateRequest struct {
	State Camera_state `json:"state" binding:"required"`
}

type Camera_state struct {
	Device_camera_id   int         `json:"device_camera_id" binding:"required"`
	Device_description string      `json:"device_description" binding:"required"`
	Device_focus       string      `json:"device_focus" binding:"required"`
	Device_fps         int         `json:"device_fps" binding:"required"`
	Device_location    string      `json:"device_location" binding:"required"`
	Device_name        string      `json:"device_name" binding:"required"`
	Device_orientation string      `json:"device_orientation" binding:"required"`
	Device_power       int         `json:"device_power" binding:"required"`
	Device_quality     int         `json:"device_quality" binding:"required"`
	Device_resolution  string      `json:"device_resolution" binding:"required"`
	Device_status      json.Number `json:"device_status,omitempty" binding:"required"`
	Device_uid         string      `json:"device_uid" binding:"required"`
}

type CameraInfoRequest struct {
	Info Device_info `json:"info" binding:"required"`
}

type Device_info struct {
	Android_id      string      `json:"android_id"`
	Board           string      `json:"board"`
	Brand           string      `json:"brand"`
	Cpu_abi         string      `json:"cpu_abi"`
	Device          string      `json:"device"`
	Display         string      `json:"display"`
	Fingerprint     string      `json:"fingerprint"`
	Hardware        string      `json:"hardware"`
	Host            string      `json:"host"`
	Id              string      `json:"id"`
	Manufacturer    string      `json:"manufacturer"`
	Model           string      `json:"model"`
	Osversion       string      `json:"osversion"`
	Product         string      `json:"product"`
	Serial          string      `json:"serial"`
	Type            string      `json:"type"`
	User            string      `json:"user"`
	Version_codes   json.Number `json:"version_codes"`
	Version_release string      `json:"version_release"`
	Versionsdk      string      `json:"versionsdk"`
}

type AppliedMessagesRequest struct {
	Messages []int `json:"messages" binding:"required"`
}

type ExecuteMessageRequest struct {
	Message_id int `json:"message_id" binding:"required"`
}

type LogListRequest struct {
	Logs []string `json:"logs" binding:"required"`
}
