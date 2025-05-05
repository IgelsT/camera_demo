package middleware

import (
	"camera_api/internal/services"

	"github.com/gin-gonic/gin"
)

func Authorization() gin.HandlerFunc {
	return func(c *gin.Context) {
		// log.Println("Authorization middleware")
		auth := c.GetHeader("Authorization")
		as := services.NewAuthService(auth)
		c.Set(services.AUTH_SERVICE, as)
		// httpRequest, _ := httputil.DumpRequest(c.Request, false)
		c.Next()
	}
}
