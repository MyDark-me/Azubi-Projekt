package org.devcloud.ap.database;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;

@RequiredArgsConstructor @NoArgsConstructor
@Getter @Setter
@Entity
@Table

@NamedQuery(name = "@HQL_GET_ALL_USERS", query = "FROM APRole apuser")
@NamedQuery(name = "@HQL_GET_SEARCH_USER_COUNT", query = "SELECT COUNT(*) FROM APUser apuser WHERE apuser.name = :name")
@NamedQuery(name = "@HQL_GET_SEARCH_USER_ID", query = "FROM APUser apuser WHERE apuser.id = :id")
@NamedQuery(name = "@HQL_GET_SEARCH_USER_NAME", query = "FROM APUser apuser WHERE apuser.name = :name")
@NamedQuery(name = "@HQL_GET_SEARCH_USER_TOKEN", query = "FROM APUser apuser WHERE apuser.token = :token")

public class APUser implements Serializable {
    @GenericGenerator(name = "generator", strategy = "increment")
    @Id
    @GeneratedValue(generator = "generator")
    @Column(nullable = false)
    Integer id;

    @Column(unique = true, nullable = false)
    @NonNull String name;

    @Column(nullable = false)
    @NonNull String password;

    @Column(unique = true, nullable = false)
    @NonNull String mail;

    @Column(unique = true, nullable = false)
    @NonNull String token;
}
