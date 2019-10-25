package com.tsystems.sfdc.csv;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("CsvIdReplacer")
public class CsvIdReplacer extends CsvFileProcessor {

	@Autowired
	private CsvConfig csvConfig;
	
	private Map<String, String> replacementMap = new HashMap<>();

	private CSVPrinter csvPrinter;
	
	@Override
	protected void beforeProcessRecords() throws Exception {
		Path outputFile = Paths.get(csvConfig.getOutputFileName("_replaced"));
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFile.toFile()), "UTF-8");
		csvPrinter = new CSVPrinter(new BufferedWriter(outputStreamWriter), CSVFormat.RFC4180.withQuoteMode(csvConfig.getOutputQuoteMode()));
		csvPrinter.printRecord(getCsvParser().getHeaderNames());
		
		//TODO
		replacementMap.put("02s1i0000060dTAAAY", "Hollebolle");
	}

	@Override
	protected void processRecord(CSVRecord record) throws Exception {
		List<String> replacementColumns = Arrays.asList("ParentId");
		Map<String, String> replacedValues = new HashMap<>();
		
		for (String columnToBeReplaced : replacementColumns) {
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

}
