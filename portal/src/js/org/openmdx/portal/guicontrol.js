// ====================================================================
// Project:     openmdx, http://www.openmdx.org/
// Name:        $Id: guicontrol.js,v 1.7 2008/12/09 14:40:54 wfro Exp $
// Description: java script helpers
// Revision:    $Revision: 1.7 $
// Owner:       OMEX AG, Switzerland, http://www.omex.ch
// Date:        $Date: 2008/12/09 14:40:54 $
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

//---------------------------------------------------------------------------
var selectedgroupTab = null;
var selectedFilterTab = null;

var shownPopup = null;
var POPUP_FIELD = null;
var POPUP_OPTIONS = null;
var dragObj = new Object();
dragObj.zIndex = 0;
var dragIfr = new Object();
var panelsFilter = new Array('panelFilter0');
var pageHasCharts = false;
var browser = new Object();
    browser.agt = navigator.userAgent.toLowerCase();
    browser.is_ie    = ((browser.agt.indexOf("msie") != -1) && (browser.agt.indexOf("opera") == -1));
    browser.is_opera = (browser.agt.indexOf("opera") != -1);
    browser.is_mac   = (browser.agt.indexOf("mac") != -1);
    browser.is_mac_ie= (browser.is_ie && browser.is_mac);
    browser.is_win_ie= (browser.is_ie && !browser.is_mac);
    browser.is_gecko = (navigator.product == "Gecko");
    //alert('agent='+browser.agt+'\r\n'+'ie='+browser.is_ie+'\r\n'+'opera='+browser.is_opera+'\r\n'+'mac='+browser.is_mac+'\r\n'+'mac_ie='+browser.is_mac_ie+'\r\n'+'win_ie='+browser.is_win_ie+'\r\n'+'gecko='+browser.is_gecko+'\r\n');

//---------------------------------------------------------------------------
//emulation of .innerText for Mozilla
if(browser.is_gecko) {
  HTMLElement.prototype.__defineGetter__("innerText", function () {
    var r = this.ownerDocument.createRange();
    r.selectNodeContents(this);
    return r.toString();
  });
}

//---------------------------------------------------------------------------
function getElement(id) {
  var ele = null;
  if (document.getElementById) ele = document.getElementById(id);
  else if (document.all) ele = document.all[id];
  else if (document.layers) ele = _getLayer(window, id);
  return ele;
}

//---------------------------------------------------------------------------
function setValue (objectId, newValue) {
  var el = getElement(objectId);
  if (el) {
    if (el.value) {
	    el.value = newValue;
	  }
  }
}

//---------------------------------------------------------------------------
function getEncodedHRef(components) {
  var href = encodeURI(components[0]);
  for(i = 1; i < components.length; i+=2) {
    if(i == 1) {
        href += "?";
    }
    else {
        href += "&";
    }
    href += components[i] + "=" + encodeURIComponent(components[i+1]);
  }
  return href;
}

//---------------------------------------------------------------------------
Array.prototype.hasMember=function(testItem){
  var i=this.length;
  while(i-->0)if(testItem==this[i])return 1;
  return 0
};

function uniqueInt(){
  var num,maxNum=100000;
  if(!uniqueInt.a||maxNum<=uniqueInt.a.length)uniqueInt.a=[];
  do num=Math.ceil(Math.random()*maxNum);
  while(uniqueInt.a.hasMember(num))
  uniqueInt.a[uniqueInt.a.length]=num;
  return num
}

function checkTextareaLimits(el,maxLines,maxChar){
  if(!el.x){
    el.x=uniqueInt();
    el.onblur=function(){clearInterval(window['int'+el.x])}
  }
  window['int'+el.x]=setInterval(function(){
    var lines=el.value.replace(/\r/g,'').split('\n'), i=lines.length, lines_removed, char_removed;
    if(maxLines&&i>maxLines){
      //alert('You can not enter\nmore than '+maxLines+' lines');
      //el.style.backgroundColor = bgColorError;
      lines=lines.slice(0,maxLines);
      lines_removed=1;
    }
    if(maxChar){
      i=lines.length;
      while(i-->0)if(lines[i].length>maxChar){
        lines[i]=lines[i].slice(0,maxChar);
        char_removed=1
      }
      //if(char_removed)alert('You can not enter more\nthan '+maxChar+' characters per line')
      //if(char_removed) {el.style.backgroundColor = bgColorError;}
    }
    if(char_removed||lines_removed)el.value=lines.join('\n')
  },50);
}

//---------------------------------------------------------------------------
function showPanel(panels, tabClass, tab, name) {
  if (tabClass=='tab') {
      <!-- Object Tab -->
      if (selectedObjTab) {
        selectedObjTab.className = tabClass;
      }
      selectedObjTab = tab
      tab.className = 'S' + tab.className;
  } else {
      if (tabClass=='opTab') {
        /*
         <!-- Operations Tab -->
        if (selectedOpTab) {
            selectedOpTab.className = tabClass;
        }
        selectedOpTab = tab
        if (tab) {tab.className = 'S' + tab.className;}
        */
      } else {
        <!-- Filter Tab -->
        if (selectedFilterTab) {
            selectedFilterTab.className = tabClass;
        }
        selectedFilterTab = tab
      }
  }
  for(i = 0; i < panels.length; i++) {
    if(document.getElementById(panels[i])) {
      if((name == panels[i]) || (name == '')) {
        var eltStyle = document.getElementById(panels[i]).style;
        //document.getElementById(panels[i]).style.display = 'block';
        //document.getElementById(panels[i]).style.width = '100%';
        eltStyle.position = 'relative';
        eltStyle.display = 'block';
        eltStyle.visibility = 'visible';
        eltStyle.height = 'auto';
      }
      else {
        //document.getElementById(panels[i]).style.width = '99%';
        document.getElementById(panels[i]).style.display =  'none';
      }
    }
  }
  if (!browser.is_ie) {
    // refresh for all browsers except IE
    if (pageHasCharts) {window.onresize();};
  };
  return false;
}

