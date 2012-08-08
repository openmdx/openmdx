/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: CutDerivedFeaturesHandler.java,v 1.4 2008/05/12 10:45:51 wfro Exp $
 * Description: 
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/05/12 10:45:51 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
 */
package org.openmdx.compatibility.base.dataprovider.exporter;

import java.util.Iterator;
import java.util.Map;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;

/**
 * Delegating handler which cuts the derived features of the objects 
 * passing through. Must be introduced into a chain of TraversalHandlers 
 * which process the input of a ProviderTraverser.
 * <p>
 * Eg.
 * ProviderTraverser --> DelegatingHandler --> XmlExporter 
 */
@SuppressWarnings("unchecked")
public class CutDerivedFeaturesHandler extends DelegatingHandler {

   /**
    * Model containing the model information. Contains all the model information 
    * for the objects treated. 
    */
   private final Model_1_0 model;
   
   /**
    * Delegating handler which removes derived features of a class.
    * 
    * Must be put in between a Traverser and and some TraversalHandler which
    * produces output.
    * 
    * @param delegation object to delegate the calls to
    * @param model must contain the model information for all classes treated 
    */
   public CutDerivedFeaturesHandler(
      TraversalHandler delegation,
      Model_1_0 model
   ) {
      super(delegation);
      
      this.model = model;
   }
   
   /**
    * Removes derived features from the object provided.
    * 
    * @see ch.omex.spice.dataprovider.exporter.TraversalHandler#featureComplete(ch.omex.spice.dataprovider.generic.DataproviderObject_1_0)
    */
   public boolean featureComplete(
       Path reference,
       DataproviderObject_1_0 object,
       Map tags
   ) throws ServiceException {
      ModelElement_1_0 objectClass = null;
      if (object.getValues(SystemAttributes.OBJECT_CLASS) != null) {
         objectClass =
            this.model.getDereferencedType(
               object.getValues(SystemAttributes.OBJECT_CLASS).get(0));
      }
      
      if (objectClass != null) {
         // get a map containing model-information for the features of this class
         Map classFeatures = (Map) objectClass.getValues("allFeature").get(0);

         for (Iterator attr = object.attributeNames().iterator(); 
            attr.hasNext();
         ) {
            String feature = (String) attr.next();
            
            if (object.getValues(feature) != null 
                && object.getValues(feature).size() != 0
            ) {
               DataproviderObject featureType =
                   (DataproviderObject) classFeatures.get(feature);
   
               if (featureType == null) {
                  // System.out.println("unkonwn attribute type for: "+ attribute +" in "+ object.getValues(SystemAttributes.OBJECT_CLASS).get(0));
                  continue;
               }
               
               if (featureType.getValues(SystemAttributes.OBJECT_CLASS).get(0)
                     .equals("org:omg:model1:Attribute") && 
                  ((Boolean)featureType.getValues("isDerived").get(0)).booleanValue()
               ) {
                  attr.remove();
               }
               else 
               if (featureType.getValues(SystemAttributes.OBJECT_CLASS).get(0)
                     .equals("org:omg:model1:Reference") &&
                  this.model.referenceIsDerived(featureType)
               ) {
                 attr.remove();
               }
               // else it remains
            }
         }
      }
      
      return super.featureComplete(
          reference,
          object
      );
   }
}

