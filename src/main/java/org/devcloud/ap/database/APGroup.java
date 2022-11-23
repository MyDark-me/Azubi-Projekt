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
@NamedQuery(name = "@HQL_GET_SEARCH_GROUP_COUNT", query = "SELECT COUNT(*) FROM APGroup apgroup WHERE apgroup.name = :name")
@NamedQuery(name = "@HQL_GET_SEARCH_GROUP_ID", query = "FROM APGroup apgroup WHERE apgroup.id = :id")
@NamedQuery(name = "@HQL_GET_SEARCH_GROUP_NAME", query = "FROM APGroup apgroup WHERE apgroup.name = :name")

public class APGroup implements Serializable {
    @GenericGenerator(name = "generator", strategy = "increment")
    @Id
    @GeneratedValue(generator = "generator")
    @Column(name = "apgroup_id", nullable = false)
    Integer id;

    @Column(name = "apgroup_name", unique = true, nullable = false)
    @NonNull String name;

    @Column(name = "apgroup_color",nullable = false)
    @NonNull String color;
}
