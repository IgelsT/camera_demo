<?php

declare(strict_types=1);

namespace Controllers;

use App\HTTP_ERROR_CODES;
use App\HTTPError;
use App\Vars;
use Models\DeviceModel;
use Utilites\LogClass;

class RtmpController
{

    // function getHash($string)
    // {
    //   $secret = $this->app->getSettings()['auth']['secretPhase'];
    //   $sig = hash_hmac('sha256', $string, $secret);
    //   return $sig;
    // }

    function publishAuthNMS()
    {
        return ["OK"];
    }

    function playAuthNMS()
    {
        return ["OK"];
    }

    function publishAuth()
    {
        // LogClass::LogV(Vars::req());
        $posts = Vars::req()->post;
        if (!isset($posts['authToken'])) throw new HTTPError(HTTP_ERROR_CODES::$FORBIDDEN);

        $deviceModel = new DeviceModel();
        $device = $deviceModel->getDeviceByToken($posts['authToken']);

        if ($device == false) return throw new HTTPError(HTTP_ERROR_CODES::$FORBIDDEN);
        return ['ok'];
    }

    function publishAuth1()
    {
        LogClass::LogV(Vars::req());
        throw new HTTPError(HTTP_ERROR_CODES::$FORBIDDEN);
        $content = json_decode(Vars::req()->content);
        if (!isset($content->param)) return false;

        $paramsArray = [];
        $params = explode('&', $content->param);

        foreach ($params as $param) {
            $pair = explode('=', $param);
            if (isset($pair[0]) && isset($pair[1])) {
                $paramsArray[str_replace('?', '', $pair[0])] = $pair[1];
            }
        }

        if (!isset($paramsArray['authToken'])) return;

        $deviceModel = new DeviceModel();
        $device = $deviceModel->getDeviceByToken($paramsArray['authToken']);

        if ($device == false) return false;
        // echo ("--------------------------\r\n");
        // print_r($device);
        // echo ("--------------------------\r\n");
        return '{"code": 0}';
    }
}
