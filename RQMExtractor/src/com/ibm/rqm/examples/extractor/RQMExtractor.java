/*******************************************************************************
 * (c) Copyright IBM Corporation 2009, 2013. All Rights Reserved.
 *******************************************************************************/
package com.ibm.rqm.examples.extractor;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import javax.xml.bind.JAXBException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Jdk14Logger;
import org.jdom.JDOMException;

import com.ibm.rqm.ct.artifacts.ArtifactFactory;
import com.ibm.rqm.ct.artifacts.ArtifactFactory.ArtifactType;
import com.ibm.rqm.ct.artifacts.ArtifactProcessor;
import com.ibm.rqm.ct.artifacts.IArtifactHandler;
import com.ibm.rqm.ct.client.IRQMRestClient;
import com.ibm.rqm.ct.client.RQMRestClientFactory;
import com.ibm.rqm.ct.client.RestException;
import com.ibm.rqm.ct.client.internal.RQMRestClient;
import com.ibm.rqm.ct.util.Logger;
import com.ibm.rqm.ct.util.ProgressLogger;
//import rqmextractor.vanlint5.nl.RQMExtractor.CmdLineArg;

public class RQMExtractor {
    
    private static final String VERSION = "0.0.1"; //$NON-NLS-N$
    
    private static enum CmdLineArg {
    	// Various settings are obtained by the properties file.
        //SOURCE("-s", "-sourceURL"),
        //DEST("-d", "-destinationURL"),
        //US("-us", "-usernameSource"),
        //PWS("-pws", "-passwordSource"),
        //UD("-ud", "-usernameDestination"),
        //PWD("-pwd", "-passwordDestination"),
        //LIST("-la", "-listArtifactTypes", false),
        //TYPE("-a", "-artifactType"),
        ID("-id", "-artifactId"),
        //NORECURSE("-nr", "-noRecurse", false),
        //FORCE("-f", "-force", false),
        LOG("-l", "-log"),
        //IGNORE("-i", "-ignoreTypes"),
        HELP("-h", "-help", false),
        VERSION("-v", "-version", false),
        //TEMPLATETP("-tp", "-testplanTemplate"),
        //TEMPLATETC("-tc", "-testcaseTemplate"),
        //TEMPLATETS("-ts", "-testsuiteTemplate"),
        //DELETESOURCE("-ds", "-deleteSource", false),
        //IGNORESPECIFICS("-is", "-ignoreSpecifics"),
        PROGRESSLOGGER("-pl", "-progressLogger"),
        //CONTINUEPROGRESS("-c", "-continueProgress"),
        ;
        
        private String name1;
        private String name2;
        private boolean valRequired;
        private String val;
        private CmdLineArg(String name1, String name2) { this(name1, name2, true); }
        private CmdLineArg(String name1, String name2, boolean valRequired) { 
            this.name1=name1; 
            this.name2=name2; 
            this.valRequired = valRequired; 
        };
        public boolean isEqual(String name) {
            return name1.equalsIgnoreCase(name) || name2.equalsIgnoreCase(name);
        }
        public boolean isValRequired() { return valRequired; }
        public void setVal(String val) { this.val = val; }
        public String getVal() { return val; }
    }
        
