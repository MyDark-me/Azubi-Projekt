package org.devcloud.ap.database;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;

@RequiredArgsConstructor @NoArgsConstructor
@Getter @Setter
@Entity
@Table(name = "pgGroup")
public class PgGroup implements Serializable {
    @GenericGenerator(name = "generator", strategy = "increment")
    @Id
    @GeneratedValue(generator = "generator")
    @Column(nullable = false, name = "group_id")
    Integer id;
    @Column(unique = true, nullable = false, name = "group_name")
    @NonNull String name;
    @Column(nullable = false, name = "group_color")
    @NonNull Integer color;
}
