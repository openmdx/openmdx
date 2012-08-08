/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LayerStatistics_1.java,v 1.5 2008/03/21 20:17:16 hburger Exp $
 * Description: LayerStatistics_1 class
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 20:17:16 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
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
 */
package org.openmdx.compatibility.base.dataprovider.kernel;

import java.util.Date;

import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.spi.LayerStatistics_1_0;

/**
 * A class for storing and managing Layer statistics data.
 */
@SuppressWarnings("unchecked")
public class LayerStatistics_1
    implements LayerStatistics_1_0
{

  public LayerStatistics_1(
  ) {

    m_totalRequestCollectionSize = 0;
    m_minRequestCollectionSize = 0;
    m_maxRequestCollectionSize = 0;

    for(int i = 0; i < MAX_OPERATIONS; i++) {

      m_totalRequests[i] = 0;
      m_totalRequestObjectSize[i] = 0;
      m_minRequestObjectSize[i] = 0;
      m_maxRequestObjectSize[i] = 0;

      m_totalReplies[i] = 0;
      m_totalReplyObjects[i] = 0;
      m_minReplyObjects[i] = 0;
      m_maxReplyObjects[i] = 0;
      m_totalReplyObjectSize[i] = 0;
      m_minReplyObjectSize[i] = 0;
      m_maxReplyObjectSize[i] = 0;

      m_modifiedAt[i] = System.currentTimeMillis();
      m_createdAt[i] = System.currentTimeMillis();
    }
  }
  /**
   * Update statistic.
   *
   * @param operation operation defined by DataproviderOperations.def.
   *
   * @param objects dataprovider objects processed by the operation.
   *
   */
  public void update(
    DataproviderRequest[] requests,
    DataproviderReply[] replies
  ) {

    // requests statistic
    m_totalRequestCollectionSize += requests.length;
    m_minRequestCollectionSize = java.lang.Math.min(
      m_minRequestCollectionSize,
      requests.length
    );
    m_maxRequestCollectionSize = java.lang.Math.max(
      m_maxRequestCollectionSize,
      requests.length
    );

    // operation-level statistics
    for(int i = 0; i < requests.length; i++) {
      short operation = requests[i].operation();

      // request
      m_totalRequests[operation] += 1;
      m_totalRequestObjectSize[operation] += requests[i].object().attributeNames().size();
      m_minRequestObjectSize[operation] = java.lang.Math.min(
        m_minRequestObjectSize[operation], 
        requests[i].object().attributeNames().size()
      );
      m_maxRequestObjectSize[operation] = java.lang.Math.max(
        m_maxRequestObjectSize[operation], 
        requests[i].object().attributeNames().size()
      );

      // reply
      if(replies[i] != null) {
        m_totalReplies[operation] += 1;
        m_totalReplyObjects[operation] += replies[i].getObjects().length;
        m_minReplyObjects[operation] = java.lang.Math.min(
          m_minReplyObjects[operation],
          replies[i].getObjects().length
        );
        m_maxReplyObjects[operation] = java.lang.Math.max(
          m_maxReplyObjects[operation],
          replies[i].getObjects().length
        );
        for (int j=0; j < replies[i].getObjects().length; j++) {
          DataproviderObject object = replies[i].getObjects()[j];
          m_totalReplyObjectSize[operation] += object.attributeNames().size();
          m_minReplyObjectSize[operation] = java.lang.Math.min(
            m_minReplyObjectSize[operation], 
            object.attributeNames().size()
          );
          m_maxReplyObjectSize[operation] = java.lang.Math.max(
            m_maxReplyObjectSize[operation], 
            object.attributeNames().size()
          );
        }
      }      
      m_modifiedAt[operation] = System.currentTimeMillis();
    }
  }

  /**
   * Get statistic values for the specified layer and operation.
   *
   * @param operation operation defined by DataproviderOperations.def.
   *
   * @param statistic values as dataprovider object. 
   *
   */
  public void get(
    short operation,
    DataproviderObject statistic
  ){
    
    // requests
    statistic.clearValues("totalRequestCollectionSize").add(
      new java.math.BigDecimal(m_totalRequestCollectionSize)
    );
    statistic.clearValues("minRequestCollectionSize").add(
      new java.math.BigDecimal(m_minRequestCollectionSize)
    );
    statistic.clearValues("maxRequestCollectionSize").add(
      new java.math.BigDecimal(m_maxRequestCollectionSize)
    );

    // single request
    statistic.clearValues("totalRequests").add(
      new java.math.BigDecimal(m_totalRequests[operation])
    );
    statistic.clearValues("totalRequestObjectSize").add(
      new java.math.BigDecimal(m_totalRequestObjectSize[operation])
    );
    statistic.clearValues("minRequestObjectSize").add(
      new java.math.BigDecimal(m_minRequestObjectSize[operation])
    );
    statistic.clearValues("maxRequestObjectSize").add(
      new java.math.BigDecimal(m_maxRequestObjectSize[operation])
    );

    // single reply
    statistic.clearValues("totalReplies").add(
      new java.math.BigDecimal(m_totalReplies[operation])
    );
    statistic.clearValues("totalReplyObjects").add(
      new java.math.BigDecimal(m_totalReplyObjects[operation])
    );
    statistic.clearValues("minReplyObjects").add(
      new java.math.BigDecimal(m_minReplyObjects[operation])
    );
    statistic.clearValues("maxReplyObjects").add(
      new java.math.BigDecimal(m_maxReplyObjects[operation])
    );
    statistic.clearValues("totalReplyObjectSize").add(
      new java.math.BigDecimal(m_totalReplyObjectSize[operation])
    );
    statistic.clearValues("minReplyObjectSize").add(
      new java.math.BigDecimal(m_minReplyObjectSize[operation])
    );
    statistic.clearValues("maxReplyObjectSize").add(
      new java.math.BigDecimal(m_maxReplyObjectSize[operation])
    );

    // timestamps
    statistic.clearValues(SystemAttributes.MODIFIED_AT).add(
      DateFormat.getInstance().format(new Date(m_modifiedAt[operation]))
    );
    statistic.clearValues(SystemAttributes.CREATED_AT).add(
      DateFormat.getInstance().format(new Date(m_createdAt[operation]))
    );
  }

  //------------------------------------------------------------------------
  // Variables
  //------------------------------------------------------------------------

  private static final int MAX_OPERATIONS = 10;

  private long m_totalRequestCollectionSize;
  private long m_minRequestCollectionSize;
  private long m_maxRequestCollectionSize;

  private long[] m_totalRequests = new long[MAX_OPERATIONS];
  private long[] m_totalRequestObjectSize = new long[MAX_OPERATIONS];
  private long[] m_minRequestObjectSize = new long[MAX_OPERATIONS];
  private long[] m_maxRequestObjectSize = new long[MAX_OPERATIONS];

  private long[] m_totalReplies = new long[MAX_OPERATIONS];
  private long[] m_totalReplyObjects = new long[MAX_OPERATIONS];
  private long[] m_minReplyObjects = new long[MAX_OPERATIONS];
  private long[] m_maxReplyObjects = new long[MAX_OPERATIONS];
  private long[] m_totalReplyObjectSize = new long[MAX_OPERATIONS];
  private long[] m_minReplyObjectSize = new long[MAX_OPERATIONS];
  private long[] m_maxReplyObjectSize = new long[MAX_OPERATIONS];

  private long[] m_modifiedAt = new long[MAX_OPERATIONS];
  private long[] m_createdAt = new long[MAX_OPERATIONS];
    
}

//--- End of File -----------------------------------------------------------
