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
        List<Comment> commentList = lambdaQuery()
                .eq(Comment::getArticleId, id)
                .eq(Comment::getLevel, 1)
                .list();

        if(commentList == null || commentList.size() == 0) {return Result.success(null);}

        List<CommentVo> commentVos = commentList.stream().map((comment -> {
            CommentVo commentVo = CommentToCommentVo(comment);
            if(comment.getLevel() == 1){
                setChildren(comment.getId(), commentVo);
            }
            return commentVo;
        })).collect(Collectors.toList());

        return Result.success(commentVos);

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

    private void updateArticleCommentCount(Comment comment) {
        articleService.lambdaUpdate()
                .setSql("comment_counts = comment_counts+1")
                .eq(Article::getId, comment.getArticleId()).update();
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
     * 若该文章是1级评论，则到数据库中查询所有该评论的子评论并设置commentVo的Children属性
     * @param commentId
     * @param commentVo
     */
    private void setChildren(Long commentId, CommentVo commentVo) {
        commentVo.setChildrens(lambdaQuery()
                .eq(Comment::getParentId, commentId)
                .list().stream()
                .map(this::CommentToCommentVo)
                .collect(Collectors.toList()));
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
