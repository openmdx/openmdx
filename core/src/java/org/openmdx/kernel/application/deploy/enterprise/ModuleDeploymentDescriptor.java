/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ModuleDeploymentDescriptor.java,v 1.12 2008/01/13 21:37:33 hburger Exp $
 * Description: Module Deployment Descriptor
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/13 21:37:33 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */

package org.openmdx.kernel.application.deploy.enterprise;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openmdx.kernel.application.configuration.Report;
import org.openmdx.kernel.application.deploy.spi.Deployment.Module;
import org.w3c.dom.Element;

public class ModuleDeploymentDescriptor 
  extends AbstractDeploymentDescriptor
  implements Module
{
  	  
  public ModuleDeploymentDescriptor(
    String moduleId,
    ApplicationDeploymentDescriptor owner,
	URL url
  ) {
    this(
        moduleId,
        owner,
        url,
        new Report(
            REPORT_EJB_MODULE_NAME, 
            REPORT_EJB_VERSION, 
            url.toString()
        )
    );
  }
  
  protected ModuleDeploymentDescriptor(
      String moduleId,
      ApplicationDeploymentDescriptor owner,
  	  URL url,
  	  Report report
    ) {
      super(url);
      this.moduleId = moduleId;
      this.owner = owner;
      this.contextUrl = url;
      this.report = report;
    }

  public void parseXml(
    Element element
  ) {
    if ("ejb-jar".equals(element.getTagName()))
    {
      this.parseXml(element, this.report);
    }
    else
    {
      this.report.addError("unexpected root tag found '" + element.getTagName() + "' (expected 'ejb-jar')");
    }
  }

  public void parseXml(
    Element element,
    Report report
  ) {
    this.displayName = this.getElementContent(getOptionalChild(element, "display-name", report));
    //
    // Assembly Descriptor
    //
    Element assemblyDescriptor = getOptionalChild(element, "assembly-descriptor", report);
    if(assemblyDescriptor != null) {
        for (
            Iterator<Element> it = getChildrenByTagName(assemblyDescriptor, "security-role");
            it.hasNext ();
        ) {
            Element securityRole = it.next(); 
            String roleName = getElementContent(
                getUniqueChild(securityRole, "role-name", report)
            );
            report.addInfo("Security is not supported; role '" + roleName + "' ignored");          
        }
        if(getChildrenByTagName(assemblyDescriptor, "method-permission").hasNext()) report.addInfo(
            "Security is not supported; method permission entry ignored"
        );          
        containerTransactions = new HashMap<String, Map>();
        for (
            Iterator<Element> it = getChildrenByTagName(assemblyDescriptor, "container-transaction");
            it.hasNext ();
        ) {
            Element containerTransaction = it.next();
            String transAttribute = getElementContent(
                getUniqueChild(containerTransaction, "trans-attribute", report)
            );
            Map<String, List> containerTransactionBeans = containerTransactions.get(transAttribute);
            if(containerTransactionBeans == null) containerTransactions.put(
                transAttribute,
                containerTransactionBeans = new HashMap<String, List>()
            );
            for (
                Iterator<?> i = getChildrenByTagName(containerTransaction, "method");
                i.hasNext ();
            ) {
                Element method = (Element) i.next();
                String ejbName = getElementContent(getUniqueChild(method, "ejb-name", report));
                List<MethodDeploymentDescriptor> methods = containerTransactionBeans.get(ejbName);
                if(methods == null) containerTransactionBeans.put(
                    ejbName, 
                    methods = new ArrayList<MethodDeploymentDescriptor>()
                );
                Element methodIntfElement = getOptionalChild(method, "method-intf", report);
                String methodIntf = methodIntfElement == null ? null : getElementContent(methodIntfElement);
                String methodName = getElementContent(getUniqueChild(method, "method-name", report));
                Element methodParamsElement = getOptionalChild(method, "method-params", report);
                List<String> methodParams;
                if(methodParamsElement == null) {
                    methodParams = null;
                } else  {
                    methodParams = new ArrayList<String>();
                    for (
                        Iterator<?> j = getChildrenByTagName(methodParamsElement, "method-param");
                        j.hasNext ();
                    ) methodParams.add(getElementContent((Element)j.next()));
                }
                methods.add(
                    new MethodDeploymentDescriptor(methodIntf, methodName, methodParams)
                );
            }
        }
        Element excludeList = getOptionalChild(assemblyDescriptor, "exclude-list", report);
        if(excludeList != null) for (
            Iterator<?> it = getChildrenByTagName(excludeList, "method");
            it.hasNext ();
          ) {
            report.addInfo("Security is not supported; exclude list entry ignored");          
        }
    }
    //
    // Enterprise Beans
    //
    Element beans = getOptionalChild(element, "enterprise-beans", report);
    if (beans!=null)
    {
      // add session beans to the collection of components
      for (
        Iterator<?> it = getChildrenByTagName(beans, "session");
        it.hasNext ();
      ) {
        Element sessionBean = (Element)it.next();
        SessionBeanDeploymentDescriptor sbDD = new SessionBeanDeploymentDescriptor(
            this, 
            this.contextUrl, 
            getContainerTransactionByName(
                getElementContent(getUniqueChild(sessionBean, "ejb-name", report))
            )
        );
        sbDD.parseXml(sessionBean);
        this.addComponent(sbDD);  
      }

      // add message driven beans to the collection of components
      for (
        Iterator<?> it = getChildrenByTagName(beans, "message-driven");
        it.hasNext ();
      ) {
        Element messageDrivenBean = (Element)it.next();
        MessageDrivenBeanDeploymentDescriptor mdbDD = new MessageDrivenBeanDeploymentDescriptor(
            this, 
            this.contextUrl, 
            getContainerTransactionByName(
                getElementContent(getUniqueChild(messageDrivenBean, "ejb-name", report))
            )
        );
        mdbDD.parseXml(messageDrivenBean);
        this.addComponent(mdbDD);  
      }
      
      Element entity = getOptionalChild(beans, "entity", report);
      if (entity != null)
      {
        report.addWarning("entity beans are not supported; entity bean declaration is ignored");
      }
    }
  }
  
  private List<ContainerTransactionDeploymentDescriptor> getContainerTransactionByName (
      String ejbName
  ){
      if(this.containerTransactions == null) return Collections.EMPTY_LIST;
      List<ContainerTransactionDeploymentDescriptor> containerTransactions = new ArrayList<ContainerTransactionDeploymentDescriptor>();
      for(
          Iterator<?> i = this.containerTransactions.entrySet().iterator();
          i.hasNext();
      ){
          Map.Entry<?, ?> e = (Entry<?, ?>) i.next();
          String transactionAttribute = (String) e.getKey();
          Map<?, ?> containerTransactionBeans = (Map<?, ?>)e.getValue();
          List<?> methods = (List<?>) containerTransactionBeans.get(ejbName);
          if(methods != null) containerTransactions.add(
              new ContainerTransactionDeploymentDescriptor(methods, transactionAttribute)
          );   
      }
      return containerTransactions;
  }
  
  private void addComponent(
      BeanDeploymentDescriptor component
  ) {
    // check whether the component names inside this module are unique
    if (getComponentByName(component.getName()) == null)
    {
      this.components.add(component);
    }
    else
    {
      this.report.addError("duplicate <ejb-name> found '" + component.getName() + "' for bean");
    }
  }
  
  public void parseOpenMdxXml(
    Element element
  ) {
    if ("openmdx-ejb-jar".equals(element.getTagName()))
    {
      this.parseOpenMdxXml(element, report);
    }
    else
    {
      this.report.addError("unexpected root tag found '" + element.getTagName() + "' (expected 'openmdx-ejb-jar')");
    }
  }
  
  public void parseOpenMdxXml(
    Element element,
    Report report
  ) {
    Element enterpriseBeans = getOptionalChild(element, "enterprise-beans", report);
    if (enterpriseBeans!=null)
    {
      // parse vendor specific deployment descriptor infos for session beans 
      for (
        Iterator<?> it = getChildrenByTagName(enterpriseBeans, "session");
        it.hasNext ();
      ) {
        Element sessionBean = (Element)it.next();
        String ejbName = getElementContent(getUniqueChild(sessionBean, "ejb-name", report));
        BeanDeploymentDescriptor beanDD = this.getComponentByName(ejbName);
        if (beanDD == null)
        {
          report.addWarning("ejb-name '" + ejbName + "' found in openmdx-ejb-jar.xml but not in ejb-jar.xml");
        }
        else if (!(beanDD instanceof SessionBeanDeploymentDescriptor))
        {
          report.addError("the bean with ejb-name '" + ejbName + "' is declared as session bean in openmdx-ejb-jar.xml but not in ejb-jar.xml");
        }
        else
        {
          beanDD.parseOpenMdxXml(sessionBean);
        }
      }

      // parse vendor specific deployment descriptor infos for message driven beans 
      for (
        Iterator<?> it = getChildrenByTagName(enterpriseBeans, "message-driven");
        it.hasNext ();
      ) {
        Element messageDrivenBean = (Element)it.next();
        String ejbName = getElementContent(getUniqueChild(messageDrivenBean, "ejb-name", report));
        BeanDeploymentDescriptor beanDD = this.getComponentByName(ejbName);
        if (beanDD == null)
        {
          report.addWarning("ejb-name '" + ejbName + "' found in openmdx-ejb-jar.xml but not in ejb-jar.xml");
        }
        else if (!(beanDD instanceof MessageDrivenBeanDeploymentDescriptor))
        {
          report.addError("the bean with ejb-name '" + ejbName + "' is declared as message driven bean in openmdx-ejb-jar.xml but not in ejb-jar.xml");
        }
        else
        {
          beanDD.parseOpenMdxXml(messageDrivenBean);
        }
      }
    }
  }
  
  public Report verify(
  ) {
    this.verify(this.report);
    return this.report;
  }

  /* (non-Javadoc)
   * @see org.openmdx.kernel.application.configuration.Configuration#validate()
   */
  public Report validate() {
      return verify();
  }
  
  public void verify(
    Report report
  ) {
    super.verify(report);
       
    if (this.getModuleURI() == null || this.getModuleURI().length() == 0)
    {
      report.addError("no module ID defined for module");
    }
  }

  public URL[] getModuleClassPath(
  ) {
    return this.moduleClassPath;
  }

  public void setModuleClassPath(
    URL[] moduleClassPath
  ) {
    this.moduleClassPath = moduleClassPath;
  }

  public URL[] getApplicationClassPath(
  ) {
    return this.applicationClassPath;
  }

  public void setApplicationClassPath(
    URL[] applicationClassPath
  ) {
    this.applicationClassPath = applicationClassPath;
  }

  public Collection<BeanDeploymentDescriptor> getComponents(
  ) {
    return Collections.unmodifiableCollection(this.components);
  }
  
  public String getDisplayName(
  ) {
    return this.displayName;
  }
  
  public String getModuleURI(
  ) {
    return this.moduleId;
  }
  
  public BeanDeploymentDescriptor getComponentByName(
    String name
  ) {
    for(
        Iterator<BeanDeploymentDescriptor> i = this.components.listIterator();
        i.hasNext();
    ){
        BeanDeploymentDescriptor bdd = i.next();
        if(bdd.getName().equals(name)) return bdd;
    }
    return null;
  }

  public ApplicationDeploymentDescriptor getOwner(
  ) {
    return this.owner;
  }

  protected final Report report;
  protected final List<BeanDeploymentDescriptor> components = new ArrayList<BeanDeploymentDescriptor>();
  private final String moduleId;
  protected String displayName = null;
  private URL[] moduleClassPath = null;
  private URL[] applicationClassPath = null;
  private final ApplicationDeploymentDescriptor owner;
  private final URL contextUrl;
  private Map<String, Map> containerTransactions = null; 
  
}
