package com.tsystems.sfdc.csv;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("CsvColumnRemove")
public class CsvColumnRemove extends CsvFileProcessor {

	@Autowired
	private CsvConfig csvConfig;

	private CSVPrinter csvPrinter;

	private List<String> resultHeaderNames;

	private Path outputFile;

	@Override
	protected void beforeProcessRecords() throws Exception {
		List<String> columnsToBeRemoved = csvConfig.getColumns();
		outputFile = Paths.get(getCurrentOutputFileName("_" + String.join("_", columnsToBeRemoved) + "_ColumnRemoved"));
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFile.toFile()), "UTF-8");
		csvPrinter = new CSVPrinter(new BufferedWriter(outputStreamWriter), CSVFormat.RFC4180.withQuoteMode(csvConfig.getOutputQuoteMode()));

		resultHeaderNames = new ArrayList<>(getCsvParser().getHeaderNames());
		resultHeaderNames.removeAll(columnsToBeRemoved);
		csvPrinter.printRecord(resultHeaderNames);
	}

	@Override
	protected void processRecord(CSVRecord record) throws Exception {
		for (String headerName : resultHeaderNames) {
			csvPrinter.print(record.get(headerName));	
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
