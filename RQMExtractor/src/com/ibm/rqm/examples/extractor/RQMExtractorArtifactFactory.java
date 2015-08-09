/*******************************************************************************
 * (c) Copyright IBM Corporation 2009, 2013. All Rights Reserved.
 *******************************************************************************/
package com.ibm.rqm.examples.extractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.jdom.JDOMException;

import com.ibm.rqm.ct.CopyUtil;
import com.ibm.rqm.ct.artifacts.internal.AttachmentArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.BuildDefinitionArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.BuildRecordArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.ChannelArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.ConfigurationArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.DatapoolArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.ExecutionResultArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.ExecutionSequenceArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.ExecutionSequenceResultArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.ExecutionWorkitemArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.KeywordArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.LabResourceArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.ObjectiveArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.RemoteScriptArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.RequestArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.ReservationArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.ResourceGroupArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.TemplateArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.TestCaseArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.TestPhaseArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.TestPlanArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.TestScriptArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.TestSuiteArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.TestSuiteExecutionRecordArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.TestSuiteLogArtifactHandler;
import com.ibm.rqm.ct.artifacts.internal.TestcellArtifactHandler;
import com.ibm.rqm.ct.client.RestException;
import com.ibm.rqm.ct.util.FeedReader;
import com.ibm.rqm.xml.bind.Configuration;

public class RQMExtractorArtifactFactory {
    /**
     * <p>Enumeration of the supported artifact types.</p>
     * 
     * <p>The artifact type name corresponds to the resource type name.</p> 
     * 
     * <p><b>Note:</b> Artifact type names <i>should</i> be alphabetical order
     * for listing the supported artifact types (see {@link CopyUtil.CmdLineArg#LIST}).</p> 
     * 
     * @see UnsupportedArtifactType
     */
    public static enum ArtifactType {
         testcase
    }
    
    /**
     * <p>Enumeration of the unsupported artifact types.</p>
     * 
     * <p>The artifact type name corresponds to the resource type name.</p> 
     * 
     * @see ArtifactType
     */
    public static enum UnsupportedArtifactType {
		attachment,
		builddefinition,
		buildrecord,
		configuration,
		channel,
		datapool,
		executionresult,
		executionsequence,
		executionsequenceresult,
		executionworkitem,
		keyword,
		labresource,
		objective,
		remotescript,
		request,
		reservation,
		resourcegroup,
		suiteexecutionrecord,
		template,
		testcell,
		testphase,
		testplan,
		testscript,
		testsuite,
		testsuitelog,         
		adapter,
		catalog,
		category,
		categoryType,
		contributor,
		job,
		jobresult,
		jobscheduler,
		labresourceattribute,
		projects,
		requirement,
		resourcecollection,
		tasks,
		teamarea,
		workitem
    }
    
    static final HashMap<String, XmlRootElement> artifactLookupTable = new HashMap<String, XmlRootElement>();
    {
        artifactLookupTable.put(Configuration.class.getAnnotation(XmlRootElement.class).name(), 
                Configuration.class.getAnnotation(XmlRootElement.class));
    }
    
    /**
     * Retrieve an appropriate handler for an artifact.
     * @param p the ArtifactProcessor used to define the policy for persistence
     * @param artifactType the artifact type. Corresponds to a value in ArtifactType.
     * @param id the id of the artifact.
     * @return
     */
    public static RQMExtractorIArtifactHandler getArtifactHandler(RQMExtractorArtifactProcessor p, String artifactType, String id) {
        RQMExtractorIArtifactHandler handler = null;
        ArtifactType type = ArtifactType.valueOf(artifactType);
        switch (type) {
/*        case remotescript:
            handler = new RemoteScriptArtifactHandler(id, p);
            break;
        case template:
            handler = new TemplateArtifactHandler(id, p);
            break;  */
        case testcase:
            System.err.println("INFO: RQM Extractor Artifact Factory - RQMExtractorTestCaseArtifactHandler...");

            handler = new RQMExtractorTestCaseArtifactHandler(id, p);
            break;     
/*        case testscript:
            handler = new TestScriptArtifactHandler(id, p);
            break;
        case datapool:
            handler = new DatapoolArtifactHandler(id, p);
            break;
        case attachment:
            handler = new AttachmentArtifactHandler(id, p);
            break;
        case testplan:
            handler = new TestPlanArtifactHandler(id, p);
            break;
        case configuration:
            handler = new ConfigurationArtifactHandler(id, p);
            break;
        case keyword:
            handler = new KeywordArtifactHandler(id, p);
            break;
        case buildrecord:
            handler = new BuildRecordArtifactHandler(id, p);
            break;
        case builddefinition:
            handler = new BuildDefinitionArtifactHandler(id, p);
            break;
        case objective:
            handler = new ObjectiveArtifactHandler(id, p);
            break;
        case resourcegroup:
            handler = new ResourceGroupArtifactHandler(id, p);
            break;
        case labresource:
            handler = new LabResourceArtifactHandler(id, p);
            break; */
        /*case job:
            handler = new JobArtifactHandler(id, p);
            break;
        case jobresult:
            handler = new JobResultArtifactHandler(id, p);
            break;*/
/*        case request:
            handler = new RequestArtifactHandler(id, p);
            break;
        case reservation:
            handler = new ReservationArtifactHandler(id, p);
            break;
        case testcell:
            handler = new TestcellArtifactHandler(id, p);
            break;
        case testphase:
            handler = new TestPhaseArtifactHandler(id, p);
            break;
        case testsuite:
            handler = new TestSuiteArtifactHandler(id, p);
            break;
        case executionworkitem:
            handler = new ExecutionWorkitemArtifactHandler(id, p);
            break; */
        /*case labresourceattribute:
            handler = new LabresourceAttributeArtifactHandler(id, p);
            break;
        case catalog:
            handler = new CatalogArtifactHandler(id, p);
            break;*/
/*        case executionsequence:
            handler = new ExecutionSequenceArtifactHandler(id, p);
            break;
        case executionsequenceresult:
            handler = new ExecutionSequenceResultArtifactHandler(id, p);
            break;
        case executionresult:
            handler = new ExecutionResultArtifactHandler(id, p);
            break;
        case suiteexecutionrecord:
            handler = new TestSuiteExecutionRecordArtifactHandler(id, p);
            break;
        case testsuitelog:
            handler = new TestSuiteLogArtifactHandler(id, p);
            break;
        case channel:
        	handler = new ChannelArtifactHandler(id, p);
        	*/
        } 
        return handler;
    }
    
    /**
     * Get a list of all artifact handlers for a given artifact type.
     * @param p the ArtifactProcessor used to define the policy for persistence
     * @param artifactType the artifact type. Corresponds to a value in ArtifactType.
     * @return
     * @throws RestException
     * @throws JDOMException
     * @throws IOException
     */
    /*
    public static List<RQMExtractorIArtifactHandler> getArtifactHandlers(ArtifactProcessor p, String artifactType) throws RestException, JDOMException, IOException{
        List<RQMExtractorIArtifactHandler> handlers = new ArrayList<RQMExtractorIArtifactHandler>();            
        List<String> ids = FeedReader.getIds(p.getSourceClient(), artifactType);
        handlers = new ArrayList<RQMExtractorIArtifactHandler>(ids.size());
        for (String id : ids) {
            handlers.add(getArtifactHandler(p, artifactType, id));
        }
        return handlers;
    }
    */
}
