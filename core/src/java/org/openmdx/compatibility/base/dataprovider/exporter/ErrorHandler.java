/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ErrorHandler.java,v 1.3 2004/04/02 16:59:01 wfro Exp $
 * Description: Handles errors occuring during traversal of data source. 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/04/02 16:59:01 $
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

import org.openmdx.base.exception.ServiceException;

/**
 * The Traverser uses this interface to inform about errors encountered while
 * reading its source. 
 * <p>
 * Exceptions from the TraversalHandler are passed on to the ErrorHandler.  
 * Exceptions from the ErrorHandler are considered fatal and fatalException() 
 * is called, if it was not the emitter of the exception.
 * <p>
 * The TraversalHandlers endTraversal is called in any case.
 * 
 * @author anyff
 */
public interface ErrorHandler {
    
    /**
     * A fatal error occured. 
     * <p>
     * Processing  will stop. 
     * <p>
     * If fatalError throws an exception, the Traverser must terminate the 
     * processing immediately by calling endTraverse on the TraversalHandler.
     * 
     * @param fatalError   contains the error and any excpetions leading to it
     * @throws ServiceException  
     */
    public void fatalError(ServiceException fatalError) 
        throws ServiceException;
    
    /**
     * An error occured but processing will continue. 
     * <p>
     * If an exception is thrown, this is considered a fatal error. The
     * ErrorHandlers fatalError() gets called and the processing stops.
     * 
     * @param error  contains the error and any exceptions leading to it
     * @throws ServiceException
     */
    public void error(ServiceException error)
        throws ServiceException;
        
    /**
     * A warning occured, but processing will continue. 
     * <p>
     * If an exception is thrown, this is considered a fatal error. The
     * ErrorHandlers fatalError() gets called and the processing stops.
     * <p>
     * Warnings can be issued e.g.
     * <ul>
     * <li> if the Traverser omits further traversal of an object or reference
     * </li>
     * <li> if the TraversalHandler or ErrorHandler is exchanged </li>
     * </ul>
     * 
     * @param warning
     * @throws ServiceException
     */
    public void warning(ServiceException warning)
        throws ServiceException;
    

}
