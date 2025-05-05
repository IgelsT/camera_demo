package handlers

import (
	"camera_api/internal/errors"
	. "camera_api/internal/requests"

	"github.com/gin-gonic/gin"
)

func ChechParams[T any](c *gin.Context, p T) T {
	req := &ApiJsonRequest[T]{Data: p}
	if err := c.ShouldBindJSON(req); err != nil {
		panic(errors.ErrorFromCode(errors.PARAM_REQUIRED, err.Error()))
	}
	return p
}
