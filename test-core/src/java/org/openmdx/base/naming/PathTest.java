/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Path Test 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2014, OMEX AG, Switzerland
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
package org.openmdx.base.naming;

import java.net.URI;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Path Test
 */
public class PathTest {

    private Expectation expectation;
    
    @Before
    public void setUp(){
        expectation = new Expectation();
    }
    
    @Test
    public void when0ComponentsThenConstructRootPath(){
        expectation.components = new String[]{};
        expectation.xri = "xri://@openmdx";
        expectation.iri = URI.create("xri://@openmdx");
        expectation.uri = "@openmdx";
        expectation.classicRepresentation = "";
        expectation.legacyUri = "spice:/";
        expectation.legacyXri = "xri:@openmdx";
        arrangeActAndAssertAccordingToExpectation();
    }
    
    @Test
    public void when1ComponentThenConstructAuthorityPath(){
        expectation.components = new String[]{"A"};
        expectation.xri = "xri://@openmdx*A";
        expectation.iri = URI.create("xri://@openmdx*A");
        expectation.uri = "@openmdx*A";
        expectation.classicRepresentation = "A";
        expectation.legacyUri = "spice://A";
        expectation.legacyXri = "xri:@openmdx:A";
        expectation.objectPath = true;
        arrangeActAndAssertAccordingToExpectation();
        
    }

    @Test
    public void when1ComponentWithAuthorityWildcardThenConstructAuthorityPattern(){
        expectation.components = new String[]{":*"};
        expectation.xri = "xri://@openmdx*($..)";
        expectation.iri = URI.create("xri://@openmdx*($..)");
        expectation.uri = "@openmdx*($..)";
        expectation.classicRepresentation = "::*";
        expectation.legacyUri = "spice://:*";
        expectation.legacyXri = "xri:@openmdx:**";
        expectation.objectPath = true;
        expectation.containsWildcarcd = true;
        arrangeActAndAssertAccordingToExpectation();
    }
    
    @Test
    public void whenUUIDThenConstructTransactionalPath(){
        // Arrange
        expectation.components = new String[]{"!($t*uuid*15b1e210-f8c2-11e3-aa50-0133ea275f6a)"};
        expectation.xri = "xri://@openmdx" + ("!($t*uuid*15b1e210-f8c2-11e3-aa50-0133ea275f6a)");
        expectation.iri = URI.create("xri://@openmdx!($t*uuid*15b1e210-f8c2-11e3-aa50-0133ea275f6a)");
        expectation.uri = "@openmdx" + ("!($t*uuid*15b1e210-f8c2-11e3-aa50-0133ea275f6a)");
        expectation.classicRepresentation = "!($t*uuid*15b1e210-f8c2-11e3-aa50-0133ea275f6a)";
        expectation.legacyUri = "spice://" + ("!($t*uuid*15b1e210-f8c2-11e3-aa50-0133ea275f6a)");
        expectation.legacyXri = "xri:@openmdx:" + ("!($t*uuid*15b1e210-f8c2-11e3-aa50-0133ea275f6a)");
        expectation.objectPath = true;
        expectation.transientObjectId = true;
        expectation.uuid = UUID.fromString("15b1e210-f8c2-11e3-aa50-0133ea275f6a");
        arrangeActAndAssertAccordingToExpectation();
    }

    /**
     * The legacy XRI transformation is not reversible
     */
    @Test
    public void whenUUIDWithWildcardThenConstructTransactionalPattern(){
        expectation.components = new String[]{"!($t*uuid*($.))"};
        expectation.xri = "xri://@openmdx!($t*uuid*($.))";
        expectation.iri = URI.create("xri://@openmdx!($t*uuid*($.))");
        expectation.uri = "@openmdx!($t*uuid*($.))";
        expectation.classicRepresentation = "!($t*uuid*($.))";
        expectation.legacyUri = "spice://!($t*uuid*($.))";
        // expectation.legacyXri = "xri:@openmdx:!($t*uuid*($.))";
        expectation.objectPath = true;
        expectation.transientObjectId = false; // TODO Should it be true in future?
        expectation.containsWildcarcd = true;
        arrangeActAndAssertAccordingToExpectation();
    }

    
    @Test
    public void when2ComponentsThenConstructProviderContainerPath(){
        expectation.components = new String[]{"A", "provider"};
        expectation.xri = "xri://@openmdx*A/provider";
        expectation.iri = URI.create("xri://@openmdx*A/provider");
        expectation.uri = "@openmdx*A/provider";
        expectation.classicRepresentation = "A/provider";
        expectation.legacyUri = "spice://A/provider";
        expectation.legacyXri = "xri:@openmdx:A/provider";
        arrangeActAndAssertAccordingToExpectation();
    }
    
