package appconfig

import (
	"errors"
	"os"
	"strconv"

	DB "camera_api/pkg/database"
	Mail "camera_api/pkg/mailsender"

	"github.com/joho/godotenv"
)

type appConfig struct {
	ServerPort         string
	DBHost             string
	DBBase             string
	DBUser             string
	DBPass             string
	MailHost           string
	MailPort           string
	MailUser           string
	MailPass           string
	MailFromName       string
	MailFromAddress    string
	AppMode            string
	FrontMainURL       string
	FrontConfirmURL    string
	FrontRTMPAddress   string
	FrontDeviceLogPath string
	HashSecret         string
}

var appconfig = new(appConfig)

func IninConfig() error {
	if err := godotenv.Load(); err != nil {
		return err
	}

	appconfig.AppMode = os.Getenv("APP_MODE")
	if appconfig.AppMode == "" {
		appconfig.AppMode = "prod"
	}

	appconfig.ServerPort = os.Getenv("SERVER_PORT")
	if appconfig.ServerPort == "" {
		return errors.New("no server port in config")
	}

	appconfig.DBHost = os.Getenv("DB_HOST")
	appconfig.DBBase = os.Getenv("DB_BASE")
	appconfig.DBUser = os.Getenv("DB_USER")
	appconfig.DBPass = os.Getenv("DB_PASS")

	if appconfig.DBHost == "" || appconfig.DBBase == "" || appconfig.DBUser == "" {
		return errors.New("error in db settings")
	}

	appconfig.MailHost = os.Getenv("MAIL_HOST")
	appconfig.MailPort = os.Getenv("MAIL_PORT")
	appconfig.MailUser = os.Getenv("MAIL_USER")
	appconfig.MailPass = os.Getenv("MAIL_PASS")
	appconfig.MailFromAddress = os.Getenv("MAIL_FROM_ADDRESS")
	appconfig.MailFromName = os.Getenv("MAIL_FROM_NAME")

	appconfig.FrontMainURL = os.Getenv("FRONT_MAIN_URL")
	appconfig.FrontConfirmURL = os.Getenv("FRONT_CONFIRM_URL")
	appconfig.FrontRTMPAddress = os.Getenv("FRONT_RTMP_ADDRESS")
	appconfig.FrontDeviceLogPath = os.Getenv("FRONT_DEVICE_LOGPATH")
	appconfig.HashSecret = os.Getenv("HASH_SECRET")

	return nil
}

func GetAppConfig() *appConfig {
	return appconfig
}

func GetDBParams() *DB.DBParams {
	return &DB.DBParams{
		Host:     appconfig.DBHost,
		User:     appconfig.DBUser,
		Password: appconfig.DBPass,
		Base:     appconfig.DBBase,
	}
}

func GetMailParams() *Mail.MailerParams {

	p := &Mail.MailerParams{
		MailHost:        appconfig.MailHost,
		MailUser:        appconfig.MailUser,
		MailPass:        appconfig.MailPass,
		MailFromName:    appconfig.MailFromName,
		MailFromAddress: appconfig.MailFromAddress,
	}

	if port, err := strconv.Atoi(appconfig.MailPort); err == nil {
		p.MailPort = port
	} else {
		p.MailPort = 25
	}
	return p
}

func GetServerPort() string {
	return appconfig.ServerPort
}

func GetAppDevMode() bool {
	return appconfig.AppMode == "dev"
}

func GetParam(name string) string {
	return os.Getenv(name)
}
