// ====================================================================
// Project:     openmdx, http://www.openmdx.org/
// Name:        $Id: findobject.js,v 1.3 2006/11/21 13:10:44 wfro Exp $
// Description: JCA: Utility methods for records
// Revision:    $Revision: 1.3 $
// Owner:       OMEX AG, Switzerland, http://www.omex.ch
// Date:        $Date: 2006/11/21 13:10:44 $
// ====================================================================
// 
// This software is published under the BSD license
// as listed below.
// 
// Copyright (c) 2004, OMEX AG, Switzerland
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or
// without modification, are permitted provided that the following
// conditions are met:
// 
// * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
// 
// * Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in
// the documentation and/or other materials provided with the
// distribution.
// 
// * Neither the name of the openMDX team nor the names of its
// contributors may be used to endorse or promote products derived
// from this software without specific prior written permission.
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
// CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
// BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
// TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
// OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.
// 
// ------------------
// 
// This product includes software developed by the Apache Software
// Foundation (http://www.apache.org/).
// 
// This product includes software developed by Mihai Bazon 
// (http://dynarch.com/mishoo/calendar.epl) published with an LGPL 
// license.
//
function ObjectFinder() {
  this.selectAndClose = selectAndClose;
  this.findObject = findObject;
  this.referenceField = new Array();
  this.titleField = new Array();
}

function findObject(href, objectTitle, objectReference, id) {
  this.referenceField[id] = objectReference;
  this.titleField[id] = objectTitle;
  win = window.open(href + '&filtervalues=' + encodeURIComponent(objectTitle.value), "OF", "help=yes,status=yes,scrollbars=yes,resizable=yes,dependent=yes,alwaysRaised=yes", true); 
  win.focus();
}
    
function selectAndClose(objectReference, objectTitle, id, win) {
  this.referenceField[id].value = objectReference;
  this.titleField[id].value = objectTitle;
  win.close();
}