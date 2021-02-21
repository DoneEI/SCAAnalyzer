package base.utils;

import java.io.*;

/**
 * Serialization Utils
 * 
 * @Author: DoneEI
 * @Since: 2021/1/21 3:10 下午
 **/
public class SerializationUtils {

    /**
     * 序列化对象
     * 
     * @param object
     *            待序列化的对象
     * @param filePath
     *            存储的位置
     */
    public static void serialize(Object object, String filePath, String fileName) throws IOException {
        File objFile = new File(filePath + File.separator + fileName);

        FileOutputStream fileOutputStream;

        if (!objFile.exists()) {
            // 创建文件夹路径
            File f = new File(filePath);
            f.mkdirs();

            // 创建文件
            objFile.createNewFile();
        }

        fileOutputStream = new FileOutputStream(objFile);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

        // 序列化 对象
        objectOutputStream.writeObject(object);
        objectOutputStream.close();
    }

    /**
     * 反序列化
     * 
     * @param filePath
     *            对象序列化存储的文件位置
     * @param <T>
     *            泛型
     * @return 对象
     */
    public static <T> T deserialize(String filePath) throws Exception {
        File file = new File(filePath);

        if (!file.exists()) {
            return null;
        }
        FileInputStream inputStream = new FileInputStream(file);
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

        return (T)objectInputStream.readObject();
    }

}
