// JavaScript Diagram Builder 3.0
// Copyright (c) 2001-2004 Lutz Tautenhahn, all rights reserved.
//
// The Author grants you a non-exclusive, royalty free, license to use,
// modify and redistribute this software, provided that this copyright notice
// and license appear on all copies of the software.
// This software is provided "as is", without a warranty of any kind.

var _N_Dia=0, _N_Bar=0, _N_Box=0, _N_Dot=0, _N_Pix=0, _N_Line=0, _N_Area=0, _N_Arrow=0, _zIndex=0;
var _dSize = (navigator.appName == "Microsoft Internet Explorer") ? 1 : -1;
if (navigator.userAgent.search("Opera")>=0) _dSize=-1;
var _IE=0;
if (_dSize==1)
{ _IE=1;
  if (window.document.documentElement.clientHeight) _dSize=-1; //IE in standards-compliant mode
}
var _nav4 = (document.layers) ? 1 : 0;
var _DiagramTarget=window;
var _BFont="font-family:Tahoma;font-weight:bold;font-size:10pt;line-height:13pt;"
var _PathToScript="javascript/";
if (document.layers) document.write("<script language=\"JavaScript\" src=\""+_PathToScript+"diagram_nav.js\"></script>");
else document.write("<script language=\"JavaScript\" src=\""+_PathToScript+"diagram_dom.js\"></script>");

