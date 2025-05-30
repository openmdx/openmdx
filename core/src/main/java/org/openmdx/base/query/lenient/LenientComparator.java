package org.openmdx.base.query.lenient;

import java.util.Comparator;
import java.util.function.BiPredicate;

interface LenientComparator extends Comparator<Object>, BiPredicate<Object, Object> {
    // Just combines the Comparator and BiPredicate interfaces
}
