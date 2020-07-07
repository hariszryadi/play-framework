package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.PagedList;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import constan.DatatablesConstant;
import models.Jurusan;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Map;

public class JurusanController extends Controller {
    public Result index(){
        return ok(views.html.admin.jurusan.list.render());
    }

    public Result add(){
        Form<Jurusan> data = Form.form(Jurusan.class);
        return ok(views.html.admin.jurusan.form.render("Jurusan", "Add", routes.JurusanController.store(), data));
    }

    public Result store(){
        try {
            Ebean.beginTransaction();
            //Binding data from form / view
            DynamicForm jurusanForm = Form.form().bindFromRequest();
            Jurusan jurusan = new Jurusan();
            jurusan.name = jurusanForm.data().get("name");

            //Save to table
            jurusan.save();
            Ebean.commitTransaction();
            flash("success", "Jurusan " + jurusanForm.data().get("name") + " has been created");
        } catch (Exception e) {
            Ebean.rollbackTransaction();
            flash("error", e.getMessage());
        } finally {
            Ebean.endTransaction();
        }
        return redirect(routes.JurusanController.index());
    }

    public Result listJurusan(){
        try {
            Map<String, String[]> parameters = request().queryString();

            int totalNumberOfJurusan = Jurusan.find.findRowCount();
            String searchParam = parameters.get(DatatablesConstant.DATATABLE_PARAM_SEARCH)[0];
            int pageSize = Integer.parseInt(parameters.get(DatatablesConstant.DATATABLE_PARAM_DISPLAY_LENGTH)[0]);
            int page = Integer.valueOf(parameters.get(DatatablesConstant.DATATABLE_PARAM_DISPLAY_START)[0]) / pageSize;
            String sortBy = "id";
            String order = parameters.get(DatatablesConstant.DATATABLE_PARAM_SORTING_ORDER)[0];
            Integer sortingColumnId = Integer.valueOf(parameters.get(DatatablesConstant.DATATABLE_PARAM_SORTING_COLUMN)[0]);

            if (sortingColumnId == 2) {
                sortBy = "name";
            }

            PagedList<Jurusan> jurusanPage = Jurusan.page(page, pageSize, sortBy, order, searchParam);

            ObjectNode result = Json.newObject();

            result.put("draw", parameters.get(DatatablesConstant.DATATABLE_PARAM_DRAW)[0]);
            result.put("recordsTotal", totalNumberOfJurusan);
            result.put("recordsFiltered", jurusanPage.getTotalRowCount());

            ArrayNode an = result.putArray("data");
            int num = Integer.valueOf(parameters.get("start")[0]) + 1;
            for (Jurusan c : jurusanPage.getList()) {
                ObjectNode row = Json.newObject();
                String action = "";
                action += "&nbsp;<a href=\"jurusan/edit/" + c.id + "\" class=\"btn btn-success\"><i class =\"fa fa-edit\"></i></a>";
                action += "&nbsp;<a href=\"javascript:deleteDataJurusan(" + c.id + ");\" class=\"btn btn-danger\"><i class=\"fa fa-remove\"></i></a>&nbsp;";

                row.put("0", num);
                row.put("1", c.name);
                row.put("2", action);
                an.add(row);
                num++;
            }
            return ok(Json.toJson(result));
        } catch (NumberFormatException e) {
            return badRequest();
        }
    }

    public Result edit(Long id){
        Jurusan jurusan = Jurusan.find.byId(id);

        Form<Jurusan> jurusanForm;
        if (jurusan!=null){
            jurusanForm = Form.form(Jurusan.class).fill(jurusan);
        }else {
            flash("error", "failed to edit data");
            return redirect(routes.JurusanController.index());
        }
        return ok(views.html.admin.jurusan.form.render("Jurusan", "Edit", routes.JurusanController.update(), jurusanForm));
    }

    public Result update(){
        try {
            Ebean.beginTransaction();
            //binding data from form view
            Form<Jurusan> jurusanForm = Form.form(Jurusan.class).bindFromRequest();
            //validation
            if (jurusanForm.hasErrors()){
                flash("error", jurusanForm.get().name + " failed to update");
            }

            Jurusan jurusan = jurusanForm.get();
            jurusan.update();
            Ebean.commitTransaction();
            flash("success", jurusanForm.get().name + " success to update");
        }catch (Exception e){
            flash("error", e.getMessage());
            Ebean.rollbackTransaction();
        }finally {
            Ebean.endTransaction();
        }
        return redirect(routes.JurusanController.index());
    }

    public Result delete(Long id){
        ObjectNode nodes = Json.newObject();

        Jurusan jurusan = Jurusan.find.byId(id);
        if (jurusan != null){
            jurusan.delete();
            return ok(Json.toJson(nodes.put("message", "success deleted !")));
        }else {
            return badRequest(Json.toJson(nodes.put("message", "failed deleted !")));
        }
    }
}
