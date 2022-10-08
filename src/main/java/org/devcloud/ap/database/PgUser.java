package org.devcloud.ap.database;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;

@Entity
@Table(name = "user")
public class PgUser implements Serializable {
    @GenericGenerator(name = "generator", strategy = "increment")
    @Id
    @GeneratedValue(generator = "generator")
    @Column(name = "user_id")
    @Getter @Setter Integer id;
    @Column(name = "user_name")
    @Getter @Setter String name;
    @Column(name = "user_password")
    @Getter @Setter String password;
    @Column(name = "user_mail")
    @Getter @Setter String mail;
    @Column(name = "user_token")
    @Getter @Setter String token;
    @Getter String table = "FROM user";
}
