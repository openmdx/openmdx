drop view state1_noUnion
;
REM providing view for shared relation to StateD
REM "new" fashioned way, without union
REM but not much faster, because e1 join is expensive 
REM Problem: object__state_id is not set correctly.
CREATE VIEW state1_noUnion AS
SELECT
  ref_2.object_referenceId,
  e_ref.c$8 || ':' || e.object_objectId object_objectId,
  e.object_idx,
  e.object__class,
  e.p$$object_parent__referenceid,
  e.p$$object_parent__objectid,

  e.created_at,
  e.modified_at,
  e.created_by,
  e.modified_by,

  e.object__valid_from,
  e.object__valid_to,
  e.object__invalidated_at,
  e.object__state_id,
  e.object__state_number,

  e.state_attr,
  e.long_values,
  e.any_u_r_i_val,
  e.boolean_val,
  e.date_val,
  e.date_time_val,
  e.decimal_val,
  e.duration_val,
  e.integer_val,
  e.long_val,
  e.short_val,

  e1.object__state_id        p$$object__state_id$0,
  e1.modified_at             p$$modified_at$0,
  e1.object__valid_from      p$$object__valid_from$0
FROM
  state1_SLICED e,
  state1_SLICED e1,
  state1_REF ref_2,
  state1_REF e_ref
WHERE
  (e_ref.object_referenceId = e.object_referenceId) AND
  (e_ref.c$9 = 'stateD') AND
  (e_ref.c$6 = ref_2.c$6) AND
  (ref_2.c$7 = 'stateD') AND
  e.object_referenceId = e1.object_referenceId AND
  e.object_objectid = e1.object_objectid AND
  e1.object_idx = 0
;
drop view state1_revolutionary
;
CREATE VIEW state1_revolutionary AS
SELECT
  ref_2.object_referenceId,
  e_ref.c$8 || ':' || e.object_objectId object_objectId,
  e.object_idx,
  e.object__class,
  e.p$$object_parent__referenceid,
  e.p$$object_parent__objectid,

  e.created_at,
  e.modified_at,
  e.created_by,
  e.modified_by,

  e.object__valid_from,
  e.object__valid_to,
  e.object__invalidated_at,
  e.object__state_id,
  e.object__state_number,

  e.state_attr,
  e.long_values,
  e.any_u_r_i_val,
  e.boolean_val,
  e.date_val,
  e.date_time_val,
  e.decimal_val,
  e.duration_val,
  e.integer_val,
  e.long_val,
  e.short_val
FROM
  state1_SLICED e,
  state1_REF ref_2,
  state1_REF e_ref
WHERE
  (e_ref.object_referenceId = e.object_referenceId) AND
  (e_ref.c$9 = 'stateD') AND
  (e_ref.c$6 = ref_2.c$6) AND
  (ref_2.c$7 = 'stateD')
;
drop view state1_union
;
CREATE VIEW state1_union
AS
SELECT     ref_2.object_referenceId, e_ref.c$8 || ':' || e.object_objectId object_objectId, e.object_idx, e.object__class, e.p$$object_parent__referenceid, 
                      e.p$$object_parent__objectid, e.created_at, e.modified_at, e.created_by, e.modified_by, e.object__valid_from, e.object__valid_to, 
                      e.object__invalidated_at, e_ref.c$8 || ':' || e.object__state_id object__state_id, e.object__state_number, e.state_attr, e.long_values, e.any_u_r_i_val, 
                      e.boolean_val, e.date_val, e.date_time_val, e.decimal_val, e.duration_val, e.integer_val, e.long_val, e.short_val
FROM         state1_SLICED e, state1_REF ref_2, state1_REF e_ref
WHERE     (e_ref.object_referenceId = e.object_referenceId) AND (e_ref.c$9 = 'stateD') AND (e_ref.c$6 = ref_2.c$6) AND (ref_2.c$7 = 'stateD') AND 
                      e.object__state_id IS NOT NULL
