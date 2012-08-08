/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Queries.java,v 1.6 2010/06/25 09:28:41 wfro Exp $
 * Description: OPENMDXQL Support
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/25 09:28:41 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.base.persistence.spi;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.jdo.JDOFatalUserException;
import javax.jdo.Query;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.kernel.exception.BasicException;
import org.w3c.spi2.Datatypes;

/**
 * OPENMDXQL Support
 */
public class Queries {

    /**
     * Constructor
     */
    private Queries() {
        // Avoid instantiation
    }

    /**
     * Apply filter and ordering
     * 
     * @param query
     * @param openmdxQuery
     */
    private static void setFilterAndOrdering(
        Query query, 
        String queryString
    ) {
        if (queryString != null) {
            int pos = 0;
            while (pos < queryString.length()) {
                char c = queryString.charAt(pos);
                if (Character.isWhitespace(c) || (c == ';')) {
                    pos++;
                } else {
                    int pos0 = queryString.indexOf("().", pos);
                    if (pos0 < 0) break;
                    String predicateName = queryString.substring(pos, pos0);
                    int pos1 = queryString.indexOf("(", pos0 + 2);
                    if (pos1 < 0) break;
                    String operator = queryString.substring(pos0 + 3, pos1);
                    // Parse operands
                    List<Object> operands = new ArrayList<Object>();
                    int pos2 = pos1 + 1;
                    Class<?> valueClass = null;
                    c = queryString.charAt(pos2);
                    while ((c != ')' && (pos2 < queryString.length()))) {
                        if (Character.isWhitespace(c) || (c == ',')) {
                            pos2++;
                        }
                        // String
                        else if (c == '"') {
                            StringBuilder s = new StringBuilder();
                            pos2++;
                            c = queryString.charAt(pos2);
                            while ((c != '"') && (pos2 < queryString.length())) {
                                s.append(c);
                                pos2++;
                                c = queryString.charAt(pos2);
                                if (c == '\\') {
                                    pos2++;
                                    c = queryString.charAt(pos2);
                                }
                            }
                            if ((valueClass == null)
                                || (valueClass == String.class)) {
                                operands.add(s.toString());
                            }
                            valueClass = null;
                            pos2++;
                        }
                        // Number, Date
                        else if ((c == '-') || Character.isDigit(c)) {
                            StringBuilder s = new StringBuilder();
                            while (!Character.isWhitespace(c) && (c != ',')
                                && (c != ')') && (pos2 < queryString.length())) {
                                s.append(c);
                                pos2++;
                                c = queryString.charAt(pos2);
                            }
                            if (valueClass != null) {
                                operands.add(Datatypes.create(valueClass, s
                                    .toString()));
                            } else {
                                operands.add(new BigDecimal(s.toString()));
                            }
                            valueClass = null;
                        }
                        // Type spec
                        else if (c == ':') {
                            StringBuilder s = new StringBuilder();
                            pos2++;
                            c = queryString.charAt(pos2);
                            while ((c != ':') && (pos2 < queryString.length())) {
                                s.append(c);
                                pos2++;
                                c = queryString.charAt(pos2);
                            }
                            String type = s.toString();
                            if ("string".equalsIgnoreCase(type)) {
                                valueClass = String.class;
                            } else if ("date".equals(type)) {
                                valueClass = XMLGregorianCalendar.class;
                            } else if ("datetime".equalsIgnoreCase(type)) {
                                valueClass = Date.class;
                            } else if ("short".equalsIgnoreCase(type)) {
                                valueClass = Short.class;
                            } else if ("int".equalsIgnoreCase(type)
                                || "integer".equalsIgnoreCase(type)) {
                                valueClass = Integer.class;
                            } else if ("long".equalsIgnoreCase(type)) {
                                valueClass = Long.class;
                            } else if ("decimal".equalsIgnoreCase(type)) {
                                valueClass = BigDecimal.class;
                            } else if ("duration".equalsIgnoreCase(type)) {
                                valueClass = Duration.class;
                            }
                            pos2++;
                        }
                        // Skip unknown char
                        else {
                            valueClass = null;
                            pos2++;
                        }
                        c = queryString.charAt(pos2);
                    }
                    try {
                        Method predicateMethod =
                            query.getClass().getMethod(predicateName);
                        Object predicate = predicateMethod.invoke(query);
                        Class<?> predicateClass =
                            predicateMethod.getReturnType();
                        Method operatorMethod = null;
                        int nArgs = 0;
                        // AnyTypePredicate
                        if ("equalTo".equalsIgnoreCase(operator)
                            && operands.size() == 1) {
                            operatorMethod =
                                predicateClass.getMethod(
                                    "equalTo",
                                    Object.class);
                            nArgs = 1;
                        } else if ("notEqualTo".equalsIgnoreCase(operator)
                            && operands.size() == 1) {
                            operatorMethod =
                                predicateClass.getMethod(
                                    "notEqualTo",
                                    Object.class);
                            nArgs = 1;
                        } else if ("elementOf".equalsIgnoreCase(operator)) {
                            operatorMethod =
                                predicateClass.getMethod(
                                    "elementOf",
                                    Collection.class);
                            nArgs = -1;
                        } else if ("notAnElementOf".equalsIgnoreCase(operator)) {
                            operatorMethod =
                                predicateClass.getMethod(
                                    "notAnElementOf",
                                    Collection.class);
                            nArgs = -1;
                        }
                        // OptionalFeaturePredicate
                        else if ("isNull".equalsIgnoreCase(operator)
                            && (operands.size() == 0)) {
                            operatorMethod = predicateClass.getMethod("isNull");
                            nArgs = 0;
                        } else if ("isNonNull".equalsIgnoreCase(operator)
                            && (operands.size() == 0)) {
                            operatorMethod =
                                predicateClass.getMethod("isNonNull");
                            nArgs = 0;
                        }
                        // BooleanTypePredicate
                        else if ("isTrue".equalsIgnoreCase(operator)
                            && (operands.size() == 0)) {
                            operatorMethod = predicateClass.getMethod("isTrue");
                            nArgs = 0;
                        } else if ("isFalse".equalsIgnoreCase(operator)
                            && (operands.size() == 0)) {
                            operatorMethod =
                                predicateClass.getMethod("isFalse");
                            nArgs = 0;
                        }
                        // MatchableTypePredicate
                        else if ("like".equalsIgnoreCase(operator)) {
                            operatorMethod =
                                predicateClass.getMethod(
                                    "like",
                                    Collection.class);
                            nArgs = -1;
                        } else if ("unlike".equalsIgnoreCase(operator)) {
                            operatorMethod =
                                predicateClass.getMethod(
                                    "unlike",
                                    Collection.class);
                            nArgs = -1;
                        } else if ("startsWith".equalsIgnoreCase(operator)) {
                            operatorMethod =
                                predicateClass.getMethod(
                                    "startsWith",
                                    Collection.class);
                            nArgs = -1;
                        } else if ("startsNotWith".equalsIgnoreCase(operator)) {
                            operatorMethod =
                                predicateClass.getMethod(
                                    "startsNotWith",
                                    Collection.class);
                            nArgs = -1;
                        } else if ("endsWith".equalsIgnoreCase(operator)) {
                            operatorMethod =
                                predicateClass.getMethod(
                                    "endsWith",
                                    Collection.class);
                            nArgs = -1;
                        } else if ("endsNotWith".equalsIgnoreCase(operator)) {
                            operatorMethod =
                                predicateClass.getMethod(
                                    "endsNotWith",
                                    Collection.class);
                            nArgs = -1;
                        }
                        // ComparableTypePredicate
                        else if ("between".equalsIgnoreCase(operator)
                            && operands.size() == 2) {
                            operatorMethod =
                                predicateClass.getMethod(
                                    "between",
                                    Comparable.class,
                                    Comparable.class);
                            nArgs = 2;
                        } else if ("outside".equalsIgnoreCase(operator)
                            && operands.size() == 2) {
                            operatorMethod =
                                predicateClass.getMethod(
                                    "outside",
                                    Comparable.class,
                                    Comparable.class);
                            nArgs = 2;
                        } else if ("lessThan".equalsIgnoreCase(operator)
                            && operands.size() == 1) {
                            operatorMethod =
                                predicateClass.getMethod(
                                    "lessThan",
                                    Comparable.class);
                            nArgs = 1;
                        } else if ("lessThanOrEqualTo"
                            .equalsIgnoreCase(operator)
                            && operands.size() == 1) {
                            operatorMethod =
                                predicateClass.getMethod(
                                    "lessThanOrEqualTo",
                                    Comparable.class);
                            nArgs = 1;
                        } else if ("greaterThanOrEqualTo"
                            .equalsIgnoreCase(operator)
                            && operands.size() == 1) {
                            operatorMethod =
                                predicateClass.getMethod(
                                    "greaterThanOrEqualTo",
                                    Comparable.class);
                            nArgs = 1;
                        } else if ("greaterThan".equalsIgnoreCase(operator)
                            && operands.size() == 1) {
                            operatorMethod =
                                predicateClass.getMethod(
                                    "greaterThan",
                                    Comparable.class);
                            nArgs = 1;
                        } else if ("ascending".equalsIgnoreCase(operator)
                            && operands.size() == 0) {
                            operatorMethod =
                                predicateClass.getMethod("ascending");
                            nArgs = 0;
                        } else if ("descending".equalsIgnoreCase(operator)
                            && operands.size() == 0) {
                            operatorMethod =
                                predicateClass.getMethod("descending");
                            nArgs = 0;
                        }
                        if (operatorMethod != null) {
                            if (nArgs == 0) {
                                operatorMethod.invoke(predicate);
                            } else if (nArgs == 1) {
                                operatorMethod.invoke(predicate, operands
                                    .get(0));
                            } else if (nArgs == 2) {
                                operatorMethod.invoke(predicate, operands
                                    .get(0), operands.get(1));
                            } else {
                                operatorMethod.invoke(predicate, operands);
                            }
                        }
                    } catch (Exception exception) {
                        throw BasicException.initHolder(
                            new JDOFatalUserException(
                                "Unknown predicate for query",
                                BasicException.newEmbeddedExceptionStack(
                                    exception,
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.BAD_QUERY_CRITERIA,
                                    new BasicException.Parameter(
                                        "query", 
                                        query.getClass().getName()
                                    ),
                                    new BasicException.Parameter(
                                        "predicate",
                                        predicateName
                                    )
                                )
                            )
                        );
                    }
                    pos = pos2 + 1;
                }
            }
        }
    }
    
    /**
     * Set the candidate collection
     * 
     * @param query
     * @param openmdxQuery
     */
    private static void setCandidates(
        Query query, 
        Collection<?> candidates
    ) {
        if(candidates != null) {
            query.setCandidates(candidates);
        }
    }

    /**
     * Create a query instance
     * 
     * @param refPackage
     * @param openmdxQuery
     * 
     * @return a new populated query instance
     */
    public static Query prepareQuery(
        Query query,
        Collection<?> candidates,
        String queryString
    ){
        setFilterAndOrdering(
            query, 
            queryString
        );
        setCandidates(
            query,
            candidates
        );
        return query;
    }

}
