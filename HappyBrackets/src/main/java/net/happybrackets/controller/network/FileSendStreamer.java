package net.happybrackets.controller.network;


import java.io.*;


/**
 * Class to sequentially read bytes for a file so we can send them out
 */
public class FileSendStreamer implements Comparable{
    String sourceFilename;
    String targetFilename;
    boolean complete = false;

    FileInputStream fileInputStream = null;
    /**
     * Constructor
     * @param source source file
     * @param target target_file
     */
    FileSendStreamer (String source, String target){
        sourceFilename = source;
        targetFilename = target;
    }

    /**
     * Read bytes from our source file
     * If input file is not open, will open a new one and keey it open
     * If file is completely read, complete flag will become true
     * @param max_bytes the maximum number of bytes to read
     * @return a byte array containing bytes
     * @exception if unable to read from file
     */
    synchronized byte [] readData(int max_bytes)throws Exception {
        byte [] ret = null;

        if (!complete) {
            if (fileInputStream == null) {
                fileInputStream = new FileInputStream(new File(sourceFilename)); // removed static attachment of bin/ to path
            }

            if (fileInputStream != null) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int data = fileInputStream.read();
                while (data != -1) {
                    buffer.write(data);
                    if (buffer.size() < max_bytes) {
                        data = fileInputStream.read();
                    }
                    else {
                        break;
                    }
                }
                if (data == -1){
                    fileInputStream.close();
                    complete = true;
                }

                ret = buffer.toByteArray();
                buffer.close();
            }
        }

        return ret;
    }

    @Override
    public boolean equals(Object o) {

        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof FileSendStreamer)) {
            return false;
        }


        return this.compareTo(o) == 0;
    }

    @Override
    public int compareTo(Object o) {
        FileSendStreamer r =  (FileSendStreamer) o;
        int ret = sourceFilename.compareTo(r.sourceFilename);

        if (ret == 0){
            ret = targetFilename.compareTo(targetFilename);
        }


        return ret;
    }


}
