/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: XMI1 Parser Constants
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
 */

package org.openmdx.application.mof.externalizer.xmi;

public final class XMI1Constants {
  
  public static final String XMI = "XMI";

  // XMI1 interchange metamodel classes
  public static final String MODEL = "UML:Model";
  public static final String PACKAGE = "UML:Package";
  public static final String DATATYPE = "UML:DataType";
  public static final String CLASS = "UML:Class";
  public static final String ATTRIBUTE = "UML:Attribute";
  public static final String STEREOTYPE = "UML:Stereotype";
  public static final String PARAMETER = "UML:Parameter";
  public static final String OPERATION = "UML:Operation";
  public static final String MULTIPLICITY_RANGE = "UML:MultiplicityRange";
  public static final String ASSOCIATION = "UML:Association";
  public static final String ASSOCIATION_END = "UML:AssociationEnd";
  public static final String TAG_DEFINITION = "UML:TagDefinition";
  public static final String TAGGED_VALUE = "UML:TaggedValue";
  public static final String GENERALIZATION = "UML:Generalization";
  public static final String COMMENT = "UML:Comment";
  
  // XMI1 interchange metamodel references
  public static final String BEHAVIORALFEATURE_PARAMETER = "UML:BehavioralFeature.parameter";
  public static final String STRUCTURALFEATURE_TYPE = "UML:StructuralFeature.type";
  public static final String STRUCTURALFEATURE_MULTIPLICITY = "UML:StructuralFeature.multiplicity";
  public static final String PARAMETER_TYPE = "UML:Parameter.type";
  public static final String ASSOCIATION_CONNECTION = "UML:Association.connection";
  public static final String ASSOCIATION_END_MULTIPLICITY = "UML:AssociationEnd.multiplicity";
  public static final String ASSOCIATION_END_PARTICIPANT = "UML:AssociationEnd.participant";
  public static final String ASSOCIATION_END_QUALIFIER = "UML:AssociationEnd.qualifier";
  public static final String CLASSIFIER_FEATURE = "UML:Classifier.feature";
  public static final String METHOD_SPECIFICATION = "UML:Method.specification";
  public static final String COMMENT_ANNOTATEDELEMENT = "UML:Comment.annotatedElement";
  public static final String GENERALIZATION_CHILD = "UML:Generalization.child";
  public static final String GENERALIZATION_PARENT = "UML:Generalization.parent";
  public static final String NAMESPACE_OWNEDELEMENT = "UML:Namespace.ownedElement";
  public static final String MODELELEMENT_STEREOTYPE = "UML:ModelElement.stereotype";
  public static final String MODELELEMENT_TAGGEDVALUE = "UML:ModelElement.taggedValue";
  public static final String MODELELEMENT_COMMENT = "UML:ModelElement.comment";
  public static final String TAGGEDVALUE_TYPE = "UML:TaggedValue.type";
  public static final String TAGGEDVALUE_DATAVALUE = "UML:TaggedValue.dataValue";
  public static final String GENERALIZABLEELEMENT_GENERALIZATION = "UML:GeneralizableElement.generalization";
  
  // Poseidon-specific diagram information
  public static final String GRAPHNODE = "UML:GraphNode";
  public static final String UML1SEMANTICMODELBRIDGE_ELEMENT = "UML:Uml1SemanticModelBridge.element";
  
}
