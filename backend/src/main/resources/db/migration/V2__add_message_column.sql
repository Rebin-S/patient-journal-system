-- V2__add_message_read_column.sql
ALTER TABLE dbo.message
    ADD [read] bit NOT NULL CONSTRAINT DF_message_read DEFAULT(0);
