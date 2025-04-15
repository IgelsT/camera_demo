<?php

require_once __DIR__ . '/app.php';

function regClasses(string $classname): void
{
    $classname = str_replace("\\", "/", $classname);
    //echo $classname;
    $file = __DIR__ . '/../' . $classname . '.php';
    if (file_exists($file))
        require_once $file;
}

spl_autoload_register('regClasses');

$settings = require_once __DIR__ . '/settings.php';
