/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Object Filter 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2014, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.rest.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.jdo.PersistenceManager;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.AnyTypeCondition;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.query.Selector;
import org.openmdx.base.rest.cci.ConditionRecord;
import org.openmdx.base.rest.cci.QueryExtensionRecord;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.kernel.jdo.ReducedJDOHelper;

/**
 * Object Filter
 */
public abstract class ObjectFilter extends ModelAwareFilter {
    
	/**
     * Constructor for a sub-class aware filter
     *
     * @param superFilter
     * @param filter
     * @param extentQuery
     *
     * @exception   IllegalArgumentException
     *              in case of an invalid filter property set
     * @exception   NullPointerException
     *              if the filter is <code>null</code>
     */
    protected ObjectFilter(
        ObjectFilter superFilter,
        QueryFilterRecord filter, 
        boolean extentQuery
    ){
        super(filter.getCondition());
        this.superFilter = superFilter;
        List<QueryExtensionRecord> extensions = filter.getExtension();
        this.extensions =
            extensions != null ? extensions : // the super filter's extension is superseded
            superFilter != null ? superFilter.getExtensions() : 
            null;
        this.evaluateSuperFilterFirst = ObjectFilter.isSuperFilterEvaluatedFirst(this.filter);
        this.extentQuery = extentQuery && isIdentityCondition(this.filter[1]);
    }

    /**
     * Implements <code>Serializable</code>
     */
	private static final long serialVersionUID = 1163766975360335130L;
    
    /**
     * 
     */
    private final Selector superFilter;
    
    /**
     * The delegate includes the super-filter's conditions
     */
    private transient ConditionRecord[] delegate;
    
    /**
     * 
     */
    private final boolean evaluateSuperFilterFirst;
    
    /**
     * 
     */
    private final List<QueryExtensionRecord> extensions;

    /**
     * 
     */
    protected final boolean extentQuery;
    
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.ModelAwareFilter#meetsCondition(java.lang.Object, int, org.openmdx.base.query.Condition)
     */
    @Override
    protected boolean meetsCondition(
        Object candidate,
        int conditionIndex,
        ConditionRecord condition
    ) {
        if(this.extentQuery && conditionIndex == 1){
            return true; // Condition handled by containment test
        } else {
        	return super.meetsCondition(candidate, conditionIndex, condition);
        }
    }

    protected static boolean isSuperFilterEvaluatedFirst(
        ConditionRecord[] conditions
    ){
        for(ConditionRecord condition : conditions) {
            String name = condition.getFeature(); 
            if(
                SystemAttributes.OBJECT_CLASS.equals(name) ||
                SystemAttributes.OBJECT_INSTANCE_OF.equals(name)
            ) {
                return false;
            }
        }
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.ModelAwareFilter#newFilter(org.openmdx.base.query.Filter)
     */
    @Override
    protected abstract AbstractFilter newFilter(QueryFilterRecord delegate);

    private List<ConditionRecord> buildDelegate(
        PersistenceManager persistenceManager
    ){
        List<ConditionRecord> delegate = this.superFilter instanceof ObjectFilter ? 
            ((ObjectFilter)this.superFilter).buildDelegate(persistenceManager) :
            new ArrayList<ConditionRecord>();
        delegate.addAll(super.getDelegate(persistenceManager));
        return delegate;
    }

    /**
     * Tells whether a filer property has to be updated because it contains 
     * a transient object id
     * 
     * @param property the filter property to be tested
     * 
     * @return <code>true</code> if the property contains a transient object id
     */
    private boolean containsTransientObjectId(
        ConditionRecord property
    ){
        for (Object value : property.getValue()) {
            if(value instanceof UUID) {
                return true;
            }
        }
        return false;
    }

    /**
     * Replace transient object id's by appropriate paths
     * 
     * @param property the filter property
     * 
     * @return a property without transient object ids
     */
    private Condition replaceTransientObjectIds(
        PersistenceManager persistenceManager,
        ConditionRecord property
    ){
        Object[] source = property.getValue();
        Object[] target = new Object[source.length];
        int i = 0;
        for(Object value : source) {
            if(value instanceof UUID) {
                Object object = persistenceManager.getObjectById(value);
                target[i++] = ReducedJDOHelper.isPersistent(object) ? ReducedJDOHelper.getObjectId(object) : new Path((UUID)value);
            } else {
                target[i++] = value;
            }
        }
        return new AnyTypeCondition(
            property.getQuantifier(),
            property.getFeature(),
            property.getType(),
            target
        );
    }

    @Override
    public List<ConditionRecord> getDelegate(
        PersistenceManager persistenceManager
    ) {
        if(this.delegate == null) {
            List<ConditionRecord> delegate = this.buildDelegate(persistenceManager);
            this.delegate = new ConditionRecord[delegate.size()];
            int i = 0;
            for(ConditionRecord property : delegate) {
                this.delegate[i++] = this.containsTransientObjectId(property) ? this.replaceTransientObjectIds(
                    persistenceManager,
                    property
                ) : property;
            }
        }
        return Arrays.asList(this.delegate);
    }

    /**
     * Retrieve the extension
     * 
     * @return the extension
     */
    public List<QueryExtensionRecord> getExtensions(
    ){
        return this.extensions;
    }
    
    @Override
    public boolean accept(Object candidate) {
        return this.superFilter == null ? ( 
            super.accept(candidate)
        ) : this.evaluateSuperFilterFirst ? (
            this.superFilter.accept(candidate) && super.accept(candidate)
        ) : (
            super.accept(candidate) && this.superFilter.accept(candidate)
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.query.AbstractFilter#equal(java.lang.Object, java.lang.Object)
     */
    @Override
    protected boolean equal(
        Object candidate, 
        Object value
    ) {
        return candidate == value || super.equal(candidate, value);
    }
    
    /**
     * Retrieve the model class corresponding to the pattern
     * 
     * @param pattern
     * 
     * @return the qualified model class name
     */
    private String getType(Path pattern) {
        try {
            return (String)Model_1Factory.getModel().getTypes(pattern)[2].objGetValue("qualifiedName") ;
        } catch (ServiceException exception) {
            return "";
        }
    }

    private boolean isInstanceOfCondition(
        ConditionRecord condition
    ){
        return 
            SystemAttributes.OBJECT_INSTANCE_OF.equals(condition.getFeature()) &&
            condition.getQuantifier() == Quantifier.THERE_EXISTS &&
            condition.getType() == ConditionType.IS_IN &&
            condition.getValue().length == 1; 
    }
    
    private boolean isInstanceOfCondition(
        ConditionRecord condition,
        String type
    ){
        return 
            isInstanceOfCondition(condition) &&
            condition.getValue(0).equals(type); 
    }

    private boolean isIdentityCondition(
        ConditionRecord condition
    ){
        return 
            SystemAttributes.OBJECT_IDENTITY.equals(condition.getFeature()) &&
            condition.getQuantifier() == Quantifier.THERE_EXISTS &&
            condition.getType() == ConditionType.IS_LIKE &&
            condition.getValue().length == 1;
    }
    
    public Path getIdentityPattern(){
        return new Path(((String)this.filter[1].getValue(0)).replace("\\.", "."));
    }
    
    public boolean isPlainExtent(){
        return
            this.filter.length == 2 && 
            isIdentityCondition(this.filter[1]) &&
            isInstanceOfCondition(this.filter[0], getType(getIdentityPattern()));
    }
    
}