package org.devcloud.ap.database;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;

@RequiredArgsConstructor @NoArgsConstructor
@Entity
@Table(name = "pgMember")
public class PgMember implements Serializable {
    @Id
    @GenericGenerator(name = "generator", strategy = "increment")
    @GeneratedValue(generator = "generator")
    @Column(name = "member_id")
    @Getter @Setter Integer id;
    @JoinColumn(name = "user_id")
    @Column(name = "member_user")
    @NonNull @Getter @Setter PgUser user;
    @JoinColumn(name = "group_id")
    @Column(name = "member_group")
    @NonNull @Getter @Setter PgGroup group;
    @JoinColumn(name = "role_id")
    @Column(name = "member_role")
    @NonNull @Getter @Setter PgRole role;
}
