/**
 * 
 */
package uk.ac.ebi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.brain.core.Brain;
import uk.ac.ebi.brain.error.BrainException;
import uk.ac.ebi.brain.error.ClassExpressionException;
import uk.ac.ebi.brain.error.ExistingClassException;
import uk.ac.ebi.brain.error.ExistingEntityException;


/**
 * @author Samuel Croset
 *
 */
public class BrainQueryTest {

	Brain brain;

	@Before
	public void bootstrap() throws BrainException {
		brain = new Brain("http://localhost/", "http://localhost/test.owl");
		brain.learn("src/test/resources/dev.owl");
	}

	@After
	public void dispose() {
		brain.sleep();
	}

	@Test
	public void getNamedSubClass() throws BrainException {
		Brain brain = new Brain();
		brain.addClass("Animal");
		brain.addClass("Lion");
		brain.subClassOf("Lion", "Animal");
		brain.getSubClasses("Animal", false);
		assertEquals(true, brain.getSubClasses("Animal", false).contains("Lion"));
		assertEquals(true, brain.getSubClasses("Animal", false).contains("Lion"));
	}

	@Test
	public void getNamedIndividual() throws BrainException {
		Brain brain = new Brain();
		brain.learn("src/test/resources/individuals.owl");
		List<String>individuals = brain.getInstances("Human", false);
		assertEquals(1, individuals.size());
		assertEquals("Joe", individuals.get(0));
		individuals = brain.getInstances("Human", true);
		assertEquals(0, individuals.size());
		individuals = brain.getInstances("Fireman", true);
		assertEquals(1, individuals.size());
		individuals = brain.getInstancesFromLabel("'a human'", false);
		assertEquals(1, individuals.size());
	}
	
	//TODO more test could be done regarding individuals - like anonymous class expressions (from label for instance)

	@Test
	public void getUnsatisfiableClasses() throws BrainException {
		Brain brain = new Brain();
		brain.addClass("A");
		brain.addClass("B");
		brain.addClass("C");
		brain.disjointClasses("A", "B");
		brain.subClassOf("C", "A");
		brain.subClassOf("C", "B");
		brain.sleep();
		assertEquals(1, brain.getUnsatisfiableClasses().size());
		brain.sleep();
	}

	@Test
	public void reStart() throws BrainException {
		brain.sleep();
		List<String> subClasses = brain.getSubClasses("I", false);
		assertEquals(2, subClasses.size());
	}

	@Test
	public void owlProfileTest() throws BrainException {
		boolean satisfied = brain.hasElProfile();
		assertEquals(true, satisfied);
		List<String> violations = brain.getElProfileViolations();
		assertEquals(0, violations.size());
	}

	@Test
	public void prefixesAndLearnTest() throws BrainException {
		Brain newBrain = new Brain();
		newBrain.addClass("http://whatever.com/TEST_A");
		newBrain.prefix("http://whatever.com/", "whatever");
		brain.prefix("http://www.example.org/", "example");
		brain.learn(newBrain);
		assertEquals("http://whatever.com/", brain.getPrefixManager().getPrefix("whatever:"));
		assertEquals("http://www.example.org/", brain.getPrefixManager().getPrefix("example:"));
	}


	@Test
	public void classifyTest() {
		brain.classify();
	}

	@Test
	public void getDirectSubClassesTest() throws ClassExpressionException{
		List<String> subClasses = brain.getSubClasses("I", true);
		assertEquals(1, subClasses.size());
	}

	@Test
	public void getIndirectSubClassesTest() throws BrainException {	
		List<String> subClasses = brain.getSubClasses("G", false);
		assertEquals(3, subClasses.size());
	}

	@Test
	public void getDirectAnonymousSubClassesTest() throws BrainException {	
		List<String> subClasses = brain.getSubClasses("part-of some L", true);
		assertEquals(1, subClasses.size());
	}

	@Test
	public void getIndirectAnonymousSubClassesTest() throws BrainException {	
		List<String> subClasses = brain.getSubClasses("part-of some L", false);
		assertEquals(3, subClasses.size());
	}

