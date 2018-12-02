package node.gpio.lcd;

public class LCDObject
{
	public final int x, y, width, height;
	public final boolean[][] shape;
	
	public LCDObject(int x, int y, int width, int height, boolean[][] shape)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.shape = shape;
	}
}
