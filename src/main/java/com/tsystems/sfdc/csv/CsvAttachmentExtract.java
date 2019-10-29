package com.tsystems.sfdc.csv;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("CsvAttachmentExtract")
public class CsvAttachmentExtract extends CsvFileProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(CsvAttachmentExtract.class);

	@Autowired
	private CsvConfig csvConfig;

	private CSVPrinter csvPrinter;

	private Path filesOutputPath;

	private List<String> resultHeaderNames;

	private Path outputFile;

	@Override
	protected void beforeProcessRecords() throws Exception {
		outputFile = Paths.get(csvConfig.getOutputFileName("_BodyRemoved"));
		filesOutputPath = Paths.get(outputFile.getParent().toString(), "binaries",
				String.valueOf(System.currentTimeMillis()));
		Files.createDirectories(filesOutputPath);

		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFile.toFile()), "UTF-8");
		csvPrinter = new CSVPrinter(new BufferedWriter(outputStreamWriter), CSVFormat.RFC4180.withQuoteMode(csvConfig.getOutputQuoteMode()));

		resultHeaderNames = new ArrayList<>(getCsvParser().getHeaderNames());
		resultHeaderNames.remove("Body");
		resultHeaderNames.add("PathOnClient");
		csvPrinter.printRecord(resultHeaderNames);
	}

	@Override
	protected void processRecord(CSVRecord record) throws Exception {
		Path binaryFile = extractBodyToFile(record, filesOutputPath);
		
		for (String headerName : resultHeaderNames) {
			if ("PathOnClient".equalsIgnoreCase(headerName)) {
				csvPrinter.print(binaryFile.toString());
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

	private Path extractBodyToFile(CSVRecord record, Path outputPath) throws IOException {
		byte[] fileRaw = Base64.getDecoder().decode(record.get(csvConfig.getBase64Column()).getBytes(StandardCharsets.UTF_8));
		String fullFileName = record.get("Name").replaceAll("[\\\\/:*?\"<>|]", "");
		String nameWithoutExt = FilenameUtils.getBaseName(fullFileName);
		String extension = FilenameUtils.getExtension(fullFileName);
		Path destinationFile = Paths.get(outputPath.toString(), record.get("Id") + "_" + fullFileName);
		int counter = 1;
		while (Files.exists(destinationFile)) {
			destinationFile = Paths.get(outputPath.toString(),
					record.get("Id") + "_" + nameWithoutExt + "_" + counter + "." + extension);
			counter++;
		}
		LOG.debug("Extracting file {}.", destinationFile);
		Files.write(destinationFile, fileRaw);
		return destinationFile;
	}

	@Override
	protected Path getOutputFile() {
		return outputFile;
	}

}
