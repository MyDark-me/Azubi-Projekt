package org.devcloud.ap.database;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;

@Entity
@Table(name = "role")
public class PgRole implements Serializable {
    @Id
    @GenericGenerator(name = "generator", strategy = "increment")
    @GeneratedValue(generator = "generator")
    @Column(name = "role_id")
    @Getter @Setter Integer id;
    @Column(name = "role_name")
    @Getter @Setter String name;
    @Column(name = "role_color")
    @Getter @Setter Integer color;
    @Getter String table = "FROM role";
}
