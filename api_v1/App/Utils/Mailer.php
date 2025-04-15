<?php

namespace App\Utils;

require_once __DIR__ . '/../../PHPMailer/PHPMailer.php';
require_once __DIR__ . '/../../PHPMailer/Exception.php';
require_once __DIR__ . '/../../PHPMailer/SMTP.php';

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\SMTP;

class Mailer
{
    protected static Mailer $_instance;

    /** @var PHPMailer */
    private $mailer;

    private function __construct() {}

    public static function getInstance(): Mailer
    {
        if (!isset(self::$_instance)) {
            self::$_instance = new self;
        }
        return self::$_instance;
    }

    public function createMailer(
        string $host,
        int $port,
        string $user,
        string $password,
        string $replayEmail,
        string $replayName
    ) {
        $this->mailer = new PHPMailer();
        $this->mailer->isSMTP();
        $this->mailer->SMTPDebug = SMTP::DEBUG_OFF; //DEBUG_SERVER;
        $this->mailer->SMTPAuth = true;
        $this->mailer->CharSet = "UTF-8";

        $this->mailer->Host = $host;
        $this->mailer->Port = $port;
        $this->mailer->Username = $user;
        $this->mailer->Password = $password;

        $this->mailer->setFrom($replayEmail, $replayName);
    }

    public static function sendEmail(string $to, string $subject, string $body): bool
    {
        return Mailer::getInstance()->_sendEmail($to, $subject, $body);
    }

    private function _sendEmail(string $to, string $subject, string $body): bool
    {
        $title = '=?UTF-8?B?' . base64_encode($subject) . '?=';

        $this->mailer->addAddress($to, '');
        $this->mailer->Subject = $title;
        $this->mailer->msgHTML($body);

        if (!$this->mailer->send()) {
            //echo 'Mailer Error: ' . $mail->ErrorInfo;
            return false;
        } else {
            return true;
        }
    }

    private function __clone() {}
    public function __wakeup() {}
}
