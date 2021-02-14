create table floor_drawing(
  id INT,
  name VARCHAR(255) NOT NULL,
  upd_date TIMESTAMP,
  text_size NUMBER,
  configuration CLOB,
  CONSTRAINT floor_drawing_pk PRIMARY KEY(id),
  CONSTRAINT floor_drawing_uk UNIQUE(name)
)
/
create table floor_area(
  drawing_id INT,
  area_id INT,
  area_name VARCHAR(255) NOT NULL,
  area_readiness VARCHAR(255) NOT NULL,
  defect_count INT,
  options CLOB,
  area  CLOB,
  cut_sheet  CLOB,
  CONSTRAINT floor_area_pk PRIMARY KEY(drawing_id, area_id),
  CONSTRAINT floor_area_uk UNIQUE(drawing_id, area_name),
  CONSTRAINT area_drawing_fk FOREIGN KEY(drawing_id)
    REFERENCES floor_drawing(id) ON DELETE CASCADE
)
/
CREATE SEQUENCE floor_seq
/
create trigger floor_trigger before insert on floor_drawing for each row when (new.id is null)
begin select floor_seq.nextval into :new.id from dual;end;
/
ALTER TRIGGER floor_trigger COMPILE
/