UNION ALL
SELECT     ref_2.object_referenceId, e_ref.c$8 || ':' || e.object_objectId object_objectId, e.object_idx, e.object__class, e.p$$object_parent__referenceid, 
                      e.p$$object_parent__objectid, e.created_at, e.modified_at, e.created_by, e.modified_by, e.object__valid_from, e.object__valid_to, 
                      e.object__invalidated_at, e.object__state_id, e.object__state_number, e.state_attr, e.long_values, e.any_u_r_i_val, e.boolean_val, e.date_val, 
                      e.date_time_val, e.decimal_val, e.duration_val, e.integer_val, e.long_val, e.short_val
FROM         state1_SLICED e, state1_REF ref_2, state1_SLICED e1, state1_REF e_ref
WHERE     (e_ref.object_referenceId = e.object_referenceId) AND (e_ref.c$9 = 'stateD') AND (e_ref.c$6 = ref_2.c$6) AND (ref_2.c$7 = 'stateD') AND 
                      e.object__state_id IS NULL AND e.object_referenceId = e1.object_referenceId AND e.object_objectid = e1.object_objectid AND e1.object_idx = 0
;
drop view state1_shared
;
CREATE VIEW state1_shared
AS
SELECT     *
FROM         state1_union
;
drop view state1_shared_native
;
CREATE VIEW state1_shared_native
AS
SELECT     ref_2.object_referenceId, e_ref.c$8 || ':' || e.object_objectId object_objectId, e.object_idx, e.object__class, e.p$$object_parent__referenceid, 
                      e.p$$object_parent__objectid, e.created_at, e.modified_at, e.created_by, e.modified_by, e.object__valid_from, e.object__valid_to, 
                      e.object__invalidated_at, e_ref.c$8 || ':' || e.object__state_id object__state_id, e.object__state_number, e.state_attr, e.long_values, e.any_u_r_i_val, 
                      e.boolean_val, e.date_val, e.date_time_val, e.decimal_val, e.duration_val, e.integer_val, e.long_val, e.short_val
FROM         state1_NATIVE e, state1_REF ref_2, state1_REF e_ref
WHERE     (e_ref.object_referenceId = e.object_referenceId) AND (e_ref.c$9 = 'stateD') AND (e_ref.c$6 = ref_2.c$6) AND (ref_2.c$7 = 'stateD') AND 
                      e.object__state_id IS NOT NULL
UNION ALL
SELECT     ref_2.object_referenceId, e_ref.c$8 || ':' || e.object_objectId object_objectId, e.object_idx, e.object__class, e.p$$object_parent__referenceid, 
                      e.p$$object_parent__objectid, e.created_at, e.modified_at, e.created_by, e.modified_by, e.object__valid_from, e.object__valid_to, 
                      e.object__invalidated_at, e.object__state_id, e.object__state_number, e.state_attr, e.long_values, e.any_u_r_i_val, e.boolean_val, e.date_val, 
                      e.date_time_val, e.decimal_val, e.duration_val, e.integer_val, e.long_val, e.short_val
FROM         state1_NATIVE e, state1_REF ref_2, state1_NATIVE e1, state1_REF e_ref
WHERE     (e_ref.object_referenceId = e.object_referenceId) AND (e_ref.c$9 = 'stateD') AND (e_ref.c$6 = ref_2.c$6) AND (ref_2.c$7 = 'stateD') AND 
                      e.object__state_id IS NULL AND e.object_referenceId = e1.object_referenceId AND e.object_objectid = e1.object_objectid AND e1.object_idx = 0
;

drop view datestate1_shared_exclusive
;
CREATE VIEW datestate1_shared_exclusive
AS
SELECT     ref_2.object_referenceId, e_ref.c$8 || ':' || e.object_objectId object_objectId, e.object_idx, e.object__class, e.p$$object_parent__referenceid, 
                      e.p$$object_parent__objectid, e.created_at, e.modified_at, e.created_by, e.modified_by, e.object__valid_from, e.object__valid_to, 
                      e.object__invalidated_at, e_ref.c$8 || ':' || e.object__state_id object__state_id, e.object__state_number, e.state_attr, e.long_values, e.any_u_r_i_val, 
                      e.boolean_val, e.date_val, e.date_time_val, e.decimal_val, e.duration_val, e.integer_val, e.long_val, e.short_val
