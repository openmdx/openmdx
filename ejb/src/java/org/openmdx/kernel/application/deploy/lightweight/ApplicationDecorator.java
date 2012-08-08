/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ApplicationDecorator.java,v 1.1 2009/01/12 12:49:23 wfro Exp $
 * Description: Application Decorator
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/12 12:49:23 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.kernel.application.deploy.lightweight;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openmdx.kernel.application.configuration.Report;
import org.openmdx.kernel.application.deploy.spi.Deployment.WebApplication;
import org.openmdx.kernel.application.deploy.spi.Deployment.Application;
import org.openmdx.kernel.application.deploy.spi.Deployment.ApplicationClient;
import org.openmdx.kernel.application.deploy.spi.Deployment.Connector;
import org.openmdx.kernel.application.deploy.spi.Deployment.Module;

public class ApplicationDecorator implements Application {

    public ApplicationDecorator(
        Application delegate
    ) {
        this.delegate = delegate;
    }

    public String getDisplayName() {
        return this.delegate.getDisplayName();
    }

    public List<Module> getModules() {
        List<Module> modules = new ArrayList<Module>();
        for(
                Iterator<Module> it = this.delegate.getModules().iterator();
                it.hasNext();
        ) {
            Module module = it.next();
            if (module instanceof Connector)
            {
                modules.add(
                    new ConnectorDecorator((Connector)module)
                );
            }
            else if (module instanceof ApplicationClient)
            {
                modules.add(
                    new ApplicationClientDecorator(
                        (ApplicationClient)module
                    )
                );
            }
            else if (module instanceof WebApplication) 
            {
                modules.add(module);
            } else
            {
                modules.add(
                    new ModuleDecorator<Module>(module)
                );
            }
        }
        return modules;
    }

    public Report validate() {
        return this.delegate.validate();
    }

    public Report verify() {
        return this.delegate.verify();
    }

    /**
     * @return
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.Application#isExpanded()
     */
    public boolean isExpanded() {
        return this.delegate.isExpanded();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.Application#getLibraryDirectory()
     */
    public String getLibraryDirectory() {
        return this.delegate.getLibraryDirectory();
    }

    /**
     * @return
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.Application#getApplicationClassPath()
     */
    public URL[] getApplicationClassPath() {
        return this.delegate.getApplicationClassPath();
    }

    private final Application delegate;

}
