/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: IterationProcessor.java,v 1.3 2009/01/06 13:14:45 wfro Exp $
 * Description: spice: dataprovider object
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/06 13:14:45 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.cci;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;


/**
 * An object request
 */
public interface IterationProcessor {

    /**
     * Add an iteration request
     * 
     * @param referenceFilter
     * @param iterator
     * @param attributeSelector
     * @param attributeSpecifiers
     * @param position
     * @param size
     * @param direction
     * @param listener
     * 
     * @exception   ServiceException
     *              if no valid request can be added
     *
     * @see org.openmdx.application.dataprovider.cci.Directions
     */
    void addIterationRequest(
        Path referenceFilter,
        byte[] iterator,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifiers,
        int position,
        int size,
        short direction, DataproviderReplyListener listener
    ) throws ServiceException;

    /** 
     * Adds a get request retrieving all attributes specified by either 
     * the <code>attributeSelector</code> or the
     * <code>attributeSpecifier</code>.
     *
     * @param       path
     *              the object's path
     * @param       attributeSelector
     *              A (class dependent) predefined set of attributes to be
     *              returned
     * @param       attributeSpecifier
     *              An array specifying additional attributes to be returned;
     *              this argument may be <code>null</code>.
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              if no valid request can be added
     */
    public DataproviderObject_1_0 addGetRequest(
        Path path,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifier
    ) throws ServiceException;

}
