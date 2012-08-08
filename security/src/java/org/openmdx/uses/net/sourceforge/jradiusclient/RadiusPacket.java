/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RadiusPacket.java,v 1.7 2009/02/06 16:43:49 hburger Exp $
 * Description: Java Radius Client Derivate
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/06 16:43:49 $
 * ====================================================================
 *
 * Copyright (C) 2004  OMEX AG
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 * 
 * This library BASED on Java Radius Client 2.0.0
 * (http://http://jradius-client.sourceforge.net/),
 * but it's namespace and content has been MODIFIED by OMEX AG
 * in order to integrate it into the openMDX framework.
 */
package org.openmdx.uses.net.sourceforge.jradiusclient;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.RadiusException;
/**
 * Released under the LGPL<BR>
 * @author <a href="mailto:bloihl@users.sourceforge.net">Robert J. Loihl</a>
 * @version $Revision: 1.7 $
 */
public class RadiusPacket {
    public static final int MIN_PACKET_LENGTH       = 20;
    public static final int MAX_PACKET_LENGTH       = 4096;
    /**
     *RADIUS_HEADER_LENGTH is 20 bytes (corresponding to
     *1 byte for code + 1 byte for Identifier + 2 bytes for Length + 16 bytes for Request Authenticator)
     *It is not a coincidence that it is the same as the MIN_PACKET_LENGTH
     **/
    public static final short RADIUS_HEADER_LENGTH  = 20;
    public static final String EMPTYSTRING = "";

    /* ***************  Constant Packet Type Codes  **************************/
    public static final int ACCESS_REQUEST      = 1;
    public static final int ACCESS_ACCEPT       = 2;
    public static final int ACCESS_REJECT       = 3;
    public static final int ACCOUNTING_REQUEST  = 4;
    public static final int ACCOUNTING_RESPONSE = 5;
    public static final int ACCOUNTING_STATUS   = 6;
    public static final int PASSWORD_REQUEST    = 7;
    public static final int PASSWORD_ACCEPT     = 8;
    public static final int PASSWORD_REJECT     = 9;
    public static final int ACCOUNTING_MESSAGE  = 10;
    public static final int ACCESS_CHALLENGE    = 11;
    public static final int STATUS_SERVER       = 12;   // experimental
    public static final int STATUS_CLIENT       = 13;   // experimental
    public static final int RESERVED            = 255;
    /* ******************  Constant Packet Type Codes  *************************/
    private static Object nextPacketIdLock = new Object();
    private static byte nextPacketId = (byte)0;

    private int packetType = 0;
    private byte packetIdentifier = (byte)0;
    private Map<Integer,RadiusAttribute> attributes;
    private int socketIndex = -1;
    
    private final static List<RadiusAttribute> NO_ATTRIBUTES = Collections.emptyList(); 
    /**
     * builds a type RadiusPacket with no Attributes set
     *
     * @param type int a PacketType to send.
     * @throws InvalidParameterException if the attributeList is null or contains non-RadiusAttribute type entries
     */
    public RadiusPacket(final int type) throws InvalidParameterException{
        this(type, getAndIncrementPacketIdentifier(),new ArrayList<RadiusAttribute>());
    }
    /**
     *
     * @param type
     * @param identifier
     * @throws InvalidParameterException
     */
    public RadiusPacket(final int type, final byte identifier) throws InvalidParameterException{
        this(type, identifier, new ArrayList<RadiusAttribute>());
    }
    /**
     * Constructor
     * 
     * @param type
     * @param identifier
     * @param socketIndex
     * @throws InvalidParameterException
     */
    public RadiusPacket(
        final int type, 
        final byte identifier,
        final int socketIndex
    ) throws InvalidParameterException{
        this(type, identifier, NO_ATTRIBUTES);
        this.setSocketIndex(socketIndex);
    }
    
    /**
     * Builds a RadiusPacket with a predefined set of attributes
     *
     * @param type int a PacketType to send.
     * @param attributeList a list of RadiusAttribute objects to initialize this RadiusPacket with
     * @throws InvalidParameterException if the attributeList is null or contains non-RadiusAttribute type entries
     */
    public RadiusPacket(final int type, final List<RadiusAttribute> attributeList) throws InvalidParameterException{
        this(type, getAndIncrementPacketIdentifier(), attributeList);
    }
    /**
     * Builds a RadiusPacket with a predefined set of attributes
     *
     * @param type
     * @param identifier
     * @param attributeList
     * @throws InvalidParameterException
     */
    public RadiusPacket(final int type, final byte identifier, final List<RadiusAttribute> attributeList) throws InvalidParameterException{
        if((type < 1)||(type > 256)){
            throw new InvalidParameterException("Type was out of bounds");
        }
        if(null == attributeList){
            throw new InvalidParameterException("Attribute List was null");
        }
        this.packetType = type;
        this.packetIdentifier = identifier;
        this.attributes = new HashMap<Integer,RadiusAttribute>();
        this.setAttributes(attributeList);
    }
    /**
     * Adds a RadiusAttribute to this RadiusPacket
     * @param radiusAttribute A RadiusAttribute to set on this RadiusPacket
     * @throws InvalidParameterException if the parameter radiusAttribute was null
     */
    public void setAttribute(RadiusAttribute radiusAttribute) throws InvalidParameterException{
        if (null == radiusAttribute){
            throw new InvalidParameterException("radiusAttribute was null");
        }
        validateAttribute(radiusAttribute);
        synchronized(this.attributes){
            this.attributes.put(new Integer(radiusAttribute.getType()),radiusAttribute);
        }
    }
    /**
     * Add a set of RadiusAttributes to this RadiusPacket
     * @param attributeList a list of RadiusAttribute objects to add to this RadiusPacket
     * @throws InvalidParameterException if the attributeList is null or contains non-RadiusAttribute type entries
     */
    public void setAttributes(final List<RadiusAttribute> attributeList) throws InvalidParameterException{
        if(null == attributeList){
            throw new InvalidParameterException("Attribute List was null");
        }
        for(RadiusAttribute tempRa : attributeList) {
            validateAttribute(tempRa);
            synchronized(this.attributes){
                this.attributes.put(new Integer(tempRa.getType()),tempRa);
            }
        }
    }
    //do nothing method, sub-classes should implement to get attributes validation during
    // the setAttribute and setAttributes methods
    protected void validateAttribute(final RadiusAttribute attribute)
    throws InvalidParameterException{

    }
    /**
     * retrieve a RadiusAttribute from this RadiusPacket
     * @param attributeType an integer between 0 and 256 (i.e. a byte) from the list of Radius constants in
     * net.sourceforge.jradiusclient.RadiusValues
     * @return a single RadiusAttribute from the RadiusPacket
     * @throws RadiusException if no attribute of type attributeType is stored in this RadiusPacket
     * @throws InvalidParameterException if the attributeType is not between 0 and 256 (i.e. a byte)
     */
    public RadiusAttribute getAttribute(int attributeType) throws InvalidParameterException,RadiusException{
        if ((attributeType < 0) || (attributeType > 256)){
            throw new InvalidParameterException("attributeType is out of bounds");
        }
        RadiusAttribute tempRa = null;
        synchronized(this.attributes){
            tempRa = (RadiusAttribute)this.attributes.get(new Integer(attributeType));
        }
        if (null == tempRa){
            throw new RadiusException("No attribute found for type " +  attributeType);
        }
        return tempRa;
    }
    /**
     * get all of the RadiusAttributes in this RadiusPacket
     * @return a java.util.Collection of RadiusAttributes
     */
    public Collection<RadiusAttribute> getAttributes(){
        //I am concerned about handing out a Collection that is backed by the attributes map,
        //i.e. changes to our own internal provate data can happen this way!!!!
        return this.attributes.values();
    }
    /**
     * get the packet type for this RadiusPacket
     * @return packet type for this RadiusPacket
     */
    public byte getPacketType(){
        return (byte)this.packetType;
    }
    /**
     * Return the packetIdentifier for this RadiusPacket. This can be used to match request packets
     * to response packets
     * @return the packet identifier for this object.
     */
    public byte getPacketIdentifier(){
        return this.packetIdentifier;
    }
    /**
     * get the byte array
     * @return a byte array of the raw bytes for all of the RadiusAttributes assigned to this RadiusPacket
     * @throws RadiusException If there is any error assembling the bytes into a byte array
     */
    protected final byte[] getAttributeBytes() throws RadiusException{
        //check for an empty packet
        ByteArrayOutputStream bytes = new  ByteArrayOutputStream();
        synchronized (this.attributes){
        	for(RadiusAttribute attribute : this.attributes.values()) {
                try{
                    bytes.write(attribute.getBytes());
                }catch(java.io.IOException ioex){
                    throw new RadiusException (ioex, "Error writing bytes to ByteArrayOutputStream!!!");
                }
            }
            return bytes.toByteArray();
        }
    }
    /**
     * get the byte array
     * @return a byte array of the raw bytes for all of the RadiusAttributes assigned to this RadiusPacket
     * @throws RadiusException If there is any error assembling the bytes into a byte array
     */
    public final byte[] getAttributeBytes(
        int[] order
    ) throws RadiusException{
        try {
	        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	        Map<Integer,RadiusAttribute> attributes = new HashMap<Integer,RadiusAttribute>(this.attributes);
            for(
                int i = 0;
                i < order.length;
                i++
            ){
                RadiusAttribute candidate = attributes.remove(new Integer(order[i]));
                if(candidate != null) bytes.write(candidate.getBytes()); 
            }
            for (RadiusAttribute attribute : attributes.values()){
                bytes.write(attribute.getBytes());
            }
	        return bytes.toByteArray();
        } catch (java.io.IOException ioex){
            throw new RadiusException (ioex, "Error writing bytes to ByteArrayOutputStream!!!");
        }
    }

    /**
     * retrieves the next PacketIdentifier to use and increments the static storage
     * @return the next packetIdentifier to use.
     */
    private static byte getAndIncrementPacketIdentifier(){
        synchronized (nextPacketIdLock){
            return nextPacketId++;
        }
    }
    
    /**
     * Set the provider's index
     * 
     * @param socketIndex
     */
    protected void setSocketIndex(
        int socketIndex
    ){
        this.socketIndex = socketIndex;
    }

    /**
     * Get the provider's index
     * 
     * @return the provider's index; or -1 if none has been set
     */
    public int getSocketIndex(
    ){
        return this.socketIndex;
    }
    
    //    /**
//     * This method returns the bytes sent in the STATE attribute of the RADIUS
//     * Server's response to a request
//     *@return java.lang.String the challenge message to display to the user
//     *@exception net.sourceforge.jradiusclient.exception.RadiusException
//     */
//    private byte[] getStateAttributeFromResponse() throws RadiusException{
//        if(this.responseAttributes == null){
//            throw new RadiusException("No Response Attributes have been set.");
//        }
//        byte[] stateBytes = (byte[])this.responseAttributes.get(new Integer(RadiusAttributeValues.STATE));
//        if ((stateBytes == null) || (stateBytes.length == 0)){
//            throw new RadiusException("No State Attribute has been set.");
//        }
//        return stateBytes;
//    }
}