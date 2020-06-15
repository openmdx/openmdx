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