    @Test
    public void when3ComponentsThenConstructProviderPattern(){
        expectation.components = new String[]{"A", "provider", ":*"};
        expectation.xri = "xri://@openmdx*A/provider/($..)";
        expectation.iri = URI.create("xri://@openmdx*A/provider/($..)");
        expectation.uri = "@openmdx*A/provider/($..)";
        expectation.classicRepresentation = "A/provider/::*";
        expectation.legacyUri = "spice://A/provider/:*";
        expectation.legacyXri = "xri:@openmdx:A/provider/**";
        expectation.objectPath = true;
        expectation.containsWildcarcd = true;
        arrangeActAndAssertAccordingToExpectation();
    }

    @Test
    public void when4ComponentsWithProviderWildcardThenConstructSegmentContainerPattern(){
        expectation.components = new String[]{"A", "provider", ":*", "segment"};
        expectation.xri = "xri://@openmdx*A/provider/($..)/segment";
        expectation.iri = URI.create("xri://@openmdx*A/provider/($..)/segment");
        expectation.uri = "@openmdx*A/provider/($..)/segment";
        expectation.classicRepresentation = "A/provider/::*/segment";
        expectation.legacyUri = "spice://A/provider/:*/segment";
        expectation.legacyXri = "xri:@openmdx:A/provider/**/segment";
        expectation.containsWildcarcd = true;
        arrangeActAndAssertAccordingToExpectation();
    }

    @Test
    public void when2SegmentsAndPercentThenConstrcutAnyProviderAndDescendantsPattern(){
        expectation.components = new String[]{"A", "provider", "%"};
        expectation.xri = "xri://@openmdx*A/provider/($...)";
        expectation.iri = URI.create("xri://@openmdx*A/provider/($...)");
        expectation.uri = "@openmdx*A/provider/($...)";
        expectation.classicRepresentation = "A/provider/%";
        expectation.legacyUri = "spice://A/provider/%";
        expectation.legacyXri = "xri:@openmdx:A/provider/***";
        expectation.containsWildcarcd = true;
        expectation.objectPath = true;
        arrangeActAndAssertAccordingToExpectation();
    }
    
    @Test
    public void when3SegmentsAndProviderWildcardThenConstrcutSomeProviderPattern(){
        expectation.components = new String[]{"A", "provider", ":p*"};
        expectation.xri = "xri://@openmdx*A/provider/($.*p)*($..)";
        expectation.iri = URI.create(expectation.xri);
        expectation.uri = "@openmdx*A/provider/($.*p)*($..)";
        expectation.classicRepresentation = "A/provider/::p*";
        expectation.legacyUri = "spice://A/provider/:p*";
        expectation.legacyXri = "xri:@openmdx:A/provider/p**";
        expectation.containsWildcarcd = true;
        expectation.objectPath = true;
        arrangeActAndAssertAccordingToExpectation();
    }
    
    @Test
    public void when2ComponentWithAnyContainerWildcardThenConstructProviderContainerPattern(){
        expectation.components = new String[]{"A:B",":*"};
        expectation.xri = "xri://@openmdx*A.B/($..)";
        expectation.iri = URI.create("xri://@openmdx*A.B/($..)");
        expectation.uri = "@openmdx*A.B/($..)";
        expectation.classicRepresentation = "A::B/::*";
        expectation.legacyUri = "spice://A:B/:*";
        expectation.legacyXri = "xri:@openmdx:A.B/**";
        expectation.containsWildcarcd = true;
        arrangeActAndAssertAccordingToExpectation();
    }
    
    /**
     * IRI transformation is not reversible
     */
    @Test
    public void whenLostAndFoundThenBuildPathWithCaseExactString(){
        expectation.components = new String[]{"org:openmdx:preferences1","provider", "Java:Properties", "segment", "System", "preferences", "lost+found"};
        expectation.xri = "xri://@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/System/preferences/($t*ces*lost%2Bfound)";
        expectation.iri = URI.create("xri://@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/System/preferences/($t*ces*lost%252Bfound)");
        expectation.uri = "@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/System/preferences/($t*ces*lost%252Bfound)";
        expectation.classicRepresentation = "org::openmdx::preferences1/provider/Java::Properties/segment/System/preferences/lost+found";
        expectation.legacyUri = "spice://org:openmdx:preferences1/provider/Java:Properties/segment/System/preferences/lost+found";
        expectation.legacyXri = "xri:@openmdx:org.openmdx.preferences1/provider/Java:Properties/segment/System/preferences/lost+found";
        expectation.objectPath = true;
        arrangeActAndAssertAccordingToExpectation();
    }
    
    @Test
    public void whenJavaComponentEnvironmentThenBuildPathWithCrossReference(){
        expectation.components = new String[]{"org:openmdx:preferences1","provider", "Java:Properties", "segment", "(java:comp/env)"};
        expectation.xri = "xri://@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/(java:comp/env)";
        expectation.iri = URI.create("xri://@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/(java:comp%2Fenv)");
        expectation.uri = "@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/(java:comp%2Fenv)";
        expectation.classicRepresentation = "org::openmdx::preferences1/provider/Java::Properties/segment/(java::comp//env)";
        expectation.legacyUri = "spice://org:openmdx:preferences1/provider/Java:Properties/segment/(java:comp%2fenv)";
        expectation.legacyXri = "xri:@openmdx:org.openmdx.preferences1/provider/Java:Properties/segment/(java:comp/env)";
        expectation.objectPath = true;
        arrangeActAndAssertAccordingToExpectation();
    }
    
