package org.openmdx.resource.ldap.ldif;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;

class ComposedPredicate implements Predicate<Entry> {

    ComposedPredicate(
        Matcher matcher
    ) throws LdapException {
        this.operation = Operation.fromOperator(matcher.group(1));
        this.predicates = fromOperands(matcher.group(2));
    }

    private final Operation operation;
    private final List<Predicate<Entry>> predicates;

    static final Pattern PATTERN = Pattern.compile("\\(([&!|])(.+)\\)");

    @Override
    public boolean test(
        Entry entry
    ) {
        return operation.test(entry, predicates);
    }

    enum Operation {

        AND {

            @Override
            boolean test(
                Entry entry,
                List<Predicate<Entry>> predicates
            ) {
                boolean result = true;
                for (Predicate<Entry> predicate: predicates) {
                    result &= predicate.test(entry);
                }
                return result;
            }

            @Override
            String operator(
            ) {
                return "&";
            }
        },
        OR {

            @Override
            boolean test(
                Entry entry,
                List<Predicate<Entry>> predicates
            ) {
                boolean result = false;
                for (Predicate<Entry> predicate: predicates) {
                    result |= predicate.test(entry);
                }
                return result;
            }

            @Override
            String operator(
            ) {
                return "|";
            }
        },
        NOT {

            @Override
            boolean test(
                Entry entry,
                List<Predicate<Entry>> predicates
            ) {
                return !predicates.get(0).test(entry);
            }

            @Override
            String operator(
            ) {
                return "!";
            }
            
        };

        abstract String operator();
        
        abstract boolean test(
            Entry entry,
            List<Predicate<Entry>> predicates
        );

        static Operation fromOperator(
            String operator
        ){
            for(Operation operation : Operation.values()) {
                if(operator.equals(operation.operator())) {
                    return operation;
                }
            }
            throw new IllegalArgumentException("Should never occur");
        }

    }
    
    static List<Predicate<Entry>> fromOperands(
        final String operands
    ) throws LdapException{
        final List<Predicate<Entry>> predicates = new ArrayList<>();
        for(int i=0,p=0;i<operands.length();i++) {
            final char c = operands.charAt(i);
            int b = -1;
            if(c == '(') {
                if(p++ == 0) {
                    b = i;
                }
            } else if (c == ')') {
                if(--p == 0) {
                    predicates.add(Predicates.fromFilter(operands.substring(b, i+1)));
                    b = -1; // not really necessary
                }
            }
        }
        return predicates;
    }

}
