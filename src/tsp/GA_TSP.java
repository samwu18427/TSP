package tsp;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class GA_TSP {
    String FILE_PATH ="Dataset/eil51";
    final int MAX_POPULATION_SIZE = 300;                         //母代群Maximum 太少 收斂過快 太多不易收斂
    final int TOURNAMENT_SELECT_NUM = 3;                         //競賽個數      太多 弱勢個體(fitness低)不易被選上
    final double CROSSOVER_RATE = 0.9;                           //交配機率
    final double MUTATION_RATE = 0.1;                            //突變機率
    final int MAX_GENERATION = 100000;                           //迭代上限 終止條件
    static ArrayList<CityNode> cityList;                                //city資料集
    int totalCity;                                              //city數量

    ArrayList<int[]> generatePopulation() {                          //產生母代群
        ArrayList<int[]> pop = new ArrayList<>();

        for (int i = 0; i < MAX_POPULATION_SIZE; i++) {
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
            pop.add(path);
        }
        return pop;
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

    double totalDistance(int[] path) {                                //計算總路徑長
        double totalDistance = 0;
        for (int index = 0; index < totalCity - 1; index++)
            totalDistance += twoPointDistance(path[index], path[index + 1]);

        return totalDistance;
    }

    double calFitness(int[] path) {                                  //計算fitness
        return Math.exp(1.0 / totalDistance(path));                         //若不*exponential fitness會是6.4760831613195E-4的樣子
    }                                                                       //*exponential 結果較直觀,ex:1.0005612

    ArrayList<int[]> selectGen(ArrayList<int[]> pop) {                               //tournament selection
        ArrayList<int[]> candidateGen = new ArrayList<>();                                 //候選基因
        ArrayList<Double> candiGenFitness = new ArrayList<>();                            //候選基因fitness
        ArrayList<int[]> result = new ArrayList<>();                                        //選擇結果基因
        boolean[] chose = new boolean[MAX_POPULATION_SIZE];                                 //檢查gen是否選過
        Arrays.fill(chose, false);

        for (int i = 0; i < TOURNAMENT_SELECT_NUM; i++) {
            int index = (int) (Math.random() * MAX_POPULATION_SIZE);                        //亂數選擇候選基因
            while (chose[index])
                index = (int) (Math.random() * MAX_POPULATION_SIZE);
            chose[index] = true;
            candidateGen.add(pop.get(index));
            candiGenFitness.add(calFitness(pop.get(index)));
        }

        multiSort(candiGenFitness, candidateGen);                             //根據fitness排序 預設由小到大排序, MutiSort(排序key,排序目標,排序目標....)
        Collections.reverse(candidateGen);                                     //大到小排序

        result.add(candidateGen.get(0));
        result.add(candidateGen.get(1));
        return result;
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

    ArrayList<int[]> crossover(ArrayList<int[]> selected_gen) {                //交配
        ArrayList<int[]> child_gen = new ArrayList<>();
        int[] motherGen = selected_gen.get(0);
        int[] fatherGen = selected_gen.get(1);
        int[] childGen1 = new int[totalCity];
        int[] childGen2 = new int[totalCity];
        int point = (int) (Math.random() * totalCity);                               //亂數生成段點,依段點前後分別組合父代母代

        for (int i = 0; i < point; i++) {                                             //ex point=3
            childGen1[i] = motherGen[(i)];                                          //father[2,4,5,1,3,6]
            childGen2[i] = fatherGen[(i)];                                          //mother[6,2,3,5,4,1]
        }                                                                             //child1[2,4,5,5,4,1]
        for (int i = point; i < totalCity; i++) {                                    //child2[6,2,3,1,3,6]
            childGen1[i] = fatherGen[(i)];                                          //child中因為有重複的city所以不合法
            childGen2[i] = motherGen[(i)];
        }

        for (int i = 0; i < totalCity - 1; i++)                                              //檢查合法 將重複city改成-1
            for (int j = i + 1; j < totalCity; j++) {                                          //child1[2,4,5,-1,-1,1]
                if (childGen1[i] != -1 && childGen1[j] == childGen1[i])                 //child2[6,2,3,1,-1,-1]
                    childGen1[j] = -1;
                if (childGen2[i] != -1 && childGen2[j] == childGen2[i])
                    childGen2[j] = -1;
            }

        for (int i = 0; i < totalCity; i++) {                                          //將-1的部分根據後段屬於父代or母代之順序填入合法city
            if (childGen1[i] == -1)                                                    //father[2,4,5,1,3,6]
                for (int j = 0; j < totalCity; j++)                                    //mother[6,2,3,5,4,1]
                    if (isContainCity(childGen1, motherGen[j])) {                         //child1[2,4,5,6,3,1] (根據母代)
                        childGen1[i] = motherGen[j];                                  //child2[6,2,3,1,4,5] (根據父代)
                        break;
                    }
            if (childGen2[i] == -1)
                for (int j = 0; j < totalCity; j++)
                    if (isContainCity(childGen2, fatherGen[j])) {
                        childGen2[i] = fatherGen[j];
                        break;
                    }
        }

        child_gen.add(childGen1);
        child_gen.add(childGen2);
        return child_gen;
    }

    boolean isContainCity(int[] ary, int target) {            //基因合法化時 尋找缺少的city
        boolean result = false;
        for (int i : ary)
            if (i == target) {
                result = true;
                break;
            }
        return !result;
    }

    void mutate(int[] gen) {                            //變異 亂數選擇2點交換
        int index1 = (int) (Math.random() * totalCity);
        int index2 = (int) (Math.random() * totalCity);
        while (index1 == index2) {
            index1 = (int) (Math.random() * totalCity);
            index2 = (int) (Math.random() * totalCity);
        }
        int temp = gen[index1];
        gen[index1] = gen[index2];
        gen[index2] = temp;
    }

    void popEvolve(ArrayList<int[]> pop, int[] childGen1, int[] childGen2) {                //母代群進化 新增子代 踢除fitness較差之基因
        ArrayList<Double> popFitness = new ArrayList<>();
        pop.add(childGen1);
        pop.add(childGen2);
        for (int[] path : pop)
            popFitness.add(calFitness(path));
        multiSort(popFitness, pop);

        pop.remove(0);
        pop.remove(0);
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

    void run() throws IOException {                                                 //GA主體
        ArrayList<int[]> pop;                                                                      //母代群
        ArrayList<int[]> selectedGen;
        ArrayList<int[]> childGen;

        cityList = CityNode.inputCity(FILE_PATH);                                                           //讀取資料集eil51.txt
        totalCity = cityList.size();                                                              //city數量
        pop = generatePopulation();

        int generation = 0;                                                       //迭代代數
        int[] bestSol = pop.get(0);                                               //最優解
        double bestFitness = calFitness(bestSol);

        while (generation != MAX_GENERATION) {                                     //終止條件
            selectedGen = selectGen(pop);                                      //selection

            if (Math.random() <= CROSSOVER_RATE) {                                //crossover
                childGen = crossover(selectedGen);
                int[] childGen1 = childGen.get(0);
                int[] childGen2 = childGen.get(1);

                if (Math.random() <= MUTATION_RATE)                                 //mutation
                    mutate(childGen1);
                if (Math.random() <= MUTATION_RATE)
                    mutate(childGen2);

                if (calFitness(childGen1) > bestFitness) {
                    bestFitness = calFitness(childGen1);
                    bestSol = childGen1;
                    System.out.println(generation + "次" + " distance:" + totalDistance(bestSol)
                            + " fitness:" + bestFitness + " path:" + Arrays.toString(bestSol));
                }
                if (calFitness(childGen2) > bestFitness) {
                    bestFitness = calFitness(childGen2);
                    bestSol = childGen2;
                    System.out.println(generation + "次" + " distance:" + totalDistance(bestSol)
                            + " fitness:" + bestFitness + " path:" + Arrays.toString(bestSol));
                }
                popEvolve(pop, childGen1, childGen2);                               //population evolution
            }
            generation++;
        }
        System.out.println("\nbest solution \ndistance:" + totalDistance(bestSol) + "\nfitness:" + bestFitness
                + "\npath:" + Arrays.toString(bestSol));
        drawPath(bestSol);
    }

    public static void main(String[] args) throws IOException {
        GA_TSP GA = new GA_TSP();
        GA.run();
    }
}