package org.zqs2019211565.hbase.inputSource;


/**
 * @Author: zqiusen@qq.com
 * @Date: 2022/4/8 18:21
 */
import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/**
 * 实验二map-reduce
 */
public class WordCount {

    /**
     * TokenizerMapper继承Mapper类, 实现map函数
     * Object,Text是输入的key/value数据类型, Text,IntWritable是输出的key/value数据类型
     */
    public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {
        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        /**
         * map 函数负责统计输入文件中单词的数量
         */
        // @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            /**
             * StringTokenizer类自动将输入的字符串进行分词操作，
             * 并通过迭代器nextToken()方法依次取出所有单词
             */
            StringTokenizer itr = new StringTokenizer(value.toString());
            while (itr.hasMoreTokens()) {
                word.set(itr.nextToken());
                //context的wirte()方法向HDFS中写入中间结果
                context.write(word, one);
            }
        }
    }

    /**
     * WordCount中添加IntSumReducer类，并在该类中实现reduce函数； reduce函数合并之前map函数统计的结果，并输出最终结果；
     */
    public static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        /**
         *
         * @param key 输入的key
         * @param values 一个key对应的多个value
         * @param context 运行上下文对象context
         * @throws IOException
         * @throws InterruptedException
         */
        // @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {

            int sum = 0;
            //一个key对应多个value,将其进行累加即可
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        //该类主要是读取MapReduce系统配置信息
        Configuration conf = new Configuration();

        /**
         * 限定输出参数必须为2个，If的语句: 运行WordCount程序时候一定是两个参数，如果不是就会输出错误提示并退出
         */
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if (otherArgs.length != 2) {
            System.err.println("Usage: wordcount <in> <out>");
            System.exit(2);
        }
        // 装载程序和类
        Job job = new Job(conf, "word count");
        // 装载程序员编写好的计算程序
        job.setJarByClass(WordCount.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        // 定义输出的key/value的类型，也就是最终存储在HDFS上结果文件的key/value的类型。
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        // 构建输入的数据文件, 从参数读入
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        // 构建输出的数据文件, 从参数读入
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        // 如果job运行成功了，程序就会正常退出。
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}

