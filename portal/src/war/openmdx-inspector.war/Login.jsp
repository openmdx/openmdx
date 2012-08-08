<%@ page contentType= "text/html;charset=UTF-8" language= "java" pageEncoding= "UTF-8" %><%
/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Login.jsp,v 1.36 2007/11/21 04:38:35 cmu Exp $
 * Description: Login.jsp
 * Revision:    $Revision: 1.36 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/11/21 04:38:35 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 *
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
%>
<%@ page session="true" import="
java.util.*,
java.net.*,
java.util.Enumeration,
java.io.PrintWriter"%>
<%
  request.setCharacterEncoding("UTF-8");

  if(request.getSession().getAttribute("ObjectInspectorServlet.ApplicationContext") != null) {
    System.out.println(new Date() + ": Login: removing application context");
    request.getSession().removeAttribute("ObjectInspectorServlet.ApplicationContext");
  }

  String requestURL = request.getRequestURL().toString();
  System.out.println(new Date() + ": Login: requestURL=" + requestURL + "; isRequestedSessionIdFromCookie=" + request.isRequestedSessionIdFromCookie());

  if(request.getParameter("locale") != null) {
    request.getSession().setAttribute(
      "locale",
      request.getParameter("locale")
    );
  }
  if(request.getParameter("timezone") != null) {
    request.getSession().setAttribute(
      "timezone",
      request.getParameter("timezone")
    );
  }

  String locale = (String)session.getAttribute("locale");
  String timezone = (String)session.getAttribute("timezone");

