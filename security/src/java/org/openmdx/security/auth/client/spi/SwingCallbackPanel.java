/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: SwingCallbackPanel.java,v 1.2 2009/03/08 18:52:20 wfro Exp $
 * Description: Swing Callback Panel
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/08 18:52:20 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.security.auth.client.spi;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.uses.layout.SpringUtilities;

/**
 * Swing Callback Panel
 */
public class SwingCallbackPanel extends JPanel
	implements ActionListener {
	
    /**
	 * Implements <code>Serializable</code>
	 */
	private static final long serialVersionUID = 7459090346508569170L;

	/**
	 * The field length methods' default value
	 */
	private static final int DEFAULT_FIELD_LENGTH = 10;
	
    /**
     * Constructor
     * 
     * @param callbackContext
     * @throws UnsupportedCallbackException 
     */
    public SwingCallbackPanel(
    	SwingCallbackContext callbackContext
    ) throws UnsupportedCallbackException {
    	super();
    	this.setOpaque(true); //content panes must be opaque
        this.callbackContext = callbackContext;
        this.addHeaderPane();
        this.fields = this.addCallbackPane(callbackContext.getCallbacks());
        this.addCompletionPane();
        // 
        // Propagate
        //
        JFrame frame = callbackContext.getFrame();
        frame.setContentPane(this);
        //
        // Make sure the focus goes to the right component
        // whenever the frame is initially given the focus.
        //
        frame.addWindowListener(
        	new WindowAdapter() {
        		public void windowActivated(WindowEvent e) {
        			resetFocus();
        		}
        	}
        );
        //
        // Display the window.
        //
        frame.pack();
        frame.setVisible(true);       
    }
    
    /**
     * This panel's callback context
     */
    private final SwingCallbackContext callbackContext;

    /**
     * One field for each callback
     */
	private final JComponent[] fields;

	protected void propagateValues(
		Callback[] target,
		JComponent[] source
	) throws UnsupportedCallbackException {
		for(
			int i = 0;
			i < target.length;
			i++
		){
			this.propagateValue(target[i], source[i]);
		}
	}
	
	protected void propagateValue(
		Callback target,
		JComponent source
	) throws UnsupportedCallbackException{
    	if(target instanceof NameCallback) {
    		this.propagatValue((NameCallback)target, (JTextField)source);
    	} 
    	else if(target instanceof PasswordCallback) {
    		this.propagatValue((PasswordCallback)target, (JPasswordField)source);
    	} 
    	else if (target instanceof TextInputCallback) {
    		this.propagatValue((TextInputCallback)target, (JTextField)source);
    	} 
    	else if (target instanceof TextOutputCallback) {
    		// nothing to be done
    	} 
    	else throw new UnsupportedCallbackException(
    		target,
        	"Unsupported callback class " + target.getClass().getName()
        );
	}

	protected void propagatValue(
		NameCallback target,
		JTextField source
	) throws UnsupportedCallbackException{
		String name = source.getText();
		target.setName(
			name == null ? target.getDefaultName() : name
		);
	}

	protected void propagatValue(
		PasswordCallback target,
		JPasswordField source
	) throws UnsupportedCallbackException{
		char[] password = source.getPassword();
		target.setPassword(password);
		Arrays.fill(password, '\0');
	}
	
	protected void propagatValue(
		TextInputCallback target,
		JTextField source
	) throws UnsupportedCallbackException{
		String text = source.getText();
		target.setText(
			text == null ? target.getDefaultText() : text
		);
	}

    protected void addHeaderPane(
    ) throws UnsupportedCallbackException {
    	String name = this.callbackContext.toLocalizedString("LOGIN_IMAGE");
    	if(
    		!"null".equalsIgnoreCase(name) &&
    		!"LOGIN_IMAGE".equals(name)
    	){
	        JPanel pane = new JPanel();
	        URL url = Classes.getApplicationResource(name);
			JLabel image = new JLabel(
				new ImageIcon(
					url
				)
			);
			pane.add(image);
			this.add(pane);
    	}
    }
	
    protected JComponent[] addCallbackPane(
    	Callback[] callbacks
    ) throws UnsupportedCallbackException {
    	JComponent[] fields = new JComponent[callbacks.length];
        JPanel pane = new JPanel(new SpringLayout());
    	int i = 0;
    	for(Callback callback : callbacks) {
    		fields[i++] = this.addCallback(pane, callback); 
    	}
        SpringUtilities.makeCompactGrid(
        	pane,
            i, 2, 	// rows, columns
            6, 6,   // initX, initY
            6, 6    // xPad, yPad
        );
        this.add(pane);
        return fields;
    }

	protected JComponent addCallback(
		JPanel pane, Callback callback
	) throws UnsupportedCallbackException{
    	if(callback instanceof NameCallback) {
    		return this.addCallback(pane, (NameCallback) callback);
    	} 
    	else if(callback instanceof PasswordCallback) {
    		return this.addCallback(pane, (PasswordCallback) callback);
    	} 
    	else if (callback instanceof TextInputCallback) {
    		return this.addCallback(pane, (TextInputCallback) callback);
    	} 
    	else if (callback instanceof TextOutputCallback) {
    		return this.addCallback(pane, (TextOutputCallback) callback);
    	} 
    	else throw new UnsupportedCallbackException(
    		callback,
        	"Unsupported callback class " + callback.getClass().getName()
        );
	}

	protected int getPasswordFieldLength(
	){
		return SwingCallbackPanel.DEFAULT_FIELD_LENGTH;
	}
	
	protected JPasswordField addCallback(
		JPanel pane, 
		PasswordCallback callback
	){
        //
        // Field
        //
		JPasswordField field = new JPasswordField(this.getPasswordFieldLength());
		field.setActionCommand(CallbackActions.LOGIN);
		field.addActionListener(this);
        pane.add(field);
        //
        // Label
        //
        JLabel label = new JLabel(
        	this.callbackContext.toLocalizedString(callback.getPrompt()), 
        	JLabel.TRAILING
        );
        label.setLabelFor(field);
        pane.add(label);
        //
        // Pane
        //
        pane.add(label);
        pane.add(field);
		return field;
	}

	protected int getNameFieldLength(
	){
		return SwingCallbackPanel.DEFAULT_FIELD_LENGTH;
	}
	
	protected JTextField addCallback(
		JPanel pane, 
		NameCallback callback
	){
        //
        // Field
        //
		JTextField field = new JTextField(
			callback.getDefaultName(), 
			this.getNameFieldLength()
		);
		field.setActionCommand(CallbackActions.LOGIN);
		field.addActionListener(this);
        //
        // Label
        //
        JLabel label = new JLabel(
        	this.callbackContext.toLocalizedString(callback.getPrompt()), 
        	JLabel.TRAILING
        );
        label.setLabelFor(field);
        //
        // Propagate
        //
        pane.add(label);
        pane.add(field);
		return field;
	}

	protected int getTextFieldLength(
	){
		return SwingCallbackPanel.DEFAULT_FIELD_LENGTH;
	}
	
	protected JTextField addCallback(
		JPanel pane, 
		TextInputCallback callback
	){
        //
        // Field
        //
		JTextField field = new JTextField(
			callback.getDefaultText(), 
			this.getTextFieldLength()
		);
		field.setActionCommand(CallbackActions.LOGIN);
		field.addActionListener(this);
        //
        // Label
        //
        JLabel label = new JLabel(
        	this.callbackContext.toLocalizedString(callback.getPrompt()), 
        	JLabel.TRAILING
        );
        label.setLabelFor(field);
        //
        // Propagate
        // 
        pane.add(label);
        pane.add(field);
		return field;
	}

	protected JTextField addCallback(
		JPanel pane, 
		TextOutputCallback callback
	){
        //
        // Field
        //
		String message = this.callbackContext.toLocalizedString(
			callback.getMessage()
		);
		JTextField field = new JTextField(
			message, 
			message.length()
		);
		field.setEnabled(false);
        //
        // Label
        //
        JLabel label = new JLabel(
        	this.callbackContext.toLocalizedString("MESSAGE_TYPE_" + callback.getMessageType()), 
        	JLabel.TRAILING
        );
        label.setLabelFor(field);
        //
        // Propagate
        // 
        pane.add(label);
        pane.add(field);
		return field;
	}
	
	protected void addCompletionPane(){
        JPanel pane = new JPanel(new GridLayout(0,1));
        JButton loginButton = new JButton(
        	this.callbackContext.toLocalizedString(CallbackActions.LOGIN)
        );
        JButton cancelButton = new JButton(
        	this.callbackContext.toLocalizedString(CallbackActions.CANCEL)
        );
        loginButton.setActionCommand(CallbackActions.LOGIN);
        cancelButton.setActionCommand(CallbackActions.CANCEL);
        loginButton.addActionListener(this);
        cancelButton.addActionListener(this);
        //
        // Propagate
        // 
        pane.add(loginButton);
        pane.add(cancelButton);
        this.add(pane);
	}
	
    public void actionPerformed(
    	ActionEvent e
    ) {
        String cmd = e.getActionCommand();

        if (CallbackActions.LOGIN.equals(cmd)) { 
        	//
        	// Login
        	//
        	try {
        		this.propagateValues(
					this.callbackContext.getCallbacks(),
					this.fields
				);
	            this.callbackContext.signalReturn();
			} 
        	catch (UnsupportedCallbackException exception) {
	            this.callbackContext.signalFailure(exception);
			}
        } 
        else if (CallbackActions.CANCEL.equals(cmd)) {
        	//
        	// Cancel
        	//
            this.callbackContext.signalFailure(
            	new IOException(CallbackActions.CANCEL)
            );
        }
    }

    protected boolean isEnabled(
    	Callback callback
    ){
    	return 
			callback instanceof NameCallback ||
			callback instanceof PasswordCallback ||
			callback instanceof TextInputCallback;
    }
    
    //Must be called from the event dispatch thread.
    protected void resetFocus() {
    	Callback[] callbacks = this.callbackContext.getCallbacks();
    	if(callbacks != null) {
	    	int i = 0;
	    	for(Callback callback : callbacks) {
	    		if(this.isEnabled(callback)) {
	    			this.fields[i].requestFocusInWindow();
	    			return;
	    		}
	    		i++;
	    	}
    	}
    }

}
