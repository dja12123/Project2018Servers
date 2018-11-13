package node;

public interface IServiceModule
{
	boolean startModule();	//모듈이 진행되기전에 필요한 파일이나 리소스를 불와서 객체생성하거나 쓰레드를 실행시키는 메서드
	void stopModule();		//스타트모듈에서 진행된 스레드나 리소스를 닫는 메서드
}
