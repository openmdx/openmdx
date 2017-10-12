package org.openmdx.portal.servlet;

public enum CssClass {

	OperationDialogTitle,
	
	abutton,
	active,
	addon,
	alert,
	alertSuccess,
	alertInfo,
	alertWarning,
	alertDanger,
	autocomplete,
	autocompleterInput,
	autocompleterMenu,
	bd,
	btn,
	btnDefault,
	btnSm,
	button,
	caret,
	cellErrorLeft,
	cellErrorRight,
	cellObject,
	collapse,
	colLg2,
	colLg10,
	colMd3,
	colSm2,
	colSm3,
	colSm6,
	colSm9,
	colSm10,
	colSm12,
	colXs1,
	colXs2,
	colXs3,
	colXs4,
	colXs5,
	colXs12,
	containerFluid,
	dashlet,
	dashletTitle,
	dialog,
	disabled,
	divImgPopUp,
	dropdown,
	dropdownMenu,
	dropdownToggle,
	fade,
	field,
	fieldGroup,
	fieldGroupName,
	fieldSpanned,
	fieldSpannedFull,
	fieldindex,
	fieldLabel,
	fieldselection,
	fieldvalue,
	filterAttr,
	filterButtons,
	filterCell,
	filterGap,
	filterHeader,
	filterTable,
	flatsubmit,
	flatsubmithover,
	gContent,
	gTabPanel,
	gap,
	gridCloser,
	gridColTypeCheck,
	gridMenu,
	gridSpacerBottom,
	gridSpacerTop,
	gridTableFull,
	gridTableHeaderFull,
	gridTableRowFull,
	hd,
	header,
	hidden,
	hiddenMd,
	hiddenPrint,
	hiddenSm,
	hiddenXs,
	hilite,
	in,
	inspContent,
	inspTabPanel,
	itable,
	loading,
	locked,
	longText,
	lookupButtons,
	lookupInput,
	lookupSelector,
	lookupTable,
	mandatory,
	menuOpPanel,
	menuOpPanelActions,
	menuOpPanelInfo,
	multiEditDialog,
	multiString,
	multiStringLocked,
	nav,
	navbar,
	navbarNav,
	navbarCollapse,
	navbarFixedTop,
	navbarHeader,
	navbarInverse,
	navbarRight,
	navbarToggle,
	navCondensed,
	navHeader,
	navList,
	navPills,
	navTabs,
	nw,
	panel,
	panelCookieWarning,
	panelJSWarning,
	panelResult,
	picture,
	popUpButton,
	popUpFrame,
	popUpImg,
	popUpTable,
	qualifier,
	qualifierText,
	row,
	selected,
	sfhover,
	shortText,
	sidebarNav,
	ssfNav,
	ssfNavigation,
	ssfNavv,
	submit,
	table,
	tableHover,
	tableStriped,
	tableCondensed,
	tabContent,
	tabPane,
	tableError,
	tableLayout,
	tablePanel,
	tableResponsive,
	textfilter,
	toolbar,
	valueAC,
	valueEmpty,
	valueL,
	valueLG,
	valueLLocked,
	valueMulti,
	valuePicture,
	valueR,
	valueRG,
	visibleSm,
	visibleMd,
	wait,
	workspaceDashboard;

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString(
	) {
		switch(this) {
			case alertSuccess: return "alert-success";
			case alertInfo: return "alert-info";
			case alertWarning: return "alert-warning";
			case alertDanger: return "alert-danger";
			case btnDefault: return "btn-default";
			case btnSm: return "btn-sm";
			case colLg2: return "col-lg-2";
			case colLg10: return "col-lg-10";
			case colMd3: return "col-md-3";
			case colSm2: return "col-sm-2";
			case colSm3: return "col-sm-3";
			case colSm6: return "col-sm-6";
			case colSm9: return "col-sm-9";
			case colSm10: return "col-sm-10";
			case colSm12: return "col-sm-12";
			case colXs1: return "col-xs-1";
			case colXs2: return "col-xs-2";
			case colXs3: return "col-xs-3";
			case colXs4: return "col-xs-4";
			case colXs5: return "col-xs-5";
			case colXs12: return "col-xs-12";
			case containerFluid: return "container-fluid";
			case hiddenMd: return "hidden-md";
			case hiddenPrint: return "hidden-print";
			case hiddenSm: return "hidden-sm";
			case hiddenXs: return "hidden-xs";
			case longText: return "long-text";
			case navbarCollapse: return "navbar-collapse";
			case navbarHeader: return "navbar-header";
			case navbarNav: return "navbar-nav";
			case navbarRight: return "navbar-right";
			case navbarToggle: return "navbar-toggle";
			case navCondensed: return "nav-condensed";
			case navHeader: return "nav-header";
			case navList: return "nav-list";
			case navTabs: return "nav-tabs";
			case shortText: return "short-text";
			case sidebarNav: return "sidebar-nav";
			case ssfNav: return "ssf-nav";
			case ssfNavv: return "ssf-navv";
			case ssfNavigation: return "ssf-navigation";
			case dropdownMenu: return "dropdown-menu";
			case dropdownToggle: return "dropdown-toggle";
			case navbarFixedTop: return "navbar-fixed-top";
			case navbarInverse: return "navbar-inverse";
			case navPills: return "nav-pills";
			case tabContent: return "tab-content";
			case tableHover: return "table-hover";
			case tableStriped: return "table-striped";
			case tableCondensed: return "table-condensed";
			case tabPane: return "tab-pane";
			case tableResponsive: return "table-responsive";
			case visibleSm: return "visible-sm";
			case visibleMd: return "visible-md";
			default: return super.toString();
		}
	}
	
}
