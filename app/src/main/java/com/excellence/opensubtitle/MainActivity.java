package com.excellence.opensubtitle;

import android.os.Bundle;
import android.util.Log;

import com.github.wtekiela.opensub4j.response.ListResponse;
import com.github.wtekiela.opensub4j.response.ResponseStatus;
import com.github.wtekiela.opensub4j.response.SubtitleFile;
import com.github.wtekiela.opensub4j.response.SubtitleInfo;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testForOpenSubtitle();
    }

    private void testForOpenSubtitle() {
        new Thread(() -> {
            try {
                ListResponse<SubtitleInfo> searchResponse = OnlineSubtitleManager.getInstance()
                        .searchSubtitle("", "The avengers", "", "");
                if (ResponseStatus.OK.equals(searchResponse.getStatus())) {
                    if (searchResponse.getData().isPresent()) {

                        List<SubtitleInfo> list = searchResponse.getData().get();
                        for (SubtitleInfo item : list) {
                            Log.d(TAG, "search subtitle: " + item.getFileName());
                        }

                        if (list.size() > 0) {
                            /**
                             * 下载
                             */
                            SubtitleInfo subtitleInfo = list.get(0);

                            Log.d(TAG, "download link: " + subtitleInfo.getDownloadLink());

                            ListResponse<SubtitleFile> downloadResponse = OnlineSubtitleManager.getInstance()
                                    .downloadSubtitle(subtitleInfo.getSubtitleFileId());
                            if (downloadResponse.getData().isPresent()) {
                                for (SubtitleFile item : downloadResponse.getData().get()) {
                                    String content = item.getContent(item.getContent().getCharsetName()).getContent();
                                    Log.d(TAG, item.getId() + ":");
                                    Log.i(TAG, content);
                                }
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "search error: " + searchResponse.getStatus().getCode());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}