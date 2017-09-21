package com.WifiPasswordSharingSystem;

import javafx.util.Pair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;

public class Server implements Runnable {
    static List<Socket> clients;
    private MongoCollection<org.bson.Document> collection;
    public class Handler implements Runnable {
        Socket sock;
        InputStream is;
        OutputStream os;
        public Handler(Socket sock) throws IOException {
            this.sock = sock;
            is = sock.getInputStream();
            os = sock.getOutputStream();
        }

        @Override
        public void run() {
            try {
                while (true){
                    byte[] data = new byte[512];
                    is.read(data);
                    JSONParser parser = new JSONParser();
                    Object obj = parser.parse(new String(data).trim());
                    JSONObject jsonObject = (JSONObject) obj;
                    String command = (String) jsonObject.get("Command");
                    switch (command){
                        case "ADD":
                            Map<String,Object> hMap = new HashMap();
                            hMap.put("SSID", (String)jsonObject.get("SSID"));
                            hMap.put("PASSWORD", (String)jsonObject.get("PASSWORD"));
                            collection.insertOne(new Document(hMap));
                           break;
                        case "GET":
                            
                            break;
                    }
                }
            }catch (Exception e){
                System.err.println(e.getMessage());
                clients.remove(sock);
            }
        }
    }


    @Override
    public void run() {
        JSONParser parser = new JSONParser();
        clients = new ArrayList<>();
        try {
            MongoClient client = new MongoClient();
            MongoDatabase db = client.getDatabase("wifi");
            Object obj = parser.parse(new FileReader("settings.json"));
            JSONObject jsonObject = (JSONObject) obj;
            long port = (long)jsonObject.get("Port");
            String collectString = (String)jsonObject.get("Collection");
            collection = db.getCollection(collectString);
            System.out.println(Inet4Address.getLocalHost().getHostAddress());
            System.out.println(port);
            ServerSocket serverSocket = new ServerSocket(new Long(port).intValue());
            while (true){
                Socket accept = serverSocket.accept();
                clients.add(accept);
                new Thread(new Handler(accept)).start();
            }
        } catch (Exception e) {
            JSONObject obj = new JSONObject();
            obj.put("Port", 2222);
            try (FileWriter file = new FileWriter("settings.json")) {
                file.write(obj.toJSONString());
                file.flush();
            } catch (IOException ex) {
                System.err.println("Ошибка перезаписи файла настроек");
                e.printStackTrace();
            }
        }
    }
}
