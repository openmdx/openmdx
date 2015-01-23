/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Standard Java Beans Transformer 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2012-2013, OMEX AG, Switzerland
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
package org.openmdx.base.beans;

import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.exception.ExceptionListener;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.io.StringInputStream;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.text.conversion.spi.BeanTransformer;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.log.SysLog;
import org.w3c.cci2.ImmutableDate;
import org.w3c.cci2.ImmutableDateTime;
import org.w3c.format.DateTimeFormat;
import org.w3c.spi.DatatypeFactories;
import org.w3c.spi2.Datatypes;

/**
 * Standard Java Beans Transformer
 *
 * @since openMDX 2.12.0
 */
public class StandardBeanTransformer implements BeanTransformer {

    private static Object xstream = null;
    private static Method xstreamFromXML = null;
    
    {
        try {
            xstream = Classes.getApplicationClass("com.thoughtworks.xstream.XStream").newInstance();
            xstreamFromXML = xstream.getClass().getMethod("fromXML", String.class);
            SysLog.info("XStream found. Using as fallback for XML decoding");
        } catch(Exception e) {
        	// no-op
        }
    }
	
    /* (non-Javadoc)
     * @see org.openmdx.base.text.conversion.spi.JavaBeanTransformer#encode(java.lang.Object, org.openmdx.base.exception.ExceptionListener)
     */
    @Override
    public String encode(
        Object javaBean,
        ExceptionListener exceptionListener
    ) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(out);
        if(exceptionListener != null) {
            encoder.setExceptionListener(
                new ExceptionListenerAdapter(exceptionListener)
            );
        }
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
        encoder.setPersistenceDelegate(
            Quantifier.class, 
            quantifierPersistenceDelegate
        );
        encoder.setPersistenceDelegate(
            URI.class, 
            uriPersistenceDelegate
        );
        encoder.writeObject(javaBean);
        encoder.close();
        try {
            return out.toString("UTF-8");
        } catch (UnsupportedEncodingException exception) {
            throw new RuntimeServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "UTF-8 is expected to be supported"
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.text.conversion.spi.JavaBeanTransformer#decode(java.lang.CharSequence, org.openmdx.base.exception.ExceptionListener)
     */
    @Override
    public Object decode(
        CharSequence encodedJavaBean,
        ExceptionListener exceptionListener
    ) {
        if(encodedJavaBean == null) {
            return null;
        } else {
        	String stringifiedBean = encodedJavaBean.toString();
            if(xstreamFromXML != null && !stringifiedBean.startsWith("<?xml")) {
                try {
                    return xstreamFromXML.invoke(xstream, stringifiedBean);
                } catch(Exception e) {
                    throw new RuntimeServiceException(e);
                }
            } else {        	
                StringInputStream source = new StringInputStream(
                	stringifiedBean,
                    "UTF-8"
                );
                XMLDecoder decoder = new XMLDecoder(
                    source
                );
                if(exceptionListener != null) {
                    decoder.setExceptionListener(
                        new ExceptionListenerAdapter(exceptionListener)
                    );
                }
                Object value = decoder.readObject();
                try {
                    source.close();
                } catch (IOException ignored) {
                    SysLog.trace("Ignored close failure", ignored);
                }
                decoder.close();
                return value;
            }
        }
    }

    /**
     * Decode a graph of java beans
     * 
     * @param xmlEncodedJavaBean an XML document 
     * @return a graph of java beans; or <code>null</code> if
     * the <code>xmlEncodedJavaBean</code> was <code>null</code>
     * 
     * @deprecated use fromXML(java.lang.CharSequence,org.openmdx.base.exception.ExceptionListener)
     * 
     * @see org.openmdx.base.text.conversion.JavaBeans#fromXML(java.lang.CharSequence,org.openmdx.base.exception.ExceptionListener)
     */
    @Deprecated
    public static Object fromXML(
        final CharSequence xmlEncodedJavaBean,
        final java.beans.ExceptionListener exceptionListener
    ){
        return new StandardBeanTransformer().encode(
            xmlEncodedJavaBean, 
            new ExceptionListener() {
                
                @Override
                public void exceptionThrown(Exception exception) {
                    exceptionListener.exceptionThrown(exception);
                }

            }
        );
    }
       
    protected abstract static class DefaultPersistenceDelegate extends PersistenceDelegate {

        protected DefaultPersistenceDelegate(){
            super();
        }
        
        @Override
        protected boolean mutatesTo(
            Object oldInstance, 
            Object newInstance
        ) {
            return newInstance != null && oldInstance.equals(newInstance);
        }

        /* (non-Javadoc)
         * @see java.beans.PersistenceDelegate#initialize(java.lang.Class, java.lang.Object, java.lang.Object, java.beans.Encoder)
         */
        @Override
        protected void initialize(
            Class<?> type,
            Object oldInstance,
            Object newInstance,
            Encoder out
        ) {
            // Object is completely initialized by the constructor
        }
        
    }

    private static class ExceptionListenerAdapter implements java.beans.ExceptionListener {
        
        /**
         * Constructor 
         *
         * @param delegate
         */
        ExceptionListenerAdapter(ExceptionListener delegate) {
            this.delegate = delegate;
        }

        private final ExceptionListener delegate;

        /* (non-Javadoc)
         * @see java.beans.ExceptionListener#exceptionThrown(java.lang.Exception)
         */
        @Override
        public void exceptionThrown(Exception exception) {
            this.delegate.exceptionThrown(exception);
        }
        
    }
    
    private static class BigDecimalPersistenceDelegate extends PersistenceDelegate {

        public BigDecimalPersistenceDelegate() {
            super();
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
    
    private static class PathPersistenceDelegate extends PersistenceDelegate {

        public PathPersistenceDelegate() {
            super();
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
            super();
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
                new Object[]{Long.valueOf(((Date)oldInstance).getTime())}
            );
        }
    }    

    private static class QuantifierPersistenceDelegate extends DefaultPersistenceDelegate {

        public QuantifierPersistenceDelegate(
        ) {            
        }
        
        @Override
        protected boolean mutatesTo(
            Object oldInstance, 
            Object newInstance
        ) {
            return oldInstance == newInstance;
        }

        @Override
        protected Expression instantiate(
            Object oldInstance, 
            Encoder out
        ) {
            return new Expression(
                oldInstance,
                Quantifier.class,
                "valueOf", 
                new Object[]{((Quantifier)oldInstance).name()}
            );
        }

    }    
    
    private static class DateTimePersistenceDelegate extends DefaultPersistenceDelegate {

        public DateTimePersistenceDelegate(
        ) {            
            super();
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
    
    private static class URIPersistenceDelegate extends DefaultPersistenceDelegate {

        public URIPersistenceDelegate(
        ) {            
            super();
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
                    URI.class, 
                    oldInstance.toString()
                }
            );
        }
        
    }    
    
    private static final PersistenceDelegate bigDecimalPersistenceDelegate = new BigDecimalPersistenceDelegate();
    private static final PersistenceDelegate pathPersistenceDelegate = new PathPersistenceDelegate();
    private static final PersistenceDelegate datePersistenceDelegate = new DatePersistenceDelegate();
    private static final PersistenceDelegate dateTimePersistenceDelegate = new DateTimePersistenceDelegate();
    private static final PersistenceDelegate durationPersistenceDelegate = new DurationPersistenceDelegate();
    private static final PersistenceDelegate immutableDatePersistenceDelegate = new ImmutableDatePersistenceDelegate();
    private static final PersistenceDelegate quantifierPersistenceDelegate = new QuantifierPersistenceDelegate();
    private static final PersistenceDelegate uriPersistenceDelegate = new URIPersistenceDelegate();
    private static final Class<? extends XMLGregorianCalendar> xmlGregorianCalendarClass = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendarDate(2000, 1, 1, DatatypeConstants.FIELD_UNDEFINED).getClass();
    
}
