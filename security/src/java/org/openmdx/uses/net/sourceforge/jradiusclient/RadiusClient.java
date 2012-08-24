/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: Java Radius Client Derivate
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * Copyright (C) 2004-2010  OMEX AG
 *
 * * This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 * * This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Lesser General Public License for more details.
 *
 * * You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * 
 * This library BASED on Java Radius Client 2.0.0
 * (http://http://jradius-client.sourceforge.net/),
 * but its namespace and content has been MODIFIED by OMEX AG
 * in order to integrate it into the openMDX framework.
 */
package org.openmdx.uses.net.sourceforge.jradiusclient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.ByteArrayFormatter;
import org.openmdx.kernel.text.format.DatagramPacketFormatter;
import org.openmdx.kernel.text.format.IndentingFormatter;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.RadiusException;

/**
 * Released under the LGPL<BR>
 *
 * This class provides basic functionality required to implement a NAS as
 * defined by the RADIUS protocol as specified in RFC 2865 and RFC 2866.
 * This implementation is stateless and not thread safe, i.e. since the
 * user name could be changed by the current thread or any other thread,
 * it is difficult to ensure that the responseAttributes correlate to the
 * request we think we are dealing with. It is up to the user of this class
 * to ensure these things at this point. A future release may change this class
 * to a stateful, threadsafe object, but it works for now. Users of this class
 * must also manage building their own request attributes and submitting them with
 * their call to authenticate. For example a programmer using this library, wanting
 * to do chap authentication needs to generate the random challenge, send it to
 *  the user, who generates the MD5 of
 * <UL><LI>a self generated CHAP identifier (a byte)</LI>
 * <LI>their password</LI>
 * <LI>and the CHAP challenge.</LI></UL>(see RFC 2865 section 2.2) The user
 * software returns the CHAP Identifier and the MD5 result and the programmer using RadiusClient
 * sets that as the CHAP Password. The programmer also sets the CHAP-Challenge attribute and
 * sends that to the Radius Server for authentication.
 *
 * <BR>Special Thanks to the original creator of the "RadiusClient"
 * <a href="http://augiesoft.com/java/radius/">August Mueller </a>
 * http://augiesoft.com/java/radius/ and to
 * <a href="http://sourceforge.net/projects/jradius-client">Aziz Abouchi</a>
 * for laying the groundwork for the development of this class.
 *
 * @author <a href="mailto:bloihl@users.sourceforge.net">Robert J. Loihl</a>
 */
public class RadiusClient
    extends AbstractRadiusClient
	implements RadiusConnection, MultiLineStringRepresentation
{
    private static byte [] NAS_ID;
    private static final int AUTH_LOOP_COUNT = 3;
    private static final int ACCT_LOOP_COUNT = 3;
    private static final int DEFAULT_AUTH_PORT = 1812;
    private static final int DEFAULT_ACCT_PORT = 1813;
    public static final int DEFAULT_SOCKET_TIMEOUT = 6000;
    private byte[] sharedSecret = null;
    private InetAddress[] hostname = null;
    private int authenticationPort[] = null;
    private int accountingPort[] = null;
    private DatagramSocket socket = null;
    private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;
    private MessageDigest md5MessageDigest;
    private final byte [] NAS_IP;
    private boolean valid;
    public static final String ENCODING = "UTF-8";
    private int authenticationRetries = AUTH_LOOP_COUNT;
    private int accountingRetries = ACCT_LOOP_COUNT;
    
    /*
     * Static Initializer
     */
    static {
        try{
            NAS_ID = InetAddress.getLocalHost().getHostName().getBytes(ENCODING);	
        }catch (UnknownHostException exception){
            //If this happens the host has no IP address, what can we do???
            //everything will be fouled up anyway!!
            throw new RuntimeException("Local host could not be determined: " + exception.getMessage());
        } catch (UnsupportedEncodingException exception) {
            throw new RuntimeException("Unsupported encoding \"" + ENCODING + "\": " + exception.getMessage());
        }
    }
    /**
     * Constructor - uses the default port 1812 for authentication and 1813 for accounting
     * @param hostname java.lang.String
     * @param sharedSecret java.lang.String
     * @exception java.net.SocketException If we could not create the necessary socket
     * @exception java.security.NoSuchAlgorithmException If we could not get an
     *                              instance of the MD5 algorithm.
     * @exception org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException If an invalid hostname
     *                              (null or empty string), an invalid port
     *                              (port < 0 or port > 65536) or an invalid
     *                              shared secret (null, shared secret can be
     *                              empty string) is passed in.
     */
    public RadiusClient(String hostname, String sharedSecret)
    throws RadiusException, InvalidParameterException{
        this(hostname, DEFAULT_AUTH_PORT, DEFAULT_ACCT_PORT, sharedSecret, DEFAULT_SOCKET_TIMEOUT);
    }
    /**
     * Constructor allows the user to specify an alternate port for the radius server
     * @param hostname java.lang.String
     * @param authPort int the port to use for authentication requests
     * @param acctPort int the port to use for accounting requests
     * @param sharedSecret java.lang.String
     * @exception java.net.SocketException If we could not create the necessary socket
     * @exception java.security.NoSuchAlgorithmException If we could not get an
     *                              instance of the MD5 algorithm.
     * @exception org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException If an invalid hostname
     *                              (null or empty string), an invalid
     *                              port ( port < 0 or port > 65536)
     *                              or an invalid shared secret (null, shared
     *                              secret can be empty string) is passed in.
     */
    public RadiusClient(String hostname, int authPort, int acctPort, String sharedSecret)
    throws RadiusException, InvalidParameterException{
        this(hostname, authPort, acctPort, sharedSecret, DEFAULT_SOCKET_TIMEOUT);
    }
    /**
     * Constructor allows the user to specify an alternate port for the radius server
     * @param hostname java.lang.String
     * @param authPort int the port to use for authentication requests
     * @param acctPort int the port to use for accounting requests
     * @param sharedSecret java.lang.String
     * @param timeout int the timeout to use when waiting for return packets can't be neg and shouldn't be zero
     * @exception org.openmdx.uses.net.sourceforge.jradiusclient.exception.RadiusException If we could not create the necessary socket,
     * If we could not get an instance of the MD5 algorithm, or the hostname did not pass validation
     * @exception org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException If an invalid hostname
     *                              (null or empty string), an invalid
     *                              port ( port < 0 or port > 65536)
     *                              or an invalid shared secret (null, shared
     *                              secret can be empty string) is passed in.
     */
    public RadiusClient(String hostname, int authPort, int acctPort, String sharedSecret, int sockTimeout)
    throws RadiusException, InvalidParameterException{
        this(
        	new String[]{hostname}, 
        	new int[]{authPort}, 
        	new int[]{acctPort}, 
        	sharedSecret, 
        	new BigDecimal(BigInteger.valueOf(sockTimeout), 3), 
        	Logger.getLogger(AbstractRadiusClient.DEFAULT_LOGGER_NAME), 
        	false, 
        	null
        );
    }

    /**
     * Constructor
     * 
     * @param hostname
     * @param authPort
     * @param acctPort
     * @param sharedSecret
     * @param sockTimeout
     * @param logger
     * @param trace
     * @param nasAddress
     * 
     * @throws RadiusException
     * @throws InvalidParameterException
     */
    public RadiusClient(
        String[] hostname, 
        int[] authPort, 
        int[] acctPort, 
        String sharedSecret, 
        BigDecimal sockTimeout, 
        Logger logger, 
        boolean trace, 
        InetAddress nasAddress
    ) throws RadiusException, InvalidParameterException{
        super(logger, trace);
        this.setProvider(hostname, authPort, acctPort);
        if(hostname.length > 1) {
        	this.authenticationRetries = hostname.length;
        	this.accountingRetries = hostname.length;
        }
        this.setSharedSecret(sharedSecret);
        //set up the socket for this client
        try{
            this.socket = new DatagramSocket();
        }catch(SocketException sex){
            throw new RadiusException(sex);
        }
        this.setTimeout(
        	sockTimeout == null ? RadiusClient.DEFAULT_SOCKET_TIMEOUT : sockTimeout.scaleByPowerOfTen(3).intValue()
        );
        //set up the md5 engine
        try{
	        this.md5MessageDigest = MessageDigest.getInstance("MD5");
        }catch(NoSuchAlgorithmException nsaex){
            throw new RadiusException(nsaex);
        }
        this.NAS_IP = nasAddress == null ? null : nasAddress.getAddress();
        this.valid = true;
        logInfo("Radius client instance #{0} created");
    }
    
    /**
     * This method performs the job of authenticating the given <code>RadiusPacket</code> against
     * the radius server.
     *
     * @param RadiusPacket containing all of the <code>RadiusAttributes</code> for this request. This
     * <code>RadiusPacket</code> must include the USER_NAME attribute and be of type ACCEES_REQUEST.
     * If the USER_PASSWORD attribute is set it must contain the plaintext bytes, we will encode the
     * plaintext to send to the server with a REVERSIBLE algorithm. We will set the NAS_IDENTIFIER
     * Attribute, so even if it is set in the RadiusPacket we will overwrite it
     *
     * @return RadiusPacket containing the response attributes for this request
     * @exception org.openmdx.uses.net.sourceforge.jradiusclient.exception.RadiusException
     * @exception org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException
     */
    public RadiusPacket authenticate(RadiusPacket accessRequest)
    throws RadiusException, InvalidParameterException {
        return this.authenticate(accessRequest, this.authenticationRetries);
    }
    /**
     * This method performs the job of authenticating the given <code>RadiusPacket</code> against
     * the radius server.
     *
     * @param RadiusPacket containing all of the <code>RadiusAttributes</code> for this request. This
     * <code>RadiusPacket</code> must include the USER_NAME attribute and be of type ACCEES_REQUEST.
     * If the USER_PASSWORD attribute is set it must contain the plaintext bytes, we will encode the
     * plaintext to send to the server with a REVERSIBLE algorithm. We will set the NAS_IDENTIFIER
     * Attribute, so even if it is set in the RadiusPacket we will overwrite it
     * @param int retries must be zero or greater, if it is zero default value of 3 will be used
     *
     * @return RadiusPacket containing the response attributes for this request
     * @exception org.openmdx.uses.net.sourceforge.jradiusclient.exception.RadiusException
     * @exception org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException
     */
    public RadiusPacket authenticate(RadiusPacket accessRequest, int retries)
    throws RadiusException, InvalidParameterException {
        if(null == accessRequest){
            throw new InvalidParameterException("accessRequest parameter cannot be null");
        }
        if(retries < 0){
            throw new InvalidParameterException("retries must be zero or greater!");
        }else if (retries == 0){
            retries = RadiusClient.AUTH_LOOP_COUNT;
        }
        byte code = accessRequest.getPacketType();
        if(code != RadiusPacket.ACCESS_REQUEST){  //1 byte: code
            throw new InvalidParameterException("Invalid packet type submitted to authenticate");
        }
        byte identifier = accessRequest.getPacketIdentifier();  //1 byte: Identifier can be anything, so should not be constant

        //16 bytes: Request Authenticator
        byte [] requestAuthenticator = this.makeRFC2865RequestAuthenticator();

        // USER_NAME should be set as an attribute already
        //USER_PASSWORD may or may not be set
        try{
            byte [] userPass = accessRequest.getAttribute(RadiusAttributeValues.USER_PASSWORD).getValue();
            if(userPass.length > 0){//otherwise we don't add it to the Attributes
                byte [] encryptedPass = this.encodePapPassword(userPass, requestAuthenticator);
                //(encryptPass gives ArrayIndexOutOfBoundsException if password is of zero length)
                accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.USER_PASSWORD, encryptedPass));
            }
        }catch(RadiusException rex){
            //only thrown if there isn't a matching attribute justifiable to ignore
            //user needs to make sure he builds RadiusPackets correctly
        }        
        if(this.NAS_IP == null ){ // Set the NAS-Identifier
	        accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.NAS_IDENTIFIER, RadiusClient.NAS_ID));
        } else {
            accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.NAS_IP_ADDRESS, this.NAS_IP));
        }
        // Length of Packet is computed as follows, 20 bytes (corresponding to
        // length of code + Identifier + Length + Request Authenticator) +
        // each attribute has a length computed as follows: 1 byte for the type +
        // 1 byte for the length of the attribute + length of attribute bytes
        byte[] requestAttributes = accessRequest.getAttributeBytes(
            new int[]{
                RadiusAttributeValues.NAS_IP_ADDRESS,
                RadiusAttributeValues.NAS_PORT_TYPE,
                RadiusAttributeValues.USER_NAME,
                RadiusAttributeValues.USER_PASSWORD
            }
        );
        short length = (short) (RadiusPacket.RADIUS_HEADER_LENGTH + requestAttributes.length );
        // now send the request and receive the response
        return this.sendReceivePacket(
            code, 
            identifier, 
            length, 
            requestAuthenticator, 
            requestAttributes, 
            retries, 
            accessRequest.getSocketIndex(), 
            this.getHostname(), 
            this.getAuthPort()
        );
    }
    /**
      * This method performs the job of sending accounting information for the
      * current user to the radius accounting server.
      * @param requestPacket Any  request attributes to add to the accounting packet.
      * @return RadiusPacket a packet containing the response from the Radius server
      * @exception org.openmdx.uses.net.sourceforge.jradiusclient.exception.RadiusException
      * @exception org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException
      */
    public RadiusPacket account(RadiusPacket requestPacket)
            throws InvalidParameterException, RadiusException{

        if(null == requestPacket){
            throw new InvalidParameterException("requestPacket parameter cannot be null");
        }
        byte code = requestPacket.getPacketType();
        if(code != RadiusPacket.ACCOUNTING_REQUEST){
            throw new InvalidParameterException("Invalid type passed in for RadiusPacket");
        }
        //make sure the folllowing attributes are set
        try{
            requestPacket.getAttribute(RadiusAttributeValues.USER_NAME);
            requestPacket.getAttribute(RadiusAttributeValues.ACCT_STATUS_TYPE);
            requestPacket.getAttribute(RadiusAttributeValues.ACCT_SESSION_ID);
            requestPacket.getAttribute(RadiusAttributeValues.SERVICE_TYPE);
        }catch(RadiusException rex){
            throw new InvalidParameterException("Missing RadiusAttribute in Accounting RequestPacket: "+ rex.getMessage());
        }
        byte identifier = requestPacket.getPacketIdentifier();//RadiusClient.getNextIdentifier();
        if(this.NAS_IP == null){
	        requestPacket.setAttribute(new RadiusAttribute(RadiusAttributeValues.NAS_IDENTIFIER, RadiusClient.NAS_ID));
        } else {
	        requestPacket.setAttribute(new RadiusAttribute(RadiusAttributeValues.NAS_IP_ADDRESS, this.NAS_IP));
        }

        // Length of Packet is computed as follows, 20 bytes (corresponding to
        // length of code + Identifier + Length + Request Authenticator) +
        // each attribute has a length computed as follows: 1 byte for the type +
        // 1 byte for the length of the attribute + length of attribute bytes
        byte[] requestAttributes = requestPacket.getAttributeBytes();
        short length = (short) (RadiusPacket.RADIUS_HEADER_LENGTH + requestAttributes.length);
        byte[] requestAuthenticator =
            this.makeRFC2866RequestAuthenticator(code, identifier, length, requestAttributes);

        //send the request / recieve the response
        RadiusPacket responsePacket = this.sendReceivePacket(
            code, 
            identifier, 
            length, 
            requestAuthenticator, 
            requestAttributes, 
            this.accountingRetries, 
            requestPacket.getSocketIndex(), 
            this.getHostname(), 
            this.getAcctPort()
        );
        if (RadiusPacket.ACCOUNTING_RESPONSE != responsePacket.getPacketType()) {
            //how did we get here!! the Radius Server sent us a non-accounting response!
            throw new RadiusException("The radius Server responded with an incorrect response type.");
        }
        return responsePacket;
    }
    /**
     * This method encodes the plaintext user password according to RFC 2865
     * @param userPass java.lang.String the password to encrypt
     * @param requestAuthenticator byte[] the requestAuthenicator to use in the encryption
     * @return byte[] the byte array containing the encrypted password
     */
    private byte [] encodePapPassword(final byte[] userPass, final byte [] requestAuthenticator) {
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

    /**
     * This method builds a Request Authenticator for use in outgoing RADIUS
     * Access-Request packets as specified in RFC 2865.
     * @return byte[]
     */
    private byte[] makeRFC2865RequestAuthenticator() {
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
    /**
     * This method builds a Response Authenticator for use in validating
     * responses from the RADIUS Authentication process as specified in RFC 2865.
     * The byte array returned should match exactly the response authenticator
     * recieved in the response packet.
     * @param code byte
     * @param identifier byte
     * @param length short
     * @param requestAuthenticator byte[]
     * @param responseAttributeBytes byte[]
     * @return byte[]
     */
    private byte[] makeRFC2865ResponseAuthenticator(byte code,
                                                byte identifier,
                                                short length,
                                                byte [] requestAuthenticator,
                                                byte[] responseAttributeBytes) {
        this.md5MessageDigest.reset();

        this.md5MessageDigest.update(code);
        this.md5MessageDigest.update(identifier);
        this.md5MessageDigest.update((byte)(length >> 8));
        this.md5MessageDigest.update((byte)(length & 0xff));
        this.md5MessageDigest.update(requestAuthenticator, 0, requestAuthenticator.length);
        this.md5MessageDigest.update(responseAttributeBytes, 0, responseAttributeBytes.length);
        this.md5MessageDigest.update(this.sharedSecret);

        return this.md5MessageDigest.digest();
    }
    /**
     * This method builds a Request Authenticator for use in RADIUS Accounting
     * packets as specified in RFC 2866.
     * @param code byte
     * @param identifier byte
     * @param length short
     * @param requestAttributes byte[]
     * @return byte[]
     */
    private byte[] makeRFC2866RequestAuthenticator(byte code,
                                                    byte identifier,
                                                    short length,
                                                    byte[] requestAttributes) {
        byte [] requestAuthenticator = new byte [16];

        for (int i = 0; i < 16; i++) {
                requestAuthenticator[i] = 0;
        }
        this.md5MessageDigest.reset();

        this.md5MessageDigest.update(code);
        this.md5MessageDigest.update(identifier);
        this.md5MessageDigest.update((byte)(length >> 8));
        this.md5MessageDigest.update((byte)(length & 0xff));
        this.md5MessageDigest.update(requestAuthenticator, 0, requestAuthenticator.length);
        this.md5MessageDigest.update(requestAttributes, 0, requestAttributes.length);
        this.md5MessageDigest.update(this.sharedSecret);

        return this.md5MessageDigest.digest();
    }

    private void setProvider(
        String[] hostname,
        int[] authPort,
        int[] acctPort
    ) throws InvalidParameterException{
        List<String> exceptions = new ArrayList<String>();
        List<InetAddress> hostnames = new ArrayList<InetAddress>();
        List<Integer> acctPorts = new ArrayList<Integer>();
        List<Integer> authPorts = new ArrayList<Integer>();
        if (hostname == null){
        	exceptions.add("Hostname array can not be null");
        } else if (authPort == null || acctPort == null) {
        	exceptions.add("Port array can not be null");
        } else if (hostname.length == 0) {
        	exceptions.add("Hostname array can't be empty");
        } else if (hostname.length != authPort.length || hostname.length != acctPort.length) {
        	exceptions.add("Hostname and port arrays must have the same length");
        }
        if(exceptions.isEmpty()) {
	        for(
	            int i = 0;
	            i < hostname.length;
	            i++
	        ){ 
	        	int exceptionCount = exceptions.size();
	            if (authPort[i] < 0 || authPort[i] > 65535) exceptions.add(
            		"authorizationPort[" + i + "] out of range: " + authPort[i]
		        );
    	    	if (acctPort[i] < 0 || acctPort[i] > 65535) exceptions.add(
	                "accountingPort[" + i + "] out of range: " + acctPort[i]
	            );
	            if (null == hostname[i]){
	            	exceptions.add("Hostname[" + i + "] is null");
	            } else if ("".equals(hostname[i].trim())){
	            	exceptions.add("Hostname[" + i + "] is empty or blank");
	            }
	            if(exceptionCount == exceptions.size()) try{
	            	hostnames.add(InetAddress.getByName(hostname[i]));
	            	authPorts.add(Integer.valueOf(authPort[i]));
	            	acctPorts.add(Integer.valueOf(acctPort[i]));
	            } catch(java.net.UnknownHostException exception){
	            	exceptions.add("Hostname[" + i + "] could not be resolved: " + hostname[i]);
	            }
	        }
    		for(
    			Iterator<String> e = exceptions.iterator();
    			e.hasNext();
    		){
    			super.logWarning("Radius Client #{0}: {1}", e.next());
    		}
    		int acceptable = hostnames.size();
	        if(acceptable == 0) {
	        	logSevere("Radius Client #{0}: None of the host configurations was acceptable");
	        	throw new InvalidParameterException(
	        		"None of the host configurations was acceptable: " + exceptions
	        	);
	        }
	        this.hostname = new InetAddress[acceptable];
	        this.authenticationPort = new int[acceptable];
	        this.accountingPort = new int[acceptable];
	        for(int i = 0; i < acceptable; i++) {
	        	this.hostname[i] = (InetAddress) hostnames.get(i);
	        	this.authenticationPort[i] = ((Number)authPorts.get(i)).intValue();
	        	this.accountingPort[i] = ((Number)acctPorts.get(i)).intValue();
	        }
    	}
    }
    
    /**
     * Get the host names
     * 
     * @return the host names
     */
    public InetAddress[] getHostname() {
        return this.hostname;
    }
    /**
     * This method returns the current port to be used for authentication
     * @return int
     */
    public int[] getAuthPort(){
        return this.authenticationPort;
    }
    /**
     * This method returns the current port to be used for accounting
     * @return int
     */
    public int[] getAcctPort(){
        return this.accountingPort;
    }

    /**
     * This method returns the current secret value that the Radius Client
     * shares with the RADIUS Server.
     * @return 
     */
    protected byte[] getSharedSecret() {
        return this.sharedSecret;
    }
    /**
     * This method sets the secret value that the Radius Client shares with the
     * RADIUS Server.
     * @param sharedSecret java.lang.String
     * @exception org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException If the shared secret is null,
     *                          or the empty string
     */
    private void setSharedSecret(String sharedSecret) throws InvalidParameterException {
        if (sharedSecret == null){
            throw new InvalidParameterException("Shared secret can not be null!");
        }else if (sharedSecret.equals("")){//we don't trim() the string here because the rfc (RFC 2865)
                                            //states the shared secret can't be empty string
                                            // but doesn't exclude an all blank string
            throw new InvalidParameterException("Shared secret can not be an empty string!");
        }else try { //everything is a-ok
            this.sharedSecret = sharedSecret.getBytes(ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding \"" + ENCODING + "\": " + e.getMessage());
        }
    }
    /**
     * This method returns the current timeout period on a recieve of a response
     * from the RADIUS Server.
     * @return int
     */
    public int getTimeout() {
        return this.socketTimeout;
    }
    /**
     * This method sets the timeout period on a recieve of a response from the
     * RADIUS Server.
     * @param socket_timeout int a positive timeout value
     * @exception org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException If the timeout value is
     *                          less than 0. a 0 value for timeout means that the
     *                          request will block until a response is recieved,
     *                          which is not recommended due to the nature of RADIUS
     *                          (i.e. RADIUS server may be silently dropping your
     *                          packets and never sending a response)
     */
    private void setTimeout(int socket_timeout) throws InvalidParameterException {
        if (socket_timeout < 0){
            throw new InvalidParameterException("A negative timeout value is not allowed!");
        }else{//everything is a-ok
            this.socketTimeout = socket_timeout;
        try{
        if(null == this.socket) {//prevent NPE
            this.socket = new DatagramSocket();
        }
        this.socket.setSoTimeout(this.socketTimeout);
        }catch(SocketException sex){}
        }
    }
    /**
     * @param packet java.net.DatagramPacket
     * @param requestIdentifier byte
     * @param requestAuthenticator byte[]
     * @param socketIndex 
     * @return int the code value from the radius response packet
     * @exception org.openmdx.uses.net.sourceforge.jradiusclient.exception.RadiusException
     * @exception java.io.IOException
     */
    private RadiusPacket checkRadiusPacket(
        DatagramPacket packet,
        byte requestIdentifier,
        byte[] requestAuthenticator, 
        int socketIndex
    ) throws RadiusException{
        ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());
        DataInputStream input = new DataInputStream(bais);
        try{
            int returnCode = -1;
            /* int packetLength = */ packet.getLength();
            byte code = input.readByte();
            returnCode = code & 0xff;
            //now check the identifiers to see if they match
            byte identifierByte = input.readByte();
            //int identifier = identifierByte & 0xff;//don't need this
            if (identifierByte != requestIdentifier){
                //wrong packet asshole!
                throw new RadiusException("The RADIUS Server returned the wrong Identifier.");
            }
            //read the length
            int intLength = input.readShort();
            short length = (short)(intLength & 0xffff);
            //now check the response authenticator to validate the packet
            byte [] responseAuthenticator = new byte[16];
            input.readFully(responseAuthenticator);
            //get the attributes as a byte[]
            byte[] responseAttributeBytes = new byte[length - RadiusPacket.RADIUS_HEADER_LENGTH];
            input.readFully(responseAttributeBytes);
            byte [] myResponseAuthenticator =
                this.makeRFC2865ResponseAuthenticator(code, identifierByte, length,
                                                    requestAuthenticator, responseAttributeBytes);
            //now compare them
            if((responseAuthenticator.length != 16) ||
                (myResponseAuthenticator.length != 16)){
                //wrong authenticator length asshole!
                throw new RadiusException("Authenticator length is incorrect.");
            }else{
                for (int i = 0; i<responseAuthenticator.length;i++){
                    if (responseAuthenticator[i] != myResponseAuthenticator[i]){
                        logWarning(
                        	"Radius Client #{0}: Response Authenticator Mismatch (Identifier={1})\n{2}\n{3}",
                        	identifierByte,
                        	new ByteArrayFormatter(
                                myResponseAuthenticator, 0, myResponseAuthenticator.length,
                                "Calculated Response Authenticator"
                            ),
                            new ByteArrayFormatter(
                                responseAuthenticator, 0, responseAuthenticator.length,
                                "Received Response Authenticator"
                            )
                        );
                        throw new RadiusException("Authenticators do not match, response packet not validated!");
                    }
                }
            }

            RadiusPacket responsePacket = new RadiusPacket(returnCode,identifierByte, socketIndex);
            //now parse out the responseAttributeBytes into the responseAttributes hashtable
            int attributesLength = responseAttributeBytes.length;
            if (attributesLength > 0){
                int attributeType;
                int attributeLength;
                byte[] attributeValue;
                DataInputStream attributeInput = new DataInputStream(new ByteArrayInputStream(responseAttributeBytes));

                for (int left=0; left < attributesLength; ){
                    attributeType = (attributeInput.readByte() & 0xff);
                    attributeLength = attributeInput.readByte() & 0xff;
                    attributeValue = new byte[attributeLength - 2];
                    attributeInput.read(attributeValue, 0, attributeLength - 2);
                    responsePacket.setAttribute(new RadiusAttribute(attributeType, attributeValue));
                    left += attributeLength;
                }
                attributeInput.close();
            }            
            return responsePacket;
        }catch(IOException ioex){
            throw new RadiusException(ioex);
        }catch(InvalidParameterException ipex){
            throw new RadiusException(ipex, "Invalid response attributes sent back from server.");
        } finally{
            try{
	            input.close();
            } catch(
            	IOException ignore
            ){
            	// Ignore close exception
            }
        }
    }
    /**
     * This method builds a Radius packet for transmission to the Radius Server
     * @param byte code
     * @param byte identifier
     * @param short length
     * @param byte[] requestAuthenticator
     * @param byte[] requestAttributes
     * @exception java.net.UnknownHostException
     * @exception java.io.IOException
     */
    private DatagramPacket composeRadiusPacket(byte code, byte identifier,
                                                short length,
                                                byte[] requestAuthenticator,
                                                byte[] requestAttributes)
    throws RadiusException{
        ByteArrayOutputStream baos 	= new ByteArrayOutputStream();
        DataOutputStream output 	= new DataOutputStream(baos);
        DatagramPacket packet_out 	= null;

        try{
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
        }catch(IOException ioex){
            throw new RadiusException(ioex);
        }
        //won't get here in the case of an exception so we won't return return null or a malformed packet
        return packet_out;
    }
    /**
     * This method sends the outgoing packet and recieves the incoming response
     * @param code 
     * @param identifier 
     * @param length 
     * @param requestAuthenticator 
     * @param requestAttributes 
     * @param p 
     * @param hostname 
     * @param port 
     * @param retryCount int Number of retries we will allow
     * @return the reply
     * @exception java.io.IOException if there is a problem sending or recieving the packet, i.e recieve timeout
     */
    private RadiusPacket sendReceivePacket(
        byte code, 
        byte identifier, 
        short length, 
        byte[] requestAuthenticator, 
        byte[] requestAttributes, 
        int retry, 
        int provider, 
        InetAddress[] hostname, 
        int[] port
    ) throws RadiusException{
    	try {
	        DatagramPacket packet_out =
	            this.composeRadiusPacket(code, identifier, length, requestAuthenticator, requestAttributes);
	
	        if (packet_out.getLength() > RadiusPacket.MAX_PACKET_LENGTH){
	            throw new RadiusException("Packet too big!");
	        }else if (packet_out.getLength() < RadiusPacket.MIN_PACKET_LENGTH){
	            throw new RadiusException("Packet too short !");
	        }else{
	            DatagramPacket packet_in =
	                    new DatagramPacket(new byte[RadiusPacket.MAX_PACKET_LENGTH],
	                                                    RadiusPacket.MAX_PACKET_LENGTH);
	            IOException ioException = null;
	            for (int i = 0; i < retry; i++) { 
	            	try{
		                int p = provider < 0 ? i % hostname.length : provider;
		                packet_out.setAddress(hostname[p]);
		                packet_out.setPort(port[p]);
		            	if(i == 0) {
		            		logDebug(
		            			"RadiusClient #{0}: Send\n{1}",
		            			new DatagramPacketFormatter(packet_out)
		            		);
		            	} else {
		            		logDebug(
		            			"RadiusClient #{0}: Retry {1}\n{2}",
		            			i,
		            			new DatagramPacketFormatter(packet_out)
		            		);
		            	}
		                this.socket.send(packet_out);
		                this.socket.receive(packet_in);
		                logDebug(
		                    "RadiusClient #{0}: Receive\n{1}",
		                    new DatagramPacketFormatter(packet_in)
		                );
		                return this.checkRadiusPacket(packet_in, identifier, requestAuthenticator, provider);
		            }catch (IOException exception){
		                ioException = exception;
		            }
	            }
	            throw new RadiusException(ioException);
	        }
    	} catch (RadiusException exception) {
    		this.valid = false;
    		throw exception;
    	}
    }
    
    /**
     * This method returns a string representation of this
     * <code>RadiusClient</code>.
     *
     * @return a string representation of this object.
     */
    @Override
    public String toString(){
        return getClass().getName() + ": " + IndentingFormatter.toString(
        	ArraysExtension.asMap(
    			new String[]{
			        "HostName",
			        "AuthenticationPort",
			        "AccountingPort"
			    },
	            new Object[]{
    			    this.getHostname(),
    			    this.getAuthPort(),
    			    this.getAcctPort()
	            }            
	         )
        );
    }

    /**
     * Compares the specified Object with this <code>RadiusClient</code>
     * for equality.  Returns true if the given object is also a
     * <code>RadiusClient</code> and the two RadiusClient
     * have the same host, port, sharedSecret & username.
     * @param object Object to be compared for equality with this
     *		<code>RadiusClient</code>.
     *
     * @return true if the specified Object is equal to this
     *		<code>RadiusClient</code>.
     */
    @Override
    public boolean equals(Object object){
        if (object == null){
            return false;
        }
        if (this == object){
            return true;
        }
        if (!(object instanceof RadiusClient)){
            return false;
        }
        RadiusClient that = (RadiusClient)object;
        return 
            Arrays.equals(this.getHostname(), that.getHostname()) &&
            Arrays.equals(this.getAuthPort(), that.getAuthPort()) &&
            Arrays.equals(this.getAcctPort(), that.getAcctPort()) &&
            this.getSharedSecret().equals(that.getSharedSecret());
    }
    /**
     * @return int the hashCode for this <code>RadiusClient</code>
     */
    @Override
    public int hashCode(){
        InetAddress[] x1 = this.getHostname();
        int[] x2 = this.getAcctPort();
        int[] x3 = this.getAuthPort();
        int h = this.getSharedSecret().hashCode();
        for(
            int i = 0; 
            i < x1.length; 
            i++
        ) {
            h=31*h+x1[i].hashCode();
            h=31*h+x2[i];
            h=31*h+x3[i];
        }
        return h;
    }
    
    /* (non-Javadoc)
     * @see java.io.Closeable#close()
     */
//  @Override
    public void close() throws IOException {
        this.socket.close();
    }

    /**
     * overrides finalize to close socket and then normal finalize on super class
     */
    @Override
    public void finalize(
    ) throws Throwable{
        this.close();
        super.finalize();
    }

    /**
     * We should not reuse radius clients with send/receive failures
     * 
     * @return <code>true</code> unless there was a send/receive failure
     */
    public boolean isValid(){
    	if(!this.valid){
	        logInfo("Radius client instance #{0} is invalid");
    	}
    	return this.valid;
    }
    
}
