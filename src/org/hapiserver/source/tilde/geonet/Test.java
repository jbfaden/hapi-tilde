
package org.hapiserver.source.tilde.geonet;

import java.util.Iterator;
import org.hapiserver.HapiRecord;
import org.hapiserver.TimeString;

/**
 *
 * @author jbf
 */
public class Test {
    public static void main(String[] args) {
        System.err.println("Catalog: ");
        System.err.println(DartCatalog.getCatalog());
        System.err.println(DartCatalog.getInfo("NZL_41"));
        DartDataHapiRecordSource dd= new DartDataHapiRecordSource("NZA_40");
        System.err.println(dd);
        Iterator<TimeString[]> its= dd.getGranuleIterator( new TimeString("2020-05-01T00:00:00Z"), new TimeString("2020-05-10T00:00:00Z") );
        while ( its.hasNext() ) {
            TimeString[] tss= its.next();
            System.err.println( String.format("%s/%s", tss[0], tss[1] ) );
            Iterator<HapiRecord> hri= dd.getIterator( tss[0], tss[1] );
            while ( hri.hasNext() ) {
                HapiRecord hr= hri.next();
                System.err.println( String.format( "%s,%10.1f", hr.getIsoTime(0), hr.getDouble(1) ) );
            }
        }
        System.err.println("done");
    }
}
