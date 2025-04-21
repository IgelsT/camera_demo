<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

/**
 * @property int $device_id
 * @property int $log_id
 * @property string $log_name
 */

class DeviceLog extends Model
{
    protected $primaryKey = 'log_id';

    public $timestamps = false;

    protected $fillable = [
        'device_id',
        'log_name',
    ];
}
