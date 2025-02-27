/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ------------------
 * 
 * This class is derived from javax.jdo.JDOHelper.
 */
package org.openmdx.kernel.jdo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.jdo.Constants;
import javax.jdo.JDOEnhancer;
import javax.jdo.JDOException;
import javax.jdo.JDOFatalInternalException;
import javax.jdo.JDOFatalUserException;
import javax.jdo.ObjectState;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.spi.I18NHelper;
import javax.jdo.spi.JDOImplHelper;
import javax.jdo.spi.JDOImplHelper.StateInterrogationBooleanReturn;
import javax.jdo.spi.JDOImplHelper.StateInterrogationObjectReturn;
import javax.jdo.spi.PersistenceCapable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.openmdx.kernel.loading.Resources;
import org.openmdx.uses.javax.jdo.LegacyJava;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class can be used by a JDO-aware application to call the JDO behavior of <code>
 * PersistenceCapable</code> instances without declaring them to be <code>PersistenceCapable</code>.
 *
 * <p>It is also used to acquire a <code>PersistenceManagerFactory</code> via various methods.
 *
 * <p>This helper class defines static methods that allow a JDO-aware application to examine the
 * runtime state of instances. For example, an application can discover whether the instance is
 * persistent, transactional, dirty, new, deleted, or detached; and to get its associated <code>
 * PersistenceManager</code> if it has one.
 *
 * <p>
 * As opposed to javax.jdo.JDOHelper this class<ul>
 * <li>does not depend on javax.naming</li>
 * <li>lacks the methods depending on javax.naming</li>
 * <li>does not provide an instance</li>
 * <li>includes additional object id accessors</li>
 * <li>returns {@code JDOPersistenceManagerFactory} instances
 * instead of {@code PersistenceManagerFactory} instances</li>
 * </ul>
 *
 * @version 2.1
 */
public class ReducedJDOHelper implements Constants {

    /**
     * The name of the standard service configuration resource text file containing
     * the name of an implementation of {@link PersistenceManagerFactory}.
     * Constant value ends in {@code services/javax.jdo.PersistenceManagerFactory}.
     *
     * @since JDO 2.1
     */
    static String SERVICE_LOOKUP_PMF_RESOURCE_NAME = Resources.toMetaInfPath("services/javax.jdo.PersistenceManagerFactory");

	
    /**
     * The name of the standard service configuration resource text file containing the name of an
     * enhancer of {@link JDOEnhancer}. Constant value ends in {@code services/javax.jdo.JDOEnhancer}.
     *
     * @since JDO 3.0
     */
	static String SERVICE_LOOKUP_ENHANCER_RESOURCE_NAME = Resources.toMetaInfPath("services/javax.jdo.JDOEnhancer");

	  /** A mapping from jdoconfig.xsd element attributes to PMF properties. */
	  static final Map<String, String> ATTRIBUTE_PROPERTY_XREF = createAttributePropertyXref();

	/**
     * The name of the standard JDO configuration resource file(s).
     * Constant value is {@code META-INF/jdoconfig.xml}.
     *
     * @since JDO 2.1
     */
    static String JDOCONFIG_RESOURCE_NAME = Resources.toMetaInfPath("jdoconfig.xml");

    /** The Internationalization message helper. */
    private static final I18NHelper MSG = I18NHelper.getInstance("javax.jdo.Bundle"); // NOI18N
    
    private static final String EXC_GET_PMF_IOEXCEPTION_RSRC = "EXC_GetPMFIOExceptionRsrc"; // NOI18N

    private static final String EXC_GET_PMF_UNEXPECTED_EXCEPTION = "EXC_GetPMFUnexpectedException"; // NOI18N

    /**
     * Creates a map from jdoconfig.xsd element attributes to PMF properties.
     *
     * @return An unmodifiable Map of jdoconfig.xsd element attributes to PMF properties.
     */
    static Map<String, String> createAttributePropertyXref() {
        Map<String, String> xref = new HashMap<>();

        xref.put(Constants.PMF_ATTRIBUTE_CLASS, Constants.PROPERTY_PERSISTENCE_MANAGER_FACTORY_CLASS);
        xref.put(
                Constants.PMF_ATTRIBUTE_CONNECTION_DRIVER_NAME, Constants.PROPERTY_CONNECTION_DRIVER_NAME);
        xref.put(
                Constants.PMF_ATTRIBUTE_CONNECTION_FACTORY_NAME,
                Constants.PROPERTY_CONNECTION_FACTORY_NAME);
        xref.put(
                Constants.PMF_ATTRIBUTE_CONNECTION_FACTORY2_NAME,
                Constants.PROPERTY_CONNECTION_FACTORY2_NAME);
        xref.put(Constants.PMF_ATTRIBUTE_CONNECTION_PASSWORD, Constants.PROPERTY_CONNECTION_PASSWORD);
        xref.put(Constants.PMF_ATTRIBUTE_CONNECTION_URL, Constants.PROPERTY_CONNECTION_URL);
        xref.put(Constants.PMF_ATTRIBUTE_CONNECTION_USER_NAME, Constants.PROPERTY_CONNECTION_USER_NAME);
        xref.put(Constants.PMF_ATTRIBUTE_IGNORE_CACHE, Constants.PROPERTY_IGNORE_CACHE);
        xref.put(Constants.PMF_ATTRIBUTE_MAPPING, Constants.PROPERTY_MAPPING);
        xref.put(Constants.PMF_ATTRIBUTE_MULTITHREADED, Constants.PROPERTY_MULTITHREADED);
        xref.put(
                Constants.PMF_ATTRIBUTE_NONTRANSACTIONAL_READ, Constants.PROPERTY_NONTRANSACTIONAL_READ);
        xref.put(
                Constants.PMF_ATTRIBUTE_NONTRANSACTIONAL_WRITE, Constants.PROPERTY_NONTRANSACTIONAL_WRITE);
        xref.put(Constants.PMF_ATTRIBUTE_OPTIMISTIC, Constants.PROPERTY_OPTIMISTIC);
        xref.put(
                Constants.PMF_ATTRIBUTE_PERSISTENCE_UNIT_NAME, Constants.PROPERTY_PERSISTENCE_UNIT_NAME);
        xref.put(Constants.PMF_ATTRIBUTE_NAME, Constants.PROPERTY_NAME);
        xref.put(Constants.PMF_ATTRIBUTE_RESTORE_VALUES, Constants.PROPERTY_RESTORE_VALUES);
        xref.put(Constants.PMF_ATTRIBUTE_RETAIN_VALUES, Constants.PROPERTY_RETAIN_VALUES);
        xref.put(Constants.PMF_ATTRIBUTE_DETACH_ALL_ON_COMMIT, Constants.PROPERTY_DETACH_ALL_ON_COMMIT);
        xref.put(Constants.PMF_ATTRIBUTE_SERVER_TIME_ZONE_ID, Constants.PROPERTY_SERVER_TIME_ZONE_ID);
        xref.put(
                Constants.PMF_ATTRIBUTE_DATASTORE_READ_TIMEOUT_MILLIS,
                Constants.PROPERTY_DATASTORE_READ_TIMEOUT_MILLIS);
        xref.put(
                Constants.PMF_ATTRIBUTE_DATASTORE_WRITE_TIMEOUT_MILLIS,
                Constants.PROPERTY_DATASTORE_WRITE_TIMEOUT_MILLIS);

        return Collections.unmodifiableMap(xref);
    }

    /** The JDOImplHelper instance used for handling non-binary-compatible implementations. */
    private static final JDOImplHelper IMPL_HELPER =
            doPrivileged((PrivilegedAction<JDOImplHelper>) JDOImplHelper::getInstance);

    /**
     * The stateless instance used for handling non-binary-compatible implementations of
     * getPersistenceManager.
     */
    static final StateInterrogationObjectReturn getPersistenceManager =
            (pc, si) -> si.getPersistenceManager(pc);

    /**
     * The stateless instance used for handling non-binary-compatible implementations of getObjectId.
     */
    static final StateInterrogationObjectReturn getObjectId = (pc, si) -> si.getObjectId(pc);

    /**
     * The stateless instance used for handling non-binary-compatible implementations of
     * getTransactionalObjectId.
     */
    static final StateInterrogationObjectReturn getTransactionalObjectId =
            (pc, si) -> si.getTransactionalObjectId(pc);

    /**
     * The stateless instance used for handling non-binary-compatible implementations of getVersion.
     */
    static final StateInterrogationObjectReturn getVersion = (pc, si) -> si.getVersion(pc);

    /**
     * The stateless instance used for handling non-binary-compatible implementations of isPersistent.
     */
    static final StateInterrogationBooleanReturn isPersistent = (pc, si) -> si.isPersistent(pc);

