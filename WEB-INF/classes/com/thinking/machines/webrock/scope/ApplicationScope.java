package com.thinking.machines.webrock.scope;
import javax.servlet.http.*;
import javax.servlet.*;

public class ApplicationScope
{
private ServletContext servletContext;

public ApplicationScope()
{
this.servletContext=null;
}
public void setServletContext(ServletContext servletContext)
{
this.servletContext=servletContext;
}
public ServletContext getServletContext()
{
return this.servletContext;
}


public void setAttribute(String name,Object object)
{
this.servletContext.setAttribute(name,object);
}
public Object getAttribute(String name)
{
return this.servletContext.getAttribute(name);
}

}