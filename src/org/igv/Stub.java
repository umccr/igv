package org.igv;


import java.io.File;

/**
 * Created by jrobinso on 1/12/18.
 *
 * Temporary class with static methods to be moved elsewhere.   Purpose is to let development proceed.
 *
 */
public class Stub {


    public static void loadFile(File file) {
        System.out.println("Loading " + file.getAbsolutePath());
    }

    public static void loadURL(String url,  String indexURL) {
        System.out.println("Loading " + url + "   index: " + indexURL);
    }

}
