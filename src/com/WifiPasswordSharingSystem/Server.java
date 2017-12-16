package com.WifiPasswordSharingSystem;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;

public class Server implements Runnable {
    static List<Socket> clients;
    private JSONArray collection;
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
                boolean status = false;
                JSONObject send = null;
                while (true){
                    byte[] data = new byte[512];
                    is.read(data);
                    JSONParser parser = new JSONParser();
                    Object obj = parser.parse(new String(data).trim());
                    JSONObject jsonObject = (JSONObject) obj;
                    String command = (String) jsonObject.get("Command");
                    switch (command){
                        case "ADD":
                            JSONObject object = new JSONObject();
                            object.put("SSID", (String)jsonObject.get("SSID"));
                            object.put("PASSWORD", (String)jsonObject.get("PASSWORD"));
                            collection.add(object);
                            writeToFile();
                           break;
                        case "GET":
                            String ssid = (String)jsonObject.get("SSID");
                            for(Object tmpObject : collection){
                                JSONObject jsonItem = (JSONObject) tmpObject;
                                if(jsonItem.get("SSID").equals(ssid)){
                                    status = true;
                                    String pwd = (String) jsonItem.get("PASSWORD");
                                    send = new JSONObject();
                                    send.put("Command", "ACCEPT");
                                    send.put("SSID", ssid);
                                    send.put("PASSWORD", pwd);
                                    break;
                                }
                            }
                            if(status){
                                send.put("STATUS", "Done");
                                status = false;
                            }else{
                                send.put("STATUS", "Fail");
                            }
                            os.write(send.toJSONString().getBytes());
                            os.flush();
                            break;
                    }
                }
            }catch (Exception e){
                System.err.println(e.getMessage());
                clients.remove(sock);
            }
        }
    }

    void writeToFile(){
        JSONObject obj = new JSONObject();
        try(FileWriter file = new FileWriter("settings.json")){
            obj.put("Port", 2222);
            obj.put("Collection", collection);
            file.write(obj.toJSONString());
            file.flush();
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void run() {
        JSONParser parser = new JSONParser();
        clients = new ArrayList<>();
        try {
            Object obj = parser.parse(new FileReader("settings.json"));
            JSONObject jsonObject = (JSONObject) obj;
            long port = (long)jsonObject.get("Port");
            Object js = jsonObject.get("Collection");
            if(js != null)
                collection = (JSONArray) js;
            else
                collection = new JSONArray();
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
