<?php

namespace App\Http\Requests\Device;

use Illuminate\Foundation\Http\FormRequest;

class AppliedMessages extends FormRequest
{

    public array $messages;

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
            'data.messages' => 'required',
        ];
    }

    protected function passedValidation()
    {
        $this->messages = $this['data']['messages'];
    }
}
