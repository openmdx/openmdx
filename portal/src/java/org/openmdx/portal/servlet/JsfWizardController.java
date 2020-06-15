/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: JsfWizardController
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2019, OMEX AG, Switzerland
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
package org.openmdx.portal.servlet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.attribute.DateValue;

/**
 * JsfWizardController
 *
 */
public abstract class JsfWizardController extends AbstractWizardController {
	
	public static class ObjectReferenceBean {
	
		/**
		 * @return the title
		 */
		public String getTitle() {
			return title;
		}
		/**
		 * @param title the title to set
		 */
		public void setTitle(String title) {
			this.title = title;
		}
		/**
		 * @return the xri
		 */
		public String getXri() {
			return xri;
		}
		/**
		 * @param xri the xri to set
		 */
		public void setXri(String xri) {
			this.xri = xri;
		}
		
		private String title;
		private String xri;
	}

	public static class OptionBean {
		
		/**
		 * @return the value
		 */
		public Short getValue() {
			return value;
		}
		/**
		 * @param value the value to set
		 */
		public void setValue(Short value) {
			this.value = value;
		}
		/**
		 * @return the title
		 */
		public String getTitle() {
			return title;
		}
		/**
		 * @param title the title to set
		 */
		public void setTitle(String title) {
			this.title = title;
		}
		
		private Short value;
		private String title;
	}

	public static class QueryBean {
		
		/**
		 * @return the position
		 */
		public Integer getPosition() {
			return position;
		}
		/**
		 * @param position the position to set
		 */
		public void setPosition(Integer position) {
			this.position = position;
		}
		/**
		 * @return the size
		 */
		public Integer getSize() {
			return size;
		}
		/**
		 * @param size the size to set
		 */
		public void setSize(Integer size) {
			this.size = size;
		}
		/**
		 * @return the query
		 */
		public String getQuery() {
			return query;
		}
		/**
		 * @param query the query to set
		 */
		public void setQuery(String query) {
			this.query = query;
		}
		
		private Integer position;
		private Integer size;
		private String query;
	}

	/**
	 * Constructor 
	 *
	 */
	public JsfWizardController(
	) {
		super();
	}	
	
	/**
	 * Init data record.
	 * 
	 * @return
	 * @throws ServiceException
	 */
	public abstract Map<String,Object> newData() throws ServiceException;

