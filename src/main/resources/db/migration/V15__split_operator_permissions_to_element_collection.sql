CREATE TABLE operator_permission
(
    permission_id BIGINT AUTO_INCREMENT NOT NULL,
    operator_id   BIGINT NOT NULL,
    payload_json  JSON   NOT NULL,
    CONSTRAINT pk_operator_permission PRIMARY KEY (permission_id)
);

INSERT INTO operator_permission (
    operator_id,
    payload_json
)
SELECT oa.operator_id,
       JSON_EXTRACT(oa.permissions_json, CONCAT('$[', jt.permission_ordinal - 1, ']'))
FROM operator_account oa
         JOIN JSON_TABLE(
        oa.permissions_json,
        '$[*]' COLUMNS (
            permission_ordinal FOR ORDINALITY
            )
              ) AS jt ON TRUE;

CREATE INDEX idx_operator_permission_operator_id
    ON operator_permission (operator_id);

ALTER TABLE operator_permission
    ADD CONSTRAINT fk_operator_permission_operator
        FOREIGN KEY (operator_id) REFERENCES operator_account (operator_id);

ALTER TABLE operator_account
    DROP COLUMN permissions_json;
