package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.PagedList;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import constan.DatatablesConstant;
import models.Admin;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Map;

public class    AdminController extends Controller {

    public Result index() {
        return ok(views.html.admin.admin.list.render());
    }

    public Result add() {
        Form<Admin> data = Form.form(Admin.class);
        return ok(views.html.admin.admin.form.render("Admin", "Add", routes.AdminController.store(), data));
    }

    public Result store() {
        try {
            Ebean.beginTransaction();
            //Binding data from form / view
            DynamicForm adminForm = Form.form().bindFromRequest();
            Admin admin = new Admin();
            admin.name = adminForm.data().get("name");
            admin.email = adminForm.data().get("email");
            admin.password = adminForm.data().get("password");
            admin.phoneNumber = adminForm.data().get("phoneNumber");

            //Save to table
            admin.save();
            Ebean.commitTransaction();
            flash("success", "Admin " + adminForm.data().get("name") + " has been created");
        } catch (Exception e) {
            Ebean.rollbackTransaction();
            flash("error", e.getMessage());
        } finally {
            Ebean.endTransaction();
        }
        return redirect(routes.AdminController.index());
    }

    public Result listAdmin() {
        try {
            Map<String, String[]> parameters = request().queryString();

            int totalNumberOfAdmin = Admin.find.findRowCount();
            String searchParam = parameters.get(DatatablesConstant.DATATABLE_PARAM_SEARCH)[0];
            int pageSize = Integer.parseInt(parameters.get(DatatablesConstant.DATATABLE_PARAM_DISPLAY_LENGTH)[0]);
            int page = Integer.valueOf(parameters.get(DatatablesConstant.DATATABLE_PARAM_DISPLAY_START)[0]) / pageSize;
            String sortBy = "id";
            String order = parameters.get(DatatablesConstant.DATATABLE_PARAM_SORTING_ORDER)[0];
            Integer sortingColumnId = Integer.valueOf(parameters.get(DatatablesConstant.DATATABLE_PARAM_SORTING_COLUMN)[0]);

            switch (sortingColumnId) {
                case 2:
                    sortBy = "name";
                    break;
                case 3:
                    sortBy = "email";
                    break;
            }

            PagedList<Admin> adminPage = Admin.page(page, pageSize, sortBy, order, searchParam);

            ObjectNode result = Json.newObject();

            result.put("draw", parameters.get(DatatablesConstant.DATATABLE_PARAM_DRAW)[0]);
            result.put("recordsTotal", totalNumberOfAdmin);
            result.put("recordsFiltered", adminPage.getTotalRowCount());

            ArrayNode an = result.putArray("data");
            int num = Integer.valueOf(parameters.get("start")[0]) + 1;
            for (Admin c : adminPage.getList()) {
                ObjectNode row = Json.newObject();
                String action = "";
                action += "&nbsp;<a href=\"admin/edit/" + c.id + "\" class=\"btn btn-success\"><i class =\"fa fa-edit\"></i></a>";
                action += "&nbsp;<a href=\"javascript:deleteDataAdmin(" + c.id + ");\" class=\"btn btn-danger\"><i class=\"fa fa-remove\"></i></a>&nbsp;";

                row.put("0", num);
                row.put("1", c.name);
                row.put("2", c.email);
                row.put("3", c.phoneNumber);
                row.put("4", action);
                an.add(row);
                num++;
            }
            return ok(Json.toJson(result));
        } catch (NumberFormatException e) {
            return badRequest();
        }
    }

    public Result edit(Long id) {
        Admin admin = Admin.find.byId(id);

        Form<Admin> adminForm;
        if (admin != null){
            adminForm = Form.form(Admin.class).fill(admin);
        }else {
            flash("error", "failed to edit data");
            return redirect(routes.AdminController.index());
        }
        return ok(views.html.admin.admin.form.render("Admin", "Edit", routes.AdminController.update(), adminForm));
    }

    public Result update() {
        try {
            Ebean.beginTransaction();
            //binding data from form view
            Form<Admin> adminForm = Form.form(Admin.class).bindFromRequest();
            //valitaion
            if (adminForm.hasErrors()){
                flash("error", "Please correct the form below");
                return badRequest(views.html.admin.admin.form.render("Admin", "Edit", routes.AdminController.update(), adminForm));
            }

            Admin admin = adminForm.get();
            admin.update();
            Ebean.commitTransaction();
            flash("success", adminForm.get().name + " success to update");
        }catch (Exception e){
            flash("error", e.getMessage());
            Ebean.rollbackTransaction();
        }finally {
            Ebean.endTransaction();
        }
        return redirect(routes.AdminController.index());
    }

    public Result delete(Long id) {
        ObjectNode result = Json.newObject();

        Admin admin = Admin.find.byId(id);
        if (admin != null) {
            admin.delete();
            return ok(Json.toJson(result.put("message", "Success deleted!")));
        } else {
            return badRequest(Json.toJson(result.put("message", "Failed deleted!")));
        }
    }
}
