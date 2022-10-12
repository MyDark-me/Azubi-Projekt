package org.devcloud.ap.database;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;

@RequiredArgsConstructor @NoArgsConstructor
@Getter @Setter
@Entity
@Table(name = "PgGroup")
public class PgGroup implements Serializable {
    @GenericGenerator(name = "generator", strategy = "increment")
    @Id
    @GeneratedValue(generator = "generator")
    @Column(nullable = false, name = "groupid")
    Integer groupid;
    @Column(unique = true, nullable = false, name = "groupname")
    @NonNull String groupname;
    @Column(nullable = false, name = "groupcolor")
    @NonNull Integer groupcolor;
}
