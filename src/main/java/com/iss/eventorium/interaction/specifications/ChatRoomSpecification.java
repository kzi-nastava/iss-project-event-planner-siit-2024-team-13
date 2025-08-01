package com.iss.eventorium.interaction.specifications;

import com.iss.eventorium.interaction.models.ChatRoom;
import com.iss.eventorium.user.models.User;
import com.iss.eventorium.user.models.UserBlock;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public class ChatRoomSpecification {

    private ChatRoomSpecification() {}

    public static Specification<ChatRoom> filterBy(User user) {
        return Specification.where(filterBySenderId(user.getId() + "_%"))
                .and(filterOutBlockedContent(user));
    }

    public static Specification<ChatRoom> filterByName(String name) {
        return (root, query, cb) ->
                name == null || name.isEmpty()
                        ? cb.conjunction()
                        : cb.like(cb.lower(root.get("name")), name);
    }

    private static Specification<ChatRoom> filterBySenderId(String senderId) {
        return (root, query, cb) -> {
            assert query != null;
            if (query.getOrderList().isEmpty()) {
                query.orderBy(cb.asc(root.get("lastMessage").get("timestamp")));
            }
            return cb.like(root.get("name"), senderId);
        };
    }

    private static Specification<ChatRoom> filterOutBlockedContent(User currentUser) {
        return (root, query, cb) -> {
            if (currentUser == null) return cb.conjunction();

            Long currentUserId = currentUser.getId();

            assert query != null;

            Subquery<Long> blockedSubquery = query.subquery(Long.class);
            Root<UserBlock> blockedRoot = blockedSubquery.from(UserBlock.class);
            blockedSubquery.select(blockedRoot.get("blocked").get("id"))
                    .where(cb.equal(blockedRoot.get("blocker").get("id"), currentUserId));

            return cb.and(
                    cb.not(root.get("lastMessage").get("recipient").get("id").in(blockedSubquery)),
                    cb.not(root.get("lastMessage").get("sender").get("id").in(blockedSubquery))
            );
        };
    }

}