//---------------------------------------------------------------------------
function selectGroupTab(grouptab, tabClass) {
  /*
  if (selectedgroupTab) {
  	selectedgroupTab.className = tabClass;
  }
  selectedgroupTab = grouptab
  */
  grouptab.className = 'S' + grouptab.className;
  // find out whether selectedgroupTab is contained in collapsed tab range
  // and if so then expand this particular tab range
  if (grouptab.parentNode) {
    if (grouptab.parentNode.tagName == "SPAN") {
      var el = getElement(grouptab.parentNode.id+'Tab');
      toggleTab(el, grouptab.parentNode.id);
    }
  }
  showPanel(panelsOp, 'opTab', null, '1');
  return false;
}

//---------------------------------------------------------------------------
function toggleTab(tab, name) {
  var el = getElement(name);
  if (el.style.display == 'none') {
    // expand/fly-out
    el.style.display = 'inline';
    tab.firstChild.data = '<';
  } else {
    // collapse/fly-in
    el.style.display = 'none';
    tab.firstChild.data = '>';
  }
  return false;
}
//---------------------------------------------------------------------------
function toggleGridBlock(tab, name, className) {
  var el = getElement(name);
  if (el.style.display == 'none') {
    // expand/fly-out
    el.style.display = 'block';
    tab.className = 'S' + className + 'hover';
  } else {
    // collapse/fly-in
    el.style.display = 'none';
    tab.className = className + 'hover';
  }
  return false;
}

//---------------------------------------------------------------------------
function RTrim(str) {
  var whitespace = new String(" \t\n\r");
  var s = new String(str);
  if (whitespace.indexOf(s.charAt(s.length-1)) != -1) {
    var i = s.length - 1;
    while (i >= 0 && whitespace.indexOf(s.charAt(i)) != -1)
       i--;
    s = s.substring(0, i+1);
  }
  return s;
}

//---------------------------------------------------------------------------
function getRealLeft(id) {
  var el = getElement(id);
  if (el) {
    xPos = el.offsetLeft;
    tempEl = el.offsetParent;
    while(tempEl != null) {
      xPos += tempEl.offsetLeft;
      if (tempEl != null) {tempEl = tempEl.offsetParent;}; // IE6 Javascript bug???
    }
    return xPos;
  }
}

//---------------------------------------------------------------------------
function getRealTop(id) {
  var el = getElement(id);
  if (el) {
    yPos = el.offsetTop;
    tempEl = el.offsetParent;
    while (tempEl != null) {
      yPos += tempEl.offsetTop;
      tempEl = tempEl.offsetParent;
    }
    return yPos;
  }
}

//---------------------------------------------------------------------------
function getRealRight(id) {
  return getRealLeft(id) + getElement(id).offsetWidth;
}

//---------------------------------------------------------------------------
function getRealBottom(id) {
  return getRealTop(id) + getElement(id).offsetHeight;
}


//---------------------------------------------------------------------------
function cloneObject(what) {
    for (i in what) {
        this[i] = what[i];
    }
}

//---------------------------------------------------------------------------

function CloneImg(imgNode) {
	var im = document.createElement("img")
	im.src = imgNode.src;
	im.id = 'drag_' + imgNode.id;
	im.style.position = 'absolute';
	im.style.left = '0px';
	//im.style.left = getRealLeft(imgNode.id) - getRealLeft(im.id) + 'px'; // this would be correct but Opera does not like it
	im.style.top = '0px';
	imgNode.parentNode.appendChild(im);
	im.style.position = 'relative';
	return im.id
}

//---------------------------------------------------------------------------
function dragPopupStart(event, id) {

  var el;
  var x, y;

  // If an element id was given, find it. Otherwise use the element being
  // clicked on.
  if (id) {
    dragObj.elNode = document.getElementById(id);
  }
  else {
    if (browser.is_gecko) { /* Mozilla */
      dragObj.elNode = event.target;
    }
    else {
      dragObj.elNode = window.event.srcElement;
    }
    // If this is a text node, use its parent element.
    if (dragObj.elNode.nodeType == 3) {
      dragObj.elNode = dragObj.elNode.parentNode;
    }
  }
  dragIfr.elNode = getElement('DivShim');

  // Get cursor position with respect to the page.
  if (browser.is_gecko) {
    /* Mozilla */
    x = event.clientX + window.scrollX;
    y = event.clientY + window.scrollY;
  }
  else {
    x = window.event.clientX + document.documentElement.scrollLeft + document.body.scrollLeft;
    y = window.event.clientY + document.documentElement.scrollTop + document.body.scrollTop;
  }

  // Save starting positions of cursor and element.
  dragObj.cursorStartX = x;
  dragObj.cursorStartY = y;
  dragObj.elStartLeft = parseInt(dragObj.elNode.style.left, 10);
  dragObj.elStartTop = parseInt(dragObj.elNode.style.top,  10);
  if (isNaN(dragObj.elStartLeft)) dragObj.elStartLeft = 0;
  if (isNaN(dragObj.elStartTop)) dragObj.elStartTop  = 0;

  dragIfr.cursorStartX = x;
  dragIfr.cursorStartY = y;
  dragIfr.elStartLeft = parseInt(dragIfr.elNode.style.left, 10);
  dragIfr.elStartTop = parseInt(dragIfr.elNode.style.top,  10);
  if (isNaN(dragIfr.elStartLeft)) dragIfr.elStartLeft = 0;
  if (isNaN(dragIfr.elStartTop))  dragIfr.elStartTop  = 0;

  // Update element's z-index.
  dragObj.elNode.style.zIndex = 600;
  dragIfr.elNode.style.zIndex = dragObj.elNode.style.zIndex-1;

  // Capture mousemove and mouseup events on the page.
  if (browser.is_gecko) {
    /* Mozilla */
    document.addEventListener("mousemove", dragPopupGo,   true);
    document.addEventListener("mouseup",   dragPopupStop, true);
    //event.preventDefault();
  }
  else {
    document.attachEvent("onmousemove", dragPopupGo);
    document.attachEvent("onmouseup", dragPopupStop);
    window.event.cancelBubble = true;
    window.event.returnValue = false;
  }
}