/*
  // Cookie test. Forward should create a session
  String cookieMissingHint = request.isRequestedSessionIdFromCookie()
    ? ""
    : "&cookieError=true";
  if(!request.isRequestedSessionIdFromCookie() && ((request.getParameter("cookieError") == null) || requestURL.endsWith("Login"))) {
      response.sendRedirect("ObjectInspectorServlet?locale=" + locale + (timezone == null ? "" : "&timezone=" + URLEncoder.encode(timezone)) + cookieMissingHint);
  }
*/

  // test whether last login failed
  boolean loginFailed = "true".equals((String)request.getSession().getAttribute("loginFailed"));
  request.getSession().setAttribute("loginFailed", "false");

  // Set default timezone
  if(timezone == null) {
    timezone = TimeZone.getDefault().getID();
    request.getSession().setAttribute("timezone", timezone);
  }

  // test whether requested locale is supported
  if((locale == null) ||
    (!locale.equals("en_US") &&
     !locale.equals("cs_CZ") &&
     !locale.equals("de_CH") &&
     !locale.equals("es_CO") &&
     !locale.equals("es_MX") &&
     !locale.equals("fa_IR") &&
     !locale.equals("fr_FR") &&
     !locale.equals("it_IT") &&
     !locale.equals("ja_JP") &&
     !locale.equals("nl_NL") &&
     !locale.equals("pl_PL") &&
     !locale.equals("pt_BR") &&
     !locale.equals("ru_RU") &&
     !locale.equals("sv_SE") &&
     !locale.equals("tr_TR") &&
     !locale.equals("zh_CN") )) {
    locale = "en_US";
    request.getSession().setAttribute("locale", locale);
  }

  Map textsJavaScript = new HashMap();
  textsJavaScript.put("en_US", "Warning: Javascript must be enabled");
  textsJavaScript.put("cs_CZ", "Upozornění: Javascript musí být povolen");
  textsJavaScript.put("de_CH", "Warnung: Javascript muss aktiviert sein");
  textsJavaScript.put("es_CO", "Advertencia: Javascript debe estar habilitado");
  textsJavaScript.put("es_MX", "Advertencia: Javascript debe estar habilitado");
  textsJavaScript.put("fa_IR", "Warning: Javascript must be enabled");
  textsJavaScript.put("fr_FR", "Attention: l'exécution de Javascript doit être autorisée");
  textsJavaScript.put("it_IT", "Attenzione: Javascript deve essere abilitato");
  textsJavaScript.put("ja_JP", "警告： Javascript が使用可能である必要があります");
  textsJavaScript.put("nl_NL", "Waarschuwing: Javascript moet geactiveerd zijn");
  textsJavaScript.put("pl_PL", "Uwaga: Javascript musi byc aktywowany");
  textsJavaScript.put("pt_BR", "Atenção: Javascript deve estar habilitado");
  textsJavaScript.put("ru_RU", "Внимание: должна быть включена поддержка Javascript");
  textsJavaScript.put("sv_SE", "Varning: Javascript måste vara aktiverat");
  textsJavaScript.put("tr_TR", "Uyarı: Javascript betikleri etkinleştirilmelidir");
  textsJavaScript.put("zh_CN", "警告： Javascript 功能必须启用");

  Map textsSessionCookie = new HashMap();
  textsSessionCookie.put("en_US", "Warning: Browser must accept session cookies");
  textsSessionCookie.put("cs_CZ", "Upozornění: Prohlížeč musí přijímat session cookies");
  textsSessionCookie.put("de_CH", "Warnung: Browser muss Session Cookies akzeptieren");
  textsSessionCookie.put("es_CO", "Advertencia: El browser debe aceptar cookies de sesión");
  textsSessionCookie.put("es_MX", "Advertencia: El browser debe aceptar cookies de sesión");
  textsSessionCookie.put("fa_IR", "Warning: Browser must accept session cookies");
  textsSessionCookie.put("fr_FR", "Attention: votre navigateur doit accepter les cookies de session");
  textsSessionCookie.put("it_IT", "Attenzione: il Browser deve accettare i cookie di sessione");
  textsSessionCookie.put("ja_JP", "警告： ブラウザは、session cookiesを受け付ける必要があります");
  textsSessionCookie.put("nl_NL", "Waarschuwing: Browser moet cookies accepteren");
  textsSessionCookie.put("pl_PL", "Uwaga: Przeglodarka musi akceptowac cookies");
  textsSessionCookie.put("pt_BR", "Atenção: O Navegador deve aceitar sessões de cook");
  textsSessionCookie.put("ru_RU", "Внимание: Браузер должен поддерживать session cookies");
  textsSessionCookie.put("sv_SE", "Varning: Webbläsaren måste acceptera session cookies");
  textsSessionCookie.put("tr_TR", "Uyarı: Internet tarayıcısı oturum çerezlerini kabul etmelidir");
  textsSessionCookie.put("zh_CN", "警告： 浏览器必须允许使用 session cookies");

  Map textsLoginFailed = new HashMap();
  textsLoginFailed.put("en_US", "Login failed. Please try again");
  textsLoginFailed.put("cs_CZ", "Přihlášení se nezdařilo. Zkuste znovu");
  textsLoginFailed.put("de_CH", "Anmeldung nicht erfolgreich - bitte noch einmal versuchen");
  textsLoginFailed.put("es_CO", "Login de sesión fallido, favor de intentar nuevamente");
  textsLoginFailed.put("es_MX", "Login de sesión fallido, favor de intentar nuevamente");
  textsLoginFailed.put("fa_IR", "Login failed. Please try again");
  textsLoginFailed.put("fr_FR", "L'authentification a échoué. Merci de réessayer");
  textsLoginFailed.put("it_IT", "Identificazione fallita. Ritenta, prego");
  textsLoginFailed.put("ja_JP", "ログインに失敗しました。もう一度やり直してください");
  textsLoginFailed.put("nl_NL", "Aanmelden niet gelukt. Probeert u het opnieuw");
  textsLoginFailed.put("pl_PL", "Logowanie nieudane. Prosze sprobowac jeszcze raz");
  textsLoginFailed.put("pt_BR", "Falha ao entrar. Favor tentar novamente");
  textsLoginFailed.put("ru_RU", "Не правильный вход в систему. Пожалуйста попробуйте еще раз");
  textsLoginFailed.put("sv_SE", "Inloggning misslyckades. Var god försök igen.");
  textsLoginFailed.put("tr_TR", "Kayıt başarısızlığa uğradı. Lütfen yeniden deneyiniz");
  textsLoginFailed.put("zh_CN", "登录失败，请再试一次");

  Map textsUsername = new HashMap();
  textsUsername.put("en_US", "Username");
  textsUsername.put("cs_CZ", "Uživatelské jméno");
  textsUsername.put("de_CH", "Benutzername");
  textsUsername.put("es_CO", "Usuario");
  textsUsername.put("es_MX", "Usuario");
  textsUsername.put("fa_IR", "&#1588;&#1606;&#1575;&#1587;&#1607; &#1705;&#1575;&#1585;&#1576;&#1585;&#1740;");
  textsUsername.put("fr_FR", "Identifiant");
  textsUsername.put("it_IT", "Nome utente");
  textsUsername.put("ja_JP", "ユーザ名");
  textsUsername.put("nl_NL", "Gebruikersnaam");
  textsUsername.put("pl_PL", "Uzytkownik");
  textsUsername.put("pt_BR", "Usuário");
  textsUsername.put("ru_RU", "Имя пользователя");
  textsUsername.put("sv_SE", "Användarnamn");
  textsUsername.put("tr_TR", "Kullanıcı");
  textsUsername.put("zh_CN", "用户");

  Map textsPassword = new HashMap();
  textsPassword.put("en_US", "Password");
  textsPassword.put("cs_CZ", "Heslo");
  textsPassword.put("de_CH", "Passwort");
  textsPassword.put("es_CO", "Contraseña");
  textsPassword.put("es_MX", "Contraseña");
  textsPassword.put("fa_IR", "&#1585;&#1605;&#1586; &#1593;&#1576;&#1608;&#1585;");
  textsPassword.put("fr_FR", "Mot de passe");
  textsPassword.put("it_IT", "Password");
  textsPassword.put("ja_JP", "パスワード");
  textsPassword.put("nl_NL", "Wachtwoord");
  textsPassword.put("pl_PL", "Haslo");
  textsPassword.put("pt_BR", "Senha");
  textsPassword.put("ru_RU", "Пароль");
  textsPassword.put("sv_SE", "Lösenord");
  textsPassword.put("tr_TR", "Parola");
  textsPassword.put("zh_CN", "密码");

  Map textsLogin = new HashMap();
  textsLogin.put("en_US", "Login");
  textsLogin.put("cs_CZ", "Přihlášení");
  textsLogin.put("de_CH", "Anmelden");
  textsLogin.put("es_CO", "Inicio de sesión");
  textsLogin.put("es_MX", "Inicio de sesión");
  textsLogin.put("fa_IR", "&#1608;&#1585;&#1608;&#1583; &#1576;&#1607; &#1587;&#1740;&#1587;&#1578;&#1605;");
  textsLogin.put("fr_FR", "Connexion");
  textsLogin.put("it_IT", "Identificazione");
  textsLogin.put("ja_JP", "ログイン");
  textsLogin.put("nl_NL", "Aanmelden");
  textsLogin.put("pl_PL", "Logowanie");
  textsLogin.put("pt_BR", "Entrar");
  textsLogin.put("ru_RU", "Вход в систему");
  textsLogin.put("sv_SE", "Inloggning");
  textsLogin.put("tr_TR", "Kayıt Girişi");
  textsLogin.put("zh_CN", "登录");

  Map textsLocale = new HashMap();
  textsLocale.put("en_US", "English (United States)");
  textsLocale.put("cs_CZ", "Česky (Česká republika)");
  textsLocale.put("de_CH", "Deutsch (Schweiz)");
  textsLocale.put("es_CO", "Español (Colombia)");
  textsLocale.put("es_MX", "Español (México)");
  textsLocale.put("fa_IR", "Farsi/Persian (Iran)");
  textsLocale.put("fr_FR", "Français (France)");
  textsLocale.put("it_IT", "Italiano (Italia)");
  textsLocale.put("ja_JP", "日本語 (日本)");
  textsLocale.put("nl_NL", "Nederlands (Nederland)");
  textsLocale.put("pl_PL", "Polski (Polska)");
  textsLocale.put("pt_BR", "Português (Brasil)");
  textsLocale.put("ru_RU", "Русский (Россия)");
  textsLocale.put("sv_SE", "Svenska (Sverige)");
  textsLocale.put("tr_TR", "Türkçe (Türkiye)");
  textsLocale.put("zh_CN", "中文 (中国)");

  List activeLocales = new LinkedList();
  activeLocales.add("en_US");
  activeLocales.add("cs_CZ");
  activeLocales.add("de_CH");
  activeLocales.add("es_CO");
  activeLocales.add("es_MX");
  activeLocales.add("fa_IR");
  activeLocales.add("fr_FR");
  activeLocales.add("it_IT");
  activeLocales.add("ja_JP");
  activeLocales.add("nl_NL");
  activeLocales.add("pl_PL");
  activeLocales.add("pt_BR");
  activeLocales.add("ru_RU");
  activeLocales.add("sv_SE");
  activeLocales.add("tr_TR");
  activeLocales.add("zh_CN");

  Map dir = new HashMap();
  dir .put("en_US", "ltr");
  dir .put("cs_CZ", "ltr");
  dir .put("de_CH", "ltr");
  dir .put("es_CO", "ltr");
  dir .put("es_MX", "ltr");
  dir .put("fa_IR", "rtl");
  dir .put("fr_FR", "ltr");
  dir .put("it_IT", "ltr");
  dir .put("ja_JP", "ltr");
  dir .put("nl_NL", "ltr");
  dir .put("pl_PL", "ltr");
  dir .put("pt_BR", "ltr");
  dir .put("ru_RU", "ltr");
  dir .put("sv_SE", "ltr");
  dir .put("tr_TR", "ltr");
  dir .put("zh_CN", "ltr");

