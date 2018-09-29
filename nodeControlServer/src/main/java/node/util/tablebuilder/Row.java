package node.util.tablebuilder;

import java.util.ArrayList;

public class Row
{	
	private String head;
	private String nullStr;
	
	private ArrayList<String> dataList;
	private ArrayList<Integer> rowSpace;
	
	Row(String head, String nullStr, ArrayList<Integer> rowSpace)
	{
		this.head = head;
		this.nullStr = nullStr;
		this.dataList = new ArrayList<String>();
		this.rowSpace = rowSpace;
		
		this.spaceCalc(head);
	}
	
	public Row put(String data)
	{
		if(data == null) data = this.nullStr;
		
		this.dataList.add(data);
		this.spaceCalc(data);
		
		return this;
	}
	
	String head()
	{
		return this.head;
	}
	
	int size()
	{
		return this.dataList.size();
	}
	
	String getData(int index)
	{
		return this.dataList.get(index);
	}
	
	private void spaceCalc(String str)
	{
		while(this.rowSpace.size() < this.dataList.size() + 1)
			this.rowSpace.add(0);
		// rowSpace의 0번 공간은 테이블의 헤드를 위한 공간.
		
		if(this.rowSpace.get(this.dataList.size()) < str.length())
		{
			this.rowSpace.set(this.dataList.size(), str.length());
		}
	}
}