package com.duan.blog.Controller;

import com.duan.blog.Service.IArticleService;
import com.duan.blog.dto.PageInfo;
import com.duan.blog.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/articles")
public class ArticleController {
    public IArticleService articleService;
    @Autowired
    public ArticleController(IArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping("")
    public Result listArticles(@RequestBody PageInfo pageInfo){
        return articleService.listArticles(pageInfo);
    }

    @PostMapping("/hot")
    public Result listHotArticles(){
        return articleService.getHotArticles();
    }

    @PostMapping("/new")
    public Result listNewArticles(){
        return articleService.getNewArticles();
    }

    @PostMapping("/listArchives")
    public Result listArchives(){
        return articleService.getArchives();
    }

}
