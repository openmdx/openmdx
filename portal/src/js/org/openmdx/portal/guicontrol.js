// ====================================================================
// Project:     openMDX/Portal, http://www.openmdx.org/
// Description: java script helpers
// Owner:       OMEX AG, Switzerland, http://www.omex.ch
// ====================================================================
//
// This software is published under the BSD license
// as listed below.
//
// Copyright (c) 2004-2014, OMEX AG, Switzerland
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
function ObjectFinder() {
  this.selectAndClose = selectAndClose;
  this.findObject = findObject;
  this.referenceField = new Array();
  this.titleField = new Array();
}

//---------------------------------------------------------------------------
function findObject(href, objectTitle, objectReference, id) {
	this.referenceField[id] = objectReference;
	this.titleField[id] = objectTitle;
	win = window.open(href + '&filtervalues=' + encodeURIComponent(objectTitle.value), "OF", "help=yes,status=yes,scrollbars=yes,resizable=yes,dependent=yes,alwaysRaised=yes", true); 
	win.focus();
}
    	    
//---------------------------------------------------------------------------
function selectAndClose(objectReference, objectTitle, id, win) {
	this.referenceField[id].value = objectReference;
	this.titleField[id].value = objectTitle;
	try {
		this.referenceField[id].onchange();
	} catch(e) {}
	win.close();
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

//---------------------------------------------------------------------------
function uniqueInt(){
  var num,maxNum=100000;
  if(!uniqueInt.a||maxNum<=uniqueInt.a.length)uniqueInt.a=[];
  do num=Math.ceil(Math.random()*maxNum);
  while(uniqueInt.a.hasMember(num))
  uniqueInt.a[uniqueInt.a.length]=num;
  return num
}

//---------------------------------------------------------------------------
function checkTextareaLimits(el,maxLines,maxChar){
  if(!el.x){
    el.x=uniqueInt();
    el.onblur=function(){clearInterval(window['int'+el.x])}
  }
  window['int'+el.x]=setInterval(function(){
    var lines=el.value.replace(/\r/g,'').split('\n'), i=lines.length, lines_removed, char_removed;
    if(maxLines&&i>maxLines){
      lines=lines.slice(0,maxLines);
      lines_removed=1;
    }
    if(maxChar){
      i=lines.length;
      while(i-->0)if(lines[i].length>maxChar){
        lines[i]=lines[i].slice(0,maxChar);
        char_removed=1
      }
    }
    if(char_removed||lines_removed)el.value=lines.join('\n')
  },50);
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
function prepareShowPopup(caller, popVar, popId, field, options) {
  POPUP_FIELD = field;
  POPUP_OPTIONS = options;
  return popVar;
}

//---------------------------------------------------------------------------
function editstrings_showPopup(event, caller, popVar, popId, field, options) {
  popVar = prepareShowPopup(caller, popVar, popId, field, options);
  editstrings_on_load();
  return popVar;
}

//---------------------------------------------------------------------------
function editbooleans_showPopup(event, caller, popVar, popId, field, options) {
  popVar = prepareShowPopup(caller, popVar, popId, field, options);
  editbooleans_on_load();
  return popVar;
}

//---------------------------------------------------------------------------
function editdates_showPopup(event, caller, popVar, popId, field, options) {
  popVar = prepareShowPopup(caller, popVar, popId, field, options);
  editdates_on_load();
  return popVar;
}

//---------------------------------------------------------------------------
function editdatetimes_showPopup(event, caller, popVar, popId, field, options) {
  popVar = prepareShowPopup(caller, popVar, popId, field, options);
  editdatetimes_on_load();
  return popVar;
}

//---------------------------------------------------------------------------
function editcodes_showPopup(event, caller, popVar, popId, field, options) {
  popVar = prepareShowPopup(caller, popVar, popId, field, options);
  editcodes_on_load();
  return popVar;
}

//---------------------------------------------------------------------------
function editnumbers_showPopup(event, caller, popVar, popId, field, options) {
  popVar = prepareShowPopup(caller, popVar, popId, field, options);
  editnumbers_on_load();
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
function getSelectedGridRows(tableID, headerRows) {
  /* returns a string with objectIDs of selected rows (e.g. "a093847+b93837+f993838+")      */
  /* or empty string (if no rows are selected); individual objectIDs are terminated by " " */
  var tableEl = document.getElementById(tableID)
  var selectedClass = 'info';
  var tableRows = tableEl.rows;
  var selectedStr = '';
  var j = headerRows;
  while (j < tableRows.length) {
    if(tableRows[j].getElementsByTagName('td').length>0) {
      if(tableRows[j].className.match(selectedClass)) {
        /*get objectID of this row */
        var strArray = tableRows[j].innerHTML.split('<!-- ObjectReferenceValue -->');
        if (strArray[1]) {
          /*grid line contains objectID*/
          var param = strArray[1].split("'parameter', '");
          if (param[1]) {
            /*objectID found*/
            selectedStr=selectedStr+param[1].substring(0, 1+param[1].indexOf(')*'))+' ';
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
  var tableRows = tableEl.rows;
  var j = headerRows;
  while (j < tableRows.length) {
    try {
    	selectGridRow(tableRows[j]);
    } catch(e) {};
    j++;
  }
}

//---------------------------------------------------------------------------
function selectGridRow(e) {
	if(e.className == '') {
		e.className = 'info';
	} else {
		e.className='';
	};	
}

//---------------------------------------------------------------------------
function updateXriField (titleField, selectedItem) {
	selectedItemHtml = selectedItem.innerHTML;
	if(
		(selectedItemHtml != null) &&
		(selectedItemHtml.indexOf("xri://") >= 0)
	) {
		posStartSpan = selectedItemHtml.indexOf("<span>");
		posStartXri = selectedItemHtml.indexOf("xri://", posStartSpan);
		posEndXri = selectedItemHtml.indexOf("</div>", posStartXri)
		if(posStartSpan > 0 && posStartXri > 0 && posEndXri > 0) {
			xriFieldId = titleField.id.substring(0, titleField.id.indexOf(".Title"));
			document.getElementById(xriFieldId).value = selectedItemHtml.substring(posStartXri, posEndXri);
			titleField.value = selectedItemHtml.substring(0, posStartSpan);
		}
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

//---------------------------------------------------------------------------
function gTabSelect(aElt, expand) {
  try {
    if (expand) {
      var aElts = aElt.parentNode.parentNode.getElementsByTagName("li");
      for(var i = 0; i < aElts.length; i++){
        if (aElts[i].className == 'hidden') {
          aElts[i].className = '';
        }
      }
      aElt.style.display = 'none';
    } else {
      var gridContent = aElt.parentNode.parentNode.nextElementSibling;
      if(gridContent.tagName == 'DIV') {
        loadingIndicator(gridContent);
      }
      // activate newly selected tab
      var aElts = aElt.parentNode.parentNode.getElementsByTagName("li");
      for(var i = 0; i < aElts.length; i++){
        if(aElts[i].className != 'hidden') {
          aElts[i].className='hidden-print';
        }
      }
      aElt.parentNode.className = 'active';
    }
  } catch(e){};
}

//---------------------------------------------------------------------------
var HTMLeditTextAreaId = "";
function loadHTMLedit(textareaID, urlPrefix) {
  HTMLeditTextAreaId = textareaID;
  if(urlPrefix == null) urlPrefix = "";
  win = window.open(urlPrefix + 'js/wymeditor/htmledit.htm', '_blank', "titlebar=no,menubar=no,help=yes,status=yes,scrollbars=yes,resizable=yes,dependent=yes,alwaysRaised=yes", true);
  win.focus();
}

//---------------------------------------------------------------------------
var WIKYeditTextAreaId = "";
function loadWIKYedit(textareaID, urlPrefix) {
  WIKYeditTextAreaId = textareaID;
  if(urlPrefix == null) urlPrefix = "";
  win = window.open(urlPrefix + 'js/wiky/wikyedit.htm', '_blank', "titlebar=no,menubar=no,help=yes,status=yes,scrollbars=yes,resizable=yes,dependent=yes,alwaysRaised=yes", true);
  win.focus();
}

//---------------------------------------------------------------------------
function evalScripts(html) {
	try{
		eval(jQuery(html).filter('script').text());
	} catch(e) {
		console.log(e);
	};
	try {
		eval(jQuery(html).find('script').text());
	} catch(e) {
		console.log(e);
	};
}

//---------------------------------------------------------------------------
function toggleMenu(e) {
	try {
		jQuery(e).siblings().removeClass("open");
		if(jQuery(e).hasClass('open')) {
			jQuery(e).removeClass('open');
		} else{
			jQuery(e).addClass('open');
		}
	} catch(e) {
		console.log(e);
	}
}

//---------------------------------------------------------------------------
function activateTab(e, tabId) {
	try {
		jQuery(e).siblings().removeClass("active");
		jQuery(e).addClass('active');
		tab = jQuery(tabId);
		tab.siblings().removeClass("active");
		tab.addClass("active");
	} catch(e) {
		console.log(e);
	}
}

//---------------------------------------------------------------------------
function activateTabs(e, tabIdPrefix) {
	try {
		jQuery(e).siblings().removeClass("active");
		jQuery(e).addClass('active');
		for(var i = 0; i < 10; i++) {
			try {
				tab = jQuery(tabIdPrefix.concat(i.toString()));
				tab.addClass("active");
			} catch(e) {}
		}
	} catch(e) {
		console.log(e);
	}
}

//---------------------------------------------------------------------------
// script.aculo.us controls.js v1.9.0, Thu Dec 23 16:54:48 -0500 2010

// Copyright (c) 2005-2010 Thomas Fuchs (http://script.aculo.us, http://mir.aculo.us)
//           (c) 2005-2010 Ivan Krstic (http://blogs.law.harvard.edu/ivan)
//           (c) 2005-2010 Jon Tirsen (http://www.tirsen.com)
// Contributors:
//  Richard Livsey
//  Rahul Bhargava
//  Rob Wills
//
// script.aculo.us is freely distributable under the terms of an MIT-style license.
// For details, see the script.aculo.us web site: http://script.aculo.us/

// Autocompleter.Base handles all the autocompletion functionality
// that's independent of the data source for autocompletion. This
// includes drawing the autocompletion menu, observing keyboard
// and mouse events, and similar.
//
// Specific autocompleters need to provide, at the very least,
// a getUpdatedChoices function that will be invoked every time
// the text inside the monitored textbox changes. This method
// should get the text for which to provide autocompletion by
// invoking this.getToken(), NOT by directly accessing
// this.element.value. This is to allow incremental tokenized
// autocompletion. Specific auto-completion logic (AJAX, etc)
// belongs in getUpdatedChoices.
//
// Tokenized incremental autocompletion is enabled automatically
// when an autocompleter is instantiated with the 'tokens' option
// in the options parameter, e.g.:
// new Ajax.Autocompleter('id','upd', '/url/', { tokens: ',' });
// will incrementally autocomplete with a comma as the token.
// Additionally, ',' in the above example can be replaced with
// a token array, e.g. { tokens: [',', '\n'] } which
// enables autocompletion on multiple tokens. This is most
// useful when one of the tokens is \n (a newline), as it
// allows smart autocompletion after linebreaks.

Element.collectTextNodes = function(element) {
  return $A($(element).childNodes).collect( function(node) {
    return (node.nodeType==3 ? node.nodeValue :
      (node.hasChildNodes() ? Element.collectTextNodes(node) : ''));
  }).flatten().join('');
};

Element.collectTextNodesIgnoreClass = function(element, className) {
  return $A($(element).childNodes).collect( function(node) {
    return (node.nodeType==3 ? node.nodeValue :
      ((node.hasChildNodes() && !Element.hasClassName(node,className)) ?
        Element.collectTextNodesIgnoreClass(node, className) : ''));
  }).flatten().join('');
};

var Autocompleter = { };
Autocompleter.Base = Class.create({
  baseInitialize: function(element, update, options) {
    element          = $(element);
    this.element     = element;
    this.update      = $(update);
    this.hasFocus    = false;
    this.changed     = false;
    this.active      = false;
    this.index       = 0;
    this.entryCount  = 0;
    this.oldElementValue = this.element.value;

    if(this.setOptions)
      this.setOptions(options);
    else
      this.options = options || { };

    this.options.paramName    = this.options.paramName || this.element.name;
    this.options.tokens       = this.options.tokens || [];
    this.options.frequency    = this.options.frequency || 0.4;
    this.options.minChars     = this.options.minChars || 1;
    this.options.onShow       = this.options.onShow ||
      function(element, update){
        if(!update.style.position || update.style.position=='absolute') {
          update.style.position = 'absolute';
          Position.clone(element, update, {
            setHeight: false,
            offsetTop: element.offsetHeight
          });
        }
        update.style.display = 'block'; /* Effect.Appear(update,{duration:0.15}); */
      };
    this.options.onHide = this.options.onHide ||
      function(element, update){
    	update.style.display = 'none'; /* new Effect.Fade(update,{duration:0.15}) */ 
    };

    if(typeof(this.options.tokens) == 'string')
      this.options.tokens = new Array(this.options.tokens);
    // Force carriage returns as token delimiters anyway
    if (!this.options.tokens.include('\n'))
      this.options.tokens.push('\n');

    this.observer = null;

    this.element.setAttribute('autocomplete','off');

    Element.hide(this.update);

    Event.observe(this.element, 'blur', this.onBlur.bindAsEventListener(this));
    Event.observe(this.element, 'keyup', this.onKeyPress.bindAsEventListener(this)); // not supported by all browsers
    Event.observe(this.element, 'input', this.onKeyPress.bindAsEventListener(this));
  },

  show: function() {
    if(Element.getStyle(this.update, 'display')=='none') this.options.onShow(this.element, this.update);
    if(!this.iefix &&
      (Prototype.Browser.IE) &&
      (Element.getStyle(this.update, 'position')=='absolute')) {
      new Insertion.After(this.update,
       '<iframe id="' + this.update.id + '_iefix" '+
       'style="display:none;position:absolute;filter:progid:DXImageTransform.Microsoft.Alpha(opacity=0);" ' +
       'src="javascript:false;" frameborder="0" scrolling="no"></iframe>');
      this.iefix = $(this.update.id+'_iefix');
    }
    if(this.iefix) setTimeout(this.fixIEOverlapping.bind(this), 50);
  },

  fixIEOverlapping: function() {
    Position.clone(this.update, this.iefix, {setTop:(!this.update.style.height)});
    this.iefix.style.zIndex = 1;
    this.update.style.zIndex = 2;
    Element.show(this.iefix);
  },

  hide: function() {
    this.stopIndicator();
    if(Element.getStyle(this.update, 'display')!='none') this.options.onHide(this.element, this.update);
    if(this.iefix) Element.hide(this.iefix);
  },

  startIndicator: function() {
    if(this.options.indicator) Element.show(this.options.indicator);
  },

  stopIndicator: function() {
    if(this.options.indicator) Element.hide(this.options.indicator);
  },

  onKeyPress: function(event) {
    if(this.active)
      switch(event.keyCode) {
       case Event.KEY_TAB:
       case Event.KEY_RETURN:
         this.selectEntry();
         Event.stop(event);
       case Event.KEY_ESC:
         this.hide();
         this.active = false;
         Event.stop(event);
         return;
       case Event.KEY_LEFT:
       case Event.KEY_RIGHT:
         return;
       case Event.KEY_UP:
         this.markPrevious();
         this.render();
         Event.stop(event);
         return;
       case Event.KEY_DOWN:
         this.markNext();
         this.render();
         Event.stop(event);
         return;         
    } else if(
        event.keyCode==Event.KEY_TAB || 
        event.keyCode==Event.KEY_RETURN ||
        (Prototype.Browser.WebKit > 0 && event.keyCode == 0)
    ) return;
    this.changed = true;
    this.hasFocus = true;
    if(this.observer) clearTimeout(this.observer);
    this.observer = setTimeout(this.onObserverEvent.bind(this), this.options.frequency*1000);
  },

  activate: function() {
    this.changed = false;
    this.hasFocus = true;
    this.getUpdatedChoices();
  },

  onHover: function(event) {
    var element = Event.findElement(event, 'LI');
    if(this.index != element.autocompleteIndex)
    {
        this.index = element.autocompleteIndex;
        this.render();
    }
    Event.stop(event);
  },

  onClick: function(event) {
    var element = Event.findElement(event, 'LI');
    this.index = element.autocompleteIndex;
    this.selectEntry();
    this.hide();
  },

  onBlur: function(event) {
	if(this.active) {
	    this.selectEntry();
	    setTimeout(this.hide.bind(this), 250);
	    this.hasFocus = false;
	    this.active = false;
	    this.element.blur();
	}
  },

  render: function() {
    if(this.entryCount > 0) {
      for (var i = 0; i < this.entryCount; i++)
        this.index==i ?
          Element.addClassName(this.getEntry(i),"selected") :
          Element.removeClassName(this.getEntry(i),"selected");
      if(this.hasFocus) {
        this.show();
        this.active = true;
      }
    } else {
      this.active = false;
      this.hide();
    }
  },

  markPrevious: function() {
    if(this.index > 0) this.index--;
      else this.index = this.entryCount-1;
    this.getEntry(this.index).scrollIntoView(true);
  },

  markNext: function() {
    if(this.index < this.entryCount-1) this.index++;
      else this.index = 0;
    this.getEntry(this.index).scrollIntoView(false);
  },

  getEntry: function(index) {
    return this.update.firstChild.childNodes[index];
  },

  getCurrentEntry: function() {
    return this.getEntry(this.index);
  },

  selectEntry: function() {
    this.active = false;
    this.updateElement(this.getCurrentEntry());
  },

  updateElement: function(selectedElement) {
    if (this.options.updateElement) {
      this.options.updateElement(selectedElement);
      return;
    }
    var value = '';
    if (this.options.select) {
      var nodes = $(selectedElement).select('.' + this.options.select) || [];
      if(nodes.length>0) value = Element.collectTextNodes(nodes[0], this.options.select);
    } else
      value = Element.collectTextNodesIgnoreClass(selectedElement, 'informal');

    var bounds = this.getTokenBounds();
    if (bounds[0] != -1) {
      var newValue = this.element.value.substr(0, bounds[0]);
      var whitespace = this.element.value.substr(bounds[0]).match(/^\s+/);
      if (whitespace)
        newValue += whitespace[0];
      this.element.value = newValue + value + this.element.value.substr(bounds[1]);
    } else {
      this.element.value = value;
    }
    this.oldElementValue = this.element.value;
    this.element.focus();

    if (this.options.afterUpdateElement)
      this.options.afterUpdateElement(this.element, selectedElement);
  },

  updateChoices: function(choices) {
    if(!this.changed && this.hasFocus) {
      this.update.innerHTML = choices;
      Element.cleanWhitespace(this.update);
      Element.cleanWhitespace(this.update.down());

      if(this.update.firstChild && this.update.down().childNodes) {
        this.entryCount =
          this.update.down().childNodes.length;
        for (var i = 0; i < this.entryCount; i++) {
          var entry = this.getEntry(i);
          entry.autocompleteIndex = i;
          this.addObservers(entry);
        }
      } else {
        this.entryCount = 0;
      }

      this.stopIndicator();
      this.index = 0;

      if(this.entryCount==1 && this.options.autoSelect) {
        this.selectEntry();
        this.hide();
      } else {
        this.render();
      }
    }
  },

  addObservers: function(element) {
    Event.observe(element, "mouseover", this.onHover.bindAsEventListener(this));
    Event.observe(element, "click", this.onClick.bindAsEventListener(this));
  },

  onObserverEvent: function() {
    this.changed = false;
    this.tokenBounds = null;
    if(this.getToken().length>=this.options.minChars) {
      this.getUpdatedChoices();
    } else {
      this.active = false;
      this.hide();
    }
    this.oldElementValue = this.element.value;
  },

  getToken: function() {
    var bounds = this.getTokenBounds();
    return this.element.value.substring(bounds[0], bounds[1]).strip();
  },

  getTokenBounds: function() {
    if (null != this.tokenBounds) return this.tokenBounds;
    var value = this.element.value;
    if (value.strip().empty()) return [-1, 0];
    var diff = arguments.callee.getFirstDifferencePos(value, this.oldElementValue);
    var offset = (diff == this.oldElementValue.length ? 1 : 0);
    var prevTokenPos = -1, nextTokenPos = value.length;
    var tp;
    for (var index = 0, l = this.options.tokens.length; index < l; ++index) {
      tp = value.lastIndexOf(this.options.tokens[index], diff + offset - 1);
      if (tp > prevTokenPos) prevTokenPos = tp;
      tp = value.indexOf(this.options.tokens[index], diff + offset);
      if (-1 != tp && tp < nextTokenPos) nextTokenPos = tp;
    }
    return (this.tokenBounds = [prevTokenPos + 1, nextTokenPos]);
  }
});

//---------------------------------------------------------------------------
Autocompleter.Base.prototype.getTokenBounds.getFirstDifferencePos = function(newS, oldS) {
  var boundary = Math.min(newS.length, oldS.length);
  for (var index = 0; index < boundary; ++index)
    if (newS[index] != oldS[index])
      return index;
  return boundary;
};

//---------------------------------------------------------------------------
Ajax.Autocompleter = Class.create(Autocompleter.Base, {
  initialize: function(element, update, url, options) {
    this.baseInitialize(element, update, options);
    this.options.asynchronous  = true;
    this.options.onComplete    = this.onComplete.bind(this);
    this.options.defaultParams = this.options.parameters || null;
    this.url                   = url;
  },

  getUpdatedChoices: function() {
    this.startIndicator();

    var entry = encodeURIComponent(this.options.paramName) + '=' +
      encodeURIComponent(this.getToken());

    this.options.parameters = this.options.callback ?
      this.options.callback(this.element, entry) : entry;

    if(this.options.defaultParams)
      this.options.parameters += '&' + this.options.defaultParams;

    new Ajax.Request(this.url, this.options);
  },

  onComplete: function(request) {
    this.updateChoices(request.responseText);
  }
});

//---------------------------------------------------------------------------
