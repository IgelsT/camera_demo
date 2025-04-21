<?php

namespace App\Services;

use Illuminate\Support\Facades\DB;
use App\Models\Messages;

class MessagesService
{

    public function getMessagesToSend(int $user_id, string $device_uid)
    {
        return Messages::where([
            ['user_id', '=', $user_id],
            ['device_uid', '=', $device_uid],
            ['message_status', '=', 0],
            ['message_type', '=', 'OUT']
        ])->get();
    }

    public function applyMessages(int $user_id, string $device_uid, array $messages)
    {
        return Messages::where(
            [
                ['user_id', '=', $user_id],
                ['device_uid', '=', $device_uid],
                ['message_status', '!=', 2]
            ]
        )
            ->whereIN('message_id', $messages)
            ->update(['message_status' => 1, 'message_sent_date' => DB::raw('NOW()')]);
    }

    public function executedMessage(int $user_id, string $device_uid, int $messageId)
    {
        return Messages::where(
            [
                ['user_id', '=', $user_id],
                ['device_uid', '=', $device_uid],
                ['message_status', '!=', 2],
                ['message_id', '=', $messageId]
            ]
        )
            ->update(['message_status' => 2]);
    }

    public function deleteMessages(int $user_id, string $device_uid)
    {
        return Messages::where([
            ['user_id', '=', $user_id],
            ['device_uid', '=', $device_uid],
            ['message_type', '=', 'OUT']
        ])->delete();
    }
}
