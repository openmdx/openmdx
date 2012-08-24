/*
 * ====================================================================
 * Description: A state-aware XML comparator
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2011, OMEX AG, Switzerland
 * All rights reserved.
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
package org.openmdx.state2.cci;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.resource.cci.MappedRecord;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.application.xml.Importer;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.collection.Sets;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.state2.spi.Order;
import org.xml.sax.InputSource;

/**
 * A state-aware XML comparator
 * <p>
 * Note:<br><em>
 * This class is not thread safe.
 * </em>
 */
public class ObjectAndValidStateComparator {
		
	private Model_1_0 model = Model_1Factory.getModel();
	List<MappedRecord> differences = new ArrayList<MappedRecord>();
	
	/**
	 * Aspects are collected under their core reference and processed in a subsequent step
	 */
	Map<Path,Aspects> aspects = new HashMap<Path, ObjectAndValidStateComparator.Aspects>();
	
	/**
	 * The features to be ignored by default
	 */
	protected final static Collection<String> FEATURES_IGNORED_BY_DEFAULT = Arrays.asList(
		"org:openmdx:base:Removable:removedAt",
		"org:openmdx:base:Removable:removedBy",
		"org:openmdx:base:Creatable:createdAt",
		"org:openmdx:base:Creatable:createdBy",
		"org:openmdx:base:Modifiable:modifiedAt",
		"org:openmdx:base:Modifiable:modifiedBy",
		"org:openmdx:state2:StateCapable:stateVersion"
	);

    /**
     * State validity
     */
    protected final static Collection<String> RANGE_FEATURES = Arrays.asList(
        "org:openmdx:state2:DateState:stateValidFrom",
        "org:openmdx:state2:DateState:stateValidTo",
        "org:openmdx:state2:DateTimeState:stateValidFrom",
        "org:openmdx:state2:DateTimeState:stateInvalidFrom"
    );
	
	/**
	 * Accepts <em>equal</em> values
	 */
	public static final FeatureComparator EQUALITY = new FeatureComparator(){

		public MappedRecord compare(
			Path xri, 
			String feature,
			Multiplicity multiplicity, 
			Object expected, 
			Object actual
		) {
			if(expected == actual) {
				return null;
			}
			MappedRecord difference = CARDINALITY.compare(xri, feature, multiplicity, expected, actual);
			if(difference != null) {
				return difference;
			}
			final boolean equal;
			switch(multiplicity) {
				case SET:
					equal = asSet(actual).equals(asSet(expected));
					break;
				case LIST:
					equal = asList(actual).equals(asList(expected));
					break;
				default:
					equal = actual != null && expected != null && actual.equals(expected);
					break;
			}
			return equal ? null : ObjectAndValidStateComparator.newDifference(
				xri, 
				feature, 
				multiplicity, 
				"The actual value of feature '" + feature + "' does not match the expected one", 
				expected, 
				actual
			);
		}
		
		private Set<?> asSet(
			Object value
		){
			if(value == null)  {
				return Collections.emptySet();
			} else if (value instanceof Collection<?>) {
				return Sets.asSet((Collection<?>)value);
			} else {
				return Collections.singleton(value);
			}
		}

		private List<?> asList(
			Object value
		){
			if(value == null)  {
				return Collections.emptyList();
			} else if (value instanceof List<?>) {
				return (List<?>)value;
			} else {
				return Collections.singletonList(value);
			}
		}
		
	};

	
	/**
	 * Accepts values with the same <em>cardinality</em>.
	 */
	public static final FeatureComparator CARDINALITY = new FeatureComparator(){

		public MappedRecord compare(
			Path xri, 
			String feature,
			Multiplicity multiplicity, 
			Object expected, 
			Object actual
		) {
			int expectedCardinality = getCardinality(expected);
			int actualCardinality = getCardinality(actual);
			return expectedCardinality == actualCardinality ? null : ObjectAndValidStateComparator.newDifference(
				xri, 
				feature, 
				multiplicity, 
				"The actual cardinality " + actualCardinality + " does not match the expected cardinality " + expectedCardinality, 
				expected, 
				actual
			);
		}

		private int getCardinality(
			Object value
		){
			return 
				value == null ? 0 :
				value instanceof Collection<?> ? ((Collection<?>)value).size() :
				value instanceof Map<?,?> ? ((Map<?,?>)value).size() :
				1;
		}
		
	};

	/**
	 * Accepts <em>any</em> values
	 */
	public static final FeatureComparator IGNORE = new FeatureComparator() {
		
		public MappedRecord compare(
			Path xri, 
			String feature, 
			Multiplicity multiplicity,
			Object expected,
			Object actual
		) {
			return null;
		}
	};
	
