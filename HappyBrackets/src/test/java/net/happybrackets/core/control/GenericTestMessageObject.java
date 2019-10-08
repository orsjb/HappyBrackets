package net.happybrackets.core.control;

import com.google.gson.Gson;

// Create a generic Class to pass as an object. It will go as a JSON string
class GenericTestMessageObject {
    String val ="";

    public GenericTestMessageObject(){};
    public GenericTestMessageObject(String message){
        val = message;
    }

    /**
     * Decode the Object as a GenericTestMessageObject
     * @param value the object to decode
     * @return a decoded value if able to or null if not
     */
    public static GenericTestMessageObject decode(Object value){
        GenericTestMessageObject ret = null;
        if (value == null){
            return null;
        }else if (value instanceof GenericTestMessageObject)
        {
            ret = (GenericTestMessageObject) value;
        }
        else if (value instanceof String)
        {
            ret = new Gson().fromJson((String) value, GenericTestMessageObject.class);
        }

        return ret;
    }
    @Override
    public String toString() {
        return val;
    }

    @Override
    public boolean equals(Object other){
        if (other == null){
            return false;
        }else if (! (other instanceof GenericTestMessageObject))
        {
            return false;
        }
        else {
            GenericTestMessageObject right  = (GenericTestMessageObject)other;

            return val.equals(right.val);
        }
    }
}