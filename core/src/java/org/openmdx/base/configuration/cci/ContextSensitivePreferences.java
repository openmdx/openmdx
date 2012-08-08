/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ContextSensitivePreferences.java,v 1.2 2007/10/10 16:05:52 hburger Exp $
 * Description: Context Sensitive Preferences
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:05:52 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006, OMEX AG, Switzerland
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

package org.openmdx.base.configuration.cci;

import java.util.prefs.Preferences;

import org.openmdx.base.configuration.spi.BasicPreferencesFactory;
import org.openmdx.base.configuration.spi.ContextSensitivePreferencesFactory;
import org.openmdx.compatibility.kernel.application.cci.Classes;

/**
 * Context Sensitive Preferences
 * <p>
 * Provides preferences for the following trees<ol>
 * <li>system
 * <li>application
 * <li>module
 * <li>component
 */
public class ContextSensitivePreferences {

    /**
     * Constructor 
     */
    protected ContextSensitivePreferences(
    ){
        super();
    }

    /**
     * Returns the topmost preference node for the system.
     *
     * @return  the topmost preference node for the system.
     */
    public static Preferences containerRoot() {
        return factory().containerRoot();
    }

    /**
     * Returns the topmost preference node for the application.
     *
     * @return  the topmost preference node for the application.
     */
    public static Preferences applicationRoot() {
        return factory().applicationRoot();
    }

    /**
     * Returns the topmost preference node for the application.
     *
     * @return  the topmost preference node for the application.
     */
    public static Preferences moduleRoot() {
        return factory().moduleRoot();
    }

    /**
     * Returns the topmost preference node for the component.
     *
     * @return the topmost preference node for the component.
     */
    public static Preferences componentRoot() {
        return factory().componentRoot();
    }

    /**
     * Retrieve the context sensitive preferences factory
     * 
     * @return the context sensitive preferences factory
     */
    private static synchronized ContextSensitivePreferencesFactory factory(){
        if(ContextSensitivePreferences.factory == null) try {
            ContextSensitivePreferences.factory = (ContextSensitivePreferencesFactory) Classes.getApplicationClass(
                PREFERENCES_FACTORY
            ).newInstance(
            );
        } catch (Exception exception) {
            throw new RuntimeException(
                "Could not acquire context sensitive preferences factory " + PREFERENCES_FACTORY,
                exception
            );
        }
        return ContextSensitivePreferences.factory;
    }
    
    /**
     * Find a package based container preference node given a class of the package.
     * 
     * @param   aClass 
     *          the class for whose package a container preference node is desired.
     * 
     * @return  the container preference node associated with the package of 
     *          which <code>aClass</code> is a member.
     *          
     * @throws  NullPointerException 
     *          if <code>aClass</code> is <code>null</code>.
     * @throws  IllegalArgumentException 
     *          if <code>aClass</code> is an array class
     */
    public static Preferences containerNodeForPackage(
        Class aClass
    ){
        return nodeForPackage(
            containerRoot(),
            aClass
        );
    }
    
    /**
     * Find a package based application preference node given a class of the package.
     * 
     * @param   aClass 
     *          the class for whose package an application preference node is desired.
     * 
     * @return  the application preference node associated with the package of 
     *          which <code>aClass</code> is a member.
     *          
     * @throws  NullPointerException 
     *          if <code>aClass</code> is <code>null</code>.
     * @throws  IllegalArgumentException 
     *          if <code>aClass</code> is an array class
     */
    public static Preferences applicationNodeForPackage(
        Class aClass
    ){
        return nodeForPackage(
            applicationRoot(),
            aClass
        );
    }

    /**
     * Find a package based module preference node given a class of the package.
     * 
     * @param   aClass 
     *          the class for whose package a module preference node is desired.
     * 
     * @return  the module preference node associated with the package of 
     *          which <code>aClass</code> is a member.
     *          
     * @throws  NullPointerException 
     *          if <code>aClass</code> is <code>null</code>.
     * @throws  IllegalArgumentException 
     *          if <code>aClass</code> is an array class
     */
    public static Preferences moduleNodeForPackage(
        Class aClass
    ){
        return nodeForPackage(
            moduleRoot(),
            aClass
        );
    }

    /**
     * Find a package based component preference node given a class of the package.
     * 
     * @param   aClass 
     *          the class for whose package a component preference node is desired.
     * 
     * @return  the component preference node associated with the package of 
     *          which <code>aClass</code> is a member.
     *          
     * @throws  NullPointerException 
     *          if <code>aClass</code> is <code>null</code>.
     * @throws  IllegalArgumentException 
     *          if <code>aClass</code> is an array class
     */
    public static Preferences componentNodeForPackage(
        Class aClass
    ){
        return nodeForPackage(
            componentRoot(),
            aClass
        );
    }
    
    /**
     * Find a package based preference node given its root node and a class of 
     * the package.
     * 
     * @param   aClass 
     *          the class for whose package a preference node is desired.
     * @param   rootNode
     *          the topmost node
     * 
     * @return  the preference node associated with the package of 
     *          which <code>c</code> is a member.
     *          
     * @throws  NullPointerException 
     *          if <code>root</code> or <code>aClass</code> is 
     *          <code>null</code>.
     * @throws  IllegalArgumentException 
     *          if <code>aClass</code> is an array class
     */
    protected static Preferences nodeForPackage(
        Preferences rootNode,
        Class aClass
    ){
        if (aClass.isArray()) throw new IllegalArgumentException(
            "Arrays have no associated preferences node"
        );
        String className = aClass.getName();
        int lastPeriod = className.lastIndexOf('.');
        return rootNode.node(                
            '/' + (
                lastPeriod < 0 ? "<unnamed>" : className.substring(0, lastPeriod).replace('.', '/')
            )
        );
    }

    /**
     * The <code>ContextSensitivePreferencesFactory</code> instance.
     */
    static private ContextSensitivePreferencesFactory factory = null;

    /**
     * The system property 
     * <code>org.openmdx.prefs.PreferencesFactory</code>
     * can be used to override the default context sensitive preferences 
     * factory name.
     * 
     * @see ContextSensitivePreferences#PREFERENCES_FACTORY
     */
    public static final String PREFERENCES_FACTORY_PROPERTY = "org.openmdx.prefs.PreferencesFactory";

    /**
     * The default <code>ContextSensitivePreferencesFactory</code> is
     * <code>org.openmdx.base.configuration.BasicPreferencesFactory</code>.
     * 
     * @see ContextSensitivePreferences#PREFERENCES_FACTORY_PROPERTY
     */
    protected final static String PREFERENCES_FACTORY = System.getProperty(
        PREFERENCES_FACTORY_PROPERTY,
        BasicPreferencesFactory.class.getName()
    );
    
}
