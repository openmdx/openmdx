/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.opencrx.org/
 * Description: CompositeGrid
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2013, OMEX AG, Switzerland
 * All rights reserved.
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
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 */
package org.openmdx.portal.servlet.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.resource.ResourceException;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefQuery_1_0;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.Queries;
import org.openmdx.base.persistence.spi.QueryExtension;
import org.openmdx.base.rest.cci.ConditionRecord;
import org.openmdx.base.rest.cci.FeatureOrderRecord;
import org.openmdx.base.rest.cci.QueryExtensionRecord;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Filter;
import org.openmdx.portal.servlet.control.UiGridControl;

/**
 * CompositeGrid
 *
 */
public class UiCompositeGrid extends UiGrid implements Serializable {

    /**
     * Constructor 
     *
     * @param gridControl
     * @param view
     * @param lookupType
     */
    public UiCompositeGrid(
        UiGridControl gridControl,
        ObjectView view,
        String lookupType
    ) {
        super(
            gridControl,
            view,
            lookupType
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.Grid#refresh(boolean)
     */
    @Override
    public void refresh(
        boolean refreshData
    ) {
        super.refresh(
            refreshData
        );
    }

    /**
     * Map filter to query.
     * 
     * @param container
     * @param filter
     * @return
     * @throws ServiceException
     * @throws ResourceException
     */
    protected javax.jdo.Query newQuery(
    	RefContainer<RefObject_1_0> container,
    	QueryFilterRecord filter
    ) throws ServiceException, ResourceException {
    	UiGridControl control = (UiGridControl)this.control;
    	PersistenceManager pm = JDOHelper.getPersistenceManager(container);
		Query_2Facade queryFacade = Query_2Facade.newInstance(new Path(container.refMofId()));
		queryFacade.setQueryFilter(filter);
		queryFacade.setQueryType(control.getObjectContainer().getReferencedTypeName());
		javax.jdo.Query query = pm.newQuery(
			Queries.QUERY_LANGUAGE, 
			queryFacade.getDelegate()
		);
		query.getFetchPlan().setFetchSize(this.getPageSize() + 1);
		return query;
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.Grid#getFilteredObjects(javax.jdo.PersistenceManager, org.openmdx.portal.servlet.Filter)
     */
    @Override
    public List<RefObject_1_0> getFilteredObjects(
    	PersistenceManager pm,
    	boolean preCalcListSize,
        Filter filter
    ) {
        List<RefObject_1_0> filteredObjects = null;
    	UiGridControl control = (UiGridControl)this.control;
       	ObjectView view = this.getView();
       	Filter preparedFilter = null;
        try {
	    	RefObject_1_0 parent = (RefObject_1_0)pm.getObjectById(
	    		view.getObjectReference().getObject().refGetPath()
	    	);
	    	if(filter != null) {
	    		preparedFilter = new Filter(
		    		filter.getName(),
		    		filter.getLabel(),
		    		filter.getGroupName(),
		    		filter.getIconKey(),
		    		filter.getOrder(),
		    		new ArrayList<ConditionRecord>(filter.getCondition()),
		    		new ArrayList<FeatureOrderRecord>(filter.getOrderSpecifier()),
		    		new ArrayList<QueryExtensionRecord>(filter.getExtension()),
		    		filter.getName()
		        );
	    	}
	    	if(preCalcListSize) {
	    		QueryExtensionRecord queryExtension = new QueryExtension();
    	    	queryExtension.setClause(
    	    		Database_1_Attributes.HINT_COUNT + "(1=1)"
    	    	);
    	    	preparedFilter.getExtension().add(queryExtension);
	    	}
	        Object allObjects = this.dataBinding.getValue(
	            parent, 
	            control.getObjectContainer().getReferenceName(),
	            view.getApplicationContext()
	        );
	    	if(allObjects instanceof RefContainer) {
	    		@SuppressWarnings("unchecked")
				RefContainer<RefObject_1_0> container = (RefContainer<RefObject_1_0>)allObjects;
    			filteredObjects = container.refGetAll(
    				this.newQuery(container, preparedFilter)
    			);
	    	} else if(
	    		allObjects instanceof Object[] &&
	    		((Object[])allObjects).length == 2 &&
	    		((Object[])allObjects)[0] instanceof RefContainer    		
	    	) {
				@SuppressWarnings("unchecked")
				RefContainer<RefObject_1_0> container = (RefContainer<RefObject_1_0>)((Object[])allObjects)[0];
				QueryFilterRecord query = ((RefQuery_1_0)((Object[])allObjects)[1]).refGetFilter();
				if(preparedFilter != null) {
					// Order specifiers not allowed in data binding
					query.getOrderSpecifier().clear();
					query.getCondition().addAll(preparedFilter.getCondition());
					query.getOrderSpecifier().addAll(preparedFilter.getOrderSpecifier());
					query.getExtension().addAll(preparedFilter.getExtension());
				}
				filteredObjects = container.refGetAll(
					this.newQuery(container, query)
				);
	    	} else if(allObjects instanceof List) {
	    		@SuppressWarnings("unchecked")
				List<RefObject_1_0> objects = (List<RefObject_1_0>)allObjects;
	    		filteredObjects = objects;
	    	} else if(allObjects instanceof Collection) {
	    		@SuppressWarnings("unchecked")
				Collection<RefObject_1_0> objects = (Collection<RefObject_1_0>)allObjects;
	    		filteredObjects = new ArrayList<RefObject_1_0>(objects);
	    	} else {
	    		filteredObjects = Collections.emptyList();
	    	}
        } catch(Exception e) {
            filteredObjects = Collections.emptyList();
            ServiceException e0 = new ServiceException(
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.PROCESSING_FAILURE,
                "error getting filtered objects",
                new BasicException.Parameter("object", view.getObject()),
                new BasicException.Parameter("reference", control.getQualifiedReferenceName()),
                new BasicException.Parameter("filter", preparedFilter),
                new BasicException.Parameter("principal", view.getApplicationContext().getLoginPrincipal())
            );
            SysLog.warning(e0.getMessage(), e0.getCause());        	
        }
    	return filteredObjects;
    }

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.view.Component#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Component> List<T> getChildren(
		Class<T> type
	) {
		return Collections.emptyList();
	}

	//-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3258408426441815605L;

}

//--- End of File -----------------------------------------------------------
