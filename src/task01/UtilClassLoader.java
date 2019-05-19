package task01;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
/*
 * public interface Worker {
 * void doWork();
 * }
 * Программа с консоли построчно считывает код метода doWork. Код не должен требовать импорта дополнительных классов.
 * После ввода пустой строки считывание прекращается и считанные строки добавляются в тело метода public void doWork() в файле SomeClass.java.
 * Файл SomeClass.java компилируется программой (в рантайме) в файл SomeClass.class.
 * Полученный файл подгружается в программу с помощью кастомного загрузчика
 * Метод, введенный с консоли, исполняется в рантайме (вызывается у экземпляра объекта подгруженного класса)
 */

/**
 * Class UtilClassLoader
 */
public class UtilClassLoader extends ClassLoader {
    private StringBuilder inputTextEntered = new StringBuilder();

    /**
     * Read code from input console string
     */
    private void readDoWork() {
        String currentString;
        boolean interruptWord = false;
        Boolean boolInput = true;
        Scanner inputText = new Scanner(System.in);
        do {
            currentString = inputText.nextLine();
            if (currentString.equalsIgnoreCase("")) {
                interruptWord = true;
            } else {
                if (boolInput.equals(true)) {
                    this.inputTextEntered.append(currentString);
                    boolInput = false;
                } else {
                    this.inputTextEntered.append("\n").append(currentString);
                }
            }
        } while (!interruptWord);
    }

    /**
     * Add in body of the method if null string interrupt read
     *
     * @param addString Add string
     * @throws IOException Input-Output Exception
     */
    private void addReadStringInFile(String addString) throws IOException {
        Path pathClass = Paths.get("./src/task01/SomeClass.java");
        List<String> writeLines = Files.readAllLines(pathClass, StandardCharsets.UTF_8);

        int position = writeLines.size() - 2;
        writeLines.add(position, addString);
        Files.write(pathClass, writeLines, StandardCharsets.UTF_8);
    }

    /**
     * Loading class data
     *
     * @param className Current method
     * @return Byte array
     */
    private byte[] loadClassData(String className) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(className.replace(".", "/") + ".class");
        ByteArrayOutputStream byteAOS = new ByteArrayOutputStream();
        int length;
        try {
            assert inputStream != null;
            while ((length = inputStream.read()) != -1) {
                byteAOS.write(length);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return byteAOS.toByteArray();
    }

    /**
     * Finding class
     *
     * @param nameOfClass Name of class this project
     * @return Searching class
     */
    public Class<?> findClass(String nameOfClass) {
        byte[] arrayByte = loadClassData(nameOfClass);
        return defineClass(nameOfClass, arrayByte, 0, arrayByte.length);
    }

    /**
     * Read compile and Run class: SomeClass.java
     *
     * @throws IOException          Input=Output exception
     */
    void readAndCompileClass() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        readDoWork();
        addReadStringInFile(inputTextEntered.toString());
        File root = new File("./src/task01/");
        File sourceFile = new File(root, "SomeClass.java");
        sourceFile.getParentFile().mkdirs();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, sourceFile.getAbsolutePath());
        final URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{root.toURI().toURL()});
        final Class<SomeClass> cls = (Class<SomeClass>) Class.forName("task01.SomeClass", true, classLoader);
        Worker instance = cls.newInstance();
        instance.doWork();
    }
}
