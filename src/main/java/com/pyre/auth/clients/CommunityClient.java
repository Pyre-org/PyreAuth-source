package com.pyre.auth.clients;

import com.pyre.auth.dto.response.enduser.ChannelInfoFromSpaceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "community", path = "/community")
public interface CommunityClient {
    String AUTHORIZATION = "Authorization";
    @GetMapping("/channel/isSubscribe/{channelId}")
    ResponseEntity<Boolean> isSubscribeChannel(
            @RequestHeader("id") String userId,
            @PathVariable String channelId);
    @GetMapping("/space/canWrite/{spaceId}")
    ResponseEntity<Boolean> canWriteSpace(
            @RequestHeader("id") String userId,
            @PathVariable String spaceId);
    @GetMapping("/space/getCapture/{channelId}")
    ResponseEntity<String> getCaptureSpace(
            @RequestHeader("id") String userId,
            @PathVariable String channelId);
    @GetMapping("/space/getChannelCapture/{spaceId}")
    ResponseEntity<ChannelInfoFromSpaceResponse> getChannelCaptureSpace(
            @RequestHeader("id") String userId,
            @PathVariable String spaceId);
}