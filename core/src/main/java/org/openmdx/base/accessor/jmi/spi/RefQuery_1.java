/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: RefQuery_1 class
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.jdo.Extent;
import javax.jdo.FetchPlan;
import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.JDOUnsupportedOptionException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jmi.reflect.JmiException;
import javax.jmi.reflect.RefObject;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefQuery_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.ExtentCollection;
import org.openmdx.base.persistence.spi.FilterCollection;
import org.openmdx.base.persistence.spi.StandardFetchPlan;
import org.openmdx.base.query.AnyTypeCondition;
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.IsInstanceOfCondition;
import org.openmdx.base.query.IsLikeCondition;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.query.SortOrder;
import org.openmdx.base.query.spi.EmbeddedFlags;
import org.openmdx.base.rest.cci.ConditionRecord;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.openmdx.kernel.log.SysLog;
import org.w3c.cci2.AnyTypePredicate;
import org.w3c.cci2.BooleanTypePredicate;
import org.w3c.cci2.ComparableTypePredicate;
import org.w3c.cci2.MultivaluedFeaturePredicate;
import org.w3c.cci2.OptionalFeaturePredicate;
import org.w3c.cci2.PartiallyOrderedTypePredicate;
import org.w3c.cci2.RegularExpressionFlag;
import org.w3c.cci2.ResourceIdentifierTypePredicate;
import org.w3c.cci2.SimpleTypeOrder;
import org.w3c.cci2.StringTypeOrder;
import org.w3c.cci2.StringTypePredicate;

/**
 * RefQuery_1_0 implementation
 */
@SuppressWarnings({
    "rawtypes", "unchecked"
})
public class RefQuery_1 implements RefQuery_1_0 {

    /**
     * Constructor
     *
     * @param filter
     * @param mapping
     * @param filterType
     * @param subclasses
     */
    RefQuery_1(
        QueryFilterRecord filter,
        Mapping_1_0 mapping,
        String filterType,
        boolean subclasses,
        PersistenceManager persistenceManager
    ) {
        this.filter = filter == null ? new org.openmdx.base.rest.spi.QueryFilterRecord() : filter;
        this.filterType = filterType;
        this.filter.getCondition().add(
            this.filterTypeCondition = new IsInstanceOfCondition(
                subclasses,
                filterType));
        this.mapping = mapping;
    }

    /**
     * Constructor
     *
     * @param that
     *            the query to be cloned
     */
    private RefQuery_1(
        RefQuery_1 that
    ) {
        this.filter = that.filter.clone();
        this.filterType = that.filterType;
        int indexOfFilterTypeCondition = that.indexOfFilterTypeCondition();
        this.filterTypeCondition = indexOfFilterTypeCondition < 0 ? that.filterTypeCondition.clone()
            : (org.openmdx.base.rest.spi.ConditionRecord) this.filter.getCondition().get(indexOfFilterTypeCondition);
        this.mapping = that.mapping;
        this.persistenceManager = that.persistenceManager;
    }

    //-----------------------------------------------------------------------
    public abstract class RefPredicate implements AnyTypePredicate {

        public RefPredicate(
            Quantifier quantifier,
            String featureName
        ) {
            this.quantifier = quantifier;
            this.featureName = featureName;
        }

        /**
         * Adding value to the filter
         * 
         * @param sortOrder
         */
        protected void refAddValue(
            SortOrder sortOrder
        ) {
            RefQuery_1.this.refAddValue(
                this.featureName,
                sortOrder);
        }

        /**
         * Adding value to the filter
         * 
         * @param sortOrder
         * @param pointer
         *            e.g. a JSON pointer according to RFC 6901 or an XPath
         */
        protected void refAddValue(
            SortOrder sortOrder,
            String pointer
        ) {
            RefQuery_1.this.refAddValue(
                this.featureName,
                sortOrder,
                pointer);
        }

        /**
         * Adding value to the filter
         * 
         * @param quantifier
         * @param conditionType
         * @param operand
         * 
         * @exception NullPointerException
         *                if quantifier or operand is null
         */
        public void refAddValue(
            Quantifier quantifier,
            ConditionType conditionType,
            Collection<?> operand
        ) {
            RefQuery_1.this.refAddValue(
                this.featureName,
                quantifier,
                conditionType,
                operand);
        }

