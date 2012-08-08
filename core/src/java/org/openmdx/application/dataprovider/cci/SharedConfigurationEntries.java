/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: SharedConfigurationEntries.java,v 1.9 2012/01/05 23:20:21 hburger Exp $
 * Description: Generated constants for SharedConfigurationEntries
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2012/01/05 23:20:21 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2012, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.cci;


/**
 * The <code>SharedConfigurationEntries</code> class contains
 * constants identifying the dataprovider configuration entries
 * shared by all layers.
 */
public class SharedConfigurationEntries {

  /**
   * Constructor 
   */
  protected SharedConfigurationEntries() {
   // Avoid instantiation
  }

  /**
   * Sun Application Server 8.1 uses URLs as <code>Binding</code> names even 
   * if <code>isRelative()</code> evauluates to <code>true</code>.
   */
  static public boolean WORK_AROUND_SUN_APPLICATION_SERVER_BINDINGS = true;
  
  /**
   * The NAMESPACE_ID configuration entry
   */
  static public final String NAMESPACE_ID = "namespaceId";

  /**
   * The DATABASE_CONNECTION_FACTORY configuration entry  
   */
  static public final String DATABASE_CONNECTION_FACTORY = "datasource";

  /**
   * Key for a JCA <code>ConnectionFactory</code> instance  
   */
  static public final String DATAPROVIDER_CONNECTION_FACTORY = "dataprovider";

  /**
   * Defines whether the UID's should be in compressed or UUID format 
   * (default). 
   */
  static public final String COMPRESS_UID = "compressUID";

  /**
   * SEQUENCE_SUPPORTED flag
   */
  static public final String SEQUENCE_SUPPORTED = "sequenceSupported";

  /**
   * BATCH_SIZE for find operations
   */
  static public final String BATCH_SIZE = "batchSize";

}
