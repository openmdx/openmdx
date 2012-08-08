/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: AttributeValueFactory.java,v 1.29 2009/06/16 17:08:26 wfro Exp $
 * Description: AttributeMapper
 * Revision:    $Revision: 1.29 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/16 17:08:26 $
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
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

import javax.jmi.reflect.RefObject;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.spi.RefMetaObject_1;
import org.openmdx.base.accessor.jmi.spi.RefPackage_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.ApplicationContext;

public final class AttributeValueFactory
  implements Serializable {

  //-------------------------------------------------------------------------
  public AttributeValueFactory(
  ) {
  }

  //-------------------------------------------------------------------------
  public synchronized void reset(
  ) {
      this.fieldDefs.clear();
  }

  
  //-------------------------------------------------------------------------
  /**
   * Maps a field of object to the specific AttributeValue type.
   * 
   * @param object either a RefObject_1_0 or a Map containing the field.
   */
  public AttributeValue getAttributeValue(
      org.openmdx.ui1.jmi1.ValuedField field,
      Object object,
      ApplicationContext application
  ) throws ServiceException {

    Path fieldIdentity = null;
    try {
        fieldIdentity = field.refGetPath();
    }
    // Retry to get the refMofId if the first attempt fails
    // Getting the refMofId the first time modifies a JMI object
    // JMI objects are not thread-safe which may throw a 
    // ConcurrentModificationException. This and other methods of 
    // openMDX/Portal are not synchronized a) for performance reasons 
    // and b) because ui objects are updated very rarely (typically 
    // only the first time a control is initialized)
    catch(ConcurrentModificationException e) {
        try { 
        	Thread.sleep(10); 
        } 
        catch(Exception e0) {}
        fieldIdentity = field.refGetPath();
    }
    
    SysLog.trace("mapping field", field);

    Model_1_0 model = null;
    ModelElement_1_0 classDef = null;

    // RefObject_1_0: derive class from object
    if(object instanceof RefObject_1_0) {
        model = ((RefPackage_1)((RefObject_1_0)object).refOutermostPackage()).refModel();
        classDef = ((RefMetaObject_1)((RefObject)object).refMetaObject()).getElementDef();
    }
    // Map: object must be class name
    else if(object instanceof Map) {
        model = application.getModel();
        try {
            classDef = model.getElement(((Map)object).get(SystemAttributes.OBJECT_CLASS));
        } 
        catch(Exception e) {}
    }

    AttributeValue value = NullValue.createNullValue();
    FieldDef fieldDef = (FieldDef)this.fieldDefs.get(fieldIdentity);

    // Number / Code
    if(field instanceof org.openmdx.ui1.jmi1.NumberField) {
      org.openmdx.ui1.jmi1.NumberField f = (org.openmdx.ui1.jmi1.NumberField)field;
      String qualifiedTypeName = null;
      String qualifiedClassName = object instanceof RefObject_1_0
          ? ((RefObject_1_0)object).refClass().refMofId()
          : object instanceof Map
              ? (String)((Map)object).get(SystemAttributes.OBJECT_CLASS)
              : null;
      if(qualifiedClassName != null) {
          try {
              ModelElement_1_0 compositeReference = model.getElement(classDef.objGetValue("compositeReference"));
              ModelElement_1_0 typeDef = model.getElement(compositeReference.objGetValue("type"));
              qualifiedTypeName = (String)typeDef.objGetValue("qualifiedName");
          } 
          catch(Exception e) {}
      }
      // return code value in case a code table is defined for feature for the instance-level class
      if(
          (qualifiedClassName != null) &&
          (application.getCodes() != null) &&
          application.getCodes().getLongText(qualifiedClassName + ":" + f.getFeatureName(), (short)0, true, true) != null
      ) {
          if(fieldDef == null) {
              this.fieldDefs.put(
                  fieldIdentity,
                  fieldDef = FieldDef.createFieldDef(application, field)
              );
          }
          value = CodeValue.createCodeValue(
              object,
              fieldDef,
              application,
              qualifiedClassName + ":" + f.getFeatureName()
          );
      }
      // Each concrete class has a compositeReference, i.e. is referenced by a 
      // parent class with aggregation kind = composite. Determine the type, i.e. 
      // class of the composite reference. The referenced class with all its subclasses 
      // defines a class hierarchy. The ObjectInspectorServlet allows to define codes 
      // either for a) a fully qualified attribute name or b) for an attribute of a class 
      // hierarchy. Option b) allows to define individual code tables for each class 
      // hiearchy in case the attribute is member of an abstract root class.
      else if(
          (application.getCodes() != null) &&
          (qualifiedTypeName != null) &&
          (application.getCodes().getLongText(qualifiedTypeName + ":" + f.getFeatureName(), (short)0, true, true) != null)
      ) {
          if(fieldDef == null) {
              this.fieldDefs.put(
                  fieldIdentity,
                  fieldDef = FieldDef.createFieldDef(application, field)
              );
          }
          value = CodeValue.createCodeValue(
              object,
              fieldDef,
              application,
              qualifiedTypeName + ":" + f.getFeatureName()
          );
      }
      else if(
          (application.getCodes() != null) &&
          application.getCodes().getLongText(f.getQualifiedFeatureName(), (short)0, true, true) != null) {
          if(fieldDef == null) {
              this.fieldDefs.put(
                  fieldIdentity,
                  fieldDef = FieldDef.createFieldDef(application, field)
              );
          }
          value = CodeValue.createCodeValue(
              object,
              fieldDef,
              application,
              f.getQualifiedFeatureName()
            );
      }
      else {
          org.openmdx.ui1.jmi1.NumberField numberField = (org.openmdx.ui1.jmi1.NumberField)field;
          if(fieldDef == null) {
              this.fieldDefs.put(
                  fieldIdentity,
                  fieldDef = FieldDef.createNumberFieldDef(application, numberField)
              );
          }
          value = NumberValue.createNumberValue(
              object,
              fieldDef,
              f.isHasThousandsSeparator(),
              f.getMinValue(),
              f.getMaxValue(),
              application
          );
      }
    }
    else if(field instanceof org.openmdx.ui1.jmi1.DateField) {
        org.openmdx.ui1.jmi1.DateField f = (org.openmdx.ui1.jmi1.DateField)field;
        if(fieldDef == null) {
            this.fieldDefs.put(
                fieldIdentity,
                fieldDef = FieldDef.createDateFieldDef(application, f)
            );
        }
        value = DateValue.createDateValue(
            object,
            fieldDef,
            application
        );
    }
    else if(field instanceof org.openmdx.ui1.jmi1.ObjectReferenceField) {
        org.openmdx.ui1.jmi1.ValuedField f = field;
        if(fieldDef == null) {
          this.fieldDefs.put(
                fieldIdentity,
                fieldDef = FieldDef.createFieldDef(application, f)
            );
        }
        value = ObjectReferenceValue.createObjectReferenceValue(
            object,
            fieldDef,
            application
        );
    }
    else if(field instanceof org.openmdx.ui1.jmi1.TextField) {
        org.openmdx.ui1.jmi1.TextField f = (org.openmdx.ui1.jmi1.TextField)field;
        if(fieldDef == null) {
          this.fieldDefs.put(
                fieldIdentity,
                fieldDef = FieldDef.createFieldDef(application, f)
            );
        }
        value = TextValue.createTextValue(
            object,
            fieldDef,
            false,
            Integer.MAX_VALUE,
            application
        );
    }
    else if(field instanceof org.openmdx.ui1.jmi1.TextBox) {
        org.openmdx.ui1.jmi1.TextBox f = (org.openmdx.ui1.jmi1.TextBox)field;
        if(fieldDef == null) {
          this.fieldDefs.put(
                fieldIdentity,
                fieldDef = FieldDef.createFieldDef(application, f)
            );
        }
        value = TextValue.createTextValue(
            object,
            fieldDef,
            f.isPassword(),
            f.getMaxLength(),
            application
        );
    }
    else if(field instanceof org.openmdx.ui1.jmi1.CheckBox) {
        org.openmdx.ui1.jmi1.ValuedField f = field;
        if(fieldDef == null) {
          this.fieldDefs.put(
                fieldIdentity,
                fieldDef = FieldDef.createFieldDef(application, f)
            );
        }
        value = BooleanValue.createBooleanValue(
            object,
            fieldDef,
            application
        );
    }
    else if(field instanceof org.openmdx.ui1.jmi1.DocumentBox) {
        org.openmdx.ui1.jmi1.DocumentBox f = (org.openmdx.ui1.jmi1.DocumentBox)field;
        if(fieldDef == null) {
          this.fieldDefs.put(
                fieldIdentity,
                fieldDef = FieldDef.createBinaryFieldDef(application, f)
            );
        }
        value = BinaryValue.createBinaryValue(
            object,
            fieldDef,
            application
        );
    }
    return value;
  }

  //-------------------------------------------------------------------------
  private static final long serialVersionUID = 3256728385526182196L;

  private final Map<Path,FieldDef> fieldDefs = new HashMap<Path,FieldDef>();

}

//--- End of File -----------------------------------------------------------
