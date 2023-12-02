package es.ulpgc.tiledMatrixMultiplication;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class MatrixMultiplicationExecutorBenchmark {

    MatrixMultiplier multiplierExecutor;
    MatrixBlock[] matrixABlocks;
    MatrixBlock[] matrixBBlocks;

    @Param({"75", "100", "200", "300", "400", "455"})
    int size;

    @Setup
    public void setup() {
        multiplierExecutor = new MatrixMultiplierWithExecutors();
        int[][] matrixA = generateRandomMatrix(size, size);
        int[][] matrixB = generateRandomMatrix(size, size);
        matrixABlocks = new MatrixBlock(matrixA).divideMatrixIntoBlocks(matrixA);
        matrixBBlocks = new MatrixBlock(matrixB).divideMatrixIntoBlocks(matrixB);
    }


    @Benchmark
    public List<int[][]> testMatrixMultiplicationWithExecutor() throws InterruptedException, ExecutionException {
        List<int[][]> result = multiplierExecutor.multiplyMatrix(matrixABlocks, matrixBBlocks);
        return  result;
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(MatrixMultiplicationExecutorBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(options).run();
    }

    private int[][] generateRandomMatrix(int rows, int cols) {
        return IntStream.range(0, rows)
                .mapToObj(i -> IntStream.range(0, cols)
                        .map(j -> (int) (Math.random() * 10))
                        .toArray())
                .toArray(int[][]::new);
    }
}
