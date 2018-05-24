/*
 * Copyright 2016 Ollie Bown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.happybrackets.core;

import de.sciss.net.OSCMessage;
import de.sciss.net.OSCTransmitter;
import net.happybrackets.core.config.LoadableConfig;

import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A tool for each device to work out its current synch with respect to all other devices.
 * We keep this independent of the audio system because the audio system start-time needs to be synched.
 *
 * Each synchronizer sends regular pulses every interval with the syntax:
 * s MAC1 timeMS
 *
 * An s means send. Upon receiving an s, each synchronizer also responds with
 * r MAC1 timeMS MAC2 timeMS
 *
 * This volley of broadcasts and responses allows each synchronizer to calculate network round-trips and average these over time. The device with the first MAC address, in alphanumeric order, is considered the master timer.
 */
public class Synchronizer {

	final static Logger logger = LoggerFactory.getLogger(Synchronizer.class);
    final static String oscPath = "/hb/synchonizer";

    private BroadcastManager broadcast;

	private String myMAC = "0"; //how to uniquely identify this machine
	private long timeCorrection = 0;			//add this to current time to getInstance the REAL current time
	private long stableTimeCorrection = 0;
	private long lastTick;
	private int stabilityCount = 0;

	private boolean on = true;
	private boolean verbose = false;
	private boolean timedebug = false;

	private Map<Long, Map<String, long[]>> log;		//first referenced by message send time, then by respodent's name, with the time the respondent replied and the current time

	static Synchronizer singletonSynchronizer;

	public synchronized static Synchronizer getInstance() {
		if(singletonSynchronizer == null) {
			singletonSynchronizer = new Synchronizer();
		}
		return singletonSynchronizer;
	}

	/**
	 * This returns the time in Linux format (ms since Jan 1st 1970) according to the synchronization. Note this is not guaranteed to be the right time, only the same time as the other devices on the network (also not guaranteed, but that's the aim).
	 * @return time in ms since Jan 1st 1970.
	 */
	public static long time() {
		return getInstance().correctedTimeNow();
	}

	private Synchronizer() {
		//basics
		log = new Hashtable<Long, Map<String, long[]>>();

		int sync_port = LoadableConfig.getInstance().getClockSynchPort();

		// if our Sync is zero then we will skip it
		if (sync_port > 0) {
			broadcast = new BroadcastManager(LoadableConfig.getInstance().getMulticastAddr(), sync_port);
			try {
				//start listening
				setupListener();
				logger.info("Synchronizer is listening.");
				//start sending
				startSending();
				logger.info("Synchronizer is sending synch pulses.");
				//display clock (optional)
				//displayClock();
			} catch (Exception e) {
				logger.error("Unable to setup Synchronizer!", e);
			}
		}
	}

	/**
	 * Returns the time corrected with long-term correction. This should be accurate over longer periods of synch but not so sensitive to recent synch changes.
	 * @return time in ms since Jan 1st 1970.
	 */
	public long stableTimeNow() {
		return System.currentTimeMillis() + stableTimeCorrection;
	}

	/**
	 * Returns the time corrected with short-term correction. This is more sensitive to recent synch changes but is not as reliable in the long-term as {@link Synchronizer#stableTimeNow()}.
	 * @return time in ms since Jan 1st 1970.
	 */
	public long correctedTimeNow() {
		return stableTimeNow() + timeCorrection;
	}

