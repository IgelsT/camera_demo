<?php

namespace App\Services;

use App\Mail\SimpleHtmlMail;
use Illuminate\Support\Facades\Mail;

class NotifyService
{

    public function sendConfirm($email, $hash)
    {
        $href = "http://" . env('FRONT_MAIN_URL', '') . "/" . env('FRONT_CONFIRM_URL', '') .
            "/" . $hash;
        $message = 'Для подтверждения регистрации пройдите по ссылке<br>' .
            '<a href="' . $href . '">' . $href . '</a>';
        $subject = 'Регистрация на ' . env('FRONT_MAIN_URL', '');

        Mail::to($email)->send(new SimpleHtmlMail($subject, $message));
    }

    public function recoveryPassword($user_email, $user_password, $user_hash, $user_confirm)
    {
        if ($user_confirm == 0) {
            $href = "http://" . env('FRONT_MAIN_URL', '') . "/" . env('FRONT_CONFIRM_URL', '') .
                "/" . $user_hash;
            $message = 'Для подтверждения регистрации пройдите по ссылке<br>' .
                '<a href="' . $href . '">' . $href . '</a>';
        } else {

            $message = 'Данные для входа<br>' .
                'Имя: ' . $user_email . '<br>' .
                'Пароль: ' . $user_password . '<br>';
        }
        $subject = 'Восстановление пароля на ' . env('FRONT_MAIN_URL', '');

        Mail::to($user_email)->send(new SimpleHtmlMail($subject, $message));
    }
}
