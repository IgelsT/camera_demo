<?php

namespace App\Exceptions;

use App\Http\Middleware\AfterApiResponse;
use Illuminate\Foundation\Exceptions\Handler as ExceptionHandler;
use Symfony\Component\HttpKernel\Exception\MethodNotAllowedHttpException;
use Illuminate\Validation\ValidationException;
use Throwable;

class Handler extends ExceptionHandler
{

    public function report(Throwable $exception)
    {
        parent::report($exception);
    }

    public function render($request, Throwable $exception)
    {
        if ($request->is('api/*')) {
            if (!is_a($exception, ApiException::class)) {
                // Hack for override MethodNotAllowedHttpException
                if (is_a($exception, MethodNotAllowedHttpException::class)) {
                    return AfterApiResponse::modifyResponse(
                        $request,
                        ApiException::fromCODE(ERROR_CODES::$WRONG_REQUEST)->render($request)
                    );
                }
                // Hack for override ValidationException
                if (is_a($exception, ValidationException::class)) {
                    return ApiException::fromCODE(
                        ERROR_CODES::$PARAM_REQUIRED,
                        implode(" ", array_map(function ($item) {
                            return $item[0];
                        }, $exception->errors()))
                    )->render($request);
                }

                return ApiException::fromException($exception)->render($request);
            }
        }
        return parent::render($request, $exception);
    }
}
