/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractRadiusClient.java,v 1.1 2009/03/31 17:32:00 hburger Exp $
 * Description: Abstract Java Radius Client
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/31 17:32:00 $
 * ====================================================================
 *
 * Copyright (C) 2007  OMEX AG
 *
 * * This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 * * This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Lesser General Public License for more details.
 *
 * * You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * 
 * This library BASED on Java Radius Client 2.0.0
 * (http://http://jradius-client.sourceforge.net/),
 * but it's namespace and content has been MODIFIED by OMEX AG
 * in order to integrate it into the openMDX framework.
 */
package org.openmdx.uses.net.sourceforge.jradiusclient;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openmdx.kernel.log.LoggerFactory;

/**
 * Abstract Radius CLient
 */
public class AbstractRadiusClient {

        /**
     * Constructor
     * 
     * @param logging
     */
    protected AbstractRadiusClient(
        boolean logging
    ){
        this.logger = logging ? LoggerFactory.getLogger() : null;
    }

    /**
     * 
     */
    private final Logger logger;

    /**
     * Log at warn level
     * 
     * @param format
     * @param id
     * @param argument1
     * @param argument2
     */
    protected final void logWarning(
        String format,
        byte id,
        Object argument1,
        Object argument2
    ){
        if(this.logger != null) this.logger.log(
        	Level.WARNING,
            format,
            new Object[]{Byte.valueOf(id),argument1,argument2}
		);
    }

    /**
     * Log at debug  level
     * 
     * @param format
     * @param id
     * @param argument1
     */
    protected final void logDebug(
        String format,
        int id,
        Object argument1
    ){
        if(this.logger != null) this.logger.log(
        	Level.FINER,
            format,
            new Object[]{Integer.valueOf(id),argument1}
        );
    }

    /**
     * Log at debug  level
     * 
     * @param format
     * @param id
     * @param argument1
     */
    protected final void logDebug(
        String format,
        Object argument1
    ){
            if(this.logger != null) this.logger.log(
            	Level.FINER,
                format,
                argument1
        );
    }

}
