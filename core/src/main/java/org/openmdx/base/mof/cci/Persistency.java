/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Persistency 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
     * The Meta-Inf located configuration
     */
    private static final String CONFIGURATION_NAME = "openmdxorm.properties";
    
    /**
     * Sanity check input
     */
    private static final List<String> PERSISTENCY_REQUIRED = Arrays.asList(
        "org:openmdx:base:Creatable:createdAt",
        "org:openmdx:base:Creatable:createdBy",
        "org:openmdx:base:Modifiable:modifiedAt",
        "org:openmdx:base:Modifiable:modifiedBy",
        "org:openmdx:base:Removable:removedBy",
        "org:openmdx:base:Removable:removedAt",
        "org:openmdx:state2:StateCapable:stateVersion"
    );

    /**
     * Sanity check input
     */
    private static final List<String> TRANSIENCY_REQUIRED = Arrays.asList(
        "org:openmdx:state2:Legacy:validTimeUnique",
        "org:openmdx:state2:StateCapable:transactionTimeUnique"
    );
    
    /**
     * Constructor 
     * 
     * @throws ServiceException 
     */
    private Persistency() throws ServiceException {
        final String openmdxormProperties = locateConfiguration();
        SysLog.info("ORM mapping: Scanning for Persistence Modifiers", openmdxormProperties); 
        try {
            final Properties persistenceModifiers = new Properties();
            for(URL url : Resources.getMetaInfResources(CONFIGURATION_NAME)) {
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
                            Level.SEVERE, 
                            "Sys|ORM mapping: Persistence Modifiers|Modifier {1} for feature {0} is not supported", 
                            attribute, modifier
                        );
                    }
                }
            }
        } catch (Exception cause) {
            throw logAsSevere(
                new ServiceException(
                    cause,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INVALID_CONFIGURATION, 
                    "Unable to load the openMDX specific ORM configuration",
                    new BasicException.Parameter("Persistence modifiers", openmdxormProperties)
                )
            );
        }
        sanityCheck();
    }

    /**
     * Throw eagerly an exception to avoid erroneous behaviour due to configuration load problems
     * 
     * @throws ServiceException in case of an unexpected configuration
     */
    private void sanityCheck() throws ServiceException {
        for(String feature : PERSISTENCY_REQUIRED) {
            assertPersistency(feature, Boolean.TRUE);
        }
        for(String feature : TRANSIENCY_REQUIRED) {
            assertPersistency(feature, Boolean.FALSE);
        }
    }
    
    /**
     * Validate a given feature's persistency configuration
     * 
     * @throws ServiceException in case of an unexpected configuration
     */
    private void assertPersistency(
        String feature, 
        Boolean expected
    ) throws ServiceException {
        final Boolean actual = this.persistentFeatures.get(feature);
        if(!expected.equals(actual)) {
            throw logAsSevere(
                new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "Sanity check: Unexpected openMDX ORM persistency configuration",
                    new BasicException.Parameter("feature", feature),
                    new BasicException.Parameter("expected", expected),
                    new BasicException.Parameter("actual", actual)
                )
            );
        }
    }
    
    /**
     * Locate the openMDX ORM configuration for logging and exception amendment.
     * 
     * @return the openMDX ORM configuration 
     * 
     * @throws ServiceException 
     */
    private String locateConfiguration() throws ServiceException {
        try {
            return Resources.toMetaInfPath(CONFIGURATION_NAME);
        } catch (Exception cause) {
            throw logAsSevere(
                new ServiceException(
                    cause,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INVALID_CONFIGURATION, 
                    "Unable to locate the openMDX specific ORM configuration",
                    new BasicException.Parameter("configuration-name", CONFIGURATION_NAME)
                )
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
     * @throws ServiceException in case of a configuration or assertion failure 
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
        final Boolean persistent = this.persistentFeatures.get(featureDef.getQualifiedName());
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
        final Model_1_0 model = featureDef.getModel();
        return (
            model.isAttributeType(featureDef) ||
            (model.isReferenceType(featureDef) && model.referenceIsStoredAsAttribute(featureDef)) 
        ) && (
            !ModelHelper.isDerived(featureDef)
        );
    }
    
    /**
     * Log the exception stack at level severe
     * 
     * @param exception the exception to be logged 
     * 
     * @return the {code exception}
     */
    private static ServiceException logAsSevere(ServiceException exception) {
        final BasicException cause = exception.getCause();
        SysLog.log(
            Level.SEVERE,
            cause == null ? exception.getMessage() : cause.getDescription(),
            exception
        );
        return exception;
    }
    
}
