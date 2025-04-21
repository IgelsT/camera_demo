<?php

namespace App\Http\Requests\Auth;

use Illuminate\Foundation\Http\FormRequest;

class UserProfile extends FormRequest
{
    public string $user_password;

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
            'data.user_password' => 'required',
        ];
    }

    protected function passedValidation()
    {
        $this->user_password = $this['data']['user_password'];
    }
}
