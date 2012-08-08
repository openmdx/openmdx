package org.openmdx.resource.http;

public class HttpSoapOverServiceMixTest extends HttpSoapTest {
	private String targetURI = "http://localhost:8192/Consumer/";

	private String listenURI = "http://localhost:8193/Provider/";

	public String getTargetURI() {
		return targetURI;
	}

	public String getListenURI() {
		return listenURI;
	}

}
