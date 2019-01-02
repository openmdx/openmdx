-- Test test::openmdx::preferences2

  DROP TABLE PREFERENCES2_SEGMENT ;
  CREATE TABLE PREFERENCES2_SEGMENT (
    OBJECT_RID VARCHAR2(200) NOT NULL ENABLE,
	OBJECT_OID VARCHAR2(200) NOT NULL ENABLE,
	OBJECT__CLASS VARCHAR2(200) NOT NULL ENABLE,
	DESCRIPTION VARCHAR2(200),
    CONSTRAINT PK_PREFERENCES2_SEGMENT PRIMARY KEY (OBJECT_RID, OBJECT_OID)
  );
  
  DROP TABLE PREFERENCES2_ROOT;
  CREATE TABLE PREFERENCES2_ROOT(
    object_rid VARCHAR2(100) NOT NULL ENABLE,
    object_oid VARCHAR2(200) NOT NULL ENABLE,
    p$$object_parent__rid VARCHAR2(100) NOT NULL ENABLE, 
    p$$object_parent__oid VARCHAR2(200) NOT NULL ENABLE,
    object__class VARCHAR2(200) NOT NULL ENABLE ,
    type VARCHAR2(10) NOT NULL ENABLE,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_by_0 VARCHAR2(50) NULL ,
    created_by_0 VARCHAR2(50) NULL,
    CONSTRAINT PK_PREFERENCES2_ROOT PRIMARY KEY (OBJECT_RID, OBJECT_OID)
  );

  DROP TABLE PREFERENCES2_NODE;
  CREATE TABLE PREFERENCES2_NODE(
    object_rid VARCHAR2(100) NOT NULL ENABLE,
    object_oid VARCHAR2(200) NOT NULL ENABLE,
    p$$object_parent__rid VARCHAR2(100) NOT NULL ENABLE, 
    p$$object_parent__oid VARCHAR2(200) NOT NULL ENABLE,
    object__class VARCHAR2(200) NOT NULL ENABLE ,
    parent VARCHAR2 (200) NULL ,
    p$$parent__rid VARCHAR2 (200) NULL ,
    p$$parent__oid VARCHAR2 (200) NULL ,
    name VARCHAR2(100) NULL,
    absolute_path VARCHAR2(1000) NOT NULL ENABLE,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_by_0 VARCHAR2(50) NULL ,
    created_by_0 VARCHAR2(50) NULL,
    CONSTRAINT PK_PREFERENCES2_NODE PRIMARY KEY (OBJECT_RID, OBJECT_OID)
  );

  DROP TABLE PREFERENCES2_ENTRY;
  CREATE TABLE PREFERENCES2_ENTRY(
    object_rid VARCHAR2(100) NOT NULL ENABLE,
    object_oid VARCHAR2(200) NOT NULL ENABLE,
    p$$object_parent__rid VARCHAR2(100) NOT NULL ENABLE, 
    p$$object_parent__oid VARCHAR2(200) NOT NULL ENABLE,
    object__class VARCHAR2(200) NOT NULL ENABLE,
    value VARCHAR2(4000) NULL,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_by_0 VARCHAR2(50) NULL ,
    created_by_0 VARCHAR2(50) NULL,
    CONSTRAINT PK_PREFERENCES2_ENTRY PRIMARY KEY (OBJECT_RID, OBJECT_OID)
  );
  
