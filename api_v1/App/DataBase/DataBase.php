<?php

namespace App\DataBase;

use PDO;
use PDOException;
use Exception;
use PDOStatement;

class DBResult
{
    public readonly ?string $id;
    public readonly int $rowsCount;

    function __construct(
        public readonly string $query,
        public readonly array $params,
        private PDOStatement $stmt
    ) {
        $this->rowsCount = $stmt->rowCount();
        $this->id = DataBase::getLastID();
    }

    /**
     * @template T
     * @param class-string<T> $model
     * @return array<T>
     */
    public function getObjects(string $model)
    {
        /** @var array<T> */
        return $this->stmt->fetchAll(\PDO::FETCH_CLASS, $model);
    }

    /**
     * @template T
     * @param class-string<T> $model
     * @return ?T
     */
    public function getObject(string $model)
    {
        /** @var T */
        return $this->stmt->fetchObject($model);
    }

    public function getRows(): array
    {
        return $this->stmt->fetchAll(\PDO::FETCH_ASSOC);
    }

    public function getRow(): array
    {
        if ($this->rowsCount == 0) return [];
        /** @var array */
        return $this->stmt->fetch(\PDO::FETCH_ASSOC);
    }
}

class DBError extends Exception
{
    function __construct(
        /** @var string */
        protected $message,
        /** @var int */
        protected $code,
        public array $error,
        public string $query,
        public array $params
    ) {}
}

class DataBase
{
    /** @var PDO */
    private PDO $connlink;
    private int $debugLevel = 0;

    protected static ?DataBase $_instance;

    private function __construct() {}

    public static function getInstance(): DataBase
    {
        if (!isset(self::$_instance)) {
            self::$_instance = new self;
        }
        return self::$_instance;
    }

    public static function setConnection(string $host, string $database, string $user, string $pass, int $debugLevel = 0): bool
    {
        return self::getInstance()->_setConnection($host, $database, $user, $pass, $debugLevel);
    }

    private function _setConnection(string $host, string $database, string $user, string $pass, int $debugLevel): bool
    {
        $this->debugLevel = $debugLevel;
        try {
            $this->connlink = new PDO(
                'mysql:host=' . $host . ';dbname=' . $database . ';charser=utf8mb4',
                $user,
                $pass,
                [
                    PDO::ATTR_EMULATE_PREPARES => true,
                    PDO::ATTR_STRINGIFY_FETCHES => false,
                    PDO::MYSQL_ATTR_USE_BUFFERED_QUERY => true,
                    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                ]
            );
            return true;
        } catch (PDOException $Exception) {
            return false;
        }
    }

    function __destruct() {}

    public static function query(string $query, array $params = []): DBResult
    {
        $result = self::getInstance()->execQuerySafe($query, $params);
        return $result;
    }


    public static function getLastID(): string
    {
        $self = self::getInstance();
        return $self->connlink->lastInsertId();
    }

    private function execQuerySafe(string $query, array $params): DBResult
    {
        if ($this->connlink == null) {
            throw new DBError('-1', -1, ['-1', '-1', 'Connection to server not initialized!'], $this->formatQuery($query), $params);
        }
        // var_dump($query, $params);
        try {
            $stmt = $this->connlink->prepare($query);
            $execResult = $stmt->execute($params);

            $result = new DBResult($this->formatQuery($query), $params, $stmt);
            return $result;
        } catch (\PDOException $e) {
            throw new DBError($e->getMessage(), $e->getCode(), $e->errorInfo, $this->formatQuery($query), $params);
        }
    }

    private function formatQuery(string $query): string
    {
        $query = str_replace("\r", "", $query);
        $query = str_replace("\n", "", $query);
        $query = preg_replace('/\s+/', ' ', $query);
        return $query;
    }

    #endregion
    private function __clone() {}
    public function __wakeup() {}
}
