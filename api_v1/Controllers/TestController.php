<?php

declare(strict_types=1);

namespace Controllers;

use App\ApiError;
use App\ERROR_CODES;
use App\IVars;
use DTO\Test\TestDTO;
use DTO\Test\TestDTOData;
use DTO\Test\TestTableRowsDTO;
use DTO\Test\TestTableRowsINDTO;
use Models\ITestModel;
use Models\TestModel;

class TestController
{

    function __construct(
        private IVars $vars,
        private ITestModel $model
    ) {}

    function index()
    {
        $jsonStr = '{
            "action": "login",
            "data": {
              "device_uid": "deviceUID12345678",
              "username": "admin",
              "pass": "md5pass1234567890",
              "rowsInt": [
                    {"id": 1, "name": "first" },
                    {"id": 2, "name": "seecond" }
              ]              
            },
            "rows": [
                {"id": 1, "name": "first" },
                {"id": 2, "name": "seecond" }
            ],
            "rowsIndexes": [100,101,102],
            "isActive": true,
            "startIdx": 17,
            "price": 45.78,
            "intTestNull": null
          }';

        $data = json_decode($jsonStr, true, 512, JSON_THROW_ON_ERROR);
        $dto = new TestDTO($data, false);
        print_r($dto);

        $dtoData = new TestDTOData($data['data']);
        // print_r($dtoData);
        // return ['dataFull' => $data, 'data' => $dtoData];
        return ['OK'];
    }

    function test1(): array
    {

        $data = new TestDTOData($this->vars->getRequestData());
        if (!isset($data->username)) throw new ApiError(ERROR_CODES::$BAD_TOKEN);

        $result = $this->model->createTable();
        $result = $this->model->insertRows();
        $result = $this->model->select1();
        $result = $this->model->updateRows();
        $result = $this->model->select2();
        $result = $this->model->callStoredProcedure();
        $result = $this->model->select3();
        $result = $this->model->insertDTO();
        $result = $this->model->select4();

        return ['result' => $result];
    }

    function test2()
    {
        $model = new TestModel();

        $rows = $model->select1();


        $rowsDTO = new TestTableRowsDTO($rows[0]);
        // print_r($rowsDTO);
        // print_r(json_encode($rowsDTO));

        $inDTO = new TestTableRowsINDTO($this->vars->getRequestData());
        $inDTO->idx = $rowsDTO;

        var_dump($inDTO);
        $rowsDTO1 = new TestTableRowsINDTO($inDTO);

        print_r($rowsDTO1);

        return ['rows' => ''];
    }
}
