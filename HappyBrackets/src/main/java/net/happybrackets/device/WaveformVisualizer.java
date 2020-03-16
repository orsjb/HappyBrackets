package net.happybrackets.device;

import net.beadsproject.beads.core.AudioContext;

import javax.swing.*;
import java.awt.*;

public class WaveformVisualizer extends JPanel {

    static final int HEIGHT = 300, WIDTH = 500;
    final AudioContext ac;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g.setColor(Color.BLUE);

        for(int chan = 0; chan < ac.out.getOuts(); chan++) {
            float[] buf = ac.out.getOutBuffer(chan);
            int lastY = (int) (((buf[0] * 0.5f + 0.5) + chan) * HEIGHT / ac.out.getOuts());
            int lastX = 0;

            //g2d.moveTo(0, lastY);
            for (int i = 0; i < buf.length; i++) {
                float f = buf[i];
                if(f < -1) f = -1; if(f > 1)  f = 1; if(Float.isNaN(f)) f = 0;
                int x = (int) ((float) i / buf.length * WIDTH);
                int y = (int) (((f * 0.5f + 0.5) + chan) * HEIGHT / ac.out.getOuts());
                g.drawLine(lastX, lastY, x, y);
                lastX = x;
                lastY = y;
            }
        }

        g2d.dispose();

    }


    /**
     * Constructor using {@link AudioContext} as parameter so we can paint the waveform
     * @param ac The {@link AudioContext} we are painting the output of
     */
    private WaveformVisualizer(AudioContext ac){
        this.ac = ac;
    }

    public static void createVisualiser(AudioContext ac){

        if (ac.out.getOuts() > 0) {
            JFrame frame = new JFrame("Happy Brackets Output");
            WaveformVisualizer visualizer = new WaveformVisualizer(ac);
            frame.getContentPane().add(visualizer);

            frame.setSize(WIDTH, HEIGHT);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            while (true){
                visualizer.repaint();
            }
        }
    }
}
