<?php

declare(strict_types=1);

namespace Models;

use App\DataBase\ORM\BasicModel;

class MessagesModel extends BasicModel
{
    protected string $_table = 'messages';
    protected string $_id = 'message_id';
    protected $_fields = [
        'message_id',
        'message',
        'message_type',
        'message_status',
        'user_id',
        'device_uid',
        'device_id',
        'message_create_date'
    ];

    public function getMessagesToSend(int $user_id, string $device_uid)
    {
        return $this->select(["message_id, message, message_create_date"])
            ->where("message_status = 0 AND message_type = 'OUT' 
                             AND user_id = :user_id AND device_uid = :device_uid", ['user_id' => $user_id, 'device_uid' => $device_uid])
            ->getAll();
    }

    public function getMessagesToWork(int $user_id, string $device_uid)
    {
        return $this->select(["message_id, message, message_create_date"])
            ->where("message_status < 2 AND message_type = 'OUT' 
                             AND user_id = :user_id AND device_uid = :device_uid", ['user_id' => $user_id, 'device_uid' => $device_uid])
            ->getAll();
    }

    public function applyMessages(int $user_id, string $device_uid, array $messages)
    {
        $query = "UPDATE messages SET message_status = 1, message_sent_date = NOW() 
                                      WHERE user_id = :user_id AND device_uid = :device_uid
                                       AND message_id IN (" . \implode(',', $messages) . ")
                                       AND message_status != 2";
        $this->query($query, ['user_id' => $user_id, 'device_uid' => $device_uid]);
    }

    public function executedMessage(int $user_id, string $device_uid, int $messageId)
    {
        $this->update()->values(['message_status' => 2])->whereId($messageId)->exec();
    }

    public function deleteMessages(int $user_id, int $device_id)
    {
        $this->delete()->where(
            "user_id = :user_id AND device_id = :device_id",
            ['user_id' => $user_id, 'device_id' => $device_id]
        )->exec();
    }
}
