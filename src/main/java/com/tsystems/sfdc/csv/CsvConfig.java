package com.tsystems.sfdc.csv;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.QuoteMode;
import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@ConfigurationProperties("csv")
public class CsvConfig {

	private CsvConfigFile inputFile;

	private QuoteMode outputQuoteMode = QuoteMode.ALL;

	private List<CsvConfigMappingInput> replacements = new ArrayList<>();

	private List<String> columnsToReplace = new ArrayList<>();

	private List<String> columns = new ArrayList<>();

	private String filter;

	private String base64Column = "Body";

	private CsvConfigSplit split;

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

	public List<String> getColumnsToReplace() {
		return columnsToReplace;
	}

	public void setColumnsToReplace(List<String> columnsToReplace) {
		this.columnsToReplace = columnsToReplace;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getBase64Column() {
		return this.base64Column;
	}

	public void setBase64Column(String base64Column) {
		this.base64Column = base64Column;
	}

	public CsvConfigSplit getSplit() {
		return split;
	}

	public void setSplit(CsvConfigSplit split) {
		this.split = split;
	}

}
