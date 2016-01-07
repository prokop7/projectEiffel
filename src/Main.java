import java.io.*;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//second execute
public class Main {
    static Node root;       //global root node
    static Node current;
    static Node currentFeature;

    public static void main(String[] args) throws IOException {
        //String path = "C:\\Users\\proko\\IdeaProjects\\hello_world\\src\\";
        BufferedReader reader = new BufferedReader(new FileReader(
                //path +
                "Main.java"));
        BufferedWriter writer = new BufferedWriter(new FileWriter("test.e"));
        convertToTree(reader);
        runDFS(root, writer);
        writer.close();
    }

    /**
     * It's writer JAVA to Eiffel
     *
     * @param current starting node
     * @param writer  BufferedWriter
     * @throws IOException
     */
    private static void runDFS(Node current, BufferedWriter writer) throws IOException {
        if (current.type == Type.CLASS) {
            writer.write(current.type + "\n\t" + current.name.toUpperCase() + "\n\ninherit\n\tARGUMENTS\n\n");
            for (Node child : current.children) {
                runDFS(child, writer);
            }
            writer.write("end");
        }
        if (current.type != Type.CLASS
                && current.type != Type.NONE
                && current.type != Type.INTERFACE
                && current.type != Type.LOOP) {
            printFeatures(current, writer);
            currentFeature = current;
            for (Node child : current.children) {
                runDFS(child, writer);
            }
            writer.write("\t\tend\n");
        }

        if (current.type == Type.LOOP)
            for (Node child : current.children) {
                writer.write("\t\t\t-- Here will be loop");
                writer.write("\n\t");
                runDFS(child, writer);
            }
        if (current.type == Type.NONE && current.name.compareTo("") != 0) {
            String name = applyAllPatterns(current.name);
            writer.write("\t\t\t" + name + "\n");
        }
    }

    /**
     * Function for changing line.
     * All patterns for replace JAVA to Eiffel code is here.
     *
     * @param line string for changing
     * @return updated line
     */
    private static String applyAllPatterns(String line) {
        line = line
                .replaceAll("System.out.println", "print")  //replace "System.out.println" --> "print"
                .replaceAll("(?<!=)=(?!=)", ":=");          //replace "="                  --> ":="
        if (line.matches("\\s*print.*"))
            line = addOutForVariables(line);                //add for variables .out
        else
            line = line.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase(); // from camelCase to Eiffel shit.
        if (line.matches("return.*"))                       //replace "return (.*)"        --> result:= .*
            line = line.replaceAll("return\\s+\\(?", "result:= ").replaceAll("\\)\\s*;", ";");
        return line;
    }

    /**
     * Add for variables .out there where necessary.
     *
     * @param line
     * @return modified line
     */
    private static String addOutForVariables(String line) {
        Matcher matcher = Analyzer.textFromPrint.matcher(line);
        matcher.find();

        String[] temp = matcher.group(1).split("\\s*\\+\\s*");
        for (int i = 0; i < temp.length; i++) {
            if (!temp[i].trim().matches("\".*\""))
                for (Variable var : currentFeature.variables) {
                    if ((var.name.matches(".*\\s?" + temp[i] + ",?\\s?.*") && var.type != Type.STRING) || temp[i].matches("^\\d")) {
                        temp[i] = temp[i].trim().replaceAll(temp[i], temp[i].replaceAll("(.)([A-Z])", "$1_$2").toLowerCase() + ".out");
                        break;
                    }
                }
        }
        String contents = "";
        for (String fromPrint : temp)
            contents += fromPrint + " + ";
        contents = contents.replaceAll("(\\s\\+\\s)$", "");
        line = replaceGroup(matcher, line, 1, contents);
        return line;
    }

    public static String replaceGroup(Matcher m, String source, int groupToReplace, String replacement) {
        //Matcher m = Pattern.compile(regex).matcher(source);
        return new StringBuilder(source).replace(m.start(groupToReplace), m.end(groupToReplace), replacement).toString();
    }

