<?php

namespace Handlers;

use Controllers\AuthController;
use App\Vars;
use Models\DeviceModel;
use Models\UserModel;

class AuthHandler
{

    private AuthController $controller;

    function __construct()
    {
        $this->controller = new AuthController(
            Vars::getInstance(),
            new UserModel(),
            new DeviceModel()
        );
    }

    /**
     * @OA\Post(
     *   tags={"register"},
     *   tags={"userAuth"}, 
     *   path="/api/auth/register",
     *   summary="register",
     *   operationId="register",
     *   @OA\RequestBody(
     *     @OA\MediaType(
     *       mediaType="application/json",
     *       @OA\Schema(ref="#/components/schemas/RegisterRequest")
     *     )
     *   ),
     *   @OA\Response(response=200, description="OK",
     *     @OA\MediaType(
     *       mediaType="application/json",
     *       @OA\Schema(ref="#/components/schemas/RegisterResponse")
     *     )
     *   ),
     *   @OA\Response(response=400, description="Bad Request",
     *     @OA\MediaType(
     *       mediaType="application/json",
     *       @OA\Schema(ref="#/components/schemas/ErrorResponse")
     *     )
     *   ),
     *   @OA\Response(response=409, description="Email exist"),
     * )
     *
     * @OA\Schema(
     *   schema="RegisterRequest",
     *   title="User registration request",
     *   @OA\Property(property="action", type="string", example="register"),
     *   @OA\Property(property="data", type="object", 
     *     @OA\Property(property="user_email", type="string", example="test@user.em"),
     *     @OA\Property(property="user_password", type="string", example="md5encriptedPass1234567")
     *   )
     * )
     * 
     * {
     * @OA\Schema(
     *   schema="RegisterResponse",
     *   title="User registration response",
     * 	 @OA\Property(property="result", type="string", example="ok"),
     * 	 @OA\Property(property="action", type="string", example="register"),
     * 	 @OA\Property(property="code", type="numeric", example="200"),
     *   @OA\Property(property="data", type="object", 
     *     @OA\Property(property="register", type="string", example="ok"),
     *   )
     * )
     */
    public function register()
    {
        return $this->controller->register();
    }

    public function getConfirm() {
        return $this->controller->getConfirmUrl();
    }

    public function confirmEmail()
    {
        return $this->controller->confirmEmail();
    }

    public function recovery()
    {
        return $this->controller->recovery();
    }

    /**
     * @OA\Post(
     *   path="/api/auth/login",
     *   summary="login",
     *   tags={"login"}, 
     *   tags={"userAuth"},
     *   @OA\RequestBody(
     *     @OA\MediaType(
     *       mediaType="application/json",
     *       @OA\Schema(ref="#/components/schemas/LoginRequest")
     *     )
     *   ),
     *   @OA\Response(response=200, description="OK",
     *     @OA\MediaType(
     *       mediaType="application/json",
     *       @OA\Schema(ref="#/components/schemas/LoginResponse")
     *     )
     *   ),
     *   @OA\Response(response=400, description="Bad Request"),
     *   @OA\Response(response=401, description="Unathorized"),
     * )
     *
     * @OA\Schema(
     *   schema="LoginRequest",
     *   title="User or device login request",
     *   @OA\Property(property="action", type="string", example="login"),
     *   @OA\Property(property="data", type="object", 
     *     @OA\Property(property="username", type="string", example="admin"),
     *     @OA\Property(property="pass", type="string", example="md5encriptedPass1234567")
     *   )
     * )
     * @OA\Schema(
     *   schema="LoginResponse",
     *   title="User or device login response",
     * 	 @OA\Property(property="result", type="string", example="ok"),
     * 	 @OA\Property(property="action", type="string", example="login"),
     *   @OA\Property(property="data", type="object", 
     *     @OA\Property(property="hash", type="string", example="userHash129084723-4723890wsdfhsjkfhsdjkhalkf"),
     *     @OA\Property(property="user_id", type="numeric", example="1"),
     *     @OA\Property(property="user_email", type="string", example="email@email.em"),
     *     @OA\Property(property="user_name", type="string", example="admin")
     *   )
     * )
     */
    public function login()
    {
        return $this->controller->login();
    }

     /**
     * @OA\Post(
     *   path="/api/auth/loginDevice",
     *   summary="login",
     *   tags={"login"}, 
     *   tags={"userAuth"},
     *   @OA\RequestBody(
     *     @OA\MediaType(
     *       mediaType="application/json",
     *       @OA\Schema(ref="#/components/schemas/LoginDeviceRequest")
     *     )
     *   ),
     *   @OA\Response(response=200, description="OK",
     *     @OA\MediaType(
     *       mediaType="application/json",
     *       @OA\Schema(ref="#/components/schemas/LoginDeviceResponse")
     *     )
     *   ),
     *   @OA\Response(response=400, description="Bad Request"),
     *   @OA\Response(response=401, description="Unathorized"),
     *   @OA\Response(response=403, description="Device limit"),
     * )
     *
     * @OA\Schema(
     *   schema="LoginDeviceRequest",
     *   title="User or device login request",
     *   @OA\Property(property="action", type="string", example="login"),
     *   @OA\Property(property="data", type="object", 
     *     @OA\Property(property="device_uid", type="string", example="deviceUID12345678"),
     *     @OA\Property(property="username", type="string", example="admin"),
     *     @OA\Property(property="pass", type="string", example="md5encriptedPass1234567")
     *   )
     * )
     * @OA\Schema(
     *   schema="LoginDeviceResponse",
     *   title="User or device login response",
     * 	 @OA\Property(property="result", type="string", example="ok"),
     * 	 @OA\Property(property="action", type="string", example="login"),
     *   @OA\Property(property="data", type="object", 
     *     @OA\Property(property="hash", type="string", example="userHash129084723-4723890wsdfhsjkfhsdjkhalkf"),
     *     @OA\Property(property="user", type="object",
     *       @OA\Property(property="user_id", type="numeric", example="1"),
     *       @OA\Property(property="user_email", type="string", example="email@email.em"),
     *       @OA\Property(property="user_hash", type="string", example="userHashToConfirma3284023984038409"),
     *       @OA\Property(property="user_name", type="string", example="admin"),
     *       @OA\Property(property="user_confirm", type="numeric", example="1"),
     *       @OA\Property(property="user_password", type="numeric", example="md5UserPassword23409823094820")
     *     )
     *   )
     * )
     */

    public function loginDevice()
    {
        return $this->controller->loginDevice();
    }

    public function saveProfile()
    {
        return $this->controller->saveProfile();
    }
}
