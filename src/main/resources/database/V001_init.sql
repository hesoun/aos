drop table if exists historical_eod_price;
drop table if exists stock;

create table stock (
  id bigserial PRIMARY KEY,
  symbol VARCHAR(8) NOT NULL,
  name VARCHAR(30) NOT NULL,
  exchange VARCHAR(8) not NULL,
  currency_code VARCHAR(4) not NULL,
  first_traded_date date not null,
  inserted timestamp not null,
  CONSTRAINT unique_stock UNIQUE (symbol,exchange)
);

create table historical_eod_price (
  id bigserial PRIMARY KEY,
  date date not null,
  open numeric not null,
  high numeric not null,
  low numeric not null,
  close numeric not null,
  adjclose numeric not null,
  unadjclose numeric not null,
  volume BIGINT not null,
  stock_id bigint not null REFERENCES stock
)

