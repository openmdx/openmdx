/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.opencrx.org/
 * Description: Component
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
import java.util.List;
import java.util.Locale;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.control.Control;

/**
 * Canvas
 *
 */
public abstract class Component implements Serializable {
    
    /**
     * Constructor.
     * 
     * @param control
     * @param view
     */
    public Component(
        Control control,
        View view
    ) {
        this.control = control;
        this.view = view;
    }

    /**
     * Get current locale.
     * 
     * @return
     */
    protected Locale getCurrentLocale(
    ) {
        String locale = this.view.getApplicationContext().getCurrentLocaleAsString();
        return new Locale(
            locale.substring(0, 2), 
            locale.substring(locale.indexOf("_") + 1)
        );              
    }
    
    /**
     * Refresh content.
     * 
     * @param refreshData
     * @throws ServiceException
     */
    abstract public void refresh(
        boolean refreshData
    ) throws ServiceException;
    
    
	/**
	 * @return the view
	 */
	public View getView(
	) {
		return this.view;
	}

    /**
     * Paint the component to the view port.
     * 
     * @param p
     * @param frame
     * @param forEditing
     * @throws ServiceException
     */
    public abstract void paint(
        ViewPort p,
        String frame,
        boolean forEditing
    ) throws ServiceException;

    /**
	 * Get child components of given type.
	 * 
	 * @param type
	 * @return
	 */
	public abstract <T extends Component> List<T> getChildren(
		Class<T> type
	);

	/**
	 * @return the control
	 */
	public Control getControl() {
		return control;
	}

	//-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
	private static final long serialVersionUID = -5618060395936357326L;

	protected Control control;
	protected View view;
    
}
