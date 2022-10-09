package org.devcloud.ap.database;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;

@RequiredArgsConstructor @NoArgsConstructor
@Getter @Setter
@Entity
@Table(name = "pgUser")
public class PgUser implements Serializable {
    @GenericGenerator(name = "generator", strategy = "increment")
    @Id
    @GeneratedValue(generator = "generator")
    @Column(nullable = false, name = "user_id")
    Integer id;

    @Column(unique = true, nullable = false, name = "user_name")
    @NonNull String name;
    @Column(nullable = false, name = "user_password")
    @NonNull String password;
    @Column(unique = true, nullable = false, name = "user_mail")
    @NonNull String mail;
    @Column(nullable = false, name = "user_token")
    @NonNull String token;
}
