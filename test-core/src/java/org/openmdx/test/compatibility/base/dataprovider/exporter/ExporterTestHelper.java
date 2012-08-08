/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ExporterTestHelper.java,v 1.9 2007/02/02 15:33:33 hburger Exp $
 * Description: class TestHelper
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/02/02 15:33:33 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.test.compatibility.base.dataprovider.exporter;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.jmi.reflect.RefObject;

import junit.framework.Assert;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSelectors;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.Directions;
import org.openmdx.compatibility.base.dataprovider.cci.QualityOfService;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.transport.dispatching.DataproviderObjectMarshaller;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.compatibility.base.dataprovider.layer.model.RoleAttributes;
import org.openmdx.compatibility.base.dataprovider.layer.model.State_1_Attributes;
//#if defined(OPENMDX1)
import org.openmdx.compatibility.state1.cci.State;
//#else
//import org.openmdx.compatibility.state1.jmi1.State;
//#endif

/**
 * provide some helper tools for writing tests.
 * 
 * @author anyff
 */
public class ExporterTestHelper {
    
    /**
     * Avoid instantiation
     */
    protected ExporterTestHelper(){        
    }
    
    static final public short createOperation = 0;
    static final public short getOperation = 1;
    static final public short findOperation = 2;
    static final public short modifyOperation = 3;
    static final public short replaceOperation = 4;
    static final public short removeOperation = 5;
    static final public short setOperation = 6;

    static final public String HISTORY_REQUEST_START = "historyRequestStart";
    static final public String HISTORY_REQUEST_END = "historyRequestEnd";
    static final public String VALID_REQUEST_START = "validRequestStart";
    static final public String VALID_REQUEST_END = "validRequestEnd";

    /**
     * decide if the string is a stateNumber, ie. contains only numbers.
     */
    static private boolean isStateNumber(String cand) {
        int i = 0;
        for (i = 0; i < cand.length() && Character.isDigit(cand.charAt(i)); i++) {
        }

        return i == cand.length();
    }

