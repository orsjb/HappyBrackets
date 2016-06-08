package net.happybrackets.core;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Scanner;

public abstract class Device {

	public static final String myHostname;						//the hostname for this PI (wifi)
	public static final String myIP;
	public static final String myMAC;							//the wlan MAC for this PI (wifi)
	public static final String preferedInterface;

	static {
		String tmpHostname = null;
        String tmpIP = null;
		String tmpMAC = null;
		String tmpPreferedInterface = null;
		try {
			NetworkInterface netInterface;
			System.out.println("Detected OS: " + System.getProperty("os.name"));
			if (System.getProperty("os.name").startsWith("Mac OS")) {
				netInterface = NetworkInterface.getByName("en1");
				//if you can't getInstance the wlan then getInstance the ethernet mac address:
				if(netInterface == null) {
					netInterface = NetworkInterface.getByName("en0");
				}
                tmpHostname = netInterface.getInetAddresses().nextElement().getHostName();
                tmpIP = netInterface.getInetAddresses().nextElement().getHostAddress();
			}
			else if (System.getProperty("os.name").startsWith("Windows")) {
				System.out.println("Interfaces:");
				Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
				String favouriteInterfaceName = null;
				while (interfaces.hasMoreElements()) {
					netInterface = interfaces.nextElement();
					// Windows by default has a lot of extra interfaces,
					//  lets at least try and getInstance a real interface...
					if (isViableNetworkInterface(netInterface)) {
						favouriteInterfaceName = netInterface.getName();
						System.out.println("I like: " + favouriteInterfaceName + ", " + netInterface.getDisplayName());
					}
					else {
						System.out.println("Ignored: " + netInterface.getName() + ", " + netInterface.getDisplayName());
					}
				}
				if (favouriteInterfaceName != null ) {
					netInterface = NetworkInterface.getByName(favouriteInterfaceName);
				}
				else {
					netInterface = NetworkInterface.getByName("wlan0"); // take a stab in the dark...
				}
				System.out.println("Selected interface: " + netInterface.getName() + ", " + netInterface.getDisplayName());
				tmpHostname = netInterface.getInetAddresses().nextElement().getHostName();
				tmpIP = netInterface.getInetAddresses().nextElement().getHostAddress();
			}
			else {
				netInterface = NetworkInterface.getByName("wlan0");
				if (netInterface == null) {
					netInterface = NetworkInterface.getByName("eth0");
				}
                tmpHostname = netInterface.getInetAddresses().nextElement().getHostName();
                tmpIP = netInterface.getInetAddresses().nextElement().getHostAddress();
			}
			if(netInterface != null) {
				//collect our chosen network interface name
				tmpPreferedInterface = netInterface.getName();
				//getInstance MAC
				byte[] mac = netInterface.getHardwareAddress();
				StringBuilder builder = new StringBuilder();
				for (byte a : mac) {
					builder.append(String.format("%02x", a));
				}
				tmpMAC = builder.substring(0, builder.length());
			}
			//first attempt at hostname is to query the /etc/hostname file which should have
			//renamed itself (on the PI) before this Java code runs
			try {
				Scanner s = new Scanner(new File("/etc/hostname"));
				String line = s.next();
				if (line != null && !line.isEmpty() && !line.endsWith("-")) {
					tmpHostname = line;
				}
				s.close();
			} catch(Exception e) {/*Swallow this exception*/}
			//if we don't have the mac derive the MAC from the hostname
			if(tmpMAC == null && tmpHostname != null) {
				tmpMAC = tmpHostname.substring(8, 20);
			}
			//if we don't have the hostname getInstance by traditional means
			// Windows seems to like this one.
			if(tmpHostname == null) {
				try {
					tmpHostname = InetAddress.getLocalHost().getHostName();
				}
				catch (UnknownHostException e) {
					System.out.println("Unable to find host name, resorting to IP Address");
					e.printStackTrace();
				}
			}
			
			//If everything still isn't working lets try via our interface for an IP address
			if (tmpHostname == null) {
				String address = netInterface.getInetAddresses().nextElement().getHostAddress();
				//strip off trailing interface name if present
				if (address.contains("%")) {
					tmpHostname = address.split("%")[0];
				}
				else {
					tmpHostname = address;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//ensure we have a local suffix
		// Windows won't care either way but *nix systems need it
		//If there are ':' we are probably dealing with a IPv6 address
		if (tmpHostname != null && !tmpHostname.contains(".") && !tmpHostname.contains(":")) {
			tmpHostname += ".local";	//we'll assume a .local extension is required if no extension exists
		}
		
		myHostname          = tmpHostname;
        myIP                = tmpIP;
		myMAC               = tmpMAC;
		preferedInterface   = tmpPreferedInterface;
		//report
		System.out.println("My hostname is:           " + myHostname);
        System.out.println("My IP address is:         " + myIP);
		System.out.println("My MAC address is:        " + myMAC);
		System.out.println("My prefered interface is: " + preferedInterface);
	}
	
	public static boolean isViableNetworkInterface(NetworkInterface ni) {
		try {
			if ( !ni.supportsMulticast()						) return false;
			if ( ni.isLoopback()								) return false;
			if ( !ni.isUp()										) return false;
			if ( ni.isVirtual()									) return false;
			if ( ni.getDisplayName().matches(".*[Vv]irtual.*")	) return false; //try and catch out any interfaces which don't admit to being virtual
		} catch (SocketException e) {
			System.out.println("Error checking interface " + ni.getName());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static void main(String[] args) {
		//static code above will run
		@SuppressWarnings("unused")
		String x = Device.myHostname;
	}
	
}
