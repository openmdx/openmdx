/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: EntityManagerFactory_2.java,v 1.2 2009/03/03 17:23:08 hburger Exp $
 * Description: Entity Manager Factory
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/03 17:23:08 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.openmdx.application.cci.ConfigurationProvider_1_0;
import org.openmdx.application.cci.ConfigurationSpecifier;
import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.base.collection.SparseList;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.BasicException.Parameter;
import org.openmdx.uses.org.apache.commons.beanutils.BeanUtils;

/**
 * Entity Manager Factory
 */
class EntityManagerFactory_2 
    extends AbstractProvider_1
{

    /**
     * Constructor 
     *
     * @param configurationProvider
     * @throws ServiceException 
     */
    EntityManagerFactory_2(
        ConfigurationProvider_1_0 configurationProvider
    ) throws ServiceException{
        super(configurationProvider);
        //
        // Section
        // 
        String[] subSection = new String[SECTION.length + 1];
        System.arraycopy(SECTION, 0, subSection, 0, SECTION.length);
        //
        // Shared User Objects
        // 
        Map<String,Object> userObjects = new HashMap<String,Object>();
        Configuration configuration = configurationProvider.getConfiguration(
            SECTION,
            managerConfigurationSpecification // specification
        );
        this.lenient = configuration.isOn(
            ManagerFactoryConfigurationEntries.LENIENT
        );
        for(
            ListIterator<?> i = configuration.values(
                ManagerFactoryConfigurationEntries.USER_OBJECT_NAME
            ).populationIterator();
            i.hasNext();
        ){
            subSection[SECTION.length] = (String) i.next();
            getUserObject(
                configurationProvider,
                subSection,
                userObjects
            );
        }
        //
        // Plug-Ins
        // 
        for(
            ListIterator<?> i = configuration.values(
                ManagerFactoryConfigurationEntries.PLUG_IN
            ).populationIterator();
            i.hasNext();
        ){
            subSection[SECTION.length] = (String) i.next();
            this.plugInConfigurations.add(
                getPlugInConfiguration(
                    configurationProvider,
                    subSection,
                    userObjects
                )
            );
        }
    }
    
    private static final String[] SECTION = {
        ManagerFactoryConfigurationEntries.ENTITY_MANAGER
    };
    
   /**
    * 
    */
   private final List<Configuration> plugInConfigurations = new ArrayList<Configuration>();    
    
   /**
    * The plug-in specific configuration specifiers.
    */
   protected final static  Map<String,ConfigurationSpecifier> plugInConfigurationSpecification = 
       new HashMap<String,ConfigurationSpecifier>();

   /**
    * The manager specific configuration specifiers.
    */
   protected final static  Map<String,ConfigurationSpecifier> managerConfigurationSpecification = 
       new HashMap<String,ConfigurationSpecifier>();

   /**
    * Tells whether the requests are lenient or not
    */
   private final boolean lenient;
   
   
   static {
       //
       // Manager Configuration Specification
       //
       managerConfigurationSpecification.put(
           ManagerFactoryConfigurationEntries.PLUG_IN,
           new ConfigurationSpecifier (
               "The plug-ins",
               true, 
               0, 
               100
           )
       );
       managerConfigurationSpecification.put(
           ManagerFactoryConfigurationEntries.LENIENT,
           new ConfigurationSpecifier (
               "Tells whether the requests should be lenient or not",
               false, 
               0, 
               1
           )
       );
       //
       // Plug.in Configuration Specification
       //
       plugInConfigurationSpecification.put(
           SharedConfigurationEntries.MODEL_PACKAGE,
           new ConfigurationSpecifier (
               "Optional model packages specified as full qualified class names.",
               true, 
               0, 
               100
             )
         );
       plugInConfigurationSpecification.put(
           SharedConfigurationEntries.PACKAGE_IMPL,
           new ConfigurationSpecifier (
               "Java packages containing implementation classes specified as " +
               "Fully qualified java package name names.",
               true, 
               0, 
               100
           )
       );
       plugInConfigurationSpecification.put(
           ManagerFactoryConfigurationEntries.USER_OBJECT_NAME,
           new ConfigurationSpecifier (
               "Names of the plug-in scoped user objects",
               false, 
               0, 
               100
           )
       );
   }

   /**
    * Tells whether the requests are lenient or not
    * 
    * @return <code>true</code> if the requests are lenient
    */
   protected boolean isLenient(){
       return this.lenient;
   }
   
   /**
    * Prepare a plug-in's configuration according to the configuration
    * provider's entries.
    * 
    * @param configurationProvider
    * @param section the plug-in's section name
    * @param sharedUserObjects
    * 
    * @return the plug-in configuration
    * 
    * @throws ServiceException 
    */
   protected static Configuration getPlugInConfiguration(
       ConfigurationProvider_1_0 configurationProvider,
       String[] section,
       Map<String,Object> sharedUserObjects
   ) throws ServiceException{
       String[] subSection = new String[section.length + 1];
       System.arraycopy(section, 0, subSection, 0, section.length);
       //
       // User Objects
       //
       Map<String,Object> userObjects = new HashMap<String,Object>(sharedUserObjects);
       Configuration plugInConfiguration = configurationProvider.getConfiguration(
           section,
           plugInConfigurationSpecification
       );
       plugInConfiguration.values(
           PlugInManagerFactory_2.USER_OBJECT_MAP
       ).set(
           0,
           userObjects
       );
       for(
           ListIterator<?> j = plugInConfiguration.values(
               ManagerFactoryConfigurationEntries.USER_OBJECT_NAME
           ).populationIterator();
           j.hasNext();
       ){
           subSection[section.length] = (String) j.next();
           getUserObject(
               configurationProvider,
               subSection,
               userObjects
           );
       }
       //
       // Model Mapping
       // 
       Map<String,String> implementationMap = new HashMap<String,String>();
       plugInConfiguration.values(
           PlugInManagerFactory_2.IMPLEMENTATION_MAP
       ).set(
           0,
           implementationMap
       );
       SparseList<String> modelPackages = plugInConfiguration.values(
           SharedConfigurationEntries.MODEL_PACKAGE
       );
       for(
           ListIterator<?> j = plugInConfiguration.values(
               SharedConfigurationEntries.PACKAGE_IMPL
           ).populationIterator();
           j.hasNext();
       ){
           implementationMap.put(
               modelPackages.get(j.nextIndex()),
               j.next().toString()
           );
       }
       return plugInConfiguration;
   }

   /**
    * Retrieve a single user object
    * 
    * @param configurationProvider
    * @param userObjects
    * @param section
    * 
    * @throws ServiceException
    */
   static private void getUserObject(
       ConfigurationProvider_1_0 configurationProvider,
       String[] section,
       Map<String,Object> userObjects
   ) throws ServiceException {
       Configuration userObjectConfiguration = configurationProvider.getConfiguration(
           section,
           null // specification
       );  
       try {
           Object userObject = Classes.getApplicationClass(
               userObjectConfiguration.getFirstValue(ManagerFactoryConfigurationEntries.USER_OBJECT_CLASS)
           ).newInstance();
           for(Map.Entry<String, SparseList<?>> e : userObjectConfiguration.entries().entrySet()) {
               if(!ManagerFactoryConfigurationEntries.USER_OBJECT_CLASS.equals(e.getKey())) {
                   Boolean multivalued = isMultivalued(userObject, e.getKey(), e.getValue());
                   if(multivalued != null) {
                       if(multivalued.booleanValue()) {
                           for(
                               ListIterator<?> i = e.getValue().populationIterator();
                               i.hasNext();
                           ){
                               String indexedName = e.getKey() + '[' + i.nextIndex() + ']';
                               BeanUtils.setProperty(
                                   userObject, // bean
                                   indexedName, // name
                                   i.next() // value
                               );
                           }
                       } else {
                           BeanUtils.setProperty(
                               userObject, // bean
                               e.getKey(), // name
                               e.getValue().get(0) // value
                           );
                       }
                   }
               }
           }
           userObjects.put(
               section[section.length - 1],
               userObject
           );
       } catch (Exception exception) {
           throw new ServiceException(
               exception,
               BasicException.Code.DEFAULT_DOMAIN,
               BasicException.Code.ACTIVATION_FAILURE,
               "User object initialization failure",
               new Parameter[]{
                   new Parameter("section",(Object[])section)
               }
           );
       } 
    }

   /**
    * Test whether a given property is multi-valued
    * 
    * @param bean
    * @param name
    * @param values
    * 
    * @return <code>true</code> if the property is multi-valued
    */
   private static Boolean isMultivalued(
        Object bean,
        String name,
        SparseList<?> values
    ){
        if(values.isEmpty()) {
            return null;
        } else if (values.lastIndex() > 0) {
            return Boolean.TRUE;
        } else try {
            //
            // TODO to be corrected as the given predicate never evaluated to true!
            //
            return false;
//            Object old = BeanUtils.getProperty(bean, name);
//            return Boolean.valueOf(
//                old != null && (
//                    old instanceof List ||
//                    old.getClass().isArray()
//                )
//            );
        } catch (Exception exception) {
            return Boolean.FALSE;
        }
    }

    /**
     * Create an entity manager
     * 
     * @param persistenceManager
     * 
     * @return a newly created entity manager
     * 
     * @throws ServiceException
     */
    protected PersistenceManager newEntityManager(
        PersistenceManager persistenceManager
    ) throws ServiceException {
        PersistenceManager layerManager = persistenceManager;
        for(
            int i = this.plugInConfigurations.size() - 1;
            i >= 0;
            i--
        ){
            layerManager =  new PlugInManagerFactory_2(
                layerManager,
                this.plugInConfigurations.get(i)
            ).newPersistenceManager(
                layerManager
            );
        }
        return layerManager;
    }

}
