package org.devcloud.ap.database;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;

@Entity
@Table(name = "member")
public class PgMember implements Serializable {
    @Id
    @GenericGenerator(name = "generator", strategy = "increment")
    @GeneratedValue(generator = "generator")
    @Column(name = "user_id")
    @Getter @Setter Integer id;
    @JoinColumn(name = "user_id")
    @Getter @Setter PgUser user;
    @JoinColumn(name = "group_id")
    @Getter @Setter PgGroup group;
    @JoinColumn(name = "role_id")
    @Getter @Setter PgRole role;
    @Getter String table = "FROM member";
}
