package com.excellence.opensubtitle;

import android.os.Bundle;

import com.github.wtekiela.opensub4j.impl.OpenSubtitlesClientImpl;
import com.github.wtekiela.opensub4j.response.ListResponse;
import com.github.wtekiela.opensub4j.response.MovieInfo;
import com.github.wtekiela.opensub4j.response.Response;

import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.URL;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testForOpenSubtitle();
    }

    private void testForOpenSubtitle() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL serverUrl = new URL("https", "api.opensubtitles.org", 443, "/xml-rpc");

                    XmlRpcClientConfigImpl xmlRpcClientConfig = new XmlRpcClientConfigImpl();
                    xmlRpcClientConfig.setServerURL(serverUrl);
                    xmlRpcClientConfig.setEnabledForExtensions(true);
                    xmlRpcClientConfig.setGzipCompressing(false);
                    xmlRpcClientConfig.setGzipRequesting(false);

                    OpenSubtitlesClientImpl mOpenSubtitlesClient = new OpenSubtitlesClientImpl(xmlRpcClientConfig);

                    Response loginResponse = mOpenSubtitlesClient.login("User",
                            "Pwd", "en",
                            "TemporaryUserAgent");
                    System.out.println(loginResponse.getStatus().toString());

                    ListResponse<MovieInfo> response = mOpenSubtitlesClient.searchMoviesOnImdb("The avengers");
                    System.out.println(response.getStatus().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}