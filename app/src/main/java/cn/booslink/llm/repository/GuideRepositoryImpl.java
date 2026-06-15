package cn.booslink.llm.repository;

import android.content.Context;
import com.google.gson.Gson;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

import cn.booslink.llm.common.model.GuideData;
import cn.booslink.llm.common.model.GuideItem;
import cn.booslink.llm.common.utils.FileUtils;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class GuideRepositoryImpl implements IGuideRepository {

    private final Context mContext;
    private final Gson mGson;

    @Inject
    public GuideRepositoryImpl(@ApplicationContext Context context, Gson gson) {
        this.mContext = context;
        this.mGson = gson;
    }

    @Override
    public Single<List<GuideItem>> getGuideItems() {
        return Single.fromCallable(() -> {
            String json = FileUtils.readJsonFromAsset(mContext, "app_guide.json");
            if (json == null) {
                throw new RuntimeException("Failed to read app_guide.json from asset");
            }
            GuideData guideData = mGson.fromJson(json, GuideData.class);
            if (guideData == null || guideData.getData() == null) {
                throw new RuntimeException("Failed to parse guide data");
            }
            return guideData.getData();
        });
    }
}
