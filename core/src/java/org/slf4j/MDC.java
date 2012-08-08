/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: MDC.java,v 1.6 2008/03/13 18:46:14 hburger Exp $
 * Description: Lenient Mapped Diagnostic Context
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/13 18:46:14 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2008, OMEX AG, Switzerland
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
 * ______________________________________________________________________
 * 
 * The original file was provided by SLF4J (http://www.slf4j.org)
 * under the following terms:
 * 
 * Copyright (c) 2004-2007 QOS.ch
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.slf4j;

import org.slf4j.helpers.BasicMDCAdapter;
import org.slf4j.helpers.LenientBinder;
import org.slf4j.spi.MDCAdapter;

/**
 * This class hides and serves as a substitute for the underlying logging
 * system's MDC implementation.
 * 
 * <p>
 * If the underlying logging system offers MDC functionality, then SLF4J's MDC,
 * i.e. this class, will delegate to the underlying system's MDC. Note that at
 * this time, only two logging systems, namely log4j and logback, offer MDC
 * functionality. If the underlying system does not support MDC, then SLF4J will
 * silently drop MDC information.
 * 
 * <p>
 * Thus, as a SLF4J user, you can take advantage of MDC in the presence of log4j
 * or logback, but without forcing log4j or logback as dependencies upon your
 * users.
 * 
 * <p>
 * For more information on MDC please see the <a
 * href="http://logback.qos.ch/manual/mdc.html">chapter on MDC</a> in the
 * logback manual.
 * 
 * <p>
 * Please note that all methods in this class are static.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.4.1
 */
public class MDC {

    /**
     * Constructor 
     */
    private MDC() {
        // Avoid instantiation
    }

    /**
     * The MDC adapter singleton
     */
    private static final MDCAdapter mdcAdapter = new LenientAdapter().narrow();

    /**
     * Put a context value (the <code>val</code> parameter) as identified with
     * the <code>key</code> parameter into the current thread's context map.
     * The <code>key</code> parameter cannot be null. The code>val</code> parameter 
     * can be null only if the underlying implementation supports it.
     * 
     * <p>
     * This method delegates all work to the MDC of the underlying logging system.
     * 
     * @throws IllegalArgumentException in case the "key" parameter is null
     */
    public static void put(String key, String val) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key parameter cannot be null");
        }
        MDC.mdcAdapter.put(key, val);
    }

    /**
     * Get the context identified by the <code>key</code> parameter. The 
     * <code>key</code> parameter cannot be null.
     * 
     * <p>This method delegates all work to the MDC of the underlying logging system. 
     * 
     * @return the string value identified by the <code>key</code> parameter.
     * @throws IllegalArgumentException in case the "key" parameter is null
     */
    public static String get(String key) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key parameter cannot be null");
        }
        return MDC.mdcAdapter.get(key);
    }

    /**
     * Remove the the context identified by the <code>key</code> parameter using
     * the underlying system's MDC implementation. The  <code>key</code> parameter 
     * cannot be null. This method does nothing if there is no previous value 
     * associated with <code>key</code>.
     * 
     * @throws IllegalArgumentException in case the "key" parameter is null
     */
    public static void remove(String key) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key parameter cannot be null");
        }
        MDC.mdcAdapter.remove(key);
    }

    /**
     * Clear all entries in the MDC of the underlying implementation.
     */
    public static void clear() {
        MDC.mdcAdapter.clear();
    }

    /**
     * Returns the MDCAdapter instance currently in use.
     * 
     * @return the MDcAdapter instance currently in use.
     * @since 1.4.2
     */
    public static MDCAdapter getMDCAdapter() {
        return MDC.mdcAdapter;
    }

    
    //------------------------------------------------------------------------
    // Class LenientAdapter
    //------------------------------------------------------------------------
    
    /**
     * Lenient MDC Adapter
     * <p>
     * This implementation uses<ul>
     * <li>a StaticMDCBinder if available in the current classloader
     * <li>a BasicMDCAdapter otherwise
     * </ul>
     */
    static class LenientAdapter
        extends LenientBinder<MDCAdapter,Object>
        implements MDCAdapter
    {
    
        /**
         * Constructor 
         */
        LenientAdapter() {
            super("org.slf4j.impl.StaticMDCBinder");
        }
    
        /* (non-Javadoc)
         * @see org.slf4j.helpers.LenientBinder#getFallbackDelegate()
         */
        protected MDCAdapter getFallbackDelegate() {
            return new BasicMDCAdapter();
        }
    
        /* (non-Javadoc)
         * @see org.slf4j.helpers.LenientBinder#getStandardDelegate(java.lang.Object)
         */
        protected MDCAdapter getStandardDelegate(
            Object binderInstance
        ) throws Exception {
            return (MDCAdapter) binderInstance.getClass().getMethod(
                "getMDCA"
            ).invoke(
                binderInstance
            );
        }
    
        /* (non-Javadoc)
         * @see org.slf4j.spi.MDCAdapter#clear()
         */
        public void clear() {
            getDelegate().clear();
        }
    
        /* (non-Javadoc)
         * @see org.slf4j.spi.MDCAdapter#get(java.lang.String)
         */
        public String get(String key) {
            return getDelegate().get(key);
        }
    
        /* (non-Javadoc)
         * @see org.slf4j.spi.MDCAdapter#put(java.lang.String, java.lang.String)
         */
        public void put(String key, String val) {
            getDelegate().put(key, val);
        }
    
        /* (non-Javadoc)
         * @see org.slf4j.spi.MDCAdapter#remove(java.lang.String)
         */
        public void remove(String key) {
            getDelegate().remove(key);
        }
    
    }
    
}