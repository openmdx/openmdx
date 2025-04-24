package org.w3c.spi;

#if CLASSIC_CHRONO_TYPES
import javax.xml.datatype.Duration;
#else
import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import javax.xml.datatype.XMLGregorianCalendar;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.SysLog;
#endif

abstract class AbstractChronoTypeFactory implements ChronoTypeFactory {

    @Override
    public final #if CLASSIC_CHRONO_TYPES Duration #else TemporalAmount #endif newDuration(String externalRepresentation) {
        if(externalRepresentation == null) {
            return null;
        }
        final int y = externalRepresentation.indexOf('Y');
        final int m = externalRepresentation.indexOf('M');
        final int w = externalRepresentation.indexOf('W');
        final int d = externalRepresentation.indexOf('D');
        final int t = externalRepresentation.indexOf('T');
        final boolean time = t > 0;
        final boolean day = d > 0;
        final boolean week = w > 0;
        final boolean year = y > 0 ;
        final boolean month = m > 0 && (!time || m < t);
        if(year || month) {
            if(time) {
                return newDurationYearMonthDayTime(externalRepresentation);
            } else if (week || day ) {
                return newDurationYearMonthDay(externalRepresentation);
            } else {
                return newDurationYearMonth(externalRepresentation);
            }
        } else {
            return newDurationDayTime(externalRepresentation);
        }
    }

    protected abstract #if CLASSIC_CHRONO_TYPES Duration #else TemporalAmount #endif newDurationYearMonthDay(String value);
    protected abstract #if CLASSIC_CHRONO_TYPES Duration #else TemporalAmount #endif newDurationYearMonthDayTime(String value);

}
