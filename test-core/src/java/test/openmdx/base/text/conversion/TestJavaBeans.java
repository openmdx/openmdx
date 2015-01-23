/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: JavaBeans Test
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2013, OMEX AG, Switzerland
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
package test.openmdx.base.text.conversion;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.query.Filter;
import org.openmdx.base.rest.cci.ConditionRecord;
import org.openmdx.base.text.conversion.JavaBeans;
import org.openmdx.kernel.exception.BasicException;

/**
 * JavaBeans Test
 */
public class TestJavaBeans {

    @Test
    public void testXMLDecoder(
    ) throws Exception {
        Filter filter = (Filter)JavaBeans.fromXML(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
            "<java version=\"1.7.0_25\" class=\"java.beans.XMLDecoder\">\n" + 
            " <object class=\"org.openmdx.base.query.Filter\">\n" + 
            "  <void property=\"condition\">\n" + 
            "   <void method=\"add\">\n" + 
            "    <object class=\"org.openmdx.base.query.AnyTypeCondition\">\n" + 
            "     <void property=\"feature\">\n" + 
            "      <string>context:LZXJ9BXXBA40ZAZZK78LPBSWU:clause</string>\n" + 
            "     </void>\n" + 
            "     <void property=\"value\">\n" + 
            "      <array class=\"java.lang.Object\" length=\"1\">\n" + 
            "       <void index=\"0\">\n" + 
            "        <string>(v.alias_name IS NOT NULL) AND (v.alias_name &lt;&gt; &apos;&apos;)</string>\n" + 
            "       </void>\n" + 
            "      </array>\n" + 
            "     </void>\n" + 
            "    </object>\n" + 
            "   </void>\n" + 
            "   <void method=\"add\">\n" + 
            "    <object class=\"org.openmdx.base.query.AnyTypeCondition\">\n" + 
            "     <void property=\"feature\">\n" + 
            "      <string>context:LZXJ9BXXBA40ZAZZK78LPBSWU:object_class</string>\n" + 
            "     </void>\n" + 
            "     <void property=\"value\">\n" + 
            "      <array class=\"java.lang.Object\" length=\"1\">\n" + 
            "       <void index=\"0\">\n" + 
            "        <string>org:openmdx:compatibility:datastore1:QueryFilter</string>\n" + 
            "       </void>\n" + 
            "      </array>\n" + 
            "     </void>\n" + 
            "    </object>\n" + 
            "   </void>\n" + 
            "   <void method=\"add\">\n" + 
            "    <object class=\"org.openmdx.base.query.AnyTypeCondition\">\n" + 
            "     <void property=\"feature\">\n" + 
            "      <string>context:LZXJ9BXXBA40ZAZZK78LPBSWU:stringParam</string>\n" + 
            "     </void>\n" + 
            "     <void property=\"value\">\n" + 
            "      <array class=\"java.lang.Object\" length=\"0\" id=\"ObjectArray0\"/>\n" + 
            "     </void>\n" + 
            "    </object>\n" + 
            "   </void>\n" + 
            "   <void method=\"add\">\n" + 
            "    <object class=\"org.openmdx.base.query.AnyTypeCondition\">\n" + 
            "     <void property=\"feature\">\n" + 
            "      <string>context:LZXJ9BXXBA40ZAZZK78LPBSWU:integerParam</string>\n" + 
            "     </void>\n" + 
            "     <void property=\"value\">\n" + 
            "      <object idref=\"ObjectArray0\"/>\n" + 
            "     </void>\n" + 
            "    </object>\n" + 
            "   </void>\n" + 
            "   <void method=\"add\">\n" + 
            "    <object class=\"org.openmdx.base.query.AnyTypeCondition\">\n" + 
            "     <void property=\"feature\">\n" + 
            "      <string>context:LZXJ9BXXBA40ZAZZK78LPBSWU:decimalParam</string>\n" + 
            "     </void>\n" + 
            "     <void property=\"value\">\n" + 
            "      <object idref=\"ObjectArray0\"/>\n" + 
            "     </void>\n" + 
            "    </object>\n" + 
            "   </void>\n" + 
            "   <void method=\"add\">\n" + 
            "    <object class=\"org.openmdx.base.query.AnyTypeCondition\">\n" + 
            "     <void property=\"feature\">\n" + 
            "      <string>context:LZXJ9BXXBA40ZAZZK78LPBSWU:booleanParam</string>\n" + 
            "     </void>\n" + 
            "     <void property=\"value\">\n" + 
            "      <object idref=\"ObjectArray0\"/>\n" + 
            "     </void>\n" + 
            "    </object>\n" + 
            "   </void>\n" + 
            "   <void method=\"add\">\n" + 
            "    <object class=\"org.openmdx.base.query.AnyTypeCondition\">\n" + 
            "     <void property=\"feature\">\n" + 
            "      <string>context:LZXJ9BXXBA40ZAZZK78LPBSWU:dateParam</string>\n" + 
            "     </void>\n" + 
            "     <void property=\"value\">\n" + 
            "      <array class=\"java.lang.Object\" length=\"0\"/>\n" + 
            "     </void>\n" + 
            "    </object>\n" + 
            "   </void>\n" + 
            "   <void method=\"add\">\n" + 
            "    <object class=\"org.openmdx.base.query.AnyTypeCondition\">\n" + 
            "     <void property=\"feature\">\n" + 
            "      <string>context:LZXJ9BXXBA40ZAZZK78LPBSWU:dateTimeParam</string>\n" + 
            "     </void>\n" + 
            "     <void property=\"value\">\n" + 
            "      <array class=\"java.lang.Object\" length=\"0\"/>\n" + 
            "     </void>\n" + 
            "    </object>\n" + 
            "   </void>\n" + 
            "  </void>\n" + 
            " </object>\n" + 
            "</java>"            
        );
        int index = 0;
        for(ConditionRecord condition: filter.getCondition()) {
            assertNotNull("condition[" + index + "].getValue() for feature '" + condition.getFeature() + "' must not be null", condition.getValue());
            index++;
        }
    }
    
