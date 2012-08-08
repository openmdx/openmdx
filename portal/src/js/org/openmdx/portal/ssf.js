// source:
// - Sons of Suckerfish - http://www.htmldog.com/articles/suckerfish/
<!--//--><![CDATA[//><!--

sfinit = function(ULelt) {
  try {
    if (window.attachEvent) {
      sfEls = ULelt.getElementsByTagName("LI");
      for (var i=0; i<sfEls.length; i++) {
        sfEls[i].onmouseover=function() {
          this.className+=" sfhover";
        }
        sfEls[i].onmouseout=function() {
          this.className=this.className.replace(new RegExp(" sfhover\\b"), "");
          this.className=this.className.replace(new RegExp(" sfhover\\b"), "");
        }
        if (sfEls[i].parentNode.className != null) {
          sfEls[i].onclick=function() {
            this.className=this.className.replace(new RegExp(" sfhover\\b"), "");
            this.className+=" sfhover"; // activate by click (in case not initialized yet)
            sfEls = this.parentNode.getElementsByTagName("LI");
            for (var i=0; i<sfEls.length; i++) {
              sfEls[i].onclick=function(){};
            }
          };
        }
      }
      ULelt.style.left = '';
    }
  } catch (e) {};
  ULelt.onmouseover=function() {}; // remove initialization handler
}

//--><!]]>
