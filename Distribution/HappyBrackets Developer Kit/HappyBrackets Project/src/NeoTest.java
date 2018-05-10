import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.util.Objects;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class NeoTest implements HBAction {

    public static class WS2812B implements AutoCloseable {

        public static final int PIXELS = 16;

        public static final int WS2812B_ADDR = 13;

        public static final int SET_ALL_LEDS_CMD = 0xfe;
        public static final int ALL_LEDS_OFF_CMD = 0xff;

        public static class LEDField {

            private Color[] field = new Color[PIXELS];

            public LEDField(Color[] field) {
                this.field = field;
            }

            byte[] getColorField() {
                byte[] retVal = new byte[3*PIXELS];
                int idx = 0;
                for (int i = 0; i < PIXELS; i++) {
                        retVal[idx++] = (byte)Math.min(field[i].getRed(),0xfd);
                        retVal[idx++] = (byte)Math.min(field[i].getGreen(),0xfd);
                        retVal[idx++] = (byte)Math.min(field[i].getBlue(),0xfd);
                }
                return retVal;
            }
        }

        private I2CBus i2c;
        private I2CDevice device;

        public WS2812B() throws IOException {
            i2c = I2CFactory.getInstance(I2CBus.BUS_1);

            device = i2c.getDevice(WS2812B_ADDR);
        }

        public void setLED(int pos, Color color) throws IOException {
//            device.write(pos.x + ROWS * pos.y, new byte[]{(byte)color.getRed(),(byte)color.getGreen(),(byte)color.getBlue()});
            device.write(pos, (byte)color.getRed());
        }

        public void setLED(LEDField field) throws IOException {
            Objects.requireNonNull(field, "LEDField must not be null");
            byte[] colorField = field.getColorField();

            int offset = 0;
            int size = Math.min(offset + 30,colorField.length);
            device.write(SET_ALL_LEDS_CMD, colorField,offset,size);

            while (offset + size < colorField.length) {
                offset = offset + size;
                size = Math.min(30,colorField.length - offset);
                device.write(colorField,offset,size);
            }
        }

        public void clear() throws IOException {
            device.write(ALL_LEDS_OFF_CMD,(byte)0);
        }

        @Override
        public void close() throws IOException {
            if (i2c != null) {
                clear();
                i2c.close();
            }
        }
    }


    @Override
    public void action(HB hb) {
        System.out.println("Running action");
        try {
            WS2812B test = new WS2812B();
            for(int i = 0; i < 16; i++) {
                test.setLED(i, Color.RED);
            }
            System.out.println("Happy so far");

        } catch (Exception e) {
            System.out.println("problem " + e.getMessage());
            e.printStackTrace();
        }
    }
}
