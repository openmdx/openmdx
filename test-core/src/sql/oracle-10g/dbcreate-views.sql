DROP VIEW BeforeImage_Address;
CREATE VIEW BeforeImage_Address AS SELECT
    'audit*' || p$$object_rsx object_rid,
    p$$object_oid$0 || '!' || p$$object_oid$1 object_oid,
    p$$object_oid$0,
    p$$object_oid$1,
    p$$object_parent__rid,
    p$$object_parent__oid,
    object__class,
    description,
    created_at,
    modified_at,
    postal_code,
    street,
    country,
    city
FROM app1_Address 
where p$$object_oid$1 IS NOT NULL;

DROP VIEW Involvement_Address;
CREATE VIEW Involvement_Address AS SELECT 
  p$$unit_of_work__rid p$$object_parent__rid,
  p$$object_oid$1 p$$object_parent__oid,
  0 as object_rid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') object_oid,
  'data*' || p$$object_rsx p$$object_oid$0,
  p$$object_oid$0 p$$object_oid$1,
  'org:openmdx:audit2:Involvement' object__class,
  'audit*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') || '!' || p$$object_oid$1 before_image,
  'audit*' || p$$object_rsx p$$before_image__rid,
  p$$object_oid$0 || '!' || p$$object_oid$1 before_image__oid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') object,  
  'data*' || p$$object_rsx p$$object__rid,  
  p$$object_oid$0 p$$object__oid  
FROM app1_Address
WHERE p$$object_oid$1 IS NOT NULL;

DROP VIEW BeforeImage_Address_;
CREATE VIEW BeforeImage_Address_ AS SELECT
    'audit*' || p$$object_rsx object_rid,
    p$$object_oid$0 || '!' || p$$object_oid$1 object_oid,
    p$$object_oid$0,
    p$$object_oid$1,
    object_idx,
    modified_by,
    created_by,
    address_line
FROM app1_Address_N 
where p$$object_oid$1 IS NOT NULL;

DROP VIEW BeforeImage_DOC;
CREATE VIEW BeforeImage_DOC AS SELECT 
    'audit*' || p$$object_rsx object_rid,
    p$$object_oid$0 || '!' || p$$object_oid$1 object_oid,
    p$$object_oid$0,
    p$$object_oid$1,
    object_idx,
    p$$object_parent__rid,
    p$$object_parent__oid,
    object__class,
    created_at,
    modified_at,
    created_by,
    modified_by,
    description,
    content,
    keyword
FROM app1_DOC 
where p$$object_oid$1 IS NOT NULL;

DROP VIEW Involvement_DOC;
CREATE VIEW Involvement_DOC AS SELECT 
  p$$unit_of_work__rid p$$object_parent__rid,
  p$$object_oid$1 p$$object_parent__oid,
  0 as object_rid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') object_oid,
  'data*' || p$$object_rsx p$$object_oid$0,
  p$$object_oid$0 p$$object_oid$1,
  'org:openmdx:audit2:Involvement' object__class,
  'audit*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') || '!' || p$$object_oid$1 before_image,
  'audit*' || p$$object_rsx p$$before_image__rid,
  p$$object_oid$0 || '!' || p$$object_oid$1 before_image__oid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') object,  
  'data*' || p$$object_rsx p$$object__rid,  
  p$$object_oid$0 p$$object__oid  
FROM app1_DOC
WHERE object_idx = 0 AND p$$object_oid$1 IS NOT NULL;

DROP VIEW BeforeImage_Invoice;
CREATE VIEW BeforeImage_Invoice AS SELECT
    'audit*' || p$$object_rsx object_rid,
    p$$object_oid$0 || '!' || p$$object_oid$1 object_oid,
    p$$object_oid$0,
    p$$object_oid$1,
    object_idx,
    p$$object_parent__rid,
    p$$object_parent__oid,
    object__class,
    description,
    product_group_id,
    created_at,
    modified_at,
    created_by,
    modified_by
FROM app1_Invoice 
where p$$object_oid$1 IS NOT NULL;

DROP VIEW Involvement_Invoice;
CREATE VIEW Involvement_Invoice AS SELECT 
  p$$unit_of_work__rid p$$object_parent__rid,
  p$$object_oid$1 p$$object_parent__oid,
  0 as object_rid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') object_oid,
  'data*' || p$$object_rsx p$$object_oid$0,
  p$$object_oid$0 p$$object_oid$1,
  'org:openmdx:audit2:Involvement' object__class,
  'audit*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') || '!' || p$$object_oid$1 before_image,
  'audit*' || p$$object_rsx p$$before_image__rid,
  p$$object_oid$0 || '!' || p$$object_oid$1 before_image__oid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') object,  
  'data*' || p$$object_rsx p$$object__rid,  
  p$$object_oid$0 p$$object__oid  
