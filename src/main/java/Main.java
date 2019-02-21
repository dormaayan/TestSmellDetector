import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import extractTestFiles.FindClasses;
import testsmell.AbstractSmell;
import testsmell.ResultsWriter;
import testsmell.TestFile;
import testsmell.TestSmellDetector;

public class Main {
	@SuppressWarnings("resource")
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

		FindClasses.extractTestClassesToCSVs(projects);

		for (String project : projects) {
			String projectName = project.split("/")[project.split("/").length - 1];
			TestSmellDetector testSmellDetector = TestSmellDetector.createTestSmellDetector();
			/*
			 * Read the input file and build the TestFile objects
			 */
			BufferedReader in = new BufferedReader(new FileReader("inputPaths/" + projectName + ".csv"));
			String str;

			String[] lineItem;
			TestFile testFile;
			List<TestFile> testFiles = new ArrayList<>();
			while ((str = in.readLine()) != null) {
				// use comma as separator
				lineItem = str.split(",");

				// check if the test file has an associated production file
				if (lineItem.length == 2) {
					testFile = new TestFile(lineItem[0], lineItem[1], "");
				} else {
					testFile = new TestFile(lineItem[0], lineItem[1], lineItem[2]);
				}

				testFiles.add(testFile);
			}

			/*
			 * Initialize the output file - Create the output file and add the column names
			 */
			ResultsWriter resultsWriter = ResultsWriter.createResultsWriter(projectName);
			List<String> columnNames;
			List<String> columnValues;

			columnNames = testSmellDetector.getTestSmellNames();
			columnNames.add(0, "Test Methods");
			resultsWriter.writeColumnName(columnNames);

			/*
			 * Iterate through all test files to detect smells and then write the output
			 */
			TestFile tempFile;
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date;
			for (TestFile file : testFiles) {
				date = new Date();
				System.out.println(dateFormat.format(date) + " Processing: " + file.getTestFilePath());
				System.out.println("Processing: " + file.getTestFilePath());

				// detect smells
				tempFile = testSmellDetector.detectSmells(file);
				for (String testMethod : file.getTestMethods()) {
					columnValues = new ArrayList<>();
					columnValues.add(testMethod);
					for (AbstractSmell smell : tempFile.getTestSmells()) {
						try {
							columnValues.add(String
									.valueOf(file.getSmellsMethods().get(smell.getSmellName()).contains(testMethod)));
						} catch (NullPointerException e) {
							columnValues.add("False");
						}
					}
					resultsWriter.writeLine(columnValues);
				}

			}

			System.out.println("end");
		}
	}

}