    /**
     * The stateless instance used for handling non-binary-compatible implementations of
     * isTransactional.
     */
    static final StateInterrogationBooleanReturn isTransactional = (pc, si) -> si.isTransactional(pc);

    /** The stateless instance used for handling non-binary-compatible implementations of isDirty. */
    static final StateInterrogationBooleanReturn isDirty = (pc, si) -> si.isDirty(pc);

    /** The stateless instance used for handling non-binary-compatible implementations of isNew. */
    static final StateInterrogationBooleanReturn isNew = (pc, si) -> si.isNew(pc);

    /**
     * The stateless instance used for handling non-binary-compatible implementations of isDeleted.
     */
    static final StateInterrogationBooleanReturn isDeleted = (pc, si) -> si.isDeleted(pc);

    /**
     * The stateless instance used for handling non-binary-compatible implementations of isDetached.
     */
    static final StateInterrogationBooleanReturn isDetached = (pc, si) -> si.isDetached(pc);

    /**
     * Return the associated <code>PersistenceManager</code> if there is one. Transactional and
     * persistent instances return the associated <code>PersistenceManager</code>.
     *
     * <p>Transient non-transactional instances and instances of classes that do not implement <code>
     * PersistenceCapable</code> return <code>null</code>.
     *
     * @see PersistenceCapable#jdoGetPersistenceManager()
     * @param pc the <code>PersistenceCapable</code> instance.
     * @return the <code>PersistenceManager</code> associated with the parameter instance.
     */
    public static PersistenceManager getPersistenceManager(Object pc) {
        if (pc instanceof PersistenceCapable) {
            return ((PersistenceCapable) pc).jdoGetPersistenceManager();
        } else {
            return (PersistenceManager) IMPL_HELPER.nonBinaryCompatibleGet(pc, getPersistenceManager);
        }
    }

    /**
     * Explicitly mark the parameter instance and field dirty. Normally, <code>PersistenceCapable
     * </code> classes are able to detect changes made to their fields. However, if a reference to an
     * array is given to a method outside the class, and the array is modified, then the persistent
     * instance is not aware of the change. This API allows the application to notify the instance
     * that a change was made to a field.
     *
     * <p>Transient instances and instances of classes that do not implement <code>PersistenceCapable
     * </code> ignore this method.
     *
     * @see PersistenceCapable#jdoMakeDirty(String fieldName)
     * @param pc the <code>PersistenceCapable</code> instance.
     * @param fieldName the name of the field to be marked dirty.
     */
    public static void makeDirty(Object pc, String fieldName) {
        if (pc instanceof PersistenceCapable) {
            ((PersistenceCapable) pc).jdoMakeDirty(fieldName);
        } else {
            IMPL_HELPER.nonBinaryCompatibleMakeDirty(pc, fieldName);
        }
    }

    /**
     * Return a copy of the JDO identity associated with the parameter instance.
     *
     * <p>Persistent instances of <code>PersistenceCapable</code> classes have a JDO identity managed
     * by the <code>PersistenceManager</code>. This method returns a copy of the ObjectId that
     * represents the JDO identity.
     *
     * <p>Transient instances and instances of classes that do not implement <code>PersistenceCapable
     * </code> return <code>null</code>.
     *
     * <p>The ObjectId may be serialized and later restored, and used with a <code>PersistenceManager
     * </code> from the same JDO implementation to locate a persistent instance with the same data
     * store identity.
     *
     * <p>If the JDO identity is managed by the application, then the ObjectId may be used with a
     * <code>PersistenceManager</code> from any JDO implementation that supports the <code>
     * PersistenceCapable</code> class.
     *
     * <p>If the JDO identity is not managed by the application or the data store, then the ObjectId
     * returned is only valid within the current transaction.
     *
     * <p>
     *
     * @see PersistenceManager#getObjectId(Object pc)
     * @see PersistenceCapable#jdoGetObjectId()
     * @see PersistenceManager#getObjectById(Object oid, boolean validate)
     * @param pc the PersistenceCapable instance.
     * @return a copy of the ObjectId of the parameter instance as of the beginning of the
     *     transaction.
     */
    public static Object getObjectId(Object pc) {
        if (pc instanceof PersistenceCapable) {
            return ((PersistenceCapable) pc).jdoGetObjectId();
        } else {
            return IMPL_HELPER.nonBinaryCompatibleGet(pc, getObjectId);
        }
    }

    /**
     * Get object ids for a collection of instances. For each instance in the parameter, the
     * getObjectId method is called. This method returns one identity instance for each element in the
     * parameter. The order of iteration of the returned Collection exactly matches the order of
     * iteration of the parameter Collection.
     *
     * @param pcs the persistence-capable instances
     * @return the object ids of the parameters
     * @see #getObjectId(Object pc)
     * @see #getObjectIds(Object[] pcs)
     * @since JDO 2.0
     */
    public static Collection<Object> getObjectIds(Collection<?> pcs) {
        ArrayList<Object> result = new ArrayList<>();
        for (Object pc : pcs) {
            result.add(getObjectId(pc));
        }
        return result;
    }

    /**
     * Get object ids for an array of instances. For each instance in the parameter, the getObjectId
     * method is called. This method returns one identity instance for each element in the parameter.
     * The order of instances of the returned array exactly matches the order of instances of the
     * parameter array.
     *
     * @param pcs the persistence-capable instances
     * @return the object ids of the parameters
     * @see #getObjectId(Object pc)
     * @see #getObjectIds(Collection pcs)
     * @since JDO 2.0
     */
    public static Object[] getObjectIds(Object[] pcs) {
        Object[] result = new Object[pcs.length];
        for (int i = 0; i < pcs.length; ++i) {
            result[i] = getObjectId(pcs[i]);
        }
        return result;
    }

    /**
     * Return a copy of the JDO identity associated with the parameter instance.
     *
     * @see PersistenceCapable#jdoGetTransactionalObjectId()
     * @see PersistenceManager#getObjectById(Object oid, boolean validate)
     * @param pc the <code>PersistenceCapable</code> instance.
     * @return a copy of the ObjectId of the parameter instance as modified in this transaction.
     */
    public static Object getTransactionalObjectId(Object pc) {
        if (pc instanceof PersistenceCapable) {
            return ((PersistenceCapable) pc).jdoGetTransactionalObjectId();
        } else {
            return IMPL_HELPER.nonBinaryCompatibleGet(pc, getTransactionalObjectId);
        }
    }

    /**
     * Return the version of the instance.
     *
     * @since JDO 2.0
     * @param pc the instance
     * @return the version of the instance
     */
    public static Object getVersion(Object pc) {
        if (pc instanceof PersistenceCapable) {
            return ((PersistenceCapable) pc).jdoGetVersion();
        } else {
            return IMPL_HELPER.nonBinaryCompatibleGet(pc, getVersion);
        }
    }

    /**
     * Tests whether the parameter instance is dirty.
     *
     * <p>Instances that have been modified, deleted, or newly made persistent in the current
     * transaction return <code>true</code>.
     *
     * <p>Transient instances and instances of classes that do not implement <code>PersistenceCapable
     * </code> return <code>false</code>.
     *
     * <p>
     *
     * @see javax.jdo.spi.StateManager#makeDirty(PersistenceCapable pc, String fieldName)
     * @see PersistenceCapable#jdoIsDirty()
     * @param pc the <code>PersistenceCapable</code> instance.
     * @return <code>true</code> if the parameter instance has been modified in the current
     *     transaction.
     */
    public static boolean isDirty(Object pc) {
        if (pc instanceof PersistenceCapable) {
            return ((PersistenceCapable) pc).jdoIsDirty();
        } else {
            return IMPL_HELPER.nonBinaryCompatibleIs(pc, isDirty);
        }
    }

    /**
     * Tests whether the parameter instance is transactional.
     *
     * <p>Instances whose state is associated with the current transaction return true.
     *
     * <p>Transient instances and instances of classes that do not implement <code>PersistenceCapable
     * </code> return <code>false</code>.
     *
     * @see PersistenceCapable#jdoIsTransactional()
     * @param pc the <code>PersistenceCapable</code> instance.
     * @return <code>true</code> if the parameter instance is transactional.
     */
    public static boolean isTransactional(Object pc) {
        if (pc instanceof PersistenceCapable) {
            return ((PersistenceCapable) pc).jdoIsTransactional();
        } else {
            return IMPL_HELPER.nonBinaryCompatibleIs(pc, isTransactional);
        }
    }

