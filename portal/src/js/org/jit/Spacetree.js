/*
 * File: Spacetree.js
 * 
 * Author: Nicolas Garcia Belmonte
 * 
 * Copyright: Copyright 2008 by Nicolas Garcia Belmonte.
 * 
 * License: BSD License
 * 
 * * Copyright (c) 2008, Nicolas Garcia Belmonte
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the organization nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY Nicolas Garcia Belmonte ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Nicolas Garcia Belmonte BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Homepage: <http://thejit.org>
 * 
 * Version: 1.0a
 */

/*
   Object: Config

   <ST> global configuration object. Contains important properties to enable customization and proper behavior for the <ST>.
*/

var Config= {
		//Property: labelContainer
		//The id for the label container. The label container should be a div dom element where label div dom elements will be injected. You have to put the label container div dom element explicitly on your page to run the <ST>.
		labelContainer: 'label_container',
		//Property: levelsToShow
		//Depth of the plotted tree. The plotted tree will be pruned in order to fit with the specified depth. Useful when using the "request" method on the controller.
		levelsToShow: 2,
		//Property: Label
		//Configuration object to customize labels size and offset.		
		Label: {
			//Property: height
			//Label height (offset included)
			height:       26,
			//Property: realHeight
			//Label realHeight (offset excluded)
			realHeight:   20,			
			//Property: width
			//Label width (offset included)
			width:        95,
			//Property: realWidth
			//Label realWidth (offset excluded)
			realWidth:    90,
			//Property: offsetHeight
			//Used on the currently expanded subtree. Adds recursively offsetHeight between nodes for each expanded level.
			offsetHeight: 30,
			//Property: offsetWidth
			//Used on the currently expanded subtree. Adds recursively offsetWidth between nodes for each expanded level.
			offsetWidth:  30
		},
		//Property: Node
		//Configuration object to customize node styles. Use <Config.Label> to configure node width and height.
		Node: {
			//Property: strokeStyle
			//If the node <Config.Node.mode> property is setted to "stroke", this property will set the color of the boundary of the node. This is also the color of the lines connecting two nodes.
			strokeStyle:       '#ccb',
			//Property: fillStyle
			//If the node <Config.Node.mode> property is setted to "fill", this property will set the color of the node.
			fillStyle:         '#ccb',
			//Property: strokeStyleInPath
			//If the node <Config.Node.mode> property is setted to "stroke", this property will set the color of the boundary of the currently selected node and all its ancestors. This is also the color of the lines connecting the currently selected node and its ancestors.
			strokeStyleInPath: '#eed',
			//Property: strokeStyleInPath
			//If the node <Config.Node.mode> property is setted to "stroke", this property will set the color of the boundary of the currently selected node and all its ancestors. This is also the color of the lines connecting the currently selected node and its ancestors.
			fillStyleInPath:   '#ff7',
			//Property: mode
			//If setted to "stroke" only the boundary of the node will be plotted. If setted to fill, each node will be plotted with a background - fill.
			mode:              'fill', //stroke or fill
			//Property: style
			//Node style. Only "squared" option available.
			style:             'squared' 
		}
};

/*
   Class: Canvas

   A multi-purpose Canvas object decorator.
*/

/*
   Constructor: Canvas

   Canvas initializer.

   Parameters:

      canvasId - The canvas tag id.

   Returns:

      A new Canvas instance.
*/
var Canvas= function (canvasId) {
	//browser supports canvas element
	if ("function" == typeof(HTMLCanvasElement) || "object" == typeof(HTMLCanvasElement)) {
		this.canvasId= canvasId;
		this.canvas= document.getElementById(this.canvasId);
		//canvas element exists
		if((this.canvas= document.getElementById(this.canvasId)) 
			&& this.canvas.getContext) {
	      this.ctx = this.canvas.getContext('2d');
	      this.ctx.fillStyle = Config.Node.fillStyle || 'black';
	      this.ctx.strokeStyle = Config.Node.strokeStyle || 'black';
	      this.setPosition();
	  	  this.translateToCenter();
      
		} else {
			throw "canvas object with id " + canvasId + " not found";
		}
	} else {
		throw "your browser does not support canvas. Try viewing this page with firefox, safari or opera 9.5";
	}
};


Canvas.prototype= {
	/*
	   Method: getContext

	   Returns:
	
	      Canvas context handler.
	*/
	getContext: function () {
		return this.ctx;
	},
	
	/*
	   Method: makeNodeStyleSelected
	
	   Sets the fill or stroke color to fillStyleInPath or strokeStyleInPath if selected. Sets colors to default otherwise.
	
	   Parameters:
	
	      selected - A Boolean value specifying if the node is selected or not.
	      mode - A String setting the "fill" or "stroke" properties.
	*/
	makeNodeStyleSelected: function(selected, mode) {
		this.ctx[mode + "Style"] = (selected)? Config.Node[mode + "StyleInPath"] : Config.Node[mode + "Style"];
	},

	/*
	   Method: makeEdgeStyleSelected
	
	   Sets the stroke color to strokeStyleInPath if selected. Sets colors to default otherwise.
	
	   Parameters:
	
	      selected - A Boolean value specifying if the node is selected or not.
	*/
	makeEdgeStyleSelected: function(selected) {
		this.ctx.strokeStyle= (selected)? Config.Node.strokeStyleInPath : Config.Node.strokeStyle;
		this.ctx.lineWidth=   (selected)? 2 : 1;
	},

	/*
	   Method: makeRect
	
	   Draws a rectangle in canvas.
	
	   Parameters:
	
	      selected - A Boolean value specifying if the node is selected or not.
	      mode - A String sepecifying if mode is "fill" or "stroke".
	      pos - A set of two coordinates specifying top left and bottom right corners of the rectangle.
	*/
	makeRect: function(selected, mode, pos) {
		if(mode == "fill" || mode == "stroke") {
			this.makeNodeStyleSelected(selected, mode);
			this.ctx[mode + "Rect"](pos.x1, pos.y1, pos.x2, pos.y2);
		} else throw "parameter not recognized " + mode;
	},

	/*
	   Method: setPosition
	
	   Calculates canvas absolute position on HTML document.
	*/	
	setPosition: function() {
		var obj= this.canvas;
		var curleft = curtop = 0;
		if (obj.offsetParent) {
			curleft = obj.offsetLeft
			curtop = obj.offsetTop
			while (obj = obj.offsetParent) {
				curleft += obj.offsetLeft
				curtop += obj.offsetTop
			}
		}
		this.position= { x: curleft, y: curtop };
	},

	/*
	   Method: getPosition

	   Returns:
	
	      Canvas absolute position to the HTML document.
	*/
	getPosition: function() {
		return this.position;
	},

	/*
	   Method: clear
	
	   Clears the canvas object.
	*/		
	clear: function () {
		this.ctx.clearRect(-this.getSize().x / 2, -this.getSize().x / 2, this.getSize().x, this.getSize().x);
	},

	/*
	   Method: clearReactangle
	
	   Same as <clear> but only clears a section of the canvas.
	   
	   Parameters:
	   
	   	top - An integer specifying the top of the rectangle.
	   	right -  An integer specifying the right of the rectangle.
	   	bottom - An integer specifying the bottom of the rectangle.
	   	left - An integer specifying the left of the rectangle.
	*/		
	clearRectangle: function (top, right, bottom, left) {
		this.ctx.clearRect(left, top-2, right - left +2, Math.abs(bottom - top)+5);
	},

	/*
	   Method: translateToCenter
	
	   Translates canvas coordinates system to the center of the canvas object.
	*/
	translateToCenter: function() {
		this.ctx.translate(this.getSize().x / 2, this.getSize().y / 2);
	},
	

	/*
	   Method: getSize

	   Returns:
	
	      An object that contains the canvas width and height.
	      i.e. { x: canvasWidth, y: canvasHeight }
	*/
	getSize: function () {
		return { x: this.canvas.width, y: this.canvas.height };
	}
};

