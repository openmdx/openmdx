/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: BeanDecorator.java,v 1.7 2008/10/09 22:33:54 hburger Exp $
 * Description: Bean Decorator
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/10/09 22:33:54 $
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

import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Reference;

import org.openmdx.kernel.application.configuration.Report;
import org.openmdx.kernel.application.deploy.spi.Deployment.Bean;
import org.openmdx.kernel.application.deploy.spi.Deployment.ContainerTransaction;

/**
 * Bean Decorator
 */
abstract class BeanDecorator<T extends Bean>
  extends ConfigurationDecorator<T>
  implements Bean
{

    public BeanDecorator(
        T delegate
    ) {
        super(delegate);
    }

    public String getEjbClass() {
        return super.delegate.getEjbClass();
    }

    public String getJndiName() {
        return super.delegate.getJndiName();
    }

    public String getLocalJndiName() {
        return super.delegate.getLocalJndiName();
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.AssemblyDescriptor#getContainerTransaction()
     */
    public List<ContainerTransaction> getContainerTransaction() {
        return super.delegate.getContainerTransaction();
    }

    public void deploy(
        Context containerContext,
        Context applicationContext,
        Reference localReference,
        Reference remoteReference
    ) throws NamingException {
        super.delegate.deploy(
            containerContext,
            applicationContext,
            localReference,
            remoteReference
        );
    }

    public String getName() {
        return super.delegate.getName();
    }

    public void populate(
        Context componentContext
    ) throws NamingException {
        super.delegate.populate(componentContext);
    }

    public Integer getInitialCapacity(
    ) {
        // do validation before accessing this property's value to ensure that
        // either the configured value or the default value is returned
        this.validate();

        return this.initialCapacity;
    }

    public Integer getMaximumCapacity(
    ) {
        // do validation before accessing this property's value to ensure that
        // either the configured value or the default value is returned
        this.validate();

        return this.maximumCapacity;
    }

    public Long getMaximumWait(
    ) {
        // do validation before accessing this property's value to ensure that
        // either the configured value or the default value is returned
        this.validate();

        return this.maximumWait;
    }

    /**
     * @return
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.Pool#getMaximumIdle()
     */
    public Integer getMaximumIdle() {
        return super.delegate.getMaximumIdle();
    }

    /**
     * @return
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.Pool#getMinimumEvictableIdleTime()
     */
    public Long getMinimumEvictableIdleTime() {
        return super.delegate.getMinimumEvictableIdleTime();
    }

    /**
     * @return
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.Pool#getMinimumIdle()
     */
    public Integer getMinimumIdle() {
        return super.delegate.getMinimumIdle();
    }

    /**
     * @return
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.Pool#getNumberOfTestsPerEvictionRun()
     */
    public Integer getNumberOfTestsPerEvictionRun() {
        return super.delegate.getNumberOfTestsPerEvictionRun();
    }

    /**
     * @return
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.Pool#getTestOnBorrow()
     */
    public Boolean getTestOnBorrow() {
        return super.delegate.getTestOnBorrow();
    }

    /**
     * @return
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.Pool#getTestOnReturn()
     */
    public Boolean getTestOnReturn() {
        return super.delegate.getTestOnReturn();
    }

    /**
     * @return
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.Pool#getTestWhileIdle()
     */
    public Boolean getTestWhileIdle() {
        return super.delegate.getTestWhileIdle();
    }

    /**
     * @return
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.Pool#getTimeBetweenEvictionRuns()
     */
    public Long getTimeBetweenEvictionRuns() {
        return super.delegate.getTimeBetweenEvictionRuns();
    }

    public Report validate() {
        // the check whether validation has been done before avoids, that
        // the report is growing and growing
        if (!this.isValidated())
        {
            Report report = super.validate();

            this.maximumCapacity = super.delegate.getMaximumCapacity();
            if (this.maximumCapacity == null)
            {
                // set default value
                this.maximumCapacity = new Integer(MAXIMUM_CAPACITY_DEFAULT);
                report.addInfo("unset attribute 'MaximumCapacity' was overriden with default value " + this.maximumCapacity);
            }

            this.initialCapacity = super.delegate.getInitialCapacity();
            if (this.initialCapacity == null)
            {
                // set default value
                this.initialCapacity = new Integer(INITIAL_CAPACITY_DEFAULT);
                report.addInfo("unset attribute 'InitialCapacity' was overriden with default value " + this.initialCapacity);
            }

            this.maximumWait = super.delegate.getMaximumWait();
            if (this.maximumWait == null)
            {
                // set default value
                this.maximumWait = new Long(MAXIMUM_WAIT_DEFAULT);
                report.addInfo("unset attribute 'MaximumWait' was overriden with default value " + this.maximumWait);
            }

            return report;
        }
        else
        {
            return super.validate();
        }
    }

    private Long maximumWait = null;
    private Integer initialCapacity = null;
    private Integer maximumCapacity = null;
    private static final long MAXIMUM_WAIT_DEFAULT = java.lang.Long.MAX_VALUE;
    private static final int INITIAL_CAPACITY_DEFAULT = 1;
    private static final int MAXIMUM_CAPACITY_DEFAULT = java.lang.Integer.MAX_VALUE;

}
