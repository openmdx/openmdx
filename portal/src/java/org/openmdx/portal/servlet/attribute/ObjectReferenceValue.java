/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ObjectReferenceValue.java,v 1.70 2010/04/27 12:21:07 wfro Exp $
 * Description: ObjectReferenceValue 
 * Revision:    $Revision: 1.70 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/27 12:21:07 $
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
package org.openmdx.portal.servlet.attribute;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.collection.MarshallingCollection;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.Autocompleter_1_0;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.ObjectReference;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.view.View;

public class ObjectReferenceValue
extends AttributeValue
implements Serializable {

    //-------------------------------------------------------------------------
    public static AttributeValue createObjectReferenceValue(
        Object object,
        FieldDef fieldDef,
        ApplicationContext application
    ) {
        // Return user defined attribute value class or ObjectReferenceValue as default
        String valueClassName = fieldDef == null
        ? null
            : (String)application.getMimeTypeImpls().get(fieldDef.mimeType);
        AttributeValue attributeValue = valueClassName == null
        ? null
            : AttributeValue.createAttributeValue(
                valueClassName,
                object,
                fieldDef,
                application
            );
        return attributeValue != null
        ? attributeValue
            : new ObjectReferenceValue(
                object,
                fieldDef,
                application
            );
    }

    //-------------------------------------------------------------------------
    protected ObjectReferenceValue(
        Object object,
        FieldDef fieldDef,
        ApplicationContext application
    ) {
        super(
            object, 
            fieldDef,
            application
        );
        this.objectReferenceMarshaller = new ObjectReferenceMarshaller(
            application
        );
    }

    //-------------------------------------------------------------------------
    public void refresh(
    ) {
        Object v = this.getValue(false);
        if(v instanceof ObjectReference) {
            ((ObjectReference)v).refresh();
        }
        else if(v instanceof Collection) {
            for(Iterator i = ((Collection)v).iterator(); i.hasNext(); ) {
                Object e = i.next();
                if(e instanceof ObjectReference) {
                    ((ObjectReference)e).refresh();
                }
            }
        }
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public Object getValue(
        boolean shortFormat
    ) {
        /**
         * Optimization: super.getValue() returns the value of
         * the feature 'identity' (= refMofId). The if|else sequence
         * below will end up in the else retrieving the object with
         * the refMofId. This results in an unecessary roundtrip
         */
        Object value = 
            this.object instanceof RefObject_1_0 && 
            this.fieldDef.qualifiedFeatureName.equals("org:openmdx:base:ExtentCapable:identity")
            ? this.object
                : super.getValue(shortFormat);
        if(value == null) {
            return new ObjectReference(
                (RefObject_1_0)null,
                this.application
            );
        }
        else if(value instanceof Exception) {
            return new ObjectReference(
                new ServiceException((Exception)value),
                this.application
            );        
        }
        else if(value instanceof Collection) {
            return new MarshallingCollection(
                this.objectReferenceMarshaller,
                (Collection)value
            );
        }
        else if(value instanceof RefObject_1_0) {
            return new ObjectReference(
                (RefObject_1_0)value,
                this.application
            );
        }
        else {
        	SysLog.warning("Reference is of type String instead RefObject or Collection. Retrieving object with new persistence manager");
            Path objectIdentity = new Path(value.toString());
            return new ObjectReference(
                (RefObject_1_0)this.application.getNewPmData().getObjectById(objectIdentity),
                this.application
            );
        }
    }

    //-------------------------------------------------------------------------
    public Object getDefaultValue(
    ) {      
        return this.fieldDef.defaultValue;
    }

    //-------------------------------------------------------------------------
    public String getBackColor(
    ) {
        Object value = this.getValue(false);
        if(value instanceof ObjectReference) {
            return ((ObjectReference)value).getBackColor();
        }
        return null;
    }

    //-------------------------------------------------------------------------
    public String getColor(
    ) {
        Object value = this.getValue(false);
        if(value instanceof ObjectReference) {
            return ((ObjectReference)value).getColor();
        }
        return null;
    }

    //-------------------------------------------------------------------------
    public String toString(
    ) {
        Object value = this.getValue(false);
        if(value == null) {
            return "";
        }
        else if(value instanceof ObjectReference) {
            return value.toString();
        }
        else if(value instanceof Collection) {
            List<String> titles = new ArrayList<String>();
            for(Iterator i = ((Collection)value).iterator(); i.hasNext(); ) {
                titles.add(i.next().toString());
            }
            return titles.toString();
        }
        else {
            return value.toString();
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Prepares a single stringified value to append.
     */
    protected String getStringifiedValueInternal(
        ViewPort p, 
        Object v,
        boolean multiLine,
        boolean forEditing,
        boolean shortFormat
    ) {
        if(forEditing) {
            return super.getStringifiedValueInternal(
                p, 
                v, 
                multiLine, 
                forEditing,
                shortFormat
            );
        }
        else {
            Action action = ((ObjectReference)v).getSelectObjectAction();
            String encodedTitle = (action.getTitle().startsWith("<") ? 
            	action.getTitle() : 
            	this.application.getHtmlEncoder().encode(action.getTitle(), false));
            return action.getEvent() == Action.EVENT_NONE ? 
            	encodedTitle : 
            	"<a href=\"\" onmouseover=\"javascript:this.href=" + p.getEvalHRef(action) + ";onmouseover=function(){};\">" + encodedTitle + "</a>";
        }
    }

    //-------------------------------------------------------------------------
    public static class ObjectReferenceMarshaller
    implements Marshaller, Serializable {

        public ObjectReferenceMarshaller(
            ApplicationContext application
        ) {
            this.application = application;
        }

        public Object marshal(Object source) throws ServiceException {
            if(source instanceof RefObject_1_0) {
                return new ObjectReference(
                    (RefObject_1_0)source,
                    this.application
                );
            }
            else if(source == null) {
                return new ObjectReference(
                    new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_FOUND,
                        "Null object can not be marshalled",
                        new BasicException.Parameter("path")
                    ),
                    this.application
                );
            }
            else {
                return source;
            }
        }

        public Object unmarshal(Object source) throws ServiceException {
            if(source instanceof ObjectReference) {
                return ((ObjectReference)source).getObject();
            }
            else {
                return source;
            }
        }

        private static final long serialVersionUID = 3257289132211778355L;
        private final ApplicationContext application;
    }

    //-------------------------------------------------------------------------
    @Override
    public void paint(
        Attribute attribute,
        ViewPort p,
        String id,
        String label,
        RefObject_1_0 lookupObject,
        int nCols,
        int tabIndex,
        String gapModifier,
        String styleModifier,
        String widthModifier,
        String rowSpanModifier,
        String readonlyModifier,
        String disabledModifier,
        String lockedModifier,
        String stringifiedValue,
        boolean forEditing
    ) throws ServiceException {
        HtmlEncoder_1_0 htmlEncoder = p.getApplicationContext().getHtmlEncoder();                
        View view = p.getView();
        label = this.getLabel(attribute, p, label);
        if(forEditing && this.isSingleValued()) {
            String feature = this.getName();
            ObjectReference objectReference = (ObjectReference)this.getValue(false);
            id = (id == null) || (id.length() == 0) ? 
                feature + "[" + tabIndex + "]" : 
                id;
            p.write("<td class=\"label\"><span class=\"nw\">", htmlEncoder.encode(label, false), "</span></td>");            
            p.write("<td ", rowSpanModifier, ">");
            // Predefined, selectable values only allowed for single-valued attributes with spanRow == 1
            // Show drop-down instead of input field
            Autocompleter_1_0 autocompleter = this.isChangeable() ? 
                this.getAutocompleter(lookupObject) : 
                null;
            if(autocompleter == null) {
                String classModifier = this.isMandatory() ?
                    "valueL mandatory" :
                    "valueL";
                p.write("  <input id=\"", id, ".Title\" name=\"", id, ".Title\" type=\"text\" class=\"", classModifier, "\"", lockedModifier, "\" ", readonlyModifier, " tabindex=\"" + tabIndex, "\" value=\"", (objectReference == null ? "" : objectReference.getTitle()), "\"");
                p.writeEventHandlers("    ", attribute.getEventHandler());
                p.write("  >");
                p.write("  <input id=\"", id, "\" name=\"", id, "\" type=\"hidden\" class=\"valueLLocked\" readonly value=\"", (objectReference == null ? "" : objectReference.refMofId()), "\">");
            }
            else {
                autocompleter.paint(                    
                    p,
                    id,
                    tabIndex,
                    feature,
                    this,
                    false,
                    null,
                    "class=\"autocompleterInput\"",
                    this.isMandatory() ? 
                        "class=\"valueL valueAC mandatory\"" : 
                        "class=\"valueL valueAC\"",
                    null
                );                    
            }
            p.write("</td>");
            p.write("<td class=\"addon\" ", rowSpanModifier, ">");
            if(this.isChangeable()) {
                if(
                    (autocompleter == null) || 
                    !autocompleter.hasFixedSelectableValues()
                ) {
                    String lookupId = org.openmdx.kernel.id.UUIDs.newUUID().toString();
                    Action findObjectAction = view.getFindObjectAction(
                        feature, 
                        lookupId
                    );
                    p.write("  ", p.getImg("class=\"popUpButton\" border=\"0\" alt=\"Click to open ObjectFinder\" src=\"", p.getResourcePath("images/"), findObjectAction.getIconKey(), "\" onclick=\"javascript:OF.findObject(", p.getEvalHRef(findObjectAction), ", $('", id, ".Title'), $('", id, "'), '", lookupId, "');\""));                    
                }
            }
            p.write("</td>");
        }
        else {
            super.paint(
                attribute,
                p,
                id,
                label,
                lookupObject,
                nCols,
                tabIndex,
                gapModifier,
                styleModifier,
                widthModifier,
                rowSpanModifier,
                readonlyModifier,
                disabledModifier,
                lockedModifier,
                stringifiedValue,
                forEditing
            );
        }
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3617855270202519606L;

    private ObjectReferenceMarshaller objectReferenceMarshaller = null;

}

//--- End of File -----------------------------------------------------------
