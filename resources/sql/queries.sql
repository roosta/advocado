-- :name create-user! :<! :1
-- :doc creates a new user record
INSERT INTO users
(first_name, last_name, username, email, is_active, password)
VALUES (initcap(:first_name), initcap(:last_name), :username, :email, :is_active, :password)
RETURNING user_id;

-- :name update-user! :! :n
-- :doc updates an existing user record
UPDATE users
SET first_name = :first_name, last_name = :last_name, email = :email
WHERE user_id = :user_id;

-- :name update-timestamp! :<! :1
-- :doc updates an existing user last_login field
UPDATE users
SET last_login = :last_login
WHERE user_id = :user_id
RETURNING last_login;

-- :name get-user :? :1
-- :doc retrieves a user record given the username (email)
SELECT * FROM users
WHERE user_id = :user_id;

-- :name email->id :? :1
-- :doc retrieves a user id given the email
SELECT user_id FROM users
WHERE email = :email;

-- :name username->id :? :1
-- :doc retrieves a user id given the username
SELECT user_id FROM users
WHERE username = :username;

-- :name delete-user! :! :n
-- :doc deletes a user record given the id
DELETE FROM users
WHERE user_id = :user_id;

-- :name sql-injection-test! :! :n
INSERT INTO temp (test)
VALUES (:inject);

-- :name update-password! :! :n
-- :doc updates password for user id
UPDATE users
SET password = :password
WHERE user_id = :user_id;

-- :name get-user-roles :? :*
-- :doc get all roles given a user-id
SELECT name FROM roles
LEFT JOIN user_roles
  ON roles.role_id = user_roles.role_id
  WHERE user_roles.user_id = :user_id;

-- :name name->role-id :? :1
-- :doc Get role ids from names
SELECT role_id FROM roles
WHERE name = :name;

-- :name add-role! :! :n
-- :doc Add user_role from user_id and role_id
INSERT INTO user_roles(user_id, role_id)
VALUES (:user_id, :role_id);

