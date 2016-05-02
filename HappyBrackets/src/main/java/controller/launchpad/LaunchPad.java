package controller.launchpad;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import javax.swing.JComponent;
import javax.swing.JFrame;


public class LaunchPad {

	//specific colours, in MIDI Colour form
	public final static int OFF = 12;
	public final static int LOW_RED = 13;
	public final static int HIGH_RED = 15;
	public final static int LOW_AMBER = 29;
	public final static int HIGH_AMBER = 63;
	public final static int YELLOW = 62;
	public final static int LOW_GREEN = 28;
	public final static int HIGH_GREEN = 60;

	public final static int MEDIUM_RED = 14;
	public final static int MEDIUM_AMBER = 46;
	public final static int MEDIUM_GREEN = 44;
	
	JFrame frame = null;
	JComponent drawingSurface = null;
	String infoString = "";
	final int[][][] buttonColours;	//[launchPadID][row][col]
	final LaunchPadBehaviour behaviour;
	final String[] launchPads;
	Receiver[] receivers;
	Transmitter[] transmitters;
	
	public LaunchPad(String[] launchPads, LaunchPadBehaviour behaviour) {
		this.launchPads = launchPads;
		this.behaviour = behaviour;
		buttonColours = new int[launchPads.length][9][9];
		receivers = new Receiver[launchPads.length];
		transmitters = new Transmitter[launchPads.length];
		for(int i = 0; i < launchPads.length; i++) {
			setupMidi(i);
		}
	}
	
