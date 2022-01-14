/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: JspWizardController
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.control.FormControl;

/**
 * JspWizardController
 *
 */
public class JspWizardController extends AbstractWizardController {
	
	/**
	 * Constructor 
	 *
	 */
	public JspWizardController(
	) {
		super();
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
					if(annotation.annotationType() == JspWizardController.RequestParameter.class) {
						JspWizardController.RequestParameter requestParameter = (JspWizardController.RequestParameter)annotation;
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
						        } catch(Exception ignore) {
									SysLog.trace("Exception ignored", ignore);
								}
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
					else if(annotation.annotationType() == JspWizardController.FormParameter.class) {
						String[] formNames = ((JspWizardController.FormParameter)annotation).forms();
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
	            Throwables.log(e);
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
	 * Retrieve exitAction.
	 *
	 * @return Returns the exitAction.
	 */
	public Action getExitAction(
	) {
		return this.exitAction;
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
	private Action exitAction;
	private Map<String,FormControl> forms = new HashMap<String,FormControl>();
	private String command;
	protected String errorMessage;
		
}
