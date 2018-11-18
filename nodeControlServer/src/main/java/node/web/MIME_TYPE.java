package node.web;
//
public enum MIME_TYPE
{
	IMAGE_JPEG("image/jpeg"),
	IMAGE_PNG("image/png");

	String typeString;

	MIME_TYPE(String typeString)
	{
		this.typeString = typeString;
	}

	@Override
	public String toString()
	{
		return typeString;
	}
}