    @Test
    public void whenJavaComponentThenBuildPathWithCrossReference(){
        expectation.components = new String[]{"org:openmdx:preferences1","provider", "Java:Properties", "segment", "(java:comp)"};
        expectation.xri = "xri://@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/(java:comp)";
        expectation.iri = URI.create("xri://@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/(java:comp)");
        expectation.uri = "@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/(java:comp)";
        expectation.classicRepresentation = "org::openmdx::preferences1/provider/Java::Properties/segment/(java::comp)";
        expectation.legacyUri = "spice://org:openmdx:preferences1/provider/Java:Properties/segment/(java:comp)";
        expectation.legacyXri = "xri:@openmdx:org.openmdx.preferences1/provider/Java:Properties/segment/(java:comp)";
        expectation.objectPath = true;
        arrangeActAndAssertAccordingToExpectation();
    }

    @Test
    public void whenJavaSystemPropertiesThenBuildSegmentPath(){
        expectation.components = new String[]{"org:openmdx:preferences1","provider", "Java:Properties", "segment", "System"};
        expectation.xri = "xri://@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/System";
        expectation.iri = URI.create("xri://@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/System");
        expectation.uri = "@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/System";
        expectation.classicRepresentation = "org::openmdx::preferences1/provider/Java::Properties/segment/System";
        expectation.legacyUri = "spice://org:openmdx:preferences1/provider/Java:Properties/segment/System";
        expectation.legacyXri = "xri:@openmdx:org.openmdx.preferences1/provider/Java:Properties/segment/System";
        expectation.objectPath = true;
        arrangeActAndAssertAccordingToExpectation();
    }
    
    @Test
    public void whenResourcePreferencesThenBuildPathWithCrossReference(){
        expectation.components = new String[]{"org:openmdx:preferences1","provider", "Java:Properties", "segment", "(+resource/application-preferences.xml)"};
        expectation.xri = "xri://@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/(+resource/application-preferences.xml)";
        expectation.iri = URI.create("xri://@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/(+resource%2Fapplication-preferences.xml)");
        expectation.uri = "@openmdx*org.openmdx.preferences1/provider/Java:Properties/segment/(+resource%2Fapplication-preferences.xml)";
        expectation.classicRepresentation = "org::openmdx::preferences1/provider/Java::Properties/segment/(+resource//application-preferences.xml)";
        expectation.legacyUri = "spice://org:openmdx:preferences1/provider/Java:Properties/segment/(+resource%2fapplication-preferences.xml)";
        expectation.legacyXri = "xri:@openmdx:org.openmdx.preferences1/provider/Java:Properties/segment/(+resource/application-preferences.xml)";
        expectation.objectPath = true;
        arrangeActAndAssertAccordingToExpectation();
        assertEquals("CR20006216", "(+resource/application-preferences.xml)", new Path(expectation.xri).getComponent(4).get(0));
    }
    
    @Test
    public void whenCedillaThenBuildPathWithUnicodeEscape(){
        expectation.components = new String[]{"A:B","Fran\u00e7ois"};
        expectation.xri = "xri://@openmdx*A.B/Fran\u00e7ois";
        expectation.iri = URI.create("xri://@openmdx*A.B/Fran\u00e7ois");
        expectation.uri = "@openmdx*A.B/Fran%C3%A7ois";
        expectation.classicRepresentation = "A::B/Fran\u00e7ois";
        expectation.legacyUri = "spice://A:B/Fran%c3%a7ois";
        expectation.legacyXri = "xri:@openmdx:A.B/Fran\u00e7ois";
        arrangeActAndAssertAccordingToExpectation();
    }
    
    @Test
    public void whenSemicolonAndEqualSignThenBuildPathWithCaseExactString(){
        expectation.components = new String[]{"A:B","provider","P:Q","segment","S.T","object","RR_1;state=0"};
        expectation.xri = "xri://@openmdx*A.B/provider/P:Q/segment/S.T/object/($t*ces*RR_1%3Bstate%3D0)";
        expectation.iri = URI.create("xri://@openmdx*A.B/provider/P:Q/segment/S.T/object/($t*ces*RR_1%253Bstate%253D0)");
        expectation.uri = "@openmdx*A.B/provider/P:Q/segment/S.T/object/($t*ces*RR_1%253Bstate%253D0)";
        expectation.classicRepresentation = "A::B/provider/P::Q/segment/S.T/object/RR_1;state=0";
        expectation.legacyUri = "spice://A:B/provider/P:Q/segment/S.T/object/RR_1;state=0";
        expectation.legacyXri = "xri:@openmdx:A.B/provider/P:Q/segment/S.T/object/RR_1;state=0";
        arrangeActAndAssertAccordingToExpectation();
    }
    
