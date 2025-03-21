/*
 * Copyright (c) 2000, 2009, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.openmdx.dalvik.uses.java.beans;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.WeakHashMap;

/*
 * Like the {@code Intropector}, the {@code MetaData} class
 * contains <em>meta</em> objects that describe the way
 * classes should express their state in terms of their
 * own public APIs.
 *
 * @see org.openmdx.dalvik.uses.java.beans.Intropector
 *
 * @author Philip Milne
 * @author Steve Langley
 * 
 * <p>
 * openMDX/Dalvik Notice (January 2013):<br>
 * THIS CODE HAS BEEN MODIFIED AND ITS NAMESPACE HAS BEEN PREFIXED WITH
 * {@code org.openmdx.dalvik.uses.}

 * </p>
 * @since openMDX 2.12
 * @author openMDX Team
 */
class NullPersistenceDelegate extends PersistenceDelegate {
    // Note this will be called by all classes when they reach the
    // top of their superclass chain.
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
    }
    protected Expression instantiate(Object oldInstance, Encoder out) { return null; }

    public void writeObject(Object oldInstance, Encoder out) {
    // System.out.println("NullPersistenceDelegate:writeObject " + oldInstance);
    }
}

/**
 * The persistence delegate for {@code enum} classes.
 *
 * @author Sergey A. Malenkov
 */
@SuppressWarnings("rawtypes")
class EnumPersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance == newInstance;
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
		Enum e = (Enum) oldInstance;
        return new Expression(e, Enum.class, "valueOf", new Object[]{e.getClass(), e.name()});
    }
}

class PrimitivePersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        return new Expression(oldInstance, oldInstance.getClass(),
                  "new", new Object[]{oldInstance.toString()});
    }
}

@SuppressWarnings("rawtypes")
class ArrayPersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return (newInstance != null &&
                oldInstance.getClass() == newInstance.getClass() && // Also ensures the subtype is correct.
                Array.getLength(oldInstance) == Array.getLength(newInstance));
        }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        // System.out.println("instantiate: " + type + " " + oldInstance);
        Class oldClass = oldInstance.getClass();
        return new Expression(oldInstance, Array.class, "newInstance",
                   new Object[]{oldClass.getComponentType(),
                                Integer.valueOf(Array.getLength(oldInstance))});
        }

    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        int n = Array.getLength(oldInstance);
        for (int i = 0; i < n; i++) {
            Object index = Integer.valueOf(i);
            // Expression oldGetExp = new Expression(Array.class, "get", new Object[]{oldInstance, index});
            // Expression newGetExp = new Expression(Array.class, "get", new Object[]{newInstance, index});
            Expression oldGetExp = new Expression(oldInstance, "get", new Object[]{index});
            Expression newGetExp = new Expression(newInstance, "get", new Object[]{index});
            try {
                Object oldValue = oldGetExp.getValue();
                Object newValue = newGetExp.getValue();
                out.writeExpression(oldGetExp);
                if (!MetaData.equals(newValue, out.get(oldValue))) {
                    // System.out.println("Not equal: " + newGetExp + " != " + actualGetExp);
                    // invokeStatement(Array.class, "set", new Object[]{oldInstance, index, oldValue}, out);
                    DefaultPersistenceDelegate.invokeStatement(oldInstance, "set", new Object[]{index, oldValue}, out);
                }
            }
            catch (Exception e) {
                // System.err.println("Warning:: failed to write: " + oldGetExp);
                out.getExceptionListener().exceptionThrown(e);
            }
        }
    }
}

