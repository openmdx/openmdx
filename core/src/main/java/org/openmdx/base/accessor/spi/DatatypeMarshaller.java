package org.openmdx.base.accessor.spi;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
#if CLASSIC_CHRONO_TYPES
import org.w3c.cci2.ImmutableDatatype;
import org.w3c.spi.ImmutableDatatypeFactory;
#endif
import org.w3c.spi2.Datatypes;

/**
 * Datatype Marshaller
 */
public abstract class DatatypeMarshaller implements Marshaller {

    /**
     * Constructor 
     *
     * @param targetClass
     */
    public DatatypeMarshaller(
        Class<?> targetClass
    ) {
        this.targetClass = targetClass;
    }

    /**
     * The datatype's java class
     */
    protected final Class<?> targetClass;

    /* (non-Javadoc)
     * @see org.openmdx.base.marshalling.Marshaller#marshal(java.lang.Object)
     */
    public Object marshal(
        Object source
    ) throws ServiceException {
        return Datatypes.create(this.targetClass, (String)source);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.marshalling.Marshaller#unmarshal(java.lang.Object)
     */
    public Object unmarshal(
        Object source
    ) throws ServiceException {
        return
            source == null ? null :
            source instanceof ImmutableDatatype<?> ? ((ImmutableDatatype<?>)source).toBasicFormat() :
            toBasicFormat(source);    
    }
    
    protected abstract String toBasicFormat(
        Object datatype
    );
    
}