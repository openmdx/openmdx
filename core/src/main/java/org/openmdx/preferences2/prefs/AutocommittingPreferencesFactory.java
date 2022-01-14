/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Autocommitting Preferences Factory 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
package org.openmdx.preferences2.prefs;

import java.util.prefs.Preferences;

import javax.jdo.PersistenceManager;

import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.persistence.cci.UnitOfWork;
import org.openmdx.preferences2.jmi1.Node;
import org.openmdx.preferences2.jmi1.Root;
import org.openmdx.preferences2.jmi1.Segment;


/**
 * Autocommitting Preferences Factory
 */
public class AutocommittingPreferencesFactory extends ManagedPreferencesFactory {

    /**
     * Constructor 
     *
     * @param jmiEntityManager
     * @param providerXRI
     * @param segmentName
     * @param systemRootName
     * @param userRootName
     */
    public AutocommittingPreferencesFactory(
        PersistenceManager jmiEntityManager,
        String providerXRI,
        String segmentName,
        String systemRootName,
        String userRootName
    ){
        super(
            jmiEntityManager,
            providerXRI,
            segmentName,
            systemRootName,
            userRootName
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.preferences2.prefs.StandardPreferencesFactory#newRootNode(org.openmdx.preferences2.jmi1.Segment, org.openmdx.preferences2.jmi1.Preferences, java.lang.String, java.lang.String)
     */
    @Override
    protected Node newRootNode(
        Segment segment,
        Root preferences,
        String type,
        String name
    ) {
    	UnitOfWork unitOfWork = PersistenceHelper.currentUnitOfWork(jmiEntityManager);
    	unitOfWork.begin();
        Node rootNode = super.newRootNode(segment, preferences, type, name);
        unitOfWork.commit();
        return rootNode;
    }

    /* (non-Javadoc);
     * @see java.util.prefs.PreferencesFactory#systemRoot()
     */    
    @Override
    public Preferences systemRoot(
    ){
        return new AutocommittingPreferences(
            getRootNode("system", this.systemPreferencesName)
        );
    }

    /* (non-Javadoc)
     * @see java.util.prefs.PreferencesFactory#userRoot()
     */
    @Override
    public Preferences userRoot(
    ){
        return new AutocommittingPreferences(
            getRootNode("user", this.userPreferencesName)
        );
    }

}
