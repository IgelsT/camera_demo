package routes

import (
	"camera_api/internal/errors"
	"camera_api/internal/handlers"
	"camera_api/internal/middleware"
	"camera_api/internal/sender"
	"camera_api/internal/services"
	"fmt"
	"io"
	"net/http"
	"os"

	"github.com/gin-gonic/gin"
)

var DefaultErrorWriter io.Writer = os.Stderr

func dummy() gin.HandlerFunc {
	return func(c *gin.Context) {
		fmt.Println("dummy")
		c.IndentedJSON(http.StatusOK, "dummy()")
	}
}

func health(c *gin.Context) {
	c.IndentedJSON(http.StatusOK, "OK")
}

func InitRoutes() *gin.Engine {
	router := gin.New()
	router.Use(middleware.Recovery())
	router.Use(gin.Logger())
	router.GET("/health", health)

	userService := services.NewUserService()
	deviceService := services.NewDeviceService()
	messageService := services.NewMessqgesService()
	dashService := services.NewDashboardService()

	authHandler := handlers.NewAuthHandler(
		userService,
		services.NewNotifyService(),
		deviceService,
	)

	auth := router.Group("auth")
	{
		auth.POST("/register", authHandler.Register)
		auth.POST("/confirmEmail", authHandler.ConfirmEmail)
		auth.POST("/recovery", authHandler.Recovery)
		auth.POST("/login", authHandler.Login)
		auth.POST("/loginDevice", authHandler.LoginDevice)
		auth.Use(middleware.Authorization()).POST("/saveProfile", authHandler.SaveProfile)
	}

	dashHandler := handlers.NewDashboardHandler(services.NewDashboardService())

	dashboard := router.Group("/dashboard").Use(middleware.Authorization())
	{
		dashboard.POST("/", dashHandler.Index)
	}

	deviceHandler := handlers.NewDeviceHandler(deviceService, messageService)

	device := router.Group("device").Use(middleware.Authorization())
	{
		device.POST("/setCameraList", deviceHandler.SetCameraList)
		device.POST("/setDeviceState", deviceHandler.SetDeviceState)
		device.POST("/setDeviceInfo", deviceHandler.SetDeviceInfo)
		device.POST("/ping", deviceHandler.Ping)
		device.POST("/appliedMessages", deviceHandler.AppliedMessages)
		device.POST("/executedMessages", deviceHandler.ExecutedMessages)
		device.POST("/setLogList", deviceHandler.SetLogList)
		device.POST("/sendLog", deviceHandler.SendLog)
	}

	deviceFrontHandler := handlers.NewFrontDeviceHandler(deviceService, messageService, dashService)

	device_front := router.Group("device_front").Use(middleware.Authorization())
	{
		device_front.POST("/", deviceFrontHandler.Index)
		device_front.POST("/info", deviceFrontHandler.Info)
		device_front.POST("/saveParams", deviceFrontHandler.SaveParams)
		device_front.POST("/delete", deviceFrontHandler.Delete)
		device_front.POST("/delMsg", deviceFrontHandler.DelMsg)
		device_front.POST("/logsList", deviceFrontHandler.LogsList)
		device_front.POST("/requestLogs", deviceFrontHandler.RequestLogs)
		device_front.POST("/requestLogFile", deviceFrontHandler.RequestLogFile)
		device_front.POST("/LogFile", deviceFrontHandler.LogFile)
	}

	router.NoRoute(func(c *gin.Context) {
		sender.ApiSendError(c, errors.ErrorFromCode(errors.WRONG_REQUEST))
	})

	return router
}