	@Test
	public void getAnonymousClassesNewOntology() throws BrainException {
		Brain brain = new Brain();
		brain.addClass("A");
		brain.addClass("B");
		brain.addObjectProperty("part-of");
		brain.subClassOf("B", "part-of some A");
		List<String> subClasses = brain.getSubClasses("part-of some A", true);
		assertEquals(1, subClasses.size());
	}

	@Test(expected = ClassExpressionException.class)
	public void getAnonymousClassesErrorNewOntology() throws BrainException {
		Brain brain = new Brain();
		brain.addClass("A");
		brain.addClass("B");
		brain.addObjectProperty("part-of");
		brain.subClassOf("B", "part-of some C");
		List<String> subClasses = brain.getSubClasses("part-of some A", true);
		assertEquals(1, subClasses.size());
	}


	@Test
	public void getSuperClassesTest() throws BrainException {
		List<String> superClasses = brain.getSuperClasses("C", false);
		assertEquals(4, superClasses.size());
		List<String> superClasses1 = brain.getSuperClasses("C", true);
		assertEquals(1, superClasses1.size());
	}

	@Test
	public void getEquivalentClassesTest() throws BrainException {
		List<String> equivalentClasses = brain.getEquivalentClasses("M");
		assertEquals(1, equivalentClasses.size());
		assertEquals("N", equivalentClasses.get(0));
	}

	@Test(expected = ClassExpressionException.class)
	public void errorsWhenQueryingForLabel() throws BrainException {
		List<String> subClasses = brain.getSubClasses("ID01", false);
		assertEquals(1, subClasses.size());
		@SuppressWarnings("unused")
		List<String> subClasses1 = brain.getSubClassesFromLabel("ID011", false);
	}

	@Test
	public void getSubClassesFromLabelTest() throws BrainException {		
		List<String> subClasses = brain.getSubClassesFromLabel("animal", false);
		assertEquals(2, subClasses.size());
		assertEquals("ID02", subClasses.get(0));
		assertEquals("R", subClasses.get(1));

		List<String> subClasses1 = brain.getSubClassesFromLabel("part-of some animal", false);
		assertEquals(2, subClasses1.size());
		assertEquals("ID01", subClasses1.get(0));
	}

	@Test
	public void getFromSpaceSeparatedLabels() throws BrainException {
		List<String> equivalents = brain.getEquivalentClassesFromLabel("'pouet pouet'");
		assertEquals(1, equivalents.size());
		assertEquals("A", equivalents.get(0));
	}

	@Test
	public void getSuperClassesFromLabelTest() throws BrainException {
		List<String> superClasses = brain.getSuperClassesFromLabel("animal", false);
		assertEquals(1, superClasses.size());
		assertEquals("Thing", superClasses.get(0));

		List<String> subClasses1 = brain.getSuperClassesFromLabel("part-of some animal", false);
		assertEquals(1, subClasses1.size());
		assertEquals("Thing", subClasses1.get(0));
	}

	@Test
	public void getEquivalentClassesFromLabelTest() throws BrainException {
		List<String> equivalentClasses = brain.getEquivalentClassesFromLabel("pouet");
		assertEquals(1, equivalentClasses.size());
		assertEquals("Z", equivalentClasses.get(0));
	}

	@Test
	public void getEquivalentAnonClassesTest() throws BrainException {
		List<String> equivalentClasses = brain.getEquivalentClasses("part-of some M");
		assertEquals(1, equivalentClasses.size());
		assertEquals("O", equivalentClasses.get(0));
	}

	@Test
	public void isSubClassTest() throws BrainException {
		boolean isSubClass = brain.isSubClass("D", "Thing", false);
		assertEquals(true, isSubClass);
		boolean isSubClass1 = brain.isSubClass("Q", "part-of some K", false);
		assertEquals(true, isSubClass1);
		boolean isSubClass2 = brain.isSubClass("part-of some K", "Q", false);
		assertEquals(false, isSubClass2);
	}

