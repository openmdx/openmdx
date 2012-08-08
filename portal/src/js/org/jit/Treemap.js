/*
 * File: Treemap.js
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

   Treemap global configuration object. Contains important properties to enable customization and proper behavior of treemaps.
*/

var Config = {
	//Property: tips
	//Enables tips for the Treemap
	tips: false,
	//Property: titleHeight
	//The height of the title. Set this to zero and remove all styles for node classes if you just want to show leaf nodes.
	titleHeight: 13,
	//Property: rootId
	//The id of the main container box. That is, the div that will contain this visualization. This div has to be explicitly added on your page.
	rootId: 'infovis',
	//Property: offset
	//Offset distance between nodes. Works better with even numbers. Set this to zero if you only want to show leaf nodes.
	offset:4,
	//Property: levelsToShow
	//Depth of the plotted tree. The plotted tree will be pruned in order to fit with the specified depth. Useful when using the "request" method on the controller.
	levelsToShow: 3,
	//Property: Color
	//Configuration object to add some color to the leaves.
	Color: {
		//Property: allow
		//Set this to true if you want to add color to the nodes. Color will be based upon the second "data" JSON object passed to the node. If your node has a "data" property which has at least two key-value objects, color will be based on your second key-value object value.
		allow: false,
		//Property: minValue
		//We need to know the minimum value of the property which will be taken in account to color the leaves.
		minValue: -100,
		//Property: maxValue
		//We need to know the maximum value of the property which will be taken in account to color the leaves.
		maxValue: 100,
		//Property: minColorValue
		//The color to be applied when displaying a min value (RGB format).
		minColorValue: [255, 0, 50],
		//Property: maxColorValue
		//The color to be applied when displaying a max value (RGB format).
		maxColorValue: [0, 255, 50]
	}
};

/*
   Object: Layout

   For the Slice and Dice method. Tells the tree to lay it's nodes vertically or horizontally.
*/
var Layout = {
	//Property: orientation
	//The default value for nodes layout. If set to vertical the root node will be displayed on a vertical fashion; all its children will be displayed horizontally; all their children vertically, and so on.
	orientation: "v",
	//Property: H
	//A constant that indicates horizontal layout.
	H: "h",
	//Property: V
	//A constant that indicates vertical layout.
	V: "v",
	//Method: switchOrientation
	//Switches default orientation.
	switchOrientation: function () {
		this.orientation = (this.orientation == "h")? "v" : "h";
	}
};

