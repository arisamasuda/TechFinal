package clawlerfinal;

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
            int i = 2;// おかず
            crawlMenu(i);// クロール ＋ csvファイルの出力
            i = 4;// スープ
            crawlMenu(i);
            i = 1;// 主食
            crawlMenu(i);
            String category = "パスタ";
            crawl(category);
            category = "サラダ";
            crawl(category);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("クロールを終了します。");
    }

    // クックパッドに掲載されているレシピのクローリング＋csvファイルの出力
    private static void crawlMenu(int idNum) throws IOException {
        Map<String, Integer> shokuzaiMap = new HashMap<>();// 食材の使用回数をカウントするためのMap
        Map<String, Integer> nikuMap = new HashMap<>();// お肉の使用回数をカウントするためのMap
        Map<String, Integer> mainMap = new HashMap<>();// メイン食材の使用回数をカウントするためのMap
        List<String> shokuzaiList = new ArrayList<>();// csvに書き出すための食材カウントList
        List<String> nikuCsvList = new ArrayList<>();// csvに書き出すためのお肉カウントList
        List<String> mainCsvList = new ArrayList<>();// csvに書き出すためのメイン食材カウントList
        List<String> menuList = new ArrayList<>();// csvに書き出すためのレシピList

        // 同じ食材の名称を統一（データクレンジング的な）
        List<String> list1 = new ArrayList<>(Arrays.asList("たまご", "卵", "タマゴ", "玉子"));
        List<String> list2 = new ArrayList<>(Arrays.asList("きゅうり", "胡瓜", "キュウリ"));
        List<String> list3 = new ArrayList<>(Arrays.asList("にんじん", "人参", "ニンジン"));
        List<String> list4 = new ArrayList<>(Arrays.asList("玉ねぎ", "たまねぎ", "タマネギ", "玉葱", "玉ネギ"));
        List<String> list5 = new ArrayList<>(Arrays.asList("ピーマン", "ぴーまん"));
        List<String> list6 = new ArrayList<>(Arrays.asList("とまと", "トマト"));
        List<String> list7 = new ArrayList<>(Arrays.asList("じゃがいも", "ジャガイモ", "じゃが芋"));
        List<String> list8 = new ArrayList<>(Arrays.asList("ごはん", "ご飯", "お米", "米"));
        List<String> list9 = new ArrayList<>(Arrays.asList("ささみ", "鶏ささみ"));
        List<String> list10 = new ArrayList<>(Arrays.asList("豚バラ肉", "豚バラ"));
        List<String> list11 = new ArrayList<>(Arrays.asList("パスタ"));
        List<String> list12 = new ArrayList<>(Arrays.asList("うどん"));
        Map<String, List<String>> wordMap = new HashMap<>();
        wordMap.put("卵", list1);
        wordMap.put("きゅうり", list2);
        wordMap.put("にんじん", list3);
        wordMap.put("たまねぎ", list4);
        wordMap.put("ピーマン", list5);
        wordMap.put("トマト", list6);
        wordMap.put("じゃがいも", list7);
        wordMap.put("ごはん", list8);
        wordMap.put("ささみ", list9);
        wordMap.put("豚バラ肉", list10);
        wordMap.put("パスタ", list11);
        wordMap.put("うどん", list12);
        

        // いらない文字や調味料の除去するためのList
        List<String> removeMoji = new ArrayList<>(
                Arrays.asList("■", "★", "☆", "〇", "●", "◎", "◇", "◆", "*", "※", "a", "b", "c", "d", "e"));
        List<String> removeIng = new ArrayList<>(Arrays.asList("砂糖", "醤油", "水", "塩コショウ", "塩胡椒", "塩", "胡椒", "たれ", "タレ",
                "ドレッシング", "マヨネーズ", "ケチャップ", "粉", "塩"));
        List<String> ajituke = new ArrayList<>(
                Arrays.asList("少々", "小さじ", "小匙", "大さじ", "大匙", "適量", "大", "小", "お好みで", "cc", "カップ"));

        // お肉リスト
        List<String> nikuList = new ArrayList<>(Arrays.asList("肉", "豚", "鶏", "牛", "羊", "ささみ", "手羽先", "手羽中"));

        // メイン食材のリスト
        List<String> mainList = new ArrayList<>(
                Arrays.asList("米", "ごはん", "ご飯", "パスタ", "マカロニ", "麺", "うどん", "そうめん", "ラーメン"));

        // いよいよクローリング開始
        String url = "https://cookpad.com/s/recipe?category_id=" + idNum + "&mode=all&page=1";
        Document doc = Jsoup.connect(url).get();

        // ページ数を調べる
        Element pageLast = doc.select("div#main > div.recipe_filter.block10").get(0);
        String lastPage = pageLast.select("div.f_right").text().substring(3, 6).trim();

        // レシピの取り出し
        for (int i = 1; i < Integer.parseInt(lastPage) + 1; i++) {
            String urlEach = "https://cookpad.com/s/recipe?category_id=" + idNum + "&mode=all&page=" + i;
            System.out.println(urlEach);
            Document docEach = Jsoup.connect(urlEach).get();
            Element elMenuList = docEach.select("div.block20").get(0);
            for (Element child : elMenuList.children()) {
                if (child.hasClass("recipe-preview")) {
                    Menu m = new Menu();
                    m.title = child.select("div.recipe-text").select("a").text();
                    m.link = "https://cookpad.com" + child.select("div.recipe-text").select("a").attr("href");

                    // 食材の書き出し開始
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

                        // 飾り文字の除去
                        for (String moji : removeMoji) {
                            ingredient = ingredient.replace(moji, "");
                            if (ingredient.contains("(")) {
                                ingredient = ingredient.substring(0, ingredient.indexOf("("));
                            }
                            if (ingredient.contains("（")) {
                                ingredient = ingredient.substring(0, ingredient.indexOf("（"));
                            }
                        }

                        // 食材名の統一
                        for (Map.Entry<String, List<String>> entry : wordMap.entrySet()) {
                            for (String word : entry.getValue()) {
                                if (ingredient.contains(word)) {
                                    ingredient = entry.getKey();
                                    break;
                                }
                            }
                        }
                        ingredient.trim();// 空白の除去
                        m.ingredients.add(ingredient);
                        m.ingredients.remove("");

                        // すべての食材をMenuクラスのingredientsAllに書き込む
                        m.ingredientsAll += ingredient + "、";

                        // 調味料は食材リストに含めない
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
                    // 食材の書き出し終了

                    // お肉はnikuMapでカウントする
                    for (String shokuzai : m.ingredients) {
                        for (String niku : nikuList) {
                            if (shokuzai.contains(niku)) {
                                nikuMap.put(shokuzai, nikuMap.getOrDefault(shokuzai, 0) + 1);
                                break;
                            }
                        }
                    }

                    // メイン食材はmainMapでカウントする
                    for (String shokuzai : m.ingredients) {
                        for (String mainShokuzai : mainList) {
                            if (shokuzai.contains(mainShokuzai)) {
                                mainMap.put(shokuzai, mainMap.getOrDefault(shokuzai, 0) + 1);
                                break;
                            }
                        }
                    }

                    // 食材の使用回数をカウントする
                    for (String shokuzai : m.ingredients) {
                        shokuzaiMap.put(shokuzai, shokuzaiMap.getOrDefault(shokuzai, 0) + 1);
                    }

                    System.out.println(m.title);

                    // csv書き出しのためのリスト作成
                    m.makeMenu();
                    menuList.add(m.menuContents);
                }

            }

        }
        for (Map.Entry<String, Integer> shokuzai : shokuzaiMap.entrySet()) {
            shokuzaiList.add("\"" + shokuzai.getKey() + "\",\"" + shokuzai.getValue() + "\"");
        }
        for (Map.Entry<String, Integer> shokuzai : nikuMap.entrySet()) {
            nikuCsvList.add("\"" + shokuzai.getKey() + "\",\"" + shokuzai.getValue() + "\"");
        }
        for (Map.Entry<String, Integer> shokuzai : mainMap.entrySet()) {
            mainCsvList.add("\"" + shokuzai.getKey() + "\",\"" + shokuzai.getValue() + "\"");
        }

        // csvファイルの出力
        Path filePath1 = Paths.get("C:\\TechTraining\\resources\\menuListId" + idNum + ".csv");
        String header1 = "レシピ名,リンク, 材料一覧";
        makeCsv(menuList, filePath1, header1);
        Path filePath2 = Paths.get("C:\\TechTraining\\resources\\ingredientListId" + idNum + ".csv");
        String header2 = "食材,使用回数";
        makeCsv(shokuzaiList, filePath2, header2);
        Path filePath3 = Paths.get("C:\\TechTraining\\resources\\nikuListId" + idNum + ".csv");
        String header3 = "肉の種類, 使用回数";
        makeCsv(nikuCsvList, filePath3, header3);
        Path filePath4 = Paths.get("C:\\TechTraining\\resources\\mainListId" + idNum + ".csv");
        String header4 = "メイン食材の種類, 使用回数";
        makeCsv(mainCsvList, filePath4, header4);
    }

    // クックパッドに掲載されているレシピ(カテゴリー別)のクローリング＋csvファイルの出力
    public static void crawl(String category) throws IOException {
        Map<String, Integer> shokuzaiMap = new HashMap<>();// 食材の使用回数をカウントするためのMap
        Map<String, Integer> nikuMap = new HashMap<>();// お肉の使用回数をカウントするためのMap
        Map<String, Integer> mainMap = new HashMap<>();// メイン食材の使用回数をカウントするためのMap
        List<String> shokuzaiList = new ArrayList<>();// csvに書き出すための食材カウントList
        List<String> nikuCsvList = new ArrayList<>();// csvに書き出すためのお肉カウントList
        List<String> mainCsvList = new ArrayList<>();// csvに書き出すためのメイン食材カウントList
        List<String> menuList = new ArrayList<>();// csvに書き出すためのレシピList

        // 同じ食材の名称を統一（データクレンジング的な）
        List<String> list1 = new ArrayList<>(Arrays.asList("たまご", "卵", "タマゴ", "玉子"));
        List<String> list2 = new ArrayList<>(Arrays.asList("きゅうり", "胡瓜", "キュウリ"));
        List<String> list3 = new ArrayList<>(Arrays.asList("にんじん", "人参", "ニンジン"));
        List<String> list4 = new ArrayList<>(Arrays.asList("玉ねぎ", "たまねぎ", "タマネギ", "玉葱", "玉ネギ"));
        List<String> list5 = new ArrayList<>(Arrays.asList("ピーマン", "ぴーまん"));
        List<String> list6 = new ArrayList<>(Arrays.asList("とまと", "トマト"));
        List<String> list7 = new ArrayList<>(Arrays.asList("じゃがいも", "ジャガイモ", "じゃが芋"));
        List<String> list8 = new ArrayList<>(Arrays.asList("ごはん", "ご飯", "お米", "米"));
        List<String> list9 = new ArrayList<>(Arrays.asList("ささみ", "鶏ささみ"));
        List<String> list10 = new ArrayList<>(Arrays.asList("豚バラ肉", "豚バラ"));
        Map<String, List<String>> wordMap = new HashMap<>();
        wordMap.put("卵", list1);
        wordMap.put("きゅうり", list2);
        wordMap.put("にんじん", list3);
        wordMap.put("たまねぎ", list4);
        wordMap.put("ピーマン", list5);
        wordMap.put("トマト", list6);
        wordMap.put("じゃがいも", list7);
        wordMap.put("ごはん", list8);
        wordMap.put("ささみ", list9);
        wordMap.put("豚バラ肉", list10);

        // いらない文字や調味料の除去するためのList
        List<String> removeMoji = new ArrayList<>(
                Arrays.asList("■", "★", "☆", "〇", "●", "◎", "◇", "◆", "*", "※", "a", "b", "c", "d", "e"));
        List<String> removeIng = new ArrayList<>(Arrays.asList("砂糖", "醤油", "水", "塩コショウ", "塩胡椒", "塩", "胡椒", "たれ", "タレ",
                "ドレッシング", "マヨネーズ", "ケチャップ", "粉", "塩"));
        List<String> ajituke = new ArrayList<>(
                Arrays.asList("少々", "小さじ", "小匙", "大さじ", "大匙", "適量", "大", "小", "お好みで", "cc", "カップ"));

        // お肉リスト
        List<String> nikuList = new ArrayList<>(Arrays.asList("肉", "豚", "鶏", "牛", "羊", "ささみ", "手羽先", "手羽中"));

        // メイン食材のリスト
        List<String> mainList = new ArrayList<>(
                Arrays.asList("米", "ごはん", "ご飯", "パスタ", "マカロニ", "麺", "うどん", "そうめん", "ラーメン"));

        // いよいよクローリング開始
        String url = "https://cookpad.com/search/" + category + "?order=date&page=1";
        Document doc = Jsoup.connect(url).get();

        // ページ数を調べる
        Element pageLast = doc.select("div#main_content > div.navigation").get(0);
        String lastPage = pageLast.select("div.paginator").text().substring(3, 6);

        // レシピの取り出し
        for (int i = 1; i < /* Integer.parseInt(lastPage) + 1 */100; i++) {
            String urlEach = "https://cookpad.com//search/" + category + "?order=date&page=" + i;
            System.out.println(urlEach);
            Document docEach = Jsoup.connect(urlEach).get();
            Element elMenuList = docEach.select("div.recipes_section").get(0);
            for (Element child : elMenuList.children()) {
                if (child.hasClass("recipe-preview")) {
                    Menu m = new Menu();
                    m.title = child.select("div.recipe-text").select("a").text();
                    m.link = "https://cookpad.com" + child.select("div.recipe-text").select("a").attr("href");

                    // 食材の書き出し
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

                        // 飾り文字の除去
                        for (String moji : removeMoji) {
                            ingredient = ingredient.replace(moji, "");
                            if (ingredient.contains("(")) {
                                ingredient = ingredient.substring(0, ingredient.indexOf("("));
                            }
                            if (ingredient.contains("（")) {
                                ingredient = ingredient.substring(0, ingredient.indexOf("（"));
                            }
                        }

                        ingredient.trim();
                        m.ingredients.add(ingredient);
                        m.ingredients.remove("");

                        // 食材名の統一
                        for (Map.Entry<String, List<String>> entry : wordMap.entrySet()) {
                            for (String word : entry.getValue()) {
                                if (ingredient.contains(word)) {
                                    ingredient = entry.getKey();
                                    break;
                                }
                            }
                        }
                        ingredient.trim();// 空白の除去
                        m.ingredients.add(ingredient);
                        m.ingredients.remove("");

                        // すべての食材をMenuクラスのingredientsAllに書き込む
                        m.ingredientsAll += ingredient + "、";

                        // 調味料は食材リストに含めない
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
                    // 食材の書き出し終了

                    // お肉はnikuMapでカウントする
                    for (String shokuzai : m.ingredients) {
                        for (String niku : nikuList) {
                            if (shokuzai.contains(niku)) {
                                nikuMap.put(shokuzai, nikuMap.getOrDefault(shokuzai, 0) + 1);
                                break;
                            }
                        }
                    }

                    // メイン食材はmainMapでカウントする
                    for (String shokuzai : m.ingredients) {
                        for (String mainShokuzai : mainList) {
                            if (shokuzai.contains(mainShokuzai)) {
                                mainMap.put(shokuzai, mainMap.getOrDefault(shokuzai, 0) + 1);
                                break;
                            }
                        }
                    }

                    // 食材の使用回数をカウントする
                    for (String shokuzai : m.ingredients) {
                        shokuzaiMap.put(shokuzai, shokuzaiMap.getOrDefault(shokuzai, 0) + 1);
                    }

                    System.out.println(m.title);

                    // csv書き出しのためのリスト作成
                    m.makeMenu();
                    menuList.add(m.menuContents);
                }
            }

        }
        for (Map.Entry<String, Integer> shokuzai : shokuzaiMap.entrySet()) {
            shokuzaiList.add("\"" + shokuzai.getKey() + "\",\"" + shokuzai.getValue() + "\"");
        }
        for (Map.Entry<String, Integer> shokuzai : nikuMap.entrySet()) {
            nikuCsvList.add("\"" + shokuzai.getKey() + "\",\"" + shokuzai.getValue() + "\"");
        }
        for (Map.Entry<String, Integer> shokuzai : mainMap.entrySet()) {
            mainCsvList.add("\"" + shokuzai.getKey() + "\",\"" + shokuzai.getValue() + "\"");
        }

        // csvファイルの出力
        Path filePath1 = Paths.get("C:\\TechTraining\\resources\\" + category + "List.csv");
        String header1 = "レシピ名,リンク, 材料一覧";
        makeCsv(menuList, filePath1, header1);
        Path filePath2 = Paths.get("C:\\TechTraining\\resources\\" + category + "ingredientList.csv");
        String header2 = "食材,使用回数";
        makeCsv(shokuzaiList, filePath2, header2);
        Path filePath3 = Paths.get("C:\\TechTraining\\resources\\" + category + "nikuList.csv");
        String header3 = "肉の種類,使用回数";
        makeCsv(nikuCsvList, filePath3, header3);
        Path filePath4 = Paths.get("C:\\TechTraining\\resources\\" + category + "mainList.csv");
        String header4 = "メイン食材の種類,使用回数";
        makeCsv(mainCsvList, filePath4, header4);
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

    void makeMenu() {
        this.menuContents = "\"" + this.title + "\",\"" + this.link + "\",\"" + this.ingredientsAll + "\"";
    }
}