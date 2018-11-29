package node.gpio.lcd;

public class LCDStringObject
{
	public final int x,y;
	public final String string;
	
	public LCDStringObject(int x, int y, String str)
	{
		this.x = x;
		this.y = y;
		this.string = str;
	}
}
