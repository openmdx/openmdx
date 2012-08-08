/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: BasicPreferencesFactory.java,v 1.1 2006/10/09 17:02:04 hburger Exp $
 * Description: Basic Preferences Factory 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/10/09 17:02:04 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.base.configuration.spi;

import java.util.prefs.Preferences;

import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.naming.PathComponent;


/**
 * Basic Preferences Factory
 * <p>
 * The <code>BasicPreferencesFactory</code>'s implementation 
 * supports the following providers:<ul>
 * <li><b>Java*Preferences</b> &mdash; 
 *     <code>java.util.prefs.Preferences</code>' <em>System</em> or 
 *     <em>User</em> preferences
 * <li><b>Java*Properties</b> &mdash; 
 *     <code>java.util.Properties</code> based configuration
 * <li><b>Java*Naming</b> &mdash; JNDI based configuration, e.g. 
 *     <em>java:comp/env</em> entries 
 * </ul>
 * Others may be supported by a subclass.
 */
public class BasicPreferencesFactory 
    implements ContextSensitivePreferencesFactory 
{
    
    /**
     * The <code>BasicPreferencesFactory</code>'s implementation supports the
     * following providers:<ul>
     * <li><b>Java*Preferences</b> &mdash; 
     *     <code>java.util.prefs.Preferences</code>' <em>System</em> or 
     *     <em>User</em> preferences
     * <li><b>Java*Properties</b> &mdash; 
     *     <code>java.util.Properties</code> based configuration
     * <li><b>Java*Naming</b> &mdash; JNDI based configuration, e.g. 
     *     <em>java:comp/env</em> entries 
     * </ul>
     * Others may be supported by a subclass.
     * <p>
     * @param xri the preferences' source
     * @param userNode 
     * 
     * @return the corresponding preferences node
     * 
     * @throws IllegalArgumentException
     *         if the given <code>path</code>'s provider is not supported
     * 
     */
    protected static Preferences getSegment(
        String xri, 
        boolean userNode
    ){
        Path path = new Path(xri);
        if(path.isLike(PREFERENCES_1_SEGMENT_PATTERN)) {            
            PathComponent provider = path.getComponent(2);
            if(
                provider.size() == 2 &&
                "Java".equals(provider.get(0)) 
            ){
                String javaProvider = provider.get(1);
                if ("Preferences".equals(provider.get(1)))  {
                    String segment = path.get(4);
                    if("System".equals(segment)) {
                        return Preferences.systemRoot();
                    } else if ("User".equals(segment)) {
                        return Preferences.userRoot();
                    } else throw new IllegalArgumentException(
                        "Can't resolve '" + path.toXri() + "' because " + Preferences.class.getName() +
                        " provides only 'System' and 'User' segments: " + segment
                    );
                } else if ("Properties".equals(javaProvider))  {
                    return new PropertyPreferences(path.getComponent(4), userNode);
                } else if ("Naming".equals(javaProvider))  {
                    String segment = path.get(4);
                    if("(java:comp/env)".equals(segment)) {
                        return new NamingPreferences();
                    } else throw new IllegalArgumentException(
                        "Can't resolve '" + path.toXri() + "' because " + NamingPreferences.class.getName() +
                        " supports the java:comp/env context only: " + segment
                    );
                } else throw new IllegalArgumentException(
                    "Can't resolve '" + path.toXri() + "' because " + BasicPreferencesFactory.class.getName() + 
                    " does not support the given Java provider: " + javaProvider
                );
            } else throw new IllegalArgumentException(
                "Path " + path.toXri() + " does not refer to a Java preferences provider"
            );
        } else throw new IllegalArgumentException(
            "Path " + path.toXri() + " does not refer to an org::openmdx::preferences1 segment"
        );
    }

    /**
     * org::openmdx::preferences1 segment pattern<ul>
     * <li><i>as XRI 1:</i> xri:@openmdx:org.openmdx.preferences1/provider/*&#42;/segment/*&#42;
     * <li><i>as XRI 2:</i> <b>xri://@openmdx*org.openmdx.preferences1/provider/$(..)/segment/($..)</b>
     * <li><i>as Path:</i> org::openmdx::preferences1/provider/::&#42;/segment/::&#42;
     * <li><i>as URI:</i> spice://org:openmdx:preferences1/provider/:&#42;/segment/:&#42;
     * </ul>
     */
    protected final static Path PREFERENCES_1_SEGMENT_PATTERN = new Path(
        new String[] {
            "org:openmdx:preferences1", 
            "provider", ":*", 
            "segment", ":*"
        }
    );

    /**
     * The system property 
     * <code>org.openmdx.prefs.ContainerSegment</code>
     * can be used to override the default container sgement path.
     * 
     * @see BasicPreferencesFactory#CONTAINER_SEGMENT
     */
    public static final String CONTAINER_SEGMENT_PROPERTY = "org.openmdx.prefs.ContainerSegment";

    /**
     * The container preferences segment has the following default value:<ul>
     * <li><b>"xri://@openmdx*org.openmdx.preferences1/provider/Java*Properties/segment/System*(xri://+resource/org/openmdx/prefs/container/preferences.properties)"</b>
     * </ul>
     * 
     * @see BasicPreferencesFactory#CONTAINER_SEGMENT_PROPERTY
     */
    protected final static String CONTAINER_SEGMENT = System.getProperty(
        CONTAINER_SEGMENT_PROPERTY,
        "xri://@openmdx*org.openmdx.preferences1/provider/Java*Properties/segment/System*(xri://+resource/org/openmdx/prefs/container/preferences.properties)"
    );
    
    /**
     * The system property 
     * <code>org.openmdx.prefs.ApplicationSegment</code>
     * can be used to override the default application sgement path.
     * 
     * @see BasicPreferencesFactory#APPLICATION_SEGMENT
     */
    public static final String APPLICATION_SEGMENT_PROPERTY = "org.openmdx.prefs.ApplicationSegment";

    /**
     * The application preferences segment has the following default value:<ul>
     * <li><b>xri://@openmdx*org.openmdx.preferences1/provider/Java*Properties/segment/(xri://+resource/org/openmdx/prefs/application/preferences.properties)</b>
     * </ul>
     * 
     * @see BasicPreferencesFactory#APPLICATION_SEGMENT_PROPERTY
     */
    protected final static String APPLICATION_SEGMENT = System.getProperty(
        APPLICATION_SEGMENT_PROPERTY,
        "xri://@openmdx*org.openmdx.preferences1/provider/Java*Properties/segment/(xri://+resource/org/openmdx/prefs/application/preferences.properties)"
    );

    /**
     * The system property 
     * <code>org.openmdx.prefs.ModuleSegment</code>
     * can be used to override the default module sgement path.
     * 
     * @see BasicPreferencesFactory#MODULE_SEGMENT
     */
    public static final String MODULE_SEGMENT_PROPERTY = "org.openmdx.prefs.ModuleSegment";

    /**
     * The module preferences segment has the following default value:<ul>
     * <li><b>xri://@openmdx*org.openmdx.preferences1/provider/Java*Properties/segment/(xri://+resource/org/openmdx/prefs/module/preferences.properties)</b>
     * </ul>
     * 
     * @see BasicPreferencesFactory#MODULE_SEGMENT_PROPERTY
     */
    protected final static String MODULE_SEGMENT = System.getProperty(
        MODULE_SEGMENT_PROPERTY,
        "xri://@openmdx*org.openmdx.preferences1/provider/Java*Properties/segment/(xri://+resource/org/openmdx/prefs/module/preferences.properties)"
    );


    /**
     * The system property 
     * <code>org.openmdx.prefs.ComponentSegment</code>
     * can be used to override the default application sgement path.
     * 
     * @see BasicPreferencesFactory#COMPONENT_SEGMENT
     */
    public static final String COMPONENT_SEGMENT_PROPERTY = "org.openmdx.prefs.ComponentSegment";

    /**
     * The component preferences segment has the following default value:<ul>
     * <li><b>xri://@openmdx*org.openmdx.preferences1/provider/Java*Naming/segment/(java:comp/env)</b>
     * </ul>
     * 
     * @see BasicPreferencesFactory#COMPONENT_SEGMENT_PROPERTY
     */
    protected final static String COMPONENT_SEGMENT = System.getProperty(
        COMPONENT_SEGMENT_PROPERTY,
        "xri://@openmdx*org.openmdx.preferences1/provider/Java*Naming/segment/(java:comp/env)"
    );


    //------------------------------------------------------------------------
    // Implements ContextSensitivePreferencesFactory
    //------------------------------------------------------------------------

    /**
     * This factory's container node may be cached as it iscontext aware
     */
    private Preferences containerNode = null;
    
    /**
     * This factory's application node may be cached as it iscontext aware
     */
    private Preferences applicationNode = null;
    
    /**
     * This factory's module node may be cached as it iscontext aware
     */
    private Preferences moduleNode = null;
    
    /**
     * This factory's component node may be cached as it iscontext aware
     */
    private Preferences componentNode = null;
    
    /* (non-Javadoc)
     * @see java.util.prefs.PreferencesFactory#systemRoot()
     */
    public synchronized Preferences containerRoot() {
        return this.containerNode == null ? 
            this.containerNode = getSegment(CONTAINER_SEGMENT, false) :
            this.containerNode;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.configuration.ContextSensitivePreferencesFactory#applicationNode()
     */
    public synchronized Preferences applicationRoot(
    ) {
        return this.applicationNode == null ? 
            this.applicationNode = getSegment(APPLICATION_SEGMENT, true) :
            this.applicationNode;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.configuration.ContextSensitivePreferencesFactory#moduleNode()
     */
    public synchronized Preferences moduleRoot() {
        return this.moduleNode == null ? 
            this.moduleNode = getSegment(MODULE_SEGMENT, true) :
            this.moduleNode;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.configuration.ContextSensitivePreferencesFactory#componentNode()
     */
    public synchronized Preferences componentRoot() {
        return this.componentNode == null ? 
            this.componentNode = getSegment(COMPONENT_SEGMENT, true) :
            this.componentNode;
    }
    
}
