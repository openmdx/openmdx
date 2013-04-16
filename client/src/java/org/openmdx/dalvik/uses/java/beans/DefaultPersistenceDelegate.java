/*
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.openmdx.dalvik.uses.sun.reflect.misc.MethodUtil;


/**
 * The <code>DefaultPersistenceDelegate</code> is a concrete implementation of
 * the abstract <code>PersistenceDelegate</code> class and
 * is the delegate used by default for classes about
 * which no information is available. The <code>DefaultPersistenceDelegate</code>
 * provides, version resilient, public API-based persistence for
 * classes that follow the JavaBeans conventions without any class specific
 * configuration.
 * <p>
 * The key assumptions are that the class has a nullary constructor
 * and that its state is accurately represented by matching pairs
 * of "setter" and "getter" methods in the order they are returned
 * by the Introspector.
 * In addition to providing code-free persistence for JavaBeans,
 * the <code>DefaultPersistenceDelegate</code> provides a convenient means
 * to effect persistent storage for classes that have a constructor
 * that, while not nullary, simply requires some property values
 * as arguments.
 *
 * @see #DefaultPersistenceDelegate(String[])
 * @see org.openmdx.dalvik.uses.java.beans.Introspector
 * 
 * <p>
 * openMDX/Dalvik Notice (January 2013):<br>
 * THIS CODE HAS BEEN MODIFIED AND ITS NAMESPACE HAS BEEN PREFIXED WITH
 * <code>org.openmdx.dalvik.uses.</code>
 * </p>
 * @since openMDX 2.12.0
 * @author openMDX Team
 *
 * @author Philip Milne
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class DefaultPersistenceDelegate extends PersistenceDelegate {
    private String[] constructor;
    private Boolean definesEquals;

    /**
     * Creates a persistence delegate for a class with a nullary constructor.
     *
     * @see #DefaultPersistenceDelegate(java.lang.String[])
     */
    public DefaultPersistenceDelegate() {
        this(new String[0]);
    }

    /**
     * Creates a default persistence delegate for a class with a
     * constructor whose arguments are the values of the property
     * names as specified by <code>constructorPropertyNames</code>.
     * The constructor arguments are created by
     * evaluating the property names in the order they are supplied.
     * To use this class to specify a single preferred constructor for use
     * in the serialization of a particular type, we state the
     * names of the properties that make up the constructor's
     * arguments. For example, the <code>Font</code> class which
     * does not define a nullary constructor can be handled
     * with the following persistence delegate:
     *
     * <pre>
     *     new DefaultPersistenceDelegate(new String[]{"name", "style", "size"});
     * </pre>
     *
     * @param  constructorPropertyNames The property names for the arguments of this constructor.
     *
     * @see #instantiate
     */
    public DefaultPersistenceDelegate(String[] constructorPropertyNames) {
        this.constructor = constructorPropertyNames;
    }

    private static boolean definesEquals(Class type) {
        try {
            return type == type.getMethod("equals", Object.class).getDeclaringClass();
        }
        catch(NoSuchMethodException e) {
            return false;
        }
    }

    private boolean definesEquals(Object instance) {
        if (definesEquals != null) {
            return (definesEquals == Boolean.TRUE);
        }
        else {
            boolean result = definesEquals(instance.getClass());
            definesEquals = result ? Boolean.TRUE : Boolean.FALSE;
            return result;
        }
    }

    /**
     * If the number of arguments in the specified constructor is non-zero and
     * the class of <code>oldInstance</code> explicitly declares an "equals" method
     * this method returns the value of <code>oldInstance.equals(newInstance)</code>.
     * Otherwise, this method uses the superclass's definition which returns true if the
     * classes of the two instances are equal.
     *
     * @param oldInstance The instance to be copied.
     * @param newInstance The instance that is to be modified.
     * @return True if an equivalent copy of <code>newInstance</code> may be
     *         created by applying a series of mutations to <code>oldInstance</code>.
     *
     * @see #DefaultPersistenceDelegate(String[])
     */
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        // Assume the instance is either mutable or a singleton
        // if it has a nullary constructor.
        return (constructor.length == 0) || !definesEquals(oldInstance) ?
            super.mutatesTo(oldInstance, newInstance) :
            oldInstance.equals(newInstance);
    }

    /**
     * This default implementation of the <code>instantiate</code> method returns
     * an expression containing the predefined method name "new" which denotes a
     * call to a constructor with the arguments as specified in
     * the <code>DefaultPersistenceDelegate</code>'s constructor.
     *
     * @param  oldInstance The instance to be instantiated.
     * @param  out The code output stream.
     * @return An expression whose value is <code>oldInstance</code>.
     *
     * @see #DefaultPersistenceDelegate(String[])
     */
    protected Expression instantiate(Object oldInstance, Encoder out) {
        int nArgs = constructor.length;
        Class type = oldInstance.getClass();
        Object[] constructorArgs = new Object[nArgs];
        for(int i = 0; i < nArgs; i++) {
            try {
                Method method = findMethod(type, this.constructor[i]);
                constructorArgs[i] = MethodUtil.invoke(method, oldInstance, new Object[0]);
            }
            catch (Exception e) {
                out.getExceptionListener().exceptionThrown(e);
            }
        }
        return new Expression(oldInstance, oldInstance.getClass(), "new", constructorArgs);
    }

    private Method findMethod(Class type, String property) throws IntrospectionException {
        if (property == null) {
            throw new IllegalArgumentException("Property name is null");
        }
        BeanInfo info = Introspector.getBeanInfo(type);
        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (property.equals(pd.getName())) {
                Method method = pd.getReadMethod();
                if (method != null) {
                    return method;
                }
                throw new IllegalStateException("Could not find getter for the property " + property);
            }
        }
        throw new IllegalStateException("Could not find property by the name " + property);
    }

    // This is a workaround for a bug in the introspector.
    // PropertyDescriptors are not shared amongst subclasses.
    private boolean isTransient(Class type, PropertyDescriptor pd) {
        if (type == null) {
            return false;
        }
        // This code was mistakenly deleted - it may be fine and
        // is more efficient than the code below. This should
        // all disappear anyway when property descriptors are shared
        // by the introspector.
        /*
        Method getter = pd.getReadMethod();
        Class declaringClass = getter.getDeclaringClass();
        if (declaringClass == type) {
            return Boolean.TRUE.equals(pd.getValue("transient"));
        }
        */
        String pName = pd.getName();
        BeanInfo info = MetaData.getBeanInfo(type);
        PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
        for (int i = 0; i < propertyDescriptors.length; ++i ) {
            PropertyDescriptor pd2 = propertyDescriptors[i];
            if (pName.equals(pd2.getName())) {
                Object value = pd2.getValue("transient");
                if (value != null) {
                    return Boolean.TRUE.equals(value);
                }
            }
        }
        return isTransient(type.getSuperclass(), pd);
    }

    private static boolean equals(Object o1, Object o2) {
        return (o1 == null) ? (o2 == null) : o1.equals(o2);
    }

    private void doProperty(Class type, PropertyDescriptor pd, Object oldInstance, Object newInstance, Encoder out) throws Exception {
        Method getter = pd.getReadMethod();
        Method setter = pd.getWriteMethod();

        if (getter != null && setter != null && !isTransient(type, pd)) {
            Expression oldGetExp = new Expression(oldInstance, getter.getName(), new Object[]{});
            Expression newGetExp = new Expression(newInstance, getter.getName(), new Object[]{});
            Object oldValue = oldGetExp.getValue();
            Object newValue = newGetExp.getValue();
            out.writeExpression(oldGetExp);
            if (!equals(newValue, out.get(oldValue))) {
                // Search for a static constant with this value;
                Object e = (Object[])pd.getValue("enumerationValues");
                if (e instanceof Object[] && Array.getLength(e) % 3 == 0) {
                    Object[] a = (Object[])e;
                    for(int i = 0; i < a.length; i = i + 3) {
                        try {
                           Field f = type.getField((String)a[i]);
                           if (f.get(null).equals(oldValue)) {
                               out.remove(oldValue);
                               out.writeExpression(new Expression(oldValue, f, "get", new Object[]{null}));
                           }
                        }
                        catch (Exception ex) {}
                    }
                }
                invokeStatement(oldInstance, setter.getName(), new Object[]{oldValue}, out);
            }
        }
    }

    static void invokeStatement(Object instance, String methodName, Object[] args, Encoder out) {
        out.writeStatement(new Statement(instance, methodName, args));
    }

    // Write out the properties of this instance.
    private void initBean(Class type, Object oldInstance, Object newInstance, Encoder out) {
        // System.out.println("initBean: " + oldInstance);
        BeanInfo info = MetaData.getBeanInfo(type);

        // Properties
        PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
        for (int i = 0; i < propertyDescriptors.length; ++i ) {
            try {
                doProperty(type, propertyDescriptors[i], oldInstance, newInstance, out);
            }
            catch (Exception e) {
                out.getExceptionListener().exceptionThrown(e);
            }
        }
        // AWT handling removed
    }

    /**
     * This default implementation of the <code>initialize</code> method assumes
     * all state held in objects of this type is exposed via the
     * matching pairs of "setter" and "getter" methods in the order
     * they are returned by the Introspector. If a property descriptor
     * defines a "transient" attribute with a value equal to
     * <code>Boolean.TRUE</code> the property is ignored by this
     * default implementation. Note that this use of the word
     * "transient" is quite independent of the field modifier
     * that is used by the <code>ObjectOutputStream</code>.
     * <p>
     * For each non-transient property, an expression is created
     * in which the nullary "getter" method is applied
     * to the <code>oldInstance</code>. The value of this
     * expression is the value of the property in the instance that is
     * being serialized. If the value of this expression
     * in the cloned environment <code>mutatesTo</code> the
     * target value, the new value is initialized to make it
     * equivalent to the old value. In this case, because
     * the property value has not changed there is no need to
     * call the corresponding "setter" method and no statement
     * is emitted. If not however, the expression for this value
     * is replaced with another expression (normally a constructor)
     * and the corresponding "setter" method is called to install
     * the new property value in the object. This scheme removes
     * default information from the output produced by streams
     * using this delegate.
     * <p>
     * In passing these statements to the output stream, where they
     * will be executed, side effects are made to the <code>newInstance</code>.
     * In most cases this allows the problem of properties
     * whose values depend on each other to actually help the
     * serialization process by making the number of statements
     * that need to be written to the output smaller. In general,
     * the problem of handling interdependent properties is reduced to
     * that of finding an order for the properties in
     * a class such that no property value depends on the value of
     * a subsequent property.
     *
     * @param oldInstance The instance to be copied.
     * @param newInstance The instance that is to be modified.
     * @param out The stream to which any initialization statements should be written.
     *
     * @see org.openmdx.dalvik.uses.java.beans.Introspector#getBeanInfo
     * @see org.openmdx.dalvik.uses.java.beans.PropertyDescriptor
     */
    protected void initialize(Class<?> type,
                              Object oldInstance, Object newInstance,
                              Encoder out)
    {
        // System.out.println("DefulatPD:initialize" + type);
        super.initialize(type, oldInstance, newInstance, out);
        if (oldInstance.getClass() == type) { // !type.isInterface()) {
            initBean(type, oldInstance, newInstance, out);
        }
    }
}
