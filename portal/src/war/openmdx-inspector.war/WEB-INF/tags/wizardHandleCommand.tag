<%@tag description="Wizard handle command tag" pageEncoding="UTF-8"
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
<%@attribute name="command"%>
<%@attribute name="defaultCommand"%>
<%@attribute name="assertRequestId" type="java.lang.Boolean"%>
<%@attribute name="assertObjectXri" type="java.lang.Boolean"%>
<%
	AbstractWizardController wc = (AbstractWizardController)jspContext.findAttribute("controller");
	String commandAttribute = (String)jspContext.findAttribute("commandAttribute");
	if(commandAttribute == null) {
		commandAttribute = "Command";
	}
	String defaultCommand = (String)jspContext.findAttribute("defaultCommand");
	if(defaultCommand == null) {
		defaultCommand = "Refresh";
	}
	Boolean assertRequestId = (Boolean)jspContext.findAttribute("assertRequestId");
	if(assertRequestId == null) {
		assertRequestId = true;
	}
	Boolean assertObjectXri = (Boolean)jspContext.findAttribute("assertObjectXri");
	if(assertObjectXri == null) {
		assertObjectXri = true;
	}
	if(!wc.init(request, "UTF-8", assertRequestId, assertObjectXri )) {
		response.sendRedirect(
			request.getContextPath() + "/" + WebKeys.SERVLET_NAME
		);
	} else {
		String command = wc.getRequestParameter(commandAttribute);
		wc.handle(command == null ? defaultCommand : command);
		if(wc.getExitAction() != null) {
			if(wc.getWizardName() != null) {
				session.setAttribute(wc.getWizardName(), null);
			}
			response.sendRedirect(
				request.getContextPath() + "/" + wc.getExitAction().getEncodedHRef()
			);
		}
	}
%>
