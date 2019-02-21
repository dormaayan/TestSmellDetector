package testsmell;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * This class is utilized to write output to a CSV file
 */
public class ResultsWriter {

	private String outputFile;
	private FileWriter writer;

	/**
	 * Creates the file into which output it to be written into. Results from each
	 * file will be stored in a new file
	 * 
	 * @throws IOException
	 */
	private ResultsWriter(String projectName) throws IOException {
		outputFile = "outputSmells/" + projectName + ".csv";
		writer = new FileWriter(outputFile, false);
	}

	/**
	 * Factory method that provides a new instance of the ResultsWriter
	 * 
	 * @return new ResultsWriter instance
	 * @throws IOException
	 */
	public static ResultsWriter createResultsWriter(String projectName) throws IOException {
		return new ResultsWriter(projectName);
	}

	/**
	 * Writes column names into the CSV file
	 * 
	 * @param columnNames
	 *            the column names
	 * @throws IOException
	 */
	public void writeColumnName(List<String> columnNames) throws IOException {
		writeOutput(columnNames);
	}

	/**
	 * Writes column values into the CSV file
	 * 
	 * @param columnValues
	 *            the column values
	 * @throws IOException
	 */
	public void writeLine(List<String> columnValues) throws IOException {
		writeOutput(columnValues);
	}

	/**
	 * Appends the input values into the CSV file
	 * 
	 * @param dataValues
	 *            the data that needs to be written into the file
	 * @throws IOException
	 */
	private void writeOutput(List<String> dataValues) throws IOException {
		writer = new FileWriter(outputFile, true);

		for (int i = 0; i < dataValues.size(); i++) {
			writer.append(String.valueOf(dataValues.get(i)));

			if (i != dataValues.size() - 1)
				writer.append(",");
			else
				writer.append(System.lineSeparator());

		}
		writer.flush();
		writer.close();
	}
}
