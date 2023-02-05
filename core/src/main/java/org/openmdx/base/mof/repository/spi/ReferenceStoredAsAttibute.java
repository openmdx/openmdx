/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Reference Stored As Attibute 
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
package org.openmdx.base.mof.repository.spi;

import org.openmdx.base.mof.repository.cci.AttributeRecord;

/**
 * ReferenceStoredAsAttibute
 */
public class ReferenceStoredAsAttibute
    extends ReferenceRecord 
    implements AttributeRecord {

    /**
     * Constructor 
     *
     * @param derived
     */
    public ReferenceStoredAsAttibute(
        ReferenceRecord source,
        boolean derived,
        String multiplicity
    ) {
        this.derived = derived;
        putAll(source);
        setMultiplicity(multiplicity);
    }

    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = 8126786916829317687L;
    
    private final boolean derived;
    
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.repository.cci.AttributeRecord#isDerived()
     */
    @Override
    public boolean isDerived() {
        return derived;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.mof.repository.cci.AttributeRecord#getMaxLength()
     */
    @Override
    public int getMaxLength() {
        return 1024;
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public Object get(Object key) {
        if(key instanceof AttributeRecord.Member) {
            return mixIn(((AttributeRecord.Member)key).name());
        } else if (key instanceof String) {
            return mixIn((String)key);
        } else {
            return super.get(key);
        }
    }
    
    private Object mixIn(String key) {
        if(AttributeRecord.Member.isDerived.name().equals(key)) {
            return Boolean.valueOf(isDerived());
        } else if(AttributeRecord.Member.maxLength.name().equals(key)) {
            return Integer.valueOf(getMaxLength());
        } else {
            return super.get(key);
        }
    }
    
}
