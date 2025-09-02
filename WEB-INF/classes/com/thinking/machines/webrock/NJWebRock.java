package com.thinking.machines.webrock;
import javax.servlet.*;
import javax.servlet.http.*;
import com.thinking.machines.webrock.model.*;
import com.thinking.machines.webrock.pojo.*;
import com.thinking.machines.webrock.annotations.*;
import com.thinking.machines.webrock.scope.*;
import com.thinking.machines.webrock.exceptions.*;
import java.util.*;
import java.lang.reflect.*;
import java.io.*;
import com.google.gson.*;


public class NJWebRock extends HttpServlet
{

private WebRockModel model=null;


private void start(Service service , HttpServletRequest request , HttpServletResponse response)
{
try
{
//get Class and Method reference 
//create object of the class using reflection




Class cls=service.getServiceClass();
Method method=service.getService();
Object object=cls.newInstance();

List<Object> arguments = new ArrayList<>();
Object[] orderedArgs=null;
ApplicationScope applicationScope=new ApplicationScope();
applicationScope.setServletContext(getServletContext());

RequestScope requestScope=new RequestScope();
requestScope.setHttpServletRequest(request);

HttpSession httpSession=request.getSession();
SessionScope sessionScope=new SessionScope();
sessionScope.setHttpSession(httpSession);


ApplicationDirectory applicationDirectory = new ApplicationDirectory(new File(System.getProperty("java.io.tmpdir")));

SecuredAccess securedAccess= null;

boolean injectSessionScope=false;
boolean injectApplicationScope=false;
boolean injectRequestScope=false;
boolean injectApplicationDirectory=false;

if (method.isAnnotationPresent(SecuredAccess.class)) {
    securedAccess = method.getAnnotation(SecuredAccess.class);
}

if (securedAccess == null && cls.isAnnotationPresent(SecuredAccess.class)) {
    securedAccess = (SecuredAccess)cls.getAnnotation(SecuredAccess.class);
}

if (securedAccess != null) {
String className = securedAccess.checkPost(); // e.g., "bobby.test.Test"
String methodName = securedAccess.guard();     // e.g., "test"

    try {
        Class<?> guardClass = Class.forName(className);
        Object guardObject = guardClass.getDeclaredConstructor().newInstance();
Method guardMethod=null;
          Method[] methods = guardClass.getDeclaredMethods(); // or getMethods() for public methods including inherited
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                guardMethod = m;
                break;
            }
        }
  

 if (guardMethod == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Guard method not found: " + methodName);
            return;
        }

Parameter[] parameters=guardMethod.getParameters();

for(int i = 0; i < parameters.length; i++)
{
    //if (arguments[i] != null) continue; // already filled by @RequestParameter block

    Parameter param = parameters[i];
    Class<?> type = param.getType();

    if(type.equals(ApplicationScope.class))
    {
arguments.add(applicationScope);
       // arguments[i] = applicationScope;
    }
    else if(type.equals(RequestScope.class))
    {
arguments.add(requestScope);        
//arguments[i] = requestScope;
    }
    else if(type.equals(SessionScope.class))
    {
arguments.add(sessionScope);        
//arguments[i] = sessionScope;
    }
    else if(type.equals(ApplicationDirectory.class))
    {
arguments.add(applicationDirectory);        
//arguments[i] = applicationDirectory;
    }
}




orderedArgs = new Object[parameters.length];
for (int i = 0; i < parameters.length; i++) {
Class<?> paramType = parameters[i].getType();
for (Object arg : arguments) {
if (arg == null) continue;
Class<?> argClass = arg.getClass();
if (paramType.isPrimitive()) {
if ((paramType == int.class && argClass == Integer.class) ||
(paramType == long.class && argClass == Long.class) ||
(paramType == short.class && argClass == Short.class) ||
(paramType == byte.class && argClass == Byte.class) ||
(paramType == boolean.class && argClass == Boolean.class) ||
(paramType == char.class && argClass == Character.class) ||
(paramType == float.class && argClass == Float.class) ||
(paramType == double.class && argClass == Double.class)) {
orderedArgs[i] = arg;
break;
}
} else {
if (paramType.isInstance(arg)) {
orderedArgs[i] = arg;
break;
}
}
}
}

if(guardClass.isAnnotationPresent(InjectSessionScope.class))
{
injectSessionScope=true;
}
if(guardClass.isAnnotationPresent(InjectRequestScope.class))
{
injectRequestScope=true;
}
if(guardClass.isAnnotationPresent(InjectApplicationScope.class))
{
injectApplicationScope=true;
}
if(guardClass.isAnnotationPresent(InjectApplicationDirectory.class))
{
injectApplicationDirectory=true;
}


if(injectSessionScope==true)
{
try
{
Method setSessionScope=guardClass.getMethod("setSessionScope",SessionScope.class);
setSessionScope.invoke(guardObject,sessionScope);
}catch(NoSuchMethodException ee)
{
 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Missing setter: setSessionScope() in " + guardClass);
return;
}
}


//to check for injectRequestScope if it is present then check for setter of RequestScope
//if it is present call it and pass RequestScope object which has wrapped HttpServletRequest object
if(injectRequestScope==true)
{
try
{
Method setRequestScope=guardClass.getMethod("setRequestScope",RequestScope.class);
setRequestScope.invoke(guardObject,requestScope);
}catch(NoSuchMethodException ee)
{
 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Missing setter: setRequestScope() in " + guardClass);
return;
}
}



//to check for injectApplicationScope if it is present then check for setter of ApplicationScope
//if it is present call it and pass ApplicationScope object which has wrapped ServletContext object
if(injectApplicationScope==true)
{
try
{
Method setApplicationScope=guardClass.getMethod("setApplicationScope",ApplicationScope.class);
setApplicationScope.invoke(guardObject,applicationScope);
}catch(NoSuchMethodException ee)
{
 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Missing setter: setApplicationScope() in " + guardClass);
return;
}
}



//to check for injectApplicationDirectory if it is present then check for setter of ApplicationDirectory
//if it is present call it and pass ApplicationDirectory object which has wrapped File type object
if(injectApplicationDirectory==true)
{
try
{
Method setApplicationDirectory=guardClass.getMethod("setApplicationDirectory",ApplicationDirectory.class);
setApplicationDirectory.invoke(guardObject,applicationDirectory);
}catch(NoSuchMethodException ee)
{
 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Missing setter: setApplicationDirectory() in " + guardClass);
return;
}
}






















Object result=null;


try {
    // Call guard method
    if (parameters.length == 0) {
        guardMethod.invoke(guardObject);
    } else {
        guardMethod.invoke(guardObject, orderedArgs);
    }

} catch (InvocationTargetException e) {
    Throwable cause = e.getTargetException();

    if (cause instanceof ServiceException) {
   
        System.out.println("Guard blocked access: " + cause.getMessage());
        response.sendError(HttpServletResponse.SC_NOT_FOUND, cause.getMessage());
        return;
    } else {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error in guard");
        return;
    }

} 










    } catch (Exception e) {
        System.out.println("Security guard invocation failed: " + e.getMessage());
        return;
    }
}

String contentType = request.getContentType();
boolean flagOfJsonInRequest = false;
String rawString = "";

if (contentType != null && contentType.toLowerCase().contains("application/json")) {
    flagOfJsonInRequest = true;

    BufferedReader bufferedReader = request.getReader();
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = bufferedReader.readLine()) != null) {
        sb.append(line);
    }
    rawString = sb.toString().trim();

    // Extra check (fallback in case Content-Type was missing)
    if ((rawString.startsWith("{") && rawString.endsWith("}")) ||
        (rawString.startsWith("[") && rawString.endsWith("]"))) {
        flagOfJsonInRequest = true;
    }

}











