package org.openxri;

import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

public class IRIUtilsTest extends TestCase
{

	/*
	 * Test method for 'org.openxri.IRIUtils.IRItoXRI(String)'
	 */
	public void testIRItoXRI() throws UnsupportedEncodingException
	{
		String r;
		r = IRIUtils.IRItoXRI("http://xri.net/");
		assertTrue(r.equals("http://xri.net/"));

		r = IRIUtils.IRItoXRI("http://xri.net/@foo%25bar");
		assertTrue(r.equals("http://xri.net/@foo%bar"));
		
		// transform the result once again, should not change
		r = IRIUtils.IRItoXRI(r);
		assertTrue(r.equals("http://xri.net/@foo%bar"));
		
		String u = "=%E6%97%A0%E8%81%8A";
		String i = IRIUtils.URItoIRI(u);
		System.out.println(IRIUtils.IRItoURI(i));
		assertTrue(IRIUtils.IRItoURI(i).equals(u));
	}

	/*
	 * Test method for 'org.openxri.IRIUtils.URItoIRI(String)'
	 */
	public void testURItoIRI()
	{
		String r;
		
		try {
			r = IRIUtils.URItoIRI("");
			assertTrue(r.length() == 0);

			r = IRIUtils.URItoIRI("http://xri.net/");
			assertTrue(r.equals("http://xri.net/"));

			r = IRIUtils.URItoIRI("http://www.example.org/%44%c3%bCrst");
			assertTrue(r.equals("http://www.example.org/Dürst"));
			
			r = IRIUtils.URItoIRI("http://r%C3%a9sum%c3%A9.example.org");
			assertTrue(r.equals("http://résumé.example.org"));

			r = IRIUtils.URItoIRI("xri://@example*(http:%2F%2Fexample.org%2F)/");
			assertTrue(r.equals("xri://@example*(http://example.org/)/"));
			
			// %-encoded BiDi - should not be converted
			r = IRIUtils.URItoIRI("http://example.org/%E2%80%AA-blah");
			assertTrue(r.equals("http://example.org/%E2%80%AA-blah"));
			
			// non-IRI
			r = IRIUtils.URItoIRI("http://www.example.org/D%FCrst");
			assertTrue(r.equals("http://www.example.org/D%FCrst"));

			// invalid Percent encoding
			try {
				r = IRIUtils.URItoIRI("http://www.example.org/D%Frst");
				assertTrue("Expected exception here", false);				
			}
			catch (XRIParseException e) {
				// ok
			}
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			assertTrue("exception caught!", false);
		}
	}

}
