package cn.booslink.llm.repository;

import java.util.List;

import cn.booslink.llm.common.model.GuideItem;
import io.reactivex.rxjava3.core.Single;

public interface IGuideRepository {
    Single<List<GuideItem>> getGuideItems();
}
