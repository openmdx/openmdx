CREATE VIEW BeforeImage_Address AS SELECT
    'audit*' || p$$object_rsx object_rid,
    'audit' as p$$object_rid$0,
    p$$object_rsx,
    p$$object_rsx as p$$object_rid$1,
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
FROM app1_aud_Address;

CREATE VIEW Involvement_Address AS SELECT 
  p$$unit_of_work__rid as p$$object_parent__rid,
  p$$object_oid$1 as p$$object_parent__oid,
  0 as object_rid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') as object_oid,
  'data' as p$$object_oid$0,
  p$$object_rsx as p$$object_oid$1,
  p$$object_oid$0 as p$$object_oid$2,
  'org:openmdx:audit2:Involvement' as object__class,
  'audit*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') || '!' || p$$object_oid$1 as before_image,
  'audit*' || p$$object_rsx as p$$before_image__rid,
  'audit' as p$$before_image__rid$0,
  p$$object_rsx as p$$before_image__rid$1,
  p$$object_oid$0 || '!' || p$$object_oid$1 as before_image__oid,
  p$$object_oid$0 as before_image__oid$0,
  p$$object_oid$1 as before_image__oid$1,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') as object,  
  'data*' || p$$object_rsx as p$$object__rid,  
  'data' as p$$object__rid$0,  
  p$$object_rsx as p$$object__rid$1,  
  p$$object_oid$0 as p$$object__oid  
FROM app1_aud_Address;
CREATE VIEW BeforeImage_Address_ AS SELECT
    'audit*' || p$$object_rsx object_rid,
    'audit' as p$$object_rid$0,
    p$$object_rsx,
    p$$object_rsx as p$$object_rid$1,
    p$$object_oid$0 || '!' || p$$object_oid$1 object_oid,
    p$$object_oid$0,
    p$$object_oid$1,
    object_idx,
    address_line
FROM app1_aud_Address_;

CREATE VIEW BeforeImage_DOC AS SELECT 
    'audit*' || p$$object_rsx object_rid,
    'audit' as p$$object_rid$0,
    p$$object_rsx,
    p$$object_rsx as p$$object_rid$1,
    p$$object_oid$0 || '!' || p$$object_oid$1 object_oid,
    p$$object_oid$0,
    p$$object_oid$1,
    p$$object_parent__rid,
    p$$object_parent__oid,
    object__class,
    created_at,
    modified_at,
    created_by_0,
    modified_by_0,
    description,
    content,
    text
FROM app1_aud_DOC;
CREATE VIEW BeforeImage_DOC_ AS SELECT 
    'audit*' || p$$object_rsx object_rid,
    'audit' as p$$object_rid$0,
    p$$object_rsx,
    p$$object_rsx as p$$object_rid$1,
    p$$object_oid$0 || '!' || p$$object_oid$1 object_oid,
    p$$object_oid$0,
    p$$object_oid$1,
    object_idx,
    keyword
FROM app1_aud_DOC_;

CREATE VIEW Involvement_DOC AS SELECT 
  p$$unit_of_work__rid p$$object_parent__rid,
  p$$object_oid$1 p$$object_parent__oid,
  0 as object_rid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') as object_oid,
  'data' as p$$object_oid$0,
  p$$object_rsx as p$$object_oid$1,
  p$$object_oid$0 as p$$object_oid$2,
  'org:openmdx:audit2:Involvement' as object__class,
  'audit*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') || '!' || p$$object_oid$1 as before_image,
  'audit*' || p$$object_rsx as p$$before_image__rid,
  'audit' as p$$before_image__rid$0,
  p$$object_rsx as p$$before_image__rid$1,
  p$$object_oid$0 || '!' || p$$object_oid$1 as before_image__oid,
  p$$object_oid$0 as before_image__oid$0,
  p$$object_oid$1 as before_image__oid$1,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') object,  
  'data*' || p$$object_rsx as p$$object__rid,  
  'data' as p$$object__rid$0,  
  p$$object_rsx as p$$object__rid$1,  
  p$$object_oid$0 as p$$object__oid  
FROM app1_aud_DOC;

