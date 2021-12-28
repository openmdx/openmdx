-- Test test::openmdx::preferences2

  CREATE TABLE PREFERENCES2_SEGMENT (
    OBJECT_RID varchar(200) NOT NULL,
	OBJECT_OID varchar(200) NOT NULL,
	OBJECT__CLASS varchar(200) NOT NULL,
	DESCRIPTION varchar(200),
    CONSTRAINT PK_PREFERENCES2_SEGMENT PRIMARY KEY (OBJECT_RID, OBJECT_OID)
  );
  
  CREATE TABLE PREFERENCES2_ROOT(
    object_rid varchar(100) NOT NULL,
    object_oid varchar(200) NOT NULL,
    p$$object_parent__rid varchar(100) NOT NULL, 
    p$$object_parent__oid varchar(200) NOT NULL,
    object__class varchar(200) NOT NULL ,
    type varchar(10) NOT NULL,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_by_0 varchar(50) NULL ,
    created_by_0 varchar(50) NULL,
    CONSTRAINT PK_PREFERENCES2_ROOT PRIMARY KEY (OBJECT_RID, OBJECT_OID)
  );

  CREATE TABLE PREFERENCES2_NODE(
    object_rid varchar(100) NOT NULL,
    object_oid varchar(200) NOT NULL,
    p$$object_parent__rid varchar(100) NOT NULL, 
    p$$object_parent__oid varchar(200) NOT NULL,
    object__class varchar(200) NOT NULL ,
    parent varchar (200) NULL ,
    p$$parent__rid varchar (200) NULL ,
    p$$parent__oid varchar (200) NULL ,
    name varchar(100) NULL,
    absolute_path varchar(1000) NOT NULL,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_by_0 varchar(50) NULL ,
    created_by_0 varchar(50) NULL,
    CONSTRAINT PK_PREFERENCES2_NODE PRIMARY KEY (OBJECT_RID, OBJECT_OID)
  );

  CREATE TABLE PREFERENCES2_ENTRY(
    object_rid varchar(100) NOT NULL,
    object_oid varchar(200) NOT NULL,
    p$$object_parent__rid varchar(100) NOT NULL, 
    p$$object_parent__oid varchar(200) NOT NULL,
    object__class varchar(200) NOT NULL,
    value varchar(4000) NULL,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_by_0 varchar(50) NULL ,
    created_by_0 varchar(50) NULL,
    CONSTRAINT PK_PREFERENCES2_ENTRY PRIMARY KEY (OBJECT_RID, OBJECT_OID)
  );
  
