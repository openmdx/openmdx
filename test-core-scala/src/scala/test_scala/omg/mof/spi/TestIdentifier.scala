/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestIdentifier.scala,v 1.1 2010/11/26 14:05:38 wfro Exp $
 * Description: Test "ear" Protocol Handler
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/11/26 14:05:38 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package test_scala.omg.mof.spi;

import java.util.{List, Arrays}
import org.junit.Test
import org.omg.mof.spi.Identifier;
import org.junit._
import Assert._

/**
 * Test Identifier
 */
class TestIdentifier {

    val STRUCTURAL_FEATURE: List[String] = Arrays.asList(
    	"structural",
    	"feature"
    );

    val FEATURE: List[String]  = Arrays.asList(
    	"feature"
    );

    val W3C: List[String]  = Arrays.asList(
    	"org",
    	"w3c"
    );
    
    @Test
    def testSplit(
    ){
    	//
    	// structural feature
    	//
    	split("StructuralFeature", STRUCTURAL_FEATURE);
    	split("structuralFeature", STRUCTURAL_FEATURE);
    	split(
    	    "structuralFEATURE", 
    	    if(Identifier.STRICTLY_JMI_1_COMPLIANT) STRUCTURAL_FEATURE else Arrays.asList(
    	        "structural","f","e","a","t","u","r","e"
    	    )
    	);
    	split(
    	    "Structural_Feature", 
            if(Identifier.STRICTLY_JMI_1_COMPLIANT) STRUCTURAL_FEATURE else Arrays.asList(
                "structural_","feature"
            )
        );
    	split("Structural Feature", STRUCTURAL_FEATURE);
    	split("Structural - Feature", STRUCTURAL_FEATURE);
    	split(" Structural\tFeature\n", STRUCTURAL_FEATURE);
    	split("structural feature", STRUCTURAL_FEATURE);
    	split("structural feature", STRUCTURAL_FEATURE);
    	split(
    	    "STRUCTURAL\rFEATURE", 
            if(Identifier.STRICTLY_JMI_1_COMPLIANT) STRUCTURAL_FEATURE else Arrays.asList(
                "s","t","r","u","c","t","u","r","a","l","f","e","a","t","u","r","e"
            )
        );
    	//
    	// feature
    	//
    	split("Feature", FEATURE);
    	split("Feature", FEATURE);
    	split(
    	    "FEATURE", 
            if(Identifier.STRICTLY_JMI_1_COMPLIANT) FEATURE else Arrays.asList(
                "f","e","a","t","u","r","e"
            )
        );
    	split("Feature", FEATURE);
    	split(" Feature", FEATURE);
    	split(" - Feature", FEATURE);
    	split(" \tFeature\n", FEATURE);
    	split(" feature", FEATURE);
    	split(" feature", FEATURE);
    	split(
    	    "\rFEATURE", 
            if(Identifier.STRICTLY_JMI_1_COMPLIANT) FEATURE else Arrays.asList(
                "f","e","a","t","u","r","e"
            )
        );
    	//
    	// w3c
    	//
    	split("org w3c", W3C);
    	split(
    	    "ORG W3C", 
    	    if(Identifier.STRICTLY_JMI_1_COMPLIANT) W3C else Arrays.asList(
                "o","r","g","w3","c"
            )
    	);
    	split(
    	    "orgW3C", 
            if(Identifier.STRICTLY_JMI_1_COMPLIANT) W3C else Arrays.asList(
                "org","w3","c"
            )
        );
    	split("OrgW3c", W3C);
    	split("org-w3c", W3C);
    }

    def split(
    	modelElementName: String,
    	words: List[String] 
    ){
    	assertEquals(
    		modelElementName, 
    		words,
    		canonical(Identifier.toWords(modelElementName))
    	);
    	assertTrue(modelElementName, Identifier.isIdentifier(modelElementName));
    }
    
