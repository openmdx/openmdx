/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Name Predicate
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
