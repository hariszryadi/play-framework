package models;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.avaje.ebean.PagedList;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "mahasiswa")
public class Mahasiswa extends Model {
    @Id
    @SequenceGenerator(name = "name_mahasiswa_seq", sequenceName = "mahasiswa_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "name_mahasiswa_seq")
    public Long id;

    @Column(name = "name")
    public String name;

    @Column(name = "image", columnDefinition = "TEXT")
    public String image;

    @Column(name = "nim")
    public String nim;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jurusan_id")
    public Jurusan jurusan;

    @Column(name = "angkatan")
    public String angkatan;

    @Column(name = "email", unique = true)
    public String email;

    @Column(name = "birth")
    public Date birth;

    @Constraints.MinLength(value = 11, message = "Minimum value 11")
    @Constraints.MaxLength(value = 12, message = "Maximum value 12")
    @Column(name = "no_phone", nullable = false, length = 45)
    public String no_phone;

    @Column(name = "address")
    public String address;

    @Transient
    public Long jurusanID;

    public static Finder<Long, Mahasiswa> find = new Finder<>(Long.class, Mahasiswa.class);

    public static PagedList<Mahasiswa> page(int page, int pageSize, String sortBy, String order, String filter) {
        return find
                .fetch("jurusan")
                .where()
                .like("name", "%" + filter + "%")
                .or(
                        Expr.ilike("jurusan.name", "%" + filter + "%"),
                        Expr.ilike("nim", "%" + filter + "%")
                )
                .orderBy(sortBy + " " + order)
                .findPagedList(page, pageSize);
    }


}
