package com.duan.blog.Service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duan.blog.Mapper.TagMapper;
import com.duan.blog.Service.ITagService;
import com.duan.blog.dto.Result;
import com.duan.blog.pojo.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.duan.blog.utils.SystemConstants.HOT_TAG_LIMIT;

@Service
@Slf4j
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements ITagService {
    @Resource
    TagMapper tagMapper;
    @Override
    public List<Tag> getTagByArticleId(Long articleId) {
        return tagMapper.selectTagById(articleId);
    }

    @Override
    public Result getHotTags() {
        String idStr = StrUtil.join(",", tagMapper.getHotTagId());
        List<Tag> list = lambdaQuery()
                .select(Tag::getId,Tag::getTagName)
                .inSql(Tag::getId, idStr)
                .last("ORDER BY FIELD(id," + idStr + ")"
                        + "limit " + HOT_TAG_LIMIT)//保证查询的id的顺序与我们传入的顺序相同
                .list();
        //log.info("list: " + list);
        return Result.success(list);

    }

    @Override
    public Result getAllTags() {
        return Result.success(lambdaQuery().select(Tag::getId,Tag::getTagName).list());
    }
}