//---------------------------------------------------------------------------
function dragAndDropStart(event, id, location, dropTarget) {

  var x, y;

  // store location
  dragObj.location = location;
  // get dropTarget
  dragObj.targetNode = document.getElementById(dropTarget);

  // If an element id was given, find it. Otherwise use the element being clicked on.

  if (id)
    dragObj.srcNode = document.getElementById(id);
  else {
    if (document.all) /* not Mozilla */
      dragObj.srcNode = window.event.srcElement;
    else
      dragObj.srcNode = event.target;

    // If this is a text node, use its parent element.

    if (dragObj.srcNode.nodeType == 3)
      dragObj.srcNode = dragObj.srcNode.parentNode;
  }

  // hide drag source
  dragObj.srcNode.style.visibility = 'hidden';

  // clone image before activating drag operation
  dragObj.dragId = CloneImg(dragObj.srcNode);
  dragObj.elNode = document.getElementById(dragObj.dragId);

  // Get cursor position with respect to the page.

  if (document.all) {
    /* not Mozilla */
    x = window.event.clientX + document.documentElement.scrollLeft
      + document.body.scrollLeft;
    y = window.event.clientY + document.documentElement.scrollTop
      + document.body.scrollTop;
  } else {
    x = event.clientX + window.scrollX;
    y = event.clientY + window.scrollY;
  }

  // Save starting positions of cursor and element.
  dragObj.cursorStartX = x;
  dragObj.cursorStartY = y;
  dragObj.elStartLeft  = parseInt(dragObj.elNode.style.left, 10);
  dragObj.elStartTop   = parseInt(dragObj.elNode.style.top,  10);
  if (isNaN(dragObj.elStartLeft)) dragObj.elStartLeft = 0;
  if (isNaN(dragObj.elStartTop))  dragObj.elStartTop  = 0;

  // Update element's z-index.

  dragObj.elNode.style.zIndex = 600;

  // Capture mousemove and mouseup events on the page.

  if (document.all) {
    /* not Mozilla */
    document.attachEvent("onmousemove", dragAndDropGo);
    document.attachEvent("onmouseup",   dragAndDropStop);
    window.event.cancelBubble = true;
    window.event.returnValue = false;
  } else {
    document.addEventListener("mousemove", dragAndDropGo,   true);
    document.addEventListener("mouseup",   dragAndDropStop, true);
    event.preventDefault();
  }
}

//---------------------------------------------------------------------------
function dragAndDropGo(event) {

  var x, y;

  // Get cursor position with respect to the page.

 if (document.all) {
   /* not Mozilla */
    x = window.event.clientX + document.documentElement.scrollLeft
      + document.body.scrollLeft;
    y = window.event.clientY + document.documentElement.scrollTop
      + document.body.scrollTop;
  } else {
    x = event.clientX + window.scrollX;
    y = event.clientY + window.scrollY;
  }

  // Move drag element by the same amount the cursor has moved.
  dragObj.elNode.style.left = (dragObj.elStartLeft + x - dragObj.cursorStartX) + "px";
  dragObj.elNode.style.top  = (dragObj.elStartTop  + y - dragObj.cursorStartY) + "px";

  if (document.all) {
    /* not Mozilla */
    window.event.cancelBubble = true;
    window.event.returnValue = false;
  } else
    event.preventDefault();

  // hovering over dragTarget?
  if ((getRealTop(dragObj.targetNode.id) < getRealBottom(dragObj.elNode.id)) &&
      (getRealBottom(dragObj.targetNode.id) > getRealTop(dragObj.elNode.id)) &&
      (getRealLeft(dragObj.targetNode.id) < getRealRight(dragObj.elNode.id)) &&
      (getRealRight(dragObj.targetNode.id) > getRealLeft(dragObj.elNode.id))) {
        /* dragObj.targetNode.style.backgroundColor = dragObj.srcNode.backgroundColor */
        if (dragObj.srcNode.currentStyle)
	      dragObj.targetNode.style.backgroundColor = dragObj.srcNode.currentStyle["backgroundColor"];
        else if (window.getComputedStyle){
          var elstyle = window.getComputedStyle(dragObj.srcNode, "");
          dragObj.targetNode.style.backgroundColor = elstyle.getPropertyValue("background-color");
        }
  }
  else {
    dragObj.targetNode.style.backgroundColor = '';
  }
}

