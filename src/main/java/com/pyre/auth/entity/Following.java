package com.pyre.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor
public class Following extends BaseEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "FOLLOWING_ID", columnDefinition = "BINARY(16)")
    private UUID id;
    @ManyToOne
    @JsonIgnore
    @ToString.Exclude
    @JoinColumn(name = "USER_ID")
    private EndUser user;
    @ManyToOne
    @JsonIgnore
    @ToString.Exclude
    @JoinColumn(name = "FOLLOWING_USER_ID")
    private EndUser following;
}