@SuppressWarnings({"rawtypes","unchecked"})
class ProxyPersistenceDelegate extends PersistenceDelegate {
    protected Expression instantiate(Object oldInstance, Encoder out) {
        Class type = oldInstance.getClass();
        java.lang.reflect.Proxy p = (java.lang.reflect.Proxy)oldInstance;
        // This unappealing hack is not required but makes the
        // representation of EventHandlers much more concise.
        java.lang.reflect.InvocationHandler ih = java.lang.reflect.Proxy.getInvocationHandler(p);
        if (ih instanceof EventHandler) {
            EventHandler eh = (EventHandler)ih;
            Vector args = new Vector();
            args.add(type.getInterfaces()[0]);
            args.add(eh.getTarget());
            args.add(eh.getAction());
            if (eh.getEventPropertyName() != null) {
                args.add(eh.getEventPropertyName());
            }
            if (eh.getListenerMethodName() != null) {
                args.setSize(4);
                args.add(eh.getListenerMethodName());
            }
            return new Expression(oldInstance,
                                  EventHandler.class,
                                  "create",
                                  args.toArray());
        }
        return new Expression(oldInstance,
                              java.lang.reflect.Proxy.class,
                              "newProxyInstance",
                              new Object[]{type.getClassLoader(),
                                           type.getInterfaces(),
                                           ih});
    }
}

// Strings
class java_lang_String_PersistenceDelegate extends PersistenceDelegate {
    protected Expression instantiate(Object oldInstance, Encoder out) { return null; }

    public void writeObject(Object oldInstance, Encoder out) {
        // System.out.println("NullPersistenceDelegate:writeObject " + oldInstance);
    }
}

// Classes
@SuppressWarnings({"rawtypes"})
class java_lang_Class_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        Class c = (Class)oldInstance;
        // As of 1.3 it is not possible to call Class.forName("int"),
        // so we have to generate different code for primitive types.
        // This is needed for arrays whose subtype may be primitive.
        if (c.isPrimitive()) {
            Field field = null;
            try {
                field = ReflectionUtils.typeToClass(c).getDeclaredField("TYPE");
            } catch (NoSuchFieldException ex) {
                System.err.println("Unknown primitive type: " + c);
            }
            return new Expression(oldInstance, field, "get", new Object[]{null});
        }
        else if (oldInstance == String.class) {
            return new Expression(oldInstance, "", "getClass", new Object[]{});
        }
        else if (oldInstance == Class.class) {
            return new Expression(oldInstance, String.class, "getClass", new Object[]{});
        }
        else {
            return new Expression(oldInstance, Class.class, "forName", new Object[]{c.getName()});
        }
    }
}

// Fields
class java_lang_reflect_Field_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        Field f = (Field)oldInstance;
        return new Expression(oldInstance,
                f.getDeclaringClass(),
                "getField",
                new Object[]{f.getName()});
    }
}

// Methods
class java_lang_reflect_Method_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        Method m = (Method)oldInstance;
        return new Expression(oldInstance,
                m.getDeclaringClass(),
                "getMethod",
                new Object[]{m.getName(), m.getParameterTypes()});
    }
}

// Dates

/**
 * The persistence delegate for {@code java.util.Date} classes.
 * Do not extend DefaultPersistenceDelegate to improve performance and
 * to avoid problems with {@code java.sql.Date},
 * {@code java.sql.Time} and {@code java.sql.Timestamp}.
 *
 * @author Sergey A. Malenkov
 */
class java_util_Date_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        if (!super.mutatesTo(oldInstance, newInstance)) {
            return false;
        }
        #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif oldDate = Datatypes.DATE_TIME_CLASS.cast(oldInstance);
        #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif newDate = Datatypes.DATE_TIME_CLASS.cast(newInstance);

        return oldDate.getTime() == newDate.getTime();
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif date = Datatypes.DATE_TIME_CLASS.cast(oldInstance);
        return new Expression(date, date.getClass(), "new", new Object[] {date.getTime()});
    }
}

/**
 * The persistence delegate for {@code java.sql.Timestamp} classes.
 * It supports nanoseconds.
 *
 * @author Sergey A. Malenkov
 */
final class java_sql_Timestamp_PersistenceDelegate extends java_util_Date_PersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        Timestamp oldTime = (Timestamp)oldInstance;
        Timestamp newTime = (Timestamp)newInstance;

        int nanos = oldTime.getNanos();
        if (nanos != newTime.getNanos()) {
            out.writeStatement(new Statement(oldTime, "setNanos", new Object[] {nanos}));
        }
    }
}

// Collections

/*
The Hashtable and AbstractMap classes have no common ancestor yet may
be handled with a single persistence delegate: one which uses the methods
of the Map insterface exclusively. Attatching the persistence delegates
to the interfaces themselves is fraught however since, in the case of
the Map, both the AbstractMap and HashMap classes are declared to
implement the Map interface, leaving the obvious implementation prone
to repeating their initialization. These issues and questions around
the ordering of delegates attached to interfaces have lead us to
ignore any delegates attached to interfaces and force all persistence
delegates to be registered with concrete classes.
*/

