drop table app1_Address
;

drop table app1_Address_N
;

drop table app1_DOC
;

drop table app1_Invoice
;

drop table app1_InvoicePosition
;

drop table app1_Member
;

drop table app1_PersonGroup
;

drop table app1_REF
;

drop table app1_SLICED
;

drop table app1_Segment
;

drop table audit1_Segment
;

drop table audit_REF
;

drop table audit_SLICED
;

drop table role1_REF
;

drop table role1_RoleType
;

drop table role1_SLICED
;

drop table role1_Segment
;

drop table state1_SLICED
;

drop table test_CB_SLICED
;

drop table test_REF
;

drop table test_SLB_SLICED
;

drop table app1_MessageTemplate
;

drop table state1_Extension
;

drop table state1_Standard
;

CREATE TABLE app1_Address (
	object_referenceid INTEGER NOT NULL ,
	object_objectid VARCHAR2 (200) NOT NULL ,
	object__class VARCHAR2 (200) NULL ,
	description VARCHAR2 (200) NULL ,
	created_at VARCHAR2 (20) NULL ,
	modified_at VARCHAR2 (20) NULL ,
	postal_code VARCHAR2 (100) NULL ,
	street VARCHAR2 (100) NULL ,
	country VARCHAR2 (100) NULL ,
	city VARCHAR2 (100) NULL ,
	house_number VARCHAR2 (100) NULL ,
	address VARCHAR2 (100) NULL 
)
;

