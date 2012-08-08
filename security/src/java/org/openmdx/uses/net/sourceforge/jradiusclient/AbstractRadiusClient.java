/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: AbstractRadiusClient.java,v 1.2 2010/03/11 18:50:58 hburger Exp $
 * Description: Abstract Java Radius Client
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/11 18:50:58 $
 * ====================================================================
 *
 * Copyright (C) 2007-2010  OMEX AG
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

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract Radius CLient
 */
public class AbstractRadiusClient {

    /**
     * Constructor
     * 
     * @param logging
     * @param trace
     */
    protected AbstractRadiusClient(
        Logger logger,
        boolean trace
    ){
        this.logger = logger;
        this.trace = trace;
        synchronized(AbstractRadiusClient.class) {
            this.id = valueOf(AbstractRadiusClient.nextId++);
        }
    }

    /**
     * The instance id
     */
    private final Integer id;

    /**
     * The next instance id
     */
    private static int nextId;

    /**
     * 
     */
    private final Logger logger;

    /**
     * 
     */
    private final boolean trace;

    /**
     * Log at warn level
     * 
     * @param format
     * @param id
     * @param argument2
     * @param argument3
     */
    protected final void logWarning(
        String format,
        byte id,
        Object argument2,
        Object argument3
    ){
        if(this.logger.isLoggable(Level.WARNING)) {
         this.logger.warning(
             MessageFormat.format(
                    format,
                 new Object[]{
                     this.id,
                     valueOf(id),
                     argument2,
                     argument3
                 }
             )
         );
        }
    }

    /**
     * Log at debug level
     * 
     * @param format
     * @param id
     * @param argument2
     */
    protected final void logDebug(
        String format,
        int id,
        Object argument2
    ){
        if(
            this.trace &&
            this.logger.isLoggable(Level.FINE)
        ){
         this.logger.fine(
                MessageFormat.format(
                    format,
                    new Object[]{
                        this.id,
                        valueOf(id),
                        argument2
                    }
                )
            );
        }
    }

    /**
     * Log at debug  level
     * 
     * @param format
     * @param argument1
     */
    protected final void logDebug(
        String format,
        Object argument1
    ){
        if(
         this.trace &&
         this.logger.isLoggable(Level.FINE)
         ){
         this.logger.fine(
                MessageFormat.format(
                    format,
                    new Object[]{
                        this.id,
                        argument1
                    }
                )
            );
        }
    }

    /**
     * Log at info level
     * 
     * @param format
     */
    protected final void logInfo(
        String format
    ){
        if(this.logger.isLoggable(Level.INFO)){
         this.logger.info(
                MessageFormat.format(
                   format,
                   new Object[]{
                       this.id
                   }
                )
            );
        }
    }

    /**
     * Log at warn level
     * 
     * @param message
     */
    protected final void logWarning(
        String format,
        Object argument1
    ){
        if(this.logger.isLoggable(Level.WARNING)){
         this.logger.warning(
                MessageFormat.format(
                   format,
                   new Object[]{
                       this.id,
                       argument1
                   }
                )
            );
        }
    }

    /**
     * Log at severe level
     * 
     * @param message
     */
    protected final void logSevere(
        String format
    ){
        if(this.logger.isLoggable(Level.SEVERE)){
         this.logger.severe(
                MessageFormat.format(
                   format,
                   new Object[]{
                       this.id,
                   }
                )
            );
        }
    }

    /**
     * Convert a number value to a number object
     * 
     * @param value the byte value 
     * 
     * @return the Byte object
     */
    protected final static Byte valueOf(
        byte value
    ){

        return Byte.valueOf(value);



    }

    /**
     * Convert a number value to a number object
     * 
     * @param value the integer value 
     * 
     * @return the Integer object
     */
    protected final static Integer valueOf(
        int value
    ){

        return Integer.valueOf(value);



    }

}
