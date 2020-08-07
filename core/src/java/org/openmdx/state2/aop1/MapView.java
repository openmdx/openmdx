/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: SortedMapView 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2013, OMEX AG, Switzerland
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
package org.openmdx.state2.aop1;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.BasicException.Parameter;
import org.openmdx.kernel.exception.Throwables;

/**
 * SortedMapView
 *
 */
public class MapView implements SortedMap<Integer, Object> {

    /**
     * Constructor
     *
     * @param involvedMembers
     */
    private MapView(
        InvolvedMembers<DataObject_1_0, SortedMap<Integer, Object>> involvedMembers
    ) {
        this.members = involvedMembers;
        this.indices = SetView.newKeySet(involvedMembers, involvedMembers.feature);
    }

    /**
     * Sorted Map View Factory Method
     * 
     * @param involvedStates
     * @param feature
     * 
     * @return a new sorted map view
     */
    static SortedMap<Integer, Object> newObjectMap(
        final Involved<DataObject_1_0> involvedStates,
        final String feature
    ) {
        return new MapView(
            new InvolvedMembers<DataObject_1_0, SortedMap<Integer, Object>>(
                involvedStates,
                feature
            ) {

                @Override
                protected SortedMap<Integer, Object> getMember(
                    DataObject_1_0 state
                )
                    throws ServiceException {
                    return state.objGetSparseArray(feature);
                }

            }
        );
    }

    private final InvolvedMembers<DataObject_1_0, SortedMap<Integer, Object>> members;

    /**
     * The key-set is eagerly instantiated
     */
    final Set<Integer> indices;

    /**
     * The entry-set is lazily instantiated
     */
    private Set<Map.Entry<Integer, Object>> entries;

    /**
     * The values are lazily
     */
    private Collection<Object> values;

    /*
     * (non-Javadoc)
     * 
     * @see java.util.SortedMap#comparator()
     */
    public Comparator<? super Integer> comparator() {
        return null;
    }

