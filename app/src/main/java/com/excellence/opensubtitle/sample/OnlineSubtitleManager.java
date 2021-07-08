package com.excellence.opensubtitle.sample;

import android.util.Log;

import com.github.wtekiela.opensub4j.api.OpenSubtitlesClient;
import com.github.wtekiela.opensub4j.impl.OpenSubtitlesClientImpl;
import com.github.wtekiela.opensub4j.response.ListResponse;
import com.github.wtekiela.opensub4j.response.Response;
import com.github.wtekiela.opensub4j.response.SubtitleFile;
import com.github.wtekiela.opensub4j.response.SubtitleInfo;

import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.URL;
import java.util.Locale;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2021/5/6
 *     desc   :
 * </pre> 
 */
public class OnlineSubtitleManager {

    private static final String TAG = OnlineSubtitleManager.class.getSimpleName();

    private static OnlineSubtitleManager INSTANCE = new OnlineSubtitleManager();

    private static final long SESSION_TIMEOUT = 15 * 60 * 1000;

    private static final String USER_NAME = "";
    private static final String USER_PWD = "";
    private static final String USER_AGENT = "TemporaryUserAgent";

    private OpenSubtitlesClient mOpenSubtitlesClient;
    private long mTokenTime;

    public static OnlineSubtitleManager getInstance() {
        return INSTANCE;
    }

    private OnlineSubtitleManager() {
        initConfig();
    }

    private void initConfig() {
        try {
            if (mOpenSubtitlesClient != null) {
                return;
            }

            URL serverUrl = new URL("https", "api.opensubtitles.org", 443, "/xml-rpc");
            XmlRpcClientConfigImpl xmlRpcClientConfig = new XmlRpcClientConfigImpl();
            xmlRpcClientConfig.setServerURL(serverUrl);
            xmlRpcClientConfig.setEnabledForExtensions(true);
            xmlRpcClientConfig.setGzipCompressing(false);
            xmlRpcClientConfig.setGzipRequesting(false);

            mOpenSubtitlesClient = new OpenSubtitlesClientImpl(xmlRpcClientConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void login() throws Exception {
        if (mOpenSubtitlesClient.isLoggedIn()) {
            if (System.currentTimeMillis() - mTokenTime > SESSION_TIMEOUT) {
                /**
                 * 保活，重新登陆
                 */
                mTokenTime = System.currentTimeMillis();
                mOpenSubtitlesClient.logout();
            } else {
                return;
            }
        }

        mTokenTime = System.currentTimeMillis();

        Response response = mOpenSubtitlesClient.login(USER_NAME,
                USER_PWD,
                /**
                 * 可设置 en
                 */
                Locale.getDefault().getLanguage(),
                USER_AGENT);
        Log.i(TAG, "login: " + response.getStatus().toString());
    }

    public ListResponse<SubtitleInfo> searchSubtitle(String imdbId,
                                                     String query, String season, String episode) throws Exception {

        login();

        ListResponse<SubtitleInfo> response = mOpenSubtitlesClient.searchSubtitles(Locale.getDefault().getLanguage(),
                null, null,
                imdbId,
                query, season, episode, null);
        Log.i(TAG, "searchSubtitle: " + response.getStatus().toString());
        return response;
    }

    public ListResponse<SubtitleFile> downloadSubtitle(int subtitleFileId) throws Exception {
        login();

        ListResponse<SubtitleFile> response = mOpenSubtitlesClient.downloadSubtitles(subtitleFileId);
        Log.i(TAG, "downloadSubtitle: " + response.getStatus().toString());
        return response;
    }

}
