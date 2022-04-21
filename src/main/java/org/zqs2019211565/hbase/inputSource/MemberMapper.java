package org.zqs2019211565.hbase.inputSource;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.Text;
import java.io.IOException;

/**
 * 扩展自Mapper类，所有以HBase作为输入源的Mapper类需要继承该类
 */
public class MemberMapper extends TableMapper<Writable, Writable> {
    private Text k = new Text();
    private Text v = new Text();
    public static final String FIELD_COMMON_separator = "\u0001";

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        }

    @Override
    protected void map(ImmutableBytesWritable row, Result columns, Context context)
            throws IOException, InterruptedException {
        String value = null;
        // 获得行键值
        String rowkey = new String(row.get());

        // 一行中所有列族
        byte[] columnFamily = null;
        // 一行中所有列名
        byte[] columnQualifier = null;
        //时间戳
        long ts = 0L;

        try {
            // 遍历一行中所有列
            for (Cell cell : columns.listCells()) {
                // 单元格的值
                value = Bytes.toStringBinary(cell.getValueArray());
                // 获得一行中的所有列族
                columnFamily = cell.getFamilyArray();
                // 获得一行中的所有列名
                columnQualifier = cell.getQualifierArray();
                // 获得单元格的时间戳
                ts = cell.getTimestamp();
                k.set(rowkey);
                v.set(Bytes.toString(columnFamily) + FIELD_COMMON_separator + Bytes.toString(columnQualifier)
                        + FIELD_COMMON_separator + value + FIELD_COMMON_separator + ts);
                context.write(k, v);
            }
        } catch (Exception e) {
            //出错信息
            e.printStackTrace();
            System.err.println("Error:" + e.getMessage() + ",Row:" + Bytes.toString(row.get()) + ",Value" + value);
        }
    }
}

