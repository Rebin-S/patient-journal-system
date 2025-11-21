IF
NOT EXISTS (
    SELECT 1
    FROM sys.tables t
    WHERE t.name = 'users' AND SCHEMA_NAME(t.schema_id) = 'dbo'
)
BEGIN
CREATE TABLE dbo.users
(
    id            BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    username      NVARCHAR(100) NOT NULL UNIQUE,
    email         NVARCHAR(255) NOT NULL UNIQUE,
    password_hash NVARCHAR(255) NOT NULL,
    created_at    DATETIME2 NOT NULL CONSTRAINT DF_users_created_at DEFAULT SYSUTCDATETIME()
);
END
ELSE
BEGIN
    PRINT
'dbo.users already exists – skipping create';
END