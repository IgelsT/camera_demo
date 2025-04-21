<?php

namespace App\Http\Middleware;

use App\Exceptions\ApiException;
use App\Exceptions\ERROR_CODES;
use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;
use App\Models\Users;
use App\Repositories\UserRepository;
use App\Repositories\DeviceRepository;
use App\Services\AuthService;

class AuthUser
{
    public function __construct(
        private AuthService $authService,
    ) {}
    /**
     * Handle an incoming request.
     *
     * @param  \Closure(\Illuminate\Http\Request): (\Symfony\Component\HttpFoundation\Response)  $next
     */
    public function handle(Request $request, Closure $next): Response
    {
        $token = $request->header('Authorization', '');
        $this->authService->checkAuthorization($token);
        return $next($request);
    }
}
