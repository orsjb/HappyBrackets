package net.happybrackets.intellij_plugin.menu.global;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import de.sciss.net.OSCMessage;
import net.happybrackets.intellij_plugin.controller.ControllerEngine;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.core.scheduling.ClockAdjustment;
import net.happybrackets.core.scheduling.HBScheduler;
import net.happybrackets.device.network.UDPCachedMessage;
import net.happybrackets.intellij_plugin.NotificationMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Menu action to reboot all devices
 */
public class SynchroniseDevicesAction extends AnAction
{
    final int MAX_UDP_SENDS = 3;

    class DeviceSendSettings{
        DatagramSocket advertiseTxSocket = null;

        int devicePort = 0;

        InetAddress [] inetAddresses = new InetAddress[3];
        public DeviceSendSettings(){
            try {
                devicePort = ControllerEngine.getInstance().getControllerConfig().getControlToDevicePort();
                advertiseTxSocket = new DatagramSocket();
                advertiseTxSocket.setBroadcast(true);
                InetAddress broadcast = InetAddress.getByName("255.255.255.255");
                InetAddress multicast = InetAddress.getByName(ControllerEngine.getInstance().getControllerConfig().getMulticastAddr());
                InetAddress localhost = InetAddress.getLoopbackAddress();

                inetAddresses[0] = broadcast;
                inetAddresses[1] = multicast;
                inetAddresses[2] = localhost;

            }
            catch (Exception ex){}
        }
    }


    static DeviceSendSettings deviceSendSettings = null;

    synchronized DeviceSendSettings getDeviceSettings(){
        if (deviceSendSettings == null){
            deviceSendSettings = new DeviceSendSettings();
        }
        return deviceSendSettings;
    }
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

        boolean success =  false;

        DeviceSendSettings settings = getDeviceSettings();


        if (settings.advertiseTxSocket != null) {
            double current_time = HBScheduler.getCalcTime();
            ClockAdjustment adjustmentMessage = new ClockAdjustment(current_time, 0);

            // encode our message
            OSCMessage message = HBScheduler.buildNetworkSendMessage(OSCVocabulary.SchedulerMessage.SET, adjustmentMessage);



            UDPCachedMessage cached_message = null;
            try {
                cached_message = new UDPCachedMessage(message);
                DatagramPacket packet = cached_message.getCachedPacket();

                for (int i = 0; i < settings.inetAddresses.length; i++) {
                    packet.setAddress(settings.inetAddresses[i]);
                    packet.setPort(settings.devicePort);

                    for (int j = 0; j < MAX_UDP_SENDS; j++) {
                        try {
                            settings.advertiseTxSocket.send(packet);
                            success = true;
                        } catch (IOException e) {
                            System.out.println("Unable to broadcast");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        if (success){
            NotificationMessage.displayNotification("Set Schedule Time", NotificationType.INFORMATION);
        }
        else
        {
            NotificationMessage.displayNotification("Unable to Set Schedule Time", NotificationType.WARNING);
        }
    }
}
