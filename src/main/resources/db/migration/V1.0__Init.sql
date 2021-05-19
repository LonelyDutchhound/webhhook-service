CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE webhook (

                        id uuid not null unique default uuid_generate_v4(),
                        dt_create timestamp default current_timestamp,
                        dt_update timestamp default current_timestamp,
                        dt_delete timestamp ,
                        is_delete smallint default 0,
                        name varchar not null,
                        url varchar not null


);
CREATE  INDEX ON webhook(id);