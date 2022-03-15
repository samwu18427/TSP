import java.awt.*;
import java.util.ArrayList;

public class DrawTspPath extends Canvas {
    ArrayList<CityNode> cityList;
    int[] path;
    int[] x;
    int[] y;

    public DrawTspPath(ArrayList<CityNode> cityList, int[] path) {
        this.cityList = cityList;
        this.path = path;
        x = new int[cityList.size()];
        y = new int[cityList.size()];
        for (int i = 0; i < cityList.size(); i++) {
            x[i] = cityList.get(i).x;
            y[i] = cityList.get(i).y;
        }
    }

    public void paint(Graphics g) {
        g.setColor(Color.RED);
        for (int i = 0; i < cityList.size(); i++) {
            g.fillOval(x[i] * 8, y[i] * 8, 5, 5);
            g.drawString(String.valueOf(i + 1), x[i] * 8, y[i] * 8);
        }

        g.setColor(Color.CYAN);
        for (int i = 0; i < cityList.size() - 1; i++) {
            try {
                g.drawLine(x[path[i] - 1] * 8, y[path[i] - 1] * 8, x[path[i + 1] - 1] * 8, y[path[i + 1] - 1] * 8);
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        g.drawLine(x[path[0] - 1] * 8, y[path[0] - 1] * 8, x[path[cityList.size() - 1] - 1] * 8, y[path[cityList.size() - 1] - 1] * 8);
        System.out.println("DONE");
    }
}