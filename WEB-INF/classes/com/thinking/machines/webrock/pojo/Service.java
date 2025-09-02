package com.thinking.machines.webrock.pojo;
import java.lang.reflect.*;


public class Service
{

private Class serviceClass;
private String path;
private Method service;
private boolean isGetAllowed;
private boolean isPostAllowed;
private String forwardTo;
private boolean runOnStart;
private int priority;
private boolean injectSessionScope;
private boolean injectRequestScope;
private boolean injectApplicationScope;
private boolean injectApplicationDirectory;
private Object parameter;
private boolean securedAccess;

public Service()
{
this.serviceClass=null;
this.path="";
this.service=null;
this.isGetAllowed=false;
this.isPostAllowed=false;
this.forwardTo=null;
this.runOnStart=false;
this.priority=0;
this.injectSessionScope=false;
this.injectRequestScope=false;
this.injectApplicationScope=false;
this.injectApplicationDirectory=false;
this.parameter=null;
this.securedAccess=false;
}
public void setSecuredAccess(boolean securedAccess)
{
this.securedAccess=securedAccess;
}
public boolean getSecuredAccess()
{
return this.securedAccess;
}

public void setParameter(Object parameter)
{
this.parameter=parameter;
}
public Object getParameter()
{
return this.parameter;
}

public boolean getInjectSessionScope()
{
return this.injectSessionScope;
}

public void setInjectSessionScope(boolean injectSessionScope)
{
this.injectSessionScope = injectSessionScope;
}


public boolean getInjectRequestScope()
{
return this.injectRequestScope;
}

public void setInjectRequestScope(boolean injectRequestScope)
{
this.injectRequestScope = injectRequestScope;
}


public boolean getInjectApplicationScope()
{
return injectApplicationScope;
}

public void setInjectApplicationScope(boolean injectApplicationScope)
{
this.injectApplicationScope = injectApplicationScope;
}

public boolean getInjectApplicationDirectory()
{
return this.injectApplicationDirectory;
}

public void setInjectApplicationDirectory(boolean injectApplicationDirectory)
{
this.injectApplicationDirectory = injectApplicationDirectory;
}


public void setRunOnStart(boolean runOnStart)
{
this.runOnStart=runOnStart;
}
public boolean getRunOnStart()
{
return this.runOnStart;
}

public void setPriority(int priority)
{
this.priority=priority;
}
public int getPriority()
{
return this.priority;
}


public void setForwardTo(String forwardTo)
{
this.forwardTo=forwardTo;
}
public String getForwardTo()
{
return this.forwardTo;
}



public void setIsGetAllowed(boolean isGetAllowed)
{
this.isGetAllowed=isGetAllowed;
}
public boolean getIsGetAllowed()
{
return this.isGetAllowed;
}

public void setIsPostAllowed(boolean isPostAllowed)
{
this.isPostAllowed=isPostAllowed;
}
public boolean getIsPostAllowed()
{
return this.isPostAllowed;
}




public void setServiceClass(Class serviceClass)
{
this.serviceClass=serviceClass;
}
public Class getServiceClass()
{
return this.serviceClass;
}
public void setPath(String path)
{
this.path=path;
}
public String getPath()
{
return this.path;
}
public void setService(Method service)
{
this.service=service;
}
public Method getService()
{
return this.service;
}
}