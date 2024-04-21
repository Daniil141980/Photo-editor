--changeset Daniil Korshunov:create-requests-table
create table requests (
                        id uuid primary key,
                        image_id uuid not null references images(id) on delete cascade,
                        image_modified_id uuid null references images(id) default null,
                        status varchar(10) not null check (length(status) > 0)
);
