/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestRolePersistenceNone_1.java,v 1.2 2004/04/02 16:59:05 wfro Exp $
 * Description: 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/04/02 16:59:05 $
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
package org.openmdx.test.compatibility.base.dataprovider.layer.model;

import java.util.Iterator;
import java.util.ListIterator;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.dataprovider.layer.persistence.none.InMemory_1;


/**
 * convert all strings starting with spice:// to path. 
 * This serves as a simulation of the real persistence layer.
 * 
 * @author anyff
 */
public class TestRolePersistenceNone_1 
  extends InMemory_1 {

    /**
     * @see Layer_1_0#activate(short, Configuration, Layer_1_0)
     */
    public void activate(short arg0, Configuration arg1, Layer_1_0 arg2)
        throws Exception {
        super.activate(arg0, arg1, arg2);
    }

    /**
     * @see Layer_1_0#deactivate()
     */
    public void deactivate() throws Exception {
        super.deactivate();
    }


    /**
     * converts strings to paths if they start with spice://
     */
    public void convertStringsToPath(
        DataproviderObject object
    ) {
        for (Iterator a = object.attributeNames().iterator();
            a.hasNext();
        ) {
            String attributeName = (String) a.next();
            
            for (ListIterator v = object.values(attributeName).populationIterator();
                v.hasNext();
            ) {
                Object value = v.next();
                if (value instanceof String) {
                    String valueString = (String) value;
                    if (valueString.startsWith("spice://")) {
                        v.set(new Path(valueString));
                    }
                }
            }
        }
    }        

    // test is only using get! don't need it all over!!
    public DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        DataproviderReply reply = super.get(header, request);
        
        convertStringsToPath(reply.getObject());
        
        return reply;
    }

}
