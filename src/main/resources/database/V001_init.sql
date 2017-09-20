DROP TABLE IF EXISTS historical_eod_price;
DROP TABLE IF EXISTS stock;

CREATE TABLE stock (
  id                BIGSERIAL PRIMARY KEY,
  symbol            VARCHAR(8)  NOT NULL,
  name              VARCHAR(30) NOT NULL,
  exchange          VARCHAR(8)  NOT NULL,
  currency_code     VARCHAR(4)  NOT NULL,
  first_traded_date DATE        NOT NULL,
  inserted          TIMESTAMP   NOT NULL,
  CONSTRAINT unique_stock UNIQUE (symbol, exchange)
);

CREATE TABLE historical_eod_price (
  id         BIGSERIAL PRIMARY KEY,
  date       DATE    NOT NULL,
  open       NUMERIC NOT NULL,
  high       NUMERIC NOT NULL,
  low        NUMERIC NOT NULL,
  close      NUMERIC NOT NULL,
  adjclose   NUMERIC NOT NULL,
  unadjclose NUMERIC NOT NULL,
  volume     BIGINT  NOT NULL,
  stock_id   BIGINT  NOT NULL REFERENCES stock
);

CREATE TYPE STATUS_TYPE AS ENUM ('O', 'C');
CREATE TYPE SLICE_TYPE AS ENUM ('10', '20', '30', '40');

CREATE TABLE position (
  id          BIGSERIAL PRIMARY KEY,
  buy_price   NUMERIC     NOT NULL,
  sell_price  NUMERIC     NOT NULL,
  status      STATUS_TYPE NOT NULL, --(O)pen or (C)lose,
  buy_date    TIMESTAMP   NOT NULL,
  sell_date   TIMESTAMP   NOT NULL,
  slice       SLICE_TYPE  NOT NULL,
  basket_uuid UUID        NOT NULL,
  stock_id    BIGINT      NOT NULL REFERENCES stock
)

