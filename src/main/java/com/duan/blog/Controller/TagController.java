package com.duan.blog.Controller;

import com.duan.blog.Service.ITagService;
import com.duan.blog.dto.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tags")
public class TagController {
    @Resource
    ITagService tagService;

    /**
     * 获取最火标签
     * @return 最火标签
     */
    @GetMapping("/hot")
    public Result listHotTags(){
        return tagService.getHotTags();
    }

    /**
     * 获取所有标签
     * @return 所有标签
     */
    @GetMapping()
    public Result listAllTags(){
        return tagService.getAllTags();
    }
    /**
     * 获取标签详情
     * @return 标签详情
     */
    @GetMapping("/detail")
    public Result listTagsDetail(){
        return tagService.getTagsDetail();
    }

    /**
     * 获取标签详情
     * @param id 标签id
     * @return 标签详情
     */
    @GetMapping("/detail/{id}")
    public Result TagDetail(@PathVariable Long id){
        return tagService.getTagsDetailById(id);
    }



}
