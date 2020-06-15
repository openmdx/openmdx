package org.openmdx.resource.ldap.ldif;

import java.util.function.Predicate;
import java.util.regex.Matcher;

import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidSearchFilterException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;

class Predicates {

    static Predicate<Entry> fromFilter(
        String filter
    ) throws LdapException {
        Matcher matcher = ComposedPredicate.PATTERN.matcher(filter);
        if(matcher.matches()) {
            return new ComposedPredicate(matcher);
        }
        matcher = AttributePredicate.PATTERN.matcher(filter);
        if(matcher.matches()) {
            return new AttributePredicate(matcher);
        }
        throw new LdapInvalidSearchFilterException(
            "Unable to parse " + filter
        );
    }

    static Predicate<Entry> fromName(
        Dn baseDn,
        SearchScope scope
    ){
        return new NamePredicate(baseDn, scope);
    }
    
}
