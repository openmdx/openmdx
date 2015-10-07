<%@ page contentType= "text/html;charset=UTF-8" language= "java" pageEncoding= "UTF-8" %>
<%
  // IMPORTANT: edit with UTF-8 enabled editor ONLY !!!
  //
	// precondition:
	// String localeStr = (String)session.getAttribute("locale");
	// or
	// String localeStr = request.getParameter("locale");

  	defaultLocale = "en_US";

	activeLocales.put("en_US", "English (United States)");
	activeLocales.put("cs_CZ", "Česky (Česká republika)");
	activeLocales.put("de_CH", "Deutsch (Schweiz)");
	activeLocales.put("es_CO", "Español (Colombia)");
	activeLocales.put("es_MX", "Español (México)");
	activeLocales.put("fa_IR", "Farsi/Persian (Iran)");
	activeLocales.put("fr_FR", "Français (France)");
	activeLocales.put("it_IT", "Italiano (Italia)");
	activeLocales.put("ja_JP", "日本語 (日本)");
	activeLocales.put("nl_NL", "Nederlands (Nederland)");
	activeLocales.put("pl_PL", "Polski (Polska)");
	activeLocales.put("pt_BR", "Português (Brasil)");
	activeLocales.put("ro_RO", "Românã (România)");
	activeLocales.put("ru_RU", "Русский (Россия)");
	activeLocales.put("sk_SK", "Slovensky (Slovensko)");
	activeLocales.put("sv_SE", "Svenska (Sverige)");
	activeLocales.put("tr_TR", "Türkçe (Türkiye)");
	activeLocales.put("zh_CN", "中文 (中国)");
  	
%>
