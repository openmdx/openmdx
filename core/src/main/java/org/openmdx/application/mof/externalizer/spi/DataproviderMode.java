/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Dataprovider Mode
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
package org.openmdx.application.mof.externalizer.spi;

import java.util.Collection;
import java.util.List;

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.cci.MappedRecord;
#endif

import org.openmdx.base.rest.cci.ObjectRecord;

/**
 * TODO to be inlined
 */
@SuppressWarnings("rawtypes")
public enum DataproviderMode {
	
	/**
	 * The standard mode
	 */
	DATAPROVIDER_2{
		
        @SuppressWarnings("unchecked")
        @Override
	    public void replaceAttributeValuesAsList(
    		ObjectRecord target,
    		String feature,
    		Collection<?> values
    	) throws ResourceException{
    		target.getValue().put(feature, values);
    	}
    		
        @SuppressWarnings("unchecked")
		@Override
        public void replaceAttributeValueAsMode1ListOrMode2Singleton(
    		ObjectRecord target,
    		String feature,
    		Object value
    	) throws ResourceException{
    		target.getValue().put(feature, value);
    	}
        
		@Override
        public Object getSingletonFromAttributeValuesAsList(
        	ObjectRecord source,
        	String feature
        ) throws ResourceException {
    		return source.getValue() == null ? null : source.getValue().get(feature);
        }
        		
		@Override
        public boolean attributeHasValue(
        	ObjectRecord source,
        	String feature
        ) throws ResourceException {
		    Object value = source.getValue().get(feature);
		    return value instanceof List
		        ? !((List)value).isEmpty()
		        : value != null;
        }

		@Override
        @SuppressWarnings("unchecked")
        public void replaceAttributeValuesAsListBySingleton(
        	ObjectRecord target,
        	String feature,
        	Object value
        ) throws ResourceException{
    		target.getValue().put(feature, value);
        }
        
		@Override
        public Object attributeValue(
        	ObjectRecord target,
        	String feature
        ) throws ResourceException {
    		return target.getValue().get(feature);
        }
        
        @SuppressWarnings("unchecked")
        public void addAllToAttributeValuesAsList(
        	ObjectRecord target,
        	String feature,
        	Collection<?> values
        ) throws ResourceException {
    		final MappedRecord mappedRecord = target.getValue();
    		final List indexedRecord = (List) mappedRecord.get(feature);
    		indexedRecord.addAll(values);
        }

        public boolean attributeValuesAsListContains(
        	ObjectRecord source,
        	String feature,
        	Object value
        ) throws ResourceException{
    		final List values = (List) source.getValue().get(feature);
    		return values != null && values.contains(value);
        }
        
        public void clearAttributeValuesAsList(
        	ObjectRecord source,
        	String feature
        ) throws ResourceException{
    		final List values = (List) source.getValue().get(feature);
    		if(values != null) {
    			values.clear();
    		}
    	}
        
        @SuppressWarnings("unchecked")
		public void addToAttributeValuesAsList(
        	ObjectRecord target,
        	String feature,
        	Object value
        ) throws ResourceException {
    		target.getValue().put(feature, value);
        }

        public List<?> getAttributeValuesAsReadOnlyList(
        	ObjectRecord source,
        	String feature
        ) throws ResourceException {
    		final List indexedRecord = (List) source.getValue().get(feature);
    		return indexedRecord;
        }

	};
	
	public abstract void replaceAttributeValuesAsList(
		ObjectRecord target,
		String feature,
		Collection<?> values
	) throws ResourceException;
		
    public abstract void replaceAttributeValueAsMode1ListOrMode2Singleton(
		ObjectRecord target,
		String feature,
		Object value
	) throws ResourceException;
    
    public abstract Object getSingletonFromAttributeValuesAsList(
    	ObjectRecord source,
    	String feature
    ) throws ResourceException;
    		
    public abstract boolean attributeHasValue(
    	ObjectRecord source,
    	String feature
    ) throws ResourceException;

    public abstract void replaceAttributeValuesAsListBySingleton(
    	ObjectRecord target,
    	String feature,
    	Object value
    ) throws ResourceException;
    
    public abstract Object attributeValue(
    	ObjectRecord target,
    	String feature
    ) throws ResourceException ;
    
    public abstract void addAllToAttributeValuesAsList(
    	ObjectRecord target,
    	String feature,
    	Collection<?> values
    ) throws ResourceException;
    
    public abstract boolean attributeValuesAsListContains(
    	ObjectRecord source,
    	String feature,
    	Object value
    ) throws ResourceException;
    
    public abstract void clearAttributeValuesAsList(
    	ObjectRecord source,
    	String feature
    ) throws ResourceException;
    
    public abstract void addToAttributeValuesAsList(
    	ObjectRecord target,
    	String feature,
    	Object value
    ) throws ResourceException;

    public abstract List<?> getAttributeValuesAsReadOnlyList(
    	ObjectRecord source,
    	String feature
    ) throws ResourceException;

}