//---------------------------------------------------------------------------
function dragPopupGo(event) {
  var x, y;

  // Get cursor position with respect to the page.
  if (document.all) {
   /* not Mozilla */
    x = window.event.clientX + document.documentElement.scrollLeft + document.body.scrollLeft;
    y = window.event.clientY + document.documentElement.scrollTop + document.body.scrollTop;
  }
  else {
    x = event.clientX + window.scrollX;
    y = event.clientY + window.scrollY;
  }

  // Move drag element by the same amount the cursor has moved.
  dragObj.elNode.style.left = (dragObj.elStartLeft + x - dragObj.cursorStartX) + "px";
  dragObj.elNode.style.top  = (dragObj.elStartTop  + y - dragObj.cursorStartY) + "px";
  // and then reposition iframe
  var IfrRef = getElement('DivShim');
  dragIfr.elNode.style.left = (dragIfr.elStartLeft + x - dragIfr.cursorStartX) + "px";
  dragIfr.elNode.style.top  = (dragIfr.elStartTop  + y - dragIfr.cursorStartY) + "px";

  if (document.all) {
    /* not Mozilla */
    window.event.cancelBubble = true;
    window.event.returnValue = false;
  }
  else  {
    //event.preventDefault();
  }
}

//---------------------------------------------------------------------------
// Stop capturing mousemove and mouseup events.
function dragPopupStop(event) {
  if (document.all) {
    /* not Mozilla */
    document.detachEvent("onmousemove", dragPopupGo);
    document.detachEvent("onmouseup", dragPopupStop);
  }
  else {
    document.removeEventListener("mousemove", dragPopupGo,   true);
    document.removeEventListener("mouseup", dragPopupStop, true);
  }
}

//---------------------------------------------------------------------------
function dragAndDropStop(event) {

  // Stop capturing mousemove and mouseup events.

  if (document.all) {
    /* not Mozilla */
    document.detachEvent("onmousemove", dragAndDropGo);
    document.detachEvent("onmouseup",   dragAndDropStop);
  } else {
    document.removeEventListener("mousemove", dragAndDropGo,   true);
    document.removeEventListener("mouseup",   dragAndDropStop, true);
  }

  if (isNaN(parseInt(dragObj.elNode.style.left)) || isNaN(parseInt(dragObj.elNode.style.top))) {
    // not dragged --> simulate click
    if (dragObj.location.length > 0) parent.location=dragObj.location;
  } else {
    // dragged
    if ((getRealTop(dragObj.targetNode.id) < getRealBottom(dragObj.elNode.id)) &&
      (getRealBottom(dragObj.targetNode.id) > getRealTop(dragObj.elNode.id)) &&
      (getRealLeft(dragObj.targetNode.id) < getRealRight(dragObj.elNode.id)) &&
      (getRealRight(dragObj.targetNode.id) > getRealLeft(dragObj.elNode.id))) {
      // dropped on target, i.e. copy location
      dragObj.targetNode.value = dragObj.location;

    }
    // simulate click if object was not dragged by more than threshold of 5px
    if ((Math.abs(parseInt(dragObj.elNode.style.left)-dragObj.elStartLeft)<5) && (Math.abs(parseInt(dragObj.elNode.style.top)-dragObj.elStartTop)<5)) {
      //simulate click
      if (dragObj.location.length > 0) parent.location=dragObj.location;
    }

		// remove drag node
		dragObj.elNode.parentNode.removeChild(dragObj.elNode);

		// unhide original drag source
		dragObj.srcNode.style.visibility = 'visible';
		// restore target
		dragObj.targetNode.style.backgroundColor = '';
  }
}

//---------------------------------------------------------------------------
function prepareShowPopup(caller, popVar, popId, field, options) {
  POPUP_FIELD = field;
  POPUP_OPTIONS = options;
  var pos = 0;
  if(shownPopup) {
    shownPopup.hide();
  }
  shownPopup = popVar;
  $(popId).style.display='block';
  if(!popVar){
    popVar = new YAHOO.widget.Panel(popId, {zindex:20000, close:true, visible:false, constraintoviewport:true, modal:true});
    popVar.cfg.queueProperty('keylisteners', new YAHOO.util.KeyListener(document, {keys:27}, {fn:popVar.hide, scope:popVar, correctScope:true}));
    popVar.render();
  }
  popVar.show();
  return popVar;
}
//---------------------------------------------------------------------------
function editstrings_showPopup(event, caller, popVar, popId, field, options) {
  popVar = prepareShowPopup(caller, popVar, popId, field, options);
  editstrings_on_load();
  popVar.moveTo(event.clientX+1, event.clientY);
  return popVar;
}

//---------------------------------------------------------------------------
function editbooleans_showPopup(event, caller, popVar, popId, field, options) {
  popVar = prepareShowPopup(caller, popVar, popId, field, options);
  editbooleans_on_load();
  popVar.moveTo(event.clientX+1, event.clientY);
  return popVar;
}

//---------------------------------------------------------------------------
function editdates_showPopup(event, caller, popVar, popId, field, options) {
  popVar = prepareShowPopup(caller, popVar, popId, field, options);
  editdates_on_load();
  popVar.moveTo(event.clientX+1, event.clientY);
  return popVar;
}

