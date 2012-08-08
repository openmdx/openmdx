/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: MonitoredLogEntity.java,v 1.1 2008/03/21 18:21:51 hburger Exp $
 * Description: Log Entity
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:21:51 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
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
 */
package org.openmdx.application.servlet.log.render;


import org.openmdx.compatibility.kernel.log.LogEntity;



public class MonitoredLogEntity
{
	public MonitoredLogEntity(LogEntity  entity, long position)
	{
		this.state    = ENITY_MONITOR_STATE_GOOD;
		this.entity   = entity;
		this.position = position;
	}

	public LogEntity getLogEntity() 
    { 
        return this.entity; 
    }

	public long getPosition() 
    { 
        return this.position; 
    }

	public int getState() 
    { 
        return this.state; 
    }

	public boolean isMarked() 
    { 
        return this.marked; 
    }


	public void  setLogEntity(LogEntity value) 
    { 
        this.entity = value; 
    }

	public void setPosition(long position) 
    { 
        this.position = position; 
    }

	public void setState(int state) 
    { 
        this.state = state; 
    }

	public void setMarked(boolean marked) 
    { 
        this.marked = marked; 
    }



	private LogEntity    entity;
	private long         position;
	private boolean      marked;
	private int          state;	

	public static final int ENITY_MONITOR_STATE_GOOD     = 0;
	public static final int ENITY_MONITOR_STATE_WARNING  = 1;
	public static final int ENITY_MONITOR_STATE_ERROR    = 2;
	public static final int ENITY_MONITOR_STATE_CRITICAL = 3;

}