/*
   Object: TreeUtil

   An object containing some common tree manipulation methods.
*/
var TreeUtil = {

	/*
	   Method: prune
	
	   Clears all tree nodes having depth greater than maxLevel.
	
	   Parameters:
	
	      tree - A JSON tree object. <http://blog.thejit.org>
	      maxLevel - An integer specifying the maximum level allowed for this tree. All nodes having depth greater than max level will be deleted.
	
	*/
	prune: function(tree, maxLevel) {
		this.each(tree, function(elem, i) {
			if(i == maxLevel && elem.children) {
				delete elem.children;
				elem.children = [];
			}
		});
	},

	/*
	   Method: getSubtree
	
	   Returns the subtree that matches the given id.
	
	   Parameters:
	
		  tree - A JSON tree object. <http://blog.thejit.org>
	      id - A node *unique* identifier.
	
	   Returns:
	
	      A subtree having a root node matching the given id. Returns null if no subtree matching the id is found.
	*/
	getSubtree: function(tree, id) {
		if(tree.id == id) return tree;
		for(var i=0; i<tree.children.length; i++) {
			var t = this.getSubtree(tree.children[i], id);
			if(t != null) return t;
		}
		return null;
	},

	/*
	   Method: getLeaves
	
		Returns the leaves of the tree.
	
	   Parameters:
	
	      node - A tree node (which is also a JSON tree object of course). <http://blog.thejit.org>
	
	   Returns:
	
	   An object with two properties. The _leaves_ property is an Array containing all leaf nodes. The _level_ property is an Array specifying the depth of each node.
	*/
	getLeaves: function (node) {
		var leaves = new Array(), level = new Array();
		this.each(node, function(elem, i) {
			if(i <= Config.levelsToShow && (!elem.children || elem.children.length == 0 )) {
				leaves.push(elem);
				level.push(Config.levelsToShow - i);
			}
		});
		return {leaves: leaves, level: level};
	},

	/*
	   Method: resetPath
	
		Removes the _.in-path_ className for all tree dom elements and then adds this className to all ancestors of the given subtree.
	
	   Parameters:
	
	      tree - A tree node (which is also a JSON tree object of course). <http://blog.thejit.org>
	*/
	resetPath: function(tree) {
		var selector = "#" + Config.rootId + " .in-path";
		$$(selector).each(function (elem) {
			elem.removeClass("in-path");
		}); 
		var _parent = (tree)? tree._parent : false;
		while(_parent) {
			_parent.head.addClass("in-path");
			_parent = _parent._parent;
		}
	},

	/*
	   Method: eachLevel
	
		Iterates on tree nodes which relative depth is less or equal than a specified level.
	
	   Parameters:
	
	      tree - A JSON tree or subtree. <http://blog.thejit.org>
	      initLevel - An integer specifying the initial relative level. Usually zero.
	      toLevel - An integer specifying a top level. This method will iterate only through nodes with depth less than or equal this number.
	      action - A function that receives a node and an integer specifying the actual level of the node.
	      	
	*/
	eachLevel: function(tree, initLevel, toLevel, action) {
		if(initLevel <= toLevel) {
			action(tree, initLevel);
			var ch= tree.children;
			for(var i=0; i<ch.length; i++) {
				this.eachLevel(ch[i], initLevel +1, toLevel, action);	
			}
		}
	},

	/*
	   Method: each
	
		A tree iterator.
	
	   Parameters:
	
	      tree - A JSON tree or subtree. <http://blog.thejit.org>
	      action - A function that receives a node.
	      	
	*/
	each: function(tree, action) {
		this.eachLevel(tree, 0, Number.MAX_VALUE, action);
	},

	/*
	   Method: leaf
	
		Returns a boolean value specifying if the node is a tree leaf or not.
	
	   Parameters:
	
	      tree - A tree node (which is also a JSON tree object of course). <http://blog.thejit.org>

	   Returns:
	
	   	  A boolean value specifying if the node is a tree leaf or not.
 
	*/
	leaf: function(tree) {
		return (tree.children == 0);
	}
};

