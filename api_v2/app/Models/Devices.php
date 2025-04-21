<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

/**
 * @property int $device_id
 * @property string $device_uid
 * @property string $device_name
 * @property string $device_description
 * @property string $device_info
 * @property int $device_access
 * @property int $user_id
 * @property string $device_token
 * @property int $device_deleted
 */
class Devices extends Model
{
    protected $primaryKey = 'device_id';

    // protected $fillable = [
    //     'user_email',
    //     'user_password',
    // ];

    public $timestamps = false;
}
