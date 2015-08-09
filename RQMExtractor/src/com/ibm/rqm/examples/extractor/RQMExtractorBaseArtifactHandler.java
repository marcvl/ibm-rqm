/*******************************************************************************
 * (c) Copyright IBM Corporation 2009, 2012. All Rights Reserved.
 *******************************************************************************/
package com.ibm.rqm.examples.extractor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.rqm.ct.artifacts.IArtifactHandler;
import com.ibm.rqm.ct.client.RestException;
import com.ibm.rqm.ct.util.Logger;
import com.ibm.rqm.ct.util.ProgressLogger;
import com.ibm.rqm.xml.bind.Richtext;
import com.ibm.rqm.xml.bind.Testcase;

//import javax.xml.bind.Element;

/**
 * Base, artifact-type neutral handler for persistence.
 * 
 */
public class RQMExtractorBaseArtifactHandler implements IArtifactHandler {
	protected RQMExtractorArtifactProcessor processor;
	protected String id = null;
	protected XmlRootElement artifactType = null;

	public RQMExtractorBaseArtifactHandler(String id,
			RQMExtractorArtifactProcessor processor) {
		this.processor = processor;
		this.id = id;
	}

	public String persist() throws RestException, IOException, JAXBException {
		String artifactSource = artifactType.name() + " " + id;
		Logger.logInfo("Starting: " + artifactSource);

		String href = null;
		String newId = null;
		try {
			if (ProgressLogger.isBeingContinued())
				ProgressLogger.existingArtifactId(artifactSource,
						ProgressLogger.getExistingLog());
			if (!processor.isIgnoreArtifact(artifactType.name(), id)
					&& !processor.isArtifactAdded(artifactType.name(), id)) {
				System.err.println("*** Processor:" + processor.toString());
				System.err.println("*** artifacType: " + artifactType.name());
				System.err.println("*** id: " + id);

				int l = id.lastIndexOf(":") + 1;
				String webID = id.substring(l);
				Object source = processor.getSourceClient().getArtifact(
						artifactType.name(), id);
				Testcase tc = (Testcase) source;
				String filename = webID + ".feature";
				filename = filename.replaceAll("\\s+", "");
				String TcDescription = tc.getDescription();
				System.err.println("*** webID: " + webID);
				System.err.println("*** Filename: " + filename);

				System.out.println("Testcase: " + tc.getTitle());
				System.out.println("WebID: " + webID);
				System.out.println("Description:");
				System.out.println(TcDescription);
				System.out.println();

				deleteFile(filename); // Delete file first
				FileWriter featurefile = new FileWriter(filename);
				BufferedWriter fileout = new BufferedWriter(featurefile);
				System.out.println("Feature source:");
				Richtext rt = tc
						.getComIbmRqmPlanningEditorSectionTestCaseDesign();
				Element el = (Element) rt.getContent().get(0);

				String cleanTemp2 = "# "; // Cleaned temp2, replace 160 by
											// spaces.
				for (int i = 0; i < TcDescription.length(); i++) {
					char c = TcDescription.charAt(i);
					int ascii = (int) c;
					if (ascii == 160)
						c = ' ';
					cleanTemp2 += c;
					if (ascii == 10) {
						cleanTemp2 += "# ";
					}
					// System.out.print("("+c+","+ascii+")");
				}

				try {
					fileout.write("# TC Description:\n");
					fileout.write("\n");
					fileout.write(cleanTemp2);
					fileout.write("\n\n");
				} catch (IOException e) {
					System.err.println("Exception in writing to feature-file.");
				}

				processNode(el, fileout);
				fileout.close();
				System.err.println();
				System.out.println();

				if (!processor.isAnotherIdExistsOrIgnored(source,
						artifactType.name(), id)) {
					// followReferences(source);

					// Check if following references caused this item to be
					// ignored and/or processed. Observed for some recursive
					// relations exist in processing
					// of ArtifactHandlers.
					if (!processor.isIgnoreArtifact(artifactType.name(), id)
							&& !processor.isArtifactAdded(artifactType.name(),
									id)) {
						if (!processor.isAnotherIdExistsOrIgnored(source,
								artifactType.name(), id)) {
							// newId = (null == existingDestId) ?
							// processor.getNewId() : existingDestId;
							// href =
							// processor.getDestinationClient().putArtifact(artifactType.name(),
							// newId, source);
							// processor.setArtifactAdded(artifactType.name(),
							// id, href);
							if (ProgressLogger.progressLoggerExists())
								ProgressLogger.logArtifactSourceAndDest(
										artifactSource, newId);
						} else {
							href = processor.getArtifactDestinationHref(
									artifactType.name(),
									processor.getAnotherId(source,
											artifactType.name(), id));
						}

					} else {
						href = processor.getArtifactDestinationHref(
								artifactType.name(), id);
					}
				} else {
					href = processor.getArtifactDestinationHref(artifactType
							.name(), processor.getAnotherId(source,
							artifactType.name(), id));
				}
			} else {
				href = processor.getArtifactDestinationHref(
						artifactType.name(), id);
			}
		} catch (Exception e) {
			Logger.logError(e.toString());
		}
		Logger.logInfo("Finishing: " + artifactType.name() + " " + id);
		Logger.logInfo("Finishing: " + href);
		return href;
	}