    /**
     * compare the two objects.
     * <p>
     * If pathCompletion is set, the path of the result object may differ from
     * the path of the expect object by the pathCompletion followed by a number
     * (state).
     * 
     * @param result          result object to compare
     * @param expect          expected object to compare
     * @param pathCompletion  pathCompletion allowed for expected object
     */
    static public void compareDataproviderObjects(
        DataproviderObject_1_0 result,
        DataproviderObject_1_0 expect,
        String pathCompletion
    ) {
        // compare path 
        if (pathCompletion == null) {
            Assert.assertTrue(
                " differing paths : " + "\n Expected: " + expect.path() + "\n Received: " + result.path(),
                result.path().equals(expect.path()));
        } else {
            Path resultPath = (Path) result.path().clone();
            Path expectPath = (Path) expect.path().clone();

            Assert.assertTrue(
                " expected path with " + pathCompletion + "/<someNumber>, " + "/n but received: " + result.path(),
                isStateNumber(((String) resultPath.remove(resultPath.size() - 1)))
                    && pathCompletion.equals(resultPath.remove(resultPath.size() - 1)));

            Assert.assertTrue(
                " differing paths : " + "\n Expected: " + expect.path() + "\n Received: " + result.path(),
                resultPath.equals(expectPath));
        }

        for (Iterator i = result.attributeNames().iterator(); i.hasNext();) {
            String attributeName = (String) i.next();

            if (attributeName.equals("context:Datastore:object_class")
                || attributeName.equalsIgnoreCase("identity")) {
                continue;
            }
            
            if (attributeName.equals(State_1_Attributes.INVALIDATED_AT)) {
                // just make sure that they both have an entry
                Assert.assertTrue(
                    "attribute "
                        + attributeName
                        + " only set in one object. "
                        + " Expected: "
                        + expect.values(attributeName)
                        + " Received: "
                        + result.values(attributeName)
                        + " Expected object: "
                        + expect.path(),
                     (expect.values(attributeName).isEmpty() && 
                      result.values(attributeName).isEmpty()) 
                      ||
                      (!expect.values(attributeName).isEmpty() && 
                       !result.values(attributeName).isEmpty()) 
                );
            } else if (attributeName.equals(RoleAttributes.HAS_ROLE)) {
                // compare role attributes regardless of sequence
                for (Iterator vIter = expect.values(attributeName).iterator(); vIter.hasNext();) {
                    String v = (String) vIter.next();
                    if (v != null) {
                        Assert.assertTrue("missing value for " + attributeName + ": " + v, result.values(RoleAttributes.HAS_ROLE).contains(v));
                    }
                }
                Assert.assertTrue(
                    "too many values in result of "
                        + attributeName
                        + " Expected: "
                        + expect.values(attributeName)
                        + " Received: "
                        + result.values(attributeName)
                        + " Expected object: "
                        + expect.path(),
                    expect.values(attributeName).size() == result.values(attributeName).size());
            } else if (
                !attributeName.equals(SystemAttributes.MODIFIED_AT)
                 && !attributeName.equals(SystemAttributes.CREATED_AT)
                 && !attributeName.equals(SystemAttributes.MODIFIED_AT)
                 && !attributeName.equals(SystemAttributes.CREATED_AT)
            ) {

                Iterator expectedAttrValues = expect.values(attributeName).iterator();
                Iterator resultAttrValues = result.values(attributeName).iterator();
                while (expectedAttrValues.hasNext()) {

                    Object expectedValue = expectedAttrValues.next();
                    Object resultValue = resultAttrValues.next();

                    if (expectedValue instanceof Short)
                        resultValue = new Short(((Short)resultValue).shortValue());
                        //expectedValue = new BigDecimal(((Short) expectedValue).shortValue());
                    if (expectedValue instanceof Integer)
                        resultValue = new Integer(((Integer)resultValue).intValue());
                        // expectedValue = new BigDecimal(((Integer) expectedValue).intValue());
                    if (expectedValue instanceof Long && resultValue instanceof BigDecimal)
                        resultValue = new Long(((BigDecimal)resultValue).longValue());
                        //expectedValue = new BigDecimal(((Long) expectedValue).longValue());
                    if (expectedValue instanceof Float)
                        resultValue = new Float(((Float)resultValue).floatValue());
                        //expectedValue = new BigDecimal(((Float) expectedValue).floatValue());
                    if (expectedValue instanceof Double)
                        resultValue = new Double(((Double)resultValue).doubleValue());
                        //expectedValue = new BigDecimal(((Double) expectedValue).doubleValue());
                    if (expectedValue instanceof BigDecimal && resultValue instanceof BigDecimal)
                        expectedValue = ((BigDecimal)expectedValue).setScale(((BigDecimal)resultValue).scale());
                        
                    Assert.assertEquals(
                        "differing attribute: " + attributeName + " expected object: " + expect + " received object: " + result,
                        expectedValue,
                        resultValue);

                }
            }
        }
    }

