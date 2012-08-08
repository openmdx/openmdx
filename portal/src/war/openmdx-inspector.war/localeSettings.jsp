<%@ page contentType= "text/html;charset=UTF-8" language= "java" pageEncoding= "UTF-8" %>
<%
  // IMPORTANT: edit with UTF-8 enabled editor ONLY !!!
  //
	// precondition:
	// String localeStr = (String)session.getAttribute("locale");
	// or
	// String localeStr = request.getParameter("locale");

  final String defaultLocale = "en_US";

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
	activeLocales.add("ro_RO");
	activeLocales.add("ru_RU");
	activeLocales.add("sk_SK");
	activeLocales.add("sv_SE");
	activeLocales.add("tr_TR");
	activeLocales.add("zh_CN");

	// test whether requested locale is supported
	if((localeStr == null) ||
		(!localeStr.equals("en_US") &&
		 !localeStr.equals("cs_CZ") &&
		 !localeStr.equals("de_CH") &&
		 !localeStr.equals("es_CO") &&
		 !localeStr.equals("es_MX") &&
		 !localeStr.equals("fa_IR") &&
		 !localeStr.equals("fr_FR") &&
		 !localeStr.equals("it_IT") &&
		 !localeStr.equals("ja_JP") &&
		 !localeStr.equals("nl_NL") &&
		 !localeStr.equals("pl_PL") &&
		 !localeStr.equals("pt_BR") &&
		 !localeStr.equals("ro_RO") &&
		 !localeStr.equals("ru_RU") &&
		 !localeStr.equals("sk_SK") &&
		 !localeStr.equals("sv_SE") &&
		 !localeStr.equals("tr_TR") &&
		 !localeStr.equals("zh_CN") )) {
		localeStr = defaultLocale;
		request.getSession().setAttribute("locale", localeStr);
	}

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
	dir .put("ro_RO", "ltr");
	dir .put("ru_RU", "ltr");
	dir .put("sk_SK", "ltr");
	dir .put("sv_SE", "ltr");
	dir .put("tr_TR", "ltr");
	dir .put("zh_CN", "ltr");

	Map textsJavaScript = new HashMap();
	textsJavaScript.put("en_US", "Warning: Javascript must be enabled");
	textsJavaScript.put("cs_CZ", "Upozornění: Javascript musí být povolen");
	textsJavaScript.put("de_CH", "Warnung: Javascript muss aktiviert sein");
	textsJavaScript.put("es_CO", "Advertencia: Javascript debe estar habilitado");
	textsJavaScript.put("es_MX", "Advertencia: Javascript debe estar habilitado");
	textsJavaScript.put("fa_IR", "Warning: Javascript must be enabled");
	textsJavaScript.put("fr_FR", "Attention: l'exécution de Javascript doit être autorisée");
	textsJavaScript.put("it_IT", "Attenzione: Javascript deve essere abilitato");
	textsJavaScript.put("ja_JP", "警告： Javascript ⟌使用䟯能⟧⟂る必襟⟌⟂り⟾⟙");
	textsJavaScript.put("nl_NL", "Waarschuwing: Javascript moet geactiveerd zijn");
	textsJavaScript.put("pl_PL", "Uwaga: Javascript musi byc aktywowany");
	textsJavaScript.put("pt_BR", "Atenção: Javascript deve estar habilitado");
	textsJavaScript.put("ro_RO", "Avertizare: trebuie acceptate Javascript");
	textsJavaScript.put("ru_RU", "Внимание: должна быть включена поддержка Javascript");
	textsJavaScript.put("sk_SK", "Varovanie: Javascript musí byť povolený");
	textsJavaScript.put("sv_SE", "Varning: Javascript måste vara aktiverat");
	textsJavaScript.put("tr_TR", "Uyarı: Javascript betikleri etkinleştirilmelidir");
	textsJavaScript.put("zh_CN", "警告： Javascript 功能必须䟯用");

	Map textsSessionCookie = new HashMap();
	textsSessionCookie.put("en_US", "Warning: Browser must accept session cookies");
	textsSessionCookie.put("cs_CZ", "Upozornění: Prohlížeß musí přijímat session cookies");
	textsSessionCookie.put("de_CH", "Warnung: Browser muss Session Cookies akzeptieren");
	textsSessionCookie.put("es_CO", "Advertencia: El browser debe aceptar cookies de sesión");
	textsSessionCookie.put("es_MX", "Advertencia: El browser debe aceptar cookies de sesión");
	textsSessionCookie.put("fa_IR", "Warning: Browser must accept session cookies");
	textsSessionCookie.put("fr_FR", "Attention: votre navigateur doit accepter les cookies de session");
	textsSessionCookie.put("it_IT", "Attenzione: il Browser deve accettare i cookie di sessione");
	textsSessionCookie.put("ja_JP", "警告： ブラウザ⟯⿟session cookiesを䟗⟑付⟑る必襟⟌⟂り⟾⟙");
	textsSessionCookie.put("nl_NL", "Waarschuwing: Browser moet cookies accepteren");
	textsSessionCookie.put("pl_PL", "Uwaga: Przeglodarka musi akceptowac cookies");
	textsSessionCookie.put("pt_BR", "Atenção: O Navegador deve aceitar sessões de cook");
	textsSessionCookie.put("ro_RO", "Avertizare: trebuie acceptate cookie-urile de sesiune");
	textsSessionCookie.put("ru_RU", "Внимание: Браузер должен поддерживать session cookies");
	textsSessionCookie.put("sk_SK", "Varovanie: Prehliadaß musí umožňovať sekcie cookies");
	textsSessionCookie.put("sv_SE", "Varning: Webbläsaren måste acceptera session cookies");
	textsSessionCookie.put("tr_TR", "Uyarı: Internet tarayıcısı oturum çerezlerini kabul etmelidir");
	textsSessionCookie.put("zh_CN", "警告： 洟览器必须償许使用 session cookies");

	Map textsLoginFailed = new HashMap();
	textsLoginFailed.put("en_US", "Login failed. Please try again");
	textsLoginFailed.put("cs_CZ", "Přihlášení se nezdařilo. Zkuste znovu");
	textsLoginFailed.put("de_CH", "Anmeldung nicht erfolgreich - bitte noch einmal versuchen");
	textsLoginFailed.put("es_CO", "Login de sesión fallido, favor de intentar nuevamente");
	textsLoginFailed.put("es_MX", "Login de sesión fallido, favor de intentar nuevamente");
	textsLoginFailed.put("fa_IR", "Login failed. Please try again");
	textsLoginFailed.put("fr_FR", "L'authentification a échoué. Merci de réessayer");
	textsLoginFailed.put("it_IT", "Identificazione fallita. Ritenta, prego");
	textsLoginFailed.put("ja_JP", "ログイン⟫失敗⟗⟾⟗⟟。も⟆一度やり直⟗⟦➟⟠⟕⟄");
	textsLoginFailed.put("nl_NL", "Aanmelden niet gelukt. Probeert u het opnieuw");
	textsLoginFailed.put("pl_PL", "Logowanie nieudane. Prosze sprobowac jeszcze raz");
	textsLoginFailed.put("pt_BR", "Falha ao entrar. Favor tentar novamente");
	textsLoginFailed.put("ro_RO", "Autentificare eşuata. Reâncearcă");
	textsLoginFailed.put("ru_RU", "ϟе правильный вход в ПиПтему. ПожалуйПта попробуйте еще раз");
	textsLoginFailed.put("sk_SK", "Prihlásenie zlyhalo. Prosím skúste znova");
	textsLoginFailed.put("sv_SE", "Inloggning misslyckades. Var god försök igen.");
	textsLoginFailed.put("tr_TR", "Kayıt başarısızlığa uğradı. Lütfen yeniden deneyiniz");
	textsLoginFailed.put("zh_CN", "登录失败，请兟试一次");

	Map textsUsername = new HashMap();
	textsUsername.put("en_US", "Username");
	textsUsername.put("cs_CZ", "Uživatelské jméno");
	textsUsername.put("de_CH", "Benutzername");
	textsUsername.put("es_CO", "Usuario");
	textsUsername.put("es_MX", "Usuario");
	textsUsername.put("fa_IR", "&#1588;&#1606;&#1575;&#1587;&#1607; &#1705;&#1575;&#1585;&#1576;&#1585;&#1740;");
	textsUsername.put("fr_FR", "Identifiant");
	textsUsername.put("it_IT", "Nome utente");
	textsUsername.put("ja_JP", "ユーザ䞟");
	textsUsername.put("nl_NL", "Gebruikersnaam");
	textsUsername.put("pl_PL", "Uzytkownik");
	textsUsername.put("pt_BR", "Usuário");
	textsUsername.put("ro_RO", "Utilizator");
	textsUsername.put("ru_RU", "ИмП пользователП");
	textsUsername.put("sk_SK", "Meno používateľa");
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
	textsPassword.put("ro_RO", "Parola");
	textsPassword.put("ru_RU", "Пароль");
	textsPassword.put("sk_SK", "Heslo");
	textsPassword.put("sv_SE", "Lösenord");
	textsPassword.put("tr_TR", "Parola");
	textsPassword.put("zh_CN", "密矟");

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
	textsLogin.put("ro_RO", "Acces");
	textsLogin.put("ru_RU", "Вход в ПиПтему");
	textsLogin.put("sk_SK", "Prihlásiť sa");
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
	textsLocale.put("ro_RO", "Românã (România)");
	textsLocale.put("ru_RU", "РуППкий (РоППиП)");
	textsLocale.put("sk_SK", "Slovensky (Slovensko)");
	textsLocale.put("sv_SE", "Svenska (Sverige)");
	textsLocale.put("tr_TR", "Türkçe (Türkiye)");
	textsLocale.put("zh_CN", "中文 (中国)");
%>