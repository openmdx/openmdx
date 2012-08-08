/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: FeedbackHandler.java,v 1.9 2008/05/12 10:45:51 wfro Exp $
 * Description: 
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/05/12 10:45:51 $
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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.naming.Path;

/**
 * Provide feedback to a user during long processings. 
 *  
 * @author anyff
 */
@SuppressWarnings("unchecked")
public class FeedbackHandler extends DelegatingHandler {

  /**
   * @param delegation
   */
  public FeedbackHandler(TraversalHandler delegation) {
    super(delegation);
    // TODO Auto-generated constructor stub
  }

    /**
     * Create a FeedbackHandler which delegates to theHandler, writes its feedback
     * to the feedbackStream and writes messages at the feedbackLevel.
     * <p>
     * feedbackLevels:
     * <ul>
     * <li> 0: no output at all</li>
     * <li> 1: a point per toplevel class</li>
     * <li> 2: point per class </li>
     * <li> 3: id of toplevel class </li>
     * <li> 4: indented id of classes </li>
     * </ul>
     * 
     * @param theHandler      handler which does the work
     * @param feedbackStream  stream to write to
     * @param feedbackLevel   see above for the levels
     */
    public FeedbackHandler(
        TraversalHandler theHandler,
        OutputStream feedbackStream,
        short feedbackLevel
    ) {
        super(theHandler);
        
        this.feedbackStream = new PrintWriter(feedbackStream);
        this.feedbackStream.flush();
        
        this.feedbackLevel = feedbackLevel; 
       
    }
 
    /**
     * Delegate call.
     * 
     * @see org.openmdx.compatibility.base.dataprovider.exporter.TraversalHandler#featureComplete(org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0)
     */
    public boolean featureComplete(
        Path reference,
        DataproviderObject_1_0 object
    ) throws ServiceException {
        
        switch (this.feedbackLevel) {
            case (0) : {
                // no output
                break;
            }
            case (1) : {
                // point per toplevel
                if (isTopLevel(object.path())) {
                    printWrapped(".");                    
                }    
                break;    
            }
            case (2) : {
                printWrapped(".");
                break;
            }
            case (3) : {
                if (isTopLevel(object.path())) {
                    printWrapped(object.path().getBase());
                }
                break;
            }
            case (4) : {
                if (isTopLevel(object.path())) {
                    this.indentation = 0;
                }
                printIndented(object.path().toString());
                break;
            }
            default: {
                // no output
            }
        }
        
        return super.featureComplete(
            reference,
            object
        );
    }

    /**
     * need to know when to indent.
     * 
     * @see org.openmdx.compatibility.base.dataprovider.exporter.TraversalHandler#startObject(java.lang.String, java.lang.String, java.lang.String, short)
     */
    public boolean startObject(
        Path reference,
        String qualifiedName,
        String qualifierName,
        String id,
        short operation
    ) throws ServiceException {        
        this.indentation++;
        return super.startObject(
            reference,
            qualifiedName, 
            qualifierName, 
            id, 
            operation
        );
    }

    /**
     * Need to know when to outdent.
     * 
     * @see org.openmdx.compatibility.base.dataprovider.exporter.TraversalHandler#startObject(java.lang.String, java.lang.String, java.lang.String, short)
     */
    public void endObject(
        String qualifiedName
    ) throws ServiceException {
        
        this.indentation--;
        
        super.endObject(qualifiedName);
    }

    /** 
     * Need the startPaths to find top level objects. 
     * 
     * Each object lying directly underneath a start path reference is a 
     * top level object.
     * (Else you would see nothing if synchronizing not on the segment.) 
     * 
     * @see org.openmdx.compatibility.base.dataprovider.exporter.TraversalHandler#startTraversal(java.util.List)
     */
    public void startTraversal(
        List startPaths
    ) throws ServiceException {
        this.startPaths = startPaths;
        
        super.startTraversal(startPaths);
    }

    /**
     * Decide if the object is a top level object.
     * <p>
     * It's a top level object if the path is underneath one of the start paths.
     * It's not bulletproof but it gives some feedback: as soon as one of 
     * the startPaths equals the qualifierName, its considered to be a 
     * top level object. Really the whole path should match, but we don't 
     * get it here (for now).
     * 
     * qualifierName 
     */
    protected boolean isTopLevel(
        Path objectPath
    ) {
        boolean topLevel = false;
        boolean found = false;
        
        for (Iterator i = startPaths.iterator();
            i.hasNext() && !found;
        ) {
            Path start = (Path) i.next();
            
            if (objectPath.startsWith(start)) {
                if (
                    objectPath.size() == start.size()       // object in startPaths
                    ||
                    objectPath.size() == start.size() + 1  // reference in startPaths
                ) {
                    topLevel = true;
                    found = true;
                }
                else {
                    found = true; 
                    // stop processing if it matched one of the paths.
                    // For this to work, the more specific paths must be 
                    // specified first, the general ones later. But this must 
                    // anyway be the case for the more specific ones to have 
                    // an effect.
                }
            }
        }
        return topLevel;
    }
    
    /**
     * Print the message and wrap the line if it becomes longer than 
     * OUTPUT_LINE_LENGTH.
     * 
     * @param message the message to print.
     */
    protected void printWrapped(
        String message
    ) {
        int len = message.length();
        
        if (this.charsPerLine + len > OUTPUT_LINE_LENGTH) {
            this.feedbackStream.println();
            this.charsPerLine = 0;
        }
        
        if (len > 1 && this.charsPerLine > 0) {
            this.feedbackStream.print(", ");
        }
        this.feedbackStream.print(message);
        this.feedbackStream.flush();
        this.charsPerLine+=len;
    }
    
    /**
     * Print the messages indented according to the containment.
     * 
     * @param message
     */
    protected void printIndented(
        String message
    ) {  
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < this.indentation; i++) {
             buffer.append(' ');
        }
        
        buffer.append(message);
        
        this.feedbackStream.println(buffer.toString());
        this.feedbackStream.flush();
    }
        
    /** stream to print on */
    protected PrintWriter feedbackStream = null;
    
    /** level granularity of the feedback */
    protected short feedbackLevel = 0;
    
    /** path where synchronization starts */
    protected List startPaths = null;
    
    /** how many chars on the current line */
    private int charsPerLine = 0;
    
    /** the current indentation level */
    private int indentation = 0;
    
    /** The length of an output line to use */
    public static int OUTPUT_LINE_LENGTH = 80;
}
