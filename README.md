# Getting Started

Use csv-processor tool via command line to manipulate CSV files in various ways.

The tool needs to be called with mandatory parameter `--processor=...`, in order to define the actual processing step. Multiple processors can be defined by providing a comma separated list. 
If done so, the output file created by the previous processor task is piped as input for the subsequent processor.

The list of processors supported is:

* **CsvColumnRemove**: Remove one or more columns from CSV file.
* **CsvIdReplacer**: Replace Ids (or even other text) with mapped input.
* **CsvAttachementExtract**: Extract base24 content into local files and add reference to local file to a new CSV column

Each processor needs defines it's own set of necessary input parameters.

The output files created will always be encoded in UTF-8 and with comma as delimiter, regardless of the input file format. 

## Common parameters

* `csv.input-file.file-name` - Path to a single input file or a whole directory. If a directory is specified as input all CSV files existing in this directory will be processed.
* `csv.input-file.encoding` - Character set the input file is encoded with - If not provided defaults to `UTF-8`
* `csv.input-file.delimter` - Character used as column delimiter - If not provided defaults to `,`

**Note:** If `csv.input-file.file-name` is pointing to a directory piping each single output file to the next processor is currently **not supported**. 

## CsvColumnRemove

* `processor=CsvColumnRemove`
* `csv.input-file.*` - See "Common parameters"
* `csv.columns` - Comma-separated list of column names to be removed from input file.

The process will create a new output file named after the input file appended with "ColumnA__ColumnRemoved".

## CsvIdReplacer

* `processor=CsvIdReplace`
* `csv.input-file.*` - See "Common parameters"
* `csv.columns-to-replace` - Comma-separated list of column names to be replaced.
* `csv.replacements[0..n].input-file.file-name` - Path to file used for mapping.
* `csv.replacements[0..n].input-file.encoding` - Character set the input file is encoded with - If not provided defaults to `UTF-8`
* `csv.replacements[0..n].input-file.delimter` - Character used as column delimiter - If not provided defaults to `,`
* `csv.replacements[0..n].key-csv-column` - CSV column used as lookup column, basically must match values available in column(s) defined in `csv.columns-to-replace`
* `csv.replacements[0..n].replacement-csv-column` - CSV column used as fill column and is thus replacing value matched in `csv.replacements[0..n].key-csv-column` 

*NOTE:* Multiple input files can be provided as mapping input!

The process will create a new output file named after the input file appended with "_replaced".

## CsvAttachmentExtract

* `processor=CsvIdReplace`
* `csv.input-file.*` - See "Common parameters"
* `csv.base64-column` - CSV column name containing the base64 data - If not provided defaults to name `Body` 

The process will create a new output file named after the input file appended with "_BodyRemoved". 
In addition it will add a new sub-directory relative to the input file, named after `./binaries/$UNIX_TIMESTAMP$/` containing all the distinct files extracted from the base64 content in the CSV.
Also, the resulting CSV file will have the base64-column removed and a new column `PathOnClient` added, containing the absolute file path pointing to the extracted file.

## CsvSplit

* `csv.split.column` - The column of which the single CSV record's value is used to determine the split bucket
* `csv.split.expression` - The expression to be applied to the value extracted from column `csv.split.column`. If not provided the value itself will be used to create split "buckets". 
* `csv.split.to-subfolder` - If set to true, the result files will be written to a sub folder named "splits"

 The process will create output files named after the input file appended by each bucket derived via `csv.split.expression`. 
 In order to split by expression, the input property `csv.split.expression` will be parsed by the Spring Expression Language evaluator. One can refer to `value` in order to use the extracted value from the CSV record.
 
 Example:
 
 * `csv.split.expression=T(java.lang.Integer).parseInt(value) < 1000000 ? "a" : "b"` would create two split buckets "a" and "b" where the CSV file "a" would contain all records with a value lower than 1,000,000 and CSV file "b" with records with a value greater or equal to 1,000,000.      