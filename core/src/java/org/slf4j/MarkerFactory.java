/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: MarkerFactory.java,v 1.6 2007/12/19 15:48:12 hburger Exp $
 * Description: Lenient Marker Factory
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/12/19 15:48:12 $
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
 * 
 * ------------------
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

import org.slf4j.helpers.Util;
import org.slf4j.lenient.DynamicMarkerBinder;
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

    static IMarkerFactory markerFactory;

    /**
     * The binder shall be loaded dynamically
     */
    private static final String STATIC_BINDER = "org.slf4j.impl.StaticMarkerBinder";

    private MarkerFactory() {
        // Avoid instantiation
    }

    static {
        MarkerFactoryBinder binder = getBinder();
        try {
            markerFactory = binder.getMarkerFactory();
        } catch (Exception exception) {
            //
            // we should never get here
            //
            Util.reportFailure(
                "Failed to instantiate instance of class [" + binder.getMarkerFactoryClassStr() + "]", 
                exception
            );
        }
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
        return markerFactory.getMarker(name);
    }

    /**
     * Return the {@link IMarkerFactory}instance in use.
     * 
     * <p>The IMarkerFactory instance is usually bound with this class at 
     * compile time.
     * 
     * @return the IMarkerFactory instance in use
     */
    public static IMarkerFactory getIMarkerFactory() {
        return markerFactory;
    }

    /**
     * Retrieve the static binder if already available to this class loader,
     * or the dynamic binder otherwise.
     * 
     * @return the appropriate binder
     */
    private static MarkerFactoryBinder getBinder(
    ){
        try {
            return (MarkerFactoryBinder) Class.forName(
                STATIC_BINDER,
                true,
                MarkerFactory.class.getClassLoader()
            ).getField(
                "SINGLETON"
            ).get(
                null // static
            );
        } catch (Exception exception) {
            try {
                return DynamicMarkerBinder.SINGLETON;
            } catch(NoClassDefFoundError error) {
                Util.reportFailure(
                    "Failed to load class \"org.slf4j.lenient.DynamicMarkerBinder\".",
                    error
                );
                throw error;
            }
        }
    }

}