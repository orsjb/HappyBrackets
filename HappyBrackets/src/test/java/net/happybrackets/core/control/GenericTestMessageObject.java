package net.happybrackets.core.control;

// Create a generic Class to pass as an object. It will go as a JSON string
class GenericTestMessageObject {
    String val ="";

    public GenericTestMessageObject(){};
    public GenericTestMessageObject(String message){
        val = message;
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