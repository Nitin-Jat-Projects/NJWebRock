package com.thinking.machines.webrock;
import java.util.*;
import java.io.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.thinking.machines.webrock.pojo.*;
import com.thinking.machines.webrock.exceptions.*;
import com.thinking.machines.webrock.annotations.*;
import com.thinking.machines.webrock.model.*;
import java.lang.reflect.*;

public class ServiceDoc
{
public static void main(String[] args) throws Exception
{
if (args.length != 2)
{
System.out.println("Usage: java -classpath (classpath) com.thinking.machines.webrock.ServicesDoc <classes folder> <output PDF path>");
return;
}

String classesFolder = args[0];
String pdfOutputPath = args[1];

File rootDir = new File(classesFolder);
if (!rootDir.exists())
{
System.out.println("Provided classes folder does not exist.");
return;
}

ArrayList<String> classNames = new ArrayList<>();
scanForClassNames(rootDir, "", classNames);

Map<String, Service> serviceMap = new TreeMap<>();

for (String className : classNames)
{
try
{
Class<?> cls = Class.forName(className);
if (!cls.isAnnotationPresent(Path.class)) continue;

Path classPath = cls.getAnnotation(Path.class);
boolean classSecured = cls.isAnnotationPresent(SecuredAccess.class);
boolean injectRequestScope = cls.isAnnotationPresent(InjectRequestScope.class);
boolean injectSessionScope = cls.isAnnotationPresent(InjectSessionScope.class);
boolean injectApplicationScope = cls.isAnnotationPresent(InjectApplicationScope.class);
boolean injectApplicationDir = cls.isAnnotationPresent(InjectApplicationDirectory.class);
boolean classGET = cls.isAnnotationPresent(GET.class);
boolean classPOST = cls.isAnnotationPresent(POST.class);

Method[] methods = cls.getDeclaredMethods();
for (Method method : methods)
{
if (!method.isAnnotationPresent(Path.class)) continue;

Path methodPath = method.getAnnotation(Path.class);
Service service = new Service();
service.setServiceClass(cls);
service.setService(method);
service.setPath(classPath.value() + methodPath.value());
service.setSecuredAccess(classSecured || method.isAnnotationPresent(SecuredAccess.class));
service.setInjectRequestScope(injectRequestScope);
service.setInjectSessionScope(injectSessionScope);
service.setInjectApplicationScope(injectApplicationScope);
service.setInjectApplicationDirectory(injectApplicationDir);
service.setIsGetAllowed(classGET || method.isAnnotationPresent(GET.class));
service.setIsPostAllowed(classPOST || method.isAnnotationPresent(POST.class));

if (method.isAnnotationPresent(Forward.class))
{
service.setForwardTo(method.getAnnotation(Forward.class).value());
}
else
{
service.setForwardTo("None");
}

if (method.isAnnotationPresent(OnStartup.class))
{
service.setRunOnStart(true);
service.setPriority(method.getAnnotation(OnStartup.class).priority());
}
else
{
service.setRunOnStart(false);
service.setPriority(0);
}

serviceMap.put(service.getPath(), service);
}
}
catch (ClassNotFoundException | NoClassDefFoundError ex)
{
System.out.println("Could not load class: " + className);
}
catch (Exception ex)
{
ex.printStackTrace();
}
}

generatePDF(serviceMap, pdfOutputPath);
System.out.println("Service documentation generated at: " + pdfOutputPath);
}

private static void generatePDF(Map<String, Service> services, String outputPath) throws Exception
{
Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
PdfWriter.getInstance(doc, new FileOutputStream(outputPath));
doc.open();

Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
Font monoFont = FontFactory.getFont(FontFactory.COURIER, 10);

Paragraph title = new Paragraph("NJWebRock Service Documentation", titleFont);
title.setAlignment(Element.ALIGN_CENTER);
doc.add(title);
doc.add(new Paragraph(" "));

for (Map.Entry<String, Service> entry : services.entrySet())
{
Service s = entry.getValue();
Method m = s.getService();

doc.add(new Paragraph("Service Path: " + entry.getKey(), sectionFont));
doc.add(new Paragraph("Class       : " + s.getServiceClass().getName(), normalFont));
doc.add(new Paragraph("Method      : " + m.getName(), normalFont));
doc.add(new Paragraph("Return Type : " + m.getReturnType().getSimpleName(), normalFont));

Parameter[] params = m.getParameters();
java.util.List<String> paramItems = new ArrayList<>();
for (int i = 0; i < params.length; i++)
{
Parameter p = params[i];
String paramStr = p.getType().getSimpleName() + " " + p.getName();
paramItems.add(paramStr);
}

String parameterLine;
if (paramItems.isEmpty())
{
parameterLine = "None";
}
else
{
parameterLine = String.join(", ", paramItems);
}

doc.add(new Paragraph("Parameters  : " + parameterLine, normalFont));
doc.add(new Paragraph("GET Allowed : " + s.getIsGetAllowed(), normalFont));
doc.add(new Paragraph("POST Allowed: " + s.getIsPostAllowed(), normalFont));
doc.add(new Paragraph("Secured     : " + s.getSecuredAccess(), normalFont));

String forwardTo;
if (s.getForwardTo() == null || s.getForwardTo().equals("None"))
{
forwardTo = "None";
}
else
{
forwardTo = s.getForwardTo();
}
doc.add(new Paragraph("Forward To  : " + forwardTo, normalFont));

if (s.getRunOnStart())
{
doc.add(new Paragraph("Startup     : Yes (Priority " + s.getPriority() + ")", normalFont));
}
else
{
doc.add(new Paragraph("Startup     : No", normalFont));
}

java.util.List<String> injectables = new ArrayList<>();
boolean[] flags = {
s.getInjectRequestScope(),
s.getInjectSessionScope(),
s.getInjectApplicationScope(),
s.getInjectApplicationDirectory()
};
String[] names = {
"RequestScope",
"SessionScope",
"ApplicationScope",
"ApplicationDirectory"
};
for (int i = 0; i < flags.length; i++)
{
if (flags[i])
{
injectables.add(names[i]);
}
}

String injectText;
if (injectables.isEmpty())
{
injectText = "None";
}
else
{
injectText = String.join(", ", injectables);
}
doc.add(new Paragraph("Injectables : " + injectText, normalFont));

Paragraph sep = new Paragraph("------------------------------------------------------", monoFont);
sep.setSpacingBefore(10f);
sep.setSpacingAfter(10f);
doc.add(sep);
}

doc.close();
}

private static void scanForClassNames(File folder, String packagePrefix, ArrayList<String> classNames)
{
File[] files = folder.listFiles();
for (File f : files)
{
if (f.isDirectory())
{
scanForClassNames(f, packagePrefix + f.getName() + ".", classNames);
}
else if (f.getName().endsWith(".class"))
{
String className = packagePrefix + f.getName().substring(0, f.getName().length() - 6);
classNames.add(className);
}
}
}
}