	public void deleteFile(String filename) {
		File temp = new File(filename);
		// Attempt to delete file first
		boolean success = true;
		if (temp.exists()) {
			success = temp.delete();
			if (!success) {
				System.err
						.println("*** Delete: deletion failed of " + filename);
			}
			;
		}
		;
	}

	public void processNode(Node n, BufferedWriter fileout) {
		Boolean localDebug = true;
		String cleanTemp2 = "";
		if (localDebug) {
			System.err.println();
			System.err.println("Processing Node");
		}
		if (n.getNodeType() == Node.ELEMENT_NODE) {
			NodeList nList = n.getChildNodes();
			for (int iNr = 0; iNr < nList.getLength(); iNr++) {
				Node subnode = nList.item(iNr);
				if (localDebug) {
					System.err.println("Processing iNodenr2=" + iNr);
					System.err.println("Processing iNodenr2-Name="
							+ subnode.getNodeName());
					System.err.println("           iNodenr2-Nodetype="
							+ subnode.getNodeType());

				}
				if (subnode.getNodeName() == "p") {
					if (localDebug)
						System.err.println("*** P-Node");
					cleanTemp2 = "\n\n";
					writeToFeatureFile(  fileout,   cleanTemp2) ;
				}
				if (subnode.getNodeName() == "br") {
					if (localDebug)
						System.err.println("*** BR-Node");
					cleanTemp2 = "\n";
					writeToFeatureFile(  fileout,   cleanTemp2) ;
				}
				if (subnode.getNodeName() == "pre") {
					if (localDebug)
						System.err.println("*** PRE-Node");
					cleanTemp2 = "\n";
					writeToFeatureFile(  fileout,   cleanTemp2) ;
				}
				if (subnode.getNodeName() == "#text") {
					if (localDebug)
						System.err.println("*** #text-Node");
					String temp2 = subnode.getTextContent();
					// temp2 =
					// e2.getElementsByTagName("div").item(i3).getTextContent();
					// //here throws null pointer exception after printing
					// staff1 tag
					cleanTemp2 = ""; // Cleaned temp2, replace 160 by spaces.
					if (localDebug)
						System.err.println("*** " + temp2);
					for (int i = 0; i < temp2.length(); i++) {
						char c = temp2.charAt(i);
						int ascii = (int) c;
						switch (ascii) {
							case 10:	c='\n';	cleanTemp2 += c; break;
							case 160:	c=' ';	cleanTemp2 += c; break;
							default: 			cleanTemp2 += c; break;
						}
						if (localDebug)
							System.err.print(c + "(" + ascii + ")");
					}
					writeToFeatureFile(  fileout,   cleanTemp2) ;
				}
				if (subnode.getNodeType() == Node.ELEMENT_NODE) {
					processNode(subnode, fileout);
				}
//				try {
//					if (localDebug) {
//						System.err.println("\n*** WRITE TO FILE: " + cleanTemp2);
//					}
//					;
//					fileout.write(cleanTemp2);
//				} catch (IOException e) {
//					System.err.println("Exception ");
//				}

			} // Next Node
		}
	}

	public void writeToFeatureFile( BufferedWriter fileout, String  message) {
		Boolean localDebug = true;

		try {
			if (localDebug) {
				System.err.println("\n*** WRITE TO FILE: " + message);
			}
			;
			fileout.write(message);
			System.err.println(message); // Output to error file.
			System.out.println(message); // Output to log file.
		} catch (IOException e) {
			System.err.println("Exception ");
		}
		
	}
	public void followReferences(Object referrer) throws JAXBException,
			IOException, RestException {
		// Intentionally blank
		// Must Remain to be in line with Interface definition.
	}
}
