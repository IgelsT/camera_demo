<?php

declare(strict_types=1);

namespace App;

use Error;

class Dotenv
{

    protected static ?Dotenv $_instance;

    private array $values = [];

    private function __construct() {}

    public static function getInstance(): Dotenv
    {
        if (!isset(self::$_instance)) {
            self::$_instance = new self;
        }
        return self::$_instance;
    }

    public static function load(string $envfile)
    {
        self::getInstance()->init($envfile);
    }

    public static function get(string $valueName)
    {
        return self::getInstance()->getValue($valueName);
    }

    private function init(string $envfile)
    {
        $file = $envfile . '/.env';
        if (!file_exists($file)) throw new ApiError(ERROR_CODES::$ENV_FILE_NOTFOUND, $file);

        $handle = fopen($file, "r");
        if ($handle) {
            while (($line = fgets($handle)) !== false) {
                $line = trim($line);
                if (str_starts_with($line, '#') || str_starts_with($line, ';') || $line == '') {
                    continue;
                } else {
                    $this->parseLine($line);
                }
            }
            fclose($handle);
        }
    }

    private function parseLine(string $line)
    {
        $parts = explode('=', $line);
        if (count($parts) == 2) {
            $this->values[trim($parts[0])] = $this->cleanStr($parts[1]);
        }
    }

    private function cleanStr(string $str): string
    {
        $str = str_replace('"', '', $str);
        $str = str_replace("'", '', $str);
        $str = trim($str);
        return $str;
    }

    private function getValue(string $valueName)
    {
        if (isset($this->values[$valueName])) {
            return $this->values[$valueName];
        }
        return '';
    }

    private function __clone() {}
    public function __wakeup() {}
}
