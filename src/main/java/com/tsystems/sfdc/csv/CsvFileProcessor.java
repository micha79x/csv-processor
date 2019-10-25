package com.tsystems.sfdc.csv;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class CsvFileProcessor implements CsvProcessor {
	
	private static final Logger LOG = LoggerFactory.getLogger(CsvFileProcessor.class);
	
	@Autowired
	private CsvConfig csvConfig;
	
	private CSVParser csvParser;
	
	private CSVPrinter csvErrorFile;
	
	public void processFile() {
		try {
			String inputFileName = csvConfig.getInputFile();
			Path inputFile = Paths.get(inputFileName);
			this.csvParser = CSVParser.parse(
					inputFile, 
					Charset.forName(csvConfig.getInputFileEncoding()), 
					CSVFormat.RFC4180.withHeader().withDelimiter(csvConfig.getInputFileDelimter()));
			
			this.prepareErrorFile();
			this.beforeProcessRecords();
			
			for (CSVRecord record : csvParser) {
				try {
					this.processRecord(record);
				} catch (Exception e) {
					this.handleRecordFailed(record, e);
				}
			}
			
			this.postProcessRecords();
		} catch (Exception e) {
			LOG.error("Error while processing input file {0}: {1}", csvConfig.getInputFile(), e.getMessage());
			throw new RuntimeException(e);
		} finally {
			try { 
				csvErrorFile.close();
			} catch (IOException e) {
				LOG.error(e.getMessage());
			}
		}
	}
	
	private void prepareErrorFile() throws Exception {
		Path outputFile = Paths.get(csvConfig.getOutputFileName("_error"));
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFile.toFile()), "UTF-8");
		csvErrorFile = new CSVPrinter(new BufferedWriter(outputStreamWriter), CSVFormat.RFC4180.withQuoteMode(csvConfig.getOutputQuoteMode()));
		for (String header : csvParser.getHeaderNames()) {
			csvErrorFile.print(header);
		}
		csvErrorFile.print("Error");
		csvErrorFile.println();
	}

	protected final CSVParser getCsvParser() {
		return this.csvParser;
	}
	
	protected final CSVPrinter getErrorPrinter() {
		return csvErrorFile;
	}

	protected abstract void beforeProcessRecords() throws Exception; 
	
	protected abstract void processRecord(CSVRecord record) throws Exception;
	
	protected void handleRecordFailed(CSVRecord record, Exception e) throws Exception {
		for (String val : record) {
			csvErrorFile.print(val);
		}
		csvErrorFile.print(e.getClass().getName() + ": " + e.getMessage());
		csvErrorFile.println();
	}
	
	protected abstract void postProcessRecords() throws Exception; 

}