    @Test
    public void whenSemicolonThenBuildObjectPath(){
        expectation.components = new String[]{"A:B","provider","P:Q","segment","S.T","object","012345;transient"};
        expectation.xri = "xri://@openmdx*A.B/provider/P:Q/segment/S.T/object/012345;transient";
        expectation.iri = URI.create("xri://@openmdx*A.B/provider/P:Q/segment/S.T/object/012345;transient");
        expectation.uri = "@openmdx*A.B/provider/P:Q/segment/S.T/object/012345;transient";
        expectation.classicRepresentation = "A::B/provider/P::Q/segment/S.T/object/012345;transient";
        expectation.legacyUri = "spice://A:B/provider/P:Q/segment/S.T/object/012345;transient";
        expectation.legacyXri = "xri:@openmdx:A.B/provider/P:Q/segment/S.T/object/012345;transient";
        arrangeActAndAssertAccordingToExpectation();
    }
    
    @Test
    public void whenSegmentNameIsItselfAPathThenBuildPathWithCrossReference(){
        expectation.components = new String[]{"A0:A1","provider","A","segment","B0::B1/provider/B"};
        expectation.xri = "xri://@openmdx*A0.A1/provider/A/segment/(@openmdx*B0.B1/provider/B)";
        expectation.iri = URI.create("xri://@openmdx*A0.A1/provider/A/segment/(@openmdx*B0.B1%2Fprovider%2FB)");
        expectation.uri = "@openmdx*A0.A1/provider/A/segment/(@openmdx*B0.B1%2Fprovider%2FB)";
        expectation.classicRepresentation = "A0::A1/provider/A/segment/B0::::B1//provider//B";
        expectation.legacyUri = "spice://A0:A1/provider/A/segment/B0%3a%3aB1%2fprovider%2fB";
        expectation.legacyXri = "xri:@openmdx:A0.A1/provider/A/segment/(@openmdx:B0.B1/provider/B)";
        arrangeActAndAssertAccordingToExpectation();
    }

    @Test
    public void whenProviderNameIsAPathWithPercentSignThenBuildPathWithCaseExactString(){
        expectation.components = new String[]{"A:B","provider","B/B0%3AB1","segment"};
        expectation.xri = "xri://@openmdx*A.B/provider/(@openmdx*B/($t*ces*B0%253AB1))/segment";
        expectation.iri = URI.create("xri://@openmdx*A.B/provider/(@openmdx*B%2F($t*ces*B0%25253AB1))/segment");
        expectation.uri = "@openmdx*A.B/provider/(@openmdx*B%2F($t*ces*B0%25253AB1))/segment";
        expectation.classicRepresentation = "A::B/provider/B//B0%3AB1/segment";
        expectation.legacyUri = "spice://A:B/provider/B%2fB0%253AB1/segment";
        expectation.legacyXri = "xri:@openmdx:A.B/provider/(@openmdx:B/B0%253AB1)/segment";
        arrangeActAndAssertAccordingToExpectation();
    }
    
    @Test
    public void whenCurlyBracketsAndDollarThenBuildPathWithCaseExactString(){
        expectation.components = new String[]{"A:B","provider","${PROVIDER}","segment","${SEGMENT}"};
        expectation.xri = "xri://@openmdx*A.B/provider/($t*ces*%24%7BPROVIDER%7D)/segment/($t*ces*%24%7BSEGMENT%7D)";
        expectation.iri = URI.create("xri://@openmdx*A.B/provider/($t*ces*%2524%257BPROVIDER%257D)/segment/($t*ces*%2524%257BSEGMENT%257D)");
        expectation.uri = "@openmdx*A.B/provider/($t*ces*%2524%257BPROVIDER%257D)/segment/($t*ces*%2524%257BSEGMENT%257D)";
        expectation.classicRepresentation = "A::B/provider/${PROVIDER}/segment/${SEGMENT}";
        expectation.legacyUri = "spice://A:B/provider/$%7bPROVIDER%7d/segment/$%7bSEGMENT%7d";
        expectation.legacyXri = "xri:@openmdx:A.B/provider/$%7BPROVIDER%7D/segment/$%7BSEGMENT%7D";
        arrangeActAndAssertAccordingToExpectation();
    }
    
