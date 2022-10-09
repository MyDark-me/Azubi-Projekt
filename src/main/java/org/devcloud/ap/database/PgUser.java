package org.devcloud.ap.database;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;

@RequiredArgsConstructor @NoArgsConstructor
@Entity
@Table(name = "pgUser")
public class PgUser implements Serializable {
    @GenericGenerator(name = "generator", strategy = "increment")
    @Id
    @GeneratedValue(generator = "generator")
    @Column(nullable = false, name = "user_id")
    @Getter @Setter Integer id;
    @Column(nullable = false, name = "user_name")
    @NonNull @Getter @Setter String name;
    @Column(nullable = false, name = "user_password")
    @NonNull @Getter @Setter String password;
    @Column(nullable = false, name = "user_mail")
    @NonNull @Getter @Setter String mail;
    @Column(nullable = false, name = "user_token")
    @NonNull @Getter @Setter String token;
}