/**
 * The base class for persistence delegates for inner classes
 * that can be created using {@link Collections}.
 *
 * @author Sergey A. Malenkov
 */
@SuppressWarnings({"rawtypes","unchecked"})
abstract class java_util_Collections extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        if (!super.mutatesTo(oldInstance, newInstance)) {
            return false;
        }
        if ((oldInstance instanceof List) || (oldInstance instanceof Set) || (oldInstance instanceof Map)) {
            return oldInstance.equals(newInstance);
        }
        Collection oldC = (Collection) oldInstance;
        Collection newC = (Collection) newInstance;
        return (oldC.size() == newC.size()) && oldC.containsAll(newC);
    }

    static final class EmptyList_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            return new Expression(oldInstance, Collections.class, "emptyList", null);
        }
    }

    static final class EmptySet_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            return new Expression(oldInstance, Collections.class, "emptySet", null);
        }
    }

    static final class EmptyMap_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            return new Expression(oldInstance, Collections.class, "emptyMap", null);
        }
    }

    static final class SingletonList_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            List list = (List) oldInstance;
            return new Expression(oldInstance, Collections.class, "singletonList", new Object[]{list.get(0)});
        }
    }

    static final class SingletonSet_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Set set = (Set) oldInstance;
            return new Expression(oldInstance, Collections.class, "singleton", new Object[]{set.iterator().next()});
        }
    }

    static final class SingletonMap_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Map map = (Map) oldInstance;
            Object key = map.keySet().iterator().next();
            return new Expression(oldInstance, Collections.class, "singletonMap", new Object[]{key, map.get(key)});
        }
    }

    static final class UnmodifiableCollection_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
			List list = new ArrayList((Collection) oldInstance);
            return new Expression(oldInstance, Collections.class, "unmodifiableCollection", new Object[]{list});
        }
    }

    static final class UnmodifiableList_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            List list = new LinkedList((Collection) oldInstance);
            return new Expression(oldInstance, Collections.class, "unmodifiableList", new Object[]{list});
        }
    }

    static final class UnmodifiableRandomAccessList_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            List list = new ArrayList((Collection) oldInstance);
            return new Expression(oldInstance, Collections.class, "unmodifiableList", new Object[]{list});
        }
    }

    static final class UnmodifiableSet_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Set set = new HashSet((Set) oldInstance);
            return new Expression(oldInstance, Collections.class, "unmodifiableSet", new Object[]{set});
        }
    }

    static final class UnmodifiableSortedSet_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            SortedSet set = new TreeSet((SortedSet) oldInstance);
            return new Expression(oldInstance, Collections.class, "unmodifiableSortedSet", new Object[]{set});
        }
    }

    static final class UnmodifiableMap_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Map map = new HashMap((Map) oldInstance);
            return new Expression(oldInstance, Collections.class, "unmodifiableMap", new Object[]{map});
        }
    }

    static final class UnmodifiableSortedMap_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            SortedMap map = new TreeMap((SortedMap) oldInstance);
            return new Expression(oldInstance, Collections.class, "unmodifiableSortedMap", new Object[]{map});
        }
    }

    static final class SynchronizedCollection_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            List list = new ArrayList((Collection) oldInstance);
            return new Expression(oldInstance, Collections.class, "synchronizedCollection", new Object[]{list});
        }
    }

    static final class SynchronizedList_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            List list = new LinkedList((Collection) oldInstance);
            return new Expression(oldInstance, Collections.class, "synchronizedList", new Object[]{list});
        }
    }

    static final class SynchronizedRandomAccessList_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            List list = new ArrayList((Collection) oldInstance);
            return new Expression(oldInstance, Collections.class, "synchronizedList", new Object[]{list});
        }
    }

    static final class SynchronizedSet_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Set set = new HashSet((Set) oldInstance);
            return new Expression(oldInstance, Collections.class, "synchronizedSet", new Object[]{set});
        }
    }

    static final class SynchronizedSortedSet_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            SortedSet set = new TreeSet((SortedSet) oldInstance);
            return new Expression(oldInstance, Collections.class, "synchronizedSortedSet", new Object[]{set});
        }
    }

    static final class SynchronizedMap_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Map map = new HashMap((Map) oldInstance);
            return new Expression(oldInstance, Collections.class, "synchronizedMap", new Object[]{map});
        }
    }

    static final class SynchronizedSortedMap_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            SortedMap map = new TreeMap((SortedMap) oldInstance);
            return new Expression(oldInstance, Collections.class, "synchronizedSortedMap", new Object[]{map});
        }
    }

    static final class CheckedCollection_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Object type = MetaData.getPrivateFieldValue(oldInstance, "java.util.Collections$CheckedCollection.type");
            List list = new ArrayList((Collection) oldInstance);
            return new Expression(oldInstance, Collections.class, "checkedCollection", new Object[]{list, type});
        }
    }

    static final class CheckedList_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Object type = MetaData.getPrivateFieldValue(oldInstance, "java.util.Collections$CheckedCollection.type");
            List list = new LinkedList((Collection) oldInstance);
            return new Expression(oldInstance, Collections.class, "checkedList", new Object[]{list, type});
        }
    }

    static final class CheckedRandomAccessList_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Object type = MetaData.getPrivateFieldValue(oldInstance, "java.util.Collections$CheckedCollection.type");
            List list = new ArrayList((Collection) oldInstance);
            return new Expression(oldInstance, Collections.class, "checkedList", new Object[]{list, type});
        }
    }

    static final class CheckedSet_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Object type = MetaData.getPrivateFieldValue(oldInstance, "java.util.Collections$CheckedCollection.type");
            Set set = new HashSet((Set) oldInstance);
            return new Expression(oldInstance, Collections.class, "checkedSet", new Object[]{set, type});
        }
    }

    static final class CheckedSortedSet_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Object type = MetaData.getPrivateFieldValue(oldInstance, "java.util.Collections$CheckedCollection.type");
            SortedSet set = new TreeSet((SortedSet) oldInstance);
            return new Expression(oldInstance, Collections.class, "checkedSortedSet", new Object[]{set, type});
        }
    }

    static final class CheckedMap_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Object keyType   = MetaData.getPrivateFieldValue(oldInstance, "java.util.Collections$CheckedMap.keyType");
            Object valueType = MetaData.getPrivateFieldValue(oldInstance, "java.util.Collections$CheckedMap.valueType");
            Map map = new HashMap((Map) oldInstance);
            return new Expression(oldInstance, Collections.class, "checkedMap", new Object[]{map, keyType, valueType});
        }
    }

    static final class CheckedSortedMap_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Object keyType   = MetaData.getPrivateFieldValue(oldInstance, "java.util.Collections$CheckedMap.keyType");
            Object valueType = MetaData.getPrivateFieldValue(oldInstance, "java.util.Collections$CheckedMap.valueType");
            SortedMap map = new TreeMap((SortedMap) oldInstance);
            return new Expression(oldInstance, Collections.class, "checkedSortedMap", new Object[]{map, keyType, valueType});
        }
    }
}

