package org.openmdx.base.query.lenient;

class StandardComparator implements LenientComparator {

    /**
     * Constructor
     */
    StandardComparator(){
        super();
    }

    @Override
    public boolean test(Object o, Object o2) {
        return o instanceof Comparable && o2 instanceof Comparable;
    }

    @Override
    public int compare(Object o1, Object o2) {
        return compare((Comparable<?>) o1, (Comparable<?>) o2);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private int compare(Comparable o1, Comparable o2) {
        return o1.compareTo(o2);
    }

}
