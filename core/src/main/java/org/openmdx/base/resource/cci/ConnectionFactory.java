/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Connection Factory
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
package org.openmdx.base.resource.cci;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;

/**
 * Non JNDI dependent EIS Connection Factory 
 **/
public interface ConnectionFactory extends java.io.Serializable {
  
  /** Gets a connection to an EIS instance. This getConnection variant
   *  should be used when a component wants the container to manage EIS 
   *  sign-on. This case is termed container-managed sign-on. The 
   *  component does not pass any security information.
   *
   *
   *  @return   Connection instance
   *  @throws   ResourceException   Failed to get a connection to
   *                                the EIS instance. Examples of
   *                                error cases are:
   *          <UL>
   *          <LI> Invalid configuration of ManagedConnectionFactory--
   *               example: invalid server name
   *          <LI> Application server-internal error--example:
   *               connection pool related error
   *          <LI> Communication error 
   *          <LI> EIS-specific error--example: EIS not active
   *          <LI> Resource adapter-internal error
   *          <LI> Security related error; example: invalid user
   *          <LI> Failure to allocate system resources
   *         </UL>                        
  **/
  public 
  Connection getConnection() throws ResourceException;

  /** Gets a connection to an EIS instance. A component should use 
   *  the getConnection variant with javax.resource.cci.ConnectionSpec
   *  parameter, if it needs to pass any resource adapter specific 
   *  security information and connection parameters. In the component-
   *  managed sign-on case, an application component passes security 
   *  information (example: username, password) through the 
   *  ConnectionSpec instance.
   * 
   *  <p>It is important to note that the properties passed through 
   *  the getConnection method should be client-specific (example: 
   *  username, password, language) and not related to the 
   *  configuration of a target EIS instance (example: port number, 
   *  server name). The ManagedConnectionFactory instance is configured
   *  with complete set of properties required for the creation of a 
   *  connection to an EIS instance. 
   *
   *  @param  properties          Connection parameters and security
   *                              information specified as 
   *                              ConnectionSpec instance
   *  @return Connection instance
   *
   *  @throws ResourceException   Failed to get a connection to
   *                              the EIS instance. Examples of
   *                              error cases are:
   *          <UL>
   *          <LI> Invalid specification of input parameters
   *          <LI> Invalid configuration of ManagedConnectionFactory--
   *               example: invalid server name
   *          <LI> Application server-internal error--example:
   *               connection pool related error
   *          <LI> Communication error 
   *          <LI> EIS-specific error--example: EIS not active
   *          <LI> Resource adapter-internal error
   *          <LI> Security related error; example: invalid user
   *          <LI> Failure to allocate system resources
   *         </UL>                        
   *  @see     javax.resource.cci.ConnectionSpec
  **/
  public 
  Connection getConnection(ConnectionSpec properties) 
                       throws ResourceException;

  /** Gets a RecordFactory instance. The RecordFactory is used for
   *  the creation of generic Record instances.
   *
   *  @return RecordFactory         RecordFactory instance
   *
   *  @throws ResourceException     Failed to create a RecordFactory
   *  @throws NotSupportedException Operation not supported
  **/
  public
  RecordFactory getRecordFactory() throws ResourceException;

  /** Gets metadata for the Resource Adapter. Note that the metadata
   *  information is about the ResourceAdapter and not the EIS instance.
   *  An invocation of this method does not require that an active
   *  connection to an EIS instance should have been established.
   *
   *  @return  ResourceAdapterMetaData instance
   *  @throws   ResourceException Failed to get metadata information 
   *                              about the resource adapter
  **/
  public
  ResourceAdapterMetaData getMetaData() throws ResourceException;

}
