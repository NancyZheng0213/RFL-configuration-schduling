package cn.nancy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import com.google.gson.Gson;

/**
 * 该类用于存储数据
 */
public class DataStore {
    public static void createNewFile(String filename) {
        try {
            File file = new File(filename);
            if(file.exists()) {
                if (file.delete()) {
                    System.out.println("文件" + filename + "已删除");
                } else {
                    System.out.println("文件" + filename + "未删除");
                }
            }
            file.createNewFile(); // 创建新文件
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
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

    public static String TimetabletoJOSN(Object TimeTable) {
        Gson gson = new Gson();
        String gsonString = gson.toJson(TimeTable);

        return gsonString;
    }
}
