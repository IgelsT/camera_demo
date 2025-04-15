<?php

declare(strict_types=1);

namespace Controllers;

use App\ApiError;
use App\ApiRequest;
use App\ERROR_CODES;
use App\Utils\Mailer;
use App\IVars;
use DTO\DataBase\UsersDTO;
use DTO\Auth\LoginRequest;
use DTO\Auth\LoginDeviceRequest;
use DTO\Auth\RegisterRequest;
use DTO\DeviceParams\DeviceDTO;
use Models\IDeviceModel;
use Models\IUserModel;


class AuthController
{

    var array $settings;
    var ApiRequest $request;

    function __construct(
        private IVars $vars,
        private IUserModel $model,
        private IDeviceModel $deviceModel
    ) {
        $this->settings = $this->vars->getSettings();
        $this->request = $this->vars->getRequest();
    }

    private function getHash($string)
    {
        $secret = $this->settings['auth']['secretPhase'];
        $sig = hash_hmac('sha256', $string, $secret);
        return $sig;
    }


    public function register()
    {
        $inUser = new RegisterRequest($this->vars->getRequestData());

        $user = $this->model->userByEmail($inUser->user_email);
        if ($user) throw ApiError::fromCODE(ERROR_CODES::$EMAIL_EXIST);

        $hash = md5($inUser->user_email . time());
        $this->model->addUser($inUser->user_email, $inUser->user_password, $hash);

        $result = $this->sendConfirm($inUser->user_email, $hash);
        if (!$result) throw new ApiError(ERROR_CODES::$EMAIL_SEND_ERROR);

        return ['register' => 'ok'];
    }

    private function sendConfirm($email, $hash)
    {
        $href = "http://" . $this->settings['mainUrl'] . "/" . $this->settings['confirmUrl'] .
            "/" . $hash;
        $message = 'Для подтверждения регистрации пройдите по ссылке<br>' .
            '<a href="' . $href . '">' . $href . '</a>';
        $result = Mailer::sendEmail($email, 'Регистрация на ' . $this->settings['mainUrl'], $message);
        return $result;
    }

    public function getConfirmUrl()
    {
        $inUser = new LoginRequest($this->vars->getRequestData());
        $user = $this->model->userByEmailPasswd($inUser->user_email, $inUser->user_password);
        if (!$user) throw new ApiError(ERROR_CODES::$WRONG_PASSWORD);

        $data = "{'hash': {$user->user_hash}}";
        $href = "{$this->settings['apiUrl']}{$this->settings['apiPath']}/auth/confirmEmail?data={$data}";

        return ['url' => $href];
    }

    public function confirmEmail()
    {
        $hash = $this->request->checkParam('hash');
        $user = $this->model->userByHash($hash);
        if (!$user) throw new ApiError(ERROR_CODES::$INVALID_HASH);
        $user->user_confirm = 1;
        $this->model->saveUser($user);
        return ['hash' => 'ok'];
    }

    public function recovery()
    {
        $email = $this->request->getParam('user_email');
        $user = $this->model->userByEmail($email);
        if (!$user) throw new ApiError(ERROR_CODES::$EMAIL_NOT_EXIST);

        if ($user->user_confirm == 0) {
            $href = "http://" . $this->settings['mainUrl'] . "/" . $this->settings['confirmUrl'] .
                "/" . $user->user_hash;
            $message = 'Для подтверждения регистрации пройдите по ссылке<br>' .
                '<a href="' . $href . '">' . $href . '</a>';
        } else {
            $password = $this->randomPassword(6);
            $user->user_password = md5($password);
            $this->model->saveUser($user);
            $message = 'Данные для входа<br>' .
                'Имя: ' . $user->user_email . '<br>' .
                'Пароль: ' . $password . '<br>';
        }
        $result = Mailer::sendEmail($user->user_email, 'Восстановление пароля на ' . $this->settings['mainUrl'], $message);
        if (!$result) new ApiError(ERROR_CODES::$EMAIL_SEND_ERROR);
        return ['recovery' => 'recovery'];
    }

    private function randomPassword($lenght)
    {
        $alphabet = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890';
        $pass = array(); //remember to declare $pass as an array
        $alphaLength = strlen($alphabet) - 1; //put the length -1 in cache
        for ($i = 0; $i < $lenght; $i++) {
            $n = rand(0, $alphaLength);
            $pass[] = $alphabet[$n];
        }
        return implode($pass); //turn the array into a string
    }

