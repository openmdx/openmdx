/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: WBXMLPlugIn.java,v 1.9 2010/03/04 14:25:19 wfro Exp $
 * Description: AirSync 
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/04 14:25:19 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.application.airsync;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.wbxml.AbstractPlugIn;
import org.openmdx.base.wbxml.CodeResolution;
import org.openmdx.base.wbxml.CodeToken;
import org.openmdx.kernel.exception.BasicException;

/**
 * AirSync
 */
public class WBXMLPlugIn extends AbstractPlugIn {
    
    /**
     * The AirSync tag tables
     */
    private static final String[][] TAGS = {
        { // AirSync
            "Sync", // 0x05
            "Responses", // 0x06
            "Add", // 0x07
            "Change", // 0x08
            "Delete", // 0x09
            "Fetch", // 0x0A
            "SyncKey", // 0x0B
            "ClientId", // 0x0C
            "ServerId", // 0x0D
            "Status", // 0x0E
            "Collection", // 0x0F
            "Class", // 0x10
            "Version", // 0x11
            "CollectionId", // 0x12
            "GetChanges", // 0x13
            "MoreAvailable", // 0x14
            "WindowSize", // 0x15
            "Commands", // 0x16
            "Options", // 0x17
            "FilterType", // 0x18
            "Truncation", // 0x19
            "RTFTruncation", // 0x1A
            "Conflict", // 0x1B
            "Collections", // 0x1C
            "ApplicationData", // 0x1D
            "DeletesAsMoves", // 0x1E
            "NotifyGUID", // 0x1F
            "Supported", // 0x20
            "SoftDelete", // 0x21
            "MIMESupport", // 0x22
            "MIMETruncation", // 0x23
            "Wait", // 0x24
            "Limit", // 0x25
            "Partial", // 0x26
        },
        { // Contacts
            "Anniversary", // 0x05
            "AssistantName", // 0x06
            "AssistantTelephoneNumber", // 0x07
            "Birthday", // 0x08
            "Body", // 0x09
            "BodySize", // 0x0A
            "BodyTruncated", // 0x0B
            "Business2PhoneNumber", // 0x0C
            "BusinessAddressCity", // 0x0D
            "BusinessAddressCountry", // 0x0E
            "BusinessAddressPostalCode", // 0x0F
            "BusinessAddressState", // 0x10
            "BusinessAddressStreet", // 0x11
            "BusinessFaxNumber", // 0x12
            "BusinessPhoneNumber", // 0x13
            "CarPhoneNumber", // 0x14
            "Categories", // 0x15
            "Category", // 0x16
            "Children", // 0x17
            "Child", // 0x18
            "CompanyName", // 0x19
            "Department", // 0x1A
            "Email1Address", // 0x1B
            "Email2Address", // 0x1C
            "Email3Address", // 0x1D
            "FileAs", // 0x1E
            "FirstName", // 0x1F
            "Home2PhoneNumber", // 0x20
            "HomeAddressCity", // 0x21
            "HomeAddressCountry", // 0x22
            "HomeAddressPostalCode", // 0x23
            "HomeAddressState", // 0x24
            "HomeAddressStreet", // 0x25
            "HomeFaxNumber", // 0x26
            "HomePhoneNumber", // 0x27
            "JobTitle", // 0x28
            "LastName", // 0x29
            "MiddleName", // 0x2A
            "MobilePhoneNumber", // 0x2B
            "OfficeLocation", // 0x2C
            "OtherAddressCity", // 0x2D
            "OtherAddressCountry", // 0x2E
            "OtherAddressPostalCode", // 0x2F
            "OtherAddressState", // 0x30
            "OtherAddressStreet", // 0x31
            "PagerNumber", // 0x32
            "RadioPhoneNumber", // 0x33
            "Spouse", // 0x34
            "Suffix", // 0x35
            "Title", // 0x36
            "WebPage", // 0x37
            "YomiCompanyName", // 0x38
            "YomiFirstName", // 0x39
            "YomiLastName", // 0x3A
            "CompressedRTF", // 0x3B
            "Picture", // 0x3C
        },
        { // Email
            "Attachment", // 0x05
            "Attachments", // 0x06
            "AttName", // 0x07
            "AttSize", // 0x08
            "Att0Id", // 0x09
            "AttMethod", // 0x0A
            "AttRemoved", // 0x0B
            "Body", // 0x0C
            "BodySize", // 0x0D
            "BodyTruncated", // 0x0E
            "DateReceived", // 0x0F
            "DisplayName", // 0x10
            "DisplayTo", // 0x11
            "Importance", // 0x12
            "MessageClass", // 0x13
            "Subject", // 0x14
            "Read", // 0x15
            "To", // 0x16
            "CC", // 0x17
            "From", // 0x18
            "ReplyTo", // 0x19
            "AllDayEvent", // 0x1A
            "Categories", // 0x1B
            "Category", // 0x1C
            "DTStamp", // 0x1D
            "EndTime", // 0x1E
            "InstanceType", // 0x1F
            "IntDBusyStatus", // 0x20
            "Location", // 0x21
            "MeetingRequest", // 0x22
            "Organizer", // 0x23
            "RecurrenceId", // 0x24
            "Reminder", // 0x25
            "ResponseRequested", // 0x26
            "Recurrences", // 0x27
            "Recurrence", // 0x28
            "Recurrence_Type", // 0x29
            "Recurrence_Until", // 0x2A
            "Recurrence_Occurrences", // 0x2B
            "Recurrence_Interval", // 0x2C
            "Recurrence_DayOfWeek", // 0x2D
            "Recurrence_DayOfMonth", // 0x2E
            "Recurrence_WeekOfMonth", // 0x2F
            "Recurrence_MonthOfYear", // 0x30
            "StartTime", // 0x31
            "Sensitivity", // 0x32
            "TimeZone", // 0x33
            "GlobalObjId", // 0x34
            "ThreadTopic", // 0x35
            "MIMEData", // 0x36
            "MIMETruncated", // 0x37
            "MIMESize", // 0x38
            "InternetCPID", // 0x39
            "Flag", // 0x3A
            "FlagStatus", // 0x3B
            "ContentClass", // 0x3C
            "FlagType", // 0x3D
            "CompleteTime", // 0x3E
            "DisallowNewTimeProposal", // 0x3F
        },
        { // AirNotify
            "Notify", // 0x05 <4>
            "Notification", // 0x06
            "Version", // 0x07
            "LifeTime", // 0x08
            "DeviceInfo", // 0x09
            "Enable", // 0x0A
            "Folder", // 0x0B
            "ServerId", // 0x0C
            "DeviceAddress", // 0x0D
            "ValidCarrierProfiles", // 0x0E
            "CarrierProfile", // 0x0F
            "Status", // 0x10
            "Responses", // 0x11
            "Devices", // 0x12
            "Device", // 0x13
            "Id", // 0x14
            "Expiry", // 0x15
            "NotifyGUID", // 0x16
            "DeviceFriendlyName", // 0x17
        },
        { // Calendar
            "Timezone", // 0x05
            "AllDayEvent", // 0x06
            "Attendees", // 0x07
            "Attendee", // 0x08
            "Attendee_Email", // 0x09
            "Attendee_Name", // 0x0A
            "Body", // 0x0B
            "BodyTruncated", // 0x0C
            "BusyStatus", // 0x0D
            "Categories", // 0x0E
            "Category", // 0x0F
            "Compressed_RTF", // 0x10
            "DtStamp", // 0x11
            "EndTime", // 0x12
            "Exception", // 0x13
            "Exceptions", // 0x14
            "Exception_Deleted", // 0x15
            "Exception_StartTime", // 0x16
            "Location", // 0x17
            "MeetingStatus", // 0x18
            "Organizer_Email", // 0x19
            "Organizer_Name", // 0x1A
            "Recurrence", // 0x1B
            "Recurrence_Type", // 0x1C
            "Recurrence_Until", // 0x1D
            "Recurrence_Occurrences", // 0x1E
            "Recurrence_Interval", // 0x1F
            "Recurrence_DayOfWeek", // 0x20
            "Recurrence_DayOfMonth", // 0x21
            "Recurrence_WeekOfMonth", // 0x22
            "Recurrence_MonthOfYear", // 0x23
            "Reminder_MinsBefore", // 0x24
            "Sensitivity", // 0x25
            "Subject", // 0x26
            "StartTime", // 0x27
            "UID", // 0x28
            "Attendee_Status", // 0x29
            "Attendee_Type", // 0x2A
            null, // 0x2B
            null, // 0x2C
            null, // 0x2D
            null, // 0x2E
            null, // 0x2F
            null, // 0x30
            null, // 0x31
            null, // 0x32
            "DisallowNewTimeProposal", // 0x33
            "ResponseRequested", // 0x34
            "AppointmentReplyTime", // 0x35
            "ResponseType", // 0x36
            "CalendarType", // 0x37
            "IsLeapMonth", // 0x38
        },
        { // Move
            "Moves", // 0x05
            "Move", // 0x06
            "SrcMsgId", // 0x07
            "SrcFldId", // 0x08
            "DstFldId", // 0x09
            "Response", // 0x0A
            "Status", // 0x0B
            "DstMsgId", // 0x0C
        },
        { // ItemEstimate
            "GetItemEstimate", // 0x05
            "Version", // 0x06
            "Collections", // 0x07
            "Collection", // 0x08
            "Class", // 0x09
            "CollectionId", // 0x0A
            "DateTime", // 0x0B
            "Estimate", // 0x0C
            "Response", // 0x0D
            "Status", // 0x0E
        },
        { // FolderHierarchy
            "Folders", // 0x05
            "Folder", // 0x06
            "DisplayName", // 0x07
            "ServerId", // 0x08
            "ParentId", // 0x09
            "Type", // 0x0A
            "Response", // 0x0B
            "Status", // 0x0C
            "ContentClass", // 0x0D
            "Changes", // 0x0E
            "Add", // 0x0F
            "Delete", // 0x10
            "Update", // 0x11
            "SyncKey", // 0x12
            "FolderCreate", // 0x13
            "FolderDelete", // 0x14
            "FolderUpdate", // 0x15
            "FolderSync", // 0x16
            "Count", // 0x17
            "Version", // 0x18
        },
        { // MeetingResponse
            "CalId", // 0x05
            "CollectionId", // 0x06
            "MeetingResponse", // 0x07
            "ReqId", // 0x08
            "Request", // 0x09
            "Result", // 0x0A
            "Status", // 0x0B
            "UserResponse", // 0x0C
            "Version", // 0x0D
        },
        { // Tasks
            "Body", // 0x05
            "BodySize", // 0x06
            "BodyTruncated", // 0x07
            "Categories", // 0x08
            "Category", // 0x09
            "Complete", // 0x0A
            "DateCompleted", // 0x0B
            "DueDate", // 0x0C
            "UTCDueDate", // 0x0D
            "Importance", // 0x0E
            "Recurrence", // 0x0F
            "Recurrence_Type", // 0x10
            "Recurrence_Start", // 0x11
            "Recurrence_Until", // 0x12
            "Recurrence_Occurrences", // 0x13
            "Recurrence_Interval", // 0x14
            "Recurrence_DayOfMonth", // 0x15
            "Recurrence_DayOfWeek", // 0x16
            "Recurrence_WeekOfMonth", // 0x17
            "Recurrence_MonthOfYear", // 0x18
            "Recurrence_Regenerate", // 0x19
            "Recurrence_DeadOccur", // 0x1A
            "ReminderSet", // 0x1B
            "ReminderTime", // 0x1C
            "Sensitivity", // 0x1D
            "StartDate", // 0x1E
            "UTCStartDate", // 0x1F
            "Subject", // 0x20_NAMESPACE_URIS
            "CompressedRTF", // 0x21
            "OrdinalDate", // 0x22 <3>
            "SubOrdinalDate", // 0x23 <3>
        },
        { // ResolveRecipients
            "ResolveRecipients", // 0x05
            "Response", // 0x06
            "Status", // 0x07
            "Type", // 0x08
            "Recipient", // 0x09
            "DisplayName", // 0x0A
            "EmailAddress", // 0x0B
            "Certificates", // 0x0C
            "Certificate", // 0x0D
            "MiniCertificate", // 0x0E
            "Options", // 0x0F
            "To", // 0x10
            "CertificateRetrieval", // 0x11
            "RecipientCount", // 0x12
            "MaxCertificates", // 0x13
            "MaxAmbiguousRecipients", // 0x14
            "CertificateCount", // 0x15
            "Availability", // 0x16
            "StartTime", // 0x17
            "EndTime", // 0x18
            "MergedFreeBusy", // 0x19 
        },
        { // ValidateCert
            "ValidateCert", // 0x05
            "Certificates", // 0x06
            "Certificate", // 0x07
            "CertificateChain", // 0x08
            "CheckCRL", // 0x09
            "Status", // 0x0A
        },
        { // Contacts2
            "CustomerId", // 0x05
            "GovernmentId", // 0x06
            "IMAddress", // 0x07
            "IMAddress2", // 0x08
            "IMAddress3", // 0x09
            "ManagerName", // 0x0A
            "CompanyMainPhone", // 0x0B
            "AccountName", // 0x0C
            "NickName", // 0x0D
            "MMS", // 0x0E
        },
        { // Ping
            "Ping", // 0x05
            "AutdState", // 0x06
            "Status", // 0x07
            "HeartbeatInterval", // 0x08
            "Folders", // 0x09
            "Folder", // 0x0A
            "Id", // 0x0B
            "Class", // 0x0C
            "MaxFolders", // 0x0D
        },
        { // Provision
            "Provision", // 0x05
            "Policies", // 0x06
            "Policy", // 0x07
            "PolicyType", // 0x08
            "PolicyKey", // 0x09
            "Data", // 0x0A
            "Status", // 0x0B
            "RemoteWipe", // 0x0C
            "EASProvisionDoc", // 0x0D <3>
            "DevicePasswordEnabled", // 0x0E <3>
            "AlphanumericDevicePasswordRequired", // 0x0F <3>
            "DeviceEncryptionEnabled", // 0x10 <3>
            "PasswordRecoveryEnabled", // 0x11 <3>
            "DocumentBrowseEnabled", // 0x12 <3>
            "AttachmentsEnabled", // 0x13 <3>
            "MinDevicePasswordLength", // 0x14 <3>
            "MaxInactivityTimeDeviceLock", // 0x15 <3>
            "MaxDevicePasswordFailedAttempts", // 0x16 <3>
            "MaxAttachmentSize", // 0x17 <3>
            "AllowSimpleDevicePassword", // 0x18 <3>
            "DevicePasswordExpiration", // 0x19 <3>
            "DevicePasswordHistory", // 0x1A <3>
            "AllowStorageCard", // 0x1B <5>
            "AllowCamera", // 0x1C <4>
            "RequireDeviceEncryption", // 0x1D <4>
            "AllowUnsignedApplications", // 0x1E <4>
            "AllowUnsignedInstallationPackages", // 0x1F <4>
            "MinDevicePasswordComplexCharacters", // 0x20 <4>
            "AllowWiFi", // 0x21 <4>
            "AllowTextMessaging", // 0x22 <4>
            "AllowPOPIMAPEmail", // 0x23 <4>
            "AllowBluetooth", // 0x24 <4>
            "AllowIrDA", // 0x25 <4>
            "RequireManualSyncWhenRoaming", // 0x26 <4>
            "AllowDesktopSync", // 0x27 <4>
            "MaxCalendarAgeFilter", // 0x28 <4>
            "AllowHTMLEmail", // 0x29 <4>
            "MaxEmailAgeFilter", // 0x2A <4>
            "MaxEmailBodyTruncationSize", // 0x2B <4>
            "MaxEmailHTMLBodyTruncationSize", // 0x2C <4>
            "RequireSignedSMIMEMessages", // 0x2D <4>
            "RequireEncryptedSMIMEMessages", // 0x2E <4>
            "RequireSignedSMIMEAlgorithm", // 0x2F <4>
            "RequireEncryptionSMIMEAlgorithm", // 0x30 <4>
            "AllowSMIMEEncryptionAlgorithmNegotiation", // 0x31 <4>
            "AllowSMIMESoftCerts", // 0x32 <4>
            "AllowBrowser", // 0x33 <4>
            "AllowConsumerEmail", // 0x34 <4>
            "AllowRemoteDesktop", // 0x35 <4>
            "AllowInternetSharing", // 0x36 <4>
            "UnapprovedInROMApplicationList", // 0x37 <4>
            "ApplicationName", // 0x38 <4>
            "ApprovedApplicationList", // 0x39 <4>
            "Hash", // 0x3A <4>
        },
        { // Search
            "Search", // 0x05
            "UNUSED", // 0x06
            "Store", // 0x07
            "Name", // 0x08
            "Query", // 0x09
            "Options", // 0x0A
            "Range", // 0x0B
            "Status", // 0x0C
            "Response", // 0x0D
            "Result", // 0x0E
            "Properties", // 0x0F
            "Total", // 0x10
            "EqualTo", // 0x11
            "Value", // 0x12
            "And", // 0x13
            "Or", // 0x14
            "FreeText", // 0x15
            "UNUSED", // 0x16
            "DeepTraversal", // 0x17
            "LongId", // 0x18
            "RebuildResults", // 0x19
            "LessThan", // 0x1A
            "GreaterThan", // 0x1B
            "Schema", // 0x1C
            "Supported", // 0x1D
        },
        { // GAL
            "DisplayName", // 0x05
            "Phone", // 0x06
            "Office", // 0x07
            "Title", // 0x08
            "Company", // 0x09
            "Alias", // 0x0A
            "FirstName", // 0x0B
            "LastName", // 0x0C
            "HomePhone", // 0x0D
            "MobilePhone", // 0x0E
            "EmailAddress", // 0x0F
        },
        { // AirSyncBase
            "BodyPreference", // 0x05
            "Type", // 0x06
            "TruncationSize", // 0x07
            "AllOrNone", // 0x08
            "UNDEFINED_IN_MICROSOFT_SPEC", // 0x09
            "Body", // 0x0A
            "Data", // 0x0B
            "EstimatedDataSize", // 0x0C
            "Truncated", // 0x0D
            "Attachments", // 0x0E
            "Attachment", // 0x0F
            "DisplayName", // 0x10
            "FileReference", // 0x11
            "Method", // 0x12
            "ContentId", // 0x13
            "ContentLocation", // 0x14
            "IsInline", // 0x15
            "NativeBodyType", // 0x16
            "ContentType", // 0x17
        },
        { // Settings
            "Settings", // 0x05
            "Status", // 0x06
            "Get", // 0x07
            "Set", // 0x08
            "Oof", // 0x09
            "OofState", // 0x0A
            "StartTime", // 0x0B
            "EndTime", // 0x0C
            "OofMessage", // 0x0D
            "AppliesToInternal", // 0x0E
            "AppliesToExternalKnown", // 0x0F
            "AppliesToExternalUnknown", // 0x10
            "Enabled", // 0x11
            "ReplyMessage", // 0x12
            "BodyType", // 0x13
            "DevicePassword", // 0x14
            "Password", // 0x15
            "DeviceInformaton", // 0x16
            "Model", // 0x17
            "IMEI", // 0x18
            "FriendlyName", // 0x19
            "OS", // 0x1A
            "OSLanguage", // 0x1B
            "PhoneNumber", // 0x1C
            "UserInformation", // 0x1D
            "EmailAddresses", // 0x1E
            "SmtpAddress", // 0x1F
        },
        { // DocumentLibrary
            "LinkId", // 0x05
            "DisplayName", // 0x06
            "IsFolder", // 0x07
            "CreationDate", // 0x08
            "LastModifiedDate", // 0x09
            "IsHidden", // 0x0A
            "ContentLength", // 0x0B
            "ContentType", // 0x0C
        },
        { // ItemOperations
            "ItemOperations", // 0x05
            "Fetch", // 0x06
            "Store", // 0x07
            "Options", // 0x08
            "Range", // 0x09
            "Total", // 0x0A
            "Properties", // 0x0B
            "Data", // 0x0C
            "Status", // 0x0D
            "Response", // 0x0E
            "Version", // 0x0F
            "Schema", // 0x10
            "Part", // 0x11
            "EmptyFolderContents", // 0x12
            "DeleteSubFolders", // 0x13
        },
        { // ComposeMail
            "SendMail", // 0x05
            "SmartForward", // 0x06
            "SmartReply", // 0x07
            "SaveInSentItems", // 0x08
            "ReplaceMime", // 0x09
            "Type", // 0x0A
            "Source", // 0x0B
            "FolderId", // 0x0C
            "ItemId", // 0x0D
            "LongId", // 0x0E
            "InstanceId", // 0x0F
            "MIME", // 0x10
            "ClientId", // 0x11
            "Status" // 0x12
        }, 
        { // Mail2
            "UmCallerID", // 0x05
            "UmUserNotes", // 0x06
            "UmAttDuration", // 0x07
            "UmAttOrder", // 0x08            
            "ConversationId", // 0x09
            "ConversationIndex", // 0x0A
            "LastVerbExecuted", // 0x0B
            "LastVerbExecutionTime", // 0x0C
            "ReceivedAsBcc", // 0x0D
            "Sender", // 0x0E
            "CalendarType", // 0x0F
            "IsLeapMonth", // 0x10
        },
        { // Notes
            "Subject", // 0x05
            "MessageClass", // 0x06
            "LastModifiedDate", // 0x07
            "Categories", // 0x08
            "Category", // 0x09
        }
    };
    