    /**
     * @return
     * @see org.openmdx.state2.aop1.InvolvedMembers#getIdParameter()
     */
    protected Parameter getIdParameter() {
        return this.members.getIdParameter();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.SortedMap#firstKey()
     */
    public Integer firstKey() {
        try {
            UniqueValue<Integer> reply = new UniqueValue<Integer>();
            for (SortedMap<Integer, Object> delegate : this.members.getInvolved(AccessMode.FOR_QUERY)) {
                reply.set(delegate.firstKey());
            }
            return reply.get();
        } catch (ServiceException exception) {
            throw toIllegalStateException(
                exception, "The underlying states are inappropriate for the unique determination of the given property"
            );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.SortedMap#headMap(java.lang.Object)
     */
    public SortedMap<Integer, Object> headMap(
        final Integer toKey
    ) {
        return new MapView(
            new InvolvedMembers<DataObject_1_0, SortedMap<Integer, Object>>(
                members.involvedStates,
                members.feature
            ) {

                @Override
                protected SortedMap<Integer, Object> getMember(
                    DataObject_1_0 state
                )
                    throws ServiceException {
                    return state.objGetSparseArray(feature).headMap(toKey);
                }

            }

        );
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.SortedMap#lastKey()
     */
    public Integer lastKey() {
        try {
            UniqueValue<Integer> reply = new UniqueValue<Integer>();
            for (SortedMap<Integer, Object> delegate : this.members.getInvolved(AccessMode.FOR_QUERY)) {
                reply.set(delegate.lastKey());
            }
            return reply.get();
        } catch (ServiceException exception) {
            throw toIllegalStateException(
                exception, "The underlying states are inappropriate for the unique determination of the given property"
            );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.SortedMap#subMap(java.lang.Object, java.lang.Object)
     */
    public SortedMap<Integer, Object> subMap(
        final Integer fromKey,
        final Integer toKey
    ) {
        return new MapView(
            new InvolvedMembers<DataObject_1_0, SortedMap<Integer, Object>>(
                members.involvedStates,
                members.feature
            ) {

                @Override
                protected SortedMap<Integer, Object> getMember(
                    DataObject_1_0 state
                )
                    throws ServiceException {
                    return state.objGetSparseArray(feature).subMap(fromKey, toKey);
                }

            }

        );
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.SortedMap#tailMap(java.lang.Object)
     */
    public SortedMap<Integer, Object> tailMap(
        final Integer fromKey
    ) {
        return new MapView(
            new InvolvedMembers<DataObject_1_0, SortedMap<Integer, Object>>(
                members.involvedStates,
                members.feature
            ) {

                @Override
                protected SortedMap<Integer, Object> getMember(
                    DataObject_1_0 state
                )
                    throws ServiceException {
                    return state.objGetSparseArray(feature).tailMap(fromKey);
                }

            }

        );
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#clear()
     */
    public void clear() {
        for (SortedMap<Integer, Object> delegate : this.members.getInvolved(AccessMode.FOR_UPDATE)) {
            delegate.clear();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        try {
            UniqueValue<Boolean> reply = new UniqueValue<Boolean>();
            for (SortedMap<Integer, Object> delegate : this.members.getInvolved(AccessMode.FOR_QUERY)) {
                reply.set(Boolean.valueOf(delegate.containsKey(key)));
            }
            return reply.get().booleanValue();
        } catch (ServiceException exception) {
            throw toIllegalStateException(
                exception, "The underlying states are inappropriate for the unique determination of the given property"
            );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        try {
            UniqueValue<Boolean> reply = new UniqueValue<Boolean>();
            for (SortedMap<Integer, Object> delegate : this.members.getInvolved(AccessMode.FOR_QUERY)) {
                reply.set(Boolean.valueOf(delegate.containsValue(value)));
            }
            return reply.get().booleanValue();
        } catch (ServiceException exception) {
            throw toIllegalStateException(
                exception, "The underlying states are inappropriate for the unique determination of the given property"
            );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#entrySet()
     */
    public Set<Map.Entry<Integer, Object>> entrySet() {
        return this.entries == null ? this.entries = new AbstractSet<Map.Entry<Integer, Object>>() {

            @Override
            public Iterator<java.util.Map.Entry<Integer, Object>> iterator() {
                return new Iterator<java.util.Map.Entry<Integer, Object>>() {

                    private Iterator<Integer> keys = MapView.this.indices.iterator();

                    private Integer current = null;

                    public boolean hasNext() {
                        return keys.hasNext();
                    }

                    public java.util.Map.Entry<Integer, Object> next() {
                        final Integer key = this.current = this.keys.next();
                        final Object value = MapView.this.get(key);
                        return new Map.Entry<Integer, Object>() {

                            public Integer getKey() {
                                return key;

                            }

                            public Object getValue() {
                                return value;
                            }

                            public Object setValue(Object value) {
                                return MapView.this.put(key, value);
                            }
                        };
                    }

                    public void remove() {
                        if (this.current == null) {
                            throw new IllegalStateException("No current element");
                        } else {
                            MapView.this.remove(this.current);
                            this.current = null;
                        }
                    }

                };
            }

            @Override
            public int size() {
                return MapView.this.size();
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.AbstractCollection#isEmpty()
             */
            @Override
            public boolean isEmpty() {
                return MapView.this.isEmpty();
            }

        } : this.entries;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {
        try {
            UniqueValue<Object> reply = new UniqueValue<Object>();
            for (SortedMap<Integer, Object> delegate : this.members.getInvolved(AccessMode.FOR_QUERY)) {
                reply.set(delegate.get(key));
            }
            return reply.get();
        } catch (ServiceException exception) {
            throw toIllegalStateException(
                exception, "The underlying states are inappropriate for the unique determination of the given value"
            );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        try {
            UniqueValue<Boolean> reply = new UniqueValue<Boolean>();
            for (SortedMap<Integer, Object> delegate : this.members.getInvolved(AccessMode.FOR_QUERY)) {
                reply.set(Boolean.valueOf(delegate.isEmpty()));
            }
            return reply.get().booleanValue();
        } catch (ServiceException exception) {
            throw toIllegalStateException(
                exception, "The underlying states are inappropriate for the unique determination of the given property"
            );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#keySet()
     */
    public Set<Integer> keySet() {
        return this.indices;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(
        Integer key,
        Object value
    ) {
        Object reply = get(key);
        for (SortedMap<Integer, Object> delegate : this.members.getInvolved(AccessMode.FOR_UPDATE)) {
            delegate.put(key, value);
        }
        return reply;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends Integer, ? extends Object> t) {
        for (SortedMap<Integer, Object> delegate : this.members.getInvolved(AccessMode.FOR_UPDATE)) {
            delegate.putAll(t);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key) {
        Object reply = get(key);
        for (SortedMap<Integer, Object> delegate : this.members.getInvolved(AccessMode.FOR_UPDATE)) {
            delegate.remove(key);
        }
        return reply;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#size()
     */
    public int size() {
        try {
            UniqueValue<Integer> reply = new UniqueValue<Integer>();
            for (SortedMap<Integer, Object> delegate : this.members.getInvolved(AccessMode.FOR_QUERY)) {
                reply.set(Integer.valueOf(delegate.size()));
            }
            return reply.get().intValue();
        } catch (ServiceException exception) {
            throw toIllegalStateException(
                exception, "The underlying states are inappropriate for the unique determination of the given property"
            );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#values()
     */
    public Collection<Object> values() {
        return this.values == null ? this.values = new AbstractCollection<Object>() {

            @Override
            public Iterator<Object> iterator() {
                return new Iterator<Object>() {

                    private Iterator<Integer> keys = MapView.this.indices.iterator();

                    private Integer current = null;

                    public boolean hasNext() {
                        return keys.hasNext();
                    }

                    public Object next() {
                        return MapView.this.get(
                            this.current = this.keys.next()
                        );
                    }

                    public void remove() {
                        if (this.current == null) {
                            throw new IllegalStateException("No current element");
                        } else {
                            MapView.this.remove(this.current);
                            this.current = null;
                        }
                    }

                };
            }

            @Override
            public int size() {
                return MapView.this.size();
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.AbstractCollection#isEmpty()
             */
            @Override
            public boolean isEmpty() {
                return MapView.this.isEmpty();
            }

        } : this.values;

    }

    private IllegalStateException toIllegalStateException(
        ServiceException exception,
        final String message
    ) {
        return Throwables.initCause(
            new IllegalStateException(
                message + ". " + exception.getCause().getDescription()
            ),
            exception,
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ILLEGAL_STATE,
            getIdParameter()
        );
    }

}
