ALTER TABLE webhooks DROP CONSTRAINT IF EXISTS webhooks_pkey;

ALTER TABLE webhooks ALTER COLUMN webhook_id TYPE BIGINT;

ALTER TABLE webhooks ADD CONSTRAINT webhooks_pkey PRIMARY KEY (webhook_id);
