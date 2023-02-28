package org.openmdx.application.mof.mapping.pimdoc;

import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MagicFileTest {

	@Test
	void when_defaultTextStyleSheet_then_recosurceURL() {
		// Arrange
		final MagicFile magicFile = MagicFile.STYLE_SHEET;
		final MagicFile.Type type = MagicFile.Type.TEXT;
		// Act
		final URL defaultURI = magicFile.getDefault(type);
		// Assert
		Assertions.assertTrue(defaultURI.toExternalForm().endsWith("/org/openmdx/application/mof/mapping/pimdoc/default-style-sheet.css"));
	}
	
	@Test
	void when_defaultImageStyleSheet_then_recosurceURL() {
		// Arrange
		final MagicFile magicFile = MagicFile.STYLE_SHEET;
		final MagicFile.Type type = MagicFile.Type.IMAGE;
		// Act
		final URL defaultURI = magicFile.getDefault(type);
		// Assert
		Assertions.assertTrue(defaultURI.toExternalForm().endsWith("/org/openmdx/application/mof/mapping/pimdoc/default-style-sheet.dots"));
	}

	@Test
	void when_defaultWelcomePageText_then_recosurceURL() {
		// Arrange
		final MagicFile magicFile = MagicFile.WELCOME_PAGE;
		final MagicFile.Type type = MagicFile.Type.TEXT;
		// Act
		final URL defaultURI = magicFile.getDefault(type);
		// Assert
		Assertions.assertTrue(defaultURI.toExternalForm().endsWith("/org/openmdx/application/mof/mapping/pimdoc/default-welcome-page.html"));
	}
	
	@Test
	void when_defaultWelcomePageImage_then_recosurceURL() {
		// Arrange
		final MagicFile magicFile = MagicFile.WELCOME_PAGE;
		final MagicFile.Type type = MagicFile.Type.IMAGE;
		// Act
		final URL defaultURI = magicFile.getDefault(type);
		// Assert
		Assertions.assertTrue(defaultURI.toExternalForm().endsWith("/org/openmdx/application/mof/mapping/pimdoc/default-welcome-page.png"));
	}

}
