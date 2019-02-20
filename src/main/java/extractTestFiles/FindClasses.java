package extractTestFiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.base.Strings;
import com.opencsv.CSVWriter;

public class FindClasses {

	public static Set<String> extractProductionFiles(File projectDir, String projectPath) {
		Set<String> productionFiles = new HashSet<>();
		new DirExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
			try {
				new VoidVisitorAdapter<Object>() {
					@Override
					public void visit(ClassOrInterfaceDeclaration n, Object arg) {
						super.visit(n, arg);
						if (path.contains("/main/"))
							productionFiles.add(projectPath + path);
					}
				}.visit(JavaParser.parse(file), null);
			} catch (IOException e) {
				new RuntimeException(e);
			}
		}).explore(projectDir);
		return productionFiles;
	}

	public static boolean isTestingImport(String imp) {
		return imp.contains("org.junit");
	}

	public static Set<String> extractTesFiles(File projectDir, String projectPath) {
		Set<String> testFiles = new HashSet<>();
		new DirExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
			try {
				new VoidVisitorAdapter<Object>() {
					@Override
					public void visit(ImportDeclaration n, Object arg) {
						super.visit(n, arg);
						if (isTestingImport(n.getName().toString()) && !path.contains("/target/")
								&& path.contains("/src/"))
							testFiles.add(projectPath + path);
					}
				}.visit(JavaParser.parse(file), null);
			} catch (IOException e) {
				new RuntimeException(e);
			}
		}).explore(projectDir);
		return testFiles;
	}

	public static Map<String, String> findMatches(Set<String> testFiles, Set<String> productionFiles) {
		Map<String, String> testToProduction = new HashMap<>();
		for (String test : testFiles) {
			for (String production : productionFiles) {
				String testName = test.split("/")[test.split("/").length - 1];
				String testNameClean = testName.split(".java")[testName.split(".java").length - 1];
				String productionName = production.split("/")[production.split("/").length - 1];
				String productionNameClean = productionName.split(".java")[productionName.split(".java").length - 1];
				if ((testNameClean.equals("Test" + productionNameClean))
						|| (testNameClean.equals(productionNameClean + "Test"))) {
					testToProduction.put(test, production);
				}
			}
			if (!testToProduction.containsKey(test))
				testToProduction.put(test, "");
		}
		return testToProduction;
	}

	public static void translateToCSV(Map<String, String> testToProduction, String projectPath) throws IOException {
		String projectName = projectPath.split("/")[projectPath.split("/").length - 1];
		CSVWriter csvWriter = new CSVWriter(new FileWriter("inputPaths/" + projectName + ".csv"));
		for (String test : testToProduction.keySet())
			csvWriter.writeNext(new String[] { projectName, test, testToProduction.get(test) });
		csvWriter.close();
		System.out.println("done exporting " + projectName);

	}

	public static void main(String[] args) throws IOException {
		String[] projects = { "/Users/Dor/Desktop/Coverage/cors-filter-cors-filter-1.0.1",
				"/Users/Dor/Desktop/Coverage/commons-functor-FUNCTOR_1_0_RC1",
				"/Users/Dor/Desktop/Coverage/cucumber-reporting-cucumber-reporting-4.3.0",
				"/Users/Dor/Desktop/Coverage/jsoup-jsoup-1.11.3", "/Users/Dor/Desktop/Coverage/exp4j-exp4j-0.4.8",
				"/Users/Dor/Desktop/Coverage/commons-io-commons-io-2.6-RC3",
				"/Users/Dor/Desktop/Coverage/retrofit-parent-2.0.0-beta3",
				"/Users/Dor/Desktop/Coverage/commons-dbcp-commons-dbcp-2.5.0",
				"/Users/Dor/Desktop/Coverage/commons-exec-1.3",
				"/Users/Dor/Desktop/Coverage/commons-collections-collections-4.3-RC2" };

		for (String projectPath : projects) {
			File projectDir = new File(projectPath);
			Set<String> tests = extractTesFiles(projectDir, projectPath);
			Set<String> productions = extractProductionFiles(projectDir, projectPath);
			translateToCSV(findMatches(tests, productions), projectPath);
		}
	}
}