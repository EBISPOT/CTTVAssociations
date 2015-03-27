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
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
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
boolean cause = false;
boolean symptom = false ;


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
//getting diabetes cause 
Cell c6 = r.getCell(6);
if (c6.toString().equalsIgnoreCase("x")){
cause = true; 
}


//getting diabetes symptom
Cell c7 = r.getCell(7);
if (c7.toString().equalsIgnoreCase("x")){
symptom = true; 
}


//associated with type1 diabetes 
Cell c8 = r.getCell(8);
if (c8.toString().equalsIgnoreCase("x")){
//subject - type 1 diabetes 
String type1 = "http://purl.obolibrary.org/obo/DIAB_000004"; 
String sourceDB = "DIAB ontology";
String assocDate = "07/12/2012";
String creatorName = "Frauke Neff";
createOBANAssociation(manager, ontology, factory, type1, object, cause, symptom, pmid, assocDate, sourceDB, freq, creatorName);
}


//associated with type 2 diabetes 
String subject = "http://purl.obolibrary.org/obo/DIAB_000005"; 
String sourceDB = "DIAB ontology";
String assocDate = "07/12/2012";
String creatorName = "Frauke Neff";
//mint subject and object assertions
                createOBANAssociation(manager, ontology, factory, subject, object, preDiab, manifest_diabetes , complications, cause, symptom, pmid, assocDate, sourceDB, freq, creatorName);	
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




