/*
 * $Header: /cvsroot/openmdx/core/src/java/org/openmdx/uses/org/apache/commons/transaction/memory/jca/MemoryMapResourceManager.java,v 1.1 2005/03/24 13:43:56 hburger Exp $
 * $Revision: 1.1 $
 * $Date: 2005/03/24 13:43:56 $
 *
 * ====================================================================
 *
 * Copyright 1999-2002 The Apache Software Foundation 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openmdx.uses.org.apache.commons.transaction.memory.jca;

import java.util.HashMap;
import java.util.Map;

import org.openmdx.uses.org.apache.commons.transaction.memory.TransactionalMapWrapper;

/**
 *   
 * @version $Revision: 1.1 $
 * 
 */
public class MemoryMapResourceManager {

    protected static MemoryMapResourceManager instance = new MemoryMapResourceManager();

    public static MemoryMapResourceManager getInstance() {
        return instance;
    }

    protected Map maps = new HashMap();

    public synchronized TransactionalMapWrapper lookup(Object id) {

        TransactionalMapWrapper map = (TransactionalMapWrapper) maps.get(id);
        // XXX simply create it when not there
        if (map == null) {
            map = new TransactionalMapWrapper(new HashMap());
            maps.put(id, map);
        }

        return map;
    }
}
