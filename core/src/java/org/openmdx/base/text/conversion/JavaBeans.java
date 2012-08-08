/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: JavaBeans.java,v 1.14 2010/01/21 17:36:09 hburger Exp $
 * Description: Java Beans 
 * Revision:    $Revision: 1.14 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/01/21 17:36:09 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.ExceptionListener;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Date;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.StringInputStream;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.ImmutableDate;
import org.w3c.cci2.ImmutableDateTime;
import org.w3c.format.DateTimeFormat;
import org.w3c.spi.DatatypeFactories;
import org.w3c.spi2.Datatypes;

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
    
    private static class JavaBeanExceptionListener implements ExceptionListener {

        public JavaBeanExceptionListener(
            Object target
        ) {
            this.target = target;
        }

        public void exceptionThrown(
            Exception e
        ) {
            if(
                !(e instanceof ClassNotFoundException) || 
                !"org.openmdx.uses.java.beans.XMLDecoder".equals(e.getMessage())
            ) {            
                new ServiceException(
                    e,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.PARSE_FAILURE,
                    "Unable to encode/decode JavaBean. Continuing...",
                    new BasicException.Parameter("target", this.target)
                ).log();
            }
        }
        
        private Object target;
    }

    private static class BigDecimalPersistenceDelegate extends DefaultPersistenceDelegate {

        /**
         * Constructor 
         */
        public BigDecimalPersistenceDelegate() {
            super();
        }

        @Override
        protected boolean mutatesTo(
            Object oldInstance, 
            Object newInstance
        ) {
            return oldInstance.equals(newInstance);
        }

        @Override
        protected Expression instantiate(
            Object oldInstance, 
            Encoder out
        ) {
            return new Expression(
                oldInstance,
                oldInstance.getClass(),
                "new", 
                new Object[]{oldInstance.toString()}
            );
        }
                
    }
    
    private static class PathPersistenceDelegate extends DefaultPersistenceDelegate {

        /**
         * Constructor 
         */
        public PathPersistenceDelegate() {
            super();
        }

        @Override
        protected boolean mutatesTo(
            Object oldInstance, 
            Object newInstance
        ) {
            return oldInstance.equals(newInstance);
        }

        @Override
        protected Expression instantiate(
            Object oldInstance, 
            Encoder out
        ) {
            return new Expression(
                oldInstance,
                oldInstance.getClass(),
                "new", 
                new Object[]{((Path)oldInstance).toXRI()}
            );
        }
    }

    private static class ImmutableDatePersistenceDelegate extends DefaultPersistenceDelegate {

        /**
         * Constructor 
         */
        public ImmutableDatePersistenceDelegate() {
            super();
        }

        @Override
        protected boolean mutatesTo(
            Object oldInstance, 
            Object newInstance
        ) {
            return newInstance != null && ( 
                oldInstance.getClass() == newInstance.getClass() ? oldInstance.equals(newInstance) : oldInstance.toString().equals(newInstance.toString())
            );
        }

        @Override
        protected Expression instantiate(
            Object oldInstance, 
            Encoder out
        ) {
            return new Expression(
                oldInstance,
                Datatypes.class,
                "create", 
                new Object[]{XMLGregorianCalendar.class,oldInstance.toString()}
            );
        }
    }    

    private static class DurationPersistenceDelegate extends DefaultPersistenceDelegate {

        /**
         * Constructor 
         */
        public DurationPersistenceDelegate() {
            super();
        }

        @Override
        protected boolean mutatesTo(
            Object oldInstance, 
            Object newInstance
        ) {
            return newInstance != null && oldInstance.equals(newInstance);
        }

        @Override
        protected Expression instantiate(
            Object oldInstance, 
            Encoder out
        ) {
            return new Expression(
                oldInstance,
                Datatypes.class,
                "create", 
                new Object[]{Duration.class,((Duration)oldInstance).toString()}
            );
        }
    }    

    private static class DatePersistenceDelegate extends DefaultPersistenceDelegate {

        public DatePersistenceDelegate(
        ) {            
        }
        
        @Override
        protected boolean mutatesTo(
            Object oldInstance, 
            Object newInstance
        ) {
            return oldInstance.equals(newInstance);
        }

        @Override
        protected Expression instantiate(
            Object oldInstance, 
            Encoder out
        ) {
            return new Expression(
                oldInstance,
                Date.class,
                "new", 
                new Object[]{((Date)oldInstance).getTime()}
            );
        }
    }    
    
    private static class DateTimePersistenceDelegate extends DefaultPersistenceDelegate {

        public DateTimePersistenceDelegate(
        ) {            
        }
        
        @Override
        protected boolean mutatesTo(
            Object oldInstance, 
            Object newInstance
        ) {
            return newInstance != null && oldInstance.equals(newInstance);
        }

        @Override
        protected Expression instantiate(
            Object oldInstance, 
            Encoder out
        ) {
            return new Expression(
                oldInstance,
                Datatypes.class,
                "create", 
                new Object[]{
                    Date.class, 
                    DateTimeFormat.BASIC_UTC_FORMAT.format((Date)oldInstance)
                }
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
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(out);
        encoder.setExceptionListener(
            new JavaBeanExceptionListener(javaBean)
        );
        encoder.setPersistenceDelegate(
            BigDecimal.class, 
            bigDecimalPersistenceDelegate
        );
        encoder.setPersistenceDelegate(
            Path.class, 
            pathPersistenceDelegate
        );
        encoder.setPersistenceDelegate(
            Date.class, 
            datePersistenceDelegate
        );
        encoder.setPersistenceDelegate(
            xmlGregorianCalendarClass, 
            immutableDatePersistenceDelegate
        );
        encoder.setPersistenceDelegate(
            ImmutableDate.class, 
            immutableDatePersistenceDelegate
        );
        encoder.setPersistenceDelegate(
            ImmutableDateTime.class, 
            dateTimePersistenceDelegate
        );
        encoder.setPersistenceDelegate(
            Duration.class, 
            durationPersistenceDelegate
        );
        try {
            encoder.writeObject(javaBean);
        }
        catch(Exception e) {
            throw new ServiceException(e);
        }
        encoder.close();
        try {
            return out.toString("UTF-8");
        } 
        catch (UnsupportedEncodingException exception) {
            throw new RuntimeServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "UTF-8 is expected to be supported"
            );
        }
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
        if(xmlEncodedJavaBean == null) return null;
        XMLDecoder decoder = new XMLDecoder(
        	new StringInputStream(
        		xmlEncodedJavaBean.toString(),
        		"UTF-8"
        	)
        );
        decoder.setExceptionListener(
            new JavaBeanExceptionListener(xmlEncodedJavaBean)
        );
        return decoder.readObject();
    }

    private static PersistenceDelegate bigDecimalPersistenceDelegate = new BigDecimalPersistenceDelegate();
    private static PersistenceDelegate pathPersistenceDelegate = new PathPersistenceDelegate();
    private static PersistenceDelegate datePersistenceDelegate = new DatePersistenceDelegate();
    private static PersistenceDelegate dateTimePersistenceDelegate = new DateTimePersistenceDelegate();
    private static PersistenceDelegate durationPersistenceDelegate = new DurationPersistenceDelegate();
    private static PersistenceDelegate immutableDatePersistenceDelegate = new ImmutableDatePersistenceDelegate();
    private static Class<? extends XMLGregorianCalendar> xmlGregorianCalendarClass = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendarDate(2000, 1, 1, DatatypeConstants.FIELD_UNDEFINED).getClass();
    
}
