<?php

namespace App\Http\Requests\Device;

use Illuminate\Foundation\Http\FormRequest;

class DeviceInfo extends FormRequest
{

    public array $info;

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
            'data.info' => 'required',
        ];
    }

    protected function passedValidation()
    {
        $this->info = $this['data']['info'];
    }
}
