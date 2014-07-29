/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: ContainerControl
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2014, OMEX AG, Switzerland
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
import java.util.ArrayList;
import java.util.List;

/**
 * ContainerControl
 *
 */
public abstract class ContainerControl extends Control implements Serializable {

	/**
	 * Constructor.
	 * 
	 * @param id
	 * @param locale
	 * @param localeAsIndex
	 */
	public ContainerControl(
        String id,
        String locale,
        int localeAsIndex
    ) {
        super(
            id,
            locale,
            localeAsIndex
        );
    }

    /**
     * Add child control.
     * 
     * @param control
     */
    public void addControl(
        Control control
    ) {
        this.addControl(
            control,
            null
        );
    }
    
    /**
     * Add child control and frame.
     * 
     * @param control
     * @param frame
     */
    public void addControl(
        Control control,
        String frame
    ) {
        this.children.add(control);
        this.frames.add(frame);
    }
    
    /**
     * Add multiple controls.
     * 
     * @param controls
     */
    public void addControl(
        Control[] controls
    ) {
        this.addControl(
            controls,
            null
        );
    }
    
    /**
     * Add controls.
     * 
     * @param controls
     */
    public void addControls(
    	List<? extends Control> controls
    ) {
    	for(Control control: controls) {
            this.addControl(
                control,
                null
            );    		
    	}
    }

    /**
     * Add multiple controls and frame.
     * 
     * @param controls
     * @param frame
     */
    public void addControl(
        Control[] controls,
        String frame
    ) {
        for(int i = 0; i < controls.length; i++) {
            this.addControl(
                controls[i],
                frame
            );
        }
    }
    
	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.control.Control#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Control> List<T> getChildren(
		Class<T> type
	) {
		@SuppressWarnings("unchecked")
		List<T> children = (List<T>)this.children;
		return children;	
	}

	//-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = -1231427781842708999L;

    protected final List<Control> children = new ArrayList<Control>();
    protected final List<String> frames = new ArrayList<String>();
    
}