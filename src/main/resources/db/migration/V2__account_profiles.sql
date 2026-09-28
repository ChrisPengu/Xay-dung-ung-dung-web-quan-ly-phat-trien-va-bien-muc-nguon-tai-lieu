ALTER TABLE user_accounts
    ADD COLUMN department VARCHAR(100) DEFAULT NULL AFTER email,
    ADD COLUMN phone VARCHAR(20) DEFAULT NULL AFTER department,
    ADD COLUMN bio VARCHAR(500) DEFAULT NULL AFTER phone,
    ADD COLUMN avatar_theme VARCHAR(20) NOT NULL DEFAULT 'TEAL' AFTER bio;

UPDATE user_accounts
SET department = CASE role
        WHEN 'ADMIN' THEN 'Quản trị hệ thống'
        WHEN 'CATALOGER' THEN 'Phòng Biên mục'
        WHEN 'ACQUISITION' THEN 'Phòng Phát triển nguồn'
    END,
    avatar_theme = CASE role
        WHEN 'ADMIN' THEN 'VIOLET'
        WHEN 'CATALOGER' THEN 'TEAL'
        WHEN 'ACQUISITION' THEN 'AMBER'
    END
WHERE department IS NULL;