FROM         datestate1_NATIVE e, state1_REF ref_2, state1_REF e_ref
WHERE     (e_ref.c$5 = 'exclusive') AND (e_ref.object_referenceId = e.object_referenceId) AND (e_ref.c$9 = 'stateD') AND (e_ref.c$6 = ref_2.c$6) AND (ref_2.c$7 = 'stateD') AND 
                      e.object__state_id IS NOT NULL
UNION ALL
SELECT     ref_2.object_referenceId, e_ref.c$8 || ':' || e.object_objectId object_objectId, e.object_idx, e.object__class, e.p$$object_parent__referenceid, 
                      e.p$$object_parent__objectid, e.created_at, e.modified_at, e.created_by, e.modified_by, e.object__valid_from, e.object__valid_to, 
                      e.object__invalidated_at, e.object__state_id, e.object__state_number, e.state_attr, e.long_values, e.any_u_r_i_val, e.boolean_val, e.date_val, 
                      e.date_time_val, e.decimal_val, e.duration_val, e.integer_val, e.long_val, e.short_val
FROM         datestate1_NATIVE e, state1_REF ref_2, datestate1_NATIVE e1, state1_REF e_ref
WHERE     (e_ref.c$5 = 'exclusive') AND (e_ref.object_referenceId = e.object_referenceId) AND (e_ref.c$9 = 'stateD') AND (e_ref.c$6 = ref_2.c$6) AND (ref_2.c$7 = 'stateD') AND 
                      e.object__state_id IS NULL AND e.object_referenceId = e1.object_referenceId AND e.object_objectid = e1.object_objectid AND e1.object_idx = 0
;

drop view datestate1_shared_inclusive
;
CREATE VIEW datestate1_shared_inclusive
AS
SELECT     ref_2.object_referenceId, e_ref.c$8 || ':' || e.object_objectId object_objectId, e.object_idx, e.object__class, e.p$$object_parent__referenceid, 
                      e.p$$object_parent__objectid, e.created_at, e.modified_at, e.created_by, e.modified_by, e.state_valid_from, e.state_valid_to, 
                      e.object__invalidated_at, e_ref.c$8 || ':' || e.object__state_id object__state_id, e.object__state_number, e.state_attr, e.long_values, e.any_u_r_i_val, 
                      e.boolean_val, e.date_val, e.date_time_val, e.decimal_val, e.duration_val, e.integer_val, e.long_val, e.short_val
FROM         datestate1_NATIVE e, state1_REF ref_2, state1_REF e_ref
WHERE     (e_ref.c$5 = 'inclusive') AND (e_ref.object_referenceId = e.object_referenceId) AND (e_ref.c$9 = 'stateD') AND (e_ref.c$6 = ref_2.c$6) AND (ref_2.c$7 = 'stateD') AND 
                      e.object__state_id IS NOT NULL
UNION ALL
SELECT     ref_2.object_referenceId, e_ref.c$8 || ':' || e.object_objectId object_objectId, e.object_idx, e.object__class, e.p$$object_parent__referenceid, 
                      e.p$$object_parent__objectid, e.created_at, e.modified_at, e.created_by, e.modified_by, e.state_valid_from, e.state_valid_to, 
                      e.object__invalidated_at, e.object__state_id, e.object__state_number, e.state_attr, e.long_values, e.any_u_r_i_val, e.boolean_val, e.date_val, 
                      e.date_time_val, e.decimal_val, e.duration_val, e.integer_val, e.long_val, e.short_val
FROM         datestate1_NATIVE e, state1_REF ref_2, datestate1_NATIVE e1, state1_REF e_ref
WHERE     (e_ref.c$5 = 'inclusive') AND (e_ref.object_referenceId = e.object_referenceId) AND (e_ref.c$9 = 'stateD') AND (e_ref.c$6 = ref_2.c$6) AND (ref_2.c$7 = 'stateD') AND 
                      e.object__state_id IS NULL AND e.object_referenceId = e1.object_referenceId AND e.object_objectid = e1.object_objectid AND e1.object_idx = 0
;
