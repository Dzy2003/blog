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
    @GetMapping("/hot")
    public Result listHotTags(){
        return tagService.getHotTags();
    }

    @GetMapping()
    public Result listAllTags(){
        return tagService.getAllTags();
    }

    @GetMapping("/detail")
    public Result listTagsDetail(){
        return tagService.getTagsDetail();
    }

    @GetMapping("/detail/{id}")
    public Result TagDetail(@PathVariable Long id){
        return tagService.getTagsDetailById(id);
    }



}