    static final String[] NAMESPACE_URIS = {
        "AirSync:", // 0
        "POOMCONTACTS:", // 1
        "POOMMAIL:", // 2
        "AirNotify:", // 3
        "POOMCAL:", // 4
        "Move:", // 5
        "ItemEstimate:", // 6
        "FolderHierarchy:", // 7
        "MeetingResponse:", // 8
        "POOMTASKS:", // 9
        "ResolveRecipients", // 10
        "ValidateCert:", // 11
        "POOMCONTACTS2:", // 12
        "Ping:", // 13
        "Provision:", // 14
        "Search:", // 15
        "Gal:", // 16
        "AirSyncBase:", // 17
        "Settings:", // 18
        "DocumentLibrary:", // 19
        "ItemOperations:", // 20
        "ComposeMail:", // 21
        "POOMMAIL2:", // 22
        "Notes:" //23
    };
       
    static final String[] NAMESPACE_PREFIXES = {
        "AirSync", // 0
        "Contacts", // 1
        "Email", // 2
        "AirNotify", // 3
        "Calendar", // 4
        "Move", // 5
        "ItemEstimate", // 6
        "FolderHierarchy", // 7
        "MeetingResponse", // 8
        "Tasks", // 9
        "ResolveRecipients", // 10
        "ValidateCert", // 11
        "Contacts2", // 12
        "Ping", // 13
        "Provision", // 14
        "Search", // 15
        "GAL", // 16
        "AirSyncBase", // 17
        "Settings", // 18
        "DocumentLibrary", // 19
        "ItemOperations", // 20
        "ComposeMail", // 21
        "Mail2", // 22
        "Notes" //23
    };

