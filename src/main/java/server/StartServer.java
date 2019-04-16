package server;

import server.HttpMythDispatcher;
import server.MythServer;
import server.Repository;

public class StartServer {


    public static void main(String[] args) throws Exception {
        new MythServer().start(args);
    }
}
