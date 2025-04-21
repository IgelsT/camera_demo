<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('dashboard', function (Blueprint $table) {
            $table->integer('dash_id', true);
            $table->integer('device_id')->default(0);
            $table->integer('user_id')->default(0);
        });

        Schema::create('devices', function (Blueprint $table) {
            $table->integer('device_id', true);
            $table->string('device_uid', 50);
            $table->string('device_name', 50)->nullable();
            $table->string('device_description', 250)->nullable();
            $table->text('device_info')->nullable();
            $table->integer('device_access')->default(1);
            $table->integer('user_id');
            $table->string('device_token', 150)->default('');
            $table->integer('device_deleted')->default(0);
        });

        Schema::create('device_camera', function (Blueprint $table) {
            $table->integer('camera_id', true);
            $table->integer('camera_num')->nullable();
            $table->string('camera_type', 15)->nullable();
            $table->string('camera_resolutions', 250)->nullable();
            $table->string('camera_focuses', 250)->nullable();
            $table->integer('device_id')->default(0);
        });

        Schema::create('device_logs', function (Blueprint $table) {
            $table->integer('log_id', true);
            $table->string('log_name', 150)->nullable();
            $table->integer('device_id')->nullable();
        });

        Schema::create('device_state', function (Blueprint $table) {
            $table->integer('device_id')->primary();
            $table->integer('device_camera_id')->nullable();
            $table->string('device_focus', 15)->nullable();
            $table->string('device_resolution', 9)->nullable();
            $table->integer('device_orientation')->nullable();
            $table->integer('device_fps')->nullable();
            $table->integer('device_quality')->nullable();
            $table->integer('device_power')->nullable()->default(-1);
            $table->integer('device_status')->nullable();
            $table->dateTime('device_lastactivity')->useCurrentOnUpdate()->nullable();
            $table->integer('device_online')->nullable()->default(0);
            $table->string('device_location', 50)->default('');
        });

        Schema::create('messages', function (Blueprint $table) {
            $table->integer('message_id', true);
            $table->text('message')->nullable();
            $table->enum('message_type', ['IN', 'OUT'])->nullable();
            $table->smallInteger('message_status')->nullable()->default(0);
            $table->integer('user_id')->nullable();
            $table->string('device_uid', 50)->nullable();
            $table->integer('device_id')->nullable();
            $table->dateTime('message_create_date')->nullable()->useCurrent();
            $table->dateTime('message_sent_date')->nullable();
        });

        Schema::create('users', function (Blueprint $table) {
            $table->integer('user_id', true);
            $table->string('user_name', 50)->default('0');
            $table->tinyText('user_description')->nullable();
            $table->string('user_password', 50)->nullable();
            $table->string('user_email', 50)->nullable();
            $table->string('user_hash', 50)->nullable();
            $table->string('user_token', 150)->default('');
            $table->tinyInteger('user_confirm')->nullable()->default(0);
            $table->dateTime('user_date')->nullable()->useCurrent();
            $table->dateTime('user_lastactivity')->nullable();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('dashboard');
        Schema::dropIfExists('devices');
        Schema::dropIfExists('device_camera');
        Schema::dropIfExists('device_logs');
        Schema::dropIfExists('device_state');
        Schema::dropIfExists('messages');
        Schema::dropIfExists('users');
    }
};