	public void displayClock() {
		Thread t = new Thread() {
			public void run() {
				while(on) {
					long timeNow = correctedTimeNow();
					long tick = timeNow / 10000;
					if(tick != lastTick && timeNow % 10000 < 4) {
						//display
						Date d = new Date(timeNow);
						// This looks like it shouldn't be logged?
						System.out.println("The time is: " + d.toString() + " (short correction = " + timeCorrection + "ms, long correction = " + stableTimeCorrection + "ms)");
						lastTick = tick;
					}
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						logger.error("Poll interval interupted for displayClock!", e);
					}
				}
			}
		};
		t.start();
	}

	private void setupListener() throws IOException {
        broadcast.addOnMessage(new BroadcastManager.OnListener(){
            @Override
            public void cb(NetworkInterface ni, OSCMessage msg, SocketAddress sender, long time) {
				if (!OSCVocabulary.match(msg, oscPath)) {
                    return;
                }
                else if(msg.getArgCount() != 5) {
                    logger.debug("Received sync message with {} args, expected 5", msg.getArgCount());
                    return;
                }
                String myMAC = Device.selectMAC(ni);
                if (logger.isTraceEnabled()) {
                    String logMessage = msg.getName() + " args:\n";
                    Object[] logArgs = new Object[msg.getArgCount() * 2];
                    for (int i = 0; i < msg.getArgCount(); i++) {
                        logMessage += "    message arg {} is a {}\n";
                        logArgs[2*i] = i;
                        logArgs[(2*i)+1] = msg.getArg(i).getClass().getName();
                    }
                    logger.trace(logMessage, logArgs);
                }
                String action           = (String) msg.getArg(0);
                String sourceMAC        = (String) msg.getArg(1);
                long timeOriginallySent = Long.parseLong((String) msg.getArg(2));
                String otherMAC         = (String) msg.getArg(3);
                long timeReturnSent     = Long.parseLong((String) msg.getArg(4));
                if(action.equals("s")) {
                    //an original send message
                    //respond if you were not the sender
                    if(!sourceMAC.equals(myMAC)) {
                        //ensure our long values are strings so we can upack them at the other side
                        // by default longs become ints when packed for OSC :( probably a comparability feature for max the ancient dinosaur.
                        broadcast.broadcast(oscPath, "r", sourceMAC, ""+timeOriginallySent, myMAC, ""+stableTimeNow());
                    }
                }
                else if(action.equals("r")) {
                    //a response message
                    //respond only if you WERE the sender
                    if(sourceMAC.equals(myMAC)) {
                        //find out how long the return trip was
                        long currentTime = stableTimeNow();
                        log(timeOriginallySent, otherMAC, timeReturnSent, currentTime);
                        if(verbose) {
                            long returnTripTime = currentTime - timeOriginallySent;
                            long timeAheadOfOther = (currentTime - (returnTripTime / 2)) - timeReturnSent;	//+ve if this unit is ahead of other unit
                            System.out.println("Return trip from " + myMAC + " to " + otherMAC + " took " + returnTripTime + "ms");
                            System.out.println("This machine (" + myMAC + ") is " + (timeAheadOfOther > 0 ? "ahead of" : "behind") + " " + otherMAC + " by " + Math.abs(timeAheadOfOther) + "ms");
                        }
                    }
                }
            }
        });
	}

	/**
	 * Cause an event to happen at the given time.
	 * @param r the {@link Runnable} to run at the given time.
	 * @param time the synchronized time to enact the event, in ms since 1st Jan 1970.
	 */
	public void doAtTime(final Runnable r, long time) {
		final long waitTime = time - correctedTimeNow();
		if(waitTime <= 0) {				//either run immediately
			r.run();
		} else {						//or wait the required time
			//create a new thread just in order to run this incoming thread
			new Thread() {
				public void run() {
					try {
						Thread.sleep(waitTime);
					} catch (InterruptedException e) {
						logger.error("Interupted while waiting to execute action in Synchronizer.doAtTime!", e);
					}
					r.run();
				}
			}.start();

		}
	}

	private void startSending() {
		Thread t = new Thread() {
			public void run() {
                BroadcastManager.OnTransmitter sync = new BroadcastManager.OnTransmitter() {
                    @Override
                    public void cb(NetworkInterface ni, OSCTransmitter transmitter) throws IOException {
                        String myMac = Device.selectMAC(ni);
                        transmitter.send(
                                new OSCMessage(
                                        oscPath,
                                        new Object[] {"s", myMac, ""+stableTimeNow(), myMac, ""+stableTimeNow() }
                                )
                        );
                    }
                };
				while(on) {
                    broadcast.forAllTransmitters(sync);
					try {
						Thread.sleep(500 + (int)(100 * Math.random()));	//randomise send time to break network send patterns
					} catch (InterruptedException e) {
						logger.error("Interupted while waiting to receive Synchronizer messages!", e);
					}
					//now that all of the responses have come back...
					calculateTimeCorrection();
					try {
						Thread.sleep(500 + (int)(100 * Math.random()));	//randomise send time to break network send patterns
					} catch (InterruptedException e) {
						logger.error("Interupted while waiting to send Synchronizer message!", e);
					}
				}
			}
		};
		t.start();
	}

	/**
	 * Shut down the Synchronizer. This is irreversible.
	 */
	public void close() {
		on = false;
		broadcast.dispose();
	}

	/**
	 * Estimates the difference between this devices clock and the clock of the "leader" device.
	 * This method modifies the value of the timeCorrection field, and less frequently updates the stableTimeCorrection field.
	 */
	private void calculateTimeCorrection() {
		for(Long sendTime : log.keySet()) {
			Map<String, long[]> responses = log.get(sendTime);
			//find the leader
			String theLeader = myMAC;
			if(timedebug) System.out.println("At send time = " + sendTime);
			for(String mac : responses.keySet()) {
				if(timedebug) System.out.println("          Response from: " + mac + " return sent: " + responses.get(mac)[0] + ", received: " + responses.get(mac)[1]);
				if(theLeader.compareTo(mac) < 0) {
					theLeader = mac;
				}
			}
			if(timedebug) System.out.println("Leader is " + theLeader);
			if(theLeader != myMAC) {
				//if you are not the leader then make a time adjustment
				long[] times = responses.get(theLeader);
				long leaderResponseTime = times[0];
				long receiveTime = times[1];
				long roundTripTime = receiveTime - sendTime;
				long messageTime = roundTripTime / 2;
				long receiveTimeAccordingToLeader = leaderResponseTime + messageTime;
				timeCorrection = receiveTimeAccordingToLeader - receiveTime;
				if(timedebug) System.out.println("time correction: " + timeCorrection + ", message time: " + messageTime + ", response sent: " + leaderResponseTime + ", response received: " + receiveTime);
			}
		}
		//finally, clear the log (for now - we might make the log last longer later)
		log.clear();
		//stability count
		if(stabilityCount++ == 20) {
			stabilityCount = 0;
			stableTimeCorrection += timeCorrection;
			timeCorrection = 0;
		}
	}

	/**
	 * Return measure of the stability of the time correction.
	 * @return near zero when very stable, higher values indicate less stability.
	 */
	public float getStability() {
		if(stableTimeCorrection == 0) {
			return 0;
		} else {
			return (float)(timeCorrection / stableTimeCorrection);
		}
	}

	/**
	 *
	 * @param timeOriginallySent
	 * @param otherMAC
	 * @param timeReturnSent
	 * @param currentTime
	 */
	private void log(long timeOriginallySent, String otherMAC, long timeReturnSent, long currentTime) {
		if(!log.containsKey(timeOriginallySent)) {
			log.put(timeOriginallySent, new Hashtable<String, long[]>());
		}
		log.get(timeOriginallySent).put(otherMAC, new long[] {timeReturnSent, currentTime});
	}

	public static void main(String[] args) {
		Synchronizer s = getInstance();
		s.displayClock();
	}

}