private void createOBANAssociation(OWLOntologyManager manager,
OWLOntology ontology, OWLDataFactory factory, String subject,
String object,boolean cause, boolean symptom,String pmid, String assocDate, String sourceDB, String freq, String creatorName){


DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss'Z'");
        //get current date time with Date()
        String date = dateFormat.format(new Date());

        
        object = "http://purl.obolibrary.org/obo/"+object;
        //create IRIs for both subject and object
        IRI subjectIRI = IRI.create(subject);
        IRI objectIRI = IRI.create(object);
        //generate hash for association & provenance URI fragment
        // association uri is generated from a combination of the subject and object URIs
        String assocHash = HashingIdGenerator.generateHashEncodedID(subject+object);
        //create IRI for association instance
        String assocString = new StringBuilder().append("http://purl.obolibrary.org/cttv/").append(assocHash).toString();
        IRI assocIRI = IRI.create(assocString);

        
        //form the string to hash for the provenance part
        StringBuilder sb = new StringBuilder();
        if(pmid != null && !pmid.isEmpty()){
        sb.append(pmid);
        }
        if(assocDate != null && !assocDate.isEmpty()){
        sb.append(assocDate);
        }
        if(creatorName != null && !creatorName.isEmpty()){
        sb.append(creatorName);
        }
        if(sourceDB != null && !sourceDB.isEmpty()){
        sb.append(sourceDB);
        }

        
        //the provenance is generated from the 
        String provHash = HashingIdGenerator.generateHashEncodedID(sb.toString());
        String provString = new StringBuilder().append("http://purl.obolibrary.org/cttv/").append(provHash).toString();
        IRI provIRI = IRI.create(provString);

        //mint classes
        OWLClass association = factory.getOWLClass(IRI.create("http://purl.org/oban/association"));
        OWLClass provenance = factory.getOWLClass(IRI.create("http://purl.org/oban/provenance"));

        //mint properties used in minting associations
        //mint object properties
        OWLObjectProperty hasSubject = factory.getOWLObjectProperty(IRI.create("http://purl.org/oban/association_has_subject"));
        OWLObjectProperty hasObject = factory.getOWLObjectProperty(IRI.create("http://purl.org/oban/association_has_object"));
        OWLObjectProperty isAbout = factory.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/IAO_0000136"));

     
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
                getOWLObjectPropertyAssertionAxiom(isAbout, associationIndividual, provenanceIndividual);
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


        if(cause){
        IRI subjectpropIRI = IRI.create("http://purl.obolibrary.org/obo/DIAB_000019");
        OWLObjectProperty hasSubjectProperty = factory.getOWLObjectProperty(IRI.create("http://purl.org/oban/association_has_subject_property"));
       	OWLNamedIndividual subjectPropertyIndividual = factory.getOWLNamedIndividual(subjectpropIRI);
       	OWLObjectPropertyAssertionAxiom subjectPropertyAssertion = factory.
                    getOWLObjectPropertyAssertionAxiom(hasSubjectProperty, associationIndividual, subjectPropertyIndividual);
            manager.addAxiom(ontology, subjectPropertyAssertion);
        }

        
        if (symptom){
        IRI subjectpropIRI = IRI.create("http://purl.obolibrary.org/obo/DIAB_000020");
        OWLObjectProperty hasSubjectProperty = factory.getOWLObjectProperty(IRI.create("http://purl.org/oban/association_has_subject_property"));
          	OWLNamedIndividual subjectPropertyIndividual = factory.getOWLNamedIndividual(subjectpropIRI);
          	OWLObjectPropertyAssertionAxiom subjectPropertyAssertion = factory.
                       getOWLObjectPropertyAssertionAxiom(hasSubjectProperty, associationIndividual, subjectPropertyIndividual);
               manager.addAxiom(ontology, subjectPropertyAssertion);
        }

        System.out.println("Axiom added");


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
* @param creatorName
*/


private void createOBANAssociation(OWLOntologyManager manager,
OWLOntology ontology, OWLDataFactory factory, String subject,
String object, boolean preDiab2, boolean manifest_diabetes2,
boolean complications2,boolean cause, boolean symptom, String pmid, String assocDate, String sourceDB, String freq, String creatorName) {


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

        
        // add pubmedid to prov if it exists 
        if(pmid != null && ! pmid.isEmpty()){
        	//create IRI for the pubmed ID
            OWLNamedIndividual pmidIndividual = factory.getOWLNamedIndividual(IRI.create("http://identifiers.org/pubmed/" + pmid));

          //make type of edam pubmedid
            OWLClass edampmidclass = factory.getOWLClass(IRI.create("http://edamontology.org/data_1187"));

            OWLClassAssertionAxiom pmidTypeAssertion = factory.getOWLClassAssertionAxiom(edampmidclass, pmidIndividual);
            manager.addAxiom(ontology, pmidTypeAssertion);

            OWLDataFactory df = manager.getOWLDataFactory();
            OWLAnnotation labelAnno = df.getOWLAnnotation(df.getRDFSLabel(),
                    df.getOWLLiteral("pubmed ID", "en"));
            OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(edampmidclass.getIRI(),
                    labelAnno);
            // Add the axiom to the ontology
            manager.applyChange(new AddAxiom(ontology, ax));

            //mint datatype properties
            OWLObjectProperty hasPubmedID = factory.getOWLObjectProperty(IRI.create("http://purl.org/oban/has_pubmed_id"));

            //make assertion
            OWLObjectPropertyAssertionAxiom pubmedAssertion = factory.
                    getOWLObjectPropertyAssertionAxiom(hasPubmedID, provenanceIndividual, pmidIndividual);
            manager.addAxiom(ontology, pubmedAssertion);
        }


        if(assocDate != null && !assocDate.isEmpty()){
            //mint datatype properties
            OWLDataProperty hasOriginCreatedDate = factory.getOWLDataProperty(IRI.create("http://purl.org/oban/date_orgin_created"));

            //make assertion
            OWLDataPropertyAssertionAxiom assocDateAssertion = factory.
                    getOWLDataPropertyAssertionAxiom(hasOriginCreatedDate, provenanceIndividual, assocDate);
            manager.addAxiom(ontology, assocDateAssertion);

        }

      //add the source ontology if it exists
        if(sourceDB != null && !sourceDB.isEmpty()){

            //create instance for source database from the uri
            OWLNamedIndividual sourceOntoIndividual = factory.getOWLNamedIndividual(IRI.create(sourceDB));
            //mint uri for creator
            OWLClass ontologyIdentifierClass = factory.getOWLClass(IRI.create("http://edamontology.org/data_0582"));
            //mint datatype properties
            OWLObjectProperty hasSourceDB = factory.getOWLObjectProperty(IRI.create("http://purl.org/oban/has_source"));
            //make individual db a type of the EDAM Miriam class
            OWLClassAssertionAxiom miriamTypeAssertion = factory.getOWLClassAssertionAxiom(ontologyIdentifierClass, sourceOntoIndividual);
            manager.addAxiom(ontology, miriamTypeAssertion);

            //make assertion on provenance
            OWLObjectPropertyAssertionAxiom sourceAssertion = factory.
                    getOWLObjectPropertyAssertionAxiom(hasSourceDB, provenanceIndividual, sourceOntoIndividual);
            manager.addAxiom(ontology, sourceAssertion);

            /*
            //add string as a label annotation on this individual
            OWLDataFactory df = manager.getOWLDataFactory();
            OWLAnnotation labelAnno = df.getOWLAnnotation(df.getRDFSLabel(),
                    df.getOWLLiteral(sourceDB, "en"));
            OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(sourceDBIndividual.getIRI(),
                    labelAnno);
            // Add the axiom to the ontology
            manager.applyChange(new AddAxiom(ontology, ax));
            */

        }
        
      //add individual name if the source was a person - can be string or an ID such as ORCID
        if(creatorName != null && !creatorName.isEmpty()){
            //create instance for person name
            OWLNamedIndividual personIndividual = factory.getOWLNamedIndividual(IRI.create("http://purl.org/oban/" + HashingIdGenerator.generateHashEncodedID(creatorName)));
            //mint uri for creator
            OWLClass foafPersonClass = factory.getOWLClass(IRI.create("http://xmlns.com/foaf/spec/#term_Person"));
            //mint datatype properties
            OWLObjectProperty hasSourceDB = factory.getOWLObjectProperty(IRI.create("http://purl.org/oban/has_source"));

            OWLClassAssertionAxiom foafPersonTypeAssertion = factory.getOWLClassAssertionAxiom(foafPersonClass, personIndividual);
            manager.addAxiom(ontology, foafPersonTypeAssertion);


            //make assertion
            OWLObjectPropertyAssertionAxiom creatorAssertion = factory.
                    getOWLObjectPropertyAssertionAxiom(hasSourceDB, provenanceIndividual, personIndividual);
            manager.addAxiom(ontology, creatorAssertion);


            //add name string as a label annotation on this individual
            OWLDataFactory df = manager.getOWLDataFactory();
            OWLAnnotation labelAnno = df.getOWLAnnotation(df.getRDFSLabel(),
                    df.getOWLLiteral(creatorName, "en"));
            OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(personIndividual.getIRI(),
                    labelAnno);
            // Add the axiom to the ontology
            manager.applyChange(new AddAxiom(ontology, ax));

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
             
           //add  label annotation on this individual
             OWLDataFactory df = manager.getOWLDataFactory();
             OWLAnnotation labelAnno = df.getOWLAnnotation(df.getRDFSLabel(),
                     df.getOWLLiteral("manifest diabetes", "en"));
             OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(subjectPropertyIndividual.getIRI(),
                     labelAnno);
             // Add the axiom to the ontology
             manager.applyChange(new AddAxiom(ontology, ax));

         

        	
        }

        

        if(complications){
        	IRI subjectpropIRI = IRI.create("http://purl.obolibrary.org/obo/DIAB_000011");
        	OWLObjectProperty hasSubjectProperty = factory.getOWLObjectProperty(IRI.create("http://purl.org/oban/association_has_subject_property"));
        	OWLNamedIndividual subjectPropertyIndividual = factory.getOWLNamedIndividual(subjectpropIRI);
        	OWLObjectPropertyAssertionAxiom subjectPropertyAssertion = factory.
                     getOWLObjectPropertyAssertionAxiom(hasSubjectProperty, associationIndividual, subjectPropertyIndividual);
             manager.addAxiom(ontology, subjectPropertyAssertion);

             //add  label annotation on this individual
             OWLDataFactory df = manager.getOWLDataFactory();
             OWLAnnotation labelAnno = df.getOWLAnnotation(df.getRDFSLabel(),
                     df.getOWLLiteral("complications", "en"));
             OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(subjectPropertyIndividual.getIRI(),
                     labelAnno);
             // Add the axiom to the ontology
             manager.applyChange(new AddAxiom(ontology, ax));

        	
        }

        

        if(preDiab){
        	IRI subjectpropIRI = IRI.create("http://purl.obolibrary.org/obo/DIAB_000009");
        	OWLObjectProperty hasSubjectProperty = factory.getOWLObjectProperty(IRI.create("http://purl.org/oban/association_has_subject_property"));
        	OWLNamedIndividual subjectPropertyIndividual = factory.getOWLNamedIndividual(subjectpropIRI);
        	OWLObjectPropertyAssertionAxiom subjectPropertyAssertion = factory.
                     getOWLObjectPropertyAssertionAxiom(hasSubjectProperty, associationIndividual, subjectPropertyIndividual);
             manager.addAxiom(ontology, subjectPropertyAssertion);

             //add  label annotation on this individual
             OWLDataFactory df = manager.getOWLDataFactory();
             OWLAnnotation labelAnno = df.getOWLAnnotation(df.getRDFSLabel(),
                     df.getOWLLiteral("pre-diabetes", "en"));
             OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(subjectPropertyIndividual.getIRI(),
                     labelAnno);
             // Add the axiom to the ontology
             manager.applyChange(new AddAxiom(ontology, ax));

        	
        }
        if(cause){
        IRI subjectpropIRI = IRI.create("http://purl.obolibrary.org/obo/DIAB_000019");
        OWLObjectProperty hasSubjectProperty = factory.getOWLObjectProperty(IRI.create("http://purl.org/oban/association_has_subject_property"));
       	OWLNamedIndividual subjectPropertyIndividual = factory.getOWLNamedIndividual(subjectpropIRI);
       	OWLObjectPropertyAssertionAxiom subjectPropertyAssertion = factory.
                    getOWLObjectPropertyAssertionAxiom(hasSubjectProperty, associationIndividual, subjectPropertyIndividual);
            manager.addAxiom(ontology, subjectPropertyAssertion);
           
            //add  label annotation on this individual
            OWLDataFactory df = manager.getOWLDataFactory();
            OWLAnnotation labelAnno = df.getOWLAnnotation(df.getRDFSLabel(),
                    df.getOWLLiteral("cause", "en"));
            OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(subjectPropertyIndividual.getIRI(),
                    labelAnno);
            // Add the axiom to the ontology
            manager.applyChange(new AddAxiom(ontology, ax));
        }

        
        if (symptom){
        IRI subjectpropIRI = IRI.create("http://purl.obolibrary.org/obo/DIAB_000020");
        OWLObjectProperty hasSubjectProperty = factory.getOWLObjectProperty(IRI.create("http://purl.org/oban/association_has_subject_property"));
          	OWLNamedIndividual subjectPropertyIndividual = factory.getOWLNamedIndividual(subjectpropIRI);
          	OWLObjectPropertyAssertionAxiom subjectPropertyAssertion = factory.
                       getOWLObjectPropertyAssertionAxiom(hasSubjectProperty, associationIndividual, subjectPropertyIndividual);
               manager.addAxiom(ontology, subjectPropertyAssertion);
               
               //add  label annotation on this individual
               OWLDataFactory df = manager.getOWLDataFactory();
               OWLAnnotation labelAnno = df.getOWLAnnotation(df.getRDFSLabel(),
                       df.getOWLLiteral("symptom", "en"));
               OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(subjectPropertyIndividual.getIRI(),
                       labelAnno);
               // Add the axiom to the ontology
               manager.applyChange(new AddAxiom(ontology, ax));
        }

        System.out.println("Axiom added");

        

        
}//end of method 




}//end of class