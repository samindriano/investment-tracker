alter table investment_thesis
    add constraint uq_investment_thesis_user_stock unique (user_id, stock_id);

create table portfolio_daily_snapshot (
    id bigserial primary key,
    user_id bigint not null references app_user(id),
    snapshot_date date not null,
    total_modal numeric(19, 2) not null,
    total_market_value numeric(19, 2) not null,
    total_unrealized_gain_loss numeric(19, 2) not null,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    constraint uq_portfolio_daily_snapshot unique (user_id, snapshot_date)
);

create table dividend_monthly_snapshot (
    id bigserial primary key,
    user_id bigint not null references app_user(id),
    snapshot_year integer not null,
    snapshot_month integer not null,
    total_gross_dividend numeric(19, 2) not null,
    total_tax numeric(19, 2) not null,
    total_net_dividend numeric(19, 2) not null,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    constraint uq_dividend_monthly_snapshot unique (user_id, snapshot_year, snapshot_month)
);

create table thesis_status_snapshot (
    id bigserial primary key,
    user_id bigint not null references app_user(id),
    snapshot_date date not null,
    total_theses integer not null,
    active_theses integer not null,
    invalidated_theses integer not null,
    reviews_last_30_days integer not null,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    constraint uq_thesis_status_snapshot unique (user_id, snapshot_date)
);