    /**
     * Maps local names to codes
     */
    @SuppressWarnings("unchecked")
    private static Map<String,Integer>[] _TAGS = new Map[TAGS.length];

    /**
     * Maps namespace URIs to code page numbers
     */
    static Map<String,Integer> _NAMESPACE_URIS = new HashMap<String,Integer>();

    /**
     * Maps namespace prefixes to code page numbers
     */
    static Map<String,Integer> _NAMESPACE_PREFIXES = new HashMap<String,Integer>();
    
    /**
     * 
     */
    private static final NamespaceContext namespaceContext = new NamespaceContext(){

        @Override
        public String getNamespaceURI(String prefix) {
            if(XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
                return NAMESPACE_URIS[0];
            } else {
                Integer page = _NAMESPACE_PREFIXES.get(prefix);
                return page == null ? XMLConstants.NULL_NS_URI : NAMESPACE_URIS[page.intValue()];
            }
        }

        @Override
        public String getPrefix(String namespaceURI) {
            Integer page = _NAMESPACE_URIS.get(namespaceURI);
            return page == null ? null : NAMESPACE_PREFIXES[page.intValue()];
        }

        @Override
        public Iterator<?> getPrefixes(String namespaceURI) {
            String prefix = getPrefix(namespaceURI);
            return (prefix == null ? Collections.emptySet() : Collections.singleton(prefix)).iterator();
        }
        
    };

