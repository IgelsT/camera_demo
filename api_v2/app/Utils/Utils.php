<?php

namespace App\Utils;

class Utils
{
    public static function getHash($string)
    {
        $secret = env('SuperSecretPhase2020', '');
        $sig = hash_hmac('sha256', $string, $secret);
        return $sig;
    }

    public static function preparePRC(string $prc, array $params): string
    {
        $query = "call $prc('" . implode("','", $params) .  "')";
        return $query;
    }

    public static function randomPassword($lenght)
    {
        $alphabet = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890';
        $pass = array(); //remember to declare $pass as an array
        $alphaLength = strlen($alphabet) - 1; //put the length -1 in cache
        for ($i = 0; $i < $lenght; $i++) {
            $n = rand(0, $alphaLength);
            $pass[] = $alphabet[$n];
        }
        return implode($pass); //turn the array into a string
    }
}
