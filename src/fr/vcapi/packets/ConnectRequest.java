package fr.vcapi.packets;

import java.util.UUID;

import fr.vcapi.management.DataClient;
import fr.vcapi.network.Context;
import fr.vcapi.network.MessageServer;
import fr.vcapi.network.NetworkUtilities;

public class ConnectRequest implements Packet {

	private static final long serialVersionUID = -4213027016689187881L;

	UUID uuid;

	public ConnectRequest(UUID uuid) {
		this.uuid = uuid;
	}

	public UUID getUUID() {
		return uuid;
	}

	/**
	 * Method only used by the <a>MessageServer</a> when the <a>Client</a> sends a
	 * <a>ConnectAnswer</a> It bounds him to a UUID which will allow him to sign his
	 * later messages.
	 * 
	 * @param ctx   Context of the call
	 * @param nUtil Context caller
	 * 
	 * @return void
	 */

	@Override
	public void parsePacket(Context ctx, NetworkUtilities nUtil) {
		MessageServer server = (MessageServer) nUtil;

		/*
		 * /!\ IMPORTANT /!\ It is important to verify if the client exists through the
		 * Context and not it's UUID Since the server doesn't manage UUIDs creation, the
		 * client can change it's UUID on each connections therefore attempt a double
		 * connection by sending the connection packet twice.
		 */
		if (!server.clientExists(ctx)) {
			server.addClient(new DataClient(uuid, ctx));
			server.sendObjectToAll(new AddClient(uuid));
			server.log("New client connected successfully");
		} else {
			server.log("Client " + ctx.toString() + " attempted a double connection");
		}
	}

}
