package net.happybrackets.core;

import java.io.File;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;

public class Device {

	public final String myHostname;						//the hostname for this PI (wifi)
	public final String myIP;
	public final String myMAC;							//the wlan MAC for this PI (wifi)
	public final String preferedInterface;

    private static Device singleton = null;

    public static Device getInstance() {
        if(singleton == null) {
            singleton = new Device();
        }
        return singleton;
    }

	private Device() {
		String tmpHostname = null;
        String tmpIP = null;
		String tmpMAC = null;
		String tmpPreferedInterface = null;
		try {
			NetworkInterface netInterface;
			String operatingSystem = System.getProperty("os.name");
			System.out.println("Detected OS: " + operatingSystem);

            System.out.println("Interfaces:");
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            String favouriteInterfaceName = null;
            ArrayList<NetworkInterface> favouredInterfaces = new ArrayList<>();
            while (interfaces.hasMoreElements()) {
                netInterface = interfaces.nextElement();
                // Windows by default has a lot of extra interfaces,
                //  lets at least try and getInstance a real interface...
                if (isViableNetworkInterface(netInterface)) {
                    //collect all viable interfaces
                    favouredInterfaces.add(netInterface);
                    //favouriteInterfaceName = netInterface.getName();
                    //System.out.println("I like: " + favouriteInterfaceName + ", " + netInterface.getDisplayName());
                }
                else {
                    System.out.println("Ignored: " + netInterface.getName() + ", " + netInterface.getDisplayName());
                }
            }
            if ( !favouredInterfaces.isEmpty() ) {
                System.out.println("Selecting from valid interfaces:");
                favouredInterfaces.forEach((i) -> System.out.println("\t" + i.getName() + ", " + i.getDisplayName()));

                if (favouredInterfaces.size() == 1) {
                    netInterface = favouredInterfaces.get(0);
                } else if (operatingSystem.startsWith("Windows") || operatingSystem.startsWith("Linux")) {
                    favouredInterfaces.sort( (a, b) -> a.getName().compareToIgnoreCase(b.getName()) ); //sort interface by name
                    netInterface = favouredInterfaces.get(favouredInterfaces.size() - 1); //get last, this should be a wlan interface if available
                }
                else if (operatingSystem.startsWith("Mac OS")) {
                    netInterface = favouredInterfaces.get(0);
                }
                else {
                    System.err.println("Operating system " + operatingSystem + " is not expressly handled, defaulting to first favoured interface");
                    netInterface = favouredInterfaces.get(0);
                }
            }
            else {
                // take a stab in the dark...
                if (operatingSystem.startsWith("Linux") || operatingSystem.startsWith("Windows")) {
                    netInterface = NetworkInterface.getByName("wlan0");
                }
                else if (operatingSystem.startsWith("Mac OS")) {
                    netInterface = NetworkInterface.getByName("en1");
                }
                else {
                    System.err.println("Unable to determine a network interface!");
                    netInterface = NetworkInterface.getByIndex(0); //Maybe the loopback?
                }
            }

            //report back
            System.out.println("Selected interface: " + netInterface.getName() + ", " + netInterface.getDisplayName());

			//Addresses
            ArrayList<InterfaceAddress> addresses = new ArrayList<>();
            netInterface.getInterfaceAddresses().forEach( (a) -> addresses.add(a) );
            addresses.sort( (a, b) -> a.getAddress().getHostAddress().compareTo(b.getAddress().getHostAddress()) );

            System.out.println("Available interface addresses:");
            addresses.forEach( (a) -> System.out.println("\t" + a.getAddress().getHostAddress() ) );

            tmpHostname = addresses.get(0).getAddress().getHostName();
            tmpIP       = addresses.get(0).getAddress().getHostAddress();

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
//			//first attempt at hostname is to query the /etc/hostname file which should have
//			//renamed itself (on the PI) before this Java code runs
//			try {
//				Scanner s = new Scanner(new File("/etc/hostname"));
//				String line = s.next();
//				if (line != null && !line.isEmpty() && !line.endsWith("-")) {
//					tmpHostname = line;
//				}
//				s.close();
//			} catch(Exception e) {/*Swallow this exception*/}
//			//if we don't have the mac derive the MAC from the hostname
//			if(tmpMAC == null && tmpHostname != null) {
//				tmpMAC = tmpHostname.substring(8, 20);
//			}
//
//			//If everything still isn't working lets try via our interface for an IP address
//			if (tmpHostname == null) {
//				String address = netInterface.getInetAddresses().nextElement().getHostAddress();
//				//strip off trailing interface name if present
//				if (address.contains("%")) {
//					tmpHostname = address.split("%")[0];
//				}
//				else {
//					tmpHostname = address;
//				}
//			}

            //strip off trailing interface name if present
            if (tmpIP.contains("%")) {
                tmpIP = tmpIP.split("%")[0];
            }

            //strip off trailing interface name if present
            if (tmpHostname.contains("%")) {
                tmpHostname = tmpHostname.split("%")[0];
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

}
