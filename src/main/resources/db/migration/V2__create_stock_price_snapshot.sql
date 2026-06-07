create table stock_price_snapshot (
    id bigserial primary key,
    user_id bigint not null references app_user(id),
    stock_id bigint not null references stock(id),
    price numeric(19, 2) not null,
    priced_at timestamp with time zone not null,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    constraint ck_stock_price_snapshot_price check (price >= 0),
    constraint uq_stock_price_snapshot_user_stock unique (user_id, stock_id)
);