	private void setupMidi(final int launchPadID) {
		try {
			Info[] infos = MidiSystem.getMidiDeviceInfo();
			for(Info info : infos) {
				if(info.getName().equals(launchPads[launchPadID])) {	
					MidiDevice launchPadDevice = MidiSystem.getMidiDevice(info);
					//try to see if it is a transmitter
					try {
						launchPadDevice.open();
						Transmitter trans = launchPadDevice.getTransmitter();
						Receiver rec = new Receiver() {
							@Override
							public void send(MidiMessage message, long timeStamp) {
//								System.out.println("LaunchPad MIDI reciever has received a message");
								ShortMessage myMessage = (ShortMessage)message;
								boolean push = (myMessage.getData2() > 0);
								int butID = myMessage.getData1();
								int row, col;
								boolean topRow = myMessage.getStatus() != 144;
								if(topRow) {
									row = 0;
									col = butID - 104;
								} else {
									row = butID / 16 + 1;
									col = butID % 16;
								}
								behaviour.buttonAction(LaunchPad.this, launchPadID, row, col, push);
								
							}
							@Override
							public void close() {
								//what to do on close?
							}
						};
						trans.setReceiver(rec);
						transmitters[launchPadID] = trans;
					} catch(Exception e) {	//else it must be a receiver
						launchPadDevice.open();
						Receiver rec = launchPadDevice.getReceiver();
						receivers[launchPadID] = rec;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		flashState();
		//add system exit hook to clear state
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				setState(new int[launchPads.length][9][9]);
			}
		});
	}
	
	public void flashState() {
		for(int i = 0; i < launchPads.length; i++) {
			for(int j = 0; j < 9; j++) {
				for(int k = 0; k < 9; k++) {
					sendButtonColour(i, j, k);
				}
			}
		}
	}
	
	public void setState(int[][][] newButtonColours) {
		for(int i = 0; i < launchPads.length; i++) {
			for(int j = 0; j < 9; j++) {
				for(int k = 0; k < 9; k++) {
					setButtonColour(i, j, k, newButtonColours[i][j][k]);
				}
			}
		}
	}
	
	public void setState(int launchPadID, int[][] newButtonColours) {
		for(int j = 0; j < 9; j++) {
			for(int k = 0; k < 9; k++) {
				setButtonColour(launchPadID, j, k, newButtonColours[j][k]);
			}
		}
	}
	
	public void saveState(String path) {
		try {
		FileOutputStream fos = new FileOutputStream(new File(path));
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(buttonColours);
		oos.close();
		fos.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public int[][][] readState(String path) {
		try {
			FileInputStream fis = new FileInputStream(new File(path));
			ObjectInputStream ois = new ObjectInputStream(fis);
			int[][][] result = (int[][][])ois.readObject();
			ois.close();
			fis.close();
			setState(result);
			return result;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void show() {
		if(frame == null) {
			frame = new JFrame();
			drawingSurface = new JComponent() {
				private static final long serialVersionUID = 1L;
				public void paintComponent(Graphics g) {
					((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g.setColor(Color.white);
					g.fillRect(0, 0, getWidth(), getHeight());
					g.setColor(Color.black);
					g.drawString(infoString, 10, 25);
					int launchPadX = 0;
					int buttonX = 0;
					int buttonY = 0;
					for(int launchPad = 0; launchPad < launchPads.length; launchPad++) {
						launchPadX = launchPad * 300;
						for(int row = 0; row < 9; row++) {
							buttonY = row * 30 + 30;
							for(int col = 0; col < 9; col++) {
								buttonX = col * 30 + 10;
								Color c = mIDIColourToJavaColour(buttonColours[launchPad][row][col]);
								g.setColor(c);
								if(col == 8 || row == 0) {
									if(!(col == 8 && row == 0)) {
										g.fillOval(launchPadX + buttonX, buttonY, 25, 25);
										g.setColor(Color.black);
										g.drawOval(launchPadX + buttonX, buttonY, 25, 25);
									}
								} else {
									g.fillRect(launchPadX + buttonX, buttonY, 25, 25);
									g.setColor(Color.black);
									g.drawRect(launchPadX + buttonX, buttonY, 25, 25);
								}
							}
						}
					}
				}
			};
			frame.setContentPane(drawingSurface);
			drawingSurface.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					int x = e.getX();
					int y = e.getY();
					int launchPadID = x / 300;
					int row = (y / 30) - 1;
					int col = (x - (300 * launchPadID) - 10) / 30;
					if(row < 9 && row >= 0 && col < 9 && col >= 0) {
						behaviour.buttonAction(LaunchPad.this, launchPadID, row, col, true);
						infoString = "last pressed: " + launchPadID + " " + row + " " + col;
						refresh();
					}
				}
				public void mouseReleased(MouseEvent e) {
					int x = e.getX();
					int y = e.getY();
					int launchPadID = x / 300;
					int row = (y / 30) - 1;
					int col = (x - (300 * launchPadID) - 10) / 30;
					if(launchPadID < 3 && row < 9 && row >= 0 && col < 9 && col >= 0) {
						behaviour.buttonAction(LaunchPad.this, launchPadID, row, col, false);
						infoString = "last pressed: " + launchPadID + " " + row + " " + col;
						refresh();
					}
				}
			});
		}
		frame.setSize(300 * launchPads.length, 330);
		frame.setResizable(false);
		frame.setVisible(true);
	}
	
	void refresh() {
		if(drawingSurface != null) {
			drawingSurface.repaint();
		}
	}
	
	public void setButtonColour(int launchPadID, int row, int col, int midiColour) {
		if(buttonColours[launchPadID][row][col] != midiColour) {
			buttonColours[launchPadID][row][col] = midiColour;
			sendButtonColour(launchPadID, row, col);
		}
	}
	
	public void sendButtonColour(int launchPadID, int row, int col) {
		long timeStamp = System.currentTimeMillis(); //??
		ShortMessage myMidiMessage = new ShortMessage();
		if(receivers[launchPadID] != null) {
			try {
				int butID;
				if(row == 0) {
					butID = 104 + col;
					myMidiMessage.setMessage(176, butID, buttonColours[launchPadID][row][col]);
				} else {
					butID = (row - 1) * 16 + col;
					myMidiMessage.setMessage(144, butID, buttonColours[launchPadID][row][col]);
				}
				receivers[launchPadID].send(myMidiMessage, timeStamp);
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}
		}
		refresh();
	}
	
	private Color mIDIColourToJavaColour(int midiColour) {
		switch(midiColour) {
		case OFF:
			return new Color(1f, 1f, 1f);
		case LOW_RED:
			return new Color(0.2f, 0f, 0f);
		case HIGH_RED:
			return new Color(1f, 0f, 0f);
		case LOW_AMBER:
			return new Color(0.2f, 0.2f, 0f);
		case HIGH_AMBER:
			return new Color(1f, 0.5f, 0f);
		case YELLOW:
			return new Color(1f, 1f, 0f);
		case LOW_GREEN:
			return new Color(0f, 0.2f, 0f);
		case HIGH_GREEN:
			return new Color(0f, 1f, 0f);
		case MEDIUM_RED:
			return new Color(0.5f, 0f, 0f);
		case MEDIUM_AMBER:
			return new Color(0.5f, 0.5f, 0f);
		case MEDIUM_GREEN:
			return new Color(0, 0.5f, 0f);
		default:
			float r = ((midiColour) % 16) / 3f;
			float g = (midiColour - r) / 48f;
			return new Color(r, g, 0);
		}
	}
	
	public static int rgToMidiColour(float r, float g) {
		if(r < 0) r = 0; else if(r > 1) r = 1;
		if(g < 0) g = 0; else if(g > 1) g = 1;
		return (int)(g * 3) * 16 + (int)(r * 3) + 12;
	}

	public int getButtonColour(int launchPadID, int row, int col) {
		return buttonColours[launchPadID][row][col];
	}
}