    /**
     * @param args
     */
    public static void main(String[] args) {
        String sourceURL = null;
    	IRQMRestClient cSource = null;
        IRQMRestClient cDest = null;
        int systemExitCode = 0;
        try {  
        	
            List<CmdLineArg> cmdArgs = processArgs(args);
            if (cmdArgs.contains(CmdLineArg.HELP) || cmdArgs.size() == 0) {
                System.out.println(HELPSTRING);
                System.exit(0);
            }
            if (cmdArgs.contains(CmdLineArg.VERSION)) {
                System.out.println(getVersion());
                System.exit(0);
            }
            
        	//Configure Apache Commons HTTP Client logging to use Java Logging:
        	//Note: Apache Commons Logging defaults to Log4j.
			LogFactory logFactory = LogFactory.getFactory();
			logFactory.setAttribute(Log.class.getName(), Jdk14Logger.class.getName());

            //Turn off Apache Commons Logging for dependencies:
            //Note: Turn off Apache Commons Logging BEFORE configuration/execution.
            String[] loggerNames = new String[]{HttpClient.class.getPackage().getName()};

            for(String loggerName : loggerNames){

            	java.util.logging.Logger logger = java.util.logging.Logger.getLogger(loggerName);

            	//Preserve Apache Commons Logging, if configured (e.g. properties file):
            	if(logger.getLevel() == null){				
            		logger.setLevel(Level.OFF); 
            	}
            }
            
            if (cmdArgs.contains(CmdLineArg.LOG)) {
                Logger.setLogFile(CmdLineArg.LOG.getVal());
            }
            if (cmdArgs.contains(CmdLineArg.PROGRESSLOGGER)) {
            	ProgressLogger.setProgressFile(CmdLineArg.PROGRESSLOGGER.getVal());
            }
            
            String uSource="";
            String pwSource="";
     


            /* Read Data from the properties files ... */
    		Properties prop = new Properties();

    		try {
    			// load a properties file
    			prop.load(new FileInputStream("RQMExtractor.properties"));

    			 String protocol=prop.getProperty("protocol");
    	         String host=prop.getProperty("host");
    	         int port=Integer.valueOf(prop.getProperty("port"));
    	         String contextRoot=prop.getProperty("contextRoot");
    	         String projectArea=prop.getProperty("projectArea");

    	         uSource=prop.getProperty("clmuser");
    	         pwSource=prop.getProperty("clmpassword");

    	         
    	         /* Below is based on the variables above */
    	         String integrationUrl=protocol+"://"+host+":"+port
    	        		+ "/"+contextRoot
    	        		+ "/service/com.ibm.rqm.integration.service.IIntegrationService";
    	         String resourceType="testcase";
    	         sourceURL=integrationUrl
    	        		+ "/resources"
    	        		+ "/"+projectArea;
    	         //  	+ "/"+resourceType;
    	         //String internalID="urn:com.ibm.rqm:"+resourceType+":"+webID;
    	         
    	         //String URI=singleProjectFeedURL+"/"+internalID;
    	         //System.out.println("URI: "+URI);


    		} catch (IOException ex) {
    			ex.printStackTrace();
    		}
    		try {
            // Build a client and connect...
        	cSource = getClient(sourceURL);
    		} catch (Exception e) {
                Logger.logError("Error set client: " + e.toString());
                systemExitCode = 2;
    			
    		}
            try {
                //Logger.logError("uSource: " + uSource);
                //Logger.logError("pwSource: " + pwSource);
                int retCode = cSource.login(uSource, pwSource);
                //Logger.logError("retCode: " + retCode);
                if (retCode != HttpStatus.SC_OK && retCode != HttpStatus.SC_MOVED_TEMPORARILY) {
                    Logger.logError("Error logging in to source. Return Code: " + retCode);
                    systemExitCode = 2;
                    return;
                }
            } catch (Exception e) {
                Logger.logError("Error logging in to source: " + e.toString());
                systemExitCode = 2;
                return;
            }

            /*
            String CLMtype="testcase";
            String CLMid="urn:com.ibm.rqm:testcase:98";
        	System.err.println("*** Hardcoded CLMtype:" + CLMtype);
        	System.err.println("*** Hardcoded CLMid:" + CLMid);
        	System.err.println("*** Hardcoded:" + cSource.artifactExists(CLMtype, CLMid));
			*/
            
        	RQMExtractorArtifactProcessor processor = new RQMExtractorArtifactProcessor(cSource);            

            // MUST ALWAYS BE TESTCASE
            ArrayList<ArtifactType> artifacts = new ArrayList<ArtifactType>();                        
            ArtifactType type = ArtifactType.valueOf("testcase");
            artifacts.add(type);
           
            if (cmdArgs.contains(CmdLineArg.ID)) {
                String id = CmdLineArg.ID.getVal();
                id="urn:com.ibm.rqm:"+type+":" +id;
                System.err.println("INFO: RQM Extractor processing TC="+id);
                if (artifacts.size() > 1) {
                    throw new IllegalArgumentException("Only a single artifact type (-a) can be used in conjunction with -id");
                } else if (artifacts.size() == 0) {
                    throw new IllegalArgumentException("Artifact type (-a) must be specified in conjunction with -id");
                } else {
                    System.err.println("INFO: RQM Extractor ...");
                	RQMExtractorIArtifactHandler handler = RQMExtractorArtifactFactory.getArtifactHandler(processor, artifacts.get(0).name(), id);
                    System.err.println("INFO: RQM Extractor assigned class "+handler.getClass());
                    handler.persist();                    
                }
            } else {            
                Logger.logError("Must have -id flag.");
            }
            Logger.logInfo("SUCCESS!");
        } catch (IllegalArgumentException e) {
            Logger.logError(e.toString());
            systemExitCode = 1;
        } catch (RestException e) {
            Logger.logError("General REST Exception: " + e.toString());
            Logger.logError("FAILURE!");
            systemExitCode = 3;
        } catch (IOException e) {
            Logger.logError("IOException invoking RQM REST API: " + e.toString());
            Logger.logError("FAILURE!");
            systemExitCode = 4;
        } catch (JAXBException e) {
            Logger.logError("Internal marshal error: " + e.toString());
            Logger.logError("FAILURE!");
            systemExitCode = 5;
/*        } catch (JDOMException e) {
            Logger.logError("Internal JDOM error: " + e.toString());
            Logger.logError("FAILURE!");
            systemExitCode = 6;   */
        } catch (Exception e) {
            Logger.logError("Internal error: " + e.toString());
            Logger.logError("FAILURE!");
            systemExitCode = 7;
        } finally {
        	attemptLogout(cSource, false);
        	
        	System.exit(systemExitCode);
        }
    }

