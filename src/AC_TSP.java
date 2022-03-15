import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.*;

public class AC_TSP {
    String FILE_PATH = "Dataset/eil51";
    ArrayList<CityNode> cityList;                                  //city資料集
    int totalCity;                                                //city數量
    double[][] messTable;                                         //訊息素表
    boolean[][] vistedTable;                                      //螞蟻禁忌表
    int[][] antColony;                                            //螞蟻路徑集
    int ANT_NUM = 51;                                               //螞蟻個數
    int Q = 1;                                                     //常數
    int MAX_GENERATION = 3000;                                     //世代數上限
    double T_DECREASE = 0.3;                                       //衰退率
    double NOT_CHOSE_RATE = 0.1;                                   //不接受機率


    double twoPointDistance(int city1_id, int city2_id) {            //計算2點路徑
        CityNode city1 = cityList.get(city1_id - 1);
        CityNode city2 = cityList.get(city2_id - 1);
        int city1_x = city1.x;
        int city1_y = city1.y;
        int city2_x = city2.x;
        int city2_y = city2.y;

        return Math.sqrt(Math.pow((city1_x - city2_x), 2) + Math.pow((city1_y - city2_y), 2));
    }


    double calDistance(int[] path) {                                                               //計算總路徑長
        double totalDistance = 0;
        for (int index = 0; index < totalCity - 1; index++)
            totalDistance += twoPointDistance(path[index], path[index + 1]);
        return totalDistance + twoPointDistance(path[0], path[totalCity - 1]);
    }


    int rouletteSelection(int antId, int pathIndex) {                              //收斂不好，還沒找到問題點
        Map<Integer, Double> unvisited = new TreeMap<>();
        double roulette = 0;
        int choseCity = -1;
        for (int i = 1; i <= totalCity; i++)
            if (!vistedTable[antId][i - 1]) {
                unvisited.put(i, messTable[antColony[antId][pathIndex] - 1][i - 1]);
                roulette += messTable[antColony[antId][pathIndex] - 1][i - 1];
            }

        double rnd = Math.random() * roulette;
        for (Map.Entry<Integer, Double> set : unvisited.entrySet()) {
            rnd -= set.getValue();
            if (rnd <= 0) {
                choseCity = set.getKey();
                break;
            }
        }
        vistedTable[antId][choseCity - 1] = true;
        return choseCity;
    }


    int BestSelection(int antId, int path_index) {
        int choseCity = -1;
        double largestMess = 0;
        ArrayList<Integer> unvisited = new ArrayList<>();
        for (int i = 1; i <= totalCity; i++)
            if (!vistedTable[antId][i - 1]) {
                unvisited.add(i);
                if (messTable[antColony[antId][path_index] - 1][i - 1] > largestMess) {
                    largestMess = messTable[antColony[antId][path_index] - 1][i - 1];
                    choseCity = i;
                }
            }

        if (Math.random() < NOT_CHOSE_RATE)                                                 //一定機率選擇非最佳解
            choseCity = unvisited.get((int) (Math.random() * unvisited.size()));

        vistedTable[antId][choseCity - 1] = true;
        return choseCity;
    }                           //最佳選擇


    void localUpdate(int[] path) {
        for (int index = 0; index < totalCity; index++) {
            if (index != totalCity - 1)
                messTable[path[index] - 1][path[index + 1] - 1] += Q / calDistance(path);
            else
                messTable[path[0] - 1][path[index] - 1] += Q / calDistance(path);
        }                                           //局部更新訊息素
    }

    void AQS(int city1_id, int city2_id) {
        messTable[city1_id - 1][city2_id - 1] = messTable[city1_id - 1][city2_id - 1] * T_DECREASE + Q / twoPointDistance(city1_id, city2_id);
    }                                   //全域更新訊息素


    void drawPath(int[] path) {
        Frame demo = new Frame("Path demo");
        demo.add(new DrawTspPath(cityList, path));
        demo.setSize(650, 650);
        demo.setLocation(100, 100);
        demo.setVisible(true);
        demo.pack();
        demo.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }                                           //畫路徑圖


    void run() throws IOException {
        cityList = CityNode.inputCity(FILE_PATH);                                                                //讀取資料集eil51.txt
        totalCity = cityList.size();                                                                   //city數量

        messTable = new double[totalCity][totalCity];                            //mess表
        int generation = MAX_GENERATION;                                            //初始化代數
        int[] bestSol = null;                                                             //最佳解
        double bestSolDistance = 9999;                                                   //最佳解長度

        //init mess_table
        for (int city1 = 1; city1 <= totalCity; city1++)                            //mess表初始化
            for (int city2 = 1; city2 <= totalCity; city2++)
                if (city1 == city2)
                    messTable[city1 - 1][city2 - 1] = 0;
                else
                    messTable[city1 - 1][city2 - 1] = Q / twoPointDistance(city1, city2);               //初始值=兩點路徑倒數

        while (generation-- != 0) {
            antColony = new int[ANT_NUM][totalCity];                                   //螞蟻路徑
            vistedTable = new boolean[ANT_NUM][totalCity];                             //螞蟻禁忌表
            for (boolean[] arr : vistedTable)
                Arrays.fill(arr, false);

            for (int antId = 0; antId < ANT_NUM; antId++)                                        //路徑產生
                for (int pathLength = 0; pathLength < totalCity; pathLength++)
                    if (pathLength == 0) {                                                         //產生初始點
                        antColony[antId][0] = (int) (Math.random() * totalCity) + 1;
                        vistedTable[antId][antColony[antId][0] - 1] = true;
                    } else                                                                          //選擇路徑點
                        antColony[antId][pathLength] = BestSelection(antId, pathLength - 1);

            for (int antId = 0; antId < ANT_NUM; antId++) {
                localUpdate(antColony[antId]);                                                    //更新路徑訊息素
                double pathDistance = calDistance(antColony[antId]);
                if (pathDistance < bestSolDistance) {                                            //檢查最佳路徑
                    bestSol = antColony[antId];
                    bestSolDistance = pathDistance;
                    System.out.println(MAX_GENERATION - generation + "代" + " distance:" + bestSolDistance + "\npath:" + Arrays.toString(bestSol));
                }
            }

            for (int city1 = 1; city1 <= totalCity; city1++)                                               //全域更新
                for (int city2 = 1; city2 <= totalCity; city2++)
                    if (city1 != city2)
                        AQS(city1, city2);
        }
        System.out.println("\nbest solution \ndistance:" + bestSolDistance + "\npath:" + Arrays.toString(bestSol));
        drawPath(bestSol);
    }

    public static void main(String[] args) throws IOException {
        AC_TSP AntColonyAlgo = new AC_TSP();
        AntColonyAlgo.run();
    }
}