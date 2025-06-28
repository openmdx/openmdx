/*
 * Copyright (c) 2005, 2009, Oracle and/or its affiliates. All rights reserved.
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

package org.openmdx.dalvik.uses.sun.reflect.misc;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.SecureClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openmdx.kernel.log.SysLog;

/* 
 * Create a trampoline class.
 * 
 * <p>
 * openMDX/Dalvik Notice (January 2013):<br>
 * THIS CODE HAS BEEN MODIFIED AND ITS NAMESPACE HAS BEEN PREFIXED WITH
 * {@code org.openmdx.dalvik.uses.}

 * </p>
 * @since openMDX 2.12
 * @author openMDX Team
 */
@SuppressWarnings({"rawtypes","unchecked"})
public final class MethodUtil extends SecureClassLoader {
    private static String TRAMPOLINE = "org.openmdx.dalvik.uses.sun.reflect.misc.Trampoline";
    private static Method bounce = getTrampoline();

    private MethodUtil() {
        super();
    }

    /*
     * Discover the public methods on public classes
     * and interfaces accessible to any caller by calling
     * Class.getMethods() and walking towards Object until
     * we're done.
     */
     public static Method[] getPublicMethods(Class cls) {
        // compatibility for update release
        if (System.getSecurityManager() == null) {
            return cls.getMethods();
        }
        Map sigs = new HashMap();
        while (cls != null) {
            boolean done = getInternalPublicMethods(cls, sigs);
            if (done) {
                break;
            }
            getInterfaceMethods(cls, sigs);
            cls = cls.getSuperclass();
        }
        Collection c = sigs.values();
        return (Method[]) c.toArray(new Method[c.size()]);
    }

    /*
     * Process the immediate interfaces of this class or interface.
     */
    private static void getInterfaceMethods(Class cls, Map sigs) {
        Class[] intfs = cls.getInterfaces();
        for (int i=0; i < intfs.length; i++) {
            Class intf = intfs[i];
            boolean done = getInternalPublicMethods(intf, sigs);
            if (!done) {
                getInterfaceMethods(intf, sigs);
            }
        }
    }

    /*
     *
     * Process the methods in this class or interface
     */
    private static boolean getInternalPublicMethods(Class cls, Map sigs) {
        Method[] methods = null;
        try {
            /*
             * This class or interface is non-public so we
             * can't use any of it's methods. Go back and
             * try again with a superclass or superinterface.
             */
            if (!Modifier.isPublic(cls.getModifiers())) {
                return false;
            }
            if (!ReflectUtil.isPackageAccessible(cls)) {
                return false;
            }

            methods = cls.getMethods();
        } catch (SecurityException se) {
            return false;
        }

        /*
         * Check for inherited methods with non-public
         * declaring classes. They might override and hide
         * methods from their superclasses or
         * superinterfaces.
         */
        boolean done = true;
        for (int i=0; i < methods.length; i++) {
            Class dc = methods[i].getDeclaringClass();
            if (!Modifier.isPublic(dc.getModifiers())) {
                done = false;
                break;
            }
        }

        if (done) {
            /*
             * We're done. Spray all the methods into
             * the list and then we're out of here.
             */
            for (int i=0; i < methods.length; i++) {
                addMethod(sigs, methods[i]);
            }
        } else {
            /*
             * Simulate cls.getDeclaredMethods() by
             * stripping away inherited methods.tram
             */
            for (int i=0; i < methods.length; i++) {
                Class dc = methods[i].getDeclaringClass();
                if (cls.equals(dc)) {
                    addMethod(sigs, methods[i]);
                }
            }
        }
        return done;
    }

    private static void addMethod(Map sigs, Method method) {
        Signature signature = new Signature(method);
        if (!sigs.containsKey(signature)) {
            sigs.put(signature, method);
        } else if (!method.getDeclaringClass().isInterface()){
            /*
             * Superclasses beat interfaces.
             */
            Method old = (Method)sigs.get(signature);
            if (old.getDeclaringClass().isInterface()) {
                sigs.put(signature, method);
            }
        }
    }

    /**
     * A class that represents the unique elements of a method that will be a
     * key in the method cache.
     */
    private static class Signature {
        private String methodName;
        private Class[] argClasses;