    /** 
     * do checks for several objects.
     * The objects in the two lists may be at random!
     * <p>
     * if completeExpected is true, results and expected must be equal. If it is
     * false, all the objects from expected must be contained in results, but
     * there may be more.
     * 
     * @param results is a list containing the results.
     * @param expect  an Array containing the expected results.
     * @param pathCompletion additional components the path must contain
     * @param ordered if true the objects must be in the same order
     * @param completeExpected if expected must contain all objects in results
     * 
     * @return sorted List of results.
     */
    static public List checkResultList(
        List results,
        DataproviderObject_1_0[] expect,
        String pathCompletion,
        boolean ordered,
        boolean completeExpected
    ) {
        // first get entire list:
        ArrayList allResults = new ArrayList();
        for (ListIterator i = results.listIterator(); i.hasNext();) {
            allResults.add(i.next());
        }

        ArrayList sortedResults = new ArrayList();

        for (int i = 0; i < allResults.size(); i++) {
            System.out.println("Received: " + allResults.get(i));
        }

        for (int i = 0; i < expect.length; i++) {
            System.out.println("Expected: " + expect[i]);
        }

        Assert.assertTrue(
            "expected: " + expect.length + " results, but received " + allResults.size(),
            expect.length == allResults.size() || !completeExpected);

        for (int i = 0; i < expect.length; i++) {
            if (ordered) {
                // objects must be at same position
                compareDataproviderObjects((DataproviderObject_1_0) allResults.get(i), expect[i], pathCompletion);
            } else {
                boolean found = false;
                Error error = null;

                for (int j = 0; j < expect.length && !found; j++) {
                    // in the presence of states, there may be several states
                    // per object, wich all would match the path (hopefully).
                    // Thus have to try all states to find if one eventually 
                    // matches.
                    try {
                        Path cpath = ((DataproviderObject_1_0) allResults.get(j)).path();
                        // test only those which have a chance to match
                        if (cpath.startsWith(expect[i].path())) {
                            compareDataproviderObjects((DataproviderObject_1_0) allResults.get(j), expect[i], pathCompletion);

                            found = true;
                            sortedResults.add(allResults.get(j));
                        }
                    } catch (Error ex) {
                        // junit throws errors
                        error = ex;
                    }
                }
                Assert.assertTrue(
                    "no matching object for " + expect[i].path() + ". Last error: " + (error == null ? null : error.getMessage()),
                    found);
            }
        }
        return ordered ? results : sortedResults;
    }

    /**
     * Set valid period of object.
     * <p>
     * For creating a copy see createExpected()
     */
    static public DataproviderObject setValidity(
        DataproviderObject object,
        String from,
        String to) {
        if (from != null) {
            object.values(State_1_Attributes.VALID_FROM).set(0, dateAsString(from));
        } else {
            object.attributeNames().remove(State_1_Attributes.VALID_FROM);
        }

        if (to != null) {
            object.values(State_1_Attributes.VALID_TO).set(0, dateAsString(to));
        } else {
            object.attributeNames().remove(State_1_Attributes.VALID_TO);
        }

        object.attributeNames().remove(State_1_Attributes.INVALIDATED_AT);

        return object;
    }

    static public DataproviderObject createExpected(DataproviderObject object, String from, String to) {
        DataproviderObject copy = new DataproviderObject(object);

        setValidity(copy, from, to);
        return copy;
    }

    static public DataproviderObject createState(DataproviderObject object, String from, String to) {
        DataproviderObject copy = new DataproviderObject(object);

        setValidity(copy, from, to);
        return copy;
    }

    static public String prepareDateAsString(String date) {
        return dateAsString(date);
    }

    /**
     * Converts a stringified date into a java.util.Date and back to String to
     * ensure correct format. 
     * 
     * Date format to use: dd.mm.yyyy
     * returns spice formatted date.
     * 
     */
    static public String dateAsString(String date) {
        final DateFormat df = DateFormat.getInstance();

        final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy,z");

        String dateString = null;

        try {
            if (date != null) {
                if (date.endsWith("Z")) {
                    // this is already a spice date
                    dateString = date;
                } else {
                    Date tmpDate = sdf.parse(date + ",GMT");
                    dateString = df.format(tmpDate);
                }
            }
        } catch (ParseException pe) {

            Assert.assertTrue("prepareDate: exception on parsing " + date, false);
        }

        return dateString;
    }

    static DataproviderObject toDataproviderObject(
        RefObject source, 
        Model_1_0 model
    ) throws ServiceException {
        if(source instanceof RefObject_1_0) {
            return toDataproviderObject(
                ((RefObject_1_0)source).refDelegate(),
                model
            );
        } else throw new UnsupportedOperationException(
            ExporterTestHelper.class.getName() + 
            ": toDataproviderObject() not implemented yet for openMDX 2"
        );
    }

