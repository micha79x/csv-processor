package com.tsystems.sfdc.csv;

public class CsvConfigSplit {

	private String column;
	private String expression;
	private boolean toSubfolder = true;

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public boolean isToSubfolder() {
		return toSubfolder;
	}

	public void setToSubfolder(boolean toSubfolder) {
		this.toSubfolder = toSubfolder;
	}

}
