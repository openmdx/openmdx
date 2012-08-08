CREATE TABLE app1_DOC (
    object_referenceid integer NOT NULL ,
    object_objectid varchar (200) NOT NULL ,
    object_idx integer NOT NULL ,
    created_at varchar (20),
    created_by varchar (200),
    modified_at varchar (20),
    modified_by varchar (200),
    object__class varchar (60),
    description varchar (100),
    content bytea,
    keyword varchar (32) 
);

CREATE TABLE app1_GENERIC (
    object_referenceId integer NOT NULL ,
    object_objectId text NOT NULL ,
    object_name varchar (64) NOT NULL ,
    object_val_string text,
    object_val_numeric decimal(18, 6),
    object_val_binary bytea,
    object_idx int
);

CREATE TABLE app1_REF (
    object_referenceId integer NOT NULL,
    object_reference text NOT NULL ,
    n integer,
    "c$0" varchar (100),
    "c$1" varchar (100),
    "c$2" varchar (100),
    "c$3" varchar (100),
    "c$4" varchar (100),
    "c$5" varchar (100),
    "c$6" varchar (100),
    "c$7" varchar (100),
    "c$8" varchar (100),
    "c$9" varchar (100)
);

CREATE TABLE app1_SLICED (
    object_referenceId integer NOT NULL ,
    object_objectId varchar (200) NOT NULL ,
    object_idx integer NOT NULL ,
    created_at varchar (20),
    created_by varchar (200),
    modified_at varchar (20),
    modified_by varchar (200),
    object__class varchar (60),
    m1 varchar (200),
    last_name varchar (200),
    house_number varchar (200),
    city varchar (200),
    foreign_id varchar (200),
    postal_code varchar (200),
    description varchar (200),
    assigned_address varchar (200),
    product_id varchar (200),
    salutation varchar (200),
    street varchar (200),
    address_line varchar (200),
    address varchar (200),
    text varchar (200),
    birthdate varchar (200),
    place_of_birth varchar (200),
    member_of_group varchar (200),
    birthdate_as_date_time varchar (200),
    person_group varchar (200),
    country varchar (200),
    sex int,
    given_name varchar (200),
    product_group_id varchar (200)
);

CREATE TABLE audit_GENERIC (
    object_referenceId integer NOT NULL ,
    object_objectId varchar (200) NOT NULL ,
    object_name varchar (64) NOT NULL ,
    object_val_string text,
    object_val_numeric decimal(18, 6),
    object_val_binary bytea,
    object_idx int
);

CREATE TABLE audit_REF (
    object_referenceId integer NOT NULL ,
    object_reference text NOT NULL ,
    n integer,
    "c$0" varchar (100),
    "c$1" varchar (100),
    "c$2" varchar (100),
    "c$3" varchar (100),
    "c$4" varchar (100),
    "c$5" varchar (100),
    "c$6" varchar (100),
    "c$7" varchar (100),
    "c$8" varchar (100),
    "c$9" varchar (100)
);

CREATE TABLE audit_SLICED (
    object_referenceId integer NOT NULL ,
    object_objectId varchar (200) NOT NULL ,
    object_idx integer NOT NULL ,
    created_at varchar (20),
    created_by varchar (200),
    modified_at varchar (20),
    modified_by varchar (200),
    object__class varchar (60),
    m1 varchar (200),
    last_name varchar (200),
    house_number varchar (200),
    city varchar (200),
    foreign_id varchar (200),
    postal_code varchar (200),
    description varchar (200),
    assigned_address varchar (200),
    product_id varchar (200),
    salutation varchar (200),
    street varchar (200),
    address_line varchar (200),
    address varchar (200),
    text varchar (200),
    birthdate varchar (200),
    place_of_birth varchar (200),
    member_of_group varchar (200),
    birthdate_as_date_time varchar (200),
    person_group varchar (200),
    country varchar (200),
    sex int,
    given_name varchar (200),
    product_group_id varchar (200),
    involved varchar (200)
);

CREATE TABLE role1_GENERIC (
    object_referenceId integer NOT NULL ,
    object_objectId varchar (200) NOT NULL ,
    object_name varchar (64) NOT NULL ,
    object_val_string text,
    object_val_numeric decimal(18, 6),
    object_val_binary bytea,
    object_idx int
);

