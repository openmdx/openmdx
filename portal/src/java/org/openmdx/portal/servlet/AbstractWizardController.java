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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.component.ObjectView;
import org.openmdx.portal.servlet.component.OperationPane;
import org.openmdx.portal.servlet.component.UiOperationTab;
import org.openmdx.portal.servlet.component.ShowObjectView;
import org.openmdx.portal.servlet.control.FormControl;
import org.openmdx.portal.servlet.control.WizardControl;
import org.openmdx.portal.servlet.control.UiWizardTabControl;
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
	 * RequestParameter
	 *
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	public @interface RequestParameter {
		
		String name() default "";
		String type() default "";
	}

	/**
	 * FormParameter
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	public @interface FormParameter {
		
		String[] forms();
		
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
		} catch(Exception ignore) {}
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
					@SuppressWarnings("unchecked")
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
								PrintWriter pw = new PrintWriter(
									new FileOutputStream(location + ".INFO")
								);
								pw.println(item.getContentType());
								int sep = item.getName().lastIndexOf("/");
								if(sep < 0) {
									sep = item.getName().lastIndexOf("\\");
								}
								pw.println(item.getName().substring(sep + 1));
								pw.close();
							}
						}
					}
				} catch(Exception e) {
					new ServiceException(e).log();
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
	 * Invoked by handle() before command-specific method is called.
	 * 
	 * @return
	 * @throws ServiceException
	 */
	protected boolean prepare(
	) throws ServiceException {
		return true;
	}
	
	/**
	 * Init form values. Called after creation of formValues and before mapping request 
	 * parameters to formValues.
	 * 
	 * @param formFields
	 */
	protected void initFormFields(
		Map<String,Object> formFields
	) throws ServiceException {		
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
	 * Handle command. Lookup up a matching controller and invoke the
	 * method corresponding to the command.
	 * 
	 * @param command
	 * @param parameterMap
	 * @throws ServiceException
	 */
	protected boolean forward(
		String command,
		Map<String,String[]> parameterMap
	) throws ServiceException {
		this.forms.clear();
		this.errorMessage = "";
		if(!this.prepare()) {
			return false;
		}
		this.command = command;
		String methodName = "do" + command;
		Method method = null;
		for(Method m: this.getClass().getDeclaredMethods()) {
			if(m.getName().equals(methodName)) {
				method = m;
				break;
			}
		}
		// No action method found in declaring class. Check super types.
		if(method == null) {
			for(Method m: this.getClass().getMethods()) {
				if(m.getName().equals(methodName)) {
					method = m;
					break;
				}
			}		
		}
		if(method != null) {
			List<Object> parameterValues = new ArrayList<Object>();
			for(int i = 0; i < method.getParameterTypes().length; i++) {
				Object parameterValue = null;
				String requestParameterName = null;
				for(Annotation annotation: method.getParameterAnnotations()[i]) {
					// RequestParameter
					if(annotation.annotationType() == RequestParameter.class) {
						RequestParameter requestParameter = (RequestParameter)annotation;
						// RequestParameter specified by name
						if(requestParameter.name() != null && !requestParameter.name().isEmpty()) {
							requestParameterName = requestParameter.name();					
							Class<?> parameterType = method.getParameterTypes()[i];
							parameterValue = parameterType.isArray()
								? this.getParameterValues(parameterMap, requestParameterName)
								: this.getFirstParameterValue(parameterMap, requestParameterName);
							if(parameterType == Boolean.class) {
								if(parameterValue == null) {
									parameterValue = "false";
								} else if("on".equals(parameterValue)) {
									parameterValue = "true";
								}
							}
							if(parameterValue != null) {
								Method valueOfMethod = null;
								try {
									valueOfMethod = parameterType.getMethod("valueOf", String.class);
								} catch(Exception ignore) {}
								if(valueOfMethod != null) {
									try {
										parameterValues.add(
											valueOfMethod.invoke(null, parameterValue)
										);
									} catch(Exception e) {
										parameterValues.add(null);
									}
								} else {
									parameterValues.add(parameterValue);
								}
							} else {
								parameterValues.add(null);
							}
						}
						// RequestParameter specified by type
						else if(requestParameter.type() != null && !requestParameter.type().isEmpty()) {
							String requestParameterType = requestParameter.type();
							// RequestParameter is a bean
							if(requestParameterType.equalsIgnoreCase("Bean")) {
								Class<?> parameterType = method.getParameterTypes()[i];
								try {
									Object bean = parameterType.newInstance();
									BeanInfo beanInfo = Introspector.getBeanInfo(parameterType);
									for(PropertyDescriptor pd: beanInfo.getPropertyDescriptors()) {
										Object propertyValue = this.getFirstParameterValue(parameterMap, pd.getName());
										Class<?> propertyType = pd.getReadMethod().getReturnType();
										if(propertyType == Boolean.class) {
											if(propertyValue == null) {
												propertyValue = "false";
											} else if("on".equals(propertyValue)) {
												propertyValue = "true";
											}
										}
										if(propertyValue != null) {
											Method valueOfMethod = null;
											try {
												valueOfMethod = propertyType.getMethod("valueOf", String.class);
											} catch(Exception ignore) {}
											if(valueOfMethod != null) {
												try {
													pd.getWriteMethod().invoke(
														bean, 
														valueOfMethod.invoke(null, propertyValue)
													);
												} catch(Exception ignore) {}
											} else {
												try {
													pd.getWriteMethod().invoke(bean, propertyValue);
												} catch(Exception ignore) {}
											}
										}
									}
									parameterValues.add(bean);
								} catch(Exception e) {
									parameterValues.add(null);
								}
							} else {
								parameterValues.add(null);
							}
						} else {
							parameterValues.add(null);
						}
					}
					// FormParameter
					else if(annotation.annotationType() == FormParameter.class) {
						String[] formNames = ((FormParameter)annotation).forms();
						Map<String,Object> formValues = new HashMap<String,Object>();
						this.initFormFields(formValues);
						for(String formName: formNames) {
				    		org.openmdx.ui1.jmi1.FormDefinition formDefinition = this.app.getUiFormDefinition(formName);
				    		if(formDefinition != null) {
					    		FormControl form = new org.openmdx.portal.servlet.control.FormControl(
									formDefinition.refGetPath().getLastSegment().toClassicRepresentation(),
									this.app.getCurrentLocaleAsString(),
									this.app.getCurrentLocaleAsIndex(),
									this.app.getUiContext(),
									formDefinition
								);
								form.updateObject(
									parameterMap,
									formValues,
									this.app,
									this.pm
								);
								this.forms.put(
									formName,
									form
								);
				    		}
						}
						parameterValues.add(formValues);
					} else {
						parameterValues.add(null);
					}
				}
			}
			try {
				method.invoke(
					this, 
					parameterValues.toArray(new Object[parameterValues.size()])
				);
			} catch(Exception e) {
				this.errorMessage = "An unexecpted error occurred. The error message is " + e.getMessage() + ". For more information inspect the server log.";
				new ServiceException(e).log();
			}
			return true;
		} else {
			return false;
		}		
	}

	/**
	 * Handle command. Lookup up a matching controller and invoke the
	 * method corresponding to the command.
	 * 
	 * @param command
	 * @return
	 * @throws ServiceException
	 */
	public boolean handle(
		String command
	) throws ServiceException {
		return this.forward(
			command, 
			this.parameterMap
		);		
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
	 * Retrieve exitAction.
	 *
	 * @return Returns the exitAction.
	 */
	public Action getExitAction(
	) {
		return this.exitAction;
	}

	/**
	 * Set exitAction.
	 * 
	 * @param exitAction The exitAction to set.
	 */
	public void setExitAction(
		Action exitAction
	) {
		this.exitAction = exitAction;
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
	 * Retrieve forms.
	 *
	 * @return Returns the forms.
	 */
	public Map<String,FormControl> getForms(
	) {
		return this.forms;
	}

	/**
	 * Set forms.
	 * 
	 * @param forms The forms to set.
	 */
	public void setForms(
		Map<String,FormControl> forms
	) {
		this.forms = forms;
	}

	/**
	 * Get first form.
	 * 
	 * @return
	 */
	public FormControl getForm(
	) {
		return this.forms == null || this.forms.isEmpty()
			? null
			: this.forms.values().iterator().next();
	}

	/**
	 * Retrieve command.
	 *
	 * @return Returns the command.
	 */
	public String getCommand(
	) {
		return this.command;
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
			String pattern = this.getWizardName().toLowerCase().replace("_", "") + ".";
			ShowObjectView currentView = (ShowObjectView)this.getCurrentView();
			for(OperationPane operationPane: currentView.getChildren(OperationPane.class)) {
				for(UiOperationTab operationTab: operationPane.getChildren(UiOperationTab.class)) {
					String name = operationTab.getOperationName().toLowerCase().replace("_", "");
					if(name.indexOf(pattern) > 0) {
						return operationTab.getToolTip();
					}
				}
			}
			for(WizardControl wizardControl: currentView.getControl().getChildren(WizardControl.class)) {
				for(UiWizardTabControl wizardTabControl: wizardControl.getChildren(UiWizardTabControl.class)) {
					String qualifiedOperationName = wizardTabControl.getQualifiedOperationName().toLowerCase().replace("_", "");
					if(qualifiedOperationName.indexOf(pattern) > 0) {
						return wizardTabControl.getToolTip();
					}
				}
			}
		}
		return "";
	}

	/**
	 * Retrieve errorMessage.
	 *
	 * @return Returns the errorMessage.
	 */
	public String getErrorMessage(
	) {
		return this.errorMessage;
    }

	//-----------------------------------------------------------------------
	// Members
	//-----------------------------------------------------------------------
	public static final String DEFAULT_CONTROLLER_ID = "Default";

	private Action exitAction;
	private String command;
	private HttpServletRequest request;
	private String requestId;
	private HttpSession session;
	private ApplicationContext app;
	private PersistenceManager pm;
	private RefObject_1_0 object;
	private Path objectIdentity;
	private ObjectView currentView;
	private Texts_1_0 texts;
	private Codes codes;
	private String providerName;
	private String segmentName;
	protected String errorMessage;
	private Map<String,FormControl> forms = new HashMap<String,FormControl>();
	private Map<String,String[]> parameterMap = null;

}
