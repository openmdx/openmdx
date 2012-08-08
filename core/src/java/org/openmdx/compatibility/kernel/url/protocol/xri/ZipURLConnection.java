/*
 * Created on Jun 15, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.openmdx.compatibility.kernel.url.protocol.xri;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Permission;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.openmdx.kernel.url.protocol.XRI_2Protocols;
import org.openmdx.kernel.url.protocol.XriAuthorities;

/**
 * An delegating URLConnection support class.
 */
public class ZipURLConnection extends JarURLConnection {

    /**
     * Constructor 
     * 
     * @param xri
     * @param url
     * 
     * @throws IOException
     */
    private ZipURLConnection(
        final URL xri,
        final URL url
    ) throws IOException {
        super(url);	      
        this.xri = xri;
        delegateConnection = (JarURLConnection)url.openConnection();
    }

    /**
     * Constructor
     * 
     * @param xri
     * @throws IOException
     */
    public ZipURLConnection(
        final URL xri
    ) throws IOException {
        this(xri,toURL(xri));
    }

    /**
     * 
     */
    protected URL xri;

    /**
     * 
     */
    protected JarURLConnection delegateConnection;


    private final static URL toURL(
        final URL xri
    ) throws MalformedURLException, IOException {
        String path = xri.getFile();
        int i = path.lastIndexOf(XRI_2Protocols.ZIP_SEPARATOR);
        if(i < 0) throw new MalformedURLException(
            "No separator ('" + XRI_2Protocols.ZIP_SEPARATOR + "' found in url " + path
        );
        return new URL(
            JAR_PREFIX + path.substring(XriAuthorities.ZIP_AUTHORITY.length() + 2, i) + 
            JAR_SEPARATOR + path.substring(i + XRI_2Protocols.ZIP_SEPARATOR.length())
        );
    }

    //------------------------------------------------------------------------
    // Extends JarURLConnection
    //------------------------------------------------------------------------

    /**
     * As defined by JarURLConnection.
     * <p>
     * A JAR may be embedded in a ZIP, an EAR, a WAR or a RAR.
     * 
     * @see java.net.JarURLConnection
     */
    public final static String JAR_SEPARATOR = "!/";

    /**
     * The jar protocol
     */
    public final static String JAR_PROTOCOL = "jar";

    /**
     * A derived valu the JAR prefix
     */
    public final static String JAR_PREFIX = JAR_PROTOCOL + ':';

    /**   
     * Return the entry name for this connection. This method
     * returns null if the JAR file URL corresponding to this
     * connection points to a JAR file and not a JAR file entry.
     *
     * @return the entry name for this connection, if any.  
     */
    @Override
    public String getEntryName() {
        return this.delegateConnection.getEntryName();
    }

    /**   
     * Return the JAR file for this connection. The returned object is
     * not modifiable, and will throw UnsupportedOperationException
     * if the caller attempts to modify it.
     *
     * @return the JAR file for this connection. If the connection is
     * a connection to an entry of a JAR file, the JAR file object is
     * returned
     *
     * @exception IOException if an IOException occurs while trying to
     * connect to the JAR file for this connection.
     *
     * @see #connect
     */
    @Override
    public JarFile getJarFile(
    ) throws IOException {
        return this.delegateConnection.getJarFile();
    }

    /**
     * Returns the Manifest for this connection, or null if none. The
     * returned object is not modifiable, and will throw
     * UnsupportedOperationException if the caller attempts to modify
     * it.
     *
     * @return the manifest object corresponding to the JAR file object
     * for this connection.
     *
     * @exception IOException if getting the JAR file for this
     * connection causes an IOException to be trown.
     *
     * @see #getJarFile
     */
    @Override
    public Manifest getManifest() throws IOException {
        return this.delegateConnection.getManifest();
    }

    /**  
     * Return the JAR entry object for this connection, if any. This
     * method returns null if the JAR file URL corresponding to this
     * connection points to a JAR file and not a JAR file entry. The
     * returned object is not modifiable, and will throw
     * UnsupportedOperationException if the caller attempts to modify
     * it.  
     *
     * @return the JAR entry object for this connection, or null if
     * the JAR URL for this connection points to a JAR file.
     *
     * @exception IOException if getting the JAR file for this
     * connection causes an IOException to be trown.
     *
     * @see #getJarFile
     * @see #getJarEntry
     */
    @Override
    public JarEntry getJarEntry(
    ) throws IOException {
        return this.delegateConnection.getJarEntry();
    }

    /**
     * Return the Attributes object for this connection if the URL
     * for it points to a JAR file entry, null otherwise.
     * 
     * @return the Attributes object for this connection if the URL
     * for it points to a JAR file entry, null otherwise.  
     *
     * @exception IOException if getting the JAR entry causes an
     * IOException to be thrown.
     *
     * @see #getJarEntry
     */
    @Override
    public Attributes getAttributes(
    ) throws IOException {
        return this.delegateConnection.getAttributes();
    }

    /**    
     * Returns the main Attributes for the JAR file for this
     * connection.
     *
     * @return the main Attributes for the JAR file for this
     * connection.
     *
     * @exception IOException if getting the manifest causes an
     * IOException to be thrown.
     *
     * @see #getJarFile
     * @see #getManifest 
     */
    @Override
    public Attributes getMainAttributes(
    ) throws IOException { 
        return this.delegateConnection.getMainAttributes();
    }