/*
   Class: Complex
	
	 A multi-purpose Complex Class with common methods.

*/


/*
   Constructor: Complex

   Complex constructor.

   Parameters:

      re - A real number.
      im - An real number representing the imaginary part.


   Returns:

      A new Complex instance.
*/
var Complex= function() {
	if (arguments.length > 1) {
		this.x= arguments[0];
		this.y= arguments[1];
		
	} else {
		this.x= null;
		this.y= null;
	}
	
}

Complex.prototype= {

	/*
	   Method: norm
	
	   Calculates the complex norm.
	
	   Returns:
	
	      A real number representing the complex norm.
	*/
	norm: function () {
		return Math.sqrt(this.squaredNorm());
	},
	
	/*
	   Method: squaredNorm
	
	   Calculates the complex squared norm.
	
	   Returns:
	
	      A real number representing the complex squared norm.
	*/
	squaredNorm: function () {
		return this.x*this.x + this.y*this.y;
	},

	/*
	   Method: add
	
	   Returns the result of adding two complex numbers.
	   Does not alter the original object.

	   Parameters:
	
	      pos - A Complex initialized instance.
	
	   Returns:
	
	     The result of adding two complex numbers.
	*/
	add: function(pos) {
		return new Complex(this.x + pos.x, this.y + pos.y);
	},

	/*
	   Method: prod
	
	   Returns the result of multiplying two complex numbers.
	   Does not alter the original object.

	   Parameters:
	
	      pos - A Complex initialized instance.
	
	   Returns:
	
	     The result of multiplying two complex numbers.
	*/
	prod: function(pos) {
		return new Complex(this.x*pos.x - this.y*pos.y, this.y*pos.x + this.x*pos.y);
	},

	/*
	   Method: conjugate
	
	   Returns the conjugate for this complex.

	   Returns:
	
	     The conjugate for this complex.
	*/
	conjugate: function() {
		return new Complex(this.x, -this.y);
	},


	/*
	   Method: scale
	
	   Returns the result of scaling a Complex instance.
	   Does not alter the original object.

	   Parameters:
	
	      factor - A scale factor.
	
	   Returns:
	
	     The result of scaling this complex to a factor.
	*/
	scale: function(factor) {
		return new Complex(this.x * factor, this.y * factor);
	}
};


/*
   Class: Label
	
	 A class that handles node labels for the Spacetree.

*/


/*
   Constructor: Label

   Label constructor.

   Parameters:

      node - A tree node.
      self - A <ST> instance.


   Returns:

      A new Label instance, creating a new anchor and setting its HTML content to the node name.
      Also adds the _node_ and _hidden_ class to the anchor, and sets an event handler to move the <ST> onclick.
*/

var Label= function(node, _self) {
	this.id= node.id;
	this.info= node.name;
	this.controller = _self.controller;
	this.node = node;
	
	var container= document.getElementById(Config.labelContainer);
	if (!(this.labelElement= document.getElementById(this.id))) {
		this.labelElement = document.createElement('a');
		this.labelElement.id = this.id;
		this.labelElement.href = '#';
		this.labelElement.onclick = function() {
			_self.onClick(node.id);
			return false;
		};
		this.labelElement.innerHTML = node.name;
		container.appendChild(this.labelElement);
		if(_self.controller && _self.controller.onCreateLabel) { 
			_self.controller.onCreateLabel(this.labelElement, node);
		}
	}
	
	this.setClass("node"); this.addClass("hidden");this.setDimensions();
};