Gson gson=new Gson();







//eg.  public void showName(@RequestParameter("age") int kalia ){}
Parameter[] parameters = method.getParameters();

//Object[] arguments = new Object[parameters.length];

arguments = new ArrayList<>();
for (int i = 0; i < parameters.length; i++) {
Parameter param = parameters[i];
 Class<?> type = param.getType();

 if (!param.isAnnotationPresent(RequestParameter.class) && flagOfJsonInRequest) {
        try {
            Object obj = gson.fromJson(rawString, type);
           // System.out.println("Injected from JSON into parameter: " + param.getName());
            arguments.add(obj);
flagOfJsonInRequest=false;
            continue;
        } catch (Exception e) {
            System.out.println("⚠️ Could not deserialize JSON into: " + type.getName());
             response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON for parameter of type: " + type.getSimpleName());
            return;
        }
    }






if (param.isAnnotationPresent(RequestParameter.class)) {

RequestParameter annotation = param.getAnnotation(RequestParameter.class);

//paramName means age
String paramName = annotation.value();

//cheak in request that with this name (age) does any data arrived
String stringValue = request.getParameter(paramName);
if(stringValue==null)
{
System.out.println("Nothing come in request with name = " +paramName);
 response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Nothing come in request with name " + paramName);
    return;
}

//get type of the kalia parameter which is int
type = param.getType();
Object value = null;

//according to type of parameter the data arrived in request must be typecasted because return type of request.getParameter is string 
if (type == String.class)
{
value = stringValue;
} 
else if (type == int.class || type == Integer.class)
{
value = Integer.parseInt(stringValue);
} 
else if (type == long.class || type == Long.class)
{
value = Long.parseLong(stringValue);
}
else if (type == short.class || type == Short.class)
{
value = Short.parseShort(stringValue);
}
else if (type == byte.class || type == Byte.class)
{
value = Byte.parseByte(stringValue);
} 
else if (type == float.class || type == Float.class)
{
value = Float.parseFloat(stringValue);
}
else if (type == double.class || type == Double.class) 
{
value = Double.parseDouble(stringValue);
} 
else if (type == boolean.class || type == Boolean.class) 
{
value = Boolean.parseBoolean(stringValue);
}
else if (type == char.class || type == Character.class)
{
if(stringValue!=null)
{
value=stringValue.charAt(0);
}
else
{
value="\0";
}
} 
else 
{
value = stringValue;
}
//System.out.println("value : "+ value);
arguments.add(value);
} else {
//arguments.add(null);
}
}



