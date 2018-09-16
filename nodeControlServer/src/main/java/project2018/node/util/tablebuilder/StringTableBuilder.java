package project2018.node.util.tablebuilder;

import java.util.ArrayList;

public class StringTableBuilder
{
	private static final char HEAD_ROW_SEP = '-';
	private static final char HEAD_COL_SEP = '|';
	private static final char DATA_COL_SEP = ':';
	private static final char DATA_INTC_SEP = '+';
	
	private ArrayList<Integer> rowSpace;
	
	private String nullStr;
	private Row headRow;
	private ArrayList<Row> rowList;
	
	public StringTableBuilder(String head, String nullStr)
	{
		this.rowSpace = new ArrayList<Integer>();
		this.nullStr = nullStr;
		this.headRow = new Row(head, this.nullStr, this.rowSpace);
		this.rowList = new ArrayList<Row>();
	}
	
	public Row addRow(String rowHead)
	{
		Row addRow = new Row(rowHead, this.nullStr, this.rowSpace);
		this.rowList.add(addRow);
		return addRow;
	}
	
	public void addHeadData(String data)
	{
		this.headRow.put(data);
	}
	
	public String build()
	{
		int minRowSize = this.headRow.size();
		for(Row row : this.rowList)
			if(minRowSize > row.size()) minRowSize = row.size();
		
		for(Row row : this.rowList)
			for(int i = row.size(); i < this.rowSpace.size() - 1; ++i)
				row.put(this.nullStr);
		
		StringBuffer buffer = new StringBuffer();
		
		this.printRow(buffer, this.headRow, minRowSize);
		
		for(int memberSpace : this.rowSpace)
		{
			for(int j = 0; j < memberSpace; ++j)
			{
				buffer.append(HEAD_ROW_SEP);
			}
			buffer.append(HEAD_ROW_SEP);
			buffer.append(DATA_INTC_SEP);
			buffer.append(HEAD_ROW_SEP);
		}
		buffer.deleteCharAt(buffer.length() - 1);
		buffer.deleteCharAt(buffer.length() - 1);
		buffer.append('\n');
		
		for(Row row : this.rowList)
			this.printRow(buffer, row, minRowSize);
		
		buffer.deleteCharAt(buffer.length() - 1);
		return buffer.toString();
	}
	
	private void printRow(StringBuffer buffer, Row row, int minRowSize)
	{
		buffer.append(String.format("%1$-"+this.rowSpace.get(0)+"s ", row.head()));
		buffer.append(HEAD_COL_SEP);
		int rowSize = this.rowSpace.size() - 1;

		for(int rowDataIndex = 0; rowDataIndex < rowSize; ++rowDataIndex)
		{
			String fillStr;
			int size = this.rowSpace.get(rowDataIndex + 1);
			if(row.size() > rowDataIndex && row.getData(rowDataIndex) != null) fillStr = row.getData(rowDataIndex);
			else fillStr = this.nullStr;
			
			buffer.append(String.format(" %1$-"+size+"s ", fillStr));
			buffer.append(DATA_COL_SEP);
		}
		buffer.deleteCharAt(buffer.length() - 1);
		buffer.append('\n');
	}
}