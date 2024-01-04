package org.openmdx.base.accessor.spi;

import java.util.Optional;

import org.openmdx.base.marshalling.TypeSafeMarshaller;
import org.w3c.cci2.ImmutableDatatype;
import org.w3c.spi2.Datatypes;

/**
 * Datatype Marshaller
 */
public abstract class DatatypeMarshaller<T> implements TypeSafeMarshaller<String, T> {

    /**
     * Constructor 
     *
     * @param targetClass
     */
    protected DatatypeMarshaller(
        Class<T> targetClass
    ) {
        this.targetClass = targetClass;
    }

    /**
     * The datatype's java class
     */
    protected final Class<T> targetClass;

    /* (non-Javadoc)
     * @see org.openmdx.base.marshalling.Marshaller#marshal(java.lang.Object)
     */
    public T marshal(
        String source
    ){
        return Datatypes.create(this.targetClass, source);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.marshalling.Marshaller#unmarshal(java.lang.Object)
     */
    public String unmarshal(
        T source
    ){
        return
            source == null ? null :
            source instanceof ImmutableDatatype<?> ? ((ImmutableDatatype<?>)source).toBasicFormat() :
            toBasicFormat(source);    
    }
    
    protected abstract String toBasicFormat(
        T datatype
    );

	@Override
	public Optional<String> asUnmarshalledValue(Object value) {
		return value instanceof String ? Optional.of((String)value) : Optional.empty();
	}

	@Override
	public Optional<T> asMarshalledValue(Object value) {
		return this.targetClass.isInstance(value) ? Optional.of(this.targetClass.cast(value)) : Optional.empty();
	}
    
}