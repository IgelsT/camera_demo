<?php

namespace Handlers;

use OpenApi\Annotations as OA;

use App\Vars;
use Controllers\TestController;
use Models\TestModel;


class TestHandler
{

    private TestController $controller;

    function __construct()
    {
        $this->controller = new TestController(
            Vars::getInstance(),
            new TestModel()
        );
    }

    function index()
    {
        return $this->controller->index();
    }


    /**
     * @OA\Post(
     *   tags={"test1"},
     *   path="/api/test",
     *   summary="test1",
     *   tags={"test"},
     *   @OA\RequestBody(
     *     @OA\MediaType(
     *       mediaType="application/json",
     *       @OA\Schema(
     *         @OA\Property(property="action", type="string", example="test1"),
     *         @OA\Property(property="data", type="object", 
     *           @OA\Property(property="device_uid", type="string", example="deviceUID12345678"),
     *           @OA\Property(property="username", type="string", example="admin"),
     *           @OA\Property(property="pass", type="string", example="md5encriptedPass1234567")
     *         )
     *       )
     *     )
     *   ),
     *   @OA\Response(response=200, description="OK",
     *     @OA\JsonContent(
     *       @OA\Schema(ref="#/components/schemas/User1"),
     *       @OA\Examples(example="result", value={"success": true}, summary="An result object.")
     *     )
     *   ),
     *   @OA\Response(response=400, description="Bad Request"),
     *   @OA\Response(response=401, description="Unathorized"),
     *   security={
     *         {"api_key"}
     *     }
     * )
     */


    /**
     * @OA\Schema(
     *   schema="User1",
     *   title="Sample schema for using references",
     * 	   @OA\Property(
     *       property="status",
     *       type="string"
     *     ),
     * 	   @OA\Property(
     *       property="error",
     *       type="string"
     *     )
     * )
     */

    function Test1()
    {
        return $this->controller->test1();
    }

    function Test2() {
        return $this->controller->test2();
    }
}
