/**
 * 
 */
package obanminter;

import java.io.File;
import java.util.HashMap;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 * @author drashtti
 * this class will try to get all the 
 * term labels using ids from the resp 
 * ontologies so that it can be asserted
 * in the OBAN model itself 
 */
public class GetOntoLabel {
		private OWLOntologyManager manager;
		private static HashMap<String,String> idmap;
	
	public void readOWLFiles(){
		
		 // Get hold of an ontology manager
       manager = OWLManager.createOWLOntologyManager();
       idmap = new HashMap<String,String>();
        
     // local copy
        File hpfile = new File("/Users/vasant/DiabetesOntology/diab_HPimports.owl");
        
        File mpfile = new File("/Users/vasant/DiabetesOntology/diab_MPimports.owl");
        // Now load the local copy
        OWLOntology localdiabHP;
        OWLOntology localdiabMP;
		try {
			localdiabHP = manager.loadOntologyFromOntologyDocument(hpfile);
			System.out.println("Loaded ontology: " + localdiabHP);
			
			localdiabMP = manager.loadOntologyFromOntologyDocument(mpfile);
			System.out.println("Loaded ontology: " + localdiabMP);
			
			
			OWLDataFactory df = manager.getOWLDataFactory();
			  OWLAnnotationProperty label = df
		                .getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		        for (OWLClass cls : localdiabHP.getClassesInSignature()) {
		            // Get the annotations on the class that use the label property
		            for (OWLAnnotation annotation : cls.getAnnotations(localdiabHP, label)) {
		                if (annotation.getValue() instanceof OWLLiteral) {
		                    OWLLiteral val = (OWLLiteral) annotation.getValue();
		                        System.out.println(cls.toStringID() + " -> " + val.getLiteral());
		                        idmap.put(cls.toStringID(), val.getLiteral().toString());
		                }
		            }
		        }
		        
		       
		        for (OWLClass cls : localdiabMP.getClassesInSignature()) {
		            // Get the annotations on the class that use the label property
		            for (OWLAnnotation annotation : cls.getAnnotations(localdiabMP, label)) {
		                if (annotation.getValue() instanceof OWLLiteral) {
		                    OWLLiteral val = (OWLLiteral) annotation.getValue();
		                        System.out.println(cls.toStringID() + " -> " + val.getLiteral());
		                        idmap.put(cls.toStringID(), val.getLiteral().toString());
		                }
		            }
		        }
		    }
			
		 catch (OWLOntologyCreationException e) {
			System.out.print("The ontology cannot be loaded. Please check the Ontology");
			e.printStackTrace();
		}		
		
	}
	
	
	public static String getLabel(String id){
		String label = "";
		if(idmap.containsKey(id)){
			 label = idmap.get(id);
			
		}
		else{
			System.out.print("Could not find the term to match the label. The id is " + id);
		}
		
		return label;
	}

}
