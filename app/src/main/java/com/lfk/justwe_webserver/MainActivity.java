package com.lfk.justwe_webserver;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.lfk.justwe_webserver.WebServer.Interface.OnLogResult;
import com.lfk.justwe_webserver.WebServer.Interface.OnPostData;
import com.lfk.justwe_webserver.WebServer.Interface.OnWebFileResult;
import com.lfk.justwe_webserver.WebServer.Interface.OnWebResult;
import com.lfk.justwe_webserver.WebServer.Interface.OnWebStringResult;
import com.lfk.justwe_webserver.WebServer.WebServer;
import com.lfk.justwe_webserver.WebServer.WebServerDefault;
import com.lfk.justweengine.Utils.logger.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.bytedance.frameworks.core.encrypt.TTEncryptUtils;

public class MainActivity extends AppCompatActivity implements OnLogResult {
    private WebServer server;
    private TextView textView;
    private ScrollView scrollView;
    private boolean open = false;

    private static int count=100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Logger.init();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textView = (TextView) findViewById(R.id.main_log);
        scrollView = (ScrollView) findViewById(R.id.main_scroll);

        server = new WebServer(MainActivity.this, this);
        server.initWebService();


        server.apply("/encrypt", new OnWebStringResult() {
            @Override
            public String OnResult(HashMap<String, String> hashMap) {
                try{
                    String S = hashMap.get("data");
                    byte[] bs = compress(S);
                    String es = Base64.encodeToString( TTEncryptUtils.e(bs,bs.length),0);
                    Logger.e(es);
                    return es;
                }catch (Exception ex){
                    return "";
                }
            }
        });

        server.apply("/dencrypt", new OnWebStringResult() {
            @Override
            public String OnResult(HashMap<String, String> hashMap) {
                try{
                    String os = hashMap.get("data");
                    byte[] bs = Base64.decode(os,0);
                    byte[] dbs = TTEncryptUtils.d(bs,bs.length);
                    return uncompress(dbs);
                }catch (Exception ex){
                    return "";
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!open) {
                    server.startWebService();
                    open = true;
                } else {
                    server.stopWebService();
                    open = false;
                }
            }
        });
    }

    public static String uncompress(byte[] bs) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bs);
        GZIPInputStream gunzip = new GZIPInputStream(in);
        byte[] buffer = new byte[256];
        int n;
        while ((n = gunzip.read(buffer))>= 0) {
            out.write(buffer, 0, n);
        }
        // toString()使用平台默认编码，也可以显式的指定如toString(&quot;GBK&quot;)
        return out.toString();
    }

    public static byte[] compress(String str) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(str.getBytes());
        gzip.close();
        return out.toByteArray();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnResult(String log) {
        Log.e("log", log);
        if(count--<0){
            textView.setText("");
            count=100;
        }
        textView.append(log + "\n");
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    @Override
    public void OnError(String error) {
        Log.e("error", error);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        server.callOffWebService();
    }
}
