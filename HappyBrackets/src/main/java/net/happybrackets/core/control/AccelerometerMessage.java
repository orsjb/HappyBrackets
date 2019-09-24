package net.happybrackets.core.control;

import com.google.gson.Gson;

/**
 * Encodes x, y and z parameters as an Object array so it can be passed via a Dynamic Control message
 * The conversion only takes place if the message is passed as a global message
 */
public class AccelerometerMessage implements CustomGlobalEncoder{
    static Gson gson = new Gson();

    float x = 0, y = 0, z = 0;

    @Override
    public Object [] encodeGlobalMessage() {
        Object [] ret = new Object[]{x, y, z};
        return ret;
    }

    @Override
    public AccelerometerMessage restore(Object restore_data) {
        AccelerometerMessage ret =  null;

        // First see if this is just the class
        if (restore_data instanceof AccelerometerMessage){
            ret = (AccelerometerMessage)restore_data;
        }
        // let us see if it is JsonData
        else if (restore_data instanceof String){
            ret = new Gson().fromJson((String) restore_data, AccelerometerMessage.class);
        }
        else if (restore_data instanceof Object[]){
            ret =  new AccelerometerMessage((Object[]) restore_data);
        }

        return ret;
    }


    /**
     * Default constructor with zero values
     */
    public AccelerometerMessage(){};

    /**
     * Constructor
     * @param x x_value
     * @param y y_value
     * @param z z_value
     */
    public AccelerometerMessage (float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Constructor using defined Object array as input.
     * @param args the arguments in object array as x, y and z
     */
    private AccelerometerMessage (Object... args){
        x = (float) args[0];
        y = (float) args[1];
        z = (float) args[2];
    }

    @Override
    public String toString() {
        return "x:" + x + " y:" + y + " z:" + z;
    }

    @Override
    public boolean equals(Object other){
        if (other == null){
            return false;
        }else if (! (other instanceof AccelerometerMessage))
        {
            return false;
        }
        else {
            AccelerometerMessage right  = (AccelerometerMessage)other;

            return Float.compare (x, right.x) == 0 && Float.compare (y, right.y) == 0 && Float.compare (z, right.z) == 0;
        }
    }
}
