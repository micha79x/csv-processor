package com.tsystems.sfdc.csv;

import org.apache.commons.csv.QuoteMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@ConfigurationProperties("csv")
public class CsvConfig {

	private String inputFile;

	private String inputFileEncoding = "UTF-8";
	
	private char inputFileDelimter = ';';
	
	private QuoteMode outputQuoteMode; 
	
	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public String getInputFileEncoding() {
		return inputFileEncoding;
	}

	public void setInputFileEncoding(String inputFileEncoding) {
		this.inputFileEncoding = inputFileEncoding;
	}

	public char getInputFileDelimter() {
		return inputFileDelimter;
	}

	public void setInputFileDelimter(char inputFileDelimter) {
		this.inputFileDelimter = inputFileDelimter;
	}

	public String getOutputFileName(String fileNameAddition) {
		return StringUtils.stripFilenameExtension(inputFile) + fileNameAddition + "." + StringUtils.getFilenameExtension(inputFile);
	}

	public QuoteMode getOutputQuoteMode() {
		return outputQuoteMode;
	}

	public void setOutputQuoteMode(QuoteMode outputQuoteMode) {
		this.outputQuoteMode = outputQuoteMode;
	}

}
