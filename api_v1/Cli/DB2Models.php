<?php

declare(strict_types=1);

namespace Cli;

use App\DataBase\DataBase;
use Error;
use Dotenv\Dotenv;

require_once __DIR__ . '/../App/autoload.php';

class DB2Models
{

    private string $path = __DIR__ . '/../DTO/DataBase/';
    private array $columnTypes = [
        'int' => 'int',
        'tinyint' => 'int',
        'smallint' => 'int',
        'datetime' => 'string',
        'varchar' => 'string',
        'char' => 'string',
        'text' => 'string',
        'tinytext' => 'string',
        'enum' => 'string',
        'decimal' => 'float',
        'bigint' => 'int',
        'timestamp' => 'string'
    ];

    private string $dbName = '';

    const DATA_TYPE = 'data_type';
    const COLUMN_NAME = 'column_name';
    const IS_NULLABLE = 'is_nullable';

    function __construct()
    {
        global $settings;

        if (!DataBase::getInstance()->setConnection(
            $settings['DB']['dbhost'],
            $settings['DB']['dbbase'],
            $settings['DB']['dbuser'],
            $settings['DB']['dbpass'],
            $settings['DB']['dblevel']
        )) {
            throw (new Error('Error DB connection'));
            exit();
        }
        $this->dbName = $settings['DB']['dbbase'];
    }
    function invoke(): void
    {
        /** @var array */
        $result = DataBase::query("SELECT * FROM information_schema.tables WHERE table_schema='{$this->dbName}';", [])->getRows();
        foreach ($result as $row) {
            /** @var string */
            $tableName = $row['TABLE_NAME'];

            $camelName = $this->snake2camel($tableName);
            echo "Generate for $tableName" . PHP_EOL;
            $columns = DataBase::query("SELECT column_name, data_type, is_nullable
                                            FROM INFORMATION_SCHEMA.COLUMNS
                                            WHERE TABLE_SCHEMA = '{$this->dbName}' AND TABLE_NAME = '$tableName';")->getRows();
            $this->toFile($camelName, $columns);
        }
        echo "invoke";
    }

    private function snake2camel(string $str): string
    {
        $result = '';
        $parts = explode('_', $str);
        foreach ($parts as $part) {
            $result .= ucfirst($part);
        }
        return $result;
    }

    private function getColumnStr(array $column): string
    {
        $type = $column[self::DATA_TYPE];

        if (!key_exists($type, $this->columnTypes)) {
            throw new \Exception("Unknown sql type $type", 1);
        }

        $nullable = ($column[self::IS_NULLABLE] == 'YES') ? '?' : '';

        return "var {$nullable}{$this->columnTypes[$type]} \${$column[self::COLUMN_NAME]};";
    }

    private function toFile(string $name, array $columns): void
    {
        $content = "<?php" . PHP_EOL;
        $content .= PHP_EOL;
        $content .= "declare(strict_types=1);" . PHP_EOL;
        $content .= PHP_EOL;
        $content .= "namespace DTO\DataBase;" . PHP_EOL;
        $content .= PHP_EOL;
        $content .= "use DTO\BaseDTO;" . PHP_EOL;
        $content .= PHP_EOL;
        $content .= "class {$name}DTO extends BaseDTO" . PHP_EOL;
        $content .= "{" . PHP_EOL;
        foreach ($columns as $column) {
            $content .= "    " . $this->getColumnStr($column) . PHP_EOL;;
        }
        $content .= "}" . PHP_EOL;

        $file = $this->path . $name . "DTO.php";
        file_put_contents($file, $content);
    }
}


(new DB2Models())->invoke();
