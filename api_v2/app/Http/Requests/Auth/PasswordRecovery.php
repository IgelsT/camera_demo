<?php

namespace App\Http\Requests\Auth;

use Illuminate\Foundation\Http\FormRequest;

class PasswordRecovery extends FormRequest
{

    public string $user_email;

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
        ];
    }

    protected function passedValidation()
    {
        $this->user_email = $this['data']['user_email'];
    }
}
