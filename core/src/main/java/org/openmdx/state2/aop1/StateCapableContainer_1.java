/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: State Object Container
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
package org.openmdx.state2.aop1;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;
import #if JAVA_8 javax.resource.cci.InteractionSpec #else jakarta.resource.cci.InteractionSpec #endif;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.spi.Delegating_1_0;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.ClassicSegments;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.TransientContainerId;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.IsGreaterCondition;
import org.openmdx.base.query.IsGreaterOrEqualCondition;
import org.openmdx.base.query.IsInCondition;
import org.openmdx.base.query.IsInstanceOfCondition;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.rest.cci.ConditionRecord;
import org.openmdx.base.rest.cci.FeatureOrderRecord;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.openmdx.state2.cci.DateStateContext;
import org.openmdx.state2.cci.DateTimeStateContext;
import org.openmdx.state2.cci.ViewKind;
import org.openmdx.state2.spi.TechnicalAttributes; 

/**
 * State Object Container
 */
public class StateCapableContainer_1 
    implements Serializable, Container_1_0, Delegating_1_0<Container_1_0> 
{

	/**
     * Constructor 
     *
     * @param parent
     * @param container
     * @param type 
     */
    protected StateCapableContainer_1(
        ObjectView_1_0 parent,
        Container_1_0 container, 
        String type 
    ) throws ServiceException {
        this.parent = parent;
		this.container = container;
		this.selection = container.subMap(
			getFilter(
				parent, 
		        (Path) ReducedJDOHelper.getObjectId(container), 
		        type
		    )
		);
		this.defaultType = parent.getInteractionSpec() == null;
    }

    /**
     * Constructor 
     *
     * @param parent
     * @param container
     * @param criteria
     * @param defaultType the core types have been set selected by default
     */
    private StateCapableContainer_1(
        ObjectView_1_0 parent,
        Container_1_0 container,
        QueryFilterRecord criteria,
        boolean defaultType
    ){
        this.parent = parent;
        this.container = container;
        this.selection = container.subMap(criteria);
        this.defaultType = defaultType;
    }
           
    /**
     * The core types have been set selected by default
     */
    private final boolean defaultType;
    
    protected final ObjectView_1_0 parent;
 
    protected final Container_1_0 container;
    
    protected final Container_1_0 selection;
    
    private transient Set<Map.Entry<String, DataObject_1_0>> entries;

    private transient Set<String> keys;
    
    private transient Collection<DataObject_1_0> values;
    
    private static final Collection<String> TYPE_FEATURES = Arrays.asList(
    	SystemAttributes.OBJECT_CLASS,
    	SystemAttributes.OBJECT_INSTANCE_OF
    );
    
    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = 1312568818384216689L;

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Container_1_0#superSet()
     */
    @Override
    public Container_1_0 container(
    ) {
        return this.container.container();
    }     

    @Override
    public Object jdoGetObjectId() {
        return this.container.jdoGetObjectId();
    }

    @Override
    public Object jdoGetTransactionalObjectId() {
        return this.container.jdoGetTransactionalObjectId();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoGetPersistenceManager()
     */
    @Override
    public PersistenceManager jdoGetPersistenceManager(){
    	return this.parent.jdoGetPersistenceManager();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoGetPersistenceManager()
     */
    @Override
    public PersistenceManager openmdxjdoGetDataObjectManager() {
        return this.container.openmdxjdoGetDataObjectManager();
    }

    @Override
    public boolean jdoIsPersistent() {
        return this.container.jdoIsPersistent();
    }

    @Override
    public final Container_1_0 objGetDelegate(){
        return this.selection;
    }
    
	/**
     * Derive the filter from the state context
     * 
     * @param type 
     * @param interactionSpec
     * 
     * @return the corresponding filter
     * @throws ServiceException 
     */
    protected Filter getFilter(
        ObjectView_1_0 parent,
        Path containerId, 
        String type
    ) throws ServiceException{
        InteractionSpec interactionSpec = parent.getInteractionSpec();
        if(interactionSpec instanceof DateStateContext) {
            DateStateContext stateContext = (DateStateContext) interactionSpec;
        	List<Condition> filter = new ArrayList<Condition>();
        	filter.add(
	            new IsInstanceOfCondition(
	                "org:openmdx:state2:DateState"
	            )
	        );
            switch(stateContext.getViewKind()) {
                case TIME_RANGE_VIEW:
                	#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif validTo = stateContext.getValidTo();
                	if(validTo != null) {
                		filter.add(
                            new IsGreaterCondition(
                                Quantifier.FOR_ALL,
                                TechnicalAttributes.STATE_VALID_FROM, // LESS_THAN_OR_EQUAL
                                false,
                                validTo
                            )
                		);
                	}
                	#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif validFrom = stateContext.getValidFrom();
                	if(validFrom != null) {
                		filter.add(
                            new IsGreaterOrEqualCondition(
                                Quantifier.FOR_ALL,
                                TechnicalAttributes.STATE_VALID_TO,
                                true,
                                validFrom
                            )
                		);
                	}
            		filter.add(
                        new IsInCondition(
                            Quantifier.FOR_ALL,
                            SystemAttributes.REMOVED_AT,
                            true
                        )
                    );
            		break;
                case TIME_POINT_VIEW:
                	#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif validAt = stateContext.getValidAt();
                	if(validAt != null) {
                		filter.add(
                            new IsGreaterCondition(
                                Quantifier.FOR_ALL,
                                TechnicalAttributes.STATE_VALID_FROM,
                                false, // IS_LESS_OR_EQUAL
                                validAt
                            )
                        );
                		filter.add(
                            new IsGreaterOrEqualCondition(
                                Quantifier.FOR_ALL,
                                TechnicalAttributes.STATE_VALID_TO,
                                true, // IS_GREATER_OR_EQUAL
                                validAt
                            )
                        );
                	}
                	#if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif existsAt = stateContext.getExistsAt();
                	if(existsAt == null) {
                		filter.add(
                            new IsInCondition(
                                Quantifier.FOR_ALL,
                                SystemAttributes.REMOVED_AT,
                                true // IS_IN
                            )
						);
                	} else {
                		filter.add(
                            new IsGreaterCondition(
                                Quantifier.THERE_EXISTS,
                                SystemAttributes.CREATED_AT,
                                false, // IS_LESS_OR_EQUAL,
                                existsAt
                            )
                		);
                		filter.add(
                            new IsGreaterOrEqualCondition(
                                Quantifier.FOR_ALL,
                                SystemAttributes.REMOVED_AT,
                                true, // IS_GREATER_OR_EQUAL
                                existsAt
                            )
            			);
                	}
                	break;
        	}
            return new Filter(filter, null, null);
        } else if(interactionSpec instanceof DateTimeStateContext) {
        	throw new ServiceException(
        		BasicException.Code.DEFAULT_DOMAIN,
        		BasicException.Code.NOT_IMPLEMENTED,
        		"DateTimeState support not implemented yet",
        		new BasicException.Parameter("interactionSpec", interactionSpec)
        	);
        } else if (type == null){
        	return null; // Extent
        } else {
    		Model_1_0 model = parent.getModel();
    		Set<String> values = new HashSet<String>();
            ModelElement_1_0 classDef = model.getDereferencedType(type);
            values.add(classDef.getQualifiedName());
            for(Object path : classDef.objGetList("allSubtype")) {
            	values.add(((Path)path).getLastSegment().toClassicRepresentation());
            }
    		for(Iterator<String> i = values.iterator(); i.hasNext();){
    			String v = i.next();
                if(
                	model.isSubtypeOf(v, "org:openmdx:state2:BasicState") ||
                	"org:openmdx:state2:StateCapable".equals(v) || 
                	!model.isSubtypeOf(v, "org:openmdx:state2:StateCapable") 
            	) {
                    i.remove();
                }
    		}
    		return new Filter(
    			new IsInstanceOfCondition(
    				false, // exclude sub-classes 
    				values.toArray(new String[values.size()])
    			)
    		);
        }
    }

    private static boolean isTypeCondition(
        ConditionRecord condition
    ){
    	return condition instanceof IsInstanceOfCondition || TYPE_FEATURES.contains(condition.getFeature());
    }

    private static boolean isInstanceOfCondition(
        ConditionRecord condition
    ){
    	return SystemAttributes.OBJECT_INSTANCE_OF.equals(condition.getFeature());
    }

	private ConditionRecord toCoreCondition(
        ConditionRecord condition
    ) throws ServiceException{
    	if(isInstanceOfCondition(condition)){
            Model_1_0 model = this.parent.getModel();
    		Set<String> values = new HashSet<String>();
    		for(Object qualifiedTypeName : condition.getValue()) {
    	        ModelElement_1_0 classDef = model.getDereferencedType(qualifiedTypeName);
    	        values.add(classDef.getQualifiedName());
    	        for(Object path : classDef.objGetList("allSubtype")) {
    	        	values.add(((Path)path).getLastSegment().toClassicRepresentation());
    	        }
    		}
    		for(Iterator<String> i = values.iterator(); i.hasNext();){
    			String v = i.next();
                if(
                	model.isSubtypeOf(v, "org:openmdx:state2:BasicState") ||
                	"org:openmdx:state2:StateCapable".equals(v) || !model.isSubtypeOf(v, "org:openmdx:state2:StateCapable") 
            	) {
                    i.remove();
                }
    		}
    		return new IsInstanceOfCondition(
    			false,
    			values.toArray(new String[values.size()])
    	    );
    	} else {
	    	return condition; 
    	}
    }
    
    private boolean isStateFilter(
        ConditionRecord condition
    ){
        Model_1_0 model = this.parent.getModel();
        for(Object value : condition.getValue()) {
            try {
                if(model.isSubtypeOf(value, "org:openmdx:state2:BasicState")) {
                    return true;
                }
            } catch (ServiceException exception) {
                exception.log();
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.FilterableMap#subMap(java.lang.Object)
     */
    @Override
    public Container_1_0 subMap(QueryFilterRecord filter) {
    	if(filter == null) {
    		return this;
    	} else {
	    	ViewKind viewKind = this.getViewKind(); 
        	boolean typed = false;
	        if(viewKind != ViewKind.TIME_POINT_VIEW){
	        	boolean stated = false;
	            for(
	            	ListIterator<ConditionRecord> i = filter.getCondition().listIterator();
	            	i.hasNext();
	            ){	
            		ConditionRecord condition = i.next();
	                if(isTypeCondition(condition)) {
	                	if(viewKind == null) {
		                    if(isStateFilter(condition)) {
		                    	//
		                    	// This mode is used by DateStateViews internally!
		                    	//
		                    	stated = true;
		                    } else if (!stated) {
		                    	try {
			                    	i.set(this.toCoreCondition(condition));
			                    } catch (ServiceException exception) {
			                    	throw new IllegalArgumentException(exception);
			                    }
		                    }
	                	} else {
	                        throw new UnsupportedOperationException(
	                            "State queries require a time point view: " +
	                            this.parent.getInteractionSpec()
	                        );
	                	}
	                	typed = true;
	                }
	            }
	        }
	        return new StateCapableContainer_1(
	            this.parent,
	            typed && this.defaultType ? this.container : this.selection,
	            filter,
	            false
	        );
    	}
    }

    /**
     * Tests whether at least one state is selected
     * 
     * @param object
     * 
     * @return {@code true{@code  if at least one state is selected
     */
    private boolean hasStates(
    	DataObject_1_0 value
    ){
    	if(value == null) {
    		return false;
    	} else {
	      	Container_1_0 states = this.selection.subMap(
	      		new Filter(
	      			new IsInCondition(
	      				Quantifier.THERE_EXISTS,
	      				SystemAttributes.CORE,
	      				true, // IS_IN,
	      				value.jdoIsPersistent() ? value.jdoGetObjectId() : value.jdoGetTransactionalObjectId()
	      			)
	      		)
	        );
	    	return !states.isEmpty();
    	}
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.FilterableMap#values(java.lang.Object)
     */
    @Override
    public List<DataObject_1_0> values(
        FetchPlan fetchPlan, FeatureOrderRecord... criteria
    ) {
        return this.selection.values(fetchPlan, criteria);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.Container_1_0#processAll(FetchPlan, FeatureOrderRecord[], Consumer)
     */
    @Override
    public void processAll(
        FetchPlan fetchPlan,
        FeatureOrderRecord[] criteria,
        Consumer<DataObject_1_0> consumer
    ) {
        this.selection.processAll(fetchPlan, criteria, consumer);
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {
        this.keySet().clear();
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        return hasStates(this.container.get(key));
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(
        Object value
    ) {
    	return this.container.containsValue(value) && hasStates((DataObject_1_0) value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<java.util.Map.Entry<String, DataObject_1_0>> entrySet() {
        if(this.entries == null) {
            this.entries = new AbstractSet<java.util.Map.Entry<String, DataObject_1_0>>() {

                @Override
                public Iterator<Map.Entry<String,DataObject_1_0>> iterator() {
                    return new EntryIterator();
                }

                @Override
                public int size() {
                    return StateCapableContainer_1.this.size();
                }

                @Override
                public boolean isEmpty() {
                    return StateCapableContainer_1.this.isEmpty();
                }

            };
        }    
        return this.entries;
    }
    
    
    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public DataObject_1_0 get(Object key) {
    	//
    	// TODO For some reason we may not return null if there are no states...
    	//
        return this.container.get(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return this.selection.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<String> keySet() {
    	if(this.keys == null) {
    		this.keys = new AbstractSet<String>() {

                @Override
                public Iterator<String> iterator() {
                    return new KeyIterator();
                }

                @Override
                public int size() {
                    return StateCapableContainer_1.this.size();
                }

                @Override
                public boolean isEmpty() {
                    return StateCapableContainer_1.this.isEmpty();
                }

				/* (non-Javadoc)
				 * @see java.util.AbstractCollection#contains(java.lang.Object)
				 */
				@Override
				public boolean contains(Object o) {
					return StateCapableContainer_1.this.containsValue(o);
				}
            	
            };
    	}
        return this.keys;
    }

    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public DataObject_1_0 put(String key, DataObject_1_0 value) {
        return this.selection.put(key, value);            
    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map<? extends String, ? extends DataObject_1_0> t) {
        throw new UnsupportedOperationException();
    }

    /**
     * Determine the container's view
     * 
     * @return the container's view
     */
    private ViewKind getViewKind(){
        InteractionSpec interactionSpec = this.parent.getInteractionSpec();
        return interactionSpec instanceof DateStateContext ?
            ((DateStateContext)interactionSpec).getViewKind() : 
            null;
    }

    /**
     * Remove internally
     * 
     * @param key
     * @param object
     */
    void remove(
    	String key,
    	DataObject_1_0 object
    ){
    	try {
			if(this.getViewKind() != ViewKind.TIME_RANGE_VIEW){
			    throw new UnsupportedOperationException(
			        "Inappropriate context for state view modification: " +
			        this.parent.getInteractionSpec()
			    );
			}
			Container_1_0 viewContainer = this.parent.objGetContainer(
				((TransientContainerId)this.container.jdoGetTransactionalObjectId()).getFeature()
			);
	        this.parent.jdoGetPersistenceManager().deletePersistent(
	        	viewContainer.get(key)
	        );
    	} catch (ServiceException exception) {
    		throw new RuntimeServiceException(exception);
    	}
    }
    
    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public DataObject_1_0 remove(
        Object key
    ) {
    	DataObject_1_0 value = this.get(key);
    	if(value != null){
    		remove((String)key, value);
    	}
        return value;
    }

    
    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        return getSnapShot().size();
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    @Override
    public Collection<DataObject_1_0> values() {
        if(this.values == null) {
            this.values = new AbstractCollection<DataObject_1_0>(){

                @Override
                public Iterator<DataObject_1_0> iterator() {
                    return new ValueIterator();
                }

                @Override
                public int size() {
                    return StateCapableContainer_1.this.size();
                }

                @Override
                public boolean isEmpty() {
                    return StateCapableContainer_1.this.isEmpty();
                }

				/* (non-Javadoc)
				 * @see java.util.AbstractCollection#contains(java.lang.Object)
				 */
				@Override
				public boolean contains(Object o) {
                    return StateCapableContainer_1.this.containsValue(o);
				}
            	
            };
        }
        return this.values;
    }

    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.Container_1_0#retrieve()
     */
    @Override
    public void openmdxjdoRetrieve(FetchPlan fetchPlan) {
        this.selection.openmdxjdoRetrieve(fetchPlan);
    }

    @Override
    public boolean isRetrieved() {
        return this.selection.isRetrieved();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoEvict()
     */
    @Override
    public void openmdxjdoEvict(boolean allMembers, boolean allSubSets) {
        this.selection.openmdxjdoEvict(allMembers, allSubSets);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.Container_1_0#refreshAll()
     */
    @Override
    public void openmdxjdoRefresh() {
        this.selection.openmdxjdoRefresh();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#toString()
     */
    @Override
    public String toString() {
        return this.selection.toString();
    }
    
    /**
     * Derive the key from the qualifier
     * 
     * @param qualifier
     * 
     * @return the derived key
     */
    private String toKey(
        String qualifier
    ){
        Optional<String> coreComponent = ClassicSegments.getCoreComponentFromAspectQualifierPlaceholder(qualifier);
        if(!coreComponent.isPresent()) {
            coreComponent = ClassicSegments.getCoreComponentFromAspectQualifier(qualifier);
        }
        return coreComponent.isPresent() ? coreComponent.get() : qualifier;
    }

    /**
     * A snapshot is required in order to allow state creation or removal during iteration
     * 
     * @return a snapshot
     */
    Map<String,DataObject_1_0> getSnapShot(){
    	if(StateCapableContainer_1.this.container.isEmpty()) {
    		return Collections.emptyMap();
    	} else {
    		Map<String,DataObject_1_0> snapshot = new HashMap<String, DataObject_1_0>();
    		for(String qualifier : this.selection.keySet()) {
    			String key = toKey(qualifier);
    			DataObject_1_0 value = this.container.get(key);
    			if(value != null) {
    				snapshot.put(key, value);
    			}
    		}
    		return snapshot;
    	}
    }

    
    //-------------------------------------------------------------------------
    // Implements PersistenceCapable
    //-------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceStateManager(javax.jdo.spi.StateManager)
     */
    @Override
    public void jdoReplaceStateManager(
        StateManager sm
    ) throws SecurityException {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideField(int)
     */
    @Override
    public void jdoProvideField(int fieldNumber) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideFields(int[])
     */
    @Override
    public void jdoProvideFields(int[] fieldNumbers) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceField(int)
     */
    @Override
    public void jdoReplaceField(int fieldNumber) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFields(int[])
     */
    @Override
    public void jdoReplaceFields(int[] fieldNumbers) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFlags()
     */
    @Override
    public void jdoReplaceFlags() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyFields(java.lang.Object, int[])
     */
    @Override
    public void jdoCopyFields(Object other, int[] fieldNumbers) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoMakeDirty(java.lang.String)
     */
    @Override
    public void jdoMakeDirty(String fieldName) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoGetVersion()
     */
    @Override
    public Object jdoGetVersion() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDirty()
     */
    @Override
    public boolean jdoIsDirty() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsTransactional()
     */
    @Override
    public boolean jdoIsTransactional() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsNew()
     */
    @Override
    public boolean jdoIsNew() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDeleted()
     */
    @Override
    public boolean jdoIsDeleted() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDetached()
     */
    @Override
    public boolean jdoIsDetached() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager)
     */
    @Override
    public PersistenceCapable jdoNewInstance(StateManager sm) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager, java.lang.Object)
     */
    @Override
    public PersistenceCapable jdoNewInstance(StateManager sm, Object oid) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance()
     */
    @Override
    public Object jdoNewObjectIdInstance() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance(java.lang.Object)
     */
    @Override
    public Object jdoNewObjectIdInstance(Object o) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(java.lang.Object)
     */
    @Override
    public void jdoCopyKeyFieldsToObjectId(Object oid) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldSupplier, java.lang.Object)
     */
    @Override
    public void jdoCopyKeyFieldsToObjectId(ObjectIdFieldSupplier fm, Object oid) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsFromObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldConsumer, java.lang.Object)
     */
    @Override
    public void jdoCopyKeyFieldsFromObjectId(
        ObjectIdFieldConsumer fm,
        Object oid
    ) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }


    //-------------------------------------------------------------------------
    // Class KeyIterator
    //-------------------------------------------------------------------------

    /**
     * Key Iterator
     */
    class KeyIterator implements Iterator<String> {

		/**
         * An entry set iterator
         */
        private final Iterator<Map.Entry<String,DataObject_1_0>> delegate = new EntryIterator();

        @Override
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        @Override
        public String next() {
        	return this.delegate.next().getKey();
        }

        @Override
        public void remove() {
            this.delegate.remove();
        }
        
    }
    
    
    //------------------------------------------------------------------------
    // Class ValueIterator
    //------------------------------------------------------------------------

    /**
     * Value Iterator
     */
    class ValueIterator implements Iterator<DataObject_1_0> {

		/**
         * An entry set iterator
         */
        private final Iterator<Map.Entry<String,DataObject_1_0>> delegate = new EntryIterator();

        @Override
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        @Override
        public DataObject_1_0 next() {
        	return this.delegate.next().getValue();
        }

        @Override
        public void remove() {
            this.delegate.remove();
        }

    }

    
    //-------------------------------------------------------------------------
    // Class EntryIterator
    //-------------------------------------------------------------------------

    /**
     * Entry Iterator
     */
    class EntryIterator implements Iterator<Map.Entry<String,DataObject_1_0>>  {

		/**
         * The snapshot iterator
         */
        private final Iterator<Map.Entry<String,DataObject_1_0>> delegate = StateCapableContainer_1.this.getSnapShot().entrySet().iterator();

        /**
         * The current element
         */
        Map.Entry<String,DataObject_1_0> current;
        
        @Override
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        @Override
        public Map.Entry<String,DataObject_1_0> next() {
        	this.current = this.delegate.next();
        	return new Map.Entry<String,DataObject_1_0> (){

				public String getKey() {
					return EntryIterator.this.current.getKey();
				}

				public DataObject_1_0 getValue() {
					return EntryIterator.this.current.getValue();
				}

				public DataObject_1_0 setValue(DataObject_1_0 value) {
					throw new UnsupportedOperationException();
				}
        		
        	};
        }

        @Override
        public void remove() {
            this.delegate.remove();
            StateCapableContainer_1.this.remove(
            	this.current.getKey(), 
            	this.current.getValue()
            );
        }
 
    }
   
}
