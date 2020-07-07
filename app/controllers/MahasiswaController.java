package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Transaction;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import constan.DatatablesConstant;
import helper.Util;
import models.Jurusan;
import models.Mahasiswa;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class MahasiswaController extends Controller {

    public Result index() {
        return ok(views.html.admin.mahasiswa.list.render());
    }

    public Result add() {
        Form<Mahasiswa> data = Form.form(Mahasiswa.class);
        List<Jurusan> jurusans = Jurusan.find.all();
        return ok(views.html.admin.mahasiswa.form.render("Mahasiswa", "Add", routes.MahasiswaController.store(), data, jurusans));
    }

    public Result store() {
        Transaction tx = Ebean.beginTransaction();

        Http.MultipartFormData body = request().body().asMultipartFormData();
        Map<String, String[]> map = body.asFormUrlEncoded();

        Http.MultipartFormData.FilePart mahasiswaImage = null;

        Mahasiswa mahasiswa = new Mahasiswa();
        try {
            mahasiswa.name = map.get("name")[0];
            mahasiswa.nim = map.get("nim")[0];
            mahasiswa.jurusan = Jurusan.find.byId(Long.parseLong(map.get("jurusanID")[0]));
            mahasiswa.angkatan = map.get("angkatan")[0];
            mahasiswa.email= map.get("email")[0];
            mahasiswa.birth= new Date();
            mahasiswa.address = map.get("address")[0];
            mahasiswa.no_phone= map.get("no_phone")[0];

            mahasiswaImage = body.getFile("image");
            if(mahasiswaImage!=null){
                mahasiswa.image = Util.saveImage(mahasiswaImage, "avatars");
            }

            mahasiswa.save();
            tx.commit();

            flash("success", "Mahasiswa " + map.get("name")[0] + " has been created");

        } catch (Exception e) {
            tx.rollback();
            Logger.error("Error", e);
        }finally {
            tx.end();
        }
        return redirect(routes.MahasiswaController.index());
    }

    public Result listMahasiswa() {
        try {
            Map<String, String[]> parameters = request().queryString();

            int totalNumberOfMahasiswa = Mahasiswa.find.findRowCount();
            String searchParam = parameters.get(DatatablesConstant.DATATABLE_PARAM_SEARCH)[0];
            int pageSize = Integer.parseInt(parameters.get(DatatablesConstant.DATATABLE_PARAM_DISPLAY_LENGTH)[0]);
            int page = Integer.valueOf(parameters.get(DatatablesConstant.DATATABLE_PARAM_DISPLAY_START)[0]) / pageSize;
            String sortBy = "id";
            String order = parameters.get(DatatablesConstant.DATATABLE_PARAM_SORTING_ORDER)[0];
            Integer sortingColumnId = Integer.valueOf(parameters.get(DatatablesConstant.DATATABLE_PARAM_SORTING_COLUMN)[0]);

            switch (sortingColumnId) {
                case 2: sortBy = "name"; break;
                case 3: sortBy = "nim"; break;
                case 4: sortBy = "jurusan"; break;
            }

            PagedList<Mahasiswa> mahasiswaPage = Mahasiswa.page(page, pageSize, sortBy, order, searchParam);

            ObjectNode result = Json.newObject();

            result.put("draw", parameters.get(DatatablesConstant.DATATABLE_PARAM_DRAW)[0]);
            result.put("recordsTotal", totalNumberOfMahasiswa);
            result.put("recordsFiltered", mahasiswaPage.getTotalRowCount());

            ArrayNode an = result.putArray("data");
            int num = Integer.valueOf(parameters.get("start")[0]) + 1;
            for (Mahasiswa c : mahasiswaPage.getList()) {
                ObjectNode row = Json.newObject();
                String action = "";
//                action += "&nbsp;<a href=\"javascript:detailMahasiswa(" + c.id + ");\" class=\"btn btn-info\" data-toggle=\"modal\" data-target=\"#myModal\"><i class=\"fa fa-info-circle\"></i>&nbspDetail</a>";
                action += "&nbsp;<a href=\"mahasiswa/detail/" + c.id + "\" class=\"btn btn-info\" data-toggle=\"modal\" data-target=\"#myModal\"><i class=\"fa fa-info-circle\"></i></a>";
                action += "&nbsp;<a href=\"mahasiswa/edit/" + c.id + "\" class=\"btn btn-success\"><i class=\"fa fa-edit\"></i></a>";
                action += "&nbsp;<a href=\"javascript:deleteDataMahasiswa(" + c.id + ");\" class=\"btn btn-danger\"><i class=\"fa fa-remove\"></i></a>&nbsp;";

                row.put("0", num);
                row.put("1", c.name);
                row.put("2", c.nim);
                row.put("3", c.jurusan.name);
                row.put("4", c.angkatan);
                row.put("5", action);
                an.add(row);
                num++;
            }

            return ok(Json.toJson(result));

        } catch (NumberFormatException e) {
            return badRequest();
        }
    }

    public Result edit(Long id) {
        Mahasiswa mahasiswa = Mahasiswa.find.byId(id);
        List<Jurusan> jurusans = Jurusan.find.all();

        Form<Mahasiswa> mahasiswaForm;
        if (mahasiswa!=null){
            mahasiswaForm = Form.form(Mahasiswa.class).fill(mahasiswa);
        }else {
            flash("error", "falied to edit data");
            return redirect(routes.MahasiswaController.index());
        }
        return ok(views.html.admin.mahasiswa.form.render("Mahasiswa", "Edit", routes.MahasiswaController.update(), mahasiswaForm, jurusans));
    }

    public Result update() {
        try{
            Ebean.beginTransaction();
            //Binding data from form view
            Form<Mahasiswa> mahasiswaForm = Form.form(Mahasiswa.class).bindFromRequest();
            //Binding data as multipartFormData to get file
            Http.MultipartFormData multipartFormData = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart mahasiswaImage = multipartFormData.getFile("image");
            //Validation
            if (mahasiswaForm.hasErrors()){
                flash("error", mahasiswaForm.get().name + " failed update");
            }

            Mahasiswa mahasiswa = mahasiswaForm.get();
            if (mahasiswaImage!=null){
                mahasiswa.image = Util.saveImage(mahasiswaImage, "avatars");
            }
            mahasiswa.update();
            Ebean.commitTransaction();
            flash("success", mahasiswaForm.get().name + " success updated");
        }catch (Exception e){
            flash("error", e.getMessage());
            Ebean.rollbackTransaction();
        }finally {
            Ebean.endTransaction();
        }
        return redirect(routes.MahasiswaController.index());
    }

    public Result delete(Long id) {
        ObjectNode nodes = Json.newObject();

        Mahasiswa mahasiswa = Mahasiswa.find.byId(id);
        if (mahasiswa != null){
            mahasiswa.delete();
            return ok(Json.toJson(nodes.put("message", "success deleted !")));
        }else {
            return badRequest(Json.toJson(nodes.put("message", "failed deleted !")));
        }
    }

    public Result detail(Long id){
        List<Mahasiswa> data = Mahasiswa.find.where().eq("id", id).findList();
        return ok(views.html.admin.mahasiswa.detail.render(id, data));
    }


}
