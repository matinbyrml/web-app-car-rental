ALTER TABLE "user"
    ADD score INTEGER;

ALTER TABLE "user"
DROP
COLUMN created_date;

ALTER TABLE "user"
DROP
COLUMN date_of_birth;

ALTER TABLE "user"
    ADD created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL;

ALTER TABLE "user"
    ADD date_of_birth TIMESTAMP WITHOUT TIME ZONE;