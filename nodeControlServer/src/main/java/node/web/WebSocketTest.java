package node.web;

import java.io.IOException;

import org.nanohttpd.protocols.websockets.NanoWSD;

public class WebSocketTest {
    public static void main(String[] args) throws IOException {
        final boolean debugMode = args.length >= 2 && "-d".equals(args[1].toLowerCase());
        NanoWSD ws = new WebSocketHandler(args.length > 0 ? Integer.parseInt(args[0]) : 8080, debugMode);
        ws.start();
        System.out.println("Server started, hit Enter to stop.\n");
        try {
            System.in.read();
        } catch (IOException ignored) {
        }
        ws.stop();
        System.out.println("Server stopped.\n");
    }
}
