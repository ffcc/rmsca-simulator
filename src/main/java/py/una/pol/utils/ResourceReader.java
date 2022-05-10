package py.una.pol.utils;


import java.io.InputStream;

public class ResourceReader {

    public static InputStream getFileFromResourceAsStream(String fileName) {

        // The class loader that loaded the class
        fileName = "topology/" + fileName;
        ClassLoader classLoader = ResourceReader.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }

    }
}
