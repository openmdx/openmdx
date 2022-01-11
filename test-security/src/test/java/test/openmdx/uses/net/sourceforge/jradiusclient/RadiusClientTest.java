/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Java Radius Client Derivate
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package test.openmdx.uses.net.sourceforge.jradiusclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import org.openmdx.uses.gnu.getopt.Getopt;
import org.openmdx.uses.gnu.getopt.LongOpt;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusAttribute;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusAttributeValues;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusClient;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusPacket;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.RadiusException;
import org.openmdx.uses.net.sourceforge.jradiusclient.packets.AccountingRequest;
import org.openmdx.uses.net.sourceforge.jradiusclient.packets.ChapAccessRequest;
import org.openmdx.uses.net.sourceforge.jradiusclient.packets.PapAccessRequest;
import org.openmdx.uses.net.sourceforge.jradiusclient.util.ChapUtil;

/**
 * @author <a href="mailto:bloihl@users.sourceforge.net">Robert J. Loihl</a>
 */
public class RadiusClientTest{
    public static String getUsage(){
        return "usage: " + 
        		RadiusClientTest.class.getSimpleName() +
        		" -s RadiusServer -S sharedSecret [--authPort=1812] [--acctPort=1813]";
    }

    public static void main(String [] args)
    {
        int authport = 1812 ;
        int acctport = 1813;
        String host = "localhost",sharedSecret = null;
        StringBuffer portSb = new StringBuffer();
        LongOpt[] longOpts = {new LongOpt("authPort",LongOpt.REQUIRED_ARGUMENT,portSb,1),
            new LongOpt("acctPort",LongOpt.REQUIRED_ARGUMENT,portSb,2)};
        Getopt gOpt = new Getopt("TestRadiusClient",args,"s:S:",longOpts,false);
        gOpt.setOpterr(true);
        int c;
        while((c = gOpt.getopt()) != -1){
            switch(c){
                case 's':
                    host = gOpt.getOptarg();
                    break;
                case 'S':
                    sharedSecret = gOpt.getOptarg();
                    break;
                case 1:
                    authport = (Integer.parseInt(portSb.toString()));
                    break;
                case 2:
                    acctport = (Integer.parseInt(portSb.toString()));
                    break;
                case '?':
                    break;//getopt already printed an error
                default:
                    System.err.println(getUsage());
            }
        }

        RadiusClient rc = null;
        try{
            rc = new RadiusClient(host, authport,acctport, sharedSecret);
        }catch(RadiusException rex){
            RadiusClientTest.log(rex.getMessage());
            RadiusClientTest.log(getUsage());
            System.exit(4);
        }catch(InvalidParameterException ivpex){
            RadiusClientTest.log("Unable to create Radius Client due to invalid parameter!");
            RadiusClientTest.log(ivpex.getMessage());
            RadiusClientTest.log(getUsage());
            System.exit(5);
        }
        ChapUtil chapUtil = new ChapUtil();
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        basicAuthenticate(rc, chapUtil, inputReader);
        advAuthenticate(rc, chapUtil, inputReader);
    }
    private static void basicAuthenticate(final RadiusClient rc,
            final ChapUtil chapUtil,
            final BufferedReader inputReader){
        try{
            boolean /* attributes = false, */ continueTest = true;
            String userName = null, userPass = null, authMethod = null;
            System.out.println("Performing tests using basic classes: ");
            while(continueTest){
                /* attributes = false; */
                RadiusPacket accessRequest = new RadiusPacket(RadiusPacket.ACCESS_REQUEST);
                RadiusAttribute userNameAttribute;
                //prompt user for input
                System.out.print("Username: ");
                userName = inputReader.readLine();
                userNameAttribute = new RadiusAttribute(RadiusAttributeValues.USER_NAME,userName.getBytes());
                accessRequest.setAttribute(userNameAttribute);
                System.out.print("Password: ");
                userPass = inputReader.readLine();
                System.out.print("Authentication method [PAP | chap]: ");
                authMethod = inputReader.readLine();
                if(authMethod.equalsIgnoreCase("chap")){
                    byte[] chapChallenge = chapUtil.getNextChapChallenge(16);
                    accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.CHAP_PASSWORD,
                            chapEncrypt(userPass, chapChallenge, chapUtil)));

                    accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.CHAP_CHALLENGE,
                            chapChallenge));
                }else{
                    accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.USER_PASSWORD,userPass.getBytes()));
                }
                System.out.print("Additional Attributes? [y|N]:");
                boolean more = (Objects.requireNonNull(inputReader.readLine()).equalsIgnoreCase("y"))?true:false;
                while(more){
                    System.out.print("Attribute Type:");
                    int type = Integer.parseInt(Objects.requireNonNull(inputReader.readLine()));
                    System.out.print("AttributeValue:");
                    byte[] value = Objects.requireNonNull(inputReader.readLine()).getBytes();
                    accessRequest.setAttribute(new RadiusAttribute(type, value));
                    System.out.print("Additional Attributes? [y|N]:");
                    more = (Objects.requireNonNull(inputReader.readLine()).equalsIgnoreCase("y"))?true:false;
                }
                RadiusPacket accessResponse = rc.authenticate(accessRequest);
                switch(accessResponse.getPacketType()){
                    case RadiusPacket.ACCESS_ACCEPT:
                        RadiusClientTest.log("User " + userName + " authenticated");
                        printAttributes(accessResponse);
                        basicAccount(rc,userName);
                        break;
                    case RadiusPacket.ACCESS_REJECT:
                        RadiusClientTest.log("User " + userName + " NOT authenticated");
                        printAttributes(accessResponse);
                        break;
                    case RadiusPacket.ACCESS_CHALLENGE:
                        String reply = new String(accessResponse.getAttribute(RadiusAttributeValues.REPLY_MESSAGE).getValue());
                        RadiusClientTest.log("User " + userName + " Challenged with " + reply);
                        break;
                    default:
                        RadiusClientTest.log("Whoa, what kind of RadiusPacket is this " + accessResponse.getPacketType());
                        break;
                }
                System.out.print("Another Basic Test [ Y | n ]: ");
                authMethod = Objects.requireNonNull(inputReader.readLine());
                if(authMethod.equalsIgnoreCase("n")){
                    continueTest = false;
                }
            }
        }catch(InvalidParameterException ivpex){
            RadiusClientTest.log(ivpex.getMessage());
        }catch(RadiusException rex){
            RadiusClientTest.log(rex.getMessage());
        }catch(IOException ioex){
            RadiusClientTest.log(ioex.getMessage());
        }
    }
    private static byte[] chapEncrypt(final String plainText,
                                      final byte[] chapChallenge,
                                      final ChapUtil chapUtil){
        // see RFC 2865 section 2.2
        byte chapIdentifier = chapUtil.getNextChapIdentifier();
        byte[] chapPassword = new byte[17];
        chapPassword[0] = chapIdentifier;
        System.arraycopy(ChapUtil.chapEncrypt(chapIdentifier, plainText.getBytes(),chapChallenge),
                         0, chapPassword, 1, 16);
        return chapPassword;
    }
    private static void basicAccount(final RadiusClient rc,
                                final String userName)
            throws InvalidParameterException, RadiusException{
        RadiusPacket accountRequest = new RadiusPacket(RadiusPacket.ACCOUNTING_REQUEST);
        accountRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.USER_NAME,userName.getBytes()));
        accountRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.ACCT_STATUS_TYPE,new byte[]{0, 0, 0, 1}));
        accountRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.ACCT_SESSION_ID,("bob").getBytes()));
        accountRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.SERVICE_TYPE,new byte[]{0, 0, 0, 1}));
        RadiusPacket accountResponse = rc.account(accountRequest);
        switch(accountResponse.getPacketType()){
            case RadiusPacket.ACCOUNTING_MESSAGE:
                RadiusClientTest.log("User " + userName + " got ACCOUNTING_MESSAGE response");
                break;
            case RadiusPacket.ACCOUNTING_RESPONSE:
                RadiusClientTest.log("User " + userName + " got ACCOUNTING_RESPONSE response");
                break;
            case RadiusPacket.ACCOUNTING_STATUS:
                RadiusClientTest.log("User " + userName + " got ACCOUNTING_STATUS response");
                break;
            default:
                RadiusClientTest.log("User " + userName + " got invalid response " + accountResponse.getPacketType() );
                break;
        }
        printAttributes(accountResponse);
    }
    private static void advAuthenticate(final RadiusClient rc,
            final ChapUtil chapUtil,
            final BufferedReader inputReader){
        try{
            boolean /* attributes = false, */ continueTest = true;
            String userName = null, userPass = null, authMethod = null;
            System.out.println("Performing tests using advanced classes: ");
            while(continueTest){
                /* attributes = false; */
                RadiusPacket accessRequest = null;
                //prompt user for input
                System.out.print("Username: ");
                userName = inputReader.readLine();
                System.out.print("Password: ");
                userPass = inputReader.readLine();
                System.out.print("Authentication method [PAP | chap]: ");
                authMethod = Objects.requireNonNull(inputReader.readLine());
                if(authMethod.equalsIgnoreCase("chap")){
                    accessRequest = new ChapAccessRequest(userName, userPass);
                }else{
                    accessRequest = new PapAccessRequest(userName,userPass);
                }
                System.out.print("Additional Attributes? [y|N]:");
                boolean more = (Objects.requireNonNull(inputReader.readLine()).equalsIgnoreCase("y"))?true:false;
                while(more){
                    System.out.print("Attribute Type:");
                    int type = Integer.parseInt(Objects.requireNonNull(inputReader.readLine()));
                    System.out.print("AttributeValue:");
                    byte[] value = Objects.requireNonNull(inputReader.readLine()).getBytes();
                    accessRequest.setAttribute(new RadiusAttribute(type, value));
                    System.out.print("Additional Attributes? [y|N]:");
                    more = (Objects.requireNonNull(inputReader.readLine()).equalsIgnoreCase("y"))?true:false;
                }
                RadiusPacket accessResponse = rc.authenticate(accessRequest);
                switch(accessResponse.getPacketType()){
                    case RadiusPacket.ACCESS_ACCEPT:
                        RadiusClientTest.log("User " + userName + " authenticated");
                        printAttributes(accessResponse);
                        advAccount(rc,userName);
                        break;
                    case RadiusPacket.ACCESS_REJECT:
                        RadiusClientTest.log("User " + userName + " NOT authenticated");
                        printAttributes(accessResponse);
                        break;
                    case RadiusPacket.ACCESS_CHALLENGE:
                        String reply = new String(accessResponse.getAttribute(RadiusAttributeValues.REPLY_MESSAGE).getValue());
                        RadiusClientTest.log("User " + userName + " Challenged with " + reply);
                        break;
                    default:
                        RadiusClientTest.log("Whoa, what kind of RadiusPacket is this " + accessResponse.getPacketType());
                        break;
                }
                System.out.print("Another Advanced Test [ Y | n ]: ");
                authMethod = Objects.requireNonNull(inputReader.readLine());
                if(authMethod.equalsIgnoreCase("n")){
                    continueTest = false;
                }
            }
        }catch(InvalidParameterException ivpex){
            RadiusClientTest.log(ivpex.getMessage());
        }catch(RadiusException rex){
            RadiusClientTest.log(rex.getMessage());
        }catch(IOException ioex){
            RadiusClientTest.log(ioex.getMessage());
        }
    }
    private static void advAccount(final RadiusClient rc,
                                final String userName)
            throws InvalidParameterException, RadiusException{
        RadiusPacket accountRequest = new AccountingRequest(userName, new byte[]{0,0,0,1}, userName);
        RadiusPacket accountResponse = rc.account(accountRequest);
        switch(accountResponse.getPacketType()){
            case RadiusPacket.ACCOUNTING_MESSAGE:
                RadiusClientTest.log("User " + userName + " got ACCOUNTING_MESSAGE response");
                break;
            case RadiusPacket.ACCOUNTING_RESPONSE:
                RadiusClientTest.log("User " + userName + " got ACCOUNTING_RESPONSE response");
                break;
            case RadiusPacket.ACCOUNTING_STATUS:
                RadiusClientTest.log("User " + userName + " got ACCOUNTING_STATUS response");
                break;
            default:
                RadiusClientTest.log("User " + userName + " got invalid response " + accountResponse.getPacketType() );
                break;
        }
        printAttributes(accountResponse);
    }
    private static void printAttributes(RadiusPacket rp){
        System.out.println("Response Packet Attributes");
        System.out.println("\tType\tValue");
        for(RadiusAttribute tempRa : rp.getAttributes()) {
            System.out.println("\t" + tempRa.getType() + "\t" + new String(tempRa.getValue()));
        }
    }
    private static void log(final String message){
        System.out.print  ("TestRadiusClient: ");
        System.out.println(message);
    }
}
