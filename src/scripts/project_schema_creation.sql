CREATE OR REPLACE FUNCTION administration.create_project(project_id character varying)
 RETURNS void AS
$BODY$
DECLARE
    rec record;
BEGIN

EXECUTE format('
   CREATE SCHEMA IF NOT EXISTS %I',  project_id);
EXECUTE format('
   CREATE TABLE IF NOT EXISTS %I.project_segments(segment_id character varying primary key, level_1_number character varying, level_1_name character varying, level_1_name_translated character varying, level_2_number character varying, level_2_name character varying, level_2_name_translated character varying, level_3_number character varying, level_3_name character varying, level_3_name_translated character varying, level_4_number character varying, level_4_name character varying, level_4_name_translated character varying)',
   project_id);

EXECUTE format('
   CREATE TABLE IF NOT EXISTS %I.project_items(item_id character varying , client_item_number character varying primary key, short_description character varying, long_description character varying, short_description_translated character varying, long_description_translated character varying, material_group character varying, pre_classification character varying, row_number serial)',  project_id);
   
EXECUTE format('
   CREATE TABLE IF NOT EXISTS %I.project_rules(rule_id character varying primary key, rule_name character varying,main character varying,application character varying,complement character varying,material_group character varying,pre_classification character varying,drawing boolean,class_id character varying,rule_source character varying,rule_type character varying,rule_level int,rule_scoring double precision,rule_rank int,rule_accuracy double precision,rule_baseline int,user_id character varying,rule_date date, active_status boolean)',  project_id);   

EXECUTE format('
   CREATE TABLE IF NOT EXISTS %I.project_classification_event(classification_event_id character varying primary key,item_id character varying,segment_id character varying,classification_method character varying,rule_id character varying,user_id character varying,rank_within_method int,general_rank int,classification_date date,classification_time timestamp with time zone)',  project_id);   

EXECUTE format('
   CREATE TABLE IF NOT EXISTS %I.project_terms(term_id character varying primary key,term_name character varying,stop_term_status boolean,application_term_status boolean,drawing_term_status boolean)',  project_id);   

EXECUTE format('
   CREATE TABLE IF NOT EXISTS %I.project_pictures(picture_id character varying primary key,picture_data bytea)',  project_id);   

EXECUTE format('
   CREATE TABLE IF NOT EXISTS %I.project_segments_x_pictures(segment_id character varying,picture_id character varying,up_votes int,down_votes int,rank int,PRIMARY KEY(segment_id,picture_id)
)',  project_id);   


EXECUTE format('
   CREATE TABLE IF NOT EXISTS %I.project_rules_x_pictures(rule_id character varying,picture_id character varying,up_votes int,down_votes int,rank int,PRIMARY KEY(rule_id,picture_id)
)',  project_id);   

EXECUTE format('
   CREATE TABLE IF NOT EXISTS %I.project_items_x_pictures(item_id character varying,picture_id character varying,up_votes int,down_votes int,rank int,PRIMARY KEY(item_id,picture_id)
)',  project_id);

EXECUTE format('
   CREATE TABLE IF NOT EXISTS %I.project_items_x_rules(item_id character varying,rule_id character varying,rule_application_description_form character varying,PRIMARY KEY(item_id,rule_id)
)',  project_id);

EXECUTE format('
   CREATE TABLE IF NOT EXISTS %I.project_items_x_terms(item_id character varying,term_id character varying,potential_term_type character varying,PRIMARY KEY(item_id,term_id)
)',  project_id);

EXECUTE format('
   CREATE TABLE IF NOT EXISTS %I.project_terms_x_terms(term_id_left character varying,term_id_right character varying,term_relationship character varying,PRIMARY KEY(term_id_left,term_id_right)
)',  project_id);

EXECUTE format('
   CREATE TABLE IF NOT EXISTS %I.project_characteristics(characteristic_id character varying primary key,characteristic_name character varying,characteristic_name_translated character varying, isNumeric boolean, isTranslatable boolean
)',  project_id);

EXECUTE format('
   CREATE TABLE IF NOT EXISTS %I.project_values(value_id character varying primary key,text_values character varying, nominal_value character varying,min_value character varying,max_value character varying,note character varying,uom_id character varying
)',  project_id);

EXECUTE format('
   CREATE TABLE IF NOT EXISTS %I.project_description_patterns(description_pattern_id character varying primary key,description_pattern_form character varying
)',  project_id);

EXECUTE format('
   CREATE TABLE IF NOT EXISTS %I.project_characteristics_x_segments(characteristic_id character varying ,segment_id character varying, sequence integer, isCritical Boolean, allowedValues character varying [],allowedUoMs character varying [], isActive boolean, PRIMARY KEY(characteristic_id,segment_id)
)',  project_id);

EXECUTE format('
   CREATE TABLE IF NOT EXISTS %I.project_items_x_values(item_id character varying, characteristic_id character varying ,user_id character varying, description_method character varying, description_time timestamp with time zone, value_id character varying, description_rule_id character varying ,PRIMARY KEY(item_id,characteristic_id)
)',  project_id);

EXECUTE format('
   CREATE TABLE IF NOT EXISTS %I.project_items_x_values_history(item_id character varying, characteristic_id character varying ,user_id character varying, description_method character varying, description_time timestamp with time zone,value_id character varying, description_rule_id character varying
)',  project_id);

EXECUTE format('
   CREATE TABLE IF NOT EXISTS %I.project_characteristics_x_description_patterns(characteristic_id character varying, description_pattern_id character varying , PRIMARY KEY(characteristic_id,description_pattern_id)
)',  project_id);


END
$BODY$
  LANGUAGE plpgsql;