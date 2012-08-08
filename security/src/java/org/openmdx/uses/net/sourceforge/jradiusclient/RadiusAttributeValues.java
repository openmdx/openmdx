/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RadiusAttributeValues.java,v 1.2 2004/10/14 19:28:22 hburger Exp $
 * Description: Java Radius Client Derivate
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/10/14 19:28:22 $
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

/**
 * Released under the LGPL<BR>
 *
 * Special Thanks to the original creator of the "RadiusClient"
 * <a href="http://augiesoft.com/java/radius/">August Mueller </a>
 * http://augiesoft.com/java/radius/ and to
 * <a href="http://sourceforge.net/projects/jradius-client">Aziz Abouchi</a>
 * for laying the groundwork for the development of this class.
 *
 * @author <a href="mailto:bloihl@users.sourceforge.net">Robert J. Loihl</a>
 * @version $Revision: 1.2 $
 */
public final class RadiusAttributeValues
{
/* ******************  Constant Attribute Types  *************************/
    public static final int USER_NAME       		= 1;
    public static final int USER_PASSWORD   		= 2;
    public static final int CHAP_PASSWORD			= 3;
    public static final int NAS_IP_ADDRESS			= 4;
    public static final int NAS_PORT			    = 5;
    public static final int SERVICE_TYPE			= 6;
    public static final int FRAMED_PROTOCOL			= 7;
    public static final int FRAMED_IP_ADDRESS		= 8;
    public static final int FRAMED_IP_NETMASK		= 9;
    public static final int FRAMED_ROUTING			= 10;
    public static final int FILTER_ID			    = 11;
    public static final int FRAMED_MTU			    = 12;
    public static final int FRAMED_COMPRESSION		= 13;
    public static final int LOGIN_IP_HOST			= 14;
    public static final int LOGIN_SERVICE			= 15;
    public static final int LOGIN_TCP_PORT			= 16;
    //17      (unassigned)
    public static final int REPLY_MESSAGE			= 18;
    public static final int CALLBACK_NUMBER			= 19;
    public static final int CALLBACK_ID			    = 20;
    //21      (unassigned)
    public static final int FRAMED_ROUTE			= 22;
    public static final int FRAMED_IPX_NETWORK		= 23;
    public static final int STATE				    = 24;
    public static final int CLASS				    = 25;
    public static final int VENDOR_SPECIFIC			= 26;
    public static final int SESSION_TIMEOUT			= 27;
    public static final int IDLE_TIMEOUT			= 28;
    public static final int TERMINATION_ACTION		= 29;
    public static final int CALLED_STATION_ID		= 30;
    public static final int CALLING_STATION_ID		= 31;
    public static final int NAS_IDENTIFIER			= 32;
    public static final int PROXY_STATE			    = 33;
    public static final int LOGIN_LAT_SERVICE		= 34;
    public static final int LOGIN_LAT_NODE			= 35;
    public static final int LOGIN_LAT_GROUP			= 36;
    public static final int FRAMED_APPLETALK_LINK		= 37;
    public static final int FRAMED_APPLETALK_NETWORK	= 38;
    public static final int FRAMED_APPLETALK_ZONE		= 39;

//40_59   (reserved for accounting)
    public static final int ACCT_STATUS_TYPE		= 40;
    public static final int ACCT_DELAY_TIME			= 41;
    public static final int ACCT_INPUT_OCTETS		= 42;
    public static final int ACCT_OUTPUT_OCTETS		= 43;
    public static final int ACCT_SESSION_ID		    = 44;
    public static final int ACCT_AUTHENTIC			= 45;
    public static final int ACCT_SESSION_TIME		= 46;
    public static final int ACCT_INPUT_PACKETS		= 47;
    public static final int ACCT_OUTPUT_PACKETS		= 48;
    public static final int ACCT_TERMINATE_CAUSE	= 49;
    public static final int ACCT_MULTI_SESSION_ID	= 50;
    public static final int ACCT_LINK_COUNT		    = 51;
    public static final int ACCT_INPUT_GIGAWORDS	= 52;
    public static final int ACCT_OUTPUT_GIGAWORDS	= 53;
    public static final int EVENT_TIMESTAMP		    = 55;

    public static final int CHAP_CHALLENGE		= 60;
    public static final int NAS_PORT_TYPE		= 61;
    public static final int PORT_LIMIT			= 62;
    public static final int LOGIN_LAT_PORT		= 63;

