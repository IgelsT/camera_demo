package services

import (
	QB "camera_api/internal/query_bulder"
	"fmt"
	"strings"
)

type Message struct {
	MessageId         int     `db:"message_id" json:"message_id"`
	Message           string  `db:"message" json:"message"`
	MessageType       string  `db:"message_type" json:"message_type"`
	MessageStatus     int     `db:"message_status" json:"message_status"`
	UserId            int     `db:"user_id" json:"user_id"`
	DevicUid          string  `db:"device_uid" json:"device_uid"`
	DevicId           *int    `db:"device_id" json:"device_id"`
	MessageCreateDate *string `db:"message_create_date" json:"message_create_date"`
	MessageSentDate   *string `db:"message_sent_date" json:"message_sent_date"`
}

type MessagesService struct{}

func NewMessqgesService() *MessagesService {

	return &MessagesService{}
}

func (s *MessagesService) GetMessagesToSend(user_id int, device_uid string) []Message {
	sql := "SELECT * FROM messages WHERE user_id = ? AND device_uid = ? AND message_status = 0 AND message_type = 'OUT'"
	return QB.GET[Message](sql).Params(user_id, device_uid).All()
}

func (s *MessagesService) ApplyMessages(user_id int, device_uid string, messages []int) {
	in := strings.Trim(strings.Replace(fmt.Sprint(messages), " ", ",", -1), "[]")
	sql := `UPDATE messages SET message_status = 1, message_sent_date = NOW() 
		WHERE user_id = ? AND device_uid = ? AND message_status != 2 AND message_id IN(` + in + `)`
	QB.UPDATE(sql).Params(user_id, device_uid)
}

func (s *MessagesService) ExecutedMessage(user_id int, device_uid string, messageId int) {
	sql := `UPDATE messages SET message_status = 2 WHERE user_id = ? AND device_uid = ? 
		AND message_status != 2 AND message_id = ?`
	QB.UPDATE(sql).Params(user_id, device_uid, messageId)
}

func (s *MessagesService) DeleteMessages(user_id int, device_uid string) {
	sql := "DELETE FROM messages WHERE user_id = ? AND device_uid = ? AND message_type = 'OUT'"
	QB.DELETE(sql).Params(user_id, device_uid)
}
