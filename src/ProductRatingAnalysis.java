import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class ProductRatingAnalysis {

    public static class RatingMapper
            extends Mapper<Object, Text, Text, Text> {

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            String line = value.toString();

            if (line.startsWith("product_id")) {
                return;
            }

            String[] fields = line.split(",");

            if (fields.length == 3) {
                String productName = fields[1].trim();
                String rating = fields[2].trim();

                context.write(new Text(productName), new Text(rating));
            }
        }
    }

    public static class RatingReducer
            extends Reducer<Text, Text, Text, Text> {

        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            int total = 0;
            int count = 0;

            for (Text value : values) {
                total += Integer.parseInt(value.toString());
                count++;
            }

            double average = (double) total / count;

            String result = "Total Ratings: " + total
                    + ", Number of Reviews: " + count
                    + ", Average Rating: " + String.format("%.2f", average);

            context.write(key, new Text(result));
        }
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.err.println("Usage: ProductRatingAnalysis <input> <output>");
            System.exit(2);
        }

        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "Product Rating Analysis");

        job.setJarByClass(ProductRatingAnalysis.class);

        job.setMapperClass(RatingMapper.class);
        job.setReducerClass(RatingReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
