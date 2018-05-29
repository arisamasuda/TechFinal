package jp.co.goalist.clawlerfinal;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Main {
    public static void main(String[] args) {
        System.out.println("クロールを開始します。");
        try {
            clawlMenu();// クロール ＋ csvファイルの出力
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("クロールを終了します。");
    }

    // クックパッドに掲載されているレシピのクローリング＋csvファイルの出力
    private static void clawlMenu() throws IOException {
        Map<String, Integer> shokuzaiMap = new HashMap<>();
        List<String> shokuzaiList = new ArrayList<>();
        List<String> menuList = new ArrayList<>();

        List<String> list1 = new ArrayList<>(Arrays.asList("たまご", "卵", "タマゴ", "玉子"));
        List<String> list2 = new ArrayList<>(Arrays.asList("きゅうり", "胡瓜", "キュウリ"));
        List<String> list3 = new ArrayList<>(Arrays.asList("にんじん", "人参", "ニンジン"));
        List<String> list4 = new ArrayList<>(Arrays.asList("玉ねぎ", "たまねぎ", "タマネギ", "玉葱", "玉ネギ"));
        List<String> list5 = new ArrayList<>(Arrays.asList("ピーマン", "ぴーまん"));
        List<String> list6 = new ArrayList<>(Arrays.asList("とまと", "トマト"));
        List<String> list7 = new ArrayList<>(Arrays.asList("じゃがいも", "ジャガイモ", "じゃが芋"));
        Map<String, List<String>> wordMap = new HashMap<>();
        wordMap.put("卵", list1);
        wordMap.put("きゅうり", list2);
        wordMap.put("にんじん", list3);
        wordMap.put("たまねぎ", list4);
        wordMap.put("ピーマン", list5);
        wordMap.put("トマト", list6);
        wordMap.put("じゃがいも", list7);

        // いらない文字や調味料の除去
        List<String> removeMoji = new ArrayList<>(Arrays.asList("■", "★", "☆", "〇", "●", "◎", "◇", "◆"));
        List<String> removeIng = new ArrayList<>(
                Arrays.asList("砂糖", "醤油", "水", "塩コショウ", "塩胡椒", "たれ", "タレ", "ドレッシング", "マヨネーズ", "ケチャップ", "粉", "塩"));
        List<String> ajituke = new ArrayList<>(
                Arrays.asList("少々", "小さじ", "小匙", "大さじ", "大匙", "適量", "大", "小", "お好みで", "cc", "カップ"));

        String url = "https://cookpad.com/s/recipe?category_id=2&mode=all&page=1";
        Document doc = Jsoup.connect(url).get();

        // ページ数を調べる
        Element pageLast = doc.select("div#main > div.recipe_filter.block10").get(0);
        String lastPage = pageLast.select("div.f_right").text().substring(3, 6);

        // レシピの取り出し
        for (int i = 1; i < /* Integer.parseInt(lastPage) + 1 */6; i++) {
            String urlEach = "https://cookpad.com/s/recipe?category_id=2&mode=all&page=" + i;
            System.out.println(urlEach);
            Document docEach = Jsoup.connect(urlEach).get();
            Element elMenuList = docEach.select("div.block20").get(0);
            for (Element child : elMenuList.children()) {
                if (child.hasClass("recipe-preview")) {
                    Menu m = new Menu();
                    m.title = child.select("div.recipe-text").select("a").text();
                    m.link = "https://cookpad.com" + child.select("div.recipe-text").select("a").attr("href");

                    // 材料と作業工程の書き出し
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Document docMenu = Jsoup.connect(m.link).get();
                    Element elIngredients = docMenu.select("div#ingredients > div#ingredients_list").get(0);
                    for (Element ing : elIngredients.children()) {
                        String ingredient = ing.select("span").text();
                        String quantity = ing.select("div.ingredient_quantity").text();

                        // 調味料は食材リストに含めない
                        for (String moji : removeMoji) {// いらない文字の除去
                            ingredient = ingredient.replace(moji, "");
                            if (ingredient.contains("(")) {
                                ingredient = ingredient.substring(0, ingredient.indexOf("("));
                            }
                            if (ingredient.contains("（")) {
                                ingredient = ingredient.substring(0, ingredient.indexOf("（"));
                            }
                            for (Map.Entry<String, List<String>> entry : wordMap.entrySet()) {
                                for (String word : entry.getValue()) {
                                    if (ingredient.contains(word)) {
                                        ingredient = entry.getKey();
                                        break;
                                    }
                                }
                            }
                        }
                        m.ingredients.add(ingredient);
                        m.ingredients.remove("");
                        for (String aji : ajituke) {
                            if (quantity.contains(aji)) {
                                m.ingredients.remove(ingredient);
                            }
                        }
                        for (String aji : removeIng) {
                            if (ingredient.contains(aji)) {
                                m.ingredients.remove(ingredient);
                            }
                        }
                    }
                    System.out.println(m.ingredients);
                    for (String shokuzai : m.ingredients) {
                        shokuzaiMap.put(shokuzai, shokuzaiMap.getOrDefault(shokuzai, 0) + 1);

                    }
                    String menu = m.makeMenu();
                    menuList.add(m.menuContents);
                }
            }
            System.out.println(shokuzaiMap);

        }
        for (Map.Entry<String, Integer> shokuzai2 : shokuzaiMap.entrySet()) {
            shokuzaiList.add("\"" + shokuzai2.getKey() + "\",\"" + shokuzai2.getValue() + "\"");
        }

        // csvファイルの出力
        Path filePath1 = Paths.get("C:\\TechTraining\\resources\\menuList.csv");
        String header1 = "レシピ名,リンク, 材料一覧";
        makeCsv(menuList, filePath1, header1);
        Path filePath2 = Paths.get("C:\\TechTraining\\resources\\ingredientList.csv");
        String header2 = "材料,使用回数";
        makeCsv(shokuzaiList, filePath2, header2);
    }

    // csvファイル出力のメソッド
    private static void makeCsv(List<String> list, Path filePath, String header) {
        try {
            Files.deleteIfExists(filePath);
            Files.createFile(filePath);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try (BufferedWriter bw = Files.newBufferedWriter(filePath)) {
            bw.write(header);
            bw.newLine();
            for (String s : list) {
                bw.write(s);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Menu {
    String link;
    String title;
    List<String> ingredients = new ArrayList<>();
    String ingredientsAll = "";
    int stepNm;
    String menuContents;

    String makeMenu() {
        for (String ing : this.ingredients) {
            ingredientsAll += ing + "、";
        }
        this.menuContents = "\"" + this.title + "\",\"" + this.link + "\",\"" + this.ingredientsAll + "\"";
        return this.menuContents;
    }
}