package testsmell;

import java.io.FileNotFoundException;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;

public abstract class AbstractSmell {
	public abstract String getSmellName();

	public abstract boolean getHasSmell();

	public abstract void runAnalysis(TestFile testFile, CompilationUnit testFileCompilationUnit,
			CompilationUnit productionFileCompilationUnit, String testFileName, String productionFileName)
			throws FileNotFoundException;

	public abstract List<TestMethod> getSmellyElements();
}
