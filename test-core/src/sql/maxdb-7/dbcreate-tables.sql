CREATE TABLE "SYSTEM"."TEST_GENERIC"
(
	"OBJECT_RID"               Integer    NOT NULL,
	"OBJECT_OID"               Varchar (256) ASCII    NOT NULL,
	"OBJECT_NAME"               Varchar (64) ASCII    NOT NULL,
	"OBJECT_VAL_STRING"               Varchar (200) ASCII,
	"OBJECT_VAL_NUMERIC"               Fixed (18,6),
	"OBJECT_VAL_BINARY"               Varchar (100) BYTE,
	"OBJECT_IDX"               Integer
)
//
CREATE TABLE "SYSTEM"."TEST_REF"
(
	"OBJECT_RID"               Integer    NOT NULL,
	"OBJECT_REFERENCE"               Varchar (512) ASCII    NOT NULL,
	"N"               Integer,
	"C$0"               Varchar (100) ASCII,
	"C$1"               Varchar (100) ASCII,
	"C$2"               Varchar (100) ASCII,
	"C$3"               Varchar (100) ASCII,
	"C$4"               Varchar (100) ASCII,
	"C$5"               Varchar (100) ASCII,
	"C$6"               Varchar (100) ASCII,
	"C$7"               Varchar (100) ASCII,
	"C$8"               Varchar (100) ASCII,
	"C$9"               Varchar (100) ASCII
)
//
CREATE TABLE "SYSTEM"."TEST_SLB_GENERIC"
(
	"OBJECT_RID"               Integer    NOT NULL,
	"OBJECT_OID"               Varchar (256) ASCII    NOT NULL,
	"OBJECT_NAME"               Varchar (64) ASCII    NOT NULL,
	"OBJECT_VAL_STRING"               Varchar (200) ASCII,
	"OBJECT_VAL_NUMERIC"               Fixed (18,6),
	"OBJECT_VAL_BINARY"               Varchar (100) BYTE,
	"OBJECT_IDX"               Integer
)
//
CREATE TABLE "SYSTEM"."TEST_SLB_SLICED"
(
	"OBJECT_RID"               Integer    NOT NULL,
	"OBJECT_OID"               Varchar (128) ASCII    NOT NULL,
	"OBJECT_IDX"               Integer    NOT NULL,
	"OBJECT__CREATED_AT"               Varchar (20) ASCII,
	"OBJECT__CREATED_BY"               Varchar (128) ASCII,
	"OBJECT__MODIFIED_AT"               Varchar (20) ASCII,
	"OBJECT__MODIFIED_BY"               Varchar (128) ASCII,
	"OBJECT__CLASS"               Varchar (60) ASCII,
	"SLB_TYPE"               Varchar (10) ASCII,
	"POS"               Varchar (128) ASCII,
	"PRICE"               Fixed (18,9),
	"IS_DEBIT"               Fixed (18,9),
	"IS_LONG"               Fixed (18,9),
	"PRICE_CURRENCY"               Varchar (10) ASCII,
	"VALUE_DATE"               Varchar (20) ASCII,
	"BOOKING_DATE"               Varchar (20) ASCII,
	"QUANTITY"               Fixed (18,9),
	"QUANTITY_ABSOLUTE"               Fixed (18,9),
	"VISIBILITY"               Varchar (10) ASCII,
	"ADMIN_DESCR"               Varchar (64) ASCII,
	"DESCRIPTION"               Varchar (64) ASCII,
	"CRED_VALUE"               Varchar (10) BYTE,
	"P$$OBJECT_PARENT__OID"               Varchar (20) ASCII,
	"P$$OBJECT_PARENT__RID"               Integer,
	"P$$POS__OID"               Varchar (20) ASCII,
	"P$$POS__RID"               Integer,
	"P$$POS_PARENT__OID"               Varchar (50) ASCII,
	"P$$POS_PARENT__RID"               Integer
)
//
CREATE SEQUENCE "TEST_REF_SEQ" INCREMENT BY 1 START WITH 54 MINVALUE 1 MAXVALUE 1000000 NOCYCLE CACHE 100
