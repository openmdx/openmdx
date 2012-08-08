import org.openmdx.portal.servlet.*;
import org.openmdx.portal.servlet.texts.*;
import org.openmdx.portal.servlet.view.*;
import org.openmdx.portal.servlet.control.*;

MenuControl menuControl = (MenuControl)control;
String menuClass = menuControl.getMenuClass();
boolean hasPrintOption = menuControl.hasPrintOption();
p.write("<div id=\"", id, "\" >");
if(hasPrintOption) {
    p.write("<div class=\"printButton\" id=\"printButton\" onClick=\"javascript:yuiPrint();\">&nbsp;</div>");
}
p.write("  <ul id=\"", menuClass, "\" class=\"", menuClass, "\" onmouseover=\"sfinit(this);\" >");        
menuControl.paintContent(
    p, 
    frame, 
    forEditing
);
p.write("  </ul>&nbsp;");
p.write("</div>");  
