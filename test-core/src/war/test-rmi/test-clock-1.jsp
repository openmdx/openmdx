<!--
=======================================================================
= Name:        $Id: test-clock-1.jsp,v 1.2 2004/07/26 23:32:16 hburger Exp $
= Description: openMDX/Core Build File
= Revision:    $Revision: 1.2 $
= Date:        $Date: 2004/07/26 23:32:16 $
= Copyright:   (c) 2003-2004 OMEX AG
=======================================================================
=
= This software is published under the BSD license
= as listed below.
= 
= Copyright (c) 2004, OMEX AG, Switzerland
= All rights reserved.
= 
= Redistribution and use in source and binary forms, with or without 
= modification, are permitted provided that the following conditions 
= are met:
= 
= * Redistributions of source code must retain the above copyright
=   notice, this list of conditions and the following disclaimer.
= 
= * Redistributions in binary form must reproduce the above copyright
=   notice, this list of conditions and the following disclaimer in
=   the documentation and/or other materials provided with the
=   distribution.
= 
= * Neither the name of the openMDX team nor the names of its
=   contributors may be used to endorse or promote products derived
=   from this software without specific prior written permission.
= 
= THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
= CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
= INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
= MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
= DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
= BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
= EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
= TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
= DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
= ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
= OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
= OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
= POSSIBILITY OF SUCH DAMAGE.
= 
-->

<body bgcolor="white">
<font color="red">
<pre>
<%= org.openmdx.test.clock1.rmi.ClockClient_1.getTime() %>
</pre>