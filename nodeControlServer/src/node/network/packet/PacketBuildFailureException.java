package node.network.packet;

/**
  * @FileName : PacketBuildFailureException.java
  * @Project : Project2018Servers
  * @Date : 2018. 9. 25. 
  * @작성자 : dja12123
  * @변경이력 :
  * @프로그램 설명 : 패킷 빌드 실패 예외
  */
class PacketBuildFailureException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PacketBuildFailureException(String msg)
	{
		super(msg);
	}
}