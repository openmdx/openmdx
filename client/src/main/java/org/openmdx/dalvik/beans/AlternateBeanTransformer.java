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

import org.openmdx.dalvik.uses.java.beans.Encoder;
import org.openmdx.dalvik.uses.java.beans.Expression;
import org.openmdx.dalvik.uses.java.beans.PersistenceDelegate;
import org.openmdx.dalvik.uses.java.beans.XMLDecoder;
import org.openmdx.dalvik.uses.java.beans.XMLEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;
import java.util.Optional;
import java.math.BigInteger;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.DatatypeConstants;

import org.openmdx.base.exception.ExceptionListener;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.io.StringInputStream;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.text.conversion.spi.BeanTransformer;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.w3c.format.DateTimeFormat;
import org.w3c.spi.DatatypeFactories;
import org.w3c.spi2.Datatypes;

/**
 * Alternate Java Bean Transfomer
 *
 * @since openMDX 2.12
 */
public class AlternateBeanTransformer implements BeanTransformer {

    static final Class<? extends XMLGregorianCalendar> xmlGregorianCalendarClass = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendarDate(2000, 1, 1, DatatypeConstants.FIELD_UNDEFINED).getClass();
    static final Class<? extends javax.xml.datatype.Duration> classicDayTimeDurationClass = DatatypeFactories.xmlDatatypeFactory().newDurationDayTime(1000L).getClass();
    static final Class<? extends javax.xml.datatype.Duration> classicYearMonthDurationClass = DatatypeFactories.xmlDatatypeFactory().newDurationYearMonth(true, BigInteger.ONE, BigInteger.ZERO).getClass();
    static final Class<? extends javax.xml.datatype.Duration> classicDurationClass = DatatypeFactories.xmlDatatypeFactory().newDuration(true, 1, 1, 1, 0, 0, 0).getClass();

