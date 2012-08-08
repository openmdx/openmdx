// FrontEnd Plus GUI for JAD
// DeCompiled : _Dataprovider_1Home_Stub.class

package org.openmdx.compatibility.application.dataprovider.transport.ejb.cci;

import java.rmi.RemoteException;
import java.rmi.UnexpectedException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.RemoveException;
import javax.rmi.CORBA.Stub;
import javax.rmi.CORBA.Util;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.CORBA.portable.ServantObject;

// Referenced classes of package org.openmdx.compatibility.application.dataprovider.transport.ejb.cci:
//            Dataprovider_1_0Remote, Dataprovider_1Home

public class _Dataprovider_1Home_Stub extends Stub
    implements Dataprovider_1Home
{

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3546076952101532472L;

    private static final String _type_ids[] = {
        "RMI:org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1Home:0000000000000000", "RMI:javax.ejb.EJBHome:0000000000000000"
    };

    public _Dataprovider_1Home_Stub()
    {
    }

    public String[] _ids()
    {
        return _type_ids;
    }

    static Class _mthclass$(String s)
    {
        try
        {
            return Class.forName(s);
        }
        catch(ClassNotFoundException classnotfoundexception)
        {
            throw new NoClassDefFoundError(classnotfoundexception.getMessage());
        }
    }

    public Dataprovider_1_0Remote create()
        throws CreateException, RemoteException
    {
        if(!Util.isLocal(this))
            try
            {
                org.omg.CORBA_2_3.portable.InputStream inputstream = null;
                try
                {
                    Dataprovider_1_0Remote dataprovider_1_0remote1;
                    try
                    {
                        org.omg.CORBA.portable.OutputStream outputstream = _request("create", true);
                        inputstream = (org.omg.CORBA_2_3.portable.InputStream)_invoke(outputstream);
                        Dataprovider_1_0Remote dataprovider_1_0remote = (Dataprovider_1_0Remote)inputstream.read_Object(org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1_0Remote.class);
                        return dataprovider_1_0remote;
                    }
                    catch(ApplicationException applicationexception)
                    {
                        inputstream = (org.omg.CORBA_2_3.portable.InputStream)applicationexception.getInputStream();
                        String s = inputstream.read_string();
                        if(s.equals("IDL:javax/ejb/CreateEx:1.0"))
                            throw (CreateException)inputstream.read_value(javax.ejb.CreateException.class);
                        else
                            throw new UnexpectedException(s);
                    }
                    catch(RemarshalException _ex)
                    {
                        dataprovider_1_0remote1 = create();
                    }
                    return dataprovider_1_0remote1;
                }
                finally
                {
                    _releaseReply(inputstream);
                }
            }
            catch(SystemException systemexception)
            {
                throw Util.mapSystemException(systemexception);
            }
        ServantObject servantobject = _servant_preinvoke("create", org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1Home.class);
        if(servantobject == null)
            return create();
        try
        {
            Throwable throwable1;
            try
            {
                Dataprovider_1_0Remote dataprovider_1_0remote3 = ((Dataprovider_1Home)servantobject.servant).create();
                Dataprovider_1_0Remote dataprovider_1_0remote2 = (Dataprovider_1_0Remote)Util.copyObject(dataprovider_1_0remote3, _orb());
                return dataprovider_1_0remote2;
            }
            catch(Throwable throwable)
            {
                throwable1 = (Throwable)Util.copyObject(throwable, _orb());
            }
            if(throwable1 instanceof CreateException)
                throw (CreateException)throwable1;
            else
                throw Util.wrapException(throwable1);
        }
        finally
        {
            _servant_postinvoke(servantobject);
        }
    }

    public EJBMetaData getEJBMetaData()
        throws RemoteException
    {
        if(!Util.isLocal(this))
            try
            {
                org.omg.CORBA_2_3.portable.InputStream inputstream = null;
                try
                {
                    EJBMetaData ejbmetadata1;
                    try
                    {
                        org.omg.CORBA.portable.OutputStream outputstream = _request("_get_EJBMetaData", true);
                        inputstream = (org.omg.CORBA_2_3.portable.InputStream)_invoke(outputstream);
                        EJBMetaData ejbmetadata = (EJBMetaData)inputstream.read_value(javax.ejb.EJBMetaData.class);
                        return ejbmetadata;
                    }
                    catch(ApplicationException applicationexception)
                    {
                        inputstream = (org.omg.CORBA_2_3.portable.InputStream)applicationexception.getInputStream();
                        String s = inputstream.read_string();
                        throw new UnexpectedException(s);
                    }
                    catch(RemarshalException _ex)
                    {
                        ejbmetadata1 = getEJBMetaData();
                    }
                    return ejbmetadata1;
                }
                finally
                {
                    _releaseReply(inputstream);
                }
            }
            catch(SystemException systemexception)
            {
                throw Util.mapSystemException(systemexception);
            }
        ServantObject servantobject = _servant_preinvoke("_get_EJBMetaData", javax.ejb.EJBHome.class);
        if(servantobject == null)
            return getEJBMetaData();
        try
        {
            Throwable throwable1;
            try
            {
                EJBMetaData ejbmetadata3 = ((EJBHome)servantobject.servant).getEJBMetaData();
                EJBMetaData ejbmetadata2 = (EJBMetaData)Util.copyObject(ejbmetadata3, _orb());
                return ejbmetadata2;
            }
            catch(Throwable throwable)
            {
                throwable1 = (Throwable)Util.copyObject(throwable, _orb());
            }
            throw Util.wrapException(throwable1);
        }
        finally
        {
            _servant_postinvoke(servantobject);
        }
    }

    public HomeHandle getHomeHandle()
        throws RemoteException
    {
        if(!Util.isLocal(this))
            try
            {
                org.omg.CORBA_2_3.portable.InputStream inputstream = null;
                try
                {
                    HomeHandle homehandle1;
                    try
                    {
                        org.omg.CORBA.portable.OutputStream outputstream = _request("_get_homeHandle", true);
                        inputstream = (org.omg.CORBA_2_3.portable.InputStream)_invoke(outputstream);
                        HomeHandle homehandle = (HomeHandle)inputstream.read_abstract_interface(javax.ejb.HomeHandle.class);
                        return homehandle;
                    }
                    catch(ApplicationException applicationexception)
                    {
                        inputstream = (org.omg.CORBA_2_3.portable.InputStream)applicationexception.getInputStream();
                        String s = inputstream.read_string();
                        throw new UnexpectedException(s);
                    }
                    catch(RemarshalException _ex)
                    {
                        homehandle1 = getHomeHandle();
                    }
                    return homehandle1;
                }
                finally
                {
                    _releaseReply(inputstream);
                }
            }
            catch(SystemException systemexception)
            {
                throw Util.mapSystemException(systemexception);
            }
        ServantObject servantobject = _servant_preinvoke("_get_homeHandle", javax.ejb.EJBHome.class);
        if(servantobject == null)
            return getHomeHandle();
        try
        {
            Throwable throwable1;
            try
            {
                HomeHandle homehandle3 = ((EJBHome)servantobject.servant).getHomeHandle();
                HomeHandle homehandle2 = (HomeHandle)Util.copyObject(homehandle3, _orb());
                return homehandle2;
            }
            catch(Throwable throwable)
            {
                throwable1 = (Throwable)Util.copyObject(throwable, _orb());
            }
            throw Util.wrapException(throwable1);
        }
        finally
        {
            _servant_postinvoke(servantobject);
        }
    }

    public void remove(java.lang.Object obj)
        throws RemoteException, RemoveException
    {
        if(!Util.isLocal(this))
            try
            {
                java.lang.Object obj1 = null;
                try
                {
                    try
                    {
                        org.omg.CORBA.portable.OutputStream outputstream = _request("remove__java_lang_Object", true);
                        Util.writeAny(outputstream, obj);
                        _invoke(outputstream);
                        return;
                    }
                    catch(ApplicationException applicationexception)
                    {
                        obj1 = (org.omg.CORBA_2_3.portable.InputStream)applicationexception.getInputStream();
                        String s = ((InputStream) (obj1)).read_string();
                        if(s.equals("IDL:javax/ejb/RemoveEx:1.0"))
                            throw (RemoveException)((org.omg.CORBA_2_3.portable.InputStream) (obj1)).read_value(javax.ejb.RemoveException.class);
                        else
                            throw new UnexpectedException(s);
                    }
                    catch(RemarshalException _ex)
                    {
                        remove(obj);
                    }
                    return;
                }
                finally
                {
                    _releaseReply(((InputStream) (obj1)));
                }
            }
            catch(SystemException systemexception)
            {
                throw Util.mapSystemException(systemexception);
            }
        ServantObject servantobject = _servant_preinvoke("remove__java_lang_Object", javax.ejb.EJBHome.class);
        if(servantobject == null)
        {
            remove(obj);
            return;
        }
        try
        {
            Throwable throwable1;
            try
            {
                java.lang.Object obj2 = Util.copyObject(obj, _orb());
                ((EJBHome)servantobject.servant).remove(obj2);
                return;
            }
            catch(Throwable throwable)
            {
                throwable1 = (Throwable)Util.copyObject(throwable, _orb());
            }
            if(throwable1 instanceof RemoveException)
                throw (RemoveException)throwable1;
            else
                throw Util.wrapException(throwable1);
        }
        finally
        {
            _servant_postinvoke(servantobject);
        }
    }

    public void remove(Handle handle)
        throws RemoteException, RemoveException
    {
        if(!Util.isLocal(this))
            try
            {
                java.lang.Object obj = null;
                try
                {
                    try
                    {
                        org.omg.CORBA.portable.OutputStream outputstream = _request("remove__javax_ejb_Handle", true);
                        Util.writeAbstractObject(outputstream, handle);
                        _invoke(outputstream);
                        return;
                    }
                    catch(ApplicationException applicationexception)
                    {
                        obj = (org.omg.CORBA_2_3.portable.InputStream)applicationexception.getInputStream();
                        String s = ((InputStream) (obj)).read_string();
                        if(s.equals("IDL:javax/ejb/RemoveEx:1.0"))
                            throw (RemoveException)((org.omg.CORBA_2_3.portable.InputStream) (obj)).read_value(javax.ejb.RemoveException.class);
                        else
                            throw new UnexpectedException(s);
                    }
                    catch(RemarshalException _ex)
                    {
                        remove(handle);
                    }
                    return;
                }
                finally
                {
                    _releaseReply(((InputStream) (obj)));
                }
            }
            catch(SystemException systemexception)
            {
                throw Util.mapSystemException(systemexception);
            }
        ServantObject servantobject = _servant_preinvoke("remove__javax_ejb_Handle", javax.ejb.EJBHome.class);
        if(servantobject == null)
        {
            remove(handle);
            return;
        }
        try
        {
            Throwable throwable1;
            try
            {
                Handle handle1 = (Handle)Util.copyObject(handle, _orb());
                ((EJBHome)servantobject.servant).remove(handle1);
                return;
            }
            catch(Throwable throwable)
            {
                throwable1 = (Throwable)Util.copyObject(throwable, _orb());
            }
            if(throwable1 instanceof RemoveException)
                throw (RemoveException)throwable1;
            else
                throw Util.wrapException(throwable1);
        }
        finally
        {
            _servant_postinvoke(servantobject);
        }
    }

}