CREATE TABLE app1_Address_N (
	object_referenceid INTEGER NOT NULL ,
	object_objectid VARCHAR2 (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	modified_by VARCHAR2 (50) NULL ,
	created_by VARCHAR2 (50) NULL ,
	address_line VARCHAR2 (100) NULL
)
;

CREATE TABLE app1_DOC (
	object_referenceid INTEGER NOT NULL ,
	object_objectid VARCHAR2 (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	created_at VARCHAR2 (20) NULL ,
	created_by VARCHAR2 (200) NULL ,
	modified_at VARCHAR2 (20) NULL ,
	modified_by VARCHAR2 (200) NULL ,
	object__class VARCHAR2 (60) NULL ,
	description VARCHAR2 (100) NULL ,
	content BLOB NULL ,
	keyword VARCHAR2 (32) NULL 
)
;

CREATE TABLE app1_Invoice (
	object_referenceid INTEGER NOT NULL ,
	object_objectid VARCHAR2 (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	object__class VARCHAR2 (200) NULL ,
	description VARCHAR2 (200) NULL ,
	product_group_id VARCHAR2 (100) NULL ,
	created_at VARCHAR2 (20) NULL ,
	modified_at VARCHAR2 (20) NULL ,
	created_by VARCHAR2 (100) NULL ,
	modified_by VARCHAR2 (100) NULL 
)
;

CREATE TABLE app1_InvoicePosition (
	object_referenceid INTEGER NOT NULL ,
	object_objectid VARCHAR2 (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	object__class VARCHAR2 (200) NULL ,
	description VARCHAR2 (200) NULL ,
	created_at VARCHAR2 (20) NULL ,
	modified_at VARCHAR2 (20) NULL ,
	created_by VARCHAR2 (100) NULL ,
	modified_by VARCHAR2 (100) NULL ,
	product_id VARCHAR2 (50) NULL 
)
;

CREATE TABLE app1_Member (
	object_referenceid INTEGER NOT NULL ,
	object_objectid VARCHAR2 (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	object__class VARCHAR2 (200) NULL ,
	description VARCHAR2 (200) NULL ,
	modified_at VARCHAR2 (20) NULL ,
	created_at VARCHAR2 (20) NULL ,
	modified_by VARCHAR2 (50) NULL ,
	created_by VARCHAR2 (50) NULL ,
	m1 VARCHAR2 (200) NULL ,
	m2 VARCHAR2 (200) NULL 
)
;

CREATE TABLE app1_PersonGroup (
	object_referenceid INTEGER NOT NULL ,
	object_objectid VARCHAR2 (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	object__class VARCHAR2 (200) NULL ,
	description VARCHAR2 (200) NULL ,
	modified_at VARCHAR2 (20) NULL ,
	created_at VARCHAR2 (20) NULL ,
	modified_by VARCHAR2 (50) NULL ,
	created_by VARCHAR2 (50) NULL ,
	name VARCHAR2 (100) NULL 
)
;

CREATE TABLE app1_REF (
	object_referenceId INTEGER NOT NULL ,
	n INTEGER NOT NULL ,
	c$0 VARCHAR2 (100) NOT NULL ,
	c$1 VARCHAR2 (100) NOT NULL ,
	c$2 VARCHAR2 (100) NOT NULL ,
	c$3 VARCHAR2 (100) NOT NULL ,
	c$4 VARCHAR2 (100) NOT NULL ,
	c$5 VARCHAR2 (100) NOT NULL ,
	c$6 VARCHAR2 (100) NOT NULL ,
	c$7 VARCHAR2 (100) NOT NULL ,
	c$8 VARCHAR2 (100) NOT NULL ,
	c$9 VARCHAR2 (100) NOT NULL ,
	c$10 VARCHAR2 (100) NOT NULL ,
	c$11 VARCHAR2 (100) NOT NULL ,
	c$12 VARCHAR2 (100) NOT NULL ,
	c$13 VARCHAR2 (100) NOT NULL ,
	c$14 VARCHAR2 (100) NOT NULL ,
	c$15 VARCHAR2 (100) NOT NULL 
)
;

DROP SEQUENCE app1_REF_SEQ;
CREATE SEQUENCE app1_REF_SEQ INCREMENT BY 1 START WITH 100000 MAXVALUE 1000000000 MINVALUE 1 NOCYCLE CACHE 100 NOORDER;

CREATE TABLE app1_SLICED (
	object_referenceId INTEGER NOT NULL ,
	object_objectId VARCHAR2 (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	created_at VARCHAR2 (20) NULL ,
	created_by VARCHAR2 (200) NULL ,
	modified_at VARCHAR2 (20) NULL ,
	modified_by VARCHAR2 (200) NULL ,
	object__class VARCHAR2 (60) NULL ,
	m1 VARCHAR2 (200) NULL ,
	last_name VARCHAR2 (200) NULL ,
	house_number VARCHAR2 (200) NULL ,
	city VARCHAR2 (200) NULL ,
	foreign_id VARCHAR2 (200) NULL ,
	postal_code VARCHAR2 (200) NULL ,
	description VARCHAR2 (200) NULL ,
	assigned_address VARCHAR2 (200) NULL ,
	product_id VARCHAR2 (200) NULL ,
	salutation VARCHAR2 (200) NULL ,
	street VARCHAR2 (200) NULL ,
	address_line VARCHAR2 (200) NULL ,
	address VARCHAR2 (200) NULL ,
	text VARCHAR2 (200) NULL ,
	birthdate VARCHAR2 (8) NULL ,
	member_of_group VARCHAR2 (200) NULL ,
	birthdate_as_date_time VARCHAR2 (200) NULL ,
	person_group VARCHAR2 (200) NULL ,
	country VARCHAR2 (200) NULL ,
	sex INTEGER NULL ,
	given_name VARCHAR2 (200) NULL ,
	product_group_id VARCHAR2 (200) NULL ,
	place_of_birth VARCHAR2 (200) NULL 
)
;

CREATE TABLE app1_Segment (
	object_referenceid INTEGER NOT NULL ,
	object_objectid VARCHAR2 (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	object__class VARCHAR2 (200) NULL ,
	description VARCHAR2 (200) NULL 
)
;

CREATE TABLE audit1_Segment (
	object_referenceid INTEGER NOT NULL ,
	object_objectid VARCHAR2 (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	object__class VARCHAR2 (200) NULL ,
	description VARCHAR2 (200) NULL
)
;

CREATE TABLE audit_REF (
	object_referenceId INTEGER NOT NULL ,
	n INTEGER NOT NULL ,
	c$0 VARCHAR2 (100) NOT NULL ,
	c$1 VARCHAR2 (100) NOT NULL ,
	c$2 VARCHAR2 (100) NOT NULL ,
	c$3 VARCHAR2 (100) NOT NULL ,
	c$4 VARCHAR2 (100) NOT NULL ,
	c$5 VARCHAR2 (100) NOT NULL ,
	c$6 VARCHAR2 (100) NOT NULL ,
	c$7 VARCHAR2 (100) NOT NULL ,
	c$8 VARCHAR2 (100) NOT NULL ,
	c$9 VARCHAR2 (100) NOT NULL ,
	c$10 VARCHAR2 (100) NOT NULL ,
	c$11 VARCHAR2 (100) NOT NULL ,
	c$12 VARCHAR2 (100) NOT NULL ,
	c$13 VARCHAR2 (100) NOT NULL ,
	c$14 VARCHAR2 (100) NOT NULL ,
	c$15 VARCHAR2 (100) NOT NULL 
)
;

DROP SEQUENCE audit_REF_SEQ;
CREATE SEQUENCE audit_REF_SEQ INCREMENT BY 1 START WITH 100000 MAXVALUE 1000000000 MINVALUE 1 NOCYCLE CACHE 100 NOORDER;

CREATE TABLE audit_SLICED (
	object_referenceId INTEGER NOT NULL ,
	object_objectId VARCHAR2 (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	created_at VARCHAR2 (20) NULL ,
	created_by VARCHAR2 (200) NULL ,
	modified_at VARCHAR2 (20) NULL ,
	modified_by VARCHAR2 (200) NULL ,
	object__class VARCHAR2 (60) NULL ,
	m1 VARCHAR2 (200) NULL ,
	last_name VARCHAR2 (200) NULL ,
	house_number VARCHAR2 (200) NULL ,
	city VARCHAR2 (200) NULL ,
	foreign_id VARCHAR2 (200) NULL ,
	postal_code VARCHAR2 (200) NULL ,
	description VARCHAR2 (200) NULL ,
	assigned_address VARCHAR2 (200) NULL ,
	product_id VARCHAR2 (200) NULL ,
	salutation VARCHAR2 (200) NULL ,
	street VARCHAR2 (200) NULL ,
	address_line VARCHAR2 (200) NULL ,
	address VARCHAR2 (200) NULL ,
	text VARCHAR2 (200) NULL ,
	birthdate VARCHAR2 (200) NULL ,
	member_of_group VARCHAR2 (200) NULL ,
	birthdate_as_date_time VARCHAR2 (200) NULL ,
	person_group VARCHAR2 (200) NULL ,
	country VARCHAR2 (200) NULL ,
	sex INTEGER NULL ,
	given_name VARCHAR2 (200) NULL ,
	product_group_id VARCHAR2 (200) NULL ,
	involved VARCHAR2 (200) NULL ,
	place_of_birth VARCHAR2 (200) NULL
)
;

CREATE TABLE role1_REF (
	object_referenceId INTEGER NOT NULL ,
	n INTEGER NOT NULL ,
	c$0 VARCHAR2 (100) NOT NULL ,
	c$1 VARCHAR2 (100) NOT NULL ,
	c$2 VARCHAR2 (100) NOT NULL ,
	c$3 VARCHAR2 (100) NOT NULL ,
	c$4 VARCHAR2 (100) NOT NULL ,
	c$5 VARCHAR2 (100) NOT NULL ,
	c$6 VARCHAR2 (100) NOT NULL ,
	c$7 VARCHAR2 (100) NOT NULL ,
	c$8 VARCHAR2 (100) NOT NULL ,
	c$9 VARCHAR2 (100) NOT NULL ,
	c$10 VARCHAR2 (100) NOT NULL ,
	c$11 VARCHAR2 (100) NOT NULL ,
	c$12 VARCHAR2 (100) NOT NULL ,
	c$13 VARCHAR2 (100) NOT NULL ,
	c$14 VARCHAR2 (100) NOT NULL ,
	c$15 VARCHAR2 (100) NOT NULL 
)
;

DROP SEQUENCE role1_REF_SEQ;
CREATE SEQUENCE role1_REF_SEQ INCREMENT BY 1 START WITH 100000 MAXVALUE 1000000000 MINVALUE 1 NOCYCLE CACHE 100 NOORDER;

CREATE TABLE role1_RoleType (
	object_referenceid INTEGER NOT NULL ,
	object_objectid VARCHAR2 (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	object__class VARCHAR2 (200) NULL ,
	created_at VARCHAR2 (20) NULL ,
	modified_at VARCHAR2 (20) NULL ,
	created_by VARCHAR2 (100) NULL ,
	modified_by VARCHAR2 (100) NULL ,
	description VARCHAR2 (200) NULL ,
	core_role VARCHAR2 (20) NULL ,
	name VARCHAR2 (50) NULL ,
	aaaa VARCHAR2 (50) NULL ,
	an_u_r_i VARCHAR2 (200) NULL 
)
;

CREATE TABLE role1_SLICED (
	object_referenceId INTEGER NOT NULL ,
	object_objectId VARCHAR2 (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	created_at VARCHAR2 (20) NULL ,
	created_by VARCHAR2 (200) NULL ,
	modified_at VARCHAR2 (20) NULL ,
	modified_by VARCHAR2 (200) NULL ,
	object__class VARCHAR2 (100) NULL ,
	rara1$rcrara_name VARCHAR2 (60) NULL ,
	tfira1$object_class VARCHAR2 (100) NULL ,
	tfira2$object_class VARCHAR2 (60) NULL ,
	rara1$gggg VARCHAR2 (60) NULL ,
	tfira2$dddd VARCHAR2 (60) NULL ,
	tfira2$ident VARCHAR2 (60) NULL ,
	grp_ra$rcra_name VARCHAR2 (60) NULL ,
	rce_name VARCHAR2 (60) NULL ,
	tfira1$rcra_name VARCHAR2 (60) NULL ,
	ra1$dddd VARCHAR2 (60) NULL ,
	role4$object_class VARCHAR2 (100) NULL ,
	id_r_t_ara1$dddd VARCHAR2 (60) NULL ,
	"_clerk$business_phone" VARCHAR2 (60) NULL ,
	id_r_t_ara2$dddd VARCHAR2 (60) NULL ,
	tfira0$rcra_name VARCHAR2 (60) NULL ,
	tfira0$object_class VARCHAR2 (100) NULL ,
	grp_ra$object_class VARCHAR2 (100) NULL ,
	role_c_r1$object_class VARCHAR2 (100) NULL ,
	role2$object_class VARCHAR2 (100) NULL ,
	role2$ident VARCHAR2 (60) NULL ,
	role_c_r1$dddd VARCHAR2 (60) NULL ,
	id_multira$object_class VARCHAR2 (100) NULL ,
	id_r_t_ara1$object_class VARCHAR2 (100) NULL ,
	extra_role$ident VARCHAR2 (60) NULL ,
	role3$object_class VARCHAR2 (100) NULL ,
	role3$rcrb_name VARCHAR2 (60) NULL ,
	role_c_r2$eeee VARCHAR2 (60) NULL ,
	id_multirb$object_class VARCHAR2 (100) NULL ,
	"_manager$object_class" VARCHAR2 (100) NULL ,
	core_role VARCHAR2 (60) NULL ,
	"_manager$business_phone" VARCHAR2 (60) NULL ,
	extra_role$object_class VARCHAR2 (100) NULL ,
	sgrp_rb$no_role VARCHAR2 (60) NULL ,
	id_r_t_ara2$object_class VARCHAR2 (100) NULL ,
	role4$rcrara_name VARCHAR2 (60) NULL ,
	sgrp_rb$ffff VARCHAR2 (60) NULL ,
	role1$object_class VARCHAR2 (100) NULL ,
	role_c_r1$ident VARCHAR2 (60) NULL ,
	grp_ra$dddd VARCHAR2 (60) NULL ,
	rara1$object_class VARCHAR2 (100) NULL ,
	id_r_t_ara2$ident VARCHAR2 (60) NULL ,
	grp_ra$group VARCHAR2 (60) NULL ,
	aaaa VARCHAR2 (60) NULL ,
	role_c_r1$rcra_name VARCHAR2 (60) NULL ,
	bbbb VARCHAR2 (60) NULL ,
	id_r_t_arara1$rcrara_name VARCHAR2 (60) NULL ,
	role_c_r2$dddd VARCHAR2 (60) NULL ,
	id_multirb$ident VARCHAR2 (60) NULL ,
	role1$ident VARCHAR2 (60) NULL ,
	name VARCHAR2 (60) NULL ,
	"_clerk$object_class" VARCHAR2 (100) NULL ,
	tfira2$rcra_name VARCHAR2 (60) NULL ,
	"_clerk$division" VARCHAR2 (60) NULL ,
	extra_role$rcra_name VARCHAR2 (60) NULL ,
	id_r_t_arara1$gggg VARCHAR2 (60) NULL ,
	role_c_r2$rcra_name VARCHAR2 (60) NULL ,
	role_c_r2$object_class VARCHAR2 (100) NULL ,
	ra1$ident VARCHAR2 (60) NULL ,
	id_r_t_ara1$ident VARCHAR2 (60) NULL ,
	role1$rcraName VARCHAR2 (60) NULL ,
	id_r_t_ara1$rcra_name VARCHAR2 (60) NULL ,
	role_b_id VARCHAR2 (60) NULL ,
	birthdate VARCHAR2 (60) NULL ,
	tfira0$ident VARCHAR2 (60) NULL ,
	role_c_r2$rcrae_name VARCHAR2 (60) NULL ,
	tfira1$dddd VARCHAR2 (60) NULL ,
	id_multira$rcra_name VARCHAR2 (60) NULL ,
	ra1$object_class VARCHAR2 (60) NULL ,
	id_r_t_ara2$rcra_name VARCHAR2 (60) NULL ,
	id_multirb$rcra_name VARCHAR2 (60) NULL ,
	rc_name VARCHAR2 (60) NULL ,
	role2$rcra_name VARCHAR2 (60) NULL ,
	extra_role$dddd VARCHAR2 (60) NULL ,
	id_multira$dddd VARCHAR2 (60) NULL ,
	"_manager$division" VARCHAR2 (60) NULL ,
	id_r_t_arara1$object_class VARCHAR2 (100) NULL ,
	role_c_r2$ident VARCHAR2 (60) NULL ,
	id_multira$ident VARCHAR2 (60) NULL ,
	grp_ra$ident VARCHAR2 (60) NULL ,
	id_multirb$dddd VARCHAR2 (60) NULL ,
	sgrp_rb$rcrb_name VARCHAR2 (60) NULL ,
	tfira0$dddd VARCHAR2 (60) NULL ,
	sgrp_rb$object_class VARCHAR2 (100) NULL ,
	ra1$rcraName VARCHAR2 (60) NULL ,
	cccc VARCHAR2 (60) NULL ,
	tfira1$ident VARCHAR2 (60) NULL ,
	role_a_s1$rcra_name VARCHAR2 (60) NULL ,
	role_a_s1$object__class VARCHAR2 (100) NULL ,
	role_a_s1$ident VARCHAR2 (60) NULL ,
	role_a_s1$dddd VARCHAR2 (60) NULL ,
	role_b_f2$rcra_name VARCHAR2 (60) NULL ,
	role_b_f2$object__class VARCHAR2 (100) NULL ,
	role_b_f2$ident VARCHAR2 (60) NULL ,
	role_b_f2$eeee VARCHAR2 (60) NULL ,
	role_b_f2$dddd VARCHAR2 (60) NULL ,
	role_b_f2$rcrae_name VARCHAR2 (60) NULL ,
	role_b_f3$object__class VARCHAR2 (100) NULL ,
	role_c_r1$object__class VARCHAR2 (100) NULL ,
	role_c_r2$object__class VARCHAR2 (100) NULL ,
	role_c_r3$object__class VARCHAR2 (100) NULL ,
	role_c_r3$rcrb_name VARCHAR2 (60) NULL ,
	role_c_r3$rcrb_u_r_i VARCHAR2 (256) NULL ,
	role_c_r3$ffff VARCHAR2 (60) NULL ,
	non_exist$object__class VARCHAR2 (100) NULL ,
	role_r_a_r2$rcra_name VARCHAR2 (60) NULL ,
	role_r_a_r2$object__class VARCHAR2 (100) NULL ,
	role_r_a_r2$ident VARCHAR2 (60) NULL ,
	role_r_a_r2$dddd VARCHAR2 (60) NULL ,
	role_r_a_r2$eeee VARCHAR2 (60) NULL ,
	role_r_a_r2$rcrae_name VARCHAR2 (60) NULL ,
	role_r_a_r5$rcraera_name VARCHAR2 (60) NULL ,
	role_r_a_r5$object__class VARCHAR2 (100) NULL ,
	role_r_a_r5$hhhh VARCHAR2 (60) NULL ,
	role_e_x_c3$object__class VARCHAR2 (100) NULL ,
	role_e_x_c3$rcrb_name VARCHAR2 (60) NULL ,
	role_e_x_c3$ffff VARCHAR2 (60) NULL ,
	role_e_x_c3$rcrb_u_r_i VARCHAR2 (256) NULL ,
	r1$rcra_name VARCHAR2 (60) NULL ,
	r1$object__class VARCHAR2 (100) NULL ,
	r1$ident VARCHAR2 (60) NULL ,
	r1$dddd VARCHAR2 (60) NULL ,
	r2$object__class VARCHAR2 (100) NULL ,
	r2$rcrara_name VARCHAR2 (60) NULL ,
	r2$gggg VARCHAR2 (60) NULL ,
	role11$rcra_name VARCHAR2 (60) NULL ,
	role11$object__class VARCHAR2 (100) NULL ,
	role11$ident VARCHAR2 (60) NULL ,
	role11$dddd VARCHAR2 (60) NULL ,
	role12$object__class VARCHAR2 (100) NULL ,
	role12$rcrb_u_r_i VARCHAR2 (256) NULL ,
	role12$rcrb_name VARCHAR2 (60) NULL ,
	role12$ffff VARCHAR2 (60) NULL ,
	role_t_c1$object__class VARCHAR2 (100) NULL ,
	role_t_c1$rcrb_name VARCHAR2 (60) NULL ,
	role_t_c1$ffff VARCHAR2 (60) NULL ,
	role_t_c1$rcrb_u_r_i VARCHAR2 (256) NULL ,
	ra1$rcra_name VARCHAR2 (60) NULL ,
	ra1$object__class VARCHAR2 (100) NULL ,
	rara1$object__class VARCHAR2 (100) NULL ,
	tfira0$object__class VARCHAR2 (100) NULL ,
	tfira1$object__class VARCHAR2 (100) NULL ,
	tfira2$object__class VARCHAR2 (100) NULL ,
	extra_role$object__class VARCHAR2 (100) NULL ,
	id_multira$object__class VARCHAR2 (100) NULL ,
	id_multirb$object__class VARCHAR2 (100) NULL ,
	grp__ra$rcra_name VARCHAR2 (60) NULL ,
	grp__ra$object__class VARCHAR2 (100) NULL ,
	grp__ra$ident VARCHAR2 (60) NULL ,
	grp__ra$dddd VARCHAR2 (60) NULL ,
	grp__ra$group VARCHAR2 (256) NULL ,
	sgrp__rb$object__class VARCHAR2 (100) NULL ,
	sgrp__rb$ffff VARCHAR2 (60) NULL ,
	sgrp__rb$rcrb_name VARCHAR2 (60) NULL ,
	sgrp__rb$no_role VARCHAR2 (256) NULL ,
	role1$rcra_name VARCHAR2 (60) NULL ,
	role1$object__class VARCHAR2 (100) NULL ,
	role2$object__class VARCHAR2 (100) NULL ,
	role3$object__class VARCHAR2 (100) NULL ,
	role4$object__class VARCHAR2 (100) NULL ,
	id_r_t_ara1$object__class VARCHAR2 (100) NULL ,
	id_r_t_arara1$object__clas VARCHAR2 (100) NULL ,
	id_r_t_arara1$object__class VARCHAR2 (100) NULL ,
	id_r_t_ara2$object__class VARCHAR2 (100) NULL ,
	id_r_t_q_ara1$rcra_name VARCHAR2 (60) NULL ,
	id_r_t_q_ara1$object__class VARCHAR2 (100) NULL ,
	id_r_t_q_ara1$ident VARCHAR2 (60) NULL ,
	id_r_t_q_ara1$dddd VARCHAR2 (60) NULL ,
	id_r_t_q_ara2$rcra_name VARCHAR2 (60) NULL ,
	id_r_t_q_ara2$object__class VARCHAR2 (100) NULL ,
	id_r_t_q_ara2$ident VARCHAR2 (60) NULL ,
	id_r_t_q_ara2$dddd VARCHAR2 (60) NULL ,
	id_r_t_q_arb1$object__class VARCHAR2 (100) NULL ,
	id_r_t_q_arb1$rcrb_name VARCHAR2 (60) NULL ,
	id_r_t_q_arb2$object__class VARCHAR2 (100) NULL ,
	id_r_t_q_arb2$rcrb_name VARCHAR2 (60) NULL ,
	"_clerk$object__class" VARCHAR2 (50) NULL ,
	"_manager$object__class" VARCHAR2 (50) NULL ,
	"_customer$object__class" VARCHAR2 (50) NULL ,
	"_customer$total_purchase" NUMBER(18, 0) NULL ,
	rcd_name VARCHAR2 (100) NULL ,
	kkkk VARCHAR2 (100) NULL ,
	"_drole_a$object__class" VARCHAR2 (100) NULL ,
	"_drole_a$rcdra_name" VARCHAR2 (100) NULL ,
	"_drole_a$mmmm" VARCHAR2 (100) NULL ,
	ahv$register_id VARCHAR2 (100) NULL ,
	ahv$object__class VARCHAR2 (100) NULL 
)
;

CREATE TABLE role1_Segment (
	object_referenceid INTEGER NOT NULL ,
	object_objectid VARCHAR2 (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	object__class VARCHAR2 (200) NULL ,
	description VARCHAR2 (200) NULL 
)
;

CREATE TABLE test_CB_SLICED (
	object_rid INTEGER NOT NULL ,
	object_oid VARCHAR2 (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	created_at VARCHAR2 (20) NULL ,
	created_by VARCHAR2 (200) NULL ,
	modified_at VARCHAR2 (20) NULL ,
	modified_by VARCHAR2 (200) NULL ,
	object__class VARCHAR2 (60) NULL ,
	cb_type VARCHAR2 (10) NULL ,
	advice_text VARCHAR2 (200) NULL ,
	cancels_c_b VARCHAR2 (512) NULL 
)
;

CREATE TABLE test_REF (
	object_rid INTEGER NOT NULL ,
	n INTEGER NOT NULL ,
	c$0 VARCHAR2 (100) NOT NULL ,
	c$1 VARCHAR2 (100) NOT NULL ,
	c$2 VARCHAR2 (100) NOT NULL ,
	c$3 VARCHAR2 (100) NOT NULL ,
	c$4 VARCHAR2 (100) NOT NULL ,
	c$5 VARCHAR2 (100) NOT NULL ,
	c$6 VARCHAR2 (100) NOT NULL ,
	c$7 VARCHAR2 (100) NOT NULL ,
	c$8 VARCHAR2 (100) NOT NULL ,
	c$9 VARCHAR2 (100) NOT NULL ,
	c$10 VARCHAR2 (100) NOT NULL ,
	c$11 VARCHAR2 (100) NOT NULL ,
	c$12 VARCHAR2 (100) NOT NULL ,
	c$13 VARCHAR2 (100) NOT NULL ,
	c$14 VARCHAR2 (100) NOT NULL ,
	c$15 VARCHAR2 (100) NOT NULL 
)
;

DROP SEQUENCE test_REF_SEQ;
CREATE SEQUENCE test_REF_SEQ INCREMENT BY 1 START WITH 100000 MAXVALUE 1000000000 MINVALUE 1 NOCYCLE CACHE 100 NOORDER;

CREATE TABLE test_SLB_SLICED (
	object_rid INTEGER NOT NULL ,
	object_oid VARCHAR2 (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	created_at VARCHAR2 (20) NULL ,
	created_by VARCHAR2 (200) NULL ,
	modified_at VARCHAR2 (20) NULL ,
	modified_by VARCHAR2 (200) NULL ,
	object__class VARCHAR2 (60) NULL ,
	slb_type VARCHAR2 (10) NULL ,
	pos VARCHAR2 (512) NULL ,
	price NUMBER(18, 9) NULL ,
	is_debit NUMBER(18, 9) NULL ,
	is_long NUMBER(18, 9) NULL ,
	price_currency VARCHAR2 (10) NULL ,
	value_date VARCHAR2 (20) NULL ,
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
)
;

CREATE TABLE app1_MessageTemplate (
	object_referenceid INTEGER NOT NULL ,
	object_objectid VARCHAR2 (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	object__class VARCHAR2 (200) NULL ,
	description VARCHAR2 (200) NULL ,
	created_at VARCHAR2 (20) NULL ,
	modified_at VARCHAR2 (20) NULL ,
	created_by VARCHAR2 (100) NULL ,
	modified_by VARCHAR2 (100) NULL
)
;

CREATE TABLE state1_SLICED (
	object_rid VARCHAR2 (200) NOT NULL ,
	object_oid VARCHAR2 (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	created_at VARCHAR2 (20) NULL ,
	created_by VARCHAR2 (200) NULL ,
	modified_at VARCHAR2 (20) NULL ,
	modified_by VARCHAR2 (200) NULL ,
	object__valid_from VARCHAR2 (20) NULL ,
	object__valid_to VARCHAR2 (20) NULL ,
	object__invalidated_at VARCHAR2 (20) NULL ,
	object__class VARCHAR2 (100) NULL ,
	object__state_number NUMBER(10, 0) NULL ,
	object__state_id VARCHAR2 (100) NULL ,
	p$$object_parent__rid VARCHAR2 (200) NULL ,
	p$$object_parent__oid VARCHAR2 (256) NULL ,
	state_attr VARCHAR2 (60) NULL ,
	state_a VARCHAR2 (256) NULL ,
	value VARCHAR2 (60) NULL ,
	num VARCHAR2 (60) NULL ,
	state_a_derived VARCHAR2 (60) NULL ,
	value_a_derived VARCHAR2 (60) NULL ,
	multi_attr VARCHAR2 (60) NULL ,
	a1 VARCHAR2 (60) NULL ,
	a2 NUMBER(10, 0) NULL ,
	rtr__r_a__$object__class VARCHAR2 (60) NULL ,
	rtr__r_a__$name VARCHAR2 (60) NULL ,
	rtr__r_a__$num VARCHAR2 (60) NULL ,
	role1211$object__class VARCHAR2 (60) NULL ,
	role1211$name VARCHAR2 (60) NULL ,
	role1211$num VARCHAR2 (60) NULL ,
	r1$object__class VARCHAR2 (60) NULL ,
	r1$name VARCHAR2 (60) NULL ,
	r1$num VARCHAR2 (60) NULL ,
	role2$kl$object__class VARCHAR2 (60) NULL ,
	role2$kl$name VARCHAR2 (60) NULL ,
	role2$kl$num VARCHAR2 (60) NULL ,
	"_c_r__rt1$object__class" VARCHAR2 (60) NULL ,
	"_c_r__rt1$name" VARCHAR2 (60) NULL ,
	"_c_r__rt1$num" VARCHAR2 (60) NULL ,
	"_r_r__rt1$object__class" VARCHAR2 (60) NULL ,
	"_r_r__rt1$name" VARCHAR2 (60) NULL ,
	"_r_r__rt1$num" VARCHAR2 (60) NULL ,
	name VARCHAR2 (60) NULL ,
	aaaa VARCHAR2 (60) NULL ,
	rc_name VARCHAR2 (60) NULL ,
	bbbb VARCHAR2 (60) NULL ,
	role_b_id VARCHAR2 (60) NULL ,
	core_role VARCHAR2 (60) NULL ,
	long_values INTEGER NULL ,
	any_u_r_i_val VARCHAR2 (200) NULL ,
	boolean_val VARCHAR2 (10) NULL ,
	date_val VARCHAR2 (20) NULL ,
	date_time_val VARCHAR2 (20) NULL ,
	decimal_val NUMBER(18, 6) NULL ,
	duration_val VARCHAR2 (10) NULL ,
	integer_val NUMBER(10, 0) NULL ,
	long_val INTEGER NULL ,
	short_val NUMBER(10, 0) NULL ,
	special_non_stated VARCHAR2 (256) NULL,
	p$$special_non_stated__rid VARCHAR2 (256) NULL, 
	p$$special_non_stated__oid VARCHAR2 (256) NULL
)
;

CREATE TABLE state1_Standard (
	object_rid VARCHAR2 (200) NOT NULL ,
	object_oid VARCHAR2 (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	object__valid_from VARCHAR2(20) ,
	object__valid_to VARCHAR2(20) ,
	object__invalidated_at VARCHAR2(20) ,
	object__class VARCHAR2 (100) NULL ,
	object__state_number NUMBER (10, 0) NULL ,
	object__state_id VARCHAR2 (100) NULL ,
	segment VARCHAR2 (200) NULL ,
	value1 CHARACTER (1) NULL,
	value2 NUMBER NULL,
	value3 NUMBER NULL,
	value4 NUMBER NULL,
	value5 NUMBER NULL,
	value9 VARCHAR2 (200) NULL,
	value10 RAW (2000) NULL,
	value11 VARCHAR2 (20) NULL,
	p$$value11_months NUMBER(9,0) NULL,
	p$$value11_seconds NUMBER(12,3) NULL
)
;

CREATE TABLE state1_Extension (
	object_rid VARCHAR2 (200) NOT NULL ,
	object_oid VARCHAR2 (200) NOT NULL ,
	object_idx INTEGER NOT NULL, 
  P$$OBJECT_parent__oid VARCHAR2(200), 
	P$$OBJECT_parent__rid VARCHAR2(200) ,
	object__valid_from TIMESTAMP (3) WITH TIME ZONE NULL ,
	object__valid_to TIMESTAMP (3) WITH TIME ZONE NULL ,
	object__invalidated_at TIMESTAMP (3) WITH TIME ZONE NULL ,
	object__class VARCHAR2 (100) NULL ,
	object__state_number NUMBER (10, 0) NULL ,
	object__state_id VARCHAR2 (100) NULL ,
	state_valid_from DATE,
	state_valid_to DATE,
	segment VARCHAR2 (200) NULL ,
	value1 CHARACTER (1) NULL,
	value2 NUMBER NULL,
	value3 NUMBER NULL,
	value4 NUMBER NULL,
	value5 NUMBER NULL,
	value9 VARCHAR2 (200) NULL,
	value10 RAW (2000) NULL,
	value11 VARCHAR2 (20) NULL,
	p$$value11_yeartomonth INTERVAL YEAR(9) TO MONTH NULL,
	p$$value11_daytosecond INTERVAL DAY (9) TO SECOND(3) NULL
)
;

REM OPENMDX_TEST EXTENSION_DEFAULT

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

REM OPENMDX_TEST EXTENSION_NATIVE

  DROP TABLE EXTENSION_NATIVE ;

  CREATE TABLE EXTENSION_NATIVE (
    OBJECT_RID VARCHAR2(200) NOT NULL ENABLE, 
 	  OBJECT_OID VARCHAR2(200) NOT NULL ENABLE, 
	  OBJECT_IDX NUMBER(*,0) NOT NULL ENABLE, 
	  P$$OBJECT_parent__rid VARCHAR2(200), 
	  P$$OBJECT_parent__oid VARCHAR2(200),
	  OBJECT__VALID_FROM TIMESTAMP (3) WITH TIME ZONE, 
	  OBJECT__VALID_TO TIMESTAMP (3) WITH TIME ZONE, 
	  OBJECT__INVALIDATED_AT TIMESTAMP (3) WITH TIME ZONE, 
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
	  VALUE7 TIMESTAMP (3) WITH TIME ZONE, 
	  VALUE8 DATE
  ) ;

REM OPENMDX_TEST EXTENSION_NUMERIC

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

REM OPENMDX_TEST EXTENSION_SEGMENT

  DROP TABLE EXTENSION_SEGMENT ;

  CREATE TABLE EXTENSION_SEGMENT (
    OBJECT_RID VARCHAR2(200) NOT NULL ENABLE,
	  OBJECT_OID VARCHAR2(200) NOT NULL ENABLE,
	  OBJECT_IDX NUMBER(*,0) NOT NULL ENABLE,
	  OBJECT__CLASS VARCHAR2(200),
	  DESCRIPTION VARCHAR2(200)
  ) ;

REM OPENMDX_TEST state1_NATIVE

  DROP TABLE state1_NATIVE ;

  CREATE TABLE state1_NATIVE (
	object_rid VARCHAR2 (200) NOT NULL ,
	object_oid VARCHAR2 (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	created_at TIMESTAMP (3) WITH TIME ZONE NULL ,
	created_by VARCHAR2 (200) NULL ,
	modified_at TIMESTAMP (3) WITH TIME ZONE NULL ,
	modified_by VARCHAR2 (200) NULL ,
	object__valid_from TIMESTAMP (3) WITH TIME ZONE NULL ,
	object__valid_to TIMESTAMP (3) WITH TIME ZONE NULL ,
	object__invalidated_at TIMESTAMP (3) WITH TIME ZONE NULL ,
	object__class VARCHAR2 (100) NULL ,
	object__state_number NUMBER(10, 0) NULL ,
	object__state_id VARCHAR2 (100) NULL ,
	p$$object_parent__rid VARCHAR2 (200) NULL ,
	p$$object_parent__oid VARCHAR2 (200) NULL ,
	state_attr VARCHAR2 (60) NULL ,
	state_a VARCHAR2 (256) NULL ,
	p$$state_a__rid VARCHAR2 (256) NULL ,
	p$$state_a__oid VARCHAR2 (256) NULL ,
	value VARCHAR2 (60) NULL ,
	num VARCHAR2 (60) NULL ,
	state_a_derived VARCHAR2 (60) NULL ,
	value_a_derived VARCHAR2 (60) NULL ,
	multi_attr VARCHAR2 (60) NULL ,
	a1 VARCHAR2 (60) NULL ,
	a2 NUMBER(10, 0) NULL ,
	rtr__r_a__$object__class VARCHAR2 (60) NULL ,
	rtr__r_a__$name VARCHAR2 (60) NULL ,
	rtr__r_a__$num VARCHAR2 (60) NULL ,
	role1211$object__class VARCHAR2 (60) NULL ,
	role1211$name VARCHAR2 (60) NULL ,
	role1211$num VARCHAR2 (60) NULL ,
	r1$object__class VARCHAR2 (60) NULL ,
	r1$name VARCHAR2 (60) NULL ,
	r1$num VARCHAR2 (60) NULL ,
	role2$kl$object__class VARCHAR2 (60) NULL ,
	role2$kl$name VARCHAR2 (60) NULL ,
	role2$kl$num VARCHAR2 (60) NULL ,
	"_c_r__rt1$object__class" VARCHAR2 (60) NULL ,
	"_c_r__rt1$name" VARCHAR2 (60) NULL ,
	"_c_r__rt1$num" VARCHAR2 (60) NULL ,
	"_r_r__rt1$object__class" VARCHAR2 (60) NULL ,
	"_r_r__rt1$name" VARCHAR2 (60) NULL ,
	"_r_r__rt1$num" VARCHAR2 (60) NULL ,
	name VARCHAR2 (60) NULL ,
	aaaa VARCHAR2 (60) NULL ,
	rc_name VARCHAR2 (60) NULL ,
	bbbb VARCHAR2 (60) NULL ,
	role_b_id VARCHAR2 (60) NULL ,
	core_role VARCHAR2 (60) NULL ,
	long_values INTEGER NULL ,
	any_u_r_i_val VARCHAR2 (200) NULL ,
	boolean_val CHAR(1 CHAR) NULL ,
	date_val VARCHAR2 (20) NULL ,
	date_time_val VARCHAR2 (20) NULL ,
	decimal_val NUMBER(18, 6) NULL ,
	duration_val VARCHAR2 (10) NULL ,
	integer_val NUMBER(10, 0) NULL ,
	long_val INTEGER NULL ,
	short_val NUMBER(10, 0) NULL ,
	special_non_stated VARCHAR2 (256) NULL,
	p$$special_non_stated__rid VARCHAR2 (256) NULL,
	p$$special_non_stated__oid VARCHAR2 (256) NULL
);

REM OPENMDX_TEST datestate1_NATIVE

  DROP TABLE datestate1_NATIVE ;

  CREATE TABLE datestate1_NATIVE (
	object_rid VARCHAR2 (200) NOT NULL ,
	object_oid VARCHAR2 (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	created_at TIMESTAMP (3) WITH TIME ZONE NULL ,
	created_by VARCHAR2 (200) NULL ,
	modified_at TIMESTAMP (3) WITH TIME ZONE NULL ,
	modified_by VARCHAR2 (200) NULL ,
	object__valid_from DATE NULL,
	state_valid_from DATE NULL,
	object__valid_to DATE NULL ,
	state_valid_to DATE NULL ,
	object__invalidated_at TIMESTAMP (3) WITH TIME ZONE NULL ,
	object__class VARCHAR2 (100) NULL ,
	object__state_number NUMBER(10, 0) NULL ,
	object__state_id VARCHAR2 (100) NULL ,
	p$$object_parent__rid VARCHAR2 (200) NULL ,
	p$$object_parent__oid VARCHAR2 (200) NULL ,
	state_attr VARCHAR2 (60) NULL ,
	state_a VARCHAR2 (256) NULL ,
	p$$state_a__rid VARCHAR2 (256) NULL ,
	p$$state_a__oid VARCHAR2 (256) NULL ,
	value VARCHAR2 (60) NULL ,
	num VARCHAR2 (60) NULL ,
	state_a_derived VARCHAR2 (60) NULL ,
	value_a_derived VARCHAR2 (60) NULL ,
	multi_attr VARCHAR2 (60) NULL ,
	a1 VARCHAR2 (60) NULL ,
	a2 NUMBER(10, 0) NULL ,
	rtr__r_a__$object__class VARCHAR2 (60) NULL ,
	rtr__r_a__$name VARCHAR2 (60) NULL ,
	rtr__r_a__$num VARCHAR2 (60) NULL ,
	role1211$object__class VARCHAR2 (60) NULL ,
	role1211$name VARCHAR2 (60) NULL ,
	role1211$num VARCHAR2 (60) NULL ,
	r1$object__class VARCHAR2 (60) NULL ,
	r1$name VARCHAR2 (60) NULL ,
	r1$num VARCHAR2 (60) NULL ,
	role2$kl$object__class VARCHAR2 (60) NULL ,
	role2$kl$name VARCHAR2 (60) NULL ,
	role2$kl$num VARCHAR2 (60) NULL ,
	"_c_r__rt1$object__class" VARCHAR2 (60) NULL ,
	"_c_r__rt1$name" VARCHAR2 (60) NULL ,
	"_c_r__rt1$num" VARCHAR2 (60) NULL ,
	"_r_r__rt1$object__class" VARCHAR2 (60) NULL ,
	"_r_r__rt1$name" VARCHAR2 (60) NULL ,
	"_r_r__rt1$num" VARCHAR2 (60) NULL ,
	name VARCHAR2 (60) NULL ,
	aaaa VARCHAR2 (60) NULL ,
	rc_name VARCHAR2 (60) NULL ,
	bbbb VARCHAR2 (60) NULL ,
	role_b_id VARCHAR2 (60) NULL ,
	core_role VARCHAR2 (60) NULL ,
	long_values INTEGER NULL ,
	any_u_r_i_val VARCHAR2 (200) NULL ,
	boolean_val CHAR(1 CHAR) NULL ,
	date_val VARCHAR2 (20) NULL ,
	date_time_val VARCHAR2 (20) NULL ,
	decimal_val NUMBER(18, 6) NULL ,
	duration_val VARCHAR2 (10) NULL ,
	integer_val NUMBER(10, 0) NULL ,
	long_val INTEGER NULL ,
	short_val NUMBER(10, 0) NULL ,
	special_non_stated VARCHAR2 (256) NULL,
	p$$special_non_stated__rid VARCHAR2 (256) NULL,
	p$$special_non_stated__oid VARCHAR2 (256) NULL,
	state_c VARCHAR2 (256) NULL,
	p$$state_c__rid VARCHAR2 (256) NULL,
	p$$state_c__oid VARCHAR2 (256) NULL
);

REM OPENMDX_TEST nostate1_NATIVE

  DROP TABLE nostate1_NATIVE ;

  CREATE TABLE nostate1_NATIVE (
	object_rid VARCHAR2 (200) NOT NULL ,
	object_oid VARCHAR2 (200) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	created_at TIMESTAMP (3) WITH TIME ZONE NULL ,
	created_by VARCHAR2 (200) NULL ,
	modified_at TIMESTAMP (3) WITH TIME ZONE NULL ,
	modified_by VARCHAR2 (200) NULL ,
	object__class VARCHAR2 (100) NULL ,
	p$$object_parent__rid VARCHAR2 (200) NULL ,
	p$$object_parent__oid VARCHAR2 (200) NULL ,
	state_attr VARCHAR2 (60) NULL ,
	state_a VARCHAR2 (256) NULL ,
	p$$state_a__rid VARCHAR2 (256) NULL ,
	p$$state_a__oid VARCHAR2 (256) NULL ,
	value VARCHAR2 (60) NULL ,
	num VARCHAR2 (60) NULL ,
	state_a_derived VARCHAR2 (60) NULL ,
	value_a_derived VARCHAR2 (60) NULL ,
	multi_attr VARCHAR2 (60) NULL ,
	a1 VARCHAR2 (60) NULL ,
	a2 NUMBER(10, 0) NULL ,
	rtr__r_a__$object__class VARCHAR2 (60) NULL ,
	rtr__r_a__$name VARCHAR2 (60) NULL ,
	rtr__r_a__$num VARCHAR2 (60) NULL ,
	role1211$object__class VARCHAR2 (60) NULL ,
	role1211$name VARCHAR2 (60) NULL ,
	role1211$num VARCHAR2 (60) NULL ,
	r1$object__class VARCHAR2 (60) NULL ,
	r1$name VARCHAR2 (60) NULL ,
	r1$num VARCHAR2 (60) NULL ,
	role2$kl$object__class VARCHAR2 (60) NULL ,
	role2$kl$name VARCHAR2 (60) NULL ,
	role2$kl$num VARCHAR2 (60) NULL ,
	"_c_r__rt1$object__class" VARCHAR2 (60) NULL ,
	"_c_r__rt1$name" VARCHAR2 (60) NULL ,
	"_c_r__rt1$num" VARCHAR2 (60) NULL ,
	"_r_r__rt1$object__class" VARCHAR2 (60) NULL ,
	"_r_r__rt1$name" VARCHAR2 (60) NULL ,
	"_r_r__rt1$num" VARCHAR2 (60) NULL ,
	name VARCHAR2 (60) NULL ,
	aaaa VARCHAR2 (60) NULL ,
	rc_name VARCHAR2 (60) NULL ,
	bbbb VARCHAR2 (60) NULL ,
	role_b_id VARCHAR2 (60) NULL ,
	core_role VARCHAR2 (60) NULL ,
	long_values INTEGER NULL ,
	any_u_r_i_val VARCHAR2 (200) NULL ,
	boolean_val CHAR(1 CHAR) NULL ,
	date_val VARCHAR2 (20) NULL ,
	date_time_val VARCHAR2 (20) NULL ,
	decimal_val NUMBER(18, 6) NULL ,
	duration_val VARCHAR2 (10) NULL ,
	integer_val NUMBER(10, 0) NULL ,
	long_val INTEGER NULL ,
	short_val NUMBER(10, 0) NULL ,
	special_non_stated VARCHAR2 (256) NULL,
	p$$special_non_stated__rid VARCHAR2 (256) NULL,
	p$$special_non_stated__oid VARCHAR2 (256) NULL,
	state_c VARCHAR2 (256) NULL,
	p$$state_c__rid VARCHAR2 (256) NULL,
	p$$state_c__oid VARCHAR2 (256) NULL
);

REM OPENMDX_TEST aspect

  drop table aspect_Segment
  ;

  drop table aspect_Object
  ;

  drop table aspect_Object_
  ;

 CREATE TABLE aspect_Segment (
	object_id VARCHAR2 (100) NOT NULL ,
	object__class VARCHAR2 (100) NULL ,
	description VARCHAR2 (200) NULL
  )
  ;

  CREATE TABLE aspect_Object (
	object_id VARCHAR2 (100) NOT NULL ,
	object__class VARCHAR2 (100) NULL ,
	core VARCHAR2 (100) NULL ,
	created_at TIMESTAMP (3) WITH TIME ZONE NULL ,
	created_by_ NUMBER(10, 0) NULL ,
	modified_at TIMESTAMP (3) WITH TIME ZONE NULL ,
	modified_by_ NUMBER(10, 0) NULL ,
	string VARCHAR2 (200) NULL ,
	prime_ NUMBER(10, 0) NULL ,
	url VARCHAR2 (200) NULL
  )
  ;

  CREATE TABLE aspect_Object_ (
	object_id VARCHAR2 (100) NOT NULL ,
	object_idx INTEGER NOT NULL ,
	object__class VARCHAR2 (100) NULL ,
	created_by VARCHAR2 (200) NULL ,
	modified_by VARCHAR2 (200) NULL ,
	prime NUMBER(10, 0) NULL
  )
  ;

REM OPENMDX_TEST state2_NATIVE

  DROP TABLE state2_NATIVE ;

  CREATE TABLE state2_NATIVE (
	object_id VARCHAR2 (100) NOT NULL ,
	object__class VARCHAR2 (100) NULL ,
	description VARCHAR2 (60) NULL ,
	core VARCHAR2 (256) NULL ,
	created_at TIMESTAMP (3) WITH TIME ZONE NULL ,
	removed_at TIMESTAMP (3) WITH TIME ZONE NULL ,
	modified_at TIMESTAMP (3) WITH TIME ZONE NULL ,
	state_valid_from DATE,
	state_valid_to DATE,
	state_a VARCHAR2 (256) NULL ,
	state_n VARCHAR2 (256) NULL ,
	string_value VARCHAR2 (60) NULL ,
	uri_value VARCHAR2 (200) NULL ,
	boolean_value CHAR(1 CHAR) NULL ,
	date_value DATE NULL ,
	date_time_value TIMESTAMP (3) WITH TIME ZONE NULL ,
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