	/**
	 * Compare the objects and return the differences
	 * 
	 * @param expectedObjects
	 * @param actualObjects
	 * 
	 * @return the differences
	 * @throws ServiceException 
	 */
	public List<MappedRecord> compare(
		SortedMap<Path,MappedRecord> expectedObjects,
		SortedMap<Path,MappedRecord> actualObjects
	) throws ServiceException {
	    clear();
		compareObjects(
			expectedObjects.values().iterator(),
			actualObjects.values().iterator()
		);
		for(Map.Entry<Path,Aspects> entry : this.aspects.entrySet()) {
			this.compareAspects(entry.getKey(), entry.getValue());
		}
		return this.differences;
	}

	protected void clear(){
        this.differences.clear();
        this.aspects.clear();
	}
	
	/**
	 * Compare the objects and return the differences
	 * 
	 * @param expected
	 * @param actual
	 * 
	 * @return the differences
	 * @throws ServiceException 
	 */
	public List<MappedRecord> compare(
		Iterable<InputSource> expected,
		Iterable<InputSource> actual
	) throws ServiceException {
		SortedMap<Path,MappedRecord> expectedObjects = new TreeMap<Path, MappedRecord>();
		Importer.importObjects(Importer.asTarget(expectedObjects), expected);
		SortedMap<Path,MappedRecord> actualObjects = new TreeMap<Path, MappedRecord>();
		Importer.importObjects(Importer.asTarget(actualObjects), actual);
		return this.compare(expectedObjects, actualObjects);
	}

	/**
	 * Retrieve a feature comparator
	 * 
	 * @param qualifiedName the qualified feature name
	 * @return the comparator to be used for this feature
	 */
	protected FeatureComparator getFeatureComparator(
		String qualifiedName
	){
		return FEATURES_IGNORED_BY_DEFAULT.contains(qualifiedName) ? IGNORE : EQUALITY;
	}

	private boolean isInstanceOf(
		MappedRecord object,
		String type
	) throws ServiceException{
		return this.model.isSubtypeOf(object.getRecordName(), type);
	}

	private Aspects getAspects(
		Path core
	){
		Aspects aspects = this.aspects.get(core);
		if(aspects == null) {
			this.aspects.put(
				core,
				aspects = new Aspects()
			);
		}
		return aspects;
	}
	
	/**
	 * Tests whether an object is modelled as stated but not used as such
	 * 
	 * @param object
	 * 
	 * @return <code>true</code> if the object is modelled as stated but not used as such
	 * @throws ServiceException
	 */
	protected boolean isValidTimeUnique(
	    MappedRecord object
	) throws ServiceException{
	    return 
	        isInstanceOf(object, "org:openmdx:state2:BasicState") &&
	        Boolean.TRUE.equals(object.get("validTimeUnique"));
	}
	
	private Object_2Facade getNext(
		Iterator<MappedRecord>	i,
		boolean expected
	) throws ServiceException{
		while(i.hasNext()) {
			Object_2Facade e = Facades.asObject(i.next());
			MappedRecord object = e.getValue();
			if(isInstanceOf(object, "org:openmdx:base:Aspect")) {
			    if(isValidTimeUnique(object)){
		            return e;
			    }
				Object core = object.get("core");
				if(core instanceof Path) {
					Aspects aspects = getAspects((Path)core);
					(expected ? aspects.expected : aspects.actual).add(object);
				} else {
					this.differences.add(
						newDifference(e.getPath(), "An aspect has no core reference", (MappedRecord)null, object)
					);
				}
			} else {
				return e;
			}
		}
		return null;
	}

	private void compareObjects(
		Iterator<MappedRecord> ei,
		Iterator<MappedRecord> ai
	) throws ServiceException {
		Object_2Facade e = getNext(ei, true);
		Object_2Facade a = getNext(ai, false);
		while(e != null || a != null) {
			while(a != null && (e == null || a.getPath().compareTo(e.getPath()) < 0 )) {
				this.differences.add(
					newDifference(a.getPath(), "Unexpected actual object", (MappedRecord)null, a.getValue())
				);
				a = getNext(ai, true);
			}
			while(e != null && (a == null || a.getPath().compareTo(e.getPath()) > 0 )) {
				this.differences.add(
					newDifference(e.getPath(), "Missing actual object", e.getValue(), null)
				);
				e = getNext(ei, false);
			}
			if(a != null && e != null && a.getPath().equals(e.getPath())) {
				compareObject(e.getPath(), e.getValue(), a.getValue(), false);
				e = getNext(ei, true);
				a = getNext(ai, false);
			}
		}
	}

