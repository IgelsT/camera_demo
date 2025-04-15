<?php

namespace App\DTO;

use App\ApiError;
use App\ERROR_CODES;
/*
    String
    Integer
    Float
    Boolean
    Array
    Object
*/

class DTOAttributeInfo
{
    var string $classPropertyName = "";
    var string $dataPropertyName = "";
    var string $classPropertyType = "";
    var string $classPropertySubType = "";
    var bool $classPropertyRequired = false;
    var mixed $defaultValue = null;
}

class DTOFactory
{

    private static array $defaultTapes = [
        'string' => '',
        'int' => 0,
        'float' => 0.0,
        'bool' => false,
        'array' => [],
        'undefined' => '',
    ];

    /**
     * @template T
     * @param T $classInstance
     * @return T
     */
    public static function initFields($classInstance, array | object | null $data, bool $safe = false)
    {
        $refClass = new \ReflectionClass($classInstance);
        $props = $refClass->getProperties();
        $requiredList = "";
        if ($data == null) $data = [];
        if (gettype($data) == 'object') $data = get_object_vars($data);

        foreach ($props as $prop) {
            $attributeInfo = self::getAttributeInfo($prop);
            $classField = $attributeInfo->classPropertyName;
            $keyExist = array_key_exists($attributeInfo->dataPropertyName, $data);

            if (!$keyExist && $attributeInfo->classPropertyRequired) {
                $requiredList .= $attributeInfo->dataPropertyName . ", ";
                continue;
            }

            if (!$keyExist && (($safe && !$prop->isInitialized($classInstance)) || $attributeInfo->defaultValue != null)) {
                $classInstance->$classField = $attributeInfo->defaultValue;
                continue;
            }

            if (!$keyExist) continue;

            if ($safe && !$prop->getType()->allowsNull() && $data[$attributeInfo->dataPropertyName] == null) {
                $classInstance->$classField = $attributeInfo->defaultValue;
                continue;
            }

            if (
                $attributeInfo->classPropertyType == 'string' || $attributeInfo->classPropertyType == 'float'
                || $attributeInfo->classPropertyType == 'bool' || $attributeInfo->classPropertyType == 'int'
                || $attributeInfo->classPropertyType == 'undefined'
            ) {
                $classInstance->$classField = $data[$attributeInfo->dataPropertyName];
                continue;
            }

            if ($attributeInfo->classPropertyType == 'array') {
                if (self::checkDefaultTypes($attributeInfo->classPropertySubType)) {
                    $classInstance->$classField = $data[$attributeInfo->dataPropertyName];
                } else {
                    foreach ($data[$attributeInfo->dataPropertyName] as $row) {
                        $classSubInstance = new $attributeInfo->classPropertySubType();
                        $classInstance->$classField[] = self::initFields($classSubInstance, $row);
                    }
                }
                continue;
            }

            try {
                if (class_exists($attributeInfo->classPropertyType)) {
                    $className = $attributeInfo->classPropertyType;
                    $classSubInstance = new $className();
                    $classInstance->$classField = self::initFields($classSubInstance, $data[$attributeInfo->dataPropertyName]);
                }
            } catch (\Exception $e) {
                // $classInstance->$classField = null;
            }
        }

        if ($requiredList != "")
            throw new ApiError(
                ERROR_CODES::$PARAM_REQUIRED,
                "params required: " . $classInstance::class . "(" . substr($requiredList, 0, -2) . ")"
            );

        return $classInstance;
    }

    private static function getAttributeInfo(\ReflectionProperty $prop): DTOAttributeInfo
    {
        $attributeInfo = new DTOAttributeInfo();
        $attributeInfo->classPropertyName = $attributeInfo->dataPropertyName = $prop->getName();
        /** @psalm-suppress UndefinedMethod */
        $attributeInfo->classPropertyType = $attributeInfo->classPropertySubType = ($prop->getType()) ? $prop->getType()->getName() : 'undefined';
        $attributeInfo->classPropertyRequired = false;
        $attributes = $prop->getAttributes(DTOAttribute::class);
        foreach ($attributes as $attr) {
            $classAttribute = $attr->newInstance();
            if ($classAttribute->field != null) $attributeInfo->dataPropertyName = $classAttribute->field;
            if ($classAttribute->type != null) $attributeInfo->classPropertySubType = $classAttribute->type;
            if ($classAttribute->required != null) $attributeInfo->classPropertyRequired = $classAttribute->required;
            if ($classAttribute->defaultValue != null) $attributeInfo->defaultValue = $classAttribute->defaultValue;
        }
        if ($attributeInfo->classPropertyType == 'undefined' && $attributeInfo->classPropertySubType != 'undefined')
            $attributeInfo->classPropertyType = $attributeInfo->classPropertySubType;

        if ($attributeInfo->defaultValue == null) $attributeInfo->defaultValue = self::getDefaultValue($attributeInfo->classPropertyType);

        return $attributeInfo;
    }

    private static function getDefaultValue(string $type): mixed
    {
        if (self::checkDefaultTypes($type)) return self::$defaultTapes[$type];
        if (class_exists($type)) return new $type();
        return null;
    }

    private static function checkDefaultTypes(string $typeName): bool
    {
        return array_key_exists($typeName, self::$defaultTapes);
    }
}
