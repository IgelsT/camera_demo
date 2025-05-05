package services

import (
	"camera_api/pkg/appconfig"
	"camera_api/pkg/mailsender"
	"fmt"
)

type NotifyService struct {
}

func NewNotifyService() *NotifyService {
	return &NotifyService{}
}

func (s *NotifyService) SendConfirm(email string, hash string) error {
	sender := mailsender.GetSender()
	href := fmt.Sprintf("http://%s/%s", appconfig.GetAppConfig().FrontMainURL, hash)
	message := fmt.Sprintf("Для подтверждения регистрации пройдите по ссылке<br><a href='%s'>%s</a>", href, href)
	subject := "Регистрация на " + appconfig.GetAppConfig().FrontMainURL

	return sender.SendMail(email, subject, message)
}

func (s *NotifyService) RecoveryPassword(email string, password string) error {
	sender := mailsender.GetSender()
	message := fmt.Sprintf("Данные для входа<br>Имя: %s<br>Пароль: %s<br>", email, password)
	subject := "Восстановление пароля на " + appconfig.GetAppConfig().FrontMainURL

	return sender.SendMail(email, subject, message)
}
