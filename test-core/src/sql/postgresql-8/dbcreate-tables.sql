REM Test test::openmdx::preferences2

  DROP TABLE PREFERENCES2_SEGMENT ;
  CREATE TABLE PREFERENCES2_SEGMENT (
    OBJECT_RID varchar(200) NOT NULL,
	  OBJECT_OID varchar(200) NOT NULL,
	  OBJECT__CLASS varchar(200),
	  DESCRIPTION varchar(200)
  );
  
  DROP TABLE PREFERENCES2_ROOT;
  CREATE TABLE PREFERENCES2_ROOT(
    object_rid varchar(100) NOT NULL,
    object_oid varchar(200) NOT NULL,
    p$$object_parent__rid varchar(100) NULL, 
    p$$object_parent__oid varchar(200) NULL,
    object__class varchar(200) NOT NULL ,
    type varchar(10) NOT NULL,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_by_0 varchar(50) NULL ,
    created_by_0 varchar(50) NULL
  );

  DROP TABLE PREFERENCES2_NODE;
  CREATE TABLE PREFERENCES2_NODE(
    object_rid varchar(100) NOT NULL,
    object_oid varchar(200) NOT NULL,
    p$$object_parent__rid varchar(100) NULL, 
    p$$object_parent__oid varchar(200) NULL,
    object__class varchar(200) NOT NULL ,
    parent varchar (200) NULL ,
    p$$parent__rid varchar (200) NULL ,
    p$$parent__oid varchar (200) NULL ,
    name varchar(100) NULL,
    absolute_path varchar(1000) NOT NULL,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_by_0 varchar(50) NULL ,
    created_by_0 varchar(50) NULL
  );

  DROP TABLE PREFERENCES2_ENTRY;
  CREATE TABLE PREFERENCES2_ENTRY(
    object_rid varchar(100) NOT NULL,
    object_oid varchar(200) NOT NULL,
    p$$object_parent__rid varchar(100) NULL, 
    p$$object_parent__oid varchar(200) NULL,
    object__class varchar(200) NOT NULL,
    value varchar(4000) NULL,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_by_0 varchar(50) NULL ,
    created_by_0 varchar(50) NULL
  );
  
DROP TABLE prefs_Preference;
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
    boolean_value boolean NULL,
    uri_value varchar(200) NULL,
    decimal_value decimal(18,9) NULL,
    PRIMARY KEY (object_rid, object_oid, object_idx)
);

DROP SEQUENCE app1_ref_seq;
CREATE SEQUENCE app1_ref_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 1000000000
  START 100000
  CACHE 1;
  
DROP SEQUENCE audit_ref_seq;
CREATE SEQUENCE audit_ref_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 1000000000
  START 100000
  CACHE 1;
  
DROP SEQUENCE test_ref_seq;
CREATE SEQUENCE test_ref_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 1000000000
  START 100000
  CACHE 1;



