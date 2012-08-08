/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestIdentifier.java,v 1.1 2009/03/05 15:46:32 hburger Exp $
 * Description: Test "ear" Protocol Handler
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/05 15:46:32 $
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
package test.omg.mof.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.omg.mof.spi.Identifier;

/**
 * Test Identifier
 */
public class TestIdentifier {

    static List<String> STRUCTURAL_FEATURE = Arrays.asList(
    	"structural",
    	"feature"
    );

    static List<String> FEATURE = Arrays.asList(
    	"feature"
    );

    static List<String> W3C = Arrays.asList(
    	"org",
    	"w3c"
    );
    
    @Test
    public void testSplit(
    ){
    	//
    	// structural feature
    	//
    	split("StructuralFeature", STRUCTURAL_FEATURE);
    	split("structuralFeature", STRUCTURAL_FEATURE);
    	split(
    	    "structuralFEATURE", 
    	    Identifier.STRICTLY_JMI_1_COMPLIANT ? STRUCTURAL_FEATURE : Arrays.asList(
    	        "structural","f","e","a","t","u","r","e"
    	    )
    	);
    	split(
    	    "Structural_Feature", 
            Identifier.STRICTLY_JMI_1_COMPLIANT ? STRUCTURAL_FEATURE : Arrays.asList(
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
            Identifier.STRICTLY_JMI_1_COMPLIANT ? STRUCTURAL_FEATURE : Arrays.asList(
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
            Identifier.STRICTLY_JMI_1_COMPLIANT ? FEATURE : Arrays.asList(
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
            Identifier.STRICTLY_JMI_1_COMPLIANT ? FEATURE : Arrays.asList(
                "f","e","a","t","u","r","e"
            )
        );
    	//
    	// w3c
    	//
    	split("org w3c", W3C);
    	split(
    	    "ORG W3C", 
    	    Identifier.STRICTLY_JMI_1_COMPLIANT ? W3C: Arrays.asList(
                "o","r","g","w3","c"
            )
    	);
    	split(
    	    "orgW3C", 
            Identifier.STRICTLY_JMI_1_COMPLIANT ? W3C: Arrays.asList(
                "org","w3","c"
            )
        );
    	split("OrgW3c", W3C);
    	split("org-w3c", W3C);
    }

    private void split(
    	String modelElementName,
    	List<String> words
    ){
    	assertEquals(
    		modelElementName, 
    		words,
    		canonical(Identifier.toWords(modelElementName))
    	);
    	assertTrue(modelElementName, Identifier.isIdentifier(modelElementName));
    }
    
    @Test
    public void testStandardIdentifiers(
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
    	    Identifier.STRICTLY_JMI_1_COMPLIANT ? "name123" : "name_1_2_3", 
	        Identifier.ATTRIBUTE_NAME, 
	        null, 
	        null, 
	        null
	    );
    	compose(
    	    "Name_1_2_3", 
    	    Identifier.STRICTLY_JMI_1_COMPLIANT ? "Name123Class" : "Name_1_2_3Class", 
    	    Identifier.CLASS_PROXY_NAME, 
    	    null, 
    	    null, 
    	    "class"
    	);
    	compose("Name_1_2_3", "NAME_1_2_3", Identifier.CONSTANT, null, null, null);
    	compose("Name_1_2_3", "NAME_1_2_3", Identifier.ENUMERATION_LITERAL, null, null, null);
    	compose(
    	    "Name_1_2_3", 
    	    Identifier.STRICTLY_JMI_1_COMPLIANT ? "name123" : "name_1_2_3", 
    	    Identifier.OPERATION_NAME, 
    	    null, 
    	    null, 
    	    null
    	);
    	compose(
    	    "Name_1_2_3", 
    	    Identifier.STRICTLY_JMI_1_COMPLIANT ? "name123" : "name_1_2_3", 
    	    Identifier.PACKAGE_NAME, 
    	    null, 
    	    null, 
    	    null
    	);
    }

    @Test
    public void testEscapedIdentifiers(
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
    
    private void compose(
    	String modelElementName,
    	String identifier,
        Identifier type, 
        String removablePrefix, 
        String prependablePrefix, 
        String appendableSuffix
    ){
    	assertEquals(
    		type.toString(), 
    		identifier,
    		type.toIdentifier(
				modelElementName, 
				removablePrefix, 
				prependablePrefix, 
				null, // removableSuffix
				appendableSuffix
    		)
    	);
    }

    
    private List<String> canonical(
    	List<String> source	
    ){
    	List<String> target = new ArrayList<String>();
    	for(String value : source) {
    		target.add(value.toLowerCase(Locale.US));
    	}
    	return target;
    }

}
