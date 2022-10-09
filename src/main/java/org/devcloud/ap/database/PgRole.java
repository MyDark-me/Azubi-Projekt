package org.devcloud.ap.database;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;

@RequiredArgsConstructor @NoArgsConstructor
@Getter @Setter
@Entity
@Table(name = "pgRole")
public class PgRole implements Serializable {
    @GenericGenerator(name = "generator", strategy = "increment")
    @Id
    @GeneratedValue(generator = "generator")
    @Column(nullable = false, name = "role_id")
    Integer id;
    @Column(unique = true, nullable = false, name = "role_name")
    @NonNull String name;
    @Column(nullable = false, name = "role_color")
    @NonNull Integer color;
}
