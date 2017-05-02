package com.lfk.justwe_webserver.WebServer.Interface;

import java.util.HashMap;

/**
 * Created by liufengkai on 16/1/14.
 */
public interface OnWebStringResult extends OnWebResult {
    String OnResult(HashMap<String, String> hashMap);
}
