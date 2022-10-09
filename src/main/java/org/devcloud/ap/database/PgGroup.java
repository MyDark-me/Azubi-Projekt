package org.devcloud.ap.database;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;

@RequiredArgsConstructor @NoArgsConstructor
@Entity
@Table(name = "pgGroup")
public class PgGroup implements Serializable {
    @GenericGenerator(name = "generator", strategy = "increment")
    @Id
    @GeneratedValue(generator = "generator")
    @Column(nullable = false, name = "group_id")
    @Getter @Setter Integer id;
    @Column(nullable = false, name = "group_name")
    @NonNull @Getter @Setter String name;
    @Column(nullable = false, name = "group_color")
    @NonNull @Getter @Setter Integer color;
}
