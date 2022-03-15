import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.*;

public class SA_TSP {
    String FILE_PATH = "Dataset/eil51";
    static double TEMPERATURE = 10000;                                                                     //初始溫度
    static double SUCCESS_RATE = 0.998;                                                                         //若接受更新最優解則較快的降溫係數
    static double FAILURE_RATE = 0.999;                                                                         //若未接受更新最優解則使用較慢的降溫係數
    static int MAX_GENERATION = 100;                                                                       //新解產生迭代上限
    int totalCity;                                                                              //city數量

    ArrayList<CityNode> cityList;                                                                //city資料集

    int[] generatePath() {                                                                       //產生初始解
        int[] path = new int[totalCity];
        boolean[] chose = new boolean[totalCity];                                                      //檢查city是否走過
        Arrays.fill(chose, false);

        for (int index = 0; index < totalCity; index++) {
            int cityId = (int) (Math.random() * totalCity);
            while (chose[cityId])
                cityId = (int) (Math.random() * totalCity);

            path[index] = cityId + 1;
            chose[cityId] = true;
        }
        return path;
    }

    double twoPointDistance(int city1Id, int city2Id) {                                        //計算2點路徑
        CityNode city1 = cityList.get(city1Id - 1);
        CityNode city2 = cityList.get(city2Id - 1);
        int city1_x = city1.x;
        int city1_y = city1.y;
        int city2_x = city2.x;
        int city2_y = city2.y;

        return Math.sqrt(Math.pow((city1_x - city2_x), 2) + Math.pow((city1_y - city2_y), 2));
    }

    double calEnergy(int[] path) {                                                               //計算Energy
        double energy = 0;
        for (int index = 0; index < totalCity - 1; index++)
            energy += twoPointDistance(path[index], path[index + 1]);
        return energy;
    }

    void swap(int[] ary, int x, int y) {                                                         //2點交換
        int temp = ary[x];
        ary[x] = ary[y];
        ary[y] = temp;
    }

    int[] generate(int[] sol) {                                                                  //隨機2點城市互換位置
        int[] path = new int[totalCity];
        int point1 = (int) (Math.random() * totalCity);
        int point2 = point1;
        while (point1 == point2)
            point2 = (int) (Math.random() * totalCity);
        System.arraycopy(sol, 0, path, 0, totalCity);
        swap(path, point1, point2);

        return path;
    }

    boolean metropolis(double f1, double f2, double temperature) {                               //Metropolis判斷法則
        if (f2 < f1)                                                                                    //若新解比當前解好 return ture
            return true;
        double probability = Math.exp(-(f2 - f1) / temperature);                                        //若新解比當前解差 根據exp(-ΔT/T)機率來決定是否接受新解
        double bigNum = Math.pow(10, 9);
/*
        if (Math.random() % bigNum < probability * bigNum)
            return true;
        return false;
*/
        return (Math.random() % bigNum < probability * bigNum);
    }

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
    }

    void run() throws IOException {
        cityList = CityNode.inputCity(FILE_PATH);                                                                //讀取資料集eil51.txt
        totalCity = cityList.size();                                                                   //city數量

        int[] bestSol = generatePath();                                                                //產生初始解=最優解
        double bestDistance = calEnergy(bestSol);                                                     //初始解長度
        int[] oldSol = new int[totalCity];                                                            //宣告當前解
        System.arraycopy(bestSol, 0, oldSol, 0, totalCity);                             //當前解解=初始解

        double newEnergy = 1;                                                                          //初始新解能量
        double oldEnergy = 0;                                                                          //初始當前解解能量
        int times = 1;                                                                                  //降溫次數

        while (TEMPERATURE > Math.pow(10, -9) && Math.abs(newEnergy - oldEnergy) > Math.pow(10, -9)) {//降溫迴圈 判斷更新最優解
            double t_g = MAX_GENERATION;                                                                //設定新解迭代次數上限
            int[] newSol;                                                                               //宣告新解
            while (t_g-- != 0 && Math.abs(newEnergy - oldEnergy) > Math.pow(10, -9)) {                //新解迭代產生
                newSol = generate(oldSol);                                                            //產生新解
                newEnergy = calEnergy(newSol);                                                        //新解能量
                oldEnergy = calEnergy(oldSol);                                                        //當前解解能量
                if (metropolis(oldEnergy, newEnergy, TEMPERATURE))                                    //根據Metropolis法則判斷是否接受新解
                    System.arraycopy(newSol, 0, oldSol, 0, totalCity);              //若接受則更新新解為當前解
            }
            newEnergy = calEnergy(oldSol);                                                            //當前解能量
            oldEnergy = calEnergy(bestSol);                                                           //最優解能量
            if (newEnergy < oldEnergy) {                                                              //判斷當前解是否比最優解好
                System.arraycopy(oldSol, 0, bestSol, 0, totalCity);
                bestDistance = calEnergy(bestSol);
                TEMPERATURE *= SUCCESS_RATE;
                System.out.println(times + "次" + " distance:" + bestDistance + " temperature:" + TEMPERATURE + "\npath:" + Arrays.toString(bestSol));
            } else
                TEMPERATURE *= FAILURE_RATE;
            times++;
        }
        System.out.println("\nbest solution \ndistance:" + bestDistance + "\npath:" + Arrays.toString(bestSol));
        drawPath(bestSol);
    }

    public static void main(String[] args) throws IOException {
        SA_TSP similarAnnealingALgo = new SA_TSP();
        similarAnnealingALgo.run();
    }
}