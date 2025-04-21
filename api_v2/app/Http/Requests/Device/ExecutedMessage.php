<?php

namespace App\Http\Requests\Device;

use Illuminate\Foundation\Http\FormRequest;

class ExecutedMessage extends FormRequest
{

    public int $message_id;

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
            'data.message' => 'message_id',
        ];
    }

    protected function passedValidation()
    {
        $this->message_id = $this['data']['message_id'];
    }
}