    /*
     * Attempts a logout of the provided client.
     * @param client The client object to handle the logout
     * @param isDestClient Whether the client is a destination or source client
     */
    private static void attemptLogout(final IRQMRestClient client, final boolean isDestClient) {
    	// Exit early if not provided a client or not of proper type
    	if (client == null || !(client instanceof RQMRestClient))
    		return;
    	
    	final String clientType = (isDestClient) ? "destination" : "source";
    	
    	System.err.println("Disconnecting from the " + clientType + 
    			" IBM Rational Quality Manager server.");
	    	
    	final int sourceReturnCode = ((RQMRestClient) client).logout();
    	
		if ((sourceReturnCode != HttpURLConnection.HTTP_OK) && 
			(sourceReturnCode != HttpURLConnection.HTTP_MOVED_TEMP)) {
			System.err.println("RQM Copy Utility failed to disconnect from the " + clientType + " server!");
		}
    }
    
    private static List<CmdLineArg> processArgs(String[] args) throws IllegalArgumentException {
        ArrayList<CmdLineArg> argList = new ArrayList<CmdLineArg>();
        for (String arg : args) {
            boolean found = false;
            for (CmdLineArg cmd : CmdLineArg.values()) {
                String[] nameVal = arg.split("=");
                if (cmd.isEqual(nameVal[0])) {
                    found = true;
                    if (nameVal.length > 1 && !cmd.isValRequired()) {
                        throw new IllegalArgumentException("Argument: " + nameVal[0] + " does not take a value."); 
                    } else if (nameVal.length > 1) {
                        cmd.setVal(nameVal[1]);
                    } else if (cmd.isValRequired()) {
                        throw new IllegalArgumentException("Value required for argument: " + nameVal[0]);
                    }
                    argList.add(cmd);
                    break;
                }
            }
            if (!found) {
                throw new IllegalArgumentException("Command line switch not found: [" + arg + "]. Try -h for help.");
            }
        }
        return argList;
    }
    
