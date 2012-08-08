if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[state1_StateD_shared]') and OBJECTPROPERTY(id, N'IsView') = 1)
drop view [dbo].[state1_StateD_shared]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[state1_StateD_shared_noUnion]') and OBJECTPROPERTY(id, N'IsView') = 1)
drop view [dbo].[state1_StateD_shared_noUnion]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[state1_StateD_shared_revolutionary]') and OBJECTPROPERTY(id, N'IsView') = 1)
drop view [dbo].[state1_StateD_shared_revolutionary]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[state1_StateD_shared_union]') and OBJECTPROPERTY(id, N'IsView') = 1)
drop view [dbo].[state1_StateD_shared_union]
GO

SET QUOTED_IDENTIFIER ON 
GO
SET ANSI_NULLS ON 
GO







-- providing view for shared relation to StateD
-- "new" fashioned way, without union
-- but not much faster, because e1 join is expensive 
-- Problem: object__state_id is not set correctly.
CREATE VIEW [dbo].[state1_StateD_shared_noUnion] AS
SELECT
  ref_2.object_referenceid,
  e_ref.c$8 + ':' + e.object_objectid object_objectid,
  e.object_idx,
  e.object__class,
  e.p$$object_parent__referenceid,
  e.p$$object_parent__objectid,
-- BasicObject
  e.created_at,
  e.modified_at,
  e.created_by,
  e.modified_by,
-- State
  e.object__valid_from,
  e.object__valid_to,
  e.object__invalidated_at,
  e.object__state_id,
  e.object__state_number,
-- Attributes
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
-- those attributes are required by the persistence plugin,
-- to support the order criterias defined in state plugin.
-- Others would be required, if sort definitions are made. 
  e1.object__state_id        p$$object__state_id$0,
  e1.modified_at             p$$modified_at$0,
  e1.object__valid_from      p$$object__valid_from$0
FROM
  state1_SLICED e,
  state1_SLICED e1,
  state1_REF ref_2,
  state1_REF e_ref
WHERE
  (e_ref.object_referenceid = e.object_referenceid) AND
  (e_ref.c$9 = 'stateD') AND
  (e_ref.c$6 = ref_2.c$4) AND
  (ref_2.c$7 = 'stateD') AND
  e.object_referenceid = e1.object_referenceid AND
  e.object_objectid = e1.object_objectid AND
  e1.object_idx = 0



GO
SET QUOTED_IDENTIFIER OFF 
GO
SET ANSI_NULLS ON 
GO

SET QUOTED_IDENTIFIER ON 
GO
SET ANSI_NULLS ON 
GO




-- very new and revolutionary way. Does only work if 
-- certain attributes are not used as a sort (or filter)
-- criteria. State-plugin must be changed not sort the
-- output as it does so far. 
CREATE VIEW [dbo].[state1_StateD_shared_revolutionary] AS
SELECT
  ref_2.object_referenceid,
  e_ref.c$8 + ':' + e.object_objectid object_objectid,
  e.object_idx,
  e.object__class,
  e.p$$object_parent__referenceid,
  e.p$$object_parent__objectid,
-- BasicObject
  e.created_at,
  e.modified_at,
  e.created_by,
  e.modified_by,
-- State
  e.object__valid_from,
  e.object__valid_to,
  e.object__invalidated_at,
  e.object__state_id,
  e.object__state_number,
-- Attributes
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
  (e_ref.object_referenceid = e.object_referenceid) AND
  (e_ref.c$8 = 'stateD') AND
  (e_ref.c$6 = ref_2.c$6) AND
  (ref_2.c$7 = 'stateD')



GO
SET QUOTED_IDENTIFIER OFF 
GO
SET ANSI_NULLS ON 
GO

SET QUOTED_IDENTIFIER ON 
GO
SET ANSI_NULLS ON 
GO


CREATE VIEW dbo.state1_StateD_shared_union
AS
SELECT     ref_2.object_referenceid, e_ref.c$8 + ':' + e.object_objectid object_objectid, e.object_idx, e.object__class, e.p$$object_parent__referenceid, 
                      e.p$$object_parent__objectid, e.created_at, e.modified_at, e.created_by, e.modified_by, e.object__valid_from, e.object__valid_to, 
                      e.object__invalidated_at, e_ref.c$8 + ':' + e.object__state_id object__state_id, e.object__state_number, e.state_attr, e.long_values, e.any_u_r_i_val, 
                      e.boolean_val, e.date_val, e.date_time_val, e.decimal_val, e.duration_val, e.integer_val, e.long_val, e.short_val
FROM         state1_SLICED e, state1_REF ref_2, state1_REF e_ref
WHERE     (e_ref.object_referenceid = e.object_referenceid) AND (e_ref.c$9 = 'stateD') AND (e_ref.c$6 = ref_2.c$6) AND (ref_2.c$7 = 'stateD') AND 
                      e.object__state_id IS NOT NULL
UNION ALL
SELECT     ref_2.object_referenceid, e_ref.c$8 + ':' + e.object_objectid object_objectid, e.object_idx, e.object__class, e.p$$object_parent__referenceid, 
                      e.p$$object_parent__objectid, e.created_at, e.modified_at, e.created_by, e.modified_by, e.object__valid_from, e.object__valid_to, 
                      e.object__invalidated_at, e.object__state_id, e.object__state_number, e.state_attr, e.long_values, e.any_u_r_i_val, e.boolean_val, e.date_val, 
                      e.date_time_val, e.decimal_val, e.duration_val, e.integer_val, e.long_val, e.short_val
FROM         state1_SLICED e, state1_REF ref_2, state1_SLICED e1, state1_REF e_ref
WHERE     (e_ref.object_referenceid = e.object_referenceid) AND (e_ref.c$9 = 'stateD') AND (e_ref.c$6 = ref_2.c$6) AND (ref_2.c$7 = 'stateD') AND 
                      e.object__state_id IS NULL AND e.object_referenceid = e1.object_referenceid AND e.object_objectid = e1.object_objectid AND e1.object_idx = 0


GO
SET QUOTED_IDENTIFIER OFF 
GO
SET ANSI_NULLS ON 
GO

SET QUOTED_IDENTIFIER ON 
GO
SET ANSI_NULLS ON 
GO


/* view to switch easily between the different versions of the view




*/
CREATE VIEW dbo.state1_StateD_shared
AS
SELECT     *
FROM         dbo.state1_StateD_shared_union


GO
SET QUOTED_IDENTIFIER OFF 
GO
SET ANSI_NULLS ON 
GO