/**
 * The persistence delegate for {@code java.util.EnumMap} classes.
 *
 * @author Sergey A. Malenkov
 */
class java_util_EnumMap_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return super.mutatesTo(oldInstance, newInstance) && (getType(oldInstance) == getType(newInstance));
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        return new Expression(oldInstance, EnumMap.class, "new", new Object[] {getType(oldInstance)});
    }

    private static Object getType(Object instance) {
        return MetaData.getPrivateFieldValue(instance, "java.util.EnumMap.keyType");
    }
}

/**
 * The persistence delegate for {@code java.util.EnumSet} classes.
 *
 * @author Sergey A. Malenkov
 */
class java_util_EnumSet_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return super.mutatesTo(oldInstance, newInstance) && (getType(oldInstance) == getType(newInstance));
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        return new Expression(oldInstance, EnumSet.class, "noneOf", new Object[] {getType(oldInstance)});
    }

    private static Object getType(Object instance) {
        return MetaData.getPrivateFieldValue(instance, "java.util.EnumSet.elementType");
    }
}

// Collection
@SuppressWarnings({"rawtypes"})
class java_util_Collection_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        java.util.Collection oldO = (java.util.Collection)oldInstance;
        java.util.Collection newO = (java.util.Collection)newInstance;

        if (newO.size() != 0) {
            invokeStatement(oldInstance, "clear", new Object[]{}, out);
        }
        for (Iterator i = oldO.iterator(); i.hasNext();) {
            invokeStatement(oldInstance, "add", new Object[]{i.next()}, out);
        }
    }
}

