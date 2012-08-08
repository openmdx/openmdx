/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: SimpleCallbackHandler.java,v 1.2 2005/11/08 14:28:22 hburger Exp $
 * Description: Simple Callback Handler
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/11/08 14:28:22 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
package org.openmdx.security.auth.servlet.simple;

import java.io.IOException;
import java.io.Writer;

import javax.security.auth.callback.ChoiceCallback;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.openmdx.base.exception.ExtendedIOException;
import org.openmdx.security.auth.servlet.spi.AbstractCallbackHandler;
import org.openmdx.security.auth.servlet.spi.TokenCallback;

/**
 * Simple Callback Handler
 */
public class SimpleCallbackHandler extends AbstractCallbackHandler {

    /**
     * Constructor
     */
    public SimpleCallbackHandler() {
        super();
    }

    /**
     * 
     */
    private String fieldPrefix;
    
    /**
     * 
     */
    private String title;

    /**
     * Construct a field name
     * @param fieldIndex
     * 
     * @return the <code>fieldPrefix</code>'s value concatenated with the index.
     */
    protected String getFieldName(
        int fieldIndex
    ) {
        return this.fieldPrefix + fieldIndex;
    }

    /**
     * Retrieve the title.
     * 
     * @return the <code>title</code>'s value
     */
    protected final String getTitle() {
        return this.title;
    }


    //------------------------------------------------------------------------
    // Extends AbstractHandler
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.spi.AbstractHandler#init()
     */
    protected void init() throws ServletException {
        this.fieldPrefix = super.getInitParameter(
            "field-prefix",
            "callback-"
        );
        this.title = super.getInitParameter(
            "title",
            "openMDX - Login"
        );
        if(isDebug()) {
            log("$Id: SimpleCallbackHandler.java,v 1.2 2005/11/08 14:28:22 hburger Exp $");
            log("field-prefix: " + this.fieldPrefix);
            log("title: " + this.title);
        }
    }

    
    //------------------------------------------------------------------------
    // Extends AbstractCallbackHandler
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.spi.AbstractCallbackHandler#beginHead(java.io.Writer)
     */
    protected void startHead(Writer html) throws IOException {
        super.startHead(html);
        html.write("<TITLE>");
        writeEncoded(html, getTitle());
        html.write("</TITLE>");
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.spi.AbstractCallbackHandler#startBody(java.io.Writer)
     */
    protected void startBody(Writer html) throws IOException {
        super.startBody(html);
        html.write("<H1>");
        writeEncoded(html, getTitle());
        html.write("</H1>");
    }

    
    //------------------------------------------------------------------------
    // Handle Callback Requests
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.spi.AbstractCallbackHandler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.security.auth.callback.ChoiceCallback)
     */
    protected boolean handle(
        HttpServletRequest request, 
        Writer html, 
        int index, 
        ChoiceCallback callback
    ) throws IOException, UnsupportedCallbackException {
        String fieldName = getFieldName(index);
        html.write("<P><LABEL>");
        writeEncoded(html, callback.getPrompt());
        html.write("&nbsp;<SELECT");
        if(callback.allowMultipleSelections()) html.write(" multiple");
        html.write(" name=\"");
        html.write(fieldName);
        html.write("\">");
        String[] choices = callback.getChoices();
        for(
            int i = 0, j = callback.getDefaultChoice();
            i < choices.length;
            i++
        ){
            html.write("<OPTION");
            if(i == j) html.write(" selected");
            html.write(" value=\"" + i + "\">");
            writeEncoded(html, choices[i]);
            html.write("</OPTION>");
        }
        html.write("</SELECT></LABEL></P>");
        return true;
    }

