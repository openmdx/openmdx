// ====================================================================
// Project:     openmdx, http://www.openmdx.org/
// Name:        $Id: chart.js,v 1.2 2007/02/20 10:13:00 cmu Exp $
// Description: helper Javascript for charts
// Revision:    $Revision: 1.2 $
// Owner:       OMEX AG, Switzerland, http://www.omex.ch
// Date:        $Date: 2007/02/20 10:13:00 $
// ====================================================================
//
// This software is published under the BSD license
// as listed below.
//
// Copyright (c) 2004, OMEX AG, Switzerland
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or
// without modification, are permitted provided that the following
// conditions are met:
//
// * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//
// * Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in
// the documentation and/or other materials provided with the
// distribution.
//
// * Neither the name of the openMDX team nor the names of its
// contributors may be used to endorse or promote products derived
// from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
// CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
// BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
// TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
// OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.
//
// ------------------
//
// This product includes software developed by the Apache Software
// Foundation (http://www.apache.org/).
//
// This product includes software developed by Mihai Bazon
// (http://dynarch.com/mishoo/calendar.epl) published with an LGPL
// license.
//

function calcChartTypeHorizontalBars(frameName, chartTitle, scaleXTitle, scaleYTitle, xvalues, xMinValue, xMaxValue, ylabels, ybarBorderColors, ybarFillColors) {
  /* fixed, customized or derived */
  var D = new Diagram();
  var yMinValue = 0; /* by design, i.e. fixed */
  var yMaxValue = ylabels.length; /* derived from raw data */
  var frameMargin = 12; /* customizer */
  var frameTextHeight = 15; /* customizer */
  var frameMarginLeft = scaleYTitle.length*6;
  var frameWidth = getRealRight(frameName)-getRealLeft(frameName);
  var frameHeight = getRealBottom(frameName)-getRealTop(frameName);
  if ((frameWidth==0) || (frameHeight==0)) {return true;}; /* do not recalc if invisible/hidden/... */
  var textColor = "#000000";
  var textHeight = 13; /* in pixels */
  _BFont = "font-family:Tahoma;font-size:8pt;text-align:left;color:" + textColor + ";";
  _DiagramTarget=window.frames[frameName];
  _DiagramTarget.document.open();
  _DiagramTarget.document.writeln('<html><head></head><body style="background-color:white;"');
  if (frameMarginLeft<40) {frameMarginLeft=40;}
  if (frameMarginLeft>90) {frameMarginLeft=90;}
  frameMarginLeft = frameMarginLeft+frameMargin;
  D.SetFrame(frameMargin, frameMargin+frameTextHeight, frameWidth-2*frameMargin, frameHeight-2*frameTextHeight); /* Left, Top, Right, Bottom [Screen] */
  D.SetBorder(xMinValue, xMaxValue+1, -0.6, ylabels.length-0.4); /* Left, Right, Bottom, Top [World] */
  D.XScale=1;
  D.YScale=0;
  D.Font = "font-family:Tahoma;font-size:8pt;text-align:default;color:" + textColor + ";";
  D.SetText(scaleXTitle,scaleYTitle, chartTitle);
  D.SetGridColor("#DDDDDD");
  D.Draw("", textColor, ((scaleXTitle!="") || (scaleYTitle!="")), chartTitle, "");
  var i2, x2, y, x20=D.ScreenX(xMinValue);
  var barHeight = D.ScreenY(yMinValue)-D.ScreenY(yMinValue+1)-3;
  var fontBoxLeft = x20+4;
  var fontBoxRight = D.ScreenX(xMaxValue+1);
  for (y=yMinValue; y<yMaxValue; y++)
  {
    x2=D.ScreenX(xvalues[y]);
    i2=D.ScreenY(y);
    new Box(x20, i2-barHeight/2, x2, i2+barHeight/2, ybarFillColors[y], "", ybarBorderColors[y], 1, "#000000");
    new Box(fontBoxLeft, i2-textHeight/2, fontBoxRight, i2+textHeight/2, "", ylabels[y], "", 0, "#000000");
  }
  _DiagramTarget.document.writeln("</body></html>");
  _DiagramTarget.document.close();
  $(frameName).style.visibility='visible';
  return true;
}

