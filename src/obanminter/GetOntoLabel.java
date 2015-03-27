/**
 * 
 */
package obanminter;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * @author drashtti
 * this class will try to get all the 
 * term labels using ids from the resp 
 * ontologies so that it can be asserted
 * in the OBAN model itself 
 */
public class GetOntoLabel {
	
	
	public void readOWLFiles(){
		
		 // Get hold of an ontology manager
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        
     // local copy
        File hpfile = new File("/home/drashtti/Desktop/ontologies/Diabetes-Onto/diab_HPimports.owl");
        
        File mpfile = new File("/home/drashtti/Desktop/ontologies/Diabetes-Onto/diab_MPimports.owl");
        // Now load the local copy
        OWLOntology localPizza = manager.loadOntologyFromOntologyDocument(file);
        System.out.println("Loaded ontology: " + localPizza);
		
		
		
		
	}

}
