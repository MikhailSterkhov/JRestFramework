package com.itzstonlex.restframework.test;

import java.util.Arrays;
import java.util.Scanner;

public class Test {

    private static final int MIN_WIDTH = 5;
    private static final int MIN_HEIGHT = 2;
    private static final int MAX_HEIGHT = 3;

    private static final char WHITE_SIGN = 'W';
    private static final char BLACK_SIGN = 'B';
    private static final char EMPTY_SIGN = '.';

    private static int maxBlack;
    private static int maxWhite;

    private static void setRow(boolean inverse, int row, char[][] matrix, int currentWidth) {
        for (int index = 0; index < currentWidth; index++) {
            char sign = (index % 2 == 0) ? inverse ? WHITE_SIGN : BLACK_SIGN : inverse ? BLACK_SIGN : WHITE_SIGN;

            if (sign == WHITE_SIGN) {
                if (maxWhite <= 0) {
                    continue;
                }

                maxWhite--;

            } else {

                if (maxBlack <= 0) {
                    continue;
                }

                maxBlack--;
            }

            matrix[row][index] = sign;
        }
    }

    private static boolean validateInputs(int currentHeight) {
        int min = Math.min(maxBlack, maxWhite);
        int max = Math.max(maxBlack, maxWhite);

        return min * currentHeight >= max;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        maxBlack = scanner.nextInt();
        maxWhite = scanner.nextInt();

        int minModifier = MIN_WIDTH * MIN_HEIGHT;
        int currentModifier = maxBlack + maxWhite;

        int currentHeight = currentModifier > minModifier ? MAX_HEIGHT : MIN_HEIGHT;
        int currentWidth = currentModifier > minModifier ? (currentModifier + (currentModifier % currentHeight)) / currentHeight : MIN_WIDTH;

        if (!validateInputs(currentHeight)) {
            System.out.println(-1);

            return;
        }

        char[][] matrix = new char[currentHeight][currentWidth];
        for (char[] chars : matrix) {
            Arrays.fill(chars, EMPTY_SIGN);
        }

        setRow(false, 1, matrix, currentWidth);

        if (maxBlack > 0 || maxWhite > 0) {
            setRow(true, 0, matrix, currentWidth);
        }

        if (currentHeight > 2 && (maxBlack > 0 || maxWhite > 0)) {
            setRow(true, 2, matrix, currentWidth);
        }

        System.out.println(currentWidth + " " + currentHeight);

        for (char[] row : matrix) {
            System.out.println(row);
        }
    }

}
