/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Abstract Mapped Record 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010-2012, OMEX AG, Switzerland
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

package org.openmdx.base.rest.spi;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.openmdx.base.collection.Sets;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.Code;
import org.openmdx.base.resource.cci.Freezable;
import org.openmdx.base.resource.spi.Isolation;
import org.openmdx.base.resource.spi.StandardRecordFactory;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.IndentingFormatter;


/**
 * Abstract Mapped Record
 */
public abstract class AbstractMappedRecord<M extends Enum<M>> 
	implements MultiLineStringRepresentation, MappedRecord, Freezable 
{

	/**
     * Constructor 
     */
    protected AbstractMappedRecord(
    ){
        super();
    }
    
    /**
     * Constructor for clones 
     *
     * @param that the object to be cloned
     */
    @SuppressWarnings("unchecked")
	protected AbstractMappedRecord(
    	AbstractMappedRecord<M> that
    ){
    	for(M member : members()) {
    		final Object value = that.get(member);
    		if(value instanceof IndexedRecord) {
    			final IndexedRecord target = (IndexedRecord) this.get(member);
    			for(Object element : (IndexedRecord)value) {
    				target.add(getValueForClone(element));
    			}
    		} else {
    			this.put(member, getValueForClone(value));
    		}
    	}
    }
    
    /**
     * Tells, whether the record is frozen or not
     */
    private boolean immutable = false;
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -4196205547784874659L;
    
	/* (non-Javadoc)
	 * @see org.openmdx.base.resource.cci.Freezable#makeImmutable()
	 */
	@Override
	public void makeImmutable() {
		this.immutable = true;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.resource.cci.Freezable#isImmutable()
	 */
	@Override
	public final boolean isImmutable() {
		return this.immutable;
	}

	/**
	 * Asserts that the object is mutable
	 * 
	 * @throws IllegalStateException if the record is immutable
	 */
	protected void assertMutability(){
		if(this.immutable) {
	        throw new IllegalStateException(
                "This record is frozen",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE,
                    new BasicException.Parameter("class", getClass().getName()),
                    new BasicException.Parameter("name", getRecordName()),
                    new BasicException.Parameter("immutable", Boolean.TRUE)
                )
            );
		}
	}
	
    private static Object getValueForClone(
    	Object source
    ){
		return source instanceof MappedRecord ? Isolation.isolate((MappedRecord)source) : source;
    }
    
    /* (non-Javadoc)
     * @see java.util.AbstractMap#clone()
     */
    @Override
    public abstract AbstractMappedRecord<M> clone(
    ) throws CloneNotSupportedException;

    /**
     * Retrieve a value by its member name
     * 
     * @param member the member name
     * 
     * @return the value
     */
    protected Object get(
        M member
    ) {
    	return null;
    }
    
    /**
     * Set a value by index 
     * 
     * @param index the index
     * @param value the new value
     * 
     * @return the old value
     */
    protected void put(
        M member,
        Object value
    ){
    	assertMutability();
        throw new IllegalArgumentException(
            "Unsupported member",
            BasicException.newEmbeddedExceptionStack(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                new BasicException.Parameter("class", getClass().getName()),
                new BasicException.Parameter("name", getRecordName()),
                new BasicException.Parameter("member", member),
                new BasicException.Parameter("value", value),
                new BasicException.Parameter("keys", keySet())
            )
        );
    }
    
    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public final Object get(Object key) {
    	final M member = members().valueOf(key);
        return member == null ? null : get(member);
    }

    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public final Object put(
        Object key, 
        Object value
    ) {
    	final M member = members().valueOf(key);
    	if(member == null) throw new IllegalArgumentException(
            "Unsupported key",
            BasicException.newEmbeddedExceptionStack(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                new BasicException.Parameter("class", getClass().getName()),
                new BasicException.Parameter("name", getRecordName()),
                new BasicException.Parameter("keys", keySet()),
                new BasicException.Parameter("key", key),
                new BasicException.Parameter("value", value)
            )
        );
        Object old = get(member);
        put(member, value);
        return old;
    }
    
    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    @Override
    final public Set<Map.Entry<?, ?>> entrySet() {
    	return new AbstractSet<Map.Entry<?, ?>>(){

            @Override
            public Iterator<Map.Entry<?, ?>> iterator() {
            	final Iterator<M> delegate = members().iterator();
            	
                return new Iterator<Map.Entry<?, ?>>(){
                    
                    @Override
                    public boolean hasNext() {
                        return delegate.hasNext();
                    }

                    @Override
                    public Map.Entry<?, ?> next() {
                    	final M member = delegate.next();
                    	
                        return new Map.Entry<Object,Object>() {

                            @Override
                            public Object getKey() {
                                return member.name();
                            }

                            @Override
                            public Object getValue() {
                                return get(member);
                            }

                            @Override
                            public Object setValue(Object value) {
                            	Object old = getValue();
                                put(member, value);
                                return old;
                            }

                        	/**
                        	 * Compares the specified object with this entry for equality.
                        	 * Returns {@code true} if the given object is also a map entry and
                        	 * the two entries represent the same mapping.	More formally, two
                        	 * entries {@code e1} and {@code e2} represent the same mapping
                        	 * if<pre>
                        	 *   (e1.getKey()==null ?
                        	 *    e2.getKey()==null :
                        	 *    e1.getKey().equals(e2.getKey()))
                        	 *   &amp;&amp;
                        	 *   (e1.getValue()==null ?
                        	 *    e2.getValue()==null :
                        	 *    e1.getValue().equals(e2.getValue()))</pre>
                        	 * This ensures that the {@code equals} method works properly across
                        	 * different implementations of the {@code Map.Entry} interface.
                        	 *
                        	 * @param o object to be compared for equality with this map entry
                        	 * @return {@code true} if the specified object is equal to this map
                        	 *	   entry
                        	 * @see    #hashCode
                        	 */
                        	public boolean equals(Object o) {
                        	    if (!(o instanceof Map.Entry)) return false;
                        	    Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                        	    return eq(getKey(), e.getKey()) && eq(getValue(), e.getValue());
                        	}

                        	/**
                        	 * Returns the hash code value for this map entry.  The hash code
                        	 * of a map entry {@code e} is defined to be: <pre>
                        	 *   (e.getKey()==null   ? 0 : e.getKey().hashCode()) ^
                        	 *   (e.getValue()==null ? 0 : e.getValue().hashCode())</pre>
                        	 * This ensures that {@code e1.equals(e2)} implies that
                        	 * {@code e1.hashCode()==e2.hashCode()} for any two Entries
                        	 * {@code e1} and {@code e2}, as required by the general
                        	 * contract of {@link Object#hashCode}.
                        	 *
                        	 * @return the hash code value for this map entry
                        	 * @see    #equals
                        	 */
                        	public int hashCode() {
                        		final Object value = getValue();
                        	    return getKey().hashCode() ^
                        		   (value == null ? 0 : value.hashCode());
                        	}

                            /**
                             * Returns a String representation of this map entry.  This
                             * implementation returns the string representation of this
                             * entry's key followed by the equals character ("<tt>=</tt>")
                             * followed by the string representation of this entry's value.
                             *
                             * @return a String representation of this map entry
                             */
                        	public String toString() {
                        	    return getKey() + "=" + getValue();
                        	}
                            
                        };
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                    
                };
            }

            @Override
            public int size() {
                return members().size();
            } 
            
        
	    };
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    @Override
    final public Set<?> keySet() {
    	return new AbstractSet<Object>(){

			@Override
			public Iterator<Object> iterator() {
				
				final Iterator<M> delegate = members().iterator();
				
				return new Iterator<Object>() {

					@Override
					public boolean hasNext() {
						return delegate.hasNext();
					}

					@Override
					public Object next() {
						return delegate.next().name();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException("Members cannot be removed from an " + getRecordName() + " record");
					}
				};
			}

			@Override
			public int size() {
				return members().size();
			}
			
		};
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    @Override
    final public void clear() {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    final public boolean containsKey(Object key) {
    	return members().valueOf(key) != null;
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    final public boolean containsValue(Object value) {
        for(M member : members()) {
            final Object candidate = get(member);
            if(value == null ? null == candidate : value.equals(candidate)) return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    @Override
    final public boolean isEmpty() {
        return size() == 0;
    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    @SuppressWarnings("rawtypes")
	@Override
    final public void putAll(Map m) {
        for(Object e : m.entrySet()) {
            Map.Entry<?, ?> entry = (Entry<?, ?>) e;
            put(entry.getKey(), entry.getValue());
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    final public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    @Override
    final public int size() {
        return members().size();
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    @Override
    final public Collection<?> values() {
        return new AbstractCollection<Object>(){

            @Override
            public Iterator<Object> iterator() {
            	
            	final Iterator<M> delegate = members().iterator();
            	
                return new Iterator<Object>(){

                    @Override
                    public boolean hasNext() {
                        return delegate.hasNext();
                    }

                    @Override
                    public Object next() {
                        return get(delegate.next());
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                    
                };
            }

            @Override
            public int size() {
                return members().size();
            }
          
        };
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#getRecordShortDescription()
     */
    @Override
    public String getRecordShortDescription() {
        return null;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#setRecordName(java.lang.String)
     */
    @Override
    public void setRecordName(String recordName) {
        if(!recordName.equals(getRecordName())) throw BasicException.initHolder(
            new IllegalArgumentException(
                "Unmodifiable Record Name",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    new BasicException.Parameter("fixed", getRecordName()),
                    new BasicException.Parameter("requested", recordName)
                )
            )
        );
    }

    /**
     * Invocation has no effect
     * 
     * @param ignored the short description is ignored
     */
    @Override
    final public void setRecordShortDescription(String ignored) {
        // The short description set by this method is ignored
    }

    /**
     * Convert an <code>IndexedRecord</code> to a <code>String</code> array
     * 
     * @param value the <code>IndexedRecord</code>
     * 
     * @return an array with the value's components
     */
    @SuppressWarnings("unchecked")
	protected static Set<String> toSet(
        Object value
    ){
        return 
            value == null ? Collections.<String>emptySet() :
            value instanceof String ? Collections.singleton((String)value) :
            Sets.asSet((Collection<String>)value); 
    }
    
    protected IndexedRecord newSet(){
    	final IndexedRecord record = createSet();
    	if(this.immutable) {
    		freeze(record);
    	}
		return record;
    }

	/**
	 * @return
	 */
	private static IndexedRecord createSet() {
		try {
			return StandardRecordFactory.getInstance().createIndexedRecord(Multiplicity.SET.code());
		} catch (ResourceException e) {
			throw new RuntimeException(e);
		}
	}

    @SuppressWarnings("unchecked")
	protected IndexedRecord newSet(
    	IndexedRecord source
    ){
    	final IndexedRecord target = createSet();
    	target.addAll(source);
    	if(this.immutable) {
    		freeze(target);
    	}
    	return target;
    }

    protected IndexedRecord newList(){
    	final IndexedRecord record = createList();
    	if(this.immutable) {
    		freeze(record);
    	}
		return record;
    }

	/**
	 * @return
	 */
	private static IndexedRecord createList() {
		try {
    		return StandardRecordFactory.getInstance().createIndexedRecord(Multiplicity.LIST.code());
    	} catch (ResourceException e) {
    		throw new RuntimeException(e);
    	}
	}
    
    @SuppressWarnings("unchecked")
	protected IndexedRecord newList(
    	IndexedRecord source
    ){
    	final IndexedRecord target = createList();
    	target.addAll(source);
    	if(this.immutable) {
    		freeze(target);
    	}
    	return target;
    }
    
    /**
     * Convert the value to a <code>Path</code> if necessary
     * 
     * @param value
     * 
     * @return the value as <code>Path</code>
     */
    protected static Path toPath(
        Object value
    ){
        return 
            value == null ? null :
            value instanceof Path ? (Path)value :
            new Path((String)value);
    }
    
    /**
     * Convert the value from a <code>Path</code>
     * 
     * @param value a Path
     * 
     * @return the value as <code>String</code>
     */
    protected static String toString(
        Path value
    ){
        return 
            value == null ? null :
            value.toXRI(); 
    }
    
    /**
     * Convert the value to a <code>Boolean</code> if necessary
     * 
     * @param value
     * 
     * @return the value as <code>Boolean</code>
     */
    protected static boolean toBoolean(
        Object value
    ){
        return 
            value == null ? false :
            value instanceof Boolean ? ((Boolean)value).booleanValue() :
            Boolean.parseBoolean((String) value);
    }

    /**
     * Convert the value to a <code>Long</code> if necessary
     * 
     * @param value
     * 
     * @return the value as <code>Long</code>
     */
    protected static Long toLong(
        Object value
    ){
        return 
            value == null ? null :
            value instanceof Long ? (Long)value :
            Long.valueOf((String) value);
    }
    
    /**
     * Convert a code to its short representation
     * 
     * @param code a code instance
     * 
     * @return the codes <code>Short</code> representation
     */
    @Nullable
    protected static Short jcaValue(@Nullable Code code) {
    	return code == null ? null : Short.valueOf(code.code());
    }
    
    /**
     * Returns a multi-line string representation of this MappedRecord.
     * <p>
     * The string representation consists of the record name, follwed by the
     * optional short description enclosed in parenthesis (" (...)"), followed 
     * by a colon and the mappings enclosed in braces (": {...}"). Each
     * key-value mapping is rendered as the key followed by an equals sign ("=")
     * followed by the associated value written on a separate line and indented
     * while embedded lines are indented as well.
     *
     * @return   a multi-line String representation of this Record.
     */
    @Override
    public String toString(
    ){
        return IndentingFormatter.toString(this);
    }
    
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int h = 0;
		for(Map.Entry<?, ?> e : entrySet()) {
			h += e.hashCode();
		}
		return h;
	}

	/**
	 * Two mapped records are considered equal if they have the same record name
	 * and the same entries. 
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof MappedRecord) {
			MappedRecord that = (MappedRecord) obj;
			if(
				this.getRecordName().equals(that.getRecordName()) &&
				this.keySet().equals(that.keySet())
			) {
				for(Object key : keySet()) {
					if(!eq(this.get(key), that.get(key))) return false;
				}
				return true;
			}
		}
		return false;
	}
    
	static boolean eq(Object left, Object right) {
		return left == right || (
			left != null && right != null && left.equals(right)
		);
	}
	
	/**
	 * Replace the target's content by the source's content
	 * 
	 * @param target the target list
	 * @param source the source list
	 */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	protected static void replaceValues(Collection target, Collection<?> source){
    	target.clear();
    	if(source != null) {
    	    target.addAll(source);
    	}
    }
    
	/**
	 * Replace the target's content by the source's content
	 * 
	 * @param target the target list
	 * @param source the source array
	 */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	protected static void replaceValues(Collection target, Object[] source){
    	target.clear();
    	if(source != null) {
    		for(Object value : source) {
    			target.add(value);
    		}
    	}
    }

	/**
	 * Replace the target's content b y the source's content
	 * 
	 * @param target the target list
	 * @param source the source array
	 */
    @SuppressWarnings("unchecked")
	protected static void replaceValues(MappedRecord target, Map<?,?> source){
    	target.clear();
    	if(source != null) {
    	    target.putAll(source);
    	}
    }
    
    /**
     * Freeze the given record if possible
     * 
     * @param record 
     */
    protected static void freeze(Record record) {
    	if(record instanceof Freezable) {
    		((Freezable) record).makeImmutable();
    	}
    }
    
    /**
     * The instance is provided by the subclasses
     * 
     * @return the sub-class specific member instance
     */
    protected abstract Members<M> members();

    //------------------------------------------------------------------------
    // Class Members
    //------------------------------------------------------------------------
    
    protected static class Members<M extends Enum<M>> implements Iterable<M> {
    	
		private Members(
    		Class<M> memberClass
    	){
    		this.memberClass = memberClass;
    		this.members = EnumSet.allOf(memberClass);
    		this.keys = new AbstractSet<String>(){

    			@Override
    			public Iterator<String> iterator() {
    				
    				final Iterator<M> delegate = members.iterator();
    				
    				return new Iterator<String>() {

    					@Override
    					public boolean hasNext() {
    						return delegate.hasNext();
    					}

    					@Override
    					public String next() {
    						return delegate.next().name();
    					}

    					@Override
    					public void remove() {
    						throw new UnsupportedOperationException("Members cannot be removed from a record");
    					}
    				};
    			}

    			@Override
    			public int size() {
    				return members.size();
    			}
    			
    		};
    	}
    	
		private final Class<M> memberClass;
		private final Set<M> members;
		private final Set<?> keys;

		
		public static <M extends Enum<M>> Members<M> newInstance(
    		Class<M> memberClass
		){
			return new Members<M>(memberClass);
		}
		
		@Override
		public Iterator<M> iterator() {
			return this.members.iterator();
		}
		
		Set<?> getKeys() {
			return this.keys;
		}

		int size(){
			return this.members.size();
		}

		/**
		 * Converts the given key to a member
		 * 
		 * @param key
		 * 
		 * @return the corresponding member, or <code>null</code> if none matches
		 */
		M valueOf(Object key) {
			if(this.memberClass.isInstance(key)) {
				return this.memberClass.cast(key);
			} else if(key instanceof String) {
				try {
					// In case of success it's cheaper than iterating over the values
					return Enum.valueOf(this.memberClass, (String)key);
				} catch (RuntimeException acceptable) {
					// In case of failure it's more expensive than iterating over the values
					return null;
				}
			} else {
				return null;
			}
		}
		
    }
    
}
