package com.github.wtekiela.opensub4j;

import android.util.Log;

import com.github.wtekiela.opensub4j.api.OpenSubtitlesClient;
import com.github.wtekiela.opensub4j.impl.OpenSubtitlesClientImpl;
import com.github.wtekiela.opensub4j.response.ListResponse;
import com.github.wtekiela.opensub4j.response.Response;
import com.github.wtekiela.opensub4j.response.SubtitleFile;
import com.github.wtekiela.opensub4j.response.SubtitleInfo;

import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.URL;
import java.util.List;

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

    private static final long SESSION_TIMEOUT = 15 * 60 * 1000;

    private static final String USER_AGENT = "TemporaryUserAgent";
    private static final String LANG_DEFAULT = "all";

    private String mUserName;
    private String mUserPwd;
    private String mUserAgent;
    private String mLanguages;

    private OpenSubtitlesClient mOpenSubtitlesClient;
    private long mTokenTime;

    public OnlineSubtitleManager(String userName, String pwd,
                                 String userAgent,
                                 List<String> langList) {
        mUserName = userName;
        mUserPwd = pwd;

        setUserAgent(userAgent);
        setLanguagesArray(langList);

        initConfig();
    }

    private void setUserAgent(String userAgent) {
        if (userAgent == null || userAgent.length() == 0) {
            userAgent = USER_AGENT;
        }
        mUserAgent = userAgent;
    }

    public void setLanguagesArray(List<String> lang) {
        if (lang == null || lang.size() == 0) {
            mLanguages = LANG_DEFAULT;
            return;
        }
        mLanguages = "";
        for (String l : lang) {
            mLanguages += "," + l;
        }
        mLanguages = mLanguages.substring(1);
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

        Response response = mOpenSubtitlesClient.login(mUserName,
                mUserPwd,
                /**
                 * 可设置 en
                 */
                mLanguages,
                mUserAgent);
        Log.i(TAG, "login: " + response.getStatus().toString());
    }

    public ListResponse<SubtitleInfo> searchSubtitle(String imdbId,
                                                     String query, String season, String episode) throws Exception {

        login();

        ListResponse<SubtitleInfo> response = mOpenSubtitlesClient.searchSubtitles(mLanguages,
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