//---------------------------------------------------------------------------
function editdatetimes_showPopup(event, caller, popVar, popId, field, options) {
  popVar = prepareShowPopup(caller, popVar, popId, field, options);
  editdatetimes_on_load();
  popVar.moveTo(event.clientX+1, event.clientY);
  return popVar;
}

//---------------------------------------------------------------------------
function editcodes_showPopup(event, caller, popVar, popId, field, options) {
  popVar = prepareShowPopup(caller, popVar, popId, field, options);
  editcodes_on_load();
  popVar.moveTo(event.clientX+1, event.clientY);
  return popVar;
}

//---------------------------------------------------------------------------
function editnumbers_showPopup(event, caller, popVar, popId, field, options) {
  popVar = prepareShowPopup(caller, popVar, popId, field, options);
  editnumbers_on_load();
  popVar.moveTo(event.clientX+1, event.clientY);
  return popVar;
}

//---------------------------------------------------------------------------
function move_topRight_to_bot_center(mover_id, fixer_id) {
  var el = getElement(mover_id);
  if (el) {
    var fix_bot = getRealBottom(fixer_id);
    var fix_lt = getRealLeft(fixer_id);
    el.style.left = (fix_lt + getElement(fixer_id).offsetWidth/2) - (el.offsetWidth/2) - (el.clientWidth/2) + 'px';
    el.style.top = fix_bot + 'px';
  }
}

//---------------------------------------------------------------------------
function move_bottomRight_to_top_center(mover_id, fixer_id) {
  // probably not correct...
  var fixer_posOff = Element.positionedOffset($(fixer_id));
  var el = getElement(mover_id);
  el.style.left = fixer_posOff.left + 'px';
  el.style.top = fixer_posOff.top + 'px';
}

//---------------------------------------------------------------------------
function move_topLeft_to_bot_center(mover_id, fixer_id) {
  var el = getElement(mover_id);
  if (el) {
    var fix_bot = getRealBottom(fixer_id);
    var fix_lt = getRealLeft(fixer_id);
    el.style.left = (fix_lt + getElement(fixer_id).offsetWidth/2) - (el.offsetWidth/2) + (el.clientWidth/2) + 'px';
    el.style.top = fix_bot + 'px';
  }
}

//---------------------------------------------------------------------------
function move_to_rt_center(mover_id, fixer_id) {
  var el = getElement(mover_id);
  if (el) {
    var fix_top = getRealTop(fixer_id);
    var fix_rt = getRealRight(fixer_id);
    el.style.left = fix_rt + 'px';
    el.style.top = (fix_top + getElement(fixer_id).offsetHeight/2) - (el.offsetHeight/2) + 'px';
  }
}

//---------------------------------------------------------------------------
function move_bottomRight_to_bottom_right(mover_id, fixer_id) {
  var el = getElement(mover_id);
  if (el) {
    var fix_top = getRealTop(fixer_id);
    var fix_lt = getRealLeft(fixer_id);
 /* el.style.left = (fix_lt + getElement(fixer_id).offsetWidth/2) - (el.offsetWidth/2) - (el.clientWidth/2) + 'px'; */
    el.style.left = (fix_lt + getElement(fixer_id).offsetWidth) - (el.offsetWidth/2) - (el.clientWidth/2) + 'px';
    el.style.top = (fix_top + getElement(fixer_id).offsetHeight) - (el.clientHeight) + 'px';
  }
}

//---------------------------------------------------------------------------
function showImage(divId, divImageId, sourceDivId, sourceImageId) {
  var pos = 0;
  var divEle = document.getElementById(divId); /* div containing popUp image */
  divEle.style.display = 'block';
  /* place popUp in lower right corner if source image (or container, if image too wide) */
  if (getRealRight(sourceDivId)>getRealRight(sourceImageId))
    move_bottomRight_to_bottom_right(divId, sourceImageId)
  else
    move_bottomRight_to_bottom_right(divId, sourceDivId)

  /* ensure popUp stays within Browser Window */
  if (window.innerWidth) {
    pos = window.pageXOffset
  }
  else if (document.documentElement && document.documentElement.scrollLeft) {
    pos = document.documentElement.scrollLeft
  }
  else if (document.body) {
    pos = document.body.scrollLeft
  }
  // pos contains the position of the left pixel displayed in the browser window
  if (pos > parseInt(divEle.style.left)) divEle.style.left = pos + 'px';
  if (window.innerHeight) {
    pos = window.pageYOffset
  }
  else if (document.documentElement && document.documentElement.scrollTop) {
    pos = document.documentElement.scrollTop
  }
  else if (document.body) {
    pos = document.body.scrollTop
  }
  // pos contains the position of the top line displayed in the browser window
  if (pos > parseInt(divEle.style.top)) divEle.style.top = pos + 'px';
}

