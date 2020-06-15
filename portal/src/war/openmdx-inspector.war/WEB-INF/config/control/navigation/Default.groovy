import org.openmdx.portal.servlet.*;
import org.openmdx.portal.servlet.texts.*;
import org.openmdx.portal.servlet.component.*;
import org.openmdx.portal.servlet.control.*;

p.write("<div id=\"breadcrum\">");
NavigationControl.paintClose(p, forEditing);
NavigationControl.paintPrint(p, forEditing);
NavigationControl.paintHeaderHider(p, forEditing);
NavigationControl.paintSelectPerspectives(p, forEditing);
NavigationControl.paintBreadcrumb(p, forEditing);
p.write("</div> <!-- breadcrum -->");
