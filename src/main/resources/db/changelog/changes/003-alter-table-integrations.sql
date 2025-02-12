ALTER TABLE integrations DROP CONSTRAINT integrations_pkey;

-- Add the new UUID column
ALTER TABLE integrations ADD COLUMN id UUID DEFAULT gen_random_uuid();

-- Add the new primary key constraint using the UUID column
ALTER TABLE integrations ADD PRIMARY KEY (id);