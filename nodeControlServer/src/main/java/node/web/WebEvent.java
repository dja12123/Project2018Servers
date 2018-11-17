package node.web;

//channel 이용

public class WebEvent {
	public final String key;
	public final String value;
	
	WebEvent(String key, String value) {
		this.key = key;
		this.value = value;
	}	
}
