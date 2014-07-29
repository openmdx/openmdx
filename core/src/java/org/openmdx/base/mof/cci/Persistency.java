/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Persistency 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2011, OMEX AG, Switzerland
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
package org.openmdx.base.mof.cci;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Resources;
import org.openmdx.kernel.log.SysLog;


/**
 * Persistency
 */
public class Persistency {

    /**
     * Constructor 
     * 
     * @throws ServiceException 
     */
    private Persistency() throws ServiceException {
        try {
            Properties persistenceModifiers = new Properties();
            for(URL url : Resources.getMetaInfResources("openmdxorm.properties")) {
                SysLog.log(
                    Level.INFO,
                    "Sys|ORM mapping: Persistence Modifiers|Apply configuration {0}", 
                    url.toExternalForm()
                );
                persistenceModifiers.clear();
				persistenceModifiers.load(url.openStream());
                for(Map.Entry<?, ?> e : persistenceModifiers.entrySet()){
                    String attribute = (String) e.getKey();
                    String modifier = (String) e.getValue();
                    SysLog.log(
                        Level.FINE, 
                        "Sys|ORM mapping: Persistence Modifiers|Feature {0} has modifier {1}", 
                        attribute, modifier
                    );
                    if("TRANSIENT".equalsIgnoreCase(modifier)){
                        this.persistentFeatures.put(attribute, Boolean.FALSE);
                    } else if ("PERSISTENT".equalsIgnoreCase(modifier)) {
                        this.persistentFeatures.put(attribute, Boolean.TRUE);
                    } else {
                        SysLog.log(
                            Level.WARNING, 
                            "Sys|ORM mapping: Persistence Modifiers|Modifier {1} for feature {0} is not supported", 
                            attribute, modifier
                        );
                    }
                }
            }
        } catch (IOException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION, 
                "Unable to load the openMDX specific ORM configuration"
            );
        } catch (ClassCastException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION, 
                "Unable to load the openMDX specific ORM configuration"
            );
        }
    }

    /**
     * Persistency registry
     */
    private final Map<String,Boolean> persistentFeatures = new HashMap<String,Boolean>();

    /**
     * A singleton is sufficient
     */
    private static Persistency instance;
    
    /**
     * Retrieve the instance
     * 
     * @return the instance
     * 
     * @throws ServiceException 
     */
    public static Persistency getInstance() throws ServiceException{
        if(instance == null) {
            instance = new Persistency();
        }
        return instance;
    }

    /**
     * Tells whether the given feature is persistent
     * 
     * @param featureDef the feature's meta-data
     * 
     * @return <code>true</code> if the given feature is persistent
     */
    public boolean isPersistentAttribute(
        ModelElement_1_0 featureDef
    ) throws ServiceException {
        Boolean persistent = this.persistentFeatures.get(featureDef.getQualifiedName());
        return persistent == null ? isNonDerivedAttribute(featureDef) : persistent.booleanValue();
    }
    
    /**
     * Tells whether the given feature is a non-derived attribute
     * 
     * @param featureDef the feature's meta-data
     * 
     * @return <code>true</code> if the given feature is a non-derived attribute
     */
    private boolean isNonDerivedAttribute(
        ModelElement_1_0 featureDef
    ) throws ServiceException {
        Model_1_0 model = featureDef.getModel();
        return (
            model.isAttributeType(featureDef) ||
            (model.isReferenceType(featureDef) && model.referenceIsStoredAsAttribute(featureDef)) 
        ) && (
            !ModelHelper.isDerived(featureDef)
        );
    }
    
}