Label.prototype= {
		
	/*
	   Method: plotOn
	   
	   Plots the label (if this fits in canvas).
	
	   Parameters:
	
	      pos - The position where to put the label. This position is relative to Canvas.
	      canvas - A Canvas instance.
	*/	
	plotOn: function (pos, canvas) {
			if(this.fitsInCanvas(pos, canvas))
				this.setDivProperties('node', canvas, pos);
			else this.hide();
	},

	/*
	   Method: fitsInCanvas
	   
	   Returns true or false if the current position is between canvas limits or not.
	
	   Parameters:
	
	      pos - The position where to put the label. This position is relative to Canvas.
	      canvas - A Canvas instance.
	*/	
	fitsInCanvas: function(pos, canvas) {
		var size = canvas.getSize();
		if(Math.abs(pos.x + Config.Label.width/2) >= size.x/2 
			|| Math.abs(pos.y) >= size.y/2) return false;
		return true;					
	},
	
	/*
	   Method: setDivProperties
	
	   Intended for private use: sets some label properties, such as positioning and className.

	   Parameters:
	
	      cssClass - A class name.
	      canvas - A Canvas instance.
	      pos - The label relative position.
	*/	
	setDivProperties: function(cssClass, canvas, pos) {
		var radius= canvas.getSize();
		var position = canvas.getPosition();
		var labelPos= {
				x: Math.round(pos.x + position.x + radius.x/2),
				y: Math.round(pos.y + position.y + radius.y/2 - Config.Label.height)
			};

		var div= this.labelElement;
	    div.style.top= labelPos.y+'px';
		div.style.left= labelPos.x+'px';
		if(this.hasClass("hidden")) this.removeClass("hidden");
		this.setDimensions();
		
	},
	
	/*
	   Method: addClass
	   
	   Adds the specified className to the label.
	
	   Parameters:
	
	      cssClass - class name to add to label.
	*/	
	addClass: function(cssClass) {
		if(!this.hasClass(cssClass)) {
			var array= this.labelElement.className.split(" ");
			array.push(cssClass); 
			this.labelElement.className= array.join(" ");
		}
	},

	/*
	   Method: setDimensions
	   
	   Sets label width and height based on <Config.Label> realWidth and realHeight values.
	*/	
	setDimensions: function () {
		this.labelElement.style.width= Config.Label.realWidth + 'px';
		this.labelElement.style.height= Config.Label.realHeight + 'px';
		if(this.controller && this.controller.onPlaceLabel) this.controller.onPlaceLabel(this.labelElement, this.node);
		
	},

	/*
	   Method: removeClass
	   
	   Removes a specified class from the label.
	
	   Parameters:
	
	      cssClass - A class name.
	*/	
	removeClass: function(cssClass) {
		var array= this.labelElement.className.split(" ");
		var exit= false;
		for(var i=0; i<array.length && !exit; i++) {
			if(array[i] == cssClass) {
				array.splice(i, 1); exit= true;
			}
		}
		this.labelElement.className= array.join(" ");
	},
	
	/*
	   Method: hasClass
	   
	   Returns true if the specified class name is found in the label. Returns false otherwise.
	   
	
	   Parameters:
	
	      cssClass - A class name.
	   
	  Returns:
	  	 A boolean value.
	*/	
	hasClass: function(cssClass) {
		var array= this.labelElement.className.split(" ");
		for(var i=0; i<array.length; i++) {
			if(cssClass == array[i]) return true;
		}
		return false;
	},
	

	/*
	   Method: setClass
	   
	   Sets the className property of the label with a cssClass String.
	   
	
	   Parameters:
	
	      cssClass - A class name.
	*/	
	setClass: function(cssClass) {
		this.labelElement.className= cssClass;
	},

	/*
	   Method: hide
	   
	   Hides the label by adding a "hidden" className to it.
	*/	
	hide: function() {
		this.addClass("hidden");
	},

	/*
	   Method: show
	   
	   Displays the label by removing the "hidden" className.
	*/	
	show: function() {
		this.removeClass("hidden");
	}	
};

/*
   Class: Tree
	
	 A tree JavaScript representation with common methods.


*/

/*
   Constructor: Tree

   	  Tree constructor.
   
	   Parameters:
	
	      json - A JSON tree object. <http://blog.thejit.org>
	      parent - The json parent node.
	      st - An ST instance.
	   
	  Returns:
	  	 A new <Tree> instance.
   
   
   
*/
function Tree (json, _parent, st) {
	//Property: id
	//Tree node *unique identifier*
	this.id= json.id;
	//Property: name
	//Tree node name
	this.name = json.name;
	//Property: data
	//Node data. An array of key-value JSON objects.
	this.data = json.data;
	//Property: selected
	//A flag.
	this.selected= false;
	//Property: data
	//Node data. An array of key-value JSON objects.
	this._parent= _parent;
	//Property: draw
	//Another node flag
	this.draw= false;
	//Property: pos
	//Node position
	this.pos= new Complex(0, 0);
	//Property: posTo
	//Node position
	this.posTo= null;
	//Property: _label
	//Node <Label> object
	this._label= new Label(json, st, st.controller);
	//Property: exists
	//Yet another label.
	this.exists= false;
	//Property: children
	//An array of children nodes.
	this.children= new Array();
	
}

