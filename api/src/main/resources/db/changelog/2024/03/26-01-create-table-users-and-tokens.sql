--changeset Korshunov Daniil:create-users-table
create sequence users_pk_seq start 1 increment 1;

create table users (
                         id bigint primary key,
                         username varchar(100) not null check (length(username) > 0 ),
                         password varchar(100) not null check (length(password) > 0 )
);

alter table users add constraint username_unique unique(username);
alter table users alter column id set default nextval('users_pk_seq');

--changeset Korshunov Daniil:create-tokens-table
create sequence tokens_pk_seq start 1 increment 1;

create table tokens (
                       id bigint primary key,
                       token text not null check (length(token) > 0),
                       user_id bigint references users(id)
);

alter table tokens alter column id set default nextval('tokens_pk_seq');