    @Test
    public void whenALotOfSpecialCharactersThenBuildPathWithCaseExactString(){
        expectation.components = new String[]{"A:B"," !\"$%00&'*+,-.0123456789:;<=>@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"};
        expectation.xri = "xri://@openmdx*A.B/($t*ces*%20%21%22%24%2500%26%27%2A%2B%2C-.0123456789%3A%3B%3C%3D%3E%40ABCDEFGHIJKLMNOPQRSTUVWXYZ%5B%5C%5D%5E_%60abcdefghijklmnopqrstuvwxyz%7B%7C%7D~)";
        expectation.iri = URI.create("xri://@openmdx*A.B/($t*ces*%2520%2521%2522%2524%252500%2526%2527%252A%252B%252C-.0123456789%253A%253B%253C%253D%253E%2540ABCDEFGHIJKLMNOPQRSTUVWXYZ%255B%255C%255D%255E_%2560abcdefghijklmnopqrstuvwxyz%257B%257C%257D~)");
        expectation.uri = "@openmdx*A.B/($t*ces*%2520%2521%2522%2524%252500%2526%2527%252A%252B%252C-.0123456789%253A%253B%253C%253D%253E%2540ABCDEFGHIJKLMNOPQRSTUVWXYZ%255B%255C%255D%255E_%2560abcdefghijklmnopqrstuvwxyz%257B%257C%257D~)";
        expectation.classicRepresentation = "A::B/ !\"$%00&'*+,-.0123456789::;<=>@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
        expectation.legacyUri = "spice://A:B/%20!%22$%2500&'*+,-.0123456789:;%3c=%3e@ABCDEFGHIJKLMNOPQRSTUVWXYZ%5b%5c%5d%5e_%60abcdefghijklmnopqrstuvwxyz%7b%7c%7d~";
        expectation.legacyXri = "xri:@openmdx:A.B/%20!%22$%2500&'*+,-.0123456789:;%3C=%3E@ABCDEFGHIJKLMNOPQRSTUVWXYZ%5B%5C%5D%5E_%60abcdefghijklmnopqrstuvwxyz%7B%7C%7D~";
        arrangeActAndAssertAccordingToExpectation();
    }
    
    @Test
    public void whenAnyAuthorityAnyProviderAnySegmentAndAllItsDescendantsThenBuildSegmentAndDescendantsPattern(){
        expectation.components = new String[]{":*","provider",":*","segment","%"};
        expectation.xri = "xri://@openmdx*($..)/provider/($..)/segment/($...)";
        expectation.iri = URI.create("xri://@openmdx*($..)/provider/($..)/segment/($...)");
        expectation.uri = "@openmdx*($..)/provider/($..)/segment/($...)";
        expectation.classicRepresentation = "::*/provider/::*/segment/%";
        expectation.legacyUri = "spice://:*/provider/:*/segment/%";
        expectation.legacyXri = "xri:@openmdx:**/provider/**/segment/***";
        expectation.containsWildcarcd = true;
        arrangeActAndAssertAccordingToExpectation();
    }

    @Test
    public void whenSomeProvidersSomeSegmentsAndAllItsDescendantsThenBuildSegmentAndDescendantsPattern(){
        expectation.components = new String[]{"A:B","provider",":org*","segment","org%"};
        expectation.xri = "xri://@openmdx*A.B/provider/($.*org)*($..)/segment/($.*org)*($..)/($...)";
        expectation.iri = URI.create("xri://@openmdx*A.B/provider/($.*org)*($..)/segment/($.*org)*($..)/($...)");
        expectation.uri = "@openmdx*A.B/provider/($.*org)*($..)/segment/($.*org)*($..)/($...)";
        expectation.classicRepresentation = "A::B/provider/::org*/segment/org%";
        expectation.legacyUri = "spice://A:B/provider/:org*/segment/org%";
        expectation.legacyXri = "xri:@openmdx:A.B/provider/org**/segment/org***";
        expectation.containsWildcarcd = true;
        arrangeActAndAssertAccordingToExpectation();
    }

    @Test
    public void whenSomeProvidersWithColonSomeSegmentsWithColonAndAllItsDescendantsThenBuildSegmentAndDescendantsPattern(){
        expectation.components = new String[]{"A:B","provider",":org:*","segment","org:%"};
        expectation.xri = "xri://@openmdx*A.B/provider/($.*org:)*($..)/segment/($.*org:)*($..)/($...)";
        expectation.iri = URI.create("xri://@openmdx*A.B/provider/($.*org:)*($..)/segment/($.*org:)*($..)/($...)");
        expectation.uri = "@openmdx*A.B/provider/($.*org:)*($..)/segment/($.*org:)*($..)/($...)";
        expectation.classicRepresentation = "A::B/provider/::org::*/segment/org::%";
        expectation.legacyUri = "spice://A:B/provider/:org:*/segment/org:%";
        expectation.legacyXri = "xri:@openmdx:A.B/provider/org:**/segment/org:***";
        expectation.containsWildcarcd = true;
        arrangeActAndAssertAccordingToExpectation();
    }

    @Test
    public void whenAnySegmentAsCompatibilityXriThenBuildSegmentAndDescendantsPattern(){
        // Arrange
        final String legacyXri = "xri:@openmdx:**/provider/**/segment/***";
        final String compatibilityXri = "xri:@openmdx:*/provider/:*/segment/%";
        // Act
        Path testee = new Path(compatibilityXri);
        // Assert
        assertEquals("Compatibility XRI", new Path(legacyXri), testee);
    }    

    @Test
    public void whenBuiltFromComponentsThenComponentsAreRetrievable(){
        // Arrange
        final String[] components = new String[]{"org:openmdx:audit2", "provider", "Audit", "segment", "000", "unitOfWork", "15b1e210-f8c2-11e3-aa50-0133ea275f6a"};
        // Act
        Path testee = new Path(components);
        // Assert
        int i = 0;
        for(XRISegment component : testee.getSegments()) {
        	Assert.assertEquals("Index " + i, components[i++], component.toClassicRepresentation());
        }
    }    
    
