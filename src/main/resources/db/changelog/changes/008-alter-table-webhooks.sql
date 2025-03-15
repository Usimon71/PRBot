ALTER TABLE webhooks
    ADD CONSTRAINT fk_webhook_chatid
        FOREIGN KEY (id) REFERENCES integrations(id)
            ON DELETE CASCADE;