        private volatile int hashCode = 0;

        Signature(Method m) {
            this.methodName = m.getName();
            this.argClasses = m.getParameterTypes();
        }

        public boolean equals(Object o2) {
        	if (null == o2) {
        		return false;
        	}
            if (this == o2) {
                return true;
            }
            Signature that = (Signature)o2;
            if (!(methodName.equals(that.methodName))) {
                return false;
            }
            if (argClasses.length != that.argClasses.length) {
                return false;
            }
            for (int i = 0; i < argClasses.length; i++) {
                if (!(argClasses[i] == that.argClasses[i])) {
                  return false;
                }
            }
            return true;
        }

        /**
         * Hash code computed using algorithm suggested in
         * Effective Java, Item 8.
         */
        public int hashCode() {
            if (hashCode == 0) {
                int result = 17;
                result = 37 * result + methodName.hashCode();
                if (argClasses != null) {
                    for (int i = 0; i < argClasses.length; i++) {
                        result = 37 * result + ((argClasses[i] == null) ? 0 :
                            argClasses[i].hashCode());
                    }
                }
                hashCode = result;
            }
            return hashCode;
        }
    }


    /*
     * Bounce through the trampoline.
     * <p>
	 * openMDX/Dalvik Notice (January 2013):<br>
	 * THIS CODE HAS BEEN MODIFIED: bounce might be null now
	 * {@code org.openmdx.dalvik.uses.}

	 * </p>
     */
    public static Object invoke(
    	Method m, 
    	Object obj, 
    	Object[] params
    ) throws InvocationTargetException, IllegalAccessException {
        if (m.getDeclaringClass().equals(AccessController.class) ||
            m.getDeclaringClass().equals(Method.class))
            throw new InvocationTargetException(
                new UnsupportedOperationException("invocation not supported"));
        try {
        	if(bounce == null) {
        		return m.invoke(obj, params);
        	} else {
	            return bounce.invoke(null, new Object[] {m, obj, params});
        	}
        } catch (InvocationTargetException ie) {
            Throwable t = ie.getCause();

            if (t instanceof InvocationTargetException) {
                throw (InvocationTargetException)t;
            } else if (t instanceof IllegalAccessException) {
                throw (IllegalAccessException)t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException)t;
            } else if (t instanceof Error) {
                throw (Error)t;
            } else {
                throw new Error("Unexpected invocation error", t);
            }
        } catch (IllegalAccessException iae) {
        	if(bounce == null) {
	            throw new Error("Trampoline is missing", iae);
        	} else {
	            // this can't happen
	            throw new Error("Unexpected invocation error", iae);
        	}
        }
    }

    private static Method getTrampoline() {
        Method tramp = null;
        try {
            tramp = (Method) AccessController.doPrivileged(
            	new PrivilegedExceptionAction() {
	                public Object run() throws Exception {
	                    Class t = getTrampolineClass();
	                    Class[] types = new Class[] {Method.class, Object.class, Object[].class};
	                    Method b = t.getDeclaredMethod("invoke", types);
	                    ((AccessibleObject)b).setAccessible(true);
	                    return b;
	                }
	            }
            );
        } catch (PrivilegedActionException exception) {
            SysLog.warning("bouncer cannot be found", exception.getException());
        } catch (RuntimeException exception) {
            SysLog.warning("bouncer cannot be found", exception);
        }
        return tramp;
    }

    protected synchronized Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        // First, check if the class has already been loaded
        ReflectUtil.checkPackageAccess(name);
        Class c = findLoadedClass(name);
        if (c == null) {
            try {
                c = findClass(name);
            } catch (ClassNotFoundException e) {
                // Fall through ...
            }
            if (c == null) {
                c = getParent().loadClass(name);
            }
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }

    protected PermissionCollection getPermissions(CodeSource codesource)
    {
        PermissionCollection perms = super.getPermissions(codesource);
        perms.add(new AllPermission());
        return perms;
    }

    private static Class getTrampolineClass(
    ) throws ClassNotFoundException {
            return Class.forName(TRAMPOLINE, true, new MethodUtil());
    }

}
