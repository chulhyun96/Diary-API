package com.cheolhyeon.diary.friendrequest.dto.response;

import com.cheolhyeon.diary.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchShareCodeOwnerResponse {
    private String ownerDisplayName;
    // 얘는 화면에 보이면 안됨.
    private String shareCodeHash;

    public static SearchShareCodeOwnerResponse toResponse(User shareCodeOwner, String shareCodeHash) {
        return new SearchShareCodeOwnerResponse(shareCodeOwner.getDisplayName(), shareCodeHash);
    }
}