    /* (non-Javadoc)
     * @see org.openmdx.base.text.conversion.spi.JavaBeanTransformer#encode(java.lang.Object, org.openmdx.base.exception.ExceptionListener)
     */
    @Override
    public String encode(
            Object javaBean,
            ExceptionListener exceptionListener
    ) {
        try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try(XMLEncoder encoder = new XMLEncoder(out)){
                if(exceptionListener != null) {
                    encoder.setExceptionListener(
                            new ExceptionListenerAdapter(exceptionListener)
                    );
                }
                encoder.setPersistenceDelegate(
                        BigDecimal.class,
                        BigDecimalPersistenceDelegate.INSTANCE
                );
                encoder.setPersistenceDelegate(
                        Path.class,
                        PathPersistenceDelegate.INSTANCE
                );
                encoder.setPersistenceDelegate(
                        Date.class,
                        MutableDateTimePersistenceDelegate.INSTANCE
                );
                encoder.setPersistenceDelegate(
                        Instant.class,
                        ContemporaryDateTimePersistenceDelegate.INSTANCE
                );
                encoder.setPersistenceDelegate(
                        xmlGregorianCalendarClass,
                        ClassicDatePersistenceDelegate.INSTANCE
                );
                encoder.setPersistenceDelegate(
                        LocalDate.class,
                        ContemporaryDatePersistenceDelegate.INSTANCE
                );
                encoder.setPersistenceDelegate(
                        java.time.Duration.class,
                        ContemporaryDayTimeDurationPersistenceDelegate.INSTANCE
                );
                encoder.setPersistenceDelegate(
                        java.time.Period.class,
                        ContemporaryYearMonthDurationPersistenceDelegate.INSTANCE
                );
                encoder.setPersistenceDelegate(
                        classicDayTimeDurationClass,
                        ClassicDurationPersistenceDelegate.INSTANCE
                );
                encoder.setPersistenceDelegate(
                        classicYearMonthDurationClass,
                        ClassicDurationPersistenceDelegate.INSTANCE
                );
                encoder.setPersistenceDelegate(
                        classicDurationClass,
                        ClassicDurationPersistenceDelegate.INSTANCE
                );
                encoder.setPersistenceDelegate(
                        Quantifier.class,
                        QuantifierPersistenceDelegate.INSTANCE
                );
                encoder.setPersistenceDelegate(
                        URI.class,
                        URIPersistenceDelegate.INSTANCE
                );
                #if CLASSIC_CHRONO_TYPES
                        encoder.setPersistenceDelegate(
                        org.w3c.cci2.ImmutableDate.class,
                        ClassicDatePersistenceDelegate.INSTANCE
                );
                encoder.setPersistenceDelegate(
                        org.w3c.cci2.ImmutableDateTime.class,
                        new ImmutableDateTimePersistenceDelegate()
                );
                #endif
                        encoder.writeObject(javaBean);
            }
            return out.toString("UTF-8");
        } catch (IOException exception) {
            throw new RuntimeServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "Unable to convert the output stream to an UTF-8 string and to close it"
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
        }
        final String stringifiedBean = encodedJavaBean.toString();
        try (
                java.io.StringReader reader = new java.io.StringReader(stringifiedBean);
                java.beans.XMLDecoder decoder = new java.beans.XMLDecoder(new org.xml.sax.InputSource(reader))
        ){
            if(exceptionListener != null) {
                decoder.setExceptionListener(
                        new ExceptionListenerAdapter(exceptionListener)
                );
            }
            return decoder.readObject();
        }
    }

    private abstract static class DefaultPersistenceDelegate extends PersistenceDelegate {

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

        static final PersistenceDelegate INSTANCE = new BigDecimalPersistenceDelegate();

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

        static final PersistenceDelegate INSTANCE = new PathPersistenceDelegate();

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

    private static class ClassicDurationPersistenceDelegate extends DefaultPersistenceDelegate {

        static final PersistenceDelegate INSTANCE = new ClassicDurationPersistenceDelegate();

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
                    new Object[]{javax.xml.datatype.Duration.class,oldInstance.toString()}
            );
        }

    }

    private static class ContemporaryDayTimeDurationPersistenceDelegate extends DefaultPersistenceDelegate {

        static final PersistenceDelegate INSTANCE = new ContemporaryDayTimeDurationPersistenceDelegate();

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
                    new Object[]{java.time.Duration.class,oldInstance.toString()}
            );
        }

    }

    private static class ContemporaryYearMonthDurationPersistenceDelegate extends DefaultPersistenceDelegate {

        static final PersistenceDelegate INSTANCE = new ContemporaryYearMonthDurationPersistenceDelegate();

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
                    new Object[]{java.time.Period.class,oldInstance.toString()}
            );
        }

    }

    private static class ContemporaryDatePersistenceDelegate extends DefaultPersistenceDelegate {

        static final PersistenceDelegate INSTANCE = new ContemporaryDatePersistenceDelegate();

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
                    new Object[]{LocalDate.class,oldInstance.toString()}
            );
        }

    }

    private static class ContemporaryDateTimePersistenceDelegate extends DefaultPersistenceDelegate {

        static final PersistenceDelegate INSTANCE = new ContemporaryDateTimePersistenceDelegate();

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
                    new Object[]{Instant.class,oldInstance.toString()}
            );
        }

    }

    private static class MutableDateTimePersistenceDelegate extends DefaultPersistenceDelegate {

        static final PersistenceDelegate INSTANCE = new MutableDateTimePersistenceDelegate();

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

        static final PersistenceDelegate INSTANCE = new QuantifierPersistenceDelegate();

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

    private static class URIPersistenceDelegate extends DefaultPersistenceDelegate {

        static final PersistenceDelegate INSTANCE = new URIPersistenceDelegate();

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

    private static class ClassicDatePersistenceDelegate extends DefaultPersistenceDelegate {

        static final PersistenceDelegate INSTANCE = new ClassicDatePersistenceDelegate();

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

    #if CLASSIC_CHRONO_TYPES

    private static class ImmutableDateTimePersistenceDelegate extends DefaultPersistenceDelegate {

        static final PersistenceDelegate INSTANCE = new ImmutableDateTimePersistenceDelegate();

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

    #endif

}