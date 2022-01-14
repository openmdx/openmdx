/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Attribute Predicate
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.resource.ldap.ldif;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.ldap.client.template.exception.LdapRuntimeException;

public class AttributePredicate implements Predicate<Entry> {

    AttributePredicate(
        Matcher matcher
    ) throws LdapException {
        this.attributeName = matcher.group(1);
        this.operation = Operation.fromOperator(matcher.group(2));
        this.attributeValue = matcher.group(3);
    }

    private final Operation operation;
    private final String attributeName;
    private final String attributeValue;
    
    static Pattern PATTERN = Pattern.compile("\\(([A-Za-z0.9]+)([>~<]?=)(.*)\\)");

    /* (non-Javadoc)
     * @see java.util.function.Predicate#test(java.lang.Object)
     */
    @Override
    public boolean test(
        Entry entry
    ) {
        try {
            return operation.test(entry, attributeName, attributeValue);
        } catch (LdapException exception) {
            throw new LdapRuntimeException(exception);
        }
    }
    
    enum Operation {
        LESS_OR_EQUAL {

            @Override
            boolean test(
                Entry entry,
                String attributeName, String attributeValue
            ) throws LdapException {
                final Attribute attribute = entry.get(attributeName);
                final AttributeType attributeType = attribute.getAttributeType();
                if (attributeType == null){
                    for(Value value : attribute) {
                        if(value.getString().compareTo(attributeValue) <= 0) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    final Value expected = new Value(attribute.getAttributeType(), attributeValue);
                    for(Value value : attribute) {
                        if(value.compareTo(expected) <= 0) {
                            return true;
                        }
                    }
                    return false;
                }
            }

            @Override
            String operator(
            ) {
                return "<=";
            }
        },
        EQUAL {

            @Override
            boolean test(
                Entry entry,
                String attributeName, 
                String attributeValue
            ) throws LdapException {
                final boolean attributeExists = entry.containsAttribute(attributeName);
                if(attributeExists) {
                    if("*".equals(attributeValue)) {    
                        return true;
                    } else {
                        final Attribute attribute = entry.get(attributeName);
                        if (attributeValue.indexOf('*') < 0){
                            final AttributeType attributeType = attribute.getAttributeType();
                            if (attributeType == null){
                                for(Value value : attribute) {
                                    if(attributeValue.equals(value.getString())) {
                                        return true;
                                    }
                                }
                                return false;
                            } else {
                                final Value expected = new Value(attributeType, attributeValue);
                                for(Value value : attribute) {
                                    if(value.compareTo(expected) == 0) {
                                        return true;
                                    }
                                }
                                return false;
                            }
                        } else {
                            final Pattern pattern = toPattern(attributeValue);
                            for(Value value : attribute) {
                                if(pattern.matcher(value.getString()).matches()) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }

            @Override
            String operator(
            ) {
                return "=";
            }
        },
        GREATER_OR_EQUAL {

            @Override
            boolean test(
                Entry entry,
                String attributeName, String attributeValue
            ) throws LdapException {
                final Attribute attribute = entry.get(attributeName);
                final AttributeType attributeType = attribute.getAttributeType();
                if (attributeType == null){
                    for(Value value : attribute) {
                        if(value.getString().compareTo(attributeValue) >= 0) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    final Value expected = new Value(attribute.getAttributeType(), attributeValue);
                    for(Value value : attribute) {
                        if(value.compareTo(expected) >= 0) {
                            return true;
                        }
                    }
                    return false;
                }
            }

            @Override
            String operator(
            ) {
                return ">=";
            }
        },
        /**
         * Behaves like {@link #EQUAL} at the moment!
         */
        SIMILAR {

            @Override
            boolean test(
                Entry entry,
                String attributeName, String attributeValue
            ) throws LdapException {
                return EQUAL.test(entry, attributeName, attributeValue);
            }

            @Override
            String operator(
            ) {
                return "~=";
            }
        };
        
        abstract boolean test(
            Entry entry, 
            String attributeName, 
            String attributeValue
        ) throws LdapException ;
        abstract String operator();
        
        static Operation fromOperator(String operator) {
            for(Operation operation : Operation.values()) {
                if(operator.equals(operation.operator())) {
                    return operation;
                }
            }
            throw new IllegalArgumentException("Should never occur");
        }
    }
    
    static Pattern toPattern(String attributeValue) {
        final StringBuilder pattern = new StringBuilder();
        final String[] literals = attributeValue.split("\\*");
        String separator = "";
        for(String literal : literals) {
            pattern.append(separator).append(Pattern.quote(literal));
            separator = ".*";
        }
        if(attributeValue.endsWith("*")) {
            pattern.append(".*");
        }
        return Pattern.compile(pattern.toString());
    }
}