//---------------------------------------------------------------------------
function setLanguage(form){
  var URL = form.options[form.selectedIndex].value;
  window.location.href = eval(URL);
}
//---------------------------------------------------------------------------
function setUserRole(form){
  var URL = form.options[form.selectedIndex].value;
  window.location.href = eval(URL);
}
//---------------------------------------------------------------------------
function removeThousandsSeparator(Str) {
  var S = String(3/2)
  Str = Str.replace(/(\.|,|'|\s|&nbsp;)/g, '');
  return Str;
}
//---------------------------------------------------------------------------
function makeZebraTable(tableID, headerRows) {
  var tableEl = document.getElementById(tableID)
  if (tableEl==null) {return;}
  var tableClass=tableEl.className;
  var zebraClass=tableClass+'zebra';
  var hoverClass=tableClass+'hover';
  var selectedClass=tableClass+'selected';
  var tableRows=tableEl.rows;
  var replaceStr='';
  var j=headerRows; /* set row counter to start AFTER header rows [note that IE ignores THEAD in .rows */
  while (j<tableRows.length) {
    if((tableRows[j].className) && (tableRows[j].className!='rowDetails') && (tableRows[j].getElementsByTagName('td').length>0)) {
      //addClass=j%2==0?' '+zebraClass:'';
      //tableRows[j].className=tableRows[j].className+addClass;
      tableRows[j].onclick=function() {
        if(this.className.match(selectedClass)) {
          replaceStr=this.className.match(' '+selectedClass)?' '+selectedClass:selectedClass;
          this.className=this.className.replace(replaceStr,'');
        } else {
          this.className+=this.className?' '+selectedClass:selectedClass;
        }
      }
      tableRows[j].onmouseover=function() {
        this.className=this.className+' '+hoverClass;
        if(!this.className.match(selectedClass)) {
          this.style.borderColorTop = this.style.color; /* temp storage OK as width=0 */
          this.style.borderColorBottom = this.style.backgroundColor; /* temp storage OK as width=0 */
          this.style.color = '';
          this.style.backgroundColor = '';
        }
      }
      tableRows[j].onmouseout=function() {
        replaceStr=this.className.match(' '+hoverClass)?' '+hoverClass:hoverClass;
        this.className=this.className.replace(replaceStr,'');
        this.className=this.className.replace(replaceStr,''); // required to clear double entries
        if(!this.className.match(selectedClass)) {
          this.style.color =  this.style.borderColorTop;
          this.style.backgroundColor = this.style.borderColorBottom;
          this.style.borderColorTop = '';
          this.style.borderColorBottom = '';
        }
      }
    }
	j++;
  }
}

//---------------------------------------------------------------------------

function getSelectedGridRows(tableID, headerRows) {
  /* returns a string with objectIDs of selected rows (e.g. "a093847+b93837+f993838+")      */
  /* or empty string (if no rows are selected); individual objectIDs are terminated by " " */
  var tableEl = document.getElementById(tableID)
  var tableClass=tableEl.className;
  var selectedClass=tableClass+'selected';
  var tableRows=tableEl.rows;
  var selectedStr='';
  var j=headerRows; /* set row counter to start AFTER header rows [note that IE ignores THEAD in .rows */

  while (j<tableRows.length) {
    if(tableRows[j].getElementsByTagName('td').length>0) {
      if(tableRows[j].className.match(selectedClass)) {
        /*get objectID of this row */
        var strArray = tableRows[j].innerHTML.split('<!-- ObjectReferenceValue -->');
        if (strArray[1]) {
          /*grid line contains objectID*/
          var param = strArray[1].split("'parameter', '");
          if (param[1]) {
            /*objectID found*/
            selectedStr=selectedStr+param[1].substring(0, 1+param[1].indexOf(')'))+' ';
          }
        }
      }
    }
    j++;
  }
  return selectedStr;
}

//---------------------------------------------------------------------------

function selectGridRows(tableID, headerRows, select) {
  var tableEl = document.getElementById(tableID)
  var tableClass=tableEl.className;
  var selectedClass=tableClass+'selected';
  var tableRows=tableEl.rows;
  var j=headerRows; /* set row counter to start AFTER header rows [note that IE ignores THEAD in .rows */

  while (j<tableRows.length) {
    try{tableRows[j].onmouseover();}catch(e){};
    if(tableRows[j].className &&
      ((tableRows[j].className.match(selectedClass) && !select) ||
       (!tableRows[j].className.match(selectedClass) && select)
      )
    ) {
      try{tableRows[j].onclick();}catch(e){};
    }
    try{tableRows[j].onmouseout();}catch(e){};
    j++;
  }
}

//---------------------------------------------------------------------------

function insertGridRow(tableID, rowID, tabOffset) {
  /* inserts a (structurally matching) row with ID="rowID" into the table with ID="tableID"   */
  /* tabOffset is used to calculate the tabIndex of the cell and (if required) the name index */
  var tableNode = document.getElementById(tableID);
  var tbodyNode = tableNode.getElementsByTagName("tbody").item(0);
  var newRow = document.getElementById(rowID).cloneNode(true);
  var cells = newRow.getElementsByTagName('td');
  newRow.id = newRow.id + '-' + tabOffset.toString();
  var currCellEl = null;
  var splitName = null;
  var colNum = 0;
  var lastTabIndex = 0;
//  try {
    while (colNum < cells.length) {
      if (cells[colNum].firstChild) {
        curCellEl = cells[colNum].firstChild;
        while (curCellEl) {
          if (curCellEl.tabIndex) {
            // note: Mozilla does not support tabIndex for some objects!
            lastTabIndex = curCellEl.tabIndex + tabOffset;
            curCellEl.tabIndex = lastTabIndex;
          }
//          if (curCellEl.id) {
//            if (curCellEl.id.match('cal_')) {
//              curCellEl.id = curCellEl.id + lastTabIndex;
//              alert(curCellEl.id);
//            }
//          }
          if (curCellEl.name) {
            splitName = curCellEl.name.split(/[\[|\]]/);
            nameIdx = parseInt(splitName[1]);
            curCellEl.name = splitName[0] + '[' + (nameIdx + tabOffset).toString() + ']';
            if (splitName[2]) {
              curCellEl.name = curCellEl.name + splitName[2];
            }
            curCellEl.id = curCellEl.name;
            //alert('inserted: name='+curCellEl.name + ' | ' + 'ID='+curCellEl.id);
          }
          curCellEl = curCellEl.nextSibling;
        }
      }
      colNum++
    }
    tbodyNode.appendChild(newRow)
//  }
//  catch (e) {
//    deleteGridRow(newRow)
//  }
}

//---------------------------------------------------------------------------

function cloneGridRow(tableID, elInRowToClone, tabOffset, rowMultiplier) {
  /* clones the row "rowToClone" and inserts the cloned row into the table with ID="tableID"  */
  /* tabOffset is used to calculate the tabIndex of the cell and (if required) the name index */
  var tableNode = document.getElementById(tableID);
  var tbodyNode = tableNode.getElementsByTagName("tbody").item(0);
  var newRow = null;
  // allow cloning of cloned rows
  var cloneIMGnode = elInRowToClone.nextSibling;
  while (cloneIMGnode.nodeType == 3) {cloneIMGnode = cloneIMGnode.nextSibling;}
  visibleStatus = cloneIMGnode.style.display;
  cloneIMGnode.style.display = 'block';
  var sourceRow = elInRowToClone.parentNode.parentNode;
  while (sourceRow) {
    // located bounding TR node
    if (sourceRow.tagName == "TR") {
      newRow = sourceRow.cloneNode(true);
      sourceRow = null;
    } else {
      sourceRow = sourceRow.parentNode;
    }
  }
  // restore visibility status of clone icon
  cloneIMGnode.style.display = visibleStatus;
  if (newRow != null) {
    var cells = newRow.getElementsByTagName('td');
    newRow.id = newRow.id + '-' + tabOffset.toString();
    var currCellEl = null;
    var splitName = null;
    var colNum = 0;
//  try {
      while (colNum < cells.length) {
        if (cells[colNum].firstChild) {
          curCellEl = cells[colNum].firstChild;
          while (curCellEl) {
            if (curCellEl.tabIndex) {
              // note: Mozilla does not support tabIndex for some objects!
              curCellEl.tabIndex = (curCellEl.tabIndex % rowMultiplier) + tabOffset;
            }
            if (curCellEl.name) {
              if ((curCellEl.name.indexOf("refMofId") >= 0) && (curCellEl.value.indexOf("xri:@") == 0 )) {
                curCellEl.value = 'clonedFrom:' + curCellEl.value
              }
              splitName = curCellEl.name.split(/[\[|\]]/);
              nameIdx = (parseInt(splitName[1]) % rowMultiplier);
              curCellEl.name = splitName[0] + '[' + (nameIdx + tabOffset).toString() + ']';
              if (splitName[2]) {
                curCellEl.name = curCellEl.name + splitName[2];
              }
              curCellEl.id = curCellEl.name;
              //alert('cloned: name='+curCellEl.name + ' | ' + 'ID='+curCellEl.id + ' | ' + 'value='+curCellEl.value);
            }
            curCellEl = curCellEl.nextSibling;
          }
        }
        colNum++
      }
      tbodyNode.appendChild(newRow)
//  }
//  catch (e) {
//    deleteGridRow(newRow)
//  }
  }
}

//---------------------------------------------------------------------------

function deleteGridRow(clickedEl) {
  /* removes the row containing the clicked element from the table */
  while (clickedEl) {
    if (clickedEl.tagName == "TR") {
      clickedEl.parentNode.removeChild(clickedEl);
      clickedEl = null;
    } else {
      clickedEl = clickedEl.parentNode;
    }
  }
}

//---------------------------------------------------------------------------

function updateCollapsableRows (tableID, headerRows, anchorID, currentLevel, collapse) {
  /* anchorID = "Tx-m-n-o-p-..." */
  /* currentLevel designates the current level */
  var tableEl = document.getElementById(tableID)
  var tableRows=tableEl.rows;
  var j=headerRows; /* set row counter to start AFTER header rows [note that IE ignores THEAD in .rows */
  var currentLevelPos = currentLevel+1;
  var levels = anchorID.split('-');
  var currentLevelNum = parseInt(levels[currentLevelPos]);
  var i = currentLevelPos;
  var parentLevels = levels[i];
  while (i>0) {i--; parentLevels = levels[i]+parentLevels;}
  var anchorParentLevels = parentLevels;
  while (j<tableRows.length) {
    if (tableRows[j].id != anchorID) {
      levels = tableRows[j].id.split('-');
      if (parseInt(levels[currentLevelPos])==currentLevelNum) {
        i = currentLevelPos;
        parentLevels = levels[i];
        while (i>0) {i--; parentLevels = levels[i]+parentLevels;}
        if (parentLevels == anchorParentLevels) {
          if (collapse) {
            tableRows[j].style.display = 'none';
            if (tableRows[j].collapseCount) {
              tableRows[j].collapseCount++;
            } else {
              tableRows[j].collapseCount = 1;
            }
          } else {
            if (tableRows[j].collapseCount == 1) {
              tableRows[j].style.display = ''; // do NOT use block to show row because Firefox/Opera do not put it back into the original table!
            }
            tableRows[j].collapseCount--;
          }
        }
      }
    }
    j++;
  }
}

//---------------------------------------------------------------------------

function updateXriField (titleField, selectedItem) {
	if(
	  (titleField.id.indexOf(".Title") != -1) &&
	  (selectedItem.childNodes[1] != null) &&
	  (selectedItem.childNodes[1].firstChild != null) &&
	  (selectedItem.childNodes[1].firstChild.firstChild != null)
	) {
		xri = selectedItem.childNodes[1].firstChild.firstChild.nodeValue;
		xriField = titleField.id.substring(0, titleField.id.indexOf(".Title"));
		document.getElementById(xriField).value = selectedItem.childNodes[1].firstChild.firstChild.nodeValue;
  }
}
//---------------------------------------------------------------------------
function navSelect(liElt) {
  try {
    var liElts = liElt.parentNode.parentNode.getElementsByTagName("LI");
    for (var i=0; i<liElts.length; i++){
      liElts[i].className='';
    }
    liElt.parentNode.className = "selected";
  } catch(e){};
}
//---------------------------------------------------------------------------
function inspTabSelect(aElt, content) {
  try {
    var aElts = aElt.parentNode.getElementsByTagName("A");
    for (var i=0; i<aElts.length; i++){
      aElts[i].className='';
    }
    aElt.className = "selected";
    var inspContent = aElt.parentNode.nextSibling;
    while ((inspContent.nodeType!=1) || (inspContent.id!="inspContent")) {inspContent=inspContent.nextSibling;}

    var divElt = (inspContent.getElementsByTagName("DIV"))[0];
    while (divElt) {
      divElt.className= (content == '' ? 'selected' : 'hidden');
      divElt= divElt.nextSibling;
    }
    $(content).className = 'selected';
    if (pageHasCharts) {window.onresize();};
  } catch(e){};
}
//---------------------------------------------------------------------------
function loadingIndicator(gridContent) {
  //gridContent must be node, not just an ID
  try {
    var prevHeight = '25px';
    // freeze height and display loading indicator
    if (gridContent.offsetHeight) {
      prevHeight = gridContent.offsetHeight-4;
      if (gridContent.style.paddingTop!="") {prevHeight -= parseInt(gridContent.style.paddingTop,10)};
      if (gridContent.style.paddingBottom!="") {prevHeight -= parseInt(gridContent.style.paddingBottom,10)};
    }
    var node = document.createElement("div");
    node.style.height = prevHeight+'px';
    node.className = 'loading';
    var elt = gridContent.firstChild;
    gridContent.insertBefore(node, elt);
    while (elt) {
      nElt = elt.nextSibling;
      gridContent.removeChild(elt);
      elt = nElt;
    }
  } catch(e){};
}
function gTabSelect(aElt, expand) {
  try {
    if (expand) {
      var aElts = aElt.parentNode.getElementsByTagName("A");
      for (var i=0; i<aElts.length; i++){
        if (aElts[i].className=='hidden') {
          aElts[i].className='';
        };
      }
      aElt.style.display = 'none';
    }
    else {
      var gridContent = aElt.parentNode.nextSibling;
      while (gridContent.tagName!="DIV") {gridContent=gridContent.nextSibling;}
      loadingIndicator(gridContent);
      // activate newly selected tab
      var aElts = aElt.parentNode.getElementsByTagName("A");
      for (var i=0; i<aElts.length; i++){
        if (aElts[i].className!='hidden') {
          aElts[i].className='';
        }
      }
      aElt.className = "selected";
    }
  } catch(e){};
}
//---------------------------------------------------------------------------
function yuiPrint() {
  try {
    var disp_setting="toolbar=no,location=yes,directories=no,menubar=yes,";
        disp_setting+="scrollbars=yes,width=650, height=600, left=100, top=25,";
        disp_setting+="resizable=yes";
    var content_value = $('content').innerHTML;
    var docprint=window.open("","",disp_setting);
    docprint.document.open();
    docprint.document.write('<html><head><style>');
    for (var S = 0; S < document.styleSheets.length; S++){
      if (document.styleSheets[S].cssText) {
        for (var S = 0; S < document.styleSheets.length; S++){
          docprint.document.write(document.styleSheets[S].cssText);
        }
      } else {
        if (document.styleSheets[S].cssRules) {
          for (var R = 0; R < document.styleSheets[S].cssRules.length; R++) {
            docprint.document.write(document.styleSheets[S].cssRules[R].cssText);
          }
        } else {
          for (var R = 0; R < document.styleSheets[S].rules.length; R++) {
            docprint.document.write(document.styleSheets[S].rules[R].cssText);
          }
        }
      }
    }
    docprint.document.write('</style>');
    docprint.document.write('<script>function getEncodedHRef(components){var href = encodeURI(components[0]);for(i=1;i<components.length; i+=2){if(i==1){href+="?";}else{href+="&";}href+=components[i]+"="+encodeURIComponent(components[i+1]);}return href;}</script>');
    docprint.document.write('<script>sfinit = function(ULelt){};</script>');
    docprint.document.write('</head><body class="ytheme-gray" onLoad="self.print()">');
    docprint.document.write(content_value);
    docprint.document.write('</body></html>');
    docprint.document.close();
    docprint.focus();
  } catch(e){};
}
//---------------------------------------------------------------------------
var HTMLeditTextAreaId = "";
function loadHTMLedit(textareaID) {
  HTMLeditTextAreaId = textareaID;
  win = window.open('javascript/wymeditor/htmledit.htm', '_blank', "titlebar=no,menubar=no,help=yes,status=yes,scrollbars=yes,resizable=yes,dependent=yes,alwaysRaised=yes", true);
  win.focus();
}
