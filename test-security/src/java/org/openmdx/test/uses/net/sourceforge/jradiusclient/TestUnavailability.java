/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestUnavailability.java,v 1.1 2009/03/11 16:32:33 hburger Exp $
 * Description: Test Unavailability
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/11 16:32:33 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.test.uses.net.sourceforge.jradiusclient;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusAttribute;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusAttributeValues;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusPacket;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.RadiusException;

/**
 * Test Unavailability
 */
public class TestUnavailability {

	protected DatagramSocket socket; 
    private MessageDigest md5MessageDigest;
    protected String localHostName;

	@org.junit.Before
	public void setUp(
	) throws SocketException, UnsupportedEncodingException, UnknownHostException, NoSuchAlgorithmException{
		this.socket = new DatagramSocket();
		this.socket.setSoTimeout(500);
		this.localHostName = InetAddress.getLocalHost().getHostName();
		this.md5MessageDigest = MessageDigest.getInstance("MD5");
	}
	
	protected void sendReceive(
		String hostName,
		int port
	) throws InvalidParameterException, RadiusException, IOException{
		sendReceive(hostName, port, Expected.REPLY);
	}

	/**
	 * Send/receive
	 * 
	 * @param hostName
	 * @param port
	 * @param expected
	 * 
	 * @throws InvalidParameterException
	 * @throws RadiusException
	 * @throws IOException
	 */
	protected void sendReceive(
		String hostName,
		int port, 
		Expected expected
	) throws InvalidParameterException, RadiusException, IOException{		
		String target = hostName + ":" + port;
		System.out.println("Testing " + target + "...");
		DatagramPacket outgoingPacket = newOutgoingPacket();
		DatagramPacket incomingPacket = newIncomingPacket();
		switch(expected) {
			case REPLY:	
				outgoingPacket.setAddress(
					InetAddress.getByName(hostName)
				);
				outgoingPacket.setPort(port);
				System.out.println("Sending to " + target);
				this.socket.send(outgoingPacket);
				System.out.println("Receiving from to " + target);
				this.socket.receive(incomingPacket);
				System.out.println("Received reply from " + target);
				break;
			case TIMEOUT: 
				outgoingPacket.setAddress(
					InetAddress.getByName(hostName)
				);
				outgoingPacket.setPort(port);
				try {
					System.out.println("Sending to " + target);
					this.socket.send(outgoingPacket);
					System.out.println("Receiving from to " + target);
					this.socket.receive(incomingPacket);
					org.junit.Assert.fail("Timeout expected for " + target);
				} catch (SocketTimeoutException ignore) {
					System.out.println(target + " timed out as expected: " + ignore.getMessage());
				}
				break;
			case UNKNOWN_HOST:	
				try {
					outgoingPacket.setAddress(
						InetAddress.getByName(hostName)
					);
					org.junit.Assert.fail("Unknown host expected for " + target);
				} catch (UnknownHostException ignore) {
					System.out.println(target + " is unknwown as expected: " + ignore.getMessage());
				}
				break;
		}
		System.out.println(target + " tested");
		System.out.println();
	}
	
	@org.junit.Test
	public void testTimeout() throws InvalidParameterException, RadiusException, IOException{
		sendReceive("switch.ch", 1812, Expected.TIMEOUT);
		sendReceive("222.33.44.55", 1812, Expected.TIMEOUT);
	}

	@org.junit.Test
	public void testRadiusAvailable() throws InvalidParameterException, RadiusException, IOException{
		sendReceive(this.localHostName, 1812, Expected.REPLY);
		sendReceive("10.1.1.175", 1812, Expected.REPLY);
	}

	@org.junit.Test
	public void testUnknownHost() throws InvalidParameterException, RadiusException, IOException{
		sendReceive("unknown.test", 1812, Expected.UNKNOWN_HOST);
	}

	
	protected DatagramPacket newIncomingPacket(
	){
        return new DatagramPacket(
        	new byte[RadiusPacket.MAX_PACKET_LENGTH],
            RadiusPacket.MAX_PACKET_LENGTH
        );		
	}
	
	protected DatagramPacket newOutgoingPacket(
	) throws InvalidParameterException, IOException, RadiusException {
        RadiusPacket accessRequest = new RadiusPacket(RadiusPacket.ACCESS_REQUEST);
        accessRequest.setAttribute(
    		new RadiusAttribute(RadiusAttributeValues.USER_NAME,"John Dow".getBytes())
        );
        accessRequest.setAttribute(
    		new RadiusAttribute(RadiusAttributeValues.USER_PASSWORD,"secret".getBytes())
        );
        accessRequest.setAttribute(
        	new RadiusAttribute(RadiusAttributeValues.NAS_IDENTIFIER, this.localHostName.getBytes())
        );
        byte[] requestAttributes = accessRequest.getAttributeBytes(
            new int[]{
                RadiusAttributeValues.NAS_IP_ADDRESS,
                RadiusAttributeValues.NAS_PORT_TYPE,
                RadiusAttributeValues.USER_NAME,
                RadiusAttributeValues.USER_PASSWORD
            }
        );
        return composeRadiusPacket(
        	accessRequest.getPacketType(),
        	accessRequest.getPacketIdentifier(),
        	(short) (RadiusPacket.RADIUS_HEADER_LENGTH + requestAttributes.length),
        	makeRFC2865RequestAuthenticator(),
        	requestAttributes
        );
	}

	/**
     * This method builds a Radius packet for transmission to the Radius Server
     * @param byte code
     * @param byte identifier
     * @param short length
     * @param byte[] requestAuthenticator
     * @param byte[] requestAttributes
	 * @throws IOException 
     * @exception java.net.UnknownHostException
     * @exception java.io.IOException
     */
    private static DatagramPacket composeRadiusPacket(byte code, byte identifier,
                                                short length,
                                                byte[] requestAuthenticator,
                                                byte[] requestAttributes)
    throws IOException{
        ByteArrayOutputStream baos 	= new ByteArrayOutputStream();
        DataOutputStream output 	= new DataOutputStream(baos);
        DatagramPacket packet_out 	= null;
        //1 byte: Code
        output.writeByte(code);
        //1 byte: identifier
        output.writeByte(identifier);
        //2 byte: Length
        output.writeShort(length);
        //16 bytes: Request Authenticator
        //only write 16 of them if there are more, which there better not be
        output.write(requestAuthenticator, 0, 16);

        output.write(requestAttributes, 0, requestAttributes.length);

        packet_out = new DatagramPacket(new byte[length], length);
        packet_out.setLength(length);

        packet_out.setData(baos.toByteArray());
        output.close();
        baos.close();
        //won't get here in the case of an exception so we won't return return null or a malformed packet
        return packet_out;
    }
    /**
     * This method builds a Request Authenticator for use in outgoing RADIUS
     * Access-Request packets as specified in RFC 2865.
     * @return byte[]
     * @throws UnsupportedEncodingException 
     */
    private byte[] makeRFC2865RequestAuthenticator() throws UnsupportedEncodingException {
        byte [] requestAuthenticator = new byte [16];

        Random r = new Random();

        for (int i = 0; i < 16; i++)
        {
            requestAuthenticator[i] = (byte) r.nextInt();
        }
        this.md5MessageDigest.reset();
        this.md5MessageDigest.update("WinRadius".getBytes("UTF-8"));
        this.md5MessageDigest.update(requestAuthenticator);
        return this.md5MessageDigest.digest();
    }

    private static enum Expected {
    	REPLY,
    	TIMEOUT,
    	UNKNOWN_HOST
    }

}
