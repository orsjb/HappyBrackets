/*
 * Copyright 2016 Ollie Bown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.happybrackets.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Device {

	final static Logger logger = LoggerFactory.getLogger(Device.class);

//	public  final String    myHostname;		//the hostname for this PI (wifi)
//	public  final String    myIP;
//	public  final String    myMAC;          //the wlan MAC for this PI (wifi)
//
//  private static Device singleton = null;
//
//  public static Device getInstance() {
//      if(singleton == null) {
//          singleton = new Device();
//      }
//      return singleton;
//  }
//
//	private Device() {
//        logger.info("Beginning device network setup");
//
//
//
//        String tmpHostname = null;
//        String tmpIP = null;
//		String tmpMAC = null;
//		String tmpPreferedInterface = null;
//		try {
//			NetworkInterface netInterface;
//			String operatingSystem = System.getProperty("os.name");
//			logger.debug("Detected OS: " + operatingSystem);
//
//            logger.debug("Interfaces:");
//            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
//            String favouriteInterfaceName = null;
//            ArrayList<NetworkInterface> favouredInterfaces = new ArrayList<>();
//            while (interfaces.hasMoreElements()) {
//                netInterface = interfaces.nextElement();
//                // Windows by default has a lot of extra interfaces,
//                //  lets at least try and getInstance a real interface...
//                if (isViableNetworkInterface(netInterface)) {
//                    //collect all viable interfaces
//                    favouredInterfaces.add(netInterface);
//                    //favouriteInterfaceName = netInterface.getName();
//                    logger.debug("    {} ({}) : VALID", netInterface.getName(), netInterface.getDisplayName());
//                }
//                else {
//                    logger.debug("    {} ({}) : IGNORED", netInterface.getName(), netInterface.getDisplayName());
//                }
//            }
//
//            if ( !favouredInterfaces.isEmpty() ) {
//                logger.debug("Selecting from valid interfaces:");
//                favouredInterfaces.forEach( (i) -> logger.debug("    {} ({})", i.getName(), i.getDisplayName()) );
//
//                // Populate valid interfaces array
//                validInterfaces = new String[favouredInterfaces.size()];
//                for (int i = 0; i < validInterfaces.length ; i++) {
//                    //if this list was longer we should use an iterator, but as it is only short this will do
//                    validInterfaces[i] = favouredInterfaces.get(i).getName();
//                }
//
//                if (favouredInterfaces.size() == 1) {
//                    netInterface = favouredInterfaces.get(0);
//                } else if (operatingSystem.startsWith("Windows") || operatingSystem.startsWith("Linux")) {
//                    favouredInterfaces.sort( (a, b) -> a.getName().compareToIgnoreCase(b.getName()) ); //sort interface by name
//                    netInterface = favouredInterfaces.get(favouredInterfaces.size() - 1); //get last, this should be a wlan interface if available
//                }
//                else if (operatingSystem.startsWith("Mac OS")) {
//                    favouredInterfaces.sort( (a, b) -> a.getName().compareToIgnoreCase(b.getName()) ); //sort interface by name
//                    netInterface = favouredInterfaces.get(0);
//                    //TODO We are hardcoding en0 as the chosen port (on Mac we insist of WiFi), but can we be smarter?
//                    netInterface = NetworkInterface.getByName("en0");
////                    netInterface = NetworkInterface.getByName("lo0");
//                }
//                else {
//                    logger.warn("Operating system {} is not expressly handled, defaulting to first favoured interface", operatingSystem);
//                    netInterface = favouredInterfaces.get(0);
//                }
//            }
//            else {
//                // take a stab in the dark...
//                if (operatingSystem.startsWith("Linux") || operatingSystem.startsWith("Windows")) {
//                    netInterface = NetworkInterface.getByName("wlan0");
//                }
//                else if (operatingSystem.startsWith("Mac OS")) {
//                    netInterface = NetworkInterface.getByName("en1");
////                    netInterface = NetworkInterface.getByName("lo0");
//                }
//                else {
//                    logger.error("Unable to determine a network interface!");
//                    netInterface = NetworkInterface.getByIndex(0); //Maybe the loopback?
//                }
//            }
//
//            //report back
//            logger.debug("Selected interface: {} ({})", netInterface.getName(), netInterface.getDisplayName());
//
//			//Addresses
//            ArrayList<InterfaceAddress> addresses = new ArrayList<>();
//            netInterface.getInterfaceAddresses().forEach( (a) -> addresses.add(a) );
//            addresses.sort( (a, b) -> a.getAddress().getHostAddress().compareTo(b.getAddress().getHostAddress()) );
//
//            logger.debug("Available interface addresses:");
//            addresses.forEach( (a) -> logger.debug("    {}", a.getAddress().getHostAddress()) );
//
//            tmpHostname = addresses.get(0).getAddress().getHostName();
//            tmpIP       = addresses.get(0).getAddress().getHostAddress();
//
//			if(netInterface != null) {
//				//collect our chosen network interface name
//				tmpPreferedInterface = netInterface.getName();
//				//getInstance MAC
//				byte[] mac = netInterface.getHardwareAddress();
//				StringBuilder builder = new StringBuilder();
//				for (byte a : mac) {
//					builder.append(String.format("%02x", a));
//				}
//				tmpMAC = builder.substring(0, builder.length());
//			}
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
//            //strip off trailing interface name if present
//            if (tmpIP.contains("%")) {
//                tmpIP = tmpIP.split("%")[0];
//            }
//
//            //strip off trailing interface name if present
//            if (tmpHostname.contains("%")) {
//                tmpHostname = tmpHostname.split("%")[0];
//            }
//
//		} catch (Exception e) {
//			logger.error("Error encountered when assessing interfaces and addresses!", e);
//		}
//
//		//ensure we have a local suffix
//		// Windows won't care either way but *nix systems need it
//		//If there are ':' we are probably dealing with a IPv6 address
//		if (tmpHostname != null && !tmpHostname.contains(".") && !tmpHostname.contains(":")) {
//			tmpHostname += ".local";	//we'll assume a .local extension is required if no extension exists
//		}
//
////		myHostname          = tmpHostname;
////      myIP                = tmpIP;
////		myMAC               = tmpMAC;
////		preferredInterface  = tmpPreferedInterface;
//
//        myHostname          = selectHostname();
//        myIP                = selectIP();
//        myMAC               = selectMAC();
//
//		//report
//		logger.debug("My hostname is:            {}", myHostname);
//        logger.debug("My IP address is:          {}", myIP);
//		logger.debug("My MAC address is:         {}", myMAC);
//        logger.debug("Device network setup complete");
//    }

    /**
     * Decide if a network interface is useful for HappyBrackets
     * @param ni networkInterface
     * @return boolean
     */
	public static boolean isViableNetworkInterface(NetworkInterface ni) {
		try {
			//if ( !ni.supportsMulticast()												) return false;
			//if ( ni.isLoopback()														) return false;
            if ( !ni.isLoopback() && !isValidMac(selectMAC(ni))                         ) return false;
			if ( !ni.isUp()										  						) return false;
			if ( ni.isVirtual()															) return false; // No sub interfaces
			//if ( ni.getDisplayName().matches(".*[Vv]irtual.*")	                    ) return false; //try and catch out any interfaces which belong to a virtualisation environment
            if ( !ni.getInetAddresses().hasMoreElements()                               ) return false; // Make sure we can access at least one address for this interface.
		} catch (SocketException e) {
			logger.error("Error checking interface {}", ni.getName(), e);
			return false;
		}
		return true;
	}

    /**
     * Obtain a list of NetworkInterface objects which are of interest to HappyBrackets.
     * @return a List of Viable network Interfaces
     */
	public static ArrayList<NetworkInterface> viableInterfaces() {
        ArrayList<NetworkInterface> viableInterfaces = new ArrayList<>();

        try {
            for (Enumeration<NetworkInterface> i = NetworkInterface.getNetworkInterfaces(); i.hasMoreElements();) {
                NetworkInterface netInt = i.nextElement();
                // Windows by default has a lot of sub interfaces, so we need to filter these out
                if (isViableNetworkInterface(netInt)) {
                    viableInterfaces.add(netInt);
                    /*
                    logger.debug("    {} ({}, MAC: {}, host name: {}, IP: {}) : VALID", new Object[]{
                            netInt.getName(),
                            netInt.getDisplayName(),
                            selectMAC(netInt),
                            selectHostname(netInt),
                            selectIP(netInt)
                    });*/
                }
                else {
                    //logger.debug("    {} ({}) : IGNORED", netInt.getName(), netInt.getDisplayName());
                }
            }
        } catch (SocketException e) {
            logger.error("Unable to collect network interfaces!", e);
        }

        return viableInterfaces;
    }

    /**
     * Decide on what my hostname should be.
     * @param ni network Interface
     * @return host name as String
     */
    public static String selectHostname(NetworkInterface ni) {
        return removeLinkLocalSuffix(ni.getInetAddresses().nextElement().getCanonicalHostName());
    }

    /**
     * Decide what my IP address should be.
     * @param ni network interface
     * @return IP address as String
     */
    public static String selectIP(NetworkInterface ni) {
        return removeLinkLocalSuffix(ni.getInetAddresses().nextElement().getHostAddress());
    }

    /**
     * * Remove the trailing link local from IPv6 address and allow IPv4 to pass through.
     * @param address The input address
     * @return the IPv4 address
     */
    public static String removeLinkLocalSuffix(String address) {
        return address.split("%")[0];
    }

    /**
     * Decide what my MAC address should be
     * @param ni Network Interface
     * @return MAC address as String
     */
    public static String selectMAC(NetworkInterface ni) {
        try {
            byte[] mac = ni.getHardwareAddress();
            if (mac == null) {
                mac = new byte[] {0, 0, 0, 0, 0, 0};
            }

            StringBuilder builder = new StringBuilder();
            for (byte a : mac) {
                builder.append(String.format("%02x", a));
            }

            return builder.substring(0, builder.length());
        }
        catch (SocketException e) {
            logger.error("Unable to obtain MAC address for interface {}", ni.getDisplayName(), e);
            return "error";
        }
    }

    public static boolean isValidMac(String mac) {
        if (mac.equals(emptyMac())) {
            return false;
        }
        return true;
    }

    public static String emptyMac() {
        return "000000000000";
    }

    public static String getDeviceName() {
        if (System.getProperty("os.name").contains("Windows")) {
            return "WindowsDevice";
        }
        try {
            Scanner s = new Scanner(new File("/etc/hostname"));
            String line = s.next();
            if (line != null && !line.isEmpty() && !line.endsWith("-")) {
                logger.debug("Read device name from /etc/hostname. Name is {}", line);
                return line;
            }
            s.close();
        } catch (Exception e) {

            //try to get it using the 'hostname' command
            String nameFromCommandLine = hostnameFromCommandline();
            if(nameFromCommandLine != null) {
                return nameFromCommandLine;
            } else {
                logger.debug("Problem reading device name at /etc/hostname for OS: {}", System.getProperty("os.name"), e);
            }
        }
        return "Unnamed";
    }

    private static String hostnameFromCommandline() {       //TODO make this work for Windows?
        String line=null;
        try {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec("hostname");
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            line = input.readLine();
//            while((line=input.readLine()) != null) {
//                System.out.println(line);
//            }
//            int exitVal = pr.waitFor();
        } catch(Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
        return line;
    }

    // http://stackoverflow.com/a/2406819
    public static boolean isThisMyIpAddress(InetAddress addr) {
        // Check if the address is a valid special local or loop back
        if (addr.isAnyLocalAddress() || addr.isLoopbackAddress())
            return true;

        // Check if the address is defined on any interface
        try {
            return NetworkInterface.getByInetAddress(addr) != null;
        } catch (SocketException e) {
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println(hostnameFromCommandline());
    }
}
