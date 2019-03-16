package net.happybrackets.core;

import de.sciss.net.OSCMessage;

public class OSCMessageBuilder {
    /**
     * Creates a valid OSC message. It converts invalid types to OSC compatible
     * EG, doubles to floats, boolean to int, long to int
     * @param name OSC Message name
     * @param args arguments
     * @return OSCMessage
     */
    public static OSCMessage createOscMessage(String name, Object ...args){
        Object [] arg_list =  new Object[args.length];
        for(int i = 0; i < args.length; i++){
            Object source_value = args[i];

            if (source_value instanceof Boolean)
            {
                boolean b = (Boolean) source_value;
                arg_list[i] =  b? 1:0;
            }else if (source_value instanceof Long)
            {
                arg_list[i] =  ((Long) source_value).intValue();

            } else if (source_value instanceof Double) {
                Double d = (Double) source_value;
                float f = d.floatValue();
                arg_list[i] = f;
            }
            else{
                arg_list[i] = source_value;
            }

        }

        return new OSCMessage(name, arg_list);
    }

    public static void main(String[] args) {

        try {

            OSCMessage message =  createOscMessage("MyName", 1, true, 1.2);

            System.out.println(message.getName());

            for (int i = 0; i < message.getArgCount(); i++){
                System.out.println(message.getArg(i));
            }
            //receiver.doReceive();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
