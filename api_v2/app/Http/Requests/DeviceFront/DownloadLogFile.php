<?php

namespace App\Http\Requests\DeviceFront;

use Illuminate\Foundation\Http\FormRequest;

class DownloadLogFile extends FormRequest
{
    public int $device_id;
    public string $filename;

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
            'data.filename' => 'required',
        ];
    }

    protected function passedValidation()
    {
        $this->device_id = $this['data']['device_id'];
        $this->filename = $this['data']['filename'];
    }
}
