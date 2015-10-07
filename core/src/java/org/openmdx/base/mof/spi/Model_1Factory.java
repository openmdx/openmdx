/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Model_1Factory
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2010, OMEX AG, Switzerland
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
 */
package org.openmdx.base.mof.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelBuilder_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.loading.Resources;
import org.openmdx.kernel.log.SysLog;

/**
 * Model_1Factory
 */
public class Model_1Factory {

    /**
     * The volatile singleton holder allows lazy synchronization
     */
    private static volatile Model_1_0 model;

    /**
     * Avoid compile time dependency on model builder class
     */
    final static private String MODEL_BUILDER_1_CLASS = "org.openmdx.application.mof.repository.accessor.ModelBuilder_1";
    
    /**
     * Tells whether the model repository is already prepared
     * 
     * @return <code>true</code> if the model is loaded
     */
    public static boolean isLoaded(
    ) {
        return Model_1Factory.model != null;
    }
    
    /**
     * Retrieve the MOF repository
     * 
     * @return the singleton
     * 
     * @exception RuntimeServiceException in case of a model acquisition failure
     */
    public static Model_1_0 getModel(
    ){
        if(!Model_1Factory.isLoaded()) {
            getModel(false);
        }
        return Model_1Factory.model;
    }

    /**
     * Retrieves the model once only unless a re-load is requested.
     * <p>
     * <em>This method should rarely be used from another class.
     * 
     * @param reload if <code>true</code> the current model cache is ignored
     * 
     * @exception RuntimeServiceException in case of a model acquisition failure
     */
    public static synchronized void getModel(
        boolean reload
    ) {
        if(!Model_1Factory.isLoaded()) {
            SysLog.detail("Initial model loading");
            Model_1Factory.model = loadModel();
        } else if (reload) {
            SysLog.warning("Forced model re-loading");
            Model_1Factory.model = loadModel();
        }
    }

    /**
     * Load the model
     * 
     * @return a new model repository instance
     * 
     * @exception RuntimeServiceException in case of a model acquisition failure
     */
    private static Model_1_0 loadModel(){
        final Properties validationProperties = Model_1Validator.getValidationProperties();
        if(Model_1Validator.isValidationRequested(validationProperties)){
            final boolean xmlValidation = Model_1Validator.isXMLValidationRequested(validationProperties);
            for(int retryCount = 0; true; retryCount++) {
                Model_1_0 model = loadModel(xmlValidation);
                if(Model_1Validator.isValid(validationProperties, model, retryCount)){
                    return model;
                }
            }
        } else {                                                                                                                                                                                                                                                                                                                                                                                                       
            return loadModel(false);
        }
    }
    
    /**
     * Load the model and calculate its checksum
     * 
     * @return the checksum
     *  
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws ServiceException
     */
    public static String calculateChecksum(
    ) throws NoSuchAlgorithmException, UnsupportedEncodingException, ServiceException{
        return Model_1Validator.calculateChecksum(loadModel(false));
    }

    /**
     * Provide the resource files
     * 
     * @param name the system property or meta information file name
     * 
     * @return the mode dump
     * 
     * @throws IOException
     */
    private static Iterable<URL> getModelResource(
        String name
    ) throws IOException{
        final String modelResource = System.getProperty(name);
        if(modelResource == null || modelResource.trim().isEmpty()) {
            return Resources.getMetaInfResources(name);
        } else {
            return Collections.singleton(new URL(modelResource.trim()));
        }
    }
        
    /**
     * The model repository content to be restored
     * 
     * @return the URL of the model repository content to be restored
     *  
     * @throws IOException
     * @throws ServiceException
     */
    private static URL getModelDump(
    ) throws IOException, ServiceException {
        final Iterator<URL> modelDumps = getModelResource("openmdxmof.wbxml").iterator();
        if(modelDumps.hasNext()) {
            final URL modelDump = modelDumps.next();
            if(modelDumps.hasNext()) {
                final URL conflictingDump = modelDumps.next();
                if (Boolean.getBoolean("org.openmdx.mof.IgnoreRedundantModelDumps")) {
                   SysLog.log(
                       Level.WARNING, 
                       "Sys|Found more than one model dump|Using {0} while ignoring {1}", modelDump, conflictingDump
                   );
                } else {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.INVALID_CONFIGURATION,
                        "There is more than one model dump in the meta information enbironment",
                        new BasicException.Parameter("dump", modelDump, conflictingDump)
                    );
                }                 
            }
            return modelDump;
        } else {
            return null;
        }
    }

    /**
     * Determine the model packages to be loaded.
     * 
     * @return the model packages enumerated by the openmdxmof.properties
     * configuration files
     * 
     * @throws IOException
     * @throws ServiceException
     */
    private static Set<String> getModelPackages(
    ) throws IOException, ServiceException {
        final Set<String> modelPackages = new LinkedHashSet<String>();
        final List<URL> propertyURLs = new ArrayList<URL>();
        for(URL propertyURL : getModelResource("openmdxmof.properties")) {
            propertyURLs.add(propertyURL);
            final BufferedReader in = new BufferedReader(new InputStreamReader(propertyURL.openStream()));
            for(
                String line;
                (line = in.readLine()) != null;
            ) {
                line = line.trim();
                if(
                    !line.isEmpty() && 
                    !line.startsWith("#") && 
                    !line.startsWith("!") &&
                    !modelPackages.contains(line) 
                ) {
                    modelPackages.add(line);
                }
            }
            in.close();
        }
        SysLog.log(
            Level.INFO,
            "The model package set {0} is based on the openmdxmof.properties located at {1}",
            modelPackages,
            propertyURLs
        );
        return modelPackages;
    }
    
    /**
     * Load the model
     * 
     * @param xmlValidation
     * 
     * @return the model
     */
    private static Model_1_0 loadModel(
        boolean xmlValidation
    ) {
        try {
            final URL modelDump = getModelDump();
            final ModelBuilder_1_0 modelBuilder;
            if(modelDump == null) {
                modelBuilder = Classes.newPlatformInstance(
                    MODEL_BUILDER_1_CLASS,	
                    ModelBuilder_1_0.class, 
                    Boolean.valueOf(xmlValidation),
                    getModelPackages(),
                    Boolean.FALSE // meta-model		
                );
            } else {
                modelBuilder = Classes.newPlatformInstance(
                    MODEL_BUILDER_1_CLASS,   
                    ModelBuilder_1_0.class, 
                    Boolean.valueOf(xmlValidation),
                    modelDump
                );                
            }
            return modelBuilder.build();
        }  catch (Exception exception) {
            SysLog.log(
                Level.SEVERE,
                "Model acquisition failure",
                exception
            );
            throw new RuntimeServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "Model acquisition failure"
            );
        }
    }
        
}
