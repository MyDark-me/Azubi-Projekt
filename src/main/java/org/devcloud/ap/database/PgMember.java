package org.devcloud.ap.database;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;

@RequiredArgsConstructor @NoArgsConstructor
@Getter @Setter
@Entity
@Table(name = "PgMember")
public class PgMember implements Serializable {
    @Id
    @GenericGenerator(name = "generator", strategy = "increment")
    @GeneratedValue(generator = "generator")
    @Column(name = "memberid")
    Integer memberid;
    @JoinColumn(name = "userid")
    @Column(name = "memberuser")
    @NonNull Integer memberuser;
    @JoinColumn(name = "groupid")
    @Column(name = "membergroup")
    @NonNull Integer membergroup;
    @JoinColumn(name = "roleid")
    @Column(name = "memberrole")
    @NonNull Integer memberrole;
}
