CREATE TABLE user_roles(
  user_id INT,
  role_id INT,
  CONSTRAINT fk_user
    FOREIGN KEY(user_id)
      REFERENCES users(user_id),
  CONSTRAINT fk_role
    FOREIGN KEY(role_id)
      REFERENCES roles(role_id)
);