	/**
	 * Init wizard controller.
	 * 
	 */
	public void init(
	) {
		try {
			if(!FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
				super.init(
					(HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest(),
					"UTF-8",
					true, // assertRequestId
					true // assertObjectXri
				);
				this.data = this.newData();
				this.doRefresh(null);
			}
		} catch(Exception e) {
            Throwables.log(e);
		}
	}

    /**
     * Create error message.
     * 
     * @param message
     * @param parameters
     * @return
     */
	public String createErrorMessage(
		String message,
		String[] parameters
	) {
		String preparedMessage = "";
		int i = 0;
		while(i < message.length()) {
			if((i <= message.length()-4) && "${".equals(message.substring(i,i+2))) {
				short index = new Short(message.substring(i+2, i+3)).shortValue();
				try {
					preparedMessage += parameters[index];
				} catch(Exception ignore) {
					SysLog.trace("Exception ignored", ignore);
				}
				
				i += 4;
			} else {
				preparedMessage += message.charAt(i);
				i++;
			}
		}
		return preparedMessage;
	}
		
	/**
	 * Get options for given code container.
	 * 
	 * @param name
	 * @param includeAll
	 * @return
	 * @throws ServiceException
	 */
	public List<JsfWizardController.OptionBean> getOptions(
		String name,
		boolean includeAll
	) throws ServiceException {
		return this.getOptions(
			name,
			this.getApp().getCurrentLocaleAsIndex(),
			includeAll
		);
	}

	/**
	 * Get date format for given element.
	 * 
	 * @return
	 * @throws ServiceException
	 */
	public String getDateFormat(
		String name,
		boolean useEditStyle
	) throws ServiceException {
		ApplicationContext app = this.getApp();
		if(app != null) {
			SimpleDateFormat dateFormat = DateValue.getLocalizedDateTimeFormatter(name, useEditStyle, app);
			return dateFormat.toPattern();
		} else {
			return null;
		}
	}

	/**
	 * Get calendar format for given element.
	 * 
	 * @return
	 * @throws ServiceException
	 */
	public String getCalendarFormat(
		String name,
		boolean useEditStyle
	) throws ServiceException {
		ApplicationContext app = this.getApp();
		if(app != null) {
			SimpleDateFormat dateFormat = DateValue.getLocalizedDateTimeFormatter(name, useEditStyle, app);
			return DateValue.getCalendarFormat(dateFormat);
		} else {
			return null;
		}
	}

	/**
	 * Get options for given code container.
	 * 
	 * @param name
	 * @param locale
	 * @param includeAll
	 * @return
	 * @throws ServiceException
	 */
	public List<JsfWizardController.OptionBean> getOptions(
		String name,
		short locale,
		boolean includeAll
	) throws ServiceException {
		Map<Short,String> codeEntries = codes.getLongTextByCode(name, locale, includeAll);		
		List<JsfWizardController.OptionBean> options = new ArrayList<JsfWizardController.OptionBean>();
		for(Map.Entry<Short,String> codeEntry: codeEntries.entrySet()) {
			JsfWizardController.OptionBean optionBean = new JsfWizardController.OptionBean();
			optionBean.setValue(codeEntry.getKey());
			optionBean.setTitle(codeEntry.getValue());
			options.add(optionBean);
		}
		return options;
	}
	
	/**
	 * Get object reference bean for given object.
	 * 
	 * @param obj
	 * @return
	 */
	protected JsfWizardController.ObjectReferenceBean newObjectReferenceBean(
		RefObject_1_0 obj
	) {
		ApplicationContext app = this.getApp();
		JsfWizardController.ObjectReferenceBean objRef = new JsfWizardController.ObjectReferenceBean();
		objRef.setXri(obj.refGetPath().toXRI());
		objRef.setTitle(
			app.getPortalExtension().getTitle(
				obj, 
				app.getCurrentLocaleAsIndex(), 
				app.getCurrentLocaleAsString(), 
				false, // asShortTitle, 
				app
			)
		);
		return objRef;
	}
		
   	/**
   	 * doRefresh action.
   	 * 
   	 * @param event
   	 * @throws ServiceException
   	 */
   	public void doRefresh(
   		javax.faces.event.AjaxBehaviorEvent event   		
   	) throws ServiceException {
   	}
   	
   	/**
   	 * doCancel action.
   	 * 
   	 * @param event
   	 * @throws ServiceException
   	 */
   	public void doCancel(
   		javax.faces.event.AjaxBehaviorEvent event
   	) throws ServiceException {
   		try {
   			Action exitAction = new ObjectReference(this.getObject(), this.getApp()).getSelectObjectAction();
   			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
   			externalContext.redirect(
   				externalContext.getRequestContextPath() + "/" + exitAction.getEncodedHRef()
   			);
   		} catch(Exception e) {
   			throw new ServiceException(e);
   		}
	}
	
   	/**
   	 * Lookup objects and store result set as List<ObjectReferenceBean> to 'data.var'.
   	 * 
   	 * @param referenceXri
   	 * @param queryType
   	 * @param queryString
   	 * @param var
   	 * @param size
   	 * @throws ServiceException
   	 */
   	public void doLookup(
   		String referenceXri,
   		String queryType,
   		String queryString,
   		String var,
   		int position,
   		int size
   	) throws ServiceException {
   		PersistenceManager pm = this.getPm();
   		Map<String,Object> data = this.getData();
   		List<JsfWizardController.ObjectReferenceBean> result = new ArrayList<JsfWizardController.ObjectReferenceBean>();
   		List<Path> referencePaths = new ArrayList<Path>();
   		if(referenceXri.startsWith("[")) {
   			referenceXri = referenceXri.substring(1, referenceXri.length() - 1);
   			for(String part: referenceXri.split(",")) {
   	   			referencePaths.add(new Path(part));
   			}
   		} else {
   			referencePaths.add(new Path(referenceXri));
   		}
   		List<String> queryTypes = new ArrayList<String>();
   		if(queryType.startsWith("[")) {
   			queryType = queryType.substring(1, queryType.length() - 1);
   			for(String part: queryType.split(",")) {
   				queryTypes.add(part);
   			}
   		} else {
   			queryTypes.add(queryType);
   		}
   		int index = 0;
   		for(Path referencePath: referencePaths) {
	   		Path objectIdentity = new Path(
	   			new String[]{
		   			referencePath.getSegment(0).toClassicRepresentation(),
		   			referencePath.getSegment(1).toClassicRepresentation(),
		   			this.getProviderName(),
		   			referencePath.getSegment(3).toClassicRepresentation(),
		   			this.getSegmentName()
	   			}
	   		);
	   		for(int i = 5; i < referencePath.size() - 1; i++) {
	   			objectIdentity = objectIdentity.getDescendant(referencePath.getSegment(i).toClassicRepresentation());
	   		}
	    	RefObject_1_0 object = (RefObject_1_0)pm.getObjectById(objectIdentity);
	   		String referenceName = referencePath.getLastSegment().toClassicRepresentation();
	        Query_2Facade queryFacade = Facades.newQuery(objectIdentity.getDescendant(referenceName));
	        queryFacade.setQueryType(queryTypes.get(index));
	        queryFacade.setQuery(queryString);
	    	javax.jdo.Query query = pm.newQuery(
	    		org.openmdx.base.persistence.cci.Queries.QUERY_LANGUAGE, 
	    		queryFacade.getDelegate()
	        );
			@SuppressWarnings("unchecked")
			RefContainer<RefObject_1_0> container = (RefContainer<RefObject_1_0>)object.refGetValue(referenceName);
			int count = 0;
			for(Iterator<RefObject_1_0> i = container.refGetAll(query).listIterator(position); i.hasNext(); ) {
				if(count < size) {
					result.add(this.newObjectReferenceBean(i.next()));
				} else {
					break;
				}
				count++;
			}
			index++;
   		}
   		data.put(var, result);
   	}

   	/**
	 * @return the data
	 */
	public Map<String,Object> getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(Map<String,Object> data) {
		this.data = data;
	}

	//-----------------------------------------------------------------------
	// Members
	//-----------------------------------------------------------------------
	protected Map<String,Object> data;
	
}