    private static void printFeatures(Node feature, BufferedWriter writer) throws IOException {
        if (feature.name.compareTo("main") == 0) {
            writer.write("create\n\tmake\n\n");
            writer.write("feature\n\t\t-- Run application");
        }
        writer.write("\n\t" + feature.name.replaceAll("main", "make"));
        if (feature.type != Type.VOID)
            writer.write(" : " + feature.type.toString());
        writer.write("\n\t\tlocal\n\t\t");
        for (Variable var : feature.variables)
            writer.write("\t" + var.type.toString() + " : " + var.name.replaceAll("(\\S)([A-Z])", "$1_$2").toLowerCase() + "\n\t\t");
        writer.write("do\n");
    }

    /**
     * @param list linked list of variables which will be added to <code>var</code>
     * @param var  variable for adding
     * @return new linked list with <code>var</code>
     */
    private static LinkedList<Variable> addVariables(LinkedList<Variable> list, Variable var) {
        int count = 0;
        for (Variable item : list) {
            if (item.type == var.type) {
                item.name = item.name + ", " + var.name;
                break;
            }
            count++;
        }
        if (count == list.size())
            list.add(var);
        return list;
    }

    private static void convertToTree(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        while (line != null) {

            line = line.trim();

            //create all matches
            Matcher aClass, feature, var, loop;
            aClass = Analyzer.classPattern.matcher(line);
            feature = Analyzer.featurePattern.matcher(line);
            loop = Analyzer.forPattern.matcher(line);
            var = Analyzer.variableCheck.matcher(line);

            if (aClass.find()) {
                //add an element into Class
                root = new Class(
                        Access.valueOf(aClass.group(1).toUpperCase().trim()), //Access
                        aClass.group(4).replaceAll("Main", "APPLICATION"));   //Name
                current = root;
            } else if (feature.find()) {
                //add an element into Feature
                Feature temp = new Feature(
                        Access.valueOf(feature.group(1).toUpperCase().trim()),                              //Access
                        Modifier.valueOf(feature.group(2).toUpperCase().trim()),                            //Modifier
                        Type.valueOf(feature.group(3).toUpperCase().trim().replaceAll("INT", "INTEGER")),   //Type
                        feature.group(5));                                                                  //Name
                root.children.add(temp);
                temp.parrent = current;
                current = temp;
            } else if (loop.find()) {
                Loop temp = new Loop(
                        loop.group(1),
                        loop.group(2),
                        loop.group(3)
                );
                Variable from = getVariable(loop.group(1) + ";", false);
                addVariables(current.variables, from);
                current.children.add(temp);
                temp.parrent = current;
                current = temp;
            } else if (line.matches("}")) {
                //find end of the expression
                current = current.parrent;
            } else if (var.find()) {
                //add Variable
                Variable newVariable = getVariable(line, true);
                current.variables = addVariables(current.variables, newVariable);
            } else {
                //Just a code
                Node temp = new Node(line);
                current.children.add(temp);
                temp.parrent = current;
            }
            line = reader.readLine();
        }
    }

    /**
     * Get variable with all attributes
     *
     * @param line line with variables
     * @param b
     * @return TODO add initialization like " = new ArrayList<>;"
     * TODO variables must be NODE! Not line of code!! p.s. Now, I don't know WHY!?
     */
    private static Variable getVariable(String line, boolean b) {
        Variable variable;
        Matcher varMatcher = Analyzer.variableDeclared.matcher(line);
        if (varMatcher.find()) {
            variable = new Variable(
                    Type.valueOf(varMatcher.group(2).toUpperCase().trim().replaceAll("INT", "INTEGER")),    //Type
                    varMatcher.group(3));                                                                   //Name
            if (b)
                current.children.add(new Node(line.replaceAll(varMatcher.group(2) + "\\s*", "")));
        } else {
            varMatcher = Analyzer.variableComma.matcher(line);
            varMatcher.find();
            variable = new Variable(
                    Type.valueOf(varMatcher.group(2).toUpperCase().trim().replaceAll("INT", "INTEGER")),    //Type
                    varMatcher.group(3));                                                                   //Name
        }
        return variable;
    }
}