// List
@SuppressWarnings({"rawtypes"})
class java_util_List_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        java.util.List oldO = (java.util.List)oldInstance;
        java.util.List newO = (java.util.List)newInstance;
        int oldSize = oldO.size();
        int newSize = (newO == null) ? 0 : newO.size();
        if (oldSize < newSize) {
            invokeStatement(oldInstance, "clear", new Object[]{}, out);
            newSize = 0;
        }
        for (int i = 0; i < newSize; i++) {
            Object index = Integer.valueOf(i);

            Expression oldGetExp = new Expression(oldInstance, "get", new Object[]{index});
            Expression newGetExp = new Expression(newInstance, "get", new Object[]{index});
            try {
                Object oldValue = oldGetExp.getValue();
                Object newValue = newGetExp.getValue();
                out.writeExpression(oldGetExp);
                if (!MetaData.equals(newValue, out.get(oldValue))) {
                    invokeStatement(oldInstance, "set", new Object[]{index, oldValue}, out);
                }
            }
            catch (Exception e) {
                out.getExceptionListener().exceptionThrown(e);
            }
        }
        for (int i = newSize; i < oldSize; i++) {
            invokeStatement(oldInstance, "add", new Object[]{oldO.get(i)}, out);
        }
    }
}


// Map
@SuppressWarnings({"rawtypes"})
class java_util_Map_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        // System.out.println("Initializing: " + newInstance);
        java.util.Map oldMap = (java.util.Map)oldInstance;
        java.util.Map newMap = (java.util.Map)newInstance;
        // Remove the new elements.
        // Do this first otherwise we undo the adding work.
        if (newMap != null) {
            for ( Object newKey : newMap.keySet() ) {
               // PENDING: This "key" is not in the right environment.
                if (!oldMap.containsKey(newKey)) {
                    invokeStatement(oldInstance, "remove", new Object[]{newKey}, out);
                }
            }
        }
        // Add the new elements.
        for ( Object oldKey : oldMap.keySet() ) {
            Expression oldGetExp = new Expression(oldInstance, "get", new Object[]{oldKey});
            // Pending: should use newKey.
            Expression newGetExp = new Expression(newInstance, "get", new Object[]{oldKey});
            try {
                Object oldValue = oldGetExp.getValue();
                Object newValue = newGetExp.getValue();
                out.writeExpression(oldGetExp);
                if (!MetaData.equals(newValue, out.get(oldValue))) {
                    invokeStatement(oldInstance, "put", new Object[]{oldKey, oldValue}, out);
                } else if ((newValue == null) && !newMap.containsKey(oldKey)) {
                    // put oldValue(=null?) if oldKey is absent in newMap
                    invokeStatement(oldInstance, "put", new Object[]{oldKey, oldValue}, out);
                }
            }
            catch (Exception e) {
                out.getExceptionListener().exceptionThrown(e);
            }
        }
    }
}

class java_util_AbstractCollection_PersistenceDelegate extends java_util_Collection_PersistenceDelegate {}
class java_util_AbstractList_PersistenceDelegate extends java_util_List_PersistenceDelegate {}
class java_util_AbstractMap_PersistenceDelegate extends java_util_Map_PersistenceDelegate {}
class java_util_Hashtable_PersistenceDelegate extends java_util_Map_PersistenceDelegate {}


// Beans
class java_beans_beancontext_BeanContextSupport_PersistenceDelegate extends java_util_Collection_PersistenceDelegate {}

