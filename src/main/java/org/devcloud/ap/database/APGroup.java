package org.devcloud.ap.database;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;

@RequiredArgsConstructor @NoArgsConstructor
@Getter @Setter
@Entity
@Table

@NamedQuery(name = "@HQL_GET_ALL_GROUPS", query = "FROM APGroup apgroup")
@NamedQuery(name = "@HQL_GET_SEARCH_GROUP_ID", query = "FROM APGroup apgroup WHERE apgroup.id = :id")
@NamedQuery(name = "@HQL_GET_SEARCH_GROUP_NAME", query = "FROM APGroup apgroup WHERE apgroup.name = :name")

public class APGroup implements Serializable {
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
