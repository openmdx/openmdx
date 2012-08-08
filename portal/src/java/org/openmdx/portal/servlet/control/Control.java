/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: Control.java,v 1.27 2009/09/25 12:02:37 wfro Exp $
 * Description: Control
 * Revision:    $Revision: 1.27 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/09/25 12:02:37 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jmi.reflect.RefObject;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.openmdx.base.accessor.jmi.spi.RefMetaObject_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.ViewPort;

public abstract class Control
    implements Serializable {
  
    //-------------------------------------------------------------------------
    public Control(
        String id,
        String locale,
        int localeAsIndex
    ) {
        this.id = id;
        this.locale = locale;
        this.localeAsIndex = localeAsIndex;
    }

    //-------------------------------------------------------------------------
    protected Locale getCurrentLocale(
    ) {
        return new Locale(
            this.locale.substring(0, 2), 
            this.locale.substring(locale.indexOf("_") + 1)
        );      
    }

    //-------------------------------------------------------------------------
    public void setId(
        String id
    ) {
        this.id = id;
    }
    
    //-------------------------------------------------------------------------
    public String getId(
    ) {
        return this.id;
    }
    
    //-------------------------------------------------------------------------
    public String getPropertyName(
        String featureName,
        String type
    ) {
        return featureName.replace(':', '.') + "." + type;
    }
  
    //-------------------------------------------------------------------------
    protected URL getGroovyURL(
        HttpServletRequest request
    ) throws MalformedURLException {
        return request.getSession().getServletContext().getResource(
            "/WEB-INF/config/control/" + this.id
        );
    }
    
    //-------------------------------------------------------------------------
    public void paint(
        ViewPort p,
        String frame,
        boolean forEditing
    ) throws ServiceException {
        try {
            URL groovyURL = this.getGroovyURL(p.getHttpServletRequest());
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
                    String qualifiedClassName = (String)classDef.getElementDef().objGetValue("qualifiedName");
                    String qualifiedPackageName = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf(":"));
                    String qualifiedScriptName = this.id + ":" + qualifiedPackageName;
                    String name = scriptNames.get(qualifiedScriptName);
                    if(name == null) {
                        name = qualifiedPackageName.replace(":", ".") + ".groovy";
                        URL realPath = this.getGroovyURL(p.getHttpServletRequest());
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
        }
        catch(Exception e) {
        	SysLog.warning("Script exception", e);
            new ServiceException(e).log();
        }
    }
  
    //-------------------------------------------------------------------------
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
    // A map of script engines for each thread
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
  
    //-------------------------------------------------------------------------
    public static class ScriptEngine {

        private static class ScriptCacheEntry {
            private Class scriptClass; 
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
                } 
                catch(Exception e) {
                    throw new ScriptException("Could not parse script: " + scriptName, e);
                }
                this.scriptCache.putIfAbsent(
                    scriptName, 
                    entry
                );
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
    
}

//--- End of File -----------------------------------------------------------
