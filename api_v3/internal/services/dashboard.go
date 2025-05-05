package services

import (
	QB "camera_api/internal/query_bulder"
)

type DashRows struct {
	DashId   string `db:"dash_id"`
	DeviceId int    `db:"device_id"`
	UserId   int    `db:"user_id"`
}

type DashList struct {
	DeviceId          int    `db:"device_id" json:"device_id"`
	DeviceUid         string `db:"device_uid" json:"device_uid"`
	DeviceName        string `db:"device_name" json:"device_name"`
	DeviceResolution  string `db:"device_resolution" json:"device_resolution"`
	DeviceOrientation string `db:"device_orientation" json:"device_orientation"`
	DeviceFps         int    `db:"device_fps" json:"device_fps"`
	DeviceQuality     int    `db:"device_quality" json:"device_quality"`
	DevicePower       int    `db:"device_power" json:"device_power"`
}

type DashboardService struct{}

func NewDashboardService() *DashboardService {
	return &DashboardService{}
}

func (s *DashboardService) GetDeviceList(user_id int) any {
	sql := `SELECT devices.device_id, device_uid, device_name
			, device_resolution, device_orientation, device_fps, device_quality, device_power
			FROM dashboard
			INNER JOIN devices ON devices.device_id = dashboard.device_id
			INNER JOIN device_state ON devices.device_id = device_state.device_id
			WHERE dashboard.user_id = ?`
	return QB.GET[DashList](sql).Params(user_id).All()
}

func (s *DashboardService) SetToDash(user_id int, device_id int, is_dash int) {
	if is_dash == 0 {
		sql := "DELETE FROM dashboard WHERE user_id = ? AND device_id = ?"
		QB.DELETE(sql).Params(user_id, device_id)
	} else {
		sql := "SELECT * FROM dashboard WHERE user_id = ? AND device_id = ?"
		result := QB.GET[DashRows](sql).Params(user_id, device_id).All()
		if len(result) == 0 {
			sql := "INSERT INTO dashboard(user_id, device_id) VALUES(?,?)"
			QB.INSERT(sql).Params(user_id, device_id)
		}
	}
}
