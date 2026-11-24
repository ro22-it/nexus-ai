package ai.nexus.server.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findAllByOrderByCreatedAtAsc();

    @Query("select c from ChatMessage c where c.conversationId = :conversationId "
            + "and ((:userId is null and c.userId is null) or c.userId = :userId) "
            + "order by c.createdAt asc")
    List<ChatMessage> findHistory(@Param("conversationId") String conversationId, @Param("userId") Long userId);

    @Query("select distinct c.conversationId from ChatMessage c "
            + "where (:userId is null and c.userId is null) or c.userId = :userId "
            + "order by c.conversationId")
    List<String> findConversationIds(@Param("userId") Long userId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("delete from ChatMessage c where c.conversationId = :conversationId "
            + "and ((:userId is null and c.userId is null) or c.userId = :userId)")
    int deleteConversation(@Param("conversationId") String conversationId, @Param("userId") Long userId);
}