-- Test test::openmdx::app1

  CREATE TABLE app1_Address (
    object_rid varchar (200) NOT NULL ,
    object_oid varchar (200) NOT NULL ,
    p$$object_parent__rid varchar (100)  NOT NULL ,
    p$$object_parent__oid varchar (100)  NOT NULL ,
    object__class varchar (200) NOT NULL ,
    p$$unit_of_work__rid varchar (100)  NULL,
    description varchar (200) NULL ,
    created_at TIMESTAMP (3) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (3) WITH TIME ZONE NULL,
    modified_by_0 varchar (50) NULL ,
    created_by_0 varchar (50) NULL ,
    postal_code varchar (100) NULL ,
    street varchar (100) NULL ,
    country varchar (100) NULL ,
    city varchar (100) NULL ,
    house_number varchar (100) NULL ,
    address varchar (100) NULL,
    CONSTRAINT PK_app1_Address PRIMARY KEY (object_rid, object_oid)
  );
  CREATE TABLE app1_Address_ (
    object_rid varchar (200) NOT NULL ,
    object_oid varchar (200) NOT NULL ,
    object_idx INTEGER NOT NULL ,
    address_line varchar (100) NULL,
    CONSTRAINT PK_Address_ PRIMARY KEY (object_rid, object_oid, object_idx)
  );
  
  CREATE TABLE app1_DOC (
    object_rid varchar (200) NOT NULL ,
    object_oid varchar (200) NOT NULL ,
    p$$object_parent__rid varchar (100)  NOT NULL ,
    p$$object_parent__oid varchar (100)  NOT NULL ,
    object__class varchar (200) NOT NULL ,
    p$$unit_of_work__rid varchar (100)  NULL,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 varchar (50) NULL ,
    modified_by_0 varchar (50) NULL ,
    description varchar (100) NULL ,
    content bytea NULL ,
    text text NULL ,
    CONSTRAINT PK_app1_DOC PRIMARY KEY (object_rid, object_oid)
  );
  CREATE TABLE app1_DOC_ (
    object_rid varchar (200) NOT NULL ,
    object_oid varchar (200) NOT NULL ,
    object_idx INTEGER NOT NULL ,
    keyword varchar (32) NULL,
    CONSTRAINT PK_app1_DOC_ PRIMARY KEY (object_rid, object_oid, object_idx)
  );
  
  CREATE TABLE app1_Invoice (
    object_rid varchar (200) NOT NULL ,
    object_oid varchar (200) NOT NULL ,
    p$$object_parent__rid varchar (100)  NOT NULL ,
    p$$object_parent__oid varchar (100)  NOT NULL ,
    object__class varchar (200) NOT NULL ,
    p$$unit_of_work__rid varchar (100)  NULL,
    description varchar (200) NULL ,
    product_group_id varchar (100) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 varchar (50) NULL ,
    modified_by_0 varchar (50) NULL ,
    INTERNATIONALPRODUCTGROUPID varchar (100) NULL,
    payment_period INTERVAL DAY TO SECOND NULL,
    CONSTRAINT PK_app1_Invoice PRIMARY KEY (object_rid, object_oid)
  );

  CREATE TABLE app1_InvoicePosition (
    object_rid varchar (200) NOT NULL ,
    object_oid varchar (200) NOT NULL ,
    p$$object_parent__rid varchar (100)  NOT NULL ,
    p$$object_parent__oid varchar (100)  NOT NULL ,
    object__class varchar (200) NOT NULL ,
    p$$unit_of_work__rid varchar (100)  NULL,
    description varchar (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 varchar (50) NULL ,
    modified_by_0 varchar (50) NULL ,
    product_id varchar (50) NULL,
    CONSTRAINT PK_app1_InvoicePosition PRIMARY KEY (object_rid, object_oid)
  );

  CREATE TABLE app1_Member (
    object_rid varchar (200) NOT NULL ,
    object_oid varchar (200) NOT NULL ,
    p$$object_parent__rid varchar (100)  NOT NULL ,
    p$$object_parent__oid varchar (100)  NOT NULL ,
    object__class varchar (200) NOT NULL ,
    p$$unit_of_work__rid varchar (100)  NULL,
    description varchar (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_by_0 varchar (50) NULL ,
    created_by_0 varchar (50) NULL ,
    m1 varchar (200) NULL ,
    p$$m1__rid varchar (200) NULL ,
    p$$m1__oid varchar (200) NULL ,
    m2 varchar (200) NULL ,
    p$$m2__rid varchar (200) NULL ,
    p$$m2__oid varchar (200) NULL,
    CONSTRAINT PK_app1_Member PRIMARY KEY (object_rid, object_oid)
  );

  CREATE TABLE app1_PersonGroup (
    object_rid varchar (200) NOT NULL ,
    object_oid varchar (200) NOT NULL ,
    p$$object_parent__rid varchar (100)  NOT NULL ,
    p$$object_parent__oid varchar (100)  NOT NULL ,
    object__class varchar (200) NOT NULL ,
    p$$unit_of_work__rid varchar (100)  NULL,
    description varchar (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_by_0 varchar (50) NULL ,
    created_by_0 varchar (50) NULL ,
    name varchar (100) NULL,
    CONSTRAINT PK_app1_PersonGroup PRIMARY KEY (object_rid, object_oid)
  );

  CREATE TABLE app1_SLICED (
    object_rid varchar (200) NOT NULL ,
    object_oid varchar (200) NOT NULL ,
    p$$object_parent__rid varchar (100)  NOT NULL ,
    p$$object_parent__oid varchar (100)  NOT NULL ,
    object__class varchar (200) NOT NULL ,
    p$$unit_of_work__rid varchar (100)  NULL,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 varchar (50)NULL ,
    modified_by_0 varchar (50)NULL ,
    m1 varchar (200) NULL ,
    last_name varchar (200) NULL ,
    house_number varchar (200) NULL ,
    city varchar (200) NULL ,
    foreign_id varchar (200) NULL ,
    postal_code varchar (200) NULL ,
    description varchar (200) NULL ,
    product_id varchar (200) NULL ,
    salutation varchar (200) NULL ,
    street varchar (200) NULL ,
    address_line varchar (200) NULL ,
    address varchar (200) NULL ,
    text varchar (200) NULL ,
    birthdate DATE NULL ,
    member_of_group varchar (200) NULL ,
    birthdate_as_date_time TIMESTAMP (6) WITH TIME ZONE NULL,
    country varchar (200) NULL ,
    sex INTEGER NULL ,
    product_group_id varchar (200) NULL ,
    place_of_birth varchar (200) NULL ,
    CONSTRAINT PK_app1_SLICED PRIMARY KEY (object_rid, object_oid)
  );
  CREATE TABLE app1_SLICED_ (
    object_rid varchar (200) NOT NULL ,
    object_oid varchar (200) NOT NULL ,
    object_idx INTEGER NOT NULL ,
    person_group varchar (200) NULL ,
    p$$person_group__rid varchar (200) NULL ,
    p$$person_group__oid varchar (200) NULL ,
    assigned_address varchar (200) NULL ,
    p$$assigned_address__rid varchar (200) NULL ,
    p$$assigned_address__oid varchar (200) NULL ,
    given_name varchar (200) NULL ,
    additional_info varchar (200) NULL,
    CONSTRAINT PK_app1_SLICED_ PRIMARY KEY (object_rid, object_oid, object_idx)
  );

  CREATE TABLE app1_Segment (
    object_rid varchar (200) NOT NULL ,
    object_oid varchar (200) NOT NULL ,
    object__class varchar (200) NULL ,
    description varchar (200) NULL,
    CONSTRAINT PK_app1_Segment PRIMARY KEY (object_rid, object_oid) 
  );

  CREATE TABLE app1_MessageTemplate (
    object_rid varchar (200) NOT NULL ,
    object_oid varchar (200) NOT NULL ,
    p$$object_parent__rid varchar (200)  NOT NULL ,
    p$$object_parent__oid varchar (200)  NOT NULL ,
    object__class varchar (200) NOT NULL ,
    p$$unit_of_work__rid varchar (100)  NULL,
    text varchar (200) NULL ,
    description varchar (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 varchar (50) NULL ,
    modified_by_0 varchar (50) NULL,
    CONSTRAINT PK_app1_MessageTemplate PRIMARY KEY (object_rid, object_oid)
  );

  CREATE TABLE audit2_UnitOfWork (
    object_rid varchar (200) NOT NULL ,
    object_oid varchar (200) NOT NULL ,
    p$$object_parent__rid varchar (200)  NOT NULL ,
    p$$object_parent__oid varchar (200)  NOT NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 varchar (50)NULL ,
    object__class varchar (60) NULL ,
    task_id varchar (200) NULL,
    CONSTRAINT PK_audit2_UnitOfWork PRIMARY KEY (object_rid, object_oid)
  );
  CREATE TABLE audit2_UnitOfWork_ (
    object_rid varchar (200) NOT NULL ,
    object_oid varchar (200) NOT NULL ,
    object_idx INTEGER NOT NULL ,
    involved varchar (200) NULL ,
    p$$involved__rid varchar (200) NULL ,
    p$$involved__oid varchar (200) NULL,
    CONSTRAINT PK_audit2_UnitOfWork_ PRIMARY KEY (object_rid, object_oid, object_idx)
  );

  CREATE TABLE audit2_Involvement (
    object_rid varchar (200) NOT NULL ,
    object_oid varchar (200) NOT NULL ,
    p$$object_parent__rid varchar (200)  NOT NULL ,
    p$$object_parent__oid varchar (200)  NOT NULL ,
    object__class varchar (60) NOT NULL ,
    before_image varchar (200) NULL,
    p$$before_image__rid varchar (200) NULL ,
    p$$before_image__oid varchar (200) NULL ,
    after_image varchar (200) NULL,
    p$$after_image__rid varchar (200) NULL ,
    p$$after_image__oid varchar (200) NULL,
    CONSTRAINT PK_audit2_Involvement PRIMARY KEY (object_rid, object_oid)
  );
  CREATE TABLE audit2_Involvement_ (
    object_rid varchar (200) NOT NULL ,
    object_oid varchar (200) NOT NULL ,
    object_idx INTEGER NOT NULL ,
    modified_feature varchar (60) NULL,
    CONSTRAINT PK_audit2_Involvement_ PRIMARY KEY (object_rid, object_oid, object_idx)
  );

  CREATE TABLE app1_aud_Address (
    p$$object_rsx varchar (100)  NOT NULL ,
    p$$object_oid$0 varchar (100)  NOT NULL ,
    p$$object_oid$1 varchar (100)  NOT NULL ,
    p$$object_parent__rid varchar (100)  NOT NULL ,
    p$$object_parent__oid varchar (100)  NOT NULL ,
    object__class varchar (200) NOT NULL ,
    p$$unit_of_work__rid varchar (100)  NULL,
    description varchar (200) NULL ,
    created_at TIMESTAMP (3) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (3) WITH TIME ZONE NULL,
    modified_by_0 varchar (50) NULL ,
    created_by_0 varchar (50) NULL ,
    postal_code varchar (100) NULL ,
    street varchar (100) NULL ,
    country varchar (100) NULL ,
    city varchar (100) NULL ,
    house_number varchar (100) NULL ,
    address varchar (100) NULL,
    CONSTRAINT PK_app1_aud_Address PRIMARY KEY (p$$object_rsx, p$$object_oid$0, p$$object_oid$1)
  );
  CREATE TABLE app1_aud_Address_ (
    p$$object_rsx varchar (100) NOT NULL ,
    p$$object_oid$0 varchar (100)  NOT NULL ,
    p$$object_oid$1 varchar (100)  NOT NULL ,
    object_idx INTEGER NOT NULL ,
    address_line varchar (100) NULL,
    CONSTRAINT PK_app1_aud_Address_ PRIMARY KEY (p$$object_rsx, p$$object_oid$0, p$$object_oid$1, object_idx)
  );

  CREATE TABLE app1_aud_DOC (
    p$$object_rsx varchar (100)  NOT NULL ,
    p$$object_oid$0 varchar (100)  NOT NULL ,
    p$$object_oid$1 varchar (100)  NOT NULL ,
    p$$object_parent__rid varchar (100)  NOT NULL ,
    p$$object_parent__oid varchar (100)  NOT NULL ,
    object__class varchar (200) NOT NULL ,
    p$$unit_of_work__rid varchar (100)  NULL,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 varchar (50) NULL ,
    modified_by_0 varchar (50) NULL ,
    description varchar (100) NULL ,
    content bytea NULL ,
    text text NULL,
    CONSTRAINT PK_app1_aud_DOC PRIMARY KEY (p$$object_rsx, p$$object_oid$0, p$$object_oid$1)
  );
  CREATE TABLE app1_aud_DOC_ (
    p$$object_rsx varchar (100)  NOT NULL ,
    p$$object_oid$0 varchar (100)  NOT NULL ,
    p$$object_oid$1 varchar (100)  NOT NULL ,
    object_idx INTEGER NOT NULL ,
    keyword varchar (32) NULL,
    CONSTRAINT PK_app1_aud_DOC_ PRIMARY KEY (p$$object_rsx, p$$object_oid$0, p$$object_oid$1, object_idx)
  );

  CREATE TABLE app1_aud_Invoice (
    p$$object_rsx varchar (100)  NOT NULL ,
    p$$object_oid$0 varchar (100)  NOT NULL ,
    p$$object_oid$1 varchar (100)  NOT NULL ,
    p$$object_parent__rid varchar (100)  NOT NULL ,
    p$$object_parent__oid varchar (100)  NOT NULL ,
    object__class varchar (200) NOT NULL ,
    p$$unit_of_work__rid varchar (100)  NULL,
    description varchar (200) NULL ,
    product_group_id varchar (100) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 varchar (50) NULL ,
    modified_by_0 varchar (50) NULL ,
    INTERNATIONALPRODUCTGROUPID varchar (100) NULL,
    payment_period INTERVAL DAY TO SECOND NULL,
    CONSTRAINT PK_app1_aud_Invoice PRIMARY KEY (p$$object_rsx, p$$object_oid$0, p$$object_oid$1)
  );

  CREATE TABLE app1_aud_InvoicePosition (
    p$$object_rsx varchar (100)  NOT NULL ,
    p$$object_oid$0 varchar (100)  NOT NULL ,
    p$$object_oid$1 varchar (100)  NOT NULL ,
    p$$object_parent__rid varchar (100)  NOT NULL ,
    p$$object_parent__oid varchar (100)  NOT NULL ,
    object__class varchar (200) NOT NULL ,
    p$$unit_of_work__rid varchar (100)  NULL,
    description varchar (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 varchar (50) NULL ,
    modified_by_0 varchar (50) NULL ,
    product_id varchar (50) NULL,
    CONSTRAINT PK_app1_aud_InvoicePosition PRIMARY KEY (p$$object_rsx, p$$object_oid$0, p$$object_oid$1)
  );
  
  CREATE TABLE app1_aud_Member (
    p$$object_rsx varchar (100)  NOT NULL ,
    p$$object_oid$0 varchar (100)  NOT NULL ,
    p$$object_oid$1 varchar (100)  NOT NULL ,
    p$$object_parent__rid varchar (100)  NOT NULL ,
    p$$object_parent__oid varchar (100)  NOT NULL ,
    object__class varchar (200) NOT NULL ,
    p$$unit_of_work__rid varchar (100)  NULL,
    description varchar (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_by_0 varchar (50) NULL ,
    created_by_0 varchar (50) NULL ,
    m1 varchar (200) NULL ,
    p$$m1__rid varchar (200) NULL ,
    p$$m1__oid varchar (200) NULL ,
    m2 varchar (200) NULL ,
    p$$m2__rid varchar (200) NULL ,
    p$$m2__oid varchar (200) NULL,
    CONSTRAINT PK_apud1_Member PRIMARY KEY (p$$object_rsx, p$$object_oid$0, p$$object_oid$1)
  );

  CREATE TABLE app1_aud_PersonGroup (
    p$$object_rsx varchar (100)  NOT NULL ,
    p$$object_oid$0 varchar (100)  NOT NULL ,
    p$$object_oid$1 varchar (100)  NOT NULL ,
    p$$object_parent__rid varchar (100)  NOT NULL ,
    p$$object_parent__oid varchar (100)  NOT NULL ,
    object__class varchar (200) NOT NULL ,
    p$$unit_of_work__rid varchar (100)  NULL,
    description varchar (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_by_0 varchar (50) NULL ,
    created_by_0 varchar (50) NULL ,
    name varchar (100) NULL,
    CONSTRAINT PK_app1_aud_PersonGroup PRIMARY KEY (p$$object_rsx, p$$object_oid$0, p$$object_oid$1)
  );

  CREATE TABLE app1_aud_SLICED (
    p$$object_rsx varchar (100)  NOT NULL ,
    p$$object_oid$0 varchar (100)  NOT NULL ,
    p$$object_oid$1 varchar (100)  NOT NULL ,
    p$$object_parent__rid varchar (100)  NOT NULL ,
    p$$object_parent__oid varchar (100)  NOT NULL ,
    object__class varchar (200) NOT NULL ,
    p$$unit_of_work__rid varchar (100)  NULL,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 varchar (50)NULL ,
    modified_by_0 varchar (50)NULL ,
    m1 varchar (200) NULL ,
    last_name varchar (200) NULL ,
    house_number varchar (200) NULL ,
    city varchar (200) NULL ,
    foreign_id varchar (200) NULL ,
    postal_code varchar (200) NULL ,
    description varchar (200) NULL ,
    product_id varchar (200) NULL ,
    salutation varchar (200) NULL ,
    street varchar (200) NULL ,
    address_line varchar (200) NULL ,
    address varchar (200) NULL ,
    text varchar (200) NULL ,
    birthdate DATE NULL ,
    member_of_group varchar (200) NULL ,
    birthdate_as_date_time TIMESTAMP (6) WITH TIME ZONE NULL,
    country varchar (200) NULL ,
    sex INTEGER NULL ,
    product_group_id varchar (200) NULL ,
    place_of_birth varchar (200) NULL,
    CONSTRAINT PK_app1_aud_SLICED PRIMARY KEY (p$$object_rsx, p$$object_oid$0, p$$object_oid$1)
  );
  CREATE TABLE app1_aud_SLICED_ (
    p$$object_rsx varchar (100)  NOT NULL ,
    p$$object_oid$0 varchar (100)  NOT NULL ,
    p$$object_oid$1 varchar (100)  NOT NULL ,
    object_idx INTEGER NOT NULL ,
    person_group varchar (200) NULL ,
    p$$person_group__rid varchar (200) NULL ,
    p$$person_group__oid varchar (200) NULL ,
    assigned_address varchar (200) NULL ,
    p$$assigned_address__rid varchar (200) NULL ,
    p$$assigned_address__oid varchar (200) NULL ,
    given_name varchar (200) NULL ,
    additional_info varchar (200) NULL,
    CONSTRAINT PK_app1_aud_SLICED_ PRIMARY KEY (p$$object_rsx, p$$object_oid$0, p$$object_oid$1, object_idx)
  );

  CREATE TABLE app1_aud_MessageTemplate (
    p$$object_rsx varchar (100)  NOT NULL ,
    p$$object_oid$0 varchar (100)  NOT NULL ,
    p$$object_oid$1 varchar (100)  NOT NULL ,
    p$$object_parent__rid varchar (200)  NOT NULL ,
    p$$object_parent__oid varchar (200)  NOT NULL ,
    object__class varchar (200) NOT NULL ,
    p$$unit_of_work__rid varchar (100)  NULL,
    text varchar (200) NULL ,
    description varchar (200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    created_by_0 varchar (50) NULL ,
    modified_by_0 varchar (50) NULL,
    CONSTRAINT PK_app1_aud_MessageTemplate PRIMARY KEY (p$$object_rsx, p$$object_oid$0, p$$object_oid$1)
  );

-- Test Extension

  CREATE TABLE EXTENSION_DEFAULT (	
    OBJECT_RID varchar(200) NOT NULL, 
	  OBJECT_OID varchar(200) NOT NULL, 
    P$$OBJECT_parent__oid varchar(200) NOT NULL, 
	  P$$OBJECT_parent__rid varchar(200) NOT NULL,
	  OBJECT__CLASS varchar(100) NOT NULL, 
	  OBJECT__VALID_FROM CHAR(20), 
	  OBJECT__VALID_TO CHAR(20), 
	  OBJECT__INVALIDATED_AT CHAR(20), 
	  OBJECT__STATE_NUMBER integer, 
	  OBJECT__STATE_ID varchar(100), 
	  SEGMENT varchar(200), 
	  P$$SEGMENT__rid varchar(200), 
	  P$$SEGMENT__oid varchar(200), 
	  VALUE1 CHAR(9), 
	  VALUE2 float, 
	  VALUE4 float, 
	  VALUE5 float, 
	  VALUE9 varchar(200), 
	  VALUE10 bytea, 
	  VALUE11A varchar(20), 
	  VALUE11B varchar(20), 
	  VALUE6 varchar(200), 
	  VALUE7 CHAR(20), 
	  VALUE8 varchar(8),
	  COUNTRY varchar(4),
      CONSTRAINT PK_EXTENSION_DEFAULT PRIMARY KEY (OBJECT_RID, OBJECT_OID)
  ) ;
  CREATE TABLE EXTENSION_DEFAULT_ (	
    OBJECT_RID varchar(200) NOT NULL, 
	OBJECT_OID varchar(200) NOT NULL, 
	OBJECT_IDX integer NOT NULL, 
	VALUE3 float,
    CONSTRAINT PK_EXTENSION_DEFAULT_ PRIMARY KEY (OBJECT_RID, OBJECT_OID, OBJECT_IDX)
  );

  CREATE TABLE EXTENSION_NATIVE (
    OBJECT_RID varchar(200) NOT NULL, 
 	  OBJECT_OID varchar(200) NOT NULL, 
	  P$$OBJECT_parent__rid varchar(200), 
	  P$$OBJECT_parent__oid varchar(200),
	  OBJECT__VALID_FROM TIMESTAMP (6) WITH TIME ZONE, 
	  OBJECT__VALID_TO TIMESTAMP (6) WITH TIME ZONE, 
	  OBJECT__INVALIDATED_AT TIMESTAMP (6) WITH TIME ZONE, 
	  OBJECT__CLASS varchar(100), 
	  OBJECT__STATE_NUMBER integer, 
	  OBJECT__STATE_ID varchar(100), 
	  STATE_VALID_FROM DATE, 
	  STATE_VALID_TO DATE, 
	  SEGMENT varchar(200), 
	  P$$SEGMENT__rid varchar(200), 
	  P$$SEGMENT__oid varchar(200),
	  VALUE1 boolean, 
	  VALUE2 float,
	  VALUE4 float,
	  VALUE5 float, 
	  VALUE9 varchar(200), 
	  VALUE10 bytea, 
	  VALUE11A INTERVAL YEAR TO MONTH, 
	  VALUE11B INTERVAL DAY TO SECOND, 
	  VALUE6 varchar(200),
	  VALUE7 TIMESTAMP (6) WITH TIME ZONE, 
	  VALUE8 DATE,
	  COUNTRY varchar(4),
      CONSTRAINT PK_EXTENSION_NATIGVE PRIMARY KEY (OBJECT_RID, OBJECT_OID)
  ) ;
  CREATE TABLE EXTENSION_NATIVE_ (
    OBJECT_RID varchar(200) NOT NULL, 
 	  OBJECT_OID varchar(200) NOT NULL, 
	  OBJECT_IDX integer NOT NULL, 
	  VALUE3 float, 
      CONSTRAINT PK_EXTENSION_NATIVE_ PRIMARY KEY (OBJECT_RID, OBJECT_OID, OBJECT_IDX)
  ) ;

  CREATE TABLE EXTENSION_NUMERIC (	
    OBJECT_RID varchar(200) NOT NULL, 
	  OBJECT_OID varchar(200) NOT NULL, 
	  P$$OBJECT_parent__rid varchar(200) NOT NULL, 
	  P$$OBJECT_parent__oid varchar(200) NOT NULL,
	  OBJECT__CLASS varchar(100) NOT NULL, 
	  OBJECT__VALID_FROM decimal, 
	  OBJECT__VALID_TO decimal, 
	  OBJECT__INVALIDATED_AT decimal, 
	  OBJECT__STATE_NUMBER integer, 
	  OBJECT__STATE_ID varchar(100), 
	  SEGMENT varchar(200), 
	  P$$SEGMENT__rid varchar(200),
	  P$$SEGMENT__oid varchar(200),
	  VALUE1 integer, 
	  VALUE2 float, 
	  VALUE4 float, 
	  VALUE5 float, 
	  VALUE9 varchar(200), 
	  VALUE10 bytea, 
	  VALUE11A integer,
	  VALUE11B decimal,
	  VALUE6 varchar(200),
	  VALUE7 decimal,
	  VALUE8 varchar(8),
	  COUNTRY varchar(4), 
      CONSTRAINT PK_EXTENSION_NUMERIC PRIMARY KEY (OBJECT_RID, OBJECT_OID)
  ) ;
  CREATE TABLE EXTENSION_NUMERIC_ (	
      OBJECT_RID varchar(200) NOT NULL, 
	  OBJECT_OID varchar(200) NOT NULL, 
	  OBJECT_IDX integer NOT NULL, 
	  VALUE3 float,
	  CONSTRAINT PK_EXTENSION_NUMERIC_ PRIMARY KEY (OBJECT_RID, OBJECT_OID, OBJECT_IDX)
  ) ;

  CREATE TABLE EXTENSION_SEGMENT (
    OBJECT_RID varchar(200) NOT NULL,
	OBJECT_OID varchar(200) NOT NULL,
	OBJECT__CLASS varchar(200) NOT NULL,
	DESCRIPTION varchar(200),
    CONSTRAINT PK_EXTENSION_SEGMENT PRIMARY KEY (OBJECT_RID, OBJECT_OID)
  );

-- Test org::openmdx::generic1

  CREATE TABLE generic1_Property (
    object_rid varchar(100) NOT NULL,
    object_oid varchar(200) NOT NULL,
    p$$object_parent__rid varchar(100) NULL, 
    p$$object_parent__oid varchar(200) NULL,
    object__class varchar(200) NOT NULL ,
    description varchar(200) NULL ,
    created_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_at TIMESTAMP (6) WITH TIME ZONE NULL,
    modified_by_0 varchar(50) NULL ,
    created_by_0 varchar(50) NULL,
    CONSTRAINT PK_generic1_Property PRIMARY KEY (OBJECT_RID, OBJECT_OID)
  );
  CREATE TABLE generic1_Property_ (
    object_rid varchar(100) NOT NULL,
    object_oid varchar(200) NOT NULL,
    object_idx INTEGER NOT NULL ,
    boolean_value boolean NULL ,
    uri_value varchar(200) NULL ,
    decimal_value decimal NULL ,
    string_value varchar(200) NULL ,
    integer_value integer NULL,
    CONSTRAINT PK_generic1_Property_ PRIMARY KEY (OBJECT_RID, OBJECT_OID, object_idx) 
  );
