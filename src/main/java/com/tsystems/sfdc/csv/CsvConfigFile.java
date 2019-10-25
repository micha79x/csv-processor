package com.tsystems.sfdc.csv;

import org.apache.commons.csv.QuoteMode;

public class CsvConfigFile {

	private String fileName;

	private String encoding = "UTF-8";

	private char delimter = ',';

	private QuoteMode quoteMode;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public char getDelimter() {
		return delimter;
	}

	public void setDelimter(char delimter) {
		this.delimter = delimter;
	}

	public QuoteMode getQuoteMode() {
		return quoteMode;
	}

	public void setQuoteMode(QuoteMode quoteMode) {
		this.quoteMode = quoteMode;
	}

}