    /**
     * Write a button into a field set
     * @param html
     * @param fieldName
     * @param value
     * @param label
     * @param defaultValue TODO
     * 
     * @throws IOException
     */
    private void writeButton(
         Writer html,
         String fieldName,
         int value,
         String label, 
         int defaultValue
    ) throws IOException{
        html.write("<INPUT name=\""); 
        html.write(fieldName);
        html.write('"');
        if(value == defaultValue) html.write(" checked");
        html.write(" type=\"radio\" value=\"" + value + "\">");
        writeEncoded(html, label);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.spi.AbstractCallbackHandler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.security.auth.callback.ConfirmationCallback)
     */
    protected boolean handle(
        HttpServletRequest request, 
        Writer html, int index, 
        ConfirmationCallback callback
    ) throws IOException, UnsupportedCallbackException {
        String fieldName = getFieldName(index);
        html.write("<FIELDSET><LEGEND>");
        switch(callback.getMessageType()) {
            case ConfirmationCallback.INFORMATION:
                html.write("Information");
                break;
            case ConfirmationCallback.WARNING:
                html.write("Warning");
                break;
            case ConfirmationCallback.ERROR:
                html.write("Error");
                break;
        }
        html.write("</LEGEND>");
        String prompt = callback.getPrompt(); 
        if(prompt != null) {
            html.write("&nbsp;");
            writeEncoded(html, prompt);
        }
        int defaultOption = callback.getDefaultOption();
        switch(callback.getOptionType()) {
            case ConfirmationCallback.UNSPECIFIED_OPTION:
                String[] options = callback.getOptions();
                for(
                    int i = 0;
                    i < options.length;
                    i++
                ) writeButton(html, fieldName, i, options[i], defaultOption);
                break;
            case ConfirmationCallback.YES_NO_OPTION:
                writeButton(html, fieldName, ConfirmationCallback.YES, "Yes", defaultOption);
                writeButton(html, fieldName, ConfirmationCallback.NO, "No", defaultOption);
                break;
            case ConfirmationCallback.YES_NO_CANCEL_OPTION:
                writeButton(html, fieldName, ConfirmationCallback.YES, "Yes", defaultOption);
                writeButton(html, fieldName, ConfirmationCallback.NO, "No", defaultOption);
                writeButton(html, fieldName, ConfirmationCallback.CANCEL, "Cancel", defaultOption);
                break;
            case ConfirmationCallback.OK_CANCEL_OPTION:
                writeButton(html, fieldName, ConfirmationCallback.OK, "OK", defaultOption);
                writeButton(html, fieldName, ConfirmationCallback.CANCEL, "Cancel", defaultOption);
                break;
        }
        html.write("</FIELDSET>");
        return true;
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.spi.AbstractCallbackHandler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.security.auth.callback.NameCallback)
     */
    protected boolean handle(
        HttpServletRequest request, 
        Writer html, 
        int index, 
        NameCallback callback
    ) throws IOException, UnsupportedCallbackException {
        String fieldName = getFieldName(index);
        html.write("<P><LABEL>");
        writeEncoded(html, callback.getPrompt());
        html.write("&nbsp;<INPUT name=\""); 
        html.write(fieldName);
        html.write("\" type=\"text");
        String value = callback.getDefaultName();
        if(value != null) {
            html.write("\" value=\"");
            writeEncoded(html, value);
        }
        html.write("\"></LABEL></P>");
        return true;
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.spi.AbstractCallbackHandler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.security.auth.callback.PasswordCallback)
     */
    protected boolean handle(
        HttpServletRequest request, 
        Writer html, 
        int index, 
        PasswordCallback callback
    ) throws IOException, UnsupportedCallbackException {
        String fieldName = getFieldName(index);
        html.write("<P><LABEL>");
        writeEncoded(html, callback.getPrompt());
        html.write("&nbsp;<INPUT name=\""); 
        html.write(fieldName);
        html.write("\" type=\"");
        html.write(callback.isEchoOn() ? "text" : "password");
        html.write("\"></LABEL></P>");
        return true;
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.spi.AbstractCallbackHandler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.security.auth.callback.TextInputCallback)
     */
    protected boolean handle(
        HttpServletRequest request, 
        Writer html, 
        int index,
        TextInputCallback callback
    ) throws IOException, UnsupportedCallbackException {
        String fieldName = getFieldName(index);
        html.write("<P><LABEL>");
        writeEncoded(html, callback.getPrompt());
        html.write("&nbsp;<INPUT name=\""); 
        html.write(fieldName);
        html.write("\" type=\"text\"");
        String value = callback.getDefaultText();
        if(value != null) {
            html.write(" value=\"");
            writeEncoded(html, value);
            html.write('"');
        }
        html.write("></LABEL></P>");
        return true;
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.spi.AbstractCallbackHandler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.security.auth.callback.TextOutputCallback)
     */
    protected boolean handle(
        HttpServletRequest request, 
        Writer html, 
        int index, 
        TextOutputCallback callback
    ) throws IOException, UnsupportedCallbackException {
        html.write("<FIELDSET><LEGEND>");
        switch(callback.getMessageType()) {
            case ConfirmationCallback.INFORMATION:
                html.write("Information");
                break;
            case ConfirmationCallback.WARNING:
                html.write("Warning");
                break;
            case ConfirmationCallback.ERROR:
                html.write("Error");
                break;
        }
        html.write("</LEGEND>");
        html.write("&nbsp;");
        writeEncoded(html, callback.getMessage());
        html.write("</FIELDSET>");
        return true;
    }