class StaticFieldsPersistenceDelegate extends PersistenceDelegate {
    protected void installFields(Encoder out, Class<?> cls) {
        Field fields[] = cls.getFields();
        for(int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            // Don't install primitives, their identity will not be preserved
            // by wrapping.
            if (Object.class.isAssignableFrom(field.getType())) {
                out.writeExpression(new Expression(field, "get", new Object[]{null}));
            }
        }
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        throw new RuntimeException("Unrecognized instance: " + oldInstance);
    }

    public void writeObject(Object oldInstance, Encoder out) {
        if (out.getAttribute(this) == null) {
            out.setAttribute(this, Boolean.TRUE);
            installFields(out, oldInstance.getClass());
        }
        super.writeObject(oldInstance, out);
    }
}

@SuppressWarnings({"rawtypes","unchecked"})
class MetaData {
    private static final Map<String,Field> fields = Collections.synchronizedMap(new WeakHashMap<String, Field>());
    private static Hashtable internalPersistenceDelegates = new Hashtable();
    private static Hashtable transientProperties = new Hashtable();

    private static PersistenceDelegate nullPersistenceDelegate = new NullPersistenceDelegate();
    private static PersistenceDelegate enumPersistenceDelegate = new EnumPersistenceDelegate();
    private static PersistenceDelegate primitivePersistenceDelegate = new PrimitivePersistenceDelegate();
    private static PersistenceDelegate defaultPersistenceDelegate = new DefaultPersistenceDelegate();
    private static PersistenceDelegate arrayPersistenceDelegate;
    private static PersistenceDelegate proxyPersistenceDelegate;

    static {

// Constructors.

  // beans

        registerConstructor("org.openms.uses.java.beans.Statement", new String[]{"target", "methodName", "arguments"});
        registerConstructor("org.openms.uses.java.beans.Expression", new String[]{"target", "methodName", "arguments"});
        registerConstructor("org.openms.uses.java.beans.EventHandler", new String[]{"target", "action", "eventPropertyName", "listenerMethodName"});

        internalPersistenceDelegates.put("java.sql.Date", new java_util_Date_PersistenceDelegate());
        internalPersistenceDelegates.put("java.sql.Time", new java_util_Date_PersistenceDelegate());

        internalPersistenceDelegates.put("java.util.JumboEnumSet", new java_util_EnumSet_PersistenceDelegate());
        internalPersistenceDelegates.put("java.util.RegularEnumSet", new java_util_EnumSet_PersistenceDelegate());

    }

    /*pp*/ static boolean equals(Object o1, Object o2) {
        return (o1 == null) ? (o2 == null) : o1.equals(o2);
    }

    public synchronized static PersistenceDelegate getPersistenceDelegate(Class type) {
        if (type == null) {
            return nullPersistenceDelegate;
        }
        if (Enum.class.isAssignableFrom(type)) {
            return enumPersistenceDelegate;
        }
        if (ReflectionUtils.isPrimitive(type)) {
            return primitivePersistenceDelegate;
        }
        // The persistence delegate for arrays is non-trivial; instantiate it lazily.
        if (type.isArray()) {
            if (arrayPersistenceDelegate == null) {
                arrayPersistenceDelegate = new ArrayPersistenceDelegate();
            }
            return arrayPersistenceDelegate;
        }
        // Handle proxies lazily for backward compatibility with 1.2.
        try {
            if (java.lang.reflect.Proxy.isProxyClass(type)) {
                if (proxyPersistenceDelegate == null) {
                    proxyPersistenceDelegate = new ProxyPersistenceDelegate();
                }
                return proxyPersistenceDelegate;
            }
        }
        catch(Exception e) {}
        // else if (type.getDeclaringClass() != null) {
        //     return new DefaultPersistenceDelegate(new String[]{"this$0"});
        // }

        String typeName = type.getName();

        // Check to see if there are properties that have been lazily registered for removal.
        if (getBeanAttribute(type, "transient_init") == null) {
            Vector tp = (Vector)transientProperties.get(typeName);
            if (tp != null) {
                for(int i = 0; i < tp.size(); i++) {
                    setPropertyAttribute(type, (String)tp.get(i), "transient", Boolean.TRUE);
                }
            }
            setBeanAttribute(type, "transient_init", Boolean.TRUE);
        }

        PersistenceDelegate pd = (PersistenceDelegate)getBeanAttribute(type, "persistenceDelegate");
        if (pd == null) {
            pd = (PersistenceDelegate)internalPersistenceDelegates.get(typeName);
            if (pd != null) {
                return pd;
            }
            internalPersistenceDelegates.put(typeName, defaultPersistenceDelegate);
            try {
                String name =  type.getName();
                Class c = Class.forName("org.openmdx.dalvik.uses.java.beans." + name.replace('.', '_')
                                        + "_PersistenceDelegate");
                pd = (PersistenceDelegate)c.newInstance();
                internalPersistenceDelegates.put(typeName, pd);
            }
            catch (ClassNotFoundException e) {
                String[] properties = getConstructorProperties(type);
                if (properties != null) {
                    pd = new DefaultPersistenceDelegate(properties);
                    internalPersistenceDelegates.put(typeName, pd);
                }
            }
            catch (Exception e) {
                System.err.println("Internal error: " + e);
            }
        }

        return (pd != null) ? pd : defaultPersistenceDelegate;
    }

