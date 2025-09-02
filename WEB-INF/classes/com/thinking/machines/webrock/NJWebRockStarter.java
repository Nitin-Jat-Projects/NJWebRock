package com.thinking.machines.webrock;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import com.thinking.machines.webrock.annotations.*;
import com.thinking.machines.webrock.model.*;
import com.thinking.machines.webrock.pojo.*;

public class NJWebRockStarter extends HttpServlet {

@Override
public void init() throws ServletException {
System.out.println("NJWebRockStarter started");

ServletContext context = getServletContext();
String basePackage = context.getInitParameter("SERVICE_PACKAGE_PREFIX");



if (basePackage == null || basePackage.trim().equals("")) {
System.out.println("Context param servicePackagePrefix is missing.");
return;
}

String packagePath=basePackage.replace(".",File.separator);

String classesPath = context.getRealPath("/WEB-INF/classes/"+packagePath);
File rootDir = new File(classesPath);

if (!rootDir.exists()) {
System.out.println("WEB-INF/classes folder does not exist.");
return;
}

ArrayList<String> classNames = new ArrayList<String>();
ArrayList<Service> onStartupMethodList=new ArrayList<Service>();
scanForClassNames(rootDir, basePackage+".", classNames);

Path classPathValue = null;
Path methodPathValue = null;
Forward forwardToValue=null;
OnStartup onStartup=null;
SecuredAccess securedAccess=null;




boolean isGetAllowed=false;
boolean isPostAllowed=false;
String forwardTo=null;
boolean runOnStart=false;
boolean injectRequestScope=false;
boolean injectSessionScope=false;
boolean injectApplicationScope=false;
boolean injectApplicationDirectory=false;
boolean isSecuredAccess=false;
int priority=0;


WebRockModel webRockModel = new WebRockModel();

int i = 0;
while (i < classNames.size()) {
String className = classNames.get(i);

if (className.startsWith(basePackage + ".")) {
try {
Class cls = Class.forName(className);
//System.out.println("class sdflfkjsflkl : " + className);

if(cls.isAnnotationPresent(SecuredAccess.class))
{
isSecuredAccess=true;
}

if(cls.isAnnotationPresent(InjectRequestScope.class))
{
injectRequestScope=true;
}

if(cls.isAnnotationPresent(InjectSessionScope.class))
{
injectSessionScope=true;
}

if(cls.isAnnotationPresent(InjectApplicationScope.class))
{
injectApplicationScope=true;
}

if(cls.isAnnotationPresent(InjectApplicationDirectory.class))
{
injectApplicationDirectory=true;
}


if(cls.isAnnotationPresent(GET.class))
{
//class ke karan get ki value true kardi
isGetAllowed=true;
}

if(cls.isAnnotationPresent(POST.class))
{
//class ke karan post ki value true kar di
isPostAllowed=true;
}


if (cls.isAnnotationPresent(Path.class)) {
classPathValue = (Path) cls.getAnnotation(Path.class);
//System.out.println("Class: " + className + " => " + classPathValue.value());

Method[] methods = cls.getDeclaredMethods();
int j = 0;
while (j < methods.length) {
Method method = methods[j];

if(method.isAnnotationPresent(SecuredAccess.class))
{
isSecuredAccess=true;
}


if(method.isAnnotationPresent(OnStartup.class))
{
if(method.getReturnType().equals(void.class) && method.getParameterCount()==0)
{
onStartup=(OnStartup) method.getAnnotation(OnStartup.class);
priority=onStartup.priority();
runOnStart=true;
}
else
{
System.out.println("@onStartup annotation can only be applied to methods which has void return type and accepts zero parameter");
return;
}
}


if(method.isAnnotationPresent(Forward.class))
{
forwardToValue=(Forward) method.getAnnotation(Forward.class);
forwardTo=forwardToValue.value();
}



if(method.isAnnotationPresent(GET.class))
{
//method par annotation ke karan get ki value true kardo
isGetAllowed=true;
}
if(method.isAnnotationPresent(POST.class))
{
//method par annotation ke karan post ki value true kardo
isPostAllowed=true;
}

if (method.isAnnotationPresent(Path.class)) {
methodPathValue = (Path) method.getAnnotation(Path.class);
//System.out.println("Method: " + className + "." + method.getName() + " => " + methodPathValue.value());

//System.out.println("runOnStart : " +runOnStart);
//System.out.println("Priority : " + priority);


Service service = new Service();
service.setServiceClass(cls);
service.setPath(classPathValue.value() + methodPathValue.value());
service.setIsGetAllowed(isGetAllowed);
service.setIsPostAllowed(isPostAllowed);
service.setForwardTo(forwardTo);
service.setRunOnStart(runOnStart);
service.setPriority(priority);
service.setInjectRequestScope(injectRequestScope);
service.setInjectSessionScope(injectSessionScope);
service.setInjectApplicationScope(injectApplicationScope);
service.setInjectApplicationDirectory(injectApplicationDirectory);
service.setSecuredAccess(isSecuredAccess);
service.setService(method);

if(runOnStart==true)
{
onStartupMethodList.add(service);
}

runOnStart=false;
priority=0;
isGetAllowed=false;
isPostAllowed=false;
forwardTo=null;

webRockModel.map.put(classPathValue.value() + methodPathValue.value(), service);
}
j++;
}
}
} catch (ClassNotFoundException cnfe) {
System.out.println("Class not found: " + className);
} catch (Throwable t) {
t.printStackTrace();
}
}
i++;
injectRequestScope=false;
injectSessionScope=false;
injectApplicationScope=false;
injectApplicationDirectory=false;
isSecuredAccess=false;
}

callMethodsOnStartup(onStartupMethodList);
context.setAttribute("WEB_ROCK_MODEL", webRockModel);
//System.out.println("WebRockModel stored in application scope.");
}


//it takes array list as parameter which stores service type objects for those //methods which are to be called on start of server
//first sort the list on the basis of priority (property in service class object) then call the methods
private void callMethodsOnStartup(ArrayList<Service> onStartupMethodList)
{
onStartupMethodList.sort((s1,s2)->Integer.compare(s1.getPriority(),s2.getPriority()));
try
{
for(Service s : onStartupMethodList)
{
Method met=s.getService();
Class c=s.getServiceClass();
Object o=c.newInstance();
met.invoke(o);
}
}catch(Exception e)
{
System.out.println(e);
}
}


//scans folder and adds all the files in the array list classNames which ends with .class
private void scanForClassNames(File folder, String packagePrefix, ArrayList<String> classNames) {
File[] files = folder.listFiles();
int i = 0;
while (i < files.length) {
File f = files[i];
if (f.isDirectory()) {
scanForClassNames(f, packagePrefix + f.getName() + ".", classNames);
} else {
String fileName = f.getName();
if (fileName.endsWith(".class")) {
String className = packagePrefix + fileName.substring(0, fileName.length() - 6);
classNames.add(className);
}
}
i++;
}
}




}
