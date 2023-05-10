package tn.insat.tp1;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IntSumReducer
        extends Reducer<Text,FloatWritable,Text,FloatWritable> {

    private FloatWritable result = new FloatWritable();
    HashMap<Integer, String> assetMap = new HashMap<Integer, String>();

    public void reduce(Text key, Iterable<FloatWritable> values,
                       Context context
    ) throws IOException, InterruptedException {
        int sum = 0;
        for (FloatWritable val : values) {
            System.out.println("value: "+val.get());
            sum += val.get();

        }

        System.out.println("--> Sum = "+sum);
        result.set(sum);


        context.write(key, result);
    }
}
