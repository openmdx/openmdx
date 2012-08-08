// JavaScript Diagram Builder 3.01
// Copyright (c) 2001-2004 Lutz Tautenhahn, all rights reserved.
//
// The Author grants you a non-exclusive, royalty free, license to use,
// modify and redistribute this software, provided that this copyright notice
// and license appear on all copies of the software.
// This software is provided "as is", without a warranty of any kind.

function _Draw(theDrawColor, theTextColor, isScaleText, theTooltipText, theAction, theGridColor, theSubGridColor)
{ var x0,y0,i,j,itext,l,x,y,r,u,fn,dx,dy,xr,yr,invdifx,invdify,deltax,deltay,id=this.ID,lay=0,selObj="",divtext="",ii=0,oo,k,sub;
  var c151="&#151;", nbsp=(_IE)? "&nbsp;" : "";
  if (_nvl(theGridColor,"")!="") this.GridColor=theGridColor;
  if (_nvl(theSubGridColor,"")!="") this.SubGridColor=theSubGridColor;
  lay--; 
  if (document.all) selObj=eval("_DiagramTarget.document.all."+id);
  else selObj=_DiagramTarget.document.getElementById(id);
  if (selObj) lay--;
  if (lay<-1)
    selObj.title=_nvl(theTooltipText,"");
  else
    _DiagramTarget.document.writeln("<div id='"+this.ID+"' title='"+_nvl(theTooltipText,"")+"'>"); 
  divtext="<div id='"+this.ID+"i"+eval(ii++)+"' onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute; left:"+eval(this.left)+"px; width:"+eval(this.right-this.left+_dSize)+"px; top:"+eval(this.top)+"px; height:"+eval(this.bottom-this.top+_dSize)+"px; background-color:"+theDrawColor+"; color:"+theTextColor+"; border-style:solid; border-width:1px; z-index:"+this.zIndex+"'>&nbsp;</div>";
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
        if (this.SubGridColor)
        { for (k=1; k<this.SubGrids; k++)
          { if ((x0-k*sub>this.left+1)&&(x0-k*sub<this.right-1))
              divtext+="<div id='"+this.ID+"i"+eval(ii++)+"' style='position:absolute; left:"+Math.round(x0-k*sub)+"px; top:"+eval(this.top+1)+"px; z-index:"+this.zIndex+"; width:1px; height:"+eval(this.bottom-this.top-1)+"px; background-color:"+this.SubGridColor+"; font-size:1pt'></div>";
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
          divtext+="<div id='"+this.ID+"i"+eval(ii++)+"' align=center style='position:absolute; left:"+eval(x0-50)+"px; width:102px; top:"+eval(this.bottom+8)+"px; color:"+theTextColor+";"+this.Font+" z-index:"+this.zIndex+"'>"+l+"</div>";
          divtext+="<div id='"+this.ID+"i"+eval(ii++)+"' style='position:absolute; left:"+x0+"px; top:"+eval(this.bottom-5)+"px; z-index:"+this.zIndex+"; width:1px; height:12px; background-color:"+theTextColor+"; font-size:1pt'></div>";
          if ((this.GridColor)&&(x0>this.left)&&(x0<this.right)) divtext+="<div id='"+this.ID+"i"+eval(ii++)+"' style='position:absolute; left:"+x0+"px; top:"+eval(this.top+1)+"px; z-index:"+this.zIndex+"; width:1px; height:"+eval(this.bottom-this.top-1)+"px; background-color:"+this.GridColor+"; font-size:1pt'></div>";
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
        if (this.SubGridColor)
        { for (k=1; k<this.SubGrids; k++)
          { if ((x0-k*sub>this.left+1)&&(x0-k*sub<this.right-1))
              divtext+="<div id='"+this.ID+"i"+eval(ii++)+"' style='position:absolute; left:"+Math.round(x0-k*sub)+"px; top:"+eval(this.top+1)+"px; z-index:"+this.zIndex+"; width:1px; height:"+eval(this.bottom-this.top-1)+"px; background-color:"+this.SubGridColor+"; font-size:1pt'></div>";
          }
        }  
        if ((x0>=this.left)&&(x0<=this.right))
        { itext++;
          if ((itext!=2)||(!isScaleText)) l=_DateFormat(xr, Math.abs(deltax), this.XScale);
          else l=this.xtext;
          divtext+="<div id='"+this.ID+"i"+eval(ii++)+"' align=center style='position:absolute; left:"+eval(x0-50)+"px; width:102px; top:"+eval(this.bottom+8)+"px; color:"+theTextColor+";"+this.Font+" z-index:"+this.zIndex+"'>"+l+"</div>";
          divtext+="<div id='"+this.ID+"i"+eval(ii++)+"' style='position:absolute; left:"+x0+"px; top:"+eval(this.bottom-5)+"px; z-index:"+this.zIndex+"; width:1px; height:12px; background-color:"+theTextColor+"; font-size:1pt'></div>";
          if ((this.GridColor)&&(x0>this.left)&&(x0<this.right)) divtext+="<div id='"+this.ID+"i"+eval(ii++)+"' style='position:absolute; left:"+x0+"px; top:"+eval(this.top+1)+"px; z-index:"+this.zIndex+"; width:1px; height:"+eval(this.bottom-this.top-1)+"px; background-color:"+this.GridColor+"; font-size:1pt'></div>";
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
        if (this.SubGridColor)
        { for (k=1; k<this.SubGrids; k++)
          { if ((y0+k*sub>this.top+1)&&(y0+k*sub<this.bottom-1))
              divtext+="<div id='"+this.ID+"i"+eval(ii++)+"' style='position:absolute; left:"+eval(this.left+1)+"px; top:"+Math.round(y0+k*sub)+"px; z-index:"+this.zIndex+"; height:1px; width:"+eval(this.right-this.left-1)+"px; background-color:"+this.SubGridColor+"; font-size:1pt;line-height:1pt'>"+nbsp+"</div>";
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
          divtext+="<div id='"+this.ID+"i"+eval(ii++)+"' align=right style='position:absolute; left:"+eval(this.left-100)+"px; width:89px; top:"+eval(y0-9)+"px; color:"+theTextColor+";"+this.Font+" z-index:"+this.zIndex+"'>"+l+"</div>";
          divtext+="<div id='"+this.ID+"i"+eval(ii++)+"' style='position:absolute; left:"+eval(this.left-5)+"px; top:"+eval(y0)+"px; z-index:"+this.zIndex+"; height:1px; width:11px; background-color:"+theTextColor+"; font-size:1pt;line-height:1pt'>"+nbsp+"</div>";
          if ((this.GridColor)&&(y0>this.top)&&(y0<this.bottom)) divtext+="<div id='"+this.ID+"i"+eval(ii++)+"' style='position:absolute; left:"+eval(this.left+1)+"px; top:"+eval(y0)+"px; z-index:"+this.zIndex+"; height:1px; width:"+eval(this.right-this.left-1)+"px; background-color:"+this.GridColor+"; font-size:1pt;line-height:1pt'>"+nbsp+"</div>";
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
        if (this.SubGridColor)
        { for (k=1; k<this.SubGrids; k++)
          { if ((y0+k*sub>this.top+1)&&(y0+k*sub<this.bottom-1))
              divtext+="<div id='"+this.ID+"i"+eval(ii++)+"' style='position:absolute; left:"+eval(this.left+1)+"px; top:"+Math.round(y0+k*sub)+"px; z-index:"+this.zIndex+"; height:1px; width:"+eval(this.right-this.left-1)+"px; background-color:"+this.SubGridColor+"; font-size:1pt;line-height:1pt'>"+nbsp+"</div>";
          }
        }
        if ((y0>=this.top)&&(y0<=this.bottom))
        { itext++;
          if ((itext!=2)||(!isScaleText)) l=_DateFormat(yr, Math.abs(deltay), this.YScale);
          else l=this.ytext;
          divtext+="<div id='"+this.ID+"i"+eval(ii++)+"' align=right style='position:absolute; left:"+eval(this.left-100)+"px; width:89px; top:"+eval(y0-9)+"px; color:"+theTextColor+";"+this.Font+" z-index:"+this.zIndex+"'>"+l+"</div>";
          divtext+="<div id='"+this.ID+"i"+eval(ii++)+"' style='position:absolute; left:"+eval(this.left-5)+"px; top:"+eval(y0)+"px; z-index:"+this.zIndex+"; height:1px; width:11px; background-color:"+theTextColor+"; font-size:1pt;line-height:1pt'>"+nbsp+"</div>";
          if ((this.GridColor)&&(y0>this.top)&&(y0<this.bottom)) divtext+="<div id='"+this.ID+"i"+eval(ii++)+"' style='position:absolute; left:"+eval(this.left+1)+"px; top:"+eval(y0)+"px; z-index:"+this.zIndex+"; height:1px; width:"+eval(this.right-this.left-1)+"px; background-color:"+this.GridColor+"; font-size:1pt;line-height:1pt'>"+nbsp+"</div>";
        }
      }
    }
  }
  divtext+="<div id='"+this.ID+"i"+eval(ii++)+"' align=center onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute; left:"+this.left+"px; width:"+eval(this.right-this.left)+"px; top:"+eval(this.top-20)+"px; color:"+theTextColor+";"+this.Font+" z-index:"+this.zIndex+"'>"+this.title+"</div>";
  if (lay<-1)
    selObj.innerHTML=divtext;
  else
    _DiagramTarget.document.writeln(divtext+"</div>");
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
  this.BorderWidth="";
  this.BorderColor="";
  this.Action=theAction;
  this.SetVisibility=_SetVisibility;
  this.SetColor=_SetBarColor;
  this.SetText=_SetBarText;
  this.SetTitle=_SetTitle;
  this.MoveTo=_MoveTo;
  this.ResizeTo=_ResizeTo;
  this.Delete=_Delete;
  if (_nvl(theText,"")!="") _DiagramTarget.document.writeln("<div id='"+this.ID+"' onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+theLeft+"px;top:"+theTop+"px;width:"+eval(theRight-theLeft)+"px;height:"+eval(theBottom-theTop)+"px;background-color:"+theDrawColor+";color:"+theTextColor+";"+_BFont+"z-index:"+_zIndex+"'; title='"+_nvl(theTooltipText,"")+"' align=center>"+theText+"</div>");
  else _DiagramTarget.document.writeln("<div id='"+this.ID+"' onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+theLeft+"px;top:"+theTop+"px;width:"+eval(theRight-theLeft)+"px;height:"+eval(theBottom-theTop)+"px;background-color:"+theDrawColor+";font-size:1pt;line-height:1pt;font-family:Verdana;font-weight:normal;z-index:"+_zIndex+"'; title='"+_nvl(theTooltipText,"")+"'>&nbsp;</div>");
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
  this.SetTitle=_SetTitle;
  this.MoveTo=_MoveTo;
  this.ResizeTo=_ResizeTo;
  this.Delete=_Delete;
  var bb="";
  var ww=theBorderWidth;
  if (_nvl(theBorderWidth,"")=="") ww=0;
  if ((_nvl(theBorderWidth,"")!="")&&(_nvl(theBorderColor,"")!=""))
    bb="border-style:solid;border-width:"+theBorderWidth+"px;border-color:"+theBorderColor+";";
  if (_nvl(theText,"")!="")
       _DiagramTarget.document.writeln("<div id='"+this.ID+"' onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+theLeft+"px;top:"+theTop+"px;width:"+eval(theRight-theLeft+ww*_dSize)+"px;height:"+eval(theBottom-theTop+ww*_dSize)+"px;"+bb+"background-color:"+theDrawColor+";color:"+theTextColor+";"+_BFont+"z-index:"+_zIndex+"'; title='"+_nvl(theTooltipText,"")+"' align=center>"+theText+"</div>");
  else _DiagramTarget.document.writeln("<div id='"+this.ID+"' onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+theLeft+"px;top:"+theTop+"px;width:"+eval(theRight-theLeft+ww*_dSize)+"px;height:"+eval(theBottom-theTop+ww*_dSize)+"px;"+bb+"background-color:"+theDrawColor+";font-size:1pt;line-height:1pt;font-family:Verdana;font-weight:normal;z-index:"+_zIndex+"'; title='"+_nvl(theTooltipText,"")+"'>&nbsp;</div>");
  return(this);
}
function _SetBarColor(theColor)
{ var id=this.ID, selObj;
  this.DrawColor=theColor;
  if (document.all) selObj=eval("_DiagramTarget.document.all."+id);
  else selObj=_DiagramTarget.document.getElementById(id);
  selObj.style.backgroundColor=theColor;
}
function _SetBarText(theText)
{ var id=this.ID, selObj;
  this.Text=String(theText);
  if (document.all) selObj=eval("_DiagramTarget.document.all."+id);
  else selObj=_DiagramTarget.document.getElementById(id);
  selObj.innerHTML=theText;
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
  this.Action=theAction;
  this.SetVisibility=_SetVisibility;
  this.SetColor=_SetDotColor;
  this.SetTitle=_SetTitle;
  this.MoveTo=_DotMoveTo;
  this.Delete=_Delete;
  _DiagramTarget.document.write("<div id='"+this.ID+"' onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+Math.round(theX-this.Size/2)+"px;top:"+Math.round(theY-this.Size/2)+"px; width:"+this.Size+"px; height:"+this.Size+"px; z-index:"+_zIndex+"' title='"+_nvl(theTooltipText,"")+"'>");
  if (isNaN(theType))
  { _DiagramTarget.document.write("<div style='position:absolute;left:0px;top:0px;width:"+this.Size+"px;height:"+this.Size+"px;background-color:"+theColor+";font-size:1pt;line-height:1pt;'>");
    _DiagramTarget.document.write("<img src='"+theType+"' width="+this.Size+"px height="+this.Size+"px style='vertical-align:bottom'></div>");
  }
  else
  { if (theType%6==0)
    { _DiagramTarget.document.write("<div style='position:absolute;left:1px;top:"+Math.round(this.Size/4+0.3)+"px;width:"+eval(this.Size-1)+"px;height:"+eval(this.Size+1-2*Math.round(this.Size/4+0.3))+"px;background-color:"+theColor+";font-size:1pt'></div>");
      _DiagramTarget.document.write("<div style='position:absolute;left:"+Math.round(this.Size/4+0.3)+"px;top:1px;width:"+eval(this.Size+1-2*Math.round(this.Size/4+0.3))+"px;height:"+eval(this.Size-1)+"px;background-color:"+theColor+";font-size:1pt'></div>");
    }
    if (theType%6==1)
    { _DiagramTarget.document.write("<div style='position:absolute;left:"+Math.round(this.Size/2-this.Size/8)+"px;top:0px;width:"+Math.round(this.Size/4)+"px;height:"+this.Size+"px;background-color:"+theColor+";font-size:1pt'></div>");
      _DiagramTarget.document.write("<div style='position:absolute;left:0px;top:"+Math.round(this.Size/2-this.Size/8)+"px;width:"+this.Size+"px;height:"+Math.round(this.Size/4)+"px;background-color:"+theColor+";font-size:1pt'></div>");
    }
    if (theType%6==2)
      _DiagramTarget.document.write("<div style='position:absolute;left:0px;top:0px;width:"+this.Size+"px;height:"+this.Size+"px;background-color:"+theColor+";font-size:1pt'></div>");
    if (theType%6==3)
    { _DiagramTarget.document.write("<div style='position:absolute;left:0px;top:"+Math.round(this.Size/4)+"px;width:"+this.Size+"px;height:"+Math.round(this.Size/2)+"px;background-color:"+theColor+";font-size:1pt'></div>");
      _DiagramTarget.document.write("<div style='position:absolute;left:"+Math.round(this.Size/2-this.Size/8)+"px;top:0px;width:"+Math.round(this.Size/4)+"px;height:"+this.Size+"px;background-color:"+theColor+";font-size:1pt'></div>");
    }
    if (theType%6==4)
    { _DiagramTarget.document.write("<div style='position:absolute;left:"+Math.round(this.Size/4)+"px;top:0px;width:"+Math.round(this.Size/2)+"px;height:"+this.Size+"px;background-color:"+theColor+";font-size:1pt'></div>");
      _DiagramTarget.document.write("<div style='position:absolute;left:0px;top:"+Math.round(this.Size/2-this.Size/8)+"px;width:"+this.Size+"px;height:"+Math.round(this.Size/4)+"px;background-color:"+theColor+";font-size:1pt'></div>");
    }
    if (theType%6==5)
    { if (_dSize==1)
        _DiagramTarget.document.write("<div style='position:absolute;left:0px;top:0px;width:"+this.Size+"px;height:"+this.Size+"px;border-width:"+Math.round(this.Size/6)+"px; border-style:solid;border-color:"+theColor+";font-size:1pt;line-height:1pt'></div>");
      else
        _DiagramTarget.document.write("<div style='position:absolute;left:0px;top:0px;width:"+Math.round(this.Size-this.Size/4)+"px;height:"+Math.round(this.Size-this.Size/4)+"px;border-width:"+Math.round(this.Size/6)+"px; border-style:solid;border-color:"+theColor+";font-size:1pt;line-height:1pt'></div>");
    }
  }      
  _DiagramTarget.document.writeln("</div>");
  return(this);
}
function _SetDotColor(theColor)
{ this.Color=theColor;
  var tt="", selObj;
  if (document.all) selObj=eval("_DiagramTarget.document.all."+this.ID);
  else selObj=_DiagramTarget.document.getElementById(this.ID);
  if (isNaN(this.Type))
  { tt+="<div style='position:absolute;left:0px;top:0px;width:"+this.Size+"px;height:"+this.Size+"px;background-color:"+theColor+";font-size:1pt;line-height:1pt;'>";
    tt+="<img src='"+theType+"' width="+this.Size+"px height="+this.Size+"px style='vertical-align:bottom'></div>";
  } 
  else
  { if (this.Type%6==0)
    { tt+="<div style='position:absolute;left:1px;top:"+Math.round(this.Size/4+0.3)+"px;width:"+eval(this.Size-1)+"px;height:"+eval(this.Size+1-2*Math.round(this.Size/4+0.3))+"px;background-color:"+theColor+";font-size:1pt'></div>";
      tt+="<div style='position:absolute;left:"+Math.round(this.Size/4+0.3)+"px;top:1px;width:"+eval(this.Size+1-2*Math.round(this.Size/4+0.3))+"px;height:"+eval(this.Size-1)+"px;background-color:"+theColor+";font-size:1pt'></div>";
    }
    if (this.Type%6==1)
    { tt+="<div style='position:absolute;left:"+Math.round(this.Size/2-this.Size/8)+"px;top:0px;width:"+Math.round(this.Size/4)+"px;height:"+this.Size+"px;background-color:"+theColor+";font-size:1pt'></div>";
      tt+="<div style='position:absolute;left:0px;top:"+Math.round(this.Size/2-this.Size/8)+"px;width:"+this.Size+"px;height:"+Math.round(this.Size/4)+"px;background-color:"+theColor+";font-size:1pt'></div>";
    }
    if (this.Type%6==2)
      tt+="<div style='position:absolute;left:0px;top:0px;width:"+this.Size+"px;height:"+this.Size+"px;background-color:"+theColor+";font-size:1pt'></div>";
    if (this.Type%6==3)
    { tt+="<div style='position:absolute;left:0px;top:"+Math.round(this.Size/4)+"px;width:"+this.Size+"px;height:"+Math.round(this.Size/2)+"px;background-color:"+theColor+";font-size:1pt'></div>";
      tt+="<div style='position:absolute;left:"+Math.round(this.Size/2-this.Size/8)+"px;top:0px;width:"+Math.round(this.Size/4)+"px;height:"+this.Size+"px;background-color:"+theColor+";font-size:1pt'></div>";
    }
    if (this.Type%6==4)
    { tt+="<div style='position:absolute;left:"+Math.round(this.Size/4)+"px;top:0px;width:"+Math.round(this.Size/2)+"px;height:"+this.Size+"px;background-color:"+theColor+";font-size:1pt'></div>";
      tt+="<div style='position:absolute;left:0px;top:"+Math.round(this.Size/2-this.Size/8)+"px;width:"+this.Size+"px;height:"+Math.round(this.Size/4)+"px;background-color:"+theColor+";font-size:1pt'></div>";
    }
    if (this.Type%6==5)
    { if (_dSize==1)
        tt+="<div style='position:absolute;left:0px;top:0px;width:"+this.Size+"px;height:"+this.Size+"px;border-width:"+Math.round(this.Size/6)+"px; border-style:solid;border-color:"+theColor+";font-size:1pt;line-height:1pt'></div>";
      else
        tt+="<div style='position:absolute;left:0px;top:0px;width:"+Math.round(this.Size-this.Size/4)+"px;height:"+Math.round(this.Size-this.Size/4)+"px;border-width:"+Math.round(this.Size/6)+"px; border-style:solid;border-color:"+theColor+";font-size:1pt;line-height:1pt'></div>";
    }
  }  
  selObj.innerHTML=tt;
}
function _DotMoveTo(theX, theY)
{ var id=this.ID, selObj;
  if (theX!="") this.X=theX;
  if (theY!="") this.Y=theY;
  if (document.all) selObj=eval("_DiagramTarget.document.all."+id);
  else selObj=_DiagramTarget.document.getElementById(id);
  with (selObj.style)
  { if (theX!="") left=eval(theX-this.dX)+"px";
    if (theY!="") top=eval(theY-this.dY)+"px";
    visibility="visible";
  }
}
function Pixel(theX, theY, theColor)
{ this.ID="Pix"+_N_Pix; _N_Pix++; _zIndex++;
  this.left=theX;
  this.top=theY;
  this.dX=0;
  this.dY=0;
  this.Color=theColor;
  this.SetVisibility=_SetVisibility;
  this.SetColor=_SetPixelColor;  
  this.MoveTo=_DotMoveTo;
  this.Delete=_Delete;
  _DiagramTarget.document.writeln("<div id='"+this.ID+"' style='position:absolute;left:"+eval(theX-this.dX)+"px;top:"+eval(theY-this.dY)+"px;width:1px;height:2px;background-color:"+theColor+";font-size:1pt;z-index:"+_zIndex+"'></div>");
  return(this);
}
function _SetPixelColor(theColor)
{ var id=this.ID, selObj;
  this.Color=theColor;
  if (document.all) selObj=eval("_DiagramTarget.document.all."+id);
  else selObj=_DiagramTarget.document.getElementById(id);
  selObj.style.backgroundColor=theColor;
}
function _SetVisibility(isVisible)
{ var ll, id=this.ID, selObj;
  if (document.all)
  { selObj=eval("_DiagramTarget.document.all."+id);
    if (isVisible) selObj.style.visibility="visible";
    else selObj.style.visibility="hidden";
  }
  else
  { selObj=_DiagramTarget.document.getElementById(id);
    if (isVisible) selObj.style.visibility="visible";
    else selObj.style.visibility="hidden";
    if (id.substr(0,3)=='Dia')
    { var ii=0;
      selObj=_DiagramTarget.document.getElementById(id+'i'+eval(ii++));
      while (selObj!=null)
      { if (isVisible) selObj.style.visibility="visible";
        else selObj.style.visibility="hidden";
        selObj=_DiagramTarget.document.getElementById(id+'i'+eval(ii++));
      }
    }
  }
}
function _SetTitle(theTitle)
{ var id=this.ID, selObj;
  if (document.all) selObj=eval("_DiagramTarget.document.all."+id);
  else selObj=_DiagramTarget.document.getElementById(id);
  selObj.title=theTitle;
}
function _MoveTo(theLeft, theTop)
{ var id=this.ID, selObj;
  if (theLeft!="") this.left=theLeft;
  if (theTop!="") this.top=theTop;
  if (document.all) selObj=eval("_DiagramTarget.document.all."+id);
  else selObj=_DiagramTarget.document.getElementById(id);
  with (selObj.style)
  { if (theLeft!="") left=theLeft+"px";
    if (theTop!="") top=theTop+"px";
    visibility="visible";
  }
}
function _ResizeTo(theLeft, theTop, theWidth, theHeight)
{ var id=this.ID, selObj;
  if (theLeft!="") this.left=theLeft;
  if (theTop!="") this.top=theTop;
  if (theWidth!="") this.width=theWidth;
  if (theHeight!="") this.height=theHeight;
  if (document.all) selObj=eval("_DiagramTarget.document.all."+id);
  else selObj=_DiagramTarget.document.getElementById(id);
  with (selObj.style)
  { if (theLeft!="") left=theLeft+"px";
    if (theTop!="") top=theTop+"px";
    if (theWidth!="") width=theWidth+"px";
    if (theHeight!="") height=theHeight+"px";
    visibility="visible";
  }
}
function _Delete()
{ var id=this.ID, selObj;
  if (document.all)
  { selObj=eval("_DiagramTarget.document.all."+id);
    selObj.outerHTML="";
  }
  else
  { selObj=_DiagramTarget.document.getElementById(id); 
    selObj.parentNode.removeChild(selObj);
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
  this.Action=theAction;
  this.SetVisibility=_SetVisibility;
  this.SetColor=_SetColor;
  this.SetTitle=_SetTitle;
  this.MoveTo=_LineMoveTo;
  this.ResizeTo=_LineResizeTo;
  this.Delete=_Delete;
  var xx0, yy0, xx1, yy1, ll, rr, tt, bb, ww, hh, ccl, ccr, cct, ccb;
  var ss2=Math.floor(this.Size/2), nbsp="";//(_IE)? "&nbsp;" : "";
  var ddir=(((this.Y1>this.Y0)&&(this.X1>this.X0))||((this.Y1<this.Y0)&&(this.X1<this.X0))) ? true : false;
  if (theX0<=theX1) { ll=theX0; rr=theX1; }
  else { ll=theX1; rr=theX0; }
  if (theY0<=theY1) { tt=theY0; bb=theY1; }
  else { tt=theY1; bb=theY0; }
  ww=rr-ll; hh=bb-tt;
  _DiagramTarget.document.write("<div id='"+this.ID+"' style='position:absolute;left:"+eval(ll-ss2)+"px;top:"+eval(tt-ss2)+"px; width:"+eval(ww+this.Size)+"px; height:"+eval(hh+this.Size)+"px; z-index:"+_zIndex+";' title='"+_nvl(theTooltipText,"")+"'>");
  if ((ww==0)||(hh==0))
    _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:0px;top:0px;width:"+eval(ww+this.Size)+"px;height:"+eval(hh+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>");
  else
  { if (ww>hh)
    { ccr=0;
      cct=0;
      while (ccr<ww)
      { ccl=ccr;
        while ((Math.round(ccr*hh/ww)==cct)&&(ccr<=ww)) ccr++;
        if (ddir)
          _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+ccl+"px;top:"+cct+"px;width:"+eval(ccr-ccl+this.Size)+"px;height:"+this.Size+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>");
        else
          _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+eval(ww-ccr)+"px;top:"+cct+"px;width:"+eval(ccr-ccl+this.Size)+"px;height:"+this.Size+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>");
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
          _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+ccl+"px;top:"+cct+"px;width:"+this.Size+"px;height:"+eval(ccb-cct+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>");
        else
          _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+eval(ww-ccl)+"px;top:"+cct+"px;width:"+this.Size+"px;height:"+eval(ccb-cct+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>");
        ccl++;
      }
    }
  }           
  _DiagramTarget.document.writeln("</div>");
  return(this);
}
function _LineResizeTo(theX0, theY0, theX1, theY1)
{ var xx0, yy0, xx1, yy1, ll, rr, tt, bb, ww, hh, ccl, ccr, cct, ccb, id=this.ID,lay=0,selObj="",divtext="";
  var ss2=Math.floor(this.Size/2), nbsp="";//(_IE)? "&nbsp;" : "";
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
  if (document.all) selObj=eval("_DiagramTarget.document.all."+id);
  else selObj=_DiagramTarget.document.getElementById(id);
  with (selObj.style)
  { left=eval(ll-ss2)+"px";
    top=eval(tt-ss2)+"px";
    width=eval(ww+this.Size)+"px";
    height=eval(hh+this.Size)+"px";
  }
  if ((ww==0)||(hh==0))
    divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:0px;top:0px;width:"+eval(ww+this.Size)+"px;height:"+eval(hh+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>";
  else
  { if (ww>hh)
    { ccr=0;
      cct=0;
      while (ccr<ww)
      { ccl=ccr;
        while ((Math.round(ccr*hh/ww)==cct)&&(ccr<=ww)) ccr++;
        if (ddir)
          divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:"+ccl+"px;top:"+cct+"px;width:"+eval(ccr-ccl+this.Size)+"px;height:"+this.Size+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>";
        else
          divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:"+eval(ww-ccr)+"px;top:"+cct+"px;width:"+eval(ccr-ccl+this.Size)+"px;height:"+this.Size+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>";
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
          divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:"+ccl+"px;top:"+cct+"px;width:"+this.Size+"px;height:"+eval(ccb-cct+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>";
        else
          divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:"+eval(ww-ccl)+"px;top:"+cct+"px;width:"+this.Size+"px;height:"+eval(ccb-cct+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>";
        ccl++;
      }
    }
  } 
  selObj.innerHTML=divtext;
}
function _LineMoveTo(theLeft, theTop)
{ var id=this.ID, selObj;
  var ss2=Math.floor(this.Size/2);
  if (theLeft!="") this.left=theLeft;
  if (theTop!="") this.top=theTop;
  if (document.all) selObj=eval("_DiagramTarget.document.all."+id);
  else selObj=_DiagramTarget.document.getElementById(id);
  with (selObj.style)
  { if (theLeft!="") left=eval(theLeft-ss2)+"px";
    if (theTop!="") top=eval(theTop-ss2)+"px";
    visibility="visible";
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
  if (theBase<=tt)
    _DiagramTarget.document.write("<div id='"+this.ID+"' style='position:absolute;left:"+ll+"px;top:"+theBase+"px; width:"+ww+"px; height:"+hh+"px; z-index:"+_zIndex+"; font-size:1pt; line-height:1pt;' title='"+_nvl(theTooltipText,"")+"'>");
  else
    _DiagramTarget.document.write("<div id='"+this.ID+"' style='position:absolute;left:"+ll+"px;top:"+tt+"px; width:"+ww+"px; height:"+Math.max(hh, theBase-tt)+"px; z-index:"+_zIndex+"; font-size:1pt; line-height:1pt;' title='"+_nvl(theTooltipText,"")+"'>");
  if (theBase<=tt)
  { if ((theBase<tt)&&(ww>0))
      _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:0px;top:0px;font-size:1pt;line-height:1pt;'><img src='o_"+theColor+".gif' width="+ww+"px height="+eval(tt-theBase)+"px style='vertical-align:bottom'></div>");
    if (((theY0<theY1)&&(theX0<theX1))||((theY0>theY1)&&(theX0>theX1)))
      _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:0px;top:"+eval(tt-theBase)+"px;font-size:1pt;line-height:1pt;'><img src='q_"+theColor+".gif' width="+ww+"px height="+hh+"px style='vertical-align:bottom'></div>");
    if (((theY0>theY1)&&(theX0<theX1))||((theY0<theY1)&&(theX0>theX1)))
      _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:0px;top:"+eval(tt-theBase)+"px;font-size:1pt;line-height:1pt;'><img src='p_"+theColor+".gif' width="+ww+"px height="+hh+"px style='vertical-align:bottom'></div>");
  }
  if ((theBase>tt)&&(theBase<bb))
  { dd=Math.round((theBase-tt)/hh*ww);
    if (((theY0<theY1)&&(theX0<theX1))||((theY0>theY1)&&(theX0>theX1)))
    { _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:0px;top:0px;font-size:1pt;line-height:1pt;'><img src='b_"+theColor+".gif' width="+dd+"px height="+eval(theBase-tt)+"px style='vertical-align:bottom'></div>");
      _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+dd+"px;top:"+eval(theBase-tt)+"px;font-size:1pt;line-height:1pt;'><img src='q_"+theColor+".gif' width="+eval(ww-dd)+"px height="+eval(bb-theBase)+"px style='vertical-align:bottom'></div>");
    }
    if (((theY0>theY1)&&(theX0<theX1))||((theY0<theY1)&&(theX0>theX1)))
    { _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:0px;top:"+eval(theBase-tt)+"px;font-size:1pt;line-height:1pt;'><img src='p_"+theColor+".gif' width="+eval(ww-dd)+"px height="+eval(bb-theBase)+"px style='vertical-align:bottom'></div>");
      _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+eval(ww-dd)+"px;top:0px;font-size:1pt;line-height:1pt;'><img src='d_"+theColor+".gif' width="+dd+"px height="+eval(theBase-tt)+"px style='vertical-align:bottom'></div>");
    }
  }
  if (theBase>=bb)
  { if ((theBase>bb)&&(ww>0))
      _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:0px;top:"+hh+"px;font-size:1pt;line-height:1pt;'><img src='o_"+theColor+".gif' width="+ww+"px height="+eval(theBase-bb)+"px style='vertical-align:bottom'></div>");
    if (((theY0<theY1)&&(theX0<theX1))||((theY0>theY1)&&(theX0>theX1)))
      _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:0px;top:0px;font-size:1pt;line-height:1pt;'><img src='b_"+theColor+".gif' width="+ww+"px height="+hh+"px style='vertical-align:bottom'></div>");
    if (((theY0>theY1)&&(theX0<theX1))||((theY0<theY1)&&(theX0>theX1)))
      _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:0px;top:0px;font-size:1pt;line-height:1pt;'><img src='d_"+theColor+".gif' width="+ww+"px height="+hh+"px style='vertical-align:bottom'></div>");
  }
  _DiagramTarget.document.writeln("</div>");
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
  if (document.all) selObj=eval("_DiagramTarget.document.all."+id);
  else selObj=_DiagramTarget.document.getElementById(id);
  with (selObj.style)
  { if (this.Base<=tt) { left=ll+"px"; top=this.Base+"px"; width=ww+"px"; height=hh+"px"; }
    else { left=ll+"px"; top=tt+"px"; width=ww+"px"; height=Math.max(hh, this.Base-tt)+"px";}
  }
  if (this.Base<=tt)
  { if ((this.Base<tt)&&(ww>0))
      divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:0px;top:0px;font-size:1pt;line-height:1pt;'><img src='o_"+this.Color+".gif' width="+ww+"px height="+eval(tt-this.Base)+"px style='vertical-align:bottom'></div>";
    if (((this.Y0<this.Y1)&&(this.X0<this.X1))||((this.Y0>this.Y1)&&(this.X0>this.X1)))
      divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:0px;top:"+eval(tt-this.Base)+"px;font-size:1pt;line-height:1pt;'><img src='q_"+this.Color+".gif' width="+ww+"px height="+hh+"px style='vertical-align:bottom'></div>";
    if (((this.Y0>this.Y1)&&(this.X0<this.X1))||((this.Y0<this.Y1)&&(this.X0>this.X1)))
      divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:0px;top:"+eval(tt-this.Base)+"px;font-size:1pt;line-height:1pt;'><img src='p_"+this.Color+".gif' width="+ww+"px height="+hh+"px style='vertical-align:bottom'></div>";
  }
  if ((this.Base>tt)&&(this.Base<bb))
  { dd=Math.round((this.Base-tt)/hh*ww);
    if (((this.Y0<this.Y1)&&(this.X0<this.X1))||((this.Y0>this.Y1)&&(this.X0>this.X1)))
    { divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:0px;top:0px;font-size:1pt;line-height:1pt;'><img src='b_"+this.Color+".gif' width="+dd+"px height="+eval(this.Base-tt)+"px style='vertical-align:bottom'></div>";
      divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:"+dd+"px;top:"+eval(this.Base-tt)+"px;font-size:1pt;line-height:1pt;'><img src='q_"+this.Color+".gif' width="+eval(ww-dd)+"px height="+eval(bb-this.Base)+"px style='vertical-align:bottom'></div>";
    }
    if (((this.Y0>this.Y1)&&(this.X0<this.X1))||((this.Y0<this.Y1)&&(this.X0>this.X1)))
    { divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:0px;top:"+eval(this.Base-tt)+"px;font-size:1pt;line-height:1pt;'><img src='p_"+this.Color+".gif' width="+eval(ww-dd)+"px height="+eval(bb-this.Base)+"px style='vertical-align:bottom'></div>";
      divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:"+eval(ww-dd)+"px;top:0px;font-size:1pt;line-height:1pt;'><img src='d_"+this.Color+".gif' width="+dd+"px height="+eval(this.Base-tt)+"px style='vertical-align:bottom'></div>";
    }
  }
  if (this.Base>=bb)
  { if ((this.Base>bb)&&(ww>0))
      divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:0px;top:"+hh+"px;font-size:1pt;line-height:1pt;'><img src='o_"+this.Color+".gif' width="+ww+"px height="+eval(this.Base-bb)+"px style='vertical-align:bottom'></div>";
    if (((this.Y0<this.Y1)&&(this.X0<this.X1))||((this.Y0>this.Y1)&&(this.X0>this.X1)))
      divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:0px;top:0px;font-size:1pt;line-height:1pt;'><img src='b_"+this.Color+".gif' width="+ww+"px height="+hh+"px style='vertical-align:bottom'></div>";
    if (((this.Y0>this.Y1)&&(this.X0<this.X1))||((this.Y0<this.Y1)&&(this.X0>this.X1)))
      divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:0px;top:0px;font-size:1pt;line-height:1pt;'><img src='d_"+this.Color+".gif' width="+ww+"px height="+hh+"px style='vertical-align:bottom'></div>";
  }
  selObj.innerHTML=divtext;
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
  this.Action=theAction;
  this.Border=8*this.Size;
  this.SetVisibility=_SetVisibility;
  this.SetColor=_SetColor;
  this.SetTitle=_SetTitle;
  this.MoveTo=_ArrowMoveTo;
  this.ResizeTo=_ArrowResizeTo;
  this.Delete=_Delete;
  var xx0, yy0, xx1, yy1, ll, rr, tt, bb, ww, hh, ccl, ccr, cct, ccb;
  var ddir, ss2=Math.floor(this.Size/2), nbsp="";//(_IE)? "&nbsp;" : "";

  ddir=(((this.Y1>this.Y0)&&(this.X1>this.X0))||((this.Y1<this.Y0)&&(this.X1<this.X0))) ? true : false;
  if (this.X0<=this.X1) { ll=this.X0; rr=this.X1; }
  else { ll=this.X1; rr=this.X0; }
  if (this.Y0<=this.Y1) { tt=this.Y0; bb=this.Y1; }
  else { tt=this.Y1; bb=this.Y0; }
  ww=rr-ll; hh=bb-tt;
  _DiagramTarget.document.write("<div id='"+this.ID+"' style='position:absolute;left:"+eval(ll-ss2-this.Border)+"px;top:"+eval(tt-ss2-this.Border)+"px; width:"+eval(ww+this.Size+2*this.Border)+"px; height:"+eval(hh+this.Size+2*this.Border)+"px; z-index:"+_zIndex+";' title='"+_nvl(theTooltipText,"")+"'>");  
  if ((ww==0)||(hh==0))
    _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+this.Border+"px;top:"+this.Border+"px;width:"+eval(ww+this.Size)+"px;height:"+eval(hh+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>");
  else
  { if (ww>hh)
    { ccr=0;
      cct=0;
      while (ccr<ww)
      { ccl=ccr;
        while ((Math.round(ccr*hh/ww)==cct)&&(ccr<=ww)) ccr++;
        if (ddir)
          _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+eval(ccl+this.Border)+"px;top:"+eval(cct+this.Border)+"px;width:"+eval(ccr-ccl+this.Size)+"px;height:"+this.Size+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>");
        else
          _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+eval(ww-ccr+this.Border)+"px;top:"+eval(cct+this.Border)+"px;width:"+eval(ccr-ccl+this.Size)+"px;height:"+this.Size+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>");
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
          _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+eval(ccl+this.Border)+"px;top:"+eval(cct+this.Border)+"px;width:"+this.Size+"px;height:"+eval(ccb-cct+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>");
        else
          _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+eval(ww-ccl+this.Border)+"px;top:"+eval(cct+this.Border)+"px;width:"+this.Size+"px;height:"+eval(ccb-cct+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>");
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
    _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+eval(this.Border+ll-ll0)+"px;top:"+eval(this.Border+tt-tt0)+"px;width:"+eval(ww+this.Size)+"px;height:"+eval(hh+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>");
  else
  { if (ww>hh)
    { ccr=0;
      cct=0;
      while (ccr<ww)
      { ccl=ccr;
        while ((Math.round(ccr*hh/ww)==cct)&&(ccr<=ww)) ccr++;
        if (ddir)
          _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+eval(ccl+this.Border+ll-ll0)+"px;top:"+eval(cct+this.Border+tt-tt0)+"px;width:"+eval(ccr-ccl+this.Size)+"px;height:"+this.Size+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>");
        else
          _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+eval(ww-ccr+this.Border+ll-ll0)+"px;top:"+eval(cct+this.Border+tt-tt0)+"px;width:"+eval(ccr-ccl+this.Size)+"px;height:"+this.Size+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>");
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
          _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+eval(ccl+this.Border+ll-ll0)+"px;top:"+eval(cct+this.Border+tt-tt0)+"px;width:"+this.Size+"px;height:"+eval(ccb-cct+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>");
        else 
          _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+eval(ww-ccl+this.Border+ll-ll0)+"px;top:"+eval(cct+this.Border+tt-tt0)+"px;width:"+this.Size+"px;height:"+eval(ccb-cct+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>");
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
    _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+eval(this.Border+ll-ll0)+"px;top:"+eval(this.Border+tt-tt0)+"px;width:"+eval(ww+this.Size)+"px;height:"+eval(hh+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>");
  else
  { if (ww>hh)
    { ccr=0;
      cct=0;
      while (ccr<ww)
      { ccl=ccr;
        while ((Math.round(ccr*hh/ww)==cct)&&(ccr<=ww)) ccr++;
        if (ddir)
          _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+eval(ccl+this.Border+ll-ll0)+"px;top:"+eval(cct+this.Border+tt-tt0)+"px;width:"+eval(ccr-ccl+this.Size)+"px;height:"+this.Size+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>");
        else
          _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+eval(ww-ccr+this.Border+ll-ll0)+"px;top:"+eval(cct+this.Border+tt-tt0)+"px;width:"+eval(ccr-ccl+this.Size)+"px;height:"+this.Size+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>");
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
          _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+eval(ccl+this.Border+ll-ll0)+"px;top:"+eval(cct+this.Border+tt-tt0)+"px;width:"+this.Size+"px;height:"+eval(ccb-cct+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>");
        else
          _DiagramTarget.document.write("<div onClick='"+_nvl(theAction,"")+"' style='"+_cursor(theAction)+"position:absolute;left:"+eval(ww-ccl+this.Border+ll-ll0)+"px;top:"+eval(cct+this.Border+tt-tt0)+"px;width:"+this.Size+"px;height:"+eval(ccb-cct+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>");
        ccl++;
      }
    }
  }
  _DiagramTarget.document.writeln("</div>");
  this.X0=theX0;
  this.Y0=theY0;
  return(this);
}
function _ArrowResizeTo(theX0, theY0, theX1, theY1)
{ var xx0, yy0, xx1, yy1, ll, rr, tt, bb, ww, hh, ccl, ccr, cct, ccb, id=this.ID,lay=0,selObj="",divtext="";
  var ss2=Math.floor(this.Size/2), nbsp="";//(_IE)? "&nbsp;" : "";
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
  if (document.all) selObj=eval("_DiagramTarget.document.all."+id);
  else selObj=_DiagramTarget.document.getElementById(id);
  with (selObj.style)
  { left=eval(ll-ss2-this.Border)+"px";
    top=eval(tt-ss2-this.Border)+"px";
    width=eval(ww+this.Size+2*this.Border)+"px";
    height=eval(hh+this.Size+2*this.Border)+"px";
  }
  if ((ww==0)||(hh==0))
    divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:"+this.Border+"px;top:"+this.Border+"px;width:"+eval(ww+this.Size)+"px;height:"+eval(hh+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>";
  else
  { if (ww>hh)
    { ccr=0;
      cct=0;
      while (ccr<ww)
      { ccl=ccr;
        while ((Math.round(ccr*hh/ww)==cct)&&(ccr<=ww)) ccr++;
        if (ddir)
          divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:"+eval(ccl+this.Border)+"px;top:"+eval(cct+this.Border)+"px;width:"+eval(ccr-ccl+this.Size)+"px;height:"+this.Size+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>";
        else
          divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:"+eval(ww-ccr+this.Border)+"px;top:"+eval(cct+this.Border)+"px;width:"+eval(ccr-ccl+this.Size)+"px;height:"+this.Size+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>";
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
          divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:"+eval(ccl+this.Border)+"px;top:"+eval(cct+this.Border)+"px;width:"+this.Size+"px;height:"+eval(ccb-cct+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>";
        else
          divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:"+eval(ww-ccl+this.Border)+"px;top:"+eval(cct+this.Border)+"px;width:"+this.Size+"px;height:"+eval(ccb-cct+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>";
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
    divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:"+eval(this.Border+ll-ll0)+"px;top:"+eval(this.Border+tt-tt0)+"px;width:"+eval(ww+this.Size)+"px;height:"+eval(hh+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>";
  else
  { if (ww>hh)
    { ccr=0;
      cct=0;
      while (ccr<ww)
      { ccl=ccr;
        while ((Math.round(ccr*hh/ww)==cct)&&(ccr<=ww)) ccr++;
        if (ddir)
          divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:"+eval(ccl+this.Border+ll-ll0)+"px;top:"+eval(cct+this.Border+tt-tt0)+"px;width:"+eval(ccr-ccl+this.Size)+"px;height:"+this.Size+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>";
        else
          divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:"+eval(ww-ccr+this.Border+ll-ll0)+"px;top:"+eval(cct+this.Border+tt-tt0)+"px;width:"+eval(ccr-ccl+this.Size)+"px;height:"+this.Size+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>";
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
          divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:"+eval(ccl+this.Border+ll-ll0)+"px;top:"+eval(cct+this.Border+tt-tt0)+"px;width:"+this.Size+"px;height:"+eval(ccb-cct+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>";
        else 
          divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:"+eval(ww-ccl+this.Border+ll-ll0)+"px;top:"+eval(cct+this.Border+tt-tt0)+"px;width:"+this.Size+"px;height:"+eval(ccb-cct+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>";
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
    divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:"+eval(this.Border+ll-ll0)+"px;top:"+eval(this.Border+tt-tt0)+"px;width:"+eval(ww+this.Size)+"px;height:"+eval(hh+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>";
  else
  { if (ww>hh)
    { ccr=0;
      cct=0;
      while (ccr<ww)
      { ccl=ccr;
        while ((Math.round(ccr*hh/ww)==cct)&&(ccr<=ww)) ccr++;
        if (ddir)
          divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:"+eval(ccl+this.Border+ll-ll0)+"px;top:"+eval(cct+this.Border+tt-tt0)+"px;width:"+eval(ccr-ccl+this.Size)+"px;height:"+this.Size+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>";
        else
          divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:"+eval(ww-ccr+this.Border+ll-ll0)+"px;top:"+eval(cct+this.Border+tt-tt0)+"px;width:"+eval(ccr-ccl+this.Size)+"px;height:"+this.Size+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>";
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
          divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:"+eval(ccl+this.Border+ll-ll0)+"px;top:"+eval(cct+this.Border+tt-tt0)+"px;width:"+this.Size+"px;height:"+eval(ccb-cct+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>";
        else
          divtext+="<div onClick='"+_nvl(this.Action,"")+"' style='"+_cursor(this.Action)+"position:absolute;left:"+eval(ww-ccl+this.Border+ll-ll0)+"px;top:"+eval(cct+this.Border+tt-tt0)+"px;width:"+this.Size+"px;height:"+eval(ccb-cct+this.Size)+"px;background-color:"+this.Color+";font-size:1pt;line-height:1pt;'>"+nbsp+"</div>";
        ccl++;
      }
    }
  }
  selObj.innerHTML=divtext;
  this.X0=tmpX0;
  this.Y0=tmpY0;
}
function _ArrowMoveTo(theLeft, theTop)
{ var id=this.ID, selObj;
  var ss2=Math.floor(this.Size/2);
  if (theLeft!="") this.left=theLeft;
  if (theTop!="") this.top=theTop;
  if (document.all) selObj=eval("_DiagramTarget.document.all."+id);
  else selObj=_DiagramTarget.document.getElementById(id);
  with (selObj.style)
  { if (theLeft!="") left=eval(theLeft-ss2-this.Border)+"px";
    if (theTop!="") top=eval(theTop-ss2-this.Border)+"px";
    visibility="visible";
  }
}
