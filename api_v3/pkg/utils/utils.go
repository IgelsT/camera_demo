package utils

import (
	"crypto/md5"
	"encoding/hex"
	"math/rand"
	"strings"
)

// public static function getHash($string)
// {
// 	$secret = env('SuperSecretPhase2020', '');
// 	$sig = hash_hmac('sha256', $string, $secret);
// 	return $sig;
// }

// public static function preparePRC(string $prc, array $params): string
// {
// 	$query = "call $prc('" . implode("','", $params) .  "')";
// 	return $query;
// }

func RandomPassword(lenght int) string {
	chars := []rune("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890")
	charsLength := len(chars)
	passwords := make([]string, lenght)
	for i := 0; i < lenght; i++ {
		simbol := rand.Intn(charsLength)
		passwords[i] = string(chars[simbol])
	}
	return strings.Join(passwords, "")
}

func HashString(str string) string {
	hash := md5.Sum([]byte(str))
	return hex.EncodeToString(hash[:])
}
