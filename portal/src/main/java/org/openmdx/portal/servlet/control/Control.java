/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: Control
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
package org.openmdx.portal.servlet.control;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jmi.reflect.RefObject;
import #if JAVA_8 javax.servlet.http.HttpServletRequest #else jakarta.servlet.http.HttpServletRequest#endif;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.openmdx.base.accessor.jmi.spi.RefMetaObject_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.ViewPort;

/**
 * Control
 *
 */
public abstract class Control implements Serializable {
  
    /**
     * ScriptEngine
     *
     */
    public static class ScriptEngine {

        private static class ScriptCacheEntry {
            private Class<?> scriptClass; 
        }

        public ScriptEngine(
            URL[] roots
        ) {
            this.roots = roots;
        }

        public ScriptEngine(
            URL[] roots, 
            ClassLoader parentClassLoader
        ) {
            this(roots);
        }

        private ScriptCacheEntry getScript(
            String scriptName 
        ) throws ResourceException, ScriptException {
            ScriptCacheEntry entry;
            scriptName = scriptName.intern();
            entry = (ScriptCacheEntry)this.scriptCache.get(scriptName);            
            if(entry == null) {
                entry = new ScriptCacheEntry();
                try {
                    entry.scriptClass = null;
                    for(int i = 0; i < this.roots.length; i++) {
                        URL scriptURL = new URL(this.roots[i] + "/" + scriptName);
                        try {
                            entry.scriptClass = this.groovyClassLoader.parseClass(scriptURL.openStream(), scriptName);
                            break;
                        }
                        catch(IOException e) {}
                    }
                    if(entry.scriptClass == null) {
                        throw new ScriptException("Could not locate script: " + scriptName);                    
                    }
                } catch(Exception e) {
                    throw new ScriptException("Could not parse script: " + scriptName, e);
                }
                final ScriptCacheEntry currentEntry = this.scriptCache.putIfAbsent(
                    scriptName, 
                    entry
                );
                if(currentEntry != null) {
                	entry = currentEntry;
                }
            }
            return entry;
        }

        public Object run(
            String scriptName, 
            Binding binding
        ) throws ResourceException, ScriptException {
            ScriptCacheEntry entry = this.getScript(scriptName);
            Script scriptObject = InvokerHelper.createScript(entry.scriptClass, binding);
            return scriptObject.run();
        }

        //-----------------------------------------------------------------------
        // Members
        //-----------------------------------------------------------------------
        private URL[] roots;
        private final GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
        private ConcurrentHashMap<String,Control.ScriptEngine.ScriptCacheEntry> scriptCache = 
            new ConcurrentHashMap<String,Control.ScriptEngine.ScriptCacheEntry>();
        
    }
    
	/**
     * Constructor.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     */
    public Control(
        String id,
        String locale,
        int localeAsIndex
    ) {
        this.id = id;
        this.locale = locale;
        this.localeAsIndex = localeAsIndex;
    }

    /**
     * Return UUID.
     * 
     * @return
     */
    protected String uuidAsString(
    ) {
        return UUIDConversion.toUID(UUIDs.newUUID());
    }    

    /**
     * Get locale for this control.
     * 
     * @return
     */
    protected Locale getCurrentLocale(
    ) {
        return new Locale(
            this.locale.substring(0, 2), 
            this.locale.substring(locale.indexOf("_") + 1)
        );      
    }

    /**
     * Set id for control.
     * 
     * @param id
     */
    public void setId(
        String id
    ) {
        this.id = id;
    }
    
    /**
     * Get id for control.
     * 
     * @return
     */
    public String getId(
    ) {
        return this.id;
    }
    
    /**
     * Map feature name to property name.
     * 
     * @param featureName
     * @param type
     * @return
     */
    public String getPropertyName(
        String featureName,
        String type
    ) {
        return featureName.replace(':', '.') + "." + type;
    }
  
    /**
     * Get script location for this control.
     * 
     * @param request
     * @return
     * @throws MalformedURLException
     */
    protected URL getScriptLocation(
        HttpServletRequest request
    ) throws MalformedURLException {
        return request.getSession().getServletContext().getResource(
            "/WEB-INF/config/control/" + this.id
        );
    }

    /**
     * Return child controls for this control.
     * 
     * @return
     */
	public abstract <T extends Control> List<T> getChildren(
		Class<T> type
	);

    /**
     * Paint the control to the view port. By default paint is implemented by the
     * script configured for this control. Override this method for custom-specific
     * behaviour.
     * 
     * @param p
     * @param frame
     * @param forEditing
     * @throws ServiceException
     */
    public void paint(
        ViewPort p,
        String frame,
        boolean forEditing
    ) throws ServiceException {
        try {
            URL groovyURL = this.getScriptLocation(p.getHttpServletRequest());
            if(groovyURL != null) {
                Map<String,ScriptEngine> engines = scriptEngines.get();
                ScriptEngine scriptEngine = engines.get(this.id);
                if(scriptEngine == null) {
                    engines.put(
                        this.id,
                        scriptEngine = new ScriptEngine(
                            new URL[]{groovyURL}
                        )
                    );
                }
                Binding binding = new Binding();
                binding.setVariable("p", p);
                binding.setVariable("frame", frame);
                binding.setVariable("forEditing", forEditing);
                binding.setVariable("control", this);
                binding.setVariable("id", this.id);
                String scriptName = "Default.groovy";
                // Try to find a model-specific script
                if(p.getView().getObject() instanceof RefObject) {
                    RefMetaObject_1 classDef = (RefMetaObject_1)((RefObject)p.getView().getObject()).refMetaObject();
                    String qualifiedClassName = (String)classDef.getElementDef().getQualifiedName();
                    String qualifiedPackageName = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf(":"));
                    String qualifiedScriptName = this.id + ":" + qualifiedPackageName;
                    String name = scriptNames.get(qualifiedScriptName);
                    if(name == null) {
                        name = qualifiedPackageName.replace(":", ".") + ".groovy";
                        URL realPath = this.getScriptLocation(p.getHttpServletRequest());
                        if(!new File(realPath + "/" + name).exists()) {
                            name = scriptName;   
                        }
                        scriptNames.putIfAbsent(
                            qualifiedScriptName, 
                            name
                        );
                    }
                    scriptName = name;
                }
                scriptEngine.run(
                   scriptName, 
                   binding
                );
            }
        } catch(Exception e) {
        	SysLog.warning("Script exception", e);
            Throwables.log(e);
        }
    }
  
    /**
     * Paint the control to the view port. By default paint is implemented by the
     * script configured for this control. Override this method for custom-specific
     * behaviour.

     * @param p
     * @param forEditing
     * @throws ServiceException
     */
    public void paint(
        ViewPort p,
        boolean forEditing
    ) throws ServiceException {
        this.paint(
            p,
            null,
            forEditing
        );
    }
  
    //-------------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
	private static final long serialVersionUID = 8303795984938379119L;

    protected static ThreadLocal<Map<String,ScriptEngine>> scriptEngines = 
        new ThreadLocal<Map<String,ScriptEngine>>() {
            protected synchronized Map<String,ScriptEngine> initialValue() {
                return new HashMap<String,ScriptEngine>();
            }
        };
    protected static ConcurrentMap<String,String> scriptNames = 
        new ConcurrentHashMap<String,String>();
  
    protected String id;
    protected String locale;
    protected int localeAsIndex;
  
}

//--- End of File -----------------------------------------------------------
