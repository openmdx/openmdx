// JavaScript Diagram Builder 3.01
// Copyright (c) 2001-2004 Lutz Tautenhahn, all rights reserved.
//
// The Author grants you a non-exclusive, royalty free, license to use,
// modify and redistribute this software, provided that this copyright notice
// and license appear on all copies of the software.
// This software is provided "as is", without a warranty of any kind.

function _Draw(theDrawColor, theTextColor, isScaleText, theTooltipText, theAction, theGridColor, theSubGridColor)
{ var x0,y0,i,j,itext,l,x,y,r,u,fn,dx,dy,xr,yr,invdifx,invdify,deltax,deltay,id=this.ID,lay=0,selObj="",divtext="",ii=0,oo,k,sub;
  var c151="&#151;";
  if (_nvl(theGridColor,"")!="") this.GridColor=theGridColor;
  if (_nvl(theSubGridColor,"")!="") this.SubGridColor=theSubGridColor;
  lay++; if (document.layers[id]) lay++;
  selObj=_nvl(theAction,"");
  if (selObj!="") selObj=" href='javascript:"+selObj+"'";
  var drawCol=(_nvl(theDrawColor,"")=="") ? "" : "bgcolor="+theDrawColor;
  if (lay>1)
  { with(_DiagramTarget.document.layers[id])
    { top=this.top;
      left=this.left;
      document.open();
      document.writeln("<div style='position:absolute; left:1; top:1;'><table border=1 bordercolor="+theTextColor+" cellpadding=0 cellspacing=0><tr><td "+drawCol+"><a"+selObj+"><img src='transparent.gif' width="+eval(this.right-this.left-1)+" height="+eval(this.bottom-this.top-2)+" border=0 alt='"+_nvl(theTooltipText,"")+"'></a></td></tr></table></div>");
    }
  }
  else
  { _DiagramTarget.document.writeln("<layer id='"+this.ID+"' top="+this.top+" left="+this.left+" z-Index="+this.zIndex+">"); 
    _DiagramTarget.document.writeln("<div style='position:absolute; left:1; top:1;'><table border=1 bordercolor="+theTextColor+" cellpadding=0 cellspacing=0><tr><td "+drawCol+"><a"+selObj+"><img src='transparent.gif' width="+eval(this.right-this.left-1)+" height="+eval(this.bottom-this.top-2)+" border=0 alt='"+_nvl(theTooltipText,"")+"'></a></td></tr></table></div>");
  }
  if ((this.XScale==1)||(isNaN(this.XScale)))
  { u="";
    fn="";
    if (isNaN(this.XScale))
    { if (this.XScale.substr(0,9)=="function ") fn=eval("window."+this.XScale.substr(9));
      else u=this.XScale;
    }
    dx=(this.xmax-this.xmin);
    if (Math.abs(dx)>0)
    { invdifx=(this.right-this.left)/(this.xmax-this.xmin);
      r=1;
      while (Math.abs(dx)>=100) { dx/=10; r*=10; }
      while (Math.abs(dx)<10) { dx*=10; r/=10; }
      if (Math.abs(dx)>=50) { this.SubGrids=5; deltax=10*r*_sign(dx); }
      else
      { if (Math.abs(dx)>=20) { this.SubGrids=5; deltax=5*r*_sign(dx); }
        else { this.SubGrids=4; deltax=2*r*_sign(dx); }
      }
      if (this.XGridDelta!=0) deltax=this.XGridDelta;
      if (this.XSubGrids!=0) this.SubGrids=this.XSubGrids;
      sub=deltax*invdifx/this.SubGrids;
      x=Math.floor(this.xmin/deltax)*deltax;
      itext=0;
      for (j=54; j>=-1; j--)
      { xr=x+j*deltax;
        x0=Math.round(this.left+(-this.xmin+xr)*invdifx);
        if (lay>1) oo=_DiagramTarget.document.layers[id];
        else oo=_DiagramTarget;
        with(oo.document)
        { if (this.SubGridColor)
          { for (k=1; k<this.SubGrids; k++)
            { if ((x0+k*sub>this.left+1)&&(x0+k*sub<this.right-1))
                writeln("<div style='position:absolute; left:"+Math.round(x0-this.left+k*sub)+"; top:1; font-size:1pt; line-height:1pt'><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+this.SubGridColor+"><img src='transparent.gif' width=1 height="+eval(this.bottom-this.top-1)+"></td></tr></table></div>");
            }
          }
        }
        if ((x0>=this.left)&&(x0<=this.right))
        { itext++;
          if ((itext!=2)||(!isScaleText))
          { if (r>1) 
            { if (fn) l=fn(xr)+"";
              else l=xr+""+u; 
            }
            else 
            { if (fn) l=fn(Math.round(10*xr/r)/Math.round(10/r))+"";
              else l=Math.round(10*xr/r)/Math.round(10/r)+""+u; 
            }
            if (l.charAt(0)==".") l="0"+l;
            if (l.substr(0,2)=="-.") l="-0"+l.substr(1,100);
          }
          else l=this.xtext;
          if (lay>1) oo=_DiagramTarget.document.layers[id];
          else oo=_DiagramTarget;
          with(oo.document)
          { writeln("<div style='position:absolute; left:"+eval(x0-50-this.left)+"; top:"+eval(this.bottom+8-this.top)+";'><table noborder cellpadding=0 cellspacing=0><tr><td width=102 align=center><div style='color:"+theTextColor+";"+this.Font+"'>"+l+"</div></td></tr></table></div>");
            writeln("<div style='position:absolute; left:"+eval(x0-this.left)+"; top:"+eval(this.bottom-5-this.top)+"; font-size:1pt; line-height:1pt'><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+theTextColor+"><img src='transparent.gif' width=1 height=12></td></tr></table></div>");
            if ((this.GridColor)&&(x0>this.left)&&(x0<this.right)) writeln("<div style='position:absolute; left:"+eval(x0-this.left)+"; top:1; font-size:1pt; line-height:1pt'><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+this.GridColor+"><img src='transparent.gif' width=1 height="+eval(this.bottom-this.top-1)+"></td></tr></table></div>");
          }
        }
      }
    }
  }
  if ((!isNaN(this.XScale))&&(this.XScale>1))
  { dx=(this.xmax-this.xmin);
    if (Math.abs(dx)>0)
    { invdifx=(this.right-this.left)/(this.xmax-this.xmin);
      deltax=this.DateInterval(Math.abs(dx))*_sign(dx);
      if (this.XGridDelta!=0) deltax=this.XGridDelta;
      if (this.XSubGrids!=0) this.SubGrids=this.XSubGrids;
      sub=deltax*invdifx/this.SubGrids;      
      x=Math.floor(this.xmin/deltax)*deltax;
      itext=0;
      for (j=54; j>=-2; j--)
      { xr=x+j*deltax;
        x0=Math.round(this.left+(-this.xmin+x+j*deltax)*invdifx);
        if (lay>1) oo=_DiagramTarget.document.layers[id];
        else oo=_DiagramTarget;
        with(oo.document)
        { if (this.SubGridColor)
          { for (k=1; k<this.SubGrids; k++)
            { if ((x0+k*sub>this.left+1)&&(x0+k*sub<this.right-1))
                writeln("<div style='position:absolute; left:"+Math.round(x0-this.left+k*sub)+"; top:1; font-size:1pt; line-height:1pt'><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+this.SubGridColor+"><img src='transparent.gif' width=1 height="+eval(this.bottom-this.top-1)+"></td></tr></table></div>");
            }
          }
        }
        if ((x0>=this.left)&&(x0<=this.right))
        { itext++;
          if ((itext!=2)||(!isScaleText)) l=_DateFormat(xr, Math.abs(deltax), this.XScale);
          else l=this.xtext;
          if (lay>1) oo=_DiagramTarget.document.layers[id];
          else oo=_DiagramTarget;
          with(oo.document)
          { writeln("<div style='position:absolute; left:"+eval(x0-50-this.left)+"; top:"+eval(this.bottom+8-this.top)+";'><table noborder cellpadding=0 cellspacing=0><tr><td width=102 align=center><div style='color:"+theTextColor+";"+this.Font+"'>"+l+"</div></td></tr></table></div>");
            writeln("<div style='position:absolute; left:"+eval(x0-this.left)+"; top:"+eval(this.bottom-5-this.top)+"; font-size:1pt; line-height:1pt'><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+theTextColor+"><img src='transparent.gif' width=1 height=12></td></tr></table></div>");
            if ((this.GridColor)&&(x0>this.left)&&(x0<this.right)) writeln("<div style='position:absolute; left:"+eval(x0-this.left)+"; top:1; font-size:1pt; line-height:1pt'><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+this.GridColor+"><img src='transparent.gif' width=1 height="+eval(this.bottom-this.top-1)+"></td></tr></table></div>");
          }
        }
      }
    }
  }
  if ((this.YScale==1)||(isNaN(this.YScale)))
  { u="";
    fn="";
    if (isNaN(this.YScale))
    { if (this.YScale.substr(0,9)=="function ") fn=eval("window."+this.YScale.substr(9));
      else u=this.YScale;
    }
    dy=this.ymax-this.ymin;
    if (Math.abs(dy)>0)
    { invdify=(this.bottom-this.top)/(this.ymax-this.ymin);
      r=1;
      while (Math.abs(dy)>=100) { dy/=10; r*=10; }
      while (Math.abs(dy)<10) { dy*=10; r/=10; }
      if (Math.abs(dy)>=50) { this.SubGrids=5; deltay=10*r*_sign(dy); }
      else
      { if (Math.abs(dy)>=20) { this.SubGrids=5; deltay=5*r*_sign(dy); }
        else { this.SubGrids=4; deltay=2*r*_sign(dy); }
      }      
      if (this.YGridDelta!=0) deltay=this.YGridDelta;
      if (this.YSubGrids!=0) this.SubGrids=this.YSubGrids;
      sub=deltay*invdify/this.SubGrids;
      y=Math.floor(this.ymax/deltay)*deltay;
      itext=0;
      for (j=-1; j<=54; j++)
      { yr=y-j*deltay;
        y0=Math.round(this.top+(this.ymax-yr)*invdify);
        if (lay>1) oo=_DiagramTarget.document.layers[id];
        else oo=_DiagramTarget;
        with(oo.document)
        { if (this.SubGridColor)
          { for (k=1; k<this.SubGrids; k++)
            { if ((y0+k*sub>this.top+1)&&(y0+k*sub<this.bottom-1))
                writeln("<div style='position:absolute; left:1; top:"+Math.round(y0-this.top+k*sub)+"; font-size:1pt; line-height:1pt'><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+this.SubGridColor+" valign=top><img src='transparent.gif' height=1 width="+eval(this.right-this.left-1)+"></td></tr></table></div>");
            }
          }
        }
        if ((y0>=this.top)&&(y0<=this.bottom))
        { itext++;
          if ((itext!=2)||(!isScaleText))
          { if (r>1)
            { if (fn) l=fn(yr)+"";
              else l=yr+""+u;
            }   
            else
            { if (fn) l=fn(Math.round(10*yr/r)/Math.round(10/r))+"";
              else l=Math.round(10*yr/r)/Math.round(10/r)+""+u;
            }  
            if (l.charAt(0)==".") l="0"+l;
            if (l.substr(0,2)=="-.") l="-0"+l.substr(1,100);
          }
          else l=this.ytext;
          if (lay>1) oo=_DiagramTarget.document.layers[id];
          else oo=_DiagramTarget;
          with(oo.document)
          { writeln("<div style='position:absolute; left:-100; top:"+eval(y0-9-this.top)+";'><table noborder cellpadding=0 cellspacing=0><tr><td width=89 align=right><div style='color:"+theTextColor+";"+this.Font+"'>"+l+"</div></td></tr></table></div>");
            writeln("<div style='position:absolute; left:-5; top:"+eval(y0-this.top)+"; font-size:1pt; line-height:1pt'><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+theTextColor+" valign=top><img src='transparent.gif' height=1 width=11></td></tr></table></div>");
            if ((this.GridColor)&&(y0>this.top)&&(y0<this.bottom)) writeln("<div style='position:absolute; left:1; top:"+eval(y0-this.top)+"; font-size:1pt; line-height:1pt'><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+this.GridColor+" valign=top><img src='transparent.gif' height=1 width="+eval(this.right-this.left-1)+"></td></tr></table></div>");
          }
        }
      }
    }
  }
  if ((!isNaN(this.YScale))&&(this.YScale>1))
  { dy=this.ymax-this.ymin;
    if (Math.abs(dy)>0)
    { invdify=(this.bottom-this.top)/(this.ymax-this.ymin);
      deltay=this.DateInterval(Math.abs(dy))*_sign(dy);
      if (this.YGridDelta!=0) deltay=this.YGridDelta;
      if (this.YSubGrids!=0) this.SubGrids=this.YSubGrids;
      sub=deltay*invdify/this.SubGrids;
      y=Math.floor(this.ymax/deltay)*deltay;
      itext=0;
      for (j=-2; j<=54; j++)
      { yr=y-j*deltay;
        y0=Math.round(this.top+(this.ymax-y+j*deltay)*invdify);
        if (lay>1) oo=_DiagramTarget.document.layers[id];
        else oo=_DiagramTarget;
        with(oo.document)
        { if (this.SubGridColor)
          { for (k=1; k<this.SubGrids; k++)
            { if ((y0+k*sub>this.top+1)&&(y0+k*sub<this.bottom-1))
                writeln("<div style='position:absolute; left:1; top:"+Math.round(y0-this.top+k*sub)+"; font-size:1pt; line-height:1pt'><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+this.SubGridColor+" valign=top><img src='transparent.gif' height=1 width="+eval(this.right-this.left-1)+"></td></tr></table></div>");
            }
          }
        }
        if ((y0>=this.top)&&(y0<=this.bottom))
        { itext++;
          if ((itext!=2)||(!isScaleText)) l=_DateFormat(yr, Math.abs(deltay), this.YScale);
          else l=this.ytext;
          if (lay>1) oo=_DiagramTarget.document.layers[id];
          else oo=_DiagramTarget;
          with(oo.document)
          { writeln("<div style='position:absolute; left:-100; top:"+eval(y0-9-this.top)+";'><table noborder cellpadding=0 cellspacing=0><tr><td width=89 align=right><div style='color:"+theTextColor+";"+this.Font+"'>"+l+"</div></td></tr></table></div>");
            writeln("<div style='position:absolute; left:-5; top:"+eval(y0-this.top)+"; font-size:1pt; line-height:1pt'><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+theTextColor+" valign=top><img src='transparent.gif' height=1 width=11></td></tr></table></div>");
            if ((this.GridColor)&&(y0>this.top)&&(y0<this.bottom)) writeln("<div style='position:absolute; left:1; top:"+eval(y0-this.top)+"; font-size:1pt; line-height:1pt'><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+this.GridColor+" valign=top><img src='transparent.gif' height=1 width="+eval(this.right-this.left-1)+"></td></tr></table></div>");
          }
        }
      }
    }
  }
  if (lay>1)
  { with(_DiagramTarget.document.layers[id])
    { document.writeln("<div style='position:absolute; left:0; top:-20;'><table noborder cellpadding=0 cellspacing=0><tr><td width="+eval(this.right-this.left)+" align=center><div style=' color:"+theTextColor+";"+this.Font+"'>"+this.title+"</div></td></tr></table></div>");
      document.close();
    }
  }
  else
  { _DiagramTarget.document.writeln("<div style='position:absolute; left:0; top:-20;'><table noborder cellpadding=0 cellspacing=0><tr><td width="+eval(this.right-this.left)+" align=center><div style=' color:"+theTextColor+";"+this.Font+"'>"+this.title+"</div></td></tr></table></div>");
    _DiagramTarget.document.writeln("</layer>");
  }
}

function Bar(theLeft, theTop, theRight, theBottom, theDrawColor, theText, theTextColor, theTooltipText, theAction)
{ this.ID="Bar"+_N_Bar; _N_Bar++; _zIndex++;
  this.left=theLeft;
  this.top=theTop;
  this.width=theRight-theLeft;
  this.height=theBottom-theTop;
  this.DrawColor=theDrawColor;
  this.Text=String(theText);
  this.TextColor=theTextColor;
  this.BorderWidth=0;
  this.BorderColor="";
  this.TooltipText=theTooltipText;
  this.Action=theAction;
  this.SetVisibility=_SetVisibility;
  this.SetColor=_SetBarColor;
  this.SetText=_SetBarText;
  this.SetTitle=_SetBarTitle;
  this.MoveTo=_MoveTo;
  this.ResizeTo=_ResizeTo;
  this.Delete=_Delete;
  var selObj=_nvl(this.Action,"");
  if (selObj!="") selObj=" href='javascript:"+selObj+"'";
  var tt="";
  while (tt.length<this.Text.length) tt=tt+" ";
  if ((tt=="")||(tt==this.Text)) tt="<img src='transparent.gif' width="+this.width+" height="+this.height+" border=0 alt='"+_nvl(theTooltipText,"")+"'>";
  else tt=this.Text;
  var drawCol=(_nvl(theDrawColor,"")=="") ? "" : "bgcolor="+theDrawColor;
  var textCol=(_nvl(theTextColor,"")=="") ? "" : "color:"+theTextColor+";";
  _DiagramTarget.document.writeln("<layer id='"+this.ID+"' left="+theLeft+" top="+theTop+" z-Index="+_zIndex+">");
  _DiagramTarget.document.writeln("<layer style='position:absolute;left:0;top:0;'><table noborder cellpadding=0 cellspacing=0><tr><td "+drawCol+" width="+eval(theRight-theLeft)+" height="+eval(theBottom-theTop)+" align=center valign=top><a style='"+textCol+"text-decoration:none;"+_BFont+"'"+selObj+">"+tt+"</a></td></tr></table></layer>");
  _DiagramTarget.document.writeln("</layer>");
  return(this);
}
function Box(theLeft, theTop, theRight, theBottom, theDrawColor, theText, theTextColor, theBorderWidth, theBorderColor, theTooltipText, theAction)
{ this.ID="Box"+_N_Box; _N_Box++; _zIndex++;
  this.left=theLeft;
  this.top=theTop;
  this.width=theRight-theLeft;
  this.height=theBottom-theTop;
  this.DrawColor=theDrawColor;
  this.Text=String(theText);
  this.TextColor=theTextColor;
  this.BorderWidth=theBorderWidth;
  this.BorderColor=theBorderColor;
  this.Action=theAction;
  this.SetVisibility=_SetVisibility;
  this.SetColor=_SetBarColor;
  this.SetText=_SetBarText;
  this.SetTitle=_SetBarTitle;
  this.MoveTo=_MoveTo;
  this.ResizeTo=_ResizeTo;
  this.Delete=_Delete;
  var bb="";
  var ww=theBorderWidth;
  if (_nvl(theBorderWidth,"")=="") ww=0;
  if ((_nvl(theBorderWidth,"")!="")&&(_nvl(theBorderColor,"")!=""))
    bb="bordercolor="+theBorderColor;
  var selObj=_nvl(this.Action,"");
  if (selObj!="") selObj=" href='javascript:"+selObj+"'";
  var tt="";
  while (tt.length<this.Text.length) tt=tt+" ";
  if ((tt=="")||(tt==this.Text)) tt="<img src='transparent.gif' width="+eval(this.width-2*this.BorderWidth)+" height="+eval(this.height-2*this.BorderWidth)+" border=0 alt='"+_nvl(theTooltipText,"")+"'>";
  else tt=this.Text;
  var drawCol=(_nvl(theDrawColor,"")=="") ? "" : "bgcolor="+theDrawColor;
  var textCol=(_nvl(theTextColor,"")=="") ? "" : "color:"+theTextColor+";";
  _DiagramTarget.document.writeln("<layer id='"+this.ID+"' left="+theLeft+" top="+theTop+" z-Index="+_zIndex+">");
  _DiagramTarget.document.writeln("<layer style='position:absolute;left:"+ww+";top:"+ww+";'><table border="+ww+" "+bb+" cellpadding=0 cellspacing=0><tr><td "+drawCol+" width="+eval(theRight-theLeft-ww)+" height="+eval(theBottom-theTop-ww)+" align=center valign=top><a style='"+textCol+"text-decoration:none;"+_BFont+"'"+selObj+">"+tt+"</a></td></tr></table></layer>");
  _DiagramTarget.document.writeln("</layer>");
  return(this);
}
function _SetBarColor(theColor)
{ var id=this.ID, selObj;
  this.DrawColor=theColor;
  var ww=this.BorderWidth;
  if (_nvl(this.BorderWidth,"")=="") ww=0;
  selObj=_nvl(this.Action,"");
  if (selObj!="") selObj=" href='javascript:"+selObj+"'";
  var tt="";
  while (tt.length<this.Text.length) tt=tt+" ";
  if ((tt=="")||(this.Text==tt)) tt="<img src='transparent.gif' width="+eval(this.width-2*this.BorderWidth)+" height="+eval(this.height-2*this.BorderWidth)+" border=0 alt='"+_nvl(this.TooltipText,"")+"'>";
  else tt=this.Text;
  var drawCol=(_nvl(this.DrawColor,"")=="") ? "" : "bgcolor="+this.DrawColor;
  var textCol=(_nvl(this.TextColor,"")=="") ? "" : "color:"+this.TextColor+";";
  with(_DiagramTarget.document.layers[id])
  { document.open();
    if ((_nvl(this.BorderWidth,"")!="")&&(_nvl(this.BorderColor,"")!=""))
      document.writeln("<layer style='position:absolute;left:"+ww+";top:"+ww+";'><table border="+ww+" bordercolor="+this.BorderColor+" cellpadding=0 cellspacing=0><tr><td "+drawCol+" width="+eval(this.width-ww)+" height="+eval(this.height-ww)+" align=center valign=top><a style='"+textCol+"text-decoration:none;"+_BFont+"'"+selObj+">"+tt+"</a></td></tr></table></layer>");
    else
      document.writeln("<layer style='position:absolute;left:0;top:0;'><table noborder cellpadding=0 cellspacing=0><tr><td "+drawCol+" width="+this.width+" height="+this.height+" align=center valign=top><a style='"+textCol+"text-decoration:none;"+_BFont+"'"+selObj+">"+tt+"</a></td></tr></table></layer>");
    document.close();
  }
}
function _SetBarTitle(theTitle)
{ this.TooltipText=theTitle;
  this.SetColor(this.DrawColor);
}
function _SetBarText(theText)
{ var id=this.ID, selObj;
  this.Text=String(theText);
  var ww=this.BorderWidth;
  if (_nvl(this.BorderWidth,"")=="") ww=0;
  selObj=_nvl(this.Action,"");
  if (selObj!="") selObj=" href='javascript:"+selObj+"'";
  var tt="";
  while (tt.length<this.Text.length) tt=tt+" ";
  if ((tt=="")||(this.Text==tt)) tt="<img src='transparent.gif' width="+eval(this.width-2*this.BorderWidth)+" height="+eval(this.height-2*this.BorderWidth)+" border=0 alt='"+_nvl(this.TooltipText,"")+"'>";
  else tt=this.Text;
  var drawCol=(_nvl(this.DrawColor,"")=="") ? "" : "bgcolor="+this.DrawColor;
  var textCol=(_nvl(this.TextColor,"")=="") ? "" : "color:"+this.TextColor+";";
  with(_DiagramTarget.document.layers[id])
  { document.open();
    if ((_nvl(this.BorderWidth,"")!="")&&(_nvl(this.BorderColor,"")!=""))
      document.writeln("<layer style='position:absolute;left:"+ww+";top:"+ww+";'><table border="+ww+" bordercolor="+this.BorderColor+" cellpadding=0 cellspacing=0><tr><td "+drawCol+" width="+eval(this.width-ww)+" height="+eval(this.height-ww)+" align=center valign=top><a style='"+textCol+"text-decoration:none;"+_BFont+"'"+selObj+">"+tt+"</a></td></tr></table></layer>");
    else
      document.writeln("<layer style='position:absolute;left:0;top:0;'><table noborder cellpadding=0 cellspacing=0><tr><td "+drawCol+" width="+this.width+" height="+this.height+" align=center valign=top><a style='"+textCol+"text-decoration:none;"+_BFont+"'"+selObj+">"+tt+"</a></td></tr></table></layer>");
    document.close();
  }
}
function Dot(theX, theY, theSize, theType, theColor, theTooltipText, theAction)
{ this.Size=theSize;
  this.ID="Dot"+_N_Dot; _N_Dot++; _zIndex++;
  this.X=theX;
  this.Y=theY;
  this.dX=Math.round(theSize/2);
  this.dY=Math.round(theSize/2);
  this.Type=theType;
  this.Color=theColor;
  this.TooltipText=theTooltipText;  
  this.Action=theAction;  
  this.SetVisibility=_SetVisibility;
  this.SetColor=_SetDotColor;
  this.SetTitle=_SetDotTitle;
  this.MoveTo=_DotMoveTo;
  this.Delete=_Delete;
  var selObj=_nvl(theAction,"");
  if (selObj!="") selObj=" href='javascript:"+selObj+"'";
  _DiagramTarget.document.writeln("<layer id='"+this.ID+"' left="+Math.round(theX-this.Size/2)+" top="+Math.round(theY-this.Size/2)+" z-index="+_zIndex+">");
  if (isNaN(theType))
  {  var cc=(_nvl(theColor,"")=="") ? "" : " bgcolor="+theColor;
    _DiagramTarget.document.writeln("<layer left=0 top=0><table noborder cellpadding=0 cellspacing=0><tr><td"+cc+"><a"+selObj+"><img src='"+theType+"' width="+this.Size+" height="+this.Size+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></dt></tr></table></layer>");
  }
  else
  { if (theType%6==0)
    { _DiagramTarget.document.writeln("<layer left=1 top="+Math.round(this.Size/4+0.3)+"><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+theColor+"><a"+selObj+"><img src='transparent.gif' width="+eval(this.Size-1)+" height="+eval(this.Size+1-2*Math.round(this.Size/4+0.3))+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></dt></tr></table></layer>");
      _DiagramTarget.document.writeln("<layer left="+Math.round(this.Size/4+0.3)+" top=1><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+theColor+"><a"+selObj+"><img src='transparent.gif' width="+eval(this.Size+1-2*Math.round(this.Size/4+0.3))+" height="+eval(this.Size-1)+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></dt></tr></table></layer>");
    }
    if (theType%6==1)
    { _DiagramTarget.document.writeln("<layer left="+Math.round(this.Size/2-this.Size/8)+" top=0><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+theColor+"><a"+selObj+"><img src='transparent.gif' width="+Math.round(this.Size/4)+" height="+this.Size+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></dt></tr></table></layer>");
      _DiagramTarget.document.writeln("<layer left=0 top="+Math.round(this.Size/2-this.Size/8)+"><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+theColor+"><a"+selObj+"><img src='transparent.gif' width="+this.Size+" height="+Math.round(this.Size/4)+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></dt></tr></table></layer>");
    }
    if (theType%6==2)
      _DiagramTarget.document.writeln("<layer left=0 top=0><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+theColor+"><a"+selObj+"><img src='transparent.gif' width="+this.Size+" height="+this.Size+" border=0 align=top valign=left></a></dt></tr></table></layer>");
    if (theType%6==3)
    { _DiagramTarget.document.writeln("<layer left=0 top="+Math.round(this.Size/4)+"><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+theColor+"><a"+selObj+"><img src='transparent.gif' width="+this.Size+" height="+Math.round(this.Size/2)+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></dt></tr></table></layer>");
      _DiagramTarget.document.writeln("<layer left="+Math.round(this.Size/2-this.Size/8)+" top=0><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+theColor+"><a"+selObj+"><img src='transparent.gif' width="+Math.round(this.Size/4)+" height="+this.Size+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></dt></tr></table></layer>");
    }
    if (theType%6==4)
    { _DiagramTarget.document.writeln("<layer left=0 top="+Math.round(this.Size/2-this.Size/8)+"><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+theColor+"><a"+selObj+"><img src='transparent.gif' width="+this.Size+" height="+Math.round(this.Size/4)+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></dt></tr></table></layer>");
      _DiagramTarget.document.writeln("<layer left="+Math.round(this.Size/4)+" top=0><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+theColor+"><a"+selObj+"><img src='transparent.gif' width="+Math.round(this.Size/2)+" height="+this.Size+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></dt></tr></table></layer>");
    }
    if (theType%6==5)
      _DiagramTarget.document.writeln("<layer left="+Math.round(1+this.Size/12)+" top="+Math.round(1+this.Size/12)+"><a"+selObj+" style='color:"+theColor+"'><img src='transparent.gif' border="+Math.round(this.Size/6)+" width="+Math.round(this.Size-this.Size/3)+" height="+Math.round(this.Size-this.Size/3)+" align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></layer>");
  }
  _DiagramTarget.document.writeln("</layer>");
  return(this);
}
function _SetDotColor(theColor)
{ if (theColor!="") this.Color=theColor;
  var selObj=_nvl(this.Action,"");
  if (selObj!="") selObj=" href='javascript:"+selObj+"'";
  with(_DiagramTarget.document.layers[this.ID])
  { document.open();
    if (isNaN(this.Type))
    { var cc=(_nvl(this.Color,"")=="") ? "" : " bgcolor="+this.Color;
      document.writeln("<layer left=0 top=0><table noborder cellpadding=0 cellspacing=0><tr><td"+cc+"><a"+selObj+"><img src='"+theType+"' width="+this.Size+" height="+this.Size+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></dt></tr></table></layer>");
    }
    else   
    { if (this.Type%6==0)
      { document.writeln("<layer left=1 top="+Math.round(this.Size/4+0.3)+"><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+this.Color+"><a"+selObj+"><img src='transparent.gif' width="+eval(this.Size-1)+" height="+eval(this.Size+1-2*Math.round(this.Size/4+0.3))+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></dt></tr></table></layer>");
        document.writeln("<layer left="+Math.round(this.Size/4+0.3)+" top=1><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+this.Color+"><a"+selObj+"><img src='transparent.gif' width="+eval(this.Size+1-2*Math.round(this.Size/4+0.3))+" height="+eval(this.Size-1)+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></dt></tr></table></layer>");
      }
      if (this.Type%6==1)
      { document.writeln("<layer left="+Math.round(this.Size/2-this.Size/8)+" top=0><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+this.Color+"><a"+selObj+"><img src='transparent.gif' width="+Math.round(this.Size/4)+" height="+this.Size+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></dt></tr></table></layer>");
        document.writeln("<layer left=0 top="+Math.round(this.Size/2-this.Size/8)+"><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+this.Color+"><a"+selObj+"><img src='transparent.gif' width="+this.Size+" height="+Math.round(this.Size/4)+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></dt></tr></table></layer>");
      }
      if (this.Type%6==2)
        document.writeln("<layer left=0 top=0><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+this.Color+"><a"+selObj+"><img src='transparent.gif' width="+this.Size+" height="+this.Size+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></dt></tr></table></layer>");
      if (this.Type%6==3)
      { document.writeln("<layer left=0 top="+Math.round(this.Size/4)+"><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+this.Color+"><a"+selObj+"><img src='transparent.gif' width="+this.Size+" height="+Math.round(this.Size/2)+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></dt></tr></table></layer>");
        document.writeln("<layer left="+Math.round(this.Size/2-this.Size/8)+" top=0><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+this.Color+"><a"+selObj+"><img src='transparent.gif' width="+Math.round(this.Size/4)+" height="+this.Size+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></dt></tr></table></layer>");
      }
      if (this.Type%6==4)
      { document.writeln("<layer left=0 top="+Math.round(this.Size/2-this.Size/8)+"><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+this.Color+"><a"+selObj+"><img src='transparent.gif' width="+this.Size+" height="+Math.round(this.Size/4)+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></dt></tr></table></layer>");
        document.writeln("<layer left="+Math.round(this.Size/4)+" top=0><table noborder cellpadding=0 cellspacing=0><tr><td bgcolor="+this.Color+"><a"+selObj+"><img src='transparent.gif' width="+Math.round(this.Size/2)+" height="+this.Size+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></dt></tr></table></layer>");
      }
      if (this.Type%6==5)
        document.writeln("<layer left="+Math.round(1+this.Size/12)+" top="+Math.round(1+this.Size/12)+"><a"+selObj+" style='color:"+this.Color+"'><img src='transparent.gif' border="+Math.round(this.Size/6)+" width="+Math.round(this.Size-this.Size/3)+" height="+Math.round(this.Size-this.Size/3)+" align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></layer>");
    }
    document.close();
  }
}
function _SetDotTitle(theTitle)
{ this.TooltipText=theTitle;
  this.SetColor("");
}
function _DotMoveTo(theX, theY)
{ var id=this.ID, selObj;
  if (theX!="") this.X=theX;
  if (theY!="") this.Y=theY;
  with(_DiagramTarget.document.layers[id])
  { if (theX!="") left=eval(theX-this.dX);
    if (theY!="") top=eval(theY-this.dY);
    visibility="show";
  }
}
function Pixel(theX, theY, theColor)
{ this.ID="Pix"+_N_Pix; _N_Pix++; _zIndex++;
  this.left=theX;
  this.top=theY;
  this.dX=2;
  this.dY=2;
  this.Color=theColor;
  this.SetVisibility=_SetVisibility;
  this.SetColor=_SetPixelColor;
  this.MoveTo=_DotMoveTo;
  this.Delete=_Delete;
  _DiagramTarget.document.writeln("<layer id='"+this.ID+"' left="+eval(theX+this.dX)+" top="+eval(theY+this.dY)+" z-Index="+_zIndex+"><layer left=0 top=0 width=1 height=2 bgcolor="+theColor+"><img src='transparent.gif' width=1 height=2></layer></layer>");
  return(this);
}
function _SetPixelColor(theColor)
{ this.Color=theColor;
  with(_DiagramTarget.document.layers[this.ID])
  { document.open();
    document.writeln("<layer left=0 top=0 width=1 height=2 bgcolor="+theColor+"><img src='transparent.gif' width=1 height=2></layer>");
    document.close();
  }
}
function _SetVisibility(isVisible)
{ var ll, id=this.ID, selObj;
  with(_DiagramTarget.document.layers[id])
  { if (isVisible) visibility="show";
    else visibility="hide";
  }
}
function _SetTitle(theTitle)
{ this.TooltipText=theTitle;
  if (this.ResizeTo) this.ResizeTo("","","","");
}
function _MoveTo(theLeft, theTop)
{ var id=this.ID, selObj;
  if (theLeft!="") this.left=theLeft;
  if (theTop!="") this.top=theTop;
  with(_DiagramTarget.document.layers[id])
  { if (theLeft!="") left=theLeft;
    if (theTop!="") top=theTop;
    visibility="show";
  }
}
function _ResizeTo(theLeft, theTop, theWidth, theHeight)
{ var id=this.ID, selObj;
  if (theLeft!="") this.left=theLeft;
  if (theTop!="") this.top=theTop;
  if (theWidth!="") this.width=theWidth;
  if (theHeight!="") this.height=theHeight;
  var ww=this.BorderWidth;
  if (_nvl(this.BorderWidth,"")=="") ww=0;
  selObj=_nvl(this.Action,"");
  if (selObj!="") selObj=" href='javascript:"+selObj+"'";
  var tt="";
  while (tt.length<this.Text.length) tt=tt+" ";
  if ((tt=="")||(tt==this.Text)) tt="";
  else tt=this.Text;
  var drawCol=(_nvl(this.DrawColor,"")=="") ? "" : "bgcolor="+this.DrawColor;
  var textCol=(_nvl(this.TextColor,"")=="") ? "" : "color:"+this.TextColor+";";
  with(_DiagramTarget.document.layers[id])
  { top=this.top;
    left=this.left;
    document.open();
    if ((_nvl(this.BorderWidth,"")!="")&&(_nvl(this.BorderColor,"")!=""))
      document.writeln("<layer style='position:absolute;left:"+ww+";top:"+ww+";'><table border="+ww+" bordercolor="+this.BorderColor+" cellpadding=0 cellspacing=0><tr><td "+drawCol+" width="+eval(this.width-ww)+" height="+eval(this.height-ww)+" align=center valign=top><a style='"+textCol+"text-decoration:none;"+_BFont+"'"+selObj+">"+tt+"</a></td></tr></table></layer>");
    else
      document.writeln("<layer style='position:absolute;left:0;top:0;'><table noborder cellpadding=0 cellspacing=0><tr><td "+drawCol+" width="+this.width+" height="+this.height+" align=center valign=top><a style='"+textCol+"text-decoration:none;"+_BFont+"'"+selObj+">"+tt+"</a></td></tr></table></layer>");
    document.close(); 
  }
}
function _Delete()
{ var id=this.ID, selObj;
  with(_DiagramTarget.document.layers[id])
  { document.open();
    document.close();
  }
}
function _SetColor(theColor)
{ this.Color=theColor;
  if ((theColor!="")&&(theColor.length<this.Color.length)) this.Color="#"+theColor;
  else this.Color=theColor;
  this.ResizeTo("", "", "", "");
}
//You can delete the following 3 functions, if you do not use Line objects
function Line(theX0, theY0, theX1, theY1, theColor, theSize, theTooltipText, theAction)
{ this.ID="Line"+_N_Line; _N_Line++; _zIndex++;
  this.X0=theX0;
  this.Y0=theY0;
  this.X1=theX1;
  this.Y1=theY1;
  this.Color=theColor;
  if ((theColor!="")&&(theColor.length==6)) this.Color="#"+theColor;
  this.Size=Number(_nvl(theSize,1));
  this.TooltipText=theTooltipText;
  this.Action=theAction;
  this.SetVisibility=_SetVisibility;
  this.SetColor=_SetColor;  
  this.SetTitle=_SetTitle;
  this.MoveTo=_LineMoveTo;
  this.ResizeTo=_LineResizeTo;
  this.Delete=_Delete;
  var xx0, yy0, xx1, yy1, ll, rr, tt, bb, ww, hh, ccl, ccr, cct, ccb;
  var ss2=Math.floor(this.Size/2);
  var ddir=(((this.Y1>this.Y0)&&(this.X1>this.X0))||((this.Y1<this.Y0)&&(this.X1<this.X0))) ? true : false;
  if (theX0<=theX1) { ll=theX0; rr=theX1; }
  else { ll=theX1; rr=theX0; }
  if (theY0<=theY1) { tt=theY0; bb=theY1; }
  else { tt=theY1; bb=theY0; }
  ww=rr-ll; hh=bb-tt;
  var selObj=_nvl(theAction,"");
  if (selObj!="") selObj=" href='javascript:"+selObj+"'";
  _DiagramTarget.document.writeln("<layer left="+eval(ll-ss2)+" top="+eval(tt-ss2)+" id='"+this.ID+"' z-Index="+_zIndex+">");
  if ((ww==0)||(hh==0))
    _DiagramTarget.document.writeln("<layer left=2 top=2 width="+eval(ww+this.Size)+" height="+eval(hh+this.Size)+" bgcolor="+this.Color+"><a"+selObj+"><img src='transparent.gif' width="+eval(ww+this.Size)+" height="+eval(hh+this.Size)+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></layer>");
  else
  { if (ww>hh)
    { ccr=0;
      cct=0;
      while (ccr<ww)
      { ccl=ccr;
        while ((Math.round(ccr*hh/ww)==cct)&&(ccr<=ww)) ccr++;
        if (ddir)
          _DiagramTarget.document.writeln("<layer left="+eval(ccl+2)+" top="+eval(cct+2)+" width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" bgcolor="+this.Color+">");
        else
          _DiagramTarget.document.writeln("<layer left="+eval(ww-ccr+2)+" top="+eval(cct+2)+" width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" bgcolor="+this.Color+">");
        _DiagramTarget.document.writeln("<a"+selObj+"><img src='transparent.gif' width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></layer>");
        cct++;
      }
    }
    else
    { ccb=0;
      ccl=0;
      while (ccb<hh)
      { cct=ccb;
        while ((Math.round(ccb*ww/hh)==ccl)&&(ccb<hh)) ccb++;
        if (ddir)
          _DiagramTarget.document.writeln("<layer left="+eval(ccl+2)+" top="+eval(cct+2)+" width="+this.Size+" height="+eval(ccb-cct+this.Size)+" bgcolor="+this.Color+">");
        else
          _DiagramTarget.document.writeln("<layer left="+eval(ww-ccl+2)+" top="+eval(cct+2)+" width="+this.Size+" height="+eval(ccb-cct+this.Size)+" bgcolor="+this.Color+">");
        _DiagramTarget.document.writeln("<a"+selObj+"><img src='transparent.gif' width="+this.Size+" height="+eval(ccb-cct+this.Size)+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></layer>");
        ccl++;
      }
    }
  }           
  _DiagramTarget.document.writeln("</layer>");
  return(this);
}
function _LineResizeTo(theX0, theY0, theX1, theY1)
{ var xx0, yy0, xx1, yy1, ll, rr, tt, bb, ww, hh, ccl, ccr, cct, ccb, id=this.ID,lay=0,selObj="",divtext="";
  var ss2=Math.floor(this.Size/2);
  if (theX0!="") this.X0=theX0;
  if (theY0!="") this.Y0=theY0;
  if (theX1!="") this.X1=theX1;
  if (theY1!="") this.Y1=theY1;
  var ddir=(((this.Y1>this.Y0)&&(this.X1>this.X0))||((this.Y1<this.Y0)&&(this.X1<this.X0))) ? true : false;
  if (this.X0<=this.X1) { ll=this.X0; rr=this.X1; }
  else { ll=this.X1; rr=this.X0; }
  if (this.Y0<=this.Y1) { tt=this.Y0; bb=this.Y1; }
  else { tt=this.Y1; bb=this.Y0; }
  ww=rr-ll; hh=bb-tt;
  selObj=_nvl(this.Action,"");
  if (selObj!="") selObj=" href='javascript:"+selObj+"'";
  with(_DiagramTarget.document.layers[id])
  { top=tt-ss2;
    left=ll-ss2;
    document.open();
    if ((ww==0)||(hh==0))
      document.writeln("<layer left=2 top=2 width="+eval(ww+this.Size)+" height="+eval(hh+this.Size)+" bgcolor="+this.Color+"><a"+selObj+"><img src='transparent.gif' width="+eval(ww+this.Size)+" height="+eval(hh+this.Size)+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></layer>");
    else
    { if (ww>hh)
      { ccr=0;
        cct=0;
        while (ccr<ww)
        { ccl=ccr;
          while ((Math.round(ccr*hh/ww)==cct)&&(ccr<=ww)) ccr++;
          if (ddir)
            document.writeln("<layer left="+eval(ccl+2)+" top="+eval(cct+2)+" width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" bgcolor="+this.Color+">");
          else
            document.writeln("<layer left="+eval(ww-ccr+2)+" top="+eval(cct+2)+" width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" bgcolor="+this.Color+">");
          document.writeln("<a"+selObj+"><img src='transparent.gif' width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></layer>");
          cct++;
        }
      }
      else
      { ccb=0;
        ccl=0;
        while (ccb<hh)
        { cct=ccb;
          while ((Math.round(ccb*ww/hh)==ccl)&&(ccb<hh)) ccb++;
          if (ddir)
            document.writeln("<layer left="+eval(ccl+2)+" top="+eval(cct+2)+" width="+this.Size+" height="+eval(ccb-cct+this.Size)+" bgcolor="+this.Color+">");
          else
            document.writeln("<layer left="+eval(ww-ccl+2)+" top="+eval(cct+2)+" width="+this.Size+" height="+eval(ccb-cct+this.Size)+" bgcolor="+this.Color+">");
          document.writeln("<a"+selObj+"><img src='transparent.gif' width="+this.Size+" height="+eval(ccb-cct+this.Size)+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></layer>");
          ccl++;
        }
      }
    }
    document.close();
  }            
}
function _LineMoveTo(theLeft, theTop)
{ var id=this.ID, selObj;
  var ss2=Math.floor(this.Size/2);
  if (theLeft!="") this.left=theLeft;
  if (theTop!="") this.top=theTop;
  with(_DiagramTarget.document.layers[id])
  { if (theLeft!="") left=theLeft-ss2;
    if (theTop!="") top=theTop-ss2;
    visibility="show";
  }
}
//You can delete the following 2 functions, if you do not use Area objects
function Area(theX0, theY0, theX1, theY1, theColor, theBase, theTooltipText, theAction)
{ this.ID="Area"+_N_Area; _N_Area++; _zIndex++;
  this.X0=theX0;
  this.Y0=theY0;
  this.X1=theX1;
  this.Y1=theY1;
  this.Color=theColor;
  this.Base=theBase;
  this.TooltipText=theTooltipText;
  this.Action=theAction;
  this.SetVisibility=_SetVisibility;
  this.SetColor=_SetColor;  
  this.SetTitle=_SetTitle;
  this.MoveTo=_MoveTo;
  this.ResizeTo=_AreaResizeTo;
  this.Delete=_Delete;
  var dd, ll, rr, tt, bb, ww, hh;
  if (theX0<=theX1) { ll=theX0; rr=theX1; }
  else { ll=theX1; rr=theX0; }
  if (theY0<=theY1) { tt=theY0; bb=theY1; }
  else { tt=theY1; bb=theY0; }
  ww=rr-ll; hh=bb-tt;
  var selObj=_nvl(theAction,"");
  if (selObj!="") selObj=" href='javascript:"+selObj+"'";
  if (theBase<=tt)
    _DiagramTarget.document.writeln("<layer left="+ll+" top="+theBase+" id='"+this.ID+"' z-index="+_zIndex+">");
  else
    _DiagramTarget.document.writeln("<layer left="+ll+" top="+tt+" id='"+this.ID+"' z-index="+_zIndex+">");
  if (theBase<=tt)
  { if ((theBase<tt)&&(ww>0))
      _DiagramTarget.document.writeln("<layer left=2 top=2><a"+selObj+"><img src='o_"+theColor+".gif' width="+ww+" height="+eval(tt-theBase)+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></layer>");
    if (((theY0<theY1)&&(theX0<theX1))||((theY0>theY1)&&(theX0>theX1)))
      _DiagramTarget.document.writeln("<layer left=2 top="+eval(tt-theBase+2)+"><a"+selObj+"><img src='q_"+theColor+".gif' width="+ww+" height="+hh+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></layer>");
    if (((theY0>theY1)&&(theX0<theX1))||((theY0<theY1)&&(theX0>theX1)))
      _DiagramTarget.document.writeln("<layer left=2 top="+eval(tt-theBase+2)+"><a"+selObj+"><img src='p_"+theColor+".gif' width="+ww+" height="+hh+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></layer>");
  }
  if ((theBase>tt)&&(theBase<bb))
  { dd=Math.round((theBase-tt)/hh*ww);
    if (((theY0<theY1)&&(theX0<theX1))||((theY0>theY1)&&(theX0>theX1)))
    { _DiagramTarget.document.writeln("<layer left=2 top=2><a"+selObj+"><img src='b_"+theColor+".gif' width="+dd+" height="+eval(theBase-tt)+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></layer>");
      _DiagramTarget.document.writeln("<layer left="+eval(dd+2)+" top="+eval(theBase-tt+2)+"><a"+selObj+"><img src='q_"+theColor+".gif' width="+eval(ww-dd)+" height="+eval(bb-theBase)+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></layer>");
    }
    if (((theY0>theY1)&&(theX0<theX1))||((theY0<theY1)&&(theX0>theX1)))
    { _DiagramTarget.document.writeln("<layer left=2 top="+eval(theBase-tt+2)+"><a"+selObj+"><img src='p_"+theColor+".gif' width="+eval(ww-dd)+" height="+eval(bb-theBase)+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></layer>");
      _DiagramTarget.document.writeln("<layer left="+eval(ww-dd+2)+" top=2><a"+selObj+"><img src='d_"+theColor+".gif' width="+dd+" height="+eval(theBase-tt)+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></layer>");
    }
  }
  if (theBase>=bb)
  { if ((theBase>bb)&&(ww>0))
      _DiagramTarget.document.writeln("<layer left=2 top="+eval(hh+2)+"><a"+selObj+"><img src='o_"+theColor+".gif' width="+ww+" height="+eval(theBase-bb)+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></layer>");
    if (((theY0<theY1)&&(theX0<theX1))||((theY0>theY1)&&(theX0>theX1)))
      _DiagramTarget.document.writeln("<layer left=2 top=2><a"+selObj+"><img src='b_"+theColor+".gif' width="+ww+" height="+hh+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></layer>");
    if (((theY0>theY1)&&(theX0<theX1))||((theY0<theY1)&&(theX0>theX1)))
      _DiagramTarget.document.writeln("<layer left=2 top=2><a"+selObj+"><img src='d_"+theColor+".gif' width="+ww+" height="+hh+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></layer>");
  }
  _DiagramTarget.document.writeln("</layer>");
}
function _AreaResizeTo(theX0, theY0, theX1, theY1)
{ var dd, ll, rr, tt, bb, ww, hh, id=this.ID,lay=0,selObj="",divtext="";
  if (theX0!="") this.X0=theX0;
  if (theY0!="") this.Y0=theY0;
  if (theX1!="") this.X1=theX1;
  if (theY1!="") this.Y1=theY1;
  if (this.X0<=this.X1) { ll=this.X0; rr=this.X1; }
  else { ll=this.X1; rr=this.X0; }
  if (this.Y0<=this.Y1) { tt=this.Y0; bb=this.Y1; }
  else { tt=this.Y1; bb=this.Y0; }
  ww=rr-ll; hh=bb-tt;
  selObj=_nvl(this.Action,"");
  if (selObj!="") selObj=" href='javascript:"+selObj+"'";
  with(_DiagramTarget.document.layers[id])
  { if (this.Base<=tt) { left=ll; top=this.Base; }
    else { left=ll; top=tt; }
    document.open();
    if (this.Base<=tt)
    { if ((this.Base<tt)&&(ww>0))
        document.writeln("<layer left=2 top=2><a"+selObj+"><img src='o_"+this.Color+".gif' width="+ww+" height="+eval(tt-this.Base)+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></layer>");
      if (((this.Y0<this.Y1)&&(this.X0<this.X1))||((this.Y0>this.Y1)&&(this.X0>this.X1)))
        document.writeln("<layer left=2 top="+eval(tt-this.Base+2)+"><a"+selObj+"><img src='q_"+this.Color+".gif' width="+ww+" height="+hh+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></layer>");
      if (((this.Y0>this.Y1)&&(this.X0<this.X1))||((this.Y0<this.Y1)&&(this.X0>this.X1)))
        document.writeln("<layer left=2 top="+eval(tt-this.Base+2)+"><a"+selObj+"><img src='p_"+this.Color+".gif' width="+ww+" height="+hh+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></layer>");
    }
    if ((this.Base>tt)&&(this.Base<bb))
    { dd=Math.round((this.Base-tt)/hh*ww);
      if (((this.Y0<this.Y1)&&(this.X0<this.X1))||((this.Y0>this.Y1)&&(this.X0>this.X1)))
      { document.writeln("<layer left=2 top=2><a"+selObj+"><img src='b_"+this.Color+".gif' width="+dd+" height="+eval(this.Base-tt)+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></layer>");
        document.writeln("<layer left="+eval(dd+2)+" top="+eval(this.Base-tt+2)+"><a"+selObj+"><img src='q_"+this.Color+".gif' width="+eval(ww-dd)+" height="+eval(bb-this.Base)+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></layer>");
      }
      if (((this.Y0>this.Y1)&&(this.X0<this.X1))||((this.Y0<this.Y1)&&(this.X0>this.X1)))
      { document.writeln("<layer left=2 top="+eval(this.Base-tt+2)+"><a"+selObj+"><img src='p_"+this.Color+".gif' width="+eval(ww-dd)+" height="+eval(bb-this.Base)+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></layer>");
        document.writeln("<layer left="+eval(ww-dd+2)+" top=2><a"+selObj+"><img src='d_"+this.Color+".gif' width="+dd+" height="+eval(this.Base-tt)+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></layer>");
      }
    }
    if (this.Base>=bb)
    { if ((this.Base>bb)&&(ww>0))
        document.writeln("<layer left=2 top="+eval(hh+2)+"><a"+selObj+"><img src='o_"+this.Color+".gif' width="+ww+" height="+eval(this.Base-bb)+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></layer>");
      if (((this.Y0<this.Y1)&&(this.X0<this.X1))||((this.Y0>this.Y1)&&(this.X0>this.X1)))
        document.writeln("<layer left=2 top=2><a"+selObj+"><img src='b_"+this.Color+".gif' width="+ww+" height="+hh+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></layer>");
      if (((this.Y0>this.Y1)&&(this.X0<this.X1))||((this.Y0<this.Y1)&&(this.X0>this.X1)))
        document.writeln("<layer left=2 top=2><a"+selObj+"><img src='d_"+this.Color+".gif' width="+ww+" height="+hh+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></layer>");
    }
    document.close();
  }  
}
//You can delete the following 3 functions, if you do not use Arrow objects
function Arrow(theX0, theY0, theX1, theY1, theColor, theSize, theTooltipText, theAction)
{ this.ID="Arrow"+_N_Arrow; _N_Arrow++; _zIndex++;
  this.X0=theX0;
  this.Y0=theY0;
  this.X1=theX1;
  this.Y1=theY1;
  this.Color=theColor;
  if ((theColor!="")&&(theColor.length==6)) this.Color="#"+theColor;
  this.Size=Number(_nvl(theSize,1));
  this.TooltipText=theTooltipText;
  this.Action=theAction;
  this.Border=8*this.Size;  
  this.SetVisibility=_SetVisibility;
  this.SetColor=_SetColor;  
  this.SetTitle=_SetTitle;
  this.MoveTo=_ArrowMoveTo;
  this.ResizeTo=_ArrowResizeTo;
  this.Delete=_Delete;
  var xx0, yy0, xx1, yy1, ll, rr, tt, bb, ww, hh, ccl, ccr, cct, ccb;
  var ss2=Math.floor(this.Size/2);
  var ddir=(((this.Y1>this.Y0)&&(this.X1>this.X0))||((this.Y1<this.Y0)&&(this.X1<this.X0))) ? true : false;
  if (theX0<=theX1) { ll=theX0; rr=theX1; }
  else { ll=theX1; rr=theX0; }
  if (theY0<=theY1) { tt=theY0; bb=theY1; }
  else { tt=theY1; bb=theY0; }
  ww=rr-ll; hh=bb-tt;
  var selObj=_nvl(theAction,"");
  if (selObj!="") selObj=" href='javascript:"+selObj+"'";
  _DiagramTarget.document.writeln("<layer left="+eval(ll-ss2-this.Border)+" top="+eval(tt-ss2-this.Border)+" id='"+this.ID+"' z-Index="+_zIndex+">");
  if ((ww==0)||(hh==0))
    _DiagramTarget.document.writeln("<layer left="+eval(2+this.Border)+" top="+eval(2+this.Border)+" width="+eval(ww+this.Size)+" height="+eval(hh+this.Size)+" bgcolor="+this.Color+"><a"+selObj+"><img src='transparent.gif' width="+eval(ww+this.Size)+" height="+eval(hh+this.Size)+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></layer>");
  else
  { if (ww>hh)
    { ccr=0;
      cct=0;
      while (ccr<ww)
      { ccl=ccr;
        while ((Math.round(ccr*hh/ww)==cct)&&(ccr<=ww)) ccr++;
        if (ddir)
          _DiagramTarget.document.writeln("<layer left="+eval(ccl+2+this.Border)+" top="+eval(cct+2+this.Border)+" width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" bgcolor="+this.Color+">");
        else
          _DiagramTarget.document.writeln("<layer left="+eval(ww-ccr+2+this.Border)+" top="+eval(cct+2+this.Border)+" width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" bgcolor="+this.Color+">");
        _DiagramTarget.document.writeln("<a"+selObj+"><img src='transparent.gif' width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></layer>");
        cct++;
      }
    }
    else
    { ccb=0;
      ccl=0;
      while (ccb<hh)
      { cct=ccb;
        while ((Math.round(ccb*ww/hh)==ccl)&&(ccb<hh)) ccb++;
        if (ddir)
          _DiagramTarget.document.writeln("<layer left="+eval(ccl+2+this.Border)+" top="+eval(cct+2+this.Border)+" width="+this.Size+" height="+eval(ccb-cct+this.Size)+" bgcolor="+this.Color+">");
        else
          _DiagramTarget.document.writeln("<layer left="+eval(ww-ccl+2+this.Border)+" top="+eval(cct+2+this.Border)+" width="+this.Size+" height="+eval(ccb-cct+this.Size)+" bgcolor="+this.Color+">");
        _DiagramTarget.document.writeln("<a"+selObj+"><img src='transparent.gif' width="+this.Size+" height="+eval(ccb-cct+this.Size)+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></layer>");
        ccl++;
      }
    }
  }
  var LL=1, ll0=ll, tt0=tt;
  var ccL=8*theSize+4, ccB=2*theSize+1;
  var DDX=theX1-theX0, DDY=theY1-theY0;
  if ((DDX!=0)||(DDY!=0)) LL=Math.sqrt((DDX*DDX)+(DDY*DDY));
  this.X0=theX1-Math.round(1/LL*(ccL*DDX-ccB*DDY));
  this.Y0=theY1-Math.round(1/LL*(ccL*DDY+ccB*DDX));
  ddir=(((this.Y1>this.Y0)&&(this.X1>this.X0))||((this.Y1<this.Y0)&&(this.X1<this.X0))) ? true : false;
  if (this.X0<=this.X1) { ll=this.X0; rr=this.X1; }
  else { ll=this.X1; rr=this.X0; }
  if (this.Y0<=this.Y1) { tt=this.Y0; bb=this.Y1; }
  else { tt=this.Y1; bb=this.Y0; }
  ww=rr-ll; hh=bb-tt;
  if ((ww==0)||(hh==0))
    _DiagramTarget.document.writeln("<layer left="+eval(2+this.Border+ll-ll0)+" top="+eval(2+this.Border+tt-tt0)+" width="+eval(ww+this.Size)+" height="+eval(hh+this.Size)+" bgcolor="+this.Color+"><a"+selObj+"><img src='transparent.gif' width="+eval(ww+this.Size)+" height="+eval(hh+this.Size)+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></layer>");
  else
  { if (ww>hh)
    { ccr=0;
      cct=0;
      while (ccr<ww)
      { ccl=ccr;
        while ((Math.round(ccr*hh/ww)==cct)&&(ccr<=ww)) ccr++;
        if (ddir)
          _DiagramTarget.document.writeln("<layer left="+eval(ccl+2+this.Border+ll-ll0)+" top="+eval(cct+2+this.Border+tt-tt0)+" width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" bgcolor="+this.Color+">");
        else
          _DiagramTarget.document.writeln("<layer left="+eval(ww-ccr+2+this.Border+ll-ll0)+" top="+eval(cct+2+this.Border+tt-tt0)+" width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" bgcolor="+this.Color+">");
        _DiagramTarget.document.writeln("<a"+selObj+"><img src='transparent.gif' width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></layer>");
        cct++;
      }
    }
    else
    { ccb=0;
      ccl=0;
      while (ccb<hh)
      { cct=ccb;
        while ((Math.round(ccb*ww/hh)==ccl)&&(ccb<hh)) ccb++;
        if (ddir)
          _DiagramTarget.document.writeln("<layer left="+eval(ccl+2+this.Border+ll-ll0)+" top="+eval(cct+2+this.Border+tt-tt0)+" width="+this.Size+" height="+eval(ccb-cct+this.Size)+" bgcolor="+this.Color+">");
        else
          _DiagramTarget.document.writeln("<layer left="+eval(ww-ccl+2+this.Border+ll-ll0)+" top="+eval(cct+2+this.Border+tt-tt0)+" width="+this.Size+" height="+eval(ccb-cct+this.Size)+" bgcolor="+this.Color+">");
        _DiagramTarget.document.writeln("<a"+selObj+"><img src='transparent.gif' width="+this.Size+" height="+eval(ccb-cct+this.Size)+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></layer>");
        ccl++;
      }
    }
  }
  this.X0=theX1-Math.round(1/LL*(ccL*DDX+ccB*DDY));
  this.Y0=theY1-Math.round(1/LL*(ccL*DDY-ccB*DDX));
  ddir=(((this.Y1>this.Y0)&&(this.X1>this.X0))||((this.Y1<this.Y0)&&(this.X1<this.X0))) ? true : false;
  if (this.X0<=this.X1) { ll=this.X0; rr=this.X1; }
  else { ll=this.X1; rr=this.X0; }
  if (this.Y0<=this.Y1) { tt=this.Y0; bb=this.Y1; }
  else { tt=this.Y1; bb=this.Y0; }
  ww=rr-ll; hh=bb-tt;
  if ((ww==0)||(hh==0))
    _DiagramTarget.document.writeln("<layer left="+eval(2+this.Border+ll-ll0)+" top="+eval(2+this.Border+tt-tt0)+" width="+eval(ww+this.Size)+" height="+eval(hh+this.Size)+" bgcolor="+this.Color+"><a"+selObj+"><img src='transparent.gif' width="+eval(ww+this.Size)+" height="+eval(hh+this.Size)+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></layer>");
  else
  { if (ww>hh)
    { ccr=0;
      cct=0;
      while (ccr<ww)
      { ccl=ccr;
        while ((Math.round(ccr*hh/ww)==cct)&&(ccr<=ww)) ccr++;
        if (ddir)
          _DiagramTarget.document.writeln("<layer left="+eval(ccl+2+this.Border+ll-ll0)+" top="+eval(cct+2+this.Border+tt-tt0)+" width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" bgcolor="+this.Color+">");
        else
          _DiagramTarget.document.writeln("<layer left="+eval(ww-ccr+2+this.Border+ll-ll0)+" top="+eval(cct+2+this.Border+tt-tt0)+" width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" bgcolor="+this.Color+">");
        _DiagramTarget.document.writeln("<a"+selObj+"><img src='transparent.gif' width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></layer>");
        cct++;
      }
    }
    else
    { ccb=0;
      ccl=0;
      while (ccb<hh)
      { cct=ccb;
        while ((Math.round(ccb*ww/hh)==ccl)&&(ccb<hh)) ccb++;
        if (ddir)
          _DiagramTarget.document.writeln("<layer left="+eval(ccl+2+this.Border+ll-ll0)+" top="+eval(cct+2+this.Border+tt-tt0)+" width="+this.Size+" height="+eval(ccb-cct+this.Size)+" bgcolor="+this.Color+">");
        else
          _DiagramTarget.document.writeln("<layer left="+eval(ww-ccl+2+this.Border+ll-ll0)+" top="+eval(cct+2+this.Border+tt-tt0)+" width="+this.Size+" height="+eval(ccb-cct+this.Size)+" bgcolor="+this.Color+">");
        _DiagramTarget.document.writeln("<a"+selObj+"><img src='transparent.gif' width="+this.Size+" height="+eval(ccb-cct+this.Size)+" border=0 align=top valign=left alt='"+_nvl(theTooltipText,"")+"'></a></layer>");
        ccl++;
      }
    }
  }
  _DiagramTarget.document.writeln("</layer>");
  this.X0=theX0;
  this.Y0=theY0;
  return(this);
}
function _ArrowResizeTo(theX0, theY0, theX1, theY1)
{ var xx0, yy0, xx1, yy1, ll, rr, tt, bb, ww, hh, ccl, ccr, cct, ccb, id=this.ID,lay=0,selObj="",divtext="";
  var ss2=Math.floor(this.Size/2);
  if (theX0!="") this.X0=theX0;
  if (theY0!="") this.Y0=theY0;
  if (theX1!="") this.X1=theX1;
  if (theY1!="") this.Y1=theY1;
  var tmpX0=this.X0, tmpY0=this.Y0;  
  var ddir=(((this.Y1>this.Y0)&&(this.X1>this.X0))||((this.Y1<this.Y0)&&(this.X1<this.X0))) ? true : false;
  if (this.X0<=this.X1) { ll=this.X0; rr=this.X1; }
  else { ll=this.X1; rr=this.X0; }
  if (this.Y0<=this.Y1) { tt=this.Y0; bb=this.Y1; }
  else { tt=this.Y1; bb=this.Y0; }
  ww=rr-ll; hh=bb-tt;
  selObj=_nvl(this.Action,"");
  if (selObj!="") selObj=" href='javascript:"+selObj+"'";
  with(_DiagramTarget.document.layers[id])
  { top=tt-ss2-this.Border;
    left=ll-ss2-this.Border;
    document.open();
    if ((ww==0)||(hh==0))
      document.writeln("<layer left="+eval(2+this.Border)+" top="+eval(2+this.Border)+" width="+eval(ww+this.Size)+" height="+eval(hh+this.Size)+" bgcolor="+this.Color+"><a"+selObj+"><img src='transparent.gif' width="+eval(ww+this.Size)+" height="+eval(hh+this.Size)+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></layer>");
    else
    { if (ww>hh)
      { ccr=0;
        cct=0;
        while (ccr<ww)
        { ccl=ccr;
          while ((Math.round(ccr*hh/ww)==cct)&&(ccr<=ww)) ccr++;
          if (ddir)
            document.writeln("<layer left="+eval(ccl+2+this.Border)+" top="+eval(cct+2+this.Border)+" width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" bgcolor="+this.Color+">");
          else
            document.writeln("<layer left="+eval(ww-ccr+2+this.Border)+" top="+eval(cct+2+this.Border)+" width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" bgcolor="+this.Color+">");
          document.writeln("<a"+selObj+"><img src='transparent.gif' width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></layer>");
          cct++;
        }
      }
      else
      { ccb=0;
        ccl=0;
        while (ccb<hh)
        { cct=ccb;
          while ((Math.round(ccb*ww/hh)==ccl)&&(ccb<hh)) ccb++;
          if (ddir)
            document.writeln("<layer left="+eval(ccl+2+this.Border)+" top="+eval(cct+2+this.Border)+" width="+this.Size+" height="+eval(ccb-cct+this.Size)+" bgcolor="+this.Color+">");
          else
            document.writeln("<layer left="+eval(ww-ccl+2+this.Border)+" top="+eval(cct+2+this.Border)+" width="+this.Size+" height="+eval(ccb-cct+this.Size)+" bgcolor="+this.Color+">");
          document.writeln("<a"+selObj+"><img src='transparent.gif' width="+this.Size+" height="+eval(ccb-cct+this.Size)+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></layer>");
          ccl++;
        }
      }
    }  
    var LL=1, ll0=ll, tt0=tt;
    var ccL=8*this.Size+4, ccB=2*this.Size+1;
    var DDX=this.X1-tmpX0, DDY=this.Y1-tmpY0;
    if ((DDX!=0)||(DDY!=0)) LL=Math.sqrt(0+(DDX*DDX)+(DDY*DDY));
    this.X0=this.X1-Math.round(1/LL*(ccL*DDX-ccB*DDY));
    this.Y0=this.Y1-Math.round(1/LL*(ccL*DDY+ccB*DDX));
    ddir=(((this.Y1>this.Y0)&&(this.X1>this.X0))||((this.Y1<this.Y0)&&(this.X1<this.X0))) ? true : false;
    if (this.X0<=this.X1) { ll=this.X0; rr=this.X1; }
    else { ll=this.X1; rr=this.X0; }
    if (this.Y0<=this.Y1) { tt=this.Y0; bb=this.Y1; }
    else { tt=this.Y1; bb=this.Y0; }
    ww=rr-ll; hh=bb-tt;
    if ((ww==0)||(hh==0))
      document.writeln("<layer left="+eval(2+this.Border+ll-ll0)+" top="+eval(2+this.Border+tt-tt0)+" width="+eval(ww+this.Size)+" height="+eval(hh+this.Size)+" bgcolor="+this.Color+"><a"+selObj+"><img src='transparent.gif' width="+eval(ww+this.Size)+" height="+eval(hh+this.Size)+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></layer>");
    else
    { if (ww>hh)
      { ccr=0;
        cct=0;
        while (ccr<ww)
        { ccl=ccr;
          while ((Math.round(ccr*hh/ww)==cct)&&(ccr<=ww)) ccr++;
          if (ddir)
            document.writeln("<layer left="+eval(ccl+2+this.Border+ll-ll0)+" top="+eval(cct+2+this.Border+tt-tt0)+" width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" bgcolor="+this.Color+">");
          else
            document.writeln("<layer left="+eval(ww-ccr+2+this.Border+ll-ll0)+" top="+eval(cct+2+this.Border+tt-tt0)+" width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" bgcolor="+this.Color+">");
          document.writeln("<a"+selObj+"><img src='transparent.gif' width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></layer>");
          cct++;
        }
      }
      else
      { ccb=0;
        ccl=0;
        while (ccb<hh)
        { cct=ccb;
          while ((Math.round(ccb*ww/hh)==ccl)&&(ccb<hh)) ccb++;
          if (ddir)
            document.writeln("<layer left="+eval(ccl+2+this.Border+ll-ll0)+" top="+eval(cct+2+this.Border+tt-tt0)+" width="+this.Size+" height="+eval(ccb-cct+this.Size)+" bgcolor="+this.Color+">");
          else
            document.writeln("<layer left="+eval(ww-ccl+2+this.Border+ll-ll0)+" top="+eval(cct+2+this.Border+tt-tt0)+" width="+this.Size+" height="+eval(ccb-cct+this.Size)+" bgcolor="+this.Color+">");
          document.writeln("<a"+selObj+"><img src='transparent.gif' width="+this.Size+" height="+eval(ccb-cct+this.Size)+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></layer>");
          ccl++;
        }
      }
    }  
    this.X0=this.X1-Math.round(1/LL*(ccL*DDX+ccB*DDY));
    this.Y0=this.Y1-Math.round(1/LL*(ccL*DDY-ccB*DDX));
    ddir=(((this.Y1>this.Y0)&&(this.X1>this.X0))||((this.Y1<this.Y0)&&(this.X1<this.X0))) ? true : false;
    if (this.X0<=this.X1) { ll=this.X0; rr=this.X1; }
    else { ll=this.X1; rr=this.X0; }
    if (this.Y0<=this.Y1) { tt=this.Y0; bb=this.Y1; }
    else { tt=this.Y1; bb=this.Y0; }
    ww=rr-ll; hh=bb-tt;
    if ((ww==0)||(hh==0))
      document.writeln("<layer left="+eval(2+this.Border+ll-ll0)+" top="+eval(2+this.Border+tt-tt0)+" width="+eval(ww+this.Size)+" height="+eval(hh+this.Size)+" bgcolor="+this.Color+"><a"+selObj+"><img src='transparent.gif' width="+eval(ww+this.Size)+" height="+eval(hh+this.Size)+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></layer>");
    else
    { if (ww>hh)
      { ccr=0;
        cct=0;
        while (ccr<ww)
        { ccl=ccr;
          while ((Math.round(ccr*hh/ww)==cct)&&(ccr<=ww)) ccr++;
          if (ddir)
            document.writeln("<layer left="+eval(ccl+2+this.Border+ll-ll0)+" top="+eval(cct+2+this.Border+tt-tt0)+" width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" bgcolor="+this.Color+">");
          else
            document.writeln("<layer left="+eval(ww-ccr+2+this.Border+ll-ll0)+" top="+eval(cct+2+this.Border+tt-tt0)+" width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" bgcolor="+this.Color+">");
          document.writeln("<a"+selObj+"><img src='transparent.gif' width="+eval(ccr-ccl+this.Size)+" height="+this.Size+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></layer>");
          cct++;
        }
      }
      else
      { ccb=0;
        ccl=0;
        while (ccb<hh)
        { cct=ccb;
          while ((Math.round(ccb*ww/hh)==ccl)&&(ccb<hh)) ccb++;
          if (ddir)
            document.writeln("<layer left="+eval(ccl+2+this.Border+ll-ll0)+" top="+eval(cct+2+this.Border+tt-tt0)+" width="+this.Size+" height="+eval(ccb-cct+this.Size)+" bgcolor="+this.Color+">");
          else
            document.writeln("<layer left="+eval(ww-ccl+2+this.Border+ll-ll0)+" top="+eval(cct+2+this.Border+tt-tt0)+" width="+this.Size+" height="+eval(ccb-cct+this.Size)+" bgcolor="+this.Color+">");
          document.writeln("<a"+selObj+"><img src='transparent.gif' width="+this.Size+" height="+eval(ccb-cct+this.Size)+" border=0 align=top valign=left alt='"+_nvl(this.TooltipText,"")+"'></a></layer>");
          ccl++;
        }
      }
    }
    document.close();
  }
  this.X0=tmpX0;
  this.Y0=tmpY0;       
}
function _ArrowMoveTo(theLeft, theTop)
{ var id=this.ID, selObj;
  var ss2=Math.floor(this.Size/2);
  if (theLeft!="") this.left=theLeft;
  if (theTop!="") this.top=theTop;
  with(_DiagramTarget.document.layers[id])
  { if (theLeft!="") left=theLeft-ss2-this.Border;
    if (theTop!="") top=theTop-ss2-this.Border;
    visibility="show";
  }
}