package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.PagedList;

import javax.persistence.*;

@Entity
@Table(name = "jurusan")
public class Jurusan extends Model {
    @Id
    @SequenceGenerator(name = "name_jurusan_seq", sequenceName = "jurusan_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "name_jurusan_seq")
    public Long id;

    @Column(name = "name")
    public String name;

    public static Finder<Long, Jurusan> find = new Finder<>(Long.class, Jurusan.class);

    public static PagedList<Jurusan> page (int page, int pageSize, String sortBy, String order, String filter){
        return
                find.where()
                .ilike("name", "%" + filter + "%")
                .orderBy(sortBy + " " + order)
                .findPagedList(page, pageSize);
    }
}
