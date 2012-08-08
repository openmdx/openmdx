/*
 * $Header: /cvsroot/openmdx/core/src/java/org/openmdx/uses/org/apache/commons/transaction/memory/SetFactory.java,v 1.2 2008/03/21 18:42:18 hburger Exp $
 * $Revision: 1.2 $
 * $Date: 2008/03/21 18:42:18 $
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

import java.util.Set;

/**
 * 
 * @version $Revision: 1.2 $
 */
@SuppressWarnings("unchecked")
public interface SetFactory {
	public Set createSet();
	public void disposeSet(Set set);
}