FROM app1_Invoice
WHERE object_idx = 0 AND p$$object_oid$1 IS NOT NULL;

DROP VIEW BeforeImage_InvoicePosition;
CREATE VIEW BeforeImage_InvoicePosition AS SELECT
    'audit*' || p$$object_rsx object_rid,
    p$$object_oid$0 || '!' || p$$object_oid$1 object_oid,
    p$$object_oid$0,
    p$$object_oid$1,
    object_idx,
    p$$object_parent__rid,
    p$$object_parent__oid,
    object__class,
    p$$unit_of_work__rid,
    description,
    created_at,
    modified_at,
    created_by,
    modified_by,
    product_id
FROM app1_InvoicePosition 
where p$$object_oid$1 IS NOT NULL;

DROP VIEW Involvement_InvoicePosition;
CREATE VIEW Involvement_InvoicePosition AS SELECT 
  p$$unit_of_work__rid p$$object_parent__rid,
  p$$object_oid$1 p$$object_parent__oid,
  0 as object_rid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') object_oid,
  'data*' || p$$object_rsx p$$object_oid$0,
  p$$object_oid$0 p$$object_oid$1,
  'org:openmdx:audit2:Involvement' object__class,
  'audit*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') || '!' || p$$object_oid$1 before_image,
  'audit*' || p$$object_rsx p$$before_image__rid,
  p$$object_oid$0 || '!' || p$$object_oid$1 before_image__oid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') object,  
  'data*' || p$$object_rsx p$$object__rid,  
  p$$object_oid$0 p$$object__oid  
FROM app1_InvoicePosition
WHERE object_idx = 0 AND p$$object_oid$1 IS NOT NULL;

DROP VIEW BeforeImage_Member;
CREATE VIEW BeforeImage_Member AS SELECT
    'audit*' || p$$object_rsx object_rid,
    p$$object_oid$0 || '!' || p$$object_oid$1 object_oid,
    p$$object_oid$0,
    p$$object_oid$1,
    object_idx,
    p$$object_parent__rid,
    p$$object_parent__oid,
    object__class,
    description,
    created_at,
    modified_at,
    modified_by,
    created_by,
    m1,
    p$$m1__rid,
    p$$m1__oid,
    m2,
    p$$m2__rid,
    p$$m2__oid
FROM app1_Member 
where p$$object_oid$1 IS NOT NULL;

DROP VIEW Involvement_Member;
CREATE VIEW Involvement_Member AS SELECT 
  p$$unit_of_work__rid p$$object_parent__rid,
  p$$object_oid$1 p$$object_parent__oid,
  0 as object_rid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') object_oid,
  'data*' || p$$object_rsx p$$object_oid$0,
  p$$object_oid$0 p$$object_oid$1,
  'org:openmdx:audit2:Involvement' object__class,
  'audit*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') || '!' || p$$object_oid$1 before_image,
  'audit*' || p$$object_rsx p$$before_image__rid,
  p$$object_oid$0 || '!' || p$$object_oid$1 before_image__oid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') object,  
  'data*' || p$$object_rsx p$$object__rid,  
  p$$object_oid$0 p$$object__oid  
FROM app1_Member
WHERE object_idx = 0 AND p$$object_oid$1 IS NOT NULL;

DROP VIEW BeforeImage_PersonGroup;
CREATE VIEW BeforeImage_PersonGroup AS SELECT 
    'audit*' || p$$object_rsx object_rid,
    p$$object_oid$0 || '!' || p$$object_oid$1 object_oid,
    p$$object_oid$0,
    p$$object_oid$1,
    object_idx,
    p$$object_parent__rid,
    p$$object_parent__oid,
    object__class,
    description,
    created_at,
    modified_at,
    modified_by,
    created_by,
    name
FROM app1_PersonGroup 
where p$$object_oid$1 IS NOT NULL;
  
DROP VIEW Involvement_PersonGroup;
CREATE VIEW Involvement_PersonGroup AS SELECT 
  p$$unit_of_work__rid p$$object_parent__rid,
  p$$object_oid$1 p$$object_parent__oid,
  0 as object_rid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') object_oid,
  'data*' || p$$object_rsx p$$object_oid$0,
  p$$object_oid$0 p$$object_oid$1,
  'org:openmdx:audit2:Involvement' object__class,
  'audit*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') || '!' || p$$object_oid$1 before_image,
  'audit*' || p$$object_rsx p$$before_image__rid,
  p$$object_oid$0 || '!' || p$$object_oid$1 before_image__oid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') object,  
  'data*' || p$$object_rsx p$$object__rid,  
  p$$object_oid$0 p$$object__oid  
