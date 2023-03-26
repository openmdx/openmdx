/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Isolation
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
package org.openmdx.base.resource.spi;

import java.util.Date;

import javax.resource.cci.Record;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.resource.cci.Freezable;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.ImmutableDatatype;
import org.w3c.spi.DatatypeFactories;

/**
 * Isolation
 */
public class Isolation {

	private Isolation() {
		// Avoid instantiation
	}

	/**
	 * Make the object immutable or create a immutable copy for supported values<ul>
	 * <li>{@code java.util.Date}
	 * <li>{@code javax.xml.datatype.XMLGregorianCalendar}
	 * <li>{@code org.openmdx.base.resource.cci.Freezable}
	 * </ul>
	 * <p>
	 * Note:<br>
	 * In order to work properly all other values must be immutable
	 * (although this is not enforced by this method).
	 * 
	 * @param value the value to be transformed
	 * 
	 * @return the given value or an immutable copy of it
	 */
	public static Object toImmutable(Object value) {
		if(value instanceof Date && !(value instanceof ImmutableDatatype<?>)) {
			return DatatypeFactories.immutableDatatypeFactory().toDateTime((Date)value);
		} else if (value instanceof XMLGregorianCalendar && !(value instanceof ImmutableDatatype<?>)) {
			return DatatypeFactories.immutableDatatypeFactory().toDate((XMLGregorianCalendar)value);
		} else if (value instanceof Record) {
			return toImmutable((Record)value);
		} else {
			return value;
		}
	}

	/**
	 * Make the object immutable or create a immutable copy for supported values<ul>
	 * <li>{@code org.openmdx.base.resource.cci.Freezable} &amp;
	 * {@code javax.resource.cci.Record}
	 * </ul>
	 * 
	 * @param value the value to be transformed
	 * 
	 * @return the given value or an immutable copy of it
	 */
	public static <T extends Record> T toImmutable(T value) {
		final T isolated = isolate(value);
		if(isolated != value && isolated instanceof Freezable) {
			((Freezable)isolated).makeImmutable();
		}
		return isolated;
	}
	
	public static void freeze(Record value) {
		if(value instanceof Freezable) {
			((Freezable)value).makeImmutable();
		}
	}
	
	/**
	 * The method returns the given record if it is immutable
	 * or its clone if it is mutable
	 * 
	 * @param record a given record
	 * 
	 * @return an isolated representation of the given record
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Record> T isolate(
		T record
	){
		if(
			record == null || (
				record instanceof Freezable && ((Freezable)record).isImmutable()
			)
		) {
			return record;
		}
		try {
			return (T)record.clone();
		} catch (CloneNotSupportedException e) {
	        throw new IllegalArgumentException(
	            "The record is neither immutable nor cloneable",
	            BasicException.newEmbeddedExceptionStack(
	                BasicException.Code.DEFAULT_DOMAIN,
	                BasicException.Code.ILLEGAL_STATE,
	                new BasicException.Parameter("name", record.getRecordName()),
	                new BasicException.Parameter("class", record.getClass().getName()),
	                new BasicException.Parameter("freezable", record instanceof Freezable)
	            )
	        );
		}
	}
	
}
