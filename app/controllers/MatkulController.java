package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.PagedList;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import constan.DatatablesConstant;
import models.MataKuliah;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Map;

public class MatkulController extends Controller {
    public Result index(){
        return ok(views.html.admin.mata_kuliah.list.render());
    }

    public Result add(){
        Form<MataKuliah> data = Form.form(MataKuliah.class);
        return ok(views.html.admin.mata_kuliah.form.render("MataKuliah", "Add", routes.MatkulController.store(), data));
    }

    public Result store(){
        try {
            Ebean.beginTransaction();
            //binding data from form view
            DynamicForm matkulForm = Form.form().bindFromRequest();
            MataKuliah mataKuliah = new MataKuliah();
            mataKuliah.name = matkulForm.data().get("name");

            //save to table
            mataKuliah.save();
            Ebean.commitTransaction();
            flash("success", "Mata Kuliah " + matkulForm.data().get("name") + " has been created" );
        }catch (Exception e){
            Ebean.rollbackTransaction();
            flash("error", e.getMessage());
        }finally {
            Ebean.endTransaction();
        }
        return redirect(routes.MatkulController.index());
    }

    public Result listMatkul(){
        try {
            Map<String, String[]> parameters = request().queryString();

            int totalNumberOfMatkul = MataKuliah.find.findRowCount();
            String searchParam = parameters.get(DatatablesConstant.DATATABLE_PARAM_SEARCH)[0];
            int pageSize = Integer.parseInt(parameters.get(DatatablesConstant.DATATABLE_PARAM_DISPLAY_LENGTH)[0]);
            int page = Integer.valueOf(parameters.get(DatatablesConstant.DATATABLE_PARAM_DISPLAY_START)[0]) / pageSize;
            String sortBy = "id";
            String order = parameters.get(DatatablesConstant.DATATABLE_PARAM_SORTING_ORDER)[0];
            Integer sortingColumnId = Integer.valueOf(parameters.get(DatatablesConstant.DATATABLE_PARAM_SORTING_COLUMN)[0]);

            if (sortingColumnId == 2){
                sortBy = "name";
            }

            PagedList<MataKuliah> mataKuliahPage = MataKuliah.page(page, pageSize, sortBy, order, searchParam);

            ObjectNode result = Json.newObject();

            result.put("draw", parameters.get(DatatablesConstant.DATATABLE_PARAM_DRAW)[0]);
            result.put("recordsTotal", totalNumberOfMatkul);
            result.put("recordsFiltered", mataKuliahPage.getTotalRowCount());

            ArrayNode an = result.putArray("data");
            int num = Integer.valueOf(parameters.get("start")[0]) + 1;
            for (MataKuliah x : mataKuliahPage.getList()){
                ObjectNode row = Json.newObject();
                String action = "";
                action += "&nbsp;<a href=\"matkul/edit/" + x.id + "\" class=\"btn btn-success\"><i class =\"fa fa-edit\"></i></a>";
                action += "&nbsp;<a href=\"javascript:deleteDataMatkul(" + x.id + ");\" class=\"btn btn-danger\"><i class=\"fa fa-remove\"></i></a>&nbsp;";

                row.put("0", num);
                row.put("1", x.name);
                row.put("2", action);
                an.add(row);
                num++;
            }
            return ok(Json.toJson(result));
        }catch (NumberFormatException e){
            return badRequest();
        }
    }

    public Result edit(Long id){
        MataKuliah mataKuliah = MataKuliah.find.byId(id);

        Form<MataKuliah> mataKuliahForm;
        if (mataKuliah != null){
            mataKuliahForm = Form.form(MataKuliah.class).fill(mataKuliah);
        }else {
            flash("error", "failed to edit data");
            return redirect(routes.MatkulController.index());
        }
        return ok(views.html.admin.mata_kuliah.form.render("MataKuliah", "Edit", routes.MatkulController.update(), mataKuliahForm));
    }

    public Result update(){
        try {
            Ebean.beginTransaction();
            //binding data from form view
            Form<MataKuliah> mataKuliahForm = Form.form(MataKuliah.class).bindFromRequest();
            //validation
            if (mataKuliahForm.hasErrors()){
                flash("error", mataKuliahForm.get().name + " failed to update");
            }

            MataKuliah mataKuliah = mataKuliahForm.get();
            mataKuliah.update();
            Ebean.commitTransaction();
            flash("success", mataKuliahForm.get().name + " success updated");
        }catch (Exception e){
            flash("error", e.getMessage());
            Ebean.rollbackTransaction();
        }finally {
            Ebean.endTransaction();
        }
        return redirect(routes.MatkulController.index());
    }

    public Result delete(Long id){
        ObjectNode node = Json.newObject();
        MataKuliah mataKuliah = MataKuliah.find.byId(id);
        if (mataKuliah != null){
            mataKuliah.delete();
            return ok(Json.toJson(node.put("message", "success deleted !")));
        }else {
            return badRequest(Json.toJson(node.put("message", "failed deleted !")));
        }
    }
}