for(int i = 0; i < parameters.length; i++)
{
    //if (arguments[i] != null) continue; // already filled by @RequestParameter block

    Parameter param = parameters[i];
    Class<?> type = param.getType();

    if(type.equals(ApplicationScope.class))
    {
arguments.add(applicationScope);
       // arguments[i] = applicationScope;
    }
    else if(type.equals(RequestScope.class))
    {
arguments.add(requestScope);        
//arguments[i] = requestScope;
    }
    else if(type.equals(SessionScope.class))
    {
arguments.add(sessionScope);        
//arguments[i] = sessionScope;
    }
    else if(type.equals(ApplicationDirectory.class))
    {
arguments.add(applicationDirectory);        
//arguments[i] = applicationDirectory;
    }
}













//to check for injectSessionScope if it is present then check for setter for the SessionScope
//if it is also present call it and pass SessionScope object which has wrapped HttpSession object
injectSessionScope=service.getInjectSessionScope();
if(injectSessionScope==true)
{
try
{
Method setSessionScope=cls.getMethod("setSessionScope",SessionScope.class);
setSessionScope.invoke(object,sessionScope);
}catch(NoSuchMethodException ee)
{
System.out.println("Please write setter for SessionScope");
 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Missing setter: setSessionScope() in " + cls);
return;
}
}


//to check for injectRequestScope if it is present then check for setter of RequestScope
//if it is present call it and pass RequestScope object which has wrapped HttpServletRequest object
 injectRequestScope=service.getInjectRequestScope();
if(injectRequestScope==true)
{
try
{
Method setRequestScope=cls.getMethod("setRequestScope",RequestScope.class);
setRequestScope.invoke(object,requestScope);
}catch(NoSuchMethodException ee)
{
System.out.println("Please write setter for RequestScope");
 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Missing setter: setRequestScope() in " + cls);
return;
}
}



//to check for injectApplicationScope if it is present then check for setter of ApplicationScope
//if it is present call it and pass ApplicationScope object which has wrapped ServletContext object
 injectApplicationScope=service.getInjectApplicationScope();
