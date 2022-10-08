package org.devcloud.ap.database;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "member")
public class PgMember implements Serializable {
    @Id
    @Column(name = "member_user")
    @Getter @Setter PgUser user;
    @Id
    @Column(name = "member_group")
    @Getter @Setter PgGroup group;
    @Id
    @Column(name = "member_role")
    @Getter @Setter PgRole role;
    @Getter String table = "FROM member";
}
