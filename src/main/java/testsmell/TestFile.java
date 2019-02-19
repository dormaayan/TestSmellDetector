package testsmell;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestFile {
	private String app, testFilePath, productionFilePath, testFileName;
	private List<AbstractSmell> testSmells;
	private Map<String, List<String>> smellsMethods;
	private Set<String> testMethods;

	public String getApp() {
		return app;
	}

	public String getProductionFilePath() {
		return productionFilePath;
	}

	public String getTestFilePath() {
		return testFilePath;
	}

	public List<AbstractSmell> getTestSmells() {
		return testSmells;
	}

	public boolean getHasProductionFile() {
		return ((productionFilePath != null && !productionFilePath.isEmpty()));
	}

	public TestFile(String app, String testFilePath, String productionFilePath) {
		this.app = app;
		this.testFilePath = testFilePath;
		this.productionFilePath = productionFilePath;
		this.testSmells = new ArrayList<>();
		this.smellsMethods = new HashMap<>();
		this.testMethods = new HashSet<>();
		this.testFileName = (this.testFilePath.split("/")[this.testFilePath.split("/").length - 1]);
		if (this.testFileName.split(".java").length != 0)
			this.testFileName = this.testFileName.split(".java")[0];
	}

	public void addSmell(AbstractSmell smell) {
		testSmells.add(smell);
	}

	public Set<String> getTestMethods() {
		return this.testMethods;
	}

	public void addTest(TestMethod m) {
		testMethods.add(testFileName + "." + m.getElementName());
	}

	public void addSmellMethod(String smell, TestMethod testMethod) {
		String methodName = testFileName + "." + testMethod.getElementName();
		if (smellsMethods.containsKey(smell)) {
			smellsMethods.get(smell).add(methodName);
		} else {
			smellsMethods.put(smell, new ArrayList<>());
			smellsMethods.get(smell).add(methodName);
		}
	}

	public Map<String, List<String>> getSmellsMethods() {
		return this.smellsMethods;
	}

	public String getTagName() {
		return testFilePath.split("////")[4];
	}

	public String getTestFileName() {
		int lastIndex = testFilePath.lastIndexOf("\\");
		return testFilePath.substring(lastIndex + 1, testFilePath.length());
	}

	public String getTestFileNameWithoutExtension() {
		int lastIndex = getTestFileName().lastIndexOf(".");
		return getTestFileName().substring(0, lastIndex);
	}

	public String getProductionFileNameWithoutExtension() {
		int lastIndex = getProductionFileName().lastIndexOf(".");
		if (lastIndex == -1)
			return "";
		return getProductionFileName().substring(0, lastIndex);
	}

	public String getProductionFileName() {
		int lastIndex = productionFilePath.lastIndexOf("\\");
		if (lastIndex == -1)
			return "";
		return productionFilePath.substring(lastIndex + 1, productionFilePath.length());
	}

	public String getRelativeTestFilePath() {
		String[] splitString = testFilePath.split("\\\\");
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < 5; i++) {
			stringBuilder.append(splitString[i] + "\\");
		}
		return testFilePath.substring(stringBuilder.toString().length()).replace("\\", "/");
	}

	public String getRelativeProductionFilePath() {
		if (!StringUtils.isEmpty(productionFilePath)) {
			String[] splitString = productionFilePath.split("\\\\");
			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < 5; i++) {
				stringBuilder.append(splitString[i] + "\\");
			}
			return productionFilePath.substring(stringBuilder.toString().length()).replace("\\", "/");
		} else {
			return "";

		}
	}
}