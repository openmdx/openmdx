/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: CutDerivedFeaturesXmlExporter.java,v 1.1 2009/05/26 14:31:21 wfro Exp $
 * Description: Cut Derived Features XML Exporter
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/26 14:31:21 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.application.dataprovider.exporter;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmdx.application.dataprovider.cci.RequestCollection;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.FilterProperty;


//------------------------------------------------------------------------

/**
 * Class doing what its name says. 
 * 
 * Mostly it is a copy of the original XMLExporter, improved by an 
 * additional DelegatingHandler which cuts out the derived features 
 * of the objects. ErrorHandling now reports intermediate errors instead
 * of just the last ones.
 * 
 * @author anyff
 */
public class CutDerivedFeaturesXmlExporter {

    /**
     * Setup an exporter with the required information.
     * 
     * 
     * @param serviceHeader  service header to use for accessing the sourceProvider
     * @param sourceProvider provider to read the data from
     * @param startPoints    list of paths within the provider, to start export from
     * @param exportStream   stream to export the xml output to
     * @param schemaString   string to write to the xml output as schema
     * @param model          model of the data read
     */
    public CutDerivedFeaturesXmlExporter(
        ServiceHeader header,
        RequestCollection reader, 
        List<Path> startPoints,
        Set<String> referenceFilters,
        Map<String,FilterProperty[]> attributeFilters,
        PrintStream exportStream,
        String schemaString,
        Model_1_0 model        
    ) {
        this.reader = reader;
        this.startPoints = startPoints;
        this.referenceFilters = referenceFilters;
        this.attributeFilters = attributeFilters;
        this.exportStream = exportStream;
        this.schemaString = schemaString;
        this.model = model;
    }    
    
    /**
     * Set the encoding to use for the xml output
     * @param encoding
     */
    public void setEncoding(
        String encoding
    ) {
        this.encoding = encoding;
    }

    /**
     * Get the encoding of the xml output
     * @return current encoding
     */
    public String getEncoding() {
        return this.encoding;
    }
    
    /**
     * Set the indent for the xml output
     * 
     * @param indentAmount number of spaces to indent
     */
    public void setIndentAmount(
        short indentAmount
    ) {
        this.xmlIndentAmount = indentAmount;
    }
    
    /**
     * Get the currently set indent amount.
     * @return current indent amount
     */
    public short getIndentAmount() {
        return this.xmlIndentAmount;
    }
    
    /**
     * Turn indentation on or off.
     * 
     * @param indent  
     */
    public void setIndenting(boolean indent) {
        this.xmlIndent = indent;
    }
    
    /**
     * is the indentation on or off.
     * @return
     */
    public boolean isIndenting() {
        return this.xmlIndent;
    }
    
    /**
     * After setting the desired behaviour, call export to execute the export 
     * 
     * @throws Exception
     */
    public void export(
    ) throws Exception {
      TraversalHandler traversalHandler = setupTraversalHandler();
      ErrorHandler errorHandler = setupErrorHandler();
      
      try {
         Traverser traverser = setupTraverser();              
         traverser.setTraversalHandler(traversalHandler);
         traverser.setErrorHandler(errorHandler);         
         // now run it:
         traverser.traverse();         
      } catch(ServiceException e) {
          // ignore
      }
      if (((KeepCauseErrorHandler)errorHandler).getCauseException() != null) {
         throw ((KeepCauseErrorHandler)errorHandler).getCauseException();
      }      
    }

   /**
    * Setup the Traverser
    */
   protected ProviderTraverser setupTraverser(
   ) {
      ProviderTraverser traverser = 
            new ProviderTraverser(
                this.reader, 
                this.model, 
                this.startPoints, 
                this.referenceFilters,
                this.attributeFilters
            );
      return traverser;
   }

   protected ErrorHandler setupErrorHandler() {
      return new KeepCauseErrorHandler();
   }

   /**
    * Setup the TraversalHandler. 
    *  
    * @throws ServiceException
    */
   protected TraversalHandler setupTraversalHandler() throws ServiceException {
      CutDerivedFeaturesHandler th = new CutDerivedFeaturesHandler(setupXMLTraversalHandler(), this.model);
      
      return th;
   }

   /**
    * Setup the TraversalHandler.  
    *  
    * @throws ServiceException
    */
   protected TraversalHandler setupXMLTraversalHandler() throws ServiceException {
      XmlContentHandler ch = setupContentHandler(this.exportStream);
   
      XMLExportHandler exportHandler = 
         new XMLExportHandler(
            this.model,
            "http://www.w3.org/2001/XMLSchema-instance",
            this.schemaString
         );
      exportHandler.setContentHandler(ch);
      return exportHandler;
   }
    
    /**
     * Setup XmlContentHandler
     */
    protected XmlContentHandler setupContentHandler(
        PrintStream target
    ) throws ServiceException {
        XmlContentHandler contentHandler = new XmlContentHandler(target);
        
        contentHandler.setAutoCollation(true);
        contentHandler.setEncoding(this.encoding);
        contentHandler.setIndentation(this.xmlIndent);
        contentHandler.setIndentationLength(this.xmlIndentAmount);
        
        return contentHandler;
    }
      
    //---------------------------------------------------------------------------
    // Variables
    //---------------------------------------------------------------------------
    
    /** encoding for the output xml */
    private String encoding = "UTF-8"; 
    
    /** indent the output xml */
    private boolean xmlIndent = true;
    
    /** amount of spaces to indent */
    private short xmlIndentAmount = 4;
    
    /** The model package for the data */
    private final Model_1_0 model;
    
    /** The file to export to */
    private PrintStream exportStream = null;
    
    /** Schema to put to the XML file generated */ 
    private final String schemaString;    

    /** start points for the export */
    private final List<Path> startPoints;
    
//  private final ServiceHeader header;   
    
    private final RequestCollection reader;
    private final Set<String> referenceFilters;
    private final Map<String,FilterProperty[]> attributeFilters;
    
}

//--- End of File -----------------------------------------------------------