    /* (non-Javadoc)
     * @see org.openmdx.security.auth.servlet.spi.AbstractCallbackHandler#handle(javax.servlet.http.HttpServletRequest, java.io.Writer, int, org.openmdx.security.auth.servlet.spi.TokenCallback)
     */
    protected boolean handle(
        HttpServletRequest request, 
        Writer html, 
        int index, 
        TokenCallback callback
    ) throws IOException, UnsupportedCallbackException {
        String fieldName = getFieldName(index);
        html.write("<INPUT name=\""); 
        html.write(fieldName);
        html.write("\" type=\"hidden");
        String value = callback.getToken();
        if(value != null) {
            html.write("\" value=\"");
            writeEncoded(html, value);
        }
        html.write("\">");
        return true;
    }

    
    //------------------------------------------------------------------------
    // Handle Callback Replies
    //------------------------------------------------------------------------

    /**
     * 
     */
    private static int[] toIndices(
        String[] values
    ) throws IOException{
        if(values == null) {
            return null;
        } else try {
            int[] indices = new int[values.length];
            for(
                int i = 0;
                i < values.length;
                i++
            ) indices[i] = Integer.parseInt(values[i]);
            return indices;
        } catch (NumberFormatException exception) {
            throw new ExtendedIOException(exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.spi.AbstractCallbackHandler#handle(javax.servlet.http.HttpServletRequest, javax.security.auth.callback.ChoiceCallback)
     */
    protected void handle(
        HttpServletRequest request, 
        int index, 
        ChoiceCallback callback
    ) throws IOException, UnsupportedCallbackException {
        int[] values = toIndices(request.getParameterValues(getFieldName(index)));
        if(values != null) {
            if(values.length == 1) {
                callback.setSelectedIndex(values[0]);
            } else {
                callback.setSelectedIndexes(values);
            }
        }
    }


    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.spi.AbstractCallbackHandler#handle(javax.servlet.http.HttpServletRequest, javax.security.auth.callback.ConfirmationCallback)
     */
    protected void handle(
        HttpServletRequest request, 
        int index, 
        ConfirmationCallback callback
    ) throws IOException, UnsupportedCallbackException {
        int[] values = toIndices(request.getParameterValues(getFieldName(index)));
        if(values != null) callback.setSelectedIndex(values[0]);
    }


    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.spi.AbstractCallbackHandler#handle(javax.servlet.http.HttpServletRequest, javax.security.auth.callback.NameCallback)
     */
    protected void handle(
        HttpServletRequest request, 
        int index, 
        NameCallback callback
    ) throws IOException, UnsupportedCallbackException {
        String[] values = request.getParameterValues(getFieldName(index));
        if(values != null) callback.setName(values[0]);
    }


    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.spi.AbstractCallbackHandler#handle(javax.servlet.http.HttpServletRequest, javax.security.auth.callback.PasswordCallback)
     */
    protected void handle(
        HttpServletRequest request, 
        int index, 
        PasswordCallback callback
    ) throws IOException, UnsupportedCallbackException {
        String[] values = request.getParameterValues(getFieldName(index));
        if(values != null) callback.setPassword(values[0].toCharArray());
    }


    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.spi.AbstractCallbackHandler#handle(javax.servlet.http.HttpServletRequest, javax.security.auth.callback.TextInputCallback)
     */
    protected void handle(
        HttpServletRequest request, 
        int index, 
        TextInputCallback callback
    ) throws IOException, UnsupportedCallbackException {
        String[] values = request.getParameterValues(getFieldName(index));
        if(values != null) callback.setText(values[0]);
    }

    /* (non-Javadoc)
     * @see org.openmdx.security.auth.servlet.spi.AbstractCallbackHandler#handle(javax.servlet.http.HttpServletRequest, int, org.openmdx.security.auth.servlet.spi.TokenCallback)
     */
    protected void handle(
        HttpServletRequest request, 
        int index, 
        TokenCallback callback
    ) throws IOException, UnsupportedCallbackException {
        String[] values = request.getParameterValues(getFieldName(index));
        if(values != null) callback.setToken(values[0]);
    }

}