/*
   Class: TreeUtil.Group
	
   A class that performs actions on group of nodes.

*/
TreeUtil.Group = new Class({

	/*
	   Method: initialize
	
		<TreeUtil.Group> constructor.
	
	   Parameters:
	
	      nodeArray - An array of tree nodes. <http://blog.thejit.org>
	      controller - A treemap controller. <http://blog.thejit.org/?p=8>

	   Returns:
	
	   	  A new <TreeUtil.Group> instance.
 
	*/
	initialize: function(nodeArray, controller) {
		this.array= nodeArray;
		this.objects= new Array();
		this.controller = controller;
		this.counter = 0;
	},

	/*
	   Method: loadNodes
	
		Uses a controller _request_ method to make a request for each node.
	
	   Parameters:
	
	      level - An integer array that specifies the maximum level of the subtrees to be requested.
	      onComplete - A controller having an onComplete method. This method will be triggered after all requests have been completed -i.e after a response was been received for each request. 
	*/
	loadNodes: function (level, onComplete) {
		this.counter = 0;
		var _self = this, len = this.array.length, post = onComplete;
		var nodeSelected = {};
		if(this.array.length > 0) {
			for(var i=0; i<len; i++) {
				nodeSelected[this.array[i].id] = this.array[i];
				this.controller.request(this.array[i].id, level[i], {
					onComplete: function(nodeId, data) {
						var tree = data;
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
	}
});

/*
   Object: TM

	The Treemap object. Contains _slice and dice_ and _squarified_ algorithms for laying out trees.
*/
var TM = {
	/*
	   Method: newHead
	
		Creates the _head_ div dom element that usually contains the name of a parent JSON tree node.
	
	   Parameters:
	
	      json - A JSON subtree. <http://blog.thejit.org>
	      container - A dom container element where this _head_ will be injected.
	      controller - A treemap controller. <http://blog.thejit.org/?p=8>

	   Returns:
	
	   	  A new _head_ div dom element that has _head_ as class name.
 
	*/
	newHead: function(json, container, controller) {
		var coord = {};
		coord.width = container.offsetWidth - Config.offset;
		coord.left = Config.offset/2;
		coord.height = Config.titleHeight;
		var elem = new Element('div', {
						'class': 'head',
						'html' : json.name,
						'styles': {
							'width':coord.width,
							'left' : coord.left
						}
					});
		if(controller && controller.onCreateElement) controller.onCreateElement(elem, json);
		return elem;
	},

	/*
	   Method: newBody
	
		Creates the _body_ div dom element that usually contains a subtree dom element layout.
	
	   Parameters:
	
	      container - A dom container element where this _body_ will be injected.

	   Returns:
	
	   	  A new _body_ div dom element that has _body_ as class name.
 
	*/
	newBody: function(container) {
		var height = container.offsetHeight - (Config.titleHeight + Config.offset);
		var width = container.offsetWidth - Config.offset;
		
		var coord = {
			'top':    Config.titleHeight + Config.offset/2,
			'height': height,
			'width':  width,
			'left' :  Config.offset/2
		};

		return new Element('div', {
			'class':'body',
			'styles':coord
		});
	},

	/*
	   Method: newContent
	
		Creates the _content_ div dom element that usually contains a _leaf_ div dom element or _head_ and _body_ div dom elements.
	
	   Parameters:
	
	      json - A JSON subtree. <http://blog.thejit.org>
	      coord - An object containing width, height, left and top coordinates.

	   Returns:
	
	   	  A new _content_ div dom element that has _content_ as class name.
 
	*/
	newContent: function(json, coord) {
		var prop = {
			'id' : json.id,
			'class':'content',
			'styles': coord
		};
		return new Element('div', prop);
	},

	/*
	   Method: newLeaf
	
		Creates the _leaf_ div dom element that usually contains nothing else.
	
	   Parameters:
	
	      json - A JSON subtree. <http://blog.thejit.org>
	      container - A dom container element where this _leaf_ will be injected.
	      controller - A treemap controller. <http://blog.thejit.org/?p=8>

	   Returns:
	
	   	  A new _leaf_ div dom element having _leaf_ as class name.
 
	*/
	newLeaf: function(json, container, controller) {
		var backgroundColor = (Config.Color.allow)? this.setColor(json) : false; 
		var width = container.offsetWidth - Config.offset;
		var height = container.offsetHeight - Config.offset;
		var coord = {
			'top':0,
			'height':height,
			'width':width,
			'left':Config.offset/2
		};
		if(backgroundColor) coord['background-color'] = backgroundColor;
		var elem = new Element('div', {
						'class':'leaf',
						'styles':coord,
						'html':json.name
					});
		if(controller && controller.onCreateElement) controller.onCreateElement(elem, json);
		return elem;
	},

	/*
	   Method: setColor
	
		A JSON tree node has usually a data property containing an Array of key-value objects. This method takes the second key-value object from that array, returning a string specifying a color relative to the value property of that object.
	
	   Parameters:
	
	      json - A JSON subtree. <http://blog.thejit.org>

	   Returns:
	
	   	  A String that represents a color in hex value.
 
	*/
	setColor: function(json) {
		var c = Config.Color;
		var x = json.data[1].value.toFloat();
		var comp = function(i, x) {return ((c.maxColorValue[i] - c.minColorValue[i]) / (c.maxValue - c.minValue)) * (x - c.minValue) + c.minColorValue[i]};
		
		var newColor = new Array();
		newColor[0] = comp(0, x).toInt(); newColor[1] = comp(1, x).toInt(); newColor[2] = comp(2, x).toInt();
		return newColor.rgbToHex();
	},

	/*
	   Method: enter
	
		Sets the _elem_ parameter as root and performs the layout.
	
	   Parameters:
	
	      _self - A <TM.Squarified> or <TM.SliceAndDice> instance.
	      elem - A JSON subtree. <http://blog.thejit.org>
	*/
	enter: function(_self, elem) {
		var id = elem.getParent().id;
		var postRequest = this.pre(_self, id);
		var paramNode = TreeUtil.getSubtree(_self.tree, id);
		this.post(_self, paramNode, id, postRequest);
	},
	
	/*
	   Method: out
	
		Takes the _parent_ node of the currently shown subtree and performs the layout.
	
	   Parameters:
	
	      _self - A <TM.Squarified> or <TM.SliceAndDice> instance.
	*/
	out: function(_self) {
		var _parent = _self.shownTree._parent;
		if(_parent) {
			var paramNode = _parent;
			if(_self.controller && _self.controller.request)
				TreeUtil.prune(_parent, Config.levelsToShow);
			var id = _parent.id;
			var postRequest = this.pre(_self, id);
			this.post(_self, paramNode, id, postRequest);
		}
	},
	
	/*
	   Method: pre
	
		Called to prepare some values before performing a <TM.enter> or <TM.out> action.
	
	   Parameters:
	
	      _self - A <TM.Squarified> or <TM.SliceAndDice> instance.
		  id - A node identifier

	   Returns:
	
	   	  A _postRequest_ object.
 
	*/
	pre: function(_self, id) {
		if(Config.tips) _self.tips.hide();
		return  postRequest = {
			onComplete: function() {
				_self.loadTree(id);
				$(Config.rootId).focus();
				if(_self.controller && _self.controller.onAfterCompute) _self.controller.onAfterCompute();
			}
		};
	},

	/*
	   Method: post
	
		Called to perform post operations after doing a <TM.enter> or <TM.out> action.
	
	   Parameters:
	
	      _self - A <TM.Squarified> or <TM.SliceAndDice> instance.
	      paramNode - A JSON subtree. <http://blog.thejit.org>
		  id - A node identifier
		  postRequest - A _postRequest_ object.
	*/
	post: function(_self, paramNode, id, postRequest) {
		if(_self.controller && _self.controller.onBeforeCompute) _self.controller.onBeforeCompute(paramNode);
		if(_self.controller && _self.controller.request) {
			var leaves = TreeUtil.getLeaves(TreeUtil.getSubtree(_self.tree, id));
			var g = new TreeUtil.Group(leaves.leaves, _self.controller);
			g.loadNodes(leaves.level, postRequest);
		} else setTimeout(function () { postRequest.onComplete(); }, 1); //deferred command
	},
	
	/*
	   Method: initializeBehavior
	
		Binds different methods to dom elements like tips, color changing, adding or removing class names on mouseenter and mouseleave, etc.
	
	   Parameters:
	
	      _self - A <TM.Squarified> or <TM.SliceAndDice> instance.
	*/
	initializeBehavior: function (_self) {
		var elems = $$('.leaf', '.head');
		if(Config.tips) _self.tips = new Tips(elems, {
										showDelay: 0,
										hideDelay: 0
									});
		
		elems.each(function(elem) {
			elem.addEvent('mouseenter', function(e) {
				var id = false;
				if(elem.hasClass("leaf")) {
					id = elem.getParent().id;
					elem.addClass("over-leaf");
				}
				else if (elem.hasClass("head")) {
					id = elem.getParent().id;
					elem.addClass("over-head");
					elem.getParent().addClass("over-content");
				}
				if(id) {
					var tree = TreeUtil.getSubtree(_self.tree, id);
					TreeUtil.resetPath(tree);
				}
				e.stopPropagation();
			});

			elem.addEvent('mouseleave', function(e) {
				if(elem.hasClass("over-leaf")) elem.removeClass("over-leaf");
				else if (elem.getParent().hasClass("over-content")){
					 elem.removeClass("over-head");
					 elem.getParent().removeClass("over-content");
				}
				TreeUtil.resetPath(false);
				e.stopPropagation();
			});
			
			elem.addEvent('mouseup', function(e) {
				if(e.rightClick) TM.out(_self); else TM.enter(_self, elem);
				e.preventDefault();
				return false;
			});
		});
	}
};

/*
   Class: TM.SliceAndDice

	A JavaScript implementation of the Slice and Dice Treemap algorithm.

	Go to <http://blog.thejit.org> to know what kind of JSON structure feeds this object.
	
	Go to <http://blog.thejit.org/?p=8> to know what kind of controller this class accepts.
	
	Refer to the <Config> object to know what properties can be modified in order to customize this object. 

	The simplest way to create and layout a slice and dice treemap from a JSON object is:
	
	(start code)

	var tm = new TM.SliceAndDice();
	tm.loadFromJSON(json);

	(end code)

*/
TM.SliceAndDice = new Class({
	/*
	   Method: initialize
	
		<TM.SliceAndDice> constructor.
	
	   Parameters:
	
	      controller - A treemap controller. <http://blog.thejit.org/?p=8>
	   
	   Returns:
	
	   	  A new <TM.SliceAndDice> instance.
 
	*/
	initialize: function (controller) {
		//Property: tree
		//The JSON tree. <http://blog.thejit.org>
		this.tree = null;
		//Property: showSubtree
		//The displayed JSON subtree. <http://blog.thejit.org>
		this.shownTree = null;
		//Property: tips
		//This property will hold the a Mootools Tips instance if specified.
		this.tips = null;
		//Property: controller
		//A treemap controller <http://blog.thejit.org/?p=8>
		this.controller = controller || false;
	},

	/*
	   Method: out
	
		Takes the _parent_ node of the currently shown subtree and performs the layout.
	*/
	out: function() { TM.out(this); },

	/*
	   Method: enter
	
		Sets the _elem_ parameter as root and performs the layout.
	
	   Parameters:
	
	      elem - A JSON subtree. <http://blog.thejit.org>
	*/
	enter: function(elem) { TM.enter(this, elem); },

	/*
	   Method: newContent
	
		Creates the _content_ div dom element that usually contains a _leaf_ div dom element or _head_ and _body_ div dom elements.
	
	   Parameters:
	
	      json - A JSON subtree. <http://blog.thejit.org>
	      orientation - The currently <Layout> orientation. This value is switched recursively.

	   Returns:
	
	   	  A new _content_ div dom element that has _content_ as class name.
 
	*/
	newContent: function(json, orientation) {
		var par = (json._parent && json._parent.body)? json._parent.body : $(Config.rootId);
		var width = par.offsetWidth;
		var height = par.offsetHeight;
		var parentData = (json._parent && json._parent.data)? json._parent.data : false;
		var fact = (parentData && parentData.length > 0)? json.data[0].value.toFloat() / parentData[0].value.toFloat() : 1;
		if(orientation == Layout.H) {
			var size = (width * fact).round();
			var otherSize = height;
		} else {
			var otherSize = (height * fact).round();
			var size = width;
		}
		var prop = {
			'width':size,
			'height':otherSize
		};
		return TM.newContent(json, prop);
	},
	
	/*
	   Method: loadTree
	
		Loads the subtree specified by _id_ and plots it on the layout container.
	
	   Parameters:
	
	      id - A subtree id.
	*/
	loadTree: function(id) {
		$(Config.rootId).empty();
		var t = TreeUtil.getSubtree(this.tree, id);
		this.loadFromJSON(t);
	},

	/*
	   Method: loadFromJSON
	
		Loads the specified JSON tree and lays it on the main container.
	
	   Parameters:
	
	      json - A JSON subtree. <http://blog.thejit.org>
	*/
	loadFromJSON: function (json) {
		var _parent = (json._parent)? json._parent : false;
		this.loadTreeFromJSON(false, json, Layout.orientation);
		json.content.setStyles({
			'top':0,
			'left':0
		});
		if(this.tree == null) this.tree = json;
		this.shownTree = json;
		json._parent = _parent;
		TM.initializeBehavior(this);
	},
	
	/*
	   Method: loadTreeFromJSON
	
		Called by loadFromJSON to calculate recursively all node positions and lay out the tree.
	
	   Parameters:

	      _parent - The parent node of the json subtree.	
	      json - A JSON subtree. <http://blog.thejit.org>
	      orientation - The currently <Layout> orientation. This value is switched recursively.
	*/
	loadTreeFromJSON: function(_parent, json, orientation) {
		json._parent = _parent;
		var actualParent = (_parent && _parent.content)? _parent.content.getLast() : $(Config.rootId);
		var content = this.newContent(json, orientation);
		json.content = content;
		json.content.inject(actualParent);
		json.content.oncontextmenu = $lambda(false);
		if(!TreeUtil.leaf(json)) {
			var head = TM.newHead(json, content, this.controller);
			var body = TM.newBody(content);
			body.inject(content);
			head.inject(content, 'top');
			json.body = body;
			json.head = head;
			json.body.oncontextmenu = $lambda(false);
			json.head.oncontextmenu = $lambda(false);
		} else {
			var leaf = TM.newLeaf(json, content, this.controller);
			leaf.inject(content);
			json.leaf = leaf;
			json.leaf.oncontextmenu = $lambda(false);
		}

		orientation = (orientation == Layout.H)? Layout.V : Layout.H;		
		var dim = (orientation == Layout.H)? 'width' : 'height';
		var pos = (orientation == Layout.H)? 'left' : 'top';
		var pos2 = (orientation == Layout.H)? 'top' : 'left';
		var offsetSize =0;
		
		var size = json.content.getStyle(dim);
		var value = json.data[0].value.toFloat();
		
		var tm = this;

		json.children.each(function(elem){
			tm.loadTreeFromJSON(json, elem, orientation);
			elem.content.setStyle(pos, offsetSize);
			elem.content.setStyle(pos2, 0);
			offsetSize += elem.content.getStyle(dim).toInt();
		});
	}
});

/*
   Class: TM.Squarified

	A JavaScript implementation of the Squarified Treemap algorithm.
	
	Go to <http://blog.thejit.org> to know what kind of JSON structure feeds this object.
	
	Go to <http://blog.thejit.org/?p=8> to know what kind of controller this class accepts.
	
	Refer to the <Config> object to know what properties can be modified in order to customize this object. 

	The simplest way to create and layout a Squarified treemap from a JSON object is:
	
	(start code)

	var tm = new TM.Squarified();
	tm.loadFromJSON(json);

	(end code)
	
*/
	
TM.Squarified = new Class({
	/*
	   Method: initialize
	
		<TM.Squarified> constructor.
	
	   Parameters:
	
	      controller - A treemap controller. <http://blog.thejit.org/?p=8>
	   
	   Returns:
	
	   	  A new <TM.Squarified> instance.
 
	*/
	initialize: function(controller) {
		//Property: tree
		//The JSON tree. <http://blog.thejit.org>
		this.tree = null;
		//Property: showSubtree
		//The displayed JSON subtree. <http://blog.thejit.org>
		this.shownTree = null;
		//Property: tips
		//This property will hold the a Mootools Tips instance if specified.
		this.tips = null;
		//Property: controller
		//A treemap controller <http://blog.thejit.org/?p=8>
		this.controller = controller || false;
	},

	/*
	   Method: out
	
		Takes the _parent_ node of the currently shown subtree and performs the layout.
	*/
	out: function() { TM.out(this); },

	/*
	   Method: enter
	
		Sets the _elem_ parameter as root and performs the layout.
	
	   Parameters:
	
	      elem - A JSON subtree. <http://blog.thejit.org>
	*/
	enter: function(elem) { TM.enter(this, elem); },

	/*
	   Method: loadTree
	
		Loads the subtree specified by _id_ and plots it on the layout container.
	
	   Parameters:
	
	      id - A subtree id.
	*/
	loadTree: function(id) {
		$(Config.rootId).empty();
		var t = TreeUtil.getSubtree(this.tree, id);
		this.loadFromJSON(t);
	},

	/*
	   Method: loadFromJSON
	
		Loads the specified JSON tree and lays it on the main container.
	
	   Parameters:
	
	      json - A JSON subtree. <http://blog.thejit.org>
	*/
	loadFromJSON: function (json) {
		var _parent = (json._parent)? json._parent : false;
		var infovis = $(Config.rootId);
		var coord =  {
			'height': infovis.offsetHeight,
			'width':infovis.offsetWidth,
			'top': 0,
			'left': 0
		};
		json.coord = coord;
		this.createBox(infovis, json, json.coord); 
		this.loadTreeFromJSON(false, json, json.coord);

		if(this.tree == null) this.tree = json;
		this.shownTree = json;
		json._parent = _parent;
		TM.initializeBehavior(this);
	},

	/*
	   Method: createBox
	
		Constructs the proper DOM layout from a json node.
		
		If this node is a leaf, then it creates a _leaf_ div dom element by calling <TM.newLeaf>. Otherwise it creates a content div dom element that contains <TM.newHead> and <TM.newBody> elements.
	
	   Parameters:

		  injectTo - A DOM element where this new DOM element will be injected.	
	      json - A JSON subtree. <http://blog.thejit.org>
		  coord - A coordinates object specifying width, height, left and top style properties.

	*/
	createBox: function(injectTo, json, coord) {
		json.content = TM.newContent(json, coord);
		json.content.oncontextmenu = $lambda(false);
		json.content.inject(injectTo);
		if(!TreeUtil.leaf(json)) {
			json.head = TM.newHead(json, json.content, this.controller);
			json.body = TM.newBody(json.content);
			json.body.inject(json.content);
			json.body.oncontextmenu = $lambda(false);
			json.head.oncontextmenu = $lambda(false);
			json.head.inject(json.content, 'top');
			json.coord.width = json.body.offsetWidth;
			json.coord.height = json.body.offsetHeight;
		} else {
			json.leaf = TM.newLeaf(json, json.content, this.controller);
			json.leaf.inject(json.content);
			json.leaf.oncontextmenu = $lambda(false);
			json.coord.width = json.leaf.offsetWidth;
			json.coord.height = json.leaf.offsetHeight;
		}
	},

	/*
	   Method: worstAspectRatio
	
		Calculates the worst aspect ratio of a group of rectangles. <http://en.wikipedia.org/wiki/Aspect_ratio>
		
	   Parameters:

		  children - An array of nodes.	
	      w - The fixed dimension where rectangles are being laid out.

	   Returns:
	
	   	  The worst aspect ratio.
 

	*/
	worstAspectRatio: function(children, w) {
		if(!children || children.length == 0) return Number.MAX_VALUE;
		var areaSum = 0, maxArea = 0, minArea = Number.MAX_VALUE;
		(function (ch) {
			for(var i=0; i<ch.length; i++) {
				var area = ch[i]._area;
				areaSum += area; 
				minArea = (minArea < area)? minArea : area;
				maxArea = (maxArea > area)? maxArea : area; 
			}
		})(children);
		
		return Math.max(w * w * maxArea / (areaSum * areaSum),
						areaSum * areaSum / (w * w * minArea));
	},

	/*
	   Method: loadTreeFromJSON
	
		Called by loadFromJSON to calculate recursively all node positions and lay out the tree.
	
	   Parameters:

	      _parent - The parent node of the json subtree.	
	      json - A JSON subtree. <http://blog.thejit.org>
		  coord - A coordinates object specifying width, height, left and top style properties.
	*/
	loadTreeFromJSON: function(_parent, json, coord) {
		json._parent = _parent;
		if (!(coord.width >= coord.height && Layout.orientation == Layout.H)) Layout.switchOrientation();
		var ch = json.children;
		if(ch.length > 0) {
			this.processChildrenLayout(json, ch, coord);
			for(var i=0; i<ch.length; i++) {
				ch[i]._parent = json;
				this.createBox(json.body, ch[i], ch[i].coord);
			}
			for(var i=0; i<ch.length; i++) {
				ch[i].coord.left = 0;
				ch[i].coord.top = 0;
				if(TreeUtil.leaf(ch[i])) {
					ch[i].coord.width = ch[i].leaf.offsetWidth;
					ch[i].coord.height = ch[i].leaf.offsetHeight;
				} else {
					ch[i].coord.width = ch[i].body.offsetWidth;
					ch[i].coord.height = ch[i].body.offsetHeight;
				}
				this.loadTreeFromJSON(json, ch[i], ch[i].coord);			
			}
		}		
	},

	/*
	   Method: processChildrenLayout
	
		Computes children real areas and other useful parameters for performing the Squarified algorithm.
	
	   Parameters:

	      _parent - The parent node of the json subtree.	
	      ch - An Array of nodes
		  coord - A coordinates object specifying width, height, left and top style properties.
	*/
	processChildrenLayout: function(_parent, ch, coord) {
		//compute children real areas
		(function (par, ch) {
			var parentArea = par.body.offsetWidth * par.body.offsetHeight;
			var parentDataValue = par.data[0].value.toFloat();
			for(var i=0; i<ch.length; i++) {
				ch[i]._area = parentArea * ch[i].data[0].value.toFloat() / parentDataValue;
			}
		})(_parent, ch);
		var minimumSideValue = (Layout.orientation == Layout.H)? coord.height : coord.width;
		ch.sort(function(a, b) {
			if(a._area < b._area) return 1;
			if(a._area == b.area) return 0;
			return -1;
		});
		var initElem = [ch[0]];
		var tail = ch.slice(1);
		this.squarify(tail, initElem, minimumSideValue, coord);
	},

	/*
	   Method: squarify
	
		Performs a heuristic method to calculate div elements sizes in order to have a good aspect ratio.
	
	   Parameters:

	      tail - An array of nodes.	
	      initElem - An array of nodes
	      w - A fixed dimension where nodes will be layed out.
		  coord - A coordinates object specifying width, height, left and top style properties.
	*/
	squarify: function(tail, initElem, w, coord) {
		if(tail.length + initElem.length == 1) {
			if(tail.length == 1) this.layoutLast(tail, w, coord);
			else this.layoutLast(initElem, w, coord);
			return;
		}
		if(tail.length >= 2 && initElem.length == 0) {
			initElem = [tail[0]];
			tail = tail.slice(1);
		}
		if(tail.length == 0) {
			if(initElem.length > 0) this.layoutRow(initElem, w, coord);
			return;
		}
		var c = tail[0];
		if(this.worstAspectRatio(initElem, w) >= this.worstAspectRatio([c].concat(initElem), w)) {
			this.squarify(tail.slice(1), initElem.concat([c]), w, coord);
		} else {
			var newCoords = this.layoutRow(initElem, w, coord);
			this.squarify(tail, [], newCoords.minimumSideValue, newCoords);
		}
	},
	
	/*
	   Method: layoutLast
	
		Performs the layout of the last computed sibling.
	
	   Parameters:

	      ch - An array of nodes.	
	      w - A fixed dimension where nodes will be layed out.
		  coord - A coordinates object specifying width, height, left and top style properties.
	*/
	layoutLast: function(ch, w, coord) {
		ch[0].coord = coord;
	},

	/*
	   Method: layoutRow
	
		Performs the layout of an array of nodes.
	
	   Parameters:

	      ch - An array of nodes.	
	      w - A fixed dimension where nodes will be layed out.
		  coord - A coordinates object specifying width, height, left and top style properties.
	*/
	layoutRow: function(ch, w, coord) {
		var totalArea = (function (ch) {
			for(var i=0, collect = 0; i<ch.length; i++) {
				collect += ch[i]._area;
			}
			return collect;
		})(ch);

		var sideA = (Layout.orientation == Layout.H)? 'height' : 'width';
		var sideB = (Layout.orientation == Layout.H)? 'width' : 'height';
		var otherSide = (totalArea / w).round();
		var top = (Layout.orientation == Layout.V)? coord.height - otherSide : 0; 
		var left = 0;
		for(var i=0; i<ch.length; i++) {
			var chCoord = {};
			chCoord[sideA] = (ch[i]._area / otherSide).round();
			chCoord[sideB] = otherSide;
			chCoord['top'] = (Layout.orientation == Layout.H)? coord.top + (w - chCoord[sideA] - top) : top;
			chCoord['left'] = (Layout.orientation == Layout.H)? coord.left : coord.left + left;
			ch[i].coord = chCoord;
			if(Layout.orientation == Layout.H) top += chCoord[sideA]; else left += chCoord[sideA];
		}
		var newCoords = {};
		newCoords[sideB] = coord[sideB] - otherSide;
		newCoords[sideA] = coord[sideA];
		newCoords['left'] = (Layout.orientation == Layout.H)? coord.left + otherSide : coord.left;
		newCoords['top'] = coord.top;
		newCoords.minimumSideValue = (newCoords[sideB] > newCoords[sideA])? newCoords[sideA] : newCoords[sideB];
		if (newCoords.minimumSideValue != newCoords[sideA]) Layout.switchOrientation();
		return newCoords;
	}
});

