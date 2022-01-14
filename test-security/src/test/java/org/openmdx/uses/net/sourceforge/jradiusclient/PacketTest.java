/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: Packet Test
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.uses.net.sourceforge.jradiusclient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tinyradius.attribute.RadiusAttribute;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusException;

public class PacketTest {

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
		Assertions.assertEquals(RadiusPacket.ACCESS_ACCEPT, packet.getPacketType(), "Access-Accept");
	}

	@Test
	public void testAccessChallengeNewPIN() throws IOException, RadiusException{
		RadiusPacket packet = getPacket("Access-Challenge-newPIN.cap", 0xc4);
		Assertions.assertEquals(RadiusPacket.ACCESS_CHALLENGE, packet.getPacketType(), "Access-Challenge");
		RadiusAttribute replyMessage = packet.getAttribute("Reply-Message");
		assertNotNull(replyMessage);
		Assertions.assertEquals(RadiusAttributeValues.REPLY_MESSAGE, replyMessage.getAttributeType(), "Reply-Message");
		Assertions.assertTrue(newPINPattern.matcher(replyMessage.getAttributeValue()).matches(),  "New PIN");
		RadiusAttribute state = packet.getAttribute("State");
		assertNotNull(state);
		Assertions.assertEquals(RadiusAttributeValues.STATE, state.getAttributeType(), "State");
	}

	@Disabled("newPIN.cap missing")
	@Test
	public void testNewPIN() throws IOException, RadiusException{
		{
			RadiusPacket packet = getPacket("newPIN.cap", 0xe2);
			Assertions.assertEquals(RadiusPacket.ACCESS_CHALLENGE, packet.getPacketType(), "Access-Challenge");
			{
				String id = "Reply-Message";
				RadiusAttribute attribute = packet.getAttribute(id);
				Assertions.assertNotNull(attribute, id);
				Assertions.assertEquals(RadiusAttributeValues.REPLY_MESSAGE, attribute.getAttributeType(), id);
				Assertions.assertTrue(newPINPattern.matcher(attribute.getAttributeValue()).matches(),  "New PIN");
			}
			{
				String id = "State";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(attribute);
				Assertions.assertEquals(RadiusAttributeValues.STATE, attribute.getAttributeType(), id);
			}
		}
		{
			RadiusPacket packet = getPacket("newPIN.cap", 0x192);
			Assertions.assertEquals(RadiusPacket.ACCESS_REQUEST, packet.getPacketType(), "Access-Request");
			{
				String id = "NAS-IP-Address";
				RadiusAttribute attribute = packet.getAttribute(id);
				Assertions.assertNotNull(attribute, id);
				Assertions.assertEquals(RadiusAttributeValues.NAS_IP_ADDRESS, attribute.getAttributeType(), id);
				Assertions.assertArrayEquals(new byte[]{10,1,58,20}, attribute.getAttributeData(), id);
			}
			{
				String id = "NAS-Port";
				RadiusAttribute attribute = packet.getAttribute(id);
				Assertions.assertNotNull(attribute, id);
				Assertions.assertEquals(RadiusAttributeValues.NAS_PORT, attribute.getAttributeType(), id);
				Assertions.assertEquals("2", attribute.getAttributeValue(), id);
			}
			{
				String id = "NAS-Port-Type";
				RadiusAttribute attribute = packet.getAttribute(id);
				Assertions.assertNotNull(attribute, id);
				Assertions.assertEquals(RadiusAttributeValues.NAS_PORT_TYPE, attribute.getAttributeType(), id);
				Assertions.assertEquals("Virtual", attribute.getAttributeValue(), id);
			}
			{
				String id = "User-Name";
				RadiusAttribute attribute = packet.getAttribute(id);
				Assertions.assertNotNull(attribute, id);
				Assertions.assertEquals(RadiusAttributeValues.USER_NAME, attribute.getAttributeType(), id);
				Assertions.assertEquals("upso", attribute.getAttributeValue(), id);
			}
			{
				String id = "Calling-Station-Id";
				RadiusAttribute attribute = packet.getAttribute(id);
				Assertions.assertNotNull(attribute, id);
				Assertions.assertEquals(RadiusAttributeValues.CALLING_STATION_ID, attribute.getAttributeType(), id);
				Assertions.assertEquals("10.1.58.68", attribute.getAttributeValue(), id);
			}
			{
				String id = "User-Password";
				RadiusAttribute attribute = packet.getAttribute(id);
				Assertions.assertNotNull(attribute, id);
				Assertions.assertEquals(RadiusAttributeValues.USER_PASSWORD, attribute.getAttributeType(), id);
			}
		}
		{
			RadiusPacket packet = getPacket("newPIN.cap", 0x22a);
			Assertions.assertEquals(RadiusPacket.ACCESS_CHALLENGE, packet.getPacketType(), "Access-Challenge");
			{
				String id = "Reply-Message";
				RadiusAttribute attribute = packet.getAttribute(id);
				Assertions.assertNotNull(attribute, id);
				Assertions.assertEquals(RadiusAttributeValues.REPLY_MESSAGE, attribute.getAttributeType(), id);
				Assertions.assertTrue(confirmPINPattern.matcher(attribute.getAttributeValue()).matches(),  "Confirm PIN");
			}
			{
				String id = "State";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(attribute);
				Assertions.assertEquals(RadiusAttributeValues.STATE, attribute.getAttributeType(), id);
			}
		}
		{
			RadiusPacket packet = getPacket("newPIN.cap", 0x2b2);
			Assertions.assertEquals(RadiusPacket.ACCESS_REQUEST, packet.getPacketType(), "Access-Request");
			{
				String id = "NAS-IP-Address";
				RadiusAttribute attribute = packet.getAttribute(id);
				Assertions.assertNotNull(attribute, id);
				Assertions.assertEquals(RadiusAttributeValues.NAS_IP_ADDRESS, attribute.getAttributeType(), id);
				Assertions.assertArrayEquals(new byte[]{10,1,58,20}, attribute.getAttributeData(), id);
			}
			{
				String id = "NAS-Port";
				RadiusAttribute attribute = packet.getAttribute(id);
				Assertions.assertNotNull(attribute, id);
				Assertions.assertEquals(RadiusAttributeValues.NAS_PORT, attribute.getAttributeType(), id);
				Assertions.assertEquals("2", attribute.getAttributeValue(), id);
			}
			{
				String id = "NAS-Port-Type";
				RadiusAttribute attribute = packet.getAttribute(id);
				Assertions.assertNotNull(attribute, id);
				Assertions.assertEquals(RadiusAttributeValues.NAS_PORT_TYPE, attribute.getAttributeType(), id);
				Assertions.assertEquals("Virtual", attribute.getAttributeValue(), id);
			}
			{
				String id = "User-Name";
				RadiusAttribute attribute = packet.getAttribute(id);
				Assertions.assertNotNull(attribute, id);
				Assertions.assertEquals(RadiusAttributeValues.USER_NAME, attribute.getAttributeType(), id);
				Assertions.assertEquals("upso", attribute.getAttributeValue(), id);
			}
			{
				String id = "Calling-Station-Id";
				RadiusAttribute attribute = packet.getAttribute(id);
				Assertions.assertNotNull(attribute, id);
				Assertions.assertEquals(RadiusAttributeValues.CALLING_STATION_ID, attribute.getAttributeType(), id);
				Assertions.assertEquals("10.1.58.68", attribute.getAttributeValue(), id);
			}
			{
				String id = "User-Password";
				RadiusAttribute attribute = packet.getAttribute(id);
				Assertions.assertNotNull(attribute, id);
				Assertions.assertEquals(RadiusAttributeValues.USER_PASSWORD, attribute.getAttributeType(), id);
			}
		}
		{
			RadiusPacket packet = getPacket("newPIN.cap", 0x34a);
			Assertions.assertEquals(RadiusPacket.ACCESS_CHALLENGE, packet.getPacketType(), "Access-Challenge");
			{
				String id = "Reply-Message";
				RadiusAttribute attribute = packet.getAttribute(id);
				Assertions.assertNotNull(attribute, id);
				Assertions.assertEquals(RadiusAttributeValues.REPLY_MESSAGE, attribute.getAttributeType(), id);
				Assertions.assertTrue(waitPattern.matcher(attribute.getAttributeValue()).matches(),  "Wait");
			}
			{
				String id = "State";
				RadiusAttribute attribute = packet.getAttribute(id);
				assertNotNull(attribute);
				Assertions.assertEquals(RadiusAttributeValues.STATE, attribute.getAttributeType(), id);
			}
		}
		{
			RadiusPacket packet = getPacket("newPIN.cap", 0x40a);
			Assertions.assertEquals(RadiusPacket.ACCESS_REQUEST, packet.getPacketType(), "Access-Request");
			{
				String id = "NAS-IP-Address";
				RadiusAttribute attribute = packet.getAttribute(id);
				Assertions.assertNotNull(attribute, id);
				Assertions.assertEquals(RadiusAttributeValues.NAS_IP_ADDRESS, attribute.getAttributeType(), id);
				Assertions.assertArrayEquals(new byte[]{10,1,58,20}, attribute.getAttributeData(), id);
			}
			{
				String id = "NAS-Port";
				RadiusAttribute attribute = packet.getAttribute(id);
				Assertions.assertNotNull(attribute, id);
				Assertions.assertEquals(RadiusAttributeValues.NAS_PORT, attribute.getAttributeType(), id);
				Assertions.assertEquals("2", attribute.getAttributeValue(), id);
			}
			{
				String id = "NAS-Port-Type";
				RadiusAttribute attribute = packet.getAttribute(id);
				Assertions.assertNotNull(attribute, id);
				Assertions.assertEquals(RadiusAttributeValues.NAS_PORT_TYPE, attribute.getAttributeType(), id);
				Assertions.assertEquals("Virtual", attribute.getAttributeValue(), id);
			}
			{
				String id = "User-Name";
				RadiusAttribute attribute = packet.getAttribute(id);
				Assertions.assertNotNull(attribute, id);
				Assertions.assertEquals(RadiusAttributeValues.USER_NAME, attribute.getAttributeType(), id);
				Assertions.assertEquals("upso", attribute.getAttributeValue(), id);
			}
			{
				String id = "Calling-Station-Id";
				RadiusAttribute attribute = packet.getAttribute(id);
				Assertions.assertNotNull(attribute, id);
				Assertions.assertEquals(RadiusAttributeValues.CALLING_STATION_ID, attribute.getAttributeType(), id);
				Assertions.assertEquals("10.1.58.68", attribute.getAttributeValue(), id);
			}
			{
				String id = "User-Password";
				RadiusAttribute attribute = packet.getAttribute(id);
				Assertions.assertNotNull(attribute, id);
				Assertions.assertEquals(RadiusAttributeValues.USER_PASSWORD, attribute.getAttributeType(), id);
			}
		}
		{
			RadiusPacket packet = getPacket("newPIN.cap", 0x4a2);
			Assertions.assertEquals(RadiusPacket.ACCESS_ACCEPT, packet.getPacketType(), "Access-Accept");
		}
	}
	

	@Test
	public void testAccessChallengeNextToken() throws IOException, RadiusException{
		RadiusPacket packet = getPacket("Access-Challenge-nextToken.cap", 0xc4);
		Assertions.assertEquals(RadiusPacket.ACCESS_CHALLENGE, packet.getPacketType(), "Access-Challenge");
		RadiusAttribute replyMessage = packet.getAttribute("Reply-Message");
		assertNotNull(replyMessage);
		Assertions.assertEquals(RadiusAttributeValues.REPLY_MESSAGE, replyMessage.getAttributeType(), "Reply-Message");
		Assertions.assertTrue(nextTokenPattern.matcher(replyMessage.getAttributeValue()).matches(),  "Next Token");
		RadiusAttribute state = packet.getAttribute("State");
		assertNotNull(state);
		Assertions.assertEquals(RadiusAttributeValues.STATE, state.getAttributeType(), "State");		
	}

}
