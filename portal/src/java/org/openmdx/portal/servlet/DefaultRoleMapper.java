/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: DefaultRoleMapper.java,v 1.10 2007/07/04 09:37:02 wfro Exp $
 * Description: DefaultRoleMapper 
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/07/04 09:37:02 $
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
 * This product includes yui-ext, the yui extension
 * developed by Jack Slocum (License - based on BSD).
 * 
 */
package org.openmdx.portal.servlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.jmi.reflect.RefObject;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.accessor.jmi.cci.RefContainer_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;

public class DefaultRoleMapper
    implements Serializable, RoleMapper_1_0 {
    
    //-------------------------------------------------------------------------
    protected List getIsMemberOfGroup(
        RefObject loginPrincipal,
        String segmentName
    ) {
        try {
            String loginPrincipalName = new Path(loginPrincipal.refMofId()).getBase();            
            RefPackage_1_0 rootPkg = (RefPackage_1_0)loginPrincipal.refOutermostPackage();
            Path loginPrincipalIdentity = new Path(loginPrincipal.refMofId());
            AppLog.detail("Group membership for segment", segmentName);
            AppLog.detail("Group membership for principal", loginPrincipalIdentity);
            RefObject principal = rootPkg.refObject(
                loginPrincipalIdentity.getPrefix(loginPrincipalIdentity.size()-3).getDescendant(
                    new String[]{segmentName, "principal", loginPrincipalName}
                ).toXri()
            );
            return (List)principal.refGetValue("isMemberOf");
        }
        catch(Exception e) {
            AppLog.detail("Can not retrieve group membership", e);
            new ServiceException(e).log();
            return null;
        }
    }
    
    //-------------------------------------------------------------------------
    public RefObject checkPrincipal(
        RefObject realm,
        String principalName
    ) {
        try {
            RefContainer_1_0 allPrincipals = (RefContainer_1_0)realm.refGetValue("principal");
            RefObject loginPrincipal = (RefObject)allPrincipals.get(principalName);
            if(loginPrincipal == null) {
                AppLog.info("principal not found in realm", "realm=" + realm + ", principal=" + principalName);
                return null;
            }
            Boolean disabled = (Boolean)loginPrincipal.refGetValue("disabled");
            return (disabled == null) || !disabled.booleanValue()
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
     * This role mapper is based on the openMDX/security model. 
     */
    public List getUserInRoles(
        RefObject loginRealm,
        String principalName
    ) {
        // Get principals owned by subject
        RefContainer_1_0 allPrincipals = (RefContainer_1_0)loginRealm.refGetValue("principal");
        RefObject primaryLoginPrincipal = (RefObject)allPrincipals.get(principalName);
        RefObject subject = (RefObject)primaryLoginPrincipal.refGetValue("subject");
        List allLoginPrincipals = allPrincipals.subSet(
            new FilterProperty[]{
                new FilterProperty(
                    Quantors.THERE_EXISTS,
                    "subject",
                    FilterOperators.IS_IN,
                    new Path[]{
                        new Path(subject.refMofId())
                    }
                )
            }
        ).toList(null);
        
        // Reverse sort user roles by their last login date
        List userRoles = new ArrayList();
        RefObject realmSegment = 
            ((RefPackage_1_0)loginRealm.refOutermostPackage()).refObject(
                new Path(loginRealm.refMofId()).getParent().getParent().toXri()
            );
        // Iterate all realms
        long leastRecentLoginAt = 0L;
        for(
            Iterator i = ((Collection)realmSegment.refGetValue("realm")).iterator();
            i.hasNext();
        ) {
            RefObject realm = (RefObject)i.next();
            AppLog.detail("Checking realm", realm);
            // Skip login realm
            if(!realm.refMofId().equals(loginRealm.refMofId())) {
                for(
                    Iterator j = allLoginPrincipals.iterator();
                    j.hasNext();
                ) {
                    RefObject loginPrincipal = (RefObject)j.next();
                    String principalId = new Path(loginPrincipal.refMofId()).getBase();
                    AppLog.detail("Checking principal", principalId);
                    RefObject principal = null;
                    String realmName = (String)realm.refGetValue("name");
                    // Do not include root realm in roles except if principal is root
                    if((principal = this.checkPrincipal(realm, principalId)) != null) {
                        try {
                            List groups = this.getIsMemberOfGroup(
                                loginPrincipal,
                                realmName
                            );
                            AppLog.detail("Principal groups", groups);
                            if(groups != null) {
                                long lastLoginAt = 0L;
                                try {
                                    lastLoginAt = ((Date)principal.refGetValue("lastLoginAt")).getTime();
                                } catch(Exception e) {}
                                String roleId = principalId + "@" + realmName;
                                AppLog.detail("Checking role", roleId);
                                if(
                                    !userRoles.contains(roleId) &&
                                    (!ROOT_REALM_NAME.equals(realmName) || ROOT_PRINCIPAL_NAME.equals(principalId))                                    
                                ) {
                                    AppLog.detail("Adding role", roleId);
                                    userRoles.add(
                                        lastLoginAt > leastRecentLoginAt ? 0 : userRoles.size(),
                                        roleId
                                    );
                                }
                                for(
                                    Iterator k = groups.iterator();
                                    k.hasNext();
                                ) {
                                    RefObject userGroup = (RefObject)k.next();
                                    AppLog.detail("Checking group", userGroup);
                                    String userGroupIdentity = new Path(userGroup.refMofId()).getBase();
                                    if(USER_GROUP_ADMINISTRATORS.equals(userGroupIdentity)) {
                                        roleId = ADMIN_PRINCIPAL_PREFIX + realmName + "@" + realmName;
                                        if(!userRoles.contains(roleId)) {
                                            AppLog.detail("Adding role", roleId);
                                            userRoles.add(
                                                lastLoginAt > leastRecentLoginAt ? 1 : userRoles.size(),
                                                roleId
                                            );
                                        }
                                    }
                                }
                                leastRecentLoginAt = Math.max(lastLoginAt, leastRecentLoginAt);
                            }
                        }
                        // Don't care if subject can not be found. Do not add it to userRoles
                        catch(Exception e) {}
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
    private static final String ROOT_PRINCIPAL_NAME = ADMIN_PRINCIPAL_PREFIX + ROOT_REALM_NAME;
    private static final String USER_GROUP_ADMINISTRATORS = "Administrators";

}

//--- End of File -----------------------------------------------------------
