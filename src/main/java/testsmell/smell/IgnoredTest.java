package testsmell.smell;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import testsmell.AbstractSmell;
import testsmell.SmellyElement;
import testsmell.TestClass;
import testsmell.TestFile;
import testsmell.TestMethod;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class IgnoredTest extends AbstractSmell {

	private List<TestMethod> smellyElementList;
	private TestFile currentTestFile;

	public IgnoredTest() {
		smellyElementList = new ArrayList<>();
	}

	/**
	 * Checks of 'Ignored Test' smell
	 */
	@Override
	public String getSmellName() {
		return "IgnoredTest";
	}

	/**
	 * Returns true if any of the elements has a smell
	 */
	@Override
	public boolean getHasSmell() {
		return smellyElementList.stream().filter(x -> x.getHasSmell()).count() >= 1;
	}

	/**
	 * Analyze the test file for test methods that contain Ignored test methods
	 */
	@Override
	public void runAnalysis(TestFile testFile, CompilationUnit testFileCompilationUnit,
			CompilationUnit productionFileCompilationUnit, String testFileName, String productionFileName)
			throws FileNotFoundException {
		this.currentTestFile = testFile;
		IgnoredTest.ClassVisitor classVisitor;
		classVisitor = new IgnoredTest.ClassVisitor();
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
		TestClass testClass;

		/**
		 * The purpose of this method is to 'visit' all test methods in the test file.
		 */
		@Override
		public void visit(MethodDeclaration n, Void arg) {

			// JUnit 4
			// check if test method has Ignore annotation
			if (n.getAnnotationByName("Test").isPresent()) {
				if (n.getAnnotationByName("Ignore").isPresent()) {
					testMethod = new TestMethod(n.getNameAsString());
					currentTestFile.addTest(testMethod);
					testMethod.setHasSmell(true);
					smellyElementList.add(testMethod);
					currentTestFile.addSmellMethod("IgnoredTest", testMethod);
					return;
				}
			}

			// JUnit 3
			// check if test method is not public
			if (n.getNameAsString().toLowerCase().startsWith("test")) {
				if (!n.getModifiers().contains(Modifier.PUBLIC)) {
					testMethod = new TestMethod(n.getNameAsString());
					currentTestFile.addTest(testMethod);
					testMethod.setHasSmell(true);
					smellyElementList.add(testMethod);
					currentTestFile.addSmellMethod("IgnoredTest", testMethod);
					return;
				}
			}
		}

	}
}
