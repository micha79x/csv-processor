package com.tsystems.sfdc.csv;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

public abstract class CsvFileProcessor implements CsvProcessor {
	
	private static final Logger LOG = LoggerFactory.getLogger(CsvFileProcessor.class);
	
	@Autowired
	private CsvConfig csvConfig;
	
	private CSVParser csvParser;
	
	private CSVPrinter csvErrorFile;
	
	private boolean recordErrorOccured = false;

	private Path errorFile;

	private Path currentInputFile;
	
	public Path processFile() {
		try {
			String inputFileName = csvConfig.getInputFile().getFileName();
			Path inputFile = Paths.get(inputFileName);
			if (Files.isDirectory(inputFile)) {
				// We need to collect input files in a list before processing each file
				// if we would process within the next for loop directly, it would also include 
				// NEW CSV result files created during the processing itself 
				List<Path> inputFiles = new ArrayList<>();
				DirectoryStream<Path> stream = Files.newDirectoryStream(inputFile, "*.csv" );
				for (Path singleFile : stream) {
				    inputFiles.add(singleFile);
				}
				stream.close();

				Path lastResult = null;
				for (Path singleFile : inputFiles) {
					lastResult = processSingleFile(singleFile);
				}
				return lastResult;
			} else {
				return processSingleFile(inputFile);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
			return null;
		}
	}

	private Path processSingleFile(Path inputFile) {
		this.currentInputFile = inputFile;
		long currentRecordNumber = -1;
		try {
			LOG.info("Processing file {}.", inputFile);
			this.csvParser = CSVParser.parse(
					inputFile, 
					Charset.forName(csvConfig.getInputFile().getEncoding()), 
					CSVFormat.RFC4180.withHeader().withDelimiter(csvConfig.getInputFile().getDelimter()));
			
			this.prepareErrorFile();
			LOG.info("Preparing processing records.");
			this.beforeProcessRecords();
			
			LOG.info("Started processing all records of input file {}.", inputFile);
			for (CSVRecord record : csvParser) {
				currentRecordNumber = csvParser.getRecordNumber();
				try {
					this.processRecord(record);
				} catch (Exception e) {
					recordErrorOccured = true;
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
				// if no error occurred we can delete the error file
				if (!recordErrorOccured && errorFile != null && Files.exists(errorFile)) {
					Files.delete(errorFile);
				} else {
					LOG.error("Errors occured. Check {} for details!", errorFile.toString());
				}
			} catch (IOException e) {
				LOG.error(e.getMessage());
			}
		}
	}
	
	private void prepareErrorFile() throws Exception {
		errorFile = Paths.get(this.getCurrentOutputFileName("_error"));
		LOG.info("Creating error file {}.", errorFile);
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(errorFile.toFile()), "UTF-8");
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

	protected Path getCurrentInputFile() {
		return currentInputFile;
	} 

	protected String getCurrentOutputFileName(String fileNameAddition) {
		return getCurrentOutputFileName(null, fileNameAddition);
	}
	
	protected String getCurrentOutputFileName(String subdirectory, String fileNameAddition) {
		Path inputFilePath = currentInputFile;
		Path baseDir = inputFilePath.getParent();
		Path fileName = inputFilePath.getFileName(); 

		String targetFileName = StringUtils.stripFilenameExtension(fileName.toString()) + fileNameAddition.replaceAll("[\\\\/:*?\"<>|]", "").substring(0, Math.min(fileNameAddition.length(), 40)) + "."
				+ StringUtils.getFilenameExtension(fileName.toString());
		
		if (Strings.isNotBlank(subdirectory)) {
			return Paths.get(baseDir.toString(), subdirectory, targetFileName).toString();
		} else {
			return Paths.get(baseDir.toString(), targetFileName).toString();
		}
	}


}
