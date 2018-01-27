package co.axelrod.vkphotodownloader.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Vadim Axelrod (vadim@axelrod.co) on 27.01.2018.
 */
public class Auth {
    private final static String USER_AGENT = "Mozilla/5.0";

    // TODO Sample, rewrite
    private static String getCode(String appId) throws Exception {
        //String requestURL = "https://api.instagram.com/v1/users/search?q=" + userName + "&access_token=" + ACCESS_TOKEN;
        String requestURL = "https://oauth.vk.com/authorize?client_id=" + appId + "&display=page&redirect_uri=&scope=friends&response_type=code&v=5.71";

        URL url = new URL(requestURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = connection.getResponseCode();

        if (responseCode != 200)
            throw new Exception();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        String responseString = response.toString();

        String userId = "not found";

        return userId;
    }

}
