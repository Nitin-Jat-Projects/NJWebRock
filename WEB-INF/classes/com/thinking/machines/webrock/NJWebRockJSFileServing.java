package com.thinking.machines.webrock;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class NJWebRockJSFileServing extends HttpServlet {

public  void doGet(HttpServletRequest request,HttpServletResponse response)
{
try 
{
String jsFileName = request.getParameter("name");
if (jsFileName == null) {
System.out.println("parameter name is required in usage: <script src='/name=whatever.js'></script>");
response.sendError(HttpServletResponse.SC_NOT_FOUND, "Parameter name is required.");
return;
}
jsFileName = jsFileName.trim();

ServletContext ctx = getServletContext();
String abs = ctx.getRealPath("/WEB-INF/js/" + jsFileName);
if (abs == null) {
response.sendError(HttpServletResponse.SC_NOT_FOUND, "Cannot resolve JS directory.");
return;
}

File file = new File(abs);
if (!file.isFile()) {
System.out.println("file not found for the name " + jsFileName +" in "+abs);
response.sendError(HttpServletResponse.SC_NOT_FOUND, "JavaScript file not found.");
return;
}

response.setContentType("application/javascript");

BufferedReader br = null;
PrintWriter pw= null;

try {
br = new BufferedReader(new FileReader(file));
pw= response.getWriter();

String line;
while ((line = br.readLine()) != null) {
pw.println(line);
}
} catch (IOException ioEx) {
response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error reading or sending file.");
} finally{
try {
if (br != null) br.close();
} catch (IOException ignored) {
}
}

} catch (Exception e) {
try {
response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error occurred.");
} catch (IOException ignored) {}
}
}


public void doPost(HttpServletRequest request, HttpServletResponse response) {
try {
doGet(request, response);
} catch (Exception e) {
try {
response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error in POST request.");
} catch (IOException ioe) {

}
}
}
}
