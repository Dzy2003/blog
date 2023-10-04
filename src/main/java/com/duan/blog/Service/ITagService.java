package com.duan.blog.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duan.blog.dto.Result;
import com.duan.blog.pojo.Tag;

import java.util.List;

public interface ITagService extends IService<Tag> {

    List<Tag> getTagByArticleId(Long articleId);

    Result getHotTags();

    Result getAllTags();

    Result getTagsDetail();

    Result getTagsDetailById(Long id);
}
