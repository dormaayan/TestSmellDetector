package testsmell.smell;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import testsmell.AbstractSmell;
import testsmell.TestFile;
import testsmell.TestMethod;
import testsmell.Util;

public class SensitiveEquality extends AbstractSmell {

	private List<TestMethod> smellyElementList;
	private TestFile currentTestFile;

	public SensitiveEquality() {
		smellyElementList = new ArrayList<>();
	}

	/**
	 * Checks of 'Sensitive Equality' smell
	 */
	@Override
	public String getSmellName() {
		return "Sensitive Equality";
	}

	/**
	 * Returns true if any of the elements has a smell
	 */
	@Override
	public boolean getHasSmell() {
		return smellyElementList.stream().filter(x -> x.getHasSmell()).count() >= 1;
	}

	/**
	 * Analyze the test file for test methods the 'Sensitive Equality' smell
	 */
	@Override
	public void runAnalysis(TestFile testFile, CompilationUnit testFileCompilationUnit,
			CompilationUnit productionFileCompilationUnit, String testFileName, String productionFileName)
			throws FileNotFoundException {
		this.currentTestFile = testFile;
		SensitiveEquality.ClassVisitor classVisitor;
		classVisitor = new SensitiveEquality.ClassVisitor();
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
		private int sensitiveCount = 0;
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

				if (sensitiveCount >= 1) {
					testMethod.setHasSmell(true);
					currentTestFile.addSmellMethod("Sensitive Equality", testMethod);
				}
				testMethod.addDataItem("SensitiveCount", String.valueOf(sensitiveCount));

				smellyElementList.add(testMethod);

				// reset values for next method
				currentMethod = null;
				sensitiveCount = 0;
			}
		}

		// examine the methods being called within the test method
		@Override
		public void visit(MethodCallExpr n, Void arg) {
			super.visit(n, arg);
			if (currentMethod != null) {
				// if the name of a method being called start with 'assert'
				if (n.getNameAsString().startsWith(("assert"))) {
					// assert methods that contain toString
					for (Expression argument : n.getArguments()) {
						if (argument.toString().contains("toString")) {
							sensitiveCount++;
						}
					}
				}
				// if the name of a method being called is 'fail'
				else if (n.getNameAsString().equals("fail")) {
					// fail methods that contain toString
					for (Expression argument : n.getArguments()) {
						if (argument.toString().contains("toString")) {
							sensitiveCount++;
						}
					}
				}

			}
		}

	}
}
