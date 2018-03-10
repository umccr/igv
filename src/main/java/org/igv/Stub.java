package org.igv;


import org.broad.igv.util.ResourceLocator;
import org.igv.feature.FeatureTrack;

import java.io.File;

/**
 * Created by jrobinso on 1/12/18.
 *
 * Temporary class with static methods to be moved elsewhere.   Purpose is to let development proceed.
 *
 */
public class Stub {

    public static FeatureTrack theTrack;

    public static void loadFile(File file) {


        if(file.getAbsolutePath().endsWith(".bed")) {

            // Name is not correct; just using for now for mock-up purposes.
            String name = file.getName() + "_" + System.currentTimeMillis();
            theTrack = new FeatureTrack(name, new ResourceLocator(file.getAbsolutePath()));

        }
        else {
            System.out.println("Unsupported file format: " + file.getAbsolutePath());
        }
    }

    public static void loadURL(String url,  String indexURL) {
        System.out.println("Loading " + url + "   index: " + indexURL);
    }

}