	private MappedRecord getState(
		List<MappedRecord> source,
		Interval interval
	) throws ServiceException{
		for(MappedRecord object : source) {
			if(this.isInstanceOf(object, "org:openmdx:state2:DateState")) {
				XMLGregorianCalendar validFrom = (XMLGregorianCalendar) object.get("stateValidFrom");
				XMLGregorianCalendar validTo = (XMLGregorianCalendar) object.get("stateValidTo");
				if(object.get(SystemAttributes.REMOVED_AT) == null) {
    				if(
    					Order.compareValidFrom(interval.validFrom, validFrom) >= 0 &&
    					Order.compareValidTo(interval.validTo, validTo) <= 0
    				){
    					return object;
    				}
				}
			} else {
				throw new ServiceException(
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.NOT_IMPLEMENTED,
					"Unsupported Aspect",
					new BasicException.Parameter("class",object.getRecordName())
				);
			}
		}
		return null;
	}
	
	private void compareAspects(
		Path xri,
		Aspects aspects
	) throws ServiceException {
		List<Interval> intervals = new ArrayList<Interval>();
		intervals.add(new Interval(null,null));
		getIntervals(intervals, aspects.expected);
		getIntervals(intervals, aspects.actual);
		for(Interval interval : intervals) {
			MappedRecord expected = getState(aspects.expected, interval);
			MappedRecord actual = getState(aspects.actual, interval);
			if(expected != actual) {
				if(actual == null) {
					this.differences.add(
						newDifference(xri, interval, ("No actual state for time range " + interval), expected, null)
					);
				} else if(expected == null) {
					this.differences.add(
						newDifference(xri, interval, ("Unexpected state for time range " + interval), null, actual)
					);
				} else {
					compareObject(xri, expected, actual, true);
				}
			}
		}			
		
	}

	private void getIntervals(
		List<Interval> intervals,
		Collection<MappedRecord> source
	){
		for(MappedRecord object : source) {
			XMLGregorianCalendar validFrom = (XMLGregorianCalendar) object.get("stateValidFrom");
			XMLGregorianCalendar validTo = (XMLGregorianCalendar) object.get("stateValidTo");
			From: for(Interval interval : intervals){
				int i = Order.compareValidFrom(validFrom, interval.validFrom);
				if(i == 0) {
					break From;
				}
				if(i > 0 && Order.compareValidFromToValidTo(validFrom, interval.validTo) <= 0) {
					intervals.add(new Interval(validFrom, interval.validTo));
					interval.validTo = Order.predecessor(validFrom);
					break From;
				}
			}
			To: for(Interval interval : intervals){
				int i = Order.compareValidTo(interval.validTo, validTo);
				if(i == 0) {
					break To;
				}
				if(i > 0 && Order.compareValidFromToValidTo(interval.validFrom, validTo) <= 0) {
					intervals.add(new Interval(interval.validFrom, validTo));
					interval.validFrom = Order.successor(validTo);
					break To;
				}
			}
		}
	}

	/**
	 * State aware feature comparator determination
	 * 
	 * @param qualifiedName the qualified feature name
	 * @param aspect <code>true</code> in case of aspects
	 * 
	 * @return the feature comparator to be used
	 * 
	 * @throws ServiceException 
	 */
	private FeatureComparator getFeatureComparator(
	    String qualifiedName,
	    boolean aspect
	) throws ServiceException{
        return aspect && RANGE_FEATURES.contains(qualifiedName) ? IGNORE : getFeatureComparator(qualifiedName);
	}
	
	@SuppressWarnings("unchecked")
	private void compareObject(
		Path xri,
		MappedRecord expected,
		MappedRecord actual, 
		boolean aspect
	) throws ServiceException{
		String type = expected.getRecordName();
		ModelElement_1_0 classifierDef = this.model.getElement(type);
		if(type.equals(actual.getRecordName())) {
			Set<String> features = new HashSet<String>(expected.keySet());
			features.addAll(actual.keySet());
			for(String feature : features) {
				ModelElement_1_0 featureDef = this.model.getFeatureDef(classifierDef, feature, false);
                Object actualValue = actual.get(feature);
				MappedRecord difference;
				if(featureDef == null) {
				    difference = ObjectAndValidStateComparator.newDifference(
		                xri, 
		                feature, 
		                null, 
		                "The attribute '" + feature + "' is not a modelled feature", 
		                null, 
		                actualValue
		            );
                } else {
    				Object expectedValue = expected.get(feature);
    				String qualifiedName = (String) featureDef.objGetValue("qualifiedName"); 
    				difference = getFeatureComparator(
    					qualifiedName,
    					aspect
    				).compare(
    					xri, 
    					feature, 
    					ModelHelper.getMultiplicity(featureDef), 
    					expectedValue, 
    					actualValue
    				);
				}
				if(difference != null){
					this.differences.add(difference);
				}
			}
		} else {
			this.differences.add(
				newDifference(xri, "Object class mismatch", expected, actual)
			);
		}
	}

