package com.jschneider.springmvc.test.harness

import javax.servlet.ServletContext
import javax.servlet.ServletException

import org.apache.catalina.Container
import org.apache.catalina.Context
import org.apache.catalina.LifecycleException
import org.apache.catalina.Service
import org.apache.catalina.core.StandardContext
import org.apache.catalina.session.StandardManager
import org.apache.catalina.startup.Tomcat
import org.apache.naming.resources.VirtualDirContext
import org.apache.tomcat.JarScannerCallback
import org.apache.tomcat.util.scan.StandardJarScanner

public class TomcatServer {
	public static void main(String[] args) throws ServletException, LifecycleException {
		Tomcat tomcat = new TomcatWithFastStartup()
		tomcat.setPort(8080)

		StandardContext ctx = (StandardContext) tomcat.addWebapp("/", new File("test/resources/web").getAbsolutePath())

		VirtualDirContext resources = new VirtualDirContext()
		resources.setExtraResourcePaths("/WEB-INF/classes=" + new File("target/classes"))
		ctx.setResources(resources)

		tomcat.start()
		println "Started Tomcat server on port 8080"
		println "To test, navigate to http://localhost:8080"
		tomcat.getServer().await()
	}
	
	private static class TomcatWithFastStartup extends Tomcat {
		@Override
		public void start() throws LifecycleException {
			getServer().findServices().each { service ->
				service.getContainer().findChildren().each { container ->
					container.findChildren().each { c ->
						((Context) c).setJarScanner(new FastJarScanner())
					}
				}
			}
			super.start()
		}
	}
	
	private static class FastJarScanner extends StandardJarScanner {
		def jarsToInclude = [ 'spring-web.*' ]
		
		@Override
		public void scan(ServletContext context, ClassLoader classloader,
				JarScannerCallback callback, Set<String> jarsToSkip) {
			jarsToSkip = new HashSet<String>()
			
			((URLClassLoader) classloader.getParent()).getURLs().each {
				def jar = it.path.find(/[^\/]+\.jar$/)
				if(!jar) return
				for(String inclusionPattern : jarsToInclude) {
					if(jar.find(inclusionPattern))	
						println "including jar: " + jar
					else jarsToSkip.add(jar)
				}
			}
			
			super.scan(context, classloader, callback, jarsToSkip)
		}
	}
}
