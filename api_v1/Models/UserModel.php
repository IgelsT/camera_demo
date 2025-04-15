<?php

declare(strict_types=1);

namespace Models;

use DTO\DataBase\UsersDTO;
use App\DataBase\ORM\BasicModel;

interface IUserModel
{
	public function userByName($name): UsersDTO | bool;
	public function userByNamePasswd($name, $passwd): UsersDTO | bool;
	public function userByEmail($email): UsersDTO | bool;
	public function userByEmailPasswd(string $email, string $passwd): UsersDTO | bool;
	public function userByHash(string $hash): UsersDTO | bool;
	public function updateUserLastActivity($id);
	public function getUsersList(): array | bool;
	public function updatePassword($user_id, $password);
	public function addUser(string $user_email, string $user_password, string $user_hash);
	public function saveUser(UsersDTO $user);
}

class UserModel extends BasicModel implements IUserModel
{
	protected string $_table = 'users';
	protected string $_id = 'user_id';

	public function userByName($name): UsersDTO | bool
	{
		$user = $this->select()->where('user_name=:name', ['name' => $name])->getOne();
		if (count($user) == 0) return false;
		return new UsersDTO($user);
	}

	public function userByNamePasswd($name, $passwd): UsersDTO | bool
	{
		$user = $this->select()->where('user_name=:name AND user_password=:passwd', ['name' => $name, 'passwd' => $passwd])->getOne();
		if (count($user) == 0) return false;
		return new UsersDTO($user);
	}

	public function userByEmail($email): UsersDTO | bool
	{
		$user =  $this->select()->where('user_email=:email', ['email' => $email])->getOne();
		if (count($user) == 0) return false;
		return new UsersDTO($user);
	}

	public function userByEmailPasswd(string $email, string $passwd): UsersDTO | bool
	{
		$user =  $this->select()->where('user_email=:email AND user_password=:password', ['email' => $email, 'password' => $passwd])->getOne();
		if (count($user) == 0) return false;
		return new UsersDTO(data: $user, safe: true);
	}

	public function userByHash(string $hash): UsersDTO | bool
	{
		$user = $this->select()->where('user_hash=:hash AND user_confirm = 0', ['hash' => $hash])->getOne();
		if (count($user) == 0) return false;
		return new UsersDTO($user);
	}

	public function updateUserLastActivity($id)
	{
		return $this->query('UPDATE users SET user_lastactivity=NOW() WHERE user_id=' . $id, []);
	}

	public function getUsersList(): array | bool
	{
		return $this->select(['user_id', 'user_name', 'user_description', 'user_isadmin'])->orderBy('user_name')->getAll();
	}

	public function updatePassword($user_id, $password)
	{
		$this->update()->values(['user_password' => $password, 'user_id' => $user_id])->exec();
	}

	public function addUser(string $user_email, string $user_password, string $user_hash)
	{
		$user = new UsersDTO();

		$user->user_hash = $user_hash;
		$user->user_email = $user->user_name = $user_email;
		$user->user_password = $user_password;
		$user->user_confirm = 0;
		$this->upsert($user);
	}

	public function saveUser(UsersDTO $user)
	{
		$this->upsert($user);
	}
}
