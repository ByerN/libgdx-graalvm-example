package com.byern.tanima;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KryonetClientServer {

  public static class ClientServerConfig {
    private final Integer listeningPort;

    public Boolean isMaster() {
      return master;
    }

    private Boolean master;

    public ClientServerConfig(Integer listeningPort) {
      this.listeningPort = listeningPort;
    }
  }

  public Server server;
  public Client client;
  public final ClientServerConfig config;
  private List<Connection> connectionList = new ArrayList<>();
  private long lastTimeConnected = 0L;

  public KryonetClientServer(ClientServerConfig config) throws IOException, InterruptedException {
    this.config = config;
    client = new Client();
    //client.getKryo().register(BallPainter.BallMessage.class);
    try {
      client.start();
      client.connect(5000, "127.0.0.1", config.listeningPort);
      this.config.master = false;
    } catch (Exception e) {
      System.out.println("Cannot connect to the server: 127.0.0.1:" + config.listeningPort);
      System.out.println("Creating server instead");
      this.config.master = true;
      server = new Server();
      server.start();
      server.bind(config.listeningPort);
      server.addListener(new Listener() {
        @Override
        public void connected(Connection connection) {
          System.out.println("Connected: " + connection.getRemoteAddressTCP().getPort());
          connectionList.add(connection);
          lastTimeConnected = System.currentTimeMillis();
        }

        @Override
        public void disconnected(Connection connection) {
          System.out.println("Disconnected: " + connection.getRemoteAddressTCP().getPort());
        }

        public void received(Connection connection, Object object) {
          System.out.println("Received " + object);
        }
      });
    }

  }

  public boolean canSend() {
    return (connectionList.size() > 0 && System.currentTimeMillis() - lastTimeConnected > 5000) || !config.isMaster();
  }

  public void send(Object msg) {
    if (config.isMaster()) {
      System.out.println("Sending " + msg + " as server");
      connectionList.forEach(c -> c.sendTCP(msg));
    } else {
      System.out.println("Sending " + msg + " as client");
      client.sendTCP(msg);
    }
  }

  public void dispose() {
    try {
      client.close();
      server.close();
      client.dispose();
      server.dispose();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


}