Tree.prototype= {
	/*
	   Method: geSubtree
	
	   	  Returns a subtree that matches the given id.
	   
		   Parameters:
		
		      id - A subtree *unique identifier*
		   
		  Returns:
		  	 A subtree matching the given id or null if not found.
	   
	   
	   
	*/
	getSubtree: function(id) {
		if(this.id == id) return this;
		for(var i=0; i<this.children.length; i++) {
			var tree= this.children[i].getSubtree(id);
			if (tree != null) return tree;
		}
		return null;
	},
	
	/*
	   Method: hasChild
	
	   	  Returns a Boolean instance specifying if the node has at least one child.
	   
		   Parameters:
		
		      id - A subtree *unique identifier*
		   
		  Returns:
		  	 A Boolean instance specifying if the node has at least one child.
	   
	   
	   
	*/
	hasChild: function(id) {
		for(var i=0; i<this.children.length; i++)
			if(id== this.children[i].id) return true;
		return false;
	},

	/*
	   Method: addChild
	
	   	  Adds a subtree to the actual node.
	   
		   Parameters:
		
		      subtree - A <Tree> instance.

		  Returns:
		  	 A Boolean instance specifying if the node has at least one child.
	   
	   
	   
	*/
	addChild: function(subtree) {
		this.children.push(subtree);
	},

	/*
	   Method: each
	
	   	  Iterates over tree nodes applying a specified action to each node.
	   
		   Parameters:
		
		      action - A function that receives a <Tree> instance as parameter.
	   
	*/
	each: function(action) {
		this.eachLevel(0, Number.MAX_VALUE, action);
	},

	/*
	   Method: eachLevel
	
		Iterates on tree nodes which relative depth is less or equal than a specified level.
	
	   Parameters:
	
	      initLevel - An integer specifying the initial relative level. Usually zero.
	      toLevel - An integer specifying a top level. This method will iterate only through nodes with depth less than or equal this number.
	      action - A function that receives a node and an integer specifying the actual level of the node.
	      	
	*/
	eachLevel: function(initLevel, toLevel, action) {
		if(initLevel <= toLevel) {
			action(this, initLevel);
			var ch= this.children;
			for(var i=0; i<ch.length; i++) {
				ch[i].eachLevel(initLevel +1, toLevel, action);	
			}
		}
	},

	/*
	   Method: level
	
		Same as eachLevel, but without an initLevel parameter.
	
	   Parameters:
	
	      i - A top relative level.
	      action - A function that receives a node and an integer specifying the actual level of the node.
	      	
	*/
	level: function(i, action) {
		var level = i, callback = action;
		this.eachLevel(0, level, function (elem, j) {
			if(j == level) callback(elem);
		});
	},

	/*
	   Method: getLength
	
		Returns the number of children of the given node that have the tag _exists_ setted to true.
	
	   Returns:
	
		The number of children of the given node that have the tag _exists_ setted to true.	      	
	*/
	getLength: function() {
		var j= 0;
		var ch= this.children;
		for(var i=0; i<ch.length; i++) {
			if(ch[i].exists) j++;	
		}
		return j;
	},

	/*
	   Method: getFirstAvailablePos
	
		Returns the first _existing_ child i.e the first child having the _exists_ tag setted to true.
	
	   Returns:
	
		The first _existing_ child i.e the first child having the _exists_ tag setted to true.	      	
	*/
	getFirstAvailablePos: function() {
		for(var i=0; i<this.children.length; i++)
			if(this.children[i].exists) return this.children[i];
		return false;
	},
	
	/*
	   Method: getPreviousAvailablePos
	
		Returns the first _existing_ child of the children array having an array index less than the specified one.
	
		Parameters:
		i - An integer specifying an array index.
	
	   Returns:
	
		The first _existing_ child i.e the first child having the _exists_ tag setted to true.	      	
	*/
	getPreviousAvailablePos: function(i) {
		for(; i>0; i--)
			if(this.children[i-1].exists) return i-1;
		return false;
	},
	

	/*
	   Method: getRoot
	
		Returns the root node of the <Tree> instance.
	
	   Returns:
	
		The root node of the <Tree> instance.	      	
	*/
	getRoot: function() {
		var t= this;
		var getRootHandle= function (tree) {
			if(tree._parent == null) return tree;
			else return getRootHandle(tree._parent);
		};
		return getRootHandle(t);
	},

	/*
	   Method: getLevel
	
		Returns the actual depth level of the subtree.
	
	   Returns:
	
		The actual depth level of the subtree.	      	
	*/
	getLevel: function() {
		var getLevelHandle= function(tree, level) {
			if (tree._parent == null) return level;
			else return getLevelHandle(tree._parent, level +1);
		};
		return getLevelHandle(this, 0);
	},

	/*
	   Method: getNodesFromSameLevel
	
		Returns an array of nodes matching the depth of the actual subtree.
	
	   Returns:
	
		An array of nodes matching the depth of the actual subtree.	      	
	*/
	getNodesFromSameLevel: function() {
		var level= this.getLevel();
		var root= this.getRoot();
		var nodesFromLevel= function(tree, level, levelAt) {
			if(levelAt == level)  {
				return [tree];
			}
			if(levelAt < level) {
				var ans= new Array();
				var ch= tree.children;
				for(var i=0; i<ch.length; i++) {
					ans= ans.concat(nodesFromLevel(ch[i], level, levelAt + 1));
				}
				return ans;
			} else {
				return [];
			}
			
		};
		return nodesFromLevel(root, level, 0);
	},
	
	/*
	   Method: childrenExpanded
	
		Returns a Boolean instance indicating if the children of the current subtree are setted to be drawn or not.
	
	   Returns:
	
		A Boolean instance indicating if the children of the current subtree are setted to be drawn or not.	      	
	*/
	childrenExpanded: function () {
		var ch= this.children;
		for(var i=0; i<ch.length; i++) {
			if(ch[i].draw) return true;
		}
		return false;
	},

	/*
	   Method: childrenExist
	
		Returns a Boolean instance indicating if the children of the current subtree have the flag _exists_ setted to true.
	
	   Returns:
	
		A Boolean instance indicating if the children of the current subtree have the flag _exists_ setted to true.	      	
	*/
	childrenExist: function () {
		var ch= this.children;
		for(var i=0; i<ch.length; i++) {
			if(ch[i].exists) return true;
		}
		return false;
	},

	/*
	   Method: levelExists
	*/
	levelExists: function (arg) {
		var level = (arg)? arg : 0;
		if(!this.childrenExist()) return level;
		if(this._parent == null) return 0;
		return this._parent.levelExists(++level);
	}
};

/*
   Object: Transform

	An object holding animation and plotting parameters.
*/
var Transform= {
	//Property: orientation
	//Sets the orientation layout. Implemented orientations are _left_ (the root node will be placed on the left side of the screen) or _top_ (the root node will be placed on top of the screen).
	orientation:             "left",
	//Property: offsetBase
	//Yet another parameter to calculate how expanded nodes are separated from each other.
	offsetBase:				 8,
	//Property: scale
	//For scaling the canvas. You should not touch this
	scale:                   new Array(),
	//Property: step
	//Rate for scaling the canvas.
	step:                    0.07,
	//Property: intervalObj
	//An interval object. You should not touch this.
	intervalObj:             new Array(),
	//Property: hideIterationSpeed
	//The number of miliseconds taken to collapse a subtree.
	hideIterationSpeed:      40,
	//Property: showIterationSpeed
	//The number of miliseconds taken to expand a subtree.
	showIterationSpeed:      40,
	//Property: busy
	//An internal flag. You should not touch this.
	busy:                    false,
	//Property: Move
	//A set of animation properties used when translating the tree.
	Move: {
		//Property: time
		//Animation time
		time:               1000,
		//Property: timeSlot
		//Animation timeSlot
		timeSlot:           40,
		//Property: currentStep
		//Indicates how many animation iterations have been made. You should not touch this.
		currentStep:        0,
		//Property: N
		//Total number of steps. Dont touch this either.
		N:                  0,
		//Property: n
		//How many steps the animation has made.
		n:			        0,
		//Property: deltaV
		//A unit vector indicating sense and direction of the animation.
		deltaV:             new Array(),
		//Property: intervalObj
		//Another interval object. Dont touch this please.
		intervalObj:        null,
		//Property: initialTranslation
		//Initial translation to be made.
		initialTranslation: 0
	},
	//Property: ajaxCalls
	//A counter for grouped ajaxCalls.
	ajaxCalls: 0
	
};

Transform.Move.N= Transform.Move.time / Transform.Move.timeSlot;

