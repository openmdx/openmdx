sfHover = function() {
	// Find all unordered lists.
	var ieNavs = document.getElementsByTagName('ul');
	for(i=0; i<ieNavs.length; i++) {
		var ul = ieNavs[i];
		// If they have a class of nav add the menu hover.
		if(ul.className == "nav" || ul.className == "navv" || ul.className == "navigation")
			setHover(ul);
	}
}

function setHover(nav) {
	var ieULs = nav.getElementsByTagName('ul');
	if (navigator.appVersion.substr(22,3)!="5.0") {
		// IE script to cover <select> elements with <iframe>s
		for (j=0; j<ieULs.length; j++) {
			var ieMat=document.createElement('iframe');
			if(document.location.protocol == "https:")
			  ieMat.src="blank.html"; // see http://ajaxian.com/archives/ie-frame-bug
				//ieMat.src="//0";
			else
				ieMat.src="javascript:false";
			ieMat.scrolling="no";
			ieMat.frameBorder="0";
			ieMat.style.width=ieULs[j].offsetWidth+"px";
			ieMat.style.height=ieULs[j].offsetHeight+"px";
			ieMat.style.zIndex="-1";
			if (ieULs[j].childNodes[0]) {ieULs[j].insertBefore(ieMat, ieULs[j].childNodes[0]);}
			ieULs[j].style.zIndex="101";
		}
		// IE script to change class on mouseover
		var ieLIs = nav.getElementsByTagName('li');
		for (var i=0; i<ieLIs.length; i++) if (ieLIs[i]) {
			// Does this LI have children? If so add a sfnode class.
			if (ieLIs[i].getElementsByTagName("UL").length>0){
				ieLIs[i].className+=" sfnode";
			}

			// Add a sfhover class to the li.
			ieLIs[i].onmouseover=function() {this.className+=" sfhover";}
			ieLIs[i].onmouseout=function() {this.className=this.className.replace(' sfhover', '');}
		}
	} else {
		// IE 5.0 doesn't support iframes so hide the select statements on hover and show on mouse out.
		// IE script to change class on mouseover
		var ieLIs = document.getElementById('nav').getElementsByTagName('li');
		for (var i=0; i<ieLIs.length; i++) if (ieLIs[i]) {
			ieLIs[i].onmouseover=function() {this.className+=" sfhover";hideSelects();}
			ieLIs[i].onmouseout=function() {this.className=this.className.replace(' sfhover', '');showSelects()}
		}
		ieLIs = document.getElementById('navv').getElementsByTagName('li');
		for (var i=0; i<ieLIs.length; i++) if (ieLIs[i]) {
			ieLIs[i].onmouseover=function() {this.className+=" sfhover";hideSelects();}
			ieLIs[i].onmouseout=function() {this.className=this.className.replace(' sfhover', '');showSelects()}
		}
		ieLIs = document.getElementById('navigation').getElementsByTagName('li');
		for (var i=0; i<ieLIs.length; i++) if (ieLIs[i]) {
			ieLIs[i].onmouseover=function() {this.className+=" sfhover";hideSelects();}
			ieLIs[i].onmouseout=function() {this.className=this.className.replace(' sfhover', '');showSelects()}
		}
	}
}

// If IE 5.0 hide and show the select statements.
function hideSelects(){
	var oSelects=document.getElementsByTagName("select");
	for(var i=0;i<oSelects.length;i++)
		oSelects[i].className+=" hide";
}

function showSelects(){
	var oSelects=document.getElementsByTagName("select");
	for(var i=0;i<oSelects.length;i++)
		oSelects[i].className=oSelects[i].className.replace(" hide","");
}

// Run this only for IE.
if (window.attachEvent) window.attachEvent('onload', sfHover);
// end
