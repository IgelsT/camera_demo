<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    /**
     * Run the migrations.
     */

    private string $definer = '`root`@`%`';

    public function up(): void
    {
        $result = DB::select("SELECT * FROM mysql.user where user='" . env('DB_USERNAME') . "'");
        $this->definer = "`" . env('DB_USERNAME') . "`@`" . $result[0]->Host . "`";

        DB::unprepared("CREATE DEFINER={$this->definer} PROCEDURE `pr_delete_device`(
            IN `p_id` INT
        )
        BEGIN
            DELETE FROM messages WHERE device_id = p_id;
            DELETE FROM device_state WHERE device_id = p_id;
            DELETE FROM device_logs WHERE device_id = p_id;
            DELETE FROM device_camera WHERE device_id = p_id;
            DELETE FROM devices WHERE device_id = p_id;
            DELETE FROM dashboard WHERE device_id = p_id;
        END");

        DB::unprepared("CREATE DEFINER={$this->definer}PROCEDURE `pr_message_to_device`(
            IN `p_user_id` INT,
            IN `p_device_id` INT,
            IN `p_device_uid` VARCHAR(50),
            IN `p_message` TEXT
        )
        BEGIN
        DECLARE p_message_id INTEGER;
            INSERT INTO messages(message, message_type, message_status, user_id, device_uid, device_id)
            VALUES(p_message, 'OUT', 0, p_user_id, p_device_uid, p_device_id);
        END");

        DB::unprepared("CREATE DEFINER=`video`@`%` PROCEDURE `pr_set_online`(
            IN `p_device_uid` VARCHAR(50),
            IN `p_user_id` INT,
            IN `p_isonline` INT
        )
        BEGIN
            DECLARE p_dev_id INTEGER;

            SELECT device_id INTO p_dev_id FROM devices WHERE device_uid = p_device_uid AND user_id = p_user_id;
            IF(p_dev_id IS NOT NULL) THEN
                UPDATE device_state SET device_online = p_isonline WHERE device_id = p_dev_id;
            END IF;
        END");

        DB::unprepared("CREATE DEFINER={$this->definer} PROCEDURE `pr_update_camera`(
            IN `p_user_id` INT,
            IN `p_device_uid` VARCHAR(50),
            IN `p_camera_num` INT,
            IN `p_camera_type` VARCHAR(12),
            IN `p_camera_resolutions` VARCHAR(250),
            IN `p_camera_focuses` VARCHAR(250)
        )
        BEGIN
            DECLARE p_dev_id INTEGER;
            DECLARE p_cam_id INTEGER;

            SELECT device_id INTO p_dev_id FROM devices WHERE device_uid = p_device_uid AND user_id = p_user_id LIMIT 0,1;

             IF(p_dev_id is NULL) THEN
                 INSERT INTO devices(device_uid, user_id) VALUES(p_device_uid, p_user_id);
                SELECT device_id INTO p_dev_id FROM devices WHERE device_uid = p_device_uid AND user_id = p_user_id LIMIT 0,1;
            END IF;

            IF(p_dev_id is not NULL) THEN
                SELECT camera_id INTO p_cam_id FROM device_camera WHERE device_id = p_dev_id AND camera_num = p_camera_num LIMIT 0,1;
                IF(p_cam_id IS NULL) THEN
                    INSERT INTO device_camera(camera_num, camera_type, camera_resolutions, camera_focuses, device_id)
                    VALUES(p_camera_num, p_camera_type, p_camera_resolutions, p_camera_focuses, p_dev_id);
                ELSE
                     UPDATE device_camera SET camera_type = p_camera_type, camera_resolutions = p_camera_resolutions, camera_focuses = p_camera_focuses
                    WHERE camera_id = p_cam_id;
                END IF;
            END IF;
        END");

        DB::unprepared("CREATE DEFINER={$this->definer} PROCEDURE `pr_update_device`(
            IN `p_user_id` INT,
            IN `p_device_uid` VARCHAR(50),
            IN `p_device_name` VARCHAR(50),
            IN `p_device_description` VARCHAR(250),
            IN `p_device_info` TEXT
        )
        BEGIN
            DECLARE p_dev_id INTEGER;

            SELECT device_id INTO p_dev_id FROM devices WHERE device_uid = p_device_uid AND user_id = p_user_id LIMIT 0,1;

            IF(p_dev_id is NULL) THEN
                INSERT INTO devices(device_uid, device_name, device_description, device_info, user_id)
                    VALUES(p_device_uid, p_device_name, p_device_description, p_device_info, p_user_id);
            ELSE
                    UPDATE devices SET device_uid = p_device_uid, device_name = p_device_name, device_description = p_device_description
                    , device_info = p_device_info
                    WHERE device_id = p_dev_id;
            END IF;
        END");

        DB::unprepared("CREATE DEFINER={$this->definer} PROCEDURE `pr_update_device_state`(
            IN `p_user_id` INT,
            IN `p_device_uid` VARCHAR(50),
            IN `p_device_name` VARCHAR(50),
            IN `p_device_description` VARCHAR(250),
            IN `p_device_camera_id` INT,
            IN `p_device_focus` VARCHAR(20),
            IN `p_device_resolution` VARCHAR(9),
            IN `p_device_orientation` ENUM('TOP','BOTTOM','LEFT','RIGHT','UNKNOWN'),
            IN `p_device_fps` INT,
            IN `p_device_quality` INT,
            IN `p_device_power` INT,
            IN `p_device_status` INT,
            IN `p_device_location` VARCHAR(50)
        )
        BEGIN
            DECLARE p_dev_id INTEGER;
            DECLARE p_dev_state_id INTEGER;

            SELECT device_id INTO p_dev_id FROM devices WHERE device_uid = p_device_uid AND user_id = p_user_id LIMIT 0,1;

            IF(p_dev_id is NULL) THEN
                INSERT INTO devices(device_uid, user_id, device_name, device_description) VALUES(p_device_uid, p_user_id, p_device_name, p_device_description);
                SELECT device_id INTO p_dev_id FROM devices WHERE device_uid = p_device_uid AND user_id = p_user_id LIMIT 0,1;
            ELSE
                UPDATE devices SET device_name = p_device_name, device_description = p_device_description
                WHERE device_uid = p_device_uid AND user_id = p_user_id;
            END IF;

            SELECT device_id INTO p_dev_state_id FROM device_state WHERE device_id = p_dev_id LIMIT 0,1;

            IF(p_dev_state_id IS NULL) THEN
                INSERT INTO device_state(device_id, device_camera_id, device_focus, device_resolution, device_orientation
                , device_fps, device_quality, device_power, device_status, device_online, device_lastactivity, device_location)
                VALUES(p_dev_id, p_device_camera_id, p_device_focus, p_device_resolution, p_device_orientation
                , p_device_fps, p_device_quality, p_device_power, p_device_status, 1, NOW(), p_device_location);
            ELSE
                UPDATE device_state SET device_camera_id = p_device_camera_id, device_focus = p_device_focus
                , device_resolution = p_device_resolution, device_orientation = p_device_orientation
                , device_fps = p_device_fps, device_quality = p_device_quality, device_power = p_device_power
                , device_status = p_device_status, device_online = 1, device_location = p_device_location
                WHERE device_id = p_dev_state_id;
            END IF;
        END");

        DB::unprepared("CREATE DEFINER={$this->definer} EVENT `ev_clearonline`
            ON SCHEDULE
                EVERY 1 MINUTE STARTS '2025-01-01 00:00:00'
            ON COMPLETION NOT PRESERVE
            ENABLE
            DO BEGIN
            update device_state
            SET device_online=0
            where NOW()-device_lastactivity>60;
        END");
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        DB::unprepared("DROP PROCEDURE IF EXISTS pr_delete_device");
        DB::unprepared("DROP PROCEDURE IF EXISTS pr_message_to_device");
        DB::unprepared("DROP PROCEDURE IF EXISTS pr_set_online");
        DB::unprepared("DROP PROCEDURE IF EXISTS pr_update_camera");
        DB::unprepared("DROP PROCEDURE IF EXISTS pr_update_device");
        DB::unprepared("DROP PROCEDURE IF EXISTS pr_update_device_state");
        DB::unprepared("DROP EVENT IF EXISTS ev_clearonline");
    }
};
