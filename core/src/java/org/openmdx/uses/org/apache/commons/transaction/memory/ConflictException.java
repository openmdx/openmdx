/*
 * $Header: /cvsroot/openmdx/core/src/java/org/openmdx/uses/org/apache/commons/transaction/memory/ConflictException.java,v 1.1 2005/03/24 13:43:56 hburger Exp $
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

package org.openmdx.uses.org.apache.commons.transaction.memory;


/**
 * Exception displaying a conflict between two optimistic transactions.
 * 
 * @version $Revision: 1.1 $
 * @see OptimisticMapWrapper
 */
public class ConflictException extends RuntimeException /* FIXME Exception*/ {
	/**
     * 
     */
    private static final long serialVersionUID = 3618420423441003062L;
    protected Object key;
	
	public ConflictException(Object key) {
		this.key = key;
	}
}
