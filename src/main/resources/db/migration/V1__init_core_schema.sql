create table app_user (
    id bigserial primary key,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp
);

create table stock (
    id bigserial primary key,
    code varchar(16) not null unique,
    name varchar(255) not null,
    sector varchar(100),
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp
);

create table transaction_entry (
    id bigserial primary key,
    user_id bigint not null references app_user(id),
    stock_id bigint not null references stock(id),
    type varchar(10) not null,
    transaction_date date not null,
    quantity_lot integer not null,
    price numeric(19, 2) not null,
    fee numeric(19, 2) not null default 0,
    notes text,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    constraint ck_transaction_type check (type in ('BUY', 'SELL')),
    constraint ck_transaction_quantity_lot check (quantity_lot > 0),
    constraint ck_transaction_price check (price >= 0),
    constraint ck_transaction_fee check (fee >= 0)
);

create table dividend (
    id bigserial primary key,
    user_id bigint not null references app_user(id),
    stock_id bigint not null references stock(id),
    cum_date date,
    payment_date date not null,
    dividend_per_share numeric(19, 2) not null,
    shares_owned integer not null,
    tax_rate numeric(5, 2) not null default 0,
    net_received numeric(19, 2) not null,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    constraint ck_dividend_per_share check (dividend_per_share >= 0),
    constraint ck_dividend_shares_owned check (shares_owned >= 0),
    constraint ck_dividend_tax_rate check (tax_rate >= 0 and tax_rate <= 100),
    constraint ck_dividend_net_received check (net_received >= 0)
);

create table watchlist_item (
    id bigserial primary key,
    user_id bigint not null references app_user(id),
    stock_id bigint not null references stock(id),
    fair_price numeric(19, 2),
    cheap_price numeric(19, 2),
    very_cheap_price numeric(19, 2),
    expensive_price numeric(19, 2),
    notes text,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    constraint uq_watchlist_item_user_stock unique (user_id, stock_id)
);

create table investment_thesis (
    id bigserial primary key,
    user_id bigint not null references app_user(id),
    stock_id bigint not null references stock(id),
    thesis text not null,
    risks text,
    invalidation_condition text,
    holding_period varchar(50),
    confidence_score smallint,
    emotion_tag varchar(50),
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    constraint ck_investment_thesis_confidence_score check (confidence_score between 0 and 10)
);

create table thesis_review (
    id bigserial primary key,
    thesis_id bigint not null references investment_thesis(id),
    review_date date not null,
    still_valid boolean not null,
    action varchar(20),
    lesson text,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp
);
