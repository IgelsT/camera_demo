<?php

namespace App\Http\Requests\DeviceFront;

use Illuminate\Foundation\Http\FormRequest;

class RequestLogFile extends FormRequest
{
    public int $device_id;
    public string $file_name;

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
            'data.device_id' => 'required',
            'data.file_name' => 'required',
        ];
    }

    protected function passedValidation()
    {
        $this->device_id = $this['data']['device_id'];
        $this->file_name = $this['data']['file_name'];
    }
}
