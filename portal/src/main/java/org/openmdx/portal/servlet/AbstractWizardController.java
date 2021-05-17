/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: AbstractWizardController
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2012, OMEX AG, Switzerland
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.component.ObjectView;
import org.openmdx.portal.servlet.component.OperationPane;
import org.openmdx.portal.servlet.component.ShowObjectView;
import org.openmdx.portal.servlet.component.UiOperationTab;
import org.openmdx.portal.servlet.control.UiWizardTabControl;
import org.openmdx.portal.servlet.control.WizardControl;
import org.openmdx.uses.org.apache.commons.fileupload.DiskFileUpload;
import org.openmdx.uses.org.apache.commons.fileupload.FileItem;
import org.openmdx.uses.org.apache.commons.fileupload.FileUpload;

/**
 * AbstractWizardController
 *
 */
public abstract class AbstractWizardController {

	/**
	 * Constructor 
	 *
	 */
	public AbstractWizardController(
	) {
	}
	
	/**
	 * Init request dispatcher.
	 * 
	 * @param request
	 * @param assertRequestId
	 * @param assertObjectXri
	 */
	public boolean init(
		HttpServletRequest request,
		String encoding,
		boolean assertRequestId,
		boolean assertObjectXri
	) {
		try {
			request.setCharacterEncoding(encoding);
		} catch(Exception ignore) {
			SysLog.trace("Exception ignored", ignore);
		}
		this.request = request;
		this.session = request.getSession();
		this.app = (ApplicationContext)this.session.getAttribute(WebKeys.APPLICATION_KEY);
		// Get parameter map
		{
			this.parameterMap = null;
			if(FileUpload.isMultipartContent(this.getRequest())) {
				this.parameterMap = new HashMap<String,String[]>();
				DiskFileUpload upload = new DiskFileUpload();
				upload.setHeaderEncoding("UTF-8");
				try {
					List<FileItem> items = upload.parseRequest(
						this.getRequest(),
						200, // in-memory threshold. Content for fields larger than threshold is written to disk
						50000000, // max request size [overall limit]
						this.app.getTempDirectory().getPath()
					);
					for(FileItem item: items) {
						if(item.isFormField()) {
							this.parameterMap.put(
								item.getFieldName(),
								new String[]{item.getString("UTF-8")}
							);
						} else {
							// reset binary
							if("#NULL".equals(item.getName())) {
								this.parameterMap.put(
									item.getFieldName(),
									new String[]{item.getName()}
								);
							} else if(item.getSize() > 0) {
								// add to parameter map if file received
								this.parameterMap.put(
									item.getFieldName(),
									new String[]{item.getName()}
								);
								String location = this.app.getTempFileName(item.getFieldName(), "");
								// bytes
								File outFile = new File(location);
								item.write(outFile);
								// type
								try(
    								PrintWriter pw = new PrintWriter(
    									new FileOutputStream(location + ".INFO")
    								)
    							){
    								pw.println(item.getContentType());
    								int sep = item.getName().lastIndexOf("/");
    								if(sep < 0) {
    									sep = item.getName().lastIndexOf("\\");
    								}
    								pw.println(item.getName().substring(sep + 1));
								}
							}
						}
					}
				} catch(Exception e) {
		            Throwables.log(e);
					SysLog.warning("Unable to parse multipart request", e.getMessage());
					this.parameterMap = Collections.emptyMap();
				}
			} else {
				this.parameterMap = this.request.getParameterMap();
			}
		}
		this.requestId = this.getFirstParameterValue(
			this.parameterMap, 
			Action.PARAMETER_REQUEST_ID
		);
		String objectXri = this.getFirstParameterValue(
			this.parameterMap,
			Action.PARAMETER_OBJECTXRI
		);
		ViewsCache showViewsCache = (ViewsCache)this.session.getAttribute(WebKeys.VIEW_CACHE_KEY_SHOW);			
		ViewsCache editViewsCache = (ViewsCache)this.session.getAttribute(WebKeys.VIEW_CACHE_KEY_EDIT);			
		ObjectView objectView = showViewsCache.getView(this.requestId) == null
			? editViewsCache.getView(this.requestId)
			: showViewsCache.getView(this.requestId);
		if(
			(this.app == null) ||
			(assertObjectXri && objectXri == null) || 
			(assertRequestId && objectView == null)
		) {
			return false;
		}
		this.currentView = objectView;
		this.pm = this.app.getNewPmData();
		if(assertObjectXri) {
			this.objectIdentity = new Path(objectXri);
			this.object = (RefObject_1_0)this.pm.getObjectById(this.objectIdentity);
			this.providerName = this.object.refGetPath().getSegment(2).toClassicRepresentation();
			this.segmentName = this.object.refGetPath().getSegment(4).toClassicRepresentation();		
		}
		this.texts = this.app.getTexts();
		this.codes = this.app.getCodes();
		return true;
	}

	/**
	 * Get first value of given parameter.
	 * 
	 * @param parameterName
	 * @return
	 */
	public String getRequestParameter(
		String parameterName
	) {
		return this.parameterMap.get(parameterName) == null || this.parameterMap.get(parameterName).length == 0
			? null
			: this.parameterMap.get(parameterName)[0];		
	}

