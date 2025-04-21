<?php

use App\Exceptions\ApiException;
use App\Exceptions\ERROR_CODES;
use App\Http\Controllers\{AuthController, DashboardController, DeviceController, DeviceFrontController};
use App\Http\Middleware\AuthUser;
use Illuminate\Support\Facades\Route;

// Route::get('/user', function (Request $request) {
//     return $request->user();
// })->middleware('auth:sanctum');

Route::controller(AuthController::class)
    ->prefix('auth')
    ->group(function () {
        Route::post('/register', 'register');
        Route::post('/confirmEmail', 'confirmEmail');
        Route::post('/recovery', 'recovery');
        Route::post('/login', 'login');
        Route::post('/loginDevice', 'loginDevice');
        Route::post('/saveProfile', 'saveProfile')->middleware(AuthUser::class);
    });

Route::controller(DashboardController::class)
    ->prefix('/dashboard')
    ->middleware(AuthUser::class)
    ->group(function () {
        Route::post('/', 'index');
    });

Route::controller(DeviceController::class)
    ->prefix('/device')
    ->middleware(AuthUser::class)
    ->group(function () {
        Route::post('/setCameraList', 'setCameraList');
        Route::post('/setDeviceState', 'setDeviceState');
        Route::post('/setDeviceInfo', 'setDeviceInfo');
        Route::post('/ping', 'ping');
        Route::post('/appliedMessages', 'appliedMessages');
        Route::post('/executedMessages', 'executedMessages');
        Route::post('/setLogList', 'setLogList');
        Route::post('/sendLog', 'sendLog');
    });

Route::controller(DeviceFrontController::class)
    ->prefix('/device_front')
    ->middleware(AuthUser::class)
    ->group(function () {
        Route::post('/', 'list');
        Route::post('/info', 'info');
        Route::post('/saveParams', 'saveParams');
        Route::post('/delete', 'delete');
        Route::post('/delMsg', 'delMsg');
        Route::post('/logsList', 'logsList');
        Route::post('/requestLogs', 'requestLogs');
        Route::post('/requestLogFile', 'requestLogFile');
        Route::post('/LogFile', 'LogFile');
    });

Route::fallback(function () {
    throw new ApiException(ERROR_CODES::$WRONG_REQUEST, "route not found");
});



// $app->addRoute('/device', [DeviceController::class]);
// $app->addRoute('/device_front', [DeviceFrontController::class]);


// $app->addRoute('/upload', [FilesController::class]); //to deprication
// $app->addRoute('/files', [FilesController::class]);
