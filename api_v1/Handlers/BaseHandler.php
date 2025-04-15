<?php

namespace Handlers;

/**
 * @OA\Info(title="backend API", version="0.1")
 *
 *     @OA\Server(
 *         description="OpenApi host",
 *         url="http://camera.local/api"
 *     ),

 * @OA\SecurityScheme(
 *     securityScheme="api_key",
 *     type="apiKey",
 *     in="header",
 *     name="api_key"
 * )
 */

/**
 * @OA\Tag(
 *     name="userAuth",
 *     description="User register and login",
 * )
 * @OA\Tag(
 *     name="test",
 *     description="test"
 * )
 * @OA\Schema(
 *   schema="ErrorResponse",
 *   title="Base error response",
 * 	 @OA\Property(property="result", type="string", example="error"),
 * 	 @OA\Property(property="action", type="string", example="register"),
 * 	 @OA\Property(property="code", type="numeric", example="500"),
 *   @OA\Property(property="data", type="object"),
 *   @OA\Property(property="error", type="object", 
 * 	   @OA\Property(property="message", type="string", example="email send error"),
 * 	   @OA\Property(property="code", type="string", example="EMAIL_SEND_ERROR"),
 * 	   @OA\Property(property="httpCode", type="numeric", example="500"),
 * 	   @OA\Property(property="reason", type="string", example="")
 *   )
 * )
 */

class BasaHandler
{

    function __constructor() {}
}