if(injectApplicationScope==true)
{
try
{
Method setApplicationScope=cls.getMethod("setApplicationScope",ApplicationScope.class);
setApplicationScope.invoke(object,applicationScope);
}catch(NoSuchMethodException ee)
{
System.out.println("Please write setter for ApplicationScope");
 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Missing setter: setApplicationScope() in " + cls);
return;
}
}



//to check for injectApplicationDirectory if it is present then check for setter of ApplicationDirectory
//if it is present call it and pass ApplicationDirectory object which has wrapped File type object
 injectApplicationDirectory=service.getInjectApplicationDirectory();
if(injectApplicationDirectory==true)
{
try
{
Method setApplicationDirectory=cls.getMethod("setApplicationDirectory",ApplicationDirectory.class);

//still in confusion what and why to do later will clear it
setApplicationDirectory.invoke(object,applicationDirectory);
}catch(NoSuchMethodException ee)
{
System.out.println("Please write setter for ApplicationDirectory");
 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Missing setter: setApplicationDirectory() in " + cls);
return;
}
}



//cheacking for AutoWired fields
Field[] fields = cls.getDeclaredFields();
if (fields != null) {
for (int i = 0; i < fields.length; i++) {
Field field=fields[i];

if (field.isAnnotationPresent(InjectRequestParameter.class)) {
        InjectRequestParameter inject = field.getAnnotation(InjectRequestParameter.class);
        String paramName = inject.value();
        String valueFromRequest = request.getParameter(paramName);

        if (valueFromRequest == null) {
            System.out.println("No value in request for: " + paramName);
            continue;
        }

        String fieldName = field.getName();
        Class<?> fieldType = field.getType();
        String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

        try {
            Method setter = cls.getMethod(setterName, fieldType);
            Object convertedValue = null;

            if (fieldType == String.class) {
                convertedValue = valueFromRequest;
            } else if (fieldType == int.class || fieldType == Integer.class) {
                convertedValue = Integer.parseInt(valueFromRequest);
            } else if (fieldType == long.class || fieldType == Long.class) {
                convertedValue = Long.parseLong(valueFromRequest);
            } else if (fieldType == short.class || fieldType == Short.class) {
                convertedValue = Short.parseShort(valueFromRequest);
            } else if (fieldType == byte.class || fieldType == Byte.class) {
                convertedValue = Byte.parseByte(valueFromRequest);
            } else if (fieldType == float.class || fieldType == Float.class) {
                convertedValue = Float.parseFloat(valueFromRequest);
            } else if (fieldType == double.class || fieldType == Double.class) {
                convertedValue = Double.parseDouble(valueFromRequest);
            } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                convertedValue = Boolean.parseBoolean(valueFromRequest);
            } else if (fieldType == char.class || fieldType == Character.class) {
                convertedValue = valueFromRequest.length() > 0 ? valueFromRequest.charAt(0) : '\0';
            } else {
                convertedValue = valueFromRequest; // fallback for unknown types
            }

            setter.invoke(object, convertedValue);
          //  System.out.println("Injected request param '" + paramName + "' into field '" + //fieldName + "' with value: " + convertedValue);
        } catch (NoSuchMethodException e) {
            System.out.println("Setter not found for field: " + fieldName);

 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Setter not found for the field : " +fieldName);
    return;


        } catch (Exception ex) {
            System.out.println("Injection error for field '" + fieldName + "': " + ex.getMessage());
        }
    }











if (fields[i].isAnnotationPresent(AutoWired.class)) {
AutoWired autoWired = fields[i].getAnnotation(AutoWired.class);
String name = autoWired.name();
String fieldName = fields[i].getName();
Class<?> fieldType = fields[i].getType();
//System.out.println("AutoWired se mila name : " + name);
//System.out.println("fieldName : " + fieldName);
//System.out.println("fieldType : " + fieldType);
String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
try {
Method setter = cls.getMethod(setterName, fieldType);
Object argumentToPassToSetter = null;
if (request.getAttribute(name) != null) {
argumentToPassToSetter = request.getAttribute(name);
}
//System.out.println("argumentTopass from request : " + argumentToPassToSetter);
HttpSession session = request.getSession(false);
if (session != null && session.getAttribute(name) != null) {
argumentToPassToSetter = session.getAttribute(name);
}
//System.out.println("argument to pass from session : " + argumentToPassToSetter);
ServletContext application = request.getServletContext();
if (application.getAttribute(name) != null) {
argumentToPassToSetter = application.getAttribute(name);
}
//System.out.println("argument to pass from application : " + argumentToPassToSetter);
Object convertedValue = null;
if (argumentToPassToSetter != null) {
try {
String stringValue = argumentToPassToSetter.toString();
if (fieldType == String.class) {
convertedValue = stringValue;
} else if (fieldType == int.class || fieldType == Integer.class) {
convertedValue = Integer.parseInt(stringValue);
} else if (fieldType == long.class || fieldType == Long.class) {
convertedValue = Long.parseLong(stringValue);
} else if (fieldType == short.class || fieldType == Short.class) {
convertedValue = Short.parseShort(stringValue);
} else if (fieldType == byte.class || fieldType == Byte.class) {
convertedValue = Byte.parseByte(stringValue);
} else if (fieldType == float.class || fieldType == Float.class) {
convertedValue = Float.parseFloat(stringValue);
} else if (fieldType == double.class || fieldType == Double.class) {
convertedValue = Double.parseDouble(stringValue);
} else if (fieldType == boolean.class || fieldType == Boolean.class) {
convertedValue = Boolean.parseBoolean(stringValue);
} else if (fieldType == char.class || fieldType == Character.class) {
convertedValue = stringValue.length() > 0 ? stringValue.charAt(0) : '\0';
} else {
convertedValue = argumentToPassToSetter;
}
setter.invoke(object, convertedValue);
} catch (Exception ex) {
System.out.println("Error during type conversion for field '" + fieldName + "': " + ex.getMessage());
}
} else {
System.out.println("Nothing found for the name = " +name + " in application,session or request scope");
//setter.invoke(object, null);
}
} catch (NoSuchMethodException e) {
System.out.println("No setter found for field: " + fieldName);
response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No setter found for field :"+fieldName);
return;


} catch (Exception ex) {
System.out.println("Exception during setter injection for field '" + fieldName + "': " + ex.getMessage());
}
}
}
}

