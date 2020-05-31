import java.io.*;
import java.util.*;

public class Game {
    static final int LEN = 10;
    static char board[][] = new char[LEN][LEN]; //[x][y]

    static Coord player = new Coord(0, 9, '+');
    static int turn = 0;
    static boolean done = false;

    static int explosionX[] = {0, 1, 1, 1, 0, -1, -1, -1, 0, 1, 2, 2, 2, 1, 0, -1, -2, -2, -2, -1};
    static int explosionY[] = {-1, -1, 0, 1, 1, 1, 0, -1, -2, -2, -1, 0, 1, 2, 2, 2, 1, 0, -1, -2};

    static Map<Character, Integer> priorities = new TreeMap<>();

    static PriorityQueue<Pair> toPut = new PriorityQueue<>();

    static String menuOptions[] = {"[a] Start Game", "[d] Instructions"};
    static String testOptions[] = {"[a] Falling Numbers", "[s] Mine Creation", "[d] Mine Detonation", "[f] Death"};

    static void clearScreen() throws InterruptedException, IOException {
        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
    }
    static void load() throws InterruptedException, IOException {
        clearScreen();
        System.out.println("Parsing Integers");
        typePrint("..............", 160);
        clearScreen();
        System.out.println("Creating Array");
        typePrint("..............", 160);
        clearScreen();
        System.out.println("Initializing Game");
        typePrint("..............", 160);
        clearScreen();
    }
    static void typePrint(String message, int delay) throws InterruptedException, IOException {
        if (message == null) return;
        for (int i = 0; i < message.length(); i++) {
            System.out.print(message.charAt(i));
            Thread.sleep(delay);
        }
    }
    static void play() throws InterruptedException, IOException {
        clearScreen();
        Scanner in = new Scanner(System.in);
        while (!done) {
            turn++;
            String line = "";
            board[player.x][player.y] = '+';
            toPut.add(new Pair(new Coord(player.x, player.y, '.'), 1));
            while (!move(line)) {
                clearScreen();
                printBoard();
                line = in.nextLine();
            }
            //check death
            toPut.add(new Pair(new Coord(player.x, player.y, '+'), 1));

            iterate();
            for (int i = 0; i <= (turn / 3); i++) spawn(-1, -1);
        }
        clearScreen();
        System.out.println("GAME OVER");
        printBoard();
    }
    public static void main(String[] args) throws InterruptedException, IOException {
        for (int i = 0; i < LEN; i++) {
            for (int j = 0; j < LEN; j++) {
                board[i][j] = '.';
            }
        }
        board[player.x][player.y] = '+';
        priorities.put('0', 1);
        priorities.put('#', 2);
        priorities.put('1', 3);  priorities.put('2', 3);  priorities.put('3', 3);  priorities.put('4', 3);  priorities.put('5', 3);
        priorities.put('+', 4);
        priorities.put('.', 5);
        menu();
    }
    static void spawn(int place, int tile) {
        if (place == -1) place = (int)(Math.random() * LEN);
        int lim = -1;
        if (turn < 10) lim = 2;
        else if (turn >= 10 && turn < 20) lim = 3;
        else if (turn >= 20 && turn < 30) lim = 4;
        else lim = 5;

        if (tile == -1) tile = (int)(Math.random() * (lim)) + 1;
        board[place][0] = (char)(tile + '0');
    }
    static void iterate() throws InterruptedException, IOException {
        int iteration = 0;
        while ((iteration == 0 || !toPut.isEmpty()) && !done) {
            iteration++;
            for (int i = 0; i < LEN; i++) {
                for (int j = 0; j < LEN; j++) {
                    if (board[i][j] == '0') board[i][j] = '.';
                }
            }
            PriorityQueue<Pair> remains = new PriorityQueue<>();
            while (!toPut.isEmpty()) {
                Pair cur = toPut.remove(); cur.s--;
                if (cur.s == 0) {
                  put(cur.f.x, cur.f.y, cur.f.type);
                  if (cur.f.type == '0') toPut.add(new Pair(new Coord(cur.f.x, cur.f.y, '.'), 2));
                }
                else remains.add(cur);
            }
            done = true;
            for (int i = 0; i < LEN; i++) {
                for (int j = 0; j < LEN; j++) {
                    if (board[i][j] == '+') done = false;
                }
            }
            if (!done) {
                toPut = remains;
                for (int i = 0; i < LEN; i++) {
                    for (int j = 0; j < LEN; j++) {
                        if (!Character.isDigit(board[i][j]) || Character.getNumericValue(board[i][j]) < iteration)
                            continue;
                        if (j + 1 < LEN) {
                            if (board[i][j + 1] == '#') {
                                toPut.add(new Pair(new Coord(i, j + 1, '.'), 1));
                                for (int k = 0; k < 20; k++) {
                                    toPut.add(new Pair(new Coord(i + explosionX[k], j + 1 + explosionY[k], '0'), ((k < 8) ? 1 : 2)));
                                }
                            } else if (Character.isDigit(board[i][j + 1]) && Character.getNumericValue(board[i][j + 1]) < iteration) {
                                toPut.add(new Pair(new Coord(i, j + 1, '#'), 1));
                            } else {
                                toPut.add(new Pair(new Coord(i, j + 1, board[i][j]), 1));
                            }
                        }
                        toPut.add(new Pair(new Coord(i, j, '.'), 1));
                    }
                }
            }
            Thread.sleep(500);
            clearScreen();
            printBoard();
        }
    }
    static void printBoard() {
        System.out.println("TURN: " + turn);
        for (int i = 0; i < LEN; i++) {
            for (int j = 0; j < LEN; j++) {
                System.out.print(board[j][i] + " ");
            }
            System.out.println();
        }
    }
    static boolean move(String line) {
        if (line.length() != 1) return false;
        char move = line.charAt(0);
        switch(move) {
            case 'w': if (put(player.x, player.y - 1, '+')) {
                player.y--; return true;
            } else return false;
            case 'a': if (put(player.x - 1, player.y, '+')) {
                player.x--; return true;
            } else return false;
            case 's': if (put(player.x, player.y + 1, '+')) {
                player.y++; return true;
            } else return false;
            case 'd': if (put(player.x + 1, player.y, '+')) {
                player.x++; return true;
            } else return false;
        }
        return false;
    }
    static public void menu() throws InterruptedException, IOException {
        clearScreen();
        Scanner in = new Scanner(System.in);
        typePrint("=====DIGIT_DODGER=====", 50); System.out.println();
        Thread.sleep(500);
        for (int i = 0; i < 2; i++) {
            typePrint(menuOptions[i], 50); System.out.println();
            Thread.sleep(500);
        }
        String choice = in.nextLine();
        if(choice.equals("a")) play();
        else if (choice.equals("d")) instructions();
        else if (choice.equals("super secret testing mode")) test();
        else menu();
    }
    static public void instructions() throws InterruptedException, IOException {
        clearScreen();
        BufferedReader br = new BufferedReader(new FileReader("Instructions.txt"));
        System.out.println("INSTRUCTIONS");
        Thread.sleep(800);
        for (int i = 0; i < 15; i++) {
            String line = br.readLine();
            typePrint(line, 50); System.out.println();
            Thread.sleep(500);
        }
        Scanner in = new Scanner(System.in);
        System.out.print("Enter to continue...");
        String next = in.nextLine();
        menu();
    }
    static boolean put(int x, int y, char z) {
        if (x < 0 || x >= LEN || y < 0 || y >= LEN) return false;
        board[x][y] = z;
        return true;
    }
    static void test() throws InterruptedException, IOException {
        clearScreen();
        System.out.println("TESTING MODE"); Thread.sleep(800);
        for (int i = 0; i < testOptions.length; i++) {
            typePrint(testOptions[i], 50); System.out.println();
            Thread.sleep(50);
        }
        Scanner in = new Scanner(System.in);
        String selections = in.nextLine();
        for (int i = 0;  i < selections.length(); i++) {
            for (int j = 0; j < LEN; j++) {
                for (int k = 0; k < LEN; k++) {
                    board[j][k] = '.';
                }
            }
            char selection = selections.charAt(i);
            clearScreen();
            switch(selection) {
                case 'a': testFallingNumbers(); break;
                case 's': testMineCreation(); break;
                case 'd': testMineDetonation(); break;
                case 'f': testDeath(); break;
            }
            System.out.print("Enter to continue...");
            String next = in.nextLine();
        }
    }
    static void testFallingNumbers() throws InterruptedException, IOException {
        typePrint("Falling Numbers", 50); System.out.println();
        Thread.sleep(500);
        clearScreen();
        spawn(0, 1);
        spawn(1, 2);
        spawn(2, 3);
        spawn(3, 4);
        spawn(4, 5);
        printBoard();
        iterate();
    }
    static void testMineCreation() throws InterruptedException, IOException {
        typePrint("Mine Creation", 50); System.out.println();
        Thread.sleep(500);
        clearScreen();
        spawn(0, 1);
        iterate();
        spawn(0, 3);
        iterate();
    }
    static void testMineDetonation() throws InterruptedException, IOException {
        typePrint("Mine Detonation", 50); System.out.println();
        Thread.sleep(500);
        clearScreen();
        board[3][3] = '#';
        spawn(3, 3);
        spawn(7, 4);
        iterate();
    }
    static void testDeath() throws InterruptedException, IOException {
        typePrint("Death", 50); System.out.println();
        Thread.sleep(500);
        clearScreen();
        board[0][2] = '+';
        board[player.x][player.y] = '.';
        spawn(0, 3);
        iterate();
    }
    static class Pair implements Comparable<Pair> {
        Coord f; int s;
        Pair(Coord a, int b) {
            f = a;
            s = b;
        }
        public int compareTo(Pair a) {
            return f.compareWith(a.f);
        }
    }
    static class Coord {
        int x, y;
        char type;
        public Coord(int a, int b, char c) {
            x = a; y = b; type = c;
        }
        public int compareWith(Coord a) {
            return -1 * (priorities.get(type) - priorities.get(a.type));
        }
    }
}
