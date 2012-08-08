/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: MarshallingRequestedList.java,v 1.6 2008/03/07 03:25:09 hburger Exp $
 * Description: MarshallingRequestedList class
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/07 03:25:09 $
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
package org.openmdx.compatibility.base.dataprovider.cci;

import java.io.InputStream;
import java.util.ListIterator;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.naming.Path;


public class MarshallingRequestedList 
  extends RequestedList {

    /**
     * 
     */
    private static final long serialVersionUID = 3905807491387175479L;

    /**
     * Constructor
     *
     * @param       marshaller
     *              The marshaller to be used to unmarshall a reply's objects
     */ 
    public MarshallingRequestedList(
        IterationProcessor iterationProcessor,
        Path referenceFilter,
        DataproviderObjectMarshaller_1_0 marshaller
    ){
        super(iterationProcessor,referenceFilter);
        this.marshaller = marshaller;
    }

    //------------------------------------------------------------------------
    // Implements Reconstructable
    //------------------------------------------------------------------------

    /**
     * Constructor
     */ 
    public MarshallingRequestedList(
        IterationProcessor iterationProcessor,
        Path referenceFilter,
        DataproviderObjectMarshaller_1_0 marshaller,
        InputStream stream
    ) throws ServiceException {
        super(iterationProcessor,referenceFilter,stream);
        this.marshaller = marshaller;
    }


    //------------------------------------------------------------------------
    // Extends RequestedList
    //------------------------------------------------------------------------

    /**
     * Returns a list iterator of the elements in this list (in proper
     * sequence), starting at the specified position in this list. The
     * specified index indicates the first element that would be returned
     * by an initial call to the next method. An initial call to the
     * previous method would return the element with the specified index
     * minus one.
     *
     * @param       index
     *              index of first element to be returned from the list
     *              iterator (by a call to the next method).
     *
     * @return      a list iterator of the elements in this list (in
     *              proper sequence), starting at the specified position
     *              in this list.
     *
     * @exception   IndexOutOfBoundsException
     *              if the index is out of range (index < 0 || index >
     *              size()).
     */
    @SuppressWarnings("unchecked")
    public ListIterator listIterator(
        int index
    ){
        return new MarshallingIterator(
            index,
            this.marshaller
        );
    }

  //------------------------------------------------------------------------
  // Variables
  //------------------------------------------------------------------------
  protected final DataproviderObjectMarshaller_1_0 marshaller;
            
}
