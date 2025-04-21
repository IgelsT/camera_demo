<?php

namespace App\Http\Requests\Device;

use Illuminate\Foundation\Http\FormRequest;

class DeviceLogs extends FormRequest
{

    public array $logs;

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
            'data.logs' => 'required',
        ];
    }

    protected function passedValidation()
    {
        $this->logs = $this['data']['logs'];
    }
}
