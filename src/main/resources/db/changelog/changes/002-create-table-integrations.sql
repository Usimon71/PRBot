CREATE TABLE integrations (
      chatid BIGINT NOT NULL,
      name VARCHAR(255) NOT NULL,
      PRIMARY KEY (chatid, name)
);