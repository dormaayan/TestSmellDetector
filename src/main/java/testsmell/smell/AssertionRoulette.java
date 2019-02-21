package testsmell.smell;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import testsmell.AbstractSmell;
import testsmell.TestFile;
import testsmell.TestMethod;
import testsmell.Util;

/**
 * "Guess what's wrong?" This smell comes from having a number of assertions in
 * a test method that have no explanation. If one of the assertions fails, you
 * do not know which one it is. A. van Deursen, L. Moonen, A. Bergh, G. Kok,
 * “Refactoring Test Code”, Technical Report, CWI, 2001.
 */
public class AssertionRoulette extends AbstractSmell {

	private List<TestMethod> smellyElementList;
	private TestFile currentTestFile;

	public AssertionRoulette() {
		smellyElementList = new ArrayList<>();
	}

	/**
	 * Checks of 'Assertion Roulette' smell
	 */
	@Override
	public String getSmellName() {
		return "Assertion Roulette";
	}

	/**
	 * Returns true if any of the elements has a smell
	 */
	@Override
	public boolean getHasSmell() {
		return smellyElementList.stream().filter(x -> x.getHasSmell()).count() >= 1;
	}

	/**
	 * Analyze the test file for test methods for multiple assert statements without
	 * an explanation/message
	 */
	@Override
	public void runAnalysis(TestFile testFile, CompilationUnit testFileCompilationUnit,
			CompilationUnit productionFileCompilationUnit, String testFileName, String productionFileName)
			throws FileNotFoundException {
		this.currentTestFile = testFile;
		AssertionRoulette.ClassVisitor classVisitor;
		classVisitor = new AssertionRoulette.ClassVisitor();
		classVisitor.visit(testFileCompilationUnit, null);
	}

	/**
	 * Returns the set of analyzed elements (i.e. test methods)
	 */
	@Override
	public List<TestMethod> getSmellyElements() {
		return smellyElementList;
	}

	private class ClassVisitor extends VoidVisitorAdapter<Void> {
		private MethodDeclaration currentMethod = null;
		private int assertNoMessageCount = 0;
		private int assertCount = 0;
		TestMethod testMethod;

		// examine all methods in the test class
		@Override
		public void visit(MethodDeclaration n, Void arg) {
			if (Util.isValidTestMethod(n)) {
				currentMethod = n;
				testMethod = new TestMethod(n.getNameAsString());
				currentTestFile.addTest(testMethod);
				testMethod.setHasSmell(false); // default value is false (i.e. no smell)
				super.visit(n, arg);

				// if there is only 1 assert statement in the method, then a explanation message
				// is not needed
				if (assertCount == 1)
					testMethod.setHasSmell(false);
				else if (assertNoMessageCount >= 1) {
					testMethod.setHasSmell(true);
					currentTestFile.addSmellMethod("Assertion Roulette", testMethod);
				}

				testMethod.addDataItem("AssertCount", String.valueOf(assertNoMessageCount));

				smellyElementList.add(testMethod);

				// reset values for next method
				currentMethod = null;
				assertCount = 0;
				assertNoMessageCount = 0;
			}
		}

		// examine the methods being called within the test method
		@Override
		public void visit(MethodCallExpr n, Void arg) {
			super.visit(n, arg);
			if (currentMethod != null) {
				// if the name of a method being called is an assertion and has 3 parameters
				if (n.getNameAsString().startsWith(("assertArrayEquals"))
						|| n.getNameAsString().startsWith(("assertEquals"))
						|| n.getNameAsString().startsWith(("assertNotSame"))
						|| n.getNameAsString().startsWith(("assertSame"))
						|| n.getNameAsString().startsWith(("assertThat"))) {
					assertCount++;
					// assert methods that do not contain a message
					if (n.getArguments().size() < 3) {
						assertNoMessageCount++;
					}
				}
				// if the name of a method being called is an assertion and has 2 parameters
				else if (n.getNameAsString().equals("assertFalse") || n.getNameAsString().equals("assertNotNull")
						|| n.getNameAsString().equals("assertNull") || n.getNameAsString().equals("assertTrue")) {
					assertCount++;
					// assert methods that do not contain a message
					if (n.getArguments().size() < 2) {
						assertNoMessageCount++;
					}
				}

				// if the name of a method being called is 'fail'
				else if (n.getNameAsString().equals("fail")) {
					assertCount++;
					// fail method does not contain a message
					if (n.getArguments().size() < 1) {
						assertNoMessageCount++;
					}
				}

			}
		}

	}
}
