package com.company;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Main {

    //весь набор исходных элементов, элементы, на которых будет тестировать, элементы для обучения, элементы для проверки (они же те, которые выбраны для тестирования)
    public static List<Element> Elements, TestElements, LearningElements, CheckElements;

    //количесвто элементов, количество фолдеров для кросс-валидации, количество соседей, количество элементов тестовой выборки
    public static int NumberOfLines, NumberOfFolders, Row, k, TestElementsNumber;

    //компараторы для всех случаев жизни
    public static Comparator<Element> EuclidDistanceComparator;
    public static Comparator<Element> ManhattanDistanceComparator;
    public static Comparator<Element> ManhattanQuartedDistanceComparator;
    public static Comparator<Element> ManhattanGaussianDistanceComparator;
    public static Comparator<Element> EuclidQuartedDistanceComparator;
    public static Comparator<Element> EuclidGaussianDistanceComparator;
    //  public static List<Lis>

    public static class Element {
        public float x, y; //координаты

        //настоящий класс, класс, информация о котором вычислена при помощи расстояния Манхэттена и квартической функции ядра;
        //при помощи расстояния Евклида и квартической функции ядра; при помощи расстояния Манхэттена и Гауссианской функции ядра;
        // при помощи расстояния Евклида и Гауссианской функции ядра
        public int trueClass, ManhQuartedSupposedClass,
                EuqlidQuartedSupposedClass, ManhGaussedSupposedClass,
                EuclidGaussedSupposedClass;

        //расстояние Манхэттена, расстояние Евклида, значение квартичной функции при расстоянии Манхэттена,
        //значение квартичной функции при расстоянии Евклида, значение Гауссовской функции при расстоянии Манхэттена,
        //значение Гауссовской функции при расстоянии Евклида
        public double ManhattenDistance, EuclidDistance,
                ManhQuartedDistance, EuclidQuartedDistance,
                ManhGaussianDistance, EuclidGaussianDistance;

        public Element(float InX, float InY, int C) {
            x = InX;
            y = InY;
            trueClass = C;
            //дабы элементы, данные значения для которых не считались, попали в конец
            ManhattenDistance = Double.MAX_VALUE;
            EuclidDistance = Double.MAX_VALUE;
            ManhQuartedDistance = Double.MAX_VALUE;
            ManhGaussianDistance = Double.MAX_VALUE;
            EuclidQuartedDistance = Double.MAX_VALUE;
            EuclidGaussianDistance = Double.MAX_VALUE;
        }
    }

    //расстояние Минковского. Классическая формулка из лекций
    public static double Minkovsky_Distance(Element x, Element y, int p) {
        return Math.pow((Math.pow(Math.abs(x.x -y.x), p) + Math.pow(Math.abs(x.y - y.y), p)), (1 / p));
    }

    //квартическая функция ядра. Подробнее: goo.gl/5kF4PG
    public static double QuartedKernalFunction(double u) {
        if (u <= 1)
            return 15 / 16 * Math.pow((1 - Math.pow(u, 2)), 2);
        else return 0;
    }

    // функция ядра Гауса (где-то называласб Гауссианской функцией)
    public static double GaussianKernalFunction(double u) {
        return Math.pow((Math.PI * 2), -1 / 2) * Math.exp(-Math.pow(u, 2) / 2);
    }

    //кросс-валидация
    public static void Cross_Validation() {
        //создаем новую ссылка на обучающую выборку, дабы значения из предыдущих шагов не попали к нам
        LearningElements = new ArrayList<>();
        TestElements = new ArrayList<>(); //аналогично

        //если не последняя проверка.
        // Т.к., поскольку количество элементов не делится нацело на количество фолдеров для кросс-валидации,
        // в последний фолдер попадает меньше значений. Это хорошо бы отслеживать
        if (Row != NumberOfFolders) {
            //вытаскиваем те элементы, которые находятся в выборке до тех элементов, которые мы оставляем для тестирования
            List<Element> FirstHelpLearning = Elements.subList(0, NumberOfLines - Row * TestElementsNumber);
            List<Element> TestHelp = Elements.subList(NumberOfLines - Row * TestElementsNumber, NumberOfLines - (Row - 1) * TestElementsNumber); // элементы для тестирования
            TestElements.addAll(TestHelp); //добавляем. Если делать так, то джава не ругается

            //если мы будем тестировать алгоритм не на последних значениях, то есть есть еще значения, которые нужно добавить в обучающую выборку
            if (Row != 1) {
                List<Element> helplist = Elements.subList(NumberOfLines - (Row - 1) * TestElementsNumber, NumberOfLines);
                LearningElements.addAll(helplist);
            }
            LearningElements.addAll(FirstHelpLearning);
        } else { //последний фолдер для кросс-валидации
            //те элементы, на которых мы уже тестировали
            int AlreadySeenElements = (Row - 1) * TestElementsNumber;

            //те элементы, на которых будем
            List<Element> TestHelp = Elements.subList(0, NumberOfLines - AlreadySeenElements);
            TestElements.addAll(TestHelp);
            List<Element> FirstHelpLearning = Elements.subList(NumberOfLines - AlreadySeenElements, NumberOfLines);
            LearningElements.addAll(FirstHelpLearning);
        }
        ListIterator<Element> LearningIt = LearningElements.listIterator(0);
        //для всей обучающей выборки присвоим значения для классов для всех рассматирваемых функций ядер и расстояний,
        // равные настоящему значению, полученному нами изначально
        while (LearningIt.hasNext()) {
            Element e = LearningIt.next();
            e.EuclidGaussedSupposedClass = e.trueClass;
            e.EuqlidQuartedSupposedClass = e.trueClass;
            e.ManhGaussedSupposedClass = e.trueClass;
            e.ManhQuartedSupposedClass = e.trueClass;
        }

        CheckElements = new ArrayList<Element>(); //очищаем массив, который будем использовать для проверки
        CheckElements.addAll(TestElements);//добавляем тестовые элементы
    }

    public static void kNN() {
        ListIterator<Element> TestIt = TestElements.listIterator(0);

        //для каждого элемента из тестовой выборки считаем расстояния Евклида и Манхэттена до всех элементов из обучающей выборки
        while (TestIt.hasNext()) {
            Element t = TestIt.next();
            ListIterator<Element> LearningIt = LearningElements.listIterator(0);
            while (LearningIt.hasNext()) {
                Element e = LearningIt.next();
                e.ManhattenDistance = Minkovsky_Distance(e, t, 1);
                e.EuclidDistance = Minkovsky_Distance(e, t, 2);
            }

            //сортируем обучающую выборку по длине расстояния Манхэттена (наименьшая длина в начало)
            LearningElements.sort(ManhattanDistanceComparator);
            LearningIt = LearningElements.listIterator(0);
            int j = 0; //необходим для расчета, не дошли ли мы до последнего элемента

            //мысль в том, что брать все элементы бессмысленно. Иначе у нас получится просто среднеквадратичное значение.
            // Поэтому посчитаем все, кроме последнего. Тогда не нужно будет ничего придумывать для решения проблемы поиска окна Парзена-Розенблата
            while (LearningIt.hasNext() && j != LearningElements.size() - 1) {
                Element e = LearningIt.next();
                j++;
                //считаем значения функций ядра для (POi/PO(i+1)), где PO - расстояние, i и i+1 - индексы
                e.ManhGaussianDistance = GaussianKernalFunction(e.ManhattenDistance / LearningElements.get(j).ManhattenDistance);
                e.ManhQuartedDistance = QuartedKernalFunction(e.ManhattenDistance / LearningElements.get(j).ManhattenDistance);
            }
            LearningElements.sort(EuclidDistanceComparator);// аналогично для расстояния Евклида
            LearningIt = LearningElements.listIterator(0);
            j = 0;

            while (LearningIt.hasNext() && j != LearningElements.size() - 1) {
                Element e = LearningIt.next();
                j++;
                e.EuclidQuartedDistance = QuartedKernalFunction(e.EuclidDistance / LearningElements.get(j).EuclidDistance);
                e.EuclidGaussianDistance = GaussianKernalFunction(e.EuclidDistance / LearningElements.get(j).EuclidDistance);
            }

            //для функции ядра Гаусса и расстояния Манхэттена считаем принадлежность к классу
            LearningElements.sort(ManhattanGaussianDistanceComparator);
            int QuantityOfAClass = 0;// класс 0
            int QuantityOfBClass = 0;// класс 1
            LearningIt = LearningElements.listIterator(0);
            int numberOfNeibours = 0;//сколько соседий уже просмотрели

            while (LearningIt.hasNext() && numberOfNeibours != k) {
                numberOfNeibours++;
                Element e = LearningIt.next();

                //еси класс соседа - 0, то увеличиваем счетчик количества соседий с классом 0
                if (e.ManhGaussedSupposedClass == 0) QuantityOfAClass++;
                else QuantityOfBClass++; // иначе увеличиваем счетчик соседей с классом 1
            }

            if (QuantityOfAClass > QuantityOfBClass) t.ManhGaussedSupposedClass = 0;
            else t.ManhGaussedSupposedClass = 1; // иначе нам подойдет класс 1

            //аналогично для квартической функции расстояния Манхэттена
            LearningElements.sort(ManhattanQuartedDistanceComparator);
            QuantityOfAClass = 0;
            QuantityOfBClass = 0;
            LearningIt = LearningElements.listIterator(0);
            numberOfNeibours = 0;

            while (LearningIt.hasNext() && numberOfNeibours != k) {
                numberOfNeibours++;
                Element e = LearningIt.next();
                if (e.ManhQuartedSupposedClass == 0) QuantityOfAClass++;
                else QuantityOfBClass++;
            }

            if (QuantityOfAClass > QuantityOfBClass) t.ManhQuartedSupposedClass = 0;
            else t.ManhQuartedSupposedClass = 1;
            //  System.out.println("MQ " + t.trueClass + " " + t.ManhQuartedSupposedClass);


            // аналогично для Гауссовской функции расстояния Евклида
            LearningElements.sort(EuclidGaussianDistanceComparator);
            QuantityOfAClass = 0;
            QuantityOfBClass = 0;
            LearningIt = LearningElements.listIterator(0);
            numberOfNeibours = 0;

            while (LearningIt.hasNext() && numberOfNeibours != k) {
                numberOfNeibours++;
                Element e = LearningIt.next();
                if (e.EuclidGaussedSupposedClass == 0) QuantityOfAClass++;
                else QuantityOfBClass++;
            }

            if (QuantityOfAClass > QuantityOfBClass) t.EuclidGaussedSupposedClass = 0;
            else t.EuclidGaussedSupposedClass = 1;
            //   System.out.println("EG " + t.trueClass + " " + t.EuclidGaussedSupposedClass);

            // и для квартической функции евклидового расстояния
            LearningElements.sort(EuclidQuartedDistanceComparator);
            QuantityOfAClass = 0;
            QuantityOfBClass = 0;
            LearningIt = LearningElements.listIterator(0);
            numberOfNeibours = 0;

            while (LearningIt.hasNext() && numberOfNeibours != k) {
                numberOfNeibours++;
                Element e = LearningIt.next();
                if (e.EuqlidQuartedSupposedClass == 0) QuantityOfAClass++;
                else QuantityOfBClass++;
            }

            if (QuantityOfAClass > QuantityOfBClass) t.EuqlidQuartedSupposedClass = 0;
            else t.EuqlidQuartedSupposedClass = 1;
            //добавляем наш тестовый элемент в обучающую выборку, так как считаем, что для него класс уже оределен
            LearningElements.add(t);
        }
    }


    //accuracy, F-measure
    public static double[] FMeasure(String type) {
        //идем по массиву проверочных элементов

        ListIterator<Element> CheckingIt = CheckElements.listIterator();

        double TP = 0;//действительно положительные
        double FP = 0;//ложно положительные
        double TN = 0;//действительно отриательные
        double FN = 0;//ложно отрицательные

        switch (type) {
            //Если расстояние манхэттена, а функция Гаусса
            case "Manhattan Gaussian": {

                while (CheckingIt.hasNext()) {
                    Element e = CheckingIt.next();

                    if (e.trueClass == 0) {
                        if (e.ManhGaussedSupposedClass == 0) TN++;
                        else FP++;

                    } else {
                        if (e.ManhGaussedSupposedClass == 0) FN++;
                        else TP++;
                    }

                }
            }

            //если расстояния Манхэттена и квартическая функция
            case "Manhattan Quarted": {

                while (CheckingIt.hasNext()) {
                    Element e = CheckingIt.next();

                    if (e.trueClass == 0) {
                        if (e.ManhQuartedSupposedClass == 0) TN++;
                        else FP++;

                    } else {
                        if (e.ManhQuartedSupposedClass == 0) FN++;
                        else TP++;
                    }
                }
            }

            //для расстояния Евклида и функции Гаусса
            case "Euclid Gaussian": {

                while (CheckingIt.hasNext()) {
                    Element e = CheckingIt.next();

                    if (e.trueClass == 0) {
                        if (e.EuclidGaussedSupposedClass == 0) TN++;
                        else FP++;

                    } else {
                        if (e.EuclidGaussedSupposedClass == 0) FN++;
                        else TP++;
                    }
                }
            }

            //для расстояния Евклида и Квартической функции
            case "Euclid Quarted": {

                while (CheckingIt.hasNext()) {
                    Element e = CheckingIt.next();

                    if (e.trueClass == 0) {
                        if (e.EuqlidQuartedSupposedClass == 0) TN++;
                        else FP++;

                    } else {
                        if (e.EuqlidQuartedSupposedClass == 0) FN++;
                        else TP++;
                    }
                }
            }
        }
        double[] result = new double[2];
        double P = TP + FN;
        double N = FP + TN;

        // System.out.println("TP " + TP + " FN " + FN + " FP " + FP + " TN " + TN + " P " + P + " N " + N);

        double Recall = TP / P;
        double Precision = TP / (TP + FP);

        // System.out.println("Recall " + Recall + " Pecision " + Precision);

        double Accuracy = (TP + TN) / (P + N);
        double F = 2 * Precision * Recall / (Precision + Recall);
        result[0] = Accuracy;
        result[1] = F;

        return result;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader infile = new BufferedReader(new FileReader("infile.in"));

        NumberOfLines = 0;
        NumberOfFolders = 10;
        Elements = new ArrayList<Element>();
        k = 19;
        createComparators();


        while (true) {
            String InString = infile.readLine();
            if (InString == null) break;
            NumberOfLines++;
            String[] InStringArray = InString.split(",");
            Elements.add(new Element(Float.valueOf(InStringArray[0]), Float.valueOf(InStringArray[1]), Integer.valueOf(InStringArray[2])));
        }


        TestElementsNumber = (int) Math.ceil((double) NumberOfLines / NumberOfFolders);
        for (int i = 0; i < NumberOfFolders; i++) {
            Row++;
            System.out.println(Row);
            Cross_Validation();
            kNN();

            double[] FmeasMG = FMeasure("Manhattan Gaussian");
            System.out.println("Manhattan Gaussian: Accuracy= " + FmeasMG[0] + ", F-measure= " + FmeasMG[1]);

            double[] FmeasMQ = FMeasure("Manhattan Quarted");
            System.out.println("Manhattan Quarted: Accuracy= " + FmeasMQ[0] + ", F-measure= " + FmeasMQ[1]);

            double[] FmeasEG = FMeasure("Euclid Gaussian");
            System.out.println("Euclid Gaussian: Accuracy= " + FmeasEG[0] + ", F-measure= " + FmeasEG[1]);

            double[] FmeasEQ = FMeasure("Euclid Quarted");
            System.out.println("Euclid Quarted: Accuracy= " + FmeasEQ[0] + ", F-measure= " + FmeasEQ[1]);
        }


    }

    public static void createComparators() {

        ManhattanDistanceComparator = new Comparator<Element>() {
            @Override
            public int compare(Element o1, Element o2) {
                double difference = o1.ManhattenDistance - o2.ManhattenDistance;
                if (difference > 0) return 1;
                else if (difference == 0) return 0;
                else return -1;
            }
        };

        EuclidDistanceComparator = new Comparator<Element>() {
            @Override
            public int compare(Element o1, Element o2) {
                double difference = o1.EuclidDistance - o2.EuclidDistance;
                return difference == 0 ? 0 : (int) (difference / Math.abs(difference));
            }
        };

        ManhattanGaussianDistanceComparator = new Comparator<Element>() {
            @Override
            public int compare(Element o1, Element o2) {
                double difference = o1.ManhGaussianDistance - o2.ManhGaussianDistance;
                return difference == 0 ? 0 : (int) (difference / Math.abs(difference));
            }
        };

        ManhattanQuartedDistanceComparator = new Comparator<Element>() {
            @Override
            public int compare(Element o1, Element o2) {
                double difference = o1.ManhQuartedDistance - o2.ManhQuartedDistance;
                return difference == 0 ? 0 : (int) (difference / Math.abs(difference));
            }
        };

        EuclidGaussianDistanceComparator = new Comparator<Element>() {
            @Override
            public int compare(Element o1, Element o2) {
                double difference = o1.EuclidGaussianDistance - o2.EuclidGaussianDistance;
                return difference == 0 ? 0 : (int) (difference / Math.abs(difference));
            }
        };

        EuclidQuartedDistanceComparator = new Comparator<Element>() {
            @Override
            public int compare(Element o1, Element o2) {
                double difference = o1.EuclidQuartedDistance - o2.EuclidQuartedDistance;
                return difference == 0 ? 0 : (int) (difference / Math.abs(difference));
            }
        };

    }
}

