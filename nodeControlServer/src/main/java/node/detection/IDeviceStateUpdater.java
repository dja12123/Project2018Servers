package node.detection;

import java.net.InetAddress;
import java.util.UUID;

public interface IDeviceStateUpdater
{
	public void updateDevice(UUID uuid, InetAddress inetAddr, boolean isDHCP);
}
