/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Java Beans 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2012, OMEX AG, Switzerland
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
package org.openmdx.base.text.conversion;

import org.openmdx.base.exception.ExceptionListener;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.conversion.spi.BeanTransformer;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;

/**
 * Java Beans
 */
public class JavaBeans {
    
    /**
     * Constructor 
     */
    private JavaBeans(){
        // Avoid instantiation
    }

    /**
     * The eagerly acquired Java bean transformer
     */
    private static final BeanTransformer transformer = Classes.newPlatformInstance(
        "org.openmdx.base.beans.StandardBeanTransformer",
        BeanTransformer.class
    );
    
    
    /**
     * Encode a graph of java beans
     * 
     * @param javaBean a graph of java beans
     * @param exceptionListener the (optional) exception listener
     * 
     * @return an XML document representing the graph of java beans
     */
    public static String toXML(
        Object javaBean,
        ExceptionListener exceptionListener
    ) throws ServiceException {
        try {
            return transformer.encode(
                javaBean,
                exceptionListener
            );
        } catch (RuntimeException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.TRANSFORMATION_FAILURE,
                "Unable to convert Java Bean to XML"
            );
        }
    }

    /**
     * Encode a graph of java beans
     * 
     * @param javaBean a graph of java beans
     * 
     * @return an XML document representing the graph of java beans
     */
    public static String toXML(
        Object javaBean
    ) throws ServiceException {
        return transformer.encode(
            javaBean,
            new JavaBeanExceptionListener(javaBean)
        );
    }
    
    /**
     * Decode a graph of java beans
     * 
     * @param xmlEncodedJavaBean an XML document 
     * @param exceptionListener the (optional) exception listener
     * 
     * @return a graph of java beans; or <code>null</code> if
     * the <code>xmlEncodedJavaBean</code> was <code>null</code>
     */
    public static Object fromXML(
        CharSequence xmlEncodedJavaBean,
        ExceptionListener exceptionListener
    ){
        return transformer.decode(xmlEncodedJavaBean, exceptionListener);
    }

    /**
     * Decode a graph of java beans
     * 
     * @param xmlEncodedJavaBean an XML document 
     * 
     * @return a graph of java beans; or <code>null</code> if
     * the <code>xmlEncodedJavaBean</code> was <code>null</code>
     */
    public static Object fromXML(
        CharSequence xmlEncodedJavaBean
    ){
        return fromXML(
            xmlEncodedJavaBean,            
            new JavaBeanExceptionListener(xmlEncodedJavaBean)
        );
    }

    //------------------------------------------------------------------------
    // Class JavaBeanExceptionListener
    //------------------------------------------------------------------------
    
    /**
     * Java Bean Exception Listener
     */
    private static class JavaBeanExceptionListener implements ExceptionListener {

        /**
         * Constructor 
         *
         * @param source
         */
        public JavaBeanExceptionListener(
            Object source
        ) {
            this.source = source;
        }

        /**
         * The source to be encoded or decoded
         */
        private Object source;
        
        /* (non-Javadoc)
         * @see org.openmdx.base.exception.ExceptionListener#exceptionThrown(java.lang.Exception)
         */
        @Override
        public void exceptionThrown(
           Exception exception
        ) {
            new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.PARSE_FAILURE,
                "Unable to encode/decode JavaBean. Continuing...",
                new BasicException.Parameter("target", this.source)
            ).log();
        }
        
    }

}
