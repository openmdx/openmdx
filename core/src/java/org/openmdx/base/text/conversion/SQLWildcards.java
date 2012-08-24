/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: SQL Wildcards 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006, OMEX AG, Switzerland
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

package org.openmdx.base.text.conversion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * SQL Wildcards
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class SQLWildcards {

    /**
     * Constructor 
     * 
     * @param escape the SQL escape character
     */
    public SQLWildcards(
        char escape
    ) {
        this.escape = escape;
    }

    /**
     * the SQL escape character
     */
    private final char escape;
    
    /**
     * Convert JDO wildcard expressions to SQL wildcard expressions
     * <p>
     * Conversion rules:<ul>
     * <li>&lsquo;.*&rsquo; &rarr; &lsquo;%&rsquo;
     * <li>&lsquo;.&rsquo; &rarr; &lsquo;_&rsquo;
     * <li>&lsquo;%&rsquo; &rarr; escape + &lsquo;%&rsquo;
     * <li>&lsquo;_&rsquo; &rarr; escape + &lsquo;_&rsquo;
     * <li>&lsquo;\.\*&rsquo; &rarr; &lsquo;.*&rsquo;
     * <li>&lsquo;\.&rsquo; &rarr; &lsquo;.&rsquo;
     * </ul>
     * 
     * @param jdoExpression the JDO expression to be converted
     * 
     * @return the corresponding SQL expression
     */
    public String fromJDO(
        String jdoExpression
    ){
        StringBuilder sqlExpression = new StringBuilder();
        for(
            int i = 0, iLimit = jdoExpression.length();
            i < iLimit;
            i++
        ){
            char c = jdoExpression.charAt(i);
            switch(c){
                case '.':
                    int j = i + 1;
                    if(j < iLimit && jdoExpression.charAt(j) == '*') {
                        i = j;
                        sqlExpression.append(
                            '%'
                        );                        
                    } else {
                        sqlExpression.append(
                            '_'
                        );
                    }
                    break;
                case '%': case '_':
                    sqlExpression.append(
                        this.escape
                    ).append(
                        c
                    );
                    break;
                case '\\':
                    if(i++ < iLimit) sqlExpression.append(
                        jdoExpression.charAt(i)
                    );
                    break;
                default:
                    sqlExpression.append(
                        c
                    );
            }
        }
        return sqlExpression.toString();
    }
    
    /**
     * Convert a collection of JDO wildcard expressions to a collection of SQL 
     * wildcard expressions
     * <p>
     * Conversion rules:<ul>
     * <li>&lsquo;.*&rsquo; &rarr; &lsquo;%&rsquo;
     * <li>&lsquo;.&rsquo; &rarr; &lsquo;_&rsquo;
     * <li>&lsquo;%&rsquo; &rarr; escape + &lsquo;%&rsquo;
     * <li>&lsquo;_&rsquo; &rarr; escape + &lsquo;_&rsquo;
     * <li>&lsquo;\.\*&rsquo; &rarr; &lsquo;.*&rsquo;
     * <li>&lsquo;\.&rsquo; &rarr; &lsquo;.&rsquo;
     * </ul>
     * 
     * @param jdoExpressions the JDO expression collection to be converted
     * 
     * @return the corresponding SQL expression collectioon
     */
    public Collection fromJDO(
        Collection jdoExpressions
    ){
        Collection sqlExpressions = new ArrayList();
        for(
            Iterator i = jdoExpressions.iterator();
            i.hasNext();
        ) sqlExpressions.add(
            fromJDO(i.next().toString())
        );
        return sqlExpressions;
    }

    /**
     * Convert an SQL wildcard expressions to a JDO wildcard expression
     * <p>
     * Conversion rules:<ul>
     * <li>&lsquo;%&rsquo; &rarr; &lsquo;.*&rsquo;
     * <li>&lsquo;_&rsquo; &rarr; &lsquo;.&rsquo;
     * <li>escape + &lsquo;%&rsquo; &rarr; &lsquo;%&rsquo;
     * <li>escape + &lsquo;_&rsquo; &rarr; &lsquo;_&rsquo; 
     * <li>&lsquo;*&rsquo; &rarr; &lsquo;\*&rsquo;
     * <li>&lsquo;.&rsquo; &rarr; &lsquo;\.&rsquo; 
     * </ul>
     * 
     * @param sqlExpression the JDO expression to be converted
     * 
     * @return the corresponding SQL expression
     */
    public String toJDO(
        String sqlExpression
    ){
        StringBuilder jdoExpression = new StringBuilder();
        for(
            int i = 0, iLimit = sqlExpression.length();
            i < iLimit;
            i++
        ){
            char c = sqlExpression.charAt(i);
            if(c == this.escape) {
                if(i++ < iLimit) jdoExpression.append(
                    sqlExpression.charAt(i)
                );
            } else switch(c){
                case '%':
                    jdoExpression.append(
                        ".*"
                    );
                    break;
                case '_':
                    jdoExpression.append(
                        '.'
                    );
                    break;
                case '.': // case '*':
                    jdoExpression.append(
                        '\\'
                    ).append(
                        c
                    );
                    break;
                default:
                    jdoExpression.append(
                        c
                    );
            }
        }
        return jdoExpression.toString();
    }
    
    /**
     * Convert a collection of SQL wildcard expressions to a collection of JDO 
     * wildcard expressions
     * <p>
     * Conversion rules:<ul>
     * <li>&lsquo;%&rsquo; &rarr; &lsquo;.*&rsquo;
     * <li>&lsquo;_&rsquo; &rarr; &lsquo;.&rsquo;
     * <li>escape + &lsquo;%&rsquo; &rarr; &lsquo;%&rsquo;
     * <li>escape + &lsquo;_&rsquo; &rarr; &lsquo;_&rsquo; 
     * <li>&lsquo;*&rsquo; &rarr; &lsquo;\*&rsquo;
     * <li>&lsquo;.&rsquo; &rarr; &lsquo;\.&rsquo; 
     * </ul>
     * 
     * @param sqlExpressions the SQL expression collection to be converted
     * 
     * @return the corresponding JDO expression collectioon
     */
    public Collection toJDO(
        Collection sqlExpressions
    ){
        Collection jdoExpressions = new ArrayList();
        for(
            Iterator i = sqlExpressions.iterator();
            i.hasNext();
        ) jdoExpressions.add(
            fromJDO((String)i.next())
        );
        return jdoExpressions;
    }
}
