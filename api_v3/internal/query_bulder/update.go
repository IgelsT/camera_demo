package querybulder

import "camera_api/pkg/database"

type UpdateQuery struct {
	sql    string
	params []interface{}
}

func UPDATE(sql string) UpdateExec {
	return &UpdateQuery{
		sql: sql,
	}
}

type UpdateExec interface {
	Named(object interface{}) int
	Params(params ...interface{}) int
}

func (q *UpdateQuery) Named(object interface{}) int {
	con := database.GetDB()
	result, err := con.NamedExec(q.sql, object)
	if err != nil {
		if database.IsError(err) {
			panic(err)
		}
	}
	if rows, err := result.RowsAffected(); err == nil {
		return int(rows)
	}
	return 0
}

func (q *UpdateQuery) Params(params ...interface{}) int {
	con := database.GetDB()
	result, err := con.Exec(q.sql, params...)
	if err != nil {
		if database.IsError(err) {
			panic(err)
		}
	}
	if rows, err := result.RowsAffected(); err == nil {
		return int(rows)
	}
	return 0
}
