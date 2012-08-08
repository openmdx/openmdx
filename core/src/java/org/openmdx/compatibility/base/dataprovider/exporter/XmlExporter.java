/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: XmlExporter.java,v 1.21 2008/10/06 17:34:52 hburger Exp $
 * Description: XML Exporter
 * Revision:    $Revision: 1.21 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/10/06 17:34:52 $
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
package org.openmdx.compatibility.base.dataprovider.exporter;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.query.Filter;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;


//---------------------------------------------------------------------------
@SuppressWarnings("unchecked")
public class XmlExporter {

    //-----------------------------------------------------------------------
    public XmlExporter(
        ServiceHeader header,
        Dataprovider_1_0 dataSource, 
        PrintStream exportStream,
        Model_1_0 model,
        String encoding
    ) {
        this(
            header,
            new RequestCollection(header, dataSource),
            exportStream,
            model,
            encoding
        );
    }

    //-----------------------------------------------------------------------
    public XmlExporter(
        ServiceHeader header,
        RequestCollection reader,
        PrintStream exportStream,
        Model_1_0 model,
        String encoding
    ) {
        this.header = header;
        this.reader = reader;
        this.exportStream = exportStream;
        this.model = model;
        this.encoding = encoding;
    }

    //-----------------------------------------------------------------------
    /**
     * Get the encoding of the xml output
     * @return current encoding
     */
    public String getEncoding() {
        return this.encoding;
    }

    //-----------------------------------------------------------------------
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

    //-----------------------------------------------------------------------
    /**
     * Get the currently set indent amount.
     * @return current indent amount
     */
    public short getIndentAmount() {
        return this.xmlIndentAmount;
    }

    //-----------------------------------------------------------------------
    /**
     * Turn indentation on or off.
     * 
     * @param indent 
     */
    public void setIndenting(boolean indent) {
        this.xmlIndent = indent;
    }

    //-----------------------------------------------------------------------
    /**
     * Return wether the indentation is on or off.
     */
    public boolean isIndenting() {
        return this.xmlIndent;
    }

    //-----------------------------------------------------------------------
    /**
     * After setting the desired behaviour, call export() to execute the export 
     */
    public void export(
        List startPoints,
        String schemaString
    ) throws ServiceException {
        this.export(
            startPoints,
            null,
            null,
            schemaString
        );
    }

    //-----------------------------------------------------------------------
    /**
     * After setting the desired behaviour, call export() to execute the export 
     */
    public void export(
        List<Path> startPoints,
        Set<String> referenceFilter,
        Map<String,Filter> attributeFilter,
        String schemaString
    ) throws ServiceException {        
        this.startPoints = startPoints;
        this.referenceFilter = referenceFilter;
        this.attributeFilter = attributeFilter;
        this.schemaString = schemaString;

        TraversalHandler traversalHandler = setupTraversalHandler();
        ErrorHandler errorHandler = setupErrorHandler();      
        Exception finalException = null;
        try {
            Traverser traverser = setupTraverser();             
            traverser.setTraversalHandler(traversalHandler);         
            if(errorHandler != null) {
                traverser.setErrorHandler(errorHandler);
            }         
            traverser.traverse();         
        } 
        catch(Exception e) {
            // this exception which occurs at the end of the run is most of the
            // times that not all open elements are beeing closed - not very helpfull
            finalException = e;
        }
        treatExceptions(errorHandler, finalException);
    }

    //-----------------------------------------------------------------------
    /**
     * Treat the exceptions which occured. This can either be the finalException 
     * which was thrown at the end of execution or it may be an exception which 
     * is kept in the ExcptionHandler provided on execution.
     * 
     * @param errorHandler
     * @param finalException
     * @throws ServiceException
     */
    protected void treatExceptions(
        ErrorHandler errorHandler, 
        Exception  finalException
    ) throws ServiceException {        
        if (errorHandler instanceof KeepCauseErrorHandler &&
                ((KeepCauseErrorHandler)errorHandler).getCauseException() != null) {
            if (finalException instanceof ServiceException) {
                ServiceException fException = (ServiceException) finalException;
                ServiceException cException = ((KeepCauseErrorHandler)errorHandler).getCauseException();
                throw isIncluded(fException, cException) ? fException : fException.appendCause(cException);
            }
            else {
                throw ((KeepCauseErrorHandler)errorHandler).getCauseException();
            }
        }
        else if(finalException != null) {
            throw new ServiceException(
                finalException,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.PROCESSING_FAILURE,
                "Exception occured at the end."
            );
        }
    }

    private boolean isIncluded(
        ServiceException finalException,
        ServiceException causeException
    ){
        List finalExceptionStack = finalException.getCause().getExceptionStack(); 
        BasicException causeExceptionStack = causeException.getCause();
        return 
        finalExceptionStack.contains(causeExceptionStack) || 
        finalExceptionStack.contains(causeExceptionStack.getCause());
    }


    //-----------------------------------------------------------------------
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
                this.referenceFilter,
                this.attributeFilter
            );
        return traverser;
    }

    //-----------------------------------------------------------------------
    protected ErrorHandler setupErrorHandler() {
        return new KeepCauseErrorHandler();
    }

    //-----------------------------------------------------------------------
    /**
     * Setup the TraversalHandler. 
     *  
     * @throws ServiceException
     */
    protected TraversalHandler setupTraversalHandler(
    ) throws ServiceException {
        CutDerivedFeaturesHandler th = new CutDerivedFeaturesHandler(setupXMLTraversalHandler(), this.model);
        return th;
    }

    //-----------------------------------------------------------------------
    /**
     * Setup the TraversalHandler.  
     *  
     * @throws ServiceException
     */
    protected TraversalHandler setupXMLTraversalHandler(
    ) throws ServiceException {
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

    //-----------------------------------------------------------------------    
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
    protected final String encoding; 

    /** indent the output xml */
    protected boolean xmlIndent = true;

    /** amount of spaces to indent */
    protected short xmlIndentAmount = 4;

    /** The model package for the data */
    protected final Model_1_0 model;

    /** The file to export to */
    protected final PrintStream exportStream;

    /** Schema to put to the XML file generated */ 
    protected String schemaString = null;    

    /** start points for the export */
    protected List<Path> startPoints = null;

    // objects are retrieved matching the specified filter
    protected Set<String> referenceFilter = null;
    protected Map<String,Filter> attributeFilter = null;

    protected final ServiceHeader header;
    protected final RequestCollection reader;

}

//--- End of File -----------------------------------------------------------
