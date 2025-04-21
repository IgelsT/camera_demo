<?php

namespace App\Http\Requests\Device;

use Illuminate\Foundation\Http\FormRequest;

class CameraList extends FormRequest
{

    public array $cameralist;

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
            'data.cameralist' => 'required',
        ];
    }

    protected function passedValidation()
    {
        $this->cameralist = $this['data']['cameralist'];
    }
}
