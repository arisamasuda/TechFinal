package clawlerfinal;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MakeRanking {
    public static void main(String[] args) {
        Map<String, Integer> shokuzaiMap = new HashMap<>();
        Path filePath = Paths.get("C:\\TechTraining\\resources\\nikuListId1.csv");// ランキングを出したいファイル名を指定する

        // ランキング作成のためにcsvからMapに格納
        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            int cnt = 0;
            while ((line = br.readLine()) != null) {
                String[] cols = line.split(",");
                cnt++;
                if (cnt == 1) {
                    continue;
                }
                cols[0] = cols[0].replace("\"", "");
                cols[1] = cols[1].replace("\"", "");
                shokuzaiMap.put(cols[0], Integer.parseInt(cols[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(shokuzaiMap);

        // ランキングを作る
        ranking(shokuzaiMap);
    }

    // ランキング作るメソッド
    public static void ranking(Map<String, Integer> map) {
        Map<Integer, List<String>> matomeMap = new TreeMap<>();// Map<順位, List<食材一覧, 使用回数>>
        Map<Integer, Integer> rankMap = new TreeMap<>(new Comparator<Integer>() {// Map<使用回数, 順位>
            public int compare(Integer m, Integer n) {
                return ((Integer) m).compareTo(n) * -1;
            }
        });

        // 使用回数によるランク付け
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            int number = entry.getValue();
            rankMap.put(number, rankMap.getOrDefault(number, 0) + 1);
        }
        int rank = 1;
        for (Map.Entry<Integer, Integer> entry : rankMap.entrySet()) {
            rank += entry.setValue(rank);
        }

        System.out.println(rankMap);

        // 順位、使用回数、食材をMapにまとめる
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println(entry);
            String shokuzai = entry.getKey();
            int num = entry.getValue();
            int junni = rankMap.get(num);
            // System.out.println(junni + ":" + shokuzai + "," + num);
            if (matomeMap.containsKey(junni)) {
                List<String> zairyouNum = matomeMap.get(junni);
                String zairyou = zairyouNum.get(0);
                zairyou += "、" + shokuzai;
                zairyouNum.set(0, zairyou);
            } else {
                List<String> zairyouNum = new ArrayList<>();
                zairyouNum.add(entry.getKey());
                zairyouNum.add(String.valueOf(num));
                matomeMap.put(junni, zairyouNum);
            }
        }

        System.out.println(matomeMap);

        // 順位と食材一覧の書き出し
        int i = 0;

        for (Map.Entry<Integer, List<String>> entry : matomeMap.entrySet()) {
            i++;
            if (i == 15) {
                break;
            }
            System.out.println(entry.getKey() + " : " + entry.getValue());

        }

    }

}
