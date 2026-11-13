package org.hapiserver.source.tilde.geonet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hapiserver.AbstractHapiRecord;
import org.hapiserver.AbstractHapiRecordSource;
import org.hapiserver.HapiRecord;
import org.hapiserver.TimeString;
import org.hapiserver.source.AggregationGranuleIterator;
import org.hapiserver.source.SourceUtil;

/**
 *
 * @author jbf
 */
public class DartDataHapiRecordSource extends AbstractHapiRecordSource {

    String id;
    String location;
    
    public DartDataHapiRecordSource( String id ) {
        if ( id.charAt(3)!='_') throw new IllegalArgumentException("expecting underscore in string, as in NZA_40");
        this.id= id.substring(0,3);
        this.location= id.substring(id.length()-2);
    }
    
    @Override
    public boolean hasGranuleIterator() {
        return true;
    }

    @Override
    public Iterator<TimeString[]> getGranuleIterator(TimeString start, TimeString stop) {
        return new AggregationGranuleIterator("$Y-$m-$d", start, stop );
    }

    
    @Override
    public boolean hasParamSubsetIterator() {
        return false;
    }

    @Override
    public Iterator<HapiRecord> getIterator(TimeString start, TimeString stop) {
        try {
            String requestTemplate= "https://tilde.geonet.org.nz/v4/data/dart/%s/water-height/%s/15s/nil/%s/%s";
            String starts= String.format( "%04d-%02d-%02d",start.getYear(),start.getMonth(),start.getDay());
            String request= String.format( requestTemplate, id, location, starts, starts );
            URL url= new URL(request);
            
            String json= SourceUtil.getAllFileLines(url);
            JSONArray ja;
            try {
                ja = new JSONArray(json);
                JSONObject stationData= ja.getJSONObject(0);
                JSONArray data;
                if ( stationData.has("data") ) {
                    data= stationData.getJSONArray("data");
                } else {
                    data= new JSONArray(0);
                }
                return getRecordIterator( data );
            } catch (JSONException ex) {
                throw new RuntimeException(ex);
            }
            
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Iterator<HapiRecord> getRecordIterator(final JSONArray array) {
        final Iterator<JSONObject> records = new Iterator<JSONObject>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < array.length();
            }

            @Override
            public JSONObject next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                try {
                    return array.getJSONObject(i++);
                } catch (JSONException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        
        return new Iterator<HapiRecord>() {
            @Override
            public boolean hasNext() {
                return records.hasNext();
            }

            @Override
            public HapiRecord next() {
                JSONObject jo= records.next();
                
                return new AbstractHapiRecord() {
                    @Override
                    public String getIsoTime(int i) {
                        assert i==0;
                        return jo.optString("ts");
                    }

                    @Override
                    public double getDouble(int i) {
                        assert i==1;
                        return Double.parseDouble(jo.optString("val"));
                    }

                    @Override
                    public int length() {
                        return 2;
                    }
                };
            }
            
        };
    }
    
    
    
    
}