/*
   Object: Geometry

	Contains methods for calculating the positioning of the <ST> nodes.
	Since a lot of things are going to be changed for the next release, I wont be commenting this code.
	You don't need to know anything about this object to animate a <ST> anyway.
*/
var Geometry = {
	
	treeFitsInCanvas: function(tree, canvas, level) {
		var size = canvas.getSize();
		size = (Transform.orientation == "left")? size.y : size.x;
		var baseSize = Geometry.getExpandedBaseSize(tree, level);
		return (baseSize < size);
		
	},
	
	setRightLevelToShow: function(tree, canvas) {
		tree.eachLevel(0, Config.levelsToShow, function (elem, i) {
			elem.exists= true;
		});
		tree.draw= true;
		var level = Config.levelsToShow;
		while(!this.treeFitsInCanvas(tree, canvas, level) && level > 1) {
			tree.level(level, function(elem) {
				elem.draw = false; elem.exists = false; elem._label.hide();
			});
			level--;
		}
	},
	
	getRightLevelToShow: function(tree, canvas) {
		var level = Config.levelsToShow;
		while(!this.treeFitsInCanvas(tree, canvas, level) && level > 1) {
			level--;
		}
		return level;
	},
	
	plotOn: function(tree, canvas, arg) {
		if(arguments.length == 1) arg= true;
		this.plotTree(tree, canvas, arg);
	},
	
	getNodesToHide: function(tree, canvas) {
		var nodeArray= new Array();
		var _self= tree;
		while(_self != null) {
			var ch= _self.getNodesFromSameLevel();
			var pushNodes= (function (arr) {
				var count= 0;
				for(var i=0; i<arr.length; i++) 
					if(arr[i].exists && arr[i].id != _self.id)
						nodeArray.push(arr[i]);
				
				return;
			})(ch);
			_self= _self._parent;
		}
		var level = this.getRightLevelToShow(tree, canvas);
		tree.eachLevel(0, level, function (elem, i) {
			if(i == level && elem.exists) {
				nodeArray.push(elem);
			}
		});
		return nodeArray;		
	},
	
	hideNodes: function (typeAjax, tree, canvas, _self, onComplete) {
		var nodeArray = this.getNodesToHide(tree, canvas);
		var group= new Group(nodeArray, _self, canvas);
		if(typeAjax) group.setProvider(typeAjax);
		group.hide(onComplete);
	},
	
	plotTree: function (tree, canvas, plotLabel) {
		if(tree.draw && tree.exists) {
			this.plotNode(tree.pos, canvas, tree.selected);
			if(plotLabel) tree._label.plotOn(tree.pos, canvas);
		}
		var begin= Geometry.getEdgeBegin(tree.pos);
		for(var i=0; i<tree.children.length; i++) {
			if(tree.children[i].exists) {
				var end= Geometry.getEdgeEnd(tree.children[i].pos);
				if(tree.children[i].draw) this.printEdge(begin, end, canvas, tree.selected && tree.children[i].selected);
				this.plotTree(tree.children[i], canvas, plotLabel);
			}
		}
	},
	
	plotScaledSubtree: function(tree, scale, canvas) {
		var trans = (Transform.orientation == "left")? 
		{ diffX: (tree.pos.x + Config.Label.width) * (1 - scale),
		  diffY: (tree.pos.y - Config.Label.height / 2) * (1-scale) }
		: 
		{ diffX: (tree.pos.x + Config.Label.width/2)*(1-scale),
		  diffY: tree.pos.y * (1-scale) };

		tree.draw= false;		
		var ctx= canvas.getContext();
		ctx.translate(trans.diffX, trans.diffY);
		ctx.scale(scale, scale);
		this.plotTree(tree, canvas, false);
		tree.draw= true;

	},
	
	hideLabels: function (tree) {
		tree.each(function(elem) {elem._label.hide()});
	},
	
	printEdge: function (begin, end, canvas, selected) {
		var ctx= canvas.getContext();
		canvas.makeEdgeStyleSelected(selected);
		ctx.beginPath();
		ctx.moveTo(begin.x, begin.y);
		ctx.lineTo(end.x, end.y);
		ctx.closePath();
		ctx.stroke();
	},
	
	getBoundingBox: function (tree) {
		if(Transform.orientation == "left") {
			var leftBottom= tree.pos.add(new Complex(Config.Label.realWidth, 0));
			var corners= this.calculateCorners(tree, {top:null, bottom:null, right:null, left:null});
			leftBottom.y= corners.bottom;
			var rightTop= new Complex(corners.right + Config.Label.width, 
																corners.top - Config.Label.height);
			return {lb: leftBottom, rt: rightTop};
		} else {
			var rightTop= tree.pos.add(new Complex(0, 0));
			var corners= this.calculateCorners(tree, {top:null, bottom:null, right:null, left:null});
			rightTop.x= corners.right + Config.Label.width;
			var leftBottom= new Complex(corners.left, corners.bottom);
			return {lb: leftBottom, rt: rightTop};
		}
	},

	calculateCorners: function(tree, corners) {
		var children= tree.children;
		for(var i=0; i<children.length; i++) {
			if(children[i].exists) {
				if(corners.top == null || corners.top > children[i].pos.y) corners.top= children[i].pos.y;
				if(corners.bottom == null || corners.bottom < children[i].pos.y) corners.bottom= children[i].pos.y;
				if(corners.right == null || corners.right < children[i].pos.x) corners.right= children[i].pos.x;
				if(corners.left == null || corners.left > children[i].pos.x) corners.left= children[i].pos.x;
				corners= this.calculateCorners(children[i], corners);	
			}
		}
		return corners;
	},
	
	//plotting methods
	getBaseSize: function (node, contracted) {
		var size = (Transform.orientation == "left")? Config.Label.height : Config.Label.width;
		return (contracted)? (node.getLength() * size + Transform.offsetBase) : this.getTreeBaseSize(node);
	},
	
	getTreeBaseSize: function(tree) {
		var size = (Transform.orientation == "left")? Config.Label.height : Config.Label.width;
		if (tree.getLength() == 0) return size;
		var children= tree.children;
		var baseHeight= 0;
		for(var i=0; i<children.length; i++) {
			if(children[i].exists) baseHeight+= this.getTreeBaseSize(children[i]);
		}
		return baseHeight + Transform.offsetBase;
	},
	
	getExpandedBaseSize: function(tree, level) {
		var size = (Transform.orientation == "left")? Config.Label.height : Config.Label.width;
		if (tree.children.length == 0 || level == 0) return size;
		var children= tree.children;
		var baseHeight= 0;
		var levelLess = level -1;
		for(var i=0; i<children.length; i++) {
			baseHeight+= this.getExpandedBaseSize(children[i], levelLess);
		}
		return baseHeight + Transform.offsetBase;
	},
	
	getAvailableBaseSize: function(node, contracted) {
		var size = (Transform.orientation == "left")? Config.Label.height : Config.Label.width;
		return (contracted)? (size) : this.getBaseSize(node);
	},
	
	getFirstPos: function(initialPos, baseHeight) {
		var size = (Transform.orientation == "left")? 
		Config.Label.width + Config.Label.offsetWidth : Config.Label.height + Config.Label.offsetHeight;

		var pos = (Transform.orientation == "left")? new Complex(initialPos.x + size, initialPos.y - baseHeight/2)
		: new Complex(initialPos.x - baseHeight / 2, initialPos.y + size);
		
		return (Transform.orientation == "left")? pos.add(new Complex(0, Transform.offsetBase/2))
		: pos.add(new Complex(Transform.offsetBase/2, 0));
	},
	
	nextPosition: function(firstPos, offsetHeight) {
		return (Transform.orientation == "left")? new Complex(firstPos.x, firstPos.y + offsetHeight)
		: new Complex(firstPos.x + offsetHeight, firstPos.y);
	},
	
	getEdgeBegin: function(pos) {
		return (Transform.orientation == "left")? 
			pos.add(new Complex(Config.Label.realWidth, -Config.Label.height / 2))
			: pos.add(new Complex(Config.Label.realWidth / 2, (Config.Label.realHeight - Config.Label.height)/2));
	},
	
	getEdgeEnd: function(pos) {
		return (Transform.orientation == "left")?
			pos.add(new Complex(0, -Config.Label.height / 2))
			: pos.add(new Complex(Config.Label.realWidth / 2, -Config.Label.realHeight));
	},
	
	
	plotNode: function(pos, canvas, selected) {
		if(Config.Node.style == 'squared')
			this.plotNodeSquared(pos, canvas, selected);
		else throw "parameter not recognized " + Config.Node.style;
	},
	
	plotNodeSquared: function(pos, canvas, selected) {
			var position= {
				x1: pos.x,
				y1: pos.y - Config.Label.height + (Config.Label.height - Config.Label.realHeight)/2,
				x2: Config.Label.realWidth,
				y2: Config.Label.realHeight
			};
		canvas.makeRect(selected, Config.Node.mode, position);
	}
	
};
/*
    Class: Group

	A class that deals with processing actions to multiple nodes _at the same time_.
*/
/*
    Constructor: Group

	Creates a new Group instance.
*/
var Group= function (array, st, canvas) {
	this.array= array;
	this.objects= new Array();
	this.intervalObj= null;
	this.scale= null;
	this.provider = null;
	this.counter = 0;
	this.st = st;
	this.canvas = canvas;
};