	/**
     * Convert a value to a string. 
     * <p>
     * Its main function is to convert object ids to XRIs
     * 
     * @param value
     * @param multiplicity
     * 
     * @return a string representation of the value
     * @deprecated Use {@link #toString(Object)} instead
     */
    public static Object toString(
    	Object value, 
    	Multiplicity multiplicity
    ){
        return toString(value);
    }

    /**
	 * Convert a value to a string. 
	 * <p>
	 * Its main function is to convert object ids to XRIs
	 * 
	 * @param value
	 * @return a string representation of the value
	 */
	public static Object toString(
		Object value
	){
		if(value instanceof Path) {
			return ((Path)value).toXRI();
		} else if(value instanceof Collection) {
			Collection<Object> target = new ArrayList<Object>();
			for(Object source : (Collection<?>)value) {
				target.add(toString(source));
			}
			return target;
		} else {
			return value;
		}
	}

	/**
	 * Create an object difference entry
	 * 
	 * @param id
	 * @param text
	 * @param expected
	 * @param actual
	 * 
	 * @return a new difference record
	 */
	public static MappedRecord newDifference(
		Path id,
		String text,
		MappedRecord expected,
		MappedRecord actual
	){
		return Records.getRecordFactory().asMappedRecord(
			id.toXRI(),
			text,
			new Object[] {"expected", "actual"},
			new Object[]{
				expected,
				actual
			}
		);
	}

	/**
	 * Create a state difference entry
	 * 
	 * @param id
	 * @param interval
	 * @param text
	 * @param expected
	 * @param actual
	 * 
	 * @return a new difference record
	 */
	private static MappedRecord newDifference(
		Path id,
		Interval interval,
		String text,
		MappedRecord expected,
		MappedRecord actual
	){
		return Records.getRecordFactory().asMappedRecord(
			id.toXRI() + "?time-range=" + interval,
			text,
			new Object[] {"interval", "expected", "actual"},
			new Object[]{
				interval,
				expected,
				actual
			}
		);
	}

	/**
	 * Create a feature difference entry
	 * 
	 * @param id
	 * @param feature
	 * @param multiplicity
	 * @param text
	 * @param expected
	 * @param actual
	 * 
	 * @return a new difference record
	 */
	public static MappedRecord newDifference(
		Path id,
		String feature,
		Multiplicity multiplicity,
		String text,
		Object expected,
		Object actual
	){
	    boolean unknownFeature = multiplicity == null;
		return Records.getRecordFactory().asMappedRecord(
			id.toXRI() + '#' + feature,
			text,
			unknownFeature ? new Object[]{
                "feature", 
                "actual"
			} : new Object[] {
			    "multiplicity", 
			    "feature", 
			    "expected", 
			    "actual"
			},
			unknownFeature ? new Object[]{
                feature,
                toString(actual)
            } : new Object[]{
				multiplicity,
				feature,
				toString(expected),
				toString(actual)
			}
		);
	}
	
	
	//------------------------------------------------------------------------
	// Class Aspects
	//------------------------------------------------------------------------

	/**
	 * Aspects 
	 */
	static class Aspects {
		
		final List<MappedRecord> expected = new ArrayList<MappedRecord>();
		final List<MappedRecord> actual = new ArrayList<MappedRecord>();
		
	}

	
	//------------------------------------------------------------------------
	// Class Interval
	//------------------------------------------------------------------------

	/**
	 * A splittable Interval
	 */
	static class Interval {

		/**
		 * Constructor
		 * 
		 * @param validFrom
		 * @param validTo
		 */
		Interval(
			XMLGregorianCalendar validFrom, 
			XMLGregorianCalendar validTo
		) {
			this.validFrom = validFrom;
			this.validTo = validTo;
		}
		
		/**
		 * The (modifiable) start date
		 */
		XMLGregorianCalendar validFrom;
		
		/**
		 * The (modifiable) end date
		 */
		XMLGregorianCalendar validTo;
	
		@Override
		public String toString(
		){
			StringBuilder id = new StringBuilder();
            if(validFrom == null) {
                id.append("(-\u221E");
            } else {
                id.append(
                    '['
                ).append(
            		validFrom.toXMLFormat()
                );
            }
            id.append(',');
            if(validFrom == null) {
                id.append("\u221E)");
            } else {
                id.append(
            		validTo.toXMLFormat()
                ).append(
                    ']'
                );
            }
			return id.toString();
		}
		
	}
	
}
