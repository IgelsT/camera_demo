<?php

namespace App;

use Throwable;

// HTTP_ERROR_CODES
// [200, 'OK']
// [204, 'No Content']
// [400, 'Bad Request']
// [401, 'Unauthorized']
// [403, 'Forbidden']
// [404, 'Not Found']
// 405 Method Not Allowed
// 409 Conflict
// [500, 'Internal Server Error']
// 501 Not Implemented

class ERROR_CODES
{
    static array $ENV_FILE_NOTFOUND = ['ENV_FILE_NOTFOUND', '.env file not found', 500];
    static array $JSON_DECODE_ERROR = ['JSON_DECODE_ERROR', 'json decode error', 400];
    static array $WRONG_REQUEST = ['WRONG_REQUEST', 'wrong request', 400];
    static array $PARAM_REQUIRED = ['PARAM_REQUIRED', 'param required', 400];
    static array $INTERNAL_ERROR = ['INTERNAL_ERROR', 'internal error', 500];
    static array $NO_RETURN_DATA = ['NO_RETURN_DATA', 'no return data', 500];
    static array $WRONG_RETURN_DATA = ['WRONG_RETURN_DATA', 'wrong return data, must be array', 500];
    static array $CREATE_MODEL_ERRROR = ['CREATE_MODEL_ERRROR', 'empty required fields', 500];
    static array $DB_CONNECTION_ERROR = ['DB_CONNECTION_ERROR', 'DB connection error', 500];
    static array $DB_REQUEST_ERROR = ['DB_REQUEST_ERROR', 'DB request error', 500];
    static array $BAD_TOKEN = ['BAD_TOKEN', 'bad token', 401];
    static array $EMAIL_EXIST = ['EMAIL_EXIST', 'email exist', 400];
    static array $EMAIL_NOT_EXIST = ['EMAIL_NOT_EXIST', 'email не зарегистрирован!', 404];
    static array $EMAIL_SEND_ERROR = ['EMAIL_SEND_ERROR', 'email send error', 500];
    static array $WRONG_PASSWORD = ['WRONG_PASSWORD', 'Не правильный email/пароль!', 403];
    static array $EMPTY_PASSWORD = ['EMPTY_PASSWORD', 'empty password!', 400];
    static array $INVALID_HASH = ['WRONG_HASH', 'wrong hash', 403];
    static array $USER_NOT_CONFIRM = ['NOT_CONFIRMED', 'Аккаунт не подтвержден! Ссылка для подтверждения отправлена на email.', 401];
    static array $FILESYSTEM_ERROR = ['FILESYSTEM_ERROR', 'error on filesistem operation', 500];
    static array $DEVICE_NOT_FOUND = ['DEVICE_NOT_FOUND', 'Device not found!', 404];
    static array $ERROR_REQUEST_PARAMS = ['ERROR_REQUEST_PARAMS', 'error in request params', 400];
    static array $ERROR_SEND_TO_DEVICE = ['ERROR_SEND_TO_DEVICE', 'Error send to device!', 400];
    static array $FILE_NOT_FOUND = ['FILE_NOT_FOUND', 'File not found!', 404];
    static array $FILE_UPLOAD_ERROR = ['FILE_UPLOAD_ERROR', 'File upload error!', 500];
    static array $DEVICE_LIMIT = ['DEVICE_LIMIT', 'device limit reached', 403];
}

class ApiError extends \Error
{
    protected $message;
    public readonly string $apiCode;
    var int $httpCode;
    var string $reason;
    private ?array $trace = null;

    /** @deprecated */
    public function __construct(array $error, string $reason = '', Throwable $exception = null)
    {
        $this->apiCode = $error[0];
        $this->message = $error[1];
        $this->httpCode = isset($error[2]) ? $error[2] : 200;
        $this->reason = $reason;
        if ($exception != null) $this->parseError($exception);
    }

    private function parseError(Throwable $exception): void
    {
        $this->file = $exception->getFile();
        $this->line = $exception->getLine();
        $this->message = $exception->getMessage();
        $this->trace = $exception->getTrace();
    }

    public function __toString(): string
    {
        // "App\ApiError: no return data in D:\Projects\Develop\vue\tgMiniApp\api\public_html\api\Services\CarsService.php:153
        // Stack trace:
        // #0 D:\Projects\Develop\vue\tgMiniApp\api\public_html\api\Handlers\CarsHandler.php(39): Services\CarsService->getCarsListAdmin()
        // #1 D:\Projects\Develop\vue\tgMiniApp\api\public_html\api\App\app.php(179): Handlers\CarsHandler->getCarsListAdmin()
        // #2 D:\Projects\Develop\vue\tgMiniApp\api\public_html\api\index.php(39): App\MainApp->run()
        // #3 {main}" while reading response header from upstream, client: 127.0.0.1, server: tgminiapp.local, request: "POST /api/cars/getcarslistadmin HTTP/1.1", upstream: "fastcgi://127.0.0.1:812", host: "tgminiapp.local:8086"
        $str = "{$this->message} in {$this->file}:{$this->line}\n";
        if ($this->trace == null) $trace = $this->getTrace();
        else $trace = $this->trace;
        foreach ($trace as $key => $row) {
            $str .= sprintf(
                "#%s %s(%s): %s%s%s()\n",
                $key,
                isset($row['file']) ? $row['file'] : "no_file",
                isset($row['line']) ? $row['line'] : "no_line",
                isset($row['class']) ? $row['class'] : "no_class",
                isset($row['type']) ? $row['type'] : "no_type",
                $row['function']
            );
        }
        // return parent::__toString();
        return $str;
    }

    public static function fromCODE(array $params, string $reason = ''): ApiError
    {
        return new self($params, $reason);
    }

    public static function fromException(Throwable $exception): ApiError
    {
        return new self(ERROR_CODES::$INTERNAL_ERROR, 'error in code', $exception);
    }
}
