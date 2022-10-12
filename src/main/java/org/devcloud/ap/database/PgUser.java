package org.devcloud.ap.database;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;

@RequiredArgsConstructor @NoArgsConstructor
@Getter @Setter
@Entity
@Table(name = "PgUser")
public class PgUser implements Serializable {
    @GenericGenerator(name = "generator", strategy = "increment")
    @Id
    @GeneratedValue(generator = "generator")
    @Column(nullable = false, name = "userid")
    Integer userid;

    @Column(unique = true, nullable = false, name = "username")
    @NonNull String username;
    @Column(nullable = false, name = "userpassword")
    @NonNull String userpassword;
    @Column(unique = true, nullable = false, name = "usermail")
    @NonNull String usermail;
    @Column(unique = true, nullable = false, name = "usertoken")
    @NonNull String usertoken;
}