    static {
        for(
            int n = 0; 
            n < NAMESPACE_URIS.length; 
            n ++
        ){
            Integer i = Integer.valueOf(n); 
            _NAMESPACE_URIS.put(NAMESPACE_URIS[n], i);
            _NAMESPACE_PREFIXES.put(NAMESPACE_PREFIXES[n], i);
            String[] tags = TAGS[n];
            Map<String,Integer> _tags = _TAGS[n] = new HashMap<String,Integer>();
            for(
                int t = 0; 
                t < tags.length; 
                t++
            ){
                _tags.put(tags[t], Integer.valueOf(t + 5));
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.wbxml.AbstractPlugIn#getTagToken(java.lang.String)
     */
    @Override
    public CodeToken getTagToken(
        String namespaceURI, 
        String value
    ) {
        Integer page = _NAMESPACE_URIS.get(namespaceURI);
        if (page == null) return null;
        Integer code = _TAGS[page.intValue()].get(value);
        if(code == null) return null;    
        return new CodeToken(
            page << 8 | code.intValue(),
            value.length(),
            false
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.TokenHandler#resolveTag(short, short)
     */
    @Override
    public CodeResolution resolveTag(
        int page, 
        int id
    ) throws ServiceException {
        try {
            String localName = TAGS[page][id - 5];
            return new CodeResolution (
                NAMESPACE_URIS[page],
                localName,
                NAMESPACE_PREFIXES[page] + ':' + localName
             );
        } catch (IndexOutOfBoundsException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "No such tag entry",
                new BasicException.Parameter("page", page),
                new BasicException.Parameter("id", id)
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.wbxml.AbstractPlugIn#getNamespaceContext()
     */
    @Override
    public NamespaceContext getNamespaceContext() {
        return namespaceContext;
    }

}
