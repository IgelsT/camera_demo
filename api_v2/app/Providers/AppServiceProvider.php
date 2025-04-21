<?php

namespace App\Providers;

use Illuminate\Support\ServiceProvider;
use Illuminate\Contracts\Debug\ExceptionHandler as ExceptionHandlerContract;
use App\Exceptions\Handler;
use App\Repositories\UserRepository;
use App\Repositories\DeviceRepository;
use App\Services\AuthService;

class AppServiceProvider extends ServiceProvider
{
    /**
     * Register any application services.
     */
    public function register(): void
    {
        // Custom error handler
        $this->app->singleton(ExceptionHandlerContract::class, Handler::class);
        $this->app->singleton(AuthService::class);
        // $this->app->bind(UserRepository::class);
        // $this->app->bind(DeviceRepository::class);
    }

    /**
     * Bootstrap any application services.
     */
    public function boot(): void
    {
        //
    }
}
