package com.cheolhyeon.diary.friendrequest.repository;

import com.cheolhyeon.diary.friendrequest.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, String> {
    @Query(
            value = """
                                      SELECT 1
                                      FROM friend_request
                                      WHERE id = :id
                                      AND status = :status
                                      LIMIT 1
                    """, nativeQuery = true
    )
    boolean existsPendingByRequestId(@Param("id") String id, @Param("status") String status);

    @Query(
            value = """
                                      SELECT count(*) as count
                                      FROM friend_request f
                                      WHERE f.owner_user_id = :recipientId
                                      AND status = :status
                    """, nativeQuery = true
    )
    Long countByRecipientId(@Param("recipientId") Long recipientId, @Param("status") String status);

    @Modifying
    @Query(
            value = """
                                        update friend_request f
                                        set f.status = :status and f.decided_at = :now
                                        where f.id = :id
                                        and f.owner_user_id = :userId
                    """, nativeQuery = true
    )
    void updateStatusByUserAction(
            @Param("id") String id,
            @Param("userId") Long userId,
            @Param("status") String status,
            @Param("now") LocalDateTime now);


    @Query(
            value = """
                                        select * from friend_request f
                                                            where f.id = :id
                                                            and f.status = :status
                    """, nativeQuery = true
    )
    Optional<FriendRequest> findPendingById(
            @Param("id") String id,
            @Param("status") String status);
}