if(service.getParameter()!=null)
{
arguments.add(service.getParameter());
}



/*
System.out.println("Kaam ki chhez (parameters[]) : " + Arrays.toString(parameters));
System.out.println("Kaam ki chhez (arguments List) : " + arguments);


System.out.println("arguments ki length : "+ arguments.size());
System.out.println("parameter ki length : "+parameters.length);

*/


orderedArgs = new Object[parameters.length];
for (int i = 0; i < parameters.length; i++) {
Class<?> paramType = parameters[i].getType();
for (Object arg : arguments) {
if (arg == null) continue;
Class<?> argClass = arg.getClass();
if (paramType.isPrimitive()) {
if ((paramType == int.class && argClass == Integer.class) ||
(paramType == long.class && argClass == Long.class) ||
(paramType == short.class && argClass == Short.class) ||
(paramType == byte.class && argClass == Byte.class) ||
(paramType == boolean.class && argClass == Boolean.class) ||
(paramType == char.class && argClass == Character.class) ||
(paramType == float.class && argClass == Float.class) ||
(paramType == double.class && argClass == Double.class)) {
orderedArgs[i] = arg;
break;
}
} else {
if (paramType.isInstance(arg)) {
orderedArgs[i] = arg;
break;
}
}
}
}






//System.out.println("orderedArgs ki length : " + orderedArgs.length);  















//System.out.println("orderedArgs: " + Arrays.toString(orderedArgs));

//session,request,application task is completed means bobby can use there pointer
//Call the actual method but before calling actual method we should check the parameters
//and return type and if it has some parameter we should pass it and if returns something
//and forwards request to other then that should become parameter for the forwarded method
Class<?>[] paramTypes = method.getParameterTypes();
Object returnValueFromMethod = null;

try {
    if (paramTypes.length == 0) {
        returnValueFromMethod = method.invoke(object);
    } else {
        returnValueFromMethod = method.invoke(object, orderedArgs);
    }

    // Send the return value in response
    // sendJSON(returnValueFromMethod);
//System.out.println("return value : " + returnValueFromMethod);

}catch (InvocationTargetException ite) {
    Throwable targetException = ite.getTargetException();

    if (targetException instanceof ServiceException) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", targetException.getMessage());

        String errorJson = new Gson().toJson(errorMap);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(errorJson);
        return;
    }
}




