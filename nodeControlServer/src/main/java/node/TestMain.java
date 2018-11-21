package node;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.network.Ip4;

import node.fileIO.FileHandler;

public class TestMain
{
	public static void main(String[] args)
	{
		File rawSocketLib = FileHandler.getExtResourceFile("rawsocket");
		StringBuffer libPathBuffer = new StringBuffer();
		libPathBuffer.append(rawSocketLib.toString());
		libPathBuffer.append(":");
		libPathBuffer.append(System.getProperty("java.library.path"));
		
		System.setProperty("java.library.path", libPathBuffer.toString());
		Field sysPathsField = null;
		try
		{
			sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
			sysPathsField.setAccessible(true);
			sysPathsField.set(null, null);
		}
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1)
		{
			// TODO Auto-generated catch blsock
			System.out.print("링크 실패");
			return;
		}
		System.loadLibrary("rocksaw");
		System.out.println("링크 성공");
		
		
		
		List<PcapIf> alldevs = new ArrayList<PcapIf>(); // Will be filled with NICs
		StringBuilder errbuf = new StringBuilder(); // For any error msgs
		int r = Pcap.findAllDevs(alldevs, errbuf);
		if (r != Pcap.OK || alldevs.isEmpty())
		{
			System.err.printf("Can't read list of devices, error is %s", errbuf.toString());
			return;
		}
		System.out.println("Network devices found:");
		int i = 0;
		for (PcapIf device : alldevs)
		{
			String description = (device.getDescription() != null) ? device.getDescription()
					: "No description available";
			System.out.printf("#%d: %s [%s]\n", i++, device.getName(), description);
		}
		PcapIf device = alldevs.get(0); // Get first device in list
		System.out.printf("\nChoosing '%s' on your behalf:\n",
				(device.getDescription() != null) ? device.getDescription() : device.getName());
		int snaplen = 64 * 1024; // Capture all packets, no trucation
		int flags = Pcap.MODE_PROMISCUOUS; // capture all packets
		int timeout = 10 * 1000; // 10 seconds in millis
		Pcap pcap = Pcap.openLive(device.getName(), snaplen, flags, timeout, errbuf);
		if (pcap == null)
		{
			System.err.printf("Error while opening device for capture: " + errbuf.toString());
			return;
		}
		PcapPacketHandler<String> jpacketHandler = new PcapPacketHandler<String>()
		{
			public void nextPacket(PcapPacket packet, String user)
			{
				byte[] data = packet.getByteArray(0, packet.size()); // the package data
				byte[] sIP = new byte[4];
				byte[] dIP = new byte[4];
				Ip4 ip = new Ip4();
				if (packet.hasHeader(ip) == false)
				{
					return; // Not IP packet
				}
				ip.source(sIP);
				ip.destination(dIP);
				/* Use jNetPcap format utilities */
				String sourceIP = org.jnetpcap.packet.format.FormatUtils.ip(sIP);
				String destinationIP = org.jnetpcap.packet.format.FormatUtils.ip(dIP);

				System.out.println("srcIP=" + sourceIP + " dstIP=" + destinationIP + " caplen="
						+ packet.getCaptureHeader().caplen());
			}
		};
		// capture first 10 packages
		pcap.loop(10, jpacketHandler, "jNetPcap");
		pcap.close();
	}
}