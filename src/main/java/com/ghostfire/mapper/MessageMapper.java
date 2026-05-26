package com.ghostfire.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ghostfire.entity.Message;
import com.ghostfire.vo.ConversationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    @Select("""
        SELECT
            CASE WHEN m.from_user_id = #{userId} THEN m.to_user_id ELSE m.from_user_id END AS targetUserId,
            u.nickname AS targetNickname,
            u.avatar AS targetAvatar,
            m.content AS lastMessage,
            m.create_time AS lastMessageTime,
            0 AS unreadCount
        FROM t_message m
        INNER JOIN (
            SELECT
                CASE WHEN from_user_id = #{userId} THEN to_user_id ELSE from_user_id END AS partner_id,
                MAX(id) AS max_id
            FROM t_message
            WHERE (from_user_id = #{userId} OR to_user_id = #{userId})
              AND type = 1
              AND (
                  (from_user_id = #{userId} AND COALESCE(from_deleted, FALSE) = FALSE)
                  OR (to_user_id = #{userId} AND COALESCE(to_deleted, FALSE) = FALSE)
              )
            GROUP BY partner_id
        ) latest ON m.id = latest.max_id
        INNER JOIN sys_user u ON u.id = latest.partner_id
        ORDER BY m.create_time DESC
    """)
    List<ConversationVO> selectConversations(@Param("userId") Long userId);

    @Select("""
        SELECT COUNT(*) FROM t_message
        WHERE from_user_id = #{targetUserId}
          AND to_user_id = #{userId}
          AND type = 1
          AND status = 0
          AND COALESCE(to_deleted, FALSE) = FALSE
    """)
    int countUnreadFromUser(@Param("userId") Long userId, @Param("targetUserId") Long targetUserId);
}
