// FrontEnd Plus GUI for JAD
// DeCompiled : _Dataprovider_1_0Remote_Stub.class

package org.openmdx.application.dataprovider.transport.ejb.cci;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.UnexpectedException;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.RemoveException;
import javax.rmi.CORBA.Stub;
import javax.rmi.CORBA.Util;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.CORBA.portable.ServantObject;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.application.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.application.dataprovider.transport.ejb.cci.Dataprovider_1_0Remote;

// Referenced classes of package org.openmdx.compatibility.application.dataprovider.transport.ejb.cci:
//            Dataprovider_1_0Remote

public class _Dataprovider_1_0Remote_Stub extends Stub
    implements Dataprovider_1_0Remote
{

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3978703982766338354L;
    
    private static final String _type_ids[] = {
        "RMI:org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1_0Remote:0000000000000000", "RMI:javax.ejb.EJBObject:0000000000000000"
    };

    public _Dataprovider_1_0Remote_Stub()
    {
    }

    public String[] _ids()
    {
        return _type_ids;
    }

    private Serializable cast_array(java.lang.Object obj)
    {
        return (Serializable)obj;
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

    public EJBHome getEJBHome()
        throws RemoteException
    {
        if(!Util.isLocal(this))
            try
            {
                org.omg.CORBA.portable.InputStream inputstream = null;
                try
                {
                    EJBHome ejbhome1;
                    try
                    {
                        org.omg.CORBA.portable.OutputStream outputstream = _request("_get_EJBHome", true);
                        inputstream = _invoke(outputstream);
                        EJBHome ejbhome = (EJBHome)inputstream.read_Object(javax.ejb.EJBHome.class);
                        return ejbhome;
                    }
                    catch(ApplicationException applicationexception)
                    {
                        inputstream = applicationexception.getInputStream();
                        String s = inputstream.read_string();
                        throw new UnexpectedException(s);
                    }
                    catch(RemarshalException _ex)
                    {
                        ejbhome1 = getEJBHome();
                    }
                    return ejbhome1;
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
        ServantObject servantobject = _servant_preinvoke("_get_EJBHome", javax.ejb.EJBObject.class);
        if(servantobject == null)
            return getEJBHome();
        try
        {
            Throwable throwable1;
            try
            {
                EJBHome ejbhome3 = ((EJBObject)servantobject.servant).getEJBHome();
                EJBHome ejbhome2 = (EJBHome)Util.copyObject(ejbhome3, _orb());
                return ejbhome2;
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

    public Handle getHandle()
        throws RemoteException
    {
        if(!Util.isLocal(this))
            try
            {
                InputStream inputstream = null;
                try
                {
                    Handle handle1;
                    try
                    {
                        org.omg.CORBA.portable.OutputStream outputstream = _request("_get_handle", true);
                        inputstream = (InputStream)_invoke(outputstream);
                        Handle handle = (Handle)inputstream.read_abstract_interface(javax.ejb.Handle.class);
                        return handle;
                    }
                    catch(ApplicationException applicationexception)
                    {
                        inputstream = (InputStream)applicationexception.getInputStream();
                        String s = inputstream.read_string();
                        throw new UnexpectedException(s);
                    }
                    catch(RemarshalException _ex)
                    {
                        handle1 = getHandle();
                    }
                    return handle1;
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
        ServantObject servantobject = _servant_preinvoke("_get_handle", javax.ejb.EJBObject.class);
        if(servantobject == null)
            return getHandle();
        try
        {
            Throwable throwable1;
            try
            {
                Handle handle3 = ((EJBObject)servantobject.servant).getHandle();
                Handle handle2 = (Handle)Util.copyObject(handle3, _orb());
                return handle2;
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

    public java.lang.Object getPrimaryKey()
        throws RemoteException
    {
        if(!Util.isLocal(this))
            try
            {
                org.omg.CORBA.portable.InputStream inputstream = null;
                try
                {
                    java.lang.Object obj1;
                    try
                    {
                        org.omg.CORBA.portable.OutputStream outputstream = _request("_get_primaryKey", true);
                        inputstream = _invoke(outputstream);
                        java.lang.Object obj = Util.readAny(inputstream);
                        return obj;
                    }
                    catch(ApplicationException applicationexception)
                    {
                        inputstream = applicationexception.getInputStream();
                        String s = inputstream.read_string();
                        throw new UnexpectedException(s);
                    }
                    catch(RemarshalException _ex)
                    {
                        obj1 = getPrimaryKey();
                    }
                    return obj1;
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
        ServantObject servantobject = _servant_preinvoke("_get_primaryKey", javax.ejb.EJBObject.class);
        if(servantobject == null)
            return getPrimaryKey();
        try
        {
            Throwable throwable1;
            try
            {
                java.lang.Object obj3 = ((EJBObject)servantobject.servant).getPrimaryKey();
                java.lang.Object obj2 = Util.copyObject(obj3, _orb());
                return obj2;
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

    public boolean isIdentical(EJBObject ejbobject)
        throws RemoteException
    {
        if(!Util.isLocal(this))
            try
            {
                org.omg.CORBA.portable.InputStream inputstream = null;
                try
                {
                    boolean flag1;
                    try
                    {
                        org.omg.CORBA.portable.OutputStream outputstream = _request("isIdentical", true);
                        Util.writeRemoteObject(outputstream, ejbobject);
                        inputstream = _invoke(outputstream);
                        boolean flag = inputstream.read_boolean();
                        return flag;
                    }
                    catch(ApplicationException applicationexception)
                    {
                        inputstream = applicationexception.getInputStream();
                        String s = inputstream.read_string();
                        throw new UnexpectedException(s);
                    }
                    catch(RemarshalException _ex)
                    {
                        flag1 = isIdentical(ejbobject);
                    }
                    return flag1;
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
        ServantObject servantobject = _servant_preinvoke("isIdentical", javax.ejb.EJBObject.class);
        if(servantobject == null)
            return isIdentical(ejbobject);
        try
        {
            Throwable throwable1;
            try
            {
                EJBObject ejbobject1 = (EJBObject)Util.copyObject(ejbobject, _orb());
                boolean flag2 = ((EJBObject)servantobject.servant).isIdentical(ejbobject1);
                return flag2;
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

    public UnitOfWorkReply[] process(ServiceHeader serviceheader, UnitOfWorkRequest aunitofworkrequest[])
        throws RemoteException
    {
        if(!Util.isLocal(this))
            try
            {
                InputStream inputstream = null;
                try
                {
                    UnitOfWorkReply aunitofworkreply1[];
                    try
                    {
                        OutputStream outputstream = (OutputStream)_request("process", true);
                        outputstream.write_value(serviceheader, org.openmdx.application.dataprovider.cci.ServiceHeader.class);
                        outputstream.write_value(cast_array(aunitofworkrequest), org.openmdx.application.dataprovider.cci.UnitOfWorkRequest[].class);
                        inputstream = (InputStream)_invoke(outputstream);
                        UnitOfWorkReply aunitofworkreply[] = (UnitOfWorkReply[])inputstream.read_value(org.openmdx.application.dataprovider.cci.UnitOfWorkReply[].class);
                        return aunitofworkreply;
                    }
                    catch(ApplicationException applicationexception)
                    {
                        inputstream = (InputStream)applicationexception.getInputStream();
                        String s = inputstream.read_string();
                        throw new UnexpectedException(s);
                    }
                    catch(RemarshalException _ex)
                    {
                        aunitofworkreply1 = process(serviceheader, aunitofworkrequest);
                    }
                    return aunitofworkreply1;
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
        ServantObject servantobject = _servant_preinvoke("process", org.openmdx.application.dataprovider.transport.ejb.cci.Dataprovider_1_0Remote.class);
        if(servantobject == null)
            return process(serviceheader, aunitofworkrequest);
        try
        {
            Throwable throwable1;
            try
            {
                java.lang.Object aobj[] = Util.copyObjects(new java.lang.Object[] {
                    serviceheader, aunitofworkrequest
                }, _orb());
                ServiceHeader serviceheader1 = (ServiceHeader)aobj[0];
                UnitOfWorkRequest aunitofworkrequest1[] = (UnitOfWorkRequest[])aobj[1];
                UnitOfWorkReply aunitofworkreply3[] = ((Dataprovider_1_0Remote)servantobject.servant).process(serviceheader1, aunitofworkrequest1);
                UnitOfWorkReply aunitofworkreply2[] = (UnitOfWorkReply[])Util.copyObject(aunitofworkreply3, _orb());
                return aunitofworkreply2;
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

    public void remove()
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
                        org.omg.CORBA.portable.OutputStream outputstream = _request("remove", true);
                        _invoke(outputstream);
                        return;
                    }
                    catch(ApplicationException applicationexception)
                    {
                        obj = (InputStream)applicationexception.getInputStream();
                        String s = ((org.omg.CORBA.portable.InputStream) (obj)).read_string();
                        if(s.equals("IDL:javax/ejb/RemoveEx:1.0"))
                            throw (RemoveException)((InputStream) (obj)).read_value(javax.ejb.RemoveException.class);
                        else
                            throw new UnexpectedException(s);
                    }
                    catch(RemarshalException _ex)
                    {
                        remove();
                    }
                    return;
                }
                finally
                {
                    _releaseReply(((org.omg.CORBA.portable.InputStream) (obj)));
                }
            }
            catch(SystemException systemexception)
            {
                throw Util.mapSystemException(systemexception);
            }
        ServantObject servantobject = _servant_preinvoke("remove", javax.ejb.EJBObject.class);
        if(servantobject == null)
        {
            remove();
            return;
        }
        try
        {
            Throwable throwable1;
            try
            {
                ((EJBObject)servantobject.servant).remove();
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
