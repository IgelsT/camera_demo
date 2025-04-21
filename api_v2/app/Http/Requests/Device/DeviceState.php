<?php

namespace App\Http\Requests\Device;

use Illuminate\Foundation\Http\FormRequest;

class DeviceState extends FormRequest
{

    public array $state;

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
            'data.state' => 'required',
        ];
    }

    protected function passedValidation()
    {
        $this->state = $this['data']['state'];
    }
}
