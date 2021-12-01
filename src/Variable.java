import java.util.ArrayList;
import java.util.List;

public class Variable {

    private String name;
    private boolean colored = false;
    private boolean beenSeenFromChild = false;
    private boolean beenSeenFromFather = false;
    private List<String> outcome = new ArrayList();
    private List<String> given = new ArrayList();
    private List<String> childes = new ArrayList();
    private double[][] cpt;
    private Factor factor;

    Variable(String name){
        this.name = name;
    }

    public void addOutcome(String outcome){
        this.outcome.add(outcome);
    }

    public void addGiven(String given){
        this.given.add(given);
    }

    public void addChildes(String child){
        this.childes.add(child);
    }

    public void updateCpt(String values , variablesCollection col){
        int rows = 1;
        for (int j = 0 ; j<this.given.size() ; j++){
            rows = rows*col.getVariable(this.given.get(j)).outcome.size();
        }
        rows = rows*this.outcome.size();
        this.cpt = new double[rows][this.given.size()+2];
        String[] v = values.split(" ");
        int index = 0;
        for (int i=0 ; i<cpt.length ; i++){
            cpt[i][this.given.size()+1] = Double.parseDouble(v[index]);
            index++;
        }
    }

    public String getName() {
        return this.name;
    }

    public List<String> getOutcome() {
        return this.outcome;
    }

    public List<String> getGiven() {
        return this.given;
    }

    public List<String> getChildes() {
        return this.childes;
    }

    public double[][] getCpt() {
        return this.cpt;
    }

    public void setColored(boolean flag){
        this.colored = flag;
    }

    public boolean getColored(){
        return this.colored;
    }

    public boolean getBeenSeenFromChild() {
        return this.beenSeenFromChild;
    }

    public void setBeenSeenFromChild(boolean flag) {
        this.beenSeenFromChild = flag;
    }

    public boolean getBeenSeenFromFather() {
        return this.beenSeenFromFather;
    }

    public void setBeenSeenFromFather(boolean flag) {
        this.beenSeenFromFather = flag;
    }

    public void updateFactor(String values , variablesCollection col){
        int rows = 1;
        for (int j = 0 ; j<this.given.size() ; j++){
            rows = rows*col.getVariable(this.given.get(j)).outcome.size();
        }
        rows = rows*this.outcome.size();
        String[] v = values.split(" ");
        this.factor = new Factor(this.given , v , col , rows , new String(this.name));
    }

    public Factor getFactor() {
        return factor;
    }
}
