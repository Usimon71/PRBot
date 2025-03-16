ALTER TABLE webhooks DROP CONSTRAINT IF EXISTS fk_webhook_chatid;

ALTER TABLE webhooks RENAME COLUMN id TO integration_id;

ALTER TABLE webhooks
    ADD CONSTRAINT your_table_integration_id_fkey
        FOREIGN KEY (integration_id) REFERENCES integrations(id) ON DELETE CASCADE;

ALTER TABLE webhooks ADD COLUMN webhook_id BIGSERIAL PRIMARY KEY;
