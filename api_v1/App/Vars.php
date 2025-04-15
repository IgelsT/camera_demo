<?php

namespace App;

use DTO\DeviceParams\DeviceDTO;
use DTO\DataBase\UsersDTO;

interface IVars
{
    function getSettings(): array;
    function getUser(): UsersDTO | null;
    function setUser(UsersDTO $user): void;
    function getRequest(): ApiRequest;
    function setDevice(DeviceDTO $device);
    function getDevice(): ?DeviceDTO;
    function getRequestData(): array;
    function getUserId(): int;
}

class Vars implements IVars
{
    /** @var array[] */
    private array $settings = [];
    private ApiRequest $request;
    private ?UsersDTO $user = null;
    private ?DeviceDTO $device = null;

    protected static ?Vars $_instance;

    private function __construct() {}

    public static function getInstance(): Vars
    {
        if (!isset(self::$_instance)) {
            self::$_instance = new self;
        }
        return self::$_instance;
    }

    public static function setSettings(array $settings): void
    {
        $self = self::getInstance();
        $self->settings = $settings;
    }

    public static function setRequest(ApiRequest $request): void
    {
        $self = self::getInstance();
        $self->request = $request;
    }

    public static function s(): array
    {
        return self::getInstance()->settings;
    }

    public static function u(): UsersDTO | null
    {
        return self::getInstance()->user;
    }

    public static function d(): DeviceDTO | null
    {
        return self::getInstance()->device;
    }

    public static function getHash(string $string): string
    {
        /** @var string */
        $secret = self::getInstance()->settings['auth']['secretPhase'];
        return hash_hmac('sha256', $string, $secret);
    }

    function getSettings(): array
    {
        return $this->settings;
    }
    /* User */
    public function setUser(UsersDTO $user): void
    {
        $this->user = $user;
    }
    function getUser(): UsersDTO | null
    {
        return $this->user;
    }

    public function setDevice(DeviceDTO $device)
    {
        $self = self::getInstance();
        $self->device = $device;
    }

    function getDevice(): ?DeviceDTO
    {
        return $this->device;
    }

    function getRequest(): ApiRequest
    {
        return $this->request;
    }
    function getRequestData(): array
    {
        return $this->request->data;
    }

    function getUserId(): int
    {
        return $this->user?->user_id ?? -1;
    }

    private function __clone() {} //запрещаем клонирование объекта модификатором private
    public function __wakeup() {} //запрещаем клонирование объекта модификатором private

}
