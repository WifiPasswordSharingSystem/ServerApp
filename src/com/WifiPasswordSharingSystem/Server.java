package com.WifiPasswordSharingSystem;

import javafx.util.Pair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable {
    static List<Socket> clients;
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
                String command = new String(data).trim();
                switch (command){
                    //Обработка запросов клиента
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        }
    }


    @Override
    public void run() {
        JSONParser parser = new JSONParser();
        clients = new ArrayList<>();
        try {
            Object obj = parser.parse(new FileReader("settings.json"));
            JSONObject jsonObject = (JSONObject) obj;
            System.out.println(jsonObject);
            long port = (long)jsonObject.get("Port");
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
