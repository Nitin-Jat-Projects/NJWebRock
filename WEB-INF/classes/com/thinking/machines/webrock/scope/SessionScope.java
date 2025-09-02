package com.thinking.machines.webrock.scope;
import javax.servlet.http.*;
import javax.servlet.*;

public class SessionScope
{
private HttpSession httpSession;

public SessionScope()
{
this.httpSession=null;
}

public void setHttpSession(HttpSession httpSession)
{
this.httpSession=httpSession;
}
public HttpSession getHttpSession()
{
return this.httpSession;
}

public void setAttribute(String name ,Object object)
{
this.httpSession.setAttribute(name,object);
}
public Object getAttribute(String name)
{
return this.httpSession.getAttribute(name);
}

}