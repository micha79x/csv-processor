package com.tsystems.sfdc.csv;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Component("CsvSplit")
public class CsvSplit extends CsvFileProcessor {

	@Autowired
	private CsvConfig csvConfig;

	private Map<String, CSVPrinter> csvPrinters = new HashMap<>();

	private String splitColumn;

	private String splitExpression;

	@Override
	protected void beforeProcessRecords() throws Exception {
		CsvConfigSplit splitConfig = csvConfig.getSplit();
		splitColumn = splitConfig.getColumn();
		splitExpression = splitConfig.getExpression();
	}

	@Override
	protected void processRecord(CSVRecord record) throws Exception {
		String value = record.get(splitColumn);
		
		// TODO expression
		String key;
		if (Strings.isBlank(splitExpression)) {
			key = value;
		} else {
			ExpressionParser parser = new SpelExpressionParser();
			Expression exp = parser.parseExpression(splitExpression);
			EvaluationContext context = new StandardEvaluationContext(new CsvValue(value));
			key = exp.getValue(context, String.class);
		}
		
		CSVPrinter csvPrinterByKey = getCsvPrinter(key);
		for (String headerName : getCsvParser().getHeaderNames()) {
			csvPrinterByKey.print(record.get(headerName));	
		}
		csvPrinterByKey.println();
	}
	
	private CSVPrinter getCsvPrinter(String key) throws Exception {
		CSVPrinter csvPrinter = csvPrinters.get(key);
		if (csvPrinter == null) {
			// If key is a Salesforce ID, it might only differ in lower/upper case differences compared to other keys in the map
			// If the final filename also only differs in lower/upper case characters, this will become an issue in Windows as
			// the file system is case-insensitive and that's why we need to find a filename that is distinct.
			// E.g.: "02s1i0000060dTAAAY" and "02s1i0000060DTAAAY" are distinct IDs only different in character #13 (d" vs. "D")
			// The resulting file would be "02s1i0000060dTAAAY.csv" vs. "02s1i0000060DTAAAY.csv" which is the same file in Windows
			// So in this case we create "02s1i0000060dTAAAY.csv" and "02s1i0000060DTAAAY_2.csv"
			int counter = 1;
			Path outputFile = null;
			do  {
				String keySuffix = key + ((counter > 1) ? "_" + counter : ""); // append "_2", "_3" ...
				String outputFileName;
				if (csvConfig.getSplit().isToSubfolder()) {
					outputFileName = csvConfig.getOutputFileName("splits", "_Split_" + splitColumn + "_" + keySuffix);
				} else {
					outputFileName = csvConfig.getOutputFileName("_Split_" + splitColumn + "_" + keySuffix);
				}
				outputFile = Paths.get(outputFileName);
				counter++;

			} while (Files.exists(outputFile));
			Files.createDirectories(outputFile.getParent());
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFile.toFile()), "UTF-8");
			csvPrinter = new CSVPrinter(new BufferedWriter(outputStreamWriter), CSVFormat.RFC4180.withQuoteMode(csvConfig.getOutputQuoteMode()));
			csvPrinter.printRecord(super.getCsvParser().getHeaderNames());
			csvPrinters.put(key, csvPrinter);
		}
		return csvPrinter;
	}

	@Override
	protected void postProcessRecords() throws Exception {
		for (CSVPrinter csvPrinter : csvPrinters.values()) {
			csvPrinter.close();
		}
	}

	@Override
	protected Path getOutputFile() {
		return null;
	}

	private static class CsvValue {
		
		private String value;
		
		private CsvValue(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
		
	}
}
