import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
        Path pathClass = Paths.get("./src/SomeClass.java");
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
     * Read and compile class: SomeClass.java
     *
     * @throws IOException          Input=Output exception
     * @throws InterruptedException Interrupting exception
     */
    void readAndCompileClass() throws IOException, InterruptedException {
        readDoWork();
        addReadStringInFile(inputTextEntered.toString());
        Process compile = Runtime.getRuntime().exec(" javac  -d " + " ./out/production/lesson09" + " ./src/SomeClass.java");
        compile.waitFor();
        Process compileClass = Runtime.getRuntime().exec("java " + "SomeClass");
        compileClass.waitFor();
    }

    /**
     * Load and run SomeClass.class
     *
     * @throws IllegalAccessException    Illegal access exception
     * @throws InstantiationException    Instantiation exception
     * @throws NoSuchMethodException     No such method exception
     * @throws InvocationTargetException Invocation target exception
     */
    void loaderAndRunClass() throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        Class<?> someClass = findClass("SomeClass");
        Object obj = someClass.newInstance();
        Method method = someClass.getMethod("doWork");
        method.invoke(obj);
    }
}
