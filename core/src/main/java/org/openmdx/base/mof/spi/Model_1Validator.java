/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Model Validator
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.mof.spi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.collection.Sets;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelBuilder_1_0;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.loading.Resources;
import org.openmdx.kernel.log.SysLog;

/**
 * Model Validator
 */
public class Model_1Validator {

    /**
     * The qualified names of the meta data model packages 
     */
    private static final Set<String> META_MODEL_PACKAGES = Collections.unmodifiableSet(
        Sets.asSet(
    		"org:oasis-open",
            "org:omg:model1",
            "org:openmdx:base",
            "org:w3c"
        )
    );

    /**
     * The cached meta model
     */
    private static Model_1_0 metaModel;
    
    /**
     * Tells whether the model is valid
     * 
     * @param model the model to be validated
     * @param retry, i.e. 0 for the initial attempt, 1 for the first retry and so on
     * 
     * @return {@code true} if the model is valid, {@code false} if the model should be reloaded
     * 
     * @throws RuntimeServiceException if no retry attempt should be made
     */
    static boolean isValid(
        Properties validationProperties, 
        Model_1_0 model,
        int retryCount
    ){
        boolean validateClassifiers = isEnabled(validationProperties, "validate-classifiers");
        boolean validateChecksum = isEnabled(validationProperties, "validate-checksum");
        boolean validateElements = isEnabled(validationProperties, "validate-elements");
        try {
            if(validateElements) {
                validateElements(model);
            }
            if(validateClassifiers) {
                validateClassifiers(model);
            }
            if(validateChecksum) {
                validateChecksum(validationProperties, model);
            }
            return true;
        } catch (Exception validationException) {
            SysLog.warning("Model Validation Failure", validationException);
            if(isEnabled(validationProperties, "dump-on-failure")) {
                dumpModel(model);
            }
            int retryLimit = getRetryLimit(validationProperties);
            if(retryCount >= retryLimit) {
                throw new RuntimeServiceException(
                    validationException,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ACTIVATION_FAILURE,
                    "Unable to load model, retry limit reached",
                    new BasicException.Parameter("retryLimit", retryLimit)
                );
            }
            return false;
        }
    }

