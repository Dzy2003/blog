package com.duan.blog.Service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duan.blog.Mapper.CommentsMapper;
import com.duan.blog.Service.IArticleService;
import com.duan.blog.Service.ICommentsService;
import com.duan.blog.Service.ISysUserService;
import com.duan.blog.dto.CommentInfo;
import com.duan.blog.dto.Result;
import com.duan.blog.dto.UserDTO;
import com.duan.blog.pojo.Article;
import com.duan.blog.pojo.Comment;
import com.duan.blog.pojo.SysUser;
import com.duan.blog.utils.UserHolder;
import com.duan.blog.vo.ArticleVo;
import com.duan.blog.vo.CommentVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 白日
 * @date Created in 2023/10/1 10:25
 */
@Service
public class CommentsServiceImpl extends ServiceImpl<CommentsMapper, Comment> implements ICommentsService {
    @Resource
    ISysUserService userService;
    @Resource
    IArticleService articleService;

    @Override
    public Result getCommentsByArticleId(Long id) {
        Map<Integer, List<Comment>> comments = lambdaQuery()
                .eq(Comment::getArticleId, id)
                .list()
                .stream()
                .collect(Collectors.groupingBy(Comment::getLevel));
        List<Comment> parents = comments.get(1);
        List<Comment> children = comments.get(2);

        if(parents == null || parents.size() == 0) {return Result.success(null);}

        List<CommentVo> parentsVo = parents
                .stream()
                .map(this::CommentToCommentVo)
                .toList();

        setChildren(children, parentsVo);
        return Result.success(parentsVo);
    }

    /**
     * 将父评论对应的子评论设置到父评论Vo中
     * @param children 子评论集合
     * @param commentVos 父评论Vo集合
     *
     */
    private void setChildren(List<Comment> children, List<CommentVo> commentVos) {
        commentVos.forEach(commentVo -> commentVo.setChildrens(this.getChildren(commentVo, children)));
    }

    /**
     * 获得该父评论对应的子评论
     * @param commentVo 父评论
     * @param children 子评论集合
     * @return 父评论下的子评论集合
     */
    private List<CommentVo> getChildren(CommentVo commentVo, List<Comment> children) {
        return children.stream()
                .filter(child -> child.getParentId().equals(commentVo.getId()))
                .map(this::CommentToCommentVo)
                .toList();
    }

    @Override
    @Transactional
    public Result insertComment(CommentInfo commentInfo) {
        Comment comment = new Comment();

        enrichComment(commentInfo, comment);
        updateArticleCommentCount(comment);
        save(comment);

        return Result.success(null);
    }

    /**
     * 评论后将文章的评论数增加
     * @param comment
     */
    private void updateArticleCommentCount(Comment comment) {
        articleService.lambdaUpdate()
                .setSql("comment_counts = comment_counts+1")
                .eq(Article::getId, comment.getArticleId())
                .update();
    }

    /**
     * 封装Comment的属性
     * @param commentInfo
     * @param comment
     */
    private void enrichComment(CommentInfo commentInfo, Comment comment) {
        comment.setAuthorId(UserHolder.getUser().getId());
        comment.setCreateDate(System.currentTimeMillis());
        comment.setToUid(commentInfo.getToUserId());
        comment.setContent(commentInfo.getContent());
        comment.setArticleId(commentInfo.getArticleId());
        comment.setParentId(commentInfo.getParent());
        comment.setLevel(commentInfo.getParent() == 0 ? 1 : 2);
    }


    /**
     * 将Comment转换为CommentVo,并设置Author和ToUser属性
     * @param comment
     * @return
     */
    private CommentVo CommentToCommentVo(Comment comment) {
        CommentVo commentVo = BeanUtil.copyProperties(comment, CommentVo.class);

        setCommentVoAuthor(commentVo, comment.getAuthorId());
        setCommentVoToUser(commentVo, comment.getToUid());
        formatCommentVoCreateDate(commentVo,comment.getCreateDate());

        return commentVo;
    }

    /**
     * 设置CommentVo的ToUser属性
     * @param commentVo
     * @param toUid
     */
    private void setCommentVoToUser(CommentVo commentVo, Long toUid) {
        commentVo.setToUser(BeanUtil.copyProperties(userService.lambdaQuery()
                .select(SysUser::getId, SysUser::getAvatar, SysUser::getNickname)
                .eq(SysUser::getId, toUid)
                .one(), UserDTO.class));
    }

    /**
     * 设置CommentVo的Author属性
     * @param commentVo
     * @param authorId
     */
    private void setCommentVoAuthor(CommentVo commentVo, Long authorId) {
        commentVo.setAuthor(BeanUtil.copyProperties(userService.lambdaQuery()
                .select(SysUser::getId, SysUser::getAvatar, SysUser::getNickname)
                .eq(SysUser::getId, authorId)
                .one(), UserDTO.class));
    }

    /**
     * 将article中CreateDate的时间戳转换为时间并添加到ArticleVo
     * @param commentVo
     */
    private void formatCommentVoCreateDate(CommentVo commentVo,Long Timestamp) {
        String CreateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Date(Timestamp));
        commentVo.setCreateDate(CreateTime);
    }
}