CREATE TABLE role1_REF (
    object_referenceId integer NOT NULL ,
    object_reference text NOT NULL ,
    n integer,
    "c$0" varchar (100),
    "c$1" varchar (100),
    "c$2" varchar (100),
    "c$3" varchar (100),
    "c$4" varchar (100),
    "c$5" varchar (100),
    "c$6" varchar (100),
    "c$7" varchar (100),
    "c$8" varchar (100),
    "c$9" varchar (100),
    "c$10" varchar (100),
    "c$11" varchar (100)
);

CREATE TABLE state1_GENERIC (
    object_referenceId integer NOT NULL ,
    object_objectId varchar (200) NOT NULL ,
    object_name varchar (64) NOT NULL ,
    object_val_string text,
    object_val_numeric decimal(18, 6),
    object_val_binary bytea,
    object_idx int
);

CREATE TABLE state1_REF (
    object_referenceId integer NOT NULL ,
    object_reference text NOT NULL ,
    n integer,
    "c$0" varchar (100),
    "c$1" varchar (100),
    "c$2" varchar (100),
    "c$3" varchar (100),
    "c$4" varchar (100),
    "c$5" varchar (100),
    "c$6" varchar (100),
    "c$7" varchar (100),
    "c$8" varchar (100),
    "c$9" varchar (100),
    "c$10" varchar (100),
    "c$11" varchar (100)
);

CREATE TABLE test_GENERIC (
    object_rid integer NOT NULL ,
    object_oid varchar (200) NOT NULL ,
    object_name varchar (64) NOT NULL ,
    object_val_string text,
    object_val_numeric decimal(18, 6),
    object_val_binary bytea,
    object_idx int
);

CREATE TABLE test_REF (
    object_rid integer NOT NULL ,
    object_reference text NOT NULL ,
    n integer,
    "c$0" varchar (100),
    "c$1" varchar (100),
    "c$2" varchar (100),
    "c$3" varchar (100),
    "c$4" varchar (100),
    "c$5" varchar (100),
    "c$6" varchar (100),
    "c$7" varchar (100),
    "c$8" varchar (100),
    "c$9" varchar (100)
);

CREATE TABLE test_SLB_GENERIC (
    object_rid integer NOT NULL ,
    object_oid varchar (200) NOT NULL ,
    object_name varchar (64) NOT NULL ,
    object_val_string text,
    object_val_numeric decimal(18, 6),
    object_val_binary bytea,
    object_idx int
);

CREATE TABLE test_SLB_SLICED (
    object_rid integer NOT NULL ,
    object_oid varchar (200) NOT NULL ,
    object_idx integer NOT NULL ,
    created_at varchar (20),
    created_by varchar (200),
    modified_at varchar (20),
    modified_by varchar (200),
    object__class varchar (60),
    slb_type varchar (10),
    pos text,
    price decimal(18, 9),
    is_debit decimal(18, 9),
    is_long decimal(18, 9),
    price_currency varchar (10),
    value_date varchar (20),
    booking_date varchar (20),
    quantity decimal(18, 9),
    quantity_absolute decimal(18, 9),
    visibility varchar (10),
    admin_descr varchar (100),
    description varchar (100),
    cred_value bytea,
    "p$$object_parent__oid" varchar (20),
    "p$$object_parent__rid" integer,
    "p$$pos__oid" varchar (20),
    "p$$pos__rid" integer,
    "p$$pos_parent__oid" varchar (50),
    "p$$pos_parent__rid" integer
);

CREATE TABLE TEST_CB_SLICED (
    OBJECT_RID     integer NOT NULL,
    OBJECT_IDX     INTEGER NOT NULL,
    CREATED_AT     VARCHAR(20),
    CREATED_BY     VARCHAR(200),
    MODIFIED_AT    VARCHAR(20),
    MODIFIED_BY    VARCHAR(200),
    OBJECT__CLASS  VARCHAR(60),
    CB_TYPE        VARCHAR(10),
    CANCELS_C_B    VARCHAR(200),
    ADVICE_TEXT    TEXT,
    OBJECT_OID     VARCHAR(120) NOT NULL
);
