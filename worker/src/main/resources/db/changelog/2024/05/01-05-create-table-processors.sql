--changeset Daniil Korshunov:create-processors-check-table
create table processors_check
(
    image_id       uuid        not null,
    request_id     uuid        not null,
    filter_type varchar(20) not null check (length(filter_type) > 0),
    PRIMARY KEY (image_id, request_id, filter_type)
);