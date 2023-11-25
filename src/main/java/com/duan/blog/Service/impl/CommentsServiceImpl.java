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
import com.duan.blog.vo.CommentManageVo;
import com.duan.blog.vo.CommentVo;
import com.duan.blog.vo.ReplyVo;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.duan.blog.utils.RedisConstants.*;

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
    @Resource
    StringRedisTemplate stringRedisTemplate;

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
        save(comment);
        if(isReply(commentInfo.getParent())){
            addReplyToParentZSet(comment.getId(),commentInfo.getParent());
        }else{
            updateArticleCommentCount(comment.getArticleId());
        }
        return Result.success(null);
    }


    @Override
    public Result getChildComments(Long id, Integer page, Integer size) {
        List<Comment> reply = getReplyByCommentId(id , page, size);
        List<ReplyVo> replyVos = replyList2ReplyVoList(reply);
        return Result.success(replyVos);
    }

    @Override
    public Result likeComment(Long id) {
        if(isCommentLiked(id)){
            cancelLike(id);
        }else{
            like(id);
        }
        return Result.success(null);
    }

    @Override
    public Result listCommentsToCurUser(Integer page, Integer size) {
        List<Article> curUserArticle = getCurUserArticle();
        //文章id到标题的映射
        Map<Long, String> idToTitle = curUserArticle.stream().collect(
                Collectors.toMap(Article::getId, Article::getTitle));
        List<Comment> comments = getUserComments(curUserArticle.stream().map(Article::getId).toList());
        return Result.success(comments.stream()
                .map(comment -> CommentManageVo
                        .builder()
                        .commentId(comment.getId())
                        .CommentUser(getUserDTOByID(comment.getAuthorId()))
                        .articleTitle(idToTitle.get(comment.getArticleId()))
                        .content(comment.getContent())
                        .createTime(formatTimeStamp(comment.getCreateDate()))
                        .build())
                .toList());

    }



    @Override
    public Result listUserComments(Integer page, Integer size) {
        //TODO 获取当前用户的评论实现
        return null;
    }


    /**
     * 当前当前登录用户的全部文章id和标题
     * @return 文章id和标题
     */
    private List<Article> getCurUserArticle() {
        return articleService.lambdaQuery()
                .select(Article::getId, Article::getTitle)
                .eq(Article::getAuthorId, UserHolder.getUserID())
                .list();
    }

    /**
     * 找到当前用户的全部评论
     * @param userArticleIds 当前用户的文章
     * @return 评论列表
     */
    private List<Comment> getUserComments(List<Long> userArticleIds) {
        return lambdaQuery()
                .select(Comment::getId, Comment::getArticleId,
                        Comment::getAuthorId, Comment::getCreateDate, Comment::getContent)
                .eq(Comment::getLevel, 1)
                .in(Comment::getArticleId, userArticleIds)
                .list();
    }

    /**
     * 判断是评论还是回复
     * @param Parent 父评论ID
     * @return true是回复，false是评论
     */
    private static boolean isReply(Long Parent) {
        return Parent != null && Parent != 0;
    }


    /**
     * 将回复添加到父评论的ZSet底下
     * @param id 回复id
     * @param parent 父评论ID
     */
    private void addReplyToParentZSet(Long id, Long parent) {
        stringRedisTemplate.opsForZSet().add(COMMENT_REPLY_KEY + parent, id.toString(), 0);
    }

    /**
     * 点赞
     * @param id 评论ID
     */
    private void like(Long id) {
        lambdaUpdate().setSql("liked = liked + 1").eq(Comment::getId, id).update();
        stringRedisTemplate.opsForSet().add(COMMENT_LIKED_KEY + id,UserHolder.getUser().getId().toString());
        Long parentId = lambdaQuery().select(Comment::getParentId).eq(Comment::getId, id).one().getParentId();
        if(isReply(parentId)){
            stringRedisTemplate.opsForZSet().incrementScore(COMMENT_REPLY_KEY + parentId, String.valueOf(id),1);
        }
    }

    /**
     * 取消点赞
     * @param id 评论ID
     */
    private void cancelLike(Long id) {
        lambdaUpdate().setSql("liked = liked - 1").eq(Comment::getId, id).update();
        stringRedisTemplate.opsForSet().remove(COMMENT_LIKED_KEY + id,UserHolder.getUser().getId().toString());
        Long parentId = lambdaQuery().select(Comment::getParentId).eq(Comment::getId, id).one().getParentId();
        if(isReply(parentId)){
            stringRedisTemplate.opsForZSet().incrementScore(COMMENT_REPLY_KEY + parentId, id.toString(),-1);
        }
    }

    /**
     * 获取用户当前用户是否点赞过该博客
     * @param commentsID 评论ID
     * @return 点赞true，未点赞false
     */
    private Boolean isCommentLiked(Long commentsID) {
         return stringRedisTemplate.opsForSet().isMember(
                 COMMENT_LIKED_KEY + commentsID,
                 UserHolder.getUser().getId().toString());
    }

    /**
     * 获取回复
     * @param id 父评论ID
     * @param page 页码
     * @param size  每页数量
     * @return 回复列表
     */
    private List<Comment> getReplyByCommentId(Long id, Integer page, Integer size) {
        return lambdaQuery()
                .select(Comment::getId,Comment::getAuthorId,Comment::getCreateDate,Comment::getToUid,Comment::getContent)
                .eq(Comment::getParentId, id)
                .page(new Page<>(page, size))
                .getRecords();
    }

    /**
     * 将回复列表转换为回复Vo列表
     * @param reply 回复列表
     * @return 回复Vo列表
     */
    private List<ReplyVo> replyList2ReplyVoList(List<Comment> reply) {
        return reply.stream()
                .map(this::CommentToCommentVo)
                .map(commentVo -> (ReplyVo) commentVo)
                .toList();
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
        return stringRedisTemplate.opsForZSet().size(COMMENT_REPLY_KEY + parentId);
    }

    /**
     * 获得该父评论对应的子评论
     *
     * @param parentId 父评论Id
     * @return 父评论下的子评论集合
     */
    private List<ReplyVo> getChildren(Long parentId) {
        Set<String> top3Reply = stringRedisTemplate.opsForZSet().reverseRange(
                COMMENT_REPLY_KEY + parentId, 0, 2);
        if(top3Reply == null || top3Reply.isEmpty()) return Collections.emptyList();
        return lambdaQuery()
                .in(Comment::getId, top3Reply.stream().map(Long::valueOf).collect(Collectors.toList()))
                .last("ORDER BY FIELD(id," + String.join(",", top3Reply) + ")")
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
        comment.setLiked(0);
        return comment;
    }


    /**
     * 将Comment转换为CommentVo,并设置Author和ToUser属性
     * @param comment 评论
     * @return CommentVo评论VO
     */
    private CommentVo CommentToCommentVo(Comment comment) {
        CommentVo commentVo = BeanUtil.copyProperties(comment, CommentVo.class);
        commentVo.setAuthor(getUserDTOByID(comment.getAuthorId()));
        commentVo.setToUser(getUserDTOByID(comment.getToUid()));
        commentVo.setCreateDate(formatTimeStamp(comment.getCreateDate()));
        setCommentVoIsLiked(commentVo);
        setCommentVoLikes(commentVo);
        return commentVo;
    }

    /**
     ** 设置VO对象的点赞数likes
     * @param commentVo VO对象
     */
    private void setCommentVoLikes(CommentVo commentVo) {
        commentVo.setLikes(stringRedisTemplate.opsForSet().size(COMMENT_LIKED_KEY + commentVo.getId().toString()));
    }

    /**
     * 设置VO对象是否被当前对象点赞
     * @param commentVo VO对象
     */
    private void setCommentVoIsLiked(CommentVo commentVo) {
        if(BeanUtil.isEmpty(UserHolder.getUser())){
            return;
        }
        commentVo.setIsLike(isCommentLiked(commentVo.getId()));
    }


    /**
     * 通过用户ID获取粗略信息
     * @param authorId 用户ID
     */
    private UserDTO getUserDTOByID(Long authorId) {
        return BeanUtil.copyProperties(userService.lambdaQuery()
                .select(SysUser::getId, SysUser::getAvatar, SysUser::getNickname)
                .eq(SysUser::getId, authorId)
                .one(), UserDTO.class);
    }

    /**
     * 获取时间戳
     * @param Timestamp 时间戳
     */
    private String formatTimeStamp(Long Timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Date(Timestamp));
    }
}