    private static IRQMRestClient getClient(String url) throws IllegalArgumentException {
        String host = null;
        int port = -1;
        String projectArea = null;
        String contextRoot = null;
        String tmpURL = url.substring(url.indexOf("//") + "//".length());
        String protocol = url.substring(0, url.indexOf("//") - 1);

        if(tmpURL.contains(":")) {
	        try { //to separate host name if both host name and port number are given
	            host = tmpURL.substring(0, tmpURL.lastIndexOf(":"));
	        } catch (Exception e) {
	            throw new IllegalArgumentException("URL not valid, parsing hostname of: " + url);
	        }
	        try { //to separate port number if given
	            int portLoc = url.lastIndexOf(":");
	            String portStr = url.substring(portLoc + ":".length(), url.indexOf("/", portLoc));
	            port = Integer.parseInt(portStr); 
	        } catch (Exception e) {
	            throw new IllegalArgumentException("URL not valid, parsing port of: " + url);
	        }
        }
        else {
        	try { //to separate host name if the port number is not given
        		host = tmpURL.substring(0, tmpURL.indexOf("/"));
        		port = -1;
        	} catch (Exception e) {
        		throw new IllegalArgumentException("URL not valid, parsing host without port number of: " + url);
        	}
        }
        
        try {
        	if(port != -1) {
	        	final int contextRootLoc = url.indexOf("/", url.lastIndexOf(":")) + 1;
	            contextRoot = url.substring(contextRootLoc, url.indexOf("/", contextRootLoc));
	        }
        	else {
        		final int contextRootLoc = url.indexOf("/", url.lastIndexOf(host))+1;
        		contextRoot = url.substring(contextRootLoc, url.indexOf("/", contextRootLoc));
        	}
        } catch (Exception e) {
            throw new IllegalArgumentException("URL not valid, parsing context root of: " + url);
        }   
        try {
            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - "/".length());
            }
            projectArea = url.substring(url.lastIndexOf("/") + "/".length());
        } catch (Exception e) {
            throw new IllegalArgumentException("URL not valid, parsing project area of: " + url);
        }
        if (host == null || projectArea == null) {
            throw new IllegalArgumentException("URL not valid: " + url);
        }
        IRQMRestClient client = null;
        try {
            client = RQMRestClientFactory.getInstance(host, port, projectArea, contextRoot, protocol);    
        } catch (Exception e) {
            throw new IllegalArgumentException("URL not valid: " + url);
        }
        return client;
    }
    
    private static String getVersion() {
        return "RQM Extractor Utility, version: " + VERSION;
    }
    
    private static final String HELPSTRING = 
            "RQM Extractor Utility\n" +  
            "==================\n" +
            "Extract RQM Test Case Design to a flat file to be processed by Cucumber.\n" +
            "\n" +
            "Argument Reference\n" +
            "==================\n" +
            "\n" +
            "-h, -help\n" +
            "Prints this help message\n" +
            "\n" +
            "-v, -version\n" +
            "Prints the version of the Copy Utility\n" +
            "\n" +
            "-id, artifactId=<RQM_TESTCASE_WEBID>\n" +
            "Used in conjunction with only the Test Case artifactType. Extract Test Case Design of this single artifact based on the artifact's id.\n" +
            "The id is the REST id (i.e. from the feed) and not the internal database id.\n" +
            "\n" +
            "-l, -log=<file>\n" +
            "Log verbose information to the specified file.\n" +
            "\n" +
            "Usage\n" +
            "==================\n" +
            "Note: These examples use the default context root (e.g. /qm/) for Rational Quality Manager 3.0.1 or later.  For more information, see the RQM Reportable REST API (https://qm.net/wiki/bin/view/Main/RqmApi#contextRoot)." + 
            "\n" +
            "Note: These examples use URL encoded project aliases in feed URL references (-d/-destinationURL/-s/-sourceURL).  When invoking the RQM Copy Utility from a batch file, enclose feed URL references (-d/-destinationURL/-s/-sourceURL) in double quotes." +  
            "\n" +
            "Copying all artifacts from one project to another:\n" +
            "copyutil -s=https://myhost:9443/qm/service/com.ibm.rqm.integration.service.IIntegrationService/resources/ProjectA -us=ADMIN -pws=ADMIN -d=https://myhost2:9443/qm/service/com.ibm.rqm.integration.service.IIntegrationService/resources/ProjectB -l=importlog.txt\n" +
            "\n" +
            "Listing the supported artifact types:\n" +
            "copyutil -la\n" +
            "\n" +
            "Copying all artifacts of a given type:\n" +
            "copyutil -s=https://myhost:9443/qm/service/com.ibm.rqm.integration.service.IIntegrationService/resources/ProjectA -us=ADMIN -pws=ADMIN -d=https://myhost2:9443/qm/service/com.ibm.rqm.integration.service.IIntegrationService/resources/ProjectB -a=testplan,testcase -l=importlog.txt\n" +
            "\n" +
            "Copying a single artifact into the same project:\n" +
            "copyutil -s=https://myhost:9443/qm/service/com.ibm.rqm.integration.service.IIntegrationService/resources/ProjectA -us=ADMIN -pws=ADMIN -a=testplan -id=urn:com.ibm.rqm:testplan:1 -l=importlog.txt\n" +
            "Continuing a previous copy session by updating all artifacts that exists in the specified progress log file and copying in new artifacts unexisting in the specified progress log file while logging the progress:\n" +
            "copyutil -s=https://myhost:9443/qm/service/com.ibm.rqm.integration.service.IIntegrationService/resources/ProjectA -us=ADMIN -pws=ADMIN -d=https://myhost:9443/qm/service/com.ibm.rqm.integration.service.IIntegrationService/resources/ProjectB -l=importlog.txt -pl=progressLog.txt -c=previousProgress.txt\n";
    }