    public static final int ARAP_PASSWORD		= 70;
    public static final int ARAP_FEATURES		= 71;
    public static final int ARAP_ZONE_ACCESS	= 72;
    public static final int ARAP_SECURITY		= 73;
    public static final int ARAP_SECURITY_DATA	= 74;
    public static final int PASSWORD_RETRY		= 75;
    public static final int PROMPT				= 76;
    public static final int CONNECT_INFO		= 77;
    public static final int CONFIGURATION_TOKEN	= 78;
    public static final int EAP_MESSAGE			= 79;
    public static final int MESSAGE_AUTHENTICATOR		= 80;
    public static final int ARAP_CHALLENGE_RESPONSE	    = 84;
    public static final int ACCT_INTERIM_INTERVAL		= 85;
    public static final int NAS_PORT_ID			= 87;
    public static final int FRAMED_POOL			= 88;
/* *******************  Constant Attribute Types  **************************/

/* ******************  Constant Attribute Values  *************************/
// Service-Type or User Types
    public static final int LOGIN				=  1;
    public static final int FRAMED				=  2;
    public static final int CALLBACK_LOGIN		=  3;
    public static final int CALLBACK_FRAMED		=  4;
    public static final int OUTBOUND			=  5;
    public static final int ADMINISTRATIVE		=  6;
    public static final int NAS_PROMPT			=  7;
    public static final int AUTHENTICATE_ONLY	=  8;
    public static final int CALLBACK_NAS_PROMPT	=  9;
    public static final int Call_CHECK			= 10;
    public static final int CALLBACK_ADMINISTRATIVE	= 11;

// Framed-Protocol
    public static final int PPP					= 1;
    public static final int SLIP				= 2;
    public static final int ARAP				= 3;
    public static final int GANDALF_SLML		= 4;
    public static final int XYLOGICS_PROPRIETARY_IPX_SLIP	= 5;
    public static final int X75_SYNCHRONOUS		= 6;

// Framed-Routing
    public static final int NONE		        = 0;
    public static final int BROADCAST		    = 1;
    public static final int LISTEN			    = 2;
    public static final int BROADCAST_LISTEN	= 3;

// Framed-Compression
//public static final int None					= 0;
    public static final int VJ_TCP_IP_HEADER_COMPRESSION	= 1;
    public static final int IPX_HEADER_COMPRESSION		    = 2;
    public static final int STAC_LZS_COMPRESSION			= 3;

// Login-Service
    public static final int TELNET			= 0;
    public static final int RLOGIN			= 1;
    public static final int TCP_CLEAR		= 2;
    public static final int PORTMASTER		= 3;
    public static final int LAT			    = 4;
    public static final int X25_PAD		    = 5;
    public static final int X25_T3POS		= 7;
    public static final int TCP_CLEAR_QUIET	= 8;

// Termination-Action
    public static final int DEFAULT		    = 0;
    public static final int RADIUS_REQUEST	= 1;

// NAS-PORT-TYPE
    public static final int ASYNC			=  0;
    public static final int SYNC			=  1;
    public static final int ISDN_SYNC		=  2;
    public static final int ISDN_ASYNC_V120	=  3;
    public static final int ISDN_ASYNC_V110	=  4;
    public static final int VIRTUAL			=  5;
    public static final int PIAFS			=  6;
    public static final int HDLC_CLEAR_CHANNEL	=  7;
    public static final int X25			    =  8;
    public static final int X75			    =  9;
    public static final int G3_FAX			= 10;
    public static final int SDSL			= 11;
    public static final int ADSL_CAP		= 12;
    public static final int ADSL_DMT		= 13;
    public static final int IDSL			= 14;
    public static final int ETHERNET		= 15;
    public static final int XDSL			= 16;
    public static final int CABLE			= 17;
    public static final int WIRELESS_OTHER	= 18;
    public static final int WIRELESS_IEEE_802_11	= 19;

/* ******************    Constant Attribute Values  *************************/
/* *****************  Attributes and sub attributes for SIP ***************/

    // SIP DIGEST AUTH - draft-sterman-aaa-sip-00
    public static final int DIGEST_RESPONSE         = 206;
    public static final int DIGEST_ATTRIBUTE        = 207;


    // SIP DIGEST AUTH - draft-sterman-aaa-sip-00
    public static final int SIP_REALM                   = 1;
    public static final int SIP_NONCE                   = 2;
    public static final int SIP_METHOD                  = 3;
    public static final int SIP_URI                     = 4;
    public static final int SIP_QOP                     = 5;
    public static final int SIP_ALGORITHM               = 6;
    public static final int SIP_BODY_DIGEST             = 7;
    public static final int SIP_CNONCE                  = 8;
    public static final int SIP_NONCE_COUNT             = 9;
    public static final int SIP_USER_NAME               = 10;
   /* *****************  Attributes and sub attributes for SIP ***************/
   /* ***************** HELPER METHODS ********************** */
   //public static String getTypeName(final int type){
   //}
}
