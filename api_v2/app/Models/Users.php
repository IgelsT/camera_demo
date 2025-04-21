<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

/**
 * @property int $user_id
 * @property string $user_name,
 * @property string $user_description,
 * @property string $user_password,
 * @property string $user_email
 * @property string $user_hash,
 * @property string $user_token,
 * @property int $user_confirm,
 * @property string $user_date,
 * @property string $user_lastactivity
 */
class Users extends Model
{

    protected $primaryKey = 'user_id';

    protected $fillable = [
        'user_email',
        'user_password',
    ];

    public $timestamps = false;
}