    @Test
    public void whenBuiltFromComponentsThenSuffixIsRetrievebla(){
        // Arrange
        final String[] components = new String[]{"org:openmdx:audit2", "provider", "Audit", "segment", "000", "unitOfWork", "15b1e210-f8c2-11e3-aa50-0133ea275f6a"};
        final String[][] expected = new String[components.length + 1][];
        for(int i = 0; i < expected.length; i++) {
        	expected[i] = Arrays.asList(components).subList(i, components.length).toArray(new String[components.length - i]);
        }
        // Act
        Path testee = new Path(components);
        // Assert
        for(int i = 0; i < expected.length; i++) {
        	Assert.assertArrayEquals("Suffix " + i, expected[i], testee.getSuffix(i));
        }
    }    

    @Test
    public void whenBuiltFromComponentsThenPrefixIsRetrievebla(){
        // Arrange
        final String[] components = new String[]{"org:openmdx:audit2", "provider", "Audit", "segment", "000", "unitOfWork", "15b1e210-f8c2-11e3-aa50-0133ea275f6a"};
        final Path[] expected = new Path[components.length + 1];
        for(int i = 0; i < expected.length; i++) {
        	expected[i] = new Path(Arrays.asList(components).subList(0, i).toArray(new String[i]));
        }
        // Act
        Path testee = new Path(components);
        // Assert
        for(int i = 0; i < expected.length; i++) {
        	Assert.assertEquals("Prefix " + i, expected[i], testee.getPrefix(i));
        }
    }    

    @Test
    public void whenBuiltFromParentThenParentIsSame(){
        // Arrange
    	final Path parent = new Path(new String[]{"org:openmdx:audit2", "provider", "Audit", "segment"});
        // Act
        Path testee = parent.getChild("000");
        // Assert
        Assert.assertSame(parent, testee.getParent());
    }    
    
    @Test
    public void whenComponentIsAPathThenChildHasACrossReference(){
    	// Arrange
    	final Path parent = new Path(new String[]{"org:openmdx:audit2", "provider", "Audit", "segment"});
    	final Path component = new Path(new String[]{"B0:B1","provider","B"});
    	// Act
    	Path testee = parent.getChild(component);
    	// Assert
    	Assert.assertEquals("base", "B0::B1/provider/B", testee.getLastSegment().toClassicRepresentation());
    }
    
    @Test 
    public void whenAnySegmentAndAllDescendantsThenBaseIsPercent(){
    	// Arrange
        String xri = "xri://@openmdx*($..)/provider/($..)/segment/($...)";
        // Act 
        Path testee = new Path(xri);
        // Assert
        Assert.assertEquals(5, testee.size());
        Assert.assertEquals("%", testee.getLastSegment().toClassicRepresentation());
    }

    @Test 
    public void whenSomeMatchAllSegmentAndAllDescendantsThenBaseEndsWithPercent(){
    	// Arrange
        String[] components = new String[]{"A:B","provider",":*","segment","00%"};
        // Act 
        Path testee = new Path(components);
        // Assert
        Assert.assertEquals(5, testee.size());
        Assert.assertTrue(testee.getLastSegment().toClassicRepresentation().endsWith("%"));
    }

    @Test 
    public void whenMatchAllThenAcceptTransientObjectId(){
    	// Arrange
        Path pattern = new Path("xri://@openmdx*($...)");
        UUID uuid = UUID.fromString("15b1e210-f8c2-11e3-aa50-0133ea275f6a");
        // Act 
        Path testee = new Path(uuid);
        // Assert
        Assert.assertTrue(testee.isLike(pattern));
    }
    
    @Test 
    public void whenComponentHasAWildcardThenIsLikeTreatsItAsRequiredEvenWhenFollowedByADescendantWildcard(){
    	// Arrange
    	final Path testee = new Path("org::opencrx::kernel::account1/provider/aProvider/segment/aSegment/account/4711");
    	final Path pattern = new Path("org::opencrx::kernel::account1/provider/::*/segment/::*/account/::*/::*/%");
		// Act
    	final boolean matches = testee.isLike(pattern);
    	// Assert
    	Assert.assertFalse(matches);
    }

    @Test 
    public void whenCandidateHasSameLengthThenItIsAPrfefix(){
    	// Arrange
    	final Path testee = new Path("org::opencrx::kernel::account1/provider");
    	final Path candidate = new Path("org::opencrx::kernel::account1/provider");
		// Act
    	final boolean prefix = testee.startsWith(candidate);
    	// Assert
    	Assert.assertTrue(prefix);
    }
    
    @Test 
    public void whenCandidateIsShorterThenItIsAPrfefix(){
    	// Arrange
    	final Path testee = new Path("org::opencrx::kernel::account1/provider");
    	final Path candidate = new Path("org::opencrx::kernel::account1");
		// Act
    	final boolean prefix = testee.startsWith(candidate);
    	// Assert
    	Assert.assertTrue(prefix);
    }

