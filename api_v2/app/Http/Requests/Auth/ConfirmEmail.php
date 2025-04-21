<?php

namespace App\Http\Requests\Auth;

use Illuminate\Foundation\Http\FormRequest;

class ConfirmEmail extends FormRequest
{

    public string $hash;

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
            'data.hash' => 'required',
        ];
    }

    protected function passedValidation()
    {
        $this->hash = $this['data']['hash'];
    }
}
