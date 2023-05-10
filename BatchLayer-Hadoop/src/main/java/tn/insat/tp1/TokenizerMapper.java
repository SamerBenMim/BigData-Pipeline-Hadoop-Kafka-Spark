package tn.insat.tp1;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.StringTokenizer;

public class TokenizerMapper
        extends Mapper<Object, Text, Text, FloatWritable>{

    private Text asset = new Text();
    private FloatWritable count = new FloatWritable();
    String[] assetNames = {
            "Bitcoin Cash",
            "Binance Coin",
            "Bitcoin",
            "EOS.IO",
            "Ethereum Classic",
            "Ethereum",
            "Litecoin",
            "Monero",
            "TRON",
            "Stellar",
            "Cardano",
            "IOTA",
            "Maker",
            "Dogecoin"
    };
    public void map(Object key, Text value, Mapper.Context context) throws IOException, InterruptedException {

        String[] columns = value.toString().split(",");

        if (columns.length == 10) {
            // Extract the 'magasin' and 'cout' values
            asset.set(new Text(assetNames[(Integer.parseInt(columns[1].toString()))]));
            count.set(Float.parseFloat(columns[7]));
            System.out.println(asset);
            System.out.println(count);
            context.write(asset, count);
        }
    }
}

