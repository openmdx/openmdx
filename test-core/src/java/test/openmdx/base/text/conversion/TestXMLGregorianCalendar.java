package test.openmdx.base.text.conversion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.DatatypeConstants;

import org.junit.Assert;
import org.junit.Test;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.IsGreaterCondition;
import org.openmdx.base.query.IsGreaterOrEqualCondition;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.text.conversion.JavaBeans;
import org.w3c.spi.DatatypeFactories;

/**
 *TestsanEXMLgregorianCalendar'sJavaBeanXMLEncoding
 */
public class TestXMLGregorianCalendar {

   /**
    * The expected query
    */
   private static final String[] expected = {
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

   private Map<?,String> parse(String line) {
       line = line.trim();
       if(line.startsWith("<") && line.endsWith(">")){
           Map<Object,String> map = new HashMap<Object, String>();
           int i = 0;
           for(String entry : line.substring(1, line.length() - 1).split("\\s+")){
               int e = entry.indexOf('=');
               if(e < 0) {
                   map.put(Integer.valueOf(++i), entry);
               } else {
                   map.put(entry.substring(0, e), entry.substring(e + 1));
               }
           }
           return map;       
       } else {
           return Collections.singletonMap(Integer.valueOf(0), line);
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
        Assert.assertEquals("<?xml...", expected[0], actual.readLine().trim());
        String line1 = actual.readLine().trim();
        Assert.assertTrue(
            "<java version=\"...\" class=\"java.beans.XMLDecoder\">", 
            line1.startsWith("<java version=\"") && line1.endsWith("\" class=\"java.beans.XMLDecoder\">")
        );
        for(int i = 2; i < expected.length; i++) {
            Assert.assertEquals("Line " + i, parse(expected[i]), parse(actual.readLine()));
        }
    }
    
    @Test
    public void testEncodingAndDecoding() throws ServiceException, IOException {
       Filter original = new Filter(
           new IsGreaterOrEqualCondition(
               Quantifier.THERE_EXISTS, 
               "stateValidFrom", 
               true, 
               DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendarDate(2000, 4, 1, DatatypeConstants.FIELD_UNDEFINED)
           ), new IsGreaterCondition(
               Quantifier.THERE_EXISTS, 
               "stateValidTo", 
               false, 
               DatatypeFactories.immutableDatatypeFactory().newDate("2049-12-31")
           )
       );
       String query = JavaBeans.toXML(original);
       assertQuery(query);
       Filter copy = (Filter) JavaBeans.fromXML(query);
       Assert.assertTrue("From 2000-04-04 to 2049-12-31 Filter", copy.equals(original));
    }

    
} 
