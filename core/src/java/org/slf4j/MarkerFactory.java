/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: MarkerFactory.java,v 1.9 2008/11/18 01:30:52 hburger Exp $
 * Description: Lenient Marker Factory
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/18 01:30:52 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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

import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.LenientBinder;
import org.slf4j.spi.MarkerFactoryBinder;

/**
 * MarkerFactory is a utility class producing {@link Marker} instances as
 * appropriate for the logging system currently in use.
 * 
 * <p>
 * This class is essentially implemented as a wrapper around an
 * {@link IMarkerFactory} instance bound at compile time.
 * 
 * <p>
 * Please note that all methods in this class are static.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class MarkerFactory {

    /**
     * Constructor 
     */
    private MarkerFactory() {
        // Avoid instantiation
    }

    /**
     * Return a Marker instance as specified by the name parameter using the
     * previously bound {@link IMarkerFactory}instance.
     * 
     * @param name
     *          The name of the {@link Marker} object to return.
     * @return marker
     */
    public static Marker getMarker(String name) {
        return getIMarkerFactory().getMarker(name);
    }

    /**
     * Create a marker which is detached (even at birth) from the MarkerFactory.
     *
     * @return a dangling marker
     * @since 1.5.1
     */
    public static Marker getDetachedMarker(String name) {
      return getIMarkerFactory().getDetachedMarker(name);
    }
        
    /**
     * Return the {@link IMarkerFactory}instance in use.
     * 
     * <p>The IMarkerFactory instance is usually bound with this class at 
     * compile time.
     * 
     * @return the IMarkerFactory instance in use
     */
    public static final IMarkerFactory getIMarkerFactory() {
        return LenientFactory.SINGLETON;
    }

    
    //------------------------------------------------------------------------
    // Class LenientFactory
    //------------------------------------------------------------------------
    
    /**
     * Lenient Marker Factory
     * <p>
     * This implementation uses<ul>
     * <li>a StaticMarkerBinder if available in the current classloader
     * <li>a BasicMarkerFactory otherwise
     * </ul>
     */
    final static class LenientFactory
        extends LenientBinder<IMarkerFactory,MarkerFactoryBinder>
        implements IMarkerFactory
    {

        
        /**
         * Constructor 
         */
        private LenientFactory() {
            super("org.slf4j.impl.StaticMarkerBinder");
        }
        
        /**
         * 
         */
        final static IMarkerFactory SINGLETON = new LenientFactory().narrow();

        /* (non-Javadoc)
         * @see org.slf4j.helpers.LenientBinder#getFallbackDelegate()
         */
        protected IMarkerFactory getFallbackDelegate() {
            return new BasicMarkerFactory();
        }

        /* (non-Javadoc)
         * @see org.slf4j.helpers.LenientBinder#getStandardDelegate(java.lang.Object)
         */
        protected IMarkerFactory getStandardDelegate(
            MarkerFactoryBinder binderInstance
        ) {
            return binderInstance.getMarkerFactory();
        }
        
        /* (non-Javadoc)
         * @see org.slf4j.IMarkerFactory#detachMarker(java.lang.String)
         */
        public boolean detachMarker(String name) {
            return getDelegate().detachMarker(name);
        }

        /* (non-Javadoc)
         * @see org.slf4j.IMarkerFactory#exists(java.lang.String)
         */
        public boolean exists(String name) {
            return getDelegate().exists(name);
        }

        /* (non-Javadoc)
         * @see org.slf4j.IMarkerFactory#getMarker(java.lang.String)
         */
        public Marker getMarker(String name) {
            return getDelegate().getMarker(name);
        }

        /* (non-Javadoc)
         * @see org.slf4j.IMarkerFactory#getDetachedMarker(java.lang.String)
         */
        public Marker getDetachedMarker(String name) {
            return getDelegate().getDetachedMarker(name);
        }

    }
        
}