function Diagram()
{ this.xtext="";
  this.ytext="";
  this.title="";
  this.XScale=1;
  this.YScale=1;
  this.Font="font-family:Verdana;font-weight:normal;font-size:10pt;line-height:13pt;";
  this.ID="Dia"+_N_Dia; _N_Dia++; _zIndex++;
  this.zIndex=_zIndex;
  this.SetFrame=_SetFrame;
  this.SetBorder=_SetBorder;
  this.SetText=_SetText;
  this.SetGridColor=_SetGridColor;
  this.ScreenX=_ScreenX;
  this.ScreenY=_ScreenY;
  this.RealX=_RealX;
  this.RealY=_RealY;
  this.XGrid=new Array(3);
  this.GetXGrid=_GetXGrid;
  this.YGrid=new Array(3);
  this.GetYGrid=_GetYGrid;
  this.XGridDelta=0;
  this.YGridDelta=0;
  this.XSubGrids=0;
  this.YSubGrids=0;
  this.SubGrids=0;
  this.GridColor="";
  this.SubGridColor="";
  this.DateInterval=_DateInterval;
  this.Draw=_Draw;
  this.SetVisibility=_SetVisibility;
  this.SetTitle=_SetTitle;
  this.Delete=_Delete;
  return(this);
}
function _SetFrame(theLeft, theTop, theRight, theBottom)
{ this.left   = theLeft;
  this.right  = theRight;
  this.top    = theTop;
  this.bottom = theBottom;
}
function _SetBorder(theLeftX, theRightX, theBottomY, theTopY)
{ this.xmin = theLeftX;
  this.xmax = theRightX;
  this.ymin = theBottomY;
  this.ymax = theTopY;
}
function _SetText(theScaleX, theScaleY, theTitle)
{ this.xtext=theScaleX;
  this.ytext=theScaleY;
  this.title=theTitle;
}
function _SetGridColor(theGridColor, theSubGridColor)
{ this.GridColor=theGridColor;
  this.SubGridColor=theSubGridColor;
}
function _ScreenX(theRealX)
{ return(Math.round((theRealX-this.xmin)/(this.xmax-this.xmin)*(this.right-this.left)+this.left));
}
function _ScreenY(theRealY)
{ return(Math.round((this.ymax-theRealY)/(this.ymax-this.ymin)*(this.bottom-this.top)+this.top));
}
function _RealX(theScreenX)
{ return(this.xmin+(this.xmax-this.xmin)*(theScreenX-this.left)/(this.right-this.left));
}
function _RealY(theScreenY)
{ return(this.ymax-(this.ymax-this.ymin)*(theScreenY-this.top)/(this.bottom-this.top));
}
function _sign(rr)
{ if (rr<0) return(-1); else return(1);
}
function _DateInterval(vv)
{ var bb=140*24*60*60*1000; //140 days
  this.SubGrids=4;
  if (vv>=bb) //140 days < 5 months
  { bb=8766*60*60*1000;//1 year
    if (vv<bb) //1 year 
      return(bb/12); //1 month
    if (vv<bb*2) //2 years 
      return(bb/6); //2 month
    if (vv<bb*5/2) //2.5 years
    { this.SubGrids=6; return(bb/4); } //3 month
    if (vv<bb*5) //5 years
    { this.SubGrids=6; return(bb/2); } //6 month
    if (vv<bb*10) //10 years
      return(bb); //1 year
    if (vv<bb*20) //20 years
      return(bb*2); //2 years
    if (vv<bb*50) //50 years
    { this.SubGrids=5; return(bb*5); } //5 years
    if (vv<bb*100) //100 years
    { this.SubGrids=5; return(bb*10); } //10 years
    if (vv<bb*200) //200 years
      return(bb*20); //20 years
    if (vv<bb*500) //500 years
    { this.SubGrids=5; return(bb*50); } //50 years
    this.SubGrids=5; return(bb*100); //100 years
  }
  bb/=2; //70 days
  if (vv>=bb) { this.SubGrids=7; return(bb/5); } //14 days
  bb/=2; //35 days
  if (vv>=bb) { this.SubGrids=7; return(bb/5); } //7 days
  bb/=7; bb*=4; //20 days
  if (vv>=bb) return(bb/5); //4 days
  bb/=2; //10 days
  if (vv>=bb) return(bb/5); //2 days
  bb/=2; //5 days
  if (vv>=bb) return(bb/5); //1 day
  bb/=2; //2.5 days
  if (vv>=bb) return(bb/5); //12 hours
  bb*=3; bb/=5; //1.5 day
  if (vv>=bb) { this.SubGrids=6; return(bb/6); } //6 hours
  bb/=2; //18 hours
  if (vv>=bb) { this.SubGrids=6; return(bb/6); } //3 hours
  bb*=2; bb/=3; //12 hours
  if (vv>=bb) return(bb/6); //2 hours
  bb/=2; //6 hours
  if (vv>=bb) return(bb/6); //1 hour
  bb/=2; //3 hours
  if (vv>=bb) { this.SubGrids=6; return(bb/6); } //30 mins
  bb/=2; //1.5 hours
  if (vv>=bb) { this.SubGrids=5; return(bb/6); } //15 mins
  bb*=2; bb/=3; //1 hour
  if (vv>=bb) { this.SubGrids=5; return(bb/6); } //10 mins
  bb/=3; //20 mins
  if (vv>=bb) { this.SubGrids=5; return(bb/4); } //5 mins
  bb/=2; //10 mins
  if (vv>=bb) return(bb/5); //2 mins
  bb/=2; //5 mins
  if (vv>=bb) return(bb/5); //1 min
  bb*=3; bb/=2; //3 mins
  if (vv>=bb) { this.SubGrids=6; return(bb/6); } //30 secs
  bb/=2; //1.5 mins
  if (vv>=bb) { this.SubGrids=5; return(bb/6); } //15 secs
  bb*=2; bb/=3; //1 min
  if (vv>=bb) { this.SubGrids=5; return(bb/6); } //10 secs
  bb/=3; //20 secs
  if (vv>=bb) { this.SubGrids=5; return(bb/4); } //5 secs
  bb/=2; //10 secs
  if (vv>=bb) return(bb/5); //2 secs
  return(bb/10); //1 sec
}
function _DayOfYear(dd,mm,yy) //Unused, you can use this for your own date format
{ DOM=new Array(31,28,31,30,31,30,31,31,30,31,30,31);
  var ii, nn=dd;
  for (ii=0; ii<mm-1; ii++) nn+=DOM[ii];
  if ((mm>2)&&(yy%4==0)) nn++;
  return(nn);
}
function _GetKWT(dd,mm,yy) //Unused, you can use this for your own date format 
{ //this is the implementation of DIN 1355, not of the american standard!   
  var ss=new Date(yy,0,1);
  var ww=ss.getDay(); //0=Sun,1=Mon,2=Tue,3=Wed,4=Thu,5=Fri,6=Sat
  ww=(ww+2)%7-3; //0=Mon,1=Tue,2=Wed,3=Thu,-3=Fri,-2=Sat,-1=Sun
  ww+=(_DayOfYear(dd,mm,yy)-1);
  if (ww<0) return(_GetKWT(24+dd,12,yy-1));
  if ((mm==12)&&(dd>28))
  { if (ww%7+29<=dd) return("01/"+eval(ww%7+1)); //31: Mon-Wed, 30: Mon-Tue, 29: Mon
  }
  ss=Math.floor(ww/7+1);
  if (ss<10) ss="0"+ss;
  return(ss+"/"+eval(ww%7+1));
}
function _DateFormat(vv, ii, ttype)
{ var yy, mm, dd, hh, nn, ss, vv_date=new Date(vv);
  Month=new Array("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec");
  Weekday=new Array("Sun","Mon","Tue","Wed","Thu","Fri","Sat");
  if (ii>15*24*60*60*1000)
  { if (ii<365*24*60*60*1000)
    { vv_date.setTime(vv+15*24*60*60*1000);
      yy=vv_date.getUTCFullYear()%100;
      if (yy<10) yy="0"+yy;
      mm=vv_date.getUTCMonth()+1;
      if (ttype==5) ;//You can add your own date format here
      if (ttype==4) return(Month[mm-1]);
      if (ttype==3) return(Month[mm-1]+" "+yy);
      return(mm+"/"+yy);
    }
    vv_date.setTime(vv+183*24*60*60*1000);
    yy=vv_date.getUTCFullYear();
    return(yy);
  }
  vv_date.setTime(vv);
  yy=vv_date.getUTCFullYear();
  mm=vv_date.getUTCMonth()+1;
  dd=vv_date.getUTCDate();
  ww=vv_date.getUTCDay();
  hh=vv_date.getUTCHours();
  nn=vv_date.getUTCMinutes();
  ss=vv_date.getUTCSeconds();
  if (ii>=86400000)//1 day
  { if (ttype==5) ;//You can add your own date format here
    if (ttype==4) return(Weekday[ww]);
    if (ttype==3) return(mm+"/"+dd);
    return(dd+"."+mm+".");
  }
  if (ii>=21600000)//6 hours 
  { if (hh==0) 
    { if (ttype==5) ;//You can add your own date format here
      if (ttype==4) return(Weekday[ww]);
      if (ttype==3) return(mm+"/"+dd);
      return(dd+"."+mm+".");
    }
    else
    { if (ttype==5) ;//You can add your own date format here
      if (ttype==4) return((hh<=12) ? hh+"am" : hh%12+"pm");
      if (ttype==3) return((hh<=12) ? hh+"am" : hh%12+"pm");
      return(hh+":00");
    }
  }
  if (ii>=60000)//1 min
  { if (nn<10) nn="0"+nn;
    if (ttype==5) ;//You can add your own date format here
    if (ttype==4) return((hh<=12) ? hh+"."+nn+"am" : hh%12+"."+nn+"pm");
    if (nn=="00") nn="";
    else nn=":"+nn;
    if (ttype==3) return((hh<=12) ? hh+nn+"am" : hh%12+nn+"pm");
    if (nn=="") nn=":00";
    return(hh+nn);
  }
  if (ss<10) ss="0"+ss;
  return(nn+":"+ss);
}
function _GetXGrid()
{ var x0,i,j,l,x,r,dx,xr,invdifx,deltax;
  dx=(this.xmax-this.xmin);
  if (Math.abs(dx)>0)
  { invdifx=(this.right-this.left)/(this.xmax-this.xmin);
    if ((this.XScale==1)||(isNaN(this.XScale)))
    { r=1;
      while (Math.abs(dx)>=100) { dx/=10; r*=10; }
      while (Math.abs(dx)<10) { dx*=10; r/=10; }
      if (Math.abs(dx)>=50) { this.SubGrids=5; deltax=10*r*_sign(dx); }
      else
      { if (Math.abs(dx)>=20) { this.SubGrids=5; deltax=5*r*_sign(dx); }
        else { this.SubGrids=4; deltax=2*r*_sign(dx); }
      }
    }
    else deltax=this.DateInterval(Math.abs(dx))*_sign(dx);
    if (this.XGridDelta!=0) deltax=this.XGridDelta;
    if (this.XSubGrids!=0) this.SubGrids=this.XSubGrids;
    x=Math.floor(this.xmin/deltax)*deltax;
    i=0;
    this.XGrid[1]=deltax;
    for (j=54; j>=-1; j--)
    { xr=x+j*deltax;
      x0=Math.round(this.left+(-this.xmin+xr)*invdifx);
      if ((x0>=this.left)&&(x0<=this.right))
      { if (i==0) this.XGrid[2]=xr;
        this.XGrid[0]=xr;
        i++;
      }
    }
  }
  return(this.XGrid);
}
function _GetYGrid()
{ var y0,i,j,l,y,r,dy,yr,invdify,deltay;
  dy=this.ymax-this.ymin;
  if (Math.abs(dy)>0)
  { invdify=(this.bottom-this.top)/(this.ymax-this.ymin);
    if ((this.YScale==1)||(isNaN(this.YScale)))
    { r=1;
      while (Math.abs(dy)>=100) { dy/=10; r*=10; }
      while (Math.abs(dy)<10) { dy*=10; r/=10; }
      if (Math.abs(dy)>=50) { this.SubGrids=5; deltay=10*r*_sign(dy); }
      else
      { if (Math.abs(dy)>=20) { this.SubGrids=5; deltay=5*r*_sign(dy); }
        else { this.SubGrids=4; deltay=2*r*_sign(dy); }
      }
    }
    else deltay=this.DateInterval(Math.abs(dy))*_sign(dy);
    if (this.YGridDelta!=0) deltay=this.YGridDelta;
    if (this.YSubGrids!=0) this.SubGrids=this.YSubGrids;
    y=Math.floor(this.ymax/deltay)*deltay;
    this.YGrid[1]=deltay;
    i=0;
    for (j=-1; j<=54; j++)
    { yr=y-j*deltay;
      y0=Math.round(this.top+(this.ymax-yr)*invdify);
      if ((y0>=this.top)&&(y0<=this.bottom))
      { if (i==0) this.YGrid[2]=yr;
        this.YGrid[0]=yr;
        i++;
      }
    }
  }
  return(this.YGrid);
}
function _nvl(vv, rr)
{ if (vv==null) return(rr);
  var ss=String(vv);
  while (ss.search("'")>=0) ss=ss.replace("'","&#39;");
  return(ss);
}
function _cursor(aa)
{ if (aa)
  { if (_dSize==1) return("cursor:hand;");
    else  return("cursor:pointer;");
  }  
  return("");
}
function _GetArrayMin(aa)
{ var ii, mm=aa[0];
  for (ii=1; ii<aa.length; ii++)
  { if (mm>aa[ii]) mm=aa[ii];
  }
  return(mm);
}
function _GetArrayMax(aa)
{ var ii, mm=aa[0];
  for (ii=1; ii<aa.length; ii++)
  { if (mm<aa[ii]) mm=aa[ii];
  }
  return(mm);
}