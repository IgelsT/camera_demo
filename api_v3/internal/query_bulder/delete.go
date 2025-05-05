package querybulder

import "camera_api/pkg/database"

type DeleteQuery struct {
	sql    string
	params []interface{}
}

func DELETE(sql string) DeleteExec {
	return &DeleteQuery{
		sql: sql,
	}
}

type DeleteExec interface {
	Named(object interface{}) int
	Params(params ...interface{}) int
}

func (q *DeleteQuery) Named(object interface{}) int {
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

func (q *DeleteQuery) Params(params ...interface{}) int {
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