CREATE VIEW BeforeImage_Invoice AS SELECT
    'audit*' || p$$object_rsx object_rid,
    'audit' as p$$object_rid$0,
    p$$object_rsx,
    p$$object_rsx as p$$object_rid$1,
    p$$object_oid$0 || '!' || p$$object_oid$1 object_oid,
    p$$object_oid$0,
    p$$object_oid$1,
    p$$object_parent__rid,
    p$$object_parent__oid,
    object__class,
    description,
    product_group_id,
    created_at,
    modified_at,
    created_by_0,
    modified_by_0
FROM app1_aud_Invoice;

CREATE VIEW Involvement_Invoice AS SELECT 
  p$$unit_of_work__rid p$$object_parent__rid,
  p$$object_oid$1 p$$object_parent__oid,
  0 as object_rid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') as object_oid,
  'data' as p$$object_oid$0,
  p$$object_rsx as p$$object_oid$1,
  p$$object_oid$0 as p$$object_oid$2,
  'org:openmdx:audit2:Involvement' as object__class,
  'audit*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') || '!' || p$$object_oid$1 as before_image,
  'audit*' || p$$object_rsx as p$$before_image__rid,
  'audit' as p$$before_image__rid$0,
  p$$object_rsx as p$$before_image__rid$1,
  p$$object_oid$0 || '!' || p$$object_oid$1 as before_image__oid,
  p$$object_oid$0 as before_image__oid$0,
  p$$object_oid$1 as before_image__oid$1,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') object,  
  'data*' || p$$object_rsx as p$$object__rid,  
  'data' as p$$object__rid$0,  
  p$$object_rsx as p$$object__rid$1,  
  p$$object_oid$0 as p$$object__oid  
FROM app1_aud_Invoice;

CREATE VIEW BeforeImage_InvoicePosition AS SELECT
    'audit*' || p$$object_rsx object_rid,
    'audit' as p$$object_rid$0,
    p$$object_rsx,
    p$$object_rsx as p$$object_rid$1,
    p$$object_oid$0 || '!' || p$$object_oid$1 object_oid,
    p$$object_oid$0,
    p$$object_oid$1,
    p$$object_parent__rid,
    p$$object_parent__oid,
    object__class,
    p$$unit_of_work__rid,
    description,
    created_at,
    modified_at,
    created_by_0,
    modified_by_0,
    product_id
FROM app1_aud_InvoicePosition;

CREATE VIEW Involvement_InvoicePosition AS SELECT 
  p$$unit_of_work__rid p$$object_parent__rid,
  p$$object_oid$1 p$$object_parent__oid,
  0 as object_rid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') as object_oid,
  'data' as p$$object_oid$0,
  p$$object_rsx as p$$object_oid$1,
  p$$object_oid$0 as p$$object_oid$2,
  'org:openmdx:audit2:Involvement' as object__class,
  'audit*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') || '!' || p$$object_oid$1 as before_image,
  'audit*' || p$$object_rsx as p$$before_image__rid,
  'audit' as p$$before_image__rid$0,
  p$$object_rsx as p$$before_image__rid$1,
  p$$object_oid$0 || '!' || p$$object_oid$1 as before_image__oid,
  p$$object_oid$0 as before_image__oid$0,
  p$$object_oid$1 as before_image__oid$1,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') object,  
  'data*' || p$$object_rsx as p$$object__rid,  
  'data' as p$$object__rid$0,  
  p$$object_rsx as p$$object__rid$1,  
  p$$object_oid$0 as p$$object__oid  
FROM app1_aud_InvoicePosition;

CREATE VIEW BeforeImage_Member AS SELECT
    'audit*' || p$$object_rsx object_rid,
    'audit' as p$$object_rid$0,
    p$$object_rsx,
    p$$object_rsx as p$$object_rid$1,
    p$$object_oid$0 || '!' || p$$object_oid$1 object_oid,
    p$$object_oid$0,
    p$$object_oid$1,
    p$$object_parent__rid,
    p$$object_parent__oid,
    object__class,
    description,
    created_at,
    modified_at,
    modified_by_0,
    created_by_0,
    m1,
    p$$m1__rid,
    p$$m1__oid,
    m2,
    p$$m2__rid,
    p$$m2__oid
