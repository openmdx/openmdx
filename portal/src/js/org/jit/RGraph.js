/*
 * File: RGraph.js
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
   Class: Canvas

   A multi-purpose Canvas object decorator.
*/

/*
   Constructor: Canvas

   Canvas initializer.

   Parameters:

      canvasId - The canvas tag id.
      fillStyle - (optional) fill color style. Default's to black
      strokeStyle - (optional) stroke color style. Default's to black

   Returns:

      A new Canvas instance.
*/
var Canvas= function (canvasId, fillStyle, strokeStyle) {
	//browser supports canvas element
		this.canvasId= canvasId;
		this.fillStyle = fillStyle;
		this.strokeStyle = strokeStyle;
		//canvas element exists
		if((this.canvas= document.getElementById(this.canvasId)) 
			&& this.canvas.getContext) {
		      this.ctx = this.canvas.getContext('2d');
		      this.ctx.fillStyle = fillStyle || 'black';
		      this.ctx.strokeStyle = strokeStyle || 'white';
			  this.setPosition();
			  this.translateToCenter();
		
		} else {
			throw "Canvas object could not initialize.";
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
	   Method: drawConcentricCircles
	
	   Draws concentric circles. Receives an integer specifying the number of concentric circles.
	*/		
	drawConcentricCircles: function (elem) {
		var times = elem || 6;
		var c = this.ctx;
		c.strokeStyle = Config.concentricCirclesColor;
		for(var i=1; i<=times; i++) {
		    c.beginPath();
		  	c.arc(0, 0, (i*Config.levelDistance), 0, Math.PI*2, true);
		  	c.stroke();
			c.closePath();
		}
		c.strokeStyle = this.strokeStyle;
	},
	
	/*
	   Method: translateToCenter
	
	   Translates canvas coordinates system to the center of the canvas object.
	*/
	translateToCenter: function() {
		this.ctx.translate(this.canvas.width / 2, this.canvas.height / 2);
	},
	

	/*
	   Method: getSize

	   Returns:
	
	      An object that contains the canvas width and height.
	      i.e. { x: canvasWidth, y: canvasHeight }
	*/
	getSize: function () {
		var width = this.canvas.width;
		var height = this.canvas.height;
		return { x: width, y: height };
	},
	
	/*
	   Method: path
	   
	  Performs a _beginPath_ executes _action_ doing then a _type_ ('fill' or 'stroke') and closing the path with closePath.
	*/
	path: function(type, action) {
		this.ctx.beginPath();
		action(this.ctx);
		this.ctx[type]();
		this.ctx.closePath();
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
	   Method: toPolar
	
	   Transforms cartesian to polar coordinates.
	
	   Returns:
	
	      A new <Polar> instance.
	*/
	
	toPolar: function() {
		var rho = this.norm();
		var atan = Math.atan2(this.y, this.x);
		if(atan < 0) atan += Math.PI * 2;
		return new Polar(atan, rho);
	},
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
	
	   Returns the conjugate por this complex.

	   Returns:
	
	     The conjugate por this complex.
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
	},


	/*
	   Method: toString
	
	   Returns a string that shows the Complex properties.

	   Returns:
	
	     A string that shows the Complex properties.
	*/
	toString: function () {
	  return "{x:"+this.x+" y:"+this.y+"}";
	}
};

/*
   Class: Polar

   A multi purpose polar representation.

*/

/*
   Constructor: Polar

   Polar constructor.

   Parameters:

      theta - An angle.
      rho - The norm.


   Returns:

      A new Polar instance.
*/

var Polar = function(theta, rho) {
	this.theta = theta;
	this.rho = rho;
};

Polar.prototype = {

	/*
	   Method: toComplex
	
	    Translates from polar to cartesian coordinates and returns a new <Complex> instance.
	
	   Returns:
	
	      A new Complex instance.
	*/
	toComplex: function() {
		return new Complex(Math.cos(this.theta), Math.sin(this.theta)).scale(this.rho);
	},

	/*
	   Method: add
	
	    Adds two <Polar> instances.
	
	   Returns:
	
	      A new Polar instance.
	*/
	add: function(polar) {
		return new Polar(this.theta + polar.theta, this.rho + polar.rho);
	},
	
	/*
	   Method: scale
	
	    Scales a polar norm.
	
	   Returns:
	
	      A new Polar instance.
	*/
	scale: function(number) {
		return new Polar(this.theta, this.rho * number);
	}
};




/*
   Object: Config

   <RGraph> global configuration object. Contains important properties to enable customization and proper behavior for the <RGraph>.
*/

var Config= {
		//Property: labelContainer
		//Id for label container. The label container is a div dom element that must be explicitly added to your page in order to enable the RGraph.
		labelContainer: 'label_container',
		
		//Property: drawConcentricCircles
		//show/hide concentricCircles
		drawConcentricCircles: true,
		
		//Property: concentricCirclesColor
		//The color of the concentric circles
		concentricCirclesColor: '#444',

		//Property: levelDistance
		//The actual distance between levels
		levelDistance: 100,
		
		//Property: normalizeDiameters
		//Not implemented yet, so keep this value to false.
		normalizeDiameters: false,
		
		//Property: nodeDiameters
		//Diameters range. Not implemented yet.
		nodeRangeDiameters: {
			min: 10,
			max: 50
		},
			
		//Property: nodeRangeValues
		//The interval of the values of the first object of your dataSet array. A superset of the values can also be specified.
		nodeRangeValues: {
			min: 1,
			max: 11
		},

		//Property: fps
		//animation frames per second
		fps:25,
		
		//Property: animationTime
		animationTime: 2000
};

/*
   Object: GraphUtil

   A multi purpose object to do graph traversal and processing.
*/

var GraphUtil = {
	/*
	   Method: eachNode
	
	   Iterates over graph nodes performing an action.
	*/
	eachNode: function(graph, action) {
		for(var i in graph.nodes) action(graph.nodes[i]);
	},
	
	/*
	   Method: getNode
	
	   Returns a node from a specified id.
	*/
	getNode: function(graph, id) {
		return graph.nodes[id];
	},
	
	/*
	   Method: eachAdjacency
	
	   Iterates over a _node_ adjacencies applying the _action_ function.
	*/
	eachAdjacency: function(graph, node, action) {
		for(var i=0, adjs = node.adjacencies; i<adjs.length; i++) action(this.getNode(graph, adjs[i]));
	},

	/*
	   Method: eachBFS
	
	   Performs a BFS traversal of a graph beginning by the node of id _id_ and performing _action_ on each node.
	*/
	eachBFS: function(graph, id, action) {
		var _self = this;
		this.eachNode(graph, function(elem) { elem._flag = false; });
		var queue = [this.getNode(graph, id)];
		while(queue.length != 0) {
			var node = queue.pop();
			node._flag = true;
			action(node, node._depth);
			for(var i=0, adj = node.adjacencies; i<adj.length; i++) {
				var n = this.getNode(graph, adj[i]);
				if(n._flag == false) {
					n._depth = node._depth + 1;
					queue.unshift(n);
				}
			}
		}
	},
	
	/*
	   Method: eachSubnode
	
	   After a BFS traversal the _depth_ property of each node has been modified. Now the graph can be traversed as a tree. This method iterates for each subnode that has depth larger than the specified node.
	*/
	eachSubnode: function(graph, node, action) {
		var d = node._depth;
		for(var i=0, ad = node.adjacencies; i<ad.length; i++) {
			var n = this.getNode(graph, ad[i]);
			if(n._depth > d) action(n);
		}
	},

	/*
	   Method: getParents
	
	   Returns all nodes having a depth that is less than the node's depth property.
	*/
	getParents: function(graph, node) {
		var adj = node.adjacencies;
		var ans = new Array();
		for(var i=0; i<adj.length; i++) {
			var n = this.getNode(graph, adj[i]);
			if(n._depth < node._depth) ans.push(n);
		}
		return ans;
	}
};

/*
   Object: GraphPlot

   An object that performs specific radial layouts for a generic graph structure.
*/
var GraphPlot = {
	//Property: labels
	//Contains an Array of labels plotted.
	labels: new Array(),
	//Property: labelsHidden
	//A flag value indicating if node labels are being displayed or not.
	labelsHidden: false,

	/*
	   Method: hideLabels
	
	   Hides all labels.
	*/
	hideLabels: function (hide) {
		var container = document.getElementById(Config.labelContainer);
		if(hide) container.style.display = 'none';
		else container.style.display = '';
		this.labelsHidden = hide;
	},

	/*
	   Method: animate
	
	   Animates the graph mantaining a radial layout.
	*/
	animate: function(graph, id, canvas, controller) {
		var _self = this;
		this.hideLabels(true);
		var comp = function(from, to, delta){ return (to.add(from.scale(-1))).scale(delta).add(from).toPolar(); };
		//could also interpolate polar coordinates.
		var interpolate = function(elem, delta) {
			var from = elem.startPos.toComplex();
			var to = elem.endPos.toComplex();
			elem.pos = comp(from, to, delta);
		};
		var animationController = {
			compute: function(delta) {
				canvas.clear();
				if(Config.drawConcentricCircles) canvas.drawConcentricCircles();
				GraphUtil.eachBFS(graph, id, function(elem) {
					if(elem._root) interpolate(elem, delta);
					_self.plotNode(elem, canvas);
					if(!this.labelsHidden) _self.plotLabel(canvas, elem, 5, elem._root);
					GraphUtil.eachSubnode(graph, elem, function(child) {
						interpolate(child, delta);
						_self.plotLine(elem, child, canvas);
					});
				});
			},
			
			complete: function() {
				_self.hideLabels(false);
				GraphUtil.eachNode(graph, function(elem) {elem.startPos = elem.pos});
				_self.plot(graph, id, canvas, false, controller);
				if(controller && controller.onAfterCompute) controller.onAfterCompute();
			}		
		};
		var _self = this;
		Animation.controller = animationController;
		Animation.start();
	},

	/*
	   Method: plot
	
	   Plots a Graph.
	*/
	plot: function(graph, id, canvas, clickListener, onCreateLabel) {
		var aGraph = graph;
		var _self = this;
		canvas.clear();
		if(Config.drawConcentricCircles) canvas.drawConcentricCircles();
		GraphUtil.eachBFS(graph, id, function(elem, i) {
			_self.plotNode(elem, canvas);
	 		if(!this.labelsHidden) _self.plotLabel(canvas, elem, 5, elem._root, clickListener, onCreateLabel);
			GraphUtil.eachSubnode(aGraph, elem, function(child) {
				_self.plotLine(elem, child, canvas);
			});
		});
	},
	
	/*
	   Method: plotNode
	
	   Plots a graph node.
	*/
	//should calculate node diameter.
	plotNode: function(node, canvas) {
		var pos = node.pos.toComplex();
		canvas.path('fill', function(context) {
	  		context.arc(pos.x, pos.y, 4, 0, Math.PI*2, true);			
		});
	},
	
	/*
	   Method: plotLine
	
	   Plots a line connecting _node_ and _child_ nodes.
	*/
	plotLine: function(node, child, canvas) {
		var pos = node.pos.toComplex();
		var posChild = child.pos.toComplex();
		canvas.path('stroke', function(context) {
			context.moveTo(pos.x, pos.y);
		  	context.lineTo(posChild.x, posChild.y);
		});
	},
	
	/*
	   Method: plotLabel
	
	   Plots a label for a given node.
	*/
	plotLabel: function(canvas, node, size, root, clickListener, controller) {
		var id = node.id;
		var tag = false;
		if(!(tag = document.getElementById(id))) {
			tag = document.createElement('div');
			var container = document.getElementById(Config.labelContainer);
			container.appendChild(tag);
			
			if(controller && controller.onCreateLabel) controller.onCreateLabel(tag, node);
			if(clickListener && clickListener.onClick) tag.onclick = function () { clickListener.onClick(id); };
		}
		var pos = node.pos.toComplex();
		var radius= canvas.getSize();
		var labelPos= {
			x: Math.round(pos.x + canvas.getPosition().x + radius.x/2 - size /2),
			y: Math.round(pos.y + canvas.getPosition().y + radius.y/2 - size /2)
		};
		tag.id = id;
		tag.className = 'node';
		tag.style.position = 'absolute';
		tag.style.width = size + 'px';
		tag.style.height = size + 'px';
		tag.style.left = labelPos.x + 'px';
		tag.style.top = labelPos.y  + 'px';
		if(this.fitsInCanvas(labelPos, canvas)) tag.style.display = '';
		else tag.style.display = 'none';
		if(controller && controller.onPlaceLabel) controller.onPlaceLabel(tag, node);
	},
	
	/*
	   Method: fitsInCanvas
	
	   Returns _true_ or _false_ if the label for the node is contained on the canvas dom element or not.
	*/
	fitsInCanvas: function(pos, canvas) {
		var size = canvas.getSize();
		if(pos.x >= size.x + canvas.position.x || pos.x < canvas.position.x 
			|| pos.y >= size.y + canvas.position.y || pos.y < canvas.position.y) return false;
		return true;					
	}
	
			
};

/*
   Class: RGraph

	An animated Graph with radial layout.

	Go to <http://blog.thejit.org> to know what kind of JSON structure feeds this object.
	
	Go to <http://blog.thejit.org/?p=8> to know what kind of controller this class accepts.
	
	Refer to the <Config> object to know what properties can be modified in order to customize this object. 

	The simplest way to create and layout a RGraph is:
	
	(start code)

	  var canvas= new Canvas('infovis', '#ccddee', '#772277');
	  var rgraph= new RGraph(canvas, controller);
	  rgraph.loadTreeFromJSON(json);
	  rgraph.compute();
	  rgraph.plot();

	(end code)

	A user should only interact with the Canvas, RGraph and Config objects/classes.
	By implementing RGraph controllers you can also customize the RGraph behavior.
*/

/*
 Constructor: RGraph

 Creates a new RGraph instance.
 
 Parameters:

    canvas - A <Canvas> instance.
    controller - _optional_ a RGraph controller <http://blog.thejit.org/?p=8>
*/
var RGraph = function(canvas, controller) {
	this.controller = controller || false;
	this.graph = new Graph();
	this.json = null;
	this.canvas = canvas;
	this.root = null;
	
};

RGraph.prototype = {
	/*
	 Method: loadTree
	
	 Loads a Graph from a json tree object <http://blog.thejit.org>
	 
	*/
	loadTree: function(json) {
		var ch = json.children;
		for(var i=0; i<ch.length; i++) {
			this.graph.addAdjacence(json, ch[i]);
			this.loadTree(ch[i]);
		}
	},
	
	/*
	 Method: flagRoot
	
	 Flags a node specified by _id_ as root.
	*/
	flagRoot: function(id) {
		this.unflagRoot();
		this.graph.nodes[id]._root = true;
	},

	/*
	 Method: unflagRoot
	
	 Unflags all nodes.
	*/
	unflagRoot: function() {
		GraphUtil.eachNode(this.graph, function(elem) {elem._root = false;});
	},

	/*
	 Method: getRoot
	
	 Returns the node flagged as root.
	*/
	getRoot: function() {
		var root = false;
		GraphUtil.eachNode(this.graph, function(elem){ if(elem._root) root = elem; });
		return root;
	},
	
	/*
	 Method: loadTreeFromJSON
	
	 Loads a RGraph from a _json_ object <http://blog.thejit.org>
	*/
	loadTreeFromJSON: function(json) {
		this.json = json;
		this.loadTree(json);
		this.root = json.id;
	},
	
	/*
	 Method: plot
	
	 Plots the RGraph
	*/
	plot: function() {
		GraphPlot.plot(this.graph, this.root, this.canvas, this, this.controller);
	},
	
	/*
	 Method: compute
	
	 Computes the graph nodes positions and stores this positions on _property_.
	*/
	compute: function(property) {
		var prop = property || ['pos', 'startPos'];
		var node = GraphUtil.getNode(this.graph, this.root);
		node._depth = 0;
		this.flagRoot(this.root);
		this.computeAngularWidths();
		this.computePositions(prop);
	},
	
	/*
	 Method: computePositions
	
	 Performs the main algorithm for computing node positions.
	*/
	computePositions: function(property) {
		var propArray = (typeof property == 'array' || typeof property == 'object')? property : [property];
		var aGraph = this.graph;
		var root = GraphUtil.getNode(this.graph, this.root);

		for(var i=0; i<propArray.length; i++)
			root[propArray[i]] = new Polar(0, 0);
		
		root.angleSpan = {
			begin: 0,
			end: 2 * Math.PI
		};
		GraphUtil.eachBFS(this.graph, this.root, function (elem) {
			var angleSpan = elem.angleSpan.end - elem.angleSpan.begin;
			var rho = (elem._depth + 1) * Config.levelDistance;
			var angleInit = elem.angleSpan.begin;
			var totalAngularWidths = (function (element){
				var total = 0;
				GraphUtil.eachSubnode(aGraph, element, function(sib) {
					total += sib._treeAngularWidth;
				});
				return total;
			})(elem);
			
			GraphUtil.eachSubnode(aGraph, elem, function(child) {
				var angleProportion = child._treeAngularWidth / totalAngularWidths * angleSpan;
				var theta = angleInit + angleProportion / 2;
				for(var i=0; i<propArray.length; i++)
					child[propArray[i]] = new Polar(theta, rho);
				child.angleSpan = {
					begin: angleInit,
					end: angleInit + angleProportion
				};
				angleInit += angleProportion;
			});
		});
	},
	
	/*
	 Method: setAngularWidthForNodes
	
	 Sets nodes angular widths.
	*/
	setAngularWidthForNodes: function() {
		var rVal = Config.nodeRangeValues, rDiam = Config.nodeRangeDiameters; 
		var diam = function(value) { return (((rDiam.max - rDiam.min)/(rVal.max - rVal.min)) * (value - rVal.min) + rDiam.min) };
		GraphUtil.eachBFS(this.graph, this.root, function(elem, i) {
			var dataValue = (Config.normalizeDiameters && elem.data && elem.data.length > 0)? elem.data[0].value : rVal.min;
			var diamValue = diam(dataValue);
			var rho = Config.levelDistance * i;
			elem._angularWidth = diamValue / rho;
		});
	},
	
	/*
	 Method: setSubtreesAngularWidths
	
	 Sets subtrees angular widths.
	*/
	setSubtreesAngularWidth: function() {
		var _self = this;
		GraphUtil.eachNode(this.graph, function(elem) {
			_self.setSubtreeAngularWidth(elem);
		});
	},
	
	/*
	 Method: setSubtreeAngularWidth
	
	 Sets the angular width for a subtree.
	*/
	setSubtreeAngularWidth: function(elem) {
		var _self = this, nodeAW = elem._angularWidth, sumAW = 0;
		GraphUtil.eachSubnode(this.graph, elem, function(child) {
			_self.setSubtreeAngularWidth(child);
			sumAW += child._treeAngularWidth;
		});
		elem._treeAngularWidth = Math.max(nodeAW, sumAW);
	},
	
	/*
	 Method: computeAngularWidths
	
	 Computes nodes and subtrees angular widths.
	*/
	computeAngularWidths: function () {
		this.setAngularWidthForNodes();
		this.setSubtreesAngularWidth();
	},
	
	/*
	 Method: getNodeAndParentAngle
	
	 Returns the _parent_ of the given node, also calculating its angle span.
	*/
	getNodeAndParentAngle: function(id) {
		var theta = false;
		var n  = GraphUtil.getNode(this.graph, id);
		var ps = GraphUtil.getParents(this.graph, n);
		var p  = (ps.length > 0)? ps[0] : false;
		if(p) {
			var posParent = p.pos.toComplex(), posChild = n.pos.toComplex();
			var newPos    = posParent.add(posChild.scale(-1));
			theta = (function(pos) {
				var t = Math.atan2(pos.y, pos.x);
				if(t < 0) t = 2 * Math.PI + t;
				
				return t;
			})(newPos);
		}
		return {_parent: p, theta: theta};
		
	},
	
	/*
	 Method: onClick
	
	 Performs all calculations and animation when clicking on a label specified by _id_. The label id is the same id as its homologue node.
	*/
	onClick: function(id) {
		if(this.root != id) {
			//we apply first constraint to the algorithm
			var obj = this.getNodeAndParentAngle(id);
			this.root = id;
			if(this.controller && this.controller.onBeforeCompute) this.controller.onBeforeCompute(GraphUtil.getNode(this.graph, id));
			this.compute('endPos');
			var thetaDiff = obj.theta - obj._parent.endPos.theta;
			GraphUtil.eachNode(this.graph, function(elem) {
				elem.endPos = elem.endPos.add(new Polar(thetaDiff, 0));
			});

			GraphPlot.animate(this.graph, id, this.canvas, this.controller);
		}		
	}
};



/*
   Class: Graph

   A generic graph class.
*/

/*
 Constructor: Graph

 Creates a new Graph instance.
 
*/
var Graph= function()  {
	//Property: nodes
	//graph nodes
	this.nodes= {};
};
	
	
Graph.prototype= {
	/*
	 Method: addAdjacence
	
	 Connects nodes specified by *obj* and *obj2*. If not found, nodes are created.
	 
	 Parameters:
	
	    obj - a <Graph.Node> object.
	    obj2 - Another <Graph.Node> object.
	*/	
  addAdjacence: function (obj, obj2) {
  	if(!this.hasNode(obj.id)) this.addNode(obj);
  	if(!this.hasNode(obj2.id)) this.addNode(obj2);
  
  	for(var i in this.nodes) {
  		if(this.nodes[i].id == obj.id) {
  			if(!this.nodes[i].adjacentTo(obj2.id)) {
  				this.nodes[i].addAdjacency(obj2.id);
  			}
  		}
  		
  		if(this.nodes[i].id == obj2.id) {	
  			if(!this.nodes[i].adjacentTo(obj.id)) {
  				this.nodes[i].addAdjacency(obj.id);
  			}
  		}
  	}
 },

	/*
	 Method: addNode
	
	 Adds a node.
	 
	 Parameters:
	
	    obj - A <Graph.Node> object.
	*/	
  addNode: function(obj) {
  	if(!this.nodes[obj.id]) {
	  	var node= new Graph.Node(obj.id, obj.name, obj.data);
	  	this.nodes[obj.id]= node;
  	}
  },


	/*
	 Method: hasNode
	
	 Returns a Boolean instance indicating if node belongs to graph or not.
	 
	 Parameters:
	
	    id - Node id.

	 Returns:
	  
	 		A Boolean instance indicating if node belongs to graph or not.
	*/	
  hasNode: function(id) {
  	for(var index in this.nodes) {
  		if (index== id) {
  			return true;
  		}
  	}
  	return false;	
  }
};
/*
   Class: Graph.Node
	
	 Behaviour of the <Graph> node.

*/
/*
   Constructor: Graph.Node

   Node constructor.

   Parameters:

      id - The node *unique identifier* id.
      name - A node's name.
      data - Place to store some extra information (can be left to null).


   Returns:

      A new <Graph.Node> instance.
*/
Graph.Node = function(id, name, data) {
	//Property: id
	this.id= id;
	//Property: name
	this.name = name;
	//Property: data
	//The dataSet object <http://blog.thejit.org>
	this.data = data;
		
	//Property: drawn
	//Node flag
	this.drawn= false;

	//Property: angle span
	//allowed angle span for adjacencies placement
	this.angleSpan= {
		begin:0,
		end:0
	};

	//Property: pos
	//node position
	this.pos= new Polar(0, 0);
	
	//Property: startPos
	//node from position
	this.startPos= new Polar(0, 0);
	
	//Property: endPos
	//node to position
	this.endPos= new Polar(0, 0);
	
	//Property: adjacencies
	//node adjacencies
	this.adjacencies= new Array();
	
}

Graph.Node.prototype= {
	
		/*
	   Method: adjacentTo
	
	   Indicates if the node is adjacent to the node indicated by the specified id

	   Parameters:
	
	      id - A node id.
	
	   Returns:
	
	     A Boolean instance indicating whether this node is adjacent to the specified by id or not.
	*/
	adjacentTo: function(id) {
		for(var index=0; index<this.adjacencies.length; index++) {
			if(id== this.adjacencies[index]) {
				return true;
			}
		}
		return false;
	},

	/*
	   Method: addAdjacency
	
	   Connects the node to the specified by id.

	   Parameters:
	
	      id - A node id.
	*/	
	addAdjacency: function(id) {
		this.adjacencies.push(id);
	}
};

/*
   Object: Trans
	
	 An object containing multiple type of transformations. Based on the mootools library <http://mootools.net>.

*/
var Trans = {
	linear: function(p) { return p;	},
	Quart: function(p) {
		return Math.pow(p, 4);
	},
	easeIn: function(transition, pos){
		return transition(pos);
	},
	easeOut: function(transition, pos){
		return 1 - transition(1 - pos);
	},
	easeInOut: function(transition, pos){
		return (pos <= 0.5) ? transition(2 * pos) / 2 : (2 - transition(2 * (1 - pos))) / 2;
	}
};

/*
   Object: Animation
	
	 An object that performs animations. Based on Fx.Base from Mootools.

*/

var Animation = {

	duration: Config.animationTime,
	fps: Config.fps,
	transition: function(p) {return Trans.easeInOut(Trans.Quart, p);},
	//transition: Trans.linear,
	controller: false,
	
	getTime: function() {
		var ans = (Date.now)? Date.now() : new Date().getTime();
		return ans;
	},
	
	step: function(){
		var time = this.getTime();
		if (time < this.time + this.duration){
			var delta = this.transition((time - this.time) / this.duration);
			this.controller.compute(delta);
		} else {
			this.timer = clearInterval(this.timer);
			this.controller.compute(1);
			this.controller.complete();
		}
	},

	start: function(){
		this.time = 0;
		this.startTimer();
		return this;
	},


	stopTimer: function(){
		if (!this.timer) return false;
		this.time = $time() - this.time;
		this.timer = $clear(this.timer);
		return true;
	},

	startTimer: function(){
		if (this.timer) return false;
		this.time = this.getTime() - this.time;
		this.timer = setInterval((function () { Animation.step(); }), Math.round(1000 / this.fps));
		return true;
	}
};
