package com.tsystems.sfdc.csv;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("CsvIdReplacer")
public class CsvIdReplacer extends CsvFileProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(CsvIdReplacer.class);
	
	@Autowired
	private CsvConfig csvConfig;
	
	private Map<String, String> replacementMap = new HashMap<>();

	private CSVPrinter csvPrinter;

	private Path outputFile;
	
	@Override
	protected void beforeProcessRecords() throws Exception {
		// Prepare output file with replaced values
		prepareOutputFile();
		
		// Init map of replacement value
		collectMappingValues();
	}

	private void prepareOutputFile() throws UnsupportedEncodingException, FileNotFoundException, IOException {
		outputFile = Paths.get(getCurrentOutputFileName("_replaced"));
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFile.toFile()), "UTF-8");
		csvPrinter = new CSVPrinter(new BufferedWriter(outputStreamWriter), CSVFormat.RFC4180.withQuoteMode(csvConfig.getOutputQuoteMode()));
		csvPrinter.printRecord(getCsvParser().getHeaderNames());
	}

	private void collectMappingValues() throws IOException {
		for (CsvConfigMappingInput replacement : csvConfig.getReplacements()) {
			CsvConfigFile file = replacement.getInputFile();
			LOG.info("Reading file {} to collect replacement values.", file.getFileName());
			Path replacementFile = Paths.get(file.getFileName());
			CSVParser csvParser = CSVParser.parse(
					replacementFile, 
					Charset.forName(file.getEncoding()), 
					CSVFormat.RFC4180.withHeader().withDelimiter(file.getDelimter()));
			for (CSVRecord csvRecord : csvParser) {
				String originalValue = csvRecord.get(replacement.getKeyCsvColumn());
				String replacedValue = csvRecord.get(replacement.getReplacementCsvColumn());
				String currentValue = replacementMap.putIfAbsent(originalValue, replacedValue);
				if (currentValue != null) {
					LOG.warn("Duplicate replacement found for value {}: Wanted to map {} but already had {}", originalValue, replacedValue, currentValue);
				}
			}
			csvParser.close();
			LOG.info("Reading file {} finished.", file.getFileName(), "", "");
		}
		
	}

	@Override
	protected void processRecord(CSVRecord record) throws Exception {
		Map<String, String> replacedValues = new HashMap<>();
		
		for (String columnToBeReplaced : csvConfig.getColumnsToReplace()) {
			String originalValue = record.get(columnToBeReplaced);
			String replacedValue = Optional.ofNullable(replacementMap.get(originalValue))
					.orElseThrow(() -> new IllegalArgumentException("No replacement found for value '" + originalValue + "' of column '" + columnToBeReplaced + "'."));
			replacedValues.put(columnToBeReplaced, replacedValue);
		}
		

		for (String headerName : getCsvParser().getHeaderNames()) {
			if (replacedValues.containsKey(headerName)) {
				csvPrinter.print(replacedValues.get(headerName));	
			} else {
				csvPrinter.print(record.get(headerName));	
			}
		}
		csvPrinter.println();
	}

	@Override
	protected void postProcessRecords() throws Exception {
		csvPrinter.close();
	}

	@Override
	protected Path getOutputFile() {
		return outputFile;
	}

}