    public function login()
    {
        $inUser = new LoginRequest($this->vars->getRequestData());
        $user = $this->getUser($inUser->user_email, $inUser->user_password);
        $first_path = base64_encode(json_encode(['user' => $user->user_email]));
        $second_path = $this->getHash(json_encode(['user' => $user->user_email, 'pass' => $user->user_password]));
        return [
            'hash' => $first_path . '.' . $second_path,
            'user_id' => $user->user_id,
            'user_name' => $user->user_name,
            'user_email' => $user->user_email
        ];
    }

    public function loginDevice()
    {
        $inUser = new LoginDeviceRequest($this->vars->getRequestData());
        $user = $this->getUser($inUser->user_email, $inUser->user_password);

        $device_token = $this->getDeviceToken($inUser->device_uid, $user);
        if ($device_token != "")
            return [
                'device_token' => $device_token,
                'device_uid' => $inUser->device_uid,
                'user_id' => $user->user_id,
                'user_name' => $user->user_name,
                'user_email' => $user->user_email,
                'rtmp_address' => $this->settings['rtmpAddress']
            ];
    }

    private function getUser(string $user_email, string $user_password): UsersDTO
    {
        $user = $this->model->userByEmailPasswd($user_email, $user_password);
        if (!$user)
            throw new ApiError(ERROR_CODES::$WRONG_PASSWORD);

        if ($user->user_confirm == 0) {
            $result = $this->sendConfirm($user->user_email, $user->user_hash);
            if (!$result) throw new ApiError(ERROR_CODES::$EMAIL_SEND_ERROR);
            throw new ApiError(ERROR_CODES::$USER_NOT_CONFIRM);
        }
        return $user;
    }

    private function getDeviceToken(string $device_uid, UsersDTO $user): string
    {
        $device = $this->deviceModel->getDeviceByUID($user->user_id, $device_uid, true);
        if ($device == false) $device = new DeviceDTO();

        $first_path = base64_encode(json_encode(['device' => $device_uid, 'user' => $user->user_name]));
        $second_path = bin2hex(random_bytes(15));
        $token = $first_path . '.' . $second_path;

        $device->user_id = $user->user_id;
        $device->device_uid = $device_uid;
        $device->device_token = $token;
        $device->device_deleted = 0;
        $this->deviceModel->saveDevice($device);
        return $token;
    }

    public function checkAuthorization($authorization)
    {
        if (!is_string($authorization)) goto badtoken;
        $auth_paths = explode('.', $authorization);
        if (!isset($auth_paths[0]) || !isset($auth_paths[1])) goto badtoken;
        try {
            $first_path = json_decode(base64_decode($auth_paths[0]));
            if ($first_path == '') goto badtoken;
        } catch (\Exception $e) {
            goto badtoken;
        }

        if (!isset($first_path->user)) goto badtoken;

        $user = $this->model->userByName($first_path->user);
        if (!$user) goto badtoken;
        if ($user->user_confirm == 0) throw (new ApiError(ERROR_CODES::$USER_NOT_CONFIRM));

        $this->vars->setUser($user);
        if (!isset($first_path->device)) {
            $this->model->updateUserLastActivity($user->user_id);
            $calc_hash = $this->getHash(json_encode(['user' => $user->user_name, 'pass' => $user->user_password]));
            $second_path = $auth_paths[1];
            if ($calc_hash == $second_path) return true;
        } else {
            $device = $this->deviceModel->getDeviceByUID($user->user_id, $first_path->device);
            if ($device == false) goto badtoken;
            $this->vars->setDevice($device);
            if ($device->device_token == $authorization) return true;
        }

        badtoken:
        throw (new ApiError(ERROR_CODES::$BAD_TOKEN));
    }

    public function saveProfile()
    {
        $user_id = $this->vars->getUser()->user_id;
        $user_name = $this->request->getParam('user_id');
        $user_password = $this->request->getParam('user_password');
        if ($user_password == "") new ApiError(ERROR_CODES::$EMPTY_PASSWORD);
        $this->model->updatePassword($user_id, $user_password);
        return ['password changed'];
    }
}
