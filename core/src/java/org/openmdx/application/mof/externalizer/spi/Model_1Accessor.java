/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: Wrapper for a org::omg::model1 compliant in-process dataprovider.
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
 
/**
 * @author wfro
 */
package org.openmdx.application.mof.externalizer.spi;

import java.util.List;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;

import org.openmdx.application.cci.ConfigurationProvider_1_0;
import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.DataproviderLayers;
import org.openmdx.application.dataprovider.cci.DataproviderRequestProcessor;
import org.openmdx.application.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.application.dataprovider.kernel.Dataprovider_1;
import org.openmdx.application.mof.externalizer.cci.ModelImporter_1_0;
import org.openmdx.application.mof.repository.layer.application.LayerConfigurationEntries;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.kernel.log.SysLog;

//---------------------------------------------------------------------------  
@SuppressWarnings({"rawtypes","unchecked"})
public class Model_1Accessor {

     /**
       * Creates an in-process org::omg::model1 compliant dataprovider with in-memory 
       * persistence. With the method importModel() models can be imported to this
       * dataprovider and with operations such as externalizeAsJar() operations can be 
       * performed on the populated dataprovider.
       * @deprecated Use {@link #Model_1Accessor(String,String)} instead
       */
      @Deprecated
      public Model_1Accessor(
        String providerName
      ) throws ServiceException {
        this(
            providerName, 
            null // openmdxjdoDir
        );
      }

  /**
   * Creates an in-process org::omg::model1 compliant dataprovider with in-memory 
   * persistence. With the method importModel() models can be imported to this
   * dataprovider and with operations such as externalizeAsJar() operations can be 
   * performed on the populated dataprovider.
   * @param openmdxjdoDir base URL for openmdxjdo files
   */
  public Model_1Accessor(
    String providerName, 
    String openmdxjdoDir
  ) throws ServiceException {

    try {
      this.providerName = providerName;
      this.configurationProvider = new LocalConfigurationProvider(openmdxjdoDir);
      this.configurationProvider.activate();
      this.dataprovider = new Dataprovider_1(
        new Configuration(),
        this.configurationProvider
      );
    }
    catch(ServiceException e) {
      throw e.log();
    } catch(Exception e) {
      throw new ServiceException(e).log();
    }
  }

  //---------------------------------------------------------------------------
  class LocalConfigurationProvider
    implements ConfigurationProvider_1_0 {
      
    /**
     * Constructor 
     *
     * @param openmdxjdoMetadataDirectory The openmdxjdo files' base directory
     */
    LocalConfigurationProvider(
        String openmdxjdoMetadataDirectory
    ) {
        this.openmdxjdoMetadataDirectory = openmdxjdoMetadataDirectory;
    }

    private final String openmdxjdoMetadataDirectory;
    
    public void activate() throws Exception {
        //
    }

    public void deactivate() throws Exception {
        //
    }

    public Configuration getConfiguration(
      String[] section,
      Map specification
    ) throws ServiceException {
      Configuration configuration = new Configuration();        
      if(section != null && section.length == 1){

          // KERNEL
        if(section[0].equals(KERNEL_CONFIGURATION_SECTION)) {

          // namespaceId
          configuration.values(SharedConfigurationEntries.NAMESPACE_ID).put(
              0,
              "org::omg::model1"
          );
  
          // layers
          configuration.values(DataproviderLayers.toString(DataproviderLayers.INTERCEPTION)).put(
              0,
              org.openmdx.application.dataprovider.layer.interception.Standard_1.class.getName()
          );
          configuration.values(DataproviderLayers.toString(DataproviderLayers.TYPE)).put(
              0,
              org.openmdx.application.mof.repository.layer.type.Model_1.class.getName()
          );
          configuration.values(DataproviderLayers.toString(DataproviderLayers.APPLICATION)).put(
              0,
              org.openmdx.application.mof.repository.layer.application.Model_1.class.getName()
          );
          configuration.values(DataproviderLayers.toString(DataproviderLayers.MODEL)).put(
              0,
              org.openmdx.application.mof.repository.layer.model.Model_1.class.getName()
          );
          configuration.values(DataproviderLayers.toString(DataproviderLayers.PERSISTENCE)).put(
              0,
              org.openmdx.application.dataprovider.layer.persistence.none.InMemory_1.class.getName()
          );
        }

        // APPLICATION        
        else if(section[0].equals(DataproviderLayers.toString(DataproviderLayers.APPLICATION))) {
          configuration.values(LayerConfigurationEntries.OPENMDXJDO_METADATA_DIRECTORY).put(
              0, 
              this.openmdxjdoMetadataDirectory
          );
        } 
      }   
      return configuration;
    }
  
    //---------------------------------------------------------------------------
    // Variables
    //---------------------------------------------------------------------------
    static private final String KERNEL_CONFIGURATION_SECTION = "KERNEL";
  }
  
  //---------------------------------------------------------------------------
  /**
   * Removes all models stored under providerName (set with constructor)
   * and loads new model elements with modelImporter.
   */  
  public void importModel(
    ModelImporter_1_0 modelImporter
  ) throws ServiceException {  
    SysLog.trace("starting import");
    modelImporter.process(
      new ServiceHeader(),
      this.dataprovider,
      this.providerName
    );
    SysLog.trace("import finished");
  }

  //---------------------------------------------------------------------------
  /**
   * Calls the ModelPackage.externalizeAsPackage operation on the in-process
   * dataprovider and returns the externalized model package as byte[].
   *
   * @param modelName must be of the form x0:x1:x2:...
   */
  public byte[] externalizePackageAsJar(
    String modelName,
    List formats
  ) throws ServiceException {

    DataproviderRequestProcessor requests = new DataproviderRequestProcessor(
      new ServiceHeader(), 
      this.dataprovider
    );
     
    SysLog.trace("> Externalize package " + modelName);

    // !!! segmentName is of the form x0:x1:x2:...
    Path modelPackagePath = new Path(
      new String[]{
        "org:omg:model1",
        "provider", 
        this.providerName,
        "segment",
        modelName,
        "element",
        modelName + ":" + modelName.substring(modelName.lastIndexOf(':') + 1)
      }
    );

    SysLog.trace("> Externalize package");
    try {
        //
        // Call externalizePackage
        //
        MessageRecord request = (MessageRecord) Records.getRecordFactory().createMappedRecord(MessageRecord.NAME);
        request.setPath(modelPackagePath.getChild("externalizePackage"));
        MappedRecord params = Records.getRecordFactory().createMappedRecord("org:omg:model1:PackageExternalizeParams"); 
        request.setBody(params);
        List values = Records.getRecordFactory().createIndexedRecord(Multiplicity.LIST.toString());
        params.put("format", values);
        values.addAll(formats);
        MessageRecord result = requests.addOperationRequest(request);
        return (byte[]) result.getBody().get("packageAsJar");
    } catch (ResourceException e) {
        throw new ServiceException(e);
    }
  }

  //---------------------------------------------------------------------------  

  protected String providerName = null;  
  protected Dataprovider_1_0 dataprovider = null;
  private LocalConfigurationProvider configurationProvider = null;

}

//--- End of File -----------------------------------------------------------
