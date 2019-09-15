package fr.vcapi.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import fr.vcapi.management.DataClient;
import fr.vcapi.packets.Packet;

public abstract class NetworkUtilities extends Thread {

	protected ArrayList<DataClient> clients = new ArrayList<DataClient>();
	protected int packetSize = 704;
	protected DatagramSocket socket;

	public static final int BLOCK_SIZE = 512, SAMPLE_RATE = 44100, SAMPLE_SIZE = 16, CHANNELS = 1;
	
	protected static int MESSAGE_SERVER_PORT = 1331, VOICE_SERVER_PORT = 1329;

	public static long deadTime = (long) (BLOCK_SIZE / (SAMPLE_RATE * (SAMPLE_SIZE / 8F)) * 1000F);
	
	/**
	 * Sends a packet to all of the registered clients
	 * 
	 * @param packet
	 */
	public void sendObjectToAll(Packet packet) {
		for (DataClient client : clients) {
			sendObject(packet, client.getIP(), client.getPort());
		}
	}

	/**
	 * Sends a packet to a specified user
	 * 
	 * @param packet
	 * @param ip
	 * @param port
	 */
	public void sendObject(Packet packet, InetAddress ip, int port) {
		try {
			ByteArrayOutputStream bStream = new ByteArrayOutputStream();
			ObjectOutput objectOutput = new ObjectOutputStream(bStream);
			objectOutput.writeObject(packet);
			objectOutput.close();
			byte[] serializedMessage = bStream.toByteArray();
			sendData(serializedMessage, ip, port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	};

	/**
	 * Sends raw data to a specified user, might throw an exception receiving this
	 * 
	 * @param data
	 * @param ip
	 * @param port
	 */
	@Deprecated
	protected void sendData(byte[] data, InetAddress ip, int port) {
		if (data.length > packetSize) throw new ArrayIndexOutOfBoundsException("data.length :" + data.length + " > packetBufferLength");
		DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
		try {
			this.socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	};

	/**
	 * Logs any object in a console with a timestamp and the class which used the
	 * method
	 * 
	 * @param o
	 */
	public void log(Object o) {
		Date date = new Date();
		String timeStamp = new SimpleDateFormat("HH:mm:ss").format(date);
		System.out.println("[" + timeStamp + "] [" + getClass().getName() + "] " + o.toString());
	}

	
	
	/**
	 * @return The arraylist containing the clients
	 */
	public ArrayList<DataClient> getClients() {
		return this.clients;
	}

	/**
	 * Adds a client to the arraylsit containing the clients
	 * 
	 * @param client
	 */
	public void addClient(DataClient client) {
		this.clients.add(client);
	}

	/**
	 * Gets a DataClient using it's UUID
	 * 
	 * @param uuid
	 * @return DataClient
	 */
	public DataClient getClientByUUID(UUID uuid) {
		//tu devrais trier tes clients suivant l'uuid, comme ça tu pourrais plus rapidement les retrouver par dichotomie
		for (DataClient client : this.clients) {
			if (client.getUUID().equals(uuid)) {
				return client;
			}
		}
		return null;
	}

	/**
	 * Checks if a client exists
	 * 
	 * @param uuid
	 * @return
	 */
	public boolean clientExists(UUID uuid) {
		return getClientByUUID(uuid) != null;
	}

	
	
	/**
	 * Checks if a client exists
	 * 
	 * @param ctx
	 * @return
	 */
	public boolean clientExists(Context ctx) {
		return getClientUsingContext(ctx) != null;
	}

	
	
	/**
	 * Gets a client using Context and not UUID
	 * 
	 * @param ctx
	 * @return
	 */
	public DataClient getClientUsingContext(Context ctx) {
		for (DataClient client : this.clients) {
			if (client.getIP().equals(ctx.getIP()) && client.getPort() == ctx.getPort()) {
				return client;
			}
		}
		return null;
	}
	
	public void removeClientByUUID(UUID uuid) {
		clients.remove(getClientByUUID(uuid));
	}
}