String forwardTo=service.getForwardTo();

//HERE I WANT WHATEVER RETURNED FROM THE METHOD SHOULD BE SEND IN RESPONSE 
//SEND IT AS A JSON OBJECT
if (forwardTo == null && returnValueFromMethod != null) {
    response.setContentType("application/json");
    PrintWriter out = response.getWriter();
    out.print(gson.toJson(returnValueFromMethod));
    out.flush();
    return;
}






if(forwardTo!=null)
{
Service s=model.map.get(forwardTo);
if(s!=null)
{
s.setParameter(returnValueFromMethod);
start(s,request,response);
}
else
{
RequestDispatcher requestDispatcher=request.getRequestDispatcher(forwardTo);
requestDispatcher.forward(request,response);
}
}

}catch(Exception e)
{
System.out.println(e.getMessage());
}
}
//end of start method


public void doGet(HttpServletRequest request, HttpServletResponse response)
{
    try
    {
        String path = request.getRequestURI(); // e.g. "/tmwebrock/schoolService/student/add"
        String contextPath = request.getContextPath(); // e.g. "/tmwebrock"
        String actualPath = path.substring(contextPath.length()); // "/schoolService/student/add"
        String keyToSearchInModelMap = actualPath.substring("/schoolService".length()); // "/student/add"

        model = (WebRockModel) getServletContext().getAttribute("WEB_ROCK_MODEL");
        if (model == null)
        {
            System.out.println("Model is null");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error: model not initialized");
            return;
        }

        Service service = model.map.get(keyToSearchInModelMap);
        if (service == null)
        {
            System.out.println("No resource found for this request: " + keyToSearchInModelMap);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No resource found for: " + keyToSearchInModelMap);
            return;
        }

        boolean isGetAllowed = service.getIsGetAllowed();
        boolean isPostAllowed=service.getIsPostAllowed();
if(!isGetAllowed && !isPostAllowed)
{
System.out.println("neither get nor post request are allowed for  " + service.getPath());
return;
}
        if (!isGetAllowed)
        {
            System.out.println("GET not allowed for: " + service.getPath());
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "GET not allowed for: " + service.getPath());
            return;
        }

        // All checks passed; start the service
        start(service, request, response);

    }
    catch (Exception exception)
    {
        System.out.println("Unhandled exception in doGet: " + exception.getMessage());
        
        try {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected server error");
        } catch (IOException ioException) {
            
        }
    }
}






public void doPost(HttpServletRequest request, HttpServletResponse response)
{
    try
    {
        String path = request.getRequestURI(); // e.g., "/tmwebrock/schoolService/student/add"
        String contextPath = request.getContextPath(); // e.g., "/tmwebrock"
        String actualPath = path.substring(contextPath.length()); // "/schoolService/student/add"

        String keyToSearchInModelMap = actualPath.substring("/schoolService".length()); // "/student/add"

        model = (WebRockModel) getServletContext().getAttribute("WEB_ROCK_MODEL");
        if (model == null)
        {
            System.out.println("Model is null");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error: model not initialized");
            return;
        }

        Service service = model.map.get(keyToSearchInModelMap);
        if (service == null)
        {
            System.out.println("No resource found for this request: " + keyToSearchInModelMap);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No resource found for: " + keyToSearchInModelMap);
            return;
        }

        boolean isPostAllowed = service.getIsPostAllowed();
        boolean isGetAllowed=service.getIsGetAllowed();

if(!isPostAllowed && !isGetAllowed)
{
System.out.println("neither get nor post request are allowed for " + service.getPath());
return;
}

        if (!isPostAllowed)
        {
            System.out.println("POST not allowed for: " + service.getPath());
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "POST not allowed for: " + service.getPath());
            return;
        }

        // All checks passed; start the service
        start(service, request, response);
    }
    catch (Exception exception)
    {
        System.out.println("Unhandled exception in doPost: " + exception.getMessage());
        exception.printStackTrace();
        try {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected server error");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}

}