    /**
     * Tests whether the parameter instance is persistent.
     *
     * <p>Instances that represent persistent objects in the data store return <code>true</code>.
     *
     * <p>Transient instances and instances of classes that do not implement <code>PersistenceCapable
     * </code> return <code>false</code>.
     *
     * <p>
     *
     * @see PersistenceManager#makePersistent(Object pc)
     * @see PersistenceCapable#jdoIsPersistent()
     * @param pc the <code>PersistenceCapable</code> instance.
     * @return <code>true</code> if the parameter instance is persistent.
     */
    public static boolean isPersistent(Object pc) {
        if (pc instanceof PersistenceCapable) {
            return ((PersistenceCapable) pc).jdoIsPersistent();
        } else {
            return IMPL_HELPER.nonBinaryCompatibleIs(pc, isPersistent);
        }
    }

    /**
     * Tests whether the parameter instance has been newly made persistent.
     *
     * <p>Instances that have been made persistent in the current transaction return <code>true</code>
     * .
     *
     * <p>Transient instances and instances of classes that do not implement <code>PersistenceCapable
     * </code> return <code>false</code>.
     *
     * <p>
     *
     * @see PersistenceManager#makePersistent(Object pc)
     * @see PersistenceCapable#jdoIsNew()
     * @param pc the <code>PersistenceCapable</code> instance.
     * @return <code>true</code> if the parameter instance was made persistent in the current
     *     transaction.
     */
    public static boolean isNew(Object pc) {
        if (pc instanceof PersistenceCapable) {
            return ((PersistenceCapable) pc).jdoIsNew();
        } else {
            return IMPL_HELPER.nonBinaryCompatibleIs(pc, isNew);
        }
    }

    /**
     * Tests whether the parameter instance has been deleted.
     *
     * <p>Instances that have been deleted in the current transaction return <code>true</code>.
     *
     * <p>Transient instances and instances of classes that do not implement <code>PersistenceCapable
     * </code> return <code>false</code>.
     *
     * <p>
     *
     * @see PersistenceManager#deletePersistent(Object pc)
     * @see PersistenceCapable#jdoIsDeleted()
     * @param pc the <code>PersistenceCapable</code> instance.
     * @return <code>true</code> if the parameter instance was deleted in the current transaction.
     */
    public static boolean isDeleted(Object pc) {
        if (pc instanceof PersistenceCapable) {
            return ((PersistenceCapable) pc).jdoIsDeleted();
        } else {
            return IMPL_HELPER.nonBinaryCompatibleIs(pc, isDeleted);
        }
    }

    /**
     * Tests whether the parameter instance has been detached.
     *
     * <p>Instances that have been detached return true.
     *
     * <p>Transient instances return false.
     *
     * <p>
     *
     * @see PersistenceCapable#jdoIsDetached()
     * @return <code>true</code> if this instance is detached.
     * @since JDO 2.0
     * @param pc the instance
     */
    public static boolean isDetached(Object pc) {
        if (pc instanceof PersistenceCapable) {
            return ((PersistenceCapable) pc).jdoIsDetached();
        } else {
            return IMPL_HELPER.nonBinaryCompatibleIs(pc, isDetached);
        }
    }

    /**
     * Accessor for the state of the passed object.
     *
     * @param pc The object
     * @return The object state
     * @since JDO 2.1
     */
    public static ObjectState getObjectState(Object pc) {
        if (pc == null) {
            return null;
        }

        if (isDetached(pc)) {
            return isDirty(pc) ? ObjectState.DETACHED_DIRTY : ObjectState.DETACHED_CLEAN;
        } else {
            if (isPersistent(pc)) {
                return isTransactional(pc)
                        ? getPersistentTransactionalObjectState(pc)
                        : getPersistentNonTransactionalObjectState(pc);
            } else {
                return isTransactional(pc)
                        ? getTransientTransactionalObjectState(pc)
                        : ObjectState.TRANSIENT;
            }
        }
    }

    /** Get a {@code PersistenceManagerFactory} based on a {@code Properties}
     * instance, using the current thread's context class loader to locate the
     * {@code PersistenceManagerFactory} class.
     * @return the {@code PersistenceManagerFactory}.
     * @param props a {@code Properties} instance with properties of the
     * {@code PersistenceManagerFactory}.
     * @see #getPersistenceManagerFactory(java.util.Map,ClassLoader)
     * @since openMDX 2.17
     */
    public static JDOPersistenceManagerFactory getPersistenceManagerFactory
            (Map<?, ?> props) {
        return getPersistenceManagerFactory(
                null, props, getContextClassLoader());
    }

    /** Get a {@code PersistenceManagerFactory} based on a 
     * {@code Map} and a class loader.
     * This method delegates to the getPersistenceManagerFactory
     * method that takes a Map of overrides and a Map of properties,
     * passing null as the overrides parameter.
     * @see #getPersistenceManagerFactory(java.util.Map, java.util.Map, ClassLoader)
     * @return the {@code PersistenceManagerFactory}.
     * @param props a {@code Map} with properties of the 
     * {@code PersistenceManagerFactory}.
     * @param pmfClassLoader the class loader used to load the
     * {@code PersistenceManagerFactory} class
     * @since openMDX 2.17
     */
    public static JDOPersistenceManagerFactory getPersistenceManagerFactory
            (Map<?, ?> props, ClassLoader pmfClassLoader) {
        return getPersistenceManagerFactory(
                null, props, pmfClassLoader);
    }

    /**
     * Returns a named {@link JDOPersistenceManagerFactory} or persistence
     * unit.
     *
     * @since openMDX 2.17
     * @see #getPersistenceManagerFactory(Map,String,ClassLoader,ClassLoader)
     */
    public static JDOPersistenceManagerFactory getPersistenceManagerFactory
        (String name) {
        ClassLoader cl = getContextClassLoader();
        return getPersistenceManagerFactory(null, name, cl, cl);
    }

    /**
     * Returns a named {@link JDOPersistenceManagerFactory} or persistence
     * unit.
     *
     * @since openMDX 2.17
     * @see #getPersistenceManagerFactory(Map,String,ClassLoader,ClassLoader)
     */
    public static JDOPersistenceManagerFactory getPersistenceManagerFactory
        (String name, ClassLoader loader) {
        
        return getPersistenceManagerFactory(null, name, loader, loader);
    }

    /**
     * Returns a named {@link JDOPersistenceManagerFactory} or persistence
     * unit.
     *
     * @since openMDX 2.17
     * @see #getPersistenceManagerFactory(Map,String,ClassLoader,ClassLoader)
     */
    public static JDOPersistenceManagerFactory getPersistenceManagerFactory
        (String name, ClassLoader resourceLoader, ClassLoader pmfLoader) {

        return getPersistenceManagerFactory(
                null, name, resourceLoader, pmfLoader);
    }

    /**
     * Returns a named {@link JDOPersistenceManagerFactory} or persistence
     * unit.
     *
     * @since openMDX 2.17
     * @see #getPersistenceManagerFactory(Map,String,ClassLoader,ClassLoader)
     */
    public static JDOPersistenceManagerFactory getPersistenceManagerFactory
            (Map<?, ?> overrides, String name) {

        ClassLoader cl = getContextClassLoader();
        return getPersistenceManagerFactory(overrides, name, cl, cl);
    }

    /**
     * Returns a named {@link JDOPersistenceManagerFactory} or persistence
     * unit.
     *
     * @since openMDX 2.17
     * @see #getPersistenceManagerFactory(Map,String,ClassLoader,ClassLoader)
     */
    public static JDOPersistenceManagerFactory getPersistenceManagerFactory
            (Map<?, ?> overrides, String name, ClassLoader resourceLoader) {

        return getPersistenceManagerFactory(
                overrides, name, resourceLoader, resourceLoader);
    }
    

