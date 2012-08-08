import org.openmdx.portal.servlet.*;
import org.openmdx.portal.servlet.texts.*;
import org.openmdx.portal.servlet.view.*;
import org.openmdx.portal.servlet.control.*;

p.write("<div id=\"menuFlyIn\">");
p.write("  <ul id=\"nav\" class=\"nav\" onmouseover=\"sfinit(this);\" >");
p.write("    <li><a href=\"#\" onclick=\"javascript:return false;\"><img id=\"rootMenuAnchor\" src=\"./images/flyin.gif\" border=\"0\"/></a>");
p.write("      <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
RootMenuControl.paintQuickAccessors(p);
p.write("      </ul>");
p.write("    </li>");
p.write("  </ul>");
p.write("</div> <!-- menuFlyIn -->");

p.write("<div id=\"breadcrum\">");
p.write("  <div id=\"breadcrumBorder\">");
NavigationControl.paintClose(
    p,
    forEditing
);
NavigationControl.paintPrint(
    p,
    forEditing
);
NavigationControl.paintHeaderHider(
    p,
    forEditing
);
NavigationControl.paintSelectPerspectives(
    p,
    forEditing
);
NavigationControl.paintToggleViewPort(
    p,
    forEditing
);
NavigationControl.paintBreadcrum(
    p,
    forEditing
);
p.write("  </div> <!-- breadcrumBorder -->");
p.write("</div> <!-- breadcrum -->");
