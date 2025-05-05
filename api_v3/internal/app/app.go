package app

import (
	"camera_api/internal/routes"
	"camera_api/pkg/appconfig"
	"camera_api/pkg/database"
	"camera_api/pkg/mailsender"
	"log"

	"github.com/gin-gonic/gin"
)

type App struct {
}

func (a *App) Run() {

	// Read config
	log.Println("Read config")
	if err := appconfig.IninConfig(); err != nil {
		log.Fatalf("error read config file: %s", err.Error())
	}

	// Check APP mode
	if appconfig.GetAppDevMode() {
		gin.SetMode(gin.DebugMode)
	} else {
		gin.SetMode(gin.ReleaseMode)
	}

	// Connect to DB
	log.Println("Connect to DB")
	if err := database.InitDB(*appconfig.GetDBParams()); err != nil {
		log.Fatalf("error connect to db: %s", err.Error())
	}

	// Config mailer
	log.Println("Config mailer")
	if err := mailsender.NewMailSender(appconfig.GetMailParams()); err != nil {
		log.Printf("error config mail service: %s", err.Error())
	}

	// Prerare routes
	log.Println("Prepare routes")
	routes := routes.InitRoutes()

	// Start Gin
	log.Println("Start GIN")
	addr := "0.0.0.0:" + appconfig.GetServerPort()
	if err := routes.Run(addr); err != nil {
		log.Fatalf("error start GIN: %s", err.Error())
	}
}
