/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.opencrx.org/
 * Description: FieldGroup
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
package org.openmdx.portal.servlet.component;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.attribute.Attribute;
import org.openmdx.portal.servlet.control.UiFieldGroupControl;

/**
 * FieldGroup
 *
 */
public class UiFieldGroup extends Component implements Serializable {
    
    /**
     * Creates a field group based on field group control. The feature values
     * of the field group are read from object. If object is null the
     * values are read from view.getObjectReference().getObject().
     */
    public UiFieldGroup(
        UiFieldGroupControl control,
        ObjectView view,
        Object object
    ) {
        super(
            control,
            view
        );
        this.object = object;
    }
 
    /* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.component.Component#getView()
	 */
	@Override
	public ObjectView getView(
	) {
		return (ObjectView)this.view;
	}

	/**
     * Test permission for this field group.
     * 
     * @param object
     * @param app
     * @param action
     * @return
     */
    protected boolean hasPermission(
    	RefObject_1_0 object,
    	ApplicationContext app,
    	String action
    ) {
    	return app.getPortalExtension().hasPermission(
        	this.control, 
        	object, 
        	app,
        	WebKeys.PERMISSION_REVOKE_SHOW 
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.Canvas#refresh(boolean)
     */
    @Override
    public void refresh(
        boolean refreshData
    ) throws ServiceException {
        // nothing to refresh
    }
    
    /**
     * Get control casted to FieldGroupControl.
     * 
     * @return
     */
    public UiFieldGroupControl getFieldGroupControl(
    ) {
    	return (UiFieldGroupControl)this.control;
    }
    
    /**
     * Get underlying object for this field group. In case of a value
     * type the field group holds its own object. Otherwise the object
     * of the assigned view is returned.
     * 
     * @return
     */
    public Object getObject(
    ) {
        return this.object == null
            ? this.view instanceof ObjectView 
            	? ((ObjectView)this.view).getObjectReference().getObject()
            	: null
            : this.object;
    }

    /**
     * Get name.
     * 
     * @return
     */
    public String getName(
    ) {
    	UiFieldGroupControl control = this.getFieldGroupControl();
    	return control.getName();
    }

    /**
     * Init attribute map for this field group.
     * 
     * @param attributeMap
     * @param app
     */
    public void initAttributeMap(
    	Map<String, Attribute> attributeMap,
    	ApplicationContext app
    ) {
    	UiFieldGroupControl control = this.getFieldGroupControl();
        Attribute[][] attributes = control.getAttribute(
        	this.getObject(), 
            app
        );
        if(attributes != null) {
			for(int u = 0; u < attributes.length; u++) {
				for (int v = 0; v < attributes[u].length; v++) {
					Attribute attribute = attributes[u][v];
					if((attribute != null) && !attribute.isEmpty()) {
						attributeMap.put(
						    attribute.getValue().getName(),
						    attribute
						);
					}
				}
			}
        }
    }

    /**
     * Get fields for this field group.
     * 
     * @return
     */
    public List<UiFieldGroupControl.Field> getFields(
    ) {
    	return this.getFieldGroupControl().getFields();
    }

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.view.Component#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Component> List<T> getChildren(
		Class<T> type
	) {
		return Collections.emptyList();
	}

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.component.Component#paint(org.openmdx.portal.servlet.ViewPort, java.lang.String, boolean)
	 */
	@Override
	public void paint(
		ViewPort p, 
		String frame, 
		boolean forEditing
	) throws ServiceException {
		this.getFieldGroupControl().paint(p, frame, forEditing);
	}
	
	//-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 1911685908400089427L;

    protected final Object object;


}
