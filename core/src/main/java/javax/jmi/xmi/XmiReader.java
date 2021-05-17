package javax.jmi.xmi;

import java.io.InputStream;
import java.io.IOException;
import java.util.Collection;
import javax.jmi.reflect.RefPackage;

@SuppressWarnings("rawtypes")
public interface XmiReader {
    public Collection read(InputStream stream, String URI, RefPackage extent) 
        throws IOException, MalformedXMIException;
    public Collection read(String URI, RefPackage extent) 
        throws IOException, MalformedXMIException;
}