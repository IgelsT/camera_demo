package querybulder

import (
	"camera_api/pkg/database"
)

type GetQuery[T interface{}] struct {
	result []T
	sql    string
	params []interface{}
	object interface{}
}

func GET[T interface{}](query string) GetType[T] {
	q := &GetQuery[T]{
		sql: query,
	}
	return q
}

type GetType[T interface{}] interface {
	Named(object interface{}) GetResult[T]
	Params(params ...interface{}) GetResult[T]
}

type GetResult[T interface{}] interface {
	One() *T
	All() []T
}

func (q *GetQuery[T]) Named(object interface{}) GetResult[T] {
	q.object = object
	return q
}

func (q *GetQuery[T]) Params(params ...interface{}) GetResult[T] {
	q.params = params
	return q
}

func (q *GetQuery[T]) One() *T {
	con := database.GetDB()
	if err := con.Select(&q.result, q.sql, q.params...); err != nil {
		if err.Error() == "sql: no rows in result set" {
			return nil
		}
		panic(err)
	}
	if len(q.result) == 0 {
		return nil
	}
	return &q.result[0]
}

func (q *GetQuery[T]) All() []T {
	con := database.GetDB()
	if err := con.Select(&q.result, q.sql, q.params...); err != nil {
		panic(err)
	}
	return q.result
}

// type SelectQuery struct {
// 	sql    string
// 	object interface{}
// 	params []interface{}
// }

// type SelectType interface {
// 	Named(object interface{}) SelectResult
// 	Params(params ...interface{}) SelectResult
// }

// type SelectResult interface {
// 	One() interface{}
// 	All() interface{}
// }

// func SELECT(query string) SelectType {
// 	q := &SelectQuery{
// 		sql: query,
// 	}
// 	return q
// }

// func (q *SelectQuery) Named(object interface{}) SelectResult {
// 	q.object = object
// 	return q
// }

// func (q *SelectQuery) Params(params ...interface{}) SelectResult {
// 	q.params = params
// 	return q
// }

// func (q *SelectQuery) One() interface{} {
// 	// con := database.GetDB()
// 	// if err := con.Select(&q.result, q.sql, q.params...); err != nil {
// 	// 	if err.Error() == "sql: no rows in result set" {
// 	// 		return nil
// 	// 	}
// 	// 	panic(err)
// 	// }
// 	// if len(q.result) == 0 {
// 	// 	return nil
// 	// }
// 	// return &q.result[0]
// 	val := []map[string]interface{}{}
// 	return val
// }

// func (q *SelectQuery) All() interface{} {
// 	con := database.GetDB()
// 	rows, err := con.Queryx(q.sql, q.params...)
// 	if err != nil {
// 		panic(err)
// 	}

// 	result := []map[string]interface{}{}
// 	for rows.Next() {
// 		row := map[string]interface{}{}
// 		if err = rows.MapScan(row); err != nil {
// 			panic(err)
// 		}
// 		result = append(result, row)
// 	}

// 	return result
// }
