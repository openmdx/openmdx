/*
 * ====================================================================
 * Project:     openMDX/Dalvik, http://www.openmdx.org/
 * Description: Alternate Java Bean Transformer 
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
package org.openmdx.dalvik.beans;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;

import javax.xml.datatype.DatatypeConstants;
import #if CLASSIC_CHRONO_TYPES javax.xml.datatype #else java.time #endif.Duration;

import org.openmdx.base.exception.ExceptionListener;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.io.StringInputStream;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.text.conversion.spi.BeanTransformer;
import org.openmdx.dalvik.uses.java.beans.Encoder;
import org.openmdx.dalvik.uses.java.beans.Expression;
import org.openmdx.dalvik.uses.java.beans.PersistenceDelegate;
import org.openmdx.dalvik.uses.java.beans.XMLDecoder;
import org.openmdx.dalvik.uses.java.beans.XMLEncoder;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.ImmutableDate;
import org.w3c.cci2.ImmutableDateTime;
import org.w3c.format.DateTimeFormat;
import org.w3c.spi.DatatypeFactories;
import org.w3c.spi2.Datatypes;

/**
 * Alternate Java Bean Transfomer
 * 
 * @since openMDX 2.12
 */
public class AlternateJavaBeanTransformer implements BeanTransformer {

    /* (non-Javadoc)
     * @see org.openmdx.base.text.conversion.spi.JavaBeanTransformer#encode(java.lang.Object, org.openmdx.base.exception.ExceptionListener)
     */
    @Override
    public String encode(
        Object javaBean,
        ExceptionListener exceptionListener
    ) {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (final XMLEncoder encoder = new XMLEncoder(out)) {
                if(exceptionListener != null) {
                    encoder.setExceptionListener(exceptionListener);
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
                    #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif.class,
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
            }
            return out.toString("UTF-8");
        } catch (IOException exception) {
            throw new RuntimeServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "Unable to read to streams content  as UTF-8 end to close it"
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
            XMLDecoder decoder = new XMLDecoder(
                new StringInputStream(
                    encodedJavaBean.toString(),
                    "UTF-8"
                )
            );
            if(exceptionListener != null) {
                decoder.setExceptionListener(exceptionListener);
            }
            return decoder.readObject();
        }
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
                new Object[]{#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif.class,oldInstance.toString()}
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
                #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif.class,
                "new", 
                new Object[]{((#if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif)oldInstance).getTime()}
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
                    #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif.class,
                    DateTimeFormat.BASIC_UTC_FORMAT.format((#if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif)oldInstance)
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
    private static final Class<? extends #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif> xmlGregorianCalendarClass = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendarDate(2000, 1, 1, DatatypeConstants.FIELD_UNDEFINED).getClass();
    
}
