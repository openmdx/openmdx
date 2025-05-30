/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Standard Marshaller 
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
package org.openmdx.base.accessor.jmi.spi;

import java.io.Serializable;
import java.util.AbstractSequentialList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jdo.spi.PersistenceCapable;
import javax.jmi.reflect.RefBaseObject;
import javax.jmi.reflect.RefObject;
import #if JAVA_8 javax.resource.cci.Record #else jakarta.resource.cci.Record #endif;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.jmi.cci.RefStruct_1_0;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingSequentialList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSortedMap;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.persistence.spi.PersistenceCapableCollection;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;
import org.w3c.cci2.Container;
import org.w3c.cci2.SortedMaps;
import org.w3c.cci2.SparseArray;

/**
 * Class StandardMarshaller
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class StandardMarshaller implements Marshaller {

	/**
	 * Constructor
	 *  
	 * @param outermostPackage
	 */
	StandardMarshaller(
		RefRootPackage_1 outermostPackage
    ){
		this.outermostPackage = outermostPackage;
    }

	/**
	 * The outermost package this marshaller belongs to
	 */
	private final RefRootPackage_1 outermostPackage;
	
    /**
     * Retrieve the marshaller's delegate
     * 
     * @return the outermost package
     */
    Jmi1Package_1_0 getOutermostPackage(){
        return this.outermostPackage;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.Marshaller#unmarshal(java.lang.Object)
     */
    public Object unmarshal(
        Object source
    ){
        try {
            return 
                source instanceof RefStruct_1_0 ? ((RefStruct_1_0)source).refDelegate() :  
                source instanceof Object[] ? unmarshal((Object[]) source) :
            	this.outermostPackage.unmarshal(source);
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.Marshaller#marshal(java.lang.Object)
     */
    public Object marshal(
        Object source
    ){
        try {
            return source instanceof Object[] ? marshal(
                (Object[])source
            ) : source instanceof Container<?> ? Classes.newProxyInstance(
                new Jmi1ContainerInvocationHandlerWithCciDelegate(this, (Container<?>)source),
                source.getClass().getInterfaces()[0], 
                RefContainer.class, 
                PersistenceCapableCollection.class,
                Serializable.class 
            ) : source instanceof List<?> ? marshal(
                (List<?>)source
            ) : source instanceof Set<?> ? new MarshallingSet(
                this, 
                (Set<?>)source
            ) : source instanceof SparseArray<?> ? SortedMaps.asSparseArray(
                new MarshallingSortedMap(this, (SparseArray<Object>)source)
            ) : source instanceof Iterator<?> ? new MarshallingIterator(
                (Iterator<?>)source
            ) : source instanceof Record ? this.outermostPackage.refCreateStruct(
                (Record)source
            ) : source instanceof PersistenceCapable ? this.outermostPackage.marshal(
                source
            ) : source;
        }  catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    public final List marshal(
        List source
    ){
        return source instanceof AbstractSequentialList ? new MarshallingSequentialList(
            this,
            source
        ) : new MarshallingList(
            this, 
            source
        );
    }

    /**
     * Unmarshal an array of objects
     * 
     * @param source
     * 
     * @return an array containing the unmarshalled objects
     */
    public final Object[] unmarshal(
        Object[] source
    ){
        if(source != null && source.length > 0) {
            for(
                int i = 0, l = source.length;
                i < l;
                i++
            ){ 
                Object s = source[i];
                Object t = unmarshal(s);
                if(s != t) {
                    Object[] target = new Object[source.length];
                    System.arraycopy(source, 0, target, 0, i);
                    target[i] = t;
                    for(
                        int j = i + 1;
                        j < l;
                        j++
                    ){
                        target[j] = unmarshal(source[j]);
                    }
                    return target;
                }
            }
        }
        return source;
    }

    /**
     * Marshal an array of objects
     * 
     * @param source
     * 
     * @return an array containing the marshalled objects
     */
    public final Object[] marshal(
        Object[] source
    ){
        if(source != null && source.length > 0) {
            for(
                int i = 0, l = source.length;
                i < l;
                i++
            ){ 
                Object s = source[i];
                Object t = marshal(s);
                if(s != t) {
                    Object[] target = new Object[source.length];
                    System.arraycopy(source, 0, target, 0, i);
                    target[i] = t;
                    for(
                        int j = i + 1;
                        j < l;
                        j++
                    ){
                        target[j] = marshal(source[j]);
                    }
                    return target;
                }
            }
        }
        return source;
    }

	/**
	 * Validate a given object
	 * 
	 * @param value
	 * 
	 * @throws ServiceException 
	 */
	void validate(
		Object value
	) throws ServiceException{
		if(value instanceof RefBaseObject) {
			if(this.outermostPackage !=  ((RefBaseObject)value).refOutermostPackage()){
				throw new ServiceException(
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.ASSERTION_FAILURE,
					"RefPackage mismatch, the object does not have the expected outermost package"
				);
			}
		}
	}
    
    /**
     * MarshallingIterator
     */
    class MarshallingIterator<T> implements Iterator<T> {

        /**
         * Constructor 
         *
         * @param delegate
         */
        MarshallingIterator(
            Iterator<?> delegate
        ){
            this.delegate = delegate;
        }

        private final Iterator<?> delegate;

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public T next() {
            return (T) marshal(this.delegate.next());
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            this.delegate.remove();
        }

    }

}