	/**
	 * Get values of given parameter.
	 * 
	 * @param parameterMap
	 * @param parameterName
	 * @return
	 */
	protected String[] getParameterValues(
		Map<String,String[]> parameterMap,
		String parameterName
	) {
		return parameterMap.get(parameterName) == null || parameterMap.get(parameterName).length == 0
			? null
			: parameterMap.get(parameterName);		
	}

	/**
	 * Get first value of given parameter.
	 * 
	 * @param parameterMap
	 * @param parameterName
	 * @return
	 */
	protected String getFirstParameterValue(
		Map<String,String[]> parameterMap,
		String parameterName
	) {
		String[] parameterValues = this.getParameterValues(parameterMap, parameterName);
		return parameterValues == null || parameterValues.length == 0
			? null 
			: parameterValues[0];
	}

	/**
	 * Close controller and associated resources.
	 * 
	 * @throws ServiceException
	 */
	public void close(
	) throws ServiceException {
		if(this.pm != null) {
			try {
				this.pm.close();
			} catch(Exception ignore) {}
		}
	}
	
	/**
	 * Get label for given feature.
	 * 
	 * @param forClass
	 * @param featureName
	 * @param locale
	 * @return
	 * @throws ServiceException
	 */
	public String getFieldLabel(
		String forClass,
		String featureName,
		short locale
	) throws ServiceException {
		if(this.currentView != null) {
			return this.currentView.getFieldLabel(forClass, featureName, locale);
		} else {
			return "";
		}
	}

	/**
	 * Retrieve app.
	 *
	 * @return Returns the app.
	 */
	public ApplicationContext getApp() {
		return this.app;
	}

	/**
	 * Set app.
	 * 
	 * @param app The app to set.
	 */
	public void setApp(
		ApplicationContext app
	) {
		this.app = app;
	}

	/**
	 * Retrieve pm.
	 *
	 * @return Returns the pm.
	 */
	public PersistenceManager getPm(
	) {
		return this.pm;
	}

	/**
	 * Set pm.
	 * 
	 * @param pm The pm to set.
	 */
	public void setPm(
		PersistenceManager pm
	) {
		this.pm = pm;
	}

	/**
	 * Retrieve object.
	 *
	 * @return Returns the object.
	 */
	public RefObject_1_0 getObject(
	) {
		return this.object;
	}

	/**
	 * Set object.
	 * 
	 * @param object The object to set.
	 */
	public void setObject(
		RefObject_1_0 object
	) {
		this.object = object;
	}

	/**
	 * Retrieve objectIdentity.
	 *
	 * @return Returns the objectIdentity.
	 */
	public Path getObjectIdentity(
	) {
		return this.objectIdentity;
	}

	/**
	 * Set objectIdentity.
	 * 
	 * @param objectIdentity The objectIdentity to set.
	 */
	public void setObjectIdentity(
		Path objectIdentity
	) {
		this.objectIdentity = objectIdentity;
	}

	/**
	 * Retrieve texts.
	 *
	 * @return Returns the texts.
	 */
	public Texts_1_0 getTexts(
	) {
		return this.texts;
	}

	/**
	 * Set texts.
	 * 
	 * @param texts The texts to set.
	 */
	public void setTexts(
		Texts_1_0 texts
	) {
		this.texts = texts;
	}

	/**
	 * Retrieve codes.
	 *
	 * @return Returns the codes.
	 */
	public Codes getCodes(
	) {
		return this.codes;
	}

	/**
	 * Set codes.
	 * 
	 * @param codes The codes to set.
	 */
	public void setCodes(
		Codes codes
	) {
		this.codes = codes;
	}

	/**
	 * Retrieve providerName.
	 *
	 * @return Returns the providerName.
	 */
	public String getProviderName(
	) {
		return this.providerName;
	}

	/**
	 * Set providerName.
	 * 
	 * @param providerName The providerName to set.
	 */
	public void setProviderName(
		String providerName
	) {
		this.providerName = providerName;
	}

	/**
	 * Retrieve segmentName.
	 *
	 * @return Returns the segmentName.
	 */
	public String getSegmentName(
	) {
		return this.segmentName;
	}

	/**
	 * Set segmentName.
	 * 
	 * @param segmentName The segmentName to set.
	 */
	public void setSegmentName(
		String segmentName
	) {
		this.segmentName = segmentName;
	}

	/**
	 * Retrieve request.
	 *
	 * @return Returns the request.
	 */
	public HttpServletRequest getRequest(
	) {
		return this.request;
	}

	/**
	 * Set request.
	 * 
	 * @param request The request to set.
	 */
	public void setRequest(
		HttpServletRequest request
	) {
		this.request = request;
	}
		
	/**
	 * Retrieve session.
	 *
	 * @return Returns the session.
	 */
	public HttpSession getSession(
	) {
		return this.session;
	}

	/**
	 * Set session.
	 * 
	 * @param session The session to set.
	 */
	public void setSession(
		HttpSession session
	) {
		this.session = session;
	}

