/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ExportTarget.java,v 1.5 2009/10/15 10:19:47 hburger Exp $
 * Description: Export Target 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/10/15 10:19:47 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.application.xml.spi;


import javax.jmi.reflect.RefObject;

import org.openmdx.base.exception.ServiceException;

/**
 * Export Target
 */
public interface ExportTarget {

    /**
     * Start export
     * 
     * @param empty <code>true</code> means that no authorities will be exported
     * 
     * @throws ServiceException
     */
    void exportProlog(
        boolean empty
    ) throws ServiceException;
    
    /**
     * Start export for a given authority
     * 
     * @param authority 
     * 
     * @throws ServiceException
     */
    void startAuthority(
        String authority
    ) throws ServiceException;

    /**
     * Start object export
     * 
     * @param refObject 
     * @param noOperation 
     * 
     * @throws ServiceException
     */
    void startObject(
        RefObject refObject, 
        boolean noOperation
    ) throws ServiceException;
    
    /**
     * Start export of attributes
     * 
     * @param empty <code>true</code> means that no attributes will be exported
     * 
     * @throws ServiceException 
     */
    void startAttributes(
        boolean empty
    ) throws ServiceException;
    
    /**
     * Start attribute export
     * 
     * @param qualifiedName
     * @param typeName 
     * @param multiplicity 
     * @param values
     * @param empty <code>true</code> means that no values will be exported
     * 
     * @throws ServiceException 
     */
    void startAttribute(
        String qualifiedName,
        String typeName, 
        String multiplicity, 
        Object values,
        boolean empty
    ) throws ServiceException;

    /**
     * Export mandatory or optional attribute value
     * 
     * @param typeName 
     * @param multiplicity the attribute's multiplicity
     * @param position 
     * @param value
     * 
     * @throws ServiceException 
     */
    void write(
        String typeName, 
        String multiplicity, 
        int position, 
        Object value
    ) throws ServiceException;
    
    /**
     * Complete attribute export
     * 
     * @param qualifiedName
     * @param multiplicity 
     * @param values
     * @param empty <code>true</code> means that no values have been
     * 
     * @throws ServiceException 
     */
    void endAttribute(
        String qualifiedName,
        String typeName, 
        String multiplicity, 
        Object values,
        boolean empty
    ) throws ServiceException;
    
    /**
     * Complete export of attributes
     * 
     * @param empty <code>true</code> means that no attributes have been exported
     * 
     * @throws ServiceException 
     */
    void endAttributes(
        boolean empty
    ) throws ServiceException;

    /**
     * Start export of children
     * 
     * @param empty <code>true</code> means that no references will be exported
     * 
     * @throws ServiceException 
     */
    void startChildren(
        boolean empty
    ) throws ServiceException;
    
    /**
     * Start reference export
     * 
     * @param name the simple name of the reference
     * @param empty <code>true</code> means that no objects will be exported
     * 
     * @throws ServiceException
     */
    void startReference(
	    String name, 
	    boolean empty
	) throws ServiceException;

	/**
	 * Complete reference export
	 * 
	 * @param reference the simple name of the reference
     * @param empty <code>true</code> means that no objects have been
	 * 
	 * @throws ServiceException
	 */
    void endReference(
	    String reference, boolean empty
	) throws ServiceException;

    /**
     * Complete export of children
     * 
     * @param empty <code>true</code> means that no references have been exported
     * 
     * @throws ServiceException 
     */
    void endChildren(boolean empty
    ) throws ServiceException;
	
    /**
	 * Complete object export
     * @param refObject 
     * 
     * @throws ServiceException
	 */
    void endObject(
	    RefObject refObject
	) throws ServiceException;

    /**
	 * Export completed for a given authority
	 * 
	 * @param authority 

	 * @throws ServiceException
	 */
    void endAuthority(
        String authority
	) throws ServiceException;

    /**
     * Export completed
     * 
     * @param empty <code>true</code> means that no authorities have been exported
     * 
     * @throws ServiceException
     */
    void exportEpilog(
        boolean empty
    ) throws ServiceException;
    
}