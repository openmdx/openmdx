package org.openmdx.resource.ldap.ldif;

import java.util.function.Predicate;

import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;


public class NamePredicate implements Predicate<Entry> {

    /**
     * Constructor
     * 
     * @param baseDn the base name
     * @param scope the search scope
     */
    NamePredicate(
        Dn baseDn,
        SearchScope scope
    ) {
        this.baseDn = baseDn;
        this.scope = Scope.fromScope(scope);
    }

    /**
     * The base name
     */
    final Dn baseDn;
    
    /**
     * The search scope
     */
    final Scope scope;
    
    @Override
    public boolean test(
        Entry entry
    ) {
        return scope.test(baseDn, entry.getDn());
    }

    
    enum Scope {
        /** 
         * Base scope 
         */
        OBJECT {

            @Override
            SearchScope scope(
            ) {
                return SearchScope.OBJECT;
            }

            @Override
            boolean test(
                Dn baseDn,
                Dn dn
            ) {
                return dn.equals(baseDn);
            }
        },
        
        /** 
         * One Level scope 
         */
        ONELEVEL {

            @Override
            SearchScope scope(
            ) {
                return SearchScope.ONELEVEL;
            }

            @Override
            boolean test(
                Dn baseDn,
                Dn dn
            ) {
                return baseDn.isAncestorOf(dn);
            }
        },
        
        /** 
         * Subtree scope
         */
        SUBTREE {

            @Override
            SearchScope scope(
            ) {
                return SearchScope.SUBTREE;
            }

            @Override
            boolean test(
                Dn baseDn,
                Dn dn
            ) {
                return dn.isDescendantOf(baseDn);
            }
        };
        
        abstract SearchScope scope();
        abstract boolean test(Dn baseDn, Dn dn);
        
        static Scope fromScope(SearchScope searchScope) {
            for(Scope scope : Scope.values()) {
                if(scope.scope() == searchScope) {
                    return scope;
                }
            }
            throw new IllegalArgumentException("Should never occur");
        }
        
    }
    
}
