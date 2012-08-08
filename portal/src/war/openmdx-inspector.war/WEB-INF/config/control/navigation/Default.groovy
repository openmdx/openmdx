import org.openmdx.portal.servlet.*;
import org.openmdx.portal.servlet.texts.*;
import org.openmdx.portal.servlet.view.*;
import org.openmdx.portal.servlet.control.*;

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
NavigationControl.paintBreadcrum(
    p,
    forEditing
);
p.write("  </div> <!-- breadcrumBorder -->");
p.write("</div> <!-- breadcrum -->");
