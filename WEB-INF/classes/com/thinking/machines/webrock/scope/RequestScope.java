package com.thinking.machines.webrock.scope;
import javax.servlet.http.*;
import javax.servlet.*;

public class RequestScope 
{
private HttpServletRequest httpServletRequest;
public RequestScope()
{
this.httpServletRequest=null;
}

public void setHttpServletRequest(HttpServletRequest httpServletRequest)
{
this.httpServletRequest=httpServletRequest;
}
public HttpServletRequest getHttpServletRequest()
{
return this.httpServletRequest;
}

public void setAttribute(String name,Object object)
{
this.httpServletRequest.setAttribute(name,object);
}

public Object getAttribute(String name)
{
return this.httpServletRequest.getAttribute(name);
}

}