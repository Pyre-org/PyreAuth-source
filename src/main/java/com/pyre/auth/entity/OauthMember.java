package com.pyre.auth.entity;


import com.pyre.auth.enumeration.OAuthServerType;
import com.pyre.auth.oauth2.OauthId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Table(name = "oauth_member",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "oauth_id_unique",
                        columnNames = {
                                "oauth_server_id",
                                "oauth_server"
                        }
                ),
        }
)
public class OauthMember extends BaseEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Embedded
    private OauthId oauthId;
    private String nickname;
    private String name;
    private String profileImageUrl;
    private String email;
    @Builder
    public OauthMember(
            OauthId oauthId,
            String nickname,
            String profileImageUrl,
            String email,
            String name
    ) {
        this.oauthId = oauthId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.email = email;
        this.name = name;
    }
    public static OauthMember createOauthMember(
            OauthId oauthId,
            String nickname,
            String profileImageUrl,
            String email,
            String name
    ) {
        OauthMember oauthMember = OauthMember.builder()
                .oauthId(oauthId)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .email(email)
                .name(name)
                .build();
        return oauthMember;
    }
    public UUID id() {
        return id;
    }

    public OauthId oauthId() {
        return oauthId;
    }

    public String nickname() {
        return nickname;
    }

    public String profileImageUrl() {
        return profileImageUrl;
    }

    public String email() { return email; }

    public String name() { return name; }
}
