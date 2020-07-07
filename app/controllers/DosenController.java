package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.PagedList;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import constan.DatatablesConstant;
import models.Dosen;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Map;

public class DosenController extends Controller {
    public Result index(){
        return ok(views.html.admin.dosen.list.render());
    }

    public Result add(){
        Form<Dosen> data = Form.form(Dosen.class);
        return ok(views.html.admin.dosen.form.render("Dosen", "Add", routes.DosenController.store(), data));
    }

    public Result store(){
        try {
            Ebean.beginTransaction();
            //binding data from form view
            DynamicForm dosenForm = Form.form().bindFromRequest();
            Dosen dosen = new Dosen();
            dosen.name = dosenForm.data().get("name");
            dosen.nrp = dosenForm.data().get("nrp");

            //save to table
            dosen.save();
            Ebean.commitTransaction();
            flash("success", "Dosen " + dosenForm.data().get("name") + " has been created");
        }catch (Exception e){
            Ebean.rollbackTransaction();
            flash("error", e.getMessage());
        }finally {
            Ebean.endTransaction();
        }
        return redirect(routes.DosenController.index());
    }

    public Result listDosen(){
        try {
            Map<String, String[]> map = request().queryString();

            int totalNumberOfDosen = Dosen.find.findRowCount();
            String searchParam = map.get(DatatablesConstant.DATATABLE_PARAM_SEARCH)[0];
            int pageSize = Integer.parseInt(map.get(DatatablesConstant.DATATABLE_PARAM_DISPLAY_LENGTH)[0]);
            int page = Integer.valueOf(map.get(DatatablesConstant.DATATABLE_PARAM_DISPLAY_START)[0]) / pageSize;
            String sortBy = "id";
            String order = map.get(DatatablesConstant.DATATABLE_PARAM_SORTING_ORDER)[0];
            Integer sortingColumnId = Integer.valueOf(map.get(DatatablesConstant.DATATABLE_PARAM_SORTING_COLUMN)[0]);

            switch (sortingColumnId){
                case 2:
                    sortBy = "name";
                    break;
                case 3:
                    sortBy = "nrp";
                    break;
            }

            PagedList<Dosen> dosenPage = Dosen.page(page, pageSize, sortBy, order, searchParam);
            ObjectNode result = Json.newObject();

            result.put("draw", map.get(DatatablesConstant.DATATABLE_PARAM_DRAW)[0]);
            result.put("recordsTotal", totalNumberOfDosen);
            result.put("recordsFiltered", dosenPage.getTotalRowCount());

            ArrayNode arrayNode = result.putArray("data");
            int num = Integer.valueOf(map.get("start")[0]) + 1;
            for (Dosen c : dosenPage.getList()){
                ObjectNode row = Json.newObject();
                String action = " ";
                action += "&nbsp;<a href=\"dosen/edit/" + c.id + "\" class=\"btn btn-success\"><i class=\"fa fa-edit\"></i></a>";
                action += "&nbsp;<a href=\"javascript:deleteDataDosen(" + c.id + ");\" class=\"btn btn-danger\"><i class=\"fa fa-remove\"></i></a>&nbsp";

                row.put("0", num);
                row.put("1", c.name);
                row.put("2", c.nrp);
                row.put("3", action);
                arrayNode.add(row);
                num++;
            }
            return ok(Json.toJson(result));

        }catch (NumberFormatException e){
            return badRequest();
        }
    }

    public Result edit(Long id){
        Dosen dosen = Dosen.find.byId(id);
        Form<Dosen> dosenForm;
        if (dosen!=null){
            dosenForm = Form.form(Dosen.class).fill(dosen);
        }else {
            flash("error", "failed to edit data");
            return redirect(routes.DosenController.index());
        }
        return ok(views.html.admin.dosen.form.render("Dosen", "Edit", routes.DosenController.update(),dosenForm));
    }

    public Result update(){
        try{
            Ebean.beginTransaction();
            //binding data from form view
            Form<Dosen> dosenForm = Form.form(Dosen.class).bindFromRequest();
            //validation
            if (dosenForm.hasErrors()){
                flash("error", dosenForm.get().name + " failed to update");
                return redirect(routes.DosenController.index());
            }
            Dosen dosen = dosenForm.get();

            dosen.update();
            Ebean.commitTransaction();
            flash("success", dosenForm.get().name + " success to update");
        }catch (Exception e){
            Ebean.rollbackTransaction();
            flash("error", e.getMessage());
        }finally {
            Ebean.endTransaction();
        }
        return redirect(routes.DosenController.index());
    }

    public Result delete(Long id){
        ObjectNode node = Json.newObject();

        Dosen dosen = Dosen.find.byId(id);
        if (dosen != null){
            dosen.delete();
            return ok(Json.toJson(node.put("message", "success deleted !")));
        }else {
            return badRequest(Json.toJson(node.put("message", "failed deleted !")));
        }
    }
}
