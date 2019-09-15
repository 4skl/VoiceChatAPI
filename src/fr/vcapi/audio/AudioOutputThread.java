package fr.vcapi.audio;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import fr.vcapi.network.Client;
import fr.vcapi.network.NetworkUtilities;
import fr.vcapi.packets.VoicePacket;

public class AudioOutputThread extends Thread {

	private static final int THRESHOLD = 200;
	
	private Queue<VoicePacket> queue = new LinkedList<VoicePacket>();
	private boolean running;
	//Use variables here it's better than the value
	private static AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
	private static DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);
	private SourceDataLine sourceLine;
	
	public AudioOutputThread() {
		this.running = true;
		try {
			this.sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
			this.sourceLine.open(format);
			this.sourceLine.start();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		while(running) {
			synchronized (this) {
				if (queue.size() > THRESHOLD) {
					queue.clear();
				}
			}
			synchronized (this) {
				VoicePacket packet;
				if ((packet = queue.poll()) != null) {
					this.sourceLine.write(packet.getData(), 0, packet.getLength());
				}
			}
			
			try {
				TimeUnit.MILLISECONDS.sleep(NetworkUtilities.deadTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void addToQueue(VoicePacket voicePacket) {
		this.queue.add(voicePacket);
	}
	
}
