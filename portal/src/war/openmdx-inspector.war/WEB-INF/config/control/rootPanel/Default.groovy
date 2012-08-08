import org.openmdx.portal.servlet.*;
import org.openmdx.portal.servlet.texts.*;
import org.openmdx.portal.servlet.view.*;
import org.openmdx.portal.servlet.control.*;

MenuControl menuControl = (MenuControl)control;
String menuClass = menuControl.getMenuClass();
ApplicationContext app = p.getApplicationContext();
p.write("<div id=\"", id, "\" >");
p.write("  <div class=\"hd\">", app.getTexts().getExploreText(), "</div>");
p.write("  <div class=\"bd\">");
p.write("    <ul id=\"", menuClass, "\" class=\"", menuClass, "\" onmouseover=\"sfinit(this);\" >");
menuControl.paintContent(
    p, 
    frame, 
    forEditing
);
p.write("    </ul>");
p.write("  </div>");
p.write("</div>");  
