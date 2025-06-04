package kia.example.springbatch.partitioning;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
@Component
public class ColumnRangePartitioner implements Partitioner {
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> partitions = new HashMap<>();
        int min = 1; // Minimum ID (can also be fetched from the database)
        int max = 10; // Maximum ID (can also be fetched from the database)
        int range = (max - min) / gridSize;
        int start = min;
        int end = start + range;
        for (int i = 0; i < gridSize; i++) {
            ExecutionContext context = new ExecutionContext();
            context.putInt("start", start);
            context.putInt("end", end);
            partitions.put("partition" + i, context);
            start = end + 1;
            end = (i == gridSize - 2) ? max : end + range;
        }
        return partitions;
    }
}
