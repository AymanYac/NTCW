package transversal.language_toolbox;

import transversal.dialog_toolbox.ExceptionDialog;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * Transliterates an Unicode string into the specified narrower charset.
 *
 * <p>This class is thread-safe.</p>
 *
 */
public class Unidecode implements Serializable {

    
    private static final String
            PREFIX = "/model/",
            ASCII = PREFIX + "ascii",
            LATIN2 = PREFIX + "latin2";

    /**
     * Placeholder for an unknown character.
     */
    public static final String UNKNOWN_CHAR = "[?]";

    /**
     * Array to cache already loaded character tables.
     */
    private final String[][] cache = new String[256][];

    /**
     * Resource paths where to look for character table files.
     */
    private final String[] tablesLookupPaths;


    protected Unidecode(String[] tablesLookupPaths) {
        this.tablesLookupPaths = tablesLookupPaths;
    }


    /**
     * Creates a new instance of {@code Unidecode} that transliterates Unicode
     * characters to characters that are available in US-ASCII.
     *
     * @see <a href="https://en.wikipedia.org/wiki/ASCII">ASCII</a>
     */
    public static Unidecode toAscii() {
        return new Unidecode(new String[]{ ASCII });
    }

    /**
     * Creates a new instance of {@code Unidecode} that transliterates Unicode
     * characters to characters that are available in ISO 8859-2 (aka Latin-2).
     *
     * @see <a href="https://en.wikipedia.org/wiki/ISO/IEC_8859-2">ISO 8859-2</a>
     */
    public static Unidecode toLatin2() {
        return new Unidecode(new String[]{ LATIN2, ASCII });
    }


    /**
     * Transliterates the given Unicode string into the specified charset, i.e.
     * substitute characters, that are not defined in the target charset, with
     * kind of "similar" (or <tt>[?]</tt> when no replacement is known) from
     * the target charset.
     *
     * <p>Characters from the private area (code point &gt; <tt>0xffff</tt>)
     * are always ignored. Characters from the ASCII area (code point &lt;
     * <tt>0x80</tt>) are always passed without a change. Leading and trailing
     * spaces are removed.</p>
     *
     * <p>This method does not change an actual <i>encoding</i> of the string,
     * it returns a plain {@link String} that is always encoded in UTF-8.</p>
     *
     * @param str The string to transliterate (may be <tt>null</tt>).
     * @return A transliterated string.
     *
     * @see <a href="http://search.cpan.org/~sburke/Text-Unidecode/lib/Text/Unidecode.pm">
     *     Description of the used transliterization method</a>
     */
    public String decodeAndTrim(String str) {
        return decode(str).trim();
    }
    public String decodeTrimLowerCase(String str){
        return decodeAndTrim(str).toLowerCase();
    }
    
    public String decode(String str) {
        if (str == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int codePoint = str.codePointAt(i);

            // basic ASCII, don't change
            if (codePoint < 0x80) {
                sb.append(c);
                continue;
            }
            // characters in the Private Use Area and above are ignored
            if (codePoint > 0xffff) {
                continue;
            }
            sb.append(substituteChar(codePoint));
        }
        return sb.toString();
    }
    /**
     * Transliterate Unicode string to an initials.
     *
     * @param str Unicode string to transliterate.
     * @return String initials.
     */
    public String toInitials(String str) {

        if (str == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();

        Pattern p = Pattern.compile("^\\w|\\s+\\w");
        Matcher m = p.matcher(decodeAndTrim(str));

        while (m.find()) {
            sb.append(m.group().replaceAll(" ", ""));
        }
        return sb.toString();
    }


    /**
     * @param block The first two bytes of the character's code in Unicode,
     *              e.g. <tt>0x5f</tt> for character <tt>U+5f25</tt>.
     * @return URL of the file that contains a mapping for Unicode characters
     *         in the specified block. Line numbers corresponds to the last two
     *         bytes of the character's code in Unicode.
     */
    protected URL resolveCharsTableFile(int block) {

        String fileName = format("X%03x", block);

        for (String path : tablesLookupPaths) {
            URL resource = getClass().getResource(path + '/' + fileName);
            if (resource != null) {
                return resource;
            }
        }
        return null;
    }

    /**
     * @param codePoint Unicode codepoint of the character.
     * @return A substitution (from the target charset) for the given character.
     */
    private String substituteChar(int codePoint) {

        int section = codePoint >> 8;   // Chop off the last two hex digits
        int position = codePoint % 256; // Last two hex digits

        String[] table = getCachedCharsTable(section);
        if (table.length > position) {
            return table[position];
        }
        return UNKNOWN_CHAR;
    }

    private synchronized String[] getCachedCharsTable(int block) {

        if (block < cache.length && cache[block] == null) {
            cache[block] = loadCharsTable(block);
        }
        return cache[block];
    }

    private String[] loadCharsTable(int block) {

        URL file = resolveCharsTableFile(block);

        if (file == null) {
            return unknownCharsTable();
        }
        try (InputStream is = file.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String[] table = new String[256];

            int i = 0;
            String line = null;
            while ((line = reader.readLine()) != null) {
                table[i] = line;
                i++;
            }
            return table;

        } catch (IOException ex) {
        	ExceptionDialog.show("IO 001 permission_denied", "IO 001 permission_denied", "IO 001 permission_denied");
            return unknownCharsTable();
        }
    }

    private static String[] unknownCharsTable() {
        String[] table = new String[256];
        Arrays.fill(table, UNKNOWN_CHAR);

        return table;
    }
}