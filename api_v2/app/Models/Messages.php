<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

/**
 * @property int $message_id
 * @property string $message
 * @property string $message_type
 * @property int $message_status
 * @property int $user_id
 * @property string $device_uid
 * @property int $device_id
 * @property string $message_create_date
 * @property string $message_sent_date
 */
class Messages extends Model
{
    protected $primaryKey = 'message_id';

    public $timestamps = false;
}
