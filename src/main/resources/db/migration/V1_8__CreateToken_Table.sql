create table Token (
    id serial primary key,
    token varchar(255) unique not null,
    token_type varchar(255) not null,
    user_id serial not null,
    expiry_date timestamp not null,
    constraint fk_user foreign key (user_id) references users(id) on delete cascade
);