FROM app1_PersonGroup
WHERE object_idx = 0 AND p$$object_oid$1 IS NOT NULL;
  
DROP VIEW BeforeImage_SLICED;
CREATE VIEW BeforeImage_SLICED AS SELECT 
    'audit*' || p$$object_rsx object_rid,
    p$$object_oid$0 || '!' || p$$object_oid$1 object_oid,
    p$$object_oid$0,
    p$$object_oid$1,
    object_idx,
    p$$object_parent__rid,
    p$$object_parent__oid,
    object__class,
    created_at,
    modified_at,
    created_by,
    modified_by,
    m1,
    last_name,
    house_number,
    city,
    foreign_id,
    postal_code,
    description,
    assigned_address,
    p$$assigned_address__rid,
    p$$assigned_address__oid,
    product_id,
    salutation,
    street,
    address_line,
    address,
    text,
    birthdate,
    member_of_group,
    birthdate_as_date_time,
    person_group,
    p$$person_group__rid,
    p$$person_group__oid,
    country,
    sex,
    given_name,
    product_group_id,
    place_of_birth,
    additional_info
FROM app1_SLICED 
where p$$object_oid$1 IS NOT NULL;
  
DROP VIEW Involvement_SLICED;
CREATE VIEW Involvement_SLICED AS SELECT 
  p$$unit_of_work__rid p$$object_parent__rid,
  p$$object_oid$1 p$$object_parent__oid,
  0 as object_rid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') object_oid,
  'data*' || p$$object_rsx p$$object_oid$0,
  p$$object_oid$0 p$$object_oid$1,
  'org:openmdx:audit2:Involvement' object__class,
  'audit*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') || '!' || p$$object_oid$1 before_image,
  'audit*' || p$$object_rsx p$$before_image__rid,
  p$$object_oid$0 || '!' || p$$object_oid$1 before_image__oid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') object,  
  'data*' || p$$object_rsx p$$object__rid,  
  p$$object_oid$0 p$$object__oid  
FROM app1_SLICED
WHERE object_idx = 0 AND p$$object_oid$1 IS NOT NULL;
  
DROP VIEW BeforeImage_MessageTemplate;
CREATE VIEW BeforeImage_MessageTemplate AS SELECT
    'audit*' || p$$object_rsx object_rid,
    p$$object_oid$0 || '!' || p$$object_oid$1 object_oid,
    p$$object_oid$0,
    p$$object_oid$1,
    object_idx,
    p$$object_parent__rid,
    p$$object_parent__oid,
    object__class,
    text,
    description,
    created_at,
    modified_at,
    created_by,
    modified_by
FROM app1_MessageTemplate 
where p$$object_oid$1 IS NOT NULL;

DROP VIEW Involvement_MessageTemplate;
CREATE VIEW Involvement_MessageTemplate AS SELECT 
  p$$unit_of_work__rid p$$object_parent__rid,
  p$$object_oid$1 p$$object_parent__oid,
  0 as object_rid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') object_oid,
  'data*' || p$$object_rsx p$$object_oid$0,
  p$$object_oid$0 p$$object_oid$1,
  'org:openmdx:audit2:Involvement' object__class,
  'audit*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') || '!' || p$$object_oid$1 before_image,
  'audit*' || p$$object_rsx p$$before_image__rid,
  p$$object_oid$0 || '!' || p$$object_oid$1 before_image__oid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') object,  
  'data*' || p$$object_rsx p$$object__rid,  
  p$$object_oid$0 p$$object__oid  
FROM app1_MessageTemplate
WHERE object_idx = 0 AND p$$object_oid$1 IS NOT NULL;

DROP VIEW Involvement_ALL;
CREATE VIEW Involvement_ALL AS 
SELECT * FROM INVOLVEMENT_ADDRESS UNION ALL
SELECT * FROM INVOLVEMENT_DOC UNION ALL
SELECT * FROM INVOLVEMENT_INVOICE UNION ALL
SELECT * FROM INVOLVEMENT_INVOICEPOSITION UNION ALL
SELECT * FROM INVOLVEMENT_MEMBER UNION ALL
SELECT * FROM INVOLVEMENT_MESSAGETEMPLATE UNION ALL
SELECT * FROM INVOLVEMENT_PERSONGROUP UNION ALL
SELECT * FROM INVOLVEMENT_SLICED;
