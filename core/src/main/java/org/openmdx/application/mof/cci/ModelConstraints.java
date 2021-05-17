/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: Generated constants for ModelConstraints
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package org.openmdx.application.mof.cci;


/**
 * The <code>ModelConstraints</code> class contains the model constraints that are used by the class
 * ModelConstraintsChecker_1
 */




public class ModelConstraints {

  protected ModelConstraints() {
      // Avoid instantiation
  }


  // MOF constraints
  static public final String MUST_BE_CONTAINED_UNLESS_PACKAGE = "[MOF C-1] A ModelElement that is not a Package must have a container";
  static public final String FROZEN_ATTRIBUTES_CANNOT_BE_CHANGED = "[MOF C-2] The attribute values of a ModelElement which is frozen cannot be changed";
  static public final String FROZEN_ELEMENTS_CANNOT_BE_DELETED = "[MOF C-3] A frozen ModelElement which is in a frozen Namespace can only be deleted, by deleting the Namespace";
  static public final String FROZEN_DEPENDENCIES_CANNOT_BE_CHANGED = "[MOF C-4] The link sets that express dependencies of a frozen Element on other Elements cannot be explicitly changed";
  static public final String CONTENT_NAMES_MUST_NOT_COLLIDE = "[MOF C-5] The names of the contents of a Namespace must not collide";
  static public final String SUPERTYPE_MUST_NOT_BE_SELF = "[MOF C-6] A Generalizable Element cannot be its own direct or indirect supertype";
  static public final String SUPERTYPE_KIND_MUST_BE_SAME = "[MOF C-7] A supertypes of a GeneralizableElement must be of the same kind as the GeneralizableElement itself";
  static public final String CONTENTS_MUST_NOT_COLLIDE_WITH_SUPERTYPES = "[MOF C-8] The names of the contents of a GeneralizableElement should not collide with the names of the contents of any direct or indirect supertype";
  static public final String DIAMOND_RULE_MUST_BE_OBEYED = "[MOF C-9] Multiple inheritance must obey the \"Diamond Rule\"";
  static public final String NO_SUPERTYPES_ALLOWED_FOR_ROOT = "[MOF C-10] If a Generalizable Element is marked as a \"root\", it cannot have any supertypes";
  static public final String SUPERTYPES_MUST_BE_VISIBLE = "[MOF C-11] A GeneralizableElement\'s immediate supertypes must all be visible to it";
  static public final String NO_SUBTYPES_ALLOWED_FOR_LEAF = "[MOF C-12] A GeneralizableElement cannot inherit from a GeneralizableElement defined as a \"leaf\"";
  static public final String ASSOCIATIONS_CANNOT_BE_TYPES = "[MOF C-13] An Association cannot be the type of a TypedElement";
  static public final String TYPE_MUST_BE_VISIBLE = "[MOF C-14] A TypedElement can only have a type that is visible to it";
  static public final String CLASS_CONTAINMENT_RULES = "[MOF C-15] A Class may contain only Classes, DataTypes, Attributes, References, Operations, Exceptions, Constants, Constraints, and Tags";
  static public final String ABSTRACT_CLASSES_CANNOT_BE_SINGLETON = "[MOF C-16] A Class that is marked as abstract cannot also be marked as singleton";
  static public final String DATA_TYPE_CONTAINMENT_RULES = "[MOF C-17] A DataType may contain only TypeAliases, Constraints, Tags (or in the case of StructureTypes) StructureFields";
  static public final String DATA_TYPES_HAVE_NO_SUPERTYPES = "[MOF C-19] Inheritance / generalization is not applicable to DataTypes";
  static public final String DATA_TYPES_CANNOT_BE_ABSTRACT = "[MOF C-20] A DataType cannot be abstract";
  static public final String REFERENCE_MULTIPLICITY_MUST_MATCH_END = "[MOF C-21] The multiplicity for a Reference must be the same as the multiplicity for the referenced AssociationEnd";
  static public final String REFERENCE_MUST_BE_INSTANCE_SCOPED = "[MOF C-22] Classifier scoped References are not meaningful in the current M1 level computational model";
  static public final String CHANGEABLE_REFERENCE_MUST_HAVE_CHANGEABLE_END = "[MOF C-23] A Reference can be changeable only if the referenced AssociationEnd is also changeable";
  static public final String REFERENCE_TYPE_MUST_MATCH_END_TYPE = "[MOF C-24] The type attribute of a Reference and its referenced AssociationEnd must be the same";
  static public final String REFERENCED_END_MUST_BE_NAVIGABLE = "[MOF C-25] A Reference is only allowed for a navigable AssociationEnd";
  static public final String CONTAINER_MUST_MATCH_EXPOSED_TYPE = "[MOF C-26] The containing Class for a Reference must be equal to or a subtype of the type of the Reference\'s exposed AssociationEnd";
  static public final String REFERENCED_END_MUST_BE_VISIBLE = "[MOF C-27] The referenced AssociationEnd for a Reference must be visible from the Reference";
  static public final String OPERATION_CONTAINMENT_RULES = "[MOF C-28] An Operation may only contain Parameters, Constraints, and Tags";
  static public final String OPERATIONS_HAVE_AT_MOST_ONE_RETURN = "[MOF C-29] An Operation may have at most one Parameter whose direction is \"return\"";
  static public final String OPERATION_EXCEPTIONS_MUST_BE_VISIBLE = "[MOF C-30] The Exceptions raised by an Operation must be visible to the Operation";
  static public final String EXCEPTION_CONTAINMENT_RULES = "[MOF C-31] An Exception may only contain Parameters and Tags";
  static public final String EXCEPTIONS_HAVE_ONLY_OUT_PARAMETERS = "[MOF C-32] An Exception\'s Parameters must all have the direction \"out\"";
  static public final String ASSOCIATIONS_CONTAINMENT_RULES = "[MOF C-33] An Association may only contain AssociationEnds, Constraints, and Tags";
  static public final String ASSOCIATIONS_HAVE_NO_SUPERTYPES = "[MOF C-34] Inheritance / generalization is not applicable to Associations";
  static public final String ASSOCIATIONS_MUST_BE_ROOT_AND_LEAF = "[MOF C-35] The values for \"isLeaf\" and \"isRoot\" on an Association must be true";
  static public final String ASSOCIATIONS_CANNOT_BE_ABSTRACT = "[MOF C-36] An Association cannot be abstract";
  static public final String ASSOCIATIONS_MUST_BE_PUBLIC = "[MOF C-37] Associations must have visibility of \"public\"";
  static public final String ASSOCIATIONS_MUST_BE_BINARY = "[MOF C-38] An Association must be binary; that is, it must have exactly two AssociationEnds";
  static public final String END_TYPE_MUST_BE_CLASS = "[MOF C-39] The type of an AssociationEnd must be Class";
  static public final String ENDS_MUST_BE_UNIQUE = "[MOF C-40] The \"isUnique\" flag in an AssociationEnd\'s multiplicity must be true";
  static public final String CANNOT_HAVE_TWO_ORDERED_ENDS = "[MOF C-41] An Association cannot have two AssociationEnds marked as \"ordered\"";
  static public final String CANNOT_HAVE_TWO_AGGREGATE_ENDS = "[MOF C-42] An Association cannot have an aggregation semantic specified for both AssociationEnds";
  static public final String PACKAGE_CONTAINMENT_RULES = "[MOF C-43] A Package may only contain Packages, Classes, DataTypes, Associations, Exceptions, Constants, Constraints, Imports, and Tags";
  static public final String PACKAGES_CANNOT_BE_ABSTRACT = "[MOF C-44] Packages cannot be declared as abstract";
  static public final String IMPORTED_NAMESPACE_MUST_BE_VISIBLE = "[MOF C-45] The Namespace imported by an Import must be visible to the Import\'s containing Package";
  static public final String CAN_ONLY_IMPORT_PACKAGES_AND_CLASSES = "[MOF C-46] It is only legal for a Package to import or cluster Packages or Classes";
  static public final String CANNOT_IMPORT_SELF = "[MOF C-47] Packages cannot import or cluster themselves";
  static public final String CANNOT_IMPORT_NESTED_COMPONENTS = "[MOF C-48] Packages cannot import or cluster Packages or Classes that they contain";
  static public final String NESTED_PACKAGES_CANNOT_IMPORT = "[MOF C-49] Nested Packages cannot import or cluster other Packages or Classes";
  static public final String CANNOT_CONSTRAIN_THIS_ELEMENT = "[MOF C-50] Constraints, Tags, Imports, and Constants cannot be constrained";
  static public final String CONSTRAINTS_LIMITED_TO_CONTAINER = "[MOF C-51] A Constraint can only constrain ModelElements that are defined by or inherited by its immediate container";
  static public final String CONSTANTS_VALUE_MUST_MATCH_TYPE = "[MOF C-52] The type of a Constant and its value must be compatible";
  static public final String CONSTANTS_TYPE_MUST_BE_PRIMITIVE = "[MOF C-53] The type of a Constant must be a PrimitiveType";
  static public final String LOWER_CANNOT_BE_NEGATIVE_OR_UNBOUNDED = "[MOF C-54] The \"lower\" bound of a MultiplicityType cannot be negative or \"Unbounded\"";
  static public final String LOWER_CANNOT_EXCEED_UPPER = "[MOF C-55] The \"lower\" bound of a MultiplicityType cannot exceed the \"upper\" bound";
  static public final String UPPER_MUST_BE_POSITIVE = "[MOF C-56] The \"upper\" bound of a MultiplicityType cannot be less than 1";
  static public final String MUST_BE_UNORDERED_NONUNIQUE = "[MOF C-57] If a MultiplicityType specifies bounds of [0..1|1..1]), the \"is_ordered\" and \"is_unique\" values must be false";
  static public final String STRUCTURE_FIELD_CONTAINMENT_RULES = "[MOF C-58] A StructureField contains Constraints and Tags";
  static public final String MUST_HAVE_FIELDS = "[MOF C-59] A StructureType must contain at least one StructureField";
  