    /**
     * Returns a {@link PersistenceManagerFactory} configured based
     * on the properties stored in the resource at
     * {@code name}, or, if not found, returns a
     * {@link PersistenceManagerFactory} with the given
     * name or, if not found, returns a
     * {@code javax.persistence.EntityManagerFactory} cast to a
     * {@link PersistenceManagerFactory}.  If the name given is null or consists
     * only of whitespace, it is interpreted as
     * {@link Constants#ANONYMOUS_PERSISTENCE_MANAGER_FACTORY_NAME}.
     * The following are standard key names:
     * <BR>{@code "javax.jdo.PersistenceManagerFactoryClass"
     * <BR>"javax.jdo.option.Optimistic",
     * <BR>"javax.jdo.option.RetainValues",
     * <BR>"javax.jdo.option.RestoreValues",
     * <BR>"javax.jdo.option.IgnoreCache",
     * <BR>"javax.jdo.option.NontransactionalRead",
     * <BR>"javax.jdo.option.NontransactionalWrite",
     * <BR>"javax.jdo.option.Multithreaded",
     * <BR>"javax.jdo.option.ConnectionUserName",
     * <BR>"javax.jdo.option.ConnectionPassword",
     * <BR>"javax.jdo.option.ConnectionURL",
     * <BR>"javax.jdo.option.ConnectionFactoryName",
     * <BR>"javax.jdo.option.ConnectionFactory2Name",
     * <BR>"javax.jdo.option.Mapping",
     * <BR>"javax.jdo.mapping.Catalog",
     * <BR>"javax.jdo.mapping.Schema",
     * <BR>"javax.jdo.option.PersistenceUnitName".
     * <BR>"javax.jdo.option.DetachAllOnCommit".
     * <BR>"javax.jdo.option.CopyOnAttach".
     * <BR>"javax.jdo.option.TransactionType".
     * <BR>"javax.jdo.option.ServerTimeZoneID".
     * <BR>"javax.jdo.option.DatastoreReadTimeoutMillis",
     * <BR>"javax.jdo.option.DatastoreWriteTimeoutMillis",
     * <BR>"javax.jdo.option.Name".
     * }
     * and properties of the form
     * <BR>{@code javax.jdo.option.InstanceLifecycleListener.{listenerClass}[=[{pcClasses}]]}
     * where {@code {listenerClass}} is the fully qualified name of a
     * class that implements
     * {@link javax.jdo.listener.InstanceLifecycleListener}, and
     * {@code {pcClasses}} is an optional comma- or whitespace-delimited
     * list of persistence-capable classes to be observed; the absence of a
     * value for a property of this form means that instances of all
     * persistence-capable classes will be observed by an instance of the given
     * listener class.
     * <P>JDO implementations
     * are permitted to define key values of their own.  Any key values not
     * recognized by the implementation must be ignored.  Key values that are
     * recognized but not supported by an implementation must result in a
     * {@code JDOFatalUserException} thrown by the method.
     * <P>The returned {@code PersistenceManagerFactory} is not 
     * configurable (the {@code set<I>XXX</I>} methods will throw an 
     * exception).
     * 
     * This method loads the properties found at {@code name}, if any, via
     * {@code resourceLoader}, and creates a {@link
     * PersistenceManagerFactory} with {@code pmfLoader}. Any
     * exceptions thrown during resource loading will
     * be wrapped in a {@link JDOFatalUserException}.
     * If multiple PMFs with the requested name are found, a
     * {@link JDOFatalUserException} is thrown.
     * @since openMDX 2.17
     * @param overrides a Map containing properties that override properties
     * defined in any resources loaded according to the "name" parameter
     * @param name interpreted as the name of the resource containing the PMF
     * properties, the name of the PMF, or the persistence unit name, in that
     * order; if name is null, blank or whitespace, it is interpreted as
     * indicating the anonymous {@link PersistenceManagerFactory}.
     * @param resourceLoader the class loader to use to load properties file
     * resources; must be non-null if {@code name} is non-null or blank
     * @param pmfLoader the class loader to use to load the 
     * {@link PersistenceManagerFactory} or
     * {@code javax.persistence.EntityManagerFactory} classes
     * @return the {@link PersistenceManagerFactory} with properties in the
     * given resource, with the given name, or with the given persitence unit
     * name
     * @see Constants#ANONYMOUS_PERSISTENCE_MANAGER_FACTORY_NAME
     */
    public static JDOPersistenceManagerFactory getPersistenceManagerFactory(
            Map<?, ?> overrides,
            String name,
            ClassLoader resourceLoader,
            ClassLoader pmfLoader) {
        if (pmfLoader == null)
            throw new JDOFatalUserException (MSG.msg (
                "EXC_GetPMFNullPMFLoader")); //NOI18N
        if (resourceLoader == null) {
            throw new JDOFatalUserException(MSG.msg(
                "EXC_GetPMFNullPropsLoader")); //NOI18N
        }

        Map<Object,Object> props = null;
        // trim spaces from name and ensure non-null
        name = (name == null?ANONYMOUS_PERSISTENCE_MANAGER_FACTORY_NAME:name.trim());
        if (!ANONYMOUS_PERSISTENCE_MANAGER_FACTORY_NAME.equals(name)) {
            props = loadPropertiesFromResource(resourceLoader, name);
        }

        if (props != null) {
            // add the SPI property to inform the implementation that
            // the PMF was configured by the given resource name
            // and not via named PMF for proper deserialization
            props.put(PROPERTY_SPI_RESOURCE_NAME, name);
            props.remove(PROPERTY_NAME);
            return getPersistenceManagerFactory(overrides, props, pmfLoader);
        }
        // props were null; try getting from jdoconfig.xml
        props = getPropertiesFromJdoconfig(name, pmfLoader);
        if (props != null) {
            // inform the impl that the config came from a jdoconfig.xml
            // element with the given name
            props.put(PROPERTY_NAME, name);
            props.remove(PROPERTY_SPI_RESOURCE_NAME);
            // we have loaded a Properties, delegate to implementation
            return getPersistenceManagerFactory(overrides, props, pmfLoader);
        }
        // no properties found; last try to see if name is a JPA PU name
        if (!ANONYMOUS_PERSISTENCE_MANAGER_FACTORY_NAME.equals(name)) {
            props = new Properties();
            props.put(PROPERTY_PERSISTENCE_UNIT_NAME, name);
            return getPersistenceManagerFactory(overrides, props, pmfLoader);
        }
        
        // no PMF found; give up
        throw new JDOFatalUserException (MSG.msg (
            "EXC_NoPMFConfigurableViaPropertiesOrXML", name)); //NOI18N
    }

    /** Invoke the getPersistenceManagerFactory method on the implementation.
     * If the overrides parameter to this method is not null, the static method 
     * with Map overrides, Map properties parameters will be invoked.
     * If the overrides parameter to this method is null,  the static method 
     * with Map properties parameter will be invoked.
     * @param pmfClassName the name of the implementation factory class
     * @param overrides a Map of overrides
     * @param properties a Map of properties
     * @param cl the class loader to use to load the implementation class
     * @return the PersistenceManagerFactory
     * @since openMDX 2.17
     */
    protected static JDOPersistenceManagerFactory
        invokeGetPersistenceManagerFactoryOnImplementation(
            String pmfClassName, Map<?, ?> overrides, Map<?, ?> properties, ClassLoader cl) {
        if (overrides != null) {
            // overrides is not null; use getPersistenceManagerFactory(Map overrides, Map props)
            try {
                Class<?> implClass = forName(pmfClassName, true, cl);
                Method m = getMethod(implClass,
                        "getPersistenceManagerFactory", //NOI18N
                        new Class[]{Map.class, Map.class});
                JDOPersistenceManagerFactory pmf = 
                    (JDOPersistenceManagerFactory) invoke(m,
                        null, new Object[]{overrides, properties});
                if (pmf == null) {
                        throw new JDOFatalInternalException(MSG.msg (
                            "EXC_GetPMFNullPMF", pmfClassName)); //NOI18N
                    }
                return pmf;

            } catch (ClassNotFoundException e) {
                throw new JDOFatalUserException(MSG.msg(
                        "EXC_GetPMFClassNotFound", pmfClassName), e); //NOI18N
            } catch (NoSuchMethodException e) {
                throw new JDOFatalInternalException(MSG.msg(
                        "EXC_GetPMFNoSuchMethod2", pmfClassName), e); //NOI18N
            } catch (NullPointerException e) {
                throw new JDOFatalInternalException (MSG.msg(
                    "EXC_GetPMFNullPointerException", pmfClassName), e); //NOI18N
            } catch (IllegalAccessException e) {
                throw new JDOFatalUserException(MSG.msg(
                        "EXC_GetPMFIllegalAccess", pmfClassName), e); //NOI18N
            } catch (ClassCastException e) {
                throw new JDOFatalInternalException (MSG.msg(
                    "EXC_GetPMFClassCastException", pmfClassName), e); //NOI18N
            } catch (InvocationTargetException ite) {
                Throwable nested = ite.getTargetException();
                if (nested instanceof JDOException) {
                    throw (JDOException)nested;
                } else throw new JDOFatalInternalException (MSG.msg(
                    "EXC_GetPMFUnexpectedException"), ite); //NOI18N
            }
        } else {
            // overrides is null; use getPersistenceManagerFactory(Map props)
            try {
                Class<?> implClass = forName(pmfClassName, true, cl);
                Method m = getMethod(implClass,
                        "getPersistenceManagerFactory", //NOI18N
                        new Class[]{Map.class});
                JDOPersistenceManagerFactory pmf = 
                    (JDOPersistenceManagerFactory) invoke(m,
                        null, new Object[]{properties});
                if (pmf == null) {
                        throw new JDOFatalInternalException(MSG.msg (
                            "EXC_GetPMFNullPMF", pmfClassName)); //NOI18N
                    }
                return pmf;
            } catch (ClassNotFoundException e) {
                throw new JDOFatalUserException(MSG.msg(
                        "EXC_GetPMFClassNotFound", pmfClassName), e); //NOI18N
            } catch (NoSuchMethodException e) {
                throw new JDOFatalInternalException(MSG.msg(
                        "EXC_GetPMFNoSuchMethod", pmfClassName), e); //NOI18N
            } catch (NullPointerException e) {
                throw new JDOFatalInternalException (MSG.msg(
                    "EXC_GetPMFNullPointerException", pmfClassName), e); //NOI18N
            } catch (IllegalAccessException e) {
                throw new JDOFatalUserException(MSG.msg(
                        "EXC_GetPMFIllegalAccess", pmfClassName), e); //NOI18N
            } catch (ClassCastException e) {
                throw new JDOFatalInternalException (MSG.msg(
                    "EXC_GetPMFClassCastException", pmfClassName), e); //NOI18N
            } catch (InvocationTargetException ite) {
                Throwable nested = ite.getTargetException();
                if (nested instanceof JDOException) {
                    throw (JDOException)nested;
                } else throw new JDOFatalInternalException (MSG.msg(
                    "EXC_GetPMFUnexpectedException"), ite); //NOI18N
            }
        }
    }