FROM app1_aud_Member;

CREATE VIEW Involvement_Member AS SELECT 
  p$$unit_of_work__rid p$$object_parent__rid,
  p$$object_oid$1 p$$object_parent__oid,
  0 as object_rid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') as object_oid,
  'data' as p$$object_oid$0,
  p$$object_rsx as p$$object_oid$1,
  p$$object_oid$0 as p$$object_oid$2,
  'org:openmdx:audit2:Involvement' as object__class,
  'audit*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') || '!' || p$$object_oid$1 as before_image,
  'audit*' || p$$object_rsx as p$$before_image__rid,
  'audit' as p$$before_image__rid$0,
  p$$object_rsx as p$$before_image__rid$1,
  p$$object_oid$0 || '!' || p$$object_oid$1 as before_image__oid,
  p$$object_oid$0 as before_image__oid$0,
  p$$object_oid$1 as before_image__oid$1,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') object,  
  'data*' || p$$object_rsx as p$$object__rid,  
  'data' as p$$object__rid$0,  
  p$$object_rsx as p$$object__rid$1,  
  p$$object_oid$0 as p$$object__oid  
FROM app1_aud_Member;

CREATE VIEW BeforeImage_PersonGroup AS SELECT 
    'audit*' || p$$object_rsx object_rid,
    'audit' as p$$object_rid$0,
    p$$object_rsx,
    p$$object_rsx as p$$object_rid$1,
    p$$object_oid$0 || '!' || p$$object_oid$1 object_oid,
    p$$object_oid$0,
    p$$object_oid$1,
    p$$object_parent__rid,
    p$$object_parent__oid,
    object__class,
    description,
    created_at,
    modified_at,
    modified_by_0,
    created_by_0,
    name
FROM app1_aud_PersonGroup;

CREATE VIEW Involvement_PersonGroup AS SELECT 
  p$$unit_of_work__rid p$$object_parent__rid,
  p$$object_oid$1 p$$object_parent__oid,
  0 as object_rid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') as object_oid,
  'data' as p$$object_oid$0,
  p$$object_rsx as p$$object_oid$1,
  p$$object_oid$0 as p$$object_oid$2,
  'org:openmdx:audit2:Involvement' as object__class,
  'audit*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') || '!' || p$$object_oid$1 as before_image,
  'audit*' || p$$object_rsx as p$$before_image__rid,
  'audit' as p$$before_image__rid$0,
  p$$object_rsx as p$$before_image__rid$1,
  p$$object_oid$0 || '!' || p$$object_oid$1 as before_image__oid,
  p$$object_oid$0 as before_image__oid$0,
  p$$object_oid$1 as before_image__oid$1,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') object,  
  'data*' || p$$object_rsx as p$$object__rid,  
  'data' as p$$object__rid$0,  
  p$$object_rsx as p$$object__rid$1,  
  p$$object_oid$0 as p$$object__oid  
FROM app1_aud_PersonGroup;

CREATE VIEW BeforeImage_SLICED AS SELECT 
    'audit*' || p$$object_rsx object_rid,
    'audit' as p$$object_rid$0,
    p$$object_rsx,
    p$$object_rsx as p$$object_rid$1,
    p$$object_oid$0 || '!' || p$$object_oid$1 object_oid,
    p$$object_oid$0,
    p$$object_oid$1,
    p$$object_parent__rid,
    p$$object_parent__oid,
    object__class,
    created_at,
    modified_at,
    created_by_0,
    modified_by_0,
    m1,
    last_name,
    house_number,
    city,
    foreign_id,
    postal_code,
    description,
    product_id,
    salutation,
    street,
    address_line,
    address,
    text,
    birthdate,
    member_of_group,
    birthdate_as_date_time,
    country,
    sex,
    product_group_id,
    place_of_birth
