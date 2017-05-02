package com.lfk.justwe_webserver.WebServer;

import com.lfk.justwe_webserver.WebServer.Interface.OnPostData;
import com.lfk.justwe_webserver.WebServer.Interface.OnWebFileResult;
import com.lfk.justwe_webserver.WebServer.Interface.OnWebResult;
import com.lfk.justwe_webserver.WebServer.Interface.OnWebStringResult;
import com.lfk.justweengine.Utils.logger.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Solve request
 *
 * @author liufengkai
 *         Created by liufengkai on 16/1/14.
 */
public class RequestSolve extends Thread {
    private Socket client;
    private BufferedReader clientReader;
    // url in link
    private String url;
    private HashMap<String, String> params;
//    private String urlName = "";
    private int type;

    public RequestSolve(Socket s) {
        super();
        this.client = s;

    }

    @Override
    public void run() {
        super.run();
        try {
            clientReader = new BufferedReader(
                    new InputStreamReader(client.getInputStream(), "UTF-8"));

            while (true) {
                String s = clientReader.readLine().trim();

                if (s.equals("")) {
                    break;
                }

//                Logger.e(s + '\n');
                Servers.getLogResult().OnResult(s);

                int httpHeader = s.indexOf(" HTTP/");
                switch (s.substring(0, 4)) {
                    case "GET ":
                        // get request link
                        int last0 = s.indexOf('?');
                        params = new HashMap<>();
                        if (last0 > 0) {
                            url = s.substring(4, last0);
                            getParams(s.substring(last0 + 1, httpHeader));
                        } else
                            url = s.substring(4, httpHeader);
                        break;
                    case "POST":
                        int last = s.indexOf('?');
                        params = new HashMap<>();
                        if (last > 0) {
                            url = s.substring(5, last);
                            getParams(s.substring(last + 1, httpHeader));
                        } else
                            url = s.substring(5, httpHeader);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Servers.getLogResult().OnError(e.getMessage());
        }

        exeResult(url);
    }

    private void exeResult(String Url) {
        OnWebResult result = WebServer.getRule(Url);
        if (result != null) {
            if (result instanceof OnWebStringResult) {
                returnString(((OnWebStringResult) result).OnResult(params));
            } else if (result instanceof OnWebFileResult) {
                returnFile(((OnWebFileResult) result).returnFile());
            } else if (result instanceof OnPostData) {
                returnString(((OnPostData) result).OnPostData(params));
            }
        } else {
            // 在新的权限管理之前,先允许所有服务器根目录下的文件访问
            File file = new File(WebServerDefault.WebServerFiles + Url);
            if (file.exists()) {
                returnFile(file);
            }
        }
    }

//    private void findChildFile(String Url) {
//        int index = Url.lastIndexOf('/');
//        if (index != -1) {
//            Log.d("index", Url.lastIndexOf('/') + "");
//            // 仍能逐层递减
//            if (index == 0) {
//                exeResult("/");
//                urlName = "/" + urlName;
//                return;
//            }
//
//            exeResult(Url.substring(0, index));
//            urlName = Url.substring(index - 1) + urlName;
//        }
//        Logger.e("Url:" + Url.substring(0, Url.lastIndexOf('/')));
//        Logger.e("UrlName:" + Url.substring(Url.lastIndexOf('/') + 1));
//    }

    private void returnString(String str) {
        try {
            OutputStream o = client.getOutputStream();
            o.write(setType(getHeaderBase(), str.length(), "200 OK").getBytes());
            o.write(str.getBytes());
            o.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void returnFile(File file) {
        if (file.exists()) {
            try {
                BufferedInputStream inputStream =
                        new BufferedInputStream(
                                new FileInputStream(file));
                BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
                ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int count;
                while ((count = inputStream.read(buf)) != -1) {
                    tempOut.write(buf, 0, count);
                }
                tempOut.flush();
                out.write(setType(getHeaderBase(), tempOut.size(), "200 OK").getBytes());
                out.write(tempOut.toByteArray());
                out.flush();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void getParams(String url) throws UnsupportedEncodingException {
        int valueFirst;
        for (String s : url.split("&")) {
            valueFirst = s.indexOf("=");
            params.put(s.substring(0, valueFirst), URLDecoder.decode(s.substring(valueFirst + 1),"UTF-8"));
        }
    }

    private String getHeaderBase() {
        return "HTTP/1.1 %code%\n" +
                "Server: JustWe_WebServer/0.1\n" +
                "Content-Length: %length%\n" +
                "Connection: close\n" +
                "Content-Type: text/html; charset=iso-8859-1\n\n";
    }

    private String setType(String str, int length, String TYPE) {
        str = str.replace("%code%", TYPE);
        str = str.replace("%length%", "" + length);
        return str;
    }
}
