<?php

declare(strict_types=1);

namespace Controllers;

use Models\DeviceModel;
use App\ApiError;
use App\ERROR_CODES;
use App\Vars;

class FilesController
{
    function sendlog()
    {
        $device_uid = Vars::req()->checkParam('device_uid');
        $filePath = Vars::s()['deviceLogPath'] . $device_uid;

        $deviceModel = new DeviceModel();
        $deviceModel->checkDeviceByUID(Vars::u()->user_id, $device_uid);

        if (!file_exists($filePath)) {
            mkdir($filePath);
        }

        $files = [];

        foreach ($_FILES as $file) {
            $fileName = $file['name'];
            $files[] = $fileName;
            if (!move_uploaded_file($file['tmp_name'], $filePath . "/" . $fileName))
                throw ApiError::fromCODE(ERROR_CODES::$FILE_UPLOAD_ERROR);
        }
        return $files;
    }
}
