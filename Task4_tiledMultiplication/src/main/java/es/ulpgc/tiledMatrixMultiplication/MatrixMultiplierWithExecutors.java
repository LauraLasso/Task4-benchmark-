package es.ulpgc.tiledMatrixMultiplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class MatrixMultiplierWithExecutors implements MatrixMultiplier {

    private static ExecutorService executorService;

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

    @Override
    public List<int[][]> multiplyMatrix(MatrixBlock[] matrixA, MatrixBlock[] matrixB)
            throws InterruptedException, ExecutionException {

        executorService = Executors.newFixedThreadPool(3);

        List<int[][]> resultMatrix = new ArrayList<>();
        List<Future<int[][]>> futures = new ArrayList<>();
        int isPair = 0;

        for (List<Integer> blocks : blocks_to_multiply) {
            if (isPair % 2 == 0 & isPair!=0) {
                int[][] result = sumPairsOfBlocks(futures);

                resultMatrix.add(result);
                futures.clear();
            }

            Future<int[][]> future = executorService.submit(() ->
                    multiplyMatricesWithExecutors(matrixA[blocks.get(0)], matrixB[blocks.get(1)]));

            futures.add(future);
            isPair++;
        }

        for (Future<int[][]> future : futures) {
            int[][] result = future.get();
            resultMatrix.add(result);
        }

        try{
            executorService.shutdown();
            executorService.awaitTermination(1000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return resultMatrix;
    }

    private int[][] sumPairsOfBlocks(List<Future<int[][]>> futures) throws InterruptedException, ExecutionException {
        List<int[][]> pairsOfBlocks = new ArrayList<>();
        for (Future<int[][]> future : futures) {
            int[][] result = future.get();
            pairsOfBlocks.add(result);
        }

        int[][] resultado = new int[pairsOfBlocks.get(0).length][pairsOfBlocks.get(0)[0].length];

        for (int[][] matriz : pairsOfBlocks) {
            sumMatrix(resultado, matriz);
        }

        return resultado;
    }

    private void sumMatrix(int[][] resultado, int[][] matriz) {
        int filas = resultado.length;
        int columnas = resultado[0].length;

        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                resultado[i][j] += matriz[i][j];
            }
        }
    }

    private int[][] multiplyMatricesWithExecutors(MatrixBlock blockA, MatrixBlock blockB) {
        int[][] matrixA = blockA.getBlock();
        int[][] matrixB = blockB.getBlock();

        int rowsA = matrixA.length;
        int colsA = matrixA[0].length;
        int colsB = matrixB[0].length;

        int[][] result = new int[rowsA][colsB];

        try {
            List<Future<Void>> futures = new ArrayList<>();

            for (int i = 0; i < rowsA; i++) {
                for (int j = 0; j < colsB; j++) {
                    int finalI = i;
                    int finalJ = j;
                    futures.add(executorService.submit(() -> {
                        result[finalI][finalJ] = IntStream.range(0, colsA)
                                .map(k -> matrixA[finalI][k] * matrixB[k][finalJ])
                                .sum();
                        return null;
                    }));
                }
            }

            for (Future<Void> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return result;
    }
}
