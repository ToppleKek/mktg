import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.lang.StringBuilder;
import java.io.*;

/**
 * Markov chain text generator.
 * Place "corpus.txt" in the same path as the class, or
 * run the program with {@code argv[1] = path/to/corpus.txt}.
 * COSC1046A
 * @author Braeden Hong
 * @since 06-10-2021
 */
class Mktg {
    private static final int K = 4; // How many characters to sample
    private static final String DEFAULT_CORPUS_PATH = "./corpus.txt";

    /**
     * Main method
     * @param args Program arguments
     */
    public static void main(String[] args) {
        try {
            var table = generateTable(readFile(args.length > 0 ? args[0] : DEFAULT_CORPUS_PATH));
            System.out.println(table.toString()); // Verify our table looks right
            System.out.print("\n\nGenerated Text:\n\n");
            System.out.println(generateText(table, 500)); // Lets get some text
        } catch (IOException err) {
            System.err.println("IOException: " + err);
        }
    }

    /**
     * Return a random context from a table.
     * @param table The table to pick from
     * @return The context randomly selected
     */
    private static String pickRandomCtx(HashMap<String, HashMap<Character, Double>> table) {
        String[] keys = table.keySet().toArray(new String[0]);
        return keys[(int) Math.floor(keys.length * Math.random())];
    }

    /**
     * Return a random character based on the probabilities in the table row.
     * @param row The row to pick from
     * @return The character randomly selected
     */
    private static char pickNextChar(HashMap<Character, Double> row) {
        double sum = 0;
        double rand = Math.random();
        char lastKey = ' ';

        for (var entry : row.entrySet()) {
            sum += entry.getValue();

            if (rand < sum)
                return entry.getKey();

            lastKey = entry.getKey();
        }

        return lastKey;
    }

    /**
     * Sum the values in a table row.
     * Helper method to determine probabilities during table generation.
     * @param row The row to sum
     * @return The sum of the values in the row
     */
    private static double sum(HashMap<Character, Double> row) {
        double sum = 0;

        for (var entry : row.entrySet())
            sum += entry.getValue();

        return sum;
    }

    /**
     * Generate a probability table used in the Markov chain.
     * @param corpus The corpus to "train" from
     * @return The generated table
     */
    private static HashMap<String, HashMap<Character, Double>> generateTable(String corpus) {
        HashMap<String, HashMap<Character, Double>> table = new HashMap<>();

        for (int i = 0; i < corpus.length(); ++i) {
            String sample = corpus.substring(i, i + K);

            if (i + K >= corpus.length())
                break;

            char nextChar = corpus.charAt(i + K);

            table.putIfAbsent(sample, new HashMap<>());

            var probabilities = table.get(sample);

            if (!probabilities.containsKey(nextChar))
                probabilities.put(nextChar, 1.0);
            else {
                double probability = probabilities.get(nextChar);
                probabilities.put(nextChar, ++probability);
            }
        }

        for (var sampleEntry : table.entrySet()) {
            final double s = sum(sampleEntry.getValue());

            for (var probabilityEntry : sampleEntry.getValue().entrySet())
                sampleEntry.getValue().put(probabilityEntry.getKey(), probabilityEntry.getValue() / s);
        }

        return table;
    }

    /**
     * Generate text from the Markov chain probability table provided.
     * @param table The table to generate text from
     * @param n The number of characters to generate
     * @return The generated text
     */
    private static String generateText(HashMap<String, HashMap<Character, Double>> table, int n) {
        String ctx = pickRandomCtx(table);
        StringBuilder output = new StringBuilder(ctx);

        for (int i = 0; i < n; ++i) {
            if (!table.containsKey(ctx))
                output.append(' ');
            else {
                output.append(pickNextChar(table.get(ctx)));
                ctx = output.substring(output.length() - K);
            }
        }

        return output.toString();
    }

    /**
     * Read a file to a string.
     * @param fileName The name of the file to read
     * @return The contents of the file
     */
    private static String readFile(String fileName) throws IOException {
        File f = new File(fileName);
        BufferedReader istream = new BufferedReader(new FileReader(f));
        StringBuilder builder = new StringBuilder();
        String line;

        while ((line = istream.readLine()) != null)
            builder.append(line);

        return builder.toString();
    }
}
