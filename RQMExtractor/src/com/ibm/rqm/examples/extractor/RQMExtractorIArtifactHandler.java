/*******************************************************************************
 * (c) Copyright IBM Corporation 2009, 2010. All Rights Reserved.
 *******************************************************************************/
package com.ibm.rqm.examples.extractor;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import com.ibm.rqm.ct.client.RestException;

public interface RQMExtractorIArtifactHandler {
    /**
     * Fully persist the artifact.
     * References will also be persisted according to ArtifactProcessor policy.
     * 
     * @return
     * @throws RestException
     * @throws IOException
     * @throws JAXBException
     */
    public String persist() throws RestException, IOException, JAXBException;
    
    /**
     * Fix all references in the referrer object.
     * References are "fixed" by either persisting the reference and updating 
     * the object, or removing it altogether, according to ArtifactProcessor policy.
     * 
     * @param referrer
     * @throws RestException
     * @throws IOException
     * @throws JAXBException
     */
    public void followReferences(Object referrer) throws RestException, IOException, JAXBException;
}