Group.prototype= {
	/*
	    Method: setProvider
	
		Sets a controller <http://blog.thejit.org/?p=8> to this object.
	*/
	setProvider: function (provider) {
		this.provider = provider;
	},
	
	/*
	    Method: loadNodes
	
		Uses a controller to performs multiple requests for subtrees for each node.
	*/
	loadNodes: function (level, onComplete) {
		this.counter = 0;
		var _self = this, len = this.array.length, post = onComplete;
		var nodeSelected = {};
		if(this.array.length > 0) {
			for(var i=0; i<len; i++) {
				nodeSelected[this.array[i].id] = this.array[i];
				this.provider.request(this.array[i].id, level[i], {
					onComplete: function(nodeId, data) {
						var tree = ST.returnJSONTree(data, null, _self.st);
						for(var j=0; j<tree.children.length; j++) {
							tree.children[j]._parent = nodeSelected[nodeId];
						}
						nodeSelected[nodeId].children = tree.children;
						if(++_self.counter == len) {
							if(post && post.onComplete) post.onComplete();
						}
					}
				});
			}
		} else {
			if(post && post.onComplete) post.onComplete();
		}		
	},
	
	/*
	    Method: hide
	
		Contracts multiple subtrees _at the same time_.
	*/
	hide : function (onComplete) {
		this.getNodesWithChildren();
		if(this.array.length > 0) {
			var ctx= this.canvas.getContext();
			ctx.save();
			for(var i=0; i<this.array.length; i++) {
				var children= this.array[i].children;
				for(var j=0; j<children.length; j++) {
					Geometry.hideLabels(children[j]);
				}
			}
			this.scale = 1;
			var group = this;
			this.intervalObj = setInterval(function () {group.hideSubtrees(onComplete);}, Transform.hideIterationSpeed);
		} else {
			this.hidePost();
			if(onComplete) onComplete.onComplete();
		}
	},

	/*
	    Method: show
	
		Expands multiple subtrees _at the same time_.
	*/
	show : function (onComplete) {
		this.getNodesWithChildren();
		var newFreshArray= new Array();
		if(this.array.length > 0) {
			var ctx= this.canvas.getContext();
			for(var i=0; i<this.array.length; i++) {
				var node= this.array[i];
				if(!node.childrenExpanded()) {
					newFreshArray.push(node);
					node.eachLevel(0, Config.levelsToShow, function (elem, i) {
						if(elem.exists) { elem.draw= true; }
					});
				}
			}
			this.array= newFreshArray;
			ctx.save();
			this.scale= .2;
			var group= this;
			this.intervalObj= setInterval(function () { group.showSubtrees(onComplete);}, Transform.showIterationSpeed);
		} else {
			if(onComplete) onComplete.onComplete();
		}
	},

	/*
	    Method: getNodesWithChildren
	
		Prunes the node array by selecting nodes that have children.
	*/
	getNodesWithChildren: function() {
		var nodeArray= new Array();
		for(var i=0; i<this.array.length; i++) {
			var subtree= this.array[i];
			if(subtree.getLength() > 0) {
				nodeArray.push(subtree);
			}
		}
		this.array= nodeArray;	
	},

	/*
	    Method: hideSubtrees
	
		Called by <hide> to hide multiple subtrees.
	*/
	hideSubtrees: function(onComplete) {
	  var ctx= this.canvas.getContext();
	  for(var i=0; i<this.array.length; i++) {
	    ctx.save();
	  	var node= this.array[i];
	    var bb= Geometry.getBoundingBox(node);
        this.canvas.clearRectangle(bb.rt.y, bb.rt.x, bb.lb.y, bb.lb.x);
	  	Geometry.plotScaledSubtree(node, this.scale, this.canvas);
		ctx.restore();
	  }
	  this.scale-= Transform.step;
	  if(this.scale <= 0) {
	  	clearInterval(this.intervalObj);
	  	this.hidePost();
	  	ctx.restore();
	  	if(onComplete) onComplete.onComplete();
	  	return;
	  }
	},
	
	/*
	    Method: hidePost
	
		Post processing for the method <hide>
	*/
	hidePost: function() {
		for(var i=0; i<this.array.length; i++) {
			var a= this.array[i];
			if(!this.provider || !this.provider.request) {
				if(a.getLength() > 0) {
					a.each(function (elem) {
						elem.draw= false;
						elem.exists= false;
					});
					a.draw= true;
					a.exists= true;
				}
			} else {
				delete a.children;
				a.children = [];
			}
		}
	},

	/*
	    Method: showSubtrees
	
		Method called by <show> to expand multiple subtree _at the same time_.
	*/
	showSubtrees: function(onComplete) {
      var ctx= this.canvas.getContext();
	  for(var i=0; i<this.array.length; i++) {
	  	ctx.save();	  	
	  	var node= this.array[i];
		var bb= Geometry.getBoundingBox(node);
	    this.canvas.clearRectangle(bb.rt.y, bb.rt.x, bb.lb.y, bb.lb.x);
	    Geometry.plotScaledSubtree(node, this.scale, this.canvas);
		ctx.restore();
	  }
	  this.scale+= Transform.step;
	  if(this.scale >= 1) {
	  	clearInterval(this.intervalObj);
	  	ctx.restore();
		
		for(var i=0; i<this.array.length; i++) {
			var node= this.array[i];
			var bb= Geometry.getBoundingBox(node);
			this.canvas.clearRectangle(bb.rt.y, bb.rt.x, bb.lb.y, bb.lb.x);
			node.draw= false;
			Geometry.plotOn(node, this.canvas, true);
			node.draw= true;
		}
	  	
	  	if(onComplete) onComplete.onComplete();
	  	return;
	  }
	}
};

