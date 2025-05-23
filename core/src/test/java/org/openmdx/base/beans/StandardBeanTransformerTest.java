//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.openmdx.base.beans;

import java.time.LocalDate;
import java.util.Date;
import java.time.Instant;
import java.time.Period;
import org.openmdx.base.exception.ExceptionListener;
import org.openmdx.base.text.conversion.spi.BeanTransformer;
import org.openmdx.state2.spi.DateStateViewContext;
import org.w3c.spi.DatatypeFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.openmdx.kernel.log.SysLog;

public class StandardBeanTransformerTest {

    private static final String CONTEMPORARY_PROLOG =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<java version=\"21.0.2\" class=\"java.beans.XMLDecoder\">\n";
    private static final String CONTEMPORARY_OBJECT =
        " <object class=\"org.openmdx.base.beans.ContemporaryBean\">\n" +
        "  <void property=\"dayTimeDuration\">\n" +
        "   <object class=\"org.w3c.spi2.Datatypes\" method=\"create\">\n" +
        "    <class>java.time.Duration</class>\n" +
        "    <string>PT26H3M4S</string>\n" +
        "   </object>\n" +
        "  </void>\n" +
        "  <void property=\"immutableDate\">\n" +
        "   <object class=\"org.w3c.spi2.Datatypes\" method=\"create\">\n" +
        "    <class>java.time.LocalDate</class>\n" +
        "    <string>2000-04-30</string>\n" +
        "   </object>\n" +
        "  </void>\n" +
        "  <void property=\"immutableDateTime\">\n" +
        "   <object class=\"org.w3c.spi2.Datatypes\" method=\"create\">\n" +
        "    <class>java.time.Instant</class>\n" +
        "    <string>2007-12-03T10:15:30Z</string>\n" +
        "   </object>\n" +
        "  </void>\n" +
        "  <void property=\"yearMonthDayDuration\">\n" +
        "   <object class=\"org.w3c.spi2.Datatypes\" method=\"create\">\n" +
        "    <class>java.time.Period</class>\n" +
        "    <string>P1Y2M3D</string>\n" +
        "   </object>\n" +
        "  </void>\n" +
        " </object>\n";
    private static final String CONTEMPORARY_EPILOG =
        "</java>";

    private static final String CLASSIC_PROLOG =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<java version=\"21.0.2\" class=\"java.beans.XMLDecoder\">\n";
    private static final String CLASSIC_OBJECT =
        " <object class=\"org.openmdx.base.beans.ClassicBean\">\n" +
        "  <void property=\"dayTimeDuration\">\n" +
        "   <object class=\"org.w3c.spi2.Datatypes\" method=\"create\">\n" +
        "    <class>javax.xml.datatype.Duration</class>\n" +
        "    <string>P1DT2H3M4S</string>\n" +
        "   </object>\n" +
        "  </void>\n" +
        "  <void property=\"immutableDate\">\n" +
        "   <object class=\"org.w3c.spi2.Datatypes\" method=\"create\">\n" +
        "    <class>javax.xml.datatype.XMLGregorianCalendar</class>\n" +
        "    <string>2000-04-30</string>\n" +
        "   </object>\n" +
        "  </void>\n" +
        "  <void property=\"immutableDateTime\">\n" +
        "   <object class=\"org.w3c.spi2.Datatypes\" method=\"create\">\n" +
        "    <class>java.util.Date</class>\n" +
        "    <string>20071203T101530.000Z</string>\n" +
        "   </object>\n" +
        "  </void>\n" +
        "  <void property=\"mutableDate\">\n" +
        "   <object class=\"org.w3c.spi2.Datatypes\" method=\"create\">\n" +
        "    <class>javax.xml.datatype.XMLGregorianCalendar</class>\n" +
        "    <string>2000-04-30</string>\n" +
        "   </object>\n" +
        "  </void>\n" +
        "  <void property=\"mutableDateTime\">\n" +
        "   <object class=\"java.util.Date\">\n" +
        "    <long>1196676930000</long>\n" +
        "   </object>\n" +
        "  </void>\n" +
        "  <void property=\"yearMonthDayDuration\">\n" +
        "   <object class=\"org.w3c.spi2.Datatypes\" method=\"create\">\n" +
        "    <class>javax.xml.datatype.Duration</class>\n" +
        "    <string>P1Y2M3D</string>\n" +
        "   </object>\n" +
        "  </void>\n" +
        "  <void property=\"yearMonthDuration\">\n" +
        "   <object class=\"org.w3c.spi2.Datatypes\" method=\"create\">\n" +
        "    <class>javax.xml.datatype.Duration</class>\n" +
        "    <string>P1Y2M</string>\n" +
        "   </object>\n" +
        "  </void>\n" +
        " </object>\n";
    private static final String CLASSIC_EPILOG =
        "</java>";

