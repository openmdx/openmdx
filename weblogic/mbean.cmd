@rem 
@rem ====================================================================
@rem Project:     openmdx, http://www.openmdx.org/
@rem Name:        $Id: mbean.cmd,v 1.2 2006/08/20 21:57:01 hburger Exp $
@rem Description: Demo Identity Constants 
@rem Revision:    $Revision: 1.2 $
@rem Owner:       OMEX AG, Switzerland, http://www.omex.ch
@rem Date:        $Date: 2006/08/20 21:57:01 $
@rem ====================================================================
@rem
@rem This software is published under the BSD license as listed below.
@rem 
@rem Copyright (c) 2006, OMEX AG, Switzerland
@rem All rights reserved.
@rem 
@rem Redistribution and use in source and binary forms, with or
@rem without modification, are permitted provided that the following
@rem conditions are met:
@rem 
@rem * Redistributions of source code must retain the above copyright
@rem   notice, this list of conditions and the following disclaimer.
@rem 
@rem * Redistributions in binary form must reproduce the above copyright
@rem   notice, this list of conditions and the following disclaimer in
@rem   the documentation and/or other materials provided with the
@rem   distribution.
@rem 
@rem * Neither the name of the openMDX team nor the names of its
@rem   contributors may be used to endorse or promote products derived
@rem   from this software without specific prior written permission.
@rem 
@rem THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
@rem CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
@rem INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
@rem MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
@rem DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
@rem BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
@rem EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
@rem TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
@rem DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
@rem ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
@rem OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
@rem OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
@rem POSSIBILITY OF SUCH DAMAGE.
@rem 
@rem ------------------
@rem 
@rem This product includes software developed by other organizations as
@rem listed in the NOTICE file.
@rem
@echo off
@echo TESTING 
@rem Check that the WebLogic classes are where we expect them to be
@if exist "%WL_HOME%\server\bin\setWLSEnv.cmd" goto setEnv
@echo.
@echo The WebLogic environment set-up command file setWLSEnv.cmd wasn't 
@echo found in the %WL_HOME%\server\bin directory.
exit 1

:setEnv
call %WL_HOME%\server\bin\setWLSEnv.cmd

ant -file mbean.xml -Dbea.weblogic.home=%WL_HOME% -Dbuild.target.platform=%BUILD_TARGET_PLATFORM% -Dmdf.name=%1
