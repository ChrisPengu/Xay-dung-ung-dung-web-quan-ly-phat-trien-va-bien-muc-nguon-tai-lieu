ALTER TABLE user_accounts
    ADD COLUMN approved BIT(1) NOT NULL DEFAULT b'1' AFTER active,
    ADD COLUMN avatar_file_name VARCHAR(120) NULL AFTER avatar_theme;
