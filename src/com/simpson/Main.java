package com.simpson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.Function;

class IntegrationResult {
    double integral;
    int steps;
    double calculationError;

    public IntegrationResult(double integral, int steps, double calculationError) {
        this.integral = integral;
        this.steps = steps;
        this.calculationError = calculationError;
    }
}

public class Main {

    private static DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    private static IntegrationResult getIntegralByRungeRule(Function<Double, Double> function, double leftLimit, double rightLimit, double epsilon) {
        double integral = 0;
        double previousIntegral;
        int direction = 1;

        if (leftLimit == rightLimit) {
            return new IntegrationResult(integral, 1, 0);
        } else if (leftLimit > rightLimit) {
            double temp = leftLimit;
            leftLimit = rightLimit;
            rightLimit = temp;
            direction = -1;
        }

        int steps = 10;
        integral = direction * getSimpsonsIntegral(function, leftLimit, rightLimit, steps);

        double theta = 1.0 / 15.0;
        do {
            steps *= 2;
            previousIntegral = integral;
            integral = direction * getSimpsonsIntegral(function, leftLimit, rightLimit, steps);
        } while (theta * Math.abs(integral - previousIntegral) > epsilon);

        return new IntegrationResult(integral, steps, theta * Math.abs(integral - previousIntegral));
    }

    private static double getSimpsonsIntegral(Function<Double, Double> function, double leftLimit, double rightLimit, int steps) {
        double h = (rightLimit - leftLimit) / steps;
        double integral = 0;
        for (int i = 1; i < steps; i += 2) {
            integral += function.apply(leftLimit + h * (i - 1));
            integral += 4 * function.apply(leftLimit + h * i);
            integral += function.apply(leftLimit + h * (i + 1));
        }
        return integral * h / 3;
    }

    private static double getDoubleFromConsole(BufferedReader reader) {
        double input = 0;
        boolean got = false;
        while (!got) {
            try {
                input = Double.parseDouble(reader.readLine().trim().replaceAll("[,]+", "."));
                got = true;
            } catch (Exception e) {
                System.out.println("Enter correct number:");
            }
        }
        return input;
    }

    public static void main(String[] args) {
        df.setMaximumFractionDigits(10);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Choose function to integrate:\n" +
                "(1) ln(x)\n" +
                "(2) sin(x)/x\n" +
                "(3) x^4 - 5x + 23\n" +
                "(4) -8*cos(x) - 3x\n" +
                "(5) 12/x^3 + sin(x+6)");

        int functionNumber = 0;
        while (functionNumber <= 0 || functionNumber > 5) {
            try {
                functionNumber = Integer.parseInt(reader.readLine().trim());
                if (functionNumber <= 0 || functionNumber > 5) throw new NumberFormatException();
            } catch (Exception e) {
                System.out.println("Enter number of chosen function (1, 2, 3, 4 or 5):");
            }
        }

        Function<Double, Double> function;
        switch (functionNumber) {
            case 1:
                function = Math::log;
                break;
            case 2:
                function = x -> {
                    if (x == 0) return 1.0;
                    else return Math.sin(x) / x;
                };
                break;
            case 3:
                function = x -> (Math.pow(x, 4) + 5 * x + 23);
                break;
            case 4:
                function = x -> (-8 * Math.cos(x) - 3 * x);
                break;
            case 5:
                function = x -> (12 / (Math.pow(x, 3)) + Math.sin(x + 6));
                break;
            default:
                function = x -> x;
        }

        System.out.println("Enter left limit:");
        double leftLimit = getDoubleFromConsole(reader);
        System.out.println("Enter right limit:");
        double rightLimit = getDoubleFromConsole(reader);
        System.out.println("Enter epsilon:");
        double epsilon = getDoubleFromConsole(reader);
        System.out.println();

        IntegrationResult result = null;
        if (functionNumber == 5 && leftLimit <= 0 && rightLimit >= 0)
            result = new IntegrationResult(Double.POSITIVE_INFINITY, 1, 0.0);
        else if (functionNumber == 1 && (leftLimit <= 0 || rightLimit <= 0))
            System.out.println("Limits is out of function domain");
        else result = getIntegralByRungeRule(function, leftLimit, rightLimit, epsilon);

        if (result != null) {
            System.out.println(String.format("Integrating function (%d) from %s to %s with epsilon = %s:",
                    functionNumber, df.format(leftLimit), df.format(rightLimit), df.format(epsilon)));
            System.out.println("I = " + df.format(result.integral));
            System.out.println("Steps: " + result.steps);
            System.out.println("Calculation error = " + df.format(result.calculationError));
        }
    }
}
