package sender

import (
	"camera_api/internal/errors"
	"camera_api/pkg/appconfig"
	"net/http"
	"runtime/debug"

	"github.com/gin-gonic/gin"
)

type successResponse struct {
	Action string `json:"action" binding:"required"`
	Code   int    `json:"code" binding:"required"`
	Data   any    `json:"data" binding:"required"`
}

type errorResponse struct {
	Action string           `json:"action" binding:"required"`
	Code   int              `json:"code" binding:"required"`
	Error  *errors.ApiError `json:"error" binding:"required"`
}

func ApiSendError(c *gin.Context, e *errors.ApiError) {
	resp := &errorResponse{
		Action: "",
		Code:   e.HTTPCode,
		Error:  e,
	}
	if appconfig.GetAppDevMode() && resp.Code == 500 {
		resp.Error.Debug = string(debug.Stack())
	}
	c.AbortWithStatusJSON(e.HTTPCode, resp)
}

func ApiSendResponse(c *gin.Context, content any) {
	resp := &successResponse{
		Action: "",
		Code:   200,
		Data:   content,
	}
	c.JSON(http.StatusOK, resp)
}

/**
{
  "action": "",
  "code": 200,
  "data": {
    "register": "ok",
    "confirm": "624ae3d4b616db9a61169565533390b4"
  }
}
*/

/*
{
	"action": "",
	"code": 409,
	"error": {
	  "code": "EMAIL_EXIST",
	  "message": "email exist",
	  "reason": "",
	  "debug":
	  }
} */
