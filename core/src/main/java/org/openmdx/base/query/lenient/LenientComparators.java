package org.openmdx.base.query.lenient;

import java.util.Comparator;
import java.util.Optional;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.kernel.exception.BasicException;

public class LenientComparators implements Comparator<Object> {

    private LenientComparators(){
        super();
    }

    private static LenientComparators INSTANCE = new LenientComparators();

    private final LenientComparator[] lenientComparators = {
        new LenientPathComparator(),
        #if CLASSIC_CHRONO_TYPES
        new LenientClassicDateComparator(),
        new LenientClassicDateTimeComparator(),
        new LenientClassicDurationComparator(),
        #else
        new LenientTemporalComparator(),
        #endif
        new LenientNumberComparator(),
        new LenientCharacterComparator(),
        new StandardComparator()
    };

    public static Comparator<Object> getComparator() {
        return INSTANCE;
    }

    public static boolean equivalent(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            return (o1 == null) == (o2 == null);
        }
        final Optional<Comparator<Object>> comparator = INSTANCE.findComparator(o1, o2);
        return comparator.isPresent() ? comparator.get().compare(o1, o2) == 0 : o1.equals(o2);
    }

    @Override
    public int compare(Object o1, Object o2) {
        return findComparator(o1, o2).orElseThrow(
            () -> new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "The two arguments can't be compared",
                new BasicException.Parameter("operands", o1, o2),
                new BasicException.Parameter(
                    "classes",
                    o1 == null ? "<null>" : o1.getClass().getName(),
                    o2 == null ? "<null>" : o2.getClass().getName()
                )
            )
        ).compare(o1, o2);
    }

    private Optional<Comparator<Object>> findComparator(Object o1, Object o2) {
        for(LenientComparator candidate : lenientComparators) {
            if(candidate.test(o1, o2)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

}
