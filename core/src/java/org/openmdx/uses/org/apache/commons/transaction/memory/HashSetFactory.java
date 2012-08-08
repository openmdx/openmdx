/*
 * $Header: /cvsroot/openmdx/core/src/java/org/openmdx/uses/org/apache/commons/transaction/memory/HashSetFactory.java,v 1.3 2008/03/21 18:42:16 hburger Exp $
 * $Revision: 1.3 $
 * $Date: 2008/03/21 18:42:16 $
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

package org.openmdx.uses.org.apache.commons.transaction.memory;

import java.util.HashSet;
import java.util.Set;

/**
 * Default set factory implementation creating {@link HashSet}s.
 * 
 * @version $Revision: 1.3 $
 */
@SuppressWarnings("unchecked")
public class HashSetFactory implements SetFactory {

    public Set createSet() {
    	return new HashSet();
    }

    public void disposeSet(Set set) {
        //
    }

}
