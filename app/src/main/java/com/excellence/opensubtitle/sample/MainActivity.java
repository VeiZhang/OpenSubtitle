package com.excellence.opensubtitle.sample;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.github.wtekiela.opensub4j.OnlineSubtitleManager;
import com.github.wtekiela.opensub4j.response.ListResponse;
import com.github.wtekiela.opensub4j.response.ResponseStatus;
import com.github.wtekiela.opensub4j.response.SubtitleFile;
import com.github.wtekiela.opensub4j.response.SubtitleInfo;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String USER_NAME = "";
    private static final String USER_PWD = "";

    private EditText mEditText = null;
    private Button mButton = null;
    private ListView mListView = null;

    private OnlineSubtitleManager mOnlineSubtitleManager = null;
    private final List<SubtitleInfo> mSubtitleInfoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mOnlineSubtitleManager = new OnlineSubtitleManager(USER_NAME, USER_PWD,
                null, null);

        mEditText = findViewById(R.id.edit_text);
        mButton = findViewById(R.id.search_btn);
        mListView = findViewById(R.id.list_view);

        mButton.setOnClickListener(v -> searchOpenSubtitles(mEditText.getText().toString()));
        mListView.setOnItemClickListener((parent, view, position, id) -> downloadSubtitle(position));
    }

    private void downloadSubtitle(int position) {
        if (mSubtitleInfoList.size() == 0) {
            return;
        }

        new Thread(() -> {
            try {
                /**
                 * 下载
                 */
                SubtitleInfo subtitleInfo = mSubtitleInfoList.get(position);

                Log.d(TAG, "download link: " + subtitleInfo.getDownloadLink());

                ListResponse<SubtitleFile> downloadResponse = mOnlineSubtitleManager
                        .downloadSubtitle(subtitleInfo.getSubtitleFileId());
                if (downloadResponse.getData().isPresent()) {
                    for (SubtitleFile item : downloadResponse.getData().get()) {
                        String content = item.getContent(item.getContent().getCharsetName()).getContent();
                        Log.d(TAG, item.getId() + ":");
                        Log.i(TAG, content);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void searchOpenSubtitles(String search) {
        mSubtitleInfoList.clear();
        if (search == null || search.length() == 0) {
            return;
        }

        new Thread(() -> {
            try {
                ListResponse<SubtitleInfo> searchResponse = mOnlineSubtitleManager
                        .searchSubtitle("", search, "", "");
                if (ResponseStatus.OK.equals(searchResponse.getStatus())) {
                    List<SubtitleInfo> list = searchResponse.getData().or(new ArrayList<>());
                    for (SubtitleInfo item : list) {
                        Log.d(TAG, "search subtitle: " + item.getFileName());
                    }

                    showSubtitleList(list);
                } else {
                    Log.e(TAG, "search error: " + searchResponse.getStatus().getCode());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showSubtitleList(List<SubtitleInfo> list) {
        mSubtitleInfoList.addAll(list);

        mListView.post(() -> {
            if (list == null || list.size() == 0) {
                Toast.makeText(MainActivity.this, "Empty subtitle", Toast.LENGTH_SHORT).show();
            } else {
                List<String> nameList = new ArrayList<>();
                for (SubtitleInfo item : list) {
                    nameList.add(item.getFileName());
                }

                mListView.setAdapter(
                        new ArrayAdapter<>(MainActivity.this,
                                android.R.layout.simple_list_item_1,
                                nameList));
            }
        });
    }
}