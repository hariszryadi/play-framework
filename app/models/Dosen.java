package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.PagedList;

import javax.persistence.*;

@Entity
@Table(name = "dosen")
public class Dosen extends Model {
    @Id
    @SequenceGenerator(name = "name_dosen_seq", sequenceName = "dosen_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "name_dosen_seq")
    public Long id;

    @Column(name = "name")
    public String name;

    @Column(name = "nrp")
    public String nrp;

    public static Finder<Long, Dosen> find = new Finder<>(Long.class, Dosen.class);

    public static PagedList<Dosen> page(int page, int pageSize, String sortBy, String order, String filter){
        return find.where()
                            .ilike("name", "%" + filter + "%")
                            .orderBy(sortBy + " " + order)
                            .findPagedList(page, pageSize);
    }
}