	/**
	 * Get servlet path.
	 * 
	 * @return
	 */
	public String getServletPath(
	) {
		return "." + this.request.getServletPath();
	}
	
	/**
	 * Get servlet path up to last /
	 * 
	 * @return
	 */
	public String getServletPathPrefix(
	) {
		String servletPath = this.getServletPath();
		return servletPath.substring(0, servletPath.lastIndexOf("/") + 1);
	}

	/**
	 * Retrieve requestId.
	 *
	 * @return Returns the requestId.
	 */
	public String getRequestId(
	) {
		return this.requestId;
	}

	/**
	 * Set requestId.
	 * 
	 * @param requestId The requestId to set.
	 */
	public void setRequestId(
		String requestId
	) {
		this.requestId = requestId;
	}

	/**
	 * Retrieve currentView.
	 *
	 * @return Returns the currentView.
	 */
	public ObjectView getCurrentView(
	) {
		return this.currentView;
	}
	
	/**
	 * Set view.
	 * 
	 * @param view The currentView to set.
	 */
	public void setCurrentView(
		ObjectView currentView
	) {
		this.currentView = currentView;
	}
	
	/**
	 * @return the wizardName
	 */
	public String getWizardName(
	) {
		String controllerName = this.getClass().getSimpleName();
		return controllerName.endsWith("Controller") 
			? controllerName.substring(0, controllerName.length() - 10)
			: controllerName;
	}

	/**
	 * Get wizard's localized toolTip. Returns empty string if not found.
	 * 
	 * @return
	 */
	public String getToolTip(
	) {
		if(this.getCurrentView() != null) {
			String pattern1 = this.getWizardName().toLowerCase().replace("_", "") + ".";
			String pattern2 = this.getWizardName().toLowerCase().replace("_", "") + "/index.jsp";
			String pattern3 = this.getWizardName().toLowerCase().replace("_", "") + "/index.xhtml";
			ShowObjectView currentView = (ShowObjectView)this.getCurrentView();
			for(OperationPane operationPane: currentView.getChildren(OperationPane.class)) {
				for(UiOperationTab operationTab: operationPane.getChildren(UiOperationTab.class)) {
					String name = operationTab.getOperationName().toLowerCase().replace("_", "");
					if(
						name.indexOf(pattern1) > 0 ||
						name.indexOf(pattern2) > 0 ||
						name.indexOf(pattern3) > 0
					) {
						return operationTab.getToolTip();
					}
				}
			}
			for(WizardControl wizardControl: currentView.getControl().getChildren(WizardControl.class)) {
				for(UiWizardTabControl wizardTabControl: wizardControl.getChildren(UiWizardTabControl.class)) {
					String qualifiedOperationName = wizardTabControl.getQualifiedOperationName().toLowerCase().replace("_", "");
					if(
						qualifiedOperationName.indexOf(pattern1) > 0 ||
						qualifiedOperationName.indexOf(pattern2) > 0 ||
						qualifiedOperationName.indexOf(pattern3) > 0
					) {
						return wizardTabControl.getToolTip();
					}
				}
			}
		}
		return "";
	}

	/**
	 * Get element label.
	 * 
	 * @param qualifiedElementName
	 * @return
	 * @throws ServiceException
	 */
	public String getLabel(
		String qualifiedElementName
	) throws ServiceException {
		ApplicationContext app = this.getApp();
		org.openmdx.ui1.jmi1.ElementDefinition elementDefinition = app.getUiElementDefinition(qualifiedElementName);
		if(elementDefinition == null || !Boolean.TRUE.equals(elementDefinition.isActive())) {
			return null;
		} else {
			List<String> labels = app.getUiElementDefinition(qualifiedElementName).getLabel();
			return app.getCurrentLocaleAsIndex() < labels.size()
				? labels.get(app.getCurrentLocaleAsIndex())
				: labels.get(0);
		}
	}

	/**
	 * Get element tool tips.
	 * 
	 * @param qualifiedElementName
	 * @return
	 * @throws ServiceException
	 */
	public String getToolTip(
		String qualifiedElementName
	) throws ServiceException {
		ApplicationContext app = this.getApp();
		org.openmdx.ui1.jmi1.ElementDefinition elementDefinition = app.getUiElementDefinition(qualifiedElementName);
		if(elementDefinition == null) {
			return null;
		} else {
			List<String> toolTips = app.getUiElementDefinition(qualifiedElementName).getToolTip();
			return app.getCurrentLocaleAsIndex() < toolTips.size()
				? toolTips.get(app.getCurrentLocaleAsIndex())
				: toolTips.get(0);
		}
	}
	
	//-----------------------------------------------------------------------
	// Members
	//-----------------------------------------------------------------------
	public static final String DEFAULT_CONTROLLER_ID = "Default";

	private HttpServletRequest request;
	private String requestId;
	private HttpSession session;
	protected ApplicationContext app;
	protected PersistenceManager pm;
	private RefObject_1_0 object;
	private Path objectIdentity;
	private ObjectView currentView;
	private Texts_1_0 texts;
	protected Codes codes;
	private String providerName;
	private String segmentName;
	protected Map<String,String[]> parameterMap = null;

}
