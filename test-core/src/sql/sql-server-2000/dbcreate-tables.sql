if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[app1_Address]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[app1_Address]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[app1_Address_N]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[app1_Address_N]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[app1_DOC]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[app1_DOC]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[app1_Invoice]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[app1_Invoice]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[app1_InvoicePosition]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[app1_InvoicePosition]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[app1_Member]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[app1_Member]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[app1_PersonGroup]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[app1_PersonGroup]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[app1_SLICED]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[app1_SLICED]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[app1_Segment]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[app1_Segment]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[audit1_Segment]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[audit1_Segment]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[audit_SLICED]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[audit_SLICED]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[role1_RoleType]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[role1_RoleType]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[role1_SLICED]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[role1_SLICED]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[role1_Segment]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[role1_Segment]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[state1_SLICED]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[state1_SLICED]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[test_CB_SLICED]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[test_CB_SLICED]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[test_SLB_SLICED]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[test_SLB_SLICED]
GO

CREATE TABLE [dbo].[app1_Address] (
	[object_rid] nvarchar(100) NOT NULL,
	[object_oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NOT NULL,
	[p$$object_parent__rid] nvarchar(100) NULL, 
	[p$$object_parent__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[object__class] [nvarchar] (200) NULL ,
	[description] [nvarchar] (200) NULL ,
	[created_at] [varchar] (20) NULL ,
	[modified_at] [varchar] (20) NULL ,
	[postal_code] [varchar] (100) NULL ,
	[street] [varchar] (100) NULL ,
	[country] [varchar] (100) NULL ,
	[city] [varchar] (100) NULL ,
	[house_number] [varchar] (100) NULL ,
	[address] [varchar] (100) NULL,
	CONSTRAINT PK_PREFS_Preferences PRIMARY KEY (object_rid, object_oid)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[app1_Address_N] (
	[object_rid] nvarchar(100) NOT NULL,
	[object_oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NOT NULL,
	[object_idx] [int] NOT NULL ,
	[modified_by] [varchar] (50) NULL ,
	[created_by] [varchar] (50) NULL,
	[address_line] [varchar] (100) NULL ,
	CONSTRAINT PK_app1_Address PRIMARY KEY (object_rid, object_oid, object_idx)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[app1_DOC] (
	[object_rid] nvarchar(100) NOT NULL,
	[object_oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NOT NULL,
	[object_idx] [bigint] NOT NULL ,
	[p$$object_parent__rid] nvarchar(100) NULL, 
	[p$$object_parent__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[created_at] [varchar] (20) NULL ,
	[created_by] [varchar] (200) NULL ,
	[modified_at] [varchar] (20) NULL ,
	[modified_by] [varchar] (200) NULL ,
	[object__class] [varchar] (60) NULL ,
	[description] [varchar] (100) NULL ,
	[content] [image] NULL ,
	[keyword] [varchar] (32) NULL,
	CONSTRAINT PK_app1_DOC PRIMARY KEY (object_rid, object_oid, object_idx) 
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

CREATE TABLE [dbo].[app1_Invoice] (
	[object_rid] nvarchar(100) NOT NULL,
	[object_oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NOT NULL,
	[object_idx] [int] NOT NULL ,
	[p$$object_parent__rid] nvarchar(100) NULL, 
	[p$$object_parent__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[object__class] [nvarchar] (200) NULL ,
	[description] [nvarchar] (200) NULL ,
	[product_group_id] [varchar] (100) NULL ,
	[created_at] [varchar] (20) NULL ,
	[modified_at] [varchar] (20) NULL ,
	[created_by] [varchar] (100) NULL ,
	[modified_by] [varchar] (100) NULL,
	CONSTRAINT PK_app1_Invoice PRIMARY KEY (object_rid, object_oid, object_idx) 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[app1_InvoicePosition] (
	[object_rid] nvarchar(100) NOT NULL,
	[object_oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NOT NULL,
	[object_idx] [int] NOT NULL ,
	[p$$object_parent__rid] nvarchar(100) NULL, 
	[p$$object_parent__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[object__class] [nvarchar] (200) NULL ,
	[description] [nvarchar] (200) NULL ,
	[created_at] [varchar] (20) NULL ,
	[modified_at] [varchar] (20) NULL ,
	[created_by] [varchar] (100) NULL ,
	[modified_by] [varchar] (100) NULL ,
	[product_id] [varchar] (50) NULL,
	CONSTRAINT PK_app1_InvoicePosition PRIMARY KEY (object_rid, object_oid, object_idx) 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[app1_Member] (
	[object_rid] nvarchar(100) NOT NULL,
	[object_oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NOT NULL,
	[object_idx] [int] NOT NULL ,
	[p$$object_parent__rid] nvarchar(100) NULL, 
	[p$$object_parent__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[object__class] [nvarchar] (200) NULL ,
	[description] [nvarchar] (200) NULL ,
	[modified_at] [varchar] (20) NULL ,
	[created_at] [varchar] (20) NULL ,
	[modified_by] [varchar] (50) NULL ,
	[created_by] [varchar] (50) NULL ,
	[m1] [varchar] (200) NULL ,
	[p$$m1__rid] nvarchar(100) NULL, 
	[p$$m1__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[m2] [varchar] (200) NULL,
	[p$$m2__rid] nvarchar(100) NULL, 
	[p$$m2__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	CONSTRAINT PK_app1_Member PRIMARY KEY (object_rid, object_oid, object_idx) 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[app1_PersonGroup] (
	[object_rid] nvarchar(100) NOT NULL,
	[object_oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NOT NULL,
	[object_idx] [int] NOT NULL ,
	[p$$object_parent__rid] nvarchar(100) NULL, 
	[p$$object_parent__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[object__class] [nvarchar] (200) NULL ,
	[description] [nvarchar] (200) NULL ,
	[modified_at] [varchar] (20) NULL ,
	[created_at] [varchar] (20) NULL ,
	[modified_by] [varchar] (50) NULL ,
	[created_by] [varchar] (50) NULL ,
	[name] [varchar] (100) NULL,
	CONSTRAINT PK_app1_PersonGroup PRIMARY KEY (object_rid, object_oid, object_idx) 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[app1_SLICED] (
	[object_rid] nvarchar(100) NOT NULL,
	[object_oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NOT NULL,
	[object_idx] [bigint] NOT NULL ,
	[p$$object_parent__rid] nvarchar(100) NULL, 
	[p$$object_parent__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[created_at] [varchar] (20) NULL ,
	[created_by] [varchar] (200) NULL ,
	[modified_at] [varchar] (20) NULL ,
	[modified_by] [varchar] (200) NULL ,
	[object__class] [varchar] (60) NULL ,
	[m1] [varchar] (200) NULL ,
	[last_name] [varchar] (200) NULL ,
	[house_number] [varchar] (200) NULL ,
	[city] [varchar] (200) NULL ,
	[foreign_id] [varchar] (200) NULL ,
	[postal_code] [varchar] (200) NULL ,
	[description] [varchar] (200) NULL ,
	[assigned_address] [varchar] (200) NULL ,
	[p$$assigned_address__rid] nvarchar(100) NULL, 
	[p$$assigned_address__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[product_id] [varchar] (200) NULL ,
	[salutation] [varchar] (200) NULL ,
	[street] [varchar] (200) NULL ,
	[address_line] [varchar] (200) NULL ,
	[address] [varchar] (200) NULL ,
	[text] [varchar] (200) NULL ,
	[birthdate] [varchar] (8) NULL ,
	[member_of_group] [varchar] (200) NULL ,
	[birthdate_as_date_time] [varchar] (200) NULL ,
	[person_group] [varchar] (200) NULL ,
	[p$$person_group__rid] nvarchar(100) NULL, 
	[p$$person_group__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[country] [varchar] (200) NULL ,
	[sex] [int] NULL ,
	[given_name] [varchar] (200) NULL ,
	[product_group_id] [varchar] (200) NULL ,
	[place_of_birth] [varchar] (200) NULL,
	CONSTRAINT PK_app1_SLICED PRIMARY KEY (object_rid, object_oid, object_idx) 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[app1_Segment] (
	[object_rid] nvarchar(100) NOT NULL,
	[object_oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NOT NULL,
	[object_idx] [int] NOT NULL ,
	[p$$object_parent__rid] nvarchar(100) NULL, 
	[p$$object_parent__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[object__class] [nvarchar] (200) NULL ,
	[description] [nvarchar] (200) NULL ,
	CONSTRAINT PK_app1_Segment PRIMARY KEY (object_rid, object_oid, object_idx)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[audit1_Segment] (
	[object_rid] nvarchar(100) NOT NULL,
	[object_oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NOT NULL,
	[object_idx] [int] NOT NULL ,
	[p$$object_parent__rid] nvarchar(100) NULL, 
	[p$$object_parent__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[object__class] [nvarchar] (200) NULL ,
	[description] [nvarchar] (200) NULL ,
	CONSTRAINT PK_audit1_Segment PRIMARY KEY (object_rid, object_oid, object_idx)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[audit_SLICED] (
	[object_rid] nvarchar(100) NOT NULL,
	[object_oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NOT NULL,
	[object_idx] [bigint] NOT NULL ,
	[p$$object_parent__rid] nvarchar(100) NULL, 
	[p$$object_parent__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[created_at] [varchar] (20) NULL ,
	[created_by] [varchar] (200) NULL ,
	[modified_at] [varchar] (20) NULL ,
	[modified_by] [varchar] (200) NULL ,
	[object__class] [varchar] (60) NULL ,
	[m1] [varchar] (200) NULL ,
	[last_name] [varchar] (200) NULL ,
	[house_number] [varchar] (200) NULL ,
	[city] [varchar] (200) NULL ,
	[foreign_id] [varchar] (200) NULL ,
	[postal_code] [varchar] (200) NULL ,
	[description] [varchar] (200) NULL ,
	[assigned_address] [varchar] (200) NULL ,
	[product_id] [varchar] (200) NULL ,
	[salutation] [varchar] (200) NULL ,
	[street] [varchar] (200) NULL ,
	[address_line] [varchar] (200) NULL ,
	[address] [varchar] (200) NULL ,
	[text] [varchar] (200) NULL ,
	[birthdate] [varchar] (200) NULL ,
	[member_of_group] [varchar] (200) NULL ,
	[birthdate_as_date_time] [varchar] (200) NULL ,
	[person_group] [varchar] (200) NULL ,
	[country] [varchar] (200) NULL ,
	[sex] [int] NULL ,
	[given_name] [varchar] (200) NULL ,
	[product_group_id] [varchar] (200) NULL ,
	[involved] [varchar] (200) NULL ,
	[p$$involved__rid] nvarchar(100) NULL, 
	[p$$involved__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[place_of_birth] [varchar] (200) NULL,
	CONSTRAINT PK_audit_SLICED PRIMARY KEY (object_rid, object_oid, object_idx) 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[role1_RoleType] (
	[object_rid] nvarchar(100) NOT NULL,
	[object_oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NOT NULL,
	[object_idx] [int] NOT NULL ,
	[p$$object_parent__rid] nvarchar(100) NULL, 
	[p$$object_parent__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[object__class] [nvarchar] (200) NULL ,
	[created_at] [nvarchar] (20) NULL ,
	[modified_at] [nvarchar] (20) NULL ,
	[created_by] [nvarchar] (100) NULL ,
	[modified_by] [nvarchar] (100) NULL ,
	[description] [nvarchar] (200) NULL ,
	[core_role] [nvarchar] (20) NULL ,
	[name] [varchar] (50) NULL ,
	[aaaa] [varchar] (50) NULL ,
	[an_u_r_i] [varchar] (200) NULL,
	CONSTRAINT PK_role1_RoleType PRIMARY KEY (object_rid, object_oid, object_idx)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[role1_SLICED] (
	[object_rid] nvarchar(100) NOT NULL,
	[object_oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NOT NULL,
	[object_idx] [bigint] NOT NULL ,
	[p$$object_parent__rid] nvarchar(100) NULL, 
	[p$$object_parent__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[created_at] [varchar] (20) NULL ,
	[created_by] [varchar] (200) NULL ,
	[modified_at] [varchar] (20) NULL ,
	[modified_by] [varchar] (200) NULL ,
	[object__class] [varchar] (100) NULL ,
	[rara1$rcrara_name] [varchar] (60) NULL ,
	[tfira1$object_class] [varchar] (100) NULL ,
	[tfira2$object_class] [varchar] (60) NULL ,
	[rara1$gggg] [varchar] (60) NULL ,
	[tfira2$dddd] [varchar] (60) NULL ,
	[tfira2$ident] [varchar] (60) NULL ,
	[grp_ra$rcra_name] [varchar] (60) NULL ,
	[rce_name] [varchar] (60) NULL ,
	[tfira1$rcra_name] [varchar] (60) NULL ,
	[ra1$dddd] [varchar] (60) NULL ,
	[role4$object_class] [varchar] (100) NULL ,
	[id_r_t_ara1$dddd] [varchar] (60) NULL ,
	[_clerk$business_phone] [varchar] (60) NULL ,
	[id_r_t_ara2$dddd] [varchar] (60) NULL ,
	[tfira0$rcra_name] [varchar] (60) NULL ,
	[tfira0$object_class] [varchar] (100) NULL ,
	[grp_ra$object_class] [varchar] (100) NULL ,
	[role_c_r1$object_class] [varchar] (100) NULL ,
	[role2$object_class] [varchar] (100) NULL ,
	[role2$ident] [varchar] (60) NULL ,
	[role_c_r1$dddd] [varchar] (60) NULL ,
	[id_multira$object_class] [varchar] (100) NULL ,
	[id_r_t_ara1$object_class] [varchar] (100) NULL ,
	[extra_role$ident] [varchar] (60) NULL ,
	[role3$object_class] [varchar] (100) NULL ,
	[role3$rcrb_name] [varchar] (60) NULL ,
	[role_c_r2$eeee] [varchar] (60) NULL ,
	[id_multirb$object_class] [varchar] (100) NULL ,
	[_manager$object_class] [varchar] (100) NULL ,
	[core_role] [varchar] (60) NULL ,
	[_manager$business_phone] [varchar] (60) NULL ,
	[extra_role$object_class] [varchar] (100) NULL ,
	[sgrp_rb$no_role] [varchar] (60) NULL ,
	[id_r_t_ara2$object_class] [varchar] (100) NULL ,
	[role4$rcrara_name] [varchar] (60) NULL ,
	[sgrp_rb$ffff] [varchar] (60) NULL ,
	[role1$object_class] [varchar] (100) NULL ,
	[role_c_r1$ident] [varchar] (60) NULL ,
	[grp_ra$dddd] [varchar] (60) NULL ,
	[rara1$object_class] [varchar] (100) NULL ,
	[id_r_t_ara2$ident] [varchar] (60) NULL ,
	[grp_ra$group] [varchar] (60) NULL ,
	[aaaa] [varchar] (60) NULL ,
	[role_c_r1$rcra_name] [varchar] (60) NULL ,
	[bbbb] [varchar] (60) NULL ,
	[id_r_t_arara1$rcrara_name] [varchar] (60) NULL ,
	[role_c_r2$dddd] [varchar] (60) NULL ,
	[id_multirb$ident] [varchar] (60) NULL ,
	[role1$ident] [varchar] (60) NULL ,
	[name] [varchar] (60) NULL ,
	[_clerk$object_class] [varchar] (100) NULL ,
	[tfira2$rcra_name] [varchar] (60) NULL ,
	[_clerk$division] [varchar] (60) NULL ,
	[extra_role$rcra_name] [varchar] (60) NULL ,
	[id_r_t_arara1$gggg] [varchar] (60) NULL ,
	[role_c_r2$rcra_name] [varchar] (60) NULL ,
	[role_c_r2$object_class] [varchar] (100) NULL ,
	[ra1$ident] [varchar] (60) NULL ,
	[id_r_t_ara1$ident] [varchar] (60) NULL ,
	[role1$rcraName] [varchar] (60) NULL ,
	[id_r_t_ara1$rcra_name] [varchar] (60) NULL ,
	[role_b_id] [varchar] (60) NULL ,
	[birthdate] [varchar] (60) NULL ,
	[tfira0$ident] [varchar] (60) NULL ,
	[role_c_r2$rcrae_name] [varchar] (60) NULL ,
	[tfira1$dddd] [varchar] (60) NULL ,
	[id_multira$rcra_name] [varchar] (60) NULL ,
	[ra1$object_class] [varchar] (60) NULL ,
	[id_r_t_ara2$rcra_name] [varchar] (60) NULL ,
	[id_multirb$rcra_name] [varchar] (60) NULL ,
	[rc_name] [varchar] (60) NULL ,
	[role2$rcra_name] [varchar] (60) NULL ,
	[extra_role$dddd] [varchar] (60) NULL ,
	[id_multira$dddd] [varchar] (60) NULL ,
	[_manager$division] [varchar] (60) NULL ,
	[id_r_t_arara1$object_class] [varchar] (100) NULL ,
	[role_c_r2$ident] [varchar] (60) NULL ,
	[id_multira$ident] [varchar] (60) NULL ,
	[grp_ra$ident] [varchar] (60) NULL ,
	[id_multirb$dddd] [varchar] (60) NULL ,
	[sgrp_rb$rcrb_name] [varchar] (60) NULL ,
	[tfira0$dddd] [varchar] (60) NULL ,
	[sgrp_rb$object_class] [varchar] (100) NULL ,
	[ra1$rcraName] [varchar] (60) NULL ,
	[cccc] [varchar] (60) NULL ,
	[tfira1$ident] [varchar] (60) NULL ,
	[role_a_s1$rcra_name] [varchar] (60) NULL ,
	[role_a_s1$object__class] [varchar] (100) NULL ,
	[role_a_s1$ident] [varchar] (60) NULL ,
	[role_a_s1$dddd] [varchar] (60) NULL ,
	[role_b_f2$rcra_name] [varchar] (60) NULL ,
	[role_b_f2$object__class] [varchar] (100) NULL ,
	[role_b_f2$ident] [varchar] (60) NULL ,
	[role_b_f2$eeee] [varchar] (60) NULL ,
	[role_b_f2$dddd] [varchar] (60) NULL ,
	[role_b_f2$rcrae_name] [varchar] (60) NULL ,
	[role_b_f3$object__class] [varchar] (100) NULL ,
	[role_c_r1$object__class] [varchar] (100) NULL ,
	[role_c_r2$object__class] [varchar] (100) NULL ,
	[role_c_r3$object__class] [varchar] (100) NULL ,
	[role_c_r3$rcrb_name] [varchar] (60) NULL ,
	[role_c_r3$rcrb_u_r_i] [varchar] (256) NULL ,
	[role_c_r3$ffff] [varchar] (60) NULL ,
	[non_exist$object__class] [varchar] (100) NULL ,
	[role_r_a_r2$rcra_name] [varchar] (60) NULL ,
	[role_r_a_r2$object__class] [varchar] (100) NULL ,
	[role_r_a_r2$ident] [varchar] (60) NULL ,
	[role_r_a_r2$dddd] [varchar] (60) NULL ,
	[role_r_a_r2$eeee] [varchar] (60) NULL ,
	[role_r_a_r2$rcrae_name] [varchar] (60) NULL ,
	[role_r_a_r5$rcraera_name] [varchar] (60) NULL ,
	[role_r_a_r5$object__class] [varchar] (100) NULL ,
	[role_r_a_r5$hhhh] [varchar] (60) NULL ,
	[role_e_x_c3$object__class] [varchar] (100) NULL ,
	[role_e_x_c3$rcrb_name] [varchar] (60) NULL ,
	[role_e_x_c3$ffff] [varchar] (60) NULL ,
	[role_e_x_c3$rcrb_u_r_i] [varchar] (256) NULL ,
	[r1$rcra_name] [varchar] (60) NULL ,
	[r1$object__class] [varchar] (100) NULL ,
	[r1$ident] [varchar] (60) NULL ,
	[r1$dddd] [varchar] (60) NULL ,
	[r2$object__class] [varchar] (100) NULL ,
	[r2$rcrara_name] [varchar] (60) NULL ,
	[r2$gggg] [varchar] (60) NULL ,
	[role11$rcra_name] [varchar] (60) NULL ,
	[role11$object__class] [varchar] (100) NULL ,
	[role11$ident] [varchar] (60) NULL ,
	[role11$dddd] [varchar] (60) NULL ,
	[role12$object__class] [varchar] (100) NULL ,
	[role12$rcrb_u_r_i] [varchar] (256) NULL ,
	[role12$rcrb_name] [varchar] (60) NULL ,
	[role12$ffff] [varchar] (60) NULL ,
	[role_t_c1$object__class] [varchar] (100) NULL ,
	[role_t_c1$rcrb_name] [varchar] (60) NULL ,
	[role_t_c1$ffff] [varchar] (60) NULL ,
	[role_t_c1$rcrb_u_r_i] [varchar] (256) NULL ,
	[ra1$rcra_name] [varchar] (60) NULL ,
	[ra1$object__class] [varchar] (100) NULL ,
	[rara1$object__class] [varchar] (100) NULL ,
	[tfira0$object__class] [varchar] (100) NULL ,
	[tfira1$object__class] [varchar] (100) NULL ,
	[tfira2$object__class] [varchar] (100) NULL ,
	[extra_role$object__class] [varchar] (100) NULL ,
	[id_multira$object__class] [varchar] (100) NULL ,
	[id_multirb$object__class] [varchar] (100) NULL ,
	[grp__ra$rcra_name] [varchar] (60) NULL ,
	[grp__ra$object__class] [varchar] (100) NULL ,
	[grp__ra$ident] [varchar] (60) NULL ,
	[grp__ra$dddd] [varchar] (60) NULL ,
	[grp__ra$group] [varchar] (256) NULL ,
	[sgrp__rb$object__class] [varchar] (100) NULL ,
	[sgrp__rb$ffff] [varchar] (60) NULL ,
	[sgrp__rb$rcrb_name] [varchar] (60) NULL ,
	[sgrp__rb$no_role] [varchar] (256) NULL ,
	[role1$rcra_name] [varchar] (60) NULL ,
	[role1$object__class] [varchar] (100) NULL ,
	[role2$object__class] [varchar] (100) NULL ,
	[role3$object__class] [varchar] (100) NULL ,
	[role4$object__class] [varchar] (100) NULL ,
	[id_r_t_ara1$object__class] [varchar] (100) NULL ,
	[id_r_t_arara1$object__clas] [varchar] (100) NULL ,
	[id_r_t_arara1$object__class] [varchar] (100) NULL ,
	[id_r_t_ara2$object__class] [varchar] (100) NULL ,
	[id_r_t_q_ara1$rcra_name] [varchar] (60) NULL ,
	[id_r_t_q_ara1$object__class] [varchar] (100) NULL ,
	[id_r_t_q_ara1$ident] [varchar] (60) NULL ,
	[id_r_t_q_ara1$dddd] [varchar] (60) NULL ,
	[id_r_t_q_ara2$rcra_name] [varchar] (60) NULL ,
	[id_r_t_q_ara2$object__class] [varchar] (100) NULL ,
	[id_r_t_q_ara2$ident] [varchar] (60) NULL ,
	[id_r_t_q_ara2$dddd] [varchar] (60) NULL ,
	[id_r_t_q_arb1$object__class] [varchar] (100) NULL ,
	[id_r_t_q_arb1$rcrb_name] [varchar] (60) NULL ,
	[id_r_t_q_arb2$object__class] [varchar] (100) NULL ,
	[id_r_t_q_arb2$rcrb_name] [varchar] (60) NULL ,
	[_clerk$object__class] [varchar] (50) NULL ,
	[_manager$object__class] [varchar] (50) NULL ,
	[_customer$object__class] [varchar] (50) NULL ,
	[_customer$total_purchase] [decimal](18, 0) NULL ,
	[rcd_name] [varchar] (100) NULL ,
	[kkkk] [varchar] (100) NULL ,
	[id_r_t_a_ird1_t_n$object__class] [varchar] (100) NULL ,
	[id_r_t_a_ird1_t_n$rcdra_name] [varchar] (100) NULL ,
	[id_r_t_a_ird1_t_n$mmmm] [varchar] (100) NULL ,
	[_drole_a$object__class] [varchar] (100) NULL ,
	[_drole_a$rcdra_name] [varchar] (100) NULL ,
	[_drole_a$mmmm] [varchar] (100) NULL ,
	[ahv$register_id] [varchar] (100) NULL ,
	[ahv$object__class] [varchar] (100) NULL ,
	CONSTRAINT PK_role1_SLICED PRIMARY KEY (object_rid, object_oid, object_idx)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[role1_Segment] (
	[object_rid] nvarchar(100) NOT NULL,
	[object_oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NOT NULL,
	[object_idx] [int] NOT NULL ,
	[p$$object_parent__rid] nvarchar(100) NULL, 
	[p$$object_parent__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[object__class] [nvarchar] (200) NULL ,
	[description] [nvarchar] (200) NULL ,
	CONSTRAINT PK_role1_Segment PRIMARY KEY (object_rid, object_oid, object_idx)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[state1_SLICED] (
	[object_rid] nvarchar(100) NOT NULL,
	[object_oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NOT NULL,
	[object_idx] [bigint] NOT NULL ,
	[p$$object_parent__rid] nvarchar(100) NULL, 
	[p$$object_parent__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[created_at] [varchar] (20) NULL ,
	[created_by] [varchar] (200) NULL ,
	[modified_at] [varchar] (20) NULL ,
	[modified_by] [varchar] (200) NULL ,
	[object__valid_from] [varchar] (20) NULL ,
	[object__valid_to] [varchar] (20) NULL ,
	[object__invalidated_at] [varchar] (20) NULL ,
	[object__class] [varchar] (100) NULL ,
	[object__state_number] [numeric](10, 0) NULL ,
	[object__state_id] [varchar] (100) NULL ,
	[state_attr] [varchar] (60) NULL ,
	[state_a] [varchar] (256) NULL ,
	[p$$state_a__rid] nvarchar(100) NULL, 
	[p$$state_a__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[value] [varchar] (60) NULL ,
	[num] [varchar] (60) NULL ,
	[state_a_derived] [varchar] (60) NULL ,
	[value_a_derived] [varchar] (60) NULL ,
	[multi_attr] [varchar] (60) NULL ,
	[a1] [varchar] (60) NULL ,
	[a2] [numeric](10, 0) NULL ,
	[rtr__r_a__$object__class] [varchar] (60) NULL ,
	[rtr__r_a__$name] [varchar] (60) NULL ,
	[rtr__r_a__$num] [varchar] (60) NULL ,
	[role1211$object__class] [varchar] (60) NULL ,
	[role1211$name] [varchar] (60) NULL ,
	[role1211$num] [varchar] (60) NULL ,
	[r1$object__class] [varchar] (60) NULL ,
	[r1$name] [varchar] (60) NULL ,
	[r1$num] [varchar] (60) NULL ,
	[role2#kl$object__class] [varchar] (60) NULL ,
	[role2#kl$name] [varchar] (60) NULL ,
	[role2#kl$num] [varchar] (60) NULL ,
	[_c_r__rt1$object__class] [varchar] (60) NULL ,
	[_c_r__rt1$name] [varchar] (60) NULL ,
	[_c_r__rt1$num] [varchar] (60) NULL ,
	[_r_r__rt1$object__class] [varchar] (60) NULL ,
	[_r_r__rt1$name] [varchar] (60) NULL ,
	[_r_r__rt1$num] [varchar] (60) NULL ,
	[name] [varchar] (60) NULL ,
	[aaaa] [varchar] (60) NULL ,
	[rc_name] [varchar] (60) NULL ,
	[bbbb] [varchar] (60) NULL ,
	[role_b_id] [varchar] (60) NULL ,
	[core_role] [varchar] (60) NULL ,
	[long_values] [bigint] NULL ,
	[any_u_r_i_val] [varchar] (200) NULL ,
	[boolean_val] [varchar] (10) NULL ,
	[date_val] [varchar] (20) NULL ,
	[date_time_val] [varchar] (20) NULL ,
	[decimal_val] [numeric](18, 6) NULL ,
	[duration_val] [varchar] (10) NULL ,
	[integer_val] [numeric](10, 0) NULL ,
	[long_val] [bigint] NULL ,
	[short_val] [numeric](10, 0) NULL ,
	[special_non_stated] [varchar] (256) NULL,
	[p$$special_non_stated__rid] nvarchar(100) NULL,
	[p$$special_non_stated__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	CONSTRAINT PK_state1_SLICED PRIMARY KEY (object_rid, object_oid, object_idx) 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[test_CB_SLICED] (
	[object_rid] [bigint] NOT NULL ,
	[object_oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NOT NULL,
	[object_idx] [bigint] NOT NULL ,
	[p$$object_parent__rid] nvarchar(100) NULL, 
	[p$$object_parent__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[created_at] [varchar] (20) NULL ,
	[created_by] [varchar] (200) NULL ,
	[modified_at] [varchar] (20) NULL ,
	[modified_by] [varchar] (200) NULL ,
	[object__class] [varchar] (60) NULL ,
	[cb_type] [varchar] (10) NULL ,
	[advice_text] [varchar] (200) NULL ,
	[cancels_c_b] [varchar] (512) NULL ,
	CONSTRAINT PK_test_CB_SLICED PRIMARY KEY (object_rid, object_oid, object_idx)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[test_SLB_SLICED] (
	[object_rid] [bigint] NOT NULL ,
	[object_oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NOT NULL,
	[object_idx] [bigint] NOT NULL ,
	[p$$object_parent__rid] nvarchar(100) NULL, 
	[p$$object_parent__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[created_at] [varchar] (20) NULL ,
	[created_by] [varchar] (200) NULL ,
	[modified_at] [varchar] (20) NULL ,
	[modified_by] [varchar] (200) NULL ,
	[object__class] [varchar] (60) NULL ,
	[slb_type] [varchar] (10) NULL ,
	[pos] [varchar] (512) NULL ,
	[p$$pos__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL ,
	[p$$pos__rid] nvarchar(100)  NULL ,
	[p$$pos_parent__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS ,
	[p$$pos_parent__rid] nvarchar(100) NULL ,
	[price] [numeric](18, 9) NULL ,
	[is_debit] [numeric](18, 9) NULL ,
	[is_long] [numeric](18, 9) NULL ,
	[price_currency] [varchar] (10) NULL ,
	[value_date] [varchar] (20) NULL ,
	[booking_date] [varchar] (20) NULL ,
	[quantity] [numeric](18, 9) NULL ,
	[quantity_absolute] [numeric](18, 9) NULL ,
	[visibility] [varchar] (10) NULL ,
	[admin_descr] [varchar] (100) NULL ,
	[description] [varchar] (200) NULL ,
	[cred_value] [varbinary] (100) NULL ,
	CONSTRAINT PK_test_SLB_SLICED PRIMARY KEY (object_rid, object_oid, object_idx)	
) ON [PRIMARY]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[app1_MessageTemplate]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[app1_MessageTemplate]
GO

CREATE TABLE [dbo].[app1_MessageTemplate] (
	[object_rid] nvarchar(100) NOT NULL,
	[object_oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NOT NULL,
	[object_idx] [int] NOT NULL ,
	[p$$object_parent__rid] nvarchar(100) NULL, 
	[p$$object_parent__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[object__class] [nvarchar] (200) NULL ,
	[description] [nvarchar] (200) NULL ,
	[created_at] [varchar] (20) NULL ,
	[modified_at] [varchar] (20) NULL ,
	[created_by] [varchar] (100) NULL ,
	[modified_by] [varchar] (100) NULL ,
	[text] [nvarchar] (200) NULL ,
	CONSTRAINT PK_app1_MessageTemplate PRIMARY KEY (object_rid, object_oid, object_idx)	
) ON [PRIMARY]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[EXTENSION_SEGMENT]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[EXTENSION_SEGMENT]
GO

CREATE TABLE [dbo].[EXTENSION_SEGMENT] (
  [object_rid] [varchar](200) not null, 
	[object_oid] [varchar](200) not null, 
	[object_idx] [int] not null, 
	[p$$object_parent__rid] nvarchar(100) NULL, 
	[p$$object_parent__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[object__class] [varchar](100), 
	[description] [varchar](200),
	CONSTRAINT PK_EXTENSION_SEGMENT PRIMARY KEY (object_rid, object_oid, object_idx)
) ON [PRIMARY]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[EXTENSION_Default]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[EXTENSION_Default]
GO

CREATE TABLE [dbo].[EXTENSION_Default] ( 
  [object_rid] [varchar](200) not null, 
	[object_oid] [varchar](200) not null, 
	[object_idx] [int] not null, 
	[p$$object_parent__rid] nvarchar(100) NULL, 
	[p$$object_parent__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[object__valid_from] [varchar](20), 
	[object__valid_to] [varchar](20), 
	[object__invalidated_at] [varchar](20), 
	[object__class] [varchar](100), 
	[object__state_number] [numeric](10,0), 
	[object__state_id] [varchar](100), 
	[segment] [varchar](200), 
	[value1] char(9), 
	[value2] smallint, 
	[value3] int, 
	[value4] bigint, 
	[value5] decimal(18, 2), 
	[value9] [varchar](200), 
	[value10] varbinary(2000), 
	[value11a] [varchar](20), 
	[value11b] [varchar](20), 
	[p$$segment_parent__rid] [varchar](200), 
	[p$$segment_parent__oid] [varchar](200), 
	[value6] [varchar](200), 
	[value7] [varchar](20), 
	[value8] [varchar](8), 
	CONSTRAINT PK_EXTENSION_Default PRIMARY KEY (object_rid, object_oid, object_idx)
) ON [PRIMARY]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[EXTENSION_NUMERIC]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[EXTENSION_NUMERIC]
GO

CREATE TABLE [dbo].[EXTENSION_NUMERIC] (
  [object_rid] [varchar](200) not null, 
	[object_oid] [varchar](200) not null, 
	[object_idx] [int] not null, 
	[object__valid_from] decimal(19, 3), 
	[object__valid_to] decimal(19, 3), 
	[object__invalidated_at] decimal(19, 3), 
	[object__class] [varchar](100), 
	[object__state_number] [numeric](10,0), 
	[object__state_id] [varchar](100), 
	[segment] [varchar](200), 
	[value1] tinyint, 
	[value2] smallint, 
	[value3] int, 
	[value4] bigint, 
	[value5] decimal(18, 2), 
	[value9] [varchar](200), 
	[value10] varbinary(2000), 
	[value11a] integer, 
	[value11b] decimal(12,3), 
	[p$$segment_parent__rid] [varchar](200), 
	[p$$segment_parent__oid] [varchar](200), 
	[value6] [varchar](200), 
	[value7] decimal(19, 3), 
	[value8] [varchar](8), 
	[p$$object_parent__rid] nvarchar(100) NULL, 
	[p$$object_parent__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	CONSTRAINT PK_EXTENSION_NUMERIC PRIMARY KEY (object_rid, object_oid, object_idx)
) ON [PRIMARY]

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[EXTENSION_NATIVE]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[EXTENSION_NATIVE]
GO

CREATE TABLE [dbo].[EXTENSION_NATIVE] (
  [object_rid] [varchar](200) not null, 
	[object_oid] [varchar](200) not null, 
	[object_idx] [int] not null, 
	[object__valid_from] datetime, 
	[object__valid_to] datetime, 
	[object__invalidated_at] datetime, 
	[object__class] [varchar](100), 
	[object__state_number] [numeric](10,0), 
	[object__state_id] [varchar](100), 
	[state_valid_from] DATETIME, 
	[state_valid_to] DATETIME, 
	[segment] [varchar](200), 
	[value1] bit, 
	[value2] smallint, 
	[value3] int, 
	[value4] bigint, 
	[value5] decimal(18, 2), 
	[value9] [varchar](200), 
	[value10] varbinary(2000), 
	[value11a] [varchar](20), 
	[value11b] [varchar](20), 
	[p$$segment_parent__rid] [varchar](200), 
	[p$$segment_parent__oid] [varchar](200), 
	[value6] [varchar](200), 
	[value7] datetime, 
	[value8] DATETIME, 
	[p$$object_parent__rid] nvarchar(100) NULL, 
	[p$$object_parent__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	CONSTRAINT PK_EXTENSION_NATIVE PRIMARY KEY (object_rid, object_oid, object_idx)
) ON [PRIMARY]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[generic1_Property]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[generic1_Property]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[generic1_Property_N]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[generic1_Property_N]
GO

CREATE TABLE [dbo].[generic1_Property] (
	[object_rid] nvarchar(100) NOT NULL,
	[object_oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NOT NULL,
	[p$$object_parent__rid] nvarchar(100) NULL, 
	[p$$object_parent__oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NULL,
	[object__class] [nvarchar] (200) NULL ,
	[description] [nvarchar] (200) NULL ,
	[created_at] [varchar] (20) NULL ,
	[modified_at] [varchar] (20) NULL ,
	CONSTRAINT PK_PREFS_generic1_Property PRIMARY KEY (object_rid, object_oid)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[generic1_Property_N] (
	[object_rid] nvarchar(100) NOT NULL,
	[object_oid] nvarchar(200) COLLATE SQL_Latin1_General_CP1_CS_AS NOT NULL,
	[object_idx] [int] NOT NULL ,
	[modified_by] [varchar] (50) NULL ,
	[created_by] [varchar] (50) NULL,
	[boolean_value] [varchar] (10) NULL ,
	[uri_value] [varchar] (200) NULL ,
	[decimal_value] [numeric](18, 6) NULL ,
	[string_value] [varchar] (200) NULL ,
	[integer_value] [numeric](10, 0) NULL ,
	CONSTRAINT PK_PREFS_generic1_Property_N PRIMARY KEY (object_rid, object_oid, object_idx)
) ON [PRIMARY]
GO
