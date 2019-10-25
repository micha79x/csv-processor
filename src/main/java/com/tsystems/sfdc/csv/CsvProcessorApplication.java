package com.tsystems.sfdc.csv;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class CsvProcessorApplication implements ApplicationRunner {

	private static final Logger LOG = LoggerFactory.getLogger(CsvProcessorApplication.class);

	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private CsvConfig csvConfig;

	public static void main(String[] args) {
		LOG.info("Starting application ...");
		SpringApplication.run(CsvProcessorApplication.class, args);
		LOG.info("Application finished.");
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		LOG.info("args: " + Arrays.stream(args.getSourceArgs()).collect(Collectors.joining("|")));
		
		List<String> optionValues = args.getOptionValues("processor");
		if (optionValues == null) {
			throw new IllegalArgumentException("Please provide input parameter '--processor=...'");
		}
		
		Path outputFile = null;
		for (String processor : optionValues) {
			CsvProcessor csvProcessor = (CsvProcessor) applicationContext.getBean(processor);
			if (outputFile != null) {
				CsvConfigFile inputForNextProcessor = new CsvConfigFile();
				inputForNextProcessor.setFileName(outputFile.toString());
				csvConfig.setInputFile(inputForNextProcessor);
			}
			outputFile = csvProcessor.processFile();
		}
		
	}

}
