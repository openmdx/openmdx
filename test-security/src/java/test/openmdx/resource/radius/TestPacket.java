package test.openmdx.resource.radius;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.junit.Test;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusAttributeValues;
import org.tinyradius.attribute.RadiusAttribute;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusException;

public class TestPacket {

	/**
	 * We have to provide a shared secret
	 */
	private static final String SHARED_SECRET = "testing123";
	
	/**
	 * The encoding
	 */
    public static final String ENCODING = "UTF-8";
	
    private static final Pattern newPINPattern = Pattern.compile("(?s).*Enter a new PIN.*");
    private static final Pattern nextTokenPattern = Pattern.compile("(?s).*Wait for token to change.*");
    private static final Pattern waitPattern = Pattern.compile("(?s).*PIN Accepted.*");
    private static final Pattern confirmPINPattern = Pattern.compile("(?s).*e-enter new PIN.*");
    
	/**
	 * Open the resource and seek the beginning
	 * 
	 * @param name the resource name
	 * @param offset the number of bytes to be skipped
	 * 
	 * @return the stream at the requested position
	 * 
	 * @throws IOException
	 */
	private InputStream getSource(
		String name,
		int offset
	) throws IOException {
		InputStream in = getClass().getResource(name).openStream();
		in.skip(offset);
		return in;
	}
	
	/**
	 * Decode and log the RADIUS packet
	 * 
	 * @param name the resource name
	 * @param offset prolog size
	 * 
	 * @return the RADIUS packet
	 * 
	 * @throws IOException
	 * @throws RadiusException
	 */
	private RadiusPacket getPacket(
		String name,
		int offset
	) throws IOException, RadiusException {
		RadiusPacket packet = RadiusPacket.decodeRequestPacket(getSource(name, offset), SHARED_SECRET);
		System.out.println("Source: " + name + "#" + Integer.toHexString(offset));
		System.out.println(packet);
		System.out.println();
		return packet;
	}
	
	@Test
	public void testAccessAccept() throws IOException, RadiusException{
		RadiusPacket packet = getPacket("Access-Accept.cap", 0xc4);
		assertEquals("Access-Accept", RadiusPacket.ACCESS_ACCEPT, packet.getPacketType());
	}

	@Test
	public void testAccessChallengeNewPIN() throws IOException, RadiusException{
		RadiusPacket packet = getPacket("Access-Challenge-newPIN.cap", 0xc4);
		assertEquals("Access-Challenge", RadiusPacket.ACCESS_CHALLENGE, packet.getPacketType());
		RadiusAttribute replyMessage = packet.getAttribute("Reply-Message");
		assertNotNull(replyMessage);
		assertEquals("Reply-Message", RadiusAttributeValues.REPLY_MESSAGE, replyMessage.getAttributeType());
		assertTrue("New PIN", newPINPattern.matcher(replyMessage.getAttributeValue()).matches());
		RadiusAttribute state = packet.getAttribute("State");
		assertNotNull(state);
		assertEquals("State", RadiusAttributeValues.STATE, state.getAttributeType());
	}
	
