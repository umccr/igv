package org.igv.feature;

import org.broad.igv.util.FileUtils;
import org.broad.igv.util.ResourceLocator;
import org.igv.tribble.AbstractFeatureReader;
import org.igv.tribble.FeatureCodec;
import org.igv.tribble.FeatureReader;

/**
 * Created by jrobinso on 1/12/18.
 */
public class FeatureReaderFactory {

    public static FeatureReader getReader(ResourceLocator locator) {


        FeatureCodec codec = new BEDCodec();
        String idxPath = ResourceLocator.indexFile(locator);
        boolean indexExists = FileUtils.resourceExists(idxPath);
        boolean indexRequired = false;

        return AbstractFeatureReader.getFeatureReader(locator.getPath(), idxPath, codec, indexRequired || indexExists);

    }


}
