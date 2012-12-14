--
-- PostgreSQL 
--

--
-- Alter the person table and add user_id so we can track users in es, tied to their cas/lab account
--
-- TODO make this part of automatic build? Pump out reminder to run this statement? Add tests for this db change?
--
ALTER TABLE person ADD COLUMN test character varying(255) NOT NULL DEFAULT 0;