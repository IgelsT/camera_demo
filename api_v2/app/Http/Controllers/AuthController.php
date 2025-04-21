<?php

namespace App\Http\Controllers;

use App\Exceptions\ApiException;
use App\Exceptions\ERROR_CODES;
use App\Http\Requests\Auth\{ConfirmEmail, PasswordRecovery, UserLogin, UserRegister, DeviceLogin, UserProfile};
use App\Services\{AuthService, DeviceService, NotifyService, UserService};
use App\Utils\Utils;


class AuthController extends Controller
{

    public function __construct(
        private AuthService $authService,
        private UserService $userService,
        private DeviceService $deviceService,
        private NotifyService $notifyService
    ) {}

    public function register(UserRegister $request)
    {
        // Check if user exist
        $userExist = $this->userService->userByEmail($request->user_email);
        if ($userExist !== null) {
            throw new ApiException(ERROR_CODES::$EMAIL_EXIST);
        }
        // Create user
        $user = $this->userService->createUser($request->user_email, $request->user_password);
        // Send confirm email
        $this->notifyService->sendConfirm($user->user_email, $user->user_hash);

        return [
            'register' => 'ok',
            'confirm' => $user->user_hash
        ];
    }

    public function confirmEmail(ConfirmEmail $request)
    {
        $user = $this->userService->userByHash($request->hash);
        if ($user === null) throw new ApiException(ERROR_CODES::$INVALID_HASH);
        $user->user_confirm = 1;
        $user->save();

        return ['hash' => 'ok'];
    }

    public function recovery(PasswordRecovery $request)
    {
        $userExist = $this->userService->userByEmail($request->user_email);
        if ($userExist === null) {
            throw new ApiException(ERROR_CODES::$EMAIL_NOT_EXIST);
        }

        if ($userExist->user_confirm == 1) {
            $password = Utils::randomPassword(6);
            $userExist->user_password = md5($password);
            $userExist->save();
        }

        $this->notifyService->recoveryPassword(
            $userExist->user_email,
            $userExist->user_password,
            $userExist->user_hash,
            $userExist->user_confirm
        );

        return ['recovery' => 'recovery'];
    }

    public function login(UserLogin $request)
    {
        $user = $this->userService->userByEMailPasswd($request->user_email, $request->user_password);
        if ($user === null) throw new ApiException(ERROR_CODES::$WRONG_PASSWORD);

        return [
            'hash' => $this->userService->makeToken($user),
            'user_id' => $user->user_id,
            'user_name' => $user->user_name,
            'user_email' => $user->user_email
        ];
        return [];
    }

    public function loginDevice(DeviceLogin $request)
    {
        $user = $this->userService->userByEMailPasswd($request->user_email, $request->user_password);
        if ($user === null) throw new ApiException(ERROR_CODES::$WRONG_PASSWORD);

        $device_token = $this->deviceService->getDeviceToken(
            $request->device_uid,
            $user->user_id,
            $user->user_email
        );
        if ($device_token == "") throw new ApiException(ERROR_CODES::$INTERNAL_ERROR, 'error generate device token');

        return [
            'device_token' => $device_token,
            'device_uid' => $request->device_uid,
            'user_id' => $user->user_id,
            'user_name' => $user->user_name,
            'user_email' => $user->user_email,
            'rtmp_address' => config('app.app_settings.rtmp_address')
        ];
    }

    public function saveProfile(UserProfile $request): array
    {
        $user_id = $this->authService->getUserId();
        $this->userService->updatePassword($user_id, $request->user_password);
        return ['password changed'];
    }
}
