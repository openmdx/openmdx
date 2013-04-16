/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.opencrx.org/
 * Description: ControlFactory
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.portal.servlet.Texts;
import org.openmdx.portal.servlet.UiContext;
import org.openmdx.portal.servlet.attribute.AttributeValueFactory;
import org.openmdx.portal.servlet.wizards.WizardDefinition;
import org.openmdx.portal.servlet.wizards.WizardDefinitionFactory;

/**
 * ControlFactory
 *
 */
public class ControlFactory implements Serializable {
    
    /**
     * Constructor 
     *
     * @param uiContext
     * @param textsFactory
     * @param wizardFactory
     */
    public ControlFactory(
        UiContext uiContext,
        Texts textsFactory,        
        WizardDefinitionFactory wizardFactory
    ) {
        this.uiContext = uiContext;
        this.texts = textsFactory;
        this.wizardFactory = wizardFactory;
    }
    
    /**
     * Reset controls cache.
     */
    public synchronized void reset(
    ) {
        this.gridControls.clear();
        this.inspectorControls.clear();
        this.valueFactory.reset();
    }
    
    /**
     * Return UUID.
     * 
     * @return
     */
    private String uuidAsString(
    ) {
        return UUIDConversion.toUID(UUIDs.newUUID());
    }
    
    /**
     * Create grid control.
     * 
     * @param id
     * @param perspective
     * @param locale
     * @param localeAsIndex
     * @param tab
     * @param paneIndex
     * @param containerClass
     * @return
     */
    public synchronized GridControl createGridControl(
        String id,
        int perspective,
        String locale,
        int localeAsIndex,
        org.openmdx.ui1.jmi1.Tab tab,
        int paneIndex,
        String containerClass
    ) {
        org.openmdx.ui1.jmi1.ObjectContainer objectContainer = (org.openmdx.ui1.jmi1.ObjectContainer)tab.getMember().get(0);
        String key = null;
        synchronized(objectContainer) {
            key = perspective + "*" + containerClass + "*" + objectContainer.refMofId() + "*" + paneIndex + "*" + locale;
        }
        GridControl gridControl = (GridControl)this.gridControls.get(key);
        if(gridControl == null) {
            this.gridControls.put(
                key,
                gridControl = new GridControl(
                    id,
                    locale,
                    localeAsIndex,
                    this,
                    objectContainer,
                    this.valueFactory,
                    containerClass,
                    paneIndex
                )
            );
        }
        return gridControl;
    }

    /**
     * Create field group control.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param fieldGroup
     * @return
     */
    public synchronized FieldGroupControl createFieldGroupControl(
        String id,
        String locale,
        int localeAsIndex,
        org.openmdx.ui1.jmi1.FieldGroup fieldGroup
    ) {
        return new FieldGroupControl(
            id == null ? this.uuidAsString() : id,
            locale,
            localeAsIndex,
            fieldGroup
        );
    }

    /**
     * Create operation control.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param pane
     * @param paneIndex
     * @param forClass
     * @return
     */
    public synchronized OperationPaneControl createOperationPaneControl(
        String id,
        String locale,
        int localeAsIndex,
        org.openmdx.ui1.jmi1.OperationPane pane,
        int paneIndex,
        String forClass
    ) {
        return new OperationPaneControl(
            id == null ? this.uuidAsString() : id,
            locale,
            localeAsIndex,
            this,
            pane,
            this.wizardFactory,
            this.valueFactory,
            paneIndex,
            forClass
        );
    }

    /**
     * Create operation tab control.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param tab
     * @param paneIndex
     * @param tabIndex
     * @return
     */
    public synchronized OperationTabControl createOperationTabControl(
        String id,
        String locale,
        int localeAsIndex,
        org.openmdx.ui1.jmi1.OperationTab tab,
        int paneIndex,
        int tabIndex
    ) {
        return new OperationTabControl(
            id == null ? this.uuidAsString() : id,
            locale,
            localeAsIndex,
            this,
            tab,
            paneIndex,
            tabIndex
        );
    }
    
    /**
     * Create attribute tab control.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param tab
     * @param tabIndex
     * @return
     */
    public synchronized AttributeTabControl createAttributeTabControl(
        String id,
        String locale,
        int localeAsIndex,
        org.openmdx.ui1.jmi1.Tab tab,
        int tabIndex
    ) {
        return new AttributeTabControl(
            id == null ? this.uuidAsString() : id,
            locale,
            localeAsIndex,
            this,
            tab,
            tabIndex
        );
    }
    
    /**
     * Create attribute pane control.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param pane
     * @param paneIndex
     * @return
     */
    public synchronized AttributePaneControl createAttributePaneControl(
        String id,
        String locale,
        int localeAsIndex,
        org.openmdx.ui1.jmi1.AttributePane pane,
        int paneIndex
    ) {
        return new AttributePaneControl(
            id == null ? this.uuidAsString() : id,
            locale,
            localeAsIndex,
            this,
            pane,
            paneIndex
        );
    }
    
    /**
     * Create reference pane control.
     * 
     * @param id
     * @param perspective
     * @param locale
     * @param localeAsIndex
     * @param pane
     * @param paneIndex
     * @param forClass
     * @return
     */
    public synchronized ReferencePaneControl createReferencePaneControl(
        String id,
        int perspective,
        String locale,
        int localeAsIndex,
        org.openmdx.ui1.jmi1.ReferencePane pane,
        int paneIndex,
        String forClass
    ) {
        return new ReferencePaneControl(
            id == null ? this.uuidAsString() : id,
            perspective,
            locale,
            localeAsIndex,
            this,
            pane,
            forClass,
            paneIndex
        );        
    }

