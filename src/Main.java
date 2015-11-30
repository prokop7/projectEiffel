import java.io.*;
import java.util.LinkedList;
import java.util.regex.Matcher;

//second execute
public class Main {
    static Node root;       //global root node
    static Node current;

    public static void main(String[] args) throws IOException {
        String path = "C:\\Users\\proko\\IdeaProjects\\hello_world\\src\\";
        BufferedReader reader = new BufferedReader(new FileReader(path + "Main.java"));
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
        if (current.type != Type.CLASS && current.type != Type.NONE && current.type != Type.INTERFACE) {
            printFunction(current, writer);
            for (Node child : current.children) {
                runDFS(child, writer);
            }
            writer.write("\t\tend\n");
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
     * add  .out for integer and other variables
     * TODO change camelCase to camel_case ( key.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase(); )
     */
    private static String applyAllPatterns(String line) {
        line = line
                .replaceAll("System.out.println", "print")  //replace "System.out.println" --> "print"
                .replaceAll("(?<!=)=(?!=)", ":=");          //replace "="                  --> ":="
        if (line.matches("return.*"))                       //replace "return (.*)"        --> result:= .*
            line = line.replaceAll("return\\s+\\(?", "result:= ").replaceAll("\\)\\s*;", ";");
        return line;
    }


    private static void printFunction(Node feature, BufferedWriter writer) throws IOException {
        if (feature.name.compareTo("main") == 0) {
            writer.write("create\n\tmake\n\n");
            writer.write("feature\n\t\t-- Run application");
        }
        writer.write("\n\t" + feature.name.replaceAll("main", "make"));
        if (feature.type != Type.VOID)
            writer.write(" : " + feature.type.toString());
        writer.write("\n\t\tlocal\n\t\t");
        for (Variable var : feature.variables)
            writer.write("\t" + var.type.toString() + " : " + var.name + "\n\t\t");
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

        //Check to null and trim
        if (line == null) return;
        else line = line.trim();

        //create all matches
        Matcher aClass, feature, var;
        aClass = Analyzer.classPattern.matcher(line);
        feature = Analyzer.featurePattern.matcher(line);
        //loop = Analyzer.forPattern.matcher(line);
        var = Analyzer.variableCheck.matcher(line);


        if (aClass.find()) {
            //add an element into Class
            root = new Class(
                    Access.valueOf(aClass.group(1).toUpperCase().trim()), //Access
                    aClass.group(4).replaceAll("Main", "APPLICATION"));   //Name
            current = root;
            convertToTree(reader);
        } else if (feature.find()) {
            //add an element into Feature
            Feature temp = new Feature(
                    Access.valueOf(feature.group(1).toUpperCase().trim()),                              //Access
                    Modifier.valueOf(feature.group(2).toUpperCase().trim()),                            //Modifier
                    Type.valueOf(feature.group(3).toUpperCase().trim().replaceAll("INT", "INTEGER")),   //Type
                    feature.group(5));                                                                  //Name
            root.children.add(temp);
            current = temp;
            convertToTree(reader);
        } else if (line.matches("}")) {
            //find end of the expression
            convertToTree(reader);
        } else if (var.find()) {
            //add Variable
            Variable newVariable = getVariable(line);
            current.variables = addVariables(current.variables, newVariable);
            convertToTree(reader);
        } else {
            //Just a code
            current.children.add(new Node(line));
            convertToTree(reader);
        }
    }

    /**
     * Get variable with all attributes
     *
     * @param line line with variables
     * @return TODO add initialization like " = new ArrayList<>;"
     * TODO variables must be NODE! Not line of code!!
     */
    private static Variable getVariable(String line) {
        Variable variable;
        Matcher varMatcher = Analyzer.variableDeclared.matcher(line);
        if (varMatcher.find()) {
            variable = new Variable(
                    Type.valueOf(varMatcher.group(2).toUpperCase().trim().replaceAll("INT", "INTEGER")),    //Type
                    varMatcher.group(3));                                                                   //Name
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