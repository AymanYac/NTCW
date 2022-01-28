TABLE tbl_changelog (
  schm TEXT,
  tbl    TEXT,
  op   TEXT,
  record_id JSON,
  old     JSON,
  new     JSON,
  ts timestamp with time zone
);


--bugs out while creating row (non adressable old)
CREATE OR REPLACE FUNCTION changelog_procedure() RETURNS trigger AS $$
BEGIN
INSERT INTO tbl_changelog
VALUES (TG_TABLE_SCHEMA, TG_TABLE_NAME, TG_OP, row_to_json(OLD), row_to_json(NEW),clock_timestamp());
RETURN NULL;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

--complete with json diff on affected fields
CREATE OR REPLACE FUNCTION changelog_procedure() RETURNS trigger as $$
declare
    json_new jsonb;
    json_old jsonb;
    affected_row JSON;
    record_id JSONB;
begin
    if tg_op = 'INSERT' then
        affected_row := row_to_json(NEW);
        json_new:= to_jsonb(new);
    elsif tg_op = 'DELETE' then
        affected_row := row_to_json(OLD);
        json_old:= to_jsonb(old);
    else
        affected_row := row_to_json(NEW);
        select jsonb_object_agg(new_key, new_value), jsonb_object_agg(old_key, old_value)
        into json_new, json_old
        from jsonb_each(to_jsonb(new)) as n(new_key, new_value)
        join jsonb_each(to_jsonb(old)) as o(old_key, old_value) 
        on new_key = old_key and new_value <> old_value;
    end if;

    --Get PK columns
    WITH pk_columns (attname) AS (
        SELECT 
            CAST(a.attname AS TEXT) 
        FROM 
            pg_index i 
            JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY(i.indkey) 
        WHERE 
            i.indrelid = TG_RELID 
            AND i.indisprimary
    )
    SELECT 
        json_object_agg(key, value) INTO record_id
    FROM 
        json_each_text(affected_row)
    WHERE 
        key IN(SELECT attname FROM pk_columns);
    insert into tbl_changelog
    VALUES (TG_TABLE_SCHEMA, TG_TABLE_NAME, TG_OP, record_id, json_old, json_new,clock_timestamp());
    return null;
end;
$$ language plpgsql;



CREATE TRIGGER changelog_trigger
AFTER INSERT OR UPDATE OR DELETE ON administration.projects
FOR EACH ROW EXECUTE PROCEDURE changelog_procedure();