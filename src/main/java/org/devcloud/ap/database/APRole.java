package org.devcloud.ap.database;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;

@RequiredArgsConstructor @NoArgsConstructor
@Getter @Setter
@Entity
@Table

@NamedQuery(name = "@HQL_GET_ALL_ROLES", query = "FROM APRole aprole")
@NamedQuery(name = "@HQL_GET_SEARCH_ROLE_ID", query = "FROM APRole aprole WHERE aprole.id = :id")
@NamedQuery(name = "@HQL_GET_SEARCH_ROLE_NAME", query = "FROM APRole aprole WHERE aprole.name = :name")

public class APRole implements Serializable {
    @GenericGenerator(name = "generator", strategy = "increment")
    @Id
    @GeneratedValue(generator = "generator")
    @Column(nullable = false)
    Integer id;

    @Column(unique = true, nullable = false)
    @NonNull String name;

    @Column(nullable = false)
    @NonNull String color;
}
