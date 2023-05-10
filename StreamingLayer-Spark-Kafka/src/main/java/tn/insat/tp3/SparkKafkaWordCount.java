package tn.insat.tp3;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.bson.Document;

import scala.Tuple2;

import java.util.*;

class Record {
    private long timestamp;
    private int id;
    private double vwap;
    private double low;
    private double high;
    private double count;

    public Record(long timestamp, int id, double vwap, double high , double  low , double count) {
        this.timestamp = timestamp;
        this.id = id;
        this.vwap = vwap;
        this.high = high ;
        this.low = low ;
        this.count = count;
    }

    public double getCount() {
        return count;
    }

    public double getLow() {
        return low;
    }

    public double getHigh() {
        return high;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getId() {
        return id;
    }


    public double getVwap() {
        return vwap;
    }
}
public class SparkKafkaWordCount {

    private SparkKafkaWordCount() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: SparkKafkaWordCount <zkQuorum> <group> <topics> <numThreads>");
            System.exit(1);
        }

        SparkConf sparkConf = new SparkConf().setAppName("SparkKafkaWordCount");
        JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, new Duration(1000));

        int numThreads = Integer.parseInt(args[3]);
        Map<String, Integer> topicMap = new HashMap<>();
        String[] topics = args[2].split(",");
        for (String topic : topics) {
            topicMap.put(topic, numThreads);
        }

        JavaPairReceiverInputDStream<String, String> messages =
                KafkaUtils.createStream(jssc, args[0], args[1], topicMap);

        JavaDStream<String> lines = messages.map(Tuple2::_2);

        JavaDStream<String> individualLines = lines.flatMap(batch -> Arrays.asList(batch.split("\n")).iterator());

        JavaDStream<Record> records = individualLines.map(line -> {
            String[] word = line.split(",");
            long timestamp = Long.parseLong(word[0].trim()) ; // Convert to milliseconds

            int id = Integer.parseInt(word[1].trim());
            double count = Double.parseDouble(word[2].trim());
            double high = Double.parseDouble(word[4].trim());
            double low = Double.parseDouble(word[5].trim());
            double vwap = Double.parseDouble(word[8].trim());

            return new Record(timestamp, id, vwap , high , low ,count);
        });

        // Save data to MongoDB
        records.foreachRDD(rdd -> {
            rdd.foreachPartition(partitionOfRecords -> {
                // MongoDB connection
                String connectionString = "mongodb+srv://sborcheni:XHJJVDb8SrAOfmig@cluster0.ymh6fip.mongodb.net/";
                MongoClientSettings settings = MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(connectionString))
                        .build();
                MongoClient mongoClient = MongoClients.create(settings);
                MongoDatabase database = mongoClient.getDatabase("BigData");
                MongoCollection<Document> collection = database.getCollection("samer");

                List<Document> documents = new ArrayList<>();

                partitionOfRecords.forEachRemaining(record -> {
                    Document document = new Document()
                            .append("time", record.getTimestamp())
                            .append("id", record.getId())
                            .append("count", record.getCount())
                            .append("high", record.getHigh())
                            .append("low", record.getLow())
                            .append("vwap", record.getVwap());
                    documents.add(document);
                });

                Document parentDocument = new Document().append("records", documents);

                collection.insertOne(parentDocument);
                mongoClient.close(); // Close the client after processing the partition
            });
        });
        jssc.start();
        jssc.awaitTermination();
    }
}

