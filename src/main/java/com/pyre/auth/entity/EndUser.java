package com.pyre.auth.entity;


import com.pyre.auth.dto.request.enduser.MyProfileEditRequest;
import com.pyre.auth.enumeration.SocialType;
import com.pyre.auth.enumeration.UserRoleEnum;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pyre.auth.enumeration.SocialType;
import com.pyre.auth.enumeration.UserRoleEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.cglib.core.Local;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@Entity
@Getter
@NoArgsConstructor
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
@EntityListeners(AuditingEntityListener.class)
public class EndUser {


    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "USER_ID", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(unique = false, nullable = false)
    @Pattern(regexp = "^[A-Za-z0-9ㄱ-ㅎ가-힣-_]{2,20}$", message = "유효하지 않은 닉네임 형식입니다. 알파벳과 숫자 조합 5~12자 사이`")
    private String nickname;

    @Column(nullable = false, unique = true, length = 40)
    @Email(message = "유효하지 않은 이메일 형식입니다.", regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$")
    private String email;
    private String password;
    @Pattern(regexp = "^.*\\.(jpg|png|jpeg|gif|)$")
    private String profilePictureUrl;
    private String shortDescription;

    @Column(nullable = false)
    private LocalDateTime createDate;
    @LastModifiedDate
    private LocalDateTime modifyDate;
    private LocalDateTime lastActive;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private UserRoleEnum role; // 0 = ROLE_ADMIN, 1 = ROLE_USER


    @Enumerated(EnumType.STRING)
    @Column
    private SocialType socialType;

    @Column
    private String socialId;

    private Boolean agreement1;

    private Boolean agreement2;

    @OneToMany(mappedBy = "user")
    private Set<Followers> followers;
    @OneToMany(mappedBy = "user")
    private Set<Following> followings;
    private UUID selectedChannel;
    private UUID selectedCapture;
    private UUID selectedSpace;
    private Boolean useCaptureRoom;
    private Boolean useFeedInfo;
    @Builder
    public EndUser(
        String nickname,
        String email,
        String password,
        String profilePictureUrl,
        String shortDescription,
        Boolean agreement1,
        Boolean agreement2,
        SocialType socialType,
        String socialId,
        UserRoleEnum role
    ) {
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.profilePictureUrl = profilePictureUrl;
        this.shortDescription = shortDescription;
        this.agreement1 = agreement1;
        this.agreement2 = agreement2;
        this.createDate = LocalDateTime.now();
        this.followers = new HashSet<>();
        this.followings = new HashSet<>();
        this.role = role;
        this.useCaptureRoom = true;
        this.socialId = socialId;
        this.socialType = socialType;
        this.useFeedInfo = true;
    }
    public static EndUser createUser(
            String nickname,
            String email,
            String password,
            String profilePictureUrl,
            String shortDescription,
            Boolean agreement1,
            Boolean agreement2,
            SocialType socialType,
            String socialId,
            UserRoleEnum role
    ) {
        EndUser user = EndUser.builder()
                .nickname(nickname)
                .email(email)
                .profilePictureUrl(profilePictureUrl)
                .shortDescription(shortDescription)
                .password(password)
                .agreement1(agreement1)
                .agreement2(agreement2)
                .role(role)
                .socialId(socialId)
                .socialType(socialType)
                .build();

        return user;
    }
    public void updateLastActive(LocalDateTime lastActive) {
        this.lastActive = lastActive;
    }
    public void updateUserProfile(MyProfileEditRequest myProfileEditRequest) {
        this.nickname = myProfileEditRequest.nickname();
        this.shortDescription = myProfileEditRequest.shortDescription();
        this.profilePictureUrl = myProfileEditRequest.profilePictureUrl();
        this.selectedSpace = !myProfileEditRequest.selectedSpaceId().equals(null) &&
                !myProfileEditRequest.selectedSpaceId().equals("") ? myProfileEditRequest.selectedSpaceId() : null;
        this.useCaptureRoom = myProfileEditRequest.useCaptureRoom();
        this.useFeedInfo = myProfileEditRequest.useFeedInfo();

    }
    public void updateSelectedCapture(UUID selectedCapture, UUID selectedChannel) {
        this.selectedCapture = selectedCapture;
        this.selectedChannel = selectedChannel;
    }
    public void updatePassword(String password) {
        this.password = password;
    }

}