	@Test
	public void isSuperClassTest() throws BrainException {
		boolean isSuperClass = brain.isSuperClass("Thing", "D", false);
		assertEquals(true, isSuperClass);
		boolean isSuperClass1 = brain.isSuperClass("Q", "part-of some K", false);
		assertEquals(false, isSuperClass1);
		boolean isSuperClass2 = brain.isSuperClass("part-of some K", "Q", false);
		assertEquals(true, isSuperClass2);
	}

	@Test
	public void knowsTest() {
		boolean knowsClass = brain.knowsClass("M");
		assertEquals(true, knowsClass);
		boolean notknowsClass = brain.knowsClass("POUET");
		assertEquals(false, notknowsClass);
		boolean notknowsObjectProperty = brain.knowsObjectProperty("part-of");
		assertEquals(true, notknowsObjectProperty);
		boolean notknowsDataProperty = brain.knowsDataProperty("age");
		assertEquals(false, notknowsDataProperty);
		boolean notknowsAnnotationProperty = brain.knowsAnnotationProperty("testing");
		assertEquals(true, notknowsAnnotationProperty);
	}

	@Test
	public void getLabelTest() throws BrainException {
		String label = brain.getLabel("A");
		assertEquals("pouet", label);
		String comment = brain.getComment("A");
		assertEquals("comment attached to the class", comment);
		String isDefinedBy = brain.getIsDefinedBy("A");
		assertEquals("something", isDefinedBy);
		String seeAlso = brain.getSeeAlso("A");
		assertEquals("bar", seeAlso);
		String testing = brain.getAnnotation("A", "testing");
		assertEquals("whatever", testing);
	}
	
	@Test
	public void getAnnotationArrayTest() throws BrainException {
		List<String> annotations = brain.getAnnotations("G", "testing");
		assertEquals(2, annotations.size());
		// fix sorting, underlying data structure is a set, so order in the list is not guaranteed.
		Collections.sort(annotations); 
		assertEquals("value1", annotations.get(0));
		assertEquals("value2", annotations.get(1));
	}

	@Test
	public void learnFromLocalFile() throws BrainException {
		Brain brain = new Brain();
		brain.learn("src/test/resources/demo.owl");
		brain.addClass("A");
		brain.save("src/test/resources/output.owl");	
	}

	@Test(expected = ExistingClassException.class)
	public void learnSupportsIdenticalIRIs() throws BrainException {
		Brain brain = new Brain();
		brain.addClass("http://www.example.org/N");
		brain.learn("src/test/resources/dev.owl");
		brain.addClass("M");
	}

	@Test(expected = ExistingEntityException.class)
	public void learnSupportsIdenticalIRIsButNotForDifferentTypes() throws BrainException {
		Brain brain = new Brain();
		brain.addClass("http://www.example.org/part-of");
		brain.learn("src/test/resources/dev.owl");
	}

	@Test
	public void learnOntologyFromTheWeb() throws BrainException {
		Brain brain = new Brain();
		brain.learn("https://raw.github.com/loopasam/Brain/master/src/test/resources/demo.owl");
		assertNotNull(brain.getOWLClass("Cell"));
	}

	@Test
	public void learnFromOtherBrainTest() throws BrainException {
		Brain brain = new Brain();
		brain.addClass("A");
		Brain brain1 = new Brain();
		brain1.learn(brain);
		assertNotNull(brain1.getOWLClass("A"));
	}

	@Test(expected = ExistingEntityException.class)
	public void learnFromOtherBrainErrorTest() throws BrainException {
		Brain brain = new Brain();
		brain.addClass("http://example.org/A");
		brain.addClass("B");
		Brain brain1 = new Brain();
		brain1.addClass("A");
		brain1.learn(brain);
	}


	@Test
	public void getTopClass() throws BrainException {
		Brain brain = new Brain();
		assertNotNull(brain.getOWLClass("Thing"));
	}


	@Test
	public void getUnstatisfiableClasses() throws BrainException {
		List<String> unsatisfiableClasses = brain.getUnsatisfiableClasses();
		assertEquals(1, unsatisfiableClasses.size());
		assertEquals("R", unsatisfiableClasses.get(0));
	}

}
