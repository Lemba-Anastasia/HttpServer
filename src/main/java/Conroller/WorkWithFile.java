package Conroller;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class WorkWithFile {
    private static final Logger log = Logger.getLogger(WorkWithFile.class);

    public byte[] readFromBinaryFile(String documentAddress) {
        try {
            return Files.readAllBytes(Paths.get(new File(documentAddress).getPath()));
        } catch (IOException e) {
            log.info("\nReading from file error: " + e.getMessage());
            return null;
        }
    }

    public boolean existFile(String path, String fileName) {
        String pathOf = path;
        File f = new File(pathOf);
        File[] list = f.listFiles();
        if (list != null) {
            for (File file : list) {
                if (fileName.equals(file.getName())) {
                    return true;
                } else {
                    if (file.isDirectory()) {
                        pathOf = path + file.getName() + "/";
                        if (existFile(pathOf, fileName)) return true;
                    }
                }
            }
        }
        return false;
    }

    public String parseToGetFile(String documentAddress) {
        for (int i = documentAddress.length() - 1; i >= 0; i--) {
            if (documentAddress.charAt(i) == '/' || documentAddress.charAt(i) == '\\') {
                return documentAddress.substring(i + 1);
            }
        }
        return null;
    }

    public String getFileLocation(String path, String fileName) {
        String pathOf;
        File f = new File(path);
        File[] list = f.listFiles();
        if (list != null) {
            for (File file : list) {
                if (fileName.equals(file.getName())) {
                    return file.getAbsolutePath();
                } else {
                    pathOf = path;
                    if (file.isDirectory()) {
                        pathOf += file.getName() + "/";
                        if (!getFileLocation(pathOf, fileName).equals(""))
                            return file.getAbsolutePath() + "/" + fileName;
                    }
                }
            }
        }
        return "";
    }

    public Map<String, String> howThisFileWereMoving(String pageAddress) {
        String s = readFromFile("src\\main\\resources\\infoForPages.json");
        Type mapTypeLevel1 = new TypeToken<Map<String, Map<String, Map<String, String>>>>() {
        }.getType();
        Map<String, Map<String, Map<String, String>>> infoAboutHistoricLocationPagesMap = new Gson().fromJson(s, mapTypeLevel1);
        List mapMovedPages =infoAboutHistoricLocationPagesMap.values().stream().
                collect(Collectors.toCollection(ArrayList::new));
        Map<String, String> returnMap = new HashMap<>();
        for (Object map : mapMovedPages) {
            if (((Map<String,Map <String, String>>)map).get("movePerm")!=null) {
                if(((Map<String,Map <String, String>>)map).get("movePerm").get("old").equals(pageAddress)){
                    returnMap.put("statusMoving", "movePerm");
                    returnMap.put("newLocation", ((Map<String,Map <String, String>>)map).get("movePerm").get("new"));
                    return returnMap;
                }
            } else if (((Map<String,Map <String, String>>)map).get("moveTemp")!= null) {
                if(((Map<String,Map <String, String>>)map).get("moveTemp").get("old").equals(pageAddress)){
                    returnMap.put("statusMoving", "moveTemp");
                    returnMap.put("newLocation", ((Map<String,Map <String, String>>)map).get("moveTemp").get("new"));
                    return returnMap;
                }
            }
        }
        return returnMap;
    }

    public String readFromFile(String documentAddress) {
        FileReader fr = null;
        Scanner scanner;
        String string = "";
        try {
            fr = new FileReader(documentAddress);
            scanner = new Scanner(fr);
            while (scanner.hasNextLine()) {
                string += scanner.nextLine();
            }
        } catch (IOException e) {
            log.info("\nReading from file error: " + e.getMessage());
            return null;
        } finally {
            if (fr != null)
                try {
                    fr.close();
                } catch (IOException e) {
                    log.info("\nClose FileInputStream error: " + e.getMessage());
                }
        }
        return string;
    }

    public boolean isEndPointBelongToPublicPage(String endPoint) {
        File f = new File(ClientHandler.PATH_TO_RESOURCES + ClientHandler.COMMON_PAGES);
        String endP = parseToGetFile(endPoint);
        File[] list = f.listFiles();
        if (list != null) {
            for (File file : list) {
                if (endP.equals(file.getName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
