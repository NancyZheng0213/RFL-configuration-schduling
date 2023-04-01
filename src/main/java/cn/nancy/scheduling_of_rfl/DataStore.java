package cn.nancy.scheduling_of_rfl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import com.google.gson.Gson;

public class DataStore {
    public static void createNewFile(String filename) {
        try {
            File file = new File(filename);
            if(file.exists()) {
                file.delete();
            }
            file.createNewFile(); // 创建新文件
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * 写入 txt，续写
     * @param data
     * @param filename
     */
    public static void writefile(String data, String filename) {
        try {
            File file = new File(filename);
            FileOutputStream fos = new FileOutputStream(file,true);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);
            bw.write(data);
            bw.newLine();
            bw.flush();
            bw.close();
            osw.close();
            fos.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    /**
     * 写入 csv，续写
     * @param data
     * @param filename
     */
    public static void writecsv(String data, String filename) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true));
            bw.write(data);
            bw.newLine();
            bw.flush();
            bw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String TimetabletoJOSN(Object TimeTable) {
        Gson gson = new Gson();
        String gsonString = gson.toJson(TimeTable);

        return gsonString;
    }

    public static void createNewDirectorys(String path) {
        File file = new File(path);
        if (!file.exists()) {
            // System.out.println("已创建文件夹" + path);
            file.mkdirs();
        }
    }
}
