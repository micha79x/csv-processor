package com.tsystems.sfdc.csv;

public class CsvConfigMappingInput {

	private CsvConfigFile inputFile;
	private String keyCsvColumn;
	private String replacementCsvColumn;

	public CsvConfigFile getInputFile() {
		return inputFile;
	}

	public void setInputFile(CsvConfigFile inputFile) {
		this.inputFile = inputFile;
	}

	public String getKeyCsvColumn() {
		return keyCsvColumn;
	}

	public void setKeyCsvColumn(String keyCsvColumn) {
		this.keyCsvColumn = keyCsvColumn;
	}

	public String getReplacementCsvColumn() {
		return replacementCsvColumn;
	}

	public void setReplacementCsvColumn(String replacementCsvColumn) {
		this.replacementCsvColumn = replacementCsvColumn;
	}

}