        public RefQuery_1 getQuery() {
            return RefQuery_1.this;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.AnyTypePredicate#equalTo(V)
         */
        public void equalTo(
            Object operand
        ) {
            this.elementOf(
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.AnyTypePredicate#elementOf(V...)
         */
        public void elementOf(
            Object... operand
        ) {
            this.elementOf(
                Arrays.asList(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.AnyTypePredicate#elementOf(Collection<V>)
         */
        public void elementOf(
            Collection<?> operand
        ) {
            if (operand instanceof ExtentCollection<?>) {
                Path pattern = ((ExtentCollection<?>) operand).getPattern();
                this.refAddValue(
                    this.quantifier,
                    pattern.isPattern() ? ConditionType.IS_LIKE : ConditionType.IS_IN,
                    Collections.singleton(pattern));
            } else {
                this.refAddValue(
                    this.quantifier,
                    ConditionType.IS_IN,
                    operand);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.AnyTypePredicate#notEqual(V)
         */
        public void notEqualTo(
            Object operand
        ) {
            this.notAnElementOf(
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.AnyTypePredicate#notAnElementOf(V...)
         */
        public void notAnElementOf(
            Object... operand
        ) {
            this.notAnElementOf(
                Arrays.asList(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.AnyTypePredicate#notAnElementOf(Collection<V>)
         */
        public void notAnElementOf(
            Collection<?> operand
        ) {
            if (operand instanceof ExtentCollection<?>) {
                Path pattern = ((ExtentCollection<?>) operand).getPattern();
                this.refAddValue(
                    this.quantifier,
                    pattern.isPattern() ? ConditionType.IS_UNLIKE : ConditionType.IS_NOT_IN,
                    Collections.singleton(pattern));
            } else {
                this.refAddValue(
                    this.quantifier,
                    ConditionType.IS_NOT_IN,
                    operand);
            }
        }

        /**
         * Add a wildcard prefix or suffix to the given operands
         * 
         * @param prefix
         *            tells whether a prefix or suffix has to be added
         * @param source
         *            the original operands
         * 
         * @return the modified operands
         */
        Collection<String> jdoWildcard(
            Collection<?> source,
            boolean prefix
        ) {
            if (source == null) {
                return Collections.EMPTY_SET;
            } else {
                Collection<String> target = new ArrayList<>();
                for (Iterator<?> i = source.iterator(); i.hasNext();) {
                    target.add(
                        prefix ? ".*" + i.next() : i.next().toString() + ".*");
                }
                return target;
            }
        }

        //-----------------------------------------------------------------------
        public String getFeatureName() {
            return this.featureName;
        }

        protected final Quantifier quantifier;
        protected final String featureName;

    }

    //-------------------------------------------------------------------------
    public class RefObjectTypePredicate extends RefPredicate {

        public RefObjectTypePredicate(
            Quantifier quantifier,
            String featureName
        ) {
            super(quantifier, featureName);
        }

    }

    //-------------------------------------------------------------------------
    public class RefBooleanTypePredicate extends RefPredicate implements BooleanTypePredicate {

        public RefBooleanTypePredicate(
            Quantifier quantifier,
            String featureName
        ) {
            super(quantifier, featureName);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.BooleanValuePredicate#equalTo()
         */
        public void equalTo(
            boolean operand
        ) {
            super.refAddValue(
                this.quantifier,
                ConditionType.IS_IN,
                operand ? TRUE : FALSE);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.BooleanTypePredicate#isFalse()
         */
        public void isFalse() {
            this.equalTo(false);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.BooleanTypePredicate#isTrue()
         */
        public void isTrue() {
            this.equalTo(true);
        }

    }

    //-------------------------------------------------------------------------
    public class RefSimpleTypePredicate extends RefPredicate implements AnyTypePredicate {

        public RefSimpleTypePredicate(
            Quantifier quantifier,
            String featureName
        ) {
            super(quantifier, featureName);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.AnyTypePredicate#equalTo(V)
         */
        @Override
        public void equalTo(
            Object operand
        ) {
            this.elementOf(
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.AnyTypePredicate#elementOf(V...)
         */
        @Override
        public void elementOf(
            Object... operand
        ) {
            this.elementOf(
                Arrays.asList(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.AnyTypePredicate#elementOf(Collection<V>)
         */
        @Override
        public void elementOf(
            Collection<?> operand
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_IN,
                operand);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.AnyTypePredicate#notEqual(V)
         */
        @Override
        public void notEqualTo(
            Object operand
        ) {
            this.notAnElementOf(
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.AnyTypePredicate#notAnElementOf(V...)
         */
        @Override
        public void notAnElementOf(
            Object... operand
        ) {
            this.notAnElementOf(
                Arrays.asList(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.AnyTypePredicate#notAnElementOf(Collection<V>)
         */
        @Override
        public void notAnElementOf(
            Collection<?> operand
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_NOT_IN,
                operand);
        }

    }

    //-------------------------------------------------------------------------
    public class RefComparableTypePredicate<V extends Comparable<?>>
        extends RefSimpleTypePredicate
        implements ComparableTypePredicate<V> {

        public RefComparableTypePredicate(
            Quantifier quantifier,
            String featureName
        ) {
            super(quantifier, featureName);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.ComparableTypePredicate#between(V, V)
         */
        public void between(
            V lowerBound,
            V upperBound
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_BETWEEN,
                Arrays.asList(
                    new Comparable[] {
                        lowerBound, upperBound
                    }));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.ComparableTypePredicate#outside(V, V)
         */
        public void outside(
            V lowerBound,
            V upperBound
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_OUTSIDE,
                Arrays.asList(
                    new Comparable[] {
                        lowerBound, upperBound
                    }));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.ComparableTypePredicate#lessThan(V)
         */
        public void lessThan(
            V operand
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_LESS,
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.ComparableTypePredicate#lessThanOrEqual(V)
         */
        public void lessThanOrEqualTo(
            V operand
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_LESS_OR_EQUAL,
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.ComparableTypePredicate#greaterThanOrEqual(V)
         */
        public void greaterThanOrEqualTo(
            V operand
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_GREATER_OR_EQUAL,
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.ComparableTypePredicate#greaterThan(V)
         */
        public void greaterThan(
            V operand
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_GREATER,
                Collections.singleton(operand));
        }

    }

    //-------------------------------------------------------------------------
    public class RefStringTypePredicate
        extends RefComparableTypePredicate<String>
        implements StringTypePredicate {

        RefStringTypePredicate(
            Quantifier quantifier,
            String featureName
        ) {
            super(quantifier, featureName);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.StringTypePredicate#like(java.lang.String)
         */
        public void like(
            String operand
        ) {
            this.like(
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.StringTypePredicate#like(java.lang.String...)
         */
        public void like(
            String... operand
        ) {
            this.like(
                Arrays.asList(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.StringTypePredicate#like(Collection<String>)
         */
        public void like(
            Collection<String> operand
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_LIKE,
                operand);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.StringTypePredicate#unlike(java.lang.String)
         */
        public void unlike(
            String operand
        ) {
            this.unlike(
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.StringTypePredicate#unlike(java.lang.String...)
         */
        public void unlike(
            String... operand
        ) {
            this.unlike(
                Arrays.asList(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.StringTypePredicate#unlike(Collection<String>)
         */
        public void unlike(
            Collection<String> operand
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_UNLIKE,
                operand);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.StringTypePredicate#like(int,java.lang.String)
         */
        public void like(
            int flags,
            String operand
        ) {
            this.like(
                flags,
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.StringTypePredicate#like(java.lang.String...)
         */
        public void like(
            int flags,
            String... operand
        ) {
            this.like(
                flags,
                Arrays.asList(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.StringTypePredicate#like(Collection<String>)
         */
        public void like(
            int flags,
            Collection<String> operand
        ) {
            this.refAddValue(
                this.quantifier,
                toConditionType(flags, true),
                embedFlags(RegularExpressionFlag.toFlagSet(flags), operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.StringTypePredicate#unlike(java.lang.String)
         */
        public void unlike(
            int flags,
            String operand
        ) {
            this.unlike(
                flags,
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.ci2.StringTypePredicate#unlike(java.lang.String...)
         */
        public void unlike(
            int flags,
            String... operand
        ) {
            this.unlike(
                flags,
                Arrays.asList(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.StringTypePredicate#unlike(Collection<String>)
         */
        public void unlike(
            int flags,
            Collection<String> operand
        ) {
            this.refAddValue(
                this.quantifier,
                toConditionType(flags, false),
                embedFlags(RegularExpressionFlag.toFlagSet(flags), operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.MatchableTypePredicate#endsWith(java.lang.Object)
         */
        public void endsWith(
            String operand
        ) {
            this.endsWith(
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.MatchableTypePredicate#endsWith(V[])
         */
        public void endsWith(
            String... operand
        ) {
            this.endsWith(
                Arrays.asList(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.w3c.cci2.MatchableTypePredicate#endsWith(java.util.Collection)
         */
        public void endsWith(
            Collection<String> operand
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_LIKE,
                this.jdoWildcard(operand, true));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.MatchableTypePredicate#startsWith(java.lang.Object)
         */
        public void startsWith(
            String operand
        ) {
            this.startsWith(
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.MatchableTypePredicate#startsWith(V[])
         */
        public void startsWith(
            String... operand
        ) {
            this.startsWith(
                Arrays.asList(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.w3c.cci2.MatchableTypePredicate#startsWith(java.util.Collection)
         */
        public void startsWith(
            Collection<String> operand
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_LIKE,
                this.jdoWildcard(operand, false));
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.w3c.cci2.MatchableTypePredicate#endsNotWith(java.lang.Object)
         */
        public void endsNotWith(
            String operand
        ) {
            this.endsNotWith(
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.MatchableTypePredicate#endsNotWith(V[])
         */
        public void endsNotWith(
            String... operand
        ) {
            this.endsNotWith(
                Arrays.asList(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.w3c.cci2.MatchableTypePredicate#endsNotWith(java.util.Collection)
         */
        public void endsNotWith(
            Collection<String> operand
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_UNLIKE,
                this.jdoWildcard(operand, true));
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.w3c.cci2.MatchableTypePredicate#startsNotWith(java.lang.Object)
         */
        public void startsNotWith(
            String operand
        ) {
            this.startsNotWith(
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.MatchableTypePredicate#startsNotWith(V[])
         */
        public void startsNotWith(
            String... operand
        ) {
            this.startsNotWith(
                Arrays.asList(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.MatchableTypePredicate#startsNotWith(java.util.
         * Collection)
         */
        public void startsNotWith(
            Collection<String> operand
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_UNLIKE,
                this.jdoWildcard(operand, false));
        }

    }

    //-------------------------------------------------------------------------
    public class RefPartiallyOrderedTypePredicate<V>
        extends RefSimpleTypePredicate
        implements PartiallyOrderedTypePredicate<V> {

        public RefPartiallyOrderedTypePredicate(
            Quantifier quantifier,
            String featureName
        ) {
            super(quantifier, featureName);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.query.PartiallyOrderedTypePredicate#between(V, V)
         */
        public void between(
            V lowerBound,
            V upperBound
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_BETWEEN,
                Arrays.asList(
                    lowerBound,
                    upperBound));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.query.PartiallyOrderedTypePredicate#outside(V, V)
         */
        public void outside(
            V lowerBound,
            V upperBound
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_OUTSIDE,
                Arrays.asList(
                    lowerBound,
                    upperBound));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.query.PartiallyOrderedTypePredicate#lessThan(V)
         */
        public void lessThan(
            V operand
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_LESS,
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.query.PartiallyOrderedTypePredicate#lessThanOrEqual(V)
         */
        public void lessThanOrEqualTo(
            V operand
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_LESS_OR_EQUAL,
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.w3c.query.PartiallyOrderedTypePredicate#greaterThanOrEqual(V)
         */
        public void greaterThanOrEqualTo(
            V operand
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_GREATER_OR_EQUAL,
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.query.PartiallyOrderedTypePredicate#greaterThan(V)
         */
        public void greaterThan(
            V operand
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_GREATER,
                Collections.singleton(operand));
        }

    }

    //-------------------------------------------------------------------------
    public class RefOptionalFeaturePredicate
        extends RefPredicate
        implements OptionalFeaturePredicate {

        public RefOptionalFeaturePredicate(
            Quantifier quantifier,
            String featureName
        ) {
            super(quantifier, featureName);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.OptionalAttributePredicate#isNull()
         */
        public void isNull() {
            this.refAddValue(
                Quantifier.FOR_ALL,
                ConditionType.IS_IN,
                Collections.EMPTY_SET);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.OptionalAttributePredicate#isNonNull()
         */
        public void isNonNull() {
            this.refAddValue(
                Quantifier.THERE_EXISTS,
                ConditionType.IS_NOT_IN,
                Collections.EMPTY_SET);
        }

    }

    //-------------------------------------------------------------------------
    public class RefMultiValuedAttributePredicate
        extends RefPredicate
        implements MultivaluedFeaturePredicate {

        public RefMultiValuedAttributePredicate(
            Quantifier quantifier,
            String featureName
        ) {
            super(quantifier, featureName);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.MultiValuedAttributePredicate#isEmpty()
         */
        public void isEmpty() {
            this.refAddValue(
                Quantifier.FOR_ALL,
                ConditionType.IS_IN,
                Collections.EMPTY_SET);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.MultiValuedAttributePredicate#isNonEmpty()
         */
        public void isNonEmpty() {
            this.refAddValue(
                Quantifier.THERE_EXISTS,
                ConditionType.IS_NOT_IN,
                Collections.EMPTY_SET);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.MultivaluedFeaturePredicate#size()
         */
        public ComparableTypePredicate<Integer> size() {
            throw new UnsupportedOperationException(
                "The only collection size predicates supported in compatibility mode are isEmpty() and isNonEmty()");
        }

    }

    //-------------------------------------------------------------------------
    public class RefResourceIdentifierTypePredicate<V extends Comparable<?>>
        extends RefComparableTypePredicate<V>
        implements ResourceIdentifierTypePredicate<V> {

        public RefResourceIdentifierTypePredicate(
            Quantifier quantifier,
            String featureName
        ) {
            super(quantifier, featureName);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.query.ResourceIdentifierTypePredicate#like(V)
         */
        public void like(
            V operand
        ) {
            this.like(
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.query.ResourceIdentifierTypePredicate#like(V...)
         */
        public void like(
            V... operand
        ) {
            this.like(
                Arrays.asList(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.w3c.query.ResourceIdentifierTypePredicate#like(Collection<V>)
         */
        public void like(
            Collection<V> operand
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_LIKE,
                operand);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.query.ResourceIdentifierTypePredicate#unlike(V)
         */
        public void unlike(
            V operand
        ) {
            this.unlike(
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.query.ResourceIdentifierTypePredicate#unlike(V...)
         */
        public void unlike(
            V... operand
        ) {
            this.unlike(
                Arrays.asList(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.w3c.query.ResourceIdentifierTypePredicate#unlike(Collection<V>)
         */
        public void unlike(
            Collection<V> operand
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_UNLIKE,
                operand);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.MatchableTypePredicate#endsWith(java.lang.Object)
         */
        public void endsWith(
            V operand
        ) {
            this.endsWith(
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.MatchableTypePredicate#endsWith(V[])
         */
        public void endsWith(
            V... operand
        ) {
            this.endsWith(
                Arrays.asList(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.w3c.cci2.MatchableTypePredicate#endsWith(java.util.Collection)
         */
        public void endsWith(
            Collection<V> operand
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_LIKE,
                this.jdoWildcard(operand, true));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.MatchableTypePredicate#startsWith(java.lang.Object)
         */
        public void startsWith(
            V operand
        ) {
            this.startsWith(
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.MatchableTypePredicate#startsWith(V[])
         */
        public void startsWith(
            V... operand
        ) {
            this.startsWith(
                Arrays.asList(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.w3c.cci2.MatchableTypePredicate#startsWith(java.util.Collection)
         */
        public void startsWith(
            Collection<V> operand
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_LIKE,
                this.jdoWildcard(operand, false));
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.w3c.cci2.MatchableTypePredicate#endsNotWith(java.lang.Object)
         */
        public void endsNotWith(
            V operand
        ) {
            this.endsNotWith(
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.MatchableTypePredicate#endsNotWith(V[])
         */
        public void endsNotWith(
            V... operand
        ) {
            this.endsNotWith(
                Arrays.asList(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.w3c.cci2.MatchableTypePredicate#endsNotWith(java.util.Collection)
         */
        public void endsNotWith(
            Collection<V> operand
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_UNLIKE,
                this.jdoWildcard(operand, true));
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.w3c.cci2.MatchableTypePredicate#startsNotWith(java.lang.Object)
         */
        public void startsNotWith(
            V operand
        ) {
            this.startsNotWith(
                Collections.singleton(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.MatchableTypePredicate#startsNotWith(V[])
         */
        public void startsNotWith(
            V... operand
        ) {
            this.startsNotWith(
                Arrays.asList(operand));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.MatchableTypePredicate#startsNotWith(java.util.
         * Collection)
         */
        public void startsNotWith(
            Collection<V> operand
        ) {
            this.refAddValue(
                this.quantifier,
                ConditionType.IS_UNLIKE,
                this.jdoWildcard(operand, false));
        }

    }

    public class RefStringTypeOrder
        extends RefSimpleTypeOrder
        implements StringTypeOrder {

        /**
         * Constructor
         *
         * @param featureName
         */
        public RefStringTypeOrder(
            String featureName
        ) {
            super(featureName);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.StringTypeOrder#ascending(java.lang.String)
         */
        @Override
        public void ascending(
            String pointer
        ) {
            this.refAddValue(SortOrder.ASCENDING, pointer);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.StringTypeOrder#descending(java.lang.String)
         */
        @Override
        public void descending(
            String pointer
        ) {
            this.refAddValue(SortOrder.DESCENDING, pointer);
        }

    }

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

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.SimpleTypeOrder#ascending()
         */
        public void ascending() {
            this.refAddValue(
                SortOrder.ASCENDING);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.SimpleTypeOrder#descending()
         */
        public void descending() {
            this.refAddValue(
                SortOrder.DESCENDING);
        }

    }

    //-------------------------------------------------------------------------
    protected final Model_1_0 getModel() {
        return Model_1Factory.getModel();
    }

    // -------------------------------------------------------------------------
    final protected ModelElement_1_0 getFeature(
        String featureName
    )
        throws ServiceException {
        Model_1_0 model = this.getModel();
        // full-qualified feature name. Lookup in model
        if (featureName.indexOf(':') >= 0) {
            return model.getElement(featureName);
        }
        // get all features of class and find feature with featureName
        else {
            ModelElement_1_0 feature = model.getFeatureDef(
                model.getElement(this.filterType),
                featureName,
                false);
            if (feature == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    "feature not found",
                    new BasicException.Parameter("class name", this.filterType),
                    new BasicException.Parameter("feature", featureName));
            }
            return feature;
        }
    }

    //-------------------------------------------------------------------------
    protected void assertAttributeType(
        ModelElement_1_0 elementDef
    )
        throws ServiceException {
        if (!elementDef.getModel().isAttributeType(elementDef)) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "model element not of type " + ModelAttributes.ATTRIBUTE,
                new BasicException.Parameter("model element", elementDef));
        }
    }

    protected void assertReferenceStoredAsAttribute(
        ModelElement_1_0 elementDef
    )
        throws ServiceException {
        if (!elementDef.getModel().referenceIsStoredAsAttribute(elementDef)) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "model element not of type " + ModelAttributes.REFERENCE + " stored as attribute",
                new BasicException.Parameter("model element", elementDef));
        }
    }

    //-------------------------------------------------------------------------
        @Override
    public void refAddValue(
        ModelElement_1_0 featureDef,
        Quantifier quantifier,
        ConditionType conditionType,
        Collection<?> value
    ) {
        try {
            this.assertModifiable();
            String featureName = featureDef.getName();
            SysLog.log(
                Level.FINEST,
                "{0}|quantifier={1}, feature={2}, conditionType={3}, value={4}",
                "Sys", quantifier, featureName, conditionType, value);
            Model_1_0 model = featureDef.getModel();
            if (model.isReferenceType(featureDef)) {
                if (value instanceof FilterCollection) {
                    this.filter.getCondition().add(
                        new AnyTypeCondition(
                            quantifier,
                            featureName,
                            conditionType,
                            ((FilterCollection) value).getFilter()));
                } else {
                    this.assertReferenceStoredAsAttribute(featureDef);
                    List<Object> values = new ArrayList<>();
                    for (Object v : value) {
                        if (v instanceof RefObject_1_0) {
                            RefObject_1_0 e = (RefObject_1_0) v;
                            String objectClass = e.refClass().refMofId();
                            if (model.isSubtypeOf(objectClass, "org:openmdx:base:ExtentCapable") &&
                                model.isSubtypeOf(objectClass, "org:openmdx:state2:BasicState") &&
                                ReducedJDOHelper.isPersistent(e) &&
                                !ReducedJDOHelper.isNew(e) &&
                                !ReducedJDOHelper.isDeleted(e))
                                try {
                                    values.add(new Path((String) e.refGetValue(SystemAttributes.OBJECT_IDENTITY)));
                                } catch (Exception exception) {
                                    values.add(e.refGetPath());
                                }
                            else {
                                values.add(e.refGetPath());
                            }
                        } else if (v instanceof Path) {
                            values.add(v);
                        } else if (v instanceof String) {
                            values.add(new Path((String) v));
                        } else {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.BAD_PARAMETER,
                                "A value's class is inappropriate for a reference filter collection",
                                new BasicException.Parameter("supported", RefObject_1_0.class.getName(), Path.class
                                    .getName(), String.class.getName()),
                                new BasicException.Parameter("actual", v == null ? null : v.getClass().getName()));
                        }
                    }
                    this.filter.getCondition().add(
                        new AnyTypeCondition(
                            quantifier,
                            featureName,
                            conditionType,
                            values.toArray()));
                }
            } else if (model.isAttributeType(featureDef)) {
                for (Object element : value) {
                    if (element == null) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            "Null is inapprpriate as attribute filter value",
                            new BasicException.Parameter("value", value));
                    }
                }
                this.filter.getCondition().add(
                    new AnyTypeCondition(
                        quantifier,
                        featureName,
                        conditionType,
                        value.toArray()));
            } else {
                //
                // unsupported feature type
                //
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "unsupported feature type. Must be [Attribute|Reference]",
                    new BasicException.Parameter("feature", featureName));
            }
        } catch (ServiceException e) {
            throw new JmiServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
    @Override
    public void refAddValue(
        ModelElement_1_0 featureDef,
        SortOrder order
    ) {
        try {
            this.assertModifiable();
            this.assertAttributeType(featureDef);
            String name = featureDef.getName();
            SysLog.log(Level.FINEST, "Order by {0} {1}", name, order);
            this.filter.getOrderSpecifier().add(
                new OrderSpecifier(
                    name,
                    order));
        } catch (ServiceException e) {
            throw new JmiServiceException(e);
        }
    }

    /**
     * Add a sort order
     * 
     * @param the
     *            feature's model element
     * @param order
     *            the sort order
     * @param featurePointer
     *            e.g. a JSON pointer according to RFC 6901 or an XPath
     */
    public void refAddValue(
        ModelElement_1_0 featureDef,
        SortOrder order,
        String featurePointer
    ) {
        try {
            this.assertModifiable();
            this.assertAttributeType(featureDef);
            final String path;
            if (featurePointer == null || featurePointer.isEmpty()) {
                path = featureDef.getName();
            } else {
                validateFeaturePointer(featurePointer);
                path = featureDef.getName() + featurePointer;
            }
            SysLog.log(Level.FINEST, "Order by {0} {1}", path, order);
            this.filter.getOrderSpecifier().add(
                new OrderSpecifier(path, order)
            );
        } catch (ServiceException e) {
            throw new JmiServiceException(e);
        }
    }

    /**
     * Validate the feature pointer.
     * 
     * @param featurePointer
     * @throws ServiceException
     */
    private void validateFeaturePointer(
        String featurePointer
    )
        throws ServiceException {
        if (!featurePointer.startsWith("/"))
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "Implementation restriction: The feature pointer must start with a slash",
                new BasicException.Parameter("featurePointer", featurePointer));
    }

    //-------------------------------------------------------------------------
    @Override
    public void refAddValue(
        String featureName,
        Quantifier quantifier,
        ConditionType conditionType,
        Collection<?> value
    ) {
        try {
            if (SystemAttributes.OBJECT_INSTANCE_OF.equals(featureName)) {
                if (quantifier != Quantifier.THERE_EXISTS ||
                    conditionType != ConditionType.IS_IN) {
                    throw new JmiServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ILLEGAL_STATE,
                        SystemAttributes.OBJECT_INSTANCE_OF + " implies THERE_EXISTS/IS_IN",
                        new BasicException.Parameter("quantifier", quantifier),
                        new BasicException.Parameter("conditionType", conditionType));
                } else if (SystemAttributes.OBJECT_INSTANCE_OF.equals(filterTypeCondition.getFeature())) {
                    Object[] instanceOf = new Object[value.size()];
                    int i = 0;
                    for (Object v : value) {
                        instanceOf[i++] = v instanceof Class<?> ? getMapping().getModelClassName((Class<?>) v)
                            : (String) v;
                    }
                    this.filterTypeCondition.setValue(instanceOf);
                } else {
                    throw new JmiServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ILLEGAL_STATE,
                        "This filter does not allow subclasses",
                        new BasicException.Parameter(
                            this.filterTypeCondition.getFeature(),
                            this.filterTypeCondition.getValue()),
                        new BasicException.Parameter(
                            SystemAttributes.OBJECT_INSTANCE_OF,
                            value));
                }
            } else {
                this.refAddValue(
                    this.getFeature(featureName),
                    quantifier,
                    conditionType,
                    value);
            }
        } catch (ServiceException e) {
            throw new JmiServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmdx.base.accessor.jmi.cci.RefFilter_1_0#refAddValue(java.lang.
     * String, short)
     */
    @Override
    public void refAddValue(
        String featureName,
        SortOrder order
    )
        throws JmiException {
        try {
            this.refAddValue(
                this.getFeature(featureName),
                order);
        } catch (ServiceException e) {
            throw new JmiServiceException(e);
        }
    }

    /**
     * Add a sort order
     * 
     * @param the
     *            feature
     * @param order
     *            the sort order
     * @param pointer
     *            e.g. a JSON pointer according to RFC 6901 or an XPath
     */
    public void refAddValue(
        String featureName,
        SortOrder order,
        String pointer
    )
        throws JmiException {
        try {
            this.refAddValue(
                this.getFeature(featureName),
                order,
                pointer);
        } catch (ServiceException e) {
            throw new JmiServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
    protected static Object[] unmarshalValues(
        Marshaller marshaller,
        Collection<?> source
    )
        throws ServiceException {
        Object[] target = new Object[source.size()];
        int i = 0;
        for (Object value : source) {
            target[i++] = marshaller.unmarshal(value);
        }
        return target;
    }

    //-------------------------------------------------------------------------
    Object refGetOrder(
        String featureName
    ) {
        try {
            return this.refGetOrder(
                this.getFeature(featureName));
        } catch (ServiceException exception) {
            throw new JmiServiceException(exception);
        }
    }

    //-------------------------------------------------------------------------
    private Object refGetOrder(
        ModelElement_1_0 featureDef
    )
        throws ServiceException {
        String multiplicity = featureDef.getMultiplicity();
        String featureName = featureDef.getQualifiedName();
        switch (ModelHelper.getMultiplicity(featureDef)) {
            case SINGLE_VALUE:
            case OPTIONAL:
                return PrimitiveTypes.STRING.equals(featureDef.getModel().getElementType(featureDef).getQualifiedName())
                    ? new RefStringTypeOrder(featureName) : new RefSimpleTypeOrder(featureName);
            default:
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "Ordering on multivalued attributes is no longer supported",
                    new BasicException.Parameter("feature", featureName),
                    new BasicException.Parameter("multiplicity", multiplicity));
        }
    }

    //-------------------------------------------------------------------------
    Object refGetPredicate(
        String featureName
    ) {
        try {
            return this.refGetPredicate(
                this.getFeature(featureName));
        } catch (ServiceException exception) {
            throw new JmiServiceException(exception);
        }
    }

    //-------------------------------------------------------------------------
    private Object refGetPredicate(
        ModelElement_1_0 featureDef
    )
        throws ServiceException {
        switch (ModelHelper.getMultiplicity(featureDef)) {
            case SINGLE_VALUE:
                return this.refGetPredicate(
                    Quantifier.THERE_EXISTS, // Quantors.FOR_ALL would give the same result but is very inefficient
                    featureDef);
            case OPTIONAL:
                return new RefOptionalFeaturePredicate(
                    Quantifier.THERE_EXISTS,
                    featureDef.getQualifiedName());
            default:
                return new RefMultiValuedAttributePredicate(
                    Quantifier.THERE_EXISTS,
                    featureDef.getQualifiedName());
        }
    }

    //-------------------------------------------------------------------------
    Object refGetPredicate(
        Quantifier quantifier,
        String featureName
    ) {
        try {
            return this.refGetPredicate(
                quantifier,
                this.getFeature(featureName));
        } catch (ServiceException exception) {
            throw new JmiServiceException(exception);
        }
    }

    //-------------------------------------------------------------------------
    private Object refGetPredicate(
        Quantifier quantifier,
        ModelElement_1_0 featureDef
    ) {
        try {
            String qualifiedName = featureDef.getQualifiedName();
            String name = featureDef.getName();
            Model_1_0 model = featureDef.getModel();
            ModelElement_1_0 typeDef = model.getElementType(
                featureDef);
            String typeName = typeDef.getQualifiedName();
            if (model.isPrimitiveType(typeDef)) {
                return PrimitiveTypes.BOOLEAN.equals(typeName) ? new RefBooleanTypePredicate(
                    quantifier,
                    qualifiedName) : PrimitiveTypes.STRING.equals(typeName) ? new RefStringTypePredicate(
                        quantifier,
                        qualifiedName)
                        : PrimitiveTypes.DATETIME.equals(typeName) ? new RefComparableTypePredicate<#if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant#endif>(
                            quantifier,
                            qualifiedName)
                            : PrimitiveTypes.DECIMAL.equals(typeName) ? new RefComparableTypePredicate<BigDecimal>(
                                quantifier,
                                qualifiedName)
                                : PrimitiveTypes.INTEGER.equals(typeName) ? new RefComparableTypePredicate<BigInteger>(
                                    quantifier,
                                    qualifiedName)
                                    : PrimitiveTypes.LONG.equals(typeName) ? new RefComparableTypePredicate<Long>(
                                        quantifier,
                                        qualifiedName)
                                        : PrimitiveTypes.SHORT.equals(typeName) ? new RefComparableTypePredicate<Short>(
                                            quantifier,
                                            qualifiedName)
                                            : PrimitiveTypes.DATE.equals(typeName)
                                                ? new RefPartiallyOrderedTypePredicate<XMLGregorianCalendar>(
                                                    quantifier,
                                                    qualifiedName)
                                                : PrimitiveTypes.ANYURI.equals(typeName)
                                                    ? new RefResourceIdentifierTypePredicate<URI>(
                                                        quantifier,
                                                        qualifiedName)
                                                    : PrimitiveTypes.DURATION.equals(typeName)
                                                        ? new RefPartiallyOrderedTypePredicate<Duration>(
                                                            quantifier,
                                                            qualifiedName) : new RefSimpleTypePredicate(
                                                                quantifier,
                                                                qualifiedName);
            } else {
                QueryFilterRecord filterValue = null;
                for (ConditionRecord condition : this.filter.getCondition()) {
                    if (condition.getFeature().equals(name) &&
                        (condition.getValue().length > 0) &&
                        (condition.getValue(0) instanceof QueryFilterRecord)) {
                        filterValue = (QueryFilterRecord) condition.getValue(0);
                        break;
                    }
                }
                return getMapping().getClassMapping(
                    typeName).newQuery(
                        new Jmi1ObjectPredicateInvocationHandler(
                            new RefObjectTypePredicate(
                                quantifier,
                                name),
                            new RefQuery_1(
                                filterValue,
                                getMapping(),
                                typeName,
                                true, // subclasses
                                getPersistenceManager()
                            )));
            }
        } catch (ServiceException exception) {
            throw new JmiServiceException(exception);
        }
    }

    //-------------------------------------------------------------------------
    // RefFilter_1_0
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.accessor.jmi.cci.RefFilter_1_0#refMofId()
     */
    @Override
    public String refMofId() {
        return this.filterType + "Query";
    }

    //-------------------------------------------------------------------------
    @Override
    public QueryFilterRecord refGetFilter() {
        return this.filter;
    }

    //-------------------------------------------------------------------------
    @Override
    public String toString() {
        return "filter=" + this.filter;
    }

    //------------------------------------------------------------------------
    public FeatureMapper getFeatureMapper()
        throws ServiceException {
        return getMapping().getFeatureMapper(
            this.filterType,
            FeatureMapper.Type.QUERY);
    }

    /**
     * Retrieve the mapping instance
     * 
     * @return the mapping instance
     */
    private Mapping_1_0 getMapping()
        throws ServiceException {
        if (this.mapping != null) {
            return this.mapping;
        } else
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "De-serialized openMDX Query instances are unmodfiable");
    }

    //-------------------------------------------------------------------------
    // Implements javax.jdo.Query
    //-------------------------------------------------------------------------

    /**
     * Assert that the query is modifiable
     * 
     * @exception JDOUserException
     *                if the query is unmodifiable
     */
    protected void assertModifiable() {
        if (this.isUnmodifiable()) {
            throw new JDOUserException("The query is unmodifiable");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#addExtension(java.lang.String, java.lang.Object)
     */
    @Override
    public void addExtension(
        String key, Object value
    ) {
        this.assertModifiable();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#close(java.lang.Object)
     */
    @Override
    public void close(
        Object queryResult
    ) {
        // nothing to do
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#closeAll()
     */
    @Override
    public void closeAll() {
        // nothing to do
    }

    /**
     * @throws Exception
     */
    public void close() throws Exception {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#compile()
     */
    @Override
    public void compile() {
        // nothing to do
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#declareImports(java.lang.String)
     */
    @Override
    public void declareImports(
        String imports
    ) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#declareParameters(java.lang.String)
     */
    @Override
    public void declareParameters(
        String parameters
    ) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#declareVariables(java.lang.String)
     */
    @Override
    public void declareVariables(
        String variables
    ) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#deletePersistentAll()
     */
    @Override
    public long deletePersistentAll() {
        if (this.pcs instanceof RefContainer<?>) {
            RefContainer<?> refContainer = (RefContainer<?>) this.pcs;
            return refContainer.refRemoveAll(this);
        } else if (this.pcs == null) {
            throw new JDOUserException(
                "No candidates set");
        } else {
            throw new JDOUserException(
                "Unsupported candidate class: " + this.pcs.getClass().getName());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#deletePersistentAll(java.util.Map)
     */
    @Override
    public long deletePersistentAll(
        Map parameters
    ) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#deletePersistentAll(java.lang.Object[])
     */
    @Override
    public long deletePersistentAll(
        Object... parameters
    ) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#execute()
     */
    @Override
    public Object execute() {
        if (this.pcs instanceof RefContainer<?>) {
            RefContainer<?> refContainer = (RefContainer<?>) this.pcs;
            List<?> result = refContainer.refGetAll(this);
            if (this.unique) {
                Iterator<?> i = result.iterator();
                return i.hasNext() ? i.next() : null;
            } else {
                return result;
            }
        }
        throw new JDOUserException(
            this.pcs == null ? "No candidates set" : "Unsupported candidate class: " + this.pcs.getClass().getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#execute(java.lang.Object, java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public Object execute(
        Object p1, Object p2, Object p3
    ) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#execute(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object execute(
        Object p1, Object p2
    ) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#execute(java.lang.Object)
     */
    @Override
    public Object execute(
        Object p1
    ) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#executeWithArray(java.lang.Object[])
     */
    @Override
    public Object executeWithArray(
        Object... parameters
    ) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#executeWithMap(java.util.Map)
     */
    @Override
    public Object executeWithMap(
        Map parameters
    ) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#getFetchPlan()
     */
    @Override
    public FetchPlan getFetchPlan() {
        if (this.fetchPlan == null) {
            this.fetchPlan = StandardFetchPlan.newInstance(
                this.getPersistenceManager());
        }
        return this.fetchPlan;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#getIgnoreCache()
     */
    @Override
    public boolean getIgnoreCache() {
        return false; // ignore-cache is not supported by openMDX
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#getPersistenceManager()
     */
    @Override
    public PersistenceManager getPersistenceManager() {
        return this.persistenceManager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#isUnmodifiable()
     */
    @Override
    public boolean isUnmodifiable() {
        return this.unmodifiable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#setCandidates(java.util.Collection)
     */
    @Override
    public void setCandidates(
        Collection pcs
    ) {
        if (pcs instanceof ExtentCollection<?>) {
            ExtentCollection extentCollection = (ExtentCollection) pcs;
            //
            // Apply the pattern only expecting that the query is based on the extent
            // 
            Path pattern = extentCollection.getPattern();
            this.pcs = (Collection<?>) extentCollection.getExtent().getPersistenceManager().getObjectById(
                pattern.getPrefix(5).getChild("extent"));
            this.filter.getCondition().add(
                new IsLikeCondition(
                    Quantifier.THERE_EXISTS,
                    SystemAttributes.OBJECT_IDENTITY,
                    true,
                    ExtentCollection.toIdentityPattern(pattern)));
        } else if (pcs instanceof FilterCollection) {
            throw new IllegalArgumentException("A filter collection cant't be used as candidate collection");
        } else {
            this.pcs = pcs;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#setCandidates(javax.jdo.Extent)
     */
    @Override
    public void setCandidates(
        Extent pcs
    ) {
        throw new UnsupportedOperationException(
            "Extent can't be set via JDO query yet");

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#setClass(java.lang.Class)
     */
    @Override
    public void setClass(
        Class cls
    ) {
        if (!cls.isInterface() ||
            !RefObject.class.isAssignableFrom(cls)) {
            throw new JDOUserException(
                "The JMI interface should be a subclass of RefObject: " + cls.getName());
        }
        try {
            this.filterTypeCondition.setValue(
                getMapping().getModelClassName(cls));
        } catch (ServiceException exception) {
            throw new JDOUserException(
                "Unable to map " + cls.getName() + " to a model class",
                exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#setExtensions(java.util.Map)
     */
    @Override
    public void setExtensions(
        Map extensions
    ) {
        this.assertModifiable();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#setFilter(java.lang.String)
     */
    @Override
    public void setFilter(
        String filter
    ) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#setGrouping(java.lang.String)
     */
    @Override
    public void setGrouping(
        String group
    ) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#setIgnoreCache(boolean)
     */
    @Override
    public void setIgnoreCache(
        boolean ignoreCache
    ) {
        if (ignoreCache) {
            throw new javax.jdo.JDOUnsupportedOptionException("Ignore cache is not supported by openMDX");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#setOrdering(java.lang.String)
     */
    @Override
    public void setOrdering(
        String ordering
    ) {
        throw new UnsupportedOperationException("Expression parsing and arguments not supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#setRange(long, long)
     */
    @Override
    public void setRange(
        long fromIncl,
        long toExcl
    ) {
        throw new UnsupportedOperationException(
            "set range operations are not supported. Use listIterator(position) instead");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#setRange(java.lang.String)
     */
    @Override
    public void setRange(
        String fromInclToExcl
    ) {
        throw new UnsupportedOperationException(
            "set range operations are not supported. Use listIterator(position) instead");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#setResult(java.lang.String)
     */
    @Override
    public void setResult(
        String data
    ) {
        throw new UnsupportedOperationException(
            "Result classes, projections and aggregate function results not supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#setResultClass(java.lang.Class)
     */
    @Override
    public void setResultClass(
        Class cls
    ) {
        throw new UnsupportedOperationException(
            "Result classes, projections and aggregate function results not supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#setUnique(boolean)
     */
    @Override
    public void setUnique(
        boolean unique
    ) {
        this.assertModifiable();
        this.unique = unique;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#setUnmodifiable()
     */
    @Override
    public void setUnmodifiable() {
        this.unmodifiable = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#addSubquery(javax.jdo.Query, java.lang.String,
     * java.lang.String)
     */
    @Override
    public void addSubquery(
        Query arg0, String arg1, String arg2
    ) {
        throw new UnsupportedOperationException("Operation not supported by RefQuery_1");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#addSubquery(javax.jdo.Query, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void addSubquery(
        Query arg0, String arg1, String arg2, String arg3
    ) {
        throw new UnsupportedOperationException("Operation not supported by RefQuery_1");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#addSubquery(javax.jdo.Query, java.lang.String,
     * java.lang.String, java.lang.String[])
     */
    @Override
    public void addSubquery(
        Query arg0,
        String arg1,
        String arg2,
        String... arg3
    ) {
        throw new UnsupportedOperationException("Operation not supported by RefQuery_1");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.Query#addSubquery(javax.jdo.Query, java.lang.String,
     * java.lang.String, java.util.Map)
     */
    @Override
    public void addSubquery(
        Query arg0, String arg1, String arg2, Map arg3
    ) {
        throw new UnsupportedOperationException("Operation not supported by RefQuery_1");
    }

    /**
     * Convert flags to the corresponding condition type
     * 
     * @param flags
     * @param fulfils
     * 
     * @return the corresponding condition type
     * 
     * @throws IllegalArgumentException
     */
    ConditionType toConditionType(
        int flags,
        boolean fulfils
    )
        throws IllegalArgumentException {
        if (RegularExpressionFlag.SOUNDS.isSet(flags)) {
            if (flags != StringTypePredicate.SOUNDS)
                throw new IllegalArgumentException(
                    "SOUNDS must not be combined with other flags: " + Integer.toHexString(flags));
            return fulfils ? ConditionType.SOUNDS_LIKE : ConditionType.SOUNDS_UNLIKE;
        } else {
            return fulfils ? ConditionType.IS_LIKE : ConditionType.IS_UNLIKE;
        }
    }

    Collection<String> embedFlags(
        EnumSet<RegularExpressionFlag> flags,
        Collection<String> rawArguments
    ) {
        if (flags.isEmpty()) {
            return rawArguments;
        } else {
            EmbeddedFlags embeddedFlags = EmbeddedFlags.getInstance();
            List<String> arguments = new ArrayList<>();
            for (String rawArgument : rawArguments) {
                arguments.add(embeddedFlags.embedFlags(flags, rawArgument));
            }
            return arguments;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    @Override
    public RefQuery_1 clone() {
        return new RefQuery_1(this);
    }

    /**
     * Find the index of the filter type condition
     * 
     * @return the index of the filter type condition
     */
    private int indexOfFilterTypeCondition() {
        int i = 0;
        for (ConditionRecord candidate : this.filter.getCondition()) {
            if (candidate == this.filterTypeCondition) {
                return i;
            } else {
                i++;
            }
        }
        return -1;
    }

    //-------------------------------------------------------------------------
    // Variablesfilter
    //-------------------------------------------------------------------------
    protected static final long serialVersionUID = 5901724265321809315L;

    protected static final Set<Boolean> TRUE = Collections.singleton(Boolean.TRUE);
    protected static final Set<Boolean> FALSE = Collections.singleton(Boolean.FALSE);
    protected boolean unique = false;
    protected boolean unmodifiable = false;
    protected transient Collection<?> pcs = null;
    protected FetchPlan fetchPlan = null;
    protected final String filterType;
    protected final org.openmdx.base.rest.spi.ConditionRecord filterTypeCondition;
    protected final QueryFilterRecord filter;
    private transient PersistenceManager persistenceManager;
    private transient Mapping_1_0 mapping;

    /**
     * The number of milliseconds allowed for read operations to complete
     */
    private Integer datastoreReadTimeoutMillis;

    /**
     * The number of milliseconds allowed for write operations to complete
     */
    private Integer datastoreWriteTimeoutMillis;
    
    /* (non-Javadoc)
     * @see javax.jdo.Query#setDatastoreReadTimeoutMillis(java.lang.Integer)
     */
    @Override
    public void setDatastoreReadTimeoutMillis(Integer interval) {
        this.datastoreReadTimeoutMillis = interval;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#getDatastoreReadTimeoutMillis()
     */
    @Override
    public Integer getDatastoreReadTimeoutMillis() {
        return this.datastoreReadTimeoutMillis;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setDatastoreWriteTimeoutMillis(java.lang.Integer)
     */
    @Override
    public void setDatastoreWriteTimeoutMillis(Integer interval) {
        this.datastoreWriteTimeoutMillis = interval;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#getDatastoreWriteTimeoutMillis()
     */
    @Override
    public Integer getDatastoreWriteTimeoutMillis() {
        return this.datastoreWriteTimeoutMillis;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#cancelAll()
     */
    @Override
    public void cancelAll() {
        throw new JDOUnsupportedOptionException(
            "Cancel is not supported by openMDX"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#cancel(java.lang.Thread)
     */
    @Override
    public void cancel(Thread thread) {
        throw new JDOUnsupportedOptionException(
            "Cancel is not supported by openMDX"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setSerializeRead(java.lang.Boolean)
     */
    @Override
    public void setSerializeRead(Boolean serialize) {
        if(Boolean.TRUE.equals(serialize)) {
            throw new JDOFatalDataStoreException("openMDX does not support read serialization");
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#getSerializeRead()
     */
    @Override
    public Boolean getSerializeRead() {
        return null;
    }

    public Query saveAsNamedQuery(String s) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Query filter(String s) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Query orderBy(String s) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Query groupBy(String s) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Query result(String s) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Query range(long l, long l1) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Query range(String s) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Query subquery(Query query, String s, String s1) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Query subquery(Query query, String s, String s1, String s2) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Query subquery(Query query, String s, String s1, String... strings) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Query subquery(Query query, String s, String s1, Map map) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Query imports(String s) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Query parameters(String s) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Query variables(String s) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Query datastoreReadTimeoutMillis(Integer integer) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Query datastoreWriteTimeoutMillis(Integer integer) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Query serializeRead(Boolean aBoolean) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Query unmodifiable() {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Query ignoreCache(boolean b) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Query extension(String s, Object o) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Query extensions(Map map) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Query setParameters(Object... objects) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public List executeList() {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Object executeUnique() {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public List<Object> executeResultList() {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Object executeResultUnique() {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Object executeResultUnique(Class aClass) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public List executeResultList(Class aClass) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

    public Query setNamedParameters(Map map) {
        throw new UnsupportedOperationException("Will be implemented from openMDX from x.20.0 on");
    }

}

//--- End of File -----------------------------------------------------------
