/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RefQuery_1.java,v 1.36 2010/04/16 18:24:20 hburger Exp $
 * Description: RefQuery_1 class
 * Revision:    $Revision: 1.36 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/16 18:24:20 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.jmi.spi;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import javax.jdo.Extent;
import javax.jdo.FetchPlan;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jmi.reflect.JmiException;
import javax.jmi.reflect.RefObject;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.oasisopen.jmi1.RefContainer;
import org.omg.mof.spi.Names;
import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefQuery_1_0;
import org.openmdx.base.accessor.spi.URIMarshaller;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.ExtentCollection;
import org.openmdx.base.persistence.spi.StandardFetchPlan;
import org.openmdx.base.query.AnyTypeCondition;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.Directions;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.IsInCondition;
import org.openmdx.base.query.IsLikeCondition;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.query.Orders;
import org.openmdx.base.query.PiggyBackCondition;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.query.Quantors;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.log.SysLog;
import org.w3c.cci2.AnyTypePredicate;
import org.w3c.cci2.BooleanTypePredicate;
import org.w3c.cci2.ComparableTypePredicate;
import org.w3c.cci2.MatchableTypePredicate;
import org.w3c.cci2.MultivaluedFeaturePredicate;
import org.w3c.cci2.MultivaluedTypeOrder;
import org.w3c.cci2.OptionalFeaturePredicate;
import org.w3c.cci2.PartiallyOrderedTypePredicate;
import org.w3c.cci2.SimpleTypeOrder;
import org.w3c.cci2.StringTypePredicate;

/**
 * RefQuery_1_0 implementation
 * <p>
 * TODO handle reference to a PersistenceManager
 */
public class RefQuery_1 implements RefQuery_1_0 {

    //-------------------------------------------------------------------------
    protected RefQuery_1(
        org.openmdx.base.query.Filter filter,
        Mapping_1_0 mapping,
        String filterType, 
        boolean subclasses
    ) {
        this.filter = filter == null ? new Filter() : filter;
        this.filterType = filterType;
        this.filter.addCondition(
            this.filterTypeCondition = new IsInCondition(
                Quantors.THERE_EXISTS,
                subclasses ? SystemAttributes.OBJECT_INSTANCE_OF : SystemAttributes.OBJECT_CLASS,
                true,
                filterType
            )
        );
        this.mapping = mapping;
    }

    //-----------------------------------------------------------------------
    public abstract class RefPredicate implements AnyTypePredicate {

        public RefPredicate(
            Short quantor,
            String featureName
        ) {
            this.quantor = quantor;
            this.featureName = featureName;
        }

        public void refAddValue(
            int index,
            short direction
        ) {
            RefQuery_1.this.refAddValue(
                this.featureName,
                0,
                direction
            );
        }
        
        /**
         * Adding value to the filter
         * 
         * @param operator
         * @param operand
         */
        public void refAddValue(
            short quantor,
            short operator,
            Collection<?> operand
        ){
            try {
                RefQuery_1.this.refAddValue(
                    this.featureName,
                    quantor,
                    operator,
                    operand
                );
            } catch (NullPointerException exception) {
                throw new JmiServiceException(
                    new ServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        "Either 'null operand' or 'candidate collection support not implemented for openMDX 1 compatibility mode'",
                        new BasicException.Parameter(
                            "quantor", 
                            Quantors.toString(quantor)
                        ),
                        new BasicException.Parameter(
                            "name", 
                            this.featureName
                        ),
                        new BasicException.Parameter(
                            "operator",
                            FilterOperators.toString(operator)
                        ),
                        operand == null ? new BasicException.Parameter(
                            "operand", 
                            "null"
                        ) : new BasicException.Parameter(
                            "operand.size()", 
                            operand.size()
                        )
                    )
                );
            }
        }

        public RefQuery_1 getQuery(
        ) {
            return RefQuery_1.this;
        }
        
