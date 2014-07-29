<%@tag description="Wizard close tag" pageEncoding="UTF-8"
import="
java.util.*,
java.io.*,
java.text.*,
org.openmdx.application.cci.*,
org.openmdx.base.text.conversion.*,
org.openmdx.base.accessor.cci.*,
org.openmdx.kernel.id.cci.*,
org.openmdx.kernel.id.*,
org.openmdx.base.accessor.jmi.cci.*,
org.openmdx.portal.servlet.*,
org.openmdx.portal.servlet.attribute.*,
org.openmdx.portal.servlet.component.*,
org.openmdx.portal.servlet.control.*,
org.openmdx.portal.servlet.wizards.*,
org.openmdx.base.naming.*" 
%>
<%@attribute name="controller" type="AbstractWizardController"%>
<%
	AbstractWizardController wc = (AbstractWizardController)jspContext.findAttribute("controller");
	wc.close();
%>
