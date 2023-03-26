/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: RefPackage 1.0 Interface
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
package org.openmdx.base.accessor.jmi.cci;

import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefPackage;
import javax.jmi.reflect.RefStruct;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.spi.PersistenceManager_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.QueryFilterRecord;

/**
 * This interface extends the javax.jmi.reflect.RefPackage interface by
 * openMDX-specific helpers. This methods must not be used
 * by 100% JMI-compliant applications. 
 */
public interface RefPackage_1_0 extends RefPackage {

  /**
   * Returns model defined for this package.
   * 
   * @return Model_1_0 model assigned to this package.
   */
  Model_1_0 refModel(
  );

  /**
   * Returns the persistence manager from which the package creates and retrieves objects.
   * 
   * @return the package's delegate
   */
  PersistenceManager refDelegate(
  );
    
  /**
   * Retrieves the JDO Persistence Manager delegating to this package.
   * 
   * @return the JDO Persistence Manager delegating to this package.
   */
  PersistenceManager_1_0 refPersistenceManager(
  );

  /**
    * Create a query
    * 
    * @param filterClassName
    * @param subclasses 
    * @param filter
    *
    * @return a new query
    * 
    * @exception ServiceException
    */
   RefQuery_1_0 refCreateQuery(
       String filterClassName,
       boolean subclasses, 
       QueryFilterRecord filter 
   ) throws ServiceException;

   /**
    * Create a structure proxy based on the record name
    * 
    * @param structName
    * @param delegate
    * 
    * @return the structure proxy based on the record name
    */
   public RefStruct refCreateStruct(
       Record record
   );

   /**
    * Retrieve the RefPackage's view context
    * 
    * @return the RefPackage's view context in case of a view,
    * {@code null} otherwise
    */
   InteractionSpec refInteractionSpec();

   /**
    * Retrieve a container specified by its resource identifier
    * 
    * @param resourceIdentifier
    * @param containerClass
    * 
    * @return the container specified by its resource identifier
    */
   <C extends RefContainer<?>> C refContainer(
       Path resourceIdentifier,
       Class<C> containerClass
   );

   /**
    * Create a context specific RefPackage
    * 
    * @param viewContext
    * 
    * @return a context specific RefPackage
    */
   RefPackage_1_0 refPackage(
       InteractionSpec viewContext
   );

}