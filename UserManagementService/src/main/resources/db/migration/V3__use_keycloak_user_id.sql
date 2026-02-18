ALTER TABLE "user"
    ADD keycloak_user_id VARCHAR(36);

ALTER TABLE "user"
    ADD CONSTRAINT uk_user_keycloak_user_id UNIQUE (keycloak_user_id);