%>
<!--[if IE]><!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"><![endif]-->
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html dir="<%= dir.get(locale) %>" style="overflow:auto;">
<head>
  <title>openCRX - Login</title>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
  <meta http-equiv="Expires" content="0">
  <link href="<%=request.getContextPath()%>/_style/colors.css" rel="stylesheet" type="text/css">
  <script language="javascript" type="text/javascript" src="<%=request.getContextPath()%>/javascript/portal-all.js"></script>
  <link href="<%=request.getContextPath()%>/_style/ssf.css" rel="stylesheet" type="text/css">
  <!--[if lt IE 7]><script type="text/javascript" src="<%=request.getContextPath()%>/javascript/iehover-fix.js"></script><![endif]-->
  <link href="<%=request.getContextPath()%>/_style/n2default.css" rel="stylesheet" type="text/css">
  <link rel='shortcut icon' href='<%=request.getContextPath()%>/images/favicon.ico' />
</head>
<body onLoad="javascript: document.forms.formLogin.j_username.focus();" style="overflow:visible;">
  <%@ include file="login-header.html" %>
  <div id="header" style="padding:10px 0px 10px 0px;">
    <table dir="ltr" id="headerlayout" style="position:relative;">
      <tr id="headRow">
        <td id="head" colspan="2">
          <table id="info">
            <tr>
              <td id="headerCellLeft"><img id="logoLeft" src="<%=request.getContextPath()%>/images/logoLeft.gif" alt="openCRX - limitless relationship management" title="" /></td>
              <td id="headerCellMiddle"></td>
              <td id="headerCellRight"><img id="logoRight" src="<%=request.getContextPath()%>/images/logoRight.gif" alt="" title="" /></td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
  </div>
  <div style="text-align:center;padding-top:100px;">
  <form name="formLogin" method="POST" action="j_security_check">
    <table style="text-align:left;border-collapse:collapse;margin-left:auto;margin-right:auto;width:550px;border:solid 1px #DDDDDD;">
      <tr class="objectTitle">
        <td colspan="2" width="100%" style="vertical-align: middle;padding:8px;white-space:nowrap;">
          <span style="font-size:18px;"><%= textsLogin.get(locale) %></span>
        </td>
        <td style="vertical-align: middle; padding-right:5px;white-space:nowrap;">
          <ul dir="ltr" id="nav" class="nav" style="width:220px;" onmouseover="sfinit(this);">
            <li id="flyout" style="border-top: solid 1px #DDDDDD;border-bottom: solid 1px #DDDDDD;"><a href="#"><img src="<%=request.getContextPath()%>/images/panel_down.gif" alt="" style="border:none 0px white;float:right;top:-20px;" /><%= locale %> - <%= textsLocale.get(locale) %></a>
              <ul onclick="this.style.left='-999em';" onmouseout="this.style.left='';">