    @Test
    def testStandardIdentifiers(
    ){
    	//
    	// structural feature
    	//
    	compose("structural-feature", "structuralFeature", Identifier.ATTRIBUTE_NAME, null, null, null);
    	compose("structural-feature", "StructuralFeatureClass", Identifier.CLASS_PROXY_NAME, null, null, "class");
    	compose("structural-feature", "STRUCTURAL_FEATURE", Identifier.CONSTANT, null, null, null);
    	compose("structural-feature", "STRUCTURAL_FEATURE", Identifier.ENUMERATION_LITERAL, null, null, null);
    	compose("structural-feature", "getStructuralFeature", Identifier.OPERATION_NAME, null, "get", null);
    	compose("structural-feature", "structuralfeature", Identifier.PACKAGE_NAME, null, null, null);
    	//
    	// feature
    	//
    	compose("feature", "feature", Identifier.ATTRIBUTE_NAME, null, null, null);
    	compose("feature", "FeatureClass", Identifier.CLASS_PROXY_NAME, null, null, "class");
    	compose("feature", "FEATURE", Identifier.CONSTANT, null, null, null);
    	compose("feature", "FEATURE", Identifier.ENUMERATION_LITERAL, null, null, null);
    	compose("feature", "feature", Identifier.OPERATION_NAME, null, null, null);
    	compose("feature", "feature", Identifier.PACKAGE_NAME, null, null, null);
    	//
    	// is abstract
    	//
    	compose("is-abstract", "isAbstract", Identifier.ATTRIBUTE_NAME, null, null, null);
    	compose("is-abstract", "IsAbstractClass", Identifier.CLASS_PROXY_NAME, null, null, "class");
    	compose("is-abstract", "IS_ABSTRACT", Identifier.CONSTANT, null, null, null);
    	compose("is-abstract", "IS_ABSTRACT", Identifier.ENUMERATION_LITERAL, null, null, null);
    	compose("is-abstract", "setAbstract", Identifier.OPERATION_NAME, "is", "set", null);
    	compose("is-abstract", "isabstract", Identifier.PACKAGE_NAME, null, null, null);
    	//
    	// abstract
    	//
    	compose("abstract", "abstract_", Identifier.ATTRIBUTE_NAME, null, null, null);
    	compose("abstract", "AbstractClass", Identifier.CLASS_PROXY_NAME, null, null, "class");
    	compose("abstract", "ABSTRACT", Identifier.CONSTANT, null, null, null);
    	compose("abstract", "ABSTRACT", Identifier.ENUMERATION_LITERAL, null, null, null);
    	compose("abstract", "setAbstract", Identifier.OPERATION_NAME, "is", "set", null);
    	compose("abstract", "abstract_", Identifier.PACKAGE_NAME, null, null, null);
    	//
    	// is final
    	//
    	compose("is-final", "isFinal", Identifier.ATTRIBUTE_NAME, null, null, null);
    	compose("is-final", "IsFinalClass", Identifier.CLASS_PROXY_NAME, null, null, "class");
    	compose("is-final", "IS_FINAL", Identifier.CONSTANT, null, null, null);
    	compose("is-final", "IS_FINAL", Identifier.ENUMERATION_LITERAL, null, null, null);
    	compose("is-final", "isFinal", Identifier.OPERATION_NAME, "is", "is", null);
    	compose("is-final", "isfinal", Identifier.PACKAGE_NAME, null, null, null);
    	//
    	// type 1.0.1
    	//
    	compose(
    	    "Name_1_2_3", 
    	    if (Identifier.STRICTLY_JMI_1_COMPLIANT) "name123" else "name_1_2_3", 
	        Identifier.ATTRIBUTE_NAME, 
	        null, 
	        null, 
	        null
	    );
    	compose(
    	    "Name_1_2_3", 
    	    if (Identifier.STRICTLY_JMI_1_COMPLIANT) "Name123Class" else "Name_1_2_3Class", 
    	    Identifier.CLASS_PROXY_NAME, 
    	    null, 
    	    null, 
    	    "class"
    	);
    	compose("Name_1_2_3", "NAME_1_2_3", Identifier.CONSTANT, null, null, null);
    	compose("Name_1_2_3", "NAME_1_2_3", Identifier.ENUMERATION_LITERAL, null, null, null);
    	compose(
    	    "Name_1_2_3", 
    	    if (Identifier.STRICTLY_JMI_1_COMPLIANT) "name123" else "name_1_2_3", 
    	    Identifier.OPERATION_NAME, 
    	    null, 
    	    null, 
    	    null
    	);
    	compose(
    	    "Name_1_2_3", 
    	    if (Identifier.STRICTLY_JMI_1_COMPLIANT) "name123" else "name_1_2_3", 
    	    Identifier.PACKAGE_NAME, 
    	    null, 
    	    null, 
    	    null
    	);
    }

