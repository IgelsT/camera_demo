<?php

declare(strict_types=1);

namespace Controllers;

use App\MainApp;

class WSController
{

    private $app;

    private $host;
    private $port;
    private $socket;

    public function __construct(MainApp $app)
    {
        $this->app = $app;
        // $this->host = $app->getSettings()['WS']['wshost'];
        // $this->port = $app->getSettings()['WS']['wsport'];
    }

    private function openConnection()
    {
        $host_uri = 'tcp://' . $this->host;

        $error = $errno = $errstr = null;
        $flags = STREAM_CLIENT_CONNECT;
        $context = stream_context_create();
        $this->socket = stream_socket_client(
            "{$host_uri}:{$this->port}",
            $errno,
            $errstr,
            2000,
            $flags,
            $context
        );
        stream_set_timeout($this->socket, 2000);
    }

    private function makeHeaders()
    {
        $key = '';
        for ($i = 0; $i < 16; $i++) {
            $key .= chr(rand(33, 126));
        }
        $key = base64_encode($key);
        $headers = "GET / HTTP/1.1\r\n";
        $headers .= "Host: $this->host:$this->port\r\n";
        $headers .= "Connection: Upgrade\r\n";
        $headers .= "Pragma: no-cache\r\n";
        $headers .= "Cache-Control: no-cache\r\n";
        $headers .= "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0";
        $headers .= "Safari/537.36\r\n";
        $headers .= "Upgrade: websocket\r\n";
        // $headers .= "Origin: chrome-extension://cbcbkhdmedgianpaifchdaddpnmgnknn\r\n";
        $headers .= "Origin: phpApi\r\n";
        $headers .= "Sec-WebSocket-Version: 13\r\n";
        $headers .= "Accept-Encoding: gzip, deflate\r\n";
        $headers .= "Accept-Language: ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7\r\n";
        $headers .= "Sec-WebSocket-Key: $key\r\n";
        // $headers .= "Sec-WebSocket-Key: iKpx4l19wbGIqvO6fZ89Zg==\r\n";
        $headers .= "Sec-WebSocket-Extensions: permessage-deflate; client_max_window_bits\r\n";
        $headers .= "\r\n";
        return $headers;
    }

    private function sendData($data)
    {
        $written = @fwrite($this->socket, $data);

        if ($written === false) {
            return false;
        }
    }

    private function readHandShake()
    {
        $response = '';
        do {
            $buffer = fgets($this->socket, 1024);
            if ($buffer === false) {
                $meta = stream_get_meta_data($this->socket);
                $message = 'Client handshake error';
                return false;
            }
            $response .= $buffer;
        } while (substr_count($response, "\r\n\r\n") == 0);
        return $response;
    }

    private function readData()
    {
        $length = 2;
        $data = '';
        while (strlen($data) < $length) {
            $buffer = @fread($this->socket, $length - strlen($data));
            if (!$buffer) {
                $meta = stream_get_meta_data($this->socket);
                if (!empty($meta['timed_out'])) {
                    // echo 'Client read timeout';
                    return false;
                }
            }
            if ($buffer === false) {
                // echo 'Buffer false';
                return false;
            }
            if ($buffer === '') {
                // echo ("Empty read; connection dead?");
                return false;
            }
            $data .= $buffer;
        }
        return $data;
    }

    private function makePacket($packet)
    {
        /*    protected static $opcodes = [
                'continuation' => 0,
                'text'         => 1,
                'binary'       => 2,
                'close'        => 8,
                'ping'         => 9,
                'pong'         => 10,
            ];	*/

        $masked = true;
        $final = true;
        $payload = json_encode($packet);
        $data = '';

        $byte_1 = $final ? 0b10000000 : 0b00000000; // Final fragment marker.
        $byte_1 |= 1; // Set opcode.
        $data .= pack('C', $byte_1);

        $byte_2 = $masked ? 0b10000000 : 0b00000000; // Masking bit marker.

        // 7 bits of payload length...
        $payload_length = strlen($payload);
        if ($payload_length > 65535) {
            $data .= pack('C', $byte_2 | 0b01111111);
            $data .= pack('J', $payload_length);
        } elseif ($payload_length > 125) {
            $data .= pack('C', $byte_2 | 0b01111110);
            $data .= pack('n', $payload_length);
        } else {
            $data .= pack('C', $byte_2 | $payload_length);
        }

        // Handle masking
        if ($masked) {
            // generate a random mask:
            $mask = '';
            for ($i = 0; $i < 4; $i++) {
                $mask .= chr(rand(0, 255));
            }
            $data .= $mask;

            // Append payload to frame:
            for ($i = 0; $i < $payload_length; $i++) {
                $data .= $payload[$i] ^ $mask[$i % 4];
            }
        } else {
            $data .= $payload;
        }
        return $data;
    }

    private function makeAuth($device_uid)
    {
        return [
            "device_id" => $device_uid,
            "user_name" => $this->app->user['user_name'],
            "user_pass" => $this->app->user['user_password'],
            "user_type" => 2
        ];
    }

    private function submit($data)
    {
        $packet = $this->makePacket($data);
        $this->openConnection();
        $headers = $this->makeHeaders();
        $response = $this->sendData($headers);
        $response = $this->readHandShake();
        $response = $this->sendData($packet);
        $response = $this->readData();
        fclose($this->socket);
    }

    public function sendToDevice($device_uid, $params)
    {
        $dev_info = [
            "device_id" => $device_uid,
            "device_name" => $params['device_name'],
            "device_description" => $params['device_description'],
            "device_camera_id" => $params['device_camera_id'],
            "device_focus" => $params['device_focus'],
            "device_resolution" => $params['device_resolution'],
            "device_orientation" => $params['device_orientation'],
            "device_fps" => $params['device_fps'],
            "device_quality" => $params['device_quality'],
            "device_status" => $params['device_status'],
        ];

        $data = [
            "type" => 100,
            "action" => "settingsToDevice",
            "messageid" => 0,
            "auth" => $this->makeAuth($device_uid),
            "data" => $dev_info
        ];

        $this->submit($data);
    }

    public function rebootDevice($device_uid)
    {
        $dev_info = [
            "device_id" => $device_uid,
        ];

        $data = [
            "type" => 100,
            "action" => "rebootDevice",
            "messageid" => 0,
            "auth" => $this->makeAuth($device_uid),
            "data" => $dev_info
        ];

        $this->submit($data);
    }

    public function requestLog($device_uid)
    {
        // $settings = $this->app->getSettings();
        $dev_info = [
            "device_id" => $device_uid,
            // "uploadUrl" => $settings['apiUrl'] . $settings['apiPath'] . '/upload'
        ];

        $data = [
            "type" => 100,
            "action" => "requestLog",
            "messageid" => 0,
            "auth" => $this->makeAuth($device_uid),
            "data" => $dev_info
        ];

        $this->submit($data);
    }
}
