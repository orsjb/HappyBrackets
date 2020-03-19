package net.happybrackets.device;

import net.beadsproject.beads.core.AudioContext;

import javax.swing.*;
import java.awt.*;

/**
 * Displays the waveform on a JPanel based on a {@link AudioContext}
 * The class must be created using {@link #createVisualiser()} first, and then
 * have the {@link AudioContext} assigned using the {@link #setAudioContext(AudioContext)} function
 * This is required to because the AWT object must be created before an {@link AudioContext} due to a quirk
 * in some systems
 */
public class WaveformVisualizer extends JPanel {

    static final int HEIGHT = 300, WIDTH = 500;
    volatile AudioContext ac = null;

    final Object acLock =  new Object(); // we need to this to prevent concurrent access

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        synchronized (acLock) {
            if (ac != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                g.setColor(Color.BLUE);


                for (int chan = 0; chan < ac.out.getOuts(); chan++) {
                    float[] buf = ac.out.getOutBuffer(chan);
                    int lastY = (int) (((buf[0] * 0.5f + 0.5) + chan) * HEIGHT / ac.out.getOuts());
                    int lastX = 0;

                    //g2d.moveTo(0, lastY);
                    for (int i = 0; i < buf.length; i++) {
                        float f = buf[i];
                        if (f < -1) f = -1;
                        if (f > 1) f = 1;
                        if (Float.isNaN(f)) f = 0;
                        int x = (int) ((float) i / buf.length * WIDTH);
                        int y = (int) (((f * 0.5f + 0.5) + chan) * HEIGHT / ac.out.getOuts());
                        g.drawLine(lastX, lastY, x, y);
                        lastX = x;
                        lastY = y;
                    }
                }

                g2d.dispose();
            }
        }
    }


    /**
     * Constructor
     */
    private WaveformVisualizer(){
    }

    /**
     * Set the {@link AudioContext} that we are displaying audio waveform of
     * @param context the {@link AudioContext} to display
     */
    public void setAudioContext(AudioContext context){
        synchronized (acLock) {
            ac = context;
        }
    }

    /**
     * Create a {@link WaveformVisualizer} that will be used to display audio waveform
     * The initial {@link AudioContext} is null so the object can be created without crashing the system
     * @return the {@link WaveformVisualizer} we will later make call to {@link #setAudioContext(AudioContext)} to
     */
    public static WaveformVisualizer createVisualiser() {

        WaveformVisualizer visualizer = null;
        try {

            JFrame frame = new JFrame("Happy Brackets Output");

            visualizer = new WaveformVisualizer();
            frame.getContentPane().add(visualizer);

            frame.setSize(WIDTH, HEIGHT);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            // Type threadFunction to generate this code
            WaveformVisualizer finalVisualizer = visualizer;

            Thread thread = new Thread(() -> {
                int SLEEP_TIME = 1;
                while (true) {// write your code below this line
                    try {
                        SwingUtilities.invokeLater(() -> {
                            try {
                                finalVisualizer.repaint();
                            } catch (Exception ex) {
                            }
                        });

                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {// remove the break below to just resume thread or add your own action
                        break;

                    } catch (Exception ex) {
                    }

                }
            });

            thread.start();// End threadFunction
        } catch (Exception ex) {
        }
        return visualizer;
    }
}
