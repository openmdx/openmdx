/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Code Token 
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
package org.openmdx.base.wbxml;

/**
 * Token<ul>
 *   <li>bit 16 defines the code space<ul>
 *      <li>0 for the tag code space
 *      <li>1 for the attribute code space
 *  </ul>    
 *   <li>bits 8..15 define the code page
 *   <li>bits 0..7 define the code value
 *  </ul>
 */
public final class CodeToken {

    /**
     * Constructor 
     *
     * @param value
     */
    public CodeToken(
        int value
    ){
        this.value = value;
        this.length = -1;
        this.created = false;
    }

    /**
     * This constructor is used by the plug-in only
     *
     * @param value
     * @param length
     * @param created tells whether the entry has been created on the fly
     */
    public CodeToken(
        int value,
        int length, 
        boolean created
    ){
        this.value = value;
        this.length = length;
        this.created = created;
    }
    
    /**
     * The token's integer representation
     */
    private final int value;

    /**
     * The length of the value
     */
    private final int length;

    /**
     * Tells whether the corresponding entry has been newly created
     */
    private final boolean created;
    
    /**
     * Tells whether this token belongs to the attribute code space
     * 
     * @return <code>false</code> if it belongs to the tag code space
     */
    public boolean isAttributeCodeSpace(
    ){
        return (this.value & 0x10000) != 0; 
    }

    /**
     * The token value may be used to reconstruct a token
     * 
     * @return the token's integer representation
     */
    public int intValue(){
        return this.value;
    }
    
    /**
     * Retrieve the token's code page
     * 
     * @return the token's code page
     */
    public int getPage(){
        return (this.value & 0xFF00) >> 8;
    }

    /**
     * Retrieve the token's code
     * 
     * @return the token's code
     */
    public int getCode(
    ){
        return this.value & 0xFF;
    }
    
    /**
     * Retrieve the length of the matching entry
     * 
     * @return the length of the matching entry; or <code>-1</code> if it is unknown
     */
    public int length(
    ){
        return this.length ;
    }
    
    /**
     * Tells whether the corresponding entry has been newly created
     * 
     * @return <code>true</code> if token has been newly created
     */
    public boolean isNew(
    ){
        return this.created;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.value;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(
        Object that
    ) {
        return that instanceof CodeToken && ((CodeToken)that).value == this.value;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString(
    ) {
        return Integer.toHexString(this.value);
    }
    
}