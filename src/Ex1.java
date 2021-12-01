import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Ex1 {

    private static int plus=0;
    private static int multiply=0;

    private static void plus(){
        plus++;
    }
    private static void multiply(){
        multiply++;
    }

    private static void setPlusMultiply(){
        plus=0;
        multiply=0;
    }

    private static variablesCollection readXML(String fileName){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(fileName));
            doc.getDocumentElement().normalize();

            NodeList list_var = doc.getElementsByTagName("VARIABLE");
            NodeList list_def = doc.getElementsByTagName("DEFINITION");
            variablesCollection col = new variablesCollection();

            for (int temp = 0; temp < list_var.getLength(); temp++) {
                Node node = list_var.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String name = element.getElementsByTagName("NAME").item(0).getTextContent();
                    Variable x = new Variable(name);
                    NodeList list_out = element.getElementsByTagName("OUTCOME");
                    for (int i = 0; i < list_out.getLength(); i++){
                        String outcome = element.getElementsByTagName("OUTCOME").item(i).getTextContent();
                        x.addOutcome(outcome);
                    }
                    col.addToCollection(x.getName(), x);
                }
            }
            for (int temp = 0; temp < list_def.getLength(); temp++) {
                Node node = list_def.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String for_ = element.getElementsByTagName("FOR").item(0).getTextContent();
                    NodeList list_giv = element.getElementsByTagName("GIVEN");
                    for (int i = 0; i < list_giv.getLength(); i++){
                        String given = element.getElementsByTagName("GIVEN").item(i).getTextContent();
                        col.getVariable(for_).addGiven(given);
                    }
                    String cpt = element.getElementsByTagName("TABLE").item(0).getTextContent();
                    col.getVariable(for_).updateCpt(cpt , col);
                    col.getVariable(for_).updateFactor(cpt , col);
                }
            }
            return col;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return new variablesCollection();
    }

    public static void main(String[] args) {
        try {
            FileWriter myWriter = new FileWriter("output.txt");
            try {
                File Questions = new File("input.txt");
                Scanner myReader = new Scanner(Questions);
                boolean firstLine = true;
                String fileNameXML = "";
                while (myReader.hasNextLine()) {
                    if (firstLine){
                        fileNameXML = myReader.nextLine();
                        firstLine = false;
                    }
                    else{
                        String ans = "";
                        try{
                            String data = myReader.nextLine();
                            variablesCollection col = readXML(fileNameXML);
                            col.setChildes();
                            setPlusMultiply();
                            if (data.charAt(0) == 'P'){
                                factorsCollection facCol = new factorsCollection();
                                col.getCollection().forEach((k,v) -> facCol.addToCollection(v.getFactor()));
                                ans = String.format("%.5f", eliminate_join(data , col , facCol));
                                ans = ans+","+plus+","+multiply;
                            }
                            else{
                                if (bayesBall(data , col)){
                                    ans = "yes";
                                }
                                else{
                                    ans = "no";
                                }

                            }
                        } catch (Exception e) {
                            ans = "";
                        }
                        myWriter.write(ans);
                        if (myReader.hasNextLine()){
                            myWriter.write("\n");
                        }
                    }
                }
                myReader.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            myWriter.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static double eliminate_join(String s, variablesCollection varCol, factorsCollection facCol) {
        // clean data so we could work with
        String[] split_q_hidden = s.split(" ");
        String q = split_q_hidden[0];
        String hidden = "";
        if (split_q_hidden.length>1){
            hidden = split_q_hidden[1];
        }
        q = q.replace("P" , "");
        q = q.replace("(" , "");
        q = q.replace(")" , "");
        String[] q_evidence = q.split("\\|");
        String query = q_evidence[0];
        String[] evidences = new String[0];
        String evidence = "";
        if (q_evidence.length>1){
            evidence = q_evidence[1];
            evidences = evidence.split(",");
        }
        String ques = query.split("=")[0];
        String val = query.split("=")[1];
        // reduce non parents
        String[] evidences1 = new String[0];
        if (q_evidence.length>1){
            evidences1 = new String[evidences.length];
            for(int i=0 ; i<evidences.length ; i++) {
                evidences1[i] = evidences[i].split("=")[0];;
            }
        }
        List<String> parents = relevantParentsNames(ques , evidences1 , varCol , facCol);
        for (String key : varCol.getCollection().keySet()) {
            for(int i=0 ; i<varCol.getVariable(key).getFactor().getParameters().size() ; i++){
                if (!parents.contains(key)){
                    facCol.removeFactorContains(key);
                }
            }
        }

        // reduce independent
        for (String key : varCol.getCollection().keySet()) {
            String checkBayesBall = key+"-"+ques+"|"+evidence;
            if (!evidence.contains(key) && bayesBall(checkBayesBall,varCol)){
                facCol.removeFactorContains(key);
            }
        }
        // reduce evidence
        for(int i=0 ; i<evidences.length ; i++){
            String k = evidences[i].split("=")[0];
            String v = evidences[i].split("=")[1];
            for (int j=0 ; j<facCol.getAllFactors().size() ; j++){
                if (facCol.getAllFactors().get(j).contains(k)){
                    // create a new factor without the hidden column that not from the right value
                    Factor a = new Factor(facCol.getAllFactors().get(j).getParameters() , facCol.getAllFactors().get(j).getRows() , k , v);
                    facCol.getAllFactors().remove(j);
                    facCol.addToCollectionIndex(a,j);
                    if (a.getRows().size()==1){
                        facCol.getAllFactors().remove(j);
                        j--;
                    }
                }
            }
        }

        boolean join = false;
        if (split_q_hidden.length>1){
            String[] hiddenOrder = hidden.split("-");
            // for each hidden, find all the factors that contain hidden
            for (int i=0 ; i<hiddenOrder.length ; i++){
                factorsCollection allHiddenFactors = new factorsCollection();
                for (int j=0 ; j<facCol.getAllFactors().size() ; j++){
                    if (facCol.getFactor(j).contains(hiddenOrder[i])){
                        allHiddenFactors.addToCollection(facCol.getFactor(j));
                        facCol.getAllFactors().remove(j);
                        j--;
                    }
                }
                // if there's only one, eliminate and finish
                if (allHiddenFactors.getAllFactors().size()==1){
                    Factor factor = eliminate(allHiddenFactors.getAllFactors().get(0),hiddenOrder[i]);
                    allHiddenFactors.getAllFactors().remove(0);
                    if (factor.getParameters().size()>0){
                        facCol.addToCollection(factor);
                    }
                }
                // if there are more than one factor, sort by size, join all, and eliminate
                sortFactors(allHiddenFactors.getAllFactors());
                boolean sizeBiggerThanOne = false;
                while (allHiddenFactors.getAllFactors().size() > 1){
                    Factor factor = join(allHiddenFactors.getFactor(0) , allHiddenFactors.getFactor(1));
                    join = true;
                    allHiddenFactors.getAllFactors().remove(0);
                    allHiddenFactors.getAllFactors().remove(0);
                    allHiddenFactors.addToCollectionIndex(factor,0);
                    sizeBiggerThanOne = true;
                }
                if (sizeBiggerThanOne){
                    Factor factor = eliminate(allHiddenFactors.getFactor(0),hiddenOrder[i]);
                    if (factor.getRows().size()>1){
                        facCol.addToCollection(factor);
                    }
                }
            }
        }

        // only relevant factors of query
        sortFactors(facCol.getAllFactors());
        Factor factor = facCol.getFactor(0);
        boolean moreThanOne = false;
        while (facCol.getAllFactors().size() > 1) {
            factor = join(facCol.getFactor(0), facCol.getFactor(1));
            facCol.getAllFactors().remove(0);
            facCol.getAllFactors().remove(0);
            facCol.addToCollectionIndex(factor,0);
            moreThanOne = true;
        }
        if (moreThanOne){
            factor = eliminate(facCol.getFactor(0),"pppppppppppppp");
        }
        // normalization of query
        if (join){
            double sum = factor.getRows().get(0).getP();
            for (int i=1 ; i<factor.getRows().size() ; i++){
                sum += factor.getRows().get(i).getP();
                plus();
            }
            for (int i=0 ; i<factor.getRows().size() ; i++){
                factor.getRows().get(i).setP(factor.getRows().get(i).getP() / sum);
            }
        }

        // find the answer in the last factor and return it
        for (int j=0 ; j<factor.getRows().size() ; j++){
            if (factor.getRows().get(j).getCases().get(ques).equals(val)){
                return factor.getRows().get(j).getP();
            }
        }
        return 0;
    }


    // function for sorting all factors by their size and by their Ascii value
    private static void sortFactors(List<Factor> allFactors) {
        allFactors.sort((o1 , o2) -> {
            Integer i1 = o1.getParameters().size();
            Integer i2 = o2.getParameters().size();
            int comp = i1.compareTo(i2);
            if (comp != 0){
                return comp;
            }
            Integer asciiValue1 = AsciiSum(o1.getParameters());
            Integer asciiValue2 = AsciiSum(o2.getParameters());
            return asciiValue1.compareTo(asciiValue2);
        });

    }
    private static Integer AsciiSum(List<String> parameters) {
        Integer ans = 0;
        for (int i=0 ; i<parameters.size() ; i++){
            for (int j=0 ; j<parameters.get(i).length() ; j++){
                ans += parameters.get(i).charAt(j);
            }
        }
        return ans;
    }

    // the function returns a list that contains all parents of query and evidence (variables that are relevant to the algorithm)
    private static List<String> relevantParentsNames(String query , String[] evidences , variablesCollection varCol , factorsCollection facCol){
        List<String> parents = new ArrayList();
        // add query and evidences to list of parents
        parents.add(query);
        for (int i=0 ; i<evidences.length ; i++){
            if (!parents.contains(evidences[i])){
                parents.add(evidences[i]);
            }
        }
        // add all given of query to list of parents
        for (int i=0 ; i<varCol.getVariable(query).getGiven().size() ; i++){
            if (!parents.contains(varCol.getVariable(query).getGiven().get(i))){
                parents.add(varCol.getVariable(query).getGiven().get(i));
            }
        }
        // add all given of evidences to list of parents
        for (int i=0 ; i<evidences.length ; i++){
            for (int j=0 ; j<varCol.getVariable(evidences[i]).getGiven().size() ; j++){
                if (!parents.contains(varCol.getVariable(evidences[i]).getGiven().get(j))){
                    parents.add(varCol.getVariable(evidences[i]).getGiven().get(j));
                }
            }
        }
        // add all parents of variables in list to list of parents
        int i=0;
        while (i< parents.size()){
            for (int j=0 ; j<varCol.getVariable(parents.get(i)).getGiven().size() ; j++){
                if (!parents.contains(varCol.getVariable(parents.get(i)).getGiven().get(j))){
                    parents.add(varCol.getVariable(parents.get(i)).getGiven().get(j));
                }
            }
            i++;
        }
        return parents;
    }

    // join two given factors
    private static Factor join(Factor a, Factor b) {
        List<factorRow> rows = new ArrayList();
        List<String> bothParameters = new ArrayList();
        // find common variables
        for (int i=0 ; i<a.getParameters().size() ; i++){
            for (int j=0 ; j<b.getParameters().size() ; j++){
               if (a.getParameters().get(i).equals(b.getParameters().get(j))){
                   bothParameters.add(b.getParameters().get(j));
               }
            }
        }
        // multiply each row with the same variables
        for (int i=0 ; i<a.getRows().size() ; i++){
            for (int j=0 ; j<b.getRows().size() ; j++){
                if (a.getRows().get(i).sameParam(b.getRows().get(j) , bothParameters)){
                    rows.add(new factorRow(a.getRows().get(i).getP()*b.getRows().get(j).getP()));
                    multiply();
                    for (int k=0 ; k<bothParameters.size() ; k++) {
                        rows.get(rows.size() - 1).addParam(bothParameters.get(k) , a.getRows().get(i).getCases().get(bothParameters.get(k)));
                    }
                    // add non-common variables that were in the two rows that were joined
                    for (int k=0 ; k<a.getParameters().size() ; k++){
                        if (!bothParameters.contains(a.getParameters().get(k))){
                            rows.get(rows.size() - 1).addParam(a.getParameters().get(k) , a.getRows().get(i).getCases().get(a.getParameters().get(k)));
                        }
                    }
                    for (int k=0 ; k<b.getParameters().size() ; k++){
                        if (!bothParameters.contains(b.getParameters().get(k))){
                            rows.get(rows.size() - 1).addParam(b.getParameters().get(k) , b.getRows().get(j).getCases().get(b.getParameters().get(k)));
                        }
                    }
                }
            }
        }
        // save the parameters for making a new factor
        List<String> parameters = new ArrayList<>();
        if (rows.size()>0){
            for (String key : rows.get(0).getCases().keySet()) {
                parameters.add(key);
            }
        }
        // make a new factor and return it
        Factor ans = new Factor(parameters , rows);
        return ans;
    }

    private static Factor eliminate(Factor a, String hidden) {
        // save all parameters of factor except for the hidden parameter
        List<String> parameters = new ArrayList<>();
        for (int i=0 ; i<a.getParameters().size() ; i++){
            if (!a.getParameters().get(i).equals(hidden)){
                parameters.add(a.getParameters().get(i));
            }
        }
        // delete key and value of hidden from each row and from parameters
        for (int i=0 ; i<a.getRows().size() ; i++){
            a.getRows().get(i).getCases().remove(hidden);
        }
        a.getParameters().remove(hidden);
        // if two rows have the same parameters, attach them to one row
        for (int i=0 ; i<a.getRows().size() ; i++){
            for (int j=i+1 ; j<a.getRows().size() ; j++){
                if (a.getRows().get(i).sameParam(a.getRows().get(j) , a.getParameters())){
                    a.getRows().get(i).setP(a.getRows().get(i).getP()+a.getRows().get(j).getP());
                    plus();
                    a.getRows().remove(j);
                    j -= 1;
                }
            }
        }
        return a;
    }

    private static boolean bayesBall(String s, variablesCollection col) {
        // clean data for working with
        col.unBeenSeenAll();
        col.unColoredAll();
        int index = s.indexOf('|');
        String[] qu = s.split("\\|");
        String firstVab = qu[0].split("-")[0];
        String secondVab = qu[0].split("-")[1];
        Variable first = col.getVariable(firstVab);
        Variable second = col.getVariable(secondVab);
        if (first.getColored() || second.getColored()){
            return true; // there is no route and so they are independent
        }
        String[] parts = s.substring(index+1).split(",");
        // color all evidences
        if (!parts[0].equals("")){
            //there is something not good here!
            for (int i=0 ; i<parts.length ; i++){
                String[] given = parts[i].split("=");
                col.getVariable(given[0]).setColored(true);
            }
        }
        // return true if there is no route, return false if there is a route
        return !route(col , first, second, false , first.getName());
    }

    // recursive loop for finding if there is a route between two variables
    private static boolean route(variablesCollection col , Variable first, Variable second, boolean prevWasFather , String cameFrom) {
        if (first.getName()==second.getName()){
            return true; // there is a route
        }
        if (first.getBeenSeenFromChild() && first.getBeenSeenFromFather()){
            return false; // we saw this variable from two directions which means there is no route
        }
        if ((first.getBeenSeenFromFather() && prevWasFather) || (first.getBeenSeenFromChild() && !prevWasFather)) {
            return false;
        } // color correct "beenSeen" depends on from whom we came from
        if (prevWasFather){
            first.setBeenSeenFromFather(true);
        }
        if (!prevWasFather){
            first.setBeenSeenFromChild(true);
        }
        // look for a route according to the bayes ball rules
        if ((first.getColored() && prevWasFather) || (!first.getColored() && !prevWasFather)){
            for (int i=0 ; i<first.getGiven().size() ; i++){
                if (route(col , col.getVariable(first.getGiven().get(i)) , second , false , first.getName())){
                    return true;
                }
            }
        }
        if (!first.getColored()){
            for (int i=0 ; i<first.getChildes().size() ; i++){
                if (route(col , col.getVariable(first.getChildes().get(i)) , second , true , first.getName())){
                    return true;
                }
            }
        }
        return false;
    }

}
