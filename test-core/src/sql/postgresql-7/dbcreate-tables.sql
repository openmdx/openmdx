CREATE TABLE prefs_Preference(
    object_rid varchar(100) NOT NULL,
    object_oid varchar(200) NOT NULL,
    object_idx integer NOT NULL,
    object__class varchar(100) NULL,
    name varchar(100) NULL,
    description varchar(100) NULL,
    absolute_path varchar(200) NULL,
    parent varchar(100) NULL,
    string_value text NULL,
    integer_value integer NULL,
    boolean_value varchar(10) NULL,
    uri_value varchar(200) NULL,
    decimal_value decimal(18,9) NULL,
    PRIMARY KEY (object_rid, object_oid, object_idx)
);

CREATE TABLE app1_Address (
	object_referenceid INTEGER NOT NULL ,
	object_objectid varchar (200) NOT NULL ,
	object__class varchar (200) NULL ,
	description varchar (200) NULL ,
	created_at varchar (20) NULL ,
	modified_at varchar (20) NULL ,
	postal_code varchar (100) NULL ,
	street varchar (100) NULL ,
	country varchar (100) NULL ,
	city varchar (100) NULL ,
	house_number varchar (100) NULL ,
	address varchar (100) NULL 
)
;

CREATE TABLE app1_Address_N (
	object_referenceid INTEGER NOT NULL ,
	object_objectid varchar (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	modified_by varchar (50) NULL ,
	created_by varchar (50) NULL ,
	address_line varchar (100) NULL
)
;

CREATE TABLE app1_DOC (
	object_referenceid INTEGER NOT NULL ,
	object_objectid varchar (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	created_at varchar (20) NULL ,
	created_by varchar (200) NULL ,
	modified_at varchar (20) NULL ,
	modified_by varchar (200) NULL ,
	object__class varchar (60) NULL ,
	description varchar (100) NULL ,
	content bytea NULL ,
	keyword varchar (32) NULL 
)
;

CREATE TABLE app1_Invoice (
	object_referenceid INTEGER NOT NULL ,
	object_objectid varchar (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	object__class varchar (200) NULL ,
	description varchar (200) NULL ,
	product_group_id varchar (100) NULL ,
	created_at varchar (20) NULL ,
	modified_at varchar (20) NULL ,
	created_by varchar (100) NULL ,
	modified_by varchar (100) NULL 
)
;

CREATE TABLE app1_InvoicePosition (
	object_referenceid INTEGER NOT NULL ,
	object_objectid varchar (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	object__class varchar (200) NULL ,
	description varchar (200) NULL ,
	created_at varchar (20) NULL ,
	modified_at varchar (20) NULL ,
	created_by varchar (100) NULL ,
	modified_by varchar (100) NULL ,
	product_id varchar (50) NULL 
)
;

CREATE TABLE app1_Member (
	object_referenceid INTEGER NOT NULL ,
	object_objectid varchar (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	object__class varchar (200) NULL ,
	description varchar (200) NULL ,
	modified_at varchar (20) NULL ,
	created_at varchar (20) NULL ,
	modified_by varchar (50) NULL ,
	created_by varchar (50) NULL ,
	m1 varchar (200) NULL ,
	m2 varchar (200) NULL 
)
;

CREATE TABLE app1_PersonGroup (
	object_referenceid INTEGER NOT NULL ,
	object_objectid varchar (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	object__class varchar (200) NULL ,
	description varchar (200) NULL ,
	modified_at varchar (20) NULL ,
	created_at varchar (20) NULL ,
	modified_by varchar (50) NULL ,
	created_by varchar (50) NULL ,
	name varchar (100) NULL 
)
;

CREATE TABLE app1_REF (
	object_referenceId INTEGER NOT NULL ,
	n INTEGER NOT NULL ,
	c$0 varchar (100) NOT NULL ,
	c$1 varchar (100) NOT NULL ,
	c$2 varchar (100) NOT NULL ,
	c$3 varchar (100) NOT NULL ,
	c$4 varchar (100) NOT NULL ,
	c$5 varchar (100) NOT NULL ,
	c$6 varchar (100) NOT NULL ,
	c$7 varchar (100) NOT NULL ,
	c$8 varchar (100) NOT NULL ,
	c$9 varchar (100) NOT NULL ,
	c$10 varchar (100) NOT NULL ,
	c$11 varchar (100) NOT NULL ,
	c$12 varchar (100) NOT NULL ,
	c$13 varchar (100) NOT NULL ,
	c$14 varchar (100) NOT NULL ,
	c$15 varchar (100) NOT NULL 
)
;

CREATE SEQUENCE app1_REF_SEQ INCREMENT BY 1 START WITH 100000 MAXVALUE 1000000000 MINVALUE 1;

CREATE TABLE app1_SLICED (
	object_referenceId INTEGER NOT NULL ,
	object_objectId varchar (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	created_at varchar (20) NULL ,
	created_by varchar (200) NULL ,
	modified_at varchar (20) NULL ,
	modified_by varchar (200) NULL ,
	object__class varchar (60) NULL ,
	m1 varchar (200) NULL ,
	last_name varchar (200) NULL ,
	house_number varchar (200) NULL ,
	city varchar (200) NULL ,
	foreign_id varchar (200) NULL ,
	postal_code varchar (200) NULL ,
	description varchar (200) NULL ,
	assigned_address varchar (200) NULL ,
	product_id varchar (200) NULL ,
	salutation varchar (200) NULL ,
	street varchar (200) NULL ,
	address_line varchar (200) NULL ,
	address varchar (200) NULL ,
	text varchar (200) NULL ,
	birthdate varchar (8) NULL ,
	member_of_group varchar (200) NULL ,
	birthdate_as_date_time varchar (200) NULL ,
	person_group varchar (200) NULL ,
	country varchar (200) NULL ,
	sex INTEGER NULL ,
	given_name varchar (200) NULL ,
	product_group_id varchar (200) NULL ,
	place_of_birth varchar (200) NULL 
)
;

CREATE TABLE app1_Segment (
	object_referenceid INTEGER NOT NULL ,
	object_objectid varchar (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	object__class varchar (200) NULL ,
	description varchar (200) NULL 
)
;

CREATE TABLE audit1_Segment (
	object_referenceid INTEGER NOT NULL ,
	object_objectid varchar (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	object__class varchar (200) NULL ,
	description varchar (200) NULL
)
;

CREATE TABLE audit_REF (
	object_referenceId INTEGER NOT NULL ,
	n INTEGER NOT NULL ,
	c$0 varchar (100) NOT NULL ,
	c$1 varchar (100) NOT NULL ,
	c$2 varchar (100) NOT NULL ,
	c$3 varchar (100) NOT NULL ,
	c$4 varchar (100) NOT NULL ,
	c$5 varchar (100) NOT NULL ,
	c$6 varchar (100) NOT NULL ,
	c$7 varchar (100) NOT NULL ,
	c$8 varchar (100) NOT NULL ,
	c$9 varchar (100) NOT NULL ,
	c$10 varchar (100) NOT NULL ,
	c$11 varchar (100) NOT NULL ,
	c$12 varchar (100) NOT NULL ,
	c$13 varchar (100) NOT NULL ,
	c$14 varchar (100) NOT NULL ,
	c$15 varchar (100) NOT NULL 
)
;

CREATE SEQUENCE audit_REF_SEQ INCREMENT BY 1 START WITH 100000 MAXVALUE 1000000000 MINVALUE 1;

CREATE TABLE audit_SLICED (
	object_referenceId INTEGER NOT NULL ,
	object_objectId varchar (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	created_at varchar (20) NULL ,
	created_by varchar (200) NULL ,
	modified_at varchar (20) NULL ,
	modified_by varchar (200) NULL ,
	object__class varchar (60) NULL ,
	m1 varchar (200) NULL ,
	last_name varchar (200) NULL ,
	house_number varchar (200) NULL ,
	city varchar (200) NULL ,
	foreign_id varchar (200) NULL ,
	postal_code varchar (200) NULL ,
	description varchar (200) NULL ,
	assigned_address varchar (200) NULL ,
	product_id varchar (200) NULL ,
	salutation varchar (200) NULL ,
	street varchar (200) NULL ,
	address_line varchar (200) NULL ,
	address varchar (200) NULL ,
	text varchar (200) NULL ,
	birthdate varchar (200) NULL ,
	member_of_group varchar (200) NULL ,
	birthdate_as_date_time varchar (200) NULL ,
	person_group varchar (200) NULL ,
	country varchar (200) NULL ,
	sex INTEGER NULL ,
	given_name varchar (200) NULL ,
	product_group_id varchar (200) NULL ,
	involved varchar (200) NULL ,
	place_of_birth varchar (200) NULL
)
;

CREATE TABLE test_CB_SLICED (
	object_rid INTEGER NOT NULL ,
	object_oid varchar (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	created_at varchar (20) NULL ,
	created_by varchar (200) NULL ,
	modified_at varchar (20) NULL ,
	modified_by varchar (200) NULL ,
	object__class varchar (60) NULL ,
	cb_type varchar (10) NULL ,
	advice_text varchar (200) NULL ,
	cancels_c_b varchar (512) NULL 
)
;

CREATE TABLE test_REF (
	object_rid INTEGER NOT NULL ,
	n INTEGER NOT NULL ,
	c$0 varchar (100) NOT NULL ,
	c$1 varchar (100) NOT NULL ,
	c$2 varchar (100) NOT NULL ,
	c$3 varchar (100) NOT NULL ,
	c$4 varchar (100) NOT NULL ,
	c$5 varchar (100) NOT NULL ,
	c$6 varchar (100) NOT NULL ,
	c$7 varchar (100) NOT NULL ,
	c$8 varchar (100) NOT NULL ,
	c$9 varchar (100) NOT NULL ,
	c$10 varchar (100) NOT NULL ,
	c$11 varchar (100) NOT NULL ,
	c$12 varchar (100) NOT NULL ,
	c$13 varchar (100) NOT NULL ,
	c$14 varchar (100) NOT NULL ,
	c$15 varchar (100) NOT NULL 
)
;

CREATE SEQUENCE test_REF_SEQ INCREMENT BY 1 START WITH 100000 MAXVALUE 1000000000 MINVALUE 1;

CREATE TABLE test_SLB_SLICED (
	object_rid INTEGER NOT NULL ,
	object_oid varchar (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	created_at varchar (20) NULL ,
	created_by varchar (200) NULL ,
	modified_at varchar (20) NULL ,
	modified_by varchar (200) NULL ,
	object__class varchar (60) NULL ,
	slb_type varchar (10) NULL ,
	pos varchar (512) NULL ,
	price DECIMAL(18, 9) NULL ,
	is_debit DECIMAL(18, 9) NULL ,
	is_long DECIMAL(18, 9) NULL ,
	price_currency varchar (10) NULL ,
	value_date varchar (20) NULL ,
	booking_date varchar (20) NULL ,
	quantity DECIMAL(18, 9) NULL ,
	quantity_absolute DECIMAL(18, 9) NULL ,
	visibility varchar (10) NULL ,
	admin_descr varchar (100) NULL ,
	description varchar (200) NULL ,
	cred_value bytea NULL ,
	p$$object_parent__oid varchar (20) NULL ,
	p$$object_parent__rid INTEGER NULL ,
	p$$pos__oid varchar (20) NULL ,
	p$$pos__rid INTEGER NULL ,
	p$$pos_parent__oid varchar (50) NULL ,
	p$$pos_parent__rid INTEGER NULL 
)
;

CREATE TABLE app1_MessageTemplate (
	object_referenceid INTEGER NOT NULL ,
	object_objectid varchar (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	object__class varchar (200) NULL ,
	description varchar (200) NULL ,
	created_at varchar (20) NULL ,
	modified_at varchar (20) NULL ,
	created_by varchar (100) NULL ,
	modified_by varchar (100) NULL
)
;

CREATE TABLE EXTENSION_DEFAULT (	
    OBJECT_RID varchar(200) NOT NULL, 
	  OBJECT_OID varchar(200) NOT NULL, 
	  OBJECT_IDX INTEGER NOT NULL, 
    P$$OBJECT_parent__oid varchar(200), 
	  P$$OBJECT_parent__rid varchar(200) ,
	  OBJECT__VALID_FROM CHAR(20), 
	  OBJECT__VALID_TO CHAR(20), 
	  OBJECT__INVALIDATED_AT CHAR(20), 
	  OBJECT__CLASS varchar(100), 
	  OBJECT__STATE_NUMBER DECIMAL(10,0), 
	  OBJECT__STATE_ID varchar(100), 
	  SEGMENT varchar(200), 
	  VALUE1 CHAR(9), 
	  VALUE2 DECIMAL, 
	  VALUE3 DECIMAL, 
	  VALUE4 DECIMAL, 
	  VALUE5 DECIMAL, 
	  VALUE9 varchar(200), 
	  VALUE10 bytea, 
	  VALUE11A varchar(20), 
	  VALUE11B varchar(20), 
	  P$$SEGMENT_parent__rid varchar(200), 
	  P$$SEGMENT_parent__oid varchar(200), 
	  VALUE6 varchar(200), 
	  VALUE7 CHAR(20), 
	  VALUE8 varchar(8)
  ) ;

CREATE TABLE EXTENSION_NATIVE (
    OBJECT_RID varchar(200) NOT NULL, 
 	  OBJECT_OID varchar(200) NOT NULL, 
	  OBJECT_IDX INTEGER NOT NULL, 
	  P$$OBJECT_parent__rid varchar(200), 
	  P$$OBJECT_parent__oid varchar(200),
	  OBJECT__VALID_FROM TIMESTAMP (3) WITH TIME ZONE, 
	  OBJECT__VALID_TO TIMESTAMP (3) WITH TIME ZONE, 
	  OBJECT__INVALIDATED_AT TIMESTAMP (3) WITH TIME ZONE, 
	  OBJECT__CLASS varchar(100), 
	  OBJECT__STATE_NUMBER DECIMAL(10,0), 
	  OBJECT__STATE_ID varchar(100), 
	  STATE_VALID_FROM DATE, 
	  STATE_VALID_TO DATE, 
	  SEGMENT varchar(200), 
	  P$$SEGMENT_parent__rid varchar(200), 
	  P$$SEGMENT_parent__oid varchar(200),
	  VALUE1 VARCHAR(1), 
	  VALUE2 DECIMAL,
	  VALUE3 DECIMAL, 
	  VALUE4 DECIMAL,
	  VALUE5 DECIMAL, 
	  VALUE9 varchar(200), 
	  VALUE10 bytea, 
	  VALUE11A VARCHAR(10), 
	  VALUE11B VARCHAR(10), 
	  VALUE6 varchar(200),
	  VALUE7 TIMESTAMP (3) WITH TIME ZONE, 
	  VALUE8 DATE
  ) ;

CREATE TABLE EXTENSION_NUMERIC (	
    OBJECT_RID varchar(200) NOT NULL, 
	  OBJECT_OID varchar(200) NOT NULL, 
	  OBJECT_IDX INTEGER NOT NULL, 
	  P$$OBJECT_parent__rid varchar(200), 
	  P$$OBJECT_parent__oid varchar(200),
	  OBJECT__VALID_FROM DECIMAL(19,3), 
	  OBJECT__VALID_TO DECIMAL(19,3), 
	  OBJECT__INVALIDATED_AT DECIMAL(19,3), 
	  OBJECT__CLASS varchar(100), 
	  OBJECT__STATE_NUMBER DECIMAL(10,0), 
	  OBJECT__STATE_ID varchar(100), 
	  SEGMENT varchar(200), 
	  VALUE1 DECIMAL(1,0), 
	  VALUE2 DECIMAL, 
	  VALUE3 DECIMAL,
	  VALUE4 DECIMAL, 
	  VALUE5 DECIMAL, 
	  VALUE9 varchar(200), 
	  VALUE10 bytea, 
	  VALUE11A DECIMAL(9,0),
	  VALUE11B DECIMAL(12,3),
	  P$$SEGMENT_parent__rid varchar(200),
	  P$$SEGMENT_parent__oid varchar(200),
	  VALUE6 varchar(200),
	  VALUE7 DECIMAL(19,3),
	  VALUE8 varchar(8)
  ) ;

CREATE TABLE EXTENSION_SEGMENT (
    OBJECT_RID varchar(200) NOT NULL,
	  OBJECT_OID varchar(200) NOT NULL,
	  OBJECT_IDX INTEGER NOT NULL,
	  OBJECT__CLASS varchar(200),
	  DESCRIPTION varchar(200)
  ) ;

CREATE TABLE datestate_NATIVE (
	OBJECT_ID varchar(200) NOT NULL, 
  	OBJECT__CLASS varchar(200) NOT NULL,
    created_at TIMESTAMP (3) WITH TIME ZONE NULL,
    removed_at TIMESTAMP (3) WITH TIME ZONE NULL,
    openmdxjdo_version DECIMAL(5, 0) NULL,
    core varchar(200) NULL,
    state_valid_from DATE NULL,
    state_valid_to DATE NULL,
    description varchar(100) NULL,
    string_value varchar(100) NULL,
    uri_value varchar (200) NULL,
    boolean_value VARCHAR(1) NULL,
    date_value DATE NULL,
    date_time_value TIMESTAMP (3) WITH TIME ZONE NULL,
    decimal_value DECIMAL(18, 6) NULL,
    duration_value varchar (10) NULL,
    p$$duration_value_yeartomonth VARCHAR(10) NULL,
    p$$duration_value_daytosecond VARCHAR(10) NULL, 
    integer_value DECIMAL(10, 0) NULL,
    long_value DECIMAL(20, 0) NULL,
    short_value DECIMAL(5, 0) NULL,
    a_reference varchar (200) NULL,
    b_reference varchar (200) NULL,
    CONSTRAINT PK_datestate_NATIVE PRIMARY KEY (OBJECT_ID)
  ) ;

  CREATE TABLE datestate_NATIVE_ (
	OBJECT_ID varchar(200) NOT NULL, 
	OBJECT_IDX DECIMAL(5,0) NOT NULL,
  	OBJECT__CLASS varchar(200) NOT NULL,
	created_by varchar(50) NULL,
	removed_at varchar(50) NULL,
	string_list varchar(100) NULL,
	long_array DECIMAL(20, 0) NULL,
	c_reference varchar (200) NULL,
    CONSTRAINT PK_datestate_NATIVE_ PRIMARY KEY (OBJECT_ID,OBJECT_IDX)
  ) ;

 CREATE TABLE aspect_Segment (
	object_id varchar (100) NOT NULL ,
	object__class varchar (100) NULL ,
	description varchar (200) NULL
  )
  ;

  CREATE TABLE aspect_Object (
	object_id varchar (100) NOT NULL ,
	object__class varchar (100) NULL ,
	core varchar (100) NULL ,
	created_at TIMESTAMP (3) WITH TIME ZONE NULL ,
	created_by_ DECIMAL(10, 0) NULL ,
	modified_at TIMESTAMP (3) WITH TIME ZONE NULL ,
	modified_by_ DECIMAL(10, 0) NULL ,
	string varchar (200) NULL ,
	prime_ DECIMAL(10, 0) NULL ,
	url varchar (200) NULL
  )
  ;

  CREATE TABLE aspect_Object_ (
	object_id varchar (100) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	object__class varchar (100) NULL ,
	created_by varchar (200) NULL ,
	modified_by varchar (200) NULL ,
	prime DECIMAL(10, 0) NULL
  )
  ;

  CREATE TABLE state2_NATIVE (
	object_id varchar (100) NOT NULL ,
	object__class varchar (100) NULL ,
	description varchar (60) NULL ,
	core varchar (256) NULL ,
	created_at TIMESTAMP (3) WITH TIME ZONE NULL ,
	removed_at TIMESTAMP (3) WITH TIME ZONE NULL ,
	modified_at TIMESTAMP (3) WITH TIME ZONE NULL ,
	state_valid_from DATE,
	state_valid_to DATE,
	state_a varchar (256) NULL ,
	state_n varchar (256) NULL ,
	string_value varchar (60) NULL ,
	uri_value varchar (200) NULL ,
	boolean_value VARCHAR(1) NULL ,
	date_value DATE NULL ,
	date_time_value TIMESTAMP (3) WITH TIME ZONE NULL ,
	decimal_value DECIMAL(18, 6) NULL ,
	duration_value varchar (10) NULL ,
	p$$duration_value_yeartomonth VARCHAR(10) NULL ,
	p$$duration_value_daytosecond VARCHAR(10) NULL ,
	integer_value DECIMAL(10, 0) NULL ,
	long_value INTEGER NULL ,
	short_value DECIMAL(10, 0) NULL
  );

  CREATE TABLE state2_NATIVE_ (
	object_id varchar (100) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	object__class varchar (100) NULL ,
	created_by varchar (60) NULL ,
	removed_by varchar (60) NULL ,
	modified_by varchar (60) NULL ,
	string_list varchar (60) NULL ,
	long_array INTEGER NULL ,
	state_c varchar (256) NULL
  );