    private static void validateChecksum(
        Properties validationProperties,
        Model_1_0 model
    ) throws ServiceException, NoSuchAlgorithmException,
        UnsupportedEncodingException {
        String expectedChecksum = validationProperties.getProperty("checksum", "");
        String actualChecksum = calculateChecksum(model);
        if(!actualChecksum.equalsIgnoreCase(expectedChecksum)) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "Unexpected model checksum",
                new BasicException.Parameter("expected", expectedChecksum),
                new BasicException.Parameter("actual", actualChecksum)
            );
        }
    }

    private static Model_1_0 getMetaModel(
    ) throws ServiceException{
    	if(metaModel == null) try {
            ModelBuilder_1_0 metaModelBuilder = Classes.newPlatformInstance(
               	"org.openmdx.application.mof.repository.accessor.ModelBuilder_1",	
                ModelBuilder_1_0.class, 
                Boolean.FALSE, // validation
                META_MODEL_PACKAGES,
                Boolean.TRUE // meta-model		
            );
            metaModel = metaModelBuilder.build();
        } catch (Exception exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "Meta-data acquisition failure"
            );
        }
        return metaModel;
    }
    
    private static void validateElements(
        Model_1_0 model
    ) throws ServiceException {
        Model_1_0 metaModel = getMetaModel();
        for(ModelElement_1_0 element : model.getContent()){
            ModelElement_1_0 metaData = metaModel.getElement(element.objGetClass());
            for(Map.Entry<String, ModelElement_1_0> e : metaModel.getAttributeDefs(metaData, false, false).entrySet()){
                ModelElement_1_0 attribute = e.getValue();
                Multiplicity multiplicity = ModelHelper.getMultiplicity(attribute); 
                switch(multiplicity) {
                    case LIST:
                        for(Object value : element.objGetList(e.getKey())) {
                            validateValue(attribute, value);
                        }
                        break;
                    case SET:
                        for(Object value : element.objGetSet(e.getKey())) {
                            validateValue(attribute, value);
                        }
                        break;
                    case OPTIONAL: {
                        Object value = element.objGetValue(e.getKey());
                        if(value != null) {
                            validateValue(attribute, value);                        
                        }
                        break;
                    }
                    case SINGLE_VALUE: { 
                        Object value = element.objGetValue(e.getKey());
                        validateValue(attribute, value);
                        break;
                    }
                    case SPARSEARRAY:
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.VALIDATION_FAILURE,
                            "Multiplicity not supported by model repository",
                            new BasicException.Parameter("key", e.getKey()),
                            new BasicException.Parameter("multiplicity", "SPARSEARRAY")
                        );
                    default:
                        // No specific check
                }
            }
        }
    }

    private static void validateValue(
        ModelElement_1_0 attribute,
        Object value
    ) throws ServiceException{
        if(value == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.VALIDATION_FAILURE,
                "Unexpected null value",
                new BasicException.Parameter("elementType", attribute.getType()),
                new BasicException.Parameter("elementName", attribute.getQualifiedName())
            );
        } else {
            String type = attribute.getType().getLastSegment().toClassicRepresentation();
            if(PrimitiveTypes.DATE.equals(type)) {
                if(!(value instanceof XMLGregorianCalendar)) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.VALIDATION_FAILURE,
                        "Invalid value class",
                        new BasicException.Parameter("type", type),
                        new BasicException.Parameter("expected", XMLGregorianCalendar.class.getName()),
                        new BasicException.Parameter("actual", value.getClass().getName())
                    );
                }
            } else if (PrimitiveTypes.BOOLEAN.equals(type)) {
                if(!(value instanceof Boolean)) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.VALIDATION_FAILURE,
                        "Invalid value class",
                        new BasicException.Parameter("type", type),
                        new BasicException.Parameter("expected", Boolean.class.getName()),
                        new BasicException.Parameter("actual", value.getClass().getName())
                    );
                }
            } else if (PrimitiveTypes.DATETIME.equals(type)) {
                if(!(value instanceof Date)) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.VALIDATION_FAILURE,
                        "Invalid value class",
                        new BasicException.Parameter("type", type),
                        new BasicException.Parameter("expected", Date.class.getName()),
                        new BasicException.Parameter("actual", value.getClass().getName())
                    );
                }
            } else if (PrimitiveTypes.DECIMAL.equals(type)) {
                if(!(value instanceof BigDecimal)) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.VALIDATION_FAILURE,
                        "Invalid value class",
                        new BasicException.Parameter("type", type),
                        new BasicException.Parameter("expected", BigDecimal.class.getName()),
                        new BasicException.Parameter("actual", value.getClass().getName())
                    );
                }
            } else if (
                PrimitiveTypes.SHORT.endsWith(type) ||
                PrimitiveTypes.INTEGER.endsWith(type) ||
                PrimitiveTypes.LONG.endsWith(type)
            ) {
                if(!(value instanceof Number)) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.VALIDATION_FAILURE,
                        "Invalid value class",
                        new BasicException.Parameter("type", type),
                        new BasicException.Parameter("expected", Number.class.getName()),
                        new BasicException.Parameter("actual", value.getClass().getName())
                    );
                }
            } else if (
                PrimitiveTypes.STRING.equals(type) ||
                type.startsWith("org:omg:PrimitiveTypes:")
            ) {
                if(!(value instanceof String)) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.VALIDATION_FAILURE,
                        "Invalid value class",
                        new BasicException.Parameter("type", type),
                        new BasicException.Parameter("expected", String.class.getName()),
                        new BasicException.Parameter("actual", value.getClass().getName())
                    );
                }
            } else if (type.startsWith("org:omg:model1")) {
                if(!(value instanceof Path)) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.VALIDATION_FAILURE,
                        "Invalid value class",
                        new BasicException.Parameter("type", type),
                        new BasicException.Parameter("expected", Path.class.getName()),
                        new BasicException.Parameter("actual", value.getClass().getName())
                    );
                }
            }
        }
    }
    
    private static void validateClassifiers(
        Model_1_0 model
    ) throws ServiceException {
        for(ModelElement_1_0 element : model.getContent()){
            if(model.isClassType(element)) {
                validateClassifier(element);
            }
        }
    }

    private static int getRetryLimit(
        Properties validationProperties
    ){
        try {
            return Integer.parseInt(validationProperties.getProperty("retry-limit","1"));
        } catch (NumberFormatException exception) {
            SysLog.warning("Retry limit determination failure", exception);
            return 0;
        }
    }
    
    private static void dumpModel(Model_1_0 model) {
        try {
            int i = 0;
            for(ModelElement_1_0 element : model.getContent()){
                SysLog.warning("Discarded Model Element " + ++i, element);
            }
        } catch (Exception dumpException) {
            SysLog.warning("Model Dump Failure", dumpException);
        }
    }
    
    private static void validateClassifier(
        ModelElement_1_0 classifier
    ) throws ServiceException {
        validateList(classifier, "allSupertype");
        validateList(classifier, "allSubtype");
    }

    private static void validateList(
        ModelElement_1_0 classifier,
        String feature
    ) throws ServiceException {
        if(classifier.objGetList(feature).isEmpty()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "Missing feature in classifier",
                new BasicException.Parameter(BasicException.Parameter.XRI, classifier.jdoGetObjectId()),
                new BasicException.Parameter("feature", feature)
            );
        }
    }

    /**
     * Calculates the checksum over the structural features
     * 
     * @param model
     * 
     * @return the checksum over the structural features
     * 
     * @throws ServiceException
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    static String calculateChecksum(
        Model_1_0 model
    ) throws ServiceException, NoSuchAlgorithmException, UnsupportedEncodingException{
        Set<String> modelElements = new TreeSet<String>();
        for(ModelElement_1_0 element : model.getContent()){
            if(model.isClassType(element)) {
                modelElements.add(element.getQualifiedName());
                for(Object featureId : element.objGetList("feature")) {
                    modelElements.add(model.getElement(featureId).getQualifiedName()); 
                }
            }
        }
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.reset();
        for(String modelElement : modelElements){
            md5.update(modelElement.getBytes("UTF-8"));
        }
        return new BigInteger(1, md5.digest()).toString(0x10);
    }
    
    /**
     * Collect the validation properties
     * 
     * @return the validation properties
     * 
     */
    static Properties getValidationProperties(
    ){
        try {
            Properties properties = new Properties();
            for(URL resource : Resources.getMetaInfResources("openmdxmof-validation.properties")) {
                properties.load(resource.openStream());
            }
            return properties;
        } catch (IOException exception) {
            SysLog.warning("Unable to retrieve validation properties, omit validation", exception);
            return null;
        }
    }
    
    /**
     * Tells whether some model validation is requested
     * 
     * @param validationProperties the (optional) validationProperties
     * 
     * @return {@code true} if validation is required
     */
    static boolean isValidationRequested(
        Properties validationProperties
    ){
        return 
            isXMLValidationRequested(validationProperties) ||
            isEnabled(validationProperties, "validate-classifiers") ||
            isEnabled(validationProperties, "validate-checksum");
    }

    /**
     * Tells whether XML validation is requested
     * 
     * @param validationProperties
     * 
     * @return {@code true} if XML validation is requested
     */
    static boolean isXMLValidationRequested(Properties validationProperties) {
        return isEnabled(validationProperties, "validate-xml");
    }
    
    /**
     * Tells whether a given option is enabled
     * 
     * @param validationProperties
     * @param option
     * 
     * @return {@code true} if the given option is enabled
     */
    private static boolean isEnabled(
        Properties validationProperties,
        String option
    ) {
        return 
            validationProperties != null && 
            Boolean.parseBoolean(validationProperties.getProperty(option,"false"));
    }

    /**
     * Print the model checksum
     * 
     * @param arguments
     * 
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws ServiceException
     */
    public static void main(
        String... arguments
    ) throws NoSuchAlgorithmException, UnsupportedEncodingException, ServiceException{
        System.setProperty("java.protocol.handler.pkgs","org.openmdx.kernel.url.protocol");
        System.out.println("checksum="+Model_1Factory.calculateChecksum());
    }

}
