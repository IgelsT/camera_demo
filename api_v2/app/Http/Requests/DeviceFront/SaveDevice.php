<?php

namespace App\Http\Requests\DeviceFront;

use Illuminate\Foundation\Http\FormRequest;

class SaveDevice extends FormRequest
{
    public int $device_id;
    public string $device_name;
    public string $device_description;
    public int $device_access;
    public int $device_camera_id;
    public string $device_focus;
    public string $device_resolution;
    public string $device_orientation;
    public int $device_fps;
    public int $device_quality;
    public int $device_status;
    public int $on_dash;

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
            'data.device_name' => 'required',
            'data.device_description' => 'required',
            'data.device_access' => 'required',
            'data.device_camera_id' => 'required',
            'data.device_focus' => 'required',
            'data.device_resolution' => 'required',
            'data.device_orientation' => 'required',
            'data.device_fps' => 'required',
            'data.device_quality' => 'required',
            'data.device_status' => 'required',
            'data.on_dash' => 'required',
        ];
    }

    protected function passedValidation()
    {
        $this->device_id = $this['data']['device_id'];
        $this->device_name = $this['data']['device_name'];
        $this->device_description = $this['data']['device_description'];
        $this->device_access = $this['data']['device_access'];
        $this->device_camera_id = $this['data']['device_camera_id'];
        $this->device_focus = $this['data']['device_focus'];
        $this->device_resolution = $this['data']['device_resolution'];
        $this->device_orientation = $this['data']['device_orientation'];
        $this->device_fps = $this['data']['device_fps'];
        $this->device_quality = $this['data']['device_quality'];
        $this->device_status = $this['data']['device_status'];
        $this->on_dash = $this['data']['on_dash'];
    }
}
