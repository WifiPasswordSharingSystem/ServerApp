package com.WifiPasswordSharingSystem;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;

public class Server implements Runnable {


    @Override
    public void run() {
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("settings.json"));
            JSONObject jsonObject = (JSONObject) obj;
            System.out.println(jsonObject);
            long port = (long)jsonObject.get("Port");
            System.out.println(port);
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