/*
   Class: ST

	A Spacetree based implementation of an animated tree.

	Go to <http://blog.thejit.org> to know what kind of JSON structure feeds this object.
	
	Go to <http://blog.thejit.org/?p=8> to know what kind of controller this class accepts.
	
	Refer to the <Config> object to know what properties can be modified in order to customize this object. 

	The simplest way to create and layout a ST is:
	
	(start code)

	  var canvas= new Canvas('infovis');
	  var st= new ST(Config, canvas);
	  st.loadFromJSON(json);
	  st.compute();
	  st.translate(st.tree, new Complex(200, 0), "pos");
	  st.onClick(st.tree.id);

	(end code)

	A user should only interact with the Canvas, ST and Config objects/classes.
	By implementing ST controllers you can also customize the ST behavior.
*/

/*
 Constructor: ST

 Creates a new ST instance.
 
 Parameters:

    config - The Configuration object <Config>.
    canvas - A <Canvas> instance.
    controller - _optional_ a ST controller <http://blog.thejit.org/?p=8>
*/	
var ST= function(canvas, controller)  {
	this.canvas=         canvas;
	this.flag=           true;
	this.tree=           null;
	this.clickedNode=    null;
	this.controller =    controller || false;
	this.onComplete =    null;
	if(controller && controller.onAfterComplete) this.onComplete = {onComplete: function() {controller.onAfterCompute();}};
};

/*
 Method: returnJSONTree

 A static and _private_ method internally used to load some tree JSON data.
*/	
ST.returnJSONTree= 	function(json, _parent, st) {
	var ans= new Tree(json, _parent, st);
	for(var i=0; i<json.children.length; i++) {
		ans.addChild(this.returnJSONTree(json.children[i], ans, st));
	}
	return ans;
};
	
