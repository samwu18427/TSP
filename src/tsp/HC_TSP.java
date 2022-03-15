package tsp;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class HC_TSP {
    String FilePath="Dataset/eil51";
    ArrayList<CityNode> cityList;                                //city資料集
    int totalCity;                                              //city數量

    int[] generatePath() {                          //產生初始解
        int[] path = new int[totalCity];
        boolean[] chose = new boolean[totalCity];                  //檢查city是否走過
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

    double twoPointDistance(int city1Id, int city2Id) {            //計算2點路徑
        CityNode city1 = cityList.get(city1Id - 1);
        CityNode city2 = cityList.get(city2Id - 1);
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

        return totalDistance;
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

    void swap(int[] ary, int x, int y) {                                                         //2點交換
        int temp = ary[x];
        ary[x] = ary[y];
        ary[y] = temp;
    }

    <T extends Comparable<T>> void multiSort(List<T> key, List<?>... lists) {    //複數矩陣排序 MutiSort(排序依據,排序目標,排序目標,....)
        List<Integer> index = new ArrayList<>();
        for (int i = 0; i < key.size(); i++)
            index.add(i);
        index.sort(Comparator.comparing(key::get));
        Map<Integer, Integer> swapMap = new HashMap<>(index.size());
        for (int i = 0; i < index.size(); i++) {
            int k = index.get(i);
            while (swapMap.containsKey(k))
                k = swapMap.get(k);
            swapMap.put(i, k);
        }
        for (Map.Entry<Integer, Integer> entrySet : swapMap.entrySet())
            for (List<?> list : lists)
                Collections.swap(list, entrySet.getKey(), entrySet.getValue());
    }

    void drawPath(int[] path) {
        Frame demo = new Frame("Path demo");
        demo.add(new drawTspPath(cityList, path));
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
        cityList = CityNode.inputCity(FilePath);                                                                //讀取資料集eil51.txt
        totalCity = cityList.size();                                                                   //city數量
        int neighborRange = 100;                                                                        //相鄰解範圍
        int[] bestSol = generatePath();                                                                //產生初始解=最佳解
        double bestDistance = calDistance(bestSol);                                                   //初始解長度
        int[] oldSol = new int[totalCity];                                                            //宣告當前解
        boolean betterSol = true;                                                                        //檢查較佳解found?
        System.arraycopy(bestSol, 0, oldSol, 0, totalCity);                                //當前解=初始解

        int times = 1;                                                                                  //搜尋次數
        while (betterSol) {                                                                             //當找不到更佳解時 停止迴圈
            ArrayList<int[]> neighborSolList = new ArrayList<>();                                     //相鄰解list
            ArrayList<Double> neighborSolDistList = new ArrayList<>();                                //相鄰解距離list
            betterSol = false;

            int findNeighborTimes = neighborRange;                                                         //相鄰解數量產生上限
            while (findNeighborTimes-- != 0) {                                                             //相鄰解產生迴圈
                int[] neighborSol = generate(oldSol);                                                 //根據當前解產生相鄰解
                neighborSolList.add(neighborSol);
                neighborSolDistList.add(calDistance(neighborSol));
            }
            multiSort(neighborSolDistList, neighborSolDistList, neighborSolList);                  //根據Distance排序(小到大)
            oldSol = neighborSolList.get(0);

            if (calDistance(oldSol) < bestDistance) {                                                 //判斷當前解是否為最佳解
                betterSol = true;
                for (int i = 0; i < totalCity; i++)
                    bestSol[i] = oldSol[i];
                bestDistance = calDistance(bestSol);
                System.out.println(times + "次" + " distance:" + bestDistance + "\npath:" + Arrays.toString(bestSol));
            }
            times++;
        }
        System.out.println("\nbest solution \ndistance:" + bestDistance + "\npath:" + Arrays.toString(bestSol));
        drawPath(bestSol);
    }

    public static void main(String[] args) throws IOException {
        HC_TSP HC = new HC_TSP();
        HC.run();
    }
}