-- Test test::openmdx::app1

  DROP TABLE app1_Address;
  CREATE TABLE app1_Address (
    object_rid VARCHAR2 (200) NOT NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    p$$object_parent__rid VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_parent__oid VARCHAR2 (100)  NOT NULL ENABLE ,
    object__class VARCHAR2 (200) NOT NULL ENABLE ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    description VARCHAR2 (200) NULL ,
    created_at TIMESTAMP (3) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (3) WITH TIME ZONE NULL,
    modified_by_0 VARCHAR2 (50) NULL ,
    created_by_0 VARCHAR2 (50) NULL ,
    postal_code VARCHAR2 (100) NULL ,
    street VARCHAR2 (100) NULL ,
    country VARCHAR2 (100) NULL ,
    city VARCHAR2 (100) NULL ,
    house_number VARCHAR2 (100) NULL ,
    address VARCHAR2 (100) NULL,
    CONSTRAINT PK_app1_Address PRIMARY KEY (object_rid, object_oid)
  );
  DROP TABLE app1_Address_;
  CREATE TABLE app1_Address_ (
    object_rid VARCHAR2 (200) NOT NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    object_idx INTEGER NOT NULL ,
    address_line VARCHAR2 (100) NULL,
    CONSTRAINT PK_Address_ PRIMARY KEY (object_rid, object_oid, object_idx)
  );
  
  DROP TABLE app1_DOC;
  CREATE TABLE app1_DOC (
    object_rid VARCHAR2 (200) NOT NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    p$$object_parent__rid VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_parent__oid VARCHAR2 (100)  NOT NULL ENABLE ,
    object__class VARCHAR2 (200) NOT NULL ENABLE ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 VARCHAR2 (50) NULL ,
    modified_by_0 VARCHAR2 (50) NULL ,
    description VARCHAR2 (100) NULL ,
    content BLOB NULL ,
    text CLOB NULL ,
    CONSTRAINT PK_app1_DOC PRIMARY KEY (object_rid, object_oid)
  );

  DROP TABLE app1_DOC_;
  CREATE TABLE app1_DOC_ (
    object_rid VARCHAR2 (200) NOT NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    object_idx INTEGER NOT NULL ,
    keyword VARCHAR2 (32) NULL,
    CONSTRAINT PK_app1_DOC_ PRIMARY KEY (object_rid, object_oid, object_idx)
  );
  
  
  DROP TABLE app1_Invoice;
  CREATE TABLE app1_Invoice (
    object_rid VARCHAR2 (200) NOT NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    p$$object_parent__rid VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_parent__oid VARCHAR2 (100)  NOT NULL ENABLE ,
    object__class VARCHAR2 (200) NOT NULL ENABLE ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    description VARCHAR2 (200) NULL ,
    product_group_id VARCHAR2 (100) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 VARCHAR2 (50) NULL ,
    modified_by_0 VARCHAR2 (50) NULL ,
    INTERNATIONALPRODUCTGROUPID VARCHAR2 (100) NULL,
    payment_period INTERVAL DAY(3) TO SECOND(0) NULL,
    CONSTRAINT PK_app1_Invoice PRIMARY KEY (object_rid, object_oid)
  );
  
  DROP TABLE app1_InvoicePosition;
  CREATE TABLE app1_InvoicePosition (
    object_rid VARCHAR2 (200) NOT NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    p$$object_parent__rid VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_parent__oid VARCHAR2 (100)  NOT NULL ENABLE ,
    object__class VARCHAR2 (200) NOT NULL ENABLE ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    description VARCHAR2 (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 VARCHAR2 (50) NULL ,
    modified_by_0 VARCHAR2 (50) NULL ,
    product_id VARCHAR2 (50) NULL,
    CONSTRAINT PK_app1_InvoicePosition PRIMARY KEY (object_rid, object_oid)
  );
  
  DROP TABLE app1_Member;
  CREATE TABLE app1_Member (
    object_rid VARCHAR2 (200) NOT NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    p$$object_parent__rid VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_parent__oid VARCHAR2 (100)  NOT NULL ENABLE ,
    object__class VARCHAR2 (200) NOT NULL ENABLE ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    description VARCHAR2 (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_by_0 VARCHAR2 (50) NULL ,
    created_by_0 VARCHAR2 (50) NULL ,
    m1 VARCHAR2 (200) NULL ,
    p$$m1__rid VARCHAR2 (200) NULL ,
    p$$m1__oid VARCHAR2 (200) NULL ,
    m2 VARCHAR2 (200) NULL ,
    p$$m2__rid VARCHAR2 (200) NULL ,
    p$$m2__oid VARCHAR2 (200) NULL,
    CONSTRAINT PK_app1_Member PRIMARY KEY (object_rid, object_oid)
  );
  
  DROP TABLE app1_PersonGroup;
  CREATE TABLE app1_PersonGroup (
    object_rid VARCHAR2 (200) NOT NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    p$$object_parent__rid VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_parent__oid VARCHAR2 (100)  NOT NULL ENABLE ,
    object__class VARCHAR2 (200) NOT NULL ENABLE ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    description VARCHAR2 (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_by_0 VARCHAR2 (50) NULL ,
    created_by_0 VARCHAR2 (50) NULL ,
    name VARCHAR2 (100) NULL,
    CONSTRAINT PK_app1_PersonGroup PRIMARY KEY (object_rid, object_oid)
  );
  
  DROP TABLE app1_SLICED;
  CREATE TABLE app1_SLICED (
    object_rid VARCHAR2 (200) NOT NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    p$$object_parent__rid VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_parent__oid VARCHAR2 (100)  NOT NULL ENABLE ,
    object__class VARCHAR2 (200) NOT NULL ENABLE ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 VARCHAR2 (50)NULL ,
    modified_by_0 VARCHAR2 (50)NULL ,
    m1 VARCHAR2 (200) NULL ,
    last_name VARCHAR2 (200) NULL ,
    house_number VARCHAR2 (200) NULL ,
    city VARCHAR2 (200) NULL ,
    foreign_id VARCHAR2 (200) NULL ,
    postal_code VARCHAR2 (200) NULL ,
    description VARCHAR2 (200) NULL ,
    product_id VARCHAR2 (200) NULL ,
    salutation VARCHAR2 (200) NULL ,
    street VARCHAR2 (200) NULL ,
    address_line VARCHAR2 (200) NULL ,
    address VARCHAR2 (200) NULL ,
    text VARCHAR2 (200) NULL ,
    birthdate DATE NULL ,
    member_of_group VARCHAR2 (200) NULL ,
    birthdate_as_date_time TIMESTAMP (6) WITH TIME ZONE NULL,
    country VARCHAR2 (200) NULL ,
    sex INTEGER NULL ,
    product_group_id VARCHAR2 (200) NULL ,
    place_of_birth VARCHAR2 (200) NULL ,
    CONSTRAINT PK_app1_SLICED PRIMARY KEY (object_rid, object_oid)
  );
  DROP TABLE app1_SLICED_;
  CREATE TABLE app1_SLICED_ (
    object_rid VARCHAR2 (200) NOT NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    object_idx INTEGER NOT NULL ,
    person_group VARCHAR2 (200) NULL ,
    p$$person_group__rid VARCHAR2 (200) NULL ,
    p$$person_group__oid VARCHAR2 (200) NULL ,
    assigned_address VARCHAR2 (200) NULL ,
    p$$assigned_address__rid VARCHAR2 (200) NULL ,
    p$$assigned_address__oid VARCHAR2 (200) NULL ,
    given_name VARCHAR2 (200) NULL ,
    additional_info VARCHAR2 (200) NULL,
    CONSTRAINT PK_app1_SLICED_ PRIMARY KEY (object_rid, object_oid, object_idx)
  );
  
  DROP TABLE app1_Segment;
  CREATE TABLE app1_Segment (
    object_rid VARCHAR2 (200) NOT NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    object__class VARCHAR2 (200) NULL ,
    description VARCHAR2 (200) NULL,
    CONSTRAINT PK_app1_Segment PRIMARY KEY (object_rid, object_oid) 
  );
  
  DROP TABLE app1_MessageTemplate;
  CREATE TABLE app1_MessageTemplate (
    object_rid VARCHAR2 (200) NOT NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    p$$object_parent__rid VARCHAR2 (200)  NOT NULL ENABLE ,
    p$$object_parent__oid VARCHAR2 (200)  NOT NULL ENABLE ,
    object__class VARCHAR2 (200) NOT NULL ENABLE ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    text VARCHAR2 (200) NULL ,
    description VARCHAR2 (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 VARCHAR2 (50) NULL ,
    modified_by_0 VARCHAR2 (50) NULL,
    CONSTRAINT PK_app1_MessageTemplate PRIMARY KEY (object_rid, object_oid)
  );
  
  DROP TABLE audit2_UnitOfWork;
  CREATE TABLE audit2_UnitOfWork (
    object_rid VARCHAR2 (200) NOT NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    p$$object_parent__rid VARCHAR2 (200)  NOT NULL ENABLE ,
    p$$object_parent__oid VARCHAR2 (200)  NOT NULL ENABLE ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 VARCHAR2 (50)NULL ,
    object__class VARCHAR2 (60) NULL ,
    task_id VARCHAR2 (200) NULL,
    CONSTRAINT PK_audit2_UnitOfWork PRIMARY KEY (object_rid, object_oid)
  );
  
  DROP TABLE audit2_UnitOfWork_;
  CREATE TABLE audit2_UnitOfWork_ (
    object_rid VARCHAR2 (200) NOT NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    object_idx INTEGER NOT NULL ,
    involved VARCHAR2 (200) NULL ,
    p$$involved__rid VARCHAR2 (200) NULL ,
    p$$involved__oid VARCHAR2 (200) NULL,
    CONSTRAINT PK_audit2_UnitOfWork_ PRIMARY KEY (object_rid, object_oid, object_idx)
  );

  DROP TABLE audit2_Involvement;
  CREATE TABLE audit2_Involvement (
    object_rid VARCHAR2 (200) NOT NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    p$$object_parent__rid VARCHAR2 (200)  NOT NULL ENABLE ,
    p$$object_parent__oid VARCHAR2 (200)  NOT NULL ENABLE ,
    object__class VARCHAR2 (60) NOT NULL ENABLE ,
    before_image VARCHAR2 (200) NULL,
    p$$before_image__rid VARCHAR2 (200) NULL ,
    p$$before_image__oid VARCHAR2 (200) NULL ,
    after_image VARCHAR2 (200) NULL,
    p$$after_image__rid VARCHAR2 (200) NULL ,
    p$$after_image__oid VARCHAR2 (200) NULL,
    CONSTRAINT PK_audit2_Involvement PRIMARY KEY (object_rid, object_oid)
  );

  DROP TABLE audit2_Involvement_;
  CREATE TABLE audit2_Involvement_ (
    object_rid VARCHAR2 (200) NOT NULL ,
    object_oid VARCHAR2 (200) NOT NULL ,
    object_idx INTEGER NOT NULL ,
    modified_feature VARCHAR2 (60) NULL,
    CONSTRAINT PK_audit2_Involvement_ PRIMARY KEY (object_rid, object_oid, object_idx)
  );

  DROP TABLE app1_aud_Address;
  CREATE TABLE app1_aud_Address (
    p$$object_rsx VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_oid$0 VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_oid$1 VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_parent__rid VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_parent__oid VARCHAR2 (100)  NOT NULL ENABLE ,
    object__class VARCHAR2 (200) NOT NULL ENABLE ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    description VARCHAR2 (200) NULL ,
    created_at TIMESTAMP (3) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (3) WITH TIME ZONE NULL,
    modified_by_0 VARCHAR2 (50) NULL ,
    created_by_0 VARCHAR2 (50) NULL ,
    postal_code VARCHAR2 (100) NULL ,
    street VARCHAR2 (100) NULL ,
    country VARCHAR2 (100) NULL ,
    city VARCHAR2 (100) NULL ,
    house_number VARCHAR2 (100) NULL ,
    address VARCHAR2 (100) NULL,
    CONSTRAINT PK_app1_aud_Address PRIMARY KEY (p$$object_rsx, p$$object_oid$0, p$$object_oid$1)
  );
  DROP TABLE app1_aud_Address_;
  CREATE TABLE app1_aud_Address_ (
    p$$object_rsx VARCHAR2 (100) NOT NULL ENABLE ,
    p$$object_oid$0 VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_oid$1 VARCHAR2 (100)  NOT NULL ENABLE ,
    object_idx INTEGER NOT NULL ,
    address_line VARCHAR2 (100) NULL,
    CONSTRAINT PK_app1_aud_Address_ PRIMARY KEY (p$$object_rsx, p$$object_oid$0, p$$object_oid$1, object_idx)
  );
  
  DROP TABLE app1_aud_DOC;
  CREATE TABLE app1_aud_DOC (
    p$$object_rsx VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_oid$0 VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_oid$1 VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_parent__rid VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_parent__oid VARCHAR2 (100)  NOT NULL ENABLE ,
    object__class VARCHAR2 (200) NOT NULL ENABLE ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 VARCHAR2 (50) NULL ,
    modified_by_0 VARCHAR2 (50) NULL ,
    description VARCHAR2 (100) NULL ,
    content BLOB NULL ,
    text CLOB NULL,
    CONSTRAINT PK_app1_aud_DOC PRIMARY KEY (p$$object_rsx, p$$object_oid$0, p$$object_oid$1)
  );

  DROP TABLE app1_aud_DOC_;
  CREATE TABLE app1_aud_DOC_ (
    p$$object_rsx VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_oid$0 VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_oid$1 VARCHAR2 (100)  NOT NULL ENABLE ,
    object_idx INTEGER NOT NULL ,
    keyword VARCHAR2 (32) NULL,
    CONSTRAINT PK_app1_aud_DOC_ PRIMARY KEY (p$$object_rsx, p$$object_oid$0, p$$object_oid$1, object_idx)
  );
  
  DROP TABLE app1_aud_Invoice;
  CREATE TABLE app1_aud_Invoice (
    p$$object_rsx VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_oid$0 VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_oid$1 VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_parent__rid VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_parent__oid VARCHAR2 (100)  NOT NULL ENABLE ,
    object__class VARCHAR2 (200) NOT NULL ENABLE ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    description VARCHAR2 (200) NULL ,
    product_group_id VARCHAR2 (100) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 VARCHAR2 (50) NULL ,
    modified_by_0 VARCHAR2 (50) NULL ,
    INTERNATIONALPRODUCTGROUPID VARCHAR2 (100) NULL,
    payment_period INTERVAL DAY(3) TO SECOND(0) NULL,
    CONSTRAINT PK_app1_aud_Invoice PRIMARY KEY (p$$object_rsx, p$$object_oid$0, p$$object_oid$1)
  );
  
  DROP TABLE app1_aud_InvoicePosition;
  CREATE TABLE app1_aud_InvoicePosition (
    p$$object_rsx VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_oid$0 VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_oid$1 VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_parent__rid VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_parent__oid VARCHAR2 (100)  NOT NULL ENABLE ,
    object__class VARCHAR2 (200) NOT NULL ENABLE ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    description VARCHAR2 (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 VARCHAR2 (50) NULL ,
    modified_by_0 VARCHAR2 (50) NULL ,
    product_id VARCHAR2 (50) NULL,
    CONSTRAINT PK_app1_aud_InvoicePosition PRIMARY KEY (p$$object_rsx, p$$object_oid$0, p$$object_oid$1)
  );
  
  DROP TABLE app1_aud_Member;
  CREATE TABLE app1_aud_Member (
    p$$object_rsx VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_oid$0 VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_oid$1 VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_parent__rid VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_parent__oid VARCHAR2 (100)  NOT NULL ENABLE ,
    object__class VARCHAR2 (200) NOT NULL ENABLE ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    description VARCHAR2 (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_by_0 VARCHAR2 (50) NULL ,
    created_by_0 VARCHAR2 (50) NULL ,
    m1 VARCHAR2 (200) NULL ,
    p$$m1__rid VARCHAR2 (200) NULL ,
    p$$m1__oid VARCHAR2 (200) NULL ,
    m2 VARCHAR2 (200) NULL ,
    p$$m2__rid VARCHAR2 (200) NULL ,
    p$$m2__oid VARCHAR2 (200) NULL,
    CONSTRAINT PK_apud1_Member PRIMARY KEY (p$$object_rsx, p$$object_oid$0, p$$object_oid$1)
  );
  
  DROP TABLE app1_aud_PersonGroup;
  CREATE TABLE app1_aud_PersonGroup (
    p$$object_rsx VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_oid$0 VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_oid$1 VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_parent__rid VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_parent__oid VARCHAR2 (100)  NOT NULL ENABLE ,
    object__class VARCHAR2 (200) NOT NULL ENABLE ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    description VARCHAR2 (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_by_0 VARCHAR2 (50) NULL ,
    created_by_0 VARCHAR2 (50) NULL ,
    name VARCHAR2 (100) NULL,
    CONSTRAINT PK_app1_aud_PersonGroup PRIMARY KEY (p$$object_rsx, p$$object_oid$0, p$$object_oid$1)
  );
  
  DROP TABLE app1_aud_SLICED;
  CREATE TABLE app1_aud_SLICED (
    p$$object_rsx VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_oid$0 VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_oid$1 VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_parent__rid VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_parent__oid VARCHAR2 (100)  NOT NULL ENABLE ,
    object__class VARCHAR2 (200) NOT NULL ENABLE ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 VARCHAR2 (50)NULL ,
    modified_by_0 VARCHAR2 (50)NULL ,
    m1 VARCHAR2 (200) NULL ,
    last_name VARCHAR2 (200) NULL ,
    house_number VARCHAR2 (200) NULL ,
    city VARCHAR2 (200) NULL ,
    foreign_id VARCHAR2 (200) NULL ,
    postal_code VARCHAR2 (200) NULL ,
    description VARCHAR2 (200) NULL ,
    product_id VARCHAR2 (200) NULL ,
    salutation VARCHAR2 (200) NULL ,
    street VARCHAR2 (200) NULL ,
    address_line VARCHAR2 (200) NULL ,
    address VARCHAR2 (200) NULL ,
    text VARCHAR2 (200) NULL ,
    birthdate DATE NULL ,
    member_of_group VARCHAR2 (200) NULL ,
    birthdate_as_date_time TIMESTAMP (6) WITH TIME ZONE NULL,
    country VARCHAR2 (200) NULL ,
    sex INTEGER NULL ,
    product_group_id VARCHAR2 (200) NULL ,
    place_of_birth VARCHAR2 (200) NULL,
    CONSTRAINT PK_app1_aud_SLICED PRIMARY KEY (p$$object_rsx, p$$object_oid$0, p$$object_oid$1)
  );
  
  DROP TABLE app1_aud_SLICED_;
  CREATE TABLE app1_aud_SLICED_ (
    p$$object_rsx VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_oid$0 VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_oid$1 VARCHAR2 (100)  NOT NULL ENABLE ,
    object_idx INTEGER NOT NULL ,
    person_group VARCHAR2 (200) NULL ,
    p$$person_group__rid VARCHAR2 (200) NULL ,
    p$$person_group__oid VARCHAR2 (200) NULL ,
    assigned_address VARCHAR2 (200) NULL ,
    p$$assigned_address__rid VARCHAR2 (200) NULL ,
    p$$assigned_address__oid VARCHAR2 (200) NULL ,
    given_name VARCHAR2 (200) NULL ,
    additional_info VARCHAR2 (200) NULL,
    CONSTRAINT PK_app1_aud_SLICED_ PRIMARY KEY (p$$object_rsx, p$$object_oid$0, p$$object_oid$1, object_idx)
  );
  
  DROP TABLE app1_aud_MessageTemplate;
  CREATE TABLE app1_aud_MessageTemplate (
    p$$object_rsx VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_oid$0 VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_oid$1 VARCHAR2 (100)  NOT NULL ENABLE ,
    p$$object_parent__rid VARCHAR2 (200)  NOT NULL ENABLE ,
    p$$object_parent__oid VARCHAR2 (200)  NOT NULL ENABLE ,
    object__class VARCHAR2 (200) NOT NULL ENABLE ,
    p$$unit_of_work__rid VARCHAR2 (100)  NULL,
    text VARCHAR2 (200) NULL ,
    description VARCHAR2 (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 VARCHAR2 (50) NULL ,
    modified_by_0 VARCHAR2 (50) NULL,
    CONSTRAINT PK_app1_aud_MessageTemplate PRIMARY KEY (p$$object_rsx, p$$object_oid$0, p$$object_oid$1)
  );

-- Test Extension

  DROP TABLE EXTENSION_DEFAULT ;
  CREATE TABLE EXTENSION_DEFAULT (	
    OBJECT_RID VARCHAR2(200) NOT NULL ENABLE, 
	  OBJECT_OID VARCHAR2(200) NOT NULL ENABLE, 
    P$$OBJECT_parent__oid VARCHAR2(200) NOT NULL ENABLE, 
	  P$$OBJECT_parent__rid VARCHAR2(200) NOT NULL ENABLE,
	  OBJECT__CLASS VARCHAR2(100) NOT NULL ENABLE, 
	  OBJECT__VALID_FROM CHAR(20), 
	  OBJECT__VALID_TO CHAR(20), 
	  OBJECT__INVALIDATED_AT CHAR(20), 
	  OBJECT__STATE_NUMBER NUMBER(10,0), 
	  OBJECT__STATE_ID VARCHAR2(100), 
	  SEGMENT VARCHAR2(200), 
	  P$$SEGMENT__rid VARCHAR2(200), 
	  P$$SEGMENT__oid VARCHAR2(200), 
	  VALUE1 CHAR(9), 
	  VALUE2 NUMBER, 
	  VALUE4 NUMBER, 
	  VALUE5 NUMBER, 
	  VALUE9 VARCHAR2(200), 
	  VALUE10 RAW(2000), 
	  VALUE11A VARCHAR2(20), 
	  VALUE11B VARCHAR2(20), 
	  VALUE6 VARCHAR2(200), 
	  VALUE7 CHAR(20), 
	  VALUE8 VARCHAR2(8),
	  COUNTRY VARCHAR2(4),
      CONSTRAINT PK_EXTENSION_DEFAULT PRIMARY KEY (OBJECT_RID, OBJECT_OID)
  ) ;
  DROP TABLE EXTENSION_DEFAULT_;
  CREATE TABLE EXTENSION_DEFAULT_ (	
    OBJECT_RID VARCHAR2(200) NOT NULL ENABLE, 
	OBJECT_OID VARCHAR2(200) NOT NULL ENABLE, 
	OBJECT_IDX NUMBER(*,0) NOT NULL ENABLE, 
	VALUE3 NUMBER,
    CONSTRAINT PK_EXTENSION_DEFAULT_ PRIMARY KEY (OBJECT_RID, OBJECT_OID, OBJECT_IDX)
  );


  DROP TABLE EXTENSION_NATIVE ;
  CREATE TABLE EXTENSION_NATIVE (
    OBJECT_RID VARCHAR2(200) NOT NULL ENABLE, 
 	  OBJECT_OID VARCHAR2(200) NOT NULL ENABLE, 
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
	  P$$SEGMENT__rid VARCHAR2(200), 
	  P$$SEGMENT__oid VARCHAR2(200),
	  VALUE1 CHAR(1 CHAR), 
	  VALUE2 NUMBER,
	  VALUE4 NUMBER,
	  VALUE5 NUMBER, 
	  VALUE9 VARCHAR2(200), 
	  VALUE10 RAW(2000), 
	  VALUE11A INTERVAL YEAR (9) TO MONTH, 
	  VALUE11B INTERVAL DAY (9) TO SECOND (3), 
	  VALUE6 VARCHAR2(200),
	  VALUE7 TIMESTAMP (6) WITH TIME ZONE, 
	  VALUE8 DATE,
	  COUNTRY VARCHAR2(4),
      CONSTRAINT PK_EXTENSION_NATIGVE PRIMARY KEY (OBJECT_RID, OBJECT_OID)
  ) ;
  DROP TABLE EXTENSION_NATIVE_ ;
  CREATE TABLE EXTENSION_NATIVE_ (
    OBJECT_RID VARCHAR2(200) NOT NULL ENABLE, 
 	  OBJECT_OID VARCHAR2(200) NOT NULL ENABLE, 
	  OBJECT_IDX NUMBER(*,0) NOT NULL ENABLE, 
	  VALUE3 NUMBER, 
      CONSTRAINT PK_EXTENSION_NATIVE_ PRIMARY KEY (OBJECT_RID, OBJECT_OID, OBJECT_IDX)
  ) ;

  DROP TABLE EXTENSION_NUMERIC ;
  CREATE TABLE EXTENSION_NUMERIC (	
    OBJECT_RID VARCHAR2(200) NOT NULL ENABLE, 
	  OBJECT_OID VARCHAR2(200) NOT NULL ENABLE, 
	  P$$OBJECT_parent__rid VARCHAR2(200) NOT NULL ENABLE, 
	  P$$OBJECT_parent__oid VARCHAR2(200) NOT NULL ENABLE,
	  OBJECT__CLASS VARCHAR2(100) NOT NULL ENABLE, 
	  OBJECT__VALID_FROM NUMBER(19,3), 
	  OBJECT__VALID_TO NUMBER(19,3), 
	  OBJECT__INVALIDATED_AT NUMBER(19,3), 
	  OBJECT__STATE_NUMBER NUMBER(10,0), 
	  OBJECT__STATE_ID VARCHAR2(100), 
	  SEGMENT VARCHAR2(200), 
	  P$$SEGMENT__rid VARCHAR2(200),
	  P$$SEGMENT__oid VARCHAR2(200),
	  VALUE1 NUMBER(1,0), 
	  VALUE2 NUMBER, 
	  VALUE4 NUMBER, 
	  VALUE5 NUMBER, 
	  VALUE9 VARCHAR2(200), 
	  VALUE10 RAW(2000), 
	  VALUE11A NUMBER(9,0),
	  VALUE11B NUMBER(12,3),
	  VALUE6 VARCHAR2(200),
	  VALUE7 NUMBER(19,3),
	  VALUE8 VARCHAR2(8),
	  COUNTRY VARCHAR2(4), 
      CONSTRAINT PK_EXTENSION_NUMERIC PRIMARY KEY (OBJECT_RID, OBJECT_OID)
  ) ;
  DROP TABLE EXTENSION_NUMERIC_ ;
  CREATE TABLE EXTENSION_NUMERIC_ (	
      OBJECT_RID VARCHAR2(200) NOT NULL ENABLE, 
	  OBJECT_OID VARCHAR2(200) NOT NULL ENABLE, 
	  OBJECT_IDX NUMBER(*,0) NOT NULL ENABLE, 
	  VALUE3 NUMBER,
	  CONSTRAINT PK_EXTENSION_NUMERIC_ PRIMARY KEY (OBJECT_RID, OBJECT_OID, OBJECT_IDX)
  ) ;

  DROP TABLE EXTENSION_SEGMENT ;
  CREATE TABLE EXTENSION_SEGMENT (
    OBJECT_RID VARCHAR2(200) NOT NULL ENABLE,
	OBJECT_OID VARCHAR2(200) NOT NULL ENABLE,
	OBJECT__CLASS VARCHAR2(200) NOT NULL ENABLE,
	DESCRIPTION VARCHAR2(200),
    CONSTRAINT PK_EXTENSION_SEGMENT PRIMARY KEY (OBJECT_RID, OBJECT_OID)
  );

-- Test org::openmdx::generic1
    
  DROP TABLE generic1_Property ;
  CREATE TABLE generic1_Property (
    object_rid VARCHAR2(100) NOT NULL,
    object_oid VARCHAR2(200) NOT NULL,
    p$$object_parent__rid VARCHAR2(100) NULL, 
    p$$object_parent__oid VARCHAR2(200) NULL,
    object__class VARCHAR2(200) NOT NULL ENABLE ,
    description VARCHAR2(200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_by_0 VARCHAR2(50) NULL ,
    created_by_0 VARCHAR2(50) NULL,
    CONSTRAINT PK_generic1_Property PRIMARY KEY (OBJECT_RID, OBJECT_OID)
  );

  DROP TABLE generic1_Property_;
  CREATE TABLE generic1_Property_ (
    object_rid VARCHAR2(100) NOT NULL,
    object_oid VARCHAR2(200) NOT NULL,
    object_idx INTEGER NOT NULL ,
    boolean_value CHAR(1 CHAR) NULL ,
    uri_value VARCHAR2(200) NULL ,
    decimal_value NUMBER(18, 6) NULL ,
    string_value VARCHAR2(200) NULL ,
    integer_value NUMBER(10, 0) NULL,
    CONSTRAINT PK_generic1_Property_ PRIMARY KEY (OBJECT_RID, OBJECT_OID, object_idx) 
  );
