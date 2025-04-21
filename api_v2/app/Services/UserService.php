<?php

namespace App\Services;

use Illuminate\Support\Facades\DB;
use App\Models\Users;
use App\Utils\Utils;

class UserService
{

    public function makeToken(Users $user): string
    {
        $firstPath = base64_encode(json_encode(['user' => $user->user_email]));
        $secondPath = bin2hex(random_bytes(15));
        // $secondPath = Utils::getHash(json_encode(['user' => $id, 'pass' => $unique]));
        $user->user_token = "$firstPath.$secondPath";
        $user->save();
        return $user->user_token;
    }

    public function updateUserLastActivity(int $user_id)
    {
        return DB::table('users')->where('user_id', '=', $user_id)
            ->update(['user_lastactivity' => DB::raw('NOW()')]);
    }

    public function userByEMailPasswd($email, $password): ?Users
    {
        return Users::where([
            ['user_email', '=', $email],
            ['user_password', '=', $password]
        ])->first();
    }

    public function userByEmail($email): ?Users
    {
        return Users::where([['user_email', '=', $email]])->first();
    }

    public function userById($id): ?Users
    {
        return Users::find($id);
    }

    public function userByToken(string $token): ?Users
    {
        return Users::where([['user_token', '=', $token]])->first();
    }

    public function userByHash(string $hash): ?Users
    {
        return Users::where([
            ['user_hash', '=', $hash],
            ['user_confirm', '=', 0]
        ])->first();
    }

    public function updatePassword($user_id, $password)
    {
        return Users::where([
            ['user_id', '=', $user_id],
        ])->update(['user_password' => $password, 'user_token' => '']);
    }

    public function createUser($user_email, $user_password): ?Users
    {
        $user = new Users();
        $user->user_email = $user_email;
        $user->user_password = $user_password;
        $user->user_hash = md5($user->user_email . time());
        if ($user->save()) return $user;
        return null;
    }
}
