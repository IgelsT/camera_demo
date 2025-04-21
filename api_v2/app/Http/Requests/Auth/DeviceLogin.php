<?php

namespace App\Http\Requests\Auth;

use Illuminate\Foundation\Http\FormRequest;

class DeviceLogin extends FormRequest
{
    public string $user_email;
    public string $user_password;
    public string $device_uid;

    public function authorize(): bool
    {
        return true;
    }

    /**
     * @return array<string, \Illuminate\Contracts\Validation\ValidationRule|array<mixed>|string>
     */
    public function rules(): array
    {
        return [
            'data.user_email' => 'required',
            'data.user_password' => 'required',
            'data.device_uid' => 'required',
        ];
    }

    protected function passedValidation()
    {
        $this->user_email = $this['data']['user_email'];
        $this->user_password = $this['data']['user_password'];
        $this->device_uid = $this['data']['device_uid'];
    }
}
