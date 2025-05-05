package main

import (
	"camera_api/internal/app"
)

func main() {
	app := new(app.App)
	app.Run()
}

/**
1. Routing
2. Global error handler
3. Parse request
4. Unify json answer
5. Authorization
6. DB requests
7.
*/
