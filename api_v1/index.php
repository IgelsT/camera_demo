<?php

declare(strict_types=1);

ini_set('display_errors', 1);
error_reporting(E_ALL);

require __DIR__ . '/App/autoload.php';

use App\MainApp;
use Handlers\AuthHandler;
use Controllers\RtmpController;
use Controllers\DeviceController;
use Controllers\DeviceFrontController;
use Controllers\DashBoardController;
use Controllers\FilesController;
use Handlers\TestHandler;

$app = new MainApp();

$app->addRoute('/rtmpauth-nms', [RtmpController::class]);
$app->addRoute('/rtmpublishpauth', [RtmpController::class, 'publishAuth'], false);
// $app->addRoute('/rtmpplayhauth', RtmpController::class, false, 'playAuth');
// $app->addRoute('/register', [AuthHandler::class], false);
$app->addRoute('/auth', [AuthHandler::class], false);
$app->addRoute('/profile', [AuthHandler::class]);
$app->addRoute('/device', [DeviceController::class]);
$app->addRoute('/device_front', [DeviceFrontController::class]);
$app->addRoute('/dashboard', [DashBoardController::class]);
// $app->addRoute('/testing', TestController::class, false);
$app->addRoute('/upload', [FilesController::class]); //to deprication
$app->addRoute('/files', [FilesController::class]);

$app->addRoute('/test', [TestHandler::class], false);
// $app->addRoute('/filesops', FilesController::class);

// $app->addRoute('/auth1', function () {
//     return 'Callback function';
//     pprint($this);
// });

// sleep(2);
$app->run();