    @Test
    public void testXStream(
    ) throws Exception {
    	try {
	        Filter filter = (Filter)JavaBeans.fromXML(
	            "<org.openmdx.base.query.Filter>\n" +
	          	"  <conditions>\n" +
	          	"    <org.openmdx.base.query.AnyTypeCondition>\n" +
	          	"      <feature>context:LZXJ9BXXBA40ZAZZK78LPBSWU:clause</feature>\n" +
	          	"      <values>\n" +
	          	"        <string>(v.alias_name IS NOT NULL) AND (v.alias_name &lt;&gt; &apos;&apos;)</string>\n" +
	          	"      </values>\n" +
	          	"    </org.openmdx.base.query.AnyTypeCondition>\n" +
	          	"    <org.openmdx.base.query.AnyTypeCondition>\n" +
	          	"      <feature>context:LZXJ9BXXBA40ZAZZK78LPBSWU:object_class</feature>\n" +
	          	"      <values>\n" +
	          	"        <string>org:openmdx:compatibility:datastore1:QueryFilter</string>\n" +
	          	"      </values>\n" +
	          	"    </org.openmdx.base.query.AnyTypeCondition>\n" +
	          	"    <org.openmdx.base.query.AnyTypeCondition>\n" +
	          	"      <feature>context:LZXJ9BXXBA40ZAZZK78LPBSWU:stringParam</feature>\n" +
	          	"      <values/>\n" +
	          	"    </org.openmdx.base.query.AnyTypeCondition>\n" +
	          	"    <org.openmdx.base.query.AnyTypeCondition>\n" +
	          	"      <feature>context:LZXJ9BXXBA40ZAZZK78LPBSWU:integerParam</feature>\n" +
	          	"      <values reference=\"../../org.openmdx.base.query.AnyTypeCondition[3]/values\"/>\n" +
	          	"    </org.openmdx.base.query.AnyTypeCondition>\n" +
	          	"    <org.openmdx.base.query.AnyTypeCondition>\n" +
	          	"      <feature>context:LZXJ9BXXBA40ZAZZK78LPBSWU:decimalParam</feature>\n" +
	          	"      <values reference=\"../../org.openmdx.base.query.AnyTypeCondition[3]/values\"/>\n" +
	          	"    </org.openmdx.base.query.AnyTypeCondition>\n" +
	          	"    <org.openmdx.base.query.AnyTypeCondition>\n" +
	          	"      <feature>context:LZXJ9BXXBA40ZAZZK78LPBSWU:booleanParam</feature>\n" +
	          	"      <values reference=\"../../org.openmdx.base.query.AnyTypeCondition[3]/values\"/>\n" +
	          	"    </org.openmdx.base.query.AnyTypeCondition>\n" +
	          	"    <org.openmdx.base.query.AnyTypeCondition>\n" +
	          	"      <feature>context:LZXJ9BXXBA40ZAZZK78LPBSWU:dateParam</feature>\n" +
	          	"      <values/>\n" +
	          	"    </org.openmdx.base.query.AnyTypeCondition>\n" +
	          	"    <org.openmdx.base.query.AnyTypeCondition>\n" +
	          	"      <feature>context:LZXJ9BXXBA40ZAZZK78LPBSWU:dateTimeParam</feature>\n" +
	          	"      <values/>\n" +
	          	"    </org.openmdx.base.query.AnyTypeCondition>\n" +
	          	"  </conditions>\n" +
	          	"  <orderSpecifiers/>\n" +
	          	"  <extensions/>\n" +
	          	"</org.openmdx.base.query.Filter>"    	
	        );
	        int index = 0;
	        for(ConditionRecord condition: filter.getCondition()) {
	            assertNotNull("condition[" + index + "].getValue() for feature '" + condition.getFeature() + "' must not be null", condition.getValue());
	            index++;
	        }
    	} catch(ServiceException e) {
    		if(e.getExceptionCode() != BasicException.Code.TRANSFORMATION_FAILURE) {
    			throw e;
    		}
    	}
    }

}
