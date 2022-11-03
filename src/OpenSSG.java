import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;

public class OpenSSG {
    private static final String OPTION_DESCRIPTION = "\nAvailable options:\n[-v | --version]\t\t\t\t\tDisplay program information\n[-h | --help]\t\t\t\t\t\tDisplay how to use options\n[-i | --input <file-or-folder>]\t\tSpecify input file or folder\n[-o | --output <folder-name>]\t\tSpecify output folder. Default is ./dist\n[-l | --lang <language-country>]\tSpecify language to add it to html tag\n[-c | --config <config-file>]\t\tSpecify a json file location that has options";
    private static final String INPUT = "--input";
    private static final String OUTPUT = "--output";
    private static final String LANG = "--lang";
    private static final String STYLESHEET = "--stylesheet";
    public static void main(String[] args) throws IOException {

        if (areArgsValid(args)) {
            switch (args[0]) {
                case "--version", "-v" ->
                        System.out.println("OpenSSG version " + Release.VERSION + ", " + Release.DATE_OF_RELEASE);
                case "--help", "-h" ->
                        System.out.println("usage: " + Release.NAME + " <option>\n" + OPTION_DESCRIPTION);
                case "--config", "-c" -> {
                    System.out.println("Parsing a potential config file.");
                    var optionArgs = createOptionFromFile(args[1]);
                    generateHtmlFiles(optionArgs);
                }
                default -> {
                    var optionArgs = createOptions(args);
                    generateHtmlFiles(optionArgs);
                }
            }
        }
    }

    /**
     * Handle opening of JSON or throwing of error and exiting of program.
     * Read from JSON and assign appropriate properties from it while ignoring the ones that do not exist.
     *
     * @param jsonFN This is the filename that should be taken as an argument when user starts the program under -c or --config
     */
    public static Options createOptionFromFile(String jsonFN) throws FileNotFoundException, UnsupportedEncodingException {
        Options options = new Options();
        JSONParser jsonParser = new JSONParser();
        FileInputStream fs = new FileInputStream(jsonFN);

        try (var fileReader = new InputStreamReader(fs, "UTF-8")) {
            Object jsonObject = jsonParser.parse(fileReader);
            JSONObject configProps = (JSONObject) jsonObject;
            JSONArray styleArray = (JSONArray) configProps.get("stylesheets");

            ArrayList<String> stylesheets = new ArrayList<>();

            if (styleArray != null) {
                for (Object o : styleArray) {
                    stylesheets.add((String) o);
                }
            }

            if (configProps.containsKey("input")) {
                options.setInput((String) configProps.get("input"));
            }
            if (configProps.containsKey("output")) {
                options.setOutput((String) configProps.get("output"));
            }
            if (configProps.containsKey("stylesheets")) {
                options.setStylesheetLinks(stylesheets);
            }
            if (configProps.containsKey("lang")) {
                options.setLanguage((String) configProps.get("lang"));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(2);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(2);
        } catch (ParseException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(2);
        }

        return options;
    }

    public static Options createOptions(String[] args) {
        Options options = new Options();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-i", INPUT -> options.setInput(args[i + 1]);
                case "-o", OUTPUT -> options.setOutput(args[i + 1]);
                case "-l", LANG -> options.setLanguage(args[i + 1]);
                case "-s", STYLESHEET -> {
                    int j = i + 1;
                    ArrayList<String> stylesheetLinks = new ArrayList<>();
                    while (j < args.length && !args[j].startsWith("-")) {
                        stylesheetLinks.add(args[j]);
                        j++;
                    }
                    options.setStylesheetLinks(stylesheetLinks);
                }
                default -> {
                    break;
                }
            }
        }
        return options;
    }

    public static boolean areArgsValid(String[] args) {
        boolean isValid = true;
        String[] basicOptions = {"-v", "--version", "-h", "--help"};
        String[] singleArgOptions = {"-i", INPUT, "-o", OUTPUT, "-l", LANG};
        String[] stylesheetOptions = {"-s", STYLESHEET};

        if (args.length > 0) {
            if (Arrays.asList(basicOptions).contains(args[0])) {
                if (args.length > 1) {
                    System.err.println("Cannot process other option or argument. Check the usage by running java OpenSSG -h or --help.");
                    isValid = false;
                }
            } else if (Arrays.asList(args).contains("-c") || Arrays.asList(args).contains("--config")) {
                isValid = true;
            } else if (!(Arrays.asList(args).contains("-i") || Arrays.asList(args).contains(INPUT))) {
                System.err.println("Input option must be provided with <File> or <Folder> argument. Check the usage by running java OpenSSG -h or --help.");
                isValid = false;
            } else {
                for (int i = 0; i < args.length; i++) {
                    if (Arrays.asList(singleArgOptions).contains(args[i])) {
                        i++;
                        if (i < args.length) {
                            if (args[i].startsWith(("-"))) {
                                System.err.println("Missing option argument. Check out the usage by running java OpenSSG -h or --help.");
                                isValid = false;
                            }
                        } else {
                            System.err.println("Missing option argument. Check out the usage by running java OpenSSG -h or --help.");
                            isValid = false;
                        }
                    } else if (Arrays.asList(stylesheetOptions).contains(args[i])) {
                        i++;
                        if (i < args.length) {
                            while (i < args.length && !args[i].startsWith("-")) {
                                i++;
                            }
                        } else {
                            System.err.println("Missing option argument. Please provide CSS links to add.");
                            isValid = false;
                        }
                    }
                }
            }
        } else {
            System.err.println("Please provide an option. Check usage by running OpenSSG -h or OpenSSG --help.");
            isValid = false;
        }
        return isValid;
    }

    public static void generateHtmlFiles(Options optionArgs) throws IOException {
        FileUtilities fileUtil = new FileUtilities();

        fileUtil.generateHtmlFiles(optionArgs);
    }
}