    /**
     * Return the Certificate object for this connection if the URL
     * for it points to a JAR file entry, null otherwise. This method 
     * can only be called once
     * the connection has been completely verified by reading
     * from the input stream until the end of the stream has been
     * reached. Otherwise, this method will return <code>null</code>
     * 
     * @return the Certificate object for this connection if the URL
     * for it points to a JAR file entry, null otherwise.  
     *
     * @exception IOException if getting the JAR entry causes an
     * IOException to be thrown.
     *
     * @see #getJarEntry
     */
    @Override
    public java.security.cert.Certificate[] getCertificates(
    ) throws IOException {
        return this.delegateConnection.getCertificates();
    }


    //------------------------------------------------------------------------
    // Extends URLConnection
    //------------------------------------------------------------------------

    @Override
    public void connect() throws IOException
    {
        try {
            delegateConnection.connect();
        } catch (IOException ioException) {
            throw new IOException(
                this.xri + ": " + ioException.getMessage()
            );
        }
    }

    @Override
    public URL getURL() {
        return delegateConnection.getURL();
    }

    @Override
    public int getContentLength() {
        return delegateConnection.getContentLength();
    }

    @Override
    public String getContentType() {
        return delegateConnection.getContentType();
    }

    @Override
    public String getContentEncoding() {
        return delegateConnection.getContentEncoding();
    }

    @Override
    public long getExpiration() {
        return delegateConnection.getExpiration();
    }

    @Override
    public long getDate() {
        return delegateConnection.getDate();
    }

    @Override
    public long getLastModified() {
        return delegateConnection.getLastModified();
    }

    @Override
    public String getHeaderField(String name) {
        return delegateConnection.getHeaderField(name);
    }

    @Override
    public Map<String,List<String>> getHeaderFields() {
        return delegateConnection.getHeaderFields();
    }

    @Override
    public int getHeaderFieldInt(String name, int _default) {
        return delegateConnection.getHeaderFieldInt(name, _default);
    }

    @Override
    public long getHeaderFieldDate(String name, long _default) {
        return delegateConnection.getHeaderFieldDate(name, _default);
    }

    @Override
    public String getHeaderFieldKey(int n) {
        return delegateConnection.getHeaderFieldKey(n);
    }

    @Override
    public String getHeaderField(int n) {
        return delegateConnection.getHeaderField(n);
    }

    @Override
    public Object getContent() throws IOException {
        try {
            return delegateConnection.getContent();
        } catch (IOException ioException) {
            throw new IOException(
                this.xri + ": " + ioException.getMessage()
            );
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getContent(Class[] classes) throws IOException {
        try {
            return delegateConnection.getContent(classes);
        } catch (IOException ioException) {
            throw new IOException(
                this.xri + ": " + ioException.getMessage()
            );
        }
    }

    @Override
    public Permission getPermission() throws IOException {
        try {
            return delegateConnection.getPermission();
        } catch (IOException ioException) {
            throw new IOException(
                this.xri + ": " + ioException.getMessage()
            );
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try{
            return delegateConnection.getInputStream();
        } catch (IOException ioException) {
            throw new IOException(
                this.xri + ": " + ioException.getMessage()
            );
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        try {
            return delegateConnection.getOutputStream();
        } catch (IOException ioException) {
            throw new IOException(
                this.xri + ": " + ioException.getMessage()
            );
        }
    }

    @Override
    public String toString() {
        return super.toString() + "{ " + delegateConnection + " }";
    }

    @Override
    public void setDoInput(boolean doinput) {
        delegateConnection.setDoInput(doinput);
    }

    @Override
    public boolean getDoInput() {
        return delegateConnection.getDoInput();
    }

    @Override
    public void setDoOutput(boolean dooutput) {
        delegateConnection.setDoOutput(dooutput);
    }

    @Override
    public boolean getDoOutput() {
        return delegateConnection.getDoOutput();
    }

    @Override
    public void setAllowUserInteraction(boolean allowuserinteraction) {
        delegateConnection.setAllowUserInteraction(allowuserinteraction);
    }

    @Override
    public boolean getAllowUserInteraction() {
        return delegateConnection.getAllowUserInteraction();
    }

    @Override
    public void setUseCaches(boolean usecaches) {
        delegateConnection.setUseCaches(usecaches);
    }

    @Override
    public boolean getUseCaches() {
        return delegateConnection.getUseCaches();
    }

    @Override
    public void setIfModifiedSince(long ifmodifiedsince) {
        delegateConnection.setIfModifiedSince(ifmodifiedsince);
    }

    @Override
    public long getIfModifiedSince() {
        return delegateConnection.getIfModifiedSince();
    }

    @Override
    public boolean getDefaultUseCaches() {
        return delegateConnection.getDefaultUseCaches();
    }

    @Override
    public void setDefaultUseCaches(boolean defaultusecaches) {
        delegateConnection.setDefaultUseCaches(defaultusecaches);
    }

    @Override
    public void setRequestProperty(String key, String value) {
        delegateConnection.setRequestProperty(key, value);
    }

    @Override
    public void addRequestProperty(String key, String value) {
        delegateConnection.addRequestProperty(key, value);
    }

    @Override
    public String getRequestProperty(String key) {
        return delegateConnection.getRequestProperty(key);
    }

    @Override
    public Map<String,List<String>> getRequestProperties() {
        return delegateConnection.getRequestProperties();
    }

}
