/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: JCA: Utility methods for records
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.base.resource;

import java.lang.reflect.Array;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Record;

import org.openmdx.base.resource.cci.ArrayBasedIndexedRecord;
import org.openmdx.base.resource.cci.ExtendedRecordFactory;
import org.openmdx.base.resource.spi.StandardRecordFactory;


/**
 * Java Connector Architecture:
 * Record utilities.
 */
public class Records 
{

	/**
	 * Avoid instantiation
	 */ 
	private Records(
	){
	    super();
	}
	

	//------------------------------------------------------------------------
	// Supports IndexedRecord
	//------------------------------------------------------------------------

	/**
	 * Return the record's values as single-dimensional or multi-dimensional
	 * array.
	 * <p>
	 * A multi-dimensional array is returned if all the object's values are 
	 * IndexedRecords as well.
	 * <p>
	 * The returned array will not be "safe" as references to the innermost 
	 * arrays are maintained by the corresponding IndexedRecord if possible. 
	 * (In other words, this method does not allocate a new array if an index
	 * record is backed uo by a one-dimensional array, e.g. int[].)
	 *
	 * @param   source
	 *          the IndexedRecord
	 *
	 * @return  the array backing the record's values.
	 *
	 * @exception ResourceException
	 *            Failed to create an n-dimensional array.
	 */
	public static Object nDimensionalArray(
		IndexedRecord source
	)throws ResourceException{

		Object[] genericArray = null;
		
		// Handle arrays or primitive types
		if(source instanceof ArrayBasedIndexedRecord){
			Object v=((ArrayBasedIndexedRecord)source).getValues();
			if(v instanceof Record[]){
				genericArray = new Object[source.size()];
			} else if(v instanceof Object[]){
				genericArray = (Object[])v;
			} else {
				return v;
			}
		} else {
			genericArray = new Object[source.size()];
		}

		// Handle lists of objects recursively
		Class<?> elementClass=null;
		for(
			int i=0;
			i<genericArray.length;
			i++
		){
			Object s=source.get(i);
			Object t=s instanceof IndexedRecord?
				nDimensionalArray((IndexedRecord)s):
				s;
			if(t!=null){
				if(elementClass==null){
					elementClass=t.getClass();
				}else while(!elementClass.isInstance(t)){
					elementClass=elementClass.getSuperclass();
				}
			}
			genericArray[i]=t;
		}

		// Convert the generic array to a more specific one if possible
		if(
			elementClass==null ||
			elementClass==genericArray.getClass().getComponentType()
		){
			return genericArray;
		} else {
			Object specificArray=Array.newInstance(
				elementClass,
				genericArray.length
			);
			System.arraycopy(
				genericArray,0,
				specificArray,0,
				genericArray.length
			);
			return specificArray;
		}
	}


    //------------------------------------------------------------------------
    // Supports ExtendedRecordFactory
    //------------------------------------------------------------------------
    
    /**
     * Get a record factory instance.
     *
     * @return  an ExtendedRecordFactory instance
     */
    public static ExtendedRecordFactory getRecordFactory(
    ){
       return StandardRecordFactory.getInstance();
    }       

}