    static DataproviderObject toDataproviderObject(
        Object_1_0 source, 
        Model_1_0 model
    ) throws ServiceException {
        
        Set requiredSet = new HashSet();
        
        ModelElement_1_0 classDef = model.getElement(
          source.objGetClass()
        );
        Map classAttributes =   model.getAttributeDefs(
            classDef, false, true);
        
        for (Iterator a = classAttributes.keySet().iterator(); a.hasNext();) {
            String attribute = (String)a.next();
            requiredSet.add(attribute);
        }
        
        /*
        
        requiredSet.add("object_hasRole");
        requiredSet.add("object_inRole");
        
        for (Iterator f = requiredSet.iterator(); f.hasNext();) {
            String feature = (String) f.next();
            ModelElement_1_0 featureDef = model.getFeatureDef(
              classDef,
              feature,
              true
            );
            
            if (featureDef == null) {
                f.remove();
            }
        }
        */
                
        return DataproviderObjectMarshaller.toDataproviderObject(
            new Path(source.objGetPath()), source, requiredSet, model);
/*
        DataproviderObject target = 
            new DataproviderObject(new Path(source.objGetPath()));
        target.values(SystemAttributes.OBJECT_CLASS).add(source.objGetClass());
        ModelElement_1_0 classDef = 
            model.getElement(source.objGetClass());

        // first get a value which must be present, to load the object in the cache
        source.objGetValue(SystemAttributes.CREATED_AT);

        for (Iterator i = source.objDefaultFetchGroup().iterator(); i.hasNext();) {
            String attribute = (String) i.next();

            if (!attribute.equalsIgnoreCase("view") 
                && !attribute.equalsIgnoreCase("role")
            ) {
                ModelElement_1_0 attributeDef = 
                    model.getFeatureDef(classDef, attribute, true);

                if (SystemAttributes.OBJECT_INSTANCE_OF.equals(attribute)) {
                    setDataproviderObjectValue(
                        source.objGetList(attribute), 
                        target.clearValues(attribute)
                    );
                    continue;
                }
                ModelElement_1_0 attributeType = 
                    model.getDereferencedType(attributeDef.values("type").get(0));
                // structure types are not supported
                if (model.isStructureType(attributeType)) {
                    throw new ServiceException(
                        StackedException.DEFAULT_DOMAIN,
                        StackedException.INVALID_CONFIGURATION,
                        new BasicException.Parameter[] {
                            new BasicException.Parameter("object", source),
                            new BasicException.Parameter("field", attribute),
                            new BasicException.Parameter("type", attributeType)
                        },
                        "structure types can not be transferred to DataproviderObjects");
                }
                String multiplicity = 
                    (String) attributeDef.values("multiplicity").get(0);

                if (Multiplicities.SET.equals(multiplicity)) {
                    setDataproviderObjectValue(
                        source.objGetSet(attribute), 
                        target.clearValues(attribute)
                    );
                } else if (Multiplicities.LIST.equals(multiplicity) 
                    || Multiplicities.MULTI_VALUE.equals(multiplicity)
                ) {
                    setDataproviderObjectValue(
                        source.objGetList(attribute), 
                        target.clearValues(attribute)
                    );
                } else if (Multiplicities.SPARSEARRAY.equals(multiplicity)) {
                    setDataproviderObjectValue(
                        source.objGetSparseArray(attribute), 
                        target.clearValues(attribute)
                    );
                } else if (attributeDef.getValues("object_class").get(0).equals("org:omg:model1:Reference")) {
                    target.clearValues(attribute);
                    for (Iterator r = source.objGetList(attribute).iterator(); r.hasNext();) {
                        Object_1_0 obj1 = (Object_1_0) r.next();
                        if (obj1 != null) {
                            target.values(attribute).add(obj1.objGetPath());
                        }
                    }
                } else {
                    setDataproviderObjectValue(
                        source.objGetValue(attribute), 
                        target.clearValues(attribute)
                    );
                }
            }
        }
        return target;
        */
    }

//    private static void setDataproviderObjectValue(Object sourceValue, SparseList targetValue) {
//
//        try {
//            if (sourceValue == null)
//                return;
//
//            if (sourceValue instanceof SparseArray) {
//                for (PopulationIterator j = ((SparseArray) sourceValue).populationIterator(); j.hasNext();) {
//                    Object obj = j.next();
//                    if (obj instanceof Object_1_0) {
//                        targetValue.set(j.nextIndex()-1, ((Object_1_0) obj).objGetPath());
//                    } else {
//                        targetValue.set(j.nextIndex()-1, obj);
//                    }
//                }
//
//            } else if (sourceValue instanceof Collection) {
//
//                Object[] sourceObjects = ((Collection) sourceValue).toArray();
//                for (int i = 0; i < sourceObjects.length; i++) {
//                    if (sourceObjects[i] instanceof Object_1_0)
//                        targetValue.set(i, ((Object_1_0) sourceObjects[i]).objGetPath());
//                    else
//                        targetValue.set(i, sourceObjects[i]);
//                }
//
//            } else {
//                if (sourceValue instanceof Object_1_0) {
//                    targetValue.add(((Object_1_0) sourceValue).objGetPath());
//                } else {
//                    targetValue.add(sourceValue);
//                }
//            }
//        } catch (ServiceException e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
//    }

