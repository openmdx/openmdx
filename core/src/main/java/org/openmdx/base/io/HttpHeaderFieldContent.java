/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: HTTP Header Field Content 
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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HTTP Header Field Content
 */
public final class HttpHeaderFieldContent implements Comparable<HttpHeaderFieldContent> {

    /**
     * Constructor 
     *
     * @param header
     */
    public HttpHeaderFieldContent(
        String header
    ){
        this(header, 0.0f);
    }
    
    /**
     * Constructor 
     * <p>
     * TODO handle escape characters
     *
     * @param header
     * @param minor
     */
    HttpHeaderFieldContent(
        String header,
        float minor
    ){
        int boa = header.indexOf(';');
        if(boa < 0) {
            this.value = header;
        } else {
            this.value = header.substring(0, boa).trim();
            for(
                int eoa;
                (eoa = header.indexOf(';', ++boa)) > 0;
                boa = eoa
            ){
                this.addParameter(header.substring(boa, eoa).trim());
            }
            this.addParameter(header.substring(boa).trim());
        }
        this.order = minor - HttpHeaderFieldContent.toQuality(
            this.getParameterValue("q",null)
        );
    }
    
    /**
     * The content field order
     */
    private final float order;
    
    /**
     * The content field value
     */
    private final String value;
    
    /**
     * The order of the amendments is kept
     */
    public final Map<String,String> parameters = new LinkedHashMap<String,String>();
    
    /**
     * Add an amendment
     * 
     * @param amendment
     */
    private void addParameter(
        String amendment
    ){
        int e = amendment.indexOf('=');
        if(e < 0) {
            this.parameters.put(amendment, null);
        } else {
            String key = amendment.substring(0, e).trim();
            String value = amendment.substring(e + 1).trim();
            this.parameters.put(
                key.toLowerCase(),
                value.startsWith("\"") && value.endsWith("\"") ? value.substring(1, value.length() - 1) : value
            );
        }
    }
    
    /**
     * Parse the {@code q} amendment
     * 
     * @param major
     * 
     * @return the {@code q} amendment's value
     */
    private static float toQuality(
        String major
    ){
        if(major == null) {
            return 1.0f;
        } else  try {
            return Float.parseFloat(major);
        } catch (NumberFormatException ignore) {
            return 1.0f; // Ignore non-quality q parameter
        }
    }
    
    /**
     * Retrieve the entry's value
     * 
     * @return the  entry's value
     */
    public String getValue(){
        return this.value;
    }
    
    /**
     * Retrieve the amendments
     * 
     * @return the amendments with lower-case keys and unquoted values
     */
    public Map<String, String> getParameters(){
        return Collections.unmodifiableMap(this.parameters);
    }
    
    /**
     * Retrieve the value of a single amendment
     * 
     * @param key the amendment's key
     * 
     * @return the unquoted value of a single amendment
     */
    public String getParameterValue(
        String key,
        String defaultValue
    ){
        String parameterValue = this.parameters.get(key.toLowerCase());
        return parameterValue == null ? defaultValue : parameterValue;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
//  @Override
    public int compareTo(
        HttpHeaderFieldContent that
    ) {
        return 
            this.order < that.order ? -1 : 
            this.order > that.order ? +1 : 
            0;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString(
    ) {
        StringBuilder text = new StringBuilder(this.value);
        for(Map.Entry<String, String> parameter : this.parameters.entrySet()) {
            text.append(';').append(parameter.getKey()).append('=').append(parameter.getValue());
        }
        return text.toString();
    }
    
}