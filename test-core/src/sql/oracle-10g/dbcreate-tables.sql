REM Test test::openmdx::app1

  DROP TABLE app1_Address;
  CREATE TABLE app1_Address (
    object_rid VARCHAR2 (100) NULL ,
    p$$object_rsx VARCHAR2 (100)  NULL ,
    object_oid VARCHAR2 (100) NULL ,
    p$$object_oid$0 VARCHAR2 (100)  NULL ,
    p$$object_oid$1 VARCHAR2 (100)  NULL ,
    p$$object_parent__rid VARCHAR2 (100)  NULL ,
    p$$object_parent__oid VARCHAR2 (100)  NULL ,
    object__class VARCHAR2 (200) NULL ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    description VARCHAR2 (200) NULL ,
    created_at TIMESTAMP (3) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (3) WITH TIME ZONE NULL,
    postal_code VARCHAR2 (100) NULL ,
    street VARCHAR2 (100) NULL ,
    country VARCHAR2 (100) NULL ,
    city VARCHAR2 (100) NULL ,
    house_number VARCHAR2 (100) NULL ,
    address VARCHAR2 (100) NULL
  );
  
  DROP TABLE app1_Address_N;
  CREATE TABLE app1_Address_N (
    object_rid VARCHAR2 (100) NULL ,
    p$$object_rsx VARCHAR2 (100) NULL ,
    object_oid VARCHAR2 (100) NULL ,
    p$$object_oid$0 VARCHAR2 (100)  NULL ,
    p$$object_oid$1 VARCHAR2 (100)  NULL ,
    object_idx INTEGER NOT NULL ,
    p$$object_parent__rid VARCHAR2 (100)  NULL ,
    p$$object_parent__oid VARCHAR2 (100)  NULL ,
    object__class VARCHAR2 (200) NULL ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    modified_by VARCHAR2 (50) NULL ,
    created_by VARCHAR2 (50) NULL ,
    address_line VARCHAR2 (100) NULL
  );
  
  DROP TABLE app1_DOC;
  CREATE TABLE app1_DOC (
    object_rid VARCHAR2 (100) NULL ,
    p$$object_rsx VARCHAR2 (100)  NULL ,
    object_oid VARCHAR2 (100) NULL ,
    p$$object_oid$0 VARCHAR2 (100)  NULL ,
    p$$object_oid$1 VARCHAR2 (100)  NULL ,
    object_idx INTEGER NOT NULL ,
    p$$object_parent__rid VARCHAR2 (100)  NULL ,
    p$$object_parent__oid VARCHAR2 (100)  NULL ,
    object__class VARCHAR2 (200) NULL ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by VARCHAR2 (200) NULL ,
    modified_by VARCHAR2 (200) NULL ,
    description VARCHAR2 (100) NULL ,
    content BLOB NULL ,
    keyword VARCHAR2 (32) NULL
  );
  
  DROP TABLE app1_Invoice;
  CREATE TABLE app1_Invoice (
    object_rid VARCHAR2 (100) NULL ,
    p$$object_rsx VARCHAR2 (100)  NULL ,
    object_oid VARCHAR2 (100) NULL ,
    p$$object_oid$0 VARCHAR2 (100)  NULL ,
    p$$object_oid$1 VARCHAR2 (100)  NULL ,
    object_idx INTEGER NOT NULL ,
    p$$object_parent__rid VARCHAR2 (100)  NULL ,
    p$$object_parent__oid VARCHAR2 (100)  NULL ,
    object__class VARCHAR2 (200) NULL ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    description VARCHAR2 (200) NULL ,
    product_group_id VARCHAR2 (100) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by VARCHAR2 (100) NULL ,
    modified_by VARCHAR2 (100) NULL
  );
  
  DROP TABLE app1_InvoicePosition;
  CREATE TABLE app1_InvoicePosition (
    object_rid VARCHAR2 (100) NULL ,
    p$$object_rsx VARCHAR2 (100)  NULL ,
    object_oid VARCHAR2 (100) NULL ,
    p$$object_oid$0 VARCHAR2 (100)  NULL ,
    p$$object_oid$1 VARCHAR2 (100)  NULL ,
    object_idx INTEGER NOT NULL ,
    p$$object_parent__rid VARCHAR2 (100)  NULL ,
    p$$object_parent__oid VARCHAR2 (100)  NULL ,
    object__class VARCHAR2 (200) NULL ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    description VARCHAR2 (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by VARCHAR2 (100) NULL ,
    modified_by VARCHAR2 (100) NULL ,
    product_id VARCHAR2 (50) NULL
  );
  
  DROP TABLE app1_Member;
  CREATE TABLE app1_Member (
    object_rid VARCHAR2 (100) NULL ,
    p$$object_rsx VARCHAR2 (100)  NULL ,
    object_oid VARCHAR2 (100) NULL ,
    p$$object_oid$0 VARCHAR2 (100)  NULL ,
    p$$object_oid$1 VARCHAR2 (100)  NULL ,
    object_idx INTEGER NOT NULL ,
    p$$object_parent__rid VARCHAR2 (100)  NULL ,
    p$$object_parent__oid VARCHAR2 (100)  NULL ,
    object__class VARCHAR2 (200) NULL ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    description VARCHAR2 (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_by VARCHAR2 (50) NULL ,
    created_by VARCHAR2 (50) NULL ,
    m1 VARCHAR2 (200) NULL ,
    p$$m1__rid VARCHAR2 (200) NULL ,
    p$$m1__oid VARCHAR2 (200) NULL ,
    m2 VARCHAR2 (200) NULL ,
    p$$m2__rid VARCHAR2 (200) NULL ,
    p$$m2__oid VARCHAR2 (200) NULL
  );
  
  DROP TABLE app1_PersonGroup;
  CREATE TABLE app1_PersonGroup (
    object_rid VARCHAR2 (100) NULL ,
    p$$object_rsx VARCHAR2 (100)  NULL ,
    object_oid VARCHAR2 (100) NULL ,
    p$$object_oid$0 VARCHAR2 (100)  NULL ,
    p$$object_oid$1 VARCHAR2 (100)  NULL ,
    object_idx INTEGER NULL ,
    p$$object_parent__rid VARCHAR2 (100)  NULL ,
    p$$object_parent__oid VARCHAR2 (100)  NULL ,
    object__class VARCHAR2 (200) NULL ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    description VARCHAR2 (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_by VARCHAR2 (50) NULL ,
    created_by VARCHAR2 (50) NULL ,
    name VARCHAR2 (100) NULL
  );
  
  DROP TABLE app1_SLICED;
  CREATE TABLE app1_SLICED (
    object_rid VARCHAR2 (100) NULL ,
    p$$object_rsx VARCHAR2 (100)  NULL ,
    object_oid VARCHAR2 (100) NULL ,
    p$$object_oid$0 VARCHAR2 (100)  NULL ,
    p$$object_oid$1 VARCHAR2 (100)  NULL ,
    object_idx INTEGER NOT NULL ,
    p$$object_parent__rid VARCHAR2 (100)  NULL ,
    p$$object_parent__oid VARCHAR2 (100)  NULL ,
    object__class VARCHAR2 (200) NULL ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by VARCHAR2 (200) NULL ,
    modified_by VARCHAR2 (200) NULL ,
    m1 VARCHAR2 (200) NULL ,
    last_name VARCHAR2 (200) NULL ,
    house_number VARCHAR2 (200) NULL ,
    city VARCHAR2 (200) NULL ,
    foreign_id VARCHAR2 (200) NULL ,
    postal_code VARCHAR2 (200) NULL ,
    description VARCHAR2 (200) NULL ,
    assigned_address VARCHAR2 (200) NULL ,
    p$$assigned_address__rid VARCHAR2 (200) NULL ,
    p$$assigned_address__oid VARCHAR2 (200) NULL ,
    product_id VARCHAR2 (200) NULL ,
    salutation VARCHAR2 (200) NULL ,
    street VARCHAR2 (200) NULL ,
    address_line VARCHAR2 (200) NULL ,
    address VARCHAR2 (200) NULL ,
    text VARCHAR2 (200) NULL ,
    birthdate DATE NULL ,
    member_of_group VARCHAR2 (200) NULL ,
    birthdate_as_date_time TIMESTAMP (6) WITH TIME ZONE NULL,
    person_group VARCHAR2 (200) NULL ,
    p$$person_group__rid VARCHAR2 (200) NULL ,
    p$$person_group__oid VARCHAR2 (200) NULL ,
    country VARCHAR2 (200) NULL ,
    sex INTEGER NULL ,
    given_name VARCHAR2 (200) NULL ,
    product_group_id VARCHAR2 (200) NULL ,
    place_of_birth VARCHAR2 (200) NULL ,
    additional_info VARCHAR2 (200) NULL
  );
  
  DROP TABLE app1_Segment;
  CREATE TABLE app1_Segment (
    object_rid VARCHAR2 (200) NOT NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    object_idx INTEGER NOT NULL ,
    p$$object_parent__rid VARCHAR2 (200)  NULL ,
    p$$object_parent__oid VARCHAR2 (200)  NULL ,
    object__class VARCHAR2 (200) NULL ,
    description VARCHAR2 (200) NULL 
  );
  
  DROP TABLE app1_MessageTemplate;
  CREATE TABLE app1_MessageTemplate (
    object_rid VARCHAR2 (200) NOT NULL ,
    p$$object_rsx VARCHAR2 (100)  NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    p$$object_oid$0 VARCHAR2 (100)  NULL ,
    p$$object_oid$1 VARCHAR2 (100)  NULL ,
    object_idx INTEGER NOT NULL ,
    p$$object_parent__rid VARCHAR2 (200)  NULL ,
    p$$object_parent__oid VARCHAR2 (200)  NULL ,
    object__class VARCHAR2 (200) NULL ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    text VARCHAR2 (200) NULL ,
    description VARCHAR2 (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by VARCHAR2 (100) NULL ,
    modified_by VARCHAR2 (100) NULL
  );
  
  DROP TABLE audit2_UnitOfWork;
  CREATE TABLE audit2_UnitOfWork (
    object_rid VARCHAR2 (200) NOT NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    p$$object_parent__rid VARCHAR2 (200)  NULL ,
    p$$object_parent__oid VARCHAR2 (200)  NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    object__class VARCHAR2 (60) NULL ,
    task_id VARCHAR2 (200) NULL
  );
  
  DROP TABLE audit2_UnitOfWork_;
  CREATE TABLE audit2_UnitOfWork_ (
    object_rid VARCHAR2 (200) NOT NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    object_idx INTEGER NOT NULL ,
    created_by VARCHAR2 (200) NULL ,
    involved VARCHAR2 (200) NULL ,
    p$$involved__rid VARCHAR2 (200) NULL ,
    p$$involved__oid VARCHAR2 (200) NULL
  );

  DROP TABLE audit2_Involvement;
  CREATE TABLE audit2_Involvement (
    object_rid VARCHAR2 (200) NOT NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    p$$object_parent__rid VARCHAR2 (200)  NULL ,
    p$$object_parent__oid VARCHAR2 (200)  NULL ,
    object__class VARCHAR2 (60) NULL ,
    before_image VARCHAR2 (200) NULL,
    p$$before_image__rid VARCHAR2 (200) NULL ,
    p$$before_image__oid VARCHAR2 (200) NULL ,
    after_image VARCHAR2 (200) NULL,
    p$$after_image__rid VARCHAR2 (200) NULL ,
    p$$after_image__oid VARCHAR2 (200) NULL
  );

  DROP TABLE audit2_Involvement_;
  CREATE TABLE audit2_Involvement_ (
    object_rid VARCHAR2 (200) NOT NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    object_idx INTEGER NOT NULL ,
    modified_feature VARCHAR2 (60) NULL
  );

REM Test Booking

  DROP TABLE test_CB_SLICED;
  CREATE TABLE test_CB_SLICED (
    object_rid VARCHAR2 (200) NOT NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    object_idx INTEGER NOT NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by VARCHAR2 (200) NULL ,
    modified_by VARCHAR2 (200) NULL ,
    object__class VARCHAR2 (60) NULL ,
    cb_type VARCHAR2 (10) NULL ,
    advice_text VARCHAR2 (200) NULL ,
    cancels_c_b VARCHAR2 (512) NULL 
  );

  DROP TABLE test_SLB_SLICED;
  CREATE TABLE test_SLB_SLICED (
    object_rid VARCHAR2 (200) NOT NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    object_idx INTEGER NOT NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by VARCHAR2 (200) NULL ,
    modified_by VARCHAR2 (200) NULL ,
    object__class VARCHAR2 (60) NULL ,
    slb_type VARCHAR2 (10) NULL ,
    pos VARCHAR2 (512) NULL ,
    price NUMBER(18, 9) NULL ,
    is_debit NUMBER(18, 9) NULL ,
    is_long NUMBER(18, 9) NULL ,
    price_currency VARCHAR2(10) NULL ,
    value_date VARCHAR2(20) NULL ,
    booking_date VARCHAR2 (20) NULL ,
    quantity NUMBER(18, 9) NULL ,
    quantity_absolute NUMBER(18, 9) NULL ,
    visibility VARCHAR2 (10) NULL ,
    admin_descr VARCHAR2 (100) NULL ,
    description VARCHAR2 (200) NULL ,
    cred_value BLOB NULL ,
    p$$object_parent__oid VARCHAR2 (20) NULL ,
    p$$object_parent__rid INTEGER NULL ,
    p$$pos__oid VARCHAR2 (20) NULL ,
    p$$pos__rid INTEGER NULL ,
    p$$pos_parent__oid VARCHAR2 (50) NULL ,
    p$$pos_parent__rid INTEGER NULL 
  );

REM Test Extension

  DROP TABLE EXTENSION_DEFAULT ;
  CREATE TABLE EXTENSION_DEFAULT (	
    OBJECT_RID VARCHAR2(200) NOT NULL ENABLE, 
	  OBJECT_OID VARCHAR2(200) NOT NULL ENABLE, 
	  OBJECT_IDX NUMBER(*,0) NOT NULL ENABLE, 
    P$$OBJECT_parent__oid VARCHAR2(200), 
	  P$$OBJECT_parent__rid VARCHAR2(200) ,
	  OBJECT__VALID_FROM CHAR(20), 
	  OBJECT__VALID_TO CHAR(20), 
	  OBJECT__INVALIDATED_AT CHAR(20), 
	  OBJECT__CLASS VARCHAR2(100), 
	  OBJECT__STATE_NUMBER NUMBER(10,0), 
	  OBJECT__STATE_ID VARCHAR2(100), 
	  SEGMENT VARCHAR2(200), 
	  VALUE1 CHAR(9), 
	  VALUE2 NUMBER, 
	  VALUE3 NUMBER, 
	  VALUE4 NUMBER, 
	  VALUE5 NUMBER, 
	  VALUE9 VARCHAR2(200), 
	  VALUE10 RAW(2000), 
	  VALUE11A VARCHAR2(20), 
	  VALUE11B VARCHAR2(20), 
	  P$$SEGMENT_parent__rid VARCHAR2(200), 
	  P$$SEGMENT_parent__oid VARCHAR2(200), 
	  VALUE6 VARCHAR2(200), 
	  VALUE7 CHAR(20), 
	  VALUE8 VARCHAR2(8)
  ) ;

  DROP TABLE EXTENSION_NATIVE ;
  CREATE TABLE EXTENSION_NATIVE (
    OBJECT_RID VARCHAR2(200) NOT NULL ENABLE, 
 	  OBJECT_OID VARCHAR2(200) NOT NULL ENABLE, 
	  OBJECT_IDX NUMBER(*,0) NOT NULL ENABLE, 
	  P$$OBJECT_parent__rid VARCHAR2(200), 
	  P$$OBJECT_parent__oid VARCHAR2(200),
	  OBJECT__VALID_FROM TIMESTAMP (6) WITH TIME ZONE, 
	  OBJECT__VALID_TO TIMESTAMP (6) WITH TIME ZONE, 
	  OBJECT__INVALIDATED_AT TIMESTAMP (6) WITH TIME ZONE, 
	  OBJECT__CLASS VARCHAR2(100), 
	  OBJECT__STATE_NUMBER NUMBER(10,0), 
	  OBJECT__STATE_ID VARCHAR2(100), 
	  STATE_VALID_FROM DATE, 
	  STATE_VALID_TO DATE, 
	  SEGMENT VARCHAR2(200), 
	  P$$SEGMENT_parent__rid VARCHAR2(200), 
	  P$$SEGMENT_parent__oid VARCHAR2(200),
	  VALUE1 CHAR(1 CHAR), 
	  VALUE2 NUMBER,
	  VALUE3 NUMBER, 
	  VALUE4 NUMBER,
	  VALUE5 NUMBER, 
	  VALUE9 VARCHAR2(200), 
	  VALUE10 RAW(2000), 
	  VALUE11A INTERVAL YEAR (9) TO MONTH, 
	  VALUE11B INTERVAL DAY (9) TO SECOND (3), 
	  VALUE6 VARCHAR2(200),
	  VALUE7 TIMESTAMP (6) WITH TIME ZONE, 
	  VALUE8 DATE
  ) ;

  DROP TABLE EXTENSION_NUMERIC ;
  CREATE TABLE EXTENSION_NUMERIC (	
    OBJECT_RID VARCHAR2(200) NOT NULL ENABLE, 
	  OBJECT_OID VARCHAR2(200) NOT NULL ENABLE, 
	  OBJECT_IDX NUMBER(*,0) NOT NULL ENABLE, 
	  P$$OBJECT_parent__rid VARCHAR2(200), 
	  P$$OBJECT_parent__oid VARCHAR2(200),
	  OBJECT__VALID_FROM NUMBER(19,3), 
	  OBJECT__VALID_TO NUMBER(19,3), 
	  OBJECT__INVALIDATED_AT NUMBER(19,3), 
	  OBJECT__CLASS VARCHAR2(100), 
	  OBJECT__STATE_NUMBER NUMBER(10,0), 
	  OBJECT__STATE_ID VARCHAR2(100), 
	  SEGMENT VARCHAR2(200), 
	  VALUE1 NUMBER(1,0), 
	  VALUE2 NUMBER, 
	  VALUE3 NUMBER,
	  VALUE4 NUMBER, 
	  VALUE5 NUMBER, 
	  VALUE9 VARCHAR2(200), 
	  VALUE10 RAW(2000), 
	  VALUE11A NUMBER(9,0),
	  VALUE11B NUMBER(12,3),
	  P$$SEGMENT_parent__rid VARCHAR2(200),
	  P$$SEGMENT_parent__oid VARCHAR2(200),
	  VALUE6 VARCHAR2(200),
	  VALUE7 NUMBER(19,3),
	  VALUE8 VARCHAR2(8)
  ) ;

  DROP TABLE EXTENSION_SEGMENT ;
  CREATE TABLE EXTENSION_SEGMENT (
    OBJECT_RID VARCHAR2(200) NOT NULL ENABLE,
	  OBJECT_OID VARCHAR2(200) NOT NULL ENABLE,
	  OBJECT_IDX NUMBER(*,0) NOT NULL ENABLE,
	  OBJECT__CLASS VARCHAR2(200),
	  DESCRIPTION VARCHAR2(200)
  ) ;


REM Test org::openmdx::base::Aspect

  DROP TABLE aspect_Segment;
  CREATE TABLE aspect_Segment (
    object_id VARCHAR2 (100) NOT NULL ,
    object__class VARCHAR2 (100) NULL ,
    description VARCHAR2 (200) NULL
  );
 
  DROP TABLE aspect_Object;
  CREATE TABLE aspect_Object (
    object_id VARCHAR2 (100) NOT NULL ,
    object__class VARCHAR2 (100) NULL ,
    core VARCHAR2 (100) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL ,
    created_by_ NUMBER(10, 0) NULL ,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL ,
    modified_by_ NUMBER(10, 0) NULL ,
    string VARCHAR2 (200) NULL ,
    prime_ NUMBER(10, 0) NULL ,
    url VARCHAR2 (200) NULL
  );

  DROP TABLE aspect_Object_;
  CREATE TABLE aspect_Object_ (
    object_id VARCHAR2 (100) NOT NULL ,
    object_idx INTEGER NOT NULL ,
    object__class VARCHAR2 (100) NULL ,
    created_by VARCHAR2 (200) NULL ,
    modified_by VARCHAR2 (200) NULL ,
    prime NUMBER(10, 0) NULL
  );


REM org::openmdx::state2

  DROP TABLE datestate_NATIVE ;
  CREATE TABLE datestate_NATIVE (
	OBJECT_ID VARCHAR2(200) NOT NULL, 
  	OBJECT__CLASS VARCHAR2(200) NOT NULL,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    removed_at TIMESTAMP (6) WITH TIME ZONE NULL,
    openmdxjdo_version NUMBER(5, 0) NULL,
    core VARCHAR2(200) NULL,
    state_valid_from DATE NULL,
    state_valid_to DATE NULL,
    description VARCHAR2(100) NULL,
    string_value VARCHAR2(100) NULL,
    uri_value VARCHAR2 (200) NULL,
    boolean_value CHAR(1 CHAR) NULL,
    date_value DATE NULL,
    date_time_value TIMESTAMP (6) WITH TIME ZONE NULL,
    decimal_value NUMBER(18, 6) NULL,
    duration_value VARCHAR2 (10) NULL,
    p$$duration_value_yeartomonth INTERVAL YEAR(9) TO MONTH NULL,
    p$$duration_value_daytosecond INTERVAL DAY (9) TO SECOND(3) NULL, 
    integer_value NUMBER(10, 0) NULL,
    long_value NUMBER(20, 0) NULL,
    short_value NUMBER(5, 0) NULL,
    a_reference VARCHAR2 (200) NULL,
    b_reference VARCHAR2 (200) NULL,
    CONSTRAINT PK_datestate_NATIVE PRIMARY KEY (OBJECT_ID)
  );

  DROP TABLE datestate_NATIVE_ ;
  CREATE TABLE datestate_NATIVE_ (
    OBJECT_ID VARCHAR2(200) NOT NULL, 
    OBJECT_IDX NUMBER(5,0) NOT NULL,
    OBJECT__CLASS VARCHAR2(200) NOT NULL,
    created_by VARCHAR2(50) NULL,
    removed_at VARCHAR2(50) NULL,
    string_list VARCHAR2(100) NULL,
    long_array NUMBER(20, 0) NULL,
    c_reference VARCHAR2 (200) NULL,
    CONSTRAINT PK_datestate_NATIVE_ PRIMARY KEY (OBJECT_ID,OBJECT_IDX)
  );

  DROP TABLE state2_NATIVE ;
  CREATE TABLE state2_NATIVE (
    object_id VARCHAR2 (100) NOT NULL ,
    object__class VARCHAR2 (100) NULL ,
    description VARCHAR2 (60) NULL ,
    core VARCHAR2 (256) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL ,
    removed_at TIMESTAMP (6) WITH TIME ZONE NULL ,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL ,
    state_valid_from DATE,
    state_valid_to DATE,
    state_a VARCHAR2 (256) NULL ,
    state_n VARCHAR2 (256) NULL ,
    string_value VARCHAR2 (60) NULL ,
    uri_value VARCHAR2 (200) NULL ,
    boolean_value CHAR(1 CHAR) NULL ,
    date_value DATE NULL ,
    date_time_value TIMESTAMP (6) WITH TIME ZONE NULL ,
    decimal_value NUMBER(18, 6) NULL ,
    duration_value VARCHAR2 (10) NULL ,
    p$$duration_value_yeartomonth INTERVAL YEAR(9) TO MONTH NULL ,
    p$$duration_value_daytosecond INTERVAL DAY (9) TO SECOND(3) NULL ,
    integer_value NUMBER(10, 0) NULL ,
    long_value INTEGER NULL ,
    short_value NUMBER(10, 0) NULL
  );

  DROP TABLE state2_NATIVE_ ;
  CREATE TABLE state2_NATIVE_ (
    object_id VARCHAR2 (100) NOT NULL ,
    object_idx INTEGER NOT NULL ,
    object__class VARCHAR2 (100) NULL ,
    created_by VARCHAR2 (60) NULL ,
    removed_by VARCHAR2 (60) NULL ,
    modified_by VARCHAR2 (60) NULL ,
    string_list VARCHAR2 (60) NULL ,
    long_array INTEGER NULL ,
    state_c VARCHAR2 (256) NULL
  );


REM Test org::openmdx::generic1

  DROP TABLE generic1_Property ;
  CREATE TABLE generic1_Property (
    object_rid VARCHAR2(100) NOT NULL,
    object_oid VARCHAR2(200) NOT NULL,
    p$$object_parent__rid VARCHAR2(100) NULL, 
    p$$object_parent__oid VARCHAR2(200) NULL,
    object__class VARCHAR2(200) NULL ,
    description VARCHAR2(200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL
  );

  DROP TABLE generic1_Property_N ;
  CREATE TABLE generic1_Property_N (
    object_rid VARCHAR2(100) NOT NULL,
    object_oid VARCHAR2(200) NOT NULL,
    object_idx INTEGER NOT NULL ,
    modified_by VARCHAR2(50) NULL ,
    created_by VARCHAR2(50) NULL,
    boolean_value CHAR(1 CHAR) NULL ,
    uri_value VARCHAR2(200) NULL ,
    decimal_value NUMBER(18, 6) NULL ,
    string_value VARCHAR2(200) NULL ,
    integer_value NUMBER(10, 0) NULL 
  );
