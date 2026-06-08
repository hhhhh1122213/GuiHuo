package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.ghostfire.dto.PostDto;
import com.ghostfire.entity.Post;
import com.ghostfire.entity.UserStat;
import com.ghostfire.mapper.PostMapper;
import com.ghostfire.service.MedalService;
import com.ghostfire.service.PostTagService;
import com.ghostfire.service.RankingService;
import com.ghostfire.service.UserStatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock PostMapper postMapper;
    @Mock UserStatService userStatService;
    @Mock MedalService medalService;
    @Mock RankingService rankingService;
    @Mock PostTagService postTagService;
    @Mock RedisTemplate<String, Object> redisTemplate;

    PostServiceImpl postService;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), ""), Post.class);
        postService = new PostServiceImpl(userStatService, medalService, rankingService, postTagService, redisTemplate);
        ReflectionTestUtils.setField(postService, "baseMapper", postMapper);
    }

    @Test
    void search_delegatesToFullTextSearch() {
        Page<Post> mockPage = new Page<>(1, 20);
        when(postMapper.searchFullText(eq("Java"), any(Page.class))).thenReturn(mockPage);
        when(postMapper.searchFullTextCount(eq("Java"))).thenReturn(5L);

        Page<Post> result = postService.search("Java", 1, 20);

        assertSame(mockPage, result);
        assertEquals(5L, result.getTotal());
        verify(postMapper).searchFullText(eq("Java"), any(Page.class));
        verify(postMapper).searchFullTextCount(eq("Java"));
    }

    @Test
    void pageFeed_hotSortsByEngagement() {
        Page<Post> mockPage = new Page<>(1, 20);
        when(postMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        Page<Post> result = postService.pageFeed(null, 1, 20, "hot");

        assertSame(mockPage, result);
        ArgumentCaptor<Wrapper<Post>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(postMapper).selectPage(any(Page.class), wrapperCaptor.capture());
        String sql = wrapperCaptor.getValue().getSqlSegment();
        assertTrue(sql.contains("commentCount DESC"), sql);
        assertTrue(sql.contains("likeCount DESC"), sql);
        assertTrue(sql.contains("viewCount DESC"), sql);
    }

    @Test
    void pageFeed_latestSortsByCreateTimeWithoutPinnedBoost() {
        Page<Post> mockPage = new Page<>(1, 20);
        when(postMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        Page<Post> result = postService.pageFeed(3L, 1, 20, "latest");

        assertSame(mockPage, result);
        ArgumentCaptor<Wrapper<Post>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(postMapper).selectPage(any(Page.class), wrapperCaptor.capture());
        String sql = wrapperCaptor.getValue().getSqlSegment();
        assertTrue(sql.contains("categoryId ="), sql);
        assertTrue(sql.contains("createTime DESC"), sql);
        assertFalse(sql.contains("is_top DESC"), sql);
    }

    @Test
    void pageEssence_canFilterByCategory() {
        Page<Post> mockPage = new Page<>(1, 20);
        when(postMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        Page<Post> result = postService.pageEssence(2L, 1, 20);

        assertSame(mockPage, result);
        ArgumentCaptor<Wrapper<Post>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(postMapper).selectPage(any(Page.class), wrapperCaptor.capture());
        String sql = wrapperCaptor.getValue().getSqlSegment();
        assertTrue(sql.contains("isEssence ="), sql);
        assertTrue(sql.contains("categoryId ="), sql);
    }

    @Test
    @SuppressWarnings("unchecked")
    void addViewCount_incrementsRedisKey() {
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        postService.addViewCount(100L);

        verify(valueOps).increment("post:views:100");
    }

    @Test
    void createPost_setsFieldsAndSaves() {
        Long userId = 1L;
        PostDto dto = new PostDto();
        dto.setTitle("Test Post");
        dto.setContent("Content");
        dto.setCategoryId(1L);

        // Mock insert to set the ID on the entity
        doAnswer(invocation -> {
            Post p = invocation.getArgument(0);
            p.setId(42L);
            return 1;
        }).when(postMapper).insert(any());

        @SuppressWarnings("unchecked")
        com.baomidou.mybatisplus.core.mapper.BaseMapper<UserStat> statMapper = mock(com.baomidou.mybatisplus.core.mapper.BaseMapper.class);
        when(userStatService.getById(userId)).thenReturn(new UserStat());
        when(userStatService.getBaseMapper()).thenReturn(statMapper);
        when(statMapper.update(any(), any())).thenReturn(1);

        Post result = postService.createPost(userId, dto);

        assertNotNull(result);
        assertEquals("Test Post", result.getTitle());
        assertEquals("Content", result.getContent());
        assertEquals(userId, result.getUserId());
        assertEquals(1L, result.getCategoryId());
        assertFalse(result.getIsTop());
        assertFalse(result.getIsEssence());
        assertEquals(1, result.getStatus());
    }

    @Test
    void deletePost_setsStatusToDeleted() {
        Post post = new Post();
        post.setId(1L);
        post.setStatus(1);
        post.setUserId(1L);
        post.setLikeCount(0);
        post.setCommentCount(0);

        UserStat stat = new UserStat();
        stat.setUserId(1L);
        stat.setPostCount(5);
        @SuppressWarnings("unchecked")
        com.baomidou.mybatisplus.core.mapper.BaseMapper<UserStat> statMapper = mock(com.baomidou.mybatisplus.core.mapper.BaseMapper.class);
        when(userStatService.getById(1L)).thenReturn(stat);
        when(userStatService.getBaseMapper()).thenReturn(statMapper);
        when(statMapper.update(any(), any())).thenReturn(1);
        when(postMapper.updateById(any())).thenReturn(1);

        postService.deletePost(post, 1L);

        assertEquals(0, post.getStatus());
    }
}
