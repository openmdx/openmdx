import org.openmdx.base.naming.*;
import org.openmdx.portal.servlet.*;
import org.openmdx.portal.servlet.texts.*;
import org.openmdx.portal.servlet.component.*;
import org.openmdx.portal.servlet.control.*;
import org.openmdx.base.accessor.jmi.cci.*;

ApplicationContext app = p.getApplicationContext();
RefObject_1_0[] rootObjects = app.getRootObject();  
String segmentName = "Standard";
if(rootObjects.length > 0) {
    Path objectPath = new Path(rootObjects[0].refMofId());
    segmentName = objectPath.get(4);
}
if(forEditing) {
	p.write("<div id=\"logoTable\">");
} else {
	p.write("<div id=\"panelLogo\" class=\"", app.getPanelState("Header") == 0 ? "logoTable" : "logoTableNH", "\">");
}
p.write("  <table id=\"headerlayout\">");
p.write("    <tr id=\"headRow\">");
p.write("      <td id=\"head\" colspan=\"2\">");
p.write("        <table id=\"info\">");
p.write("          <tr>");
p.write("            <td id=\"headerCellLeft\"><img id=\"logoLeft\" class=\"d-xs-none\" src=\"", p.getResourcePath("images/"), "logoLeft.gif\" alt=\"", app.getApplicationName(), "\" title=\"\" /><img id=\"logoLeft\" class=\"d-none d-xs-block\" style=\"width:100px;\" src=\"", p.getResourcePath("images/"), "logoLeft.gif\" alt=\"", app.getApplicationName(), "\" title=\"\" /></td>");
p.write("            <td id=\"headerCellSpacerLeft\"></td>");
p.write("            <td id=\"headerCellMiddle\">");
p.write("              <table id=\"headerMiddleLayout\">");
p.write("                <tr id=\"hm1\">");
p.write("                  <td>");        
SessionInfoControl.paintRolesMenu(p, forEditing);
p.write("                  </td>");
p.write("                  <td rowspan=\"2\" class=\"hidden-xs\">");
p.write("                    <div id=\"segmentLogo\">");
p.write("                      <img src=\"./images/segment_", segmentName, ".gif\" alt=\"", segmentName, "\" title=\"", segmentName, "\" />");
p.write("                    </div>");
p.write("                  </td>");
p.write("                  <td class=\"hidden-xs\">");
p.write("                  </td>");
p.write("                </tr>");
p.write("                <tr id=\"hm2\">");    
p.write("                  <td>");
SessionInfoControl.paintLocalesMenu(p, forEditing);
p.write("                  </td>");
p.write("                  <td class=\"hidden-xs\">");
p.write("                  </td>");
p.write("                </tr>");
p.write("              </table>");
p.write("            </td>");
p.write("            <td id=\"headerCellRight\" class=\"hidden-xs\"><img id=\"logoRight\" src=\"", p.getResourcePath("images/"), "logoRight.gif\" alt=\"\" title=\"\" /></td>");
p.write("          </tr>");
p.write("        </table>");
p.write("      </td>");
p.write("    </tr>");
p.write(" </table>");                
p.write("</div>");                
