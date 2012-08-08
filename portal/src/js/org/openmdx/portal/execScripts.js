YAHOO.widget.Tab.prototype.executeScripts = function(htmlResponse) {
    // from yui-ext
    var hd = document.getElementsByTagName("head")[0];
    var re = /(?:<script.*?>)((\n|\r|.)*?)(?:<\/script>)/img;
    var srcRe = /\ssrc=([\'\"])(.*?)\1/i;
    var match;
    while(match = re.exec(htmlResponse)){
        var srcMatch = match[0].match(srcRe);
        if(srcMatch && srcMatch[2]){
           var s = document.createElement("script");
           s.src = srcMatch[2];
           hd.appendChild(s);
        }else if(match[1] && match[1].length > 0){
           eval(match[1]);
        }
    }
}
