package org.cnodejs.android.md.presenter.implement;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.cnodejs.android.md.R;
import org.cnodejs.android.md.model.api.ApiClient;
import org.cnodejs.android.md.model.api.DefaultCallback;
import org.cnodejs.android.md.model.entity.Author;
import org.cnodejs.android.md.model.entity.Reply;
import org.cnodejs.android.md.model.entity.Result;
import org.cnodejs.android.md.model.storage.LoginShared;
import org.cnodejs.android.md.model.storage.SettingShared;
import org.cnodejs.android.md.presenter.contract.ITopicReplyPresenter;
import org.cnodejs.android.md.ui.view.ITopicReplyView;
import org.joda.time.DateTime;

import java.util.ArrayList;

import retrofit2.Response;

public class TopicReplyPresenter implements ITopicReplyPresenter {

    private final Activity activity;
    private final ITopicReplyView topicReplyView;

    public TopicReplyPresenter(@NonNull Activity activity, @NonNull ITopicReplyView topicReplyView) {
        this.activity = activity;
        this.topicReplyView = topicReplyView;
    }

    @Override
    public void replyTopicAsyncTask(@NonNull String topicId, String content, final String targetId) {
        if (TextUtils.isEmpty(content)) {
            topicReplyView.onContentError(activity.getString(R.string.content_empty_error_tip));
        } else {
            final String finalContent;
            if (SettingShared.isEnableTopicSign(activity)) { // 添加小尾巴
                finalContent = content + "\n\n" + SettingShared.getTopicSignContent(activity);
            } else {
                finalContent = content;
            }
            topicReplyView.onReplyTopicStart();
            ApiClient.service.replyTopic(topicId, LoginShared.getAccessToken(activity), finalContent, targetId).enqueue(new DefaultCallback<Result.ReplyTopic>(activity) {

                @Override
                public boolean onResultOk(Response<Result.ReplyTopic> response, Result.ReplyTopic result) {
                    Reply reply = new Reply();
                    reply.setId(result.getReplyId());
                    Author author = new Author();
                    author.setLoginName(LoginShared.getLoginName(getActivity()));
                    author.setAvatarUrl(LoginShared.getAvatarUrl(getActivity()));
                    reply.setAuthor(author);
                    reply.setContentFromLocal(finalContent); // 这里要使用本地的访问器
                    reply.setCreateAt(new DateTime());
                    reply.setUpList(new ArrayList<String>());
                    reply.setReplyId(targetId);
                    topicReplyView.onReplyTopicOk(reply);
                    return false;
                }

                @Override
                public void onFinish() {
                    topicReplyView.onReplyTopicFinish();
                }

            });
        }
    }

}
