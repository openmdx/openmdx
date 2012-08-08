Ext.Element.prototype.getStyle = function(){
        var propCache = {}, camel, v, cs;
        var camelRe = /(-[a-z])/gi;
        var camelFn = function(m, a){ return a.charAt(1).toUpperCase(); };
        var view = document.defaultView;
        return view && view.getComputedStyle ?
            function(prop){
                var el = this.dom;
                if(v = el.style[prop]){
                    return v;
                }
                if(cs = view.getComputedStyle(el, '')){
                    if(!(camel = propCache[prop])){
                        camel = propCache[prop] = prop.replace(camelRe, camelFn);
                    }
                    return cs[camel];
                }
                return null;
            } :
            function(prop){
                if(!(camel = propCache[prop])){
                    camel = propCache[prop] = prop.replace(camelRe, camelFn);
                }
                var el = this.dom;
                if(v = el.style[camel]){
                    return v;
                }
                if(cs = el.currentStyle){
                    return cs[camel];
                }
                return null;
            };
    }();