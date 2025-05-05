package handlers

import (
	"camera_api/internal/sender"
	"camera_api/internal/services"

	"github.com/gin-gonic/gin"
)

type DashboardHandler struct {
	DashboardService *services.DashboardService
}

func NewDashboardHandler(ds *services.DashboardService) *DashboardHandler {
	return &DashboardHandler{
		DashboardService: ds,
	}
}

func (h *DashboardHandler) Index(c *gin.Context) {
	authService := services.GetAuthService(c)
	user_id := authService.GetUserId()
	list := h.DashboardService.GetDeviceList(user_id)

	response := map[string]any{
		"devicelist": list,
	}

	sender.ApiSendResponse(c, response)
}