    /**
     * Get a <code>JDOPersistenceManagerFactory</code> based on a <code>Map</code> of overrides, a <code>
     * Map</code> of properties, and a class loader. The following are standard key names: <br>
     * <code>"javax.jdo.PersistenceManagerFactoryClass"
     * <BR>"javax.jdo.option.Optimistic",
     * <BR>"javax.jdo.option.RetainValues",
     * <BR>"javax.jdo.option.RestoreValues",
     * <BR>"javax.jdo.option.IgnoreCache",
     * <BR>"javax.jdo.option.NontransactionalRead",
     * <BR>"javax.jdo.option.NontransactionalWrite",
     * <BR>"javax.jdo.option.Multithreaded",
     * <BR>"javax.jdo.option.ConnectionUserName",
     * <BR>"javax.jdo.option.ConnectionPassword",
     * <BR>"javax.jdo.option.ConnectionURL",
     * <BR>"javax.jdo.option.ConnectionFactoryName",
     * <BR>"javax.jdo.option.ConnectionFactory2Name",
     * <BR>"javax.jdo.option.Mapping",
     * <BR>"javax.jdo.mapping.Catalog",
     * <BR>"javax.jdo.mapping.Schema",
     * <BR>"javax.jdo.option.PersistenceUnitName",
     * <BR>"javax.jdo.option.DetachAllOnCommit",
     * <BR>"javax.jdo.option.CopyOnAttach",
     * <BR>"javax.jdo.option.ReadOnly",
     * <BR>"javax.jdo.option.TransactionIsolationLevel",
     * <BR>"javax.jdo.option.TransactionType",
     * <BR>"javax.jdo.option.ServerTimeZoneID",
     * <BR>"javax.jdo.option.DatastoreReadTimeoutMillis",
     * <BR>"javax.jdo.option.DatastoreWriteTimeoutMillis",
     * <BR>"javax.jdo.option.Name".
     * </code> and properties of the form <br>
     * <code>javax.jdo.option.InstanceLifecycleListener.{listenerClass}[=[{pcClasses}]]</code> where
     * <code>{listenerClass}</code> is the fully qualified name of a class that implements {@link
     * javax.jdo.listener.InstanceLifecycleListener}, and <code>{pcClasses}</code> is an optional
     * comma- or whitespace-delimited list of persistence-capable classes to be observed; the absence
     * of a value for a property of this form means that instances of all persistence-capable classes
     * will be observed by an instance of the given listener class.
     *
     * <p>JDO implementations are permitted to define key values of their own. Any key values not
     * recognized by the implementation must be ignored. Key values that are recognized but not
     * supported by an implementation must result in a <code>JDOFatalUserException</code> thrown by
     * the method.
     *
     * <p>The returned <code>PersistenceManagerFactory</code> is not configurable (the <code>
     * set<I>XXX</I></code> methods will throw an exception).
     *
     * <p>JDO implementations might manage a map of instantiated <code>PersistenceManagerFactory
     * </code> instances based on specified property key values, and return a previously instantiated
     * <code>PersistenceManagerFactory</code> instance. In this case, the properties of the returned
     * instance must exactly match the requested properties.
     *
     * @param overrides Overrides of properties
     * @param props a <code>Properties</code> instance with properties of the <code>
     *     PersistenceManagerFactory</code>.
     * @param pmfClassLoader the class loader to use to load the <code>PersistenceManagerFactory
     *     </code> class
     * @return the <code>PersistenceManagerFactory</code>.
     * @throws JDOFatalUserException if
     *     <ul>
     *       <li>the pmfClassLoader passed is invalid; or
     *       <li>a valid class name cannot be obtained from either <code>props</code> or system
     *           resources (an entry in META-INF/services/javax.jdo.PersistenceManagerFactory); or
     *       <li>all implementations throw an exception.
     *     </ul>
     *
     * @since JDO 2.1
     */
    protected static JDOPersistenceManagerFactory getPersistenceManagerFactory(
        Map<?, ?> overrides, Map<?, ?> props, ClassLoader pmfClassLoader) {

      List<Throwable> exceptions = new ArrayList<>();
      if (pmfClassLoader == null)
        throw new JDOFatalUserException(MSG.msg("EXC_GetPMFNullLoader")); // NOI18N

      JDOImplHelper.assertOnlyKnownStandardProperties(overrides);
      JDOImplHelper.assertOnlyKnownStandardProperties(props);

      // first try to get the class name from the properties object.
      String pmfClassName = (String) props.get(Constants.PROPERTY_PERSISTENCE_MANAGER_FACTORY_CLASS);

      if (!isNullOrBlank(pmfClassName)) {
        // a valid name was returned from the properties.
        return invokeGetPersistenceManagerFactoryOnImplementation(
            pmfClassName, overrides, props, pmfClassLoader);

      } else {
        /*
         * If you have a jar file that provides the jdo implementation,
         * a file naming the implementation goes into the file
         * packaged into the jar file, called
         * META-INF/services/javax.jdo.PersistenceManagerFactory.
         * The contents of the file is a string that is the PMF class name,
         * null or blank.
         * For each file in pmfClassLoader named
         * META-INF/services/javax.jdo.PersistenceManagerFactory,
         * this method will try to invoke the getPersistenceManagerFactory
         * method of the implementation class.
         * Return the factory if a valid class name is extracted from
         * resources and the invocation returns an instance.
         * Otherwise add the exception thrown to
         * an exception list.
         */
        Enumeration<URL> urls = null;
        try {
          urls = getResources(pmfClassLoader, SERVICE_LOOKUP_PMF_RESOURCE_NAME);
        } catch (Exception ex) {
          exceptions.add(ex);
        }

        if (urls != null) {
          while (urls.hasMoreElements()) {

            try {
              pmfClassName = getClassNameFromURL(urls.nextElement());

              // return the implementation that is valid.
              return invokeGetPersistenceManagerFactoryOnImplementation(
                  pmfClassName, overrides, props, pmfClassLoader);

            } catch (Exception ex) {

              // remember exceptions from failed pmf invocations
              exceptions.add(ex);
            }
          }
        }
      }

      // no PMF class name in props and no services.

      throw new JDOFatalUserException(
          MSG.msg("EXC_GetPMFNoPMFClassNamePropertyOrPUNameProperty"),
          exceptions.toArray(new Throwable[exceptions.size()]));
    }