ST.prototype= {
	/*
	 Method: loadFromJSON
	
	 Use this method to load a json tree into the <ST> instance. <http://blog.thejit.org>.
	*/	
	loadFromJSON: function(json) {
		this.tree= ST.returnJSONTree(json, null, this);
	},

	/*
	 Method: loadNode
	
	 Makes requests to load leaf nodes in order to complete a <ST> subtree layout that has at most depth of <Config.levelsToShow>
	*/	
	loadNode: function(node, onComplete) {
		var leaves = this.getLeaves(node);
		var g = new Group(leaves.leaves, this, this.canvas);
		g.setProvider(this.controller);
		g.loadNodes(leaves.level, onComplete);
	},
	
	/*
	 Method: getLeaves
	
	 Returns an object holding the leaf nodes and their level.
	*/	
	getLeaves: function (node) {
		var leaves = new Array(), level = new Array();
		node.each(function(elem, i) {
			if(i <= Config.levelsToShow && elem.draw && (!elem.children || elem.children.length == 0 || elem.getLength() == 0)) {
				leaves.push(elem);
				level.push(Config.levelsToShow - i);
			}
		});
		return {leaves: leaves, level: level};
	},
	

	/*
	 Method: compute
	
	 Calculates positions from root node.
	*/	
	compute: function () {
		  this.tree.eachLevel(0, 1, function (elem) {
		  	elem.draw= false;
		  	elem.exists= true;
		  });
		  
		  this.tree.draw= true;
		  this.tree.exists= true;
		  this.tree.selected= true;
		this.calculatePositions(this.tree, new Complex(-Transform.Move.initialTranslation, 0), "pos", true);
	},
	
	/*
	 Method: calculatePositions
	
	 This method implements the core algorithm to calculate node positioning.
	*/	
	calculatePositions: function (tree, initialPos, property, contracted) {
		if((tree.id == this.clickedNode) && contracted) {
			this.calculatePositions(tree, initialPos, property, false);
			return;
		}
		tree[property]= initialPos;
		var baseHeight= Geometry.getBaseSize(tree, contracted);
		var firstPos= Geometry.getFirstPos(initialPos, baseHeight);

		if (tree.getLength() > 0) {
			var available= tree.getFirstAvailablePos();
			var availableBaseSize= (available)? Geometry.getAvailableBaseSize(available, contracted) : Config.Label.height;
			var firstPos= Geometry.getFirstPos(initialPos, baseHeight - availableBaseSize);
			
			if(tree.getLength() == 1) firstPos= Geometry.getFirstPos(initialPos, Transform.offsetBase);

			available[property]= firstPos;
			this.calculatePositions(available, firstPos, property, contracted);

			var children= tree.children;
			for(var i=0; i<children.length; i++) {
				if(children[i].exists && children[i].id != available.id) {
					var offsetHeight= 0;
					var childBaseSize= Geometry.getAvailableBaseSize(children[i], contracted);
					var poso= tree.getPreviousAvailablePos(i);
					offsetHeight+= (poso >= 0)? (Geometry.getAvailableBaseSize(children[poso], contracted) / 2) : 0;
					offsetHeight+= childBaseSize / 2;
					firstPos= Geometry.nextPosition(firstPos, offsetHeight);
				}
				this.calculatePositions(children[i], firstPos, property, contracted);
			}
		}
	},

	/*
	 Method: plot
	
	 This method plots the tree. Note that, before plotting the tree, you have to specifically call <compute> to properly calculatePositions.
	*/	
	plot: function(plotLabels) {
		if(arguments.length== 0) plotLabels= true;
		Geometry.plotOn(this.tree, this.canvas, plotLabels);
	},

	/*
	 Method: setDeltaVector
	
	 Calculates the unit vector that specifies sense and direction for the animation.
	*/	
  setDeltaVector: function(tree)  {
	tree.each(function(elem) {
	  	if(elem.exists) {
		  	Transform.Move.deltaV[elem.id]= elem.posTo
		  									.add(elem.pos.scale(-1))
		  										.scale(1.25/Transform.Move.N);
	  	}
	});
  },

  	/*
	 Method: move
	
	 Performs a translation of the tree.
	*/	
  move: function(onComplete) {
  	Transform.Move.n= 0;
  	var root = this;
  	Transform.Move.intervalObj= setInterval(function () {
  													root.moveFrameByFrame(root.tree,
  													 onComplete);
  							 				}, Transform.Move.timeSlot);
  },

	/*
	 Method: moveFrameByFrame
	
	 Called by <move> to plot each frame for the animation.
	*/	
  moveFrameByFrame: function(tree, onComplete) {
  	if(Transform.Move.n*1.25 < Transform.Move.N) {
	  	this.doStep(tree);
	  	this.canvas.clear();
	  	this.plot();
	  	Transform.Move.n++;
  	} else {
  		clearInterval(Transform.Move.intervalObj);
  		if(onComplete && onComplete.onComplete) onComplete.onComplete();
  	}
  },
 
 	/*
	 Method: doStep
	
	 Calculates nodes positions for the next frame.
	*/	
  doStep: function(tree) {
	tree.each(function (elem) {
		if(elem.exists && elem.posTo) {
			 elem.pos= elem.pos.add(Transform.Move.deltaV[elem.id]);
		}
	});
  },
  
  	/*
	 Method: showPertinentNodes
	
	 Determines which nodes to expand.
	*/	
  showPertinentNodes: function (ide) {
  	var tree= this.tree.getSubtree(ide);
	var nodeArray= new Array();
	tree.eachLevel(0, Config.levelsToShow, function (elem, i)  {
		if(!elem.childrenExpanded()) {
			nodeArray.push(elem);
		}
	});
	var group = new Group(nodeArray, this, this.canvas);
	group.show({onComplete: function() {
		Transform.busy= false;
	}});

  },

  	/*
	 Method: translate
	
	 Calculates a translation from the start positions to the given position.
	*/	
  translate: function(tree, pos, property) {
  	tree.each(function (elem) {
  		if(elem.exists) elem[property]= elem[property].add(pos.scale(-1));
  	});
  },

 	/*
	 Method: selectPath
	
	 Sets a "selected" flag to nodes that are in the path.
	*/	
  selectPath: function(node) {
  	if(node == null) return;
  	node.selected= true; 
  	this.selectPath(node._parent);
  },
  
 	/*
	 Method: switchPosition
	
	 Now here's a method you _can_ use. Switches the tree orientation from vertical to horizontal or viceversa.
	*/	
  switchPosition: function(onComplete) {
  	if(!Transform.busy) {
	  	Transform.busy = true;
	  	var betaComplete = onComplete;
	  	var node = this.tree.getSubtree(this.clickedNode);
		var t= this.tree;
		var _self = this;
		var switchState = function () {return (Transform.orientation == "left")? "top" : "left"};
		
		Transform.orientation = switchState();
		var nodeArray = Geometry.getNodesToHide(node, this.canvas);
		Transform.orientation = switchState();
		var g = new Group(nodeArray, this, this.canvas);
		g.hide({onComplete: function() {
			Transform.orientation = switchState();
			_self.calculatePositions(t, new Complex(0, 0), "posTo", true);
			Transform.busy = false;
			_self.onClick(_self.clickedNode, betaComplete);
		}});
	}
  },
  
  /*
	 Method: onClick

	Another method you _can_ use. This method is called when clicking on a tree node. It mainly performs all calculations and the animation of contracting, translating and expanding pertinent nodes.
	
		
	 Parameters:
	
	    ide - The label id. The label id is usually the same as the tree node id.

	*/	  
  onClick: function (ide) {
	var canvas = this.canvas;
	var complete = (arguments.length == 2 && arguments[1].onComplete)? arguments[1] : false;
	var finalComplete = null;
	var _self = this;
	finalComplete = (complete)? { onComplete: function() { complete.onComplete(); }	} : false;
	
	if(!Transform.busy) {
		Transform.busy= true;
		this.clickedNode= ide;
		var node=  this.tree.getSubtree(ide);
		this.tree.each(function (elem) {
			if(elem.exists) {
				elem.selected= false;
			}
		});
		this.selectPath(node);
		if(this.controller && this.controller.onBeforeCompute) this.controller.onBeforeCompute(node);
		var tree= this;
		var postLoad = {
			onComplete: function () {
				Geometry.hideNodes(tree.controller, node, canvas, _self, {
					onComplete: function() {
						var initialLevelExpanded = node.levelExists();
						Geometry.setRightLevelToShow(node, canvas);
						var newLevelExpanded = node.levelExists();
						if(initialLevelExpanded < newLevelExpanded 
							|| (initialLevelExpanded == newLevelExpanded && initialLevelExpanded == 0)) {
							tree.calculatePositions(tree.tree, new Complex(-Transform.Move.initialTranslation, 0), "posTo", true);
							tree.translate(tree.tree, node.posTo.add(new Complex(Transform.Move.initialTranslation, 0)), "posTo");
							tree.setDeltaVector(tree.tree);
							tree.move({onComplete: function () {
								if(finalComplete) finalComplete.onComplete();
								if(_self.controller && _self.controller.onAfterCompute) _self.controller.onAfterCompute();	
									tree.showPertinentNodes(ide);
							}});
						}
					}
				});
			}
		};
		if(this.controller && this.controller.request) this.loadNode(node, postLoad);
		else postLoad.onComplete();
		
	}
  }
};