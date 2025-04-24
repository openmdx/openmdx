package org.openmdx.base.query.lenient;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.kernel.exception.BasicException;

class LenientTemporalComparator implements LenientComparator {

    LenientTemporalComparator() {
        super();
    }

    @Override
    public boolean test(Object first, Object second) {
        return first instanceof TemporalAmount && second instanceof TemporalAmount;
    }

    @Override
    public int compare(Object first, Object second) {
        if (first instanceof Duration && second instanceof Duration) {
            return compare((Duration) first, (Duration) second);
        } else if (first instanceof Period && second instanceof Period) {
            return compare((Period) first, (Period) second);
        } else if (first instanceof Period && second instanceof Duration) {
            return compare((Period) first, (Duration) second);
        } else if (first instanceof Duration && second instanceof Period) {
            return -compare((Period) second, (Duration) first);
        } else {
            throw new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "The arguments can't be compared",
                new BasicException.Parameter("operands", first, second),
                new BasicException.Parameter("classes", first.getClass(), second.getClass())
            );
        }
    }

    private int compare(Duration first, Duration second) {
        return first.compareTo(second);
    }

    private int compare(Period first, Period second) {
        final Period difference = first.minus(second);
        return difference.isZero() ? 0 : difference.isNegative() ? -1 : 1;
    }

    private int compare(Period period, Duration duration) {
        final int days = (int) duration.toDays();
        final Period second = Period.ofDays(days);
        final Period difference = period.minus(duration);
        if(difference.isZero()) {
            return Duration.ofDays(days).compareTo(duration);
        } else {
            return difference.isNegative() ? -1 : 1;
        }
    }

}
