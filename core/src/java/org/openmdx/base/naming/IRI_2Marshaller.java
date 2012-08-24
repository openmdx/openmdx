/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: Path/ObjectId Marshaller 
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
package org.openmdx.base.naming;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.text.conversion.UnicodeTransformation;
import org.openmdx.kernel.url.protocol.XRI_2Protocols;
import org.openmdx.kernel.url.protocol.XriAuthorities;


/**
 * Path/ObjectId Marshaller
 */
public final class IRI_2Marshaller
    implements Marshaller
{

    private IRI_2Marshaller(
    ){
        // Avoid external instantiation
    }
    
    /**
     * Memorize the singleton
     */ 
    final static private Marshaller instance = new IRI_2Marshaller();
    
    /**
     * Return the singleton
     */ 
    static public Marshaller getInstance(
    ){
        return IRI_2Marshaller.instance;
    }

    
    //------------------------------------------------------------------------
    // Implements Marshaller
    //------------------------------------------------------------------------

    /**
     * Marshal a CharSequence[] into a CharSequence
     * 
     * @param     charSequences
     *            The array of CharSequences to be marshalled.
     * 
     * @return      A CharSequence containing the marshalled objects.
   */
    public Object marshal (
        Object charSequences
    ) throws ServiceException {
        throw new UnsupportedOperationException(
            "Direct IRI marshalling not supported yet"
        );
    }

    /**
     * Unmarshal a CharSequence into a CharSequence[].
     * 
     * @param         marshalledObjects
     *            A string containing a marshalled sequence of objects
     * 
     * @return    A String array containing the unmarshaled sequence
     *                  of objects.
     * @exception ServiceException ILLEGAL_ARGUMENT
    */
    public Object unmarshal (
        Object charSequence
    ) throws ServiceException {
        String source = charSequence.toString();
        String iri;
        if(source.indexOf('%') < 0) {
            iri = source;
        } else {
            int sourceLength = source.length();
            byte[] target = new byte[sourceLength];
            int targetLength = 0;
            for(
                int sourceIndex = 0;
                sourceIndex < sourceLength;
            ){
                if(source.charAt(sourceIndex) == '%' && sourceIndex + 2 < sourceLength) {
                    target[targetLength++] = (byte) Integer.parseInt( // Parse as int to cope with F0 to FF
                        source.substring(++sourceIndex, sourceIndex+=2),
                        16
                    );
                } else {
                    target[targetLength++] = (byte) source.charAt(sourceIndex++); 
                }
            }
            iri = UnicodeTransformation.toString(target,0, targetLength);
        }
        if(iri.startsWith(XriAuthorities.OPENMDX_AUTHORITY)) {
            iri = XRI_2Protocols.SCHEME_PREFIX + iri;
        }
        try {
            return XRI_2Marshaller.getInstance().unmarshal(iri);
        } catch (RuntimeException exception) {
            throw new ServiceException(exception);
        }
    }
        
}
