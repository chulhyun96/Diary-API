package com.cheolhyeon.diary.friendrequest.repository;

import com.cheolhyeon.diary.friendrequest.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, String> {
    @Query(
            value = """
                                      SELECT 1
                                      FROM friend_request
                                      WHERE request_id = :requestId
                                      AND status = :status
                                      LIMIT 1
                    """, nativeQuery = true
    )
    boolean existsPendingByRequestId(@Param("requestId") String requestId, @Param("status") String status);

    @Query(
            value = """
                                      SELECT count(*) as count
                                      FROM friend_request
                                      WHERE owner_user_id = :requestId
                                      AND status = :status
                    """, nativeQuery = true
    )
    Long countByRecipientId(@Param("requestId") Long recipientId, @Param("status") String status);
}
