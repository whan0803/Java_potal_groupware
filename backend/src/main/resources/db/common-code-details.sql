create table if not exists common_code_details (
    code_group_id varchar(30) not null references common_codes(code_group_id) on delete cascade,
    code_value varchar(50) not null,
    code_name varchar(100) not null,
    sort_order integer not null default 0,
    use_yn char(1) not null default 'Y',
    created_at timestamp,
    created_by bigint,
    updated_at timestamp,
    updated_by bigint,
    constraint pk_common_code_details primary key (code_group_id, code_value)
);