<%
                for (int i = 0; i < activeLocales.size(); i++) {
%>
                  <li><a href="#" onclick="javascript:window.location.href='Login?locale=<%= activeLocales.get(i).toString() %>&timezone=<%= URLEncoder.encode(timezone) %>';"><%= activeLocales.get(i).toString() %> - <%= textsLocale.get(activeLocales.get(i)).toString() %></a></li>
<%
                }
%>
              </ul>
            </li>
          </ul>
        </td>
      </tr>
      <tr>
        <td colspan="3">
          <noscript>
            <div class="panelJSWarning" style="display: block;">
              <a href="helpJsCookie_<%= locale %>.html" target="_blank"><img class="popUpButton" src="<%=request.getContextPath()%>/images/help.gif" width="16" height="16" border="0" alt="" /></a> <%= textsJavaScript.get(locale) %>
            </div>
          </noscript>
          <div id="cookieWarningBlock" class="panelCookieWarning" style="display: none;">
            <a href="helpJsCookie_<%= locale %>.html" target="_blank"><img class="popUpButton" src="<%=request.getContextPath()%>/images/help.gif" width="16" height="16" border="0" onclick="javascript:void(window.open('helpJsCookie_<%= locale %>.html', 'Help', 'fullscreen=no,toolbar=no,status=no,menubar=no,scrollbars=yes,resizable=yes,directories=no,location=no,width=400'));" alt="" /></a> <%= textsSessionCookie.get(locale) %>
          </div>
        </td>
      </tr>
      <tr>
        <td style="height:30px;"></td>
      </tr>
      <tr>
        <td style="vertical-align: middle;padding-left:8px;padding-right:5px;" nowrap>
          <%= textsUsername.get(locale) %>:
	      </td>
	      <td style="vertical-align: middle;padding-right:8px;">
	        <input type="text" name="j_username" title="<%= textsUsername.get(locale) %>">
	      </td>
	      <td>
	      </td>
      </tr>
      <tr>
        <td style="height:2px;"></td>
      </tr>
      <tr>
        <td style="vertical-align: middle;padding-left:8px;padding-right:5px;" nowrap>
	        <%= textsPassword.get(locale) %>:
	      </td>
	      <td style="vertical-align: middle;padding-right:8px;">
	        <input type="password" name="j_password" title="<%= textsPassword.get(locale) %>">
	      </td>
	      <td nowrap style="vertical-align: middle;padding-right:8px;">
	        <span style="vertical-align: bottom;"><input class="submit" type="submit" name="button" value="<%= textsLogin.get(locale) %>" onclick="$('flyout').style.display='none';$('wait').style.visibility='visible';this.disabled=true;this.form.submit();" >&nbsp;<img id="wait" src="<%=request.getContextPath()%>/images/wait.gif" alt="" title="" style="visibility:hidden;" /></span>
	      </td>
      </tr>
      <tr>
        <td colspan="3" style="height:10px;"></td>
      </tr>
      <tr>
        <td colspan="3">
          <%@ include file="login-note.html" %>
        </td>
      </tr>
<%
      if(loginFailed) {
%>
      <tr>
        <td colspan="3" class="cellErrorRight" style="padding:5px;">
          &nbsp;<b>Error</b>: <%= textsLoginFailed.get(locale) %>
        </td>
      </tr>
<%
      }
%>
    </table>
  </form>
  </div>
  <div style="height:100px;"></div>
  <%@ include file="login-footer.html" %>
  <script language="javascript" type="text/javascript">
    if(<%= !request.isRequestedSessionIdFromCookie() && (request.getParameter("cookieError") != null) %>) {
      if($('cookieWarningBlock')) {
        $('cookieWarningBlock').style.display = 'block';
      }
    }
    var fullRequest = '<%= request.getRequestURL()%>' + '<%= request.getQueryString() == null ? "" : "?" + request.getQueryString() %>';
    if (fullRequest != location.href) {window.location.href = fullRequest;} // never embed the login page in another page
  </script>
</body>
</html>
