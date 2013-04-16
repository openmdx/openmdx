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
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
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
     * Retrieve the MOF repository
     * 
     * @return the singleton
     */
    public static Model_1_0 getModel(
    ) {
        if(Model_1Factory.model == null) {
            synchronized(Model_1Factory.class) {
                if(Model_1Factory.model == null) {
                    Properties validationProperties = Model_1Validator.getValidationProperties();
                    if(Model_1Validator.isValidationRequested(validationProperties)){
                        final boolean xmlValidation = Model_1Validator.isXMLValidationRequested(validationProperties);
                        for(int retryCount = 0; model == null; retryCount++) {
                            Model_1_0 model = loadModel(xmlValidation);
                            if(Model_1Validator.isValid(validationProperties, model, retryCount)){
                                Model_1Factory.model = model;
                            }
                        }
                    } else {                                                                                                                                                                                                                                                                                                                                                                                                       
                        final boolean xmlValidation = false;
                        Model_1Factory.model = loadModel(xmlValidation);
                    }
                }
            }
        }
        return Model_1Factory.model;
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
        String modelResource = System.getProperty(name);
        if(modelResource == null || modelResource.trim().isEmpty()) {
            return Resources.getMetaInfResources(name);
        } else {
            return Collections.singleton(new URL(modelResource.trim()));
        }
    }
        
    /**
     * Try to restore the model repository
     * 
     * @param model
     * @return
     * @throws IOException
     * @throws ServiceException
     */
    private static boolean restoreModelPackages(
        Model_1_0 model
    ) throws IOException, ServiceException {
        Iterator<URL> modelDumps = getModelResource("openmdxmof.wbxml").iterator();
        if(modelDumps.hasNext()) {
            URL modelDump = modelDumps.next();
            if(modelDumps.hasNext()) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INVALID_CONFIGURATION,
                    "There is more than one model dump in the meta information enbironment",
                    new BasicException.Parameter("dump", modelDump, modelDumps.next())
               );
            }
            long repositoryCreated = System.currentTimeMillis();
            model.load(modelDump.openStream());
            long repositoryRestored =  System.currentTimeMillis() - repositoryCreated;
            SysLog.performance("Model restored", repositoryRestored);
            SysLog.log(
                Level.INFO,
                "The model repository has been restored from {0}",
                modelDump
            );
            return true;
        }
        return false;
    }
        
    /**
     * @param model
     * @throws IOException
     * @throws ServiceException
     */
    private static void mergeModelPackages(
        Model_1_0 model
    ) throws IOException, ServiceException {
        List<String> modelPackages = new ArrayList<String>();
        List<URL> propertyURLs = new ArrayList<URL>();
        for(URL propertyURL : getModelResource("openmdxmof.properties")) {
            propertyURLs.add(propertyURL);
            BufferedReader in = new BufferedReader(new InputStreamReader(propertyURL.openStream()));
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
        long repositoryCreated = System.currentTimeMillis();
        model.addModels(modelPackages);
        long repositoryPopulated =  System.currentTimeMillis() - repositoryCreated;
        SysLog.performance("Model population", repositoryPopulated);
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
            Model_1_0 model = Classes.newApplicationInstance(
                Model_1_0.class, 
                "org.openmdx.application.mof.repository.accessor.Model_1"
            );
            if(!restoreModelPackages(model)) {
                mergeModelPackages(model);
                
            }
            return model;
        }  catch(Exception exception) {
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
    
    /**
     * Tells whether the MOF repository is already prepared
     * 
     * @return <code>true</code> if the model is loaded
     */
    public static boolean isLoaded(
    ) {
        return Model_1Factory.model != null;
    }
    
    /**
     * Print the model checksum
     * 
     * @param arguments
     * 
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws ServiceException
     * 
     * @deprecated Use {@link Model_1Validator#main(String...)} instead
     */
    public static void main(
        String... arguments
    ) throws NoSuchAlgorithmException, UnsupportedEncodingException, ServiceException{
        Model_1Validator.main(arguments);
    }

}
