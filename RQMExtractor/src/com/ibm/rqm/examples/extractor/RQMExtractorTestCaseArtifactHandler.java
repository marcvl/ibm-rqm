/*******************************************************************************
 * (c) Copyright IBM Corporation 2009, 2012. All Rights Reserved.
 *******************************************************************************/
package com.ibm.rqm.examples.extractor;

import java.io.IOException;
import java.util.Iterator;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;

import com.ibm.rqm.ct.artifacts.ArtifactProcessor;
import com.ibm.rqm.ct.artifacts.IArtifactHandler;
import com.ibm.rqm.ct.artifacts.ArtifactFactory.ArtifactType;
import com.ibm.rqm.ct.client.RestException;
import com.ibm.rqm.ct.util.URLUtil;
import com.ibm.rqm.xml.bind.Testcase;

public class RQMExtractorTestCaseArtifactHandler extends RQMExtractorBaseArtifactHandler implements
    RQMExtractorIArtifactHandler {
    
    public RQMExtractorTestCaseArtifactHandler(String id, RQMExtractorArtifactProcessor processor) {
        super(id, processor);
        this.artifactType = Testcase.class.getAnnotation(XmlRootElement.class);
    }
    
    
    public void followReferences(Object source) throws JAXBException, IOException, RestException {
    	//* Do nothing ....
    }   
}
