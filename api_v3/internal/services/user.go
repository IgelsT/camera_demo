package services

import (
	QB "camera_api/internal/query_bulder"
	"camera_api/pkg/utils"
	"crypto/md5"
	"database/sql"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"time"
)

type Users struct {
	UserId           int            `db:"user_id"`
	UserName         string         `db:"user_name"`
	UserDescription  sql.NullString `db:"user_description"`
	UserPassword     sql.NullString `db:"user_password"`
	UserEmail        string         `db:"user_email"`
	UserHash         string         `db:"user_hash"`
	UserToken        string         `db:"user_token"`
	UserConfirm      int            `db:"user_confirm"`
	UserDate         sql.NullString `db:"user_date"`
	UserDastactivity sql.NullString `db:"user_lastactivity"`
}

type UserService struct {
}

func NewUserService() *UserService {
	return &UserService{}
}

func (s *UserService) UserByEmail(email string) *Users {
	sql := "SELECT * FROM users WHERE user_email=?"
	return QB.GET[Users](sql).Params(email).One()
}

func (s *UserService) UserByHash(hash string) *Users {
	sql := "SELECT * FROM users WHERE user_hash=? AND user_confirm=0"
	return QB.GET[Users](sql).Params(hash).One()
}

func (s *UserService) CreateUser(email string, password string) *Users {
	user := new(Users)
	user.UserEmail = email
	user.UserPassword = sql.NullString{String: password, Valid: true}
	user.UserHash = fmt.Sprintf("%x", md5.Sum([]byte(email+time.Now().GoString())))
	sql := "INSERT INTO users (user_email, user_password, user_hash) VALUES (:user_email, :user_password, :user_hash)"
	result := QB.INSERT(sql).Named(user)
	user.UserId = result
	return user
}

func (s *UserService) UpdateUser(user *Users) {
	sql := `UPDATE users SET user_name=:user_name, user_description=:user_description,
        user_password=:user_password, user_email=:user_email, user_hash=:user_hash, user_token=:user_token,
        user_confirm=:user_confirm WHERE user_id=:user_id`
	QB.UPDATE(sql).Named(user)
}

func (s *UserService) UserByEMailPasswd(email string, password string) *Users {
	sql := "SELECT * FROM users WHERE user_email=? AND user_password=?"
	return QB.GET[Users](sql).Params(email, password).One()
}

func (s *UserService) MakeToken(user_id int, user_email string) string {
	json, err := json.Marshal(map[string]string{"user": user_email})
	if err != nil {
		panic(err)
	}
	firstPath := base64.StdEncoding.EncodeToString([]byte(json))
	secondPath := utils.RandomPassword(15)
	token := firstPath + "." + secondPath

	sql := "UPDATE users SET user_token=:user_token WHERE user_id=:user_id"
	params := map[string]any{"user_token": token, "user_id": user_id}
	QB.UPDATE(sql).Named(params)

	return token
}

func (s *UserService) UpdatePassword(user_id int, password string) {
	sql := "UPDATE users SET user_password=:user_password WHERE user_id=:user_id"
	params := map[string]any{"user_password": password, "user_id": user_id}
	QB.UPDATE(sql).Named(params)
}

func (s *UserService) UserById(id int) *Users {
	sql := "SELECT * FROM users WHERE user_id=?"
	return QB.GET[Users](sql).Params(id).One()
}

func (s *UserService) UserByToken(token string) *Users {
	sql := "SELECT * FROM users WHERE user_token=?"
	return QB.GET[Users](sql).Params(token).One()
}
func (s *UserService) UpdateUserLastActivity(user_id int) {
	sql := "UPDATE users SET user_lastactivity=NOW() WHERE user_id=:user_id"
	params := map[string]any{"user_id": user_id}
	QB.UPDATE(sql).Named(params)
}
