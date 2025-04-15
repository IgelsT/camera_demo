<?php

namespace App;

use App\Dotenv;

$dotenv = Dotenv::load(__DIR__ . '/..');

$settings = [
  'apiUrl' => 'http://camera.local',
  'apiPath' => '/api/v1',
  'mainUrl' => 'localhost:8080',
  'rtmpAddress' => 'camera.imile.ru',
  'replayEmail' => 'camera@imile.ru',
  'sendernameEmail' => 'Camera Mailer',
  'confirmUrl' => 'confirm',
  'deviceLogPath' => 'D:/Projects/Develop/camera/web/public_html/deviceLogs/',
  'auth' => [
    'secretPhase' => 'SuperSecretPhase2020',
  ],
  'DB' => [
    'dbhost' => Dotenv::get('DB_HOST'),
    'dbbase' => Dotenv::get("DB_BASE"),
    'dbuser' => Dotenv::get("DB_USER"),
    'dbpass' => Dotenv::get("DB_PASS"),
    'dblevel' => 2
  ],

  'Mailer' => [
    'host' => Dotenv::get('MAIL_HOST'),
    'port' => Dotenv::get('MAIL_PORT'),
    'user' => Dotenv::get('MAIL_USER'),
    'pass' => Dotenv::get('MAIL_PASS'),
  ]
];

return $settings;