    @Test 
    public void whenCandidateIsEmptyThenItIsAPrfefix(){
    	// Arrange
    	final Path testee = new Path("org::opencrx::kernel::account1/provider");
    	final Path candidate = new Path("");
		// Act
    	final boolean prefix = testee.startsWith(candidate);
    	// Assert
    	Assert.assertTrue(prefix);
    }
    
    @Test 
    public void whenCandidateIsLongerThenItIsNotAPrfefix(){
    	// Arrange
    	final Path testee = new Path("org::opencrx::kernel::account1/provider");
    	final Path candidate = new Path("org::opencrx::kernel::account1/provider/aProvider");
		// Act
    	final boolean prefix = testee.startsWith(candidate);
    	// Assert
    	Assert.assertFalse(prefix);
    }
    
    @Test 
    public void whenComponentHasAStandardWildcardThenIsLikeTreatsItAsRequired(){
    	// Arrange
    	final Path testee = new Path("org::opencrx::kernel::account1/provider/aProvider/segment/aSegment/account/4711");
    	final Path pattern = new Path("org::opencrx::kernel::account1/provider/::*/segment/::*/account/::*/::*");
		// Act
    	final boolean matches = testee.isLike(pattern);
    	// Assert
    	Assert.assertFalse(matches);
    }

    @Test 
    public void whenComponentHasADescendantWildcardThenIsLikeTreatsItAsOptional(){
    	// Arrange
    	final Path testee = new Path("org::opencrx::kernel::account1/provider/aProvider/segment/aSegment/account/4711");
    	final Path pattern = new Path("org::opencrx::kernel::account1/provider/::*/segment/::*/account/::*/%");
		// Act
    	final boolean matches = testee.isLike(pattern);
    	// Assert
    	Assert.assertTrue(matches);
    }
    
    @Test
    public void whenExternalRepresentationIsRetrievedTwiceThenTheSameObjectIsReturned(){
    	// Arrange
    	final Path testee = new Path(new String[]{"org:openmdx:audit2", "provider", "Audit"});
		// Act
    	final String xri = testee.toXRI();
    	// Assert
    	Assert.assertSame(xri, testee.toXRI());
    }

    @Test
    public void whenParentIsRetrievedTwiceThenTheSameObjectIsReturned(){
    	// Arrange
    	final Path testee = new Path(new String[]{"org:openmdx:audit2", "provider", "Audit"});
		// Act
    	final Path parent = testee.getParent();
    	// Assert
    	Assert.assertSame(parent, testee.getParent());
    }

    @Test
    public void whenLastSegmentIsRetrievedTwiceThenTheSameObjectIsReturned(){
    	// Arrange
    	final Path testee = new Path(new String[]{"org:openmdx:audit2", "provider", "Audit"});
		// Act
    	final XRISegment lastSegment = testee.getLastSegment();
    	// Assert
    	Assert.assertSame(lastSegment, testee.getLastSegment());
    }
    
    @Test
    public void oneSegmentPathIsGreaterThanEmptyPath(){
        // Arrange
        final Path left = new Path(new String[]{"org:openmdx:base"}); 
        final Path right = new Path("");
        // Act
        int value = left.compareTo(right);
        // Assert
        Assert.assertTrue(value > 0);
    }
    
    @Test
    public void emptyPathIsLessThanOneSegmentPath(){
        // Arrange
        final Path right = new Path(new String[]{"org:openmdx:base"}); 
        final Path left = new Path("");
        // Act
        int value = left.compareTo(right);
        // Assert
        Assert.assertTrue(value < 0);
    }

    @Test
    public void twoSegmentPathIsGreaterThanOneSegmentPath(){
        // Arrange
        final Path left = new Path("xri://@openmdx*A/provider"); 
        final Path right = new Path("xri://@openmdx*A");
        // Act
        int value = left.compareTo(right);
        // Assert
        Assert.assertTrue(value > 0);
    }
    
    @Test
    public void twoSegmentPathIsLessThanThreeSegmentPath(){
        // Arrange
        final Path left = new Path("xri://@openmdx*A/provider"); 
        final Path right = new Path("xri://@openmdx*A/provider/P");
        // Act
        int value = left.compareTo(right);
        // Assert
        Assert.assertTrue(value < 0);
    }

    @Test
    public void leadingSegmentDeterminesOrder(){
        // Arrange
        final Path left = new Path("xri://@openmdx*B/provider"); 
        final Path right = new Path("xri://@openmdx*A/provider/P");
        // Act
        int value = left.compareTo(right);
        // Assert
        Assert.assertTrue(value > 0);
    }

    @Test
    public void samePathComparesTo0(){
        // Arrange
        final Path left = new Path(new String[]{"A","provider", "P"}); 
        final Path right = new Path("xri://@openmdx*A/provider/P");
        // Act
        int value = left.compareTo(right);
        // Assert
        Assert.assertEquals(0, value);
    }
    
    private void arrangeActAndAssertAccordingToExpectation(){
        for(Format buildFormat : applicableFormats()) {
            arrangeActAndAssert(buildFormat);
        }
    }