        /* (non-Javadoc)
         * @see org.w3c.cci2.AnyTypePredicate#equalTo(V)
         */
        public void equalTo(
            Object operand
        ) {
            this.elementOf(
                Collections.singleton(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.AnyTypePredicate#elementOf(V...)
         */
        public void elementOf(
            Object... operand
        ) {
            this.elementOf(
                Arrays.asList(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.AnyTypePredicate#elementOf(Collection<V>)
         */
        public void elementOf(
            Collection<?> operand
        ) {
            if(operand instanceof ExtentCollection<?>) {
                Path pattern = ((ExtentCollection<?>)operand).getPattern();
                this.refAddValue(
                    this.quantor,
                    pattern.containsWildcard() ? FilterOperators.IS_LIKE : FilterOperators.IS_IN,
                    Collections.singleton(pattern)
                );
            } else {
                this.refAddValue(
                    this.quantor,
                    FilterOperators.IS_IN,
                    operand
                );
            }
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.AnyTypePredicate#notEqual(V)
         */
        public void notEqualTo(
            Object operand
        ) {
            this.notAnElementOf(
                Collections.singleton(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.AnyTypePredicate#notAnElementOf(V...)
         */
        public void notAnElementOf(
            Object... operand
        ) {
            this.notAnElementOf(
                Arrays.asList(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.AnyTypePredicate#notAnElementOf(Collection<V>)
         */
        public void notAnElementOf(
            Collection<?> operand
        ) {
            if(operand instanceof ExtentCollection<?>) {
                Path pattern = ((ExtentCollection<?>)operand).getPattern();
                this.refAddValue(
                    this.quantor,
                    pattern.containsWildcard() ? FilterOperators.IS_UNLIKE : FilterOperators.IS_NOT_IN,
                    Collections.singleton(pattern)
                );
            } else {
                this.refAddValue(
                    this.quantor,
                    FilterOperators.IS_NOT_IN,
                    operand
                );
            }
        }

        /**
         * Add a wildcard prefix or suffix to the given operands
         * 
         * @param prefix tells whether a prefix or suffix has to be added
         * @param source the original operands
         * 
         * @return the modified operands
         */
        @SuppressWarnings("unchecked")
        Collection jdoWildcard (
            Collection source,
            boolean prefix
        ){
            if (source == null) {
                return Collections.EMPTY_SET;
            } 
            else {
                Collection target = new ArrayList();
                for(
                    Iterator i = source.iterator();
                    i.hasNext();
                ){
                    target.add(
                        prefix ? ".*" + i.next() : i.next().toString() + ".*"
                    );
                }
                return target;
            }
        }
        
        //-----------------------------------------------------------------------
        public String getFeatureName(
        ) {
            return this.featureName;
        }
        
        //-----------------------------------------------------------------------
        // Members
        //-----------------------------------------------------------------------
        private static final long serialVersionUID = -5668618898901781191L;

        protected final Short quantor;
        protected final String featureName;

    }

    //-------------------------------------------------------------------------
    public class RefObjectTypePredicate extends RefPredicate {
        
        public RefObjectTypePredicate(
            short quantor,
            String featureName
        ) {
            super(quantor, featureName);
        }
        
    }
    
    //-------------------------------------------------------------------------
    public class RefBooleanTypePredicate
        extends RefPredicate
        implements BooleanTypePredicate {

        public RefBooleanTypePredicate(
            short quantor,
            String featureName
        ) {
            super(quantor, featureName);
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.BooleanValuePredicate#equalTo()
         */
        public void equalTo(
            boolean operand
        ) {
            super.refAddValue(
                this.quantor,
                FilterOperators.IS_IN,
                operand ? TRUE : FALSE
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.BooleanTypePredicate#isFalse()
         */
        public void isFalse() {
            equalTo(false);
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.BooleanTypePredicate#isTrue()
         */
        public void isTrue() {
            equalTo(true);
        }

    }
    
    //-------------------------------------------------------------------------
    public class RefSimpleTypePredicate
        extends RefPredicate
        implements AnyTypePredicate {

        public RefSimpleTypePredicate (
            short quantor,
            String featureName
        ){
            super(quantor, featureName);
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.AnyTypePredicate#equalTo(V)
         */
        public void equalTo(
            Object operand
        ) {
            elementOf(
                Collections.singleton(operand)
            );
        }
    
        /* (non-Javadoc)
         * @see org.w3c.cci2.AnyTypePredicate#elementOf(V...)
         */
        public void elementOf(
            Object... operand
        ) {
            elementOf(
                Arrays.asList(operand)
            );
        }
    
        /* (non-Javadoc)
         * @see org.w3c.cci2.AnyTypePredicate#elementOf(Collection<V>)
         */
        public void elementOf(
            Collection<?> operand
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_IN,
                operand
            );
        }
    
        /* (non-Javadoc)
         * @see org.w3c.cci2.AnyTypePredicate#notEqual(V)
         */
        public void notEqualTo(
            Object operand
        ) {
            this.notAnElementOf(
                Collections.singleton(operand)
            );
        }
    
        /* (non-Javadoc)
         * @see org.w3c.cci2.AnyTypePredicate#notAnElementOf(V...)
         */
        public void notAnElementOf(
            Object... operand
        ) {
            this.notAnElementOf(
                Arrays.asList(operand)
            );
        }
    
        /* (non-Javadoc)
         * @see org.w3c.cci2.AnyTypePredicate#notAnElementOf(Collection<V>)
         */
        public void notAnElementOf(
            Collection<?> operand
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_NOT_IN,
                operand
            );
        }
    
    }

    //-------------------------------------------------------------------------
    public class RefComparableTypePredicate<V extends Comparable<?>> 
        extends RefSimpleTypePredicate 
        implements ComparableTypePredicate<V> {
    
        public RefComparableTypePredicate (
            short quantor,
            String featureName
        ) {
            super(quantor, featureName);
        }
    
        /* (non-Javadoc)
         * @see org.w3c.cci2.ComparableTypePredicate#between(V, V)
         */
        public void between(
            V lowerBound,
            V upperBound
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_BETWEEN,
                Arrays.asList(
                    new Comparable[]{lowerBound, upperBound}
                )
            );
        }
    
        /* (non-Javadoc)
         * @see org.w3c.cci2.ComparableTypePredicate#outside(V, V)
         */
        public void outside(
            V lowerBound,
            V upperBound
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_OUTSIDE,
                Arrays.asList(
                    new Comparable[]{lowerBound, upperBound}
                )
            );
        }
    
        /* (non-Javadoc)
         * @see org.w3c.cci2.ComparableTypePredicate#lessThan(V)
         */
        public void lessThan(
            V operand
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_LESS,
                Collections.singleton(operand)
            );
        }
    
        /* (non-Javadoc)
         * @see org.w3c.cci2.ComparableTypePredicate#lessThanOrEqual(V)
         */
        public void lessThanOrEqualTo(
            V operand
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_LESS_OR_EQUAL,
                Collections.singleton(operand)
            );
        }
    
        /* (non-Javadoc)
         * @see org.w3c.cci2.ComparableTypePredicate#greaterThanOrEqual(V)
         */
        public void greaterThanOrEqualTo(
            V operand
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_GREATER_OR_EQUAL,
                Collections.singleton(operand)
            );
        }
    
        /* (non-Javadoc)
         * @see org.w3c.cci2.ComparableTypePredicate#greaterThan(V)
         */
        public void greaterThan(
            V operand
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_GREATER,
                Collections.singleton(operand)
            );
        }
    
    }
    
    //-------------------------------------------------------------------------
    public class RefStringTypePredicate
        extends RefComparableTypePredicate<String>
        implements StringTypePredicate {

        RefStringTypePredicate (
            short quantor,
            String featureName
        ) {
            super(quantor, featureName);
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.StringTypePredicate#like(java.lang.String)
         */
        public void like(
            String operand
        ) {
            this.like(
                Collections.singleton(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.StringTypePredicate#like(java.lang.String...)
         */
        public void like(
            String... operand
        ) {
            this.like(
                Arrays.asList(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.StringTypePredicate#like(Collection<String>)
         */
        public void like(
            Collection<String> operand
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_LIKE,
                operand
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.StringTypePredicate#unlike(java.lang.String)
         */
        public void unlike(
            String operand
        ) {
            this.unlike(
                Collections.singleton(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.StringTypePredicate#unlike(java.lang.String...)
         */
        public void unlike(
            String... operand
        ) {
            unlike(
                Arrays.asList(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.StringTypePredicate#unlike(Collection<String>)
         */
        public void unlike(
            Collection<String> operand
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_UNLIKE,
                operand
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.StringTypePredicate#like(int,java.lang.String)
         */
        public void like(
            int flags,
            String operand
        ) {
            this.like(
                flags,
                Collections.singleton(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.StringTypePredicate#like(java.lang.String...)
         */
        public void like(
            int flags,
            String... operand
        ) {
            this.like(
                flags,
                Arrays.asList(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.StringTypePredicate#like(Collection<String>)
         */
        public void like(
            int flags,
            Collection<String> operand
        ) {
            if(flags == 0) {
                this.refAddValue(
                    this.quantor,                    
                    FilterOperators.IS_LIKE,
                    operand
                );
            } 
            else if(flags == SOUNDS) {
                this.refAddValue(
                    this.quantor,
                    FilterOperators.SOUNDS_LIKE,
                    operand
                );
            } 
            else throw new IllegalArgumentException(
                "No other flag than SOUNDS is supported in compatibility mode"
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.StringTypePredicate#unlike(java.lang.String)
         */
        public void unlike(
            int flags,
            String operand
         ) {
            this.unlike(
                flags,
                Collections.singleton(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.ci2.StringTypePredicate#unlike(java.lang.String...)
         */
        public void unlike(
            int flags,
            String... operand
        ) {
            this.unlike(
                flags,
                Arrays.asList(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.StringTypePredicate#unlike(Collection<String>)
         */
        public void unlike(
            int flags,
            Collection<String> operand
        ) {
            if(flags == 0) {
                this.refAddValue(
                    this.quantor,
                    FilterOperators.IS_UNLIKE,
                    operand
                );
            } 
            else if(flags == SOUNDS) {
                this.refAddValue(
                    this.quantor,
                    FilterOperators.SOUNDS_UNLIKE,
                    operand
                );
            } 
            else throw new IllegalArgumentException(
                "No other flag than SOUNDS is supported in compatibility mode"
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#endsWith(java.lang.Object)
         */
        public void endsWith(
            String operand
        ) {
            this.endsWith(
                Collections.singleton(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#endsWith(V[])
         */
        public void endsWith(
            String... operand
        ) {
            this.endsWith(
                Arrays.asList(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#endsWith(java.util.Collection)
         */
        public void endsWith(
            Collection<String> operand
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_LIKE,
                jdoWildcard(operand, true)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#startsWith(java.lang.Object)
         */
        public void startsWith(
            String operand
        ) {
            this.startsWith(
                Collections.singleton(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#startsWith(V[])
         */
        public void startsWith(
            String... operand
        ) {
            this.startsWith(
                Arrays.asList(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#startsWith(java.util.Collection)
         */
        public void startsWith(
            Collection<String> operand
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_LIKE,
                this.jdoWildcard(operand, false)
            );
        }
    
        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#endsNotWith(java.lang.Object)
         */
        public void endsNotWith(
            String operand
        ) {
            this.endsNotWith(
                Collections.singleton(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#endsNotWith(V[])
         */
        public void endsNotWith(
            String... operand
        ) {
            this.endsNotWith(
                Arrays.asList(operand)
            );
        }
    
        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#endsNotWith(java.util.Collection)
         */
        public void endsNotWith(
            Collection<String> operand
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_UNLIKE,
                this.jdoWildcard(operand, true)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#startsNotWith(java.lang.Object)
         */
        public void startsNotWith(
            String operand
        ) {
            this.startsNotWith(
                Collections.singleton(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#startsNotWith(V[])
         */
        public void startsNotWith(
            String... operand
        ) {
            this.startsNotWith(
                Arrays.asList(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#startsNotWith(java.util.Collection)
         */
        public void startsNotWith(
            Collection<String> operand
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_UNLIKE,
                this.jdoWildcard(operand, false)
            );
        }

    }
    
    //-------------------------------------------------------------------------
    public class RefPartiallyOrderedTypePredicate<V>
        extends RefSimpleTypePredicate
        implements PartiallyOrderedTypePredicate<V>
    {

        public RefPartiallyOrderedTypePredicate (
            short quantor,
            String featureName
        ){
            super(quantor, featureName);
        }

        /* (non-Javadoc)
         * @see org.w3c.query.PartiallyOrderedTypePredicate#between(V, V)
         */
        @SuppressWarnings("unchecked")
        public void between(
            V lowerBound,
            V upperBound
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_BETWEEN,
                Arrays.asList(
                    lowerBound, 
                    upperBound
                )
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.query.PartiallyOrderedTypePredicate#outside(V, V)
         */
        @SuppressWarnings("unchecked")
        public void outside(
            V lowerBound,
            V upperBound
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_OUTSIDE,
                Arrays.asList(
                    lowerBound, 
                    upperBound
                )
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.query.PartiallyOrderedTypePredicate#lessThan(V)
         */
        public void lessThan(
            V operand
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_LESS,
                Collections.singleton(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.query.PartiallyOrderedTypePredicate#lessThanOrEqual(V)
         */
        public void lessThanOrEqualTo(
            V operand
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_LESS_OR_EQUAL,
                Collections.singleton(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.query.PartiallyOrderedTypePredicate#greaterThanOrEqual(V)
         */
        public void greaterThanOrEqualTo(
            V operand
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_GREATER_OR_EQUAL,
                Collections.singleton(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.query.PartiallyOrderedTypePredicate#greaterThan(V)
         */
        public void greaterThan(
            V operand
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_GREATER,
                Collections.singleton(operand)
            );
        }

    }
    
    //-------------------------------------------------------------------------
    public class RefOptionalFeaturePredicate
        extends RefPredicate
        implements OptionalFeaturePredicate {

        public RefOptionalFeaturePredicate (
            short quantor,
            String featureName
        ) {
            super(quantor, featureName);
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.OptionalAttributePredicate#isNull()
         */
        public void isNull(
        ) {
            this.refAddValue(
                Quantors.FOR_ALL,
                FilterOperators.IS_IN,
                Collections.EMPTY_SET
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.OptionalAttributePredicate#isNonNull()
         */
        public void isNonNull(
        ) {
            this.refAddValue(
                Quantors.THERE_EXISTS,
                FilterOperators.IS_NOT_IN,
                Collections.EMPTY_SET
            );
        }

    }
    
    //-------------------------------------------------------------------------
    public class RefMultiValuedAttributePredicate 
        extends RefPredicate
        implements MultivaluedFeaturePredicate {

        public RefMultiValuedAttributePredicate (
            Short quantor,
            String featureName
        ) {
            super(quantor, featureName);
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MultiValuedAttributePredicate#isEmpty()
         */
        public void isEmpty(
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_IN,
                Collections.EMPTY_SET
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MultiValuedAttributePredicate#isNonEmpty()
         */
        public void isNonEmpty() {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_NOT_IN,
                Collections.EMPTY_SET
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MultivaluedFeaturePredicate#size()
         */
        public ComparableTypePredicate<Integer> size() {
            throw new UnsupportedOperationException(
                "The only collection size predicates supported in compatibility mode are isEmpty() and isNonEmty()"
            );
        }

    }
    
    //-------------------------------------------------------------------------
    public class RefResourceIdentifierTypePredicate<V extends Comparable<?>>
        extends RefComparableTypePredicate<V>
        implements MatchableTypePredicate<V> {

        public RefResourceIdentifierTypePredicate (
            short quantor,
            String featureName
        ) {
            super(quantor, featureName);
        }
    
        /* (non-Javadoc)
         * @see org.w3c.query.ResourceIdentifierTypePredicate#like(V)
         */
        public void like(
            V operand
        ) {
            this.like(
                Collections.singleton(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.query.ResourceIdentifierTypePredicate#like(V...)
         */
        public void like(
            V... operand
        ) {
            this.like(
                Arrays.asList(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.query.ResourceIdentifierTypePredicate#like(Collection<V>)
         */
        public void like(
            Collection<V> operand
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_LIKE,
                operand
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.query.ResourceIdentifierTypePredicate#unlike(V)
         */
        public void unlike(
            V operand
        ) {
            this.unlike(
                Collections.singleton(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.query.ResourceIdentifierTypePredicate#unlike(V...)
         */
        public void unlike(
            V... operand
        ) {
            this.unlike(
                Arrays.asList(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.query.ResourceIdentifierTypePredicate#unlike(Collection<V>)
         */
        public void unlike(
            Collection<V> operand
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_UNLIKE,
                operand
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#endsWith(java.lang.Object)
         */
        public void endsWith(
            V operand
        ) {
            this.endsWith(
                Collections.singleton(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#endsWith(V[])
         */
        public void endsWith(
            V... operand
        ) {
            this.endsWith(
                Arrays.asList(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#endsWith(java.util.Collection)
         */
        public void endsWith(
            Collection<V> operand
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_LIKE,
                this.jdoWildcard(operand, true)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#startsWith(java.lang.Object)
         */
        public void startsWith(
            V operand
        ) {
            this.startsWith(
                Collections.singleton(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#startsWith(V[])
         */
        public void startsWith(
            V... operand
        ) {
            this.startsWith(
                Arrays.asList(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#startsWith(java.util.Collection)
         */
        public void startsWith(
            Collection<V> operand
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_LIKE,
                this.jdoWildcard(operand, false)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#endsNotWith(java.lang.Object)
         */
        public void endsNotWith(
            V operand
        ) {
            this.endsNotWith(
                Collections.singleton(operand)
            );
        }
    
        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#endsNotWith(V[])
         */
        public void endsNotWith(
            V... operand
        ) {
            this.endsNotWith(
                Arrays.asList(operand)
            );
        }
    
        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#endsNotWith(java.util.Collection)
         */
        public void endsNotWith(
            Collection<V> operand
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_UNLIKE,
                this.jdoWildcard(operand, true)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#startsNotWith(java.lang.Object)
         */
        public void startsNotWith(
            V operand
        ) {
            this.startsNotWith(
                Collections.singleton(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#startsNotWith(V[])
         */
        public void startsNotWith(
            V... operand
        ) {
            this.startsNotWith(
                Arrays.asList(operand)
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MatchableTypePredicate#startsNotWith(java.util.Collection)
         */
        public void startsNotWith(
            Collection<V> operand
        ) {
            this.refAddValue(
                this.quantor,
                FilterOperators.IS_UNLIKE,
                this.jdoWildcard(operand, false)
            );
        }

    }
    
    //-------------------------------------------------------------------------
    /**
     * SimpleTypeOrder_1
     */
    public class RefSimpleTypeOrder
        extends RefPredicate
        implements SimpleTypeOrder {

        public RefSimpleTypeOrder(
            String featureName
        ) {
            super(null, featureName);
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.SimpleTypeOrder#ascending()
         */
        public void ascending(
        ) {
            this.refAddValue(
                0,
                Directions.ASCENDING
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.SimpleTypeOrder#descending()
         */
        public void descending(
        ) {
            this.refAddValue(
                0,
                Directions.DESCENDING
            );
        }

    }
    
    //-------------------------------------------------------------------------
    public class RefMultivaluedTypeOrder
        extends RefPredicate
        implements MultivaluedTypeOrder {

        public RefMultivaluedTypeOrder(
            String featureName
        ) {
            super(null, featureName);
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MultivaluedTypeOrder#ascending(int)
         */
        public void ascending(
            int index
        ) {
            this.refAddValue(
                index,
                Directions.ASCENDING
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.MultivaluedTypeOrder#descending(int)
         */
        public void descending(
            int index
        ) {
            this.refAddValue(
                index,
                Directions.DESCENDING
            );
        }
        
    }
    
    //-------------------------------------------------------------------------
    protected final Model_1_0 getModel(
    ) {
        return Model_1Factory.getModel();
    }

    // -------------------------------------------------------------------------
    final protected ModelElement_1_0 getFeature(
        String featureName
    ) throws ServiceException {

        // full-qualified feature name. Lookup in model
        if (featureName.indexOf(':') >= 0) {
            return this.getModel().getElement(featureName);
        }
        // get all features of class and find feature with featureName
        else {
            ModelElement_1_0 feature = this.getModel().getFeatureDef(
                this.getModel().getElement(this.filterType),
                featureName,
                false
            );
            if (feature == null) { 
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    "feature not found",
                    new BasicException.Parameter("class name", this.filterType),
                    new BasicException.Parameter("feature", featureName)
                ); 
            }
            return feature;
        }
    }

    //-------------------------------------------------------------------------
    protected void assertAttributeOrReferenceStoredAsAttribute(
        ModelElement_1_0 elementDef
    ) throws ServiceException {
        if(
            !elementDef.objGetClass().equals(ModelAttributes.ATTRIBUTE) &&
            !this.getModel().referenceIsStoredAsAttribute(elementDef)
        ) {
            throw new ServiceException (
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "model element not of type " + ModelAttributes.ATTRIBUTE + " and not " + ModelAttributes.REFERENCE + " stored as attribute",
                new BasicException.Parameter("model element", elementDef)
            );
        }
    }

    //-------------------------------------------------------------------------
    public void refAddValue(
        ModelElement_1_0 featureDef,
        short quantor,
        short operator,
        Collection<?> value
    ) {
        try {
            this.assertModifiable();
            this.assertAttributeOrReferenceStoredAsAttribute(featureDef);
            String featureName = (String)featureDef.objGetValue("name");

            SysLog.trace("feature", featureName);
            SysLog.trace("quantor", Quantors.toString(quantor));
            SysLog.trace("operator", FilterOperators.toString(operator));
            SysLog.trace("value", value);

            if(this.getModel().isReferenceType(featureDef)) {
                if("org:openmdx:base:ContextCapable:context".equals(featureDef.objGetValue("qualifiedName"))) {
                    if(
                        quantor == Quantors.THERE_EXISTS &&
                        operator == FilterOperators.IS_IN 
                    ){
                        Model_1_0 m = getModel();
                        int ii = 0;
                        for(
                            Iterator<?> i = value.iterator();
                            i.hasNext();
                            ii++
                        ) {
                            Object c = i.next();
                            if(c instanceof RefObject_1_0){
                                RefObject_1_0 e = (RefObject_1_0) c;
                                String objectClass = e.refClass().refMofId();
                                String namespace = featureName + ':' + UUIDs.newUUID() + ':';
                                if(m.isSubtypeOf(objectClass, "org:openmdx:base:Context")) {
                                    this.filter.addCondition(
                                        new PiggyBackCondition(
                                            namespace + SystemAttributes.OBJECT_CLASS,
                                            objectClass
                                        )
                                    );
                                    for(
                                        Iterator<String> j = e.refDefaultFetchGroup().iterator();
                                        j.hasNext();
                                    ){
                                        String attribute = j.next();
                                        Object v = e.refGetValue(attribute);
                                        this.filter.addCondition(
                                            new PiggyBackCondition(
                                                namespace + attribute,
                                                v instanceof Collection<?> ? ((Collection<?>)v).toArray() : new Object[]{v}
                                            )
                                        );
                                    }
                                } else throw new ServiceException (
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.ASSERTION_FAILURE,
                                    "Object can't be piggy backed as context unless it is an instance of org::openmdx::base::Context",
                                    new BasicException.Parameter("quantor", Quantors.toString(quantor)),
                                    new BasicException.Parameter("feature", featureName),
                                    new BasicException.Parameter("operator", FilterOperators.toString(operator)),
                                    new BasicException.Parameter("index", ii),
                                    new BasicException.Parameter("class", objectClass)
                                );
                            }
                        }
                    } else throw new ServiceException (
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED,
                        "The context feature supports piggy backing with 'THERE EXISTS context EQUAL TO' clauses only",
                        new BasicException.Parameter("quantor", Quantors.toString(quantor)),
                        new BasicException.Parameter("feature", featureName),
                        new BasicException.Parameter("operator", FilterOperators.toString(operator))
                    );
                } else {
                    List<Path> paths = new ArrayList<Path>();
                    for(
                        Iterator<?> i = value.iterator();
                        i.hasNext();
                    ) {
                        Object v = i.next();
                        if(v instanceof RefObject_1_0){
                            RefObject_1_0 e = (RefObject_1_0) v;
                            String objectClass = e.refClass().refMofId();
                            Model_1_0 m = getModel();
                            if(
                                m.isSubtypeOf(objectClass, "org:openmdx:base:ExtentCapable") &&
                                m.isSubtypeOf(objectClass, "org:openmdx:state2:BasicState") &&
                                JDOHelper.isPersistent(e) &&
                                !JDOHelper.isNew(e) &&
                                !JDOHelper.isDeleted(e)
                            ) try {
                                paths.add(new Path((String)e.refGetValue(SystemAttributes.OBJECT_IDENTITY)));
                            } catch (Exception exception) {
                                paths.add(e.refGetPath());
                            } else {
                                paths.add(e.refGetPath());
                            }
                        } else if (v instanceof Path){
                            paths.add((Path)v);
                        } else {
                            paths.add(new Path((String)v));
                        }
                    }
                    this.filter.addCondition(
                        new AnyTypeCondition(
                            new FilterProperty(
                                quantor,
                                featureName,
                                operator,
                                paths.toArray()
                            )
                        )
                    );
                }   
            } else if(this.getModel().isAttributeType(featureDef)) {
                Object featureType = this.getModel().getElement(featureDef.objGetValue("type")).objGetValue("qualifiedName");
                FilterProperty p = null;
                // anyURI
                if(PrimitiveTypes.ANYURI.equals(featureType)) {
                    p = new FilterProperty(
                        quantor,
                        featureName,
                        operator,
                        unmarshalValues(URIMarshaller.STRING_TO_URI, value)
                    );
                }

                // other primitive types require no unmarshalling
                else {
                    p = new FilterProperty(
                        quantor,
                        featureName,
                        operator,
                        value.toArray()
                    );
                }
                this.filter.addCondition(
                    new AnyTypeCondition(p)
                );
            }

            // unsupported feature type
            else {
                throw new ServiceException (
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "unsupported feature type. Must be [Attribute|Reference]",
                    new BasicException.Parameter("feature", featureName)
                );
            }
        }
        catch(ServiceException e) {
            throw new JmiServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
    public void refAddValue(
        ModelElement_1_0 featureDef,
        int index, 
        short order
    ) {
        try {
            this.assertModifiable();            
            this.assertAttributeOrReferenceStoredAsAttribute(featureDef);
            String name = (String)featureDef.objGetValue("name"); 
            SysLog.log(Level.FINEST, "Order by {0} {1}", name, Orders.toString(order));
            this.filter.addOrderSpecifier(
                new OrderSpecifier(
                    name,
                    order
                )
            );
        }
        catch(ServiceException e) {
            throw new JmiServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
    public void refAddValue(
        String featureName,
        int index, 
        short order
   ) {
        try {
            this.refAddValue(
                this.getFeature(featureName), 
                index,
                order
            );
        } catch (ServiceException e) {
            throw new JmiServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
    public void refAddValue(
        String featureName,
        short quantor,
        short operator,
        Collection<?> value
    ) {
        try {
            if(SystemAttributes.OBJECT_INSTANCE_OF.equals(featureName)) {
                if(
                    quantor != Quantifier.THERE_EXISTS.code() ||
                    operator != FilterOperators.IS_IN
                ){
                    throw new JmiServiceException(
                        null,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ILLEGAL_STATE,
                        SystemAttributes.OBJECT_INSTANCE_OF + "implies FOR_ALL/IS_IN",
                        new BasicException.Parameter(
                            "quantifier", 
                            Quantifier.valueOf(quantor)
                        ),
                        new BasicException.Parameter(
                            "operator",
                            FilterOperators.toString(operator)
                        )
                    );
                } else if(featureName.equals(filterTypeCondition.getFeature())) {
                    Object[] instanceOf = new Object[value.size()];
                    int i = 0;
                    for(Object v : value) {
                        instanceOf[i++] = v instanceof Class<?> ? this.mapping.getModelClassName((Class<?>)v) : (String) v;
                    }
                    this.filterTypeCondition.setValue(instanceOf);
                } else {
                    throw new JmiServiceException(
                        null,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ILLEGAL_STATE,
                        "This filter does not allow subclasses",
                        new BasicException.Parameter(
                            this.filterTypeCondition.getName(), 
                            this.filterTypeCondition.getValue()
                        ),
                        new BasicException.Parameter(
                            featureName, value
                        )
                    );
                }
            } else {
                this.refAddValue(
                    this.getFeature(featureName), 
                    quantor, 
                    operator, 
                    value
                );
            }
        } catch (ServiceException e) {
            throw new JmiServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefFilter_1_0#refAddValue(java.lang.String, short)
     */
    public void refAddValue(
        String featureName, 
        short order
    ) throws JmiException {
        try {
            this.refAddValue(
                this.getFeature(featureName), 
                0,
                order
            );
        } catch (ServiceException e) {
            throw new JmiServiceException(e);
        }
    }
    
    //-------------------------------------------------------------------------
    protected static Object[] unmarshalValues(
        Marshaller marshaller,
        Collection<?> source
    ) throws ServiceException{
        Object[] target = new Object[source.size()];
        int i = 0;
        for(Object value : source) {
            target[i++] = marshaller.unmarshal(value);
        }
        return target;
    }
    
    //-------------------------------------------------------------------------
    public Object refGetOrder(
        String featureName
    ){
        try {
            return refGetOrder(
                this.getFeature(featureName)
            );
        } 
        catch (ServiceException exception) {
            throw new JmiServiceException(exception);
        }
    }

    //-------------------------------------------------------------------------
    public Object refGetOrder(
        ModelElement_1_0 featureDef
    ) throws ServiceException {
        String multiplicity = (String)featureDef.objGetValue("multiplicity");
        String featureName = (String)featureDef.objGetValue("qualifiedName");
        return 
            Multiplicities.SINGLE_VALUE.equals(multiplicity) ||
            Multiplicities.OPTIONAL_VALUE.equals(multiplicity) ? 
                (Object)new RefSimpleTypeOrder(featureName) : 
                    new RefMultivaluedTypeOrder(featureName);
    }

    //-------------------------------------------------------------------------
    public Object refGetPredicate(
        String featureName
    ){
        try {
            return refGetPredicate(
                this.getFeature(featureName)
            );
        } 
        catch (ServiceException exception) {
            throw new JmiServiceException(exception);
        }
    }

    //-------------------------------------------------------------------------
    public Object refGetPredicate(
        ModelElement_1_0 featureDef
    ) throws ServiceException {
        String multiplicity = (String)featureDef.objGetValue("multiplicity");
        String name = (String)featureDef.objGetValue("qualifiedName");
        return Multiplicities.SINGLE_VALUE.equals(multiplicity) ? refGetPredicate(
            Quantors.THERE_EXISTS, // Quantors.FOR_ALL would give the same result but is very inefficient
            featureDef
        ) : Multiplicities.OPTIONAL_VALUE.equals(multiplicity) ? (Object) new RefOptionalFeaturePredicate(
            Quantors.THERE_EXISTS,
            name
        ) : new RefMultiValuedAttributePredicate(
            Quantors.THERE_EXISTS,
            name
        );
    }

    //-------------------------------------------------------------------------
    public Object refGetPredicate(
        short quantor,
        String featureName
    ) {
        try {
            return this.refGetPredicate(
                quantor,
                this.getFeature(featureName)
            );
        } 
        catch (ServiceException exception) {
            throw new JmiServiceException(exception);
        }
    }

    //-------------------------------------------------------------------------
    public Object refGetPredicate(
        short quantor,
        ModelElement_1_0 featureDef
    ){
        try {
            String qualifiedName = (String) featureDef.objGetValue("qualifiedName");
            String name = (String) featureDef.objGetValue("name");
            ModelElement_1_0 typeDef = this.getModel().getElementType(
                featureDef
            );
            String typeName = (String) typeDef.objGetValue("qualifiedName");
            if(this.getModel().isPrimitiveType(typeDef)) {
                return PrimitiveTypes.BOOLEAN.equals(typeName) ? new RefBooleanTypePredicate(
                    quantor,
                    qualifiedName
                ) : PrimitiveTypes.STRING.equals(typeName) ? new RefStringTypePredicate(
                    quantor,
                    qualifiedName
                ) : PrimitiveTypes.DATETIME.equals(typeName) ? new RefComparableTypePredicate<Date>(
                    quantor,
                    qualifiedName
                ) : PrimitiveTypes.DECIMAL.equals(typeName) ? new RefComparableTypePredicate<BigDecimal>(
                    quantor,
                    qualifiedName
                ) : PrimitiveTypes.INTEGER.equals(typeName) ? new RefComparableTypePredicate<BigInteger>(
                    quantor,
                    qualifiedName
                ) : PrimitiveTypes.LONG.equals(typeName) ? new RefComparableTypePredicate<Long>(
                    quantor,
                    qualifiedName
                ) : PrimitiveTypes.SHORT.equals(typeName) ? new RefComparableTypePredicate<Short>(
                    quantor,
                    qualifiedName
                ) : PrimitiveTypes.DATE.equals(typeName) ? new RefPartiallyOrderedTypePredicate<XMLGregorianCalendar>(
                    quantor,
                    qualifiedName
                ) : PrimitiveTypes.ANYURI.equals(typeName) ? new RefResourceIdentifierTypePredicate<URI>(
                    quantor,
                    qualifiedName
                ) : PrimitiveTypes.DURATION.equals(typeName) ? new RefPartiallyOrderedTypePredicate<Duration>(
                    quantor,
                    qualifiedName
                ) : new RefSimpleTypePredicate(
                    quantor,
                    qualifiedName
                ); 
            } else {
                Filter filterValue = null;
                for(Condition condition: this.filter.getCondition()) {
                    if(
                        condition.getFeature().equals(name) && 
                        (condition.getValue().length > 0) && 
                        (condition.getValue(0) instanceof Filter)
                    ) {
                        filterValue = (Filter)condition.getValue(0);
                        break;
                    }
                }
                return this.mapping.getClassMapping(
                    typeName
                ).newQuery(
                    new Jmi1ObjectPredicateInvocationHandler(
                        new RefObjectTypePredicate(
                            quantor,
                            name
                        ),
                        new RefQuery_1(
                            filterValue,
                            this.mapping,
                            typeName, 
                            true // subclasses
                        )
                    )
                );
            }
        } catch (ServiceException exception) {
            throw new JmiServiceException(exception);
        }
    }

    //-------------------------------------------------------------------------
    // RefFilter_1_0
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefFilter_1_0#refMofId()
     */
    public String refMofId() {
        return this.filterType + "Query";
    }

    //-------------------------------------------------------------------------
    public Filter refGetFilter(
    ) {
        return this.filter;
    }

    //-------------------------------------------------------------------------
    public String toString(
    ) {
        return "filter=" + this.filter;
    }

    //------------------------------------------------------------------------
    public FeatureMapper getFeatureMapper(
    ) {
        try {
            ModelElement_1_0 classDef = getModel().getElement(this.filterType);   
            String qualifiedClassName = (String)classDef.objGetValue("qualifiedName");
            FeatureMapper featureMapper = featureMappers.get(qualifiedClassName);
            if(featureMapper == null) {
                Class<?> queryIntf = Classes.getApplicationClass(
                    Names.toClassName(qualifiedClassName, Names.CCI2_PACKAGE_SUFFIX) + "Query"
                );
                FeatureMapper concurrent = featureMappers.putIfAbsent(
                    qualifiedClassName,
                    featureMapper = new FeatureMapper(
                        classDef,
                        queryIntf
                    )
                );
                if(concurrent != null) {
                    featureMapper = concurrent;
                }
            }
            return featureMapper;
        }
        catch(Exception e) {
            throw new JmiServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
    // javax.jdo.Query
    //-------------------------------------------------------------------------

    protected void assertModifiable(){
        if(isUnmodifiable()) {
            throw new JDOUserException("The query is unmodifiable");
        }
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.Query#addExtension(java.lang.String, java.lang.Object)
     */
    public void addExtension(String key, Object value) {
        assertModifiable();
        if(key.startsWith("org.openmdx.")) {
            throw BasicException.initHolder(
                new JDOFatalUserException(
                    "Invalid openMDX extension",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("key", key),
                        new BasicException.Parameter("value", value),
                        new BasicException.Parameter("supported")
                    )
                )
            );
        } 
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#close(java.lang.Object)
     */
    public void close(Object queryResult) {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#closeAll()
     */
    public void closeAll() {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#compile()
     */
    public void compile() {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#declareImports(java.lang.String)
     */
    public void declareImports(String imports) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#declareParameters(java.lang.String)
     */
    public void declareParameters(String parameters) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#declareVariables(java.lang.String)
     */
    public void declareVariables(String variables) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#deletePersistentAll()
     */
    public long deletePersistentAll(
    ) {
        if(this.pcs instanceof RefContainer) {
            RefContainer refContainer = (RefContainer) this.pcs;
            return refContainer.refRemoveAll(this);
        } 
        else if (this.pcs == null) {
            throw new JDOUserException(
                "No candidates set"
            );
        } 
        else {
            throw new JDOUserException(
                "Unsupported candidate class: " + this.pcs.getClass().getName()
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#deletePersistentAll(java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public long deletePersistentAll(Map parameters) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#deletePersistentAll(java.lang.Object[])
     */
    public long deletePersistentAll(Object... parameters) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#execute()
     */
    public Object execute(
    ) {
        if(this.pcs instanceof RefContainer) {
            RefContainer refContainer = (RefContainer) this.pcs;
            List<?> result = refContainer.refGetAll(this);
            if(this.unique) {
                Iterator<?> i = result.iterator();
                return i.hasNext() ? i.next() : null;
            } else {
                return result;
            }
        }
        throw new JDOUserException(
            this.pcs == null ? "No candidates set" : "Unsupported candidate class: " + this.pcs.getClass().getName()
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#execute(java.lang.Object, java.lang.Object, java.lang.Object)
     */
    public Object execute(Object p1, Object p2, Object p3) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#execute(java.lang.Object, java.lang.Object)
     */
    public Object execute(Object p1, Object p2) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#execute(java.lang.Object)
     */
    public Object execute(Object p1) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#executeWithArray(java.lang.Object[])
     */
    public Object executeWithArray(Object... parameters) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#executeWithMap(java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public Object executeWithMap(Map parameters) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#getFetchPlan()
     */
    public FetchPlan getFetchPlan() {
        if(this.fetchPlan == null) {
            this.fetchPlan = StandardFetchPlan.newInstance(getPersistenceManager());
        }
        return this.fetchPlan;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#getIgnoreCache()
     */
    public boolean getIgnoreCache() {
        return false; // ignore-cache is not supported by openMDX
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#getPersistenceManager()
     */
    public PersistenceManager getPersistenceManager() {
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#isUnmodifiable()
     */
    public boolean isUnmodifiable() {
        return this.unmodifiable;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setCandidates(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void setCandidates(Collection pcs) {
        if(pcs instanceof ExtentCollection<?>) {
            ExtentCollection extentCollection = (ExtentCollection) pcs;
            //
            // Apply the pattern olny expecting that the query is based on the extent
            // 
            Path pattern = extentCollection.getPattern();
            this.pcs = (Collection<?>) extentCollection.getExtent().getPersistenceManager().getObjectById(
                pattern.getPrefix(5).getChild("extent")
            );
            this.filter.addCondition(
                new IsLikeCondition(
                    Quantifier.THERE_EXISTS.code(),
                    SystemAttributes.OBJECT_IDENTITY,
                    true,
                    ExtentCollection.toIdentityPattern(pattern)
                )
            );
        } else {
            this.pcs = pcs;
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setCandidates(javax.jdo.Extent)
     */
    @SuppressWarnings("unchecked")
    public void setCandidates(Extent pcs) {
        throw new UnsupportedOperationException(
            "Extent can't be set via JDO query yet"
        );
        
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setClass(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void setClass(Class cls) {
        if(
            !cls.isInterface() ||
            !RefObject.class.isAssignableFrom(cls)
        ) {
            throw new JDOUserException(
                "The JMI interface should be a subclass of RefObject: " + cls.getName()
            );
        }
        try {
            this.filterTypeCondition.setValue(
                this.mapping.getModelClassName(cls)
            );
        } catch (ServiceException exception) {
            throw new JDOUserException(
                "Unable to map " + cls.getName() + " to a model class",
                exception
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setExtensions(java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public void setExtensions(
        Map extensions
    ) {
        assertModifiable();
        this.extensions.clear();
        for(Map.Entry extension : (Set<Map.Entry<?,?>>)extensions.entrySet()) {
            Object key = extension.getKey();
            if(key instanceof String) {
                addExtension((String) key, extension.getValue());
            } else {
                throw new JDOUserException("The extension key must be a non-null String");
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setFilter(java.lang.String)
     */
    public void setFilter(
        String filter
    ) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setGrouping(java.lang.String)
     */
    public void setGrouping(
        String group
    ) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setIgnoreCache(boolean)
     */
    public void setIgnoreCache(
        boolean ignoreCache
    ) {
        if(ignoreCache) {
            throw new javax.jdo.JDOUnsupportedOptionException("Ignore cache is not supported by openMDX");
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setOrdering(java.lang.String)
     */
    public void setOrdering(String ordering) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setRange(long, long)
     */
    public void setRange(
        long fromIncl, 
        long toExcl
    ) {
        throw new UnsupportedOperationException("set range operations are not supported. Use listIterator(position) instead");
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setRange(java.lang.String)
     */
    public void setRange(String fromInclToExcl) {
        throw new UnsupportedOperationException("set range operations are not supported. Use listIterator(position) instead");
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setResult(java.lang.String)
     */
    public void setResult(String data) {
        throw new UnsupportedOperationException("Result classes, projections and aggregate function results not supported");
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setResultClass(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public void setResultClass(Class cls) {
        throw new UnsupportedOperationException("Result classes, projections and aggregate function results not supported");
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setUnique(boolean)
     */
    public void setUnique(boolean unique) {
        assertModifiable();
        this.unique = unique;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setUnmodifiable()
     */
    public void setUnmodifiable() {
        this.unmodifiable = true;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#addSubquery(javax.jdo.Query, java.lang.String, java.lang.String)
     */
    public void addSubquery(Query arg0, String arg1, String arg2) {
        throw new UnsupportedOperationException("Operation not supported by RefQuery_1");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#addSubquery(javax.jdo.Query, java.lang.String, java.lang.String, java.lang.String)
     */
    public void addSubquery(Query arg0, String arg1, String arg2, String arg3) {
        throw new UnsupportedOperationException("Operation not supported by RefQuery_1");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#addSubquery(javax.jdo.Query, java.lang.String, java.lang.String, java.lang.String[])
     */
    public void addSubquery(
        Query arg0,
        String arg1,
        String arg2,
        String... arg3
    ) {
        throw new UnsupportedOperationException("Operation not supported by RefQuery_1");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#addSubquery(javax.jdo.Query, java.lang.String, java.lang.String, java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public void addSubquery(Query arg0, String arg1, String arg2, Map arg3) {
        throw new UnsupportedOperationException("Operation not supported by RefQuery_1");        
    }
    
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    protected static final long serialVersionUID = 5901724265321809315L;

    protected static final Set<Boolean> TRUE =  Collections.singleton(Boolean.TRUE);    
    protected static final Set<Boolean> FALSE =  Collections.singleton(Boolean.FALSE);

    protected boolean unique = false;
    protected boolean unmodifiable = false;
    protected Map<String,Object> extensions = new HashMap<String,Object>();
    protected transient Collection<?> pcs = null;
    protected FetchPlan fetchPlan = null;

    protected final static ConcurrentMap<String,FeatureMapper> featureMappers = 
        new ConcurrentHashMap<String,FeatureMapper>();

    protected final String filterType;
    protected final Condition filterTypeCondition;
    protected final Filter filter;
    protected final Mapping_1_0 mapping;
    
}

//--- End of File -----------------------------------------------------------
