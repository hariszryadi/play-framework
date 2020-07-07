package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.PagedList;

import javax.persistence.*;

@Entity
@Table(name = "mata_kuliah")
public class MataKuliah extends Model {
    @Id
    @SequenceGenerator(name = "name_matkul_seq", sequenceName = "matkul_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "name_matkul_seq")
    public Long id;

    @Column(name = "name")
    public String name;

    public static Finder<Long, MataKuliah> find = new Finder<>(Long.class, MataKuliah.class);

    public static PagedList<MataKuliah> page(int page, int pageSize, String sortBy, String order, String filter){
        return
                find.where()
                        .ilike("name", "%" + filter + "%")
                        .orderBy(sortBy + " " + order)
                        .findPagedList(page, pageSize);
    }
}
