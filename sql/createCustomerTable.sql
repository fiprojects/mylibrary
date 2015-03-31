CREATE TABLE CUSTOMER (
  ID          BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  IDCARD      VARCHAR(50) NOT NULL,
  "NAME"      VARCHAR(50) NOT NULL,
  ADDRESS     VARCHAR(50) NOT NULL,
  TELEPHONE   VARCHAR(50) NOT NULL,
  EMAIL       VARCHAR(50) NOT NULL
);