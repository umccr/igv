package org.igv.feature;

import htsjdk.samtools.util.CloseableIterator;
import org.broad.igv.util.ResourceLocator;
import org.igv.tribble.Feature;
import org.igv.tribble.FeatureReader;
import org.igv.tribble.annotation.Strand;
import org.igv.tribble.readers.TabixReader;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Created by jrobinso on 1/12/18.
 */
public class FeatureReaderTest {

    @Test
    public void testBed() throws Exception {

        String testFile = "test/datafx/feature/bed12.bed";

        FeatureReader reader = FeatureReaderFactory.getReader(new ResourceLocator(testFile));

        CloseableIterator<Feature> iter = reader.iterator();

        // Test first feature in detail
        // chr8	127736068	127741434	NM_002467.4	0	+	127736593	127740958	0	3	555,772,1039,	0,2179,4327,

        Feature first = iter.next();
        assertEquals("chr8", first.getChr());
        assertEquals(127736068, first.getStart());
        assertEquals(127741434, first.getEnd());
        assertEquals("NM_002467.4", first.getName());
        assertEquals(0, first.getScore(), 1.0e-6);
        assertEquals(Strand.POSITIVE, first.getStrand());
        assertEquals(127736593, first.getThickStart());
        assertEquals(127740958, first.getThickEnd());
        assertEquals(null, first.getColor());
        assertEquals(3, first.getExons().size());

        int count = 1;
        while(iter.hasNext()) {
            iter.next();
            count++;
        }

        assertEquals(10, count);

        iter.close();


    }

}