    /**
     * Get a class name from a URL. The URL is from getResources with e.g.
     * META-INF/services/javax.jdo.PersistenceManagerFactory as the parameter. Parse the file,
     * removing blank lines, comment lines, and comments.
     *
     * @param url the URL of the services file
     * @return the name of the class contained in the file
     * @throws java.io.IOException Throw if an error occurs on accessing this URL
     * @since JDO 2.1
     */
    protected static String getClassNameFromURL(URL url) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(openStream(url)))) {
            for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                // else assume first line of text is the PMF class name
                String[] tokens = line.split("\\s");
                String pmfClassName = tokens[0];
                int indexOfComment = pmfClassName.indexOf("#");
                if (indexOfComment == -1) {
                    return pmfClassName;
                }
                // else pmfClassName has a comment at the end of it -- remove
                return pmfClassName.substring(0, indexOfComment);
            }
            return null;
        }
    }

    /**
     * Load a Properties instance by name from the class loader.
     *
     * @param resourceLoader the class loader from which to load the properties
     * @param name the name of the resource
     * @return a Properties instance or null if no resource is found
     */
    protected static Map<Object, Object> loadPropertiesFromResource(
        ClassLoader resourceLoader,
        String name
    ) {
        // try to load resources from properties file
        try ( InputStream in = getResourceAsStream(resourceLoader, name)) {
            if (in == null) {
                return null;
            } else {
                // then some kind of resource was found by the given name;
                // assume that it's a properties file
                Properties props = new Properties();
                props.load(in);
                return props;
            }
        } catch (IOException ioe) {
            throw new JDOFatalUserException(MSG.msg(EXC_GET_PMF_IOEXCEPTION_RSRC, name), ioe); // NOI18N
        }
    }

    /**
     * @see #getNamedPMFProperties(String,ClassLoader,String)
     * @since JDO 2.1
     * @param name Name of the PMF
     * @param resourceLoader ClassLoader to use for loading resources
     * @return The properties for this PMF
     */
    protected static Map<Object, Object> getPropertiesFromJdoconfig(
            String name, ClassLoader resourceLoader) {
        return getNamedPMFProperties(name, resourceLoader, JDOCONFIG_RESOURCE_NAME);
    }

    /**
     * Find and return the named {@link PersistenceManagerFactory}'s properties, or null if not found.
     * If multiple named PMF property sets with the given name are found (including anonymous ones),
     * throw {@link JDOFatalUserException}. This method is here only to facilitate testing; the
     * parameter "jdoconfigResourceName" in public usage should always have the value given in the
     * constant {@code JDOCONFIG_RESOURCE_NAME}.
     *
     * @param name The persistence unit name; null is disallowed.
     * @param resourceLoader The ClassLoader used to load the standard JDO configuration file.
     * @param jdoconfigResourceName The name of the configuration file to read. In public usage, this
     *     should always be the value of {@code JDOCONFIG_RESOURCE_NAME}.
     * @return The named <code>PersistenceManagerFactory</code> properties if found, null if not.
     * @since JDO 2.1
     * @throws JDOFatalUserException if multiple named PMF property sets are found with the given
     *     name, or any other exception is encountered.
     */
    protected static Map<Object, Object> getNamedPMFProperties(
            String name, ClassLoader resourceLoader, String jdoconfigResourceName) {
        // key is PU name, value is Map of PU properties
        Map<String, Map<Object, Object>> propertiesByNameInAllConfigs = new HashMap<>();
        try {
            URL firstFoundConfigURL = null;

            // get all JDO configurations
            Enumeration<URL> resources = getResources(resourceLoader, jdoconfigResourceName);

            if (resources.hasMoreElements()) {
                ArrayList<URL> processedResources = new ArrayList<>();

                // get ready to parse XML
                DocumentBuilderFactory factory = getDocumentBuilderFactory();
                do {
                    URL currentConfigURL = resources.nextElement();
                    if (processedResources.contains(currentConfigURL)) {
                        continue;
                    } else {
                        processedResources.add(currentConfigURL);
                    }

                    Map<String, Map<Object, Object>> propertiesByNameInCurrentConfig =
                            readNamedPMFProperties(currentConfigURL, name, factory);

                    // try to detect duplicate requested PU
                    if (propertiesByNameInCurrentConfig.containsKey(name)) {
                        // possible dup -- check for it
                        if (firstFoundConfigURL == null) {
                            firstFoundConfigURL = currentConfigURL;
                        }

                        if (propertiesByNameInAllConfigs.containsKey(name))
                            throw new JDOFatalUserException(
                                    MSG.msg(
                                            "EXC_DuplicateRequestedNamedPMFFoundInDifferentConfigs",
                                            "".equals(name) ? "(anonymous)" : name,
                                            firstFoundConfigURL.toExternalForm(),
                                            currentConfigURL.toExternalForm())); // NOI18N
                    }
                    // no dups -- add found PUs to all PUs and keep going
                    propertiesByNameInAllConfigs.putAll(propertiesByNameInCurrentConfig);
                } while (resources.hasMoreElements());
            }
        } catch (FactoryConfigurationError e) {
            throw new JDOFatalUserException(MSG.msg("ERR_NoDocumentBuilderFactory"), e);
        } catch (IOException ioe) {
            throw new JDOFatalUserException(MSG.msg(EXC_GET_PMF_IOEXCEPTION_RSRC, name), ioe); // NOI18N
        }

        // done with reading all config resources;
        // return what we found, which may very well be null
        return propertiesByNameInAllConfigs.get(name);
    }

    protected static DocumentBuilderFactory getDocumentBuilderFactory() {
        @SuppressWarnings("static-access")
        DocumentBuilderFactory factory = IMPL_HELPER.getRegisteredDocumentBuilderFactory();
        if (factory == null) {
            factory = getDefaultDocumentBuilderFactory();
        }
        return factory;
    }

    protected static DocumentBuilderFactory getDefaultDocumentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        } catch (ParserConfigurationException e) {
            throw new JDOFatalUserException(e.getMessage());
        }
        factory.setIgnoringComments(true);
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setExpandEntityReferences(true);

        return factory;
    }

    protected static ErrorHandler getErrorHandler() {
        @SuppressWarnings("static-access")
        ErrorHandler handler = IMPL_HELPER.getRegisteredErrorHandler();
        if (handler == null) {
            handler = getDefaultErrorHandler();
        }
        return handler;
    }

    protected static ErrorHandler getDefaultErrorHandler() {
        return new ErrorHandler() {
            public void error(SAXParseException exception) throws SAXException {
                throw exception;
            }

            public void fatalError(SAXParseException exception) throws SAXException {
                throw exception;
            }

            public void warning(SAXParseException exception){
                // gulp:  ignore warnings
            }
        };
    }

    /**
     * Reads JDO configuration file, creates a Map for each persistence-manager-factory, then returns
     * the map.
     *
     * @param url URL of a JDO configuration file compliant with javax/jdo/jdoconfig.xsd.
     * @param requestedPMFName The name of the requested persistence unit (allows for fail-fast).
     * @param factory The <code>DocumentBuilderFactory</code> to use for XML parsing.
     * @return a Map&lt;String,Map&gt; holding persistence unit configurations; for the anonymous
     *     persistence unit, the value of the String key is the empty string, "".
     */
    protected static Map<String, Map<Object, Object>> readNamedPMFProperties(
            URL url,
            String requestedPMFName,
            DocumentBuilderFactory factory
    ) {
        requestedPMFName = requestedPMFName == null ? "" : requestedPMFName.trim();
        Map<String, Map<Object, Object>> propertiesByName = new HashMap<>();
        try (final InputStream in  = url.openStream()) {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(getErrorHandler());
            Document doc = builder.parse(in);

            Element root = doc.getDocumentElement();
            if (root == null) {
                throw new JDOFatalUserException(
                        MSG.msg("EXC_InvalidJDOConfigNoRoot", url.toExternalForm()));
            }

            NodeList pmfs = root.getElementsByTagName(Constants.ELEMENT_PERSISTENCE_MANAGER_FACTORY);

            for (int i = 0; i < pmfs.getLength(); i++) {
                Node pmfElement = pmfs.item(i);

                Properties pmfPropertiesFromAttributes = readPropertiesFromPMFElementAttributes(pmfElement);

                Properties pmfPropertiesFromElements = readPropertiesFromPMFSubelements(pmfElement, url);

                // for informative error handling, get name (or names) now
                String pmfNameFromAtts = pmfPropertiesFromAttributes.getProperty(Constants.PROPERTY_NAME);
                String pmfNameFromElem = pmfPropertiesFromElements.getProperty(Constants.PROPERTY_NAME);

                String pmfName;
                if (isNullOrBlank(pmfNameFromAtts)) {
                    // no PMF name attribute given
                    if (!isNullOrBlank(pmfNameFromElem)) {
                        // PMF name element was given
                        pmfName = pmfNameFromElem;
                    } else {
                        // PMF name not given at all, means the "anonymous" PMF
                        pmfName = Constants.ANONYMOUS_PERSISTENCE_MANAGER_FACTORY_NAME;
                    }
                } else {
                    // PMF name given in an attribute
                    if (!isNullOrBlank(pmfNameFromElem)) {
                        // exception -- PMF name given as both att & elem
                        throw new JDOFatalUserException(
                                MSG.msg(
                                        "EXC_DuplicatePMFNamePropertyFoundWithinConfig",
                                        pmfNameFromAtts,
                                        pmfNameFromElem,
                                        url.toExternalForm()));
                    }
                    pmfName = pmfNameFromAtts;
                }
                pmfName = pmfName == null ? "" : pmfName.trim();

                // check for duplicate properties among atts & elems
                if (requestedPMFName.equals(pmfName)) {
                    for (Object o : pmfPropertiesFromAttributes.keySet()) {
                        String property = (String) o;
                        if (pmfPropertiesFromElements.contains(property)) {
                            throw new JDOFatalUserException(
                                    MSG.msg("EXC_DuplicatePropertyFound", property, pmfName, url.toExternalForm()));
                        }
                    }
                }

                // at this point, we're guaranteed not to have duplicate
                // properties -- merge them
                Properties pmfProps = new Properties();
                pmfProps.putAll(pmfPropertiesFromAttributes);
                pmfProps.putAll(pmfPropertiesFromElements);

                // check for duplicate requested PMF name
                if (pmfName.equals(requestedPMFName) && propertiesByName.containsKey(pmfName)) {

                    throw new JDOFatalUserException(
                            MSG.msg(
                                    "EXC_DuplicateRequestedNamedPMFFoundInSameConfig",
                                    pmfName,
                                    url.toExternalForm()));
                }
                propertiesByName.put(pmfName, pmfProps);
            }
            return propertiesByName;
        } catch (IOException ioe) {
            throw new JDOFatalUserException(
                    MSG.msg(EXC_GET_PMF_IOEXCEPTION_RSRC, url.toString()), ioe); // NOI18N
        } catch (ParserConfigurationException e) {
            throw new JDOFatalInternalException(MSG.msg("EXC_ParserConfigException"), e);
        } catch (SAXParseException e) {
            throw new JDOFatalUserException(
                    MSG.msg(
                            "EXC_SAXParseException",
                            url.toExternalForm(),
                            e.getLineNumber(),
                            e.getColumnNumber()),
                    e);
        } catch (JDOException e) {
            throw e;
        } catch (SAXException|RuntimeException e) {
            throw new JDOFatalUserException(MSG.msg("EXC_SAXException", url.toExternalForm()), e);
        }
    }

    protected static Properties readPropertiesFromPMFElementAttributes(Node pmfElement) {
        Properties p = new Properties();
        NamedNodeMap attributes = pmfElement.getAttributes();
        if (attributes == null) {
            return p;
        }

        for (int i = 0; i < attributes.getLength(); i++) {
            Node att = attributes.item(i);
            String attName = att.getNodeName();
            String attValue = att.getNodeValue().trim();

            String jdoPropertyName = ATTRIBUTE_PROPERTY_XREF.get(attName);

            p.put(jdoPropertyName != null ? jdoPropertyName : attName, attValue);
        }

        return p;
    }

    protected static Properties readPropertiesFromPMFSubelements(Node pmfElement, URL url) {
        Properties p = new Properties();
        NodeList elements = pmfElement.getChildNodes();
        if (elements == null) {
            return p;
        }
        for (int i = 0; i < elements.getLength(); i++) {
            Node element = elements.item(i);
            if (element.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String elementName = element.getNodeName();
            NamedNodeMap attributes = element.getAttributes();
            if (Constants.ELEMENT_PROPERTY.equalsIgnoreCase(elementName)) {
                // <property name="..." value="..."/>

                // get the "name" attribute's value (required)
                Node nameAtt = attributes.getNamedItem(Constants.PROPERTY_ATTRIBUTE_NAME);
                if (nameAtt == null) {
                    throw new JDOFatalUserException(MSG.msg("EXC_PropertyElementHasNoNameAttribute", url));
                }
                String name = nameAtt.getNodeValue().trim();
                if (name.isEmpty()) {
                    throw new JDOFatalUserException(
                            MSG.msg("EXC_PropertyElementNameAttributeHasNoValue", name, url));
                }
                // The next call allows users to use either the
                // <persistence-manager-factory> attribute names or the
                // "javax.jdo" property names in <property> element "name"
                // attributes.  Handy-dandy.
                String jdoPropertyName = ATTRIBUTE_PROPERTY_XREF.get(name);

                String propertyName = jdoPropertyName != null ? jdoPropertyName : name;

                if (p.containsKey(propertyName)) {
                    throw new JDOFatalUserException(
                            MSG.msg("EXC_DuplicatePropertyNameGivenInPropertyElement", propertyName, url));
                }

                // get the "value" attribute's value (optional)
                Node valueAtt = attributes.getNamedItem(Constants.PROPERTY_ATTRIBUTE_VALUE);
                String value = valueAtt == null ? null : valueAtt.getNodeValue().trim();

                p.put(propertyName, value);
            } else if (Constants.ELEMENT_INSTANCE_LIFECYCLE_LISTENER.equals(elementName)) {
                // <instance-lifecycle-listener listener="..." classes="..."/>

                // get the "listener" attribute's value
                Node listenerAtt =
                        attributes.getNamedItem(Constants.INSTANCE_LIFECYCLE_LISTENER_ATTRIBUTE_LISTENER);
                if (listenerAtt == null) {
                    throw new JDOFatalUserException(MSG.msg("EXC_MissingListenerAttribute", url));
                }
                String listener = listenerAtt.getNodeValue().trim();
                if (listener.isEmpty()) {
                    throw new JDOFatalUserException(MSG.msg("EXC_MissingListenerAttributeValue", url));
                }

                // listener properties are of the form
                // "javax.jdo.option.InstanceLifecycleListener." + listener
                listener = Constants.PROPERTY_PREFIX_INSTANCE_LIFECYCLE_LISTENER + listener;

                // get the "classes" attribute's value (optional)
                Node classesAtt =
                        attributes.getNamedItem(Constants.INSTANCE_LIFECYCLE_LISTENER_ATTRIBUTE_CLASSES);
                String value = classesAtt == null ? "" : classesAtt.getNodeValue().trim();

                p.put(listener, value);
            }
        }
        return p;
    }

    protected static boolean isNullOrBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Returns a {@link JDOPersistenceManagerFactory} configured based on the properties stored in the
     * file at <code>propsFile</code>. This method is equivalent to invoking {@link
     * #getPersistenceManagerFactory(File,ClassLoader)} with <code>
     * Thread.currentThread().getContextClassLoader()</code> as the <code>loader</code> argument.
     *
     * @since JDO 2.0
     * @param propsFile the file containing the Properties
     * @return the PersistenceManagerFactory
     */
    public static JDOPersistenceManagerFactory getPersistenceManagerFactory(File propsFile) {
        return getPersistenceManagerFactory(propsFile, getContextClassLoader());
    }

    /**
     * Returns a {@link JDOPersistenceManagerFactory} configured based on the properties stored in the
     * file at <code>propsFile</code>. Creates a {@link PersistenceManagerFactory} with <code>loader
     * </code>. Any <code>IOException</code>s or <code>FileNotFoundException</code>s thrown during
     * resource loading will be wrapped in a {@link JDOFatalUserException}.
     *
     * @since JDO 2.0
     * @param propsFile the file containing the Properties
     * @param loader the class loader to use to load the <code>PersistenceManagerFactory</code> class
     * @return the PersistenceManagerFactory
     */
    public static JDOPersistenceManagerFactory getPersistenceManagerFactory(
            File propsFile, ClassLoader loader) {
        if (propsFile == null) throw new JDOFatalUserException(MSG.msg("EXC_GetPMFNullFile")); // NOI18N
        JDOPersistenceManagerFactory persistenceManagerFactory = null;
        try (InputStream in = new FileInputStream(propsFile)) {
            persistenceManagerFactory = getPersistenceManagerFactory(in, loader);
        } catch (FileNotFoundException fileNotFoundException) {
            throw new JDOFatalUserException(MSG.msg("EXC_GetPMFNoFile", propsFile), fileNotFoundException); // NOI18N
        } catch (IOException closeEexception) {
            // this code block is deliberately left empty,
        }
        return persistenceManagerFactory;
    }

    /**
     * Returns a {@link JDOPersistenceManagerFactory} configured based on the Properties stored in the
     * input stream at <code>stream</code>. This method is equivalent to invoking {@link
     * #getPersistenceManagerFactory(InputStream,ClassLoader)} with <code>
     * Thread.currentThread().getContextClassLoader()</code> as the <code>loader</code> argument.
     *
     * @since JDO 2.0
     * @param stream the stream containing the Properties
     * @return the PersistenceManagerFactory
     */
    public static JDOPersistenceManagerFactory getPersistenceManagerFactory(InputStream stream) {
        return getPersistenceManagerFactory(stream, getContextClassLoader());
    }

    /**
     * Returns a {@link PersistenceManagerFactory} configured based on the Properties stored in the
     * input stream at <code>stream</code>. Creates a {@link PersistenceManagerFactory} with <code>
     * loader</code>. Any <code>IOException</code>s thrown during resource loading will be wrapped in
     * a {@link JDOFatalUserException}.
     *
     * @since JDO 2.0
     * @param stream the stream containing the Properties
     * @param loader the class loader to use to load the <code>PersistenceManagerFactory</code> class
     * @return the PersistenceManagerFactory
     */
    public static JDOPersistenceManagerFactory getPersistenceManagerFactory(
            InputStream stream, ClassLoader loader) {
        if (stream == null) throw new JDOFatalUserException(MSG.msg("EXC_GetPMFNullStream")); // NOI18N

        Properties props = new Properties();
        try {
            props.load(stream);
        } catch (IOException ioe) {
            throw new JDOFatalUserException(MSG.msg("EXC_GetPMFIOExceptionStream"), ioe); // NOI18N
        }
        return getPersistenceManagerFactory(props, loader);
    }

    /**
     * Get a <code>JDOEnhancer</code> using the available enhancer(s) specified in
     * "META-INF/services/JDOEnhancer" using the context class loader.
     *
     * @return the <code>JDOEnhancer</code>.
     * @throws JDOFatalUserException if no available enhancer
     * @since JDO 3.0
     */
    public static JDOEnhancer getEnhancer() {
        return getEnhancer(getContextClassLoader());
    }

    /**
     * Get a <code>JDOEnhancer</code> using the available enhancer(s) specified in
     * "META-INF/services/JDOEnhancer"
     *
     * @param loader the loader to use for loading the JDOEnhancer class (if any)
     * @return the <code>JDOEnhancer</code>.
     * @throws JDOFatalUserException if no available enhancer
     * @since JDO 3.0
     */
    public static JDOEnhancer getEnhancer(ClassLoader loader) {
        ClassLoader ctrLoader = loader;
        if (ctrLoader == null) {
            ctrLoader = Thread.currentThread().getContextClassLoader();
        }

        /*
         * If you have a jar file that provides the jdo enhancer implementation,
         * a file naming the implementation goes into the file
         * packaged into the jar file, called "META-INF/services/javax.jdo.JDOEnhancer".
         * The contents of the file is a string that is the enhancer class name.
         * For each file in the class loader named "META-INF/services/javax.jdo.JDOEnhancer",
         * this method will invoke the default constructor of the implementation class.
         * Return the enhancer if a valid class name is extracted from resources and
         * the invocation returns an instance.
         * Otherwise add the exception thrown to an exception list.
         */
        ArrayList<Throwable> exceptions = new ArrayList<>();
        int numberOfJDOEnhancers = 0;
        Enumeration<URL> urls = null;
        try {
            urls = getResources(loader, SERVICE_LOOKUP_ENHANCER_RESOURCE_NAME);
        } catch (Throwable ex) {
            exceptions.add(ex);
        }

        if (urls != null) {
            while (urls.hasMoreElements()) {
                numberOfJDOEnhancers++;
                try {
                    String enhancerClassName = getClassNameFromURL(urls.nextElement());
                    Class<?> enhancerClass = forName(enhancerClassName, true, ctrLoader);
                    return (JDOEnhancer) enhancerClass.getConstructor().newInstance();
                } catch (Throwable ex) {
                    // remember exceptions from failed enhancer invocations
                    exceptions.add(ex);
                }
            }
        }

        throw new JDOFatalUserException(
                MSG.msg("EXC_GetEnhancerNoValidEnhancerAvailable", numberOfJDOEnhancers),
                exceptions.toArray(new Throwable[exceptions.size()]));
    }

    /**
     * Get the context class loader associated with the current thread. This is done in a doPrivileged
     * block because it is a secure method.
     *
     * @return the current thread's context class loader.
     * @since JDO 2.0
     */
    private static ClassLoader getContextClassLoader() {
        return doPrivileged(
                (PrivilegedAction<ClassLoader>) () -> Thread.currentThread().getContextClassLoader());
    }

    /**
     * Get the named resource as a stream from the resource loader. Perform this operation in a
     * doPrivileged block.
     */
    private static InputStream getResourceAsStream(
            final ClassLoader resourceLoader, final String name) {
        return doPrivileged(
                (PrivilegedAction<InputStream>) () -> resourceLoader.getResourceAsStream(name));
    }

    /**
     * Get the named Method from the named class. Perform this operation in a doPrivileged block.
     *
     * @param implClass the class
     * @param methodName the name of the method
     * @param parameterTypes the parameter types of the method
     * @return the Method instance
     */
    private static Method getMethod(
            final Class<?> implClass, final String methodName, final Class<?>[] parameterTypes)
            throws NoSuchMethodException {
        try {
            return doPrivileged(
                    (PrivilegedExceptionAction<Method>)
                            () -> implClass.getMethod(methodName, parameterTypes));
        } catch (PrivilegedActionException ex) {
            throw (NoSuchMethodException) ex.getException();
        }
    }

    /** Invoke the method. Perform this operation in a doPrivileged block. */
    private static Object invoke(
            final Method method, final Object instance, final Object[] parameters)
            throws IllegalAccessException, InvocationTargetException {
        try {
            return doPrivileged(
                    (PrivilegedExceptionAction<Object>) () -> method.invoke(instance, parameters));
        } catch (PrivilegedActionException ex) {
            Exception cause = ex.getException();
            if (cause instanceof IllegalAccessException) {
                throw (IllegalAccessException) cause;
            } else if (cause instanceof InvocationTargetException) {
                throw (InvocationTargetException) cause;
            } else {
                throw new JDOFatalInternalException(MSG.msg(EXC_GET_PMF_UNEXPECTED_EXCEPTION), cause);
            }
        }
    }

    /**
     * Get resources of the resource loader. Perform this operation in a doPrivileged block.
     *
     * @param resourceLoader ClassLoader to use for loading resources
     * @param resourceName Name of the resource
     * @return the resources
     * @throws IOException if an error occurs accessing the resources
     */
    protected static Enumeration<URL> getResources(
            final ClassLoader resourceLoader, final String resourceName) throws IOException {
        try {
            return doPrivileged(
                    (PrivilegedExceptionAction<Enumeration<URL>>)
                            () -> resourceLoader.getResources(resourceName));
        } catch (PrivilegedActionException ex) {
            throw (IOException) ex.getException();
        }
    }

    /**
     * Get the named class. Perform this operation in a doPrivileged block.
     *
     * @param name the name of the class
     * @param init whether to initialize the class
     * @param loader which class loader to use
     * @return the class
     */
    private static Class<?> forName(final String name, final boolean init, final ClassLoader loader)
            throws ClassNotFoundException {
        try {
            return doPrivileged(
                    (PrivilegedExceptionAction<Class<?>>) () -> Class.forName(name, init, loader));
        } catch (PrivilegedActionException ex) {
            throw (ClassNotFoundException) ex.getException();
        }
    }

    /**
     * Open an input stream on the url. Perform this operation in a doPrivileged block.
     *
     * @return the input stream
     */
    private static InputStream openStream(final URL url) throws IOException {
        try {
            return doPrivileged((PrivilegedExceptionAction<InputStream>) url::openStream);
        } catch (PrivilegedActionException ex) {
            throw (IOException) ex.getException();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T doPrivileged(PrivilegedAction<T> privilegedAction) {
        try {
            return (T) LegacyJava.doPrivilegedAction.invoke(null, privilegedAction);
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new JDOFatalInternalException(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T doPrivileged(PrivilegedExceptionAction<T> privilegedAction)
            throws PrivilegedActionException {
        try {
            return (T) LegacyJava.doPrivilegedExceptionAction.invoke(null, privilegedAction);
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (e.getCause() instanceof PrivilegedActionException) {
                throw (PrivilegedActionException) e.getCause();
            }
            throw new PrivilegedActionException(e);
        }
    }

    private static ObjectState getPersistentTransactionalObjectState(Object pc) {
        if (isDirty(pc)) {
            if (isNew(pc)) {
                return isDeleted(pc) ? ObjectState.PERSISTENT_NEW_DELETED : ObjectState.PERSISTENT_NEW;
            } else {
                return isDeleted(pc) ? ObjectState.PERSISTENT_DELETED : ObjectState.PERSISTENT_DIRTY;
            }
        } else {
            // Persistent Transactional Not Dirty
            return ObjectState.PERSISTENT_CLEAN;
        }
    }

    private static ObjectState getPersistentNonTransactionalObjectState(Object pc) {
        return isDirty(pc)
                ? ObjectState.PERSISTENT_NONTRANSACTIONAL_DIRTY
                : ObjectState.HOLLOW_PERSISTENT_NONTRANSACTIONAL;
    }

    private static ObjectState getTransientTransactionalObjectState(Object pc) {
        return isDirty(pc) ? ObjectState.TRANSIENT_DIRTY : ObjectState.TRANSIENT_CLEAN;
    }
    
    /**
     * This method is used mainly for logging or exception parameters
     * 
     * @param pc a persistence capable instance
     * 
     * @return its object id or the transactional object id in its absence
     */
    public static Object getAnyObjectId(
        Object pc
    ){
    	return 
    		pc == null ? null :
    		isPersistent(pc) ? getObjectId(pc) : 
    		getTransactionalObjectId(pc);
    }

    /**
     * This method replaces object values by their id and leaves other values
     * 
     * @param value a persistence capable instance of another value
     * 
     * @return the id in case of a persistence capable value, the value itself otherwise
     */
    public static Object replaceObjectById(
        Object value
    ){
    	return value instanceof PersistenceCapable ? getAnyObjectId(value) : value;
    }
    
}