    #if CLASSIC_CHRONO_TYPES
    @Test
    void encodeClassicBean() {
        //
        // Arrange
        //
        BeanTransformer beanTransformer = new StandardBeanTransformer();
        ClassicBean bean = new ClassicBean();
        javax.xml.datatype.XMLGregorianCalendar mutableDate = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-04-30");
        java.util.Date mutableDateTime = java.util.Date.from(java.time.Instant.parse("2007-12-03T10:15:30.00Z"));
        //
        // Act
        //
        bean.setMutableDateTime(mutableDateTime);
        bean.setImmutableDateTime(DatatypeFactories.immutableDatatypeFactory().toImmutableDateTime(mutableDateTime));
        bean.setMutableDate(mutableDate);
        bean.setImmutableDate(DatatypeFactories.immutableDatatypeFactory().toImmutableDate(mutableDate));
        bean.setYearMonthDuration(DatatypeFactories.immutableDatatypeFactory().newDuration("P1Y2M"));
        bean.setYearMonthDayDuration(DatatypeFactories.immutableDatatypeFactory().newDuration("P1Y2M3D"));
        bean.setDayTimeDuration(DatatypeFactories.immutableDatatypeFactory().newDuration("P1DT2H3M4S"));
        String encoded = beanTransformer.encode(bean, Exception::printStackTrace);
        //
        // Assert
        //
        SysLog.detail("Encoded Classic Bean", encoded);
        Assertions.assertTrue(bean.getMutableDateTime() instanceof java.util.Date, "Original Mutable Date Time");
        Assertions.assertTrue(bean.getImmutableDateTime() instanceof org.w3c.cci2.ImmutableDateTime, "Original Immutable Date Time");
        Assertions.assertEquals(StandardBeanTransformer.classicDateClass, bean.getMutableDate().getClass(), "Original Mutable Date");
        Assertions.assertFalse(bean.getMutableDate() instanceof org.w3c.cci2.ImmutableDate, "Original Mutable Date");
        Assertions.assertTrue(bean.getImmutableDate() instanceof org.w3c.cci2.ImmutableDate, "Original Immutable Date");
        Assertions.assertEquals(StandardBeanTransformer.classicYearMonthDurationClass, bean.getYearMonthDuration().getClass(), "Original Year Month Duration");
        Assertions.assertEquals(StandardBeanTransformer.classicDurationClass, bean.getYearMonthDayDuration().getClass(), "Original Year Month Day Duration");
        Assertions.assertEquals(StandardBeanTransformer.classicDayTimeDurationClass, bean.getDayTimeDuration().getClass(), "Original Day Time Duration");
        Assertions.assertTrue(encoded.contains(CLASSIC_OBJECT), "Encoded Classic Bean");
    }
    #else
    @Test
    void encodeContemporaryBean() {
        //
        // Arrange
        //
        BeanTransformer beanTransformer = new StandardBeanTransformer();
        ContemporaryBean bean = new ContemporaryBean();
        LocalDate immutableDate = LocalDate.of(2000, 4, 30);
        Instant immutableDateTime = Instant.parse("2007-12-03T10:15:30.00Z");
        java.time.Duration dayTimeDuration = java.time.Duration.parse("P1DT2H3M4S");
        java.time.Period yearMonthDayDuration = java.time.Period.parse("P1Y2M3D");
        //
        // Act
        //
        bean.setImmutableDateTime(immutableDateTime);
        bean.setImmutableDate(immutableDate);
        bean.setYearMonthDayDuration(yearMonthDayDuration);
        bean.setDayTimeDuration(dayTimeDuration);
        String encoded = beanTransformer.encode(bean, Exception::printStackTrace);
        //
        // Assert
        //
        SysLog.detail("Encoded Contemporary Bean", encoded);
        Assertions.assertTrue(encoded.contains(CONTEMPORARY_OBJECT), "Encoded Classic Bean");
    }
    #endif

