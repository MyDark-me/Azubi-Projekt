package org.devcloud.ap.database;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.awt.*;
import java.io.Serializable;

@RequiredArgsConstructor @NoArgsConstructor
@Getter @Setter
@Entity
@Table(name = "PgRole")
public class PgRole implements Serializable {
    @GenericGenerator(name = "generator", strategy = "increment")
    @Id
    @GeneratedValue(generator = "generator")
    @Column(nullable = false, name = "roleid")
    Integer roleid;
    @Column(unique = true, nullable = false, name = "rolename")
    @NonNull String rolename;
    @Column(nullable = false, name = "rolecolor")
    @NonNull String rolecolor;
}
