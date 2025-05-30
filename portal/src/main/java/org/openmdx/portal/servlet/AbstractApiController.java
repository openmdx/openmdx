/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: AbstractApiController
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

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
#if JAVA_8
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
#else
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
#endif

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * AbstractApiController
 *
 */
public abstract class AbstractApiController {

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

	/**
	 * VoidBean
	 *
	 */
	public static class VoidBean {
		
	}

	/**
	 * Constructor 
	 *
	 */
	public AbstractApiController(
	) {
	}
	
	/**
	 * Init request dispatcher.
	 * 
	 * @param request
	 * @param encoding
     */
	public boolean init(
		HttpServletRequest request,
		String encoding
	) {
		try {
			request.setCharacterEncoding(encoding);
		} catch(Exception ignore) {
			SysLog.trace("Exception ignored", ignore);
		}
		this.request = request;
		this.session = request.getSession();
		this.app = (ApplicationContext)this.session.getAttribute(WebKeys.APPLICATION_KEY);
		this.parameterMap = this.request.getParameterMap();
		this.requestId = this.getFirstParameterValue(
			this.parameterMap, 
			Action.PARAMETER_REQUEST_ID
		);
		this.pm = this.app.getNewPmData();
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
	 * Get element label.
	 * 
	 * @param qualifiedElementName
	 * @return
	 * @throws ServiceException
	 */
	protected String getLabel(
		String qualifiedElementName
	) throws ServiceException {
		ApplicationContext app = this.getApp();
		List<String> labels = app.getUiElementDefinition(qualifiedElementName).getLabel();
		return app.getCurrentLocaleAsIndex() < labels.size()
			? labels.get(app.getCurrentLocaleAsIndex())
			: labels.get(0);
	}

	/**
	 * Get element tool tips.
	 * 
	 * @param qualifiedElementName
	 * @return
	 * @throws ServiceException
	 */
	protected String getToolTip(
		String qualifiedElementName
	) throws ServiceException {
		ApplicationContext app = this.getApp();
		List<String> toolTips = app.getUiElementDefinition(qualifiedElementName).getToolTip();
		return app.getCurrentLocaleAsIndex() < toolTips.size()
			? toolTips.get(app.getCurrentLocaleAsIndex())
			: toolTips.get(0);
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
	protected List<OptionBean> getOptions(
		String name,
		short locale,
		boolean includeAll
	) throws ServiceException {
		Map<Short,String> codeEntries = codes.getLongTextByCode(name, locale, includeAll);		
		List<OptionBean> options = new ArrayList<OptionBean>();
		for(Map.Entry<Short,String> codeEntry: codeEntries.entrySet()) {
			OptionBean optionBean = new OptionBean();
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
	protected ObjectReferenceBean newObjectReferenceBean(
		RefObject_1_0 obj
	) {
		ApplicationContext app = this.getApp();
		ObjectReferenceBean objRef = new ObjectReferenceBean();
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
	 * Invoke the method corresponding to the command.
	 * 
	 * @param command
	 * @param position
	 * @param size
	 * @param reader
	 * @return
	 * @throws ServiceException
	 */
	protected Object forward(
		String command,
		Path xri,
		Reader reader
	) throws ServiceException {
		if(!this.prepare()) {
			return false;
		}
		this.command = command;
		String methodName = command;
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
			Class<?>[] parameterTypes = method.getParameterTypes();
			// get...(path)
			if(
				parameterTypes.length == 1 &&
				command.startsWith("get") &&
				parameterTypes[0] == Path.class
			) {
				try {
					return method.invoke(
						this, 
						xri 
					);
				} catch(InvocationTargetException e) {
		        	ServiceException e0 = new ServiceException(
	                	BasicException.toStackedException(
	                		e.getTargetException(),
	                		e,
	    	                BasicException.Code.DEFAULT_DOMAIN,
	    	                BasicException.Code.SYSTEM_EXCEPTION,
	    	                "Error invoking method " + command
	                	));
		        	throw e0;
				} catch(Exception e) {
					throw new ServiceException(e);
				}
			} else if(
				parameterTypes.length == 2 &&
				parameterTypes[0] == Path.class
			) {
				try {
					return method.invoke(
						this, 
						xri,
						this.fromJson(reader, parameterTypes[1])
					);
				} catch(InvocationTargetException e) {
		        	ServiceException e0 = new ServiceException(
	                	BasicException.toStackedException(
	                		e.getTargetException(),
	                		e,
	    	                BasicException.Code.DEFAULT_DOMAIN,
	    	                BasicException.Code.SYSTEM_EXCEPTION,
	    	                "Error invoking method " + command
	                	));
		        	throw e0;
				} catch(Exception e) {
					throw new ServiceException(e);
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Read Json and map to bean.
	 * 
	 * @param reader
	 * @param clazz
	 * @return
	 * @throws ServiceException
	 */
	public abstract Object fromJson(
		Reader reader,
		Class<?> clazz
	) throws ServiceException;

	/**
	 * Stringify object as Json.
	 * 
	 * @param object
	 * @return
	 * @throws ServiceException
	 */
	public abstract String toJson(
		Object object
	) throws ServiceException;

	/**
	 * Handle the requested command. Invoke the method corresponding to the command.
	 * 
	 * @param command
	 * @return
	 * @throws ServiceException
	 */
	public Object handle(
	) throws ServiceException {
		Path xri = new Path(this.getFirstParameterValue(this.parameterMap, "xri"));
		// PUT|POST
		if("PUT".equalsIgnoreCase(this.request.getMethod()) || "POST".equalsIgnoreCase(this.request.getMethod())) {
			SysLog.detail("PUT {0}", xri.toXRI());
			Reader reader;
			try {
				reader = new InputStreamReader(this.request.getInputStream(), "UTF-8");
			} catch(Exception ignore) {
				reader = null;
			}
			String command = xri.getLastSegment().toClassicRepresentation();
			return this.forward(
				command,
				xri,
				reader
			);
		} else {
			SysLog.detail("GET {0}", xri.toXRI());
			String command = xri.getLastSegment().toClassicRepresentation();
			return this.forward(
				command,
				xri,
				null
			);
		}
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
	 * Retrieve app.
	 *
	 * @return Returns the app.
	 */
	public ApplicationContext getApp(
	) {
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
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * @return the parameterMap
	 */
	public Map<String, String[]> getParameterMap() {
		return parameterMap;
	}

	/**
	 * @param parameterMap the parameterMap to set
	 */
	public void setParameterMap(Map<String, String[]> parameterMap) {
		this.parameterMap = parameterMap;
	}

	//-----------------------------------------------------------------------
	// Members
	//-----------------------------------------------------------------------
	private HttpServletRequest request;
	private String command;	
	private String requestId;
	private HttpSession session;
	private ApplicationContext app;
	private PersistenceManager pm;
	private Texts_1_0 texts;
	private Codes codes;
	private Map<String,String[]> parameterMap = null;
}
