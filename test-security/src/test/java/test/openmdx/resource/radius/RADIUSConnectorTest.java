/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: RADIUS Connector Test
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010-2022, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package test.openmdx.resource.radius;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmdx.kernel.lightweight.naming.NonManagedInitialContextFactoryBuilder;
import org.openmdx.resource.cci.ConnectionFactory;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusConnection;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusPacket;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.RadiusException;
import org.openmdx.uses.net.sourceforge.jradiusclient.packets.PapAccessRequest;

/**
 * LDAP Test
 */
@Disabled("RADIUS server is usually not running")
public class RADIUSConnectorTest {

	/**
	 * 
	 */
	@BeforeAll
	public static synchronized void setUp(
	) throws Exception {  
		if(!NamingManager.hasInitialContextFactoryBuilder()) {
			Map<String,String> resources = new HashMap<String,String>();
			resources.put(
				"org.openmdx.comp.env.radius.free",
				"eis:org.openmdx.resource.radius.client.ManagedConnectionFactory?" +
				"ConnectionURL=aaa:\\/\\/localhost:1812;transport=udp;protocol=radius&" +
				"Password=testing123&" +
				"SocketTimeout=1.5&" + 
				"Trace=(java.lang.Boolean)true"
			);
			NonManagedInitialContextFactoryBuilder.install(resources);
		}
    }

	/**
	 * Use the local host's free radius server
	 * 
	 * @throws Exception
	 */
	@Test
	public void freeTest(
	) throws Exception {
		run("free");
	}

	/**
	 * Run a specific test
	 * 
	 * @param name
	 * 
	 * @throws NamingException 
	 * @throws RadiusException 
	 * @throws IOException 
	 * @throws InvalidParameterException 
	 */
	@SuppressWarnings("unchecked")
	private void run(
		String name
	) throws NamingException, RadiusException, IOException, InvalidParameterException{
		System.out.println("Test '" + name + "' started");
		ConnectionFactory<RadiusConnection,RadiusException> radiusConnectionFactory = (ConnectionFactory<RadiusConnection,RadiusException>) new InitialContext(
		).lookup(
			"java:comp/env/radius/" + name
		); 
		for(int i = 0; i < 3; i++) {
			try (RadiusConnection radiusConnection = radiusConnectionFactory.getConnection()) {
				Assertions.assertEquals(
					RadiusPacket.ACCESS_ACCEPT,
					radiusConnection.authenticate(newAccessRequest()).getPacketType()
				);
			}
		}
		System.out.println("Test '" + name + "' completed");
	}
		
	private RadiusPacket newAccessRequest(
	) throws InvalidParameterException{
		return new PapAccessRequest(
			"Benutzer", // "John Dow",
			"0000123456" // "secret"
		);
	}
	
}
