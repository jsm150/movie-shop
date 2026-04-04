ALTER TABLE operator_account
    ADD COLUMN permissions_json JSON NULL;

UPDATE operator_account
SET permissions_json = JSON_ARRAY()
WHERE permissions_json IS NULL;

ALTER TABLE operator_account
    MODIFY COLUMN permissions_json JSON NOT NULL;
