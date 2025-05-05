package database

import (
	"time"

	ms "github.com/go-sql-driver/mysql"
	"github.com/jmoiron/sqlx"
)

type DBParams struct {
	Host     string
	User     string
	Password string
	Base     string
}

var dbParams DBParams
var connection *sqlx.DB

func InitDB(params DBParams) error {
	dbParams = params
	db, err := sqlx.Open("mysql", dbParams.User+":"+dbParams.Password+"@tcp("+dbParams.Host+")/"+dbParams.Base)
	if err != nil {
		return err
	}
	// See "Important settings" section.
	db.SetConnMaxLifetime(time.Minute * 3)
	db.SetMaxOpenConns(10)
	db.SetMaxIdleConns(10)

	err = db.Ping()
	if err != nil {
		return err
	}

	connection = db
	return nil
}

func GetDB() *sqlx.DB {
	return connection
}

func IsError(err error) bool {
	if _, ok := err.(*ms.MySQLError); ok {
		return true
	}
	return false
}
