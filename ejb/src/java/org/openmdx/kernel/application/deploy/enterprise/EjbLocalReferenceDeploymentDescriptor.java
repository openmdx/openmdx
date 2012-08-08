/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: EjbLocalReferenceDeploymentDescriptor.java,v 1.3 2010/06/04 22:44:59 hburger Exp $
 * Description: lab client
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/04 22:44:59 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */

package org.openmdx.kernel.application.deploy.enterprise;
import java.util.Set;

import javax.naming.Context;
import javax.naming.LinkRef;
import javax.naming.NamingException;

import org.openmdx.kernel.application.deploy.spi.Report;
import org.openmdx.kernel.naming.Contexts;
import org.openmdx.kernel.naming.container.openmdx.openmdxURLContextFactory;
import org.w3c.dom.Element;

@SuppressWarnings("unchecked")
public class EjbLocalReferenceDeploymentDescriptor
    extends EjbReferenceDeploymentDescriptor
{
    public EjbLocalReferenceDeploymentDescriptor(
        ModuleDeploymentDescriptor module
    ) {
        super(module);
    }

    public String getLocal(
    ) {
        return this.local;
    }

    public String getLocalHome(
    ) {
        return this.localHome;
    }

    public String getLocalJndiName(
    ) {
        return this.localJndiName;
    }

    @Override
	public void parseXml(
        Element element,
        Report report
    ) {
        super.parseXml(element, report);
        this.localHome = getElementContent(getUniqueChild(element, "local-home", report));
        this.local = getElementContent(getUniqueChild(element, "local", report));
    }

    @Override
	public void parseOpenMdxXml(
        Element element,
        Report report
    ) {
        super.parseOpenMdxXml(element, report);
        this.localJndiName = getElementContent(getOptionalChild(element, "local-jndi-name", report));
    }

    @Override
	public void bindEjbReference(
        Context context,
        Appendable webApplicationContext, 
        Report report
    ) throws NamingException {
        StringBuilder link = new StringBuilder(
            openmdxURLContextFactory.URL_PREFIX
        );
        // distinguish between external and internal links
        if (this.getLink() != null && this.getLink().length() != 0) {
            // internal link
            link.append(
                openmdxURLContextFactory.APPLICATION_CONTEXT
            ).append('/');

            // distinguish between links within the same module and links
            // that reference other modules within the same J2EE application
            int indexOfHash = this.getLink().indexOf('#');
            if (indexOfHash == -1)
            {
                ApplicationDeploymentDescriptor appDD = this.getModule().getOwner();
                Set modules = appDD.getModulesForEjb(this.getLink());
                if(modules == null) {
                    report.addError(
                        "Could not resolve EJB reference '" + this.getLink() + "' in enterprise application"
                    );
                    return;
                }
                // check whether the target bean is defined locally (i.e. in the same module)
                if (modules.contains(this.getModule().getModuleURI()))
                {
                    link.append(
                        this.createUniqueLocalApplicationContextLink(
                            this.getModule().getModuleURI(),
                            this.getLink()
                        )
                    );
                }
                else
                {
                    // bean is not defined in local module
                    // precondition: link is unique (has been checked by verify operation)
                    link.append(
                        this.createUniqueLocalApplicationContextLink(
                            (String)modules.iterator().next(),
                            this.getLink()
                        )
                    );
                }
            }
            else
            {
                // link that references another module within the same J2EE application
                // e.g. MyModule#MyBean
                link.append(
                    this.createUniqueLocalApplicationContextLink(
                        this.getLink().substring(0, indexOfHash),
                        this.getLink().substring(indexOfHash+1)
                    )
                );
            }
        } else if (this.getLocalJndiName() != null && this.getLocalJndiName().length() != 0) {
            // external link
            link.append(
                openmdxURLContextFactory.CONTAINER_CONTEXT
            ).append(
                '/'
            ).append(
                this.getLocalJndiName()
            );
        } else {
            return;
        }
        report.addInfo("Link local reference " + this.getName() + " to " + link);
        if(context != null) {
            Contexts.bind(context, this.getName(), new LinkRef(link.toString()));
        }
        if(webApplicationContext != null) bind(
            webApplicationContext,
            getName(),
            getLocalHome(),
            link
        );
    }

    @Override
	public void verify(
        Report report
    ) {
        super.verify(report);

        if (this.getLocal() == null || this.getLocal().length() == 0)
        {
            report.addError("No value for 'local' defined for local ejb reference " + this.getName());
        }
        if (this.getLocalHome() == null || this.getLocalHome().length() == 0)
        {
            report.addError("No value for 'local-home' defined for local ejb reference " + this.getName());
        }
        if (
                (this.getLink() == null || this.getLink().length() == 0) &&
                (this.getLocalJndiName() == null || this.getLocalJndiName().length() == 0)
        ) {
            report.addError("Either 'ejb-link' or 'local-jndi-name' must be defined for local ejb reference " + this.getName());
        }
        if (this.getLink() != null && this.getLink().length() != 0 && this.getLocalJndiName() != null && this.getLocalJndiName().length() != 0)
        {
            report.addWarning("Local ejb reference " + this.getName() + " has values defined for 'ejb-link' and 'local-jndi-name' => local jndi entry is ignored");
        }
    }

    private String localHome;
    private String local;
    private String localJndiName;
}
