package com.duan.blog.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duan.blog.dto.Result;
import com.duan.blog.pojo.Tag;

import java.util.List;

public interface ITagService extends IService<Tag> {
    /**
     * 获取文章的标签列表
     * @param articleId
     * @return 标签列表
     */
    List<Tag> getTagByArticleId(Long articleId);

    /**
     * 获取最火标签
     * @return 最火标签
     */
    Result getHotTags();
    /**
     * 获取所有标签
     * @return 标签列表
     */
    Result   getAllTags();
    /**
     * 获取标签详情
     * @return 标签详情
     */
    Result getTagsDetail();

    /**
     * 根据id获取标签详情
     * @param id 标签id
     * @return 标签详情
     */
    Result getTagsDetailById(Long id);
}
