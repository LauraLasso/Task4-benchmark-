package es.ulpgc.tiledMatrixMultiplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class MatrixMultiplierWithSemaphore implements MatrixMultiplier {

    static List<List<Integer>> blocks_to_multiply = new ArrayList<>(Arrays.asList(
            Arrays.asList(0, 0),
            Arrays.asList(1, 2),
            Arrays.asList(0, 1),
            Arrays.asList(1, 3),
            Arrays.asList(2, 0),
            Arrays.asList(3, 2),
            Arrays.asList(2, 1),
            Arrays.asList(3, 3)
    ));

    private final Semaphore semaphore = new Semaphore(1);

    @Override
    public List<int[][]> multiplyMatrix(MatrixBlock[] matrixA, MatrixBlock[] matrixB) throws ExecutionException, InterruptedException {
        List<int[][]> resultMatrix = new ArrayList<>();
        List<int[][]> pairsOfBlocks = new ArrayList<>();
        AtomicInteger isPair = new AtomicInteger(1);

        IntStream.range(0, blocks_to_multiply.size())
                .mapToObj(i -> blocks_to_multiply.get(i))
                .forEach(pair -> {
                    pairsOfBlocks.add(multiplyMatricesWithSemaphore(matrixA[pair.get(0)], matrixB[pair.get(1)]));

                    if (isPair.getAndIncrement() % 2 == 0) {
                        int[][] result = sumMatrix(pairsOfBlocks, semaphore);
                        resultMatrix.add(result);
                        pairsOfBlocks.clear();
                    }
                });

        return resultMatrix;
    }

    public int[][] sumMatrix(List<int[][]> listOfMatrix, Semaphore semaphore) {
        int filas = listOfMatrix.get(0).length;
        int columnas = listOfMatrix.get(0)[0].length;

        int[][] resultado = new int[filas][columnas];

        AtomicInteger rowCounter = new AtomicInteger(0);

        try {
            semaphore.acquire();

            listOfMatrix.stream()
                    .parallel()
                    .forEach(matriz -> {
                        IntStream.range(0, filas)
                                .forEach(i -> {
                                    int currentRow = rowCounter.getAndIncrement();
                                    if (currentRow < filas) {
                                        IntStream.range(0, columnas)
                                                .forEach(j -> resultado[currentRow][j] += matriz[currentRow][j]);
                                    }
                                });
                    });

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            semaphore.release();
        }

        return resultado;
    }

    public int[][] multiplyMatricesWithSemaphore(MatrixBlock blockA, MatrixBlock blockB) {
        int[][] matrixA = blockA.getBlock();
        int[][] matrixB = blockB.getBlock();

        int rowsA = matrixA.length;
        int colsA = matrixA[0].length;
        int colsB = matrixB[0].length;

        int[][] result = new int[rowsA][colsB];

        IntStream.range(0, rowsA).parallel().forEach(i -> {
            IntStream.range(0, colsB).forEach(j -> {
                AtomicInteger sum = new AtomicInteger(0);

                IntStream.range(0, colsA).forEach(k -> {
                    sum.addAndGet(matrixA[i][k] * matrixB[k][j]);
                });

                result[i][j] = sum.get();
            });
        });

        return result;
    }
}

