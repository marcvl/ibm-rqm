/*******************************************************************************
 * (c) Copyright IBM Corporation 2009, 2013. All Rights Reserved.
 *******************************************************************************/
package com.ibm.rqm.examples.extractor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.ibm.rqm.ct.artifacts.ArtifactProcessor.Artifact;
import com.ibm.rqm.ct.client.IRQMRestClient;
import com.ibm.rqm.ct.util.Logger;
import com.ibm.rqm.xml.bind.ReportableArtifact;

/**
 * Handles general policy for a copy operations - managing copy-wide settings, histories, and common
 * clients used for the duration of the operation.
 */
public class RQMExtractorArtifactProcessor {       
    private IRQMRestClient sourceClient;
    private Random idGenerator;
    private boolean isRecursive = true;
    private boolean forceContinue = false;
    private boolean isLocal = false;
    private String defaultTestplanTemplateId = null;
    private String defaultTestcaseTemplateId = null;
    private String defaultTestsuiteTemplateId = null;
    
    private static HashMap<String, String> putArtifacts = new HashMap<String, String>();
    private static ArrayList<String> ignoreTypeList = new ArrayList<String>();
    private static ArrayList<String> ignoreArtifactList = new ArrayList<String>();
    
    @SuppressWarnings("unused")
    private RQMExtractorArtifactProcessor() {
        // Prevent default construction
    }
    
    public RQMExtractorArtifactProcessor(IRQMRestClient source) {
        this.sourceClient = source;
        this.idGenerator = new Random(System.currentTimeMillis());
    }
    
    public IRQMRestClient getSourceClient() {
        return sourceClient;
    }
    
    
    /**
     * Generate a new ID suitable to PUT an artifact
     * @return
     */
    public String getNewId() {
        return Integer.toString(java.lang.Math.abs(idGenerator.nextInt()));
    }
    
    /**
     * Determine if an artifact has already been PUT to the destination or not.
     * @param sourceArtifactType
     * @param sourceArtifactId
     * @return
     */
    public boolean isArtifactAdded(String sourceArtifactType, String sourceArtifactId) {
        boolean added = putArtifacts.containsKey(generateSourceArtifactKey(sourceArtifactType, sourceArtifactId));
        if (added) {
            Logger.logInfo("Already Added: " + sourceArtifactType + " " + sourceArtifactId);
        }
        return added; 
    }
    
    /**
     * Mark an artifact as being PUT to the destination.
     * @param sourceArtifactType
     * @param sourceArtifactId
     * @param destinationHref
     */
    public void setArtifactAdded(String sourceArtifactType, String sourceArtifactId, String destinationHref) {
        putArtifacts.put(generateSourceArtifactKey(sourceArtifactType, sourceArtifactId), destinationHref);
    }
    
    /**
     * Get the destination HREF of an artifact that has already been PUT, based on the source info. 
     * @param sourceArtifactType
     * @param sourceArtifactId
     * @return
     */
    public String getArtifactDestinationHref(String sourceArtifactType, String sourceArtifactId) {
        return putArtifacts.get(generateSourceArtifactKey(sourceArtifactType, sourceArtifactId));
    }
    
    /**
     * Return all of the source artifacts that have already been added to destination
     * @return
     */
    public List<Artifact> getAddedSourceArtifacts() {
        ArrayList<Artifact> sources = new ArrayList<Artifact>(putArtifacts.size());
        for (String source : putArtifacts.keySet()) {
            sources.add(deconstructKey(source));            
        }
        return sources;
    }
    
    /**
     * Simple tuple for an artifact: the type and the id.
     */
    public class Artifact {
        public String artifactType;
        public String artifactId;
    }
     
    /**
     * General copy policy: is this a recursive copy?
     * @return
     */
    public boolean isRecursive() {
        return isRecursive;
    }
    
    /**
     * Mark this copy as recursive or depth=1
     * @param isRecursive
     */
    public void setIsRecursive(boolean isRecursive) {
        this.isRecursive = isRecursive;
    }
    
    /**
     * Determine if the artifact should be ignored for the copy, based
     * on policy determined from the -i and/or -is command line flag.
     * @param artifactType
     * @param id
     * @return
     */
    public boolean isIgnoreArtifact(String artifactType, String id) {
        boolean ignore  = false; 
        if (ignoreTypeList.contains(artifactType)) {
            ignore = true;
            Logger.logInfo("Ignoring referenced artifact of type: " + artifactType);
        } else if (ignoreArtifactList.contains(artifactType + id)) {
            ignore = true;
            Logger.logInfo("Ignoring artifact of type: " + artifactType + " with id: " + id);
        }
        return ignore;
    }
    
    /**
     * Add an artifact type to ignore for the copy operation
     * @param artifactType
     */
    public void addIgnoreArtifactType(String artifactType) {
        ignoreTypeList.add(artifactType);
    }
    
    /**
     * Add a specific artifact to ignore for the copy operation
     * @param artifactType
     * @param id
     */
    public void addIgnoreArtifact(String artifactType, String id) {
        ignoreArtifactList.add(artifactType + id);
    }
    
    /**
     * Query policy of whether the copy should continue in the event of an error.
     * @return
     */
    public boolean isContinueOnError() {
        return forceContinue;
    }
    
