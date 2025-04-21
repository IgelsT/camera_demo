<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class DeviceState extends Model
{
    protected $primaryKey = 'device_id';
    protected $table = 'device_state';

    // protected $fillable = [
    //     'user_email',
    //     'user_password',
    // ];

    public $timestamps = false;
}
