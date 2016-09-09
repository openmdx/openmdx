/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Abstract Filter Class
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2011, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.spi;

import java.util.Iterator;
import java.util.List;

import org.openmdx.application.dataprovider.cci.FilterProperty;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.LenientPathComparator;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.query.Selector;
import org.openmdx.base.query.spi.AbstractPattern;
import org.openmdx.base.query.spi.PathPattern;
import org.openmdx.base.query.spi.Soundex;
import org.openmdx.base.resource.Records;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;


/**
 * FilterProperty based Filter
 */
public abstract class AbstractFilter implements Selector {

    /**
     * Constructor
     * 
     * @param filter
     * 
     * @exception   IllegalArgumentException
     *              in case of an invalid filter property set
     * @exception   NullPointerException
     *              if the filter is <code>null</code>
     */
    protected AbstractFilter(
        FilterProperty[] filter
    ){
        this.filter = filter;
        for (
            int i = 0;
            i < this.filter.length;
            i++
        ) try {
            short operator = filter[i].operator();
            if(
                operator == ConditionType.IS_LIKE.code() ||
                operator == ConditionType.IS_UNLIKE.code()
            ){
                if(this.pattern == null) {
                    this.pattern = new AbstractPattern[filter.length];
                }
                Object value = filter[i].getValue(0);
                this.pattern[i] = AbstractPattern.newInstance(value);
            }
        } 
        catch (IllegalArgumentException exception) {
            throw BasicException.initHolder(
                new IllegalArgumentException(
                    "Invalid filter property",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("filterProperty",filter[i])
                    )
                )
            );
        }
    }

    /**
     * 
     * @param candidate
     * @param attribute
     * @return an iterator for the values, never <code>null</code>
     * 
     * @exception   Exception
     *              in case of failure
     */
    protected abstract Iterator<?> getValuesIterator(
        Object candidate,
        String attribute
    ) throws Exception;

    /**
     * 
     * @param candidate
     * @param attribute
     * @return an iterator for the values, never <code>null</code>
     * 
     * @exception   Exception
     *              in case of failure
     */
    protected abstract Iterator<?> getObjectIterator(
        Object candidate,
        String attribute
    ) throws Exception;
    
    /**
     * 
     */
    protected FilterProperty[] filter;

    /**
     * 
     */
    private AbstractPattern[] pattern;

    /**
     * Test two values for equality in the context of a filter
     * 
     * @param candidate the candidate to be compared with the filter value
     * @param filterValue the filter value
     * 
     * @return <code>true</code> if the two values are considered to be equal 
     * in the context of a filter
     */
    protected boolean equal(
        Object candidate,
        Object filterValue
    ){
        return 
            LenientPathComparator.isComparable(candidate) ? compare(candidate,filterValue) == 0 :
            candidate.equals(filterValue);
    }

    /**
     * Compare two values in the context of a filter
     * 
     * @param candidate the candidate to be compared with the filter value
     * @param filterValue the filter value
     * 
     * @return the result of the comparisom
     */
    protected int compare(
        Object candidate,
        Object filterValue
    ){
        return LenientPathComparator.getInstance().compare(candidate, filterValue);
    }
    
    /**
     * 
     */
    public FilterProperty[] getDelegate(
    ){
        return this.filter;
    }


    //------------------------------------------------------------------------
    // Implements Selector 
    //------------------------------------------------------------------------

    private boolean isComplex(
        FilterProperty predicate
    ){
        List<?> values = predicate.values();
        return !values.isEmpty() && values.get(0) instanceof QueryFilterRecord;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.query.Selector#accept(java.lang.Object)
     */
    public boolean accept(
        Object candidate
    ){
        Properties: for (
            int propertyIndex = 0;
            propertyIndex < this.filter.length;
            propertyIndex++
        ){
            FilterProperty property = this.filter[propertyIndex];
            Quantifier quantifier = Quantifier.valueOf(property.quantor());
            Iterator<?> iterator;
            try {                
                iterator = isComplex(property) ? getObjectIterator(candidate, property.name()) : getValuesIterator(candidate, property.name());
            } catch (Exception exception) {
                Throwables.log(exception);
                return false;
            }
            while (iterator.hasNext()){
                Object raw = iterator.next();
                switch(ConditionType.valueOf(property.operator())){

                    case IS_UNLIKE: {
                        if (!matches(propertyIndex, raw)){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    case IS_LIKE: {
                        if (matches(propertyIndex, raw)){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    case IS_OUTSIDE: {
                        if(
                            compare(raw,property.getValue(0)) < 0 ||
                            compare(raw,property.getValue(1)) > 0
                        ){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    case IS_BETWEEN: {
                        if(
                            compare(raw,property.getValue(0)) >= 0 &&
                            compare(raw,property.getValue(1)) <= 0
                        ){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    case IS_LESS_OR_EQUAL: {                
                        if(
                            compare(raw,property.getValue(0)) <= 0
                        ){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    case IS_GREATER: {
                        if(
                            compare(raw,property.getValue(0)) > 0
                        ){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    case IS_LESS: {
                        if(
                            compare(raw,property.getValue(0)) < 0
                        ){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    case IS_GREATER_OR_EQUAL: {
                        if(
                            compare(raw,property.getValue(0)) >= 0
                        ){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    case IS_NOT_IN: {
                        boolean test = true;
                        IsNotIn: for(
                            int setIndex = 0, setSize = property.getValues().length;
                            setIndex < setSize;
                            setIndex++
                        ) {
                            if(equal(raw, property.getValue(setIndex))) {
                                test = false;
                                break IsNotIn;
                            }
                        }
                        if(test){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    case IS_IN: {
                        boolean test = false;
                        IsIn: for(
                            int setIndex = 0, setSize = property.getValues().length;
                            setIndex < setSize;
                            setIndex++
                        ) {
                            if(equal(raw, property.getValue(setIndex))) {
                                test = true;
                                break IsIn;
                            }
                        }
                        if(test){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    case SOUNDS_LIKE: {
                        boolean test = false;
                        final Soundex soundex = Soundex.getInstance();
						String encoded = soundex.encode((String)raw);
                        SoundsLike: for(
                            int setIndex = 0, setSize = property.getValues().length;
                            setIndex < setSize;
                            setIndex++
                        ){
                            if(encoded.equals(soundex.encode((String)property.getValue(setIndex)))) {
                                test = true;
                                break SoundsLike;
                            }
                        } 
                        if(test){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    case SOUNDS_UNLIKE: {
                        boolean test = true;
                        final Soundex soundex = Soundex.getInstance();
						String encoded = soundex.encode((String)raw);
                        SoundsUnlike: for(
                            int setIndex = 0, setSize = property.getValues().length;
                            setIndex < setSize;
                            setIndex++
                        ) {
                            if(encoded.equals(soundex.encode((String)property.getValue(setIndex)))) {
                                test = false;
                                break SoundsUnlike;
                            }
                        }
                        if(test){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    default: throw BasicException.initHolder( 
                        new IllegalArgumentException(
                            "Unsupported operator",
                            BasicException.newEmbeddedExceptionStack(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.BAD_PARAMETER,
                                new BasicException.Parameter("operator", ConditionType.valueOf(property.operator()))
                            )
                        )
                    );                
                }
            }
            if(quantifier == Quantifier.THERE_EXISTS) return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return Records.getRecordFactory().asIndexedRecord(
		    getClass().getName(), 
		    null, 
		    this.filter
		).toString();
    }

    public int size(){
        return this.filter.length;
    }

    /**
     * PathPattern aware match method
     * 
     * @param index
     * @param value
     * 
     * @return
     */
    private boolean matches (
        int index,
        Object value
    ){        
        if(value instanceof Path){
            if(!(this.pattern[index] instanceof PathPattern)) try {
                this.pattern[index] = PathPattern.newInstance(this.pattern[index].pattern());
            } catch (Exception exception) {
                return false;
            }
            return ((PathPattern)this.pattern[index]).matches((Path)value);
        } else {
            return this.pattern[index].matches(value.toString());
        }
    }

}
