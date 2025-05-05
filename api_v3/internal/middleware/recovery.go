package middleware

import (
	"camera_api/internal/errors"
	"camera_api/internal/sender"
	"fmt"

	"github.com/gin-gonic/gin"
)

func Recovery() gin.HandlerFunc {
	return recoveryWithWriter()
}

func recoveryWithWriter() gin.HandlerFunc {
	return func(c *gin.Context) {
		defer func() {
			if err := recover(); err != nil {
				// fmt.Println(string(debug.Stack()))
				if apiError, ok := err.(*errors.ApiError); ok {
					sender.ApiSendError(c, apiError)
					// fmt.Println("!!!!!!!!!!! Custom RECOVERY !!!! " + apiError.Error())
					// httpRequest, _ := httputil.DumpRequest(c.Request, false)
				} else {
					switch e := err.(type) {
					case error:
						sender.ApiSendError(c, errors.ErrorFromError(e))
					default:
						sender.ApiSendError(c, errors.ErrorFromCode(errors.INTERNAL_ERROR, fmt.Sprintf("%s", e)))
					}
				}
			}
		}()
		c.Next()
	}
}
