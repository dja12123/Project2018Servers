package node.security;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.CloseCode;
import org.nanohttpd.protocols.websockets.NanoWSD;
import org.nanohttpd.protocols.websockets.WebSocket;
import org.nanohttpd.protocols.websockets.WebSocketFrame;

public class test 
{
    public static void main(String[] args) throws IOException 
    {
        final boolean debugMode = args.length >= 2 && "-d".equals(args[1].toLowerCase());
        NanoWSD ws = new DebugWebSocketServer(args.length > 0 ? Integer.parseInt(args[0]) : 9090, debugMode);
        ws.start();
        System.out.println("Server started, hit Enter to stop.\n");
        try 
        {
            System.in.read();
        }
        catch (IOException ignored) 
        {
        }
        ws.stop();
        System.out.println("Server stopped.\n");
    }
}


class DebugWebSocketServer extends NanoWSD {

    /**
     * logger to log to.
     */
    private static final Logger LOG = Logger.getLogger(DebugWebSocketServer.class.getName());

    private final boolean debug;

    public DebugWebSocketServer(int port, boolean debug) {
        super(port);
        this.debug = debug;
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        return new DebugWebSocket(this, handshake);
    }

    private static class DebugWebSocket extends WebSocket {

        private final DebugWebSocketServer server;

        public DebugWebSocket(DebugWebSocketServer server, IHTTPSession handshakeRequest) {
            super(handshakeRequest);
            this.server = server;
        }

        @Override
        protected void onOpen() {
        }

        @Override
        protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {
            if (server.debug) {
                System.out.println("C [" + (initiatedByRemote ? "Remote" : "Self") + "] " + (code != null ? code : "UnknownCloseCode[" + code + "]")
                        + (reason != null && !reason.isEmpty() ? ": " + reason : ""));
            }
        }

        @Override
        protected void onMessage(WebSocketFrame message) {
            try {
                message.setUnmasked();
                sendFrame(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPong(WebSocketFrame pong) {
            if (server.debug) {
                System.out.println("P " + pong);
            }
        }

        @Override
        protected void onException(IOException exception) {
            DebugWebSocketServer.LOG.log(Level.SEVERE, "exception occured", exception);
        }

        @Override
        protected void debugFrameReceived(WebSocketFrame frame) {
            if (server.debug) {
                System.out.println("R " + frame);
            }
        }

        @Override
        protected void debugFrameSent(WebSocketFrame frame) {
            if (server.debug) {
                System.out.println("S " + frame);
            }
        }
    }
}