    /**
     * Set policy of whether the copy should continue in the event of an error.
     * @param continueOnError
     */
    public void setContinueOnError(boolean continueOnError) {
        forceContinue = continueOnError;
    }
    
    /**
     * Query whether this copy is local (source=destination)
     * @return
     */
    public boolean isLocalCopy() {
        return isLocal;
    }
    
    /**
     * Set whether this copy is local (source=destination)
     * @param isLocal
     */
    public void setLocalCopy(boolean isLocal) {
        this.isLocal = isLocal;
    }
    
    public boolean isDefaultTestplanTemplateSet() {
        return defaultTestplanTemplateId != null;
    }
    
    public String getDefaultTestplanTemplateId() {
        return defaultTestplanTemplateId;
    }

    public void setDefaultTestplanTemplateId(String defaultTestplanTemplateId) {
        this.defaultTestplanTemplateId = defaultTestplanTemplateId;
    }

    public boolean isDefaultTestcaseTemplateSet() {
        return defaultTestcaseTemplateId != null;
    }
    
    public String getDefaultTestcaseTemplateId() {
        return defaultTestcaseTemplateId;
    }

    public void setDefaultTestcaseTemplateId(String defaultTestcaseTemplateId) {
        this.defaultTestcaseTemplateId = defaultTestcaseTemplateId;
    }
    
    public boolean isDefaultTestsuiteTemplateSet() {
        return defaultTestsuiteTemplateId != null;
    }
    
    public String getDefaultTestsuiteTemplateId() {
        return defaultTestsuiteTemplateId;
    }

    public void setDefaultTestsuiteTemplateId(String defaultTestsuiteTemplateId) {
        this.defaultTestsuiteTemplateId = defaultTestsuiteTemplateId;
    }
    
    private static String DELIMITER = "#####"; //$NON-NLS-N$ 
    private String generateSourceArtifactKey(String artifactType, String artifactId) {
        return artifactType + DELIMITER + artifactId;
    }
    private Artifact deconstructKey(String key) {
        Artifact a = new Artifact();
        String[] pair = key.split(DELIMITER);
        a.artifactType = pair[0];
        a.artifactId = pair[1];
        return a;
    }
    
    private String getExternalId(String artifactType, String identifier){
    	String externalId = null;
    	if(identifier != null){
	    	int index = identifier.lastIndexOf("/");
	    	if(index > -1){
	    		String id = identifier.substring(index + 1);
	    		if(id.indexOf(getPrefix(artifactType)) == -1){
	    			externalId = id;
	    		}
	    	}
    	}
    	return externalId;
    }
    
    private String getInternalId(String artifactType, ReportableArtifact source){
    	String internalId = null;
    	try {
    		Integer internal = null;
			Method method = null;
			method = source.getClass().getMethod("getWebId");
			if(method != null){
				internal = (Integer)(method.invoke(source));
			}
			if(internal != null){
				internalId = getPrefix(artifactType) + String.valueOf(internal);
			}
		} catch (Exception e) {
			//do nothing
		}
    	return internalId;
    }
    
    private String getPrefix(String artifactType){
    	String prefix = "urn:com.ibm.rqm:" + artifactType + ":";
    	return prefix;
    }
    
    // There are cases that the CopyUtil uses the external id of an artifact; but when the artifact is
    // referenced by others, its internal id is used. And vice versa. (see defect 37212)
    // This method is to check when given the resource's internal id, whether an artifact with its
    // corresponding external id has already existed or marked as ignored. And vice versa.
    public boolean isAnotherIdExistsOrIgnored(Object source, String artifactType, String givenId){
    	if(!(source instanceof ReportableArtifact)){
    		// some resource type like resourcecollection, resourcegroup, doesn't inherit ReportableArtifact.
    		// For such case, doesn't consider its 'another id'
    		return false;
    	}
    	String externalId = getExternalId(artifactType, ((ReportableArtifact)source).getIdentifier());
        String internalId = getInternalId(artifactType, (ReportableArtifact)source);
        if(externalId != null){
        	//check internal id
        	if(externalId.equals(givenId) && internalId != null){       		
        		return (isIgnoreArtifact(artifactType, internalId) || isArtifactAdded(artifactType, internalId));
        	}
        }
        if(internalId != null){
        	//check external id
        	if(internalId.equals(givenId) && externalId != null){       		
        		return (isIgnoreArtifact(artifactType, externalId) || isArtifactAdded(artifactType, externalId));
        	}
        }
        return false;
    }
    
    // There are cases that the CopyUtil uses the external id of an artifact; but when the artifact is
    // referenced by others, its internal id is used. And vice versa. (see defect 37212)
    // This method is to get the resource's internal id when given its external id. And vice versa.
    public String getAnotherId(Object source, String artifactType, String givenId){
    	if(!(source instanceof ReportableArtifact)){
    		// some resource type like resourcecollection, resourcegroup, doesn't inherit ReportableArtifact.
    		// For such case, doesn't consider its 'another id'
    		return null;
    	}
    	
    	String externalId = getExternalId(artifactType, ((ReportableArtifact)source).getIdentifier());
        String internalId = getInternalId(artifactType, (ReportableArtifact)source);
        
        if(externalId != null && externalId.equals(givenId)){
        	//return internal id
        	return internalId;
        }
        
        if(internalId != null && internalId.equals(givenId)){
        	//return external id
        	return externalId;
        }
        
        return null;
    }
    
}