    /**
     * Create show object control.
     * 
     * @param id
     * @param perspective
     * @param locale
     * @param localeAsIndex
     * @param inspector
     * @param forClass
     * @return
     */
    public synchronized ShowInspectorControl createShowInspectorControl(
        String id,
        int perspective,
        String locale,
        int localeAsIndex,
        org.openmdx.ui1.jmi1.Inspector inspector,
        String forClass
    ) {
        String key = perspective + "*" + forClass + "*Show*" + locale;
        ShowInspectorControl inspectorControl = (ShowInspectorControl)this.inspectorControls.get(key);
        if(inspectorControl == null) {
            inspectorControl = new ShowInspectorControl(
                id == null ? this.uuidAsString() : id,
                perspective,
                locale,
                localeAsIndex,
                this,
                this.wizardFactory,
                inspector,
                forClass
            );
            this.inspectorControls.put(
                key,
                inspectorControl
            );
        }
        return inspectorControl;
    }

    /**
     * Create edit object control.
     * 
     * @param id
     * @param perspective
     * @param locale
     * @param localeAsIndex
     * @param inspector
     * @param forClass
     * @return
     */
    public synchronized EditInspectorControl createEditInspectorControl(
        String id,
        int perspective,
        String locale,
        int localeAsIndex,
        org.openmdx.ui1.jmi1.Inspector inspector,
        String forClass
    ) {
        String key = perspective + "*" + forClass + "*Edit*" + locale;
        EditInspectorControl inspectorControl = (EditInspectorControl)this.inspectorControls.get(key);
        if(inspectorControl == null) {
            inspectorControl = new EditInspectorControl(
                id == null ? this.uuidAsString() : id,
                locale,
                localeAsIndex,
                this,
                inspector
            );
            this.inspectorControls.put(
                key,
                inspectorControl
            );
        }
        return inspectorControl;
    }

    /**
     * Create wizard control.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param reports
     * @return
     */
    public synchronized WizardControl createWizardControl(
        String id,
        String locale,
        int localeAsIndex,
        WizardDefinition[] reports
    ) {
        return new WizardControl(
            id == null ? this.uuidAsString() : id,
            locale,
            localeAsIndex,
            this,
            reports
        );
    }

    /**
     * Create wizard tab control.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param tab
     * @param wizardDefinition
     * @param paneIndex
     * @param tabIndex
     * @return
     */
    public synchronized WizardTabControl createWizardTabControl(
        String id,
        String locale,
        int localeAsIndex,
        org.openmdx.ui1.jmi1.OperationTab tab,
        WizardDefinition wizardDefinition,
        int paneIndex,
        int tabIndex
    ) {
        return new WizardTabControl(
            id == null ? this.uuidAsString() : id,
            locale,
            localeAsIndex,
            tab,
            this,
            wizardDefinition,
            paneIndex,
            tabIndex
        );
    }

    /**
     * Create control of given type.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param controlClass
     * @return
     * @throws ServiceException
     */
    public synchronized Control createControl(
        String id,
        String locale,
        int localeAsIndex,
        Class<?> controlClass
    ) throws ServiceException {
        try {
            Constructor cons = controlClass.getConstructor(
                new Class[]{
                    String.class,
                    String.class,
                    int.class
                }
            );
            return (Control)cons.newInstance(
                new Object[]{
                    id == null 
                    	? this.uuidAsString() 
                    	: id,
                    locale,
                    new Integer(localeAsIndex)
                }
            );
        } catch(Exception e) {
            throw new ServiceException(e);
        }       
    }
    
    /**
     * Create control of given type.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param controlClass
     * @param parameter
     * @return
     * @throws ServiceException
     */
    public synchronized Control createControl(
        String id,
        String locale,
        int localeAsIndex,
        Class<?> controlClass,
        Object[] parameter
    ) throws ServiceException {
        try {
            Constructor cons = controlClass.getConstructor(
                new Class[]{
                    String.class,
                    String.class,
                    int.class,
                    Object[].class
                }
            );
            return (Control)cons.newInstance(
                new Object[]{
                    id == null ? this.uuidAsString() : id,
                    locale,
                    new Integer(localeAsIndex),
                    parameter
                }
            );
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Get attribute value factory.
     * 
     * @return
     */
    public AttributeValueFactory getAttributeValueFactory(
    ) {
        return this.valueFactory;
    }
    
    /**
     * Get texts factory.
     * 
     * @return
     */
    public Texts getTextsFactory(
    ) {
        return this.texts;
    }
    
    /**
     * Get ui context.
     * 
     * @return
     */
    public UiContext getUiContext(
    ) {
        return this.uiContext;
    }
    
    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = -1451986642508901552L;
    
    private final UiContext uiContext;
    private final WizardDefinitionFactory wizardFactory;
    private final Texts texts;
    private Map<String,GridControl> gridControls = new HashMap<String,GridControl>();
    private Map<String,InspectorControl> inspectorControls = new HashMap<String,InspectorControl>();
    private AttributeValueFactory valueFactory = new AttributeValueFactory();
    
}

//--- End of File -----------------------------------------------------------
