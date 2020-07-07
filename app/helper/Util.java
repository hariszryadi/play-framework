package helper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import play.Play;
import play.mvc.Http;

import java.io.File;
import java.io.IOException;

public class Util {
    public static final String BASE_IMAGE = Play.application().configuration().getString("application.base.image");

    public static String saveImage(Http.MultipartFormData.FilePart filePart, String type)
        throws IOException {

        String fileName = null;
        String generatedImageId = null;
        if (filePart!=null){
            fileName = filePart.getFilename();
            File file = filePart.getFile();
            generatedImageId = RandomStringUtils.random(10, false, true);
            FileUtils.moveFile(file, getFileImage(fileName, generatedImageId, type));
        }
        return generatedImageId + '.' + FilenameUtils.getExtension(fileName);
    }

    private static File getFileImage(String fileName, String id, String type){
        return new File("public/images/upload/" + type, id + "." + FilenameUtils.getExtension(fileName));
    }
}
