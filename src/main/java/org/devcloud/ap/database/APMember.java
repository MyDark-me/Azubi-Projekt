package org.devcloud.ap.database;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;

@RequiredArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Entity
@Table

@NamedQuery(name = "@HQL_GET_ALL_MEMBERS", query = "FROM APMember apmember")
@NamedQuery(name = "@HQL_GET_ALL_MEMBERS_GROUP", query = "FROM APMember apmember WHERE apmember.group= :group")
@NamedQuery(name = "@HQL_GET_ALL_MEMBERS_USER", query = "FROM APMember apmember WHERE apmember.user= :user")
@NamedQuery(name = "@HQL_GET_SEARCH_MEMBER_USER_GROUP", query = "FROM APMember apmember WHERE apmember.user= :user AND apmember.group= :group")

public class APMember implements Serializable {
    @Id
    @GenericGenerator(name = "generator", strategy = "increment")
    @GeneratedValue(generator = "generator")
    @Column(name = "apmember_id", nullable = false)
    Integer id;

    @OneToOne
    @JoinColumn(name = "apmember_user", nullable = false)
    @NonNull APUser user;

    @OneToOne
    @JoinColumn(name = "apmember_group", nullable = false)
    @NonNull APGroup group;

    @OneToOne
    @JoinColumn(name = "apmember_role", nullable = false)
    @NonNull APRole role;
}
