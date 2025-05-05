package mailsender

import (
	"fmt"
	"net/mail"
	"net/smtp"
	"strings"
)

type MailSender struct {
	params *MailerParams
}

type MailerParams struct {
	MailHost        string
	MailPort        int
	MailUser        string
	MailPass        string
	MailFromName    string
	MailFromAddress string
}

var sender *MailSender

func NewMailSender(_params *MailerParams) error {
	sender = &MailSender{
		params: _params,
	}
	return nil
}

func GetSender() *MailSender {
	return sender
}

func (s *MailSender) SendMail(to string, subject string, message string) error {
	mime := "MIME-version: 1.0;\nContent-Type: text/html; charset=\"UTF-8\";"

	from := &mail.Address{
		Name:    s.params.MailFromName,
		Address: s.params.MailFromAddress,
	}

	body := fmt.Sprintf("From: %s\nSubject: %s\n%s\n\n%s", from.String(), strings.TrimSpace(subject), mime, strings.TrimSpace(message))
	msg := []byte(body)

	addr := fmt.Sprintf("%s:%d", s.params.MailHost, s.params.MailPort)

	auth := &plainAuth{
		"",
		s.params.MailUser,
		s.params.MailPass,
		s.params.MailHost,
	}

	// from := fmt.Sprintf("From: \"%s\" <%s>", s.params.MailFromName, s.params.MailFromAddress)

	err := smtp.SendMail(addr, auth, s.params.MailFromAddress, []string{to}, msg)
	if err != nil {
		return err
	}

	return nil
}
