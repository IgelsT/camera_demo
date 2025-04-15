<?php

namespace App\Utils;

class LogClass
{

    public static function LogT(string $text, string $file_sufix = ""): void
    {
        $file = __DIR__ . "/../logs/varlog_" . date("Y.m.d_H.i.s.u") . $file_sufix . ".log";
        file_put_contents($file, $text);
    }

    public static function LogV(mixed $var, string $file_sufix = ""): void
    {
        $file = __DIR__ . "/../logs/varlog_" . date("Y.m.d_H.i.s.u") . $file_sufix . ".log";
        file_put_contents($file, print_r($var, true));
    }

    public static function pprint(mixed $value): void
    {
        echo '<pre>' . print_r($value, true) . '</pre>';
    }

    public static function logToSTD(mixed $var): void
    {
        fwrite(STDERR, print_r($var, TRUE));
    }
}