    private static String[] getConstructorProperties(Class type) {
        String[] names = null;
        int length = 0;
        for (Constructor<?> constructor : type.getConstructors()) {
            String[] value = getAnnotationValue(constructor);
            if ((value != null) && (length < value.length) && isValid(constructor, value)) {
                names = value;
                length = value.length;
            }
        }
        return names;
    }

    private static String[] getAnnotationValue(Constructor<?> constructor) {
        return null; // no ConstructorProperties support
    }

    private static boolean isValid(Constructor<?> constructor, String[] names) {
        Class[] parameters = constructor.getParameterTypes();
        if (names.length != parameters.length) {
            return false;
        }
        for (String name : names) {
            if (name == null) {
                return false;
            }
        }
        return true;
    }

    // Wrapper for Introspector.getBeanInfo to handle exception handling.
    // Note: this relys on new 1.4 Introspector semantics which cache the BeanInfos
    public static BeanInfo getBeanInfo(Class type) {
        BeanInfo info = null;
        try {
            info = Introspector.getBeanInfo(type);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return info;
    }

    private static PropertyDescriptor getPropertyDescriptor(Class type, String propertyName) {
        BeanInfo info = getBeanInfo(type);
        PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
        // System.out.println("Searching for: " + propertyName + " in " + type);
        for(int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor pd  = propertyDescriptors[i];
            if (propertyName.equals(pd.getName())) {
                return pd;
            }
        }
        return null;
    }

    private static void setPropertyAttribute(Class type, String property, String attribute, Object value) {
        PropertyDescriptor pd = getPropertyDescriptor(type, property);
        if (pd == null) {
            System.err.println("Warning: property " + property + " is not defined on " + type);
            return;
        }
        pd.setValue(attribute, value);
    }

    private static void setBeanAttribute(Class type, String attribute, Object value) {
        getBeanInfo(type).getBeanDescriptor().setValue(attribute, value);
    }

    private static Object getBeanAttribute(Class type, String attribute) {
        return getBeanInfo(type).getBeanDescriptor().getValue(attribute);
    }

    // MetaData registration

    private synchronized static void registerConstructor(String typeName,
                                                         String[] constructor) {
        internalPersistenceDelegates.put(typeName,
                                         new DefaultPersistenceDelegate(constructor));
    }

    static Object getPrivateFieldValue(Object instance, String name) {
        Field field = fields.get(name);
        if (field == null) {
            int index = name.lastIndexOf('.');
            final String className = name.substring(0, index);
            final String fieldName = name.substring(1 + index);
            field = AccessController.doPrivileged(new PrivilegedAction<Field>() {
                public Field run() {
                    try {
                        Field field = Class.forName(className).getDeclaredField(fieldName);
                        field.setAccessible(true);
                        return field;
                    }
                    catch (ClassNotFoundException exception) {
                        throw new IllegalStateException("Could not find class", exception);
                    }
                    catch (NoSuchFieldException exception) {
                        throw new IllegalStateException("Could not find field", exception);
                    }
                }
            });
            fields.put(name, field);
        }
        try {
            return field.get(instance);
        }
        catch (IllegalAccessException exception) {
            throw new IllegalStateException("Could not get value of the field", exception);
        }
    }
}
