/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openmdx.uses.org.apache.commons.collections.collection;

import java.util.Collection;
import java.util.Iterator;

/**
 * Decorates another <code>Collection</code> to provide additional behaviour.
 * <p>
 * Each method call made on this <code>Collection</code> is forwarded to the
 * decorated <code>Collection</code>. This class is used as a framework on which
 * to build to extensions such as synchronized and unmodifiable behaviour. The
 * main advantage of decoration is that one decorator can wrap any implementation
 * of <code>Collection</code>, whereas sub-classing requires a new class to be
 * written for each implementation.
 * <p>
 * This implementation does not perform any special processing with
 * {@link #iterator()}. Instead it simply returns the value from the 
 * wrapped collection. This may be undesirable, for example if you are trying
 * to write an unmodifiable implementation it might provide a loophole.
 *
 * @since Commons Collections 3.0
 * @version $Revision: 1.5 $ $Date: 2008/06/27 16:59:27 $
 * 
 * @author Stephen Colebourne
 * @author Paul Jack
 */
@SuppressWarnings("unchecked")
public abstract class AbstractCollectionDecorator implements Collection {

    /** The collection being decorated */
    protected Collection collection;

    /**
     * Constructor only used in deserialization, do not use otherwise.
     * @since Commons Collections 3.1
     */
    protected AbstractCollectionDecorator() {
        super();
    }

    /**
     * Constructor that wraps (not copies).
     * 
     * @param coll  the collection to decorate, must not be null
     * @throws IllegalArgumentException if the collection is null
     */
    protected AbstractCollectionDecorator(Collection coll) {
        if (coll == null) {
            throw new IllegalArgumentException("Collection must not be null");
        }
        this.collection = coll;
    }

    /**
     * Gets the collection being decorated.
     * 
     * @return the decorated collection
     */
    protected Collection getCollection() {
        return this.collection;
    }

    //-----------------------------------------------------------------------
    public boolean add(Object object) {
        return this.collection.add(object);
    }

    public boolean addAll(Collection coll) {
        return this.collection.addAll(coll);
    }

    public void clear() {
        this.collection.clear();
    }

    public boolean contains(Object object) {
        return this.collection.contains(object);
    }

    public boolean isEmpty() {
        return this.collection.isEmpty();
    }

    public Iterator iterator() {
        return this.collection.iterator();
    }

    public boolean remove(Object object) {
        return this.collection.remove(object);
    }

    public int size() {
        return this.collection.size();
    }

    public Object[] toArray() {
        return this.collection.toArray();
    }

    public Object[] toArray(Object[] object) {
        return this.collection.toArray(object);
    }

    public boolean containsAll(Collection coll) {
        return this.collection.containsAll(coll);
    }

    public boolean removeAll(Collection coll) {
        return this.collection.removeAll(coll);
    }

    public boolean retainAll(Collection coll) {
        return this.collection.retainAll(coll);
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        return this.collection.equals(object);
    }

    public int hashCode() {
        return this.collection.hashCode();
    }

    public String toString() {
        return this.collection.toString();
    }

}
