package net.happybrackets.core.control;
/**
 * Interface for Encoding and decoding GlobalControl message
 * This interface will allow the encoded data to be optimised instead of encoding via Json
 * Apart from the interface, the target class will need to implement a restore function
 */
public interface CustomGlobalEncoder {
    // Encode the message. Will return either a Json or Object Array
    Object[] encodeGlobalMessage();

    /**
     * Decode the data based on how it is encoded. If the data is not encoded, then it will be returned as is
     * @param restore_data the encoded or decoded data
     * @return the decoded object
     */
    Object restore(Object restore_data);
}
