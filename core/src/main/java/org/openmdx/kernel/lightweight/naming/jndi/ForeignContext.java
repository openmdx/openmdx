/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Foreign Context 
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
package org.openmdx.kernel.lightweight.naming.jndi;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.openmdx.base.text.conversion.URITransformation;
import org.openmdx.kernel.lightweight.naming.spi.UnmodifiableContext;

/**
 * Foreign Context
 */
class ForeignContext extends UnmodifiableContext {

    /**
     * Constructor 
     */
    ForeignContext(
    ){
    }

    /**
     * The foreign context cache
     */
    private final Map<String,Context> foreignContexts = new HashMap<String, Context>();
    
    /* (non-Javadoc)
     * @see javax.naming.Context#lookup(java.lang.String)
     */
    public Object lookup(
        String url
    ) throws NamingException {
        String name;
        String query;
        int q = url.indexOf('?');
        if(q < 0){
            name = null;
            query = url.substring(5);
        } else {
            name = URITransformation.decode(url.substring(5, q));
            query = url.substring(q + 1);
        }
        Context foreignContext = this.foreignContexts.get(query);
        if(foreignContext == null) {
            Map<String,?> conf = getParameters(query);
            this.foreignContexts.put(
                query,
                foreignContext = new InitialContext(
                    new Hashtable<String, Object>(conf)
                )
            );    
        }
        return name == null ? foreignContext : foreignContext.lookup(name);
    }

}
