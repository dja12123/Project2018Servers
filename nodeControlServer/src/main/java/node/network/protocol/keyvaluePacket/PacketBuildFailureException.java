package node.network.protocol.keyvaluePacket;

/**
  * @FileName : PacketBuildFailureException.java
  * @Project : Project2018Servers
  * @Date : 2018. 9. 25. 
  * @작성자 : dja12123
  * @변경이력 :
  * @프로그램 설명 : 패킷 빌드 실패 예외
  */
public class PacketBuildFailureException extends Exception
{
	public PacketBuildFailureException(String msg)
	{
		super(msg);
	}
}