<?php

namespace App\Http\Controllers;

use App\Repositories\DashboardRepository;
use App\Services\AuthService;
use App\Services\DashboardService;
use Illuminate\Http\Request;

class DashboardController
{

    public function __construct(
        private AuthService $authService,
        private DashboardService $dashboardService
    ) {}

    public function index(): array
    {
        $list = $this->dashboardService->getDeviceList($this->authService->getUserId());
        return ['devicelist' => $list];
    }
}
