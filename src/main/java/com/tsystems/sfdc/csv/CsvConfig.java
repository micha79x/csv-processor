package com.tsystems.sfdc.csv;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.QuoteMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@ConfigurationProperties("csv")
public class CsvConfig {

	private CsvConfigFile inputFile;

	private QuoteMode outputQuoteMode;

	private List<CsvConfigMappingInput> replacements = new ArrayList<>();
	
	private List<String> columns = new ArrayList<>();

	public List<CsvConfigMappingInput> getReplacements() {
		return replacements;
	}

	public void setReplacements(List<CsvConfigMappingInput> replacements) {
		this.replacements = replacements;
	}

	public CsvConfigFile getInputFile() {
		return inputFile;
	}

	public void setInputFile(CsvConfigFile inputFile) {
		this.inputFile = inputFile;
	}

	public String getOutputFileName(String fileNameAddition) {
		return StringUtils.stripFilenameExtension(inputFile.getFileName()) + fileNameAddition + "."
				+ StringUtils.getFilenameExtension(inputFile.getFileName());
	}

	public QuoteMode getOutputQuoteMode() {
		return outputQuoteMode;
	}

	public void setOutputQuoteMode(QuoteMode outputQuoteMode) {
		this.outputQuoteMode = outputQuoteMode;
	}

	public List<String> getColumns() {
		return columns;
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

}
