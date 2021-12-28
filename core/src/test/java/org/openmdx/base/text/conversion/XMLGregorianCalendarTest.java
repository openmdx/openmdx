/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: XML Gregorian Calendar Test
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2021, OMEX AG, Switzerland
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
package org.openmdx.base.text.conversion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.datatype.DatatypeConstants;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.IsGreaterCondition;
import org.openmdx.base.query.IsGreaterOrEqualCondition;
import org.openmdx.base.query.Quantifier;
import org.openmdx.state2.spi.TechnicalAttributes;
import org.w3c.spi.DatatypeFactories;

/**
 * Test XMLGregorianCalendar's JavaBean XMLEncoding
 */
public class XMLGregorianCalendarTest {

   /**
    * The expected query
    */
   private static final String[] EXPECTED_XML_DECODER = {
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>", 
        "<java version=\"...\" class=\"java.beans.XMLDecoder\">", 
         "<object class=\"org.openmdx.base.query.Filter\">", 
          "<void property=\"condition\">", 
           "<void method=\"add\">", 
            "<object class=\"org.openmdx.base.query.IsGreaterOrEqualCondition\">", 
             "<void property=\"feature\">", 
              "<string>stateValidFrom</string>", 
             "</void>", 
             "<void property=\"fulfil\">", 
              "<boolean>true</boolean>", 
             "</void>", 
             "<void property=\"quantifier\">", 
              "<object id=\"Quantifier0\" class=\"org.openmdx.base.query.Quantifier\" method=\"valueOf\">", 
               "<string>THERE_EXISTS</string>", 
              "</object>", 
             "</void>", 
             "<void property=\"value\">", 
              "<array class=\"java.lang.Object\" length=\"1\">", 
               "<void index=\"0\">", 
                "<object class=\"org.w3c.spi2.Datatypes\" method=\"create\">", 
                 "<class>javax.xml.datatype.XMLGregorianCalendar</class>", 
                 "<string>2000-04-01</string>", 
                "</object>", 
               "</void>", 
              "</array>", 
             "</void>", 
            "</object>", 
           "</void>", 
           "<void method=\"add\">", 
            "<object class=\"org.openmdx.base.query.IsGreaterCondition\">", 
             "<void property=\"feature\">", 
              "<string>stateValidTo</string>", 
             "</void>", 
             "<void property=\"quantifier\">", 
              "<object idref=\"Quantifier0\"/>", 
             "</void>", 
             "<void property=\"value\">", 
              "<array class=\"java.lang.Object\" length=\"1\">", 
               "<void index=\"0\">", 
                "<object class=\"org.w3c.spi2.Datatypes\" method=\"create\">", 
                 "<class>javax.xml.datatype.XMLGregorianCalendar</class>", 
                 "<string>2049-12-31</string>", 
                "</object>", 
               "</void>", 
              "</array>", 
             "</void>", 
            "</object>", 
           "</void>", 
          "</void>", 
         "</object>", 
        "</java>", 
    };
   
   private static final String[] EXPECTED_XML_STREAM = {
	   "<org.openmdx.base.query.Filter>",
	   "<conditions>",
	     "<org.openmdx.base.query.IsGreaterOrEqualCondition>",
	       "<quantifier>THERE_EXISTS</quantifier>",
	       "<feature>stateValidFrom</feature>",
	       "<values>",
	         "<com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl>",
	           "<year>2000</year>",
	           "<month>4</month>",
	           "<day>1</day>",
	           "<timezone>-2147483648</timezone>",
	           "<hour>-2147483648</hour>",
	           "<minute>-2147483648</minute>",
	           "<second>-2147483648</second>",
	         "</com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl>",
	       "</values>",
	       "<fulfils>true</fulfils>",
	     "</org.openmdx.base.query.IsGreaterOrEqualCondition>",
	     "<org.openmdx.base.query.IsGreaterCondition>",
	       "<quantifier>THERE_EXISTS</quantifier>",
	       "<feature>stateValidTo</feature>",
	       "<values>",
	         "<org.w3c.cci2.ImmutableDate resolves-to=\"com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl\">",
	           "<year>2049</year>",
	           "<month>12</month>",
	           "<day>31</day>",
	           "<timezone>-2147483648</timezone>",
	           "<hour>-2147483648</hour>",
	           "<minute>-2147483648</minute>",
	           "<second>-2147483648</second>",
	         "</org.w3c.cci2.ImmutableDate>",
	       "</values>",
	       "<fulfils>false</fulfils>",
	     "</org.openmdx.base.query.IsGreaterCondition>",
	   "</conditions>",
	   "<orderSpecifiers/>",
	   "<extensions/>",
	 "</org.openmdx.base.query.Filter>"
   };

