package querybulder

import "camera_api/pkg/database"

type InsertQuery struct {
	sql    string
	params []interface{}
}

func INSERT(sql string) InsertExec {
	return &InsertQuery{
		sql: sql,
	}
}

type InsertExec interface {
	Named(object interface{}) int
	Params(params ...interface{}) int
}

func (q *InsertQuery) Named(object interface{}) int {
	con := database.GetDB()
	result, err := con.NamedExec(q.sql, object)
	if err != nil {
		if database.IsError(err) {
			panic(err)
		}
	}
	if id, err := result.LastInsertId(); err == nil {
		return int(id)
	}
	return 0
}

func (q *InsertQuery) Params(params ...interface{}) int {
	con := database.GetDB()
	result, err := con.Exec(q.sql, params...)
	if err != nil {
		if database.IsError(err) {
			panic(err)
		}
	}
	if id, err := result.LastInsertId(); err == nil {
		return int(id)
	}
	return 0
}