    private void arrangeActAndAssert(Format buildFormat) {
        // Arrange
        Object externalRepesentation = buildFormat.getExternalRepresentation(expectation);
        // Act 
        Path testee = buildFormat.fromExternalRepresentation(externalRepesentation);
        // Assert
        String testedRepresentation = getTestedRepresentation(externalRepesentation);
        for(Format validationFormat : applicableFormats()) {
            final String message = testedRepresentation + " \u2014" + buildFormat.name() + "\u2192 " + expectation.xri + " \u2014" + validationFormat.name() + "\u2192 ";
            Object expectedExternalRepresentation = validationFormat.getExternalRepresentation(expectation);
            Object actualExternalRepresentation = validationFormat.toExternalRepresentation(testee);
            assertEquals(message, expectedExternalRepresentation, actualExternalRepresentation);
        }
        assertEquals(testedRepresentation + " wildcard", expectation.containsWildcarcd, testee.isPattern());
        assertEquals(testedRepresentation + " transientObjectId", expectation.transientObjectId, testee.isTransactionalObjectId());
    }
    
    private String getTestedRepresentation(Object externalRepesentation){
        if(externalRepesentation.getClass().isArray()) {
            return Arrays.asList((Object[])externalRepesentation).toString();
        } else {
            return externalRepesentation.toString();
        }
    }
    
    private static void assertEquals(String message, Object expectedValue, Object actualValue) {
        if(expectedValue.getClass().isArray()) {
            Assert.assertArrayEquals(message, (Object[])expectedValue, (Object[])actualValue);
        } else {
            Assert.assertEquals(message, expectedValue, actualValue);
        }
    }
    
    private static void assertEquals(String message, boolean expectedValue, boolean actualValue) {
        if(expectedValue) {
            Assert.assertTrue(message, actualValue);
        } else {
            Assert.assertFalse(message, actualValue);
        }
    }
    
    private Iterable<Format> applicableFormats(){
        EnumSet<Format> reply = EnumSet.noneOf(Format.class);
        for(Format format : Format.values()) {
            if(format.isApplicable(expectation)) {
                reply.add(format);
            }
        }
        return reply;
    }

    static class Expectation {
        String[] components;
        String xri;
        URI iri;
        String uri;
        String legacyXri;
        String legacyUri;
        String classicRepresentation;
        UUID uuid;
        boolean objectPath;
        boolean containsWildcarcd;
        boolean transientObjectId;
    }

    enum Format {
        COMPONENTS {

            @Override
            String[] toExternalRepresentation(Path source) {
                return source.getComponents();
            }

            @Override
            String[] getExternalRepresentation(Expectation expectation) {
                return expectation.components;
            }

            @Override
            Path fromExternalRepresentation(Object source) {
                return new Path((String[])source);
            }

        },
        XRI {

            @Override
            String toExternalRepresentation(Path source) {
                return source.toXRI();
            }
            
            @Override
            String getExternalRepresentation(Expectation expectation) {
                return expectation.xri;
            }
            
        },
        IRI {

            @Override                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
            URI toExternalRepresentation(Path source) {
                return source.toIRI();
            }
            
            @Override
            URI getExternalRepresentation(Expectation expectation) {
                return expectation.iri;
            }
            
            @Override
            Path fromExternalRepresentation(Object source) {
                return new Path((URI)source);
            }
            
       },
       URI {

           @Override                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
           String toExternalRepresentation(Path source) {
               return source.toURI();
           }
           
           @Override
           String getExternalRepresentation(Expectation expectation) {
               return expectation.uri;
           }
           
      },
        LEGACY_XRI {

            @SuppressWarnings("deprecation")
            @Override
            String toExternalRepresentation(Path source) {
                return source.toXri();
            }

            @Override
            String getExternalRepresentation(Expectation expectation) {
                return expectation.legacyXri;
            }

        },
        LEGACY_URI {

            @SuppressWarnings("deprecation")
            @Override
            String toExternalRepresentation(Path source) {
                return source.toUri();
            }
            

            @Override
            String getExternalRepresentation(Expectation expectation) {
                return expectation.legacyUri;
            }

        },
        CLASSIC_REPRESENTATION {

            @Override
            String toExternalRepresentation(Path source) {
                return source.toClassicRepresentation();
            }

            @Override
            String getExternalRepresentation(Expectation expectation) {
                return expectation.classicRepresentation;
            }

        },
        UUID {

            @Override
            UUID toExternalRepresentation(Path source) {
                return source.toTransactionalObjectId();
            }
            
            @Override
            UUID getExternalRepresentation(Expectation expectation) {
                return expectation.uuid;
            }


            @Override
            Path fromExternalRepresentation(Object source) {
                return new Path((UUID)source);
            }

        };        
        
        abstract Object toExternalRepresentation(Path source);

        Path fromExternalRepresentation(Object source){
            return new Path(source.toString());
        }
        
        abstract Object getExternalRepresentation(Expectation expectation);
        
        boolean isApplicable(Expectation expectation){
            return getExternalRepresentation(expectation) != null;
        }
        
    }
    
}
