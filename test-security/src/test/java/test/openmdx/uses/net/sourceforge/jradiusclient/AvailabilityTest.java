/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Availability Test
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package test.openmdx.uses.net.sourceforge.jradiusclient;

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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusAttribute;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusAttributeValues;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusPacket;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.RadiusException;

/**
 * Availability Test
 */
public class AvailabilityTest {

	protected DatagramSocket socket; 
    private MessageDigest md5MessageDigest;
    protected String localHostName;
    private byte[] sharedSecret;

	@BeforeEach
	public void setUp(
	) throws SocketException, UnsupportedEncodingException, UnknownHostException, NoSuchAlgorithmException{
		this.socket = new DatagramSocket();
		this.socket.setSoTimeout(1500);
		this.localHostName = InetAddress.getLocalHost().getHostName();
		this.md5MessageDigest = MessageDigest.getInstance("MD5");
		this.sharedSecret = "testing123".getBytes("UTF-8");
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
				System.out.println("Sending request to " + target);
				this.socket.send(outgoingPacket);
				System.out.println("Receiving reply from " + target);
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
					Assertions.fail("Timeout expected for " + target);
				} catch (SocketTimeoutException ignore) {
					System.out.println(target + " timed out as expected: " + ignore.getMessage());
				}
				break;
			case UNKNOWN_HOST:	
				try {
					outgoingPacket.setAddress(
						InetAddress.getByName(hostName)
					);
					Assertions.fail("Unknown host expected for " + target);
				} catch (UnknownHostException ignore) {
					System.out.println(target + " is unknwown as expected: " + ignore.getMessage());
				}
				break;
		}
		System.out.println(target + " tested");
		System.out.println();
	}
	
	@Test
	public void testTimeout() throws InvalidParameterException, RadiusException, IOException{
		sendReceive("switch.ch", 1812, Expected.TIMEOUT);
		sendReceive("222.33.44.55", 1812, Expected.TIMEOUT);
	}

	@Disabled("RADIUS server is usually not running")
	@Test
	public void testRadiusAvailable() throws InvalidParameterException, RadiusException, IOException{
		sendReceive(this.localHostName, 1812, Expected.REPLY);
		sendReceive("127.0.0.1", 1812, Expected.REPLY);
		sendReceive("localhost", 1812, Expected.REPLY);
	}

	@Test
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
        byte[] requestAuthenticator = makeRFC2865RequestAuthenticator();
        accessRequest.setAttribute(
    		new RadiusAttribute(
    			RadiusAttributeValues.USER_PASSWORD,
    			this.encodePapPassword(
					"secret".getBytes(),
					requestAuthenticator
    			)
    		)
        );
        accessRequest.setAttribute(
        	new RadiusAttribute(RadiusAttributeValues.NAS_IDENTIFIER, this.localHostName.getBytes())
        );
        byte[] requestAttributes = accessRequest.getAttributeBytes(
            RadiusAttributeValues.NAS_IP_ADDRESS,
            RadiusAttributeValues.NAS_PORT_TYPE,
            RadiusAttributeValues.USER_NAME,
            RadiusAttributeValues.USER_PASSWORD
        );
        return composeRadiusPacket(
        	accessRequest.getPacketType(),
        	accessRequest.getPacketIdentifier(),
        	(short) (RadiusPacket.RADIUS_HEADER_LENGTH + requestAttributes.length),
        	requestAuthenticator,
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
        try(
            ByteArrayOutputStream baos 	= new ByteArrayOutputStream();
            DataOutputStream output 	= new DataOutputStream(baos);
        ){
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
    
            final DatagramPacket packet_out = new DatagramPacket(new byte[length], length);
            packet_out.setLength(length);
    
            packet_out.setData(baos.toByteArray());
            //won't get here in the case of an exception so we won't return return null or a malformed packet
            return packet_out;
        }
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
        this.md5MessageDigest.update(this.sharedSecret);
        this.md5MessageDigest.update(requestAuthenticator);
        return this.md5MessageDigest.digest();
    }

    private static enum Expected {
    	REPLY,
    	TIMEOUT,
    	UNKNOWN_HOST
    }

    /**
     * This method encodes the plaintext user password according to RFC 2865
     * @param userPass java.lang.String the password to encrypt
     * @param requestAuthenticator byte[] the requestAuthenicator to use in the encryption
     * @return byte[] the byte array containing the encrypted password
     */
    private byte [] encodePapPassword(
    	final byte[] userPass, 
    	final byte [] requestAuthenticator
    ) {
        // encrypt the password.
        byte[] userPassBytes = null;
        //the password must be a multiple of 16 bytes and less than or equal
        //to 128 bytes. If it isn't a multiple of 16 bytes fill it out with zeroes
        //to make it a multiple of 16 bytes. If it is greater than 128 bytes
        //truncate it at 128

        if (userPass.length > 128){
            userPassBytes = new byte[128];
            System.arraycopy(userPass,0,userPassBytes,0,128);
        }else {
            userPassBytes = userPass;
        }
        // declare the byte array to hold the final product
        byte[] encryptedPass = null;

        if (userPassBytes.length < 128) {
            if (userPassBytes.length % 16 == 0) {
                // It is already a multiple of 16 bytes
                encryptedPass = new byte[userPassBytes.length];
            } else {
                // Make it a multiple of 16 bytes
                encryptedPass = new byte[((userPassBytes.length / 16) * 16) + 16];
            }
        } else {
            // the encrypted password must be between 16 and 128 bytes
            encryptedPass = new byte[128];
        }

        // copy the userPass into the encrypted pass and then fill it out with zeroes
        System.arraycopy(userPassBytes, 0, encryptedPass, 0, userPassBytes.length);
        for(int i = userPassBytes.length; i < encryptedPass.length; i++) {
            encryptedPass[i] = 0;  //fill it out with zeroes
        }

        this.md5MessageDigest.reset();
        // add the shared secret
        this.md5MessageDigest.update(this.sharedSecret);
        // add the  Request Authenticator.
        this.md5MessageDigest.update(requestAuthenticator);
        // get the md5 hash( b1 = MD5(S + RA) ).
        byte bn[] = this.md5MessageDigest.digest();

        for (int i = 0; i < 16; i++){
            // perform the XOR as specified by RFC 2865.
            encryptedPass[i] = (byte)(bn[i] ^ encryptedPass[i]);
        }

        if (encryptedPass.length > 16){
            for (int i = 16; i < encryptedPass.length; i+=16){
                this.md5MessageDigest.reset();
                // add the shared secret
                this.md5MessageDigest.update(this.sharedSecret);
                //add the previous(encrypted) 16 bytes of the user password
                this.md5MessageDigest.update(encryptedPass, i - 16, 16);
                // get the md5 hash( bn = MD5(S + c(i-1)) ).
                bn = this.md5MessageDigest.digest();
                for (int j = 0; j < 16; j++) {
                    // perform the XOR as specified by RFC 2865.
                    encryptedPass[i+j] = (byte)(bn[j] ^ encryptedPass[i+j]);
                }
            }
        }
        return encryptedPass;
    }

}
