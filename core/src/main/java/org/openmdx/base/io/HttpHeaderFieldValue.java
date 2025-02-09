/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: HTTP Header Field Value 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.base.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;


/**
 * Parse HTTP Headers
 */
public class HttpHeaderFieldValue implements Iterable<HttpHeaderFieldContent>{

    /**
     * Constructor 
     *
     * @param headers
     */
    public HttpHeaderFieldValue(
        Enumeration<?> headers
    ){
        List<String> values = new ArrayList<String>();
        while(headers.hasMoreElements()) {
            String value = headers.nextElement().toString();
            int begin = 0;
            for(
                int end = value.indexOf(',');
                end >= 0;
                begin = end + 1, end = value.indexOf(',', begin)
            ){
                values.add(value.substring(begin, end).trim());
            }
            values.add(value.substring(begin).trim());
        }
        float minorOrder = 0.0f;
        for(String value : values) {
            this.content.add(
                new HttpHeaderFieldContent(
                    value,
                    minorOrder
                )
            );
            minorOrder += 1.0 / 1048576.0;
        }
        Collections.sort(this.content); 
    }

    
    /**
     * The parsed and sorted list of headers
     */
    private final List<HttpHeaderFieldContent> content = new ArrayList<HttpHeaderFieldContent>();
    
    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<HttpHeaderFieldContent> iterator() {
        return this.content.iterator();
    }
    
    /**
     * This method inspects the {@code q} parameter
     * 
     * @param defaultValue the default value is returned in case of an empty enumeration
     * 
     * @return the value of the header with the highest priority
     */
    public HttpHeaderFieldContent getPreferredContent(
        String defaultContent
    ){
        return this.content.isEmpty() ? (
            defaultContent == null ? null : new HttpHeaderFieldContent(defaultContent, 1.0f)
        ) : this.content.get(0);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString(
    ) {
        return this.content.toString();
    }

    /**
     * Tells whether there is no content
     * 
     * @return {@code true} if there is no content
     */
    public boolean isEmpty(){
        return this.content.isEmpty();
    }

}
