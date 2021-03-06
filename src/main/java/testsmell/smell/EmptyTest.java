package testsmell.smell;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import testsmell.AbstractSmell;
import testsmell.TestFile;
import testsmell.TestMethod;
import testsmell.Util;

/**
 * This class checks if a test method is empty (i.e. the method does not contain
 * statements in its body) If the the number of statements in the body is 0,
 * then the method is smelly
 */
public class EmptyTest extends AbstractSmell {

	private List<TestMethod> smellyElementList;
	private TestFile currentTestFile;

	public EmptyTest() {
		smellyElementList = new ArrayList<>();
	}

	/**
	 * Checks of 'Empty Test' smell
	 */
	@Override
	public String getSmellName() {
		return "EmptyTest";
	}

	/**
	 * Returns true if any of the elements has a smell
	 */
	@Override
	public boolean getHasSmell() {
		return smellyElementList.stream().filter(x -> x.getHasSmell()).count() >= 1;
	}

	/**
	 * Analyze the test file for test methods that are empty (i.e. no method body)
	 */
	@Override
	public void runAnalysis(TestFile testFile, CompilationUnit testFileCompilationUnit,
			CompilationUnit productionFileCompilationUnit, String testFileName, String productionFileName)
			throws FileNotFoundException {
		this.currentTestFile = testFile;
		EmptyTest.ClassVisitor classVisitor;
		classVisitor = new EmptyTest.ClassVisitor();
		classVisitor.visit(testFileCompilationUnit, null);
	}

	/**
	 * Returns the set of analyzed elements (i.e. test methods)
	 */
	@Override
	public List<TestMethod> getSmellyElements() {
		return smellyElementList;
	}

	/**
	 * Visitor class
	 */
	private class ClassVisitor extends VoidVisitorAdapter<Void> {
		TestMethod testMethod;

		/**
		 * The purpose of this method is to 'visit' all test methods in the test file
		 */
		@Override
		public void visit(MethodDeclaration n, Void arg) {
			if (Util.isValidTestMethod(n)) {
				testMethod = new TestMethod(n.getNameAsString());
				currentTestFile.addTest(testMethod);
				testMethod.setHasSmell(false); // default value is false (i.e. no smell)
				// method should not be abstract
				if (!n.isAbstract()) {
					if (n.getBody().isPresent()) {
						// get the total number of statements contained in the method
						if (n.getBody().get().getStatements().size() == 0) {
							testMethod.setHasSmell(true); // the method has no statements (i.e no body)
							currentTestFile.addSmellMethod("EmptyTest", testMethod);
						}
					}
				}
				smellyElementList.add(testMethod);
			}
		}
	}
}
