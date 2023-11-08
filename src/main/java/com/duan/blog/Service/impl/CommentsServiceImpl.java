package com.duan.blog.Service.impl;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.duan.blog.vo.CommentVo;
import com.duan.blog.vo.ReplyVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;

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
    public Result getCommentsByArticleId(Long id, Integer page, Integer size) {
        List<Comment> parents = lambdaQuery()
                .eq(Comment::getArticleId, id)
                .eq(Comment::getLevel, 1)
                .orderByAsc(Comment::getCreateDate)
                .page(new Page<>(page, size))
                .getRecords();

        if(parents.isEmpty()) return Result.success(Collections.emptyList());
        List<CommentVo> commentVo = parents.stream()
                .map(this::CommentToCommentVo)
                .map(this::setChildren)
                .toList();
        return Result.success(commentVo);
    }


    @Override
    @Transactional
    public Result insertComment(CommentInfo commentInfo) {
        Comment comment = getCommentByCommentInfo(commentInfo);
        updateArticleCommentCount(comment.getArticleId());
        save(comment);

        return Result.success(null);
    }

    @Override
    public Result getChildComments(Long id, Integer page, Integer size) {
        List<Comment> reply = getReplyByCommentId(id , page, size);
        List<ReplyVo> replyVos = replyList2ReplyVoList(reply);
        return Result.success(replyVos);
    }

    private List<ReplyVo> replyList2ReplyVoList(List<Comment> reply) {
        return reply.stream()
                .map(this::CommentToCommentVo)
                .map(commentVo -> (ReplyVo) commentVo)
                .toList();
    }


    private List<Comment> getReplyByCommentId(Long id, Integer page, Integer size) {
        return lambdaQuery()
                .select(Comment::getId,Comment::getAuthorId,Comment::getCreateDate,Comment::getToUid,Comment::getContent)
                .eq(Comment::getParentId, id)
                .page(new Page<>(page, size))
                .getRecords();
    }


    /**
     * 将父评论对应的子评论设置到父评论Vo中
     * @param parent 父评论
     *
     */
    private CommentVo setChildren(CommentVo parent) {
        parent.setChildren(getChildren(parent.getId()));
        parent.setChildrenCount(getChildrenCount(parent.getId()));
        return parent;
    }
    /**
     * 获取指定评论的子评论数量
     * @param parentId 父评论id
     * @return 子评论数量
     */
    private Long getChildrenCount(Long parentId) {
        return lambdaQuery()
                .eq(Comment::getParentId, parentId)
                .count();
    }

    /**
     * 获得该父评论对应的子评论
     *
     * @param parentId 父评论Id
     * @return 父评论下的子评论集合
     */
    private List<ReplyVo> getChildren(Long parentId) {
        return lambdaQuery()
                .eq(Comment::getParentId, parentId)
                .orderByAsc(Comment::getCreateDate)
                .last("limit 3")
                .list()
                .stream()
                .map(this::CommentToCommentVo)
                .map(commentVo -> (ReplyVo) commentVo)
                .toList();
    }

    /**
     * 评论后将文章的评论数增加
     * @param articleId 文章id
     */
    private void updateArticleCommentCount(Long articleId) {
        articleService.lambdaUpdate()
                .setSql("comment_counts = comment_counts+1")
                .eq(Article::getId, articleId)
                .update();
    }

    /**
     * 评论信息转换位评论的实体类
     * @param commentInfo 评论信息
     */
    private Comment getCommentByCommentInfo(CommentInfo commentInfo) {
        Comment comment = new Comment();
        comment.setAuthorId(UserHolder.getUser().getId());
        comment.setCreateDate(System.currentTimeMillis());
        comment.setToUid(commentInfo.getToUserId());
        comment.setContent(commentInfo.getContent());
        comment.setArticleId(commentInfo.getArticleId());
        comment.setParentId(commentInfo.getParent());
        comment.setLevel(commentInfo.getParent() == 0 ? 1 : 2);
        return comment;
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
