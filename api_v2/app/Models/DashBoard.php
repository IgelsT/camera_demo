<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

/**
 * @property int $dash_id
 * @property int $device_id
 * @property int $user_id
 */
class DashBoard extends Model
{
    protected $table = 'dashboard';

    protected $primaryKey = 'dash_id';

    public $timestamps = false;

    protected $fillable = [
        'user_id',
        'device_id',
    ];
}