    @Test
    def testEscapedIdentifiers(
    ){
    	//
    	// toString()
    	//
    	compose("to-string", "toString", Identifier.ATTRIBUTE_NAME, null, null, null);
    	compose("to-string", "ToStringClass", Identifier.CLASS_PROXY_NAME, null, null, "class");
    	compose("to-string", "TO_STRING", Identifier.CONSTANT, null, null, null);
    	compose("to-string", "TO_STRING", Identifier.ENUMERATION_LITERAL, null, null, null);
    	compose("to-string", "toString_", Identifier.OPERATION_NAME, null, null, null);
    	compose("to-string", "tostring", Identifier.PACKAGE_NAME, null, null, null);
    	//
    	// instanceOf
    	//
    	compose("instance-of", "instanceOf", Identifier.ATTRIBUTE_NAME, null, null, null);
    	compose("instance-of", "InstanceOfClass", Identifier.CLASS_PROXY_NAME, null, null, "class");
    	compose("instance-of", "INSTANCE_OF", Identifier.CONSTANT, null, null, null);
    	compose("instance-of", "INSTANCE_OF", Identifier.ENUMERATION_LITERAL, null, null, null);
    	compose("instance-of", "isInstanceOf", Identifier.OPERATION_NAME, "is", "is", null);
    	compose("instance-of", "instanceof_", Identifier.PACKAGE_NAME, null, null, null);
    	//
    	// instanceof
    	//
    	compose("instanceof", "instanceof_", Identifier.ATTRIBUTE_NAME, null, null, null);
    	compose("instanceof", "InstanceofClass", Identifier.CLASS_PROXY_NAME, null, null, "class");
    	compose("instanceof", "INSTANCEOF", Identifier.CONSTANT, null, null, null);
    	compose("instanceof", "INSTANCEOF", Identifier.ENUMERATION_LITERAL, null, null, null);
    	compose("instanceof", "isInstanceof", Identifier.OPERATION_NAME, "is", "is", null);
    	compose("instanceof", "instanceof_", Identifier.PACKAGE_NAME, null, null, null);
    	//
    	// class
    	//
    	compose("class", "class_", Identifier.ATTRIBUTE_NAME, null, null, null);
    	compose("class", "ClassClass", Identifier.CLASS_PROXY_NAME, null, null, "class"	);
    	compose("class", "CLASS", Identifier.CONSTANT, null, null, null);
    	compose("class", "CLASS", Identifier.ENUMERATION_LITERAL, null, null, null);
    	compose("class", "getClass_", Identifier.OPERATION_NAME, null, "get", null);
    	compose("class", "class_", Identifier.PACKAGE_NAME, null, null, null);
    	//
    	// final
    	//
    	compose("final", "final_", Identifier.ATTRIBUTE_NAME, null, null, null);
    	compose("final", "FinalClass", Identifier.CLASS_PROXY_NAME, null, null, "class");
    	compose("final", "FINAL", Identifier.CONSTANT, null, null, null);
    	compose("final", "FINAL", Identifier.ENUMERATION_LITERAL, null, null, null);
    	compose("final", "isFinal", Identifier.OPERATION_NAME, "is", "is", null);
    	compose("final", "final_", Identifier.PACKAGE_NAME, null, null, null);
    }
    
    def compose(
    	modelElementName: String,
    	identifier: String,
        `type`: Identifier, 
        removablePrefix: String, 
        prependablePrefix: String, 
        appendableSuffix: String
    ){
    	assertEquals(
    		`type`.toString(), 
    		identifier,
    		`type`.toIdentifier(
				modelElementName, 
				removablePrefix, 
				prependablePrefix, 
				null, // removableSuffix
				appendableSuffix
    		)
    	);
    }

    def canonical(source: List[String]): List[String] = {
    	var target: java.util.List[String] = new java.util.ArrayList[String]()
    	val i: java.util.Iterator[String] = source.iterator()
    	while(i.hasNext()) {
    		val value = i.next()
    		target.add(value.toLowerCase(java.util.Locale.US));
    	}
    	return target;
    }

}
