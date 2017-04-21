package sample;

/**
 * Created by weiss on 4/21/2017.
 */
public class Substitution {
    static int firstChar;
    static int secondChar;

    static String substituted(String text, char[] key) {
        if (text.equals("")) {
            System.out.println("Test0");
            return "";
        }
        char[] textArr = text.toCharArray();
        char[] substitution = new char[textArr.length];
        for (int i = 0; i < textArr.length; i++) {
            substitution[i] = substituted(textArr[i], key);
        }
        return String.valueOf(substitution);
    }

    private static char substituted(char input, char[] key) {
        if (input >= 'a' && input <= 'z') {
            char newChar = key[(int) input - 97];
            if (newChar == '*') return input;
            return newChar;
        }
        return input;
    }

    static boolean keyValid(char[] key) {
        for (int i = 0; i < key.length; i++) {
            for (int j = i+1; j < key.length; j++) {
                if (key[i] == key[j] && key[i] != '*') {
                    firstChar = i;
                    secondChar = j;
                    return false;
                }
            }
        }
        return true;
    }
}
