/**
 * 
 */
package obanminter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

/**
 * @author drashtti
 * Method to parse the diabetes ontology
 * spreadsheet - 2nd curation sheet. The subject will always be 
 * Type 2 diabetes. 
 *
 */
public class DiabParser {
	
		boolean manifest_diabetes = false ;
		boolean complications = false; 
		boolean preDiab = false;  
		
		/**
		 * Method to parse tab delimited file
		 * Input file is expected to be tab delimited with column headers. 
		 * @param outputpath
		 */
	
	public void parseDiabFile (String outputpath){
		
		try {
			Workbook wbk1 = WorkbookFactory
					.create(new FileInputStream(
							"/home/drashtti/Desktop/ontologies/Diabetes-Onto/DIAB ontology development and annotated datasets.xlsx"));
			Sheet curation = wbk1.getSheet("2nd expert curation");
			System.out.println("Reading from sheet - 2nd expert curation.");
			
			//for pmid and frequency information
			Workbook wbk2 = WorkbookFactory.create(new FileInputStream ("/home/drashtti/Desktop/ontologies/Diabetes-Onto/processedtype2diabetes.xls"));
			Sheet mining = wbk2.getSheetAt(0);
			System.out.println("Reading from text mining sheet.");
			
			//prepare ontology to save RDF into
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            IRI ontologyIRI = IRI.create("http://cttv.org/associations/");
            IRI documentIRI = IRI.create(outputpath);
            SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);
            manager.addIRIMapper(mapper);
            OWLOntology ontology = manager.createOntology(ontologyIRI);
            // Get hold of a data factory from the manager
            OWLDataFactory factory = manager.getOWLDataFactory();

            //read the file 
            //last 2 terms are new 
			int rownum = (curation.getLastRowNum()-2);
			for (int i = 7; i <= rownum ; i++){
				Row r = curation.getRow(i); 
				//Getting the phenotype/object id. 
				Cell c0 = r.getCell(0);
				String object = c0.toString();
				String freq  = null ;
				String pmid  = null ;
				//getting pre-diabetes stage 
					for ( Row r1 : mining){
						Cell id = r1.getCell(1);
						String ids = id.toString();
						if (object.equalsIgnoreCase(ids)){
							
							Cell tf = r1.getCell(8);
							 freq = tf.toString();
							
							Cell pmId = r1.getCell(10);
							 pmid = pmId.toString();
						}
					}
				
				Cell c3 = r.getCell(3);
				if (c3.toString().equalsIgnoreCase("x")){
					preDiab = true;
				}
				//getting manifest diabetes stage 
				Cell c4 = r.getCell(4);
				if (c4.toString().equalsIgnoreCase("x")){
					manifest_diabetes = true;
				}
				//getting consequences/complications
				Cell c5 = r.getCell(5);
				if (c5.toString().equalsIgnoreCase("x")){
					complications = true; 
				}
				//subject - type 2 diabetes 
				String subject = "http://www.ebi.ac.uk/efo/EFO_0001360";
				String sourceDB = "BioMedBridges";
				String assocDate = "07/12/2012";
				//mint subject and object assertions
                createOBANAssociation(manager, ontology, factory, subject, object, preDiab, manifest_diabetes , complications, pmid, assocDate, sourceDB, freq);				
			}
			
			 //save ontology
	        manager.saveOntology(ontology);
	        System.out.println("ontology saved");
	        
	        
		} catch (InvalidFormatException e) {
			System.out.println("The file is an invalid format");
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.out.println("Please check the file path");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Could not read the file");
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			System.out.println("Could not create OWL file");
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			System.out.println("Could not save OWL file");
			e.printStackTrace();
		}
				
	}

	
	
	
	/**
	 * create a single association between a subject and object with evidence and any provenance attached to it
	 * @param manager
	 * @param ontology
	 * @param factory
	 * @param subject
	 * @param object
	 * @param preDiab2
	 * @param manifest_diabetes2
	 * @param complications2
	 * @param pmid
	 * @param assocDate
	 * @param sourceDB
	 * @param freq
	 */

	private void createOBANAssociation(OWLOntologyManager manager,
			OWLOntology ontology, OWLDataFactory factory, String subject,
			String object, boolean preDiab2, boolean manifest_diabetes2,
			boolean complications2,String pmid, String assocDate, String sourceDB, String freq) {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss'Z'");
        //get current date time with Date()
        String date = dateFormat.format(new Date());
        object = "http://purl.obolibrary.org/obo/"+object;
        //create IRIs for both subject and object
        IRI subjectIRI = IRI.create(subject);
        IRI objectIRI = IRI.create(object);
        //generate hash for association & provenance URI fragment
        String assocHash = HashingIdGenerator.generateHashEncodedID(subject+object+sourceDB);
        String provHash = HashingIdGenerator.generateHashEncodedID(subject+object+assocHash);
        //create IRI for association instance
        String assocString = new StringBuilder().append("http://purl.obolibrary.org/cttv/").append(assocHash).toString();
        IRI assocIRI = IRI.create(assocString);
        String provString = new StringBuilder().append("http://purl.obolibrary.org/cttv/").append(provHash).toString();
        IRI provIRI = IRI.create(provString);

        //mint classes
        OWLClass association = factory.getOWLClass(IRI.create("http://purl.org/oban/association"));
        OWLClass provenance = factory.getOWLClass(IRI.create("http://purl.org/oban/provenance"));

        //mint properties used in minting associations
        //mint object properties
        OWLObjectProperty hasSubject = factory.getOWLObjectProperty(IRI.create("http://purl.org/oban/association_has_subject"));
        OWLObjectProperty hasObject = factory.getOWLObjectProperty(IRI.create("http://purl.org/oban/association_has_object"));
        OWLObjectProperty hasProvenance = factory.getOWLObjectProperty(IRI.create("http://purl.org/oban/has_provenance"));
     
        //mint datatype properties
        OWLDataProperty hasAssocCreatedDate = factory.getOWLDataProperty(IRI.create("http://purl.org/oban/date_association_created"));
		

        //create individuals for subject and object and an association instance
        OWLNamedIndividual subjectIndividual = factory.getOWLNamedIndividual(subjectIRI);
        OWLNamedIndividual objectIndividual = factory.getOWLNamedIndividual(objectIRI);
        OWLNamedIndividual associationIndividual = factory.getOWLNamedIndividual(assocIRI);
        OWLNamedIndividual provenanceIndividual = factory.getOWLNamedIndividual(provIRI);
        
        
      //assert types
        OWLClassAssertionAxiom assocTypeAssertion = factory.getOWLClassAssertionAxiom(association, associationIndividual);
        manager.addAxiom(ontology, assocTypeAssertion);
        OWLClassAssertionAxiom provTypeAssertion = factory.getOWLClassAssertionAxiom(provenance, provenanceIndividual);
        manager.addAxiom(ontology, provTypeAssertion);

        //add subject and object to association
        OWLObjectPropertyAssertionAxiom subjectAssertion = factory.
                getOWLObjectPropertyAssertionAxiom(hasSubject, associationIndividual, subjectIndividual);
        manager.addAxiom(ontology, subjectAssertion);
        OWLObjectPropertyAssertionAxiom objectAssertion = factory.
                getOWLObjectPropertyAssertionAxiom(hasObject, associationIndividual, objectIndividual);
        manager.addAxiom(ontology, objectAssertion);
        OWLObjectPropertyAssertionAxiom provAssertion = factory.
                getOWLObjectPropertyAssertionAxiom(hasProvenance, associationIndividual, provenanceIndividual);
        manager.addAxiom(ontology, provAssertion);
        
        
        OWLDataPropertyAssertionAxiom dateAssertion = factory.
                getOWLDataPropertyAssertionAxiom(hasAssocCreatedDate, provenanceIndividual, date);
        manager.addAxiom(ontology, dateAssertion);


        //add evidence assertion
        OWLObjectProperty hasEvidence = factory.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/RO_0002558"));
        //eco class for: inference from background scientific knowledge used in manual assertion
        OWLNamedIndividual evidenceIndividual = factory.getOWLNamedIndividual(IRI.create("http://purl.obolibrary.org/obo/ECO_0000306"));
        OWLObjectPropertyAssertionAxiom evidenceAssertion = factory.
                getOWLObjectPropertyAssertionAxiom(hasEvidence, provenanceIndividual, evidenceIndividual);
        manager.addAxiom(ontology, evidenceAssertion);
        
        if(pmid != null){
            //mint datatype properties
            OWLDataProperty hasPubmedID = factory.getOWLDataProperty(IRI.create("http://purl.org/oban/has_pubmed_id"));

            //make assertion
            OWLDataPropertyAssertionAxiom pubmedAssertion = factory.
                    getOWLDataPropertyAssertionAxiom(hasPubmedID, provenanceIndividual, pmid);
            manager.addAxiom(ontology, pubmedAssertion);
        }


        if(assocDate != null){
            //mint datatype properties
            OWLDataProperty hasOriginCreatedDate = factory.getOWLDataProperty(IRI.create("http://purl.org/oban/date_orgin_created"));

            //make assertion
            OWLDataPropertyAssertionAxiom assocDateAssertion = factory.
                    getOWLDataPropertyAssertionAxiom(hasOriginCreatedDate, provenanceIndividual, assocDate);
            manager.addAxiom(ontology, assocDateAssertion);

        }

        if(sourceDB != null){
            //mint datatype properties
            OWLDataProperty hasSourceDB = factory.getOWLDataProperty(IRI.create("http://purl.org/oban/has_source_db"));

            //make assertion
            OWLDataPropertyAssertionAxiom sourceDBAssertion = factory.
                    getOWLDataPropertyAssertionAxiom(hasSourceDB, provenanceIndividual, sourceDB);
            manager.addAxiom(ontology, sourceDBAssertion);
        }

        if(freq != null){
            //mint datatype properties
            OWLDataProperty hasFreq = factory.getOWLDataProperty(IRI.create("http://purl.org/oban/has_frequency"));

            //make assertion
            OWLDataPropertyAssertionAxiom freqAssertion = factory.
                    getOWLDataPropertyAssertionAxiom(hasFreq, provenanceIndividual, freq);
            manager.addAxiom(ontology, freqAssertion);

        }
        
        if(manifest_diabetes){
        	 IRI subjectpropIRI = IRI.create("http://purl.obolibrary.org/obo/DIAB_000010");
        	 OWLObjectProperty hasSubjectProperty = factory.getOWLObjectProperty(IRI.create("http://purl.org/oban/association_has_subject_property"));
        	 OWLNamedIndividual subjectPropertyIndividual = factory.getOWLNamedIndividual(subjectpropIRI);
        	 OWLObjectPropertyAssertionAxiom subjectPropertyAssertion = factory.
                     getOWLObjectPropertyAssertionAxiom(hasSubjectProperty, associationIndividual, subjectPropertyIndividual);
             manager.addAxiom(ontology, subjectPropertyAssertion);
        	 
        	
        }
        

        if(complications){
        	 IRI subjectpropIRI = IRI.create("http://purl.obolibrary.org/obo/DIAB_000011");
        	 OWLObjectProperty hasSubjectProperty = factory.getOWLObjectProperty(IRI.create("http://purl.org/oban/association_has_subject_property"));
        	 OWLNamedIndividual subjectPropertyIndividual = factory.getOWLNamedIndividual(subjectpropIRI);
        	 OWLObjectPropertyAssertionAxiom subjectPropertyAssertion = factory.
                     getOWLObjectPropertyAssertionAxiom(hasSubjectProperty, associationIndividual, subjectPropertyIndividual);
             manager.addAxiom(ontology, subjectPropertyAssertion);
        	 
        	
        }
        

        if(preDiab){
        	 IRI subjectpropIRI = IRI.create("http://purl.obolibrary.org/obo/DIAB_000009");
        	 OWLObjectProperty hasSubjectProperty = factory.getOWLObjectProperty(IRI.create("http://purl.org/oban/association_has_subject_property"));
        	 OWLNamedIndividual subjectPropertyIndividual = factory.getOWLNamedIndividual(subjectpropIRI);
        	 OWLObjectPropertyAssertionAxiom subjectPropertyAssertion = factory.
                     getOWLObjectPropertyAssertionAxiom(hasSubjectProperty, associationIndividual, subjectPropertyIndividual);
             manager.addAxiom(ontology, subjectPropertyAssertion);
        	 
        	
        }
        

        System.out.println("Axiom added");

        
        
	}//end of method 
	
	

}//end of class
