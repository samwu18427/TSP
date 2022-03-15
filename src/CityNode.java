import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class CityNode {
    int ID;
    int x;
    int y;

    CityNode(int ID, int x, int y) {
        this.ID = ID;
        this.x = x;
        this.y = y;
    }

    public static ArrayList<CityNode> inputCity(String FILE_PATH) throws IOException {                                       //輸入城市資料集 格式:id x y, ex: 1 37 52, 2 49 49
        ArrayList<CityNode> cityList = new ArrayList<>();
//        BufferedReader br = new BufferedReader(new FileReader(FilePath));
        Scanner sc = new Scanner(new FileReader(FILE_PATH));
        while (sc.hasNext()) {
            int id = sc.nextInt();
            int x = sc.nextInt();
            int y = sc.nextInt();
//            System.out.println(id+","+x+","+y);
            CityNode city = new CityNode(id, x, y);
            cityList.add(city);
        }
        return cityList;
    }
}
