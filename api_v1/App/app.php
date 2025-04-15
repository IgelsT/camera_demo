<?php

namespace App;

use Controllers\AuthController;
use Models\DeviceModel;
use App\DataBase\DataBase;
use App\Utils\Mailer;
use Models\UserModel;

class ApiRoute
{
    var string $route;
    var string $method;
    var bool $auth;
    var string $action;

    public function __construct(string $route, array $method, bool $auth = true)
    {
        $this->route = strtolower($route);
        $this->method = isset($method[0]) ? $method[0] : "";
        $this->auth = $auth;
        $this->action = isset($method[1]) ? $method[1] : "index";
    }
}

class MainApp
{
    /** @var ApiRoute[] */
    private array $routes;

    /** @var array */
    private array $settings;

    /** @var ApiRequest */
    private ApiRequest $request;

    /** @var ApiResponse */
    private ApiResponse $response;

    private ApiError $lastError;

    /** @psalm-suppress MissingParamType */
    public function GlobalErrorHandler($errno, $errstr, $errfile, $errline): void
    {
        throw new \ErrorException($errstr, 0, $errno, $errfile, $errline);
    }

    public function GlobalExceptionHandler(\Throwable $exception): void
    {
        if (!$exception instanceof ApiError) {
            $exception = ApiError::fromException($exception);
        }
        $this->lastError = $exception;
        // if ($exception->apiCode == 'DB_REQUEST_ERROR') {
        //     echo "<pre>";
        //     print_r($exception); //->getTrace()[0]['args'][0]);
        //     die();
        // }
        $this->sendError($exception);
    }

    public function __construct()
    {
        global $settings;
        /** @psalm-suppress InvalidArgument */
        set_error_handler(array($this, 'GlobalErrorHandler'));
        set_exception_handler(array($this, 'GlobalExceptionHandler'));

        $this->settings = $settings;
        $this->response = new ApiResponse();
        Vars::setSettings($settings);
    }

    public function addRoute(string $route, array $method, bool $auth = true): void
    {
        $this->routes[] = new ApiRoute($route, $method, $auth);
    }

    private function createRequest(): void
    {
        $this->request = new ApiRequest(
            $this->settings['apiPath'],
            $this->routes
        );
        Vars::setRequest($this->request);
        $this->response->setAction($this->request->action);
    }

    private function beforeRun(): void
    {
        $this->createRequest();
        // fix CORS
        if ($this->request->requestMethod == 'OPTIONS') {
            $this->response->setCode(200);
            $this->sendResponse();
        }

        if (!DataBase::getInstance()->setConnection(
            $this->settings['DB']['dbhost'],
            $this->settings['DB']['dbbase'],
            $this->settings['DB']['dbuser'],
            $this->settings['DB']['dbpass'],
            $this->settings['DB']['dblevel']
        )) {
            throw (new ApiError(ERROR_CODES::$DB_CONNECTION_ERROR));
            exit();
        }

        Mailer::getInstance()->createMailer(
            Vars::s()['Mailer']['host'],
            Vars::s()['Mailer']['port'],
            Vars::s()['Mailer']['user'],
            Vars::s()['Mailer']['pass'],
            Vars::s()['replayEmail'],
            Vars::s()['sendernameEmail']
        );
    }

    private function beforeRoute(): void
    {
        // Check route for auth.
        if ($this->request->route != null && $this->request->route->auth) {
            $authController = new AuthController(
                Vars::getInstance(),
                new UserModel(),
                new DeviceModel(),
            );
            $authorization = isset($_SERVER['HTTP_AUTHORIZATION']) ? $_SERVER['HTTP_AUTHORIZATION'] : '';
            $authController->checkAuthorization($authorization);
        }
    }

    private function beforeSend(): void
    {
        if ($this->response->getCode() == 500) {
            $str = $this->lastError;
            error_log($str);
        }
    }

    public function run(): void
    {
        $this->beforeRun();

        if ($this->request->route == null) throw (new ApiError(ERROR_CODES::$WRONG_REQUEST));

        $this->beforeRoute();

        $method = $this->chechMethodExist($this->request->controller, $this->request->action);
        $result = $method();
        if ($result === NULL) throw (new ApiError(ERROR_CODES::$NO_RETURN_DATA));
        if (\gettype($result) == "array") $this->sendOK($result);
        else if (\gettype($result) == "string") $this->sendRaw($result);
        else throw (new ApiError(ERROR_CODES::$WRONG_RETURN_DATA, "must be array or string"));
    }

    private function chechMethodExist(string $controller, string $method): array
    {
        if (!class_exists($controller) || !method_exists($controller, $method)) {
            throw (new ApiError(ERROR_CODES::$WRONG_REQUEST));
        }
        $object = new $controller();
        return array($object, $method);
    }

    #region Response functions

    private function setHeaders(): void
    {
        // header("Vary: Origin");
        if (Dotenv::get('APP_MODE') == 'dev') {
            header("Access-Control-Allow-Origin: *");
            header('Access-Control-Allow-Headers: X-Requested-With, Content-Type, Accept, Origin, Authorization');
            header('Access-Control-Expose-Headers: Content-Disposition');
            header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS');
        }
        header("Expires: 0");
        header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT");
        header("Cache-Control: no-store, no-cache, must-revalidate, max-age=0");
        header("Cache-Control: post-check=0, pre-check=0", false);
        header("Pragma: no-cache");
    }

    private function sendResponse(): void
    {
        $this->beforeSend();

        $this->setHeaders();
        // header("HTTP/1.0 " . $this->response->getCode());
        http_response_code($this->response->getCode());
        header("Content-Type: application/json");
        echo $this->response->toJSON();
        exit();
    }

    private function sendRaw(string $content): void
    {
        $this->beforeSend();

        $this->setHeaders();
        echo $content;
    }

    public function sendFile(string $file_path, string $file_name, string $file_type = 'application/octet-stream'): void
    {
        $this->setHeaders();
        header('X-Accel-Buffering: no');
        // header('Content-Encoding: none;');
        header('Content-Description: File Transfer');
        header('Content-Type: ' . $file_type);
        header('Content-Disposition: attachment; filename=' . $file_name);
        header('Content-Length: ' . filesize($file_path . $file_name));
        header("Content-Transfer-Encoding: binary");

        ini_set('output_buffering', 0);
        ini_set('zlib.output_compression', 0);
        @ob_end_flush();
        @flush();
        readfile($file_path . $file_name);
        exit();
    }

    public function sendOK(array $data = []): void
    {
        $this->response->setOK($data);
        $this->sendResponse();
    }

    public function sendError(ApiError $error, array $data = []): void
    {
        $this->response->setError($error, $data);
        $this->sendResponse();
    }
    #endregion
}
