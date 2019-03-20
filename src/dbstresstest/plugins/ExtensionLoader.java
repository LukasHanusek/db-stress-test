package dbstresstest.plugins;

import java.io.File;  
import java.lang.reflect.Constructor;  
import java.net.URL;  
import java.net.URLClassLoader;

/**
 * Source: https://stackabuse.com/example-loading-a-java-class-at-runtime/
 * 
 * Fixed jar.toURL() to jar.toURI().toURL() as the first function is deprecated since Java 6
 *
 * @param <C>
 */
public class ExtensionLoader<C> {

    /**
     * Loads instance of the given class from external jar file with the help of
     * reflection
     *
     * @param jarpath
     * @param classpath
     * @param parentClass
     * @return
     * @throws ClassNotFoundException
     */
    public C LoadClass(String jarpath, String classpath, Class<C> parentClass) throws ClassNotFoundException {
        File jar = new File(jarpath);
        try {
            ClassLoader loader = URLClassLoader.newInstance(new URL[]{jar.toURI().toURL()}, getClass().getClassLoader());
            Class<?> clazz = Class.forName(classpath, true, loader);
            Class<? extends C> newClass = clazz.asSubclass(parentClass); //we want to load main plugin class extending our class for plugins
            Constructor<? extends C> constructor = newClass.getConstructor(); //call the constructor
            return constructor.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new ClassNotFoundException("Class " + classpath + " wasn't found in directory");
    }
}
