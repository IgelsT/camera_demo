<?php

namespace App;

use App\ApiError;

class ApiRequestFile
{
    function __construct(
        public readonly string $param_name,
        public readonly string $name,
        public readonly string $full_path,
        public readonly string $type,
        public readonly string $tmp_name,
        public readonly int $error,
        public readonly int $size
    ) {}

    public function moveTo(string $dst): void
    {
        $result = move_uploaded_file($this->tmp_name, $dst);
        if (!$result) {
            throw new ApiError(ERROR_CODES::$FILESYSTEM_ERROR, 'error move uploaded file');
        }
    }
}

class ApiRequest
{
    var array $headers = [];
    var string $uri = "";
    var string $requestMethod = "";
    var array $get = [];
    /** @var mixed[] */
    var array $post = [];
    var string $content = "";
    /** @var ApiRequestFile[] */
    var array $files = [];
    var string $methodPath = "/default";
    var string $controller = "";
    var ?ApiRoute $route = null;
    var string $action = "";
    /** @var mixed[] */
    var array $data = [];

    public function __construct(
        string $apiPath,
        array $routes
    ) {
        $this->headers = getallheaders();
        $this->uri = $_SERVER['REQUEST_URI'] ?? '';
        $this->requestMethod = $_SERVER['REQUEST_METHOD'] ?? '';
        $this->get = $_GET;
        $this->post = $_POST;
        // $this->files = $_FILES;
        $this->content = file_get_contents("php://input");

        if ($this->requestMethod == 'OPTIONS') return;

        $uri = preg_split("/\/\?|\?/", $this->uri);
        $uri = str_replace($apiPath, '', isset($uri[0]) ? $uri[0] : '/default');

        $this->methodPath = $uri;

        $data = [];

        /** @var string */
        $contentType = (isset($this->headers['Content-Type'])) ? $this->headers['Content-Type'] : '';

        if (str_contains($contentType, 'application/json')) {
            /** @var mixed[] */
            $data = json_decode($this->content, true);
            $this->action = ((isset($data['action']) && $data['action'] != "")) ? $data['action'] : '';
            $this->data = (isset($data['data'])) ? $data['data'] : [];
        } elseif (str_contains($contentType, 'multipart/form-data')) {
            $this->action = (isset($this->post['action']) && $this->post['action'] != '') ? $this->post['action'] : '';
            $this->data = (isset($this->post['data'])) ? json_decode($this->post['data'], true) : [];
        } else {
            $this->action = (isset($this->get['action']) && $this->get['action'] != '') ? $this->get['action'] : '';
            $this->data = (isset($this->get['data'])) ? json_decode($this->get['data'], true) : [];
        }

        if (isset($_FILES) && count($_FILES) > 0) {
            foreach ($_FILES as $param => $file) {
                $this->files[] = new ApiRequestFile(
                    $param,
                    $file['name'],
                    $file['full_path'],
                    $file['type'],
                    $file['tmp_name'],
                    $file['error'],
                    $file['size'],
                );
            }
        }

        $this->findController($routes);

        // print_r($this);
    }

    private function findController(array $routes): void
    {
        $maxRang = 0;
        foreach ($routes as $route) {
            $pathArr = explode('/', $this->methodPath);
            $routeArr = explode('/', $route->route);

            if (count($pathArr) >= count($routeArr)) {
                $rang = 0;
                for ($i = 0; $i < count($routeArr); $i++) {
                    if ($pathArr[$i] != $routeArr[$i]) {
                        $rang = 0;
                        break;
                    } else {
                        $rang++;
                    }
                }
                if ($rang > $maxRang) {
                    $maxRang = $rang;
                    $this->route = $route;
                    $this->controller = $route->method;
                    if (count($pathArr) > count($routeArr))
                        $this->action = $pathArr[count($routeArr)];
                }
            }
        }
        if ($this->action == "" && $this->route != null) $this->action = $this->route->action;
    }
    //TODO: Params to low case!!!
    public function getParam(string $name, mixed $def_val = ''): mixed
    {
        return (isset($this->data[$name])) ? $this->data[$name] : $def_val;
    }

    public function checkParams(array $fields_array): array
    {
        $ret = [];
        foreach ($fields_array as $field) {
            $value = $this->checkParam($field);
            $ret[$field] = $value;
        }
        return $ret;
    }

    public function checkParam(string $field, string $erroMessage = ''): mixed
    {
        $message = ($erroMessage == '') ? "no " . $field : $erroMessage;
        if (!isset($this->data[$field]))
            throw new ApiError(ERROR_CODES::$PARAM_REQUIRED, $message);
        return $this->data[$field];
    }
}