   private Map<?,String> parse(int line, String value) {
       value = value.trim();
       if(value.startsWith("<") && value.endsWith(">")){
           Map<Object,String> map = new HashMap<Object, String>();
           int i = 0;
           for(String entry : value.substring(1, value.length() - 1).split("\\s+")){
               int e = entry.indexOf('=');
               if(e < 0) {
                   map.put(Integer.valueOf(++i), entry);
               } else {
					String key = entry.substring(0, e);
	            	if(!exclude(line, key)) {
	            		map.put(key, entry.substring(e + 1));
	            	}
               }
           }
           return map;       
       } else {
           return Collections.singletonMap(Integer.valueOf(0), value);
       }
   }
   
    /**
     * Validate everything except the java version
     * Hash
     * @param query
     * @throws IOException
     */
    private void assertQuery(
        String query
    ) throws IOException{
        BufferedReader actual = new BufferedReader(new StringReader(query));
        String line0 = Objects.requireNonNull(actual.readLine()).trim();
        if(line0.startsWith("<?xml")) {
	        Assertions.assertEquals(EXPECTED_XML_DECODER[0], line0, "<?xml...");
	        for(int i = 1; i < EXPECTED_XML_DECODER.length; i++) {
	            Assertions.assertEquals(parse(i, EXPECTED_XML_DECODER[i]), parse(i, actual.readLine()),"Line " + i);
	        }
        } else {
	        Assertions.assertEquals(EXPECTED_XML_STREAM[0], line0, "<org.openmdx.base.query.Filter>");
	        for(int i = 1; i < EXPECTED_XML_STREAM.length; i++) {
	            Assertions.assertEquals(parse(-i, EXPECTED_XML_STREAM[i]), parse(-i, actual.readLine()), "Line " + i);
	        }
        }
    }

    @Test
    public void testEncodingAndDecoding() throws ServiceException, IOException {
       Filter original = new Filter(
           new IsGreaterOrEqualCondition(
               Quantifier.THERE_EXISTS, 
               TechnicalAttributes.STATE_VALID_FROM, 
               true, 
               DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendarDate(2000, 4, 1, DatatypeConstants.FIELD_UNDEFINED)
           ), new IsGreaterCondition(
               Quantifier.THERE_EXISTS, 
               TechnicalAttributes.STATE_VALID_TO, 
               false, 
               DatatypeFactories.immutableDatatypeFactory().newDate("2049-12-31")
           )
       );
       String query = JavaBeans.toXML(original);
       assertQuery(query);
       Filter copy = (Filter) JavaBeans.fromXML(query);
       Assertions.assertTrue(copy.equals(original), "From 2000-04-04 to 2049-12-31 Filter");
    }

    /**
     * Exclude elements from comparison
     * 
     * @param line positive line numbers for JavaBean, negative ones for XStream
     * @param the attribute to be excluded
     */
    private boolean exclude(int line, Object key) {
    	switch(line) {
	    	case 1: return "version".equals(key);
	    	case 2: return "id".equals(key);
    		default: return false;
    	}
    }
    
} 
