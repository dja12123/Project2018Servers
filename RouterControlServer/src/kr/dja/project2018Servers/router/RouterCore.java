package kr.dja.project2018Servers.router;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dhcp4java.DHCPPacket;
import org.dhcp4java.DHCPCoreServer;
import org.dhcp4java.DHCPServerInitException;
import org.dhcp4java.DHCPServlet;

/**
 * A simple DHCP sniffer based on DHCP servlets.
 *
 * @author Stephan Hadinger
 * @version 1.00
 */
public class RouterCore extends DHCPServlet {

    private static final Logger logger = Logger.getLogger("org.dhcp4java.examples.dhcpsnifferservlet");
    
    /**
     * Print received packet as INFO log, and do not respnd.
     * 
     * @see org.dhcp4java.DHCPServlet#service(org.dhcp4java.DHCPPacket)
     */
    
    @Override
    public DatagramPacket serviceDatagram(DatagramPacket requestDatagram) {
    	System.out.println("req");
    	return super.serviceDatagram(requestDatagram);
    }
    @Override
    public DHCPPacket service(DHCPPacket request) {
        logger.info(request.toString());
        System.out.println(request.toString());
        return null;
    }

    /**
     * Launcher for the server.
     * 
     * <p>No args.
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
        	Properties prop = new Properties();
        	prop.setProperty(DHCPCoreServer.SERVER_ADDRESS, "192.168.0.1:67");
            DHCPCoreServer server = DHCPCoreServer.initServer(new RouterCore(), null);
            new Thread(server).start();
        } catch (DHCPServerInitException e) {
            logger.log(Level.SEVERE, "Server init", e);
        }
    }
}
