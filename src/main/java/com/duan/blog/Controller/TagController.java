package com.duan.blog.Controller;

import com.duan.blog.Service.ITagService;
import com.duan.blog.dto.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tags")
public class TagController {
    @Resource
    ITagService tagService;
    @GetMapping("/hot")
    public Result listHotTags(){
        return tagService.getHotTags();
    }
}
