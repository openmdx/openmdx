/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: PathOnlyMarshaller.java,v 1.2 2009/01/06 13:14:45 wfro Exp $
 * Description: spice: DataproviderObject marshaller interface
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/06 13:14:45 $
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
package org.openmdx.application.dataprovider.cci;

import java.io.Serializable;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.naming.Path;


/**
 * Marshals from and to generic dataprovider objects ignoring everything other
 * than the dataprovider objects path.
 */
public class PathOnlyMarshaller 
    implements DataproviderObjectMarshaller_1_0, Marshaller, Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 3256726182241383728L;


    /**
     * Constructor for Serialization only
     */
    public PathOnlyMarshaller(
    ){
        super();
    }
    

    /**
     * Get instance
     *
     * @return      the singleton
     */ 
    public static PathOnlyMarshaller getInstance(
    ){
        return PathOnlyMarshaller.instance;
    }
    

    //------------------------------------------------------------------------
    // Implements Dataprovider_1_0
    //------------------------------------------------------------------------
    
    /**
     * Marshals the source to an object tree. An object tree is a
     * DataproviderObject containing values possibly of type
     * DataproviderObject.
     *
     * @param   source
     *          is transformed to a DataproviderObject tree. The semantics of
     *          source is defined by the class implementing the interface.
     *
     * @return  DataproviderObject with values of the standard types
     *          String, BigDecimal, byte[].
     *
     * @exception   ServiceException    DATA_CONVERSION
     *              in the source can't be marshalled into a dataprovider
     *              object
     */
    public DataproviderObject_1_0 toDataproviderObject(
        Object source
    ) throws ServiceException {
        return new DataproviderObject(
            (Path)source
        );
    }

    /**
     * Marshals a DataproviderObject to the destination object. The
     * format and semantics of the object is defined by the class implementing
     * the interface.
     *
     * @param   source
     *          DataproviderObject containing values of the standard types
     *          String, BigDecimal, byte[].
     *
     * @return marshalled destination object.
     *
     * @exception   ServiceException    DATA_CONVERSION
     *              in the source can't be marshalled into a destination
     *              object
     */
    public Object fromDataproviderObject(
        DataproviderObject_1_0 source
    ) throws ServiceException {
        return source.path();
    }


    //------------------------------------------------------------------------
    // Implements Marshaller
    //------------------------------------------------------------------------

    /**
     * Marshals an object
     *
     * @param  source    The object to be marshalled
     * 
     * @return           The marshalled object
     * 
     * @exception        ServiceException
     *                   DATA_CONVERSION: Object can't be marshalled
     */
    public Object marshal (
        Object source
    ) throws ServiceException {
        return fromDataproviderObject((DataproviderObject_1_0)source);
    }

    /**
     * Unmarshals an object
     *
     * @param  source   The marshalled object
     * 
     * @return          The unmarshalled object
     * 
     * @exception       ServiceException
     *                  DATA_CONVERSION: Object can't be unmarshalled
     */
    public Object unmarshal (
        Object source
    ) throws ServiceException {
        return toDataproviderObject(source);
    }


    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------

    /**
     * The singleton
     */ 
    final static private PathOnlyMarshaller instance=new PathOnlyMarshaller();

}
