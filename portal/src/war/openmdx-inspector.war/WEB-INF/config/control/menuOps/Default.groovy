import org.openmdx.portal.servlet.*;
import org.openmdx.portal.servlet.texts.*;
import org.openmdx.portal.servlet.view.*;
import org.openmdx.portal.servlet.control.*;

MenuControl menuControl = (MenuControl)control;
String menuClass = menuControl.getMenuClass();
p.write("<div id=\"", id, "\" >");
p.write("  <ul id=\"", menuClass, "\" class=\"", menuClass, "\" onmouseover=\"sfinit(this);\" >");        
menuControl.paintContent(
    p, 
    frame, 
    forEditing
);
p.write("  </ul>");
p.write("</div>");  
