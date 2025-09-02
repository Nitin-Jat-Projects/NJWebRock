package com.thinking.machines.webrock;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import com.google.gson.*;

public class NJWebRockJSFileGeneratorOnStartup extends HttpServlet 
{
public void init()
{
ServletContext ctx=getServletContext();
String contextPath=ctx.getContextPath();
String jsonFileName=getServletConfig().getInitParameter("jsonFileName");
if(jsonFileName==null) 
{
System.out.println("Please provide jsonFileName in web.xml under WEB-INF");
return;
}
String jsonRel="/WEB-INF/"+jsonFileName;
String jsonAbs=ctx.getRealPath(jsonRel);
if(jsonAbs==null) 
{
System.out.println("JSON file not found at: "+jsonRel);
return;
}
StringBuilder jsonSB=new StringBuilder();
BufferedReader br=null;
try 
{
br=new BufferedReader(new FileReader(jsonAbs));
String line;
while((line=br.readLine())!=null)
{
jsonSB.append(line).append('\n');
}
} catch(IOException io) {
System.out.println(io.getMessage());
return;
} finally {
try {
if(br!=null) br.close();
} catch(IOException ignored)
{
//do nothing
}
}
JsonObject root=JsonParser.parseString(jsonSB.toString()).getAsJsonObject();
String jsDirPath=ctx.getRealPath("/WEB-INF/js");
File jsDir=new File(jsDirPath);
if(!jsDir.exists()) jsDir.mkdirs();
JsonArray entities=root.getAsJsonArray("entities");
for(int i=0;i<entities.size();i++) {
JsonObject e=entities.get(i).getAsJsonObject();
String eName=e.get("name").getAsString();
JsonArray fields=e.getAsJsonArray("fields");
File pojoFile=new File(jsDir,eName+".js");
if(pojoFile.exists()) pojoFile.delete();
RandomAccessFile raf=null;
try {
pojoFile.createNewFile();
raf=new RandomAccessFile(pojoFile,"rw");
StringBuilder params=new StringBuilder();
for(int j=0;j<fields.size();j++) {
params.append(fields.get(j).getAsString());
if(j<fields.size()-1) params.append(", ");
}
raf.writeBytes("// POJO for "+eName+"\n");
raf.writeBytes("class "+eName+" {\n");
raf.writeBytes("constructor("+params+") {\n");
for(int j=0;j<fields.size();j++) {
String f=fields.get(j).getAsString();
raf.writeBytes("this."+f+" = "+f+";\n");
}
raf.writeBytes("}\n}\n");
//System.out.println("Created: "+pojoFile.getName());
} catch(IOException io) {
io.printStackTrace();
} finally {
try {
if(raf!=null) raf.close();
} catch(IOException ignored) {}
}
}

//Services js file generation code
JsonArray services=root.getAsJsonArray("services");
for(int i=0;i<services.size();i++) 
{
JsonObject s=services.get(i).getAsJsonObject();
String sName=s.get("name").getAsString();
String basePath=s.get("basePath").getAsString();
String webPath=s.get("webPath").getAsString();
File serviceFile=new File(jsDir,sName+".js");
if(serviceFile.exists()) serviceFile.delete();
RandomAccessFile raf=null;
try 
{
serviceFile.createNewFile();
raf=new RandomAccessFile(serviceFile,"rw");
raf.writeBytes("// Service class for "+sName+"\n");
raf.writeBytes("class "+sName+" {\n");
JsonArray methods=s.getAsJsonArray("methods");
for(int k=0;k<methods.size();k++) {
JsonObject m=methods.get(k).getAsJsonObject();
String mName=m.get("name").getAsString();
String http=m.get("http").getAsString();
String path=m.get("path").getAsString();
String paramList="";
String ajaxData="";
String urlConcat="\""+contextPath+webPath+basePath+path+"\"";
if(m.has("body")) 
{
String bodyType=m.get("body").getAsString();
paramList=bodyType.toLowerCase();
ajaxData="data: JSON.stringify("+paramList+"),\ncontentType: 'application/json',";
} 
else if(m.has("query")) 
{
JsonArray qArr=m.getAsJsonArray("query");
List<String> qps=new ArrayList<>();
for(JsonElement el:qArr)
{
qps.add(el.getAsString());
}
StringBuilder paramListSB=new StringBuilder();
StringBuilder urlConcatSB=new StringBuilder();
urlConcatSB.append("\"").append(contextPath).append(webPath).append(basePath).append(path).append("?");
for(int idx=0;idx<qps.size();idx++) 
{
String p=qps.get(idx);
paramListSB.append(p);
urlConcatSB.append(p).append("=\"+").append(p);
if(idx<qps.size()-1) {
paramListSB.append(", ");
urlConcatSB.append("+\"&\"+");
}
}
paramList=paramListSB.toString();
urlConcat=urlConcatSB.toString();
}
raf.writeBytes(mName+"("+paramList+") {\n");
raf.writeBytes("return new Promise(function(resolve, reject) {\n");
raf.writeBytes("$.ajax({\n");
raf.writeBytes("url: "+urlConcat+",\n");
raf.writeBytes("method: '"+http+"',\n");
if(!ajaxData.isEmpty()) raf.writeBytes(ajaxData+"\n");
raf.writeBytes("success: function(r) { resolve(r); },\n");
raf.writeBytes("error: function(e) { reject(e); }\n");
raf.writeBytes("});\n");
raf.writeBytes("});\n");
raf.writeBytes("}\n\n");
}
raf.writeBytes("}\n");
System.out.println("Created: "+serviceFile.getName());
} catch(IOException io) {
System.out.println(io.getMessage());
} finally {
try {
if(raf!=null) raf.close();
} catch(IOException ignored) {}
}
}
System.out.println("All JS class files created under: "+jsDirPath);
}
}