function calcChartTypeHorizontalBarsOverlay(frameName, chartTitle, scaleXTitle, scaleYTitle, xvalues, xMinValue, xMaxValue, ylabels, ybarBorderColors, ybarFillColors, xvaluesOverlay, ybarFillColorsOverlay) {
  /* fixed, customized or derived */
  var D = new Diagram();
  var yMinValue = 0; /* by design, i.e. fixed */
  var yMaxValue = ylabels.length; /* derived from raw data */
  var frameMargin = 12; /* customizer */
  var frameTextHeight = 15; /* customizer */
  var frameMarginLeft = scaleYTitle.length*6;
  var frameWidth = getRealRight(frameName)-getRealLeft(frameName);
  var frameHeight = getRealBottom(frameName)-getRealTop(frameName);
  if ((frameWidth==0) || (frameHeight==0)) {return true;}; /* do not recalc if invisible/hidden/... */
  _BFont = "font-family:Tahoma;font-size:8pt;text-align:left;color:" + textColor + ";";
  var textColor = "#000000";
  var textHeight = 13; /* in pixels */
  _DiagramTarget=window.frames[frameName];
  _DiagramTarget.document.open();
  _DiagramTarget.document.writeln('<html><head></head><body bgcolor=white');
  if (frameMarginLeft<40) {frameMarginLeft=40;}
  if (frameMarginLeft>90) {frameMarginLeft=90;}
  frameMarginLeft = frameMarginLeft+frameMargin;
  D.SetFrame(frameMargin, frameMargin+frameTextHeight, frameWidth-2*frameMargin, frameHeight-2*frameTextHeight); /* Left, Top, Right, Bottom [Screen] */
  D.SetBorder(xMinValue, xMaxValue+1, -0.6, ylabels.length-0.4); /* Left, Right, Bottom, Top [World] */
  D.XScale=1;
  D.YScale=0;
  D.Font = "font-family:Tahoma;font-size:8pt;text-align:default;color:" + textColor + ";";
  D.SetText(scaleXTitle,scaleYTitle, chartTitle);
  D.SetGridColor("#DDDDDD");
  D.Draw("", textColor, ((scaleXTitle!="") || (scaleYTitle!="")), chartTitle, "");
  var i2, x2, o2, y, x20=D.ScreenX(xMinValue);
  var barHeight = D.ScreenY(yMinValue)-D.ScreenY(yMinValue+1)-3;
  var fontBoxLeft = x20+4;
  var fontBoxRight = D.ScreenX(xMaxValue+1);
  for (y=yMinValue; y<yMaxValue; y++)
  {
    x2=D.ScreenX(xvalues[y]);
    i2=D.ScreenY(y);
    new Box(x20, i2-barHeight/2, x2, i2+barHeight/2, ybarFillColors[y], "", ybarBorderColors[y], 1, "#000000");
    o2=D.ScreenX(xvaluesOverlay[y]);
    new Box(x20, i2-barHeight/2, o2, i2+barHeight/2, ybarFillColorsOverlay[y], "", ybarBorderColors[y], 1, "#000000");
    new Box(fontBoxLeft, i2-textHeight/2, fontBoxRight, i2+textHeight/2, "", ylabels[y], "", 0, "#000000");
  }
  _DiagramTarget.document.writeln("</body></html>");
  _DiagramTarget.document.close();
  $(frameName).style.visibility='visible';
  return true;
}

function calcChartTypeVerticalBars(frameName, chartTitle, scaleXTitle, scaleYTitle, yvalues, yMinValue, yMaxValue, xlabels, xbarBorderColors, xbarFillColors) {
  /* fixed, customized or derived */
  var D = new Diagram();
  var xMinValue = 0; /* by design, i.e. fixed */
  var xMaxValue = xlabels.length; /* derived from raw data */
  var frameMargin = 12; /* customizer */
  var frameTextHeight = 15; /* customizer */
  var frameMarginLeft = scaleYTitle.length*6;
  var frameWidth = getRealRight(frameName)-getRealLeft(frameName);
  var frameHeight = getRealBottom(frameName)-getRealTop(frameName);
  if ((frameWidth==0) || (frameHeight==0)) {return true;}; /* do not recalc if invisible/hidden/... */
  _BFont = "font-family:Tahoma;font-size:8pt;text-align:center;color:" + textColor + ";";
  var textColor = "#000000";
  var textHeight = 13; /* in pixels */
  _DiagramTarget=window.frames[frameName];
  _DiagramTarget.document.open();
  _DiagramTarget.document.writeln('<html><head></head><body bgcolor=white');
  if (frameMarginLeft<40) {frameMarginLeft=40;}
  if (frameMarginLeft>90) {frameMarginLeft=90;}
  frameMarginLeft = frameMarginLeft+frameMargin;
  D.SetFrame(frameMarginLeft, frameMargin+frameTextHeight, frameWidth-2*frameMargin, frameHeight-2*frameTextHeight); /* Left, Top, Right, Bottom [Screen] */
  D.SetBorder(-0.6, xlabels.length-0.4, yMinValue, yMaxValue+1); /* Left, Right, Bottom, Top [World] */
  D.YScale=1;
  D.XScale=0;
  D.Font = "font-family:Tahoma;font-size:8pt;text-align:default;color:" + textColor + ";";
  D.SetText(scaleXTitle,scaleYTitle, chartTitle);
  D.SetGridColor("#DDDDDD");
  D.Draw("", textColor, ((scaleXTitle!="") || (scaleYTitle!="")), chartTitle, "");
  var i2, y2, x, y20=D.ScreenY(yMinValue);
  var barWidth = D.ScreenX(xMinValue+1)-D.ScreenX(xMinValue)-3;
  var fontBoxBottom = y20;
  var fontBoxTop = D.ScreenX(xMaxValue);
  for (x=xMinValue; x<xMaxValue; x++)
  {
    y2=D.ScreenY(yvalues[x]);
    i2=D.ScreenX(x);
	if (y2==y20) {y2=y2-2;}; /* ensure box is not below zero line!!! */
    new Box(i2-barWidth/2, y2, i2+barWidth/2, y20, xbarFillColors[x], "", xbarBorderColors[x], 1, "#000000");
    new Box(i2-barWidth/2, fontBoxBottom, i2+barWidth/2, y20, "", xlabels[x], "", 0, "#000000");
  }
  _DiagramTarget.document.writeln("</body></html>");
  _DiagramTarget.document.close();
  $(frameName).style.visibility='visible';
  return true;
}