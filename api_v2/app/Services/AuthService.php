<?php

namespace App\Services;

use App\Exceptions\ApiException;
use App\Exceptions\ERROR_CODES;
use App\Models\Users;
use App\Models\Devices;

class AuthService
{
    private $user = null;
    private $device = null;

    public function __construct(
        private UserService $userService,
        private DeviceService $deviceService
    ) {}

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
        if (isset($first_path->device)) {
            $this->device = $this->deviceService->getDeviceByToken($authorization);
            if ($this->device === null) goto badtoken;
            $this->user = $this->userService->userById($this->device->user_id);
        } elseif (isset($first_path->user)) {
            $this->user = $this->userService->userByToken($authorization);
        }

        if ($this->user === null) goto badtoken;

        if ($this->user->user_confirm == 0) throw (new ApiException(ERROR_CODES::$USER_NOT_CONFIRM));

        $this->userService->updateUserLastActivity($this->user->user_id);
        return true;

        badtoken:
        throw new ApiException(ERROR_CODES::$BAD_TOKEN);
    }

    public function getUserId(): int
    {
        if ($this->user === null) {
            throw new ApiException(ERROR_CODES::$INTERNAL_ERROR, 'User not set');
        }
        return $this->user->user_id;
    }

    public function getDeviceUid(): string
    {
        $this->checkDevice();
        return $this->device->device_uid;
    }

    public function getDeviceId(): string
    {
        $this->checkDevice();
        return $this->device->device_id;
    }

    private function checkDevice()
    {
        if ($this->device === null) {
            throw new ApiException(ERROR_CODES::$INTERNAL_ERROR, 'Device not set');
        }
    }
}
