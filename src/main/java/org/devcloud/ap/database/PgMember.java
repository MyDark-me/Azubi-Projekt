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
    @JoinColumn
    @NonNull PgUser memberuser;
    @JoinColumn
    @NonNull PgGroup membergroup;
    @JoinColumn
    @NonNull PgRole memberrole;
}
