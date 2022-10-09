package org.devcloud.ap.database;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;

@Entity
@Table(name = "group")
public class PgGroup implements Serializable {
    @Id
    @GenericGenerator(name = "generator", strategy = "increment")
    @GeneratedValue(generator = "generator")
    @Column(name = "group_id")
    @Getter @Setter Integer id;
    @Column(name = "group_name")
    @Getter @Setter String name;
    @Column(name = "group_color")
    @Getter @Setter Integer color;
    @Getter String table = "FROM group";
}
