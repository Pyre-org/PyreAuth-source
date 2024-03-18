package com.pyre.auth.dto.response.enduser;

import com.pyre.auth.entity.EndUser;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record FeedSpaceResponse(
        @Schema(description = "방금 캡처됨 사용 유무", example = "true")
        Boolean useCaptureRoom,
        @Schema(description = "피드 제목 팝업 창 사용 유무", example = "true")
        Boolean useFeedInfo,
        @Schema(description = "캡처됨 채널 UUID", example = "asdasf-qweqw-czxc")
        UUID channelId,
        @Schema(description = "캡처됨 룸 스페이스 UUID", example = "asdasf-qweqw-czxc")
        UUID captureRoomSpaceId,
        @Schema(description = "나의 스페이스 UUID", example = "asdasf-qweqw-czxc")
        UUID spaceId
) {
        public static FeedSpaceResponse makeDto(EndUser endUser) {
                return new FeedSpaceResponse(endUser.getUseCaptureRoom(), endUser.getUseFeedInfo(), endUser.getSelectedChannel(), endUser.getSelectedCapture(), endUser.getSelectedSpace());
        }
}