	@Test
	public void testNewPIN() throws IOException, RadiusException{
		{
			RadiusPacket packet = getPacket("newPIN.cap", 0xe2);
			assertEquals("Access-Challenge", RadiusPacket.ACCESS_CHALLENGE, packet.getPacketType());
			{
				String id = "Reply-Message";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(id, attribute);
				assertEquals(id, RadiusAttributeValues.REPLY_MESSAGE, attribute.getAttributeType());
				assertTrue("New PIN", newPINPattern.matcher(attribute.getAttributeValue()).matches());
			}
			{
				String id = "State";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(attribute);
				assertEquals(id, RadiusAttributeValues.STATE, attribute.getAttributeType());
			}
		}
		{
			RadiusPacket packet = getPacket("newPIN.cap", 0x192);
			assertEquals("Access-Request", RadiusPacket.ACCESS_REQUEST, packet.getPacketType());
			{
				String id = "NAS-IP-Address";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(id, attribute);
				assertEquals(id, RadiusAttributeValues.NAS_IP_ADDRESS, attribute.getAttributeType());
				assertArrayEquals(id, new byte[]{10,1,58,20}, attribute.getAttributeData());
			}
			{
				String id = "NAS-Port";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(id, attribute);
				assertEquals(id, RadiusAttributeValues.NAS_PORT, attribute.getAttributeType());
				assertEquals(id, "2", attribute.getAttributeValue());
			}
			{
				String id = "NAS-Port-Type";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(id, attribute);
				assertEquals(id, RadiusAttributeValues.NAS_PORT_TYPE, attribute.getAttributeType());
				assertEquals(id, "Virtual", attribute.getAttributeValue());
			}
			{
				String id = "User-Name";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(id, attribute);
				assertEquals(id, RadiusAttributeValues.USER_NAME, attribute.getAttributeType());
				assertEquals(id, "upso", attribute.getAttributeValue());
			}
			{
				String id = "Calling-Station-Id";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(id, attribute);
				assertEquals(id, RadiusAttributeValues.CALLING_STATION_ID, attribute.getAttributeType());
				assertEquals(id, "10.1.58.68", attribute.getAttributeValue());
			}
			{
				String id = "User-Password";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(id, attribute);
				assertEquals(id, RadiusAttributeValues.USER_PASSWORD, attribute.getAttributeType());
			}
		}
		{
			RadiusPacket packet = getPacket("newPIN.cap", 0x22a);
			assertEquals("Access-Challenge", RadiusPacket.ACCESS_CHALLENGE, packet.getPacketType());
			{
				String id = "Reply-Message";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(id, attribute);
				assertEquals(id, RadiusAttributeValues.REPLY_MESSAGE, attribute.getAttributeType());
				assertTrue("Confirm PIN", confirmPINPattern.matcher(attribute.getAttributeValue()).matches());
			}
			{
				String id = "State";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(attribute);
				assertEquals(id, RadiusAttributeValues.STATE, attribute.getAttributeType());
			}
		}
		{
			RadiusPacket packet = getPacket("newPIN.cap", 0x2b2);
			assertEquals("Access-Request", RadiusPacket.ACCESS_REQUEST, packet.getPacketType());
			{
				String id = "NAS-IP-Address";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(id, attribute);
				assertEquals(id, RadiusAttributeValues.NAS_IP_ADDRESS, attribute.getAttributeType());
				assertArrayEquals(id, new byte[]{10,1,58,20}, attribute.getAttributeData());
			}
			{
				String id = "NAS-Port";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(id, attribute);
				assertEquals(id, RadiusAttributeValues.NAS_PORT, attribute.getAttributeType());
				assertEquals(id, "2", attribute.getAttributeValue());
			}
			{
				String id = "NAS-Port-Type";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(id, attribute);
				assertEquals(id, RadiusAttributeValues.NAS_PORT_TYPE, attribute.getAttributeType());
				assertEquals(id, "Virtual", attribute.getAttributeValue());
			}
			{
				String id = "User-Name";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(id, attribute);
				assertEquals(id, RadiusAttributeValues.USER_NAME, attribute.getAttributeType());
				assertEquals(id, "upso", attribute.getAttributeValue());
			}
			{
				String id = "Calling-Station-Id";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(id, attribute);
				assertEquals(id, RadiusAttributeValues.CALLING_STATION_ID, attribute.getAttributeType());
				assertEquals(id, "10.1.58.68", attribute.getAttributeValue());
			}
			{
				String id = "User-Password";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(id, attribute);
				assertEquals(id, RadiusAttributeValues.USER_PASSWORD, attribute.getAttributeType());
			}
		}
		{
			RadiusPacket packet = getPacket("newPIN.cap", 0x34a);
			assertEquals("Access-Challenge", RadiusPacket.ACCESS_CHALLENGE, packet.getPacketType());
			{
				String id = "Reply-Message";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(id, attribute);
				assertEquals(id, RadiusAttributeValues.REPLY_MESSAGE, attribute.getAttributeType());
				assertTrue("Wait", waitPattern.matcher(attribute.getAttributeValue()).matches());
			}
			{
				String id = "State";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(attribute);
				assertEquals(id, RadiusAttributeValues.STATE, attribute.getAttributeType());
			}
		}
		{
			RadiusPacket packet = getPacket("newPIN.cap", 0x40a);
			assertEquals("Access-Request", RadiusPacket.ACCESS_REQUEST, packet.getPacketType());
			{
				String id = "NAS-IP-Address";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(id, attribute);
				assertEquals(id, RadiusAttributeValues.NAS_IP_ADDRESS, attribute.getAttributeType());
				assertArrayEquals(id, new byte[]{10,1,58,20}, attribute.getAttributeData());
			}
			{
				String id = "NAS-Port";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(id, attribute);
				assertEquals(id, RadiusAttributeValues.NAS_PORT, attribute.getAttributeType());
				assertEquals(id, "2", attribute.getAttributeValue());
			}
			{
				String id = "NAS-Port-Type";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(id, attribute);
				assertEquals(id, RadiusAttributeValues.NAS_PORT_TYPE, attribute.getAttributeType());
				assertEquals(id, "Virtual", attribute.getAttributeValue());
			}
			{
				String id = "User-Name";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(id, attribute);
				assertEquals(id, RadiusAttributeValues.USER_NAME, attribute.getAttributeType());
				assertEquals(id, "upso", attribute.getAttributeValue());
			}
			{
				String id = "Calling-Station-Id";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(id, attribute);
				assertEquals(id, RadiusAttributeValues.CALLING_STATION_ID, attribute.getAttributeType());
				assertEquals(id, "10.1.58.68", attribute.getAttributeValue());
			}
			{
				String id = "User-Password";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(id, attribute);
				assertEquals(id, RadiusAttributeValues.USER_PASSWORD, attribute.getAttributeType());
			}
		}
		{
			RadiusPacket packet = getPacket("newPIN.cap", 0x4a2);
			assertEquals("Access-Accept", RadiusPacket.ACCESS_ACCEPT, packet.getPacketType());
		}
	}
	

	@Test
	public void testAccessChallengeNextToken() throws IOException, RadiusException{
		RadiusPacket packet = getPacket("Access-Challenge-nextToken.cap", 0xc4);
		assertEquals("Access-Challenge", RadiusPacket.ACCESS_CHALLENGE, packet.getPacketType());
		RadiusAttribute replyMessage = packet.getAttribute("Reply-Message");
		assertNotNull(replyMessage);
		assertEquals("Reply-Message", RadiusAttributeValues.REPLY_MESSAGE, replyMessage.getAttributeType());
		assertTrue("Next Token", nextTokenPattern.matcher(replyMessage.getAttributeValue()).matches());
		RadiusAttribute state = packet.getAttribute("State");
		assertNotNull(state);
		assertEquals("State", RadiusAttributeValues.STATE, state.getAttributeType());		
	}

}