    @Test
    void decodeClassicBean() {
        //
        // Arrange
        //
        BeanTransformer beanTransformer = new StandardBeanTransformer();
        //
        // Act
        //
        ClassicBean decoded = (ClassicBean)beanTransformer.decode(
            CLASSIC_PROLOG + CLASSIC_OBJECT + CLASSIC_EPILOG,
            Exception::printStackTrace
        );
        //
        // Assert
        //
        Assertions.assertTrue(decoded.getMutableDateTime() instanceof java.util.Date, "Reconstructed Mutable Date Time");
        Assertions.assertTrue(decoded.getYearMonthDuration() instanceof javax.xml.datatype.Duration, "Original Year Month Duration");
        Assertions.assertTrue(decoded.getDayTimeDuration() instanceof javax.xml.datatype.Duration, "Original Day Time Duration");
        #if CLASSIC_CHRONO_TYPES
        Assertions.assertFalse(decoded.getMutableDateTime() instanceof org.w3c.cci2.ImmutableDateTime, "Reconstructed Mutable Date Time");
        Assertions.assertTrue(decoded.getImmutableDateTime() instanceof org.w3c.cci2.ImmutableDateTime, "Reconstructed Immutable Date Time");
        Assertions.assertTrue(decoded.getMutableDate() instanceof org.w3c.cci2.ImmutableDate, "Reconstructed Mutable Date");
        Assertions.assertTrue(decoded.getImmutableDate() instanceof org.w3c.cci2.ImmutableDate, "Reconstructed Immutable Date");
        #else
        Assertions.assertEquals(java.util.Date.class, decoded.getMutableDateTime().getClass(), "Reconstructed Mutable Date Time");
        Assertions.assertEquals(java.util.Date.class, decoded.getImmutableDateTime().getClass(), "Reconstructed Immutable Date Time");
        Assertions.assertEquals(StandardBeanTransformer.classicDateClass, decoded.getMutableDate().getClass(), "Reconstructed Mutable Date");
        Assertions.assertEquals(StandardBeanTransformer.classicDateClass, decoded.getImmutableDate().getClass(), "Reconstructed Immutable Date");
        #endif
    }

    @Test
    void decodeContemporaryBean() {
        //
        // Arrange
        //
        BeanTransformer beanTransformer = new StandardBeanTransformer();
        //
        // Act
        //
        ContemporaryBean decoded = (ContemporaryBean)beanTransformer.decode(
                CONTEMPORARY_PROLOG + CONTEMPORARY_OBJECT + CONTEMPORARY_EPILOG,
                Exception::printStackTrace
        );
        //
        // Assert
        //
        Assertions.assertEquals("PT26H3M4S", decoded.getDayTimeDuration().toString(), "Reconstructed Day Time Duration");
        Assertions.assertEquals("P1Y2M3D", decoded.getYearMonthDayDuration().toString(), "Reconstructed Yaer Month Day Duration");
    }

}
