<?php

namespace App\Http\Middleware;

use App\Exceptions\ApiException;
use App\Exceptions\ERROR_CODES;
use Closure;
use Illuminate\Http\Request;
use Illuminate\Http\JsonResponse;

class AfterApiResponse
{

    public function handle(Request $request, Closure $next): JsonResponse
    {
        // Illuminate\Http\Response

        /** @var \Illuminate\Http\JsonResponse */
        $response = $next($request);

        if (!is_a($response, JsonResponse::class)) {
            throw new ApiException(ERROR_CODES::$INTERNAL_ERROR, 'No json result from controller');
        }

        $response = $this->modifyResponse($request, $response);
        return $response;
    }

    // Need for hack for override MethodNotAllowedHttpException
    public static function modifyResponse(Request $request, JsonResponse $response): JsonResponse
    {
        $original = $response->original;
        $newResponse = [
            'action' => '',
            'code' => $response->getStatusCode()
        ];

        if (isset($original['isError'])) {
            $newResponse['error'] = $original['error'];
        } else {
            $newResponse['data'] = $original;
        }

        $response->setData($newResponse);
        return $response;
    }
}
