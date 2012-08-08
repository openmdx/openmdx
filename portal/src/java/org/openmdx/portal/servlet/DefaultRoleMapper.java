/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: DefaultRoleMapper.java,v 1.21 2009/06/16 17:08:26 wfro Exp $
 * Description: DefaultRoleMapper 
 * Revision:    $Revision: 1.21 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/16 17:08:26 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 */
package org.openmdx.portal.servlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;

import org.oasisopen.cci2.QualifierType;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.log.SysLog;

public class DefaultRoleMapper
    implements Serializable, RoleMapper_1_0 {
    
    //-------------------------------------------------------------------------
    protected List<org.openmdx.security.realm1.jmi1.Group> getGroupMembership(
        org.openmdx.security.realm1.jmi1.Principal loginPrincipal,
        String segmentName,
        PersistenceManager pm
    ) {
        try {
            String loginPrincipalName = new Path(loginPrincipal.refMofId()).getBase();            
            Path loginPrincipalIdentity = loginPrincipal.refGetPath();
            SysLog.detail("Group membership for segment", segmentName);
            SysLog.detail("Group membership for principal", loginPrincipalIdentity);
            org.openmdx.security.realm1.jmi1.Principal principal = 
                (org.openmdx.security.realm1.jmi1.Principal)pm.getObjectById(
                    loginPrincipalIdentity.getPrefix(loginPrincipalIdentity.size()-3).getDescendant(
                        new String[]{segmentName, "principal", loginPrincipalName}
                    )
                );
            return principal.getIsMemberOf();
        }
        catch(Exception e) {
        	SysLog.detail("Can not retrieve group membership", e);
            new ServiceException(e).log();
            return null;
        }
    }
    
    //-------------------------------------------------------------------------
    public org.openmdx.security.realm1.cci2.Principal checkPrincipal(
        org.openmdx.security.realm1.cci2.Realm realm,
        String principalName,
        PersistenceManager pm
    ) {
        try {
            org.openmdx.security.realm1.cci2.Principal loginPrincipal = realm.getPrincipal().get(QualifierType.REASSIGNABLE, principalName);
            if(loginPrincipal == null) {
            	SysLog.info("principal not found in realm", "realm=" + realm + ", principal=" + principalName);
                return null;
            }
            boolean disabled = false;
            try {
                disabled = loginPrincipal.isDisabled();
            } 
            catch(NullPointerException e) {}
            return !disabled
                ? loginPrincipal
                : null;
        }
        catch(Exception e) {
            new ServiceException(e).log();
            return null;
        }
    }
    
    //-------------------------------------------------------------------------
    /**
     * Return set of roles for specified principal in given realm.
     * This role mapper is based on the openMDX/Security model. 
     */
    public List<String> getUserInRoles(
        org.openmdx.security.realm1.cci2.Realm loginRealm_,
        String principalName,
        PersistenceManager pm
    ) {
        List<String> userRoles = new ArrayList<String>();
        if(loginRealm_ instanceof org.openmdx.security.realm1.jmi1.Realm) {
            org.openmdx.security.realm1.jmi1.Realm loginRealm = (org.openmdx.security.realm1.jmi1.Realm)loginRealm_;
            org.openmdx.security.realm1.jmi1.Realm1Package realmPkg = (org.openmdx.security.realm1.jmi1.Realm1Package)loginRealm.refImmediatePackage();
            // Get principals owned by subject
            org.openmdx.security.realm1.jmi1.Principal primaryLoginPrincipal = loginRealm.getPrincipal(principalName);
            org.openmdx.security.realm1.jmi1.Subject subject = primaryLoginPrincipal.getSubject();
            org.openmdx.security.realm1.cci2.PrincipalQuery principalQuery = realmPkg.createPrincipalQuery();
            principalQuery.thereExistsSubject().equalTo(subject);
            List<org.openmdx.security.realm1.jmi1.Principal> allLoginPrincipals = loginRealm.getPrincipal(principalQuery);        
            // Reverse sort user roles by their last login date
            org.openmdx.security.realm1.jmi1.Segment realmSegment = 
                (org.openmdx.security.realm1.jmi1.Segment)pm.getObjectById(loginRealm.refGetPath().getParent().getParent());
            // Iterate all realms
            long leastRecentLoginAt = 0L;
            Collection<org.openmdx.security.realm1.jmi1.Realm> realms = realmSegment.getRealm();
            for(org.openmdx.security.realm1.jmi1.Realm realm: realms) {
            	SysLog.detail("Checking realm", realm);
                // Skip login realm
                if(!realm.refGetPath().equals(loginRealm.refGetPath())) {
                    for(org.openmdx.security.realm1.jmi1.Principal loginPrincipal: allLoginPrincipals) {
                        String principalId = loginPrincipal.refGetPath().getBase();
                        SysLog.detail("Checking principal", principalId);
                        org.openmdx.security.realm1.jmi1.Principal principal = 
                            (org.openmdx.security.realm1.jmi1.Principal)this.checkPrincipal(realm, principalId, pm);
                        String realmName = realm.getName();
                        // Do not include root realm in roles except if principal is root
                        if(principal != null) {
                            try {
                                List<org.openmdx.security.realm1.jmi1.Group> groups = this.getGroupMembership(
                                    principal,
                                    realmName,
                                    pm
                                );
                                SysLog.detail("Principal groups", groups);
                                if(groups != null) {
                                    long lastLoginAt = 0L;
                                    try {
                                        Date at = (Date)principal.refGetValue("lastLoginAt");
                                        if(at != null) {
                                            lastLoginAt = at.getTime();
                                        }
                                    } 
                                    catch(Exception e) {}
                                    String roleId = principalId + "@" + realmName;
                                    SysLog.detail("Checking role", roleId);
                                    if(
                                        !userRoles.contains(roleId) &&
                                        (!ROOT_REALM_NAME.equals(realmName) || ROOT_PRINCIPAL_NAME.equals(principalId))                                    
                                    ) {
                                    	SysLog.detail("Adding role", roleId);
                                        userRoles.add(
                                            lastLoginAt > leastRecentLoginAt ? 0 : userRoles.size(),
                                            roleId
                                        );
                                    }
                                    try {
                                        for(org.openmdx.security.realm1.jmi1.Group userGroup: groups) {
                                        	SysLog.detail("Checking group", userGroup);
                                            String userGroupIdentity = userGroup.refGetPath().getBase();
                                            if(USER_GROUP_ADMINISTRATORS.equals(userGroupIdentity)) {
                                                roleId = ADMIN_PRINCIPAL_PREFIX + realmName + "@" + realmName;
                                                if(!userRoles.contains(roleId)) {
                                                	SysLog.detail("Adding role", roleId);
                                                    userRoles.add(
                                                        lastLoginAt > leastRecentLoginAt ? 1 : userRoles.size(),
                                                        roleId
                                                    );
                                                }
                                            }
                                        }
                                    }
                                    // Ignore errors while inspecting groups
                                    catch(Exception e) {
//                                      boolean error = true;
                                    }
                                    leastRecentLoginAt = Math.max(lastLoginAt, leastRecentLoginAt);
                                }
                            }
                            // Ignore errors while inspecting user roles (e.g. subject can not be found)
                            catch(Exception e) {
//                              boolean error = true;
                            }
                        }
                    }
                }
            }
        }
        return userRoles;
    }

    //-----------------------------------------------------------------------
    public String getAdminPrincipal(
        String realmName
    ) {
        return ADMIN_PRINCIPAL_PREFIX + realmName;
    }
  
    //-----------------------------------------------------------------------
    public boolean isRootPrincipal(
        String principalName
    ) {
        return principalName.startsWith(ROOT_PRINCIPAL_NAME);
    }
    
    //-----------------------------------------------------------------------
    
    private static final String ADMIN_PRINCIPAL_PREFIX = "admin-";
    private static final String ROOT_REALM_NAME = "Root";
    private static final String ROOT_PRINCIPAL_NAME = DefaultRoleMapper.ADMIN_PRINCIPAL_PREFIX + DefaultRoleMapper.ROOT_REALM_NAME;
    private static final String USER_GROUP_ADMINISTRATORS = "Administrators";

}

//--- End of File -----------------------------------------------------------