/*  Test test::openmdx::app1 */

  DROP TABLE app1_Address;
  CREATE TABLE app1_Address (
    object_rid VARCHAR (100) NULL ,
    p$$object_rsx VARCHAR (100)  NULL ,
    object_oid VARCHAR (100) NULL ,
    p$$object_oid$0 VARCHAR (100)  NULL ,
    p$$object_oid$1 VARCHAR (100)  NULL ,
    p$$object_parent__rid VARCHAR (100)  NULL ,
    p$$object_parent__oid VARCHAR (100)  NULL ,
    object__class VARCHAR (200) NULL ,
    p$$unit_of_work__rid VARCHAR (100)  NULL,
    description VARCHAR (200) NULL ,
    created_at TIMESTAMP (3) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (3) WITH TIME ZONE NULL,
    modified_by_0 VARCHAR (50) NULL ,
    created_by_0 VARCHAR (50) NULL ,    
    postal_code VARCHAR (100) NULL ,
    street VARCHAR (100) NULL ,
    country VARCHAR (100) NULL ,
    city VARCHAR (100) NULL ,
    house_number VARCHAR (100) NULL ,
    address VARCHAR (100) NULL
  );
  
  DROP TABLE app1_Address_N;
  CREATE TABLE app1_Address_N (
    object_rid VARCHAR (100) NULL ,
    p$$object_rsx VARCHAR (100) NULL ,
    object_oid VARCHAR (100) NULL ,
    p$$object_oid$0 VARCHAR (100)  NULL ,
    p$$object_oid$1 VARCHAR (100)  NULL ,
    object_idx INTEGER NOT NULL ,
    address_line VARCHAR (100) NULL
  );
  
  DROP TABLE app1_DOC;
  CREATE TABLE app1_DOC (
    object_rid VARCHAR (100) NULL ,
    p$$object_rsx VARCHAR (100)  NULL ,
    object_oid VARCHAR (100) NULL ,
    p$$object_oid$0 VARCHAR (100)  NULL ,
    p$$object_oid$1 VARCHAR (100)  NULL ,
    object_idx INTEGER NOT NULL ,
    p$$object_parent__rid VARCHAR (100)  NULL ,
    p$$object_parent__oid VARCHAR (100)  NULL ,
    object__class VARCHAR (200) NULL ,
    p$$unit_of_work__rid VARCHAR (100)  NULL,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 VARCHAR (200) NULL ,
    modified_by_0 VARCHAR (200) NULL ,
    description VARCHAR (100) NULL ,
    content bytea NULL ,
    keyword VARCHAR (32) NULL
  );
  
  DROP TABLE app1_Invoice;
  CREATE TABLE app1_Invoice (
    object_rid VARCHAR (100) NULL ,
    p$$object_rsx VARCHAR (100)  NULL ,
    object_oid VARCHAR (100) NULL ,
    p$$object_oid$0 VARCHAR (100)  NULL ,
    p$$object_oid$1 VARCHAR (100)  NULL ,
    object_idx INTEGER NOT NULL ,
    p$$object_parent__rid VARCHAR (100)  NULL ,
    p$$object_parent__oid VARCHAR (100)  NULL ,
    object__class VARCHAR (200) NULL ,
    p$$unit_of_work__rid VARCHAR (100)  NULL,
    description VARCHAR (200) NULL ,
    product_group_id VARCHAR (100) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 VARCHAR (100) NULL ,
    modified_by_0 VARCHAR (100) NULL,
    internationalproductgroupid VARCHAR (100) NULL,
    payment_period VARCHAR (100) NULL    
  );
  
  DROP TABLE app1_InvoicePosition;
  CREATE TABLE app1_InvoicePosition (
    object_rid VARCHAR (100) NULL ,
    p$$object_rsx VARCHAR (100)  NULL ,
    object_oid VARCHAR (100) NULL ,
    p$$object_oid$0 VARCHAR (100)  NULL ,
    p$$object_oid$1 VARCHAR (100)  NULL ,
    object_idx INTEGER NOT NULL ,
    p$$object_parent__rid VARCHAR (100)  NULL ,
    p$$object_parent__oid VARCHAR (100)  NULL ,
    object__class VARCHAR (200) NULL ,
    p$$unit_of_work__rid VARCHAR (100)  NULL,
    description VARCHAR (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 VARCHAR (100) NULL ,
    modified_by_0 VARCHAR (100) NULL ,
    product_id VARCHAR (50) NULL
  );
  
  DROP TABLE app1_Member;
  CREATE TABLE app1_Member (
    object_rid VARCHAR (100) NULL ,
    p$$object_rsx VARCHAR (100)  NULL ,
    object_oid VARCHAR (100) NULL ,
    p$$object_oid$0 VARCHAR (100)  NULL ,
    p$$object_oid$1 VARCHAR (100)  NULL ,
    object_idx INTEGER NOT NULL ,
    p$$object_parent__rid VARCHAR (100)  NULL ,
    p$$object_parent__oid VARCHAR (100)  NULL ,
    object__class VARCHAR (200) NULL ,
    p$$unit_of_work__rid VARCHAR (100)  NULL,
    description VARCHAR (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_by_0 VARCHAR (50) NULL ,
    created_by_0 VARCHAR (50) NULL ,
    m1 VARCHAR (200) NULL ,
    p$$m1__rid VARCHAR (200) NULL ,
    p$$m1__oid VARCHAR (200) NULL ,
    m2 VARCHAR (200) NULL ,
    p$$m2__rid VARCHAR (200) NULL ,
    p$$m2__oid VARCHAR (200) NULL
  );
  
  DROP TABLE app1_PersonGroup;
  CREATE TABLE app1_PersonGroup (
    object_rid VARCHAR (100) NULL ,
    p$$object_rsx VARCHAR (100)  NULL ,
    object_oid VARCHAR (100) NULL ,
    p$$object_oid$0 VARCHAR (100)  NULL ,
    p$$object_oid$1 VARCHAR (100)  NULL ,
    object_idx INTEGER NULL ,
    p$$object_parent__rid VARCHAR (100)  NULL ,
    p$$object_parent__oid VARCHAR (100)  NULL ,
    object__class VARCHAR (200) NULL ,
    p$$unit_of_work__rid VARCHAR (100)  NULL,
    description VARCHAR (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_by_0 VARCHAR (50) NULL ,
    created_by_0 VARCHAR (50) NULL ,
    name VARCHAR (100) NULL
  );
  
  DROP TABLE app1_SLICED;
  CREATE TABLE app1_SLICED (
    object_rid VARCHAR (100) NULL ,
    p$$object_rsx VARCHAR (100)  NULL ,
    object_oid VARCHAR (100) NULL ,
    p$$object_oid$0 VARCHAR (100)  NULL ,
    p$$object_oid$1 VARCHAR (100)  NULL ,
    object_idx INTEGER NOT NULL ,
    p$$object_parent__rid VARCHAR (100)  NULL ,
    p$$object_parent__oid VARCHAR (100)  NULL ,
    object__class VARCHAR (200) NULL ,
    p$$unit_of_work__rid VARCHAR (100)  NULL,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 VARCHAR (200) NULL ,
    modified_by_0 VARCHAR (200) NULL ,
    m1 VARCHAR (200) NULL ,
    last_name VARCHAR (200) NULL ,
    house_number VARCHAR (200) NULL ,
    city VARCHAR (200) NULL ,
    foreign_id VARCHAR (200) NULL ,
    postal_code VARCHAR (200) NULL ,
    description VARCHAR (200) NULL ,
    assigned_address VARCHAR (200) NULL ,
    p$$assigned_address__rid VARCHAR (200) NULL ,
    p$$assigned_address__oid VARCHAR (200) NULL ,
    product_id VARCHAR (200) NULL ,
    salutation VARCHAR (200) NULL ,
    street VARCHAR (200) NULL ,
    address_line VARCHAR (200) NULL ,
    address VARCHAR (200) NULL ,
    text VARCHAR (200) NULL ,
    birthdate DATE NULL ,
    member_of_group VARCHAR (200) NULL ,
    birthdate_as_date_time TIMESTAMP (6) WITH TIME ZONE NULL,
    person_group VARCHAR (200) NULL ,
    p$$person_group__rid VARCHAR (200) NULL ,
    p$$person_group__oid VARCHAR (200) NULL ,
    country VARCHAR (200) NULL ,
    sex INTEGER NULL ,
    given_name VARCHAR (200) NULL ,
    product_group_id VARCHAR (200) NULL ,
    place_of_birth VARCHAR (200) NULL ,
    additional_info VARCHAR (200) NULL
  );
  
  DROP TABLE app1_Segment;
  CREATE TABLE app1_Segment (
    object_rid VARCHAR (200) NOT NULL ,
    object_oid VARCHAR (200) NOT NULL ,
    object_idx INTEGER NOT NULL ,
    p$$object_parent__rid VARCHAR (200)  NULL ,
    p$$object_parent__oid VARCHAR (200)  NULL ,
    object__class VARCHAR (200) NULL ,
    description VARCHAR (200) NULL 
  );
  
  DROP TABLE app1_MessageTemplate;
  CREATE TABLE app1_MessageTemplate (
    object_rid VARCHAR (200) NOT NULL ,
    p$$object_rsx VARCHAR (100)  NULL ,
    object_oid VARCHAR (200) NOT NULL ,
    p$$object_oid$0 VARCHAR (100)  NULL ,
    p$$object_oid$1 VARCHAR (100)  NULL ,
    object_idx INTEGER NOT NULL ,
    p$$object_parent__rid VARCHAR (200)  NULL ,
    p$$object_parent__oid VARCHAR (200)  NULL ,
    object__class VARCHAR (200) NULL ,
    p$$unit_of_work__rid VARCHAR (100)  NULL,
    text VARCHAR (200) NULL ,
    description VARCHAR (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 VARCHAR (100) NULL ,
    modified_by_0 VARCHAR (100) NULL
  );
  
  DROP TABLE audit2_UnitOfWork;
  CREATE TABLE audit2_UnitOfWork (
    object_rid VARCHAR (200) NOT NULL ,
    object_oid VARCHAR (200) NOT NULL ,
    p$$object_parent__rid VARCHAR (200)  NULL ,
    p$$object_parent__oid VARCHAR (200)  NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 VARCHAR (200) NULL ,
    object__class VARCHAR (60) NULL ,
    task_id VARCHAR (200) NULL
  );
  
  DROP TABLE audit2_UnitOfWork_;
  CREATE TABLE audit2_UnitOfWork_ (
    object_rid VARCHAR (200) NOT NULL ,
    object_oid VARCHAR (200) NOT NULL ,
    object_idx INTEGER NOT NULL ,
    involved VARCHAR (200) NULL ,
    p$$involved__rid VARCHAR (200) NULL ,
    p$$involved__oid VARCHAR (200) NULL
  );

  DROP TABLE audit2_Involvement;
  CREATE TABLE audit2_Involvement (
    object_rid VARCHAR (200) NOT NULL ,
    object_oid VARCHAR (200) NOT NULL ,
    p$$object_parent__rid VARCHAR (200)  NULL ,
    p$$object_parent__oid VARCHAR (200)  NULL ,
    object__class VARCHAR (60) NULL ,
    before_image VARCHAR (200) NULL,
    p$$before_image__rid VARCHAR (200) NULL ,
    p$$before_image__oid VARCHAR (200) NULL ,
    after_image VARCHAR (200) NULL,
    p$$after_image__rid VARCHAR (200) NULL ,
    p$$after_image__oid VARCHAR (200) NULL
  );

  DROP TABLE audit2_Involvement_;
  CREATE TABLE audit2_Involvement_ (
    object_rid VARCHAR (200) NOT NULL ,
    object_oid VARCHAR (200) NOT NULL ,
    object_idx INTEGER NOT NULL ,
    modified_feature VARCHAR (60) NULL
  );

/*  Test Booking */

  DROP TABLE test_CB_SLICED;
  CREATE TABLE test_CB_SLICED (
    object_rid VARCHAR (200) NOT NULL ,
    object_oid VARCHAR (200) NOT NULL ,
    object_idx INTEGER NOT NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 VARCHAR (200) NULL ,
    modified_by_0 VARCHAR (200) NULL ,
    object__class VARCHAR (60) NULL ,
    cb_type VARCHAR (10) NULL ,
    advice_text VARCHAR (200) NULL ,
    cancels_c_b VARCHAR (512) NULL 
  );

  DROP TABLE test_SLB_SLICED;
  CREATE TABLE test_SLB_SLICED (
    object_rid VARCHAR (200) NOT NULL ,
    object_oid VARCHAR (200) NOT NULL ,
    object_idx INTEGER NOT NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 VARCHAR (200) NULL ,
    modified_by_0 VARCHAR (200) NULL ,
    object__class VARCHAR (60) NULL ,
    slb_type VARCHAR (10) NULL ,
    pos VARCHAR (512) NULL ,
    price DECIMAL(18, 9) NULL ,
    is_debit DECIMAL(18, 9) NULL ,
    is_long DECIMAL(18, 9) NULL ,
    price_currency VARCHAR(10) NULL ,
    value_date VARCHAR(20) NULL ,
    booking_date VARCHAR (20) NULL ,
    quantity DECIMAL(18, 9) NULL ,
    quantity_absolute DECIMAL(18, 9) NULL ,
    visibility VARCHAR (10) NULL ,
    admin_descr VARCHAR (100) NULL ,
    description VARCHAR (200) NULL ,
    cred_value bytea NULL ,
    p$$object_parent__oid VARCHAR (20) NULL ,
    p$$object_parent__rid INTEGER NULL ,
    p$$pos__oid VARCHAR (20) NULL ,
    p$$pos__rid INTEGER NULL ,
    p$$pos_parent__oid VARCHAR (50) NULL ,
    p$$pos_parent__rid INTEGER NULL 
  );

/*  Test Extension */

  DROP TABLE EXTENSION_DEFAULT;
  CREATE TABLE EXTENSION_DEFAULT (	
    OBJECT_RID varchar(200) NOT NULL, 
	  OBJECT_OID varchar(200) NOT NULL, 
	  OBJECT_IDX integer NOT NULL, 
      P$$OBJECT_parent__oid varchar(200), 
	  P$$OBJECT_parent__rid varchar(200) ,
	  OBJECT__VALID_FROM CHAR(20), 
	  OBJECT__VALID_TO CHAR(20), 
	  OBJECT__INVALIDATED_AT CHAR(20), 
	  OBJECT__CLASS varchar(100), 
	  OBJECT__STATE_NUMBER decimal(10,0), 
	  OBJECT__STATE_ID varchar(100), 
	  SEGMENT varchar(200), 
	  P$$SEGMENT__rid VARCHAR(200), 
	  P$$SEGMENT__oid VARCHAR(200), 
	  VALUE1 CHAR(9), 
	  VALUE2 decimal, 
	  VALUE3 decimal, 
	  VALUE4 decimal, 
	  VALUE5 decimal, 
	  VALUE9 varchar(200), 
	  VALUE10 bytea, 
	  VALUE11A varchar(20), 
	  VALUE11B varchar(20), 
	  VALUE6 varchar(200), 
	  VALUE7 CHAR(20), 
	  VALUE8 varchar(8)
  ) ;

  DROP TABLE EXTENSION_NATIVE;
  CREATE TABLE EXTENSION_NATIVE (
    OBJECT_RID varchar(200) NOT NULL, 
 	  OBJECT_OID varchar(200) NOT NULL, 
	  OBJECT_IDX integer NOT NULL, 
	  P$$OBJECT_parent__rid varchar(200), 
	  P$$OBJECT_parent__oid varchar(200),
	  OBJECT__VALID_FROM TIMESTAMP (3) WITH TIME ZONE, 
	  OBJECT__VALID_TO TIMESTAMP (3) WITH TIME ZONE, 
	  OBJECT__INVALIDATED_AT TIMESTAMP (3) WITH TIME ZONE, 
	  OBJECT__CLASS varchar(100), 
	  OBJECT__STATE_NUMBER decimal(10,0), 
	  OBJECT__STATE_ID varchar(100), 
	  STATE_VALID_FROM DATE, 
	  STATE_VALID_TO DATE, 
	  SEGMENT varchar(200), 
	  P$$SEGMENT__rid VARCHAR(200), 
	  P$$SEGMENT__oid VARCHAR(200), 
	  VALUE1 boolean, 
	  VALUE2 decimal,
	  VALUE3 decimal, 
	  VALUE4 decimal,
	  VALUE5 decimal, 
	  VALUE9 varchar(200), 
	  VALUE10 bytea, 
	  VALUE11A varchar(20), 
	  VALUE11B varchar(20), 
	  VALUE6 varchar(200),
	  VALUE7 TIMESTAMP (3) WITH TIME ZONE, 
	  VALUE8 DATE
  ) ;

  DROP TABLE EXTENSION_NUMERIC;
  CREATE TABLE EXTENSION_NUMERIC (	
    OBJECT_RID varchar(200) NOT NULL, 
	  OBJECT_OID varchar(200) NOT NULL, 
	  OBJECT_IDX integer NOT NULL, 
	  P$$OBJECT_parent__rid varchar(200), 
	  P$$OBJECT_parent__oid varchar(200),
	  OBJECT__VALID_FROM decimal(19,3), 
	  OBJECT__VALID_TO decimal(19,3), 
	  OBJECT__INVALIDATED_AT decimal(19,3), 
	  OBJECT__CLASS varchar(100), 
	  OBJECT__STATE_NUMBER decimal(10,0), 
	  OBJECT__STATE_ID varchar(100), 
	  SEGMENT varchar(200), 
	  P$$SEGMENT__rid VARCHAR(200), 
	  P$$SEGMENT__oid VARCHAR(200), 
	  VALUE1 decimal(1,0), 
	  VALUE2 decimal, 
	  VALUE3 decimal,
	  VALUE4 decimal, 
	  VALUE5 decimal, 
	  VALUE9 varchar(200), 
	  VALUE10 bytea, 
	  VALUE11A integer,
	  VALUE11B decimal(12,3),
	  VALUE6 varchar(200),
	  VALUE7 decimal(19,3),
	  VALUE8 varchar(8)
  ) ;

  DROP TABLE EXTENSION_SEGMENT ;
  CREATE TABLE EXTENSION_SEGMENT (
    OBJECT_RID VARCHAR(200) NOT NULL,
	  OBJECT_OID VARCHAR(200) NOT NULL,
	  OBJECT_IDX INTEGER NOT NULL,
	  OBJECT__CLASS VARCHAR(200),
	  DESCRIPTION VARCHAR(200)
  ) ;


/*  Test org::openmdx::base::Aspect */

  DROP TABLE aspect_Segment;
  CREATE TABLE aspect_Segment (
    object_id VARCHAR (100) NOT NULL ,
    object__class VARCHAR (100) NULL ,
    description VARCHAR (200) NULL
  );
 
  DROP TABLE aspect_Object;
  CREATE TABLE aspect_Object (
    object_id VARCHAR (100) NOT NULL ,
    object__class VARCHAR (100) NULL ,
    core VARCHAR (100) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL ,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL ,
    created_by_0 VARCHAR (200) NULL ,
    modified_by_0 VARCHAR (200) NULL ,
    string VARCHAR (200) NULL ,
    prime_ DECIMAL(10, 0) NULL ,
    url VARCHAR (200) NULL
  );

  DROP TABLE aspect_Object_;
  CREATE TABLE aspect_Object_ (
    object_id VARCHAR (100) NOT NULL ,
    object_idx INTEGER NOT NULL ,
    object__class VARCHAR (100) NULL ,
    prime DECIMAL(10, 0) NULL
  );


/*  org::openmdx::state2 */

  DROP TABLE datestate_NATIVE;
  CREATE TABLE datestate_NATIVE (
	OBJECT_ID varchar(200) NOT NULL, 
  	OBJECT__CLASS varchar(200) NOT NULL,
    created_at TIMESTAMP (3) WITH TIME ZONE NULL,
    removed_at TIMESTAMP (3) WITH TIME ZONE NULL,
	created_by_0 varchar(50) NULL,
	removed_by_0 varchar(50) NULL,
    openmdxjdo_version decimal(5, 0) NULL,
    core varchar(200) NULL,
    state_valid_from DATE NULL,
    state_valid_to DATE NULL,
    description varchar(100) NULL,
    string_value varchar(100) NULL,
    uri_value varchar (200) NULL,
    boolean_value boolean NULL,
    date_value DATE NULL,
    date_time_value TIMESTAMP (3) WITH TIME ZONE NULL,
    decimal_value decimal(18, 6) NULL,
    duration_value varchar (10) NULL,
    p$$duration_value_yeartomonth varchar(10) NULL,
    p$$duration_value_daytosecond varchar(10) NULL, 
    integer_value decimal(10, 0) NULL,
    long_value decimal(20, 0) NULL,
    short_value decimal(5, 0) NULL,
    a_reference varchar (200) NULL,
    b_reference varchar (200) NULL,
    CONSTRAINT PK_datestate_NATIVE PRIMARY KEY (OBJECT_ID)
  ) ;

  DROP TABLE datestate_NATIVE_;
  CREATE TABLE datestate_NATIVE_ (
	OBJECT_ID varchar(200) NOT NULL, 
	OBJECT_IDX decimal(5,0) NOT NULL,
	string_list varchar(100) NULL,
	long_array decimal(20, 0) NULL,
	c_reference varchar (200) NULL,
    CONSTRAINT PK_datestate_NATIVE_ PRIMARY KEY (OBJECT_ID,OBJECT_IDX)
  ) ;

  DROP TABLE state2_NATIVE ;
  CREATE TABLE state2_NATIVE (
	object_id varchar (100) NOT NULL ,
	object__class varchar (100) NULL ,
	description varchar (60) NULL ,
	core varchar (256) NULL ,
	created_at TIMESTAMP (3) WITH TIME ZONE NULL ,
	removed_at TIMESTAMP (3) WITH TIME ZONE NULL ,
	modified_at TIMESTAMP (3) WITH TIME ZONE NULL ,
	created_by_0 varchar (60) NULL ,
	removed_by_0 varchar (60) NULL ,
	modified_by_0 varchar (60) NULL ,
	state_valid_from DATE,
	state_valid_to DATE,
	state_a varchar (256) NULL ,
	state_n varchar (256) NULL ,
	string_value varchar (60) NULL ,
	uri_value varchar (200) NULL ,
	boolean_value boolean NULL ,
	date_value DATE NULL ,
	date_time_value TIMESTAMP (3) WITH TIME ZONE NULL ,
	decimal_value decimal(18, 6) NULL ,
	duration_value varchar (10) NULL ,
	p$$duration_value_yeartomonth varchar(10) NULL ,
	p$$duration_value_daytosecond varchar(10) NULL ,
	integer_value decimal(10, 0) NULL ,
	long_value INTEGER NULL ,
	short_value decimal(10, 0) NULL
  );

  DROP TABLE state2_NATIVE_;
  CREATE TABLE state2_NATIVE_ (
	object_id varchar (100) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	string_list varchar (60) NULL ,
	long_array INTEGER NULL ,
	state_c varchar (256) NULL
  );


/*  Test org::openmdx::generic1 */

  DROP TABLE generic1_Property ;
  CREATE TABLE generic1_Property (
    object_rid VARCHAR(100) NOT NULL,
    object_oid VARCHAR(200) NOT NULL,
    p$$object_parent__rid VARCHAR(100) NULL, 
    p$$object_parent__oid VARCHAR(200) NULL,
    object__class VARCHAR(200) NULL ,
    description VARCHAR(200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
	modified_by_0 varchar(50) NULL ,
	created_by_0 varchar(50) NULL
  );

  DROP TABLE generic1_Property_N ;
 CREATE TABLE generic1_Property_N (
	object_rid varchar(100) NOT NULL,
	object_oid varchar(200) NOT NULL,
	object_idx INTEGER NOT NULL ,
	boolean_value varchar(10) NULL ,
	uri_value varchar(200) NULL ,
	decimal_value decimal(18, 6) NULL ,
	string_value varchar(200) NULL ,
	integer_value decimal(10, 0) NULL 
);