FROM app1_aud_SLICED;
CREATE VIEW BeforeImage_SLICED_ AS SELECT 
    'audit*' || p$$object_rsx object_rid,
    'audit' as p$$object_rid$0,
    p$$object_rsx,
    p$$object_rsx as p$$object_rid$1,
    p$$object_oid$0 || '!' || p$$object_oid$1 object_oid,
    p$$object_oid$0,
    p$$object_oid$1,
    object_idx,
    assigned_address,
    p$$assigned_address__rid,
    p$$assigned_address__oid,
    given_name,
    additional_info
FROM app1_aud_SLICED_;

CREATE VIEW Involvement_SLICED AS SELECT 
  p$$unit_of_work__rid p$$object_parent__rid,
  p$$object_oid$1 p$$object_parent__oid,
  0 as object_rid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') as object_oid,
  'data' as p$$object_oid$0,
  p$$object_rsx as p$$object_oid$1,
  p$$object_oid$0 as p$$object_oid$2,
  'org:openmdx:audit2:Involvement' as object__class,
  'audit*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') || '!' || p$$object_oid$1 as before_image,
  'audit*' || p$$object_rsx as p$$before_image__rid,
  'audit' as p$$before_image__rid$0,
  p$$object_rsx as p$$before_image__rid$1,
  p$$object_oid$0 || '!' || p$$object_oid$1 as before_image__oid,
  p$$object_oid$0 as before_image__oid$0,
  p$$object_oid$1 as before_image__oid$1,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') object,  
  'data*' || p$$object_rsx as p$$object__rid,  
  'data' as p$$object__rid$0,  
  p$$object_rsx as p$$object__rid$1,  
  p$$object_oid$0 as p$$object__oid  
FROM app1_aud_SLICED;

CREATE VIEW BeforeImage_MessageTemplate AS SELECT
    'audit*' || p$$object_rsx object_rid,
    'audit' as p$$object_rid$0,
    p$$object_rsx,
    p$$object_rsx as p$$object_rid$1,
    p$$object_oid$0 || '!' || p$$object_oid$1 object_oid,
    p$$object_oid$0,
    p$$object_oid$1,
    p$$object_parent__rid,
    p$$object_parent__oid,
    object__class,
    text,
    description,
    created_at,
    modified_at,
    created_by_0,
    modified_by_0
FROM app1_aud_MessageTemplate;

CREATE VIEW Involvement_MessageTemplate AS SELECT 
  p$$unit_of_work__rid p$$object_parent__rid,
  p$$object_oid$1 p$$object_parent__oid,
  0 as object_rid,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') as object_oid,
  'data' as p$$object_oid$0,
  p$$object_rsx as p$$object_oid$1,
  p$$object_oid$0 as p$$object_oid$2,
  'org:openmdx:audit2:Involvement' as object__class,
  'audit*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') || '!' || p$$object_oid$1 as before_image,
  'audit*' || p$$object_rsx as p$$before_image__rid,
  'audit' as p$$before_image__rid$0,
  p$$object_rsx as p$$before_image__rid$1,
  p$$object_oid$0 || '!' || p$$object_oid$1 as before_image__oid,
  p$$object_oid$0 as before_image__oid$0,
  p$$object_oid$1 as before_image__oid$1,
  'data*' || p$$object_rsx || '/' || REPLACE(REPLACE(p$$object_oid$0,':','::'),'/','//') object,  
  'data*' || p$$object_rsx as p$$object__rid,  
  'data' as p$$object__rid$0,  
  p$$object_rsx as p$$object__rid$1,  
  p$$object_oid$0 as p$$object__oid  
FROM app1_aud_MessageTemplate;

CREATE VIEW Involvement_ALL AS 
SELECT * FROM INVOLVEMENT_ADDRESS UNION ALL
SELECT * FROM INVOLVEMENT_DOC UNION ALL
SELECT * FROM INVOLVEMENT_INVOICE UNION ALL
SELECT * FROM INVOLVEMENT_INVOICEPOSITION UNION ALL
SELECT * FROM INVOLVEMENT_MEMBER UNION ALL
SELECT * FROM INVOLVEMENT_MESSAGETEMPLATE UNION ALL
SELECT * FROM INVOLVEMENT_PERSONGROUP UNION ALL
SELECT * FROM INVOLVEMENT_SLICED;