  // openMDX-specific constraints
  static public final String CANNOT_HAVE_MORE_THAN_ONE_QUALIFIER = "[openMDX C-1000] An AssociationEnd cannot have more than one qualifier attribute";
  static public final String NON_PRIMITIVE_QUALIFIER_MUST_HAVE_MULTIPLICITY_0_TO_N = "[openMDX C-1001] An AssociationEnd with a non primitive type qualifier (class) must have multiplicity [0..n]";
  static public final String PRIMITIVE_QUALIFIER_MUST_HAVE_MULTIPLICITY_0_OR_1_TO_1 = "[openMDX C-1002] An AssociationEnd with a primitive type qualifier (class) must have multiplicity [0..1|1..1]";
  static public final String OPERATION_ARGUMENTS_MUST_BE_PARAMETER = "[openMDX C-1003] The arguments of an operation must be parameters";
  static public final String PARAMETER_TYPE_MUST_BE_STRUCTURE_TYPE = "[openMDX C-1004] Parameters must be structure types";
  static public final String STEREOTYPE_STREAM_IMPLIES_PRIMITIVE_TYPE = "[openMDX C-1005] The stereotype <<stream>> is only allowed for primitive types";
  static public final String PRIMITIVE_TYPE_BINARY_RESTRICTS_MULTIPLICITY = "[openMDX C-1006] The primitive type org:w3c:binary requires multiplicities [0..1|1..1|<<stream>>]";
  static public final String CANNOT_BE_DERIVED_AND_CHANGEABLE = "[openMDX C-1007] An attribute that is derived cannot be changeable";
  static public final String INVALID_MULTIPLICITY = "[openMDX C-1008] Invalid multiplicity. Must be [0..1|1..1|0..n|list|set|sparsearray|stream]. WARNING: [0..n] is deprecated, use [list] instead";
  static public final String ASSOCIATION_END_WITH_COMPLEX_QUALIFIER_MUST_BE_FROZEN = "[openMDX C-1009] Invalid constraint. Association end with complex qualifier must be 'isFrozen'";
  static public final String ONE_ASSOCIATION_END_MUST_HAVE_AGGREGATION_NONE = "[openMDX C-1010] References not stored as attribute must have aggregation [composite|shared]. References stored as attributes must have aggregation [none] and [numeric] qualifier";
  static public final String AGGREGATION_NOT_EQUAL_NONE_REQUIRES_PRIMITIVE_TYPE_QUALIFIER_AND_SINGLE_MULTIPLICITY = "[openMDX C-1011] Association end with aggregation not equal [none] requires a primitive type qualifier and multiplicity [0..1|1..1]";
  static public final String AGGREGATION_NONE_REQUIRES_NO_OR_UNIQUE_PRIMITIVE_OR_NON_UNIQUE_CLASS_QUALIFIER = "[openMDX C-1013] Association end with aggregation [none] requires no qualifier or a qualifier [primitive with multiplicity 0..1|class with multiplicity 0..n]";
  static public final String QUALIFIER_REQUIRES_NAVIGABILITY = "[openMDX C-1014] Association end with qualifier must be navigable";
  static public final String END1_CLASS_QUALIFIER_REQUIRES_END2_NONE_OR_PRIMITIVE_QUALIFIER = "[openMDX C-1015] Association end1 with qualifier type class requires end2 with none or primitive qualifier";
  static public final String OPERATION_EXCEPTION_MUST_BE_EXCEPTION = "[openMDX C-1016] Operation declares an exception which is not an exception type";
  static public final String CIRCULAR_TYPE_DEPENCENCY_NOT_ALLOWED = "[openMDX C-1017] A type can not have itself as direct or indirect type";

  /**
   * Returns the smallest defined integer constant or
   * Integer.MAX_VALUE if no integer constant is defined.
   *
   * @return an int
   */
  static public int min()
  {
  return Integer.MAX_VALUE; // no constants defined
  }



  /**
   * Returns the biggest defined integer constant or
   * Integer.MIN_VALUE if no integer constant is defined.
   *
   * @return an int
   */
  static public int max()
  {
  return Integer.MIN_VALUE; // no constants defined
  }



  /**
   * Returns a string representation of the passed code
   *
   * @param code  a code to be stringified
   * @return a stringified code
   */
  static public String toString(int code)
  {
      // no integer constants defined
      return String.valueOf(code);
  }



  /**
   * Returns the code of the passed code's string representation.
   * The string representation is case insensitive.
   *
   * @exception  throws an <code>IllegalArgumentException</code> 
   *             if the stringified code cannot be resolved
   * @param code a stringified code
   * @return a code
   */
  static public int fromString(String code)
  {  

    // Not found
    throw new IllegalArgumentException(
          "The code '" + code + "' is unkown to the class ModelConstraints");
  }



}

// end-of-file
