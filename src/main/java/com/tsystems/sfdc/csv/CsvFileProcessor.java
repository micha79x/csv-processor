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
	
	public Path processFile() {
		long currentRecordNumber = -1;
		try {
			String inputFileName = csvConfig.getInputFile().getFileName();
			LOG.info("Processing file {}.", inputFileName);
			Path inputFile = Paths.get(inputFileName);
			this.csvParser = CSVParser.parse(
					inputFile, 
					Charset.forName(csvConfig.getInputFile().getEncoding()), 
					CSVFormat.RFC4180.withHeader().withDelimiter(csvConfig.getInputFile().getDelimter()));
			
			this.prepareErrorFile();
			LOG.info("Preparing processing records.");
			this.beforeProcessRecords();
			
			LOG.info("Starting processing all records of input file {}.", inputFileName);
			for (CSVRecord record : csvParser) {
				currentRecordNumber = csvParser.getRecordNumber();
				try {
					this.processRecord(record);
				} catch (Exception e) {
					this.handleRecordFailed(record, e);
				}
			}
			
			LOG.info("Post processing records.");
			this.postProcessRecords();
			
			return this.getOutputFile();
		} catch (Exception e) {
			LOG.error("Error while processing record number {} of input file {}: {}", currentRecordNumber, csvConfig.getInputFile(), e.getMessage());
			throw new RuntimeException(e);
		} finally {
			try {
				if (csvErrorFile != null) {
					csvErrorFile.close();
				}
			} catch (IOException e) {
				LOG.error(e.getMessage());
			}
		}
	}
	
	private void prepareErrorFile() throws Exception {
		Path outputFile = Paths.get(csvConfig.getOutputFileName("_error"));
		LOG.info("Creating error file {}.", outputFile);
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

	protected abstract Path getOutputFile(); 

}