    /**
     * Converts a stringified date into a Date. 
     * 
     * Date format to use: dd.mm.yyyy
     */
    static public Date asDate(String dateString) {

        final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy,z");
        Date date = null;

        try {
            date = sdf.parse(dateString + ",GMT");
        } catch (ParseException pe) {

            Assert.assertTrue("prepareDate: exception on parsing " + date, false);
        }

        return date;
    }

    /**
     * Converts a Date into a formatted String. 
     * 
     * Date format to use: dd.mm.yyyy
     */
    static public String asString(Date date) {

        final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy");

        if (date == null)
            return "          ";

        return sdf.format(date);
    }

    /**
     * Execute an operation on the provider with the object specified.
     * <p>
     * requestedAt, requestedFor may be null in which case the default is used.
     * 
     * @param operation operation to execute (createOperation, findOperation...)
     * @param provider provider to execute operation on
     * @param object   object holding the data/path
     * @param requestedAt  set RequestedAt date to this 
     * @param requestedFor set RequestedFor date to this
     * @param expected   expected exception (leave null if none)
     */
    static public Object executeOperation(
        short operation,
        Dataprovider_1_0 provider,
        DataproviderObject object,
        String requestedAt,
        String requestedFor,
        ServiceException expected
    ) {
        Object result = null;

        ServiceHeader header =
            new ServiceHeader(
                "anyff",
                null,
                false,
                new QualityOfService(),
                requestedAt == null ? null : dateAsString(requestedAt),
                requestedFor == null ? null : dateAsString(requestedFor));

        RequestCollection requests = new RequestCollection(header, provider);

        try {
            switch (operation) {
                case createOperation :
                    result = requests.addCreateRequest(object);
                    break;
                case getOperation :
                    result = requests.addGetRequest(object.path(), AttributeSelectors.ALL_ATTRIBUTES, new AttributeSpecifier[0]);
                    break;
                case modifyOperation :
                    result = requests.addModifyRequest(object);
                    break;
                case replaceOperation :
                    result = requests.addReplaceRequest(object);
                    break;
                case setOperation :
                    result = requests.addSetRequest(object);
                    break;
                case removeOperation :
                    short selector = AttributeSelectors.NO_ATTRIBUTES;
                    if (object.getValues("selector") != null) {
                        selector = ((Short) object.getValues("selector").get(0)).shortValue();
                    }

                    result = requests.addRemoveRequest(object.path(), selector, null);

                    break;
                case findOperation :
                    ArrayList filters = new ArrayList();
                    // all the attributes of the object are treated as 
                    // properties:
                    for (Iterator i = object.attributeNames().iterator(); i.hasNext();) {
                        String attributeName = (String) i.next();
                        // introducing some technical attributes:

                        if (attributeName.equals(HISTORY_REQUEST_START)) {
                            filters.add(
                                new FilterProperty(
                                    Quantors.THERE_EXISTS,
                                    SystemAttributes.MODIFIED_AT,
                                    FilterOperators.IS_GREATER_OR_EQUAL,
                                    object.values(attributeName).toArray()));
                        } else if (attributeName.equals(HISTORY_REQUEST_END)) {
                            filters.add(
                                new FilterProperty(
                                    Quantors.THERE_EXISTS,
                                    State_1_Attributes.INVALIDATED_AT,
                                    FilterOperators.IS_LESS_OR_EQUAL,
                                    object.values(attributeName).toArray()));
                        } else if (attributeName.equals(VALID_REQUEST_START)) {
                            // WARNING: does not work if valid_to is null!
                            filters.add(
                                new FilterProperty(
                                    Quantors.THERE_EXISTS,
                                    State_1_Attributes.VALID_TO,
                                    FilterOperators.IS_GREATER_OR_EQUAL,
                                    object.values(attributeName).toArray()));
                        } else if (attributeName.equals(VALID_REQUEST_END)) {
                            // WARNING: does not work if validFrom is null!
                            filters.add(
                                new FilterProperty(
                                    Quantors.THERE_EXISTS,
                                    State_1_Attributes.VALID_FROM,
                                    FilterOperators.IS_LESS_OR_EQUAL,
                                    object.values(attributeName).toArray()));
                        } else {
                            // it is a normal attribute
                            filters.add(
                                new FilterProperty(
                                    Quantors.THERE_EXISTS,
                                    attributeName,
                                    FilterOperators.IS_IN,
                                    object.values(attributeName).toArray()));
                        }
                    }

                    result =
                        requests.addFindRequest(
                            object.path(),
                            (FilterProperty[]) filters.toArray(new FilterProperty[0]),
                            AttributeSelectors.ALL_ATTRIBUTES,
                            0,
                            1000,
                            Directions.ASCENDING);

                    break;
                default :
                    Assert.assertTrue("unsupported operation: " + operation, false);
            }
            if (expected != null) {
                Assert.assertTrue(" expected exception " + expected.getClass() + "\n but none was thrown ", expected == null);
            }
        } catch (ServiceException es) {
            ServiceException e = (ServiceException) es.getExceptionStack().getExceptionStack().get(0);

            if (expected != null) {
                Assert.assertTrue(
                    " expected exception " + expected.toString() + "\n but caught exception " + e.toString(),
                    expected.getExceptionDomain().equals(e.getExceptionDomain())
                        && expected.getExceptionCode() == e.getExceptionCode()
                        && (expected.getExceptionStack().getDescription() == null || expected.getExceptionStack().getDescription().equals(e.getExceptionStack().getDescription())));
            } else {
                Assert.assertTrue(" expected no exception " + "\n but caught exception " + e.toString(), false);

                System.out.println("#### ServiceException: " + es.getMessage());
                es.printStackTrace();
                List exceptions = es.getExceptionStack().getExceptionStack();
                for (Iterator i = exceptions.iterator(); i.hasNext();) {
                    Exception ee = (Exception) i.next();
                    System.out.println("#### Exception: " + ee.getMessage());
                    ee.printStackTrace();
                    ee.printStackTrace();
                }

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            Assert.assertTrue(" expected ServiceException, but got " + e.getClass(), false);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /** 
     * Map the paths path and the paths in the objects according to the mapping 
     * rules in the pathMap.
     * 
     * @param pathMap
     * @param path
     * @param states
     * @return Path
     */
    public static Path mapAllPaths(
        Map pathMap, 
        Path path, 
        ArrayList objects
    ) {
        // map path
        Path prefix = null;
        for (Iterator keyIter = pathMap.keySet().iterator(); keyIter.hasNext() && prefix == null;) {
            Path current = (Path) keyIter.next();
            if (path.startsWith(current)) {
                prefix = (Path) pathMap.get(current);
            }
        }
        if (prefix != null) {
            Path newPath = new Path(prefix);
            newPath.addAll(path.getSuffix(prefix.size()));
            path = newPath;

            for (Iterator s = objects.iterator(); s.hasNext();) {
                DataproviderObject dpo = (DataproviderObject) s.next();
                newPath = new Path(prefix);
                newPath.addAll(dpo.path().getSuffix(prefix.size()));
                dpo.path().setTo(newPath);
            }
        }

        // map the paths in the objects values

        for (Iterator o = objects.iterator(); o.hasNext();) {
            DataproviderObject_1_0 object = (DataproviderObject_1_0) o.next();

            for (Iterator a = object.attributeNames().iterator(); a.hasNext();) {
                String attribute = (String) a.next();

                for (int i = 0; i < object.values(attribute).size(); i++) {
                    Object value = object.values(attribute).get(i);

                    if (value instanceof Path) {
                        Path pathValue = (Path) value;
                        Path newPath = null;

                        for (Iterator mapIter = pathMap.keySet().iterator(); mapIter.hasNext() && newPath == null;) {
                            Path current = (Path) mapIter.next();
                            if (pathValue.startsWith(current)) {
                                newPath = new Path((Path) pathMap.get(current));
                                newPath.addAll(pathValue.getSuffix(newPath.size()));
                            }
                        }

                        if (newPath != null) {
                            object.values(attribute).set(i, newPath);
                        }
                    }
                }
            }
        }

        return path;
    }
    
    
    /**
     * order the list of dataprovider objects according to the attribute 
     * specified.
     *
     */
    static public void orderObjects(
        List objects, 
        String attribute
    ) {        
        if (objects.size() > 1) {
            final String attributeName = attribute; 
            Collections.sort(
                objects,  
                new Comparator () {          
                    public int compare(Object o1, Object o2) {
                        if (o1 == null || o2 == null || 
                            !(o1 instanceof DataproviderObject_1_0) ||
                            !(o2 instanceof DataproviderObject_1_0)
                        ) {
                            throw new ClassCastException(
                                "empty object " + attributeName  
                                + " o1: " + o1 
                                + " o2: "+ o2
                             );
                        }

                        if (attributeName.equals("path")) {
                            return 
                                ((DataproviderObject_1_0) o1).path().compareTo(
                                    ((DataproviderObject_1_0) o2).path()
                                );
                        }
                            
                        
                        Object att1 = null;
                        Object att2 = null;
                        if (o1 instanceof DataproviderObject_1_0 &&
                            ((DataproviderObject_1_0) o1).values(attributeName) != null &&
                            ((DataproviderObject_1_0) o1).values(attributeName).size() > 0
                        ) {
                            att1 = ((DataproviderObject_1_0) o1).values(attributeName).get(0);
                        }
                        
                        if (o2 instanceof DataproviderObject_1_0 &&
                            ((DataproviderObject_1_0) o2).values(attributeName) != null &&
                            ((DataproviderObject_1_0) o2).values(attributeName).size() > 0
                        ) {
                            att2 = ((DataproviderObject_1_0) o2).values(attributeName).get(0);
                        }
                                                
                        if (att1 == att2) {  // if they are both null
                            return 0;
                        }
                        if (att1.equals(att2)) {
                            return 0;
                        }
                        if (att1 instanceof String && att2 instanceof String) {
                            return ((String)att1).compareTo((String)att2);
                        }
                        
                        if (att1 instanceof Number && att2 instanceof Number) {
                            // equal has been treated
                            return ((Number)att1).doubleValue() < ((Number)att2).doubleValue() ? -1 : 1;
                        }
                        
                        throw new ClassCastException(
                            "can not compare objects values for " + attributeName 
                            + " att1: " + att1 
                            + " att2: "+ att2);
                    }
                    
                    public boolean equals(Object obj) {
                        return obj == this;
                    }
                }
            );
        }
    }


    /**
     * check that the objects have the valid states as expected. 
     * <p>
     * objectMap must contain object paths and the states of that object.
     * <p>
     * pathMap may contain prefix paths. Each path prefix from the objectMap 
     * which matches a key of the pathMap is replaced by the value of the map 
     * entry. This allows using the same objectMap for two different providers.
     * 
     * 
     * @param dataprovider      datapovider with the objects to check for
     * @param objectMap         objects and their paths which should be present
     * @param pathMap           remap paths of objects
     * @throws JmiServiceException
     */
    static public void checkDBForValidStates(
        Dataprovider_1_0 dataprovider,
        Map objectMap,
        Map pathMap
    ) throws ServiceException {

        for (Iterator k = objectMap.keySet().iterator(); k.hasNext();) {
            Path path = (Path) k.next();
            ArrayList states = (ArrayList) objectMap.get(path);

            path = mapAllPaths(pathMap, path, states);

            if (path.get(path.size() - 2).equals("role")) {
                // role path
            } else {
                DataproviderObject obj = new DataproviderObject(path.getChild("validState"));

                List validStates = (List) ExporterTestHelper.executeOperation(ExporterTestHelper.findOperation, dataprovider, obj, null, null, null);

                ExporterTestHelper.checkResultList(
                    validStates,
                    (DataproviderObject_1_0[]) states.toArray(new DataproviderObject_1_0[0]),
                    "validState",
                    true,
                    true);
            }
        }
    }

    /** 
     * check that the objects at a certain path are as expected.
     * <p>
     * objectMap contains reference paths and all the objects expected to be at
     * this path. For stated objects it is just the current state.
     * <p> 
     * In case of roles, unexpected roles are detected because of
     * object_hasRole. 
     * <p>
     * To check roles explicitely the path must be an object path and contain
     * the ending .../role/<roleName>.
     * <p>
     * if mapHasAllObjects is true, its an error if there are more objects found 
     * at path, then there are contained in the map for this path.
     * 
     * @param dataprovider      datapovider with the objects to check for
     * @param objectMap
     * @param mapHasAllObjects
     */
    static public void checkDBForObjects(
        Dataprovider_1_0 dataprovider,
        Map objectMap,
        Map pathMap,
        boolean mapHasAllObjects,
        boolean ordered
    ) {
        
        Assert.assertTrue(
            "need objectMap and pathMap to do the test correctly (remove --noSetup).", 
            objectMap != null && pathMap != null
        );
        for (Iterator k = objectMap.keySet().iterator(); k.hasNext();) {
            Path path = (Path) k.next();
            ArrayList objects = (ArrayList) objectMap.get(path);

            path = mapAllPaths(pathMap, path, objects);
            
            // any ??? orderObjects(objects, SystemAttributes.MODIFIED_AT);

            if (path.get(path.size() - 2).equals("role")) {
                // role path
                DataproviderObject obj = new DataproviderObject(path);

                DataproviderObject_1_0 role =
                    (DataproviderObject_1_0) ExporterTestHelper.executeOperation(ExporterTestHelper.getOperation, dataprovider, obj, null, null, null);

                ExporterTestHelper.compareDataproviderObjects(role, (DataproviderObject_1_0) objects.get(0), null);
            } 
            else {
                DataproviderObject obj = new DataproviderObject(path);

                List results = (List) ExporterTestHelper.executeOperation(ExporterTestHelper.findOperation, dataprovider, obj, null, null, null);

                ExporterTestHelper.checkResultList(
                    results,
                    (DataproviderObject_1_0[]) objects.toArray(new DataproviderObject_1_0[0]),
                    null,
                    ordered,
                    mapHasAllObjects
                );
            }
        }
    }

    static void showValidStates(
        State aStatedObject
    ) {

        Iterator stats = aStatedObject.getValidState().iterator();
        int stateNumber = 1;
        
        System.out.println();
        System.out.println(aStatedObject.refMofId());
        
        while (stats.hasNext()) {
            State state = (State) stats.next();
            System.out.print("State " + stateNumber + ": ");
            System.out.print(asString(state.getObject_validFrom()) + " - ");
            System.out.println(asString(state.getObject_validTo()));
            stateNumber++;
        }

    }
}
