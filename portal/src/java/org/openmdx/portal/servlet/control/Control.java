/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: Control.java,v 1.18 2008/05/01 21:43:56 wfro Exp $
 * Description: Control
 * Revision:    $Revision: 1.18 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/05/01 21:43:56 $
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
 * This product includes yui-ext, the yui extension
 * developed by Jack Slocum (License - based on BSD).
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jmi.reflect.RefObject;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.openmdx.application.log.AppLog;
import org.openmdx.base.accessor.jmi.spi.RefMetaObject_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.HtmlPage;
import org.openmdx.portal.servlet.texts.Texts_1_0;

public abstract class Control
    implements Serializable {
  
    //-------------------------------------------------------------------------
    public Control(
        String id,
        String locale,
        int localeAsIndex,
        ControlFactory controlFactory
    ) {
        this.id = id;
        this.locale = locale;
        this.localeAsIndex = localeAsIndex;
        this.controlFactory = controlFactory;
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
    public Texts_1_0 getTexts(
    ) {
        return this.controlFactory.getTextsFactory().getTexts(
            this.locale
        );
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
        HtmlPage p,
        String frame,
        boolean forEditing
    ) throws ServiceException {
        try {
            URL groovyURL = this.getGroovyURL(p.getHttpServletRequest());
            if(groovyURL != null) {
                ScriptEngine scriptEngine = scriptEngines.get(this.id);
                if(scriptEngine == null) {
                    scriptEngines.put(
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
                    String qualifiedClassName = (String)classDef.getElementDef().values("qualifiedName").get(0);
                    String qualifiedPackageName = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf(":"));
                    String qualifiedScriptName = this.id + ":" + qualifiedPackageName;
                    String name = scriptNames.get(qualifiedScriptName);
                    if(name == null) {
                        name = qualifiedPackageName.replace(":", ".") + ".groovy";
                        URL realPath = this.getGroovyURL(p.getHttpServletRequest());
                        if(!new File(realPath + "/" + name).exists()) {
                            name = scriptName;   
                        }
                        scriptNames.put(qualifiedScriptName, name);
                    }
                    scriptName = name;
                }
                scriptEngine.run(scriptName, binding);
            }
        }
        catch(Exception e) {
            AppLog.warning("Script exception", e);
            new ServiceException(e).log();
        }
    }
  
    //-------------------------------------------------------------------------
    public void paint(
        HtmlPage p,
        boolean forEditing
    ) throws ServiceException {
        this.paint(
            p,
            null,
            forEditing
        );
    }
  
    //-------------------------------------------------------------------------
    public ControlFactory getControlFactory(
    ) {
        return this.controlFactory;
    }
  
    //-------------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    protected static Map<String,ScriptEngine> scriptEngines = new HashMap<String,ScriptEngine>();
    protected static Map<String,String> scriptNames = new HashMap<String,String>();
  
    protected String id;
    protected String locale;
    protected int localeAsIndex;
    protected final ControlFactory controlFactory;
  
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
            this.parentClassLoader = parentClassLoader;
        }

        public ClassLoader getParentClassLoader(
        ) {
            return parentClassLoader;
        }

        public void setParentClassLoader(
            ClassLoader parentClassLoader
        ) {
            if (parentClassLoader == null) {
                throw new IllegalArgumentException("The parent class loader must not be null.");
            }
            this.parentClassLoader = parentClassLoader;
        }

        private ScriptCacheEntry updateCacheEntry(
            String scriptName, 
            final ClassLoader parentClassLoader
        ) throws ResourceException, ScriptException {
            ScriptCacheEntry entry;
            scriptName = scriptName.intern();
            synchronized (scriptName) {
                entry = (ScriptCacheEntry)this.scriptCache.get(scriptName);            
                if(entry == null) {
                    entry = new ScriptCacheEntry();
                    GroovyClassLoader groovyLoader = new GroovyClassLoader();
                    try {
                        entry.scriptClass = null;
                        for(int i = 0; i < roots.length; i++) {
                            URL scriptURL = new URL(roots[i] + "/" + scriptName);
                            try {
                                entry.scriptClass = groovyLoader.parseClass(scriptURL.openStream(), scriptName);
                                break;
                            }
                            catch(IOException e) {}
                        }
                        if(entry.scriptClass == null) {
                            throw new ScriptException("Could not locate scriptName: " + scriptName);                    
                        }
                    } 
                    catch(Exception e) {
                        throw new ScriptException("Could not parse scriptName: " + scriptName, e);
                    }
                    this.scriptCache.put(scriptName, entry);
                }
            }
            return entry;
        }

        public Object run(
            String scriptName, 
            Binding binding
        ) throws ResourceException, ScriptException {
            ScriptCacheEntry entry = updateCacheEntry(scriptName, getParentClassLoader());
            Script scriptObject = InvokerHelper.createScript(entry.scriptClass, binding);
            return scriptObject.run();
        }

        //-----------------------------------------------------------------------
        // Members
        //-----------------------------------------------------------------------
        private URL[] roots;
        private Map<String,ScriptCacheEntry> scriptCache = Collections.synchronizedMap(new HashMap<String,ScriptCacheEntry>());
        private ClassLoader parentClassLoader = getClass().getClassLoader();

    }
    
}

//--- End of File -----------------------------------------------------------
