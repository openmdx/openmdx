/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LoggerFactory.java,v 1.9 2008/03/13 17:16:15 hburger Exp $
 * Description: Dynamic Logger Binder
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/13 17:16:15 $
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

import org.slf4j.helpers.FallbackLoggerFactory;
import org.slf4j.helpers.LenientBinder;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * The <code>LoggerFactory</code> is a utility class producing Loggers for
 * various logging APIs, most notably for log4j, logback and JDK 1.4 logging. 
 * Other implementations such as {@link org.slf4j.impl.NOPLogger NOPLogger} and
 * {@link org.slf4j.impl.SimpleLogger SimpleLogger} are also supported.
 * 
 * <p>
 * <code>LoggerFactory</code> is essentially a wrapper around an
 * {@link ILoggerFactory} instance bound with <code>LoggerFactory</code> at
 * compile time.
 * 
 * <p>
 * Please note that all methods in <code>LoggerFactory</code> are static.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public final class LoggerFactory {

    /**
     * Constructor 
     */
    private LoggerFactory() {
        // private constructor prevents instantiation
    }

    /**
     * The logger factory singleton
     */
    private static final ILoggerFactory loggerFactory = new LenientFactory().narrow();

    /**
     * Return a logger named according to the name parameter using the statically
     * bound {@link ILoggerFactory} instance.
     * 
     * @param name
     *          The name of the logger.
     * @return logger
     */
    public static Logger getLogger(String name) {
        return LoggerFactory.loggerFactory.getLogger(name);
    }

    /**
     * Return a logger named corresponding to the class passed as parameter, using
     * the statically bound {@link ILoggerFactory} instance.
     * 
     * @param clazz
     *          the returned logger will be named after clazz
     * @return logger
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.loggerFactory.getLogger(clazz.getName());
    }

    /**
     * Return the {@link ILoggerFactory} instance in use.
     * 
     * <p>ILoggerFactory instance is bound with this class at compile
     * time.
     * 
     * @return the ILoggerFactory instance in use
     */
    public static ILoggerFactory getILoggerFactory() {
        return LoggerFactory.loggerFactory;
    }


    //------------------------------------------------------------------------
    // Class LenientFactory
    //------------------------------------------------------------------------
    
    /**
     * Lenient Logger Factory
     * <p>
     * This implementation uses<ul>
     * <li>a StaticLoggerBinder if available in the current classloader
     * <li>a JDK 1.4 Logger Factory otherwise
     * </ul>
     */
    static class LenientFactory
        extends LenientBinder<ILoggerFactory,LoggerFactoryBinder>
        implements ILoggerFactory
    {
        
        /**
         * Constructor 
         */
        LenientFactory() {
            super("org.slf4j.impl.StaticLoggerBinder");
        }

        /* (non-Javadoc)
         * @see org.slf4j.helpers.LenientBinder#getFallbackDelegate()
         */
        protected ILoggerFactory getFallbackDelegate() {
            return new FallbackLoggerFactory();
        }

        /* (non-Javadoc)
         * @see org.slf4j.helpers.LenientBinder#getStandardDelegate(java.lang.Object)
         */
        protected ILoggerFactory getStandardDelegate(
            LoggerFactoryBinder binderInstance
        ) {
            return binderInstance.getLoggerFactory();
        }

        /* (non-Javadoc)
         * @see org.slf4j.ILoggerFactory#getLogger(java.lang.String)
         */
        public Logger getLogger(String name) {
            return getDelegate().getLogger(name);
        }

    }
    
}
