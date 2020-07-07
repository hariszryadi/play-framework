package models;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.avaje.ebean.PagedList;
import play.data.validation.Constraints;

import javax.persistence.*;

@Entity
@Table(name = "admin")
public class Admin extends Model {
    @Id
    @SequenceGenerator(name = "name_admin_seq", sequenceName = "admin_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "name_admin_seq")
    public Long id;

    @Constraints.Pattern(value = "^[a-zA-Z \\\\._\\\\-]+$", message = "name hanya diperbolehkan abjad")
    @Constraints.MinLength(value = 2, message = "Minimum value 2")
    @Constraints.MaxLength(value = 45, message = "Maximum value 50")
    @Constraints.Required
    @Column(name = "name")
    public String name;

    @Constraints.Required
    @Column(name = "email", unique = true)
    public String email;

    @Constraints.Required
    @Column(name = "password")
    public String password;

    @Constraints.Required
    @Transient
    public String confirmPassword;

    @Constraints.MinLength(value = 11, message = "Minimum value 11")
    @Constraints.MaxLength(value = 12, message = "Maximum value 12")
    @Column(name = "phoneNumber", nullable = false, length = 45)
    @Constraints.Required
    public String phoneNumber;

    public static Finder<Long, Admin> find = new Finder<>(Long.class, Admin.class);

    /**
     * Return a page of computer
     *
     * @param page     Page to display
     * @param pageSize Number of computers per page
     * @param sortBy   Computer property used for sorting
     * @param order    Sort order (either or asc or desc)
     * @param filter   Filter applied on the name column
     */
    public static PagedList<Admin> page(int page, int pageSize, String sortBy, String order, String filter) {
        return
                find.where()
                        .or(
                                Expr.ilike("name", "%" + filter + "%"),
                                Expr.or(
                                        Expr.ilike("email", "%" + filter + "%"),
                                        Expr.ilike("phoneNumber", "%" + filter + "%")
                                )
                        )
                        .orderBy(sortBy + " " + order)
                        .findPagedList(page, pageSize);
    }

}
