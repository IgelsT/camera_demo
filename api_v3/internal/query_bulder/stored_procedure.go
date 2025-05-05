package querybulder

import (
	"camera_api/pkg/database"
	"fmt"
	"strings"
)

func CallPRC(prcName string, params ...interface{}) {
	var p []string

	for range len(params) {
		p = append(p, "?")
	}

	sql := fmt.Sprintf("CALL %s (%s)", prcName, strings.Join(p, ","))
	con := database.GetDB()
	_, err := con.Exec(sql, params...)
	if err != nil {
		panic(err)

	}

}
