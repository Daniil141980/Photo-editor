--changeset Daniil Korshunov:create-images-table
create table images (
                       id uuid primary key,
                       filename varchar(100) not null check (length(filename) > 0 ),
                       size bigint not null,
                       user_id bigint null references users